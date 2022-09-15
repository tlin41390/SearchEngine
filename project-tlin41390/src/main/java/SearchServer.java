import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Demonstrates how to create a simple message board using Jetty and servlets,
 * as well as how to initialize servlets when you need to call its constructor.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class SearchServer {
	/**
	 * The index used to search
	 */
	private final ThreadedInvertedIndex index;

	/**
	 * Sets up a Jetty server with different servlet instances.
	 * 
	 * @param index The index used for searching
	 *
	 */
	public SearchServer(ThreadedInvertedIndex index) {
		this.index = index;
	}

	/**
	 * @param port The port used for connecting
	 * @param crawler 
	 * @param seed 
	 * @throws Exception The Exception to throw when there is an error.
	 */
	public void searchServers(int port,WebCrawler crawler,URL seed) throws Exception {
		Server server = new Server(port);

		ServletHandler handler = new ServletHandler();

		// must use servlet holds when need to call a constructor
		handler.addServletWithMapping(new ServletHolder(new BulmaSearchServlet(this.index)), "/");
		handler.addServletWithMapping(new ServletHolder(new BulmaIndexServlet(this.index)), "/index");
		handler.addServletWithMapping(new ServletHolder(new BulmaCountsServlet(this.index)), "/counts");
		handler.addServletWithMapping(new ServletHolder(new BulmaSeedServlet(this.index,seed,crawler)), "/seed");
		server.setHandler(handler);
		server.start();
		server.join();
	}
}
