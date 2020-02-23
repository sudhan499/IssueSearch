package com.madhu.IssueSearch.service;

import com.madhu.IssueSearch.config.AppConfig;
import com.madhu.IssueSearch.model.Issue;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to provide rest client services to Data load controller.
 */
@Service
public class RestClientServiceImpl {

    private static final String PAGE = "page";
    private static final String PER_PAGE = "per_page";
    private static final Logger LOG = LoggerFactory.getLogger(RestClientServiceImpl.class);

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Autowired
    RestTemplate restTemplate;

    private Set<String> ignoreTerms;
    private String storagePath;

    private static String BASE_URL;

    @Autowired
    public RestClientServiceImpl(RestTemplateBuilder restTemplateBuilder, AppConfig config) {
        this.restTemplate = restTemplateBuilder.build();
        this.ignoreTerms = config.getIgnoreTerms();
        this.storagePath = config.getStoragePath();
        this.BASE_URL = config.getBaseURL();
    }

    /*public List<LinkedHashMap<String, String>> loadData1(int pageIndex, int pageSze) {
        LOG.info("Fetching for page {}, with size {}", pageIndex, pageSze);
        List<LinkedHashMap<String, String>> issuesList = restTemplate.getForObject(BASE_URL+"?"+PAGE+"="+pageIndex+"&"+PER_PAGE+"="+pageSze, List.class);
        return issuesList;
    }*/

    /**
     * Loads data from rest api of github url and builds an global index and frequency map.
     * @param pageIndex
     * @param pageSize
     * @param indexMap
     * @param frequencyMap
     * @return
     */
    @Async
    public CompletableFuture<String> loadDataFromRest(int pageIndex, int pageSize,
                                              ConcurrentHashMap<String, List<Pair<Integer, Issue>>> indexMap,
                                              ConcurrentHashMap<String, Integer> frequencyMap) {
        LOG.info("Fetching for page {}, with size {}", pageIndex, pageSize);
        LOG.info("Current Thread : {}",Thread.currentThread().getName());
        String result = "";
        List<LinkedHashMap<String, String>> issuesList = restTemplate.getForObject(BASE_URL+"?"+PAGE+"="+pageIndex+"&"+PER_PAGE+"="+pageSize, List.class);
        LOG.info("Results Fetched successfully, building index...");
        File file = new File(storagePath+"/issues_github"+System.currentTimeMillis()+".txt");
        try {
            if(file.createNewFile()) {
                LOG.info("File created with name [{}] at [{}]", file.getName(), file.getAbsoluteFile());
            } else {
                LOG.info("File already exists with name [{}] at [{}]", file.getName(), file.getAbsoluteFile());
            }
        } catch (IOException e) {
            LOG.error("Failed to create file: {} with exception : {}", file.getName(), e);
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))){
            oos.writeObject(issuesList);
            LOG.info("Data written to file [{}]",file.getAbsolutePath());
        } catch (IOException e) {
            LOG.error("Failed to write to file: {} with exception : {}", file.getName(), e);
            result = "Failed for pageIndex: "+pageIndex+" page size : "+pageSize;
        }
        for(LinkedHashMap<String, String> lhmap : issuesList) {
            Issue issue = new Issue();
            issue.setNumber(Integer.parseInt(String.valueOf(lhmap.get("number"))));
            issue.setUrl(lhmap.get("url"));
            populateIndexMaps(indexMap, frequencyMap, lhmap.get("body"), issue);
        }

        result = "Success.";
        return CompletableFuture.completedFuture(result);
    }

    /**
     * Loads data from already saved files and builds an global index and frequency map.
     * @param indexMap
     * @param frequencyMap
     * @return
     */
    @Async
    public CompletableFuture<String> loadDataFromFile(ConcurrentHashMap<String, List<Pair<Integer, Issue>>> indexMap,
                                                      ConcurrentHashMap<String, Integer> frequencyMap,
                                                      File issueFile) {
        LOG.info("Fetching data from file [{}]", issueFile);
        String result = "";
        LOG.info("Current Thread : {}",Thread.currentThread().getName());
        List<LinkedHashMap<String, String>> issuesList = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(issueFile)))){

            issuesList = (List<LinkedHashMap<String, String>>) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            LOG.error("Failed to read from file: {} with exception : {}", issueFile.getName(), e);
            result = "Failed for file : "+issueFile.getAbsolutePath();
        }

        LOG.info("Results Fetched successfully, building index...");


        for(LinkedHashMap<String, String> lhmap : issuesList) {
            Issue issue = new Issue();
            issue.setNumber(Integer.parseInt(String.valueOf(lhmap.get("number"))));
            issue.setUrl(lhmap.get("url"));
            populateIndexMaps(indexMap, frequencyMap, lhmap.get("body"), issue);
        }

        result = "Success.";
        return CompletableFuture.completedFuture(result);
    }

    /**
     * Populates global index map and frequency map using generated maps inside each thread.
     * @param indexMap
     * @param frequencyMap
     * @param body
     * @param issue
     */
    private void populateIndexMaps(ConcurrentHashMap<String, List<Pair<Integer, Issue>>> indexMap,
                                   ConcurrentHashMap<String, Integer> frequencyMap,
                                   String body,
                                   Issue issue) {
        String[] terms = body.replaceAll("[^a-zA-Z0-9\\s]", " ").replaceAll("\n"," ").replaceAll("\r", " ").split(" ");
        Map<String, Integer> map = new HashMap<>();
        //Populating frequency map and local map
        for(int i=0; i< terms.length;i++) {
            terms[i] = terms[i].trim();
            if(terms[i].isEmpty()) {
                continue;
            }
            if(ignoreTerms.contains(terms[i].toLowerCase())) {
                continue;
            }
            if(frequencyMap.containsKey(terms[i])) {
                frequencyMap.put(terms[i].toLowerCase(), frequencyMap.get(terms[i])+1);
            } else {
                frequencyMap.put(terms[i].toLowerCase(), 1);
            }
            if(map.containsKey(terms[i])) {
                map.put(terms[i].toLowerCase(), map.get(terms[i])+1);
            } else {
                map.put(terms[i].toLowerCase(), 1);
            }
        }

        for(Map.Entry<String, Integer> entry : map.entrySet()) {
            MutablePair<Integer, Issue> pair = new MutablePair();
            pair.setLeft(entry.getValue());
            pair.setRight(issue);
            List<Pair<Integer, Issue>> pairs = indexMap.get(entry.getKey());
            if(pairs == null) {
                pairs = new ArrayList<>();
            }
            pairs.add(pair);
            indexMap.put(entry.getKey(), pairs);
        }

    }
}
