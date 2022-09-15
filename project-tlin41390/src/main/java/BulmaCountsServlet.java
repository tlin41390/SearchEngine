import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Terence Lin
 *The Servlet that will display the files as well as the number of words for each location/file.
 */
public class BulmaCountsServlet extends HttpServlet {
	/**
	 * An alternative implemention of the {@MessageServlet} class but using the
	 * Bulma CSS framework.
	 *
	 * @see SearchServlet
	 *
	 * @author CS 272 Software Development (University of San Francisco)
	 * @version Fall 2021
	 */
		/** Class version for serialization, in [YEAR][TERM] format (unused). */
		private static final long serialVersionUID = 202140;

		/** The title to use for this webpage. */
		private static final String TITLE = "Inverted Index";

		/** The logger to use for this servlet. */
		private static Logger log = LogManager.getLogger();


		/** Base path with HTML templates. */
		private static final Path BASE = Path.of("src", "main", "resources", "html");
		
		private final String textTemplate;
		
		private final String countsTemplate;
		
		private final String footerTemplate;

		/**
		 * Inverted Index to search for.
		 */
		private final ThreadedInvertedIndex index;

		/**
		 * Initializes this message board. Each message board has its own collection of
		 * messages.
		 * 
		 * @param index The inverted index to build
		 *
		 * @throws IOException if unable to read templates
		 */
		public BulmaCountsServlet(ThreadedInvertedIndex index) throws IOException {
			super();
			// load templates
			countsTemplate = Files.readString(BASE.resolve("bulma-counts.html"), StandardCharsets.UTF_8);
			textTemplate = Files.readString(BASE.resolve("bulma-text.html"));
			footerTemplate = Files.readString(BASE.resolve("bulma-foot.html"), StandardCharsets.UTF_8);
			
			this.index = index;
		}

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			response.setContentType("text/html");

			StringBuilder buildCounts = new StringBuilder();
			
			
			for(String location: index.getFiles().keySet()){
				buildCounts.append(String.format("<li>Location: <a href = %s target = _blank>%s</a></br>WordCount: %s</br></li>\n",location,location,index.getFiles().get(location)));
			}
//			
			// used to substitute values in our templates
			Map<String, String> values = new HashMap<>();

			values.put("title", TITLE);
			values.put("thread", Thread.currentThread().getName());
			values.put("output",buildCounts.toString());
			
			// setup form
//			values.put("method", "GET");
//			values.put("action", request.getServletPath());

			// generate html from template
			StringSubstitutor replacer = new StringSubstitutor(values);
			String countsReplacer =replacer.replace(countsTemplate);
			String textReplacer = replacer.replace(textTemplate);
			String footerReplacer = replacer.replace(footerTemplate);
			
			// output generated html
			PrintWriter out = response.getWriter();
			
			out.println(countsReplacer);
			out.println(textReplacer);
			out.println(footerReplacer);

			out.flush();

			response.setStatus(HttpServletResponse.SC_OK);
		}
}
