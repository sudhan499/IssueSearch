package com.madhu.IssueSearch.model;

import java.io.Serializable;

public class Issue implements Serializable {
    private int number;
    private String url;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {

        return "Number: "+number+ " URL : "+url;
    }
}
