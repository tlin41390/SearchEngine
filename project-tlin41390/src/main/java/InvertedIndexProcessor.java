import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import java.io.BufferedReader;

/**
 * Inverted IndexProcessor that will traverse directories, find files and then
 * build the inverted index.
 *
 * @author Terence Lin
 */
public class InvertedIndexProcessor {

	/**
	 * process files and/or traverse directories and subdiriectories if present.
	 *
	 * @param path  file/directory to interact
	 * @param index index to add the data
	 * @throws IOException if file cannot be read or written.
	 */
	public static void processFiles(Path path, InvertedIndex index) throws IOException {
		if (Files.isDirectory(path)) {
			traverseDirectory(path, index);
		} else {
			stemWords(path, index);
		}
	}
	// method to loop through the command line arguments

	/**
	 * private method to help traverse a directory
	 * 
	 * @param path  the files in a directory to traverse or stem words from
	 * @param index index to traverse through
	 * @throws IOException the exception to throw when there is an error opening a
	 *                     file
	 */
	private static void traverseDirectory(Path path, InvertedIndex index) throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {
			for (Path files : listing) {
				if (Files.isDirectory(files)) {
					traverseDirectory(files, index);
				} else if (isTextFile(files)) {
					stemWords(files, index);
				}
			}
		}
	}

	/**
	 * checks a textfile to see if it has the extension we want.
	 *
	 * @param path the file to process
	 * @return boolean to determine if the file has the extension we want.
	 */
	public static boolean isTextFile(Path path) {
		String lower = path.toString().toLowerCase();
		return lower.endsWith(".txt") || lower.endsWith(".text");
	}

	/**
	 * stem each words and put it in inverted index for each word in file.
	 *
	 * @param file  the file to stem words in.
	 * @param index the index to store the data
	 * @throws IOException if file cannot be read or writer
	 */
	public static void stemWords(Path file, InvertedIndex index) throws IOException {
		Integer position = 1;
		Stemmer stemmer = new SnowballStemmer(TextFileStemmer.ENGLISH);
		String location = file.toString();

		try (BufferedReader reader = Files.newBufferedReader(file, TextFileStemmer.UTF8)) {
			String line = reader.readLine();
			while (line != null) {
				for (String word : TextParser.parse(line)) {
					index.add(stemmer.stem(word).toString(), location, position++);
				}
				line = reader.readLine();
			}
		}
	}
}
