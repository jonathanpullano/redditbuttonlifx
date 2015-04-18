package buttonmonitor;

import org.joda.time.DateTime;

public class TickingData {
	private int participants;
	private String tickMac;
	private double secondsLeft;
	private DateTime now;

	TickingData(int participants, String tickMac, double secondsLeft,
			DateTime now) {
		this.participants = participants;
		this.tickMac = tickMac;
		this.secondsLeft = secondsLeft;
		this.now = now;
	}

	public int getParticipants() {
		return participants;
	}

	public String getTickMac() {
		return tickMac;
	}

	public double getSecondsLeft() {
		return secondsLeft;
	}

	public DateTime getNow() {
		return now;
	}

	@Override
	public String toString() {
		return "{participants: " + participants + ",tickMac: " + tickMac
				+ ",secondsleft: " + secondsLeft + ",now: "
				+ now.toString(ButtonWebSocketListener.TIME_FORMAT);
	}
}
