package com.sankholin.comp90049.project2.model;

import com.google.common.collect.ComparisonChain;

import java.util.ArrayList;
import java.util.List;

public class TweetTerm implements Comparable<TweetTerm> {

    private String term;
    private Integer count;
    private List<String> users;

    public TweetTerm(String term) {
        this.term = term;
        users = new ArrayList<>();
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return term;
    }

    @Override
    public int compareTo(TweetTerm t) {
        //return (this.count - t.getCount());
        return ComparisonChain.start()
                .compare(this.users.size(), t.getUsers().size())
                .compare(this.count, t.getCount())
                .result();
    }
}
