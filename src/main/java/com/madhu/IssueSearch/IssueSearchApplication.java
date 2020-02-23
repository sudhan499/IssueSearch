package com.madhu.IssueSearch;

import com.madhu.IssueSearch.config.AppConfig;
import com.madhu.IssueSearch.helper.IndexBuildHelper;
import com.madhu.IssueSearch.service.RestClientServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IssueSearchApplication implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(IssueSearchApplication.class);

	private RestClientServiceImpl restClientService;
	private AppConfig config;

	@Autowired
	public IssueSearchApplication(RestClientServiceImpl restClientService, AppConfig config) {
		this.restClientService = restClientService;
		this.config = config;
	}
	public static void main(String[] args) {

		SpringApplication.run(IssueSearchApplication.class, args);
	}

	/**
	 * Fetches data from github and saves the data to file and also builds global index.
	 * In case files already exists, index is build using file data.
	 * @param args
	 * @throws Exception
	 */
	@Override
	public void run(String... args) throws Exception {
		if(!config.isInvalidateOnRestart()) {
			IndexBuildHelper.checkAndLoadIndexFromFile(config, restClientService);
		} else {
			if(config.getBuildOnStart()) {
				IndexBuildHelper.loadData(config, restClientService);
			}
		}


	}
}
