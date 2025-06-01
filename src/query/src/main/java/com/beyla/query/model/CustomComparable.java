package com.beyla.query.model;

import java.util.List;

public interface CustomComparable<T> {
    public void setScore(Integer score);
    public int compareTo(T object, List<String> context);
    public Integer getScore();
}
