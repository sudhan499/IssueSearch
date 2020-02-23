package com.madhu.IssueSearch.controller;

import com.madhu.IssueSearch.config.AppConfig;
import com.madhu.IssueSearch.helper.IndexBuildHelper;
import com.madhu.IssueSearch.model.Issue;
import com.madhu.IssueSearch.service.SearchServiceImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping(path = "/v1/search-issues", consumes = "application/json", produces = "application/json")
public class SearchController {
    private static final Logger LOG = LoggerFactory.getLogger(SearchController.class);

    private SearchServiceImpl searchService;

    private AppConfig config;

    @Autowired
    public SearchController(SearchServiceImpl searchService, AppConfig config) {
        this.searchService = searchService;
        this.config = config;
    }

    /**
     * Rest API to search for keyword(s) from the issues description(body).
     * @param keywords
     * @return
     */
    @GetMapping("/{keywords}")
    @ResponseBody
    public ResponseEntity<List<String>> searchIssues1(@PathVariable("keywords") String keywords) {
        List<String> urls = new ArrayList<>();
        HttpStatus status = null;
        ConcurrentHashMap<String, List<Pair<Integer, Issue>>> indexMap =  IndexBuildHelper.getIndexMap();
        ConcurrentHashMap<String, Integer> frequencyMap = IndexBuildHelper.getTermFrequency();
        status = HttpStatus.OK;
        String[] karray = keywords.trim().replaceAll("[^a-zA-Z0-9\\s]", " ").replaceAll("\n"," ").replaceAll("\r", " ").toLowerCase().split(" ");

        if(indexMap == null || indexMap.isEmpty()) {
            urls.add("Data is not indexed, please use \"/v1/load-data/\" to index the data.");
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        if(karray.length == 1) {
            //Singe keyword search.
            urls = searchService.getMatchingIssues(karray[0], indexMap);
            return new ResponseEntity<>(urls, status);
        }
        //Multi keyword search.
        for(int i=0;i<karray.length;i++) {
            karray[i] = karray[i].trim();
        }
        urls = searchService.getMatchingIssues(karray, indexMap, frequencyMap);
        return new ResponseEntity<>(urls, status);
    }
}
