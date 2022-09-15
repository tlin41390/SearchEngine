
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Terence Lin The Servlet that will display the files as well as the
 *         number of words for each location/file.
 */
public class BulmaSeedServlet extends HttpServlet {
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

	/**
	 * The template to display the seed
	 */
	private final String seedTemplate;

	/**
	 * The footer of the page
	 */
	private final String footerTemplate;


	/**
	 * Inverted Index to search for.
	 */
	private final ThreadedInvertedIndex index;

	/**
	 * the crawler to use to crawl more links
	 */
	private final WebCrawler crawler;

	/**
	 * The seed to check for
	 */
	private final URL seed;

	/**
	 * The thread safe data structure to check for visited
	 */
	private final Set<URL> visited;

	/**
	 * Initializes this message board. Each message board has its own collection of
	 * messages.
	 * 
	 * @param index   The inverted index to build
	 * @param seed    The URL seed to initially check for
	 * @param crawler the crawler to crawl the index
	 *
	 * @throws IOException if unable to read templates
	 */
	public BulmaSeedServlet(ThreadedInvertedIndex index, URL seed, WebCrawler crawler) throws IOException {
		super();
		// load templates
		seedTemplate = Files.readString(BASE.resolve("bulma-seed.html"), StandardCharsets.UTF_8);
		footerTemplate = Files.readString(BASE.resolve("bulma-foot.html"), StandardCharsets.UTF_8);
		this.index = index;
		this.crawler = crawler;
		this.seed = seed;
		this.visited = new HashSet<>();
		this.visited.add(seed);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");

		boolean seedRequest = request.getParameter("add seed") == null ? false : true;
		String query = request.getParameter("seed");

		query = query == null ? "" : query;

		// avoid xss attacks using apache commons text
		query = StringEscapeUtils.escapeHtml4(query);

		// used to substitute values in our templates
		Map<String, String> values = new HashMap<>();

		values.put("title", TITLE);
		values.put("thread", Thread.currentThread().getName());

		// setup form
		values.put("method", "GET");
		values.put("action", request.getServletPath());

		// generate html from template
		StringSubstitutor replacer = new StringSubstitutor(values);

		String seedReplacer = replacer.replace(seedTemplate);
		String footerReplacer = replacer.replace(footerTemplate);
		// output generated html
		PrintWriter out = response.getWriter();
		out.println(seedReplacer);
		synchronized (visited) {
			if (seedRequest) {
				try {
					URL location = new URL(query);
					if (!visited.contains(location)) {
						crawler.crawl(location);
						this.visited.add(location);
					}

				} catch (MalformedURLException | URISyntaxException e) {
					System.err.println("Exception in crawling the url");
				}
			}
		}
		out.println(footerReplacer);

		out.flush();

		response.setStatus(HttpServletResponse.SC_OK);
	}
}
