import java.io.IOException;
import java.time.Instant;
import java.time.Duration;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * An alternative implemention of the {@MessageServlet} class but using the
 * Bulma CSS framework.
 *
 * @see SearchServlet
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class BulmaSearchServlet extends HttpServlet {
	/** Class version for serialization, in [YEAR][TERM] format (unused). */
	private static final long serialVersionUID = 202140;

	/** The title to use for this webpage. */
	private static final String TITLE = "Search Engine";

	/** The logger to use for this servlet. */
	private static Logger log = LogManager.getLogger();

	/** Template for starting HTML (including <head> tag). **/
//	private final String headTemplate;

	/** Template for ending HTML (including <foot> tag). **/
	private final String footTemplate;

	/** Template for individual message HTML. **/
	private final String textTemplate;

	/**
	 * Template for the SearchBox of the Search Engine
	 */
	private final String searchBoxTemplate;

	/** Base path with HTML templates. */
	private static final Path BASE = Path.of("src", "main","resources","html");

	/**
	 * Inverted Index to search for.
	 */
	private final ThreadedInvertedIndex index;

	/**
	 * The history of queries created by the user history
	 */
	private final List<String> history;

	/**
	 * Initializes this message board. Each message board has its own collection of
	 * messages.
	 * 
	 * @param index The inverted index to build
	 *
	 * @throws IOException if unable to read templates
	 */
	public BulmaSearchServlet(ThreadedInvertedIndex index) throws IOException {
		super();
		// load templates
//		headTemplate = Files.readString(BASE.resolve("bulma-head.html"), StandardCharsets.UTF_8);
		footTemplate = Files.readString(BASE.resolve("bulma-foot.html"), StandardCharsets.UTF_8);
		textTemplate = Files.readString(BASE.resolve("bulma-text.html"), StandardCharsets.UTF_8);
		searchBoxTemplate = Files.readString(BASE.resolve("bulma-searchbox.html"), StandardCharsets.UTF_8);
		this.index = index;
		this.history = new ArrayList<>();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		log.info("MessageServlet ID " + this.hashCode() + " handling GET request.");

		String query = request.getParameter("queries");

		query = query == null ? "" : query;

		// avoid xss attacks using apache commons text
		query = StringEscapeUtils.escapeHtml4(query);

		boolean search = request.getParameter("search") == null ? false : true;
		boolean quickSearch = request.getParameter("quickSearch") == null ? false : true;
		boolean exact = request.getParameter("exact") == null ? false : true;
		boolean reverse = request.getParameter("reverse") == null ? false : true;
		boolean history = request.getParameter("history") == null ? false : true;
		boolean clear = request.getParameter("clear") == null ? false : true;

		long start = System.nanoTime();
		List<InvertedIndex.SearchResult> results = this.index.search(TextFileStemmer.uniqueStems(query), exact);
		long end = System.nanoTime();
		long totalTime = end - start;

		StringBuilder build = new StringBuilder();
		StringBuilder compileHistory = new StringBuilder();

		for (String userQuery : this.history) {
			compileHistory.append(String.format("<li>%s<br/></li>\n", userQuery));
		}

		// used to substitute values in our templates
		Map<String, String> values = new HashMap<>();
		values.put("title", TITLE);
		values.put("thread", Thread.currentThread().getName());
		// setup form
		values.put("method", "GET");
		values.put("action", request.getServletPath());
		values.put("updated", getDate());

		// generate html from template
		StringSubstitutor replacer = new StringSubstitutor(values);
		// output generated html
		PrintWriter out = response.getWriter();

//		String head = replacer.replace(headTemplate);
		String foot = replacer.replace(footTemplate);
		String textBox = replacer.replace(searchBoxTemplate);

//		out.println(head);

		if (search) {
			if (reverse) {
				Collections.reverse(results);
			}

			if (query != "") {
				synchronized (this.history) {
					this.history.add(query);
				}
			}

			if (results.isEmpty()) {
				build.append("<p>No results.</p>");
			} else {
				DecimalFormat FORMATTER = new DecimalFormat("0.00000000");
				for (InvertedIndex.SearchResult result : results) {
					build.append(String.format(
							"<li><a href = %s  target=_blank>%s</a><br/>Matches: %s<br/>Score(Matches/Total Words): %s </li>\n",
							result.getSource(), result.getSource(), result.getTotalQueryCount(),
							FORMATTER.format(result.getScore())));
				}
				build.append(
						String.format("</br><p>It took: %s nanoseconds to generate these results</p1>", totalTime));
			}

			values.put("output", build.toString());
			String text = replacer.replace(textTemplate);
			out.println(text);
		}

		if (quickSearch) {
			if (results.isEmpty()) {
				build.append("<p>No results.</p>");
			} else {
				response.sendRedirect(results.get(0).getSource());
			}

			values.put("output", build.toString());
			String text = replacer.replace(textTemplate);
			out.println(text);
		}

		out.println(textBox);

		if (history) {
			values.put("output", compileHistory.toString());
			String historyBox = replacer.replace(textTemplate);
			out.println(historyBox);
		}
		if (clear) {
			synchronized(this.history) {
				this.history.clear();
			}
		}

		out.println(foot);
		out.flush();

		response.setStatus(HttpServletResponse.SC_OK);
	}
	/**
	 * Returns the date and time in a long format. For example: "12:00 am on
	 * Saturday, January 01 2000".
	 *
	 * @return current date and time
	 */
	private static String getDate() {
		String format = "hh:mm a 'on' EEEE, MMMM dd yyyy";
		DateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(new Date());
	}
}
