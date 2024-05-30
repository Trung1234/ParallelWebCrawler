package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
	private final Clock clock;
	  private final Duration timeout;
	  private final int popularWordCount;
	  private final ForkJoinPool pool;
	  private final int maxDepth;
	  private final List<Pattern> ignoredUrls;
	  private final PageParserFactory parserFactory;

	  @Inject
	  ParallelWebCrawler(
	      Clock clock,
	      @Timeout Duration timeout,
	      @PopularWordCount int popularWordCount,
	      @TargetParallelism int threadCount, 
	      @MaxDepth int maxDepth,
	      @IgnoredUrls List<Pattern> ignoredUrls,
	      PageParserFactory parserFactory) {
	    this.clock = clock;
	    this.timeout = timeout;
	    this.popularWordCount = popularWordCount;
	    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
	    this.maxDepth = maxDepth;
	    this.ignoredUrls = ignoredUrls;
	    this.parserFactory = parserFactory;
	  }

	  /**
	   * Computes the crawl result.
	   *
	   * @param startingUrls the URLs to crawl.
	   * @return the {@link CrawlResult}.
	   */
	  @Override
	  public CrawlResult crawl(List<String> startingUrls) {
	      Instant deadline = clock.instant().plus(timeout);
	      ConcurrentMap<String, Integer> counts = new ConcurrentHashMap<>();
	      ConcurrentSkipListSet<String> visitedUrls = new ConcurrentSkipListSet<>();

	      startingUrls.forEach(url -> pool.invoke(new DataCrawler(url, maxDepth, deadline, counts, visitedUrls, ignoredUrls, clock, parserFactory)));

	      Map<String, Integer> wordCounts = counts.isEmpty() ? counts : WordCounts.sort(counts, popularWordCount);

	      return new CrawlResult.Builder()
	          .setWordCounts(wordCounts)
	          .setUrlsVisited(visitedUrls.size())
	          .build();
	  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }
}
