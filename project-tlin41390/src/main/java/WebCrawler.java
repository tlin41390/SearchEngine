
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author terencelin
 *
 */
public class WebCrawler {

	/**
	 * the number of stuff to crawl.
	 */
	private volatile int crawlLimit;

	/**
	 * The number of visited links.
	 */
	private final Set<URL> lookup;

	/**
	 * The workqueue used to multithread
	 */
	private final WorkQueue manager;

	/**
	 * The index for building
	 */
	private final ThreadedInvertedIndex index;

	/**
	 * @param manager    the workqueue to use for web crawling
	 * @param index      the index used to build and process the crawler.
	 * @param crawlLimit the total limit for crawling.
	 */
	public WebCrawler(WorkQueue manager, ThreadedInvertedIndex index, int crawlLimit) {
		this.manager = manager;
		this.index = index;
		this.crawlLimit = crawlLimit;
		this.lookup = new HashSet<>();
	}

	/**
	 * the method used to crawl through webpages
	 * 
	 * @param seed the seed url to crawl
	 * @throws URISyntaxException    Exception Occur if there is something wrong
	 *                               with the URI
	 * @throws MalformedURLException If URL is malformed
	 * 
	 */
	public void crawl(URL seed) throws MalformedURLException, URISyntaxException {
		lookup.add(seed);
		manager.execute(new Task(seed));
		manager.finish();
//			}
	}

	/**
	 * @author terencelin
	 *
	 */
	public class Task implements Runnable {
		/**
		 * The webpage to crawl and process.
		 */
		private final URL page;

		/**
		 * @param page the page to crawl.
		 * 
		 */
		public Task(URL page) {
			this.page = page;

		}

		@Override
		public void run() throws UncheckedIOException {
			URL normalizedURL = null;
			try {
				normalizedURL = LinkParser.normalize(this.page);
			} catch (MalformedURLException e) {
				System.out.println("URL is malformed");
			} catch (URISyntaxException e) {
				System.err.println("Error in the URI Syntax Exception");
			}
			String location = normalizedURL.toString();
			InvertedIndex local = new InvertedIndex();
			String html = HtmlFetcher.fetch(location, 3);

			if (html == null) {
				return;
			}

			String strippedHtml = HtmlCleaner.stripBlockElements(html);
			synchronized (lookup) {
				for (URL links : LinkParser.getValidLinks(normalizedURL, strippedHtml)) {
					if (lookup.size() == crawlLimit) {
						break;
					}
					if (!lookup.contains(links)) {
						lookup.add(links);
						manager.execute(new Task(links));
					}
				}
			}

			String cleanedHtml = HtmlCleaner.stripHtml(strippedHtml);
			int count = 1;

			for (String line : TextFileStemmer.listStems(cleanedHtml)) {
				local.add(line, location, count++);
			}
			index.addAll(local);
		}
	}
}