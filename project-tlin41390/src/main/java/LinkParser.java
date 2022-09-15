import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses URL links from the anchor tags within HTML text.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class LinkParser {
	/**
	 * Returns a list of all the valid HTTP(S) links found in the href attribute of
	 * the anchor tags in the provided HTML. The links will be converted to absolute
	 * using the base URL and normalized (removing fragments and encoding special
	 * characters as necessary).
	 *
	 * Any links that are unable to be properly parsed (throwing an
	 * {@link MalformedURLException}) or that do not have the HTTP/S protocol will
	 * not be included.
	 *
	 * @param base the base url used to convert relative links to absolute3
	 * @param html the raw html associated with the base url
	 * @return list of all valid http(s) links in the order they were found
	 *
	 * @see Pattern#compile(String)
	 * @see Matcher#find()
	 * @see Matcher#group(int)
	 * @see #normalize(URL)
	 * @see #isHttp(URL)
	 */
	public static ArrayList<URL> getValidLinks(URL base, String html) {
		ArrayList<URL> links = new ArrayList<URL>();
		
		/**
		 * (case insensitive)->(group 1: check for anchor tag with a reluctant search for whitespace between the a and the href, making sure to exclude the > symbol and any variants for the anchor tag beginning.)
		 * ->(group 2: the url that can have any characters excluding line breaks reluctantly.)
		 */
		String regex = "(?i)(<a\\s+?[^>]*.?href\\s?=\\s?\")(.*?)\"";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(html);
		
		while (matcher.find()) {
			String match = matcher.group(2);
			try {
				URL absolute = new URL(base, match);
				if (isHttp(absolute)) {
					links.add(normalize(absolute));
				}
			} catch (MalformedURLException e) {
				System.err.println("invalid absolute url " + match);
			} catch (URISyntaxException e) {
				System.err.println("something wrong with the URI.");
			}
		}

		return links;
	}

	/**
	 * Removes the fragment component of a URL (if present), and properly encodes
	 * the query string (if necessary).
	 *
	 * @param url the url to normalize
	 * @return normalized url
	 * @throws URISyntaxException    if unable to craft new URI
	 * @throws MalformedURLException if unable to craft new URL
	 */
	public static URL normalize(URL url) throws MalformedURLException, URISyntaxException {
		return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
				url.getQuery(), null).toURL();
	}

	/**
	 * Determines whether the URL provided uses the HTTP or HTTPS protocol.
	 *
	 * @param url the url to check
	 * @return true if the URL uses the HTTP or HTTPS protocol
	 */
	public static boolean isHttp(URL url) {
		return url.getProtocol().matches("(?i)https?");
	}
}
