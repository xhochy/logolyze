package de.logotakt.logolyze.model.config;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.Test;

import de.logotakt.logolyze.view.swing2d.Swing2DState;


/**
 * This class tests the ConfigManager.
 */
public class ConfigTest {
    @Test
    public void tryWritingAndReading() throws IOException {
        ConfigManager manager = ConfigManager.getInstance();
        File configDirPath = new File(ConfigManager.getConfigurationDir());
        File tempFile;
        if (configDirPath.exists()) {
            tempFile = File.createTempFile("logolyze_test", "file", configDirPath);
        } else {
            tempFile = File.createTempFile("logolyze_test", "file");
        }
        tempFile.deleteOnExit();
        Collection<ConnectionConfig> conns = manager.loadLastConnections();
        manager.saveLastConnections(conns);
        // the config dir was probably just created, at least on the build server, so we load the newly created file :)
        conns = manager.loadLastConnections();

        Swing2DState state = new Swing2DState();

        state.setCube("hello world");
        state.setXSpinner(5.0);
        state.setYSpinner(99.0);

        manager.saveViewConfig(tempFile.getAbsolutePath(), state);
        Swing2DState newState = (Swing2DState) manager.loadViewConfig(tempFile.getAbsolutePath());

        assert newState != null;

        assert newState.getCube().equals(state.getCube())
		&& newState.getXSpinner().equals(state.getXSpinner())
		&& newState.getYSpinner().equals(state.getYSpinner());
    }
}
