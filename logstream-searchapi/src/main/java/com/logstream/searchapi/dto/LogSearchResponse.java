package com.logstream.searchapi.dto;

import java.util.List;

public class LogSearchResponse {

    private List<LogResponse> results;
    private long total;
    private int page;
    private int size;

    public LogSearchResponse() {
    }

    public LogSearchResponse(List<LogResponse> results, long total, int page, int size) {
        this.results = results;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<LogResponse> getResults() {
        return results;
    }

    public void setResults(List<LogResponse> results) {
        this.results = results;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
