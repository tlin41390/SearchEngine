import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Terence Lin
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class Driver {
	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		ArgumentMap map = new ArgumentMap(args);
		InvertedIndex index = null;
		ThreadedInvertedIndex safe = null;
		Queries query = null;
		WorkQueue manager = null;
		WebCrawler crawler = null;
		URL seed = null;
		Integer defaultThread = 5;

		if (map.hasFlag("-threads") || map.hasFlag("-html")) {
			Integer totalThreads = map.getInteger("-threads", defaultThread);
			if (totalThreads <= 0) {
				totalThreads = 5;
			}
			manager = new WorkQueue(totalThreads);
			safe = new ThreadedInvertedIndex();
			query = new ThreadedQueryProcessor(safe, manager);
			index = safe;

		} else {
			index = new InvertedIndex();
			query = new QueryProcessor(index);
		}
		if (map.hasFlag("-html")) {
			int crawlLimit = 1;
			try {
				if (map.hasFlag("-max")) {
					crawlLimit = map.getInteger("-max", 1);
				}

				seed = new URL(map.getString("-html"));
				crawler = new WebCrawler(manager, safe, crawlLimit);
				crawler.crawl(seed);

			} catch (MalformedURLException e) {
				System.err.println("Cannot crawl the web with the given URL");
			} catch (URISyntaxException e) {
				System.err.println("Error with the URL Syntax");
			}
		}
		if (map.hasFlag("-server")) {
			int PORT = map.getInteger("-server", 8080);
			SearchServer server = new SearchServer(safe);
			try {
				server.searchServers(PORT,crawler,seed);
			} catch (Exception e) {
							
			}

		}

		if (map.hasFlag("-text")) {
			Path path = map.getPath("-text");
			try {
				if (safe != null && manager != null) {
					ThreadedInvertedIndexProcessor.processFiles(path, safe, manager);
				} else {
					InvertedIndexProcessor.processFiles(path, index);
				}

			} catch (IOException e) {
				System.err.println("Unable to build the inverted index from -text " + path.toString());
			} catch (NullPointerException e) {
				System.err.println("Missing a required path value for the -text flag.");
			}
		}

		if (map.hasFlag("-query")) {
			Path path = map.getPath("-query");
			try {
				query.processQueries(path, map.hasFlag("-exact"));

			} catch (IOException e) {
				System.err.println("Unable to parse queries from: " + path.toString());
			} catch (NullPointerException e) {
				System.err.println("Cannot parse queries because query file is null.");
			}
		}

		if (map.hasFlag("-index")) {
			Path path = map.getPath("-index", Path.of("index.json"));

			try {
				index.toJson(path);
			} catch (IOException e) {
				System.err.println("Unable to write inverted index to: " + path.toString());
			}
		}

		if (map.hasFlag("-counts")) {
			Path path = map.getPath("-counts", Path.of("count.json"));

			try {
				index.wordCountJson(path);
			} catch (IOException e) {
				System.err.println("Unable to parse word count to: " + path.toString());
			}
		}

		if (map.hasFlag("-results")) {
			Path resultsPath = map.getPath("-results", Path.of("results.json"));

			try {
				query.resultsToJson(resultsPath);
			} catch (IOException e) {
				System.err.println("Unable to display results.");
			}
		}
		if (manager != null) {
			manager.shutdown();
		}
	}
}