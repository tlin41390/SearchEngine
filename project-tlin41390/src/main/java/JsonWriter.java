import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using tabs.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class JsonWriter {

	/**
	 * helper function for writing to an array
	 *
	 * @param iterator number iterator to use
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeArray(Iterator<Integer> iterator, Writer writer, int level) throws IOException {
		writer.write("\n");
		indent(iterator.next().toString(), writer, level + 1);
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asArray(Collection<Integer> elements, Writer writer, int level) throws IOException {
		writer.write("[");
		var iterator = elements.iterator();
		if (iterator.hasNext()) {
			writeArray(iterator, writer, level);
		}
		while (iterator.hasNext()) {
			writer.write(",");
			writeArray(iterator, writer, level);
		}
		writer.write("\n");
		indent("]", writer, level);
	}

	/**
	 * helper function to pass in an object.
	 * 
	 * @param objectIterator the iterator to use
	 * @param elements       the elements to write
	 * @param writer         the writer to use
	 * @param level          the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObject(Iterator<String> objectIterator, Map<String, Integer> elements, Writer writer,
			int level) throws IOException {
		writer.write("\n");
		String key = objectIterator.next();
		quote(key, writer, level + 1);
		writer.write(": ");
		writer.write(elements.get(key).toString());
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asObject(Map<String, Integer> elements, Writer writer, int level) throws IOException {
		writer.write("{");
		var objectIterator = elements.keySet().iterator();
		if (objectIterator.hasNext()) {
			writeObject(objectIterator, elements, writer, level);
		}
		while (objectIterator.hasNext()) {
			writer.write(",");
			writeObject(objectIterator, elements, writer, level);

		}
		writer.write("\n");
		writer.write("}");
	}

	/**
	 *
	 * @param keyIterator iterator of keys to use
	 * @param elements    the elements to writer
	 * @param writer      the writer to use
	 * @param level       the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeNestedArray(Iterator<String> keyIterator,
			Map<String, ? extends Collection<Integer>> elements, Writer writer, int level) throws IOException {
		writer.write("\n");
		String key = keyIterator.next();
		quote(key, writer, level + 1);
		writer.write(": ");
		asArray(elements.get(key), writer, level + 1);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of integer objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param level    the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements, Writer writer, int level)
			throws IOException {
		writer.write(": {");
		var keys = elements.keySet().iterator();
		if (keys.hasNext()) {
			writeNestedArray(keys, elements, writer, level);
		}
		while (keys.hasNext()) {
			writer.write(",");
			writeNestedArray(keys, elements, writer, level);
		}
		writer.write("\n");
		indent("}", writer, level);
	}

	/**
	 *
	 * helper function to build the invertedIndex to JSON file
	 *
	 * @param indexIterator the iterator for the keyindex values.
	 * @param map           the elements to write
	 * @param writer        the writer to use.
	 * @param level         the initial indent level
	 * @throws IOException if an IO error occurs.
	 */
	public static void indexElements(Iterator<String> indexIterator,
			Map<String, ? extends Map<String, ? extends Collection<Integer>>> map, Writer writer, int level)
			throws IOException {
		writer.write("\n");
		String key = indexIterator.next();
		quote(key, writer, level + 1);
		asNestedArray(map.get(key), writer, level + 1);
	}

	/**
	 *
	 * Writes the elements as a pretty JSON inverted index where the words are the
	 * keys that have a corresponding list of files that contain that word. There is
	 * also the position in the file where the word is located and that is stored in
	 * list
	 *
	 * @param map    the elements to write
	 * @param writer the writer to use
	 * @param level  the initial indent level
	 * @throws IOException if an IO error occurs.
	 */
	public static void writeIndex(Map<String, ? extends Map<String, ? extends Collection<Integer>>> map, Writer writer,
			int level) throws IOException {
		writer.write("{");
		var indexIterator = map.keySet().iterator();
		if (indexIterator.hasNext()) {
			indexElements(indexIterator, map, writer, level);
		}
		while (indexIterator.hasNext()) {
			writer.write(",");
			indexElements(indexIterator, map, writer, level);
		}
		writer.write("\n");
		writer.write("}");
	}

	/**
	 * 
	 * @param queryIterator the queries to go through
	 * @param searchResult  the datastructure to parse
	 * @param writer        the writer to write to file
	 * @param level         the indentation level
	 * @throws IOException IOException to throw if error trying to write to file
	 */
	public static void queryDisplay(Iterator<String> queryIterator,
			Map<String, List<InvertedIndex.SearchResult>> searchResult, Writer writer, int level) throws IOException {

		String query = queryIterator.next();
		quote(query, writer, level + 1);
		writer.write(": [");
		List<InvertedIndex.SearchResult> results = searchResult.get(query);
		writeContentResults(results, writer, level + 1);
		writer.write("\n");
		indent("]", writer, level + 1);
	}

	/**
	 * write the whole results json format
	 * 
	 * @param searchResult the data to write to
	 * @param writer       writer to write
	 * @param level        level of indent
	 * @throws IOException throwIO Exception if error buildingJSON
	 */
	public static void writeResults(Map<String, List<InvertedIndex.SearchResult>> searchResult, Writer writer,
			int level) throws IOException {
		writer.write("{\n");
		var queryIterator = searchResult.keySet().iterator();
		if (queryIterator.hasNext()) {
			queryDisplay(queryIterator, searchResult, writer, level);
		}
		while (queryIterator.hasNext()) {
			writer.write(",");
			writer.write("\n");
			queryDisplay(queryIterator, searchResult, writer, level);
		}
		writer.write("\n");
		writer.write("}");
	}

	/**
	 * @param resultsIterator iterator for going through the properties of
	 *                        searchresults
	 * @param results         the inner content of results
	 * @param writer          writer to write.
	 * @param level           level of indent
	 * @throws IOException Exception to throw if there is an error writing to file
	 */
	public static void resultProperties(Iterator<InvertedIndex.SearchResult> resultsIterator,
			List<InvertedIndex.SearchResult> results, Writer writer, int level) throws IOException {
		writer.write("\n");
		indent("{", writer, level + 1);
		DecimalFormat FORMATTER = new DecimalFormat("0.00000000");
		writer.write("\n");
		InvertedIndex.SearchResult searchResult = resultsIterator.next();
		indent('"' + "count" + '"' + ": " + searchResult.getTotalQueryCount() + ",", writer, level + 2);
		writer.write("\n");
		indent('"' + "score" + '"' + ": " + FORMATTER.format(searchResult.getScore()) + ",", writer, level + 2);
		writer.write("\n");
		indent('"' + "where" + '"' + ": " + '"' + searchResult.getSource().toString() + '"', writer, level + 2);
		writer.write("\n");
		indent("}", writer, level + 1);
	}

	/**
	 * write inner nested values
	 * 
	 * @param results the inner content of results
	 * @param writer  writer to write
	 * @param level   level of indent
	 * @throws IOException exception thrown if error
	 */
	public static void writeContentResults(List<InvertedIndex.SearchResult> results, Writer writer, int level)
			throws IOException {
		var resultsIterator = results.iterator();
		if (resultsIterator.hasNext()) {
			resultProperties(resultsIterator, results, writer, level);
		}
		while (resultsIterator.hasNext()) {
			writer.write(",");
			resultProperties(resultsIterator, results, writer, level);

		}
	}

	/**
	 * write directly to file
	 * 
	 * @param searchResult the contents to use
	 * @param path         the path to use to write
	 * @throws IOException IOException to write to files
	 */
	public static void writeResults(Map<String, List<InvertedIndex.SearchResult>> searchResult, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			writeResults(searchResult, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static void asArray(Collection<Integer> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asArray(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static void asObject(Map<String, Integer> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asNestedArray(Map, Writer, int)
	 */
	public static void asNestedArray(Map<String, ? extends Collection<Integer>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asNestedArray(elements, writer, 0);
		}
	}

	/**
	 *
	 * overloaded method that takes in the other writeIndex function then write to
	 * the JSON file.
	 *
	 * @param map  the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #writeIndex(Map, Writer, int)
	 */
	public static void writeIndex(Map<String, ? extends Map<String, ? extends Collection<Integer>>> map, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			writeIndex(map, writer, 0);
		}
	}

	/**
	 * writes the elements as a pretty JSON index with nested maps to file.
	 *
	 * @param elements the elements to write
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String writeIndex(Map<String, Map<String, ? extends Collection<Integer>>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeIndex(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static String asArray(Collection<Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asNestedArray(Map, Writer, int)
	 */
	public static String asNestedArray(Map<String, ? extends Collection<Integer>> elements) {
		try {
			StringWriter writer = new StringWriter();
			asNestedArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param level  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void indent(Writer writer, int level) throws IOException {
		while (level-- > 0) {
			writer.write("\t");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param level   the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void indent(String element, Writer writer, int level) throws IOException {
		indent(writer, level);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param level   the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void quote(String element, Writer writer, int level) throws IOException {
		indent(writer, level);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}
}
