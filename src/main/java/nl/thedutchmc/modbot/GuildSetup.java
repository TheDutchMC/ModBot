package nl.thedutchmc.modbot;

import java.awt.Color;
import java.util.EnumSet;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import nl.thedutchmc.modbot.guildConfig.GuildConfig;

public class GuildSetup {

	public void setupGuild(Guild g) {
		
		//Check if we already have this Guild configured
		if(GuildConfig.getGuildIds().contains(g.getIdLong())) {
			return;
		}

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
			return;
		}
		
		TextChannel logChannel;
		
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
		
			//Set the channel description
			logChannelCreateAction.setTopic("ModBot Log Chanel - ** DO NOT REMOVE **");
			
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
				logChannel.sendMessage("Preparing your Discord for ModBot...OK").queue();
				Category cat = g.createCategory("Tickets").complete();
				
				GuildConfig.writeToConfig(g.getIdLong(), "ticketCategory", cat.getIdLong());
			}
		}
				
		EmbedBuilder embed = new EmbedBuilder()
				.setTitle("ModBot")
				.setColor(Color.cyan)
				.appendDescription("Your Discord is now ready for use with ModBot!\n")
				.appendDescription("\n")
				.appendDescription("**Get started**\n")
				.appendDescription("Have a look at **$mb config help** to see what configuration options are available to you\n")
				.appendDescription("\n")
				.appendDescription("**Important**\n")
				.appendDescription("You should not remove this channel, or the Ticket category, this will cause the bot to break!")
				.appendDescription("You can however move them. You are also free to change permissions for them.")
				.appendDescription("Keep in mind though that ModBot needs full access to both!\n")
				.appendDescription("\n")
				.appendDescription("**Support**\n")
				.appendDescription("Support will be provided in Dutch76's Discord: <https://discord.com/invite/BrhNg7z>");
		
		logChannel.sendMessage(embed.build()).queue();
	}
}
