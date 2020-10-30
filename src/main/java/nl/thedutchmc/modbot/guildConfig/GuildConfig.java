package nl.thedutchmc.modbot.guildConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONObject;

import nl.thedutchmc.modbot.ModBot;

public class GuildConfig {
	
	private static HashMap<Long, HashMap<String, Object>> configOptions = new HashMap<>();
	private static File CONFIG_FOLDER;
	
	//Load the configuration from file.
	public void load() throws URISyntaxException, IOException {
		
		//First we want to get the path of the config folder
		final File jarPath = new File(GuildConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		final File folderPath = new File(jarPath.getParentFile().getPath());
		CONFIG_FOLDER = new File(folderPath + File.separator + "guildConfig");
		
		//Check if the folder exists, if not, create it 
		if(!CONFIG_FOLDER.exists()) {
			Files.createDirectories(Paths.get(CONFIG_FOLDER.getAbsolutePath()));
		}
		
		//Discover all the config files, so we can iterate over it
		List<String> configs = discoverConfigs(CONFIG_FOLDER);
		
		
		for(String path : configs) {
			
			//Read the content from the file, and turn it into a JSONObject for parsing
			String content = new String(Files.readAllBytes(Paths.get(path)));
			JSONObject root = new JSONObject(content);
			
			//Loop over all keys, and put the values in a HashMap for storing
			HashMap<String, Object> options = new HashMap<>();			
			for(String key : root.keySet()) {
				options.put(key, root.get(key));
			}
			
			//Put the new HashMap in the configs HashMap along with the ID of the guild
			configOptions.put(root.getLong("id"), options);
		}
	}
	
	//Discover Configs from config file folder
	private List<String> discoverConfigs(File path) throws IOException {
		
		//Create a Stream and walk it, then return a list of the found paths.
		//Only files ending in .json will be used.
		Stream<Path> walk = Files.walk(Paths.get(path.getAbsolutePath()));
		List<String> result = walk.map(x -> x.toString()).filter(f -> f.endsWith(".json")).collect(Collectors.toList());
		walk.close();
		
		return result;
	}
	
	//Used for creating a new configuration file
	public static void createConfig(Long id) {
		File configFile = new File(CONFIG_FOLDER, id.toString() + ".json");
		
		try {
			
			//Check if the file already exists
			//If not, create it and write the id along with basic options to it.
			if(!configFile.exists()) {
				configFile.createNewFile();
				
				JSONObject root = new JSONObject();
				root.put("id", id);
				root.put("enableTickets", true);
				
				FileWriter fw = new FileWriter(configFile);
				fw.write(root.toString());
				
				fw.close();
				
				//Put the new options in the HashMap as well, so they can be used immediately
				HashMap<String, Object> options = new HashMap<>();			
				for(String key : root.keySet()) {
					options.put(key, root.get(key));
				}
				
				configOptions.put(root.getLong("id"), options);
			}
		} catch (IOException e) {
			ModBot.logWarn("Failed to create configuration file for Guild: " + id);
		}
	}
	
	//Used to the HashMap for a specific Guild
	public static HashMap<String, Object> getConfigForGuild(long id) {
		return configOptions.get(id);
	}
	
	//Update a Key-value pair in the config.
	//Not important if the pair already exists or not.
	public static void writeToConfig(Long id, String key, Object value) {
		File configFile = new File(CONFIG_FOLDER, id.toString() + ".json");
		
		try {
			//Read the current contents of the config file
			String content = new String(Files.readAllBytes(Paths.get(configFile.getAbsolutePath())));
			JSONObject root = new JSONObject(content);
			
			root.put(key, value);
			
			//Write the new contents to the file
			FileWriter fw = new FileWriter(configFile);
			fw.write(root.toString());
			
			fw.close();

			//Put the new contents in the HashMap so it can be used immediately.
			HashMap<String, Object> options = configOptions.get(id);
			options.put(key, value);
			configOptions.put(id, options);
			
		} catch (IOException e) {
			ModBot.logWarn("An IOException occured when updating the GuildConfig for Guild: " + id);
		}
	}
	
	//Get a list of all Guild IDs we have configured
 	public static List<Long> getGuildIds() {
 		List<Long> result = new LinkedList<Long>(configOptions.keySet());
 		return result;
 	}
}
