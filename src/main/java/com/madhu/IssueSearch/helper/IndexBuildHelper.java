package com.madhu.IssueSearch.helper;

import com.madhu.IssueSearch.config.AppConfig;
import com.madhu.IssueSearch.model.Issue;
import com.madhu.IssueSearch.service.RestClientServiceImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class IndexBuildHelper {

    private static final Logger LOG = LoggerFactory.getLogger(IndexBuildHelper.class);

    private static ConcurrentHashMap<String, List<Pair<Integer, Issue>>> indexMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> termFrequency = new ConcurrentHashMap<>();
    private static final String INDEX_FILE_NAME = "indexmap.txt";
    private static final String FREQUENCY_FILE_NAME = "frequency.txt";

    /**
     * Loads pre-computed index and frequency from disc.
     * @param config
     * @param restClientService
     */
    public static void checkAndLoadIndexFromFile(AppConfig config, RestClientServiceImpl restClientService) {
        File dirFile = new File(config.getIndexPath());
        if(dirFile.exists() && dirFile.isDirectory()) {
            File indexFile = new File(dirFile.getAbsolutePath()+"/"+INDEX_FILE_NAME);
            File frequencyFile = new File(dirFile.getAbsolutePath()+"/"+FREQUENCY_FILE_NAME);
            if(indexFile.exists() && frequencyFile.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(indexFile)))){
                    indexMap = (ConcurrentHashMap<String, List<Pair<Integer, Issue>>>) ois.readObject();
                    LOG.info("Data read from file [{}]",indexFile.getAbsolutePath());
                } catch (IOException | ClassNotFoundException e) {
                    LOG.error("Failed to read from file: {} with exception : {}", indexFile.getName(), e);
                }

                try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(frequencyFile)))){
                    termFrequency = (ConcurrentHashMap<String, Integer>) ois.readObject();
                    LOG.info("Data read from file [{}]",frequencyFile.getAbsolutePath());
                } catch (IOException | ClassNotFoundException e) {
                    LOG.error("Failed to read from file: {} with exception : {}", frequencyFile.getName(), e);
                }
                return;
            }
        }
        loadData(config, restClientService);
    }

    /**
     * Builds index from File/Rest api based on the configuration.
     * @param config
     * @param restClientService
     */
    public static void loadData(AppConfig config, RestClientServiceImpl restClientService) {
        LOG.info("Clearing index files if they already exists.");
        clearIndexFiles(config);
        LOG.info("Fetching data from github.");
        long start = System.currentTimeMillis();
        List<CompletableFuture<String>> completableFutures = new ArrayList<>();
        if(config.getReadFromFile()) {
            File dirFile = new File(config.getStoragePath());
            if(dirFile.exists() && dirFile.isDirectory()) {
                Iterator<File> files = FileUtils.iterateFiles(new File(config.getStoragePath()), null,false);
                boolean hasFiles = false;
                while(files.hasNext()) {
                    hasFiles = true;
                    File f = files.next();
                    CompletableFuture<String> completableFuture = restClientService.loadDataFromFile(indexMap, termFrequency, f);
                    completableFutures.add(completableFuture);
                }
                //If directory don't have any files by default loads from Rest endpoint.
                if(!hasFiles) {
                    loadDataUsingRest(restClientService, config, completableFutures);
                }
            }
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();

        } else {
            loadDataUsingRest(restClientService, config, completableFutures);
        }


        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();

        saveFrequencyToFile(config);
        saveIndexesToFile(config);
    }

    private static void clearIndexFiles(AppConfig config) {
        File dirFile = new File(config.getIndexPath());
        if(dirFile.exists() && dirFile.isDirectory()) {
            try {
                FileUtils.cleanDirectory(dirFile);
            } catch (IOException e) {
                LOG.error("Failed to clear index directory");
            }
        }
    }

    /**
     * Loads/builds index from rest api.
     * @param restClientService
     * @param config
     * @param completableFutures
     */
    private static void loadDataUsingRest(RestClientServiceImpl restClientService,
                                                                     AppConfig config,
                                                                     List<CompletableFuture<String>> completableFutures) {
        for(int i=0;i<config.getTotalpages();i++) {
            try {
                FileUtils.cleanDirectory(new File(config.getStoragePath()));
            } catch (IOException e) {
                LOG.error("Failed to clean up files under path [{}]", config.getStoragePath());
            }
            CompletableFuture<String> completableFuture = restClientService.loadDataFromRest(i, config.getPagesize(), indexMap, termFrequency);
            completableFutures.add(completableFuture);
        }
    }

    /**
     * Saving indexes to file, to avoid building indexes every time we restart.
     */
    private static void saveIndexesToFile(AppConfig config) {
        File file = new File(config.getIndexPath()+"/"+INDEX_FILE_NAME);
        validateAndCreateFile(file);
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))){
            oos.writeObject(indexMap);
            LOG.info("Data written to file [{}]",file.getAbsolutePath());
        } catch (IOException e) {
            LOG.error("Failed to write to file: {} with exception : {}", file.getName(), e);
        }
    }

    /**
     * Saving term frequency to file, to avoid building indexes every time we restart.
     */
    private static void saveFrequencyToFile(AppConfig config) {
        File file = new File(config.getIndexPath()+"/"+FREQUENCY_FILE_NAME);
        validateAndCreateFile(file);
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))){
            oos.writeObject(termFrequency);
            LOG.info("Data written to file [{}]",file.getAbsolutePath());
        } catch (IOException e) {
            LOG.error("Failed to write to file: {} with exception : {}", file.getName(), e);
        }
    }

    private static void validateAndCreateFile(File file) {
        try {
            if(file.createNewFile()) {
                LOG.info("File created with name [{}] at [{}]", file.getName(), file.getAbsoluteFile());
            } else {
                LOG.info("File already exists with name [{}] at [{}]", file.getName(), file.getAbsoluteFile());
            }
        } catch (IOException e) {
            LOG.error("Failed to create file: {} with exception : {}", file.getName(), e);
        }
    }

    public static ConcurrentHashMap<String, List<Pair<Integer, Issue>>> getIndexMap() {
        return indexMap;
    }

    public static ConcurrentHashMap<String, Integer> getTermFrequency() {
        return termFrequency;
    }

}
