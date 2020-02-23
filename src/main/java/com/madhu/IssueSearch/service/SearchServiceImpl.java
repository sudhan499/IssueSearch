package com.madhu.IssueSearch.service;

import com.madhu.IssueSearch.config.AppConfig;
import com.madhu.IssueSearch.model.Issue;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SearchServiceImpl {

    private AppConfig config;

    public SearchServiceImpl(AppConfig config) {
        this.config = config;
    }

    /**
     * Gets the matching issues urls by sorting in descending order of the frequency of the keyword occurrences in the issue.
     * @param keyword
     * @param indexMap
     * @return
     */
    public Set<String> getMatchingIssues(String keyword, Map<String, List<Pair<Integer, Issue>>> indexMap) {
        Set<String> urls = new HashSet<>();
        if(indexMap.containsKey(keyword)) {
            List<Pair<Integer, Issue>> pairs = indexMap.get(keyword);
            Collections.sort(pairs, new Comparator<Pair<Integer, Issue>>() {
                @Override
                public int compare(Pair<Integer, Issue> integerIssuePair, Pair<Integer, Issue> t1) {
                    return t1.getLeft().compareTo(integerIssuePair.getLeft());
                }
            });
            pairs.stream().forEach(p -> urls.add(p.getRight().getUrl()));
        }
        return urls;
    }

    /**
     * Gets the matching issues urls by sorting in descending order of the frequency of the keyword occurrences among all issues.
     * @param keywords
     * @param indexMap
     * @return
     */
    public Set<String> getMatchingIssues(String[] keywords, Map<String, List<Pair<Integer, Issue>>> indexMap, Map<String, Integer> frequencyMap) {
        Set<String> urls = new HashSet<>();
        //1.Array of occurrences of keywords
        Map<String, Integer> ranksMap = new HashMap<>();
        Map<String, Integer> issuesRank = new HashMap<>();
        int[] o = new int[keywords.length];
        for(int i=1;i<keywords.length;i++) {
            o[i] = frequencyMap.get(keywords[i]);
        }
        //Finding rank for keywords occurrences.
        for(int i=0;i<o.length;i++) {
            int rank = 1;
            for(int j=0;j<o.length;j++) {
                if(o[i] > o[j]) {
                    rank++;
                }
            }

            ranksMap.put(keywords[i], rank);
            rank = 1;
        }
        // finding rank for issues by multiplying the keywords frequency in issue to the rank of the keyword.
        AtomicInteger index = new AtomicInteger();
        index.addAndGet(0);
        for(int i=0;i<keywords.length;i++) {

            List<Pair<Integer, Issue>> pairs = indexMap.get(keywords[i]);
            pairs.stream().forEach(p -> {
                String url = p.getRight().getUrl();
                if(issuesRank.containsKey(url)) {
                    issuesRank.put(url, issuesRank.get(url)+(frequencyMap.get(keywords[index.get()])*ranksMap.get(keywords[index.get()])));
                } else {
                    issuesRank.put(url, frequencyMap.get(keywords[index.get()])*ranksMap.get(keywords[index.get()]));
                }
            });
            index.incrementAndGet();
        }

        //Sorting the map based on rank of issues in ascending order(less rank more popular).
        List<Map.Entry<String, Integer>> list = new ArrayList<>(issuesRank.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> t1, Map.Entry<String, Integer> t2) {
                return t1.getValue().compareTo(t2.getValue());
            }
        });

        list.stream().forEach(s -> urls.add(s.getKey()));

        return urls;
    }



}
