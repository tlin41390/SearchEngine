import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * An index to store locations and the words found at those locations. Makes no
 * assumption about order or duplicates.
 *
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class InvertedIndex {
	/**
	 * datastructure to store the index.
	 */
	private final TreeMap<String, TreeMap<String, Collection<Integer>>> map;
	/**
	 * datastructure to store word count.
	 */
	private final Map<String, Integer> wordCount;

	/**
	 * constructor to construct the datastructure.
	 */
	public InvertedIndex() {
		this.map = new TreeMap<>();
		this.wordCount = new TreeMap<>();
	}

	/**
	 * Adds the location and word, and the position in file.
	 *
	 * @param word     the word found
	 * @param location the location the word was found
	 * @param count    the number/position where the word is located
	 */
	public void add(String word, String location, Integer count) {
		this.map.putIfAbsent(word, new TreeMap<>());
		this.map.get(word).putIfAbsent(location, new TreeSet<>());
		this.map.get(word).get(location).add(count);
		int oldCount = wordCount.getOrDefault(location, 0);
		this.wordCount.put(location, Math.max(oldCount, count));
	}

	/**
	 * Add function to add a list of words to the inverted index
	 *
	 * @param location the location the word was found
	 * @param words    the list of words.
	 * @param count    the number where the position of the words were found
	 */
	public void add(String location, List<String> words, int count) {
		for (String word : words) {
			add(word, location, count++);
		}
	}

	/**
	 * merge one inverted index with another.
	 *
	 * @param other the other index to copy
	 */
	public void addAll(InvertedIndex other) {
		for (String key : other.map.keySet()) {
			if (this.map.containsKey(key)) {
				for (String locations : other.map.get(key).keySet()) {
					if (this.map.get(key).containsKey(locations)) {
						this.map.get(key).get(locations).addAll(other.map.get(key).get(locations));
					} else {
						this.map.get(key).put(locations, other.map.get(key).get(locations));
					}
				}
			} else {
				this.map.put(key, other.map.get(key));
			}
		}

		for (String location : other.wordCount.keySet()) {
			if (other.getLocationCount(location) > this.getLocationCount(location)) {
				this.wordCount.put(location, other.wordCount.get(location));
			}
		}
	}

	/**
	 * method to get the total words from a particular file path.
	 *
	 * @param location get the wordcount for the location.
	 * @return the total wordcount for that specific path.
	 */
	public Integer getLocationCount(String location) {
		return this.wordCount.getOrDefault(location, 0);
	}
	
	/**
	 * @return the unmodifiable map of a hashmap for the word count.
	 */
	public Map<String,Integer> getFiles(){
		return Collections.unmodifiableMap(this.wordCount);
	}

	/**
	 *
	 * method to check if the file is in the wordCount data structure.
	 *
	 * @param file the file to check if is in data structure
	 * @return t/f whether or not the file is in the structure or not.
	 */
	public boolean containsLocation(String file) {
		return file != null && this.wordCount.containsKey(file);
	}

	/**
	 * Returns the number of words in the index.
	 *
	 * @return the total number of words.
	 */
	public int size() {
		return this.map.keySet().size();
	}

	/**
	 * Returns the number of files containing the word.
	 *
	 * @param word the words in included in the files.
	 * @return 0 if the location is not in the index or has no words, otherwise the
	 *         number of words stored for that element
	 */
	public int size(String word) {
		return contains(word) ? this.map.get(word).size() : 0;
	}

	/**
	 * returns the number of times the word is in the location
	 *
	 * @param word     the words that are included in the files.
	 * @param location the specified path.
	 * @return 0 if the word is not in the file.
	 */
	public int size(String word, String location) {
		return contains(word, location) ? this.map.get(word).get(location).size() : 0;
	}

	/**
	 * Determines whether the word is stored in the index.
	 *
	 * @param word the location to lookup
	 * @return {@true} if the location is stored in the index
	 */
	public boolean contains(String word) {
		return this.map.containsKey(word);
	}

	/**
	 * determines if the word is in the location.
	 *
	 * @param word     the location to look up
	 * @param location the file path specified to find.
	 * @return boolean for if the index contains the location
	 */
	public boolean contains(String word, String location) {
		return this.map.get(word) != null && this.map.get(word).containsKey(location);
	}

	/**
	 * determines if the list is in the inverted index.
	 *
	 * @param word     the word being referenced
	 * @param location the file location
	 * @param position the integer for the exact position where the word is found
	 * @return boolean for if the index has a position list.
	 */
	public boolean contains(String word, String location, Integer position) {
		return contains(word, location) && this.map.get(word).get(location).contains(position);
	}

	/**
	 * getter for the word key set.
	 *
	 * @return the keys/strings for the index.
	 */
	public Collection<String> get() {
		if (map.keySet() != null) {
			return Collections.unmodifiableCollection(map.keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * Returns an unmodifiable view of the locations stored in the index.
	 *
	 * @param word the word referenced to return a list of paths.
	 * @return an unmodifiable view of the locations stored in the index
	 * @see Collections#unmodifiableCollection(Collection)
	 */
	public Collection<String> get(String word) {
		if (contains(word)) {
			return Collections.unmodifiableCollection(map.get(word).keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * Returns an unmodifiable view of the words stored in the index for the
	 * provided location, or an empty collection if the location is not in the
	 * index.
	 *
	 * @param word     the word that is being referenced
	 * @param location the location to lookup
	 * @return an unmodifiable view of the words stored for the location
	 * @see Collections#unmodifiableCollection(Collection)
	 */
	public Collection<Integer> get(String word, String location) {
		if (contains(word, location)) {
			return Collections.unmodifiableCollection(this.map.get(word).get(location));
		}
		return Collections.emptySet();
	}

	@Override
	public String toString() {
		return this.map.toString();
	}

	/**
	 * method to write the contents of the map to a JsonFile.
	 *
	 * @param path the file to write to
	 * @throws IOException the exception thrown when there is no file to write.
	 */
	public void toJson(Path path) throws IOException {
		JsonWriter.writeIndex(this.map, path);
	}

	/**
	 *
	 * json writer to write the word count
	 *
	 * @param path the path to write to
	 * @throws IOException throw IO Exception if there is a problem building the
	 *                     word count
	 */
	public void wordCountJson(Path path) throws IOException {
		JsonWriter.asObject(this.wordCount, path);
	}

	/**
	 * the method to determine whether to use exact or partial search.
	 *
	 * @param queries the set of queries to process
	 * @param exact   boolean to determine whether or not to use exact or partial
	 *                search
	 * @return the list of results from searching
	 */
	public List<SearchResult> search(Set<String> queries, boolean exact) {
		return exact ? exactSearch(queries) : partialSearch(queries);
	}

	/**
	 * method to perform exact search on inverted index
	 *
	 * @param queries the set of queries to look.
	 * @return a list of search results
	 */
	public List<SearchResult> exactSearch(Set<String> queries) {
		List<SearchResult> results = new ArrayList<>();
		Map<String, SearchResult> lookup = new HashMap<>();
		for (String query : queries) {
			buildResults(query, results, lookup);
		}
		Collections.sort(results);
		return results;
	}

	/**
	 * private helper method to build the results
	 *
	 * @param query   the query to look for
	 * @param results the structure to build the results
	 * @param lookup  the lookup map for reference
	 */
	private void buildResults(String query, List<SearchResult> results, Map<String, SearchResult> lookup) {
		if (this.map.containsKey(query)) {
			for (String files : this.map.get(query).keySet()) {
				if (!lookup.containsKey(files)) {
					SearchResult result = new SearchResult(files);
					results.add(result);
					lookup.put(files, result);
				}
				lookup.get(files).updateQueryCount(query);
			}
		}

	}

	/**
	 * method to perform partial search on the queries
	 *
	 * @param queries the stemmed queries to look
	 * @return the list of results found.
	 */
	public List<SearchResult> partialSearch(Set<String> queries) {
		List<SearchResult> results = new ArrayList<>();
		Map<String, SearchResult> lookup = new HashMap<>();
		for (String query : queries) {
			for (String partial : map.tailMap(query).keySet()) {
				if (!partial.startsWith(query)) {
					break;
				}
				buildResults(partial, results, lookup);
			}
		}
		Collections.sort(results);
		return results;

	}

	/**
	 * class will be the searchresults with the members and comparable
	 *
	 * @author terencelin
	 *
	 */
	public class SearchResult implements Comparable<SearchResult> {

		/** the text file source/location. */
		private final String source;

		/** total count of queries */
		private int totalQueryCount;

		/** the total score for the double */
		private double score;

		/**
		 *
		 * the search result object to be created
		 *
		 * @param source the natural path of the file
		 */

		public SearchResult(String source) {
			this.source = source;
			this.totalQueryCount = 0;
			this.score = 0.0;
		}

		/**
		 * method to update the query count whenever there is a match or when we need to
		 * initialze search result
		 *
		 * @param query the query word to update the search result
		 */
		private void updateQueryCount(String query) {
			this.totalQueryCount += map.get(query).get(source).size();
			this.score = (double) this.totalQueryCount / (double) wordCount.get(source);
		}

		/**
		 * get the total query count
		 *
		 * @return total number of querys
		 */
		public int getTotalQueryCount() {
			return this.totalQueryCount;
		}

		/**
		 * getter for the score
		 *
		 * @return the value of the score
		 */
		public double getScore() {
			return this.score;
		}

		/**
		 * getter for the source of the search result
		 *
		 * @return the source to return.
		 */

		public String getSource() {
			return this.source;
		}

		@Override
		public int compareTo(SearchResult o) {

			int result = Double.compare(o.score, this.score);

			if (result == 0) {
				result = Integer.compare(o.totalQueryCount, this.totalQueryCount);

				if (result == 0) {
					result = this.getSource().compareToIgnoreCase(o.getSource());
				}
			}

			return result;
		}

	}

}
