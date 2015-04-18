package lights;

import lifx.java.android.client.LFXClient;
import lifx.java.android.entities.LFXHSBKColor;
import lifx.java.android.entities.LFXTypes.LFXPowerState;
import lifx.java.android.light.LFXLight;
import lifx.java.android.network_context.LFXNetworkContext;
import android.content.Context;

/**
 * Singleton to control the lights
 * @author Jonathan
 *
 */
public class Lights {
	private final static LFXNetworkContext LOCAL_NETWORK_CONTEXT  = LFXClient.getSharedInstance(new Context())
			.getLocalNetworkContext();
	private final static Lights INSTANCE  = new Lights();
	
	/**
	 * Represents the colors of the button, with corresponding hue values
	 */
	public enum COLOR {
		PURPLE(300),
		BLUE(180),
		GREEN(120),
		YELLOW(60),
		ORANGE(30),
		RED(0);
		
		private int hue;
		COLOR(int hue) { this.hue = hue; }
		public int getHue() { return hue; }
	}
	
	/**
	 * Connects to the local network
	 */
	private Lights() {
		LOCAL_NETWORK_CONTEXT.connect();
	}
	
	/**
	 * Sets the color of all the lights on the network based on HSBK
	 * @param color
	 */
	public void setColor(LFXHSBKColor color) {
		for (LFXLight light : LOCAL_NETWORK_CONTEXT.getAllLightsCollection().getLights()) {
			light.setColor(color);
		}
	}
	
	/**
	 * @return an instance of the Singleton Lights class
	 */
	public static Lights getInstance() {
		return INSTANCE;
	}
}
