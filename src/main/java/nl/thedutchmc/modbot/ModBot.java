package nl.thedutchmc.modbot;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.security.auth.login.LoginException;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;

import nl.thedutchmc.modbot.commands.CommandManager;
import nl.thedutchmc.modbot.commands.executors.ConfigCommandExecutor;
import nl.thedutchmc.modbot.commands.executors.HelpCommandExecutor;
import nl.thedutchmc.modbot.commands.executors.TicketCommandExecutor;
import nl.thedutchmc.modbot.guildConfig.GuildConfig;

//@SpringBootApplication
public class ModBot {

	public static void main(String[] args) {
		
		logInfo("Starting ModBot...");
		logInfo("Reading Configuration File...");
		
		ConfigurationHandler config = new ConfigurationHandler();
		try {
			config.load();
		} catch (IOException e) {
			logWarn("Unable to read the configuration file due to an IOException.");
		} catch (URISyntaxException e) {
			logWarn("Unable to read the configuration file due to an URISyntaxException");
		}
		
		logInfo("Reading complete.");
		logInfo("Reading Guild Config folder...");
		
		GuildConfig gc = new GuildConfig();
		try {
			gc.load();
		} catch (IOException e) {
			logWarn("Unable to read the Guild Configuration files due to an IOException.");
		} catch (URISyntaxException e) {
			logWarn("Unable to read the Guild Configuration files due to an URISyntaxException");
		}
		
		logInfo("Reading complete.");
		logInfo("Starting JDA...");
		
		JdaHandler jda = new JdaHandler();
		try {
			jda.load();
		} catch(LoginException e) {
			logWarn("Unable to login to Discord. Your token is likely invalid");
		}
		
		logInfo("JDA started.");
		logInfo("Registering commands...");
		
		CommandManager.registerCommand("config").setExecutor(new ConfigCommandExecutor());
		CommandManager.registerCommand("help").setExecutor(new HelpCommandExecutor());
		CommandManager.registerCommand("ticket").setExecutor(new TicketCommandExecutor());
		
		logInfo("Commands registered.");
		logInfo("Starting Spring Boot Webserver...");
		
		//SpringApplication.run(ModBot.class, args);
		
		logInfo("Webserver started.");
		
		logInfo("Startup complete. Welcome to " + jda.getBotName() + ".");
		
	}
	
	public static void logInfo(Object log) {
		final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");
		System.out.println("[" + LocalTime.now(ZoneId.systemDefault()).format(f) + "][INFO] " + log);
	}
	
	public static void logWarn(Object log) {
		final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");
		System.err.println("[" + LocalTime.now(ZoneId.systemDefault()).format(f) + "][WARN] " + log);
	}
	
	
	
}
