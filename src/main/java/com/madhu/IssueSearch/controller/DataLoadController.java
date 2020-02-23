package com.madhu.IssueSearch.controller;

import com.madhu.IssueSearch.config.AppConfig;
import com.madhu.IssueSearch.helper.IndexBuildHelper;
import com.madhu.IssueSearch.service.RestClientServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v1/load-data", consumes = "application/json", produces = "application/json")
public class DataLoadController {

    private static final Logger LOG = LoggerFactory.getLogger(DataLoadController.class);

    private RestClientServiceImpl restClientService;

    private AppConfig config;

    @Autowired
    public DataLoadController(RestClientServiceImpl restClientService, AppConfig config) {
        this.restClientService = restClientService;
        this.config = config;
    }

    /**
     * Fetches data from github and saves the data to file and also builds global index.
     * In case files already exists, index is build using file data.
     */
    @PostMapping(path = "/")
    public void loadData() {
        IndexBuildHelper.loadData(config, restClientService);
    }

}
