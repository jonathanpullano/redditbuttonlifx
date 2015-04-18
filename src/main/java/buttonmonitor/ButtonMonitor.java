package buttonmonitor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import buttonmonitor.exception.CouldNotFindWebsocketException;

/**
 * Connects to r/thebutton's websocket and provides an UpdateListener
 * 
 * @author Jonathan
 *
 */
public class ButtonMonitor {
	private static final String THE_BUTTON_SUBREDDIT_URL = "http://www.reddit.com/r/thebutton";
	private static final String CHROME_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36";
	private static final Pattern WEBSOCKET_REGEX = Pattern
			.compile("wss://wss.redditmedia.com/thebutton\\?h=[0-9a-f]*&e=[0-9a-f]*");
	private static final Logger LOG = Logger.getLogger(ButtonMonitor.class);
	private static final WebSocketClientFactory factory = new WebSocketClientFactory();

	/**
	 * @return String representing a valid url to listen to thebutton updates
	 * @throws IOException
	 *             an error occurred during the HTTP request
	 * @throws CouldNotFindWebsocketException
	 *             The page was successfully loaded but the websocket url was
	 *             not present
	 */
	public String getButtonWebsocketUrl() throws IOException,
			CouldNotFindWebsocketException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		LOG.info("Connecting to " + THE_BUTTON_SUBREDDIT_URL);
		HttpGet httpGet = new HttpGet(THE_BUTTON_SUBREDDIT_URL);
		httpGet.setHeader("User-Agent", CHROME_USER_AGENT);
		CloseableHttpResponse response = httpclient.execute(httpGet);
		String webSocketUrl;
		try {
			HttpEntity entity = response.getEntity();
			webSocketUrl = findWebSocketUrlInEntity(entity);
			EntityUtils.consume(entity);
		} finally {
			response.close();
		}
		return webSocketUrl;
	}

	/**
	 * Searches the http response for a string that looks like a wss url for
	 * thebutton
	 */
	private String findWebSocketUrlInEntity(HttpEntity entity)
			throws CouldNotFindWebsocketException, IOException {
		String responseBody = convertStreamToString(entity.getContent());
		if (LOG.isDebugEnabled())
			LOG.debug("Response Body:\n" + responseBody);
		Matcher matcher = WEBSOCKET_REGEX.matcher(responseBody);
		if (!matcher.find())
			throw new CouldNotFindWebsocketException();
		return matcher.group();
	}

	/**
	 * Converts a stream to a string http://stackoverflow.com/a/5445161/1082169
	 */
	@SuppressWarnings("resource")
	private String convertStreamToString(InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	/**
	 * Connects to a websocket url and begins listening for button messages
	 * 
	 * @param redditUrl
	 *            - The url to connect to
	 * @throws
	 * @throws IOException
	 * @throws Exception
	 */
	public void openWebSocket(String redditUrl, OnTextMessage websocketListener)
			throws Exception, IOException {
		LOG.info("Opening websocket connection to: " + redditUrl);
		factory.start();
		WebSocketClient client = factory.newWebSocketClient();
		try {
			client.open(new URI(redditUrl), websocketListener);
		} catch (URISyntaxException e) {
			LOG.error("Reddit URI for the button appears invalid.");
		}
	}

	/**
	 * Connects to reddit and starts controlling the lights via the button
	 */
	public void start() throws IOException, CouldNotFindWebsocketException,
			Exception {
		openWebSocket(getButtonWebsocketUrl(), new ButtonWebSocketListener());
	}

	public static void main(String[] args) {
		try {
			new ButtonMonitor().start();
		} catch (IOException e) {
			LOG.error(
					"Network error occured trying to launch the ButtonMonitor.",
					e);
		} catch (CouldNotFindWebsocketException e) {
			LOG.error(
					"Could not find WebSocket url on r/thebutton. Perhaps the experiment has ended?",
					e);
		} catch (Exception e) {
			LOG.error(
					"Jetty failed to instantiate the WebSocket factory. Or, maybe something else has gone wrong. Jetty makes me sad by throwing Exception",
					e);
		}
	}
}
