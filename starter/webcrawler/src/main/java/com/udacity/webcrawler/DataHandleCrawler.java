package com.udacity.webcrawler;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.time.Clock;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;


/**
 * A class that crawls data from a given URL and its links.
 */
public class DataHandleCrawler extends RecursiveTask<Boolean> {
    private final String url;
    private final int maxDepth;
    private final Instant deadline;
    private final ConcurrentMap<String, Integer> counts;
    private final ConcurrentSkipListSet<String> visitedUrls;
    private final List<Pattern> ignoredUrls;
    private final Clock clock;
    private final PageParserFactory parserFactory;

    public DataHandleCrawler(String url, int maxDepth, Instant deadline, ConcurrentMap<String, Integer> counts,
            ConcurrentSkipListSet<String> visitedUrls, List<Pattern> ignoredUrls, Clock clock,
            PageParserFactory parserFactory) {
        this.url = url;
        this.maxDepth = maxDepth;
        this.deadline = deadline;
        this.counts = counts;
        this.visitedUrls = visitedUrls;
        this.ignoredUrls = ignoredUrls;
        this.clock = clock;
        this.parserFactory = parserFactory;
    }

    /**
     * Computes the crawling task.
     * 
     * @return true if the task was successful, false otherwise.
     */
    @Override
    protected Boolean compute() {
        if (checkStopCrawling()) {
            return false;
        }
        if(!visitedUrls.add(url)) {
            return false;
        }
        PageParser.Result result = parsePage();
        updateWordCounts(result);
        crawlLinks(result);

        return true;
    }

    /**
     * Checks if the crawling task should stop.
     * 
     * @return true if the task should stop, false otherwise.
     */
    private boolean checkStopCrawling() {
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return true;
        }
        if (isUrlIgnored()) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the URL is ignored.
     * 
     * @return true if the URL is ignored, false otherwise.
     */
    private boolean isUrlIgnored() {
        return ignoredUrls.stream().anyMatch(pattern -> pattern.matcher(url).matches());
    }

    /**
     * Parses the page.
     * 
     * @return the result of the page parsing.
     */
    private PageParser.Result parsePage() {
        return parserFactory.get(url).parse();
    }

    /**
     * Updates the word counts.
     * 
     * @param result the result of the page parsing.
     */
    private void updateWordCounts(PageParser.Result result) {
        for (ConcurrentMap.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
            counts.compute(e.getKey(), (k, v) -> (v == null) ? e.getValue() : e.getValue() + v);
        }
    }

    /**
     * Crawls the links.
     * 
     * @param result the result of the page parsing.
     */
    private void crawlLinks(PageParser.Result result) {
        List<DataHandleCrawler> subtasks = new ArrayList<>();
        for (String link : result.getLinks()) {
            subtasks.add(new DataHandleCrawler(link, maxDepth - 1, deadline, counts, visitedUrls, ignoredUrls, clock,
                    parserFactory));
        }
        invokeAll(subtasks);
    }
}
