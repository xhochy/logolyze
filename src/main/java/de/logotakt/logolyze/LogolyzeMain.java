package de.logotakt.logolyze;

import java.io.File;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import de.logotakt.logolyze.controller.Controller;
import de.logotakt.logolyze.model.config.ConfigManager;
import de.logotakt.logolyze.model.olap.OLAPEngine;
import de.logotakt.logolyze.view.swing2d.Swing2DView;

/**
 * Main class, only used for starting Logolyze.
 */
public final class LogolyzeMain {

    private static final Logger LOGGER = Logger.getLogger(LogolyzeMain.class);

    /**
     * The version of Logolyze.
     */
    public static final String VERSION = "0.8 ($Rev: 2506 $)";

    private LogolyzeMain() {
    }

    /**
     * The main entry point of Logolyze. Simply clues Model, View and Controller together.
     * @param args The argmuments passed via command line. They are ignored.
     */
    public static void main(final String[] args) {
        if ((new File(ConfigManager.getConfigurationDir() + "/log4j.xml")).exists()) {
            DOMConfigurator.configure(ConfigManager.getConfigurationDir() + "/log4j.xml");
        } else {
            DOMConfigurator.configure(LogolyzeMain.class.getResource("/log4j.xml"));
        }
        LOGGER.info("Starting logolyze");
        setNimbusLookAndFeel();

        new Controller(new Swing2DView(), new OLAPEngine());
    }

    private static void setNimbusLookAndFeel() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (UnsupportedLookAndFeelException e) {
            LOGGER.debug("Failed to set Nimbus look and feel", e);
        } catch (ClassNotFoundException e) {
            LOGGER.debug("Unable to set Nimbus look and feel", e);
        } catch (InstantiationException e) {
            LOGGER.debug("Could not set Nimbus look and feel", e);
        } catch (IllegalAccessException e) {
            LOGGER.debug("Nimbus look and feel could not be set", e);
        }
        LOGGER.debug("Error while setting Nimbus look and feel");
    }
}
