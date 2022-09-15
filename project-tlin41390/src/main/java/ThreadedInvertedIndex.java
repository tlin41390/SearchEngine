import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An index to store locations and the words found at those locations. Makes no
 * assumption about order or duplicates.
 *
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class ThreadedInvertedIndex extends InvertedIndex {
	/**
	 * lock for threadsafe inverted index.
	 */
	private final SimpleReadWriteLock lock;

	/**
	 * constructor to construct the datastructure.
	 */
	public ThreadedInvertedIndex() {
		super();
		lock = new SimpleReadWriteLock();
	}

	@Override
	/**
	 * Adds the location and word, and the position in file.
	 *
	 * @param word     the word found
	 * @param location the location the word was found
	 * @param count    the number/position where the word is located
	 */
	public void add(String word, String location, Integer count) {
		lock.writeLock().lock();
		try {
			super.add(word, location, count);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	/**
	 * Add function to add a list of words to the inverted index
	 *
	 * @param location the location the word was found
	 * @param words    the list of words.
	 * @param count    the number where the position of the words were found
	 */
	public void add(String location, List<String> words, int count) {
		lock.writeLock().lock();
		try {
			super.add(location, words, count);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	/**
	 * merge one inverted index with another.
	 *
	 * @param other the other index to copy
	 */
	public void addAll(InvertedIndex other) {
		lock.writeLock().lock();
		try {
			super.addAll(other);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	/**
	 * method to get the total words from a particular file path.
	 *
	 * @param location get the wordcount for the location.
	 * @return the total wordcount for that specific path.
	 */
	public Integer getLocationCount(String location) {
		lock.readLock().lock();
		try {
			return super.getLocationCount(location);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	@Override
	/**
	 * @return the unmodifiable map of a hashmap for the word count.
	 */
	public Map<String,Integer> getFiles(){
		lock.readLock().lock();
		try {
			return super.getFiles();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	/**
	 *
	 * method to check if the file is in the wordCount data structure.
	 *
	 * @param file the file to check if is in data structure
	 * @return t/f whether or not the file is in the structure or not.
	 */
	public boolean containsLocation(String file) {
		lock.readLock().lock();
		try {
			return super.contains(file);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	/**
	 * Returns the number of words in the index.
	 *
	 * @return the total number of words.
	 */
	public int size() {
		lock.readLock().lock();
		try {
			return super.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	/**
	 * Returns the number of files containing the word.
	 *
	 * @param word the words in included in the files.
	 * @return 0 if the location is not in the index or has no words, otherwise the
	 *         number of words stored for that element
	 */
	public int size(String word) {
		lock.readLock().lock();
		try {
			return super.size(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	/**
	 * returns the number of times the word is in the location
	 *
	 * @param word     the words that are included in the files.
	 * @param location the specified path.
	 * @return 0 if the word is not in the file.
	 */
	public int size(String word, String location) {
		lock.readLock().lock();
		try {
			return super.size(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	/**
	 * Determines whether the word is stored in the index.
	 *
	 * @param word the location to lookup
	 * @return {@true} if the location is stored in the index
	 */
	public boolean contains(String word) {
		lock.readLock().lock();
		try {
			return super.contains(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	/**
	 * determines if the word is in the location.
	 *
	 * @param word     the location to look up
	 * @param location the file path specified to find.
	 * @return boolean for if the index contains the location
	 */
	public boolean contains(String word, String location) {
		lock.readLock().lock();
		try {
			return super.contains(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	/**
	 * determines if the list is in the inverted index.
	 *
	 * @param word     the word being referenced
	 * @param location the file location
	 * @param position the integer for the exact position where the word is found
	 * @return boolean for if the index has a position list.
	 */
	public boolean contains(String word, String location, Integer position) {
		lock.readLock().lock();
		try {
			return super.contains(location, location, position);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	/**
	 * getter for the word key set.
	 *
	 * @return the keys/strings for the index.
	 */
	public Collection<String> get() {
		lock.readLock().lock();
		try {
			return super.get();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	/**
	 * Returns an unmodifiable view of the locations stored in the index.
	 *
	 * @param word the word referenced to return a list of paths.
	 * @return an unmodifiable view of the locations stored in the index
	 */
	public Collection<String> get(String word) {
		lock.readLock().lock();
		try {
			return super.get(word);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	/**
	 * Returns an unmodifiable view of the words stored in the index for the
	 * provided location, or an empty collection if the location is not in the
	 * index.
	 *
	 * @param word     the word that is being referenced
	 * @param location the location to lookup
	 * @return an unmodifiable view of the words stored for the location
	 */
	public Collection<Integer> get(String word, String location) {
		lock.readLock().lock();
		try {
			return super.get(word, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			return super.toString();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	/**
	 * method to write the contents of the map to a JsonFile.
	 *
	 * @param path the file to write to
	 * @throws IOException the exception thrown when there is no file to write.
	 */
	public void toJson(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.toJson(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	/**
	 *
	 * @param path the path to write to
	 * @throws IOException throw IO Exception if there is a problem building the
	 *                     word count
	 */
	public void wordCountJson(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.wordCountJson(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	/**
	 * method to perform exact search on inverted index
	 *
	 * @param queries the set of queries to look for
	 * @return a list of search results
	 */
	public List<InvertedIndex.SearchResult> exactSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.exactSearch(queries);
		} finally {
			lock.readLock().unlock();
		}

	}

	@Override
	/**
	 * @param queries the stemmed queries to look
	 * @return the list of results found.
	 */
	public List<InvertedIndex.SearchResult> partialSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			return super.partialSearch(queries);
		} finally {
			lock.readLock().unlock();
		}

	}
}
