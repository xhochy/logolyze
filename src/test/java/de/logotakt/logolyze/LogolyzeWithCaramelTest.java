package de.logotakt.logolyze;

import java.util.Collection;
import java.util.Iterator;
import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.event.WindowStateListener;
import java.awt.event.WindowEvent;

import com.xhochy.carameldb.CaramelRunner;
import com.xhochy.carameldb.CaramelFixture;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.logotakt.logolyze.controller.Controller;
import de.logotakt.logolyze.model.olap.OLAPEngine;
import de.logotakt.logolyze.view.swing2d.Swing2DView;
import de.logotakt.logolyze.model.config.ConfigManager;
import de.logotakt.logolyze.model.config.ConnectionConfig;

/**
 * Main class, only used for starting Logolyze.
 */
@RunWith(CaramelRunner.class)
public final class LogolyzeWithCaramelTest {

	@Inject
	@Named("testJDBCString")
	private String caramelConnection;
	
	/**
	 * The version of Logolyze.
	 */
	public static final String VERSION = "0.8 ($Rev: 1678 $)";

	public LogolyzeWithCaramelTest() {
	}

	/**
	 * The main entry point of Logolyze. Simply clues Model, View and Controller together.
	 * @param args The argmuments passed via command line. They are ignored.
	 */
	@Test
	@CaramelFixture("/db-fixtures/fullmetadatatree_measures.yml")
	public void runCaramel() {
		setNimbusLookAndFeel();

		System.out.println("Your connection string is: " + caramelConnection);

		System.out.println("Inserting CaramelDB connection...");
		try {
			Collection<ConnectionConfig> lastConnections = ConfigManager.getInstance().loadLastConnections();
			lastConnections.add(new ConnectionConfig(caramelConnection, "", "CaramelDB " + caramelConnection));
			ConfigManager.getInstance().saveLastConnections(lastConnections);
		} catch (IOException ex) {
			System.out.println("Sorry, IOException occured. Please set up the connection yourself.");
			System.out.println("Error was: " + ex.getMessage());
		}
		System.out.println("Starting Logolyze.");

		Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					System.out.println("Trying to remove CaramelDB connection...");
					try {
						Collection<ConnectionConfig> lastConnections = ConfigManager.getInstance().loadLastConnections();
						Iterator<ConnectionConfig> iter = lastConnections.iterator();

						while(iter.hasNext()) {
							ConnectionConfig cfg = iter.next();
							if (cfg.getName().equals("CaramelDB " + caramelConnection)) {
								iter.remove();
								System.out.println("Got it.");
							}
						}
			
						ConfigManager.getInstance().saveLastConnections(lastConnections);
					} catch (IOException ex) {
						System.out.println("Sorry, IOException occured. Please clean up the connection yourself.");
						System.out.println("Error was: " + ex.getMessage());
					}
				}
			});
		
		Controller controller = new Controller(new Swing2DView(), new OLAPEngine());

		while(true);
	}

	private void setNimbusLookAndFeel() {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
