package buttonmonitor;

import java.text.NumberFormat;
import java.text.ParseException;

import lifx.java.android.entities.LFXHSBKColor;
import lights.Lights;

import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ButtonWebSocketListener implements OnTextMessage {

	public final static String TIME_FORMAT = "yyyy-mm-dd-HH-mm-ss";

	private final static Logger LOG = LoggerFactory
			.getLogger(ButtonWebSocketListener.class);

	public ButtonWebSocketListener() {
	}

	public void onOpen(Connection connection) {
		LOG.info("WebSocket Connection opened");
	}

	public void onClose(int closeCode, String message) {
		LOG.info("WebSocket Connection closed by the remote server");
	}

	/**
	 * Parses received message into an appropriate object
	 */
	public void onMessage(String data) {
		JSONObject jsonMessage = new JSONObject(data);
		String type = jsonMessage.getString("type");
		if (!type.equals("ticking")) {
			if (LOG.isDebugEnabled())
				LOG.debug("Unknown message type received, msg=" + data);
		} else {
			TickingData tickingData;
			try {
				tickingData = parseTickingMessage(jsonMessage);
			} catch (ParseException e) {
				LOG.error("Failed to parse ticking message", e);
				return;
			}
			processTicking(tickingData);
		}
	}

	/**
	 * Turn a received message:ticking from the websocket into an instance of
	 * TickingData
	 * 
	 * @param jsonMessage
	 * @return
	 * @throws ParseException
	 *             If the received websocket message could not be read
	 */
	private TickingData parseTickingMessage(JSONObject jsonMessage)
			throws ParseException {
		DateTimeFormatter formatter = DateTimeFormat.forPattern(TIME_FORMAT);
		JSONObject payload = jsonMessage.getJSONObject("payload");
		LOG.debug("Payload: {}", payload);
		int participants = NumberFormat.getNumberInstance(java.util.Locale.US)
				.parse(payload.getString("participants_text")).intValue();
		String tickMac = payload.getString("tick_mac");
		double secondsLeft = payload.getDouble("seconds_left");
		DateTime now = formatter.parseDateTime(payload.getString("now_str"));
		return new TickingData(participants, tickMac, secondsLeft, now);
	}

	/**
	 * Update the lights based on the received tick data
	 * 
	 * @param data
	 */
	public void processTicking(TickingData data) {
		LOG.info("Ticking Message Received, Remaining Seconds={}", data.getSecondsLeft());
		int hue;
		if (data.getSecondsLeft() > 50)
			hue = Lights.COLOR.PURPLE.getHue();
		else if (data.getSecondsLeft() > 40)
			hue = Lights.COLOR.BLUE.getHue();
		else if (data.getSecondsLeft() > 30)
			hue = Lights.COLOR.GREEN.getHue();
		else if (data.getSecondsLeft() > 20)
			hue = Lights.COLOR.YELLOW.getHue();
		else if (data.getSecondsLeft() > 10)
			hue = Lights.COLOR.ORANGE.getHue();
		else
			hue = Lights.COLOR.RED.getHue();
		Lights.getInstance().setColor(
				LFXHSBKColor.getColor(hue, 1.0f, 1.0f, 3500));
	}
}
