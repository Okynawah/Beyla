package com.beyla.query.model;

import java.util.ArrayList;
import java.util.List;

public class CustomSorter {

    public static <T extends CustomComparable<T>> List<T> sort(List<T> list, List<String> context) {
        List<T> sortedList = new ArrayList<>(list);

        for (int i = 1; i < sortedList.size(); i++) {
            T key = sortedList.get(i);
            int j = i - 1;

            while (j >= 0 && sortedList.get(j).compareTo(key, context) > 0) {
                sortedList.set(j + 1, sortedList.get(j));
                j = j - 1;
            }
            sortedList.set(j + 1, key);
        }

        return sortedList;
    }
}

