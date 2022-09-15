import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Threaded version of the inverted index builder
 *
 * @author terencelin
 *
 */
public class ThreadedInvertedIndexProcessor {
	/**
	 * process files and/or traverse directories and subdiriectories if present.
	 *
	 * @param path    file/directory to interact
	 * @param index   index to add the data
	 * @param manager the workqueue to process the data
	 * @throws IOException if file cannot be read or written.
	 */
	public static void processFiles(Path path, ThreadedInvertedIndex index, WorkQueue manager) throws IOException {
		if (Files.isDirectory(path)) {
			traverseDirectory(path, index, manager);
		} else {
			manager.execute(new Task(path, index));
		}
		manager.finish();
	}
	// method to loop through the command line arguments

	/**
	 * private method to help traverse a directory
	 *
	 * @param path    the files in a directory to traverse or stem words from
	 * @param index   index to traverse through
	 * @param manager the workqueue to use for executing threads
	 * @throws IOException the exception to throw when there is an error opening a
	 *                     file
	 */
	private static void traverseDirectory(Path path, ThreadedInvertedIndex index, WorkQueue manager)
			throws IOException {
		try (DirectoryStream<Path> listing = Files.newDirectoryStream(path)) {
			for (Path files : listing) {
				if (Files.isDirectory(files)) {
					traverseDirectory(files, index, manager);
				} else if (InvertedIndexProcessor.isTextFile(files)) {
					manager.execute(new Task(files, index));

				}
			}
		}
	}

	/**
	 * task class that will read in a file and then stem.
	 *
	 * @author terencelin
	 *
	 */
	public static class Task implements Runnable {
		/**
		 * The file to open and read
		 */
		private final Path file;
		/**
		 * The index to build
		 */
		private final ThreadedInvertedIndex index;

		/**
		 * @param file  the file to read
		 * @param index the indes to add to
		 */
		public Task(Path file, ThreadedInvertedIndex index) {
			this.file = file;
			this.index = index;
		}

		@Override
		public void run() throws UncheckedIOException {
			try {

				InvertedIndex local = new InvertedIndex();
				InvertedIndexProcessor.stemWords(file, local);
				index.addAll(local);

			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
