package nl.thedutchmc.modbot;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import nl.thedutchmc.modbot.guildConfig.GuildConfig;
import nl.thedutchmc.modbot.listeners.MessageReceivedEventListener;

public class JdaHandler {

	private static TextChannel logChannel;
	
	public void load() throws LoginException {
		
		//Declare the Gateway Intents we want to use.
		List<GatewayIntent> intents = new ArrayList<>();
		intents.add(GatewayIntent.GUILD_MESSAGES);
		intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
		
		//Log into Discord
		JDA jda = JDABuilder.createDefault(ConfigurationHandler.getConfig().get("botToken"))
					.enableIntents(intents)
					.setActivity(Activity.playing("ModBot'ing"))
					.addEventListeners(new MessageReceivedEventListener())
					.build();
		
		try {
			jda.awaitReady();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//Iterate over all Guilds the bot is connected to
		for(Guild g : jda.getGuilds()) {
			
			//Create a config file. If one already exists, nothing happens
			GuildConfig.createConfig(g.getIdLong());
			
			//Get the Bot user
			Member self = g.getSelfMember();
			
			//Check if we have administrator access in the current Guild
			if(!self.getPermissions().contains(Permission.ADMINISTRATOR)) {
				
				//We dont have administrator access
				//Iterate over all channels in the guild
				for(int i = 0; i < g.getTextChannels().size(); i++) {
					TextChannel c = g.getTextChannels().get(i);
					
					//We don't have write access to the current channel, continue to the next
					if(!self.hasPermission(c, Permission.MESSAGE_WRITE)) continue;
					
					//We've found a channel we've got write access to. Inform the users that the bot can't operate without Administrator permissions
					c.sendMessage("ModBot does not have Administrator permissions. We cannot operate without this!").queue();
					return;
				}
				continue;
			} 
			
			//Check if we have a config option for 'log-channel', if not, we havent made a channel for it yet
			if(!GuildConfig.getConfigForGuild(g.getIdLong()).containsKey("log-channel")) {
				
				//Create a ChannelAction to create the log channel
				ChannelAction<TextChannel> logChannelCreateAction = g.createTextChannel("modbot-logs");
				
				//Create an EnumSet containing only the read channel permission
				EnumSet<Permission> read = EnumSet.of(Permission.VIEW_CHANNEL);
				
				//Set the permissions on the channel.
				//@Owner: Allow read
				//@Everyone: Deny read
				logChannelCreateAction.addMemberPermissionOverride(g.getOwnerIdLong(), read, null);
				logChannelCreateAction.addRolePermissionOverride(g.getPublicRole().getIdLong(), null, read);
				
				//Create the channel
				logChannel = logChannelCreateAction.complete();
				
				//Write the change to the configuration file.
				GuildConfig.writeToConfig(g.getIdLong(), "log-channel", logChannel.getIdLong());
			} else {
				
				//The config contains an id for the log channel, so assign it to the logChannel variable.
				logChannel = g.getTextChannelById(GuildConfig.getConfigForGuild(g.getIdLong()).get("log-channel").toString());
			}

			//Check if we have the Ticket system enabled, yes by default.
			if((boolean) GuildConfig.getConfigForGuild(g.getIdLong()).get("enableTickets") == true) {
				
				//Check if a Tickets category already exists.
				boolean ticketCategoryExists = false;
				for(Category cat : g.getCategories()) {
					if(cat.getName().equals("Tickets")) {
						ticketCategoryExists = true;
						break;
					}
				}
				
				//Tickets category does not exist, so create it, and log it to the log channel
				if(!ticketCategoryExists) {
					logChannel.sendMessage("Tickets category does not exist. Creating").queue();
					Category cat = g.createCategory("Tickets").complete();
					
					GuildConfig.writeToConfig(g.getIdLong(), "ticketCategory", cat.getIdLong());
					
					logChannel.sendMessage("Done.").queue();
				}
			}
		}
	}
	
	public static TextChannel getLogChannel() {
		return logChannel;
	}
}
