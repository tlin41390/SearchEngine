import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author terencelin
 *
 */
public class QueryProcessor implements Queries {

	/**
	 * datastructure to store the search result
	 */
	private final Map<String, List<InvertedIndex.SearchResult>> searchResult;

	/**
	 * Inverted Index to search for
	 */
	private final InvertedIndex index;

	/**
	 * constructor to construct data structure
	 *
	 * @param invertedIndex the index to use to perform searches
	 */
	public QueryProcessor(InvertedIndex invertedIndex) {
		searchResult = new TreeMap<>();
		index = invertedIndex;
	}

	@Override
	/**
	 * helper method to stem and search each processed line
	 *
	 * @param line  the single line to stem and join.
	 * @param exact checker to see if the ssearch type is exact or not
	 */
	public void processQueries(String line, boolean exact) {
		Set<String> stemmedLine = TextFileStemmer.uniqueStems(line);
		String joined = String.join(" ", stemmedLine);

		if (!stemmedLine.isEmpty() && !searchResult.containsKey(joined)) {
			this.searchResult.put(joined, this.index.search(stemmedLine, exact));
		}

	}

	@Override
	/**
	 * writer to write to json file for the search results
	 *
	 * @param path file to write to
	 * @throws IOException if error in writing json file
	 */
	public void resultsToJson(Path path) throws IOException {
		JsonWriter.writeResults(this.searchResult, path);
	}
}
