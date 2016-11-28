package com.sankholin.comp90049.project2.model;

import com.google.common.collect.ComparisonChain;

public class PartedTweetTerm implements Comparable<PartedTweetTerm> {

    private String term;
    private Integer count;
    private Integer userCount;

    public PartedTweetTerm(String term) {
        this.term = term;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    @Override
    public String toString() {
        return term;
    }

    @Override
    public int compareTo(PartedTweetTerm t) {
        return ComparisonChain.start()
                //.compare(this.userCount, t.getUserCount())
                .compare(this.count, t.getCount())
                .result();
    }
}
