package de.logotakt.logolyze.model.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

/**
 * This class provides configuration management for the Logolyze application. It uses the XStream library to manage
 * configuration data for the components of the application. This class is a Singleton.
 */
public class ConfigManager { // NO! NO! NO! do NOT turn this class final, all tests will fail immediately!
    /* XStream object this ConnectionConfig uses. */
    private XStream xstream = new XStream();

    /* The single ConfigManager instance available. */
    private static ConfigManager instance;
    private static final Logger logger = Logger.getLogger(ConfigManager.class);

    // The logolyze config directory is simply ~/.logolyze/, but platform-independent.
    // There seems to be no easy way to find the preferred config directory for an application.
    private static final String CONFIG_DIRECTORY = System.getProperty("user.home")
            + System.getProperty("file.separator") + ".logolyze";
    private static final String LAST_CONNECTIONS_PATH = CONFIG_DIRECTORY + System.getProperty("file.separator")
            + "last_connections.xml";

    /**
     * Get the directory where all configuration is stored.
     * @return The configuration directory.
     */
    public static String getConfigurationDir() {
        return CONFIG_DIRECTORY;
    }

    /* Private constructor to enforce Singletonness. */
    private ConfigManager() {
    }

    /**
     * Returns the single ConnectionConfig instance, and creates it if it is not yet available.
     * @return The single ConnectionConfig instance
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }

        return instance;
    }

    private void writeWholeFile(final String pathname, final String content) throws IOException {
        FileWriter writer = null;

        try {
            File confDir = new File(CONFIG_DIRECTORY);
            if (!confDir.exists() && !confDir.mkdirs()) {
                throw new IOException("Configuration directory could not be created");
            }

            writer = new FileWriter(pathname, false);
            writer.write(content, 0, content.length());
        } catch (IOException ex) {
            // Make sure the data does not get lost.
            logger.error("IOException while saving configuration data.", ex);
            logger.info("Dumping configuration data:");
            logger.info(content);

            throw ex;
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ex) {
                logger.error("Could not close file to write configuration to.", ex);
                // Not being able to close the file is not as bad as crashing the whole program.
            }
        }
    }

    private String readWholeFile(final String pathname) throws IOException {
        FileReader reader = null;

        if (!new File(LAST_CONNECTIONS_PATH).exists()) {
            return "";
        }

        try {
            reader = new FileReader(pathname);
            StringBuffer buffer = new StringBuffer();
            int lastChar = reader.read();

            while (lastChar != -1) {
                buffer.append((char) lastChar);
                lastChar = reader.read();
            }

            return buffer.toString();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                logger.error("Could not close file where configuration was loaded from.", ex);
            }
        }
    }

    /**
     * Saves the given list of last connections to the application's configuration directory.
     * @param connections The list of connection to save.
     * @throws IOException if an IO error ocurred.
     */
    public void saveLastConnections(final Collection<ConnectionConfig> connections) throws IOException {
        String xmlContent = xstream.toXML(connections);

        writeWholeFile(LAST_CONNECTIONS_PATH, xmlContent);
    }

    /**
     * Loads the list of last connections from the application's configuration directory.
     * @return The list of connections just loaded.
     * @throws IOException if an IO error ocurred.
     */
    @SuppressWarnings("unchecked")
    public Collection<ConnectionConfig> loadLastConnections() throws IOException {
        String xmlContent = readWholeFile(LAST_CONNECTIONS_PATH);

        if (xmlContent.equals("")) {
            return new ArrayList<ConnectionConfig>();
        }

        return (Collection<ConnectionConfig>) xstream.fromXML(xmlContent);
    }

    /**
     * Saves the configuration of the view to the specified path. The View configuration may be any object.
     * @param path The path to save the view configuration to.
     * @param config The configuration to save.
     * @throws IOException if an IO error ocurred.
     */
    public void saveViewConfig(final String path, final Object config) throws IOException {
        String xmlContent = xstream.toXML(config);

        writeWholeFile(path, xmlContent);
    }

    /**
     * Loads the configuration of the view form the specified path. By means of reflection the returned object's actual
     * type will be the same as the type of the Object passed to {@link saveViewConfig}.
     * @param path The path to load the view configuration from.
     * @return The configuration loaded from the given path.
     * @throws IOException if an IO error ocurred.
     */
    public Object loadViewConfig(final String path) throws IOException {
        String xmlContent = readWholeFile(path);

        if (xmlContent.equals("")) {
            throw new FileNotFoundException("File to load view config from has not been found.");
        }

        return xstream.fromXML(xmlContent);
    }
}
