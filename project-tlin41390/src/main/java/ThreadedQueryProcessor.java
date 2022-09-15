import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * multithreaded version of a query processor
 *
 * @author terencelin
 *
 */
public class ThreadedQueryProcessor implements Queries {
	/**
	 * datastructure to store the search result
	 */
	private final Map<String, List<InvertedIndex.SearchResult>> searchResult;

	/**
	 * Inverted Index to search for
	 */
	private final ThreadedInvertedIndex index;

	/**
	 * workqueue member is used for multithreading
	 */
	private final WorkQueue manager;

	/**
	 * lock for the Json Writer.
	 */

	/**
	 * constructor to construct data structure
	 *
	 * @param index   the index to use to perform searches
	 * @param manager the work queue to use to multi thread.
	 */
	public ThreadedQueryProcessor(ThreadedInvertedIndex index, WorkQueue manager) {
		searchResult = new TreeMap<>();
		this.index = index;
		this.manager = manager;
	}

	@Override
	/**
	 * the method to process each line in the query text file.
	 *
	 * @param path  the path to process the queries
	 * @param exact deterimie if search will be exact or not.
	 * @throws IOException the exception to throw if there is a error in processing
	 *                     the file
	 */
	public void processQueries(Path path, boolean exact) throws IOException {
		Queries.super.processQueries(path, exact);
		this.manager.finish();
	}

	@Override
	public void processQueries(String line, boolean exact) {
		manager.execute(new Task(line, exact));
	}

	@Override
	/**
	 * writer to write to json file for the search results
	 *
	 * @param path file to write to
	 * @throws IOException if error in writing json file
	 */
	public void resultsToJson(Path path) throws IOException {
		synchronized (searchResult) {
			JsonWriter.writeResults(this.searchResult, path);
		}
	}

//	public List<InvertedIndex.SearchResult> resultsToStruct() {
//		synchronized(searchResult) {
//			return Collections.unmodifiableMap(this.searchResult); 
//		}
//	}

	/**
	 * task class that each thread will do
	 *
	 * @author terencelin
	 *
	 */
	public class Task implements Runnable {
		/**
		 * The line to pares from
		 */
		private final String line;
		/**
		 * the boolean to see if we are doing exact search or not
		 */
		private final boolean exact;

		/**
		 * @param line  the line to parse from
		 * @param exact the boolean to see if we are doing exact search or not
		 *
		 */
		public Task(String line, boolean exact) {
			this.line = line;
			this.exact = exact;

		}

		@Override
		public void run() {
			Set<String> stemmedLine = TextFileStemmer.uniqueStems(line);
			String joined = String.join(" ", stemmedLine);

			synchronized (searchResult) {
				if (stemmedLine.isEmpty() || searchResult.containsKey(joined)) {
					return;
				}
			}

			var local = index.search(stemmedLine, exact);

			synchronized (searchResult) {
				searchResult.put(joined, local);
			}
		}

	}
}
