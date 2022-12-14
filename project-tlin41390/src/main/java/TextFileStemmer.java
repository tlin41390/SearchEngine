import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Collection;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Utility class for parsing and stemming text and text files into collections
 * of stemmed words.
 *
 * @see TextParser
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class TextFileStemmer {
	/** The default stemmer algorithm used by this class. */
	public static final SnowballStemmer.ALGORITHM ENGLISH = SnowballStemmer.ALGORITHM.ENGLISH;

	/** The default character set used by this class. */
	public static final Charset UTF8 = StandardCharsets.UTF_8;

	/**
	 *
	 * method to add data to a data structure, whether that would be a set or a
	 * list.
	 *
	 * @param dataStructure the dataStructure specified for data to be added
	 * @param line          the line of words to clean, split, and stem
	 * @param stemmer       the stemmer to use
	 * @return a list of cleaned and stemmed words in parsed order
	 */
	public static Collection<String> addData(Collection<String> dataStructure, String line, Stemmer stemmer) {
		for (String word : TextParser.parse(line)) {
			dataStructure.add(stemmer.stem(word).toString());
		}
		return dataStructure;
	}

	/**
	 * Parses each line into cleaned and stemmed words.
	 *
	 * @param line    the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @return a list of cleaned and stemmed words in parsed order
	 *
	 * @see Stemmer#stem(CharSequence)
	 * @see TextParser#parse(String)
	 */

	public static List<String> listStems(String line, Stemmer stemmer) {
		// create a arraylist containing the stemmed word list.
		List<String> words = new ArrayList<>();
		addData(words, line, stemmer);
		return words;
	}

	/**
	 * Parses each line into cleaned and stemmed words using the default stemmer.
	 *
	 * @param line the line of words to parse and stem
	 * @return a list of cleaned and stemmed words in parsed order
	 *
	 * @see SnowballStemmer
	 * @see #ENGLISH
	 * @see #listStems(String, Stemmer)
	 */
	public static List<String> listStems(String line) {
		return listStems(line, new SnowballStemmer(ENGLISH));
	}

	/**
	 *
	 * parse files to add stemmed words to datastructure.
	 *
	 * @param dataStructure the dataStructure specified for data to be added
	 * @param input         the input file to parse and stem
	 * @return a datastructure of stems from file in parsed order
	 * @throws IOException if unable to read or parse file
	 */
	public static Collection<String> fileParser(Collection<String> dataStructure, Path input) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(input, UTF8)) {
			String line = reader.readLine();
			Stemmer stemmer = new SnowballStemmer(ENGLISH);

			while (line != null) {
				addData(dataStructure, line, stemmer);
				line = reader.readLine();
			}

			return dataStructure;
		}
	}

	/**
	 * Reads a file line by line, parses each line into cleaned and stemmed words
	 * using the default stemmer.
	 *
	 * @param input the input file to parse and stem
	 * @return a list of stems from file in parsed order
	 * @throws IOException if unable to read or parse file
	 *
	 * @see #UTF8
	 * @see #uniqueStems(String, Stemmer)
	 * @see TextParser#parse(String)
	 */

	public static List<String> listStems(Path input) throws IOException {
		List<String> parsedWords = new ArrayList<>();
		fileParser(parsedWords, input);
		return parsedWords;
	}

	/**
	 * Parses the line into unique, sorted, cleaned, and stemmed words.
	 *
	 * @param line    the line of words to parse and stem
	 * @param stemmer the stemmer to use
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see Stemmer#stem(CharSequence)
	 * @see TextParser#parse(String)
	 */
	public static Set<String> uniqueStems(String line, Stemmer stemmer) {
		Set<String> words = new TreeSet<>();
		addData(words, line, stemmer);
		return words;
	}

	/**
	 * Parses the line into unique, sorted, cleaned, and stemmed words using the
	 * default stemmer.
	 *
	 * @param line the line of words to parse and stem
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see SnowballStemmer
	 * @see #ENGLISH
	 * @see #uniqueStems(String, Stemmer)
	 */
	public static Set<String> uniqueStems(String line) {
		return uniqueStems(line, new SnowballStemmer(ENGLISH));
	}

	/**
	 * Reads a file line by line, parses each line into unique, sorted, cleaned, and
	 * stemmed words using the default stemmer.
	 *
	 * @param input the input file to parse and stem
	 * @return a sorted set of unique cleaned and stemmed words from file
	 * @throws IOException if unable to read or parse file
	 *
	 * @see #UTF8
	 * @see #uniqueStems(String, Stemmer)
	 * @see TextParser#parse(String)
	 */
	public static Set<String> uniqueStems(Path input) throws IOException {
		Set<String> words = new TreeSet<>();
		fileParser(words, input);
		return words;
	}

	/**
	 * Reads a file line by line, parses each line into unique, sorted, cleaned, and
	 * stemmed words using the default stemmer, and adds the set of unique sorted
	 * stems to a list per line in the file.
	 *
	 * @param input the input file to parse and stem
	 * @return a list where each item is the set of unique sorted stems parsed from
	 *         a single line of the input file
	 * @throws IOException if unable to read or parse file
	 *
	 * @see #UTF8
	 * @see #ENGLISH
	 * @see #uniqueStems(String, Stemmer)
	 */
	public static List<Set<String>> listUniqueStems(Path input) throws IOException {
		Stemmer stemmer = new SnowballStemmer(ENGLISH);
		List<Set<String>> uniqueValues = new ArrayList<>();

		try (BufferedReader reader = Files.newBufferedReader(input, UTF8)) {
			String line = reader.readLine();
			while (line != null) {
				uniqueValues.add(uniqueStems(line, stemmer));
				line = reader.readLine();
			}

		}
		return uniqueValues;
	}
}
