/*
 * ThermalCraftApp.java
 */

package thermalcraft;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class ThermalCraftApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
		theView = new ThermalCraftView(this);
        show(theView);
		theView.getTabbedFrame().setSelectedIndex(0);
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of ThermalCraftApp
     */
    public static ThermalCraftApp getApplication() {
        return Application.getInstance(ThermalCraftApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(ThermalCraftApp.class, args);
    }

	public ThermalCraftView getView() {
		return theView;
	}

	private ThermalCraftView theView;
}
