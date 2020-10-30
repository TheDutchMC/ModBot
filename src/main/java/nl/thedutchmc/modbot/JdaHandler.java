package nl.thedutchmc.modbot;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import nl.thedutchmc.modbot.listeners.GuildJoinEventListener;
import nl.thedutchmc.modbot.listeners.GuildMessageReactionAddEventListener;
import nl.thedutchmc.modbot.listeners.MessageReceivedEventListener;

public class JdaHandler {

	private static TextChannel logChannel;
	private static JDA jda;
	
	public void load() throws LoginException {
		
		//Declare the Gateway Intents we want to use.
		List<GatewayIntent> intents = new ArrayList<>();
		intents.add(GatewayIntent.GUILD_MESSAGES);
		intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
		
		List<Object> listeners = new ArrayList<>();
		listeners.add(new MessageReceivedEventListener());
		listeners.add(new GuildMessageReactionAddEventListener());
		listeners.add(new GuildJoinEventListener());
		
		//Log into Discord
		JDA jda = JDABuilder.createDefault(ConfigurationHandler.getConfig().get("botToken"))
					.enableIntents(intents)
					.setActivity(Activity.playing("ModBot'ing"))
					//.addEventListeners(new MessageReceivedEventListener())
					//.addEventListeners(new GuildMessageReactionAddEventListener())
					.addEventListeners(listeners.toArray())
					.build();
		
		try {
			jda.awaitReady();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		JdaHandler.jda = jda;
		
		GuildSetup setup = new GuildSetup();
		
		//Iterate over all Guilds the bot is connected to
		for(Guild g : jda.getGuilds()) {
			setup.setupGuild(g);
		}
	}
	
	public static TextChannel getLogChannel() {
		return logChannel;
	}
	
	public String getBotName() {
		return jda.getSelfUser().getName();
	}
	
	public static void setLogChannel(TextChannel c) {
		JdaHandler.logChannel = c;
	}
}
