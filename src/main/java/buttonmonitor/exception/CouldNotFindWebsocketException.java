package buttonmonitor.exception;

/**
 * Indicates that the WebSocket url could not be found. This might happen if the
 * the websocket server changes or the websocket itself is shut down at some
 * point in the future.
 * 
 * @author Jonathan
 */
public class CouldNotFindWebsocketException extends Exception {
	private static final long serialVersionUID = 1L;
}
