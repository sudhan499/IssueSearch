package com.madhu.IssueSearch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@ConfigurationProperties
public class AppConfig {
    private int threadPool;
    private Set<String> ignoreTerms;
    private boolean buildOnStart;
    private boolean readFromFile;
    private boolean invalidateOnRestart;
    private String storagePath;
    private String baseURL;
    private String indexPath;
    private int pagesize;
    private int totalpages;

    public int getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(int threadPool) {
        this.threadPool = threadPool;
    }

    public Set<String> getIgnoreTerms() {
        return ignoreTerms;
    }

    public void setIgnoreTerms(Set<String> ignoreTerms) {
        this.ignoreTerms = ignoreTerms;
    }


    public boolean getBuildOnStart() {
        return buildOnStart;
    }

    public void setBuildOnStart(boolean buildOnStart) {
        this.buildOnStart = buildOnStart;
    }

    public boolean getReadFromFile() {
        return readFromFile;
    }

    public void setReadFromFile(boolean readFromFile) {
        this.readFromFile = readFromFile;
    }

    public boolean isInvalidateOnRestart() {
        return invalidateOnRestart;
    }

    public void setInvalidateOnRestart(boolean invalidateOnRestart) {
        this.invalidateOnRestart = invalidateOnRestart;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getIndexPath() {
        return indexPath;
    }

    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }

    public boolean isBuildOnStart() {
        return buildOnStart;
    }

    public boolean isReadFromFile() {
        return readFromFile;
    }

    public int getPagesize() {
        return pagesize;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }

    public int getTotalpages() {
        return totalpages;
    }

    public void setTotalpages(int totalpages) {
        this.totalpages = totalpages;
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                "threadPool=" + threadPool +
                ", ignoreTerms=" + ignoreTerms +
                ", buildOnStart=" + buildOnStart +
                ", readFromFile=" + readFromFile +
                ", invalidateOnRestart=" + invalidateOnRestart +
                ", storagePath='" + storagePath + '\'' +
                ", baseURL='" + baseURL + '\'' +
                ", indexPath='" + indexPath + '\'' +
                ", pagesize='" + pagesize + '\'' +
                ", totalpages='" + totalpages + '\'' +
                '}';
    }
}
