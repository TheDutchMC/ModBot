package nl.thedutchmc.modbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Properties;

public class ConfigurationHandler {

	private static HashMap<String, String> configOptions = new HashMap<>();
	
	public void load() throws IOException, URISyntaxException {
		final File jarPath = new File(ConfigurationHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		final File folderPath = new File(jarPath.getParentFile().getPath());
		final File configFile = new File(folderPath, "modbot.properties");
		
		if(!configFile.exists()) {	
			configFile.createNewFile();
			FileWriter fw = new FileWriter(configFile, true);
			
			fw.write("ModBot Configuration File\n");
			fw.write("botToken=\n");
			
			fw.close();
		}
		
		Properties properties = new Properties();
		properties.load(new FileInputStream(configFile));
		
		configOptions.put("botToken", properties.getProperty("botToken"));
		
		if(configOptions.get("botToken").equals("")) {
			ModBot.logWarn("botToken is not set! Exiting");
			System.exit(-1);
		}
	}
	
	public static HashMap<String, String> getConfig() {
		return configOptions;
	}
}
