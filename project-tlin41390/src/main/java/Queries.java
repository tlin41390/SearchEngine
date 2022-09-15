import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Terence Lin
 *
 */
public interface Queries {
	/**
	 * default method to process the queries
	 * 
	 * @param path  the path to read from
	 * @param exact boolean to determing if we do exact or partial search
	 * @throws IOException exception to throw if there is error reading file
	 */
	public default void processQueries(Path path, boolean exact) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			String line;
			while ((line = reader.readLine()) != null) {
				processQueries(line, exact);
			}
		}
	}

	/**
	 * abstract method to process the queries
	 * 
	 * @param line  the line to stem and process
	 * @param exact the boolean to determine if we are doing exact or partial
	 *              search.
	 */
	public void processQueries(String line, boolean exact);

	/**
	 * @param path the path to write to
	 * @throws IOException the exception to throw if we can't write to json file.
	 */
	public void resultsToJson(Path path) throws IOException;

}
