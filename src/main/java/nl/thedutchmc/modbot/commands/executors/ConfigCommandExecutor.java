package nl.thedutchmc.modbot.commands.executors;

import java.awt.Color;
import java.util.EnumSet;
import java.util.List;

import com.vdurmont.emoji.EmojiParser;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import nl.thedutchmc.modbot.JdaHandler;
import nl.thedutchmc.modbot.commands.CommandExecutor;
import nl.thedutchmc.modbot.commands.CommandInformation;
import nl.thedutchmc.modbot.guildConfig.GuildConfig;

public class ConfigCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandInformation information) {		

		//Permissions check
		//First we check if the Moderator role is set,
		//If this is the case, we check if the author has that role.
		//If it is not set, only the Guild owner may use the config command.
		if(GuildConfig.getConfigForGuild(information.getGuild().getIdLong()).containsKey("moderatorRole")) {
			//Moderator role has been set
			
			//Get the moderator role from the config
			long moderatorRoleId = (long) GuildConfig.getConfigForGuild(information.getGuild().getIdLong()).get("moderatorRole");
			Role moderatorRole = information.getGuild().getRoleById(moderatorRoleId);
			
			//Check if the author doesn't have the moderator role
			//If this is the case, they may not use this command
			//Inform them of this and return.
			if(!information.getMember().getRoles().contains(moderatorRole)) {
				information.getChannel().sendMessage("You do not have the required permissions to use this command!").queue();
				return true;
			}
		} else {
			//Moderator role is not set
			
			//Check if the author is not the guild owner
			//If this is the case, they may not use the command
			//Inform them and return
			if(!information.getGuild().getOwner().equals(information.getMember())) {
				information.getChannel().sendMessage("You do not have the required permissions to use this command!").queue();
				return true;
			}
		}
		
		String[] args = information.getArgs();
		
		//Check if any arguments were passed to us
		//This should be at least 1 for this command.
		//If nothing was passed, send the user the help menu for the configuration command and return
		if(args.length == 0) {
			
			information.getChannel().sendMessage(getConfigHelp().build()).queue();
			return true;
		}
		
		//Configuration help menu
		//$mb config help
		//
		//The user requested the help menu, so send them this.
		if(args[0].equalsIgnoreCase("help")) {
			
			information.getChannel().sendMessage(getConfigHelp().build()).queue();
			return true;
		}
		
		//Configuration option to enable/disable the Ticket system for the current guild
		//$mb config enableTickets <true/false>
		if(args[0].equalsIgnoreCase("enableTickets")) {
			
			//Check if a value was passed to us
			//If not, send the help menu for the config command and return.
			if(args.length < 2) {	
				information.getChannel().sendMessage(getConfigHelp().build()).queue();
				return true;
			}
			
			//The value is true
			//Edit the Guild's configuration file to reflect this command
			//After the changes have been made, inform the sender that this was done
			if(args[1].equalsIgnoreCase("true")) {
				GuildConfig.writeToConfig(information.getGuild().getIdLong(), "enableTickets", true);
				information.getChannel().sendMessage("Configuration has been updated!").queue();
			}
			
			//Value is false
			//Edit the Guild's configuration file to reflect this command
			//After the changes have been made, inform the sender that this was done
			else if(args[1].equalsIgnoreCase("false")) {
				GuildConfig.writeToConfig(information.getGuild().getIdLong(), "enableTickets", false);
				information.getChannel().sendMessage("Configuration has been updated!").queue();
			}
			
			//Neither true nor false was given as value
			else {
				information.getChannel().sendMessage("Invalid option! May only be **true** or **false**!").queue();
				return true;
			}
			
			//Log to the log channel.
			JdaHandler.getLogChannel().sendMessage("The option **enableTickets** has been set to **" + args[1] + "** by **" + information.getAuthor().getName() + "** !").queue();
		}
		
		//Configuration option to set the Moderator role for the current guild.
		//$mb config moderator <@Moderator>
		if(args[0].equalsIgnoreCase("moderator")) {
			
			//Check if a value was passed to us
			//If not, send the help menu for the config command and return.
			if(args.length < 2) {
				information.getChannel().sendMessage(getConfigHelp().build()).queue();
				return true;
			}
			
			//Create a new array, which is the same as the old one, except that the first argument has been removed.
			String[] roleArgs = new String[args.length - 1];
			for(int i = 1; i < args.length; i++) {
				roleArgs[i - 1] = args[i]; 
			}
			
			//Turn the new Array into a space seperated String
			//Also replace the '@' symbol with nothing.
			String roleName = String.join(" ", roleArgs).replaceAll("@", "");
			
			//Get a list of roles that match the name we got			
			List<Role> moderatorRolesList = information.getGuild().getRolesByName(roleName, true);
			
			//No roles could be found by the name the user provided us.
			//Inform them and return
			if(moderatorRolesList == null || moderatorRolesList.size() == 0) {
				information.getChannel().sendMessage("No role(s) by that name could be found!").queue();
				return true;
			}
			
			//Get the Moderator role, this will just be the first item from the list
			Role moderatorRole = moderatorRolesList.get(0);

			//Save the role to the configuration file.
			GuildConfig.writeToConfig(information.getGuild().getIdLong(), "moderatorRole", moderatorRole.getIdLong());
			
			//Perform updates to channels that Moderators should be able to access.
			//e.g Log channel
			
			//Get the Log channel from the config
			long logChannelId = (long) GuildConfig.getConfigForGuild(information.getGuild().getIdLong()).get("log-channel");
			TextChannel logChannel = information.getGuild().getTextChannelById(logChannelId);
			
			//Create an EnumSet containing the read permission only
			EnumSet<Permission> read = EnumSet.of(Permission.VIEW_CHANNEL);
			
			//Set the permissions for the channel
			logChannel.createPermissionOverride(moderatorRole).setAllow(read).queue();
			
			//Inform the user of the change.
			information.getChannel().sendMessage("The role " + moderatorRole.getAsMention() + " is now configured as the Moderator role!").queue();
			
			//Log to the log channel
			JdaHandler.getLogChannel().sendMessage("The role **" + moderatorRole.getName() + "** has been configured as the Moderator role by **" + information.getAuthor().getName() + "**!").queue();
			
			return true;
		}
		
		//Configuration option to set the emoji that should be used when reporting a message
		//$mb config setReportEmoji <:emoji:>
		if(args[0].equalsIgnoreCase("setReportEmoji")) {
			
			//Check if a value was passed to us
			//If not, send the help menu for the config command and return.
			if(args.length < 2) {
				information.getChannel().sendMessage(getConfigHelp().build()).queue();
				return true;
			}
			
			//Parse the Emoji into text (Format: ':emoji:'), then remove the colons (Format: 'emoji')
			String emojiParsed = EmojiParser.parseToAliases(args[1]).replaceAll(":", "");
			
			//Write the configuration change to the config.
			long guildId = information.getGuild().getIdLong();
			GuildConfig.writeToConfig(guildId, "reportEmoji", emojiParsed);
			
			//Inform the user of the change
			information.getChannel().sendMessage("The report emoji has been changed to: ``:" +  emojiParsed + ":``!").queue();
			
			//Log to the log channel
			JdaHandler.getLogChannel().sendMessage("The report emoji has been changed to ``:" + emojiParsed + ":`` by **" + information.getAuthor().getName() + "**!").queue();
			
			return true;
		}
		
		if(args[0].equalsIgnoreCase("reportEmojiCount")) {
			//Check if a value was passed to us
			//If not, send the help menu for the config command and return.
			if(args.length < 2) {
				information.getChannel().sendMessage(getConfigHelp().build()).queue();
				return true;
			}
			
			//Check if the provided argument is not positive integer
			//If this is the case, inform the user and return
			if(args[1].matches("(0|[1-9]\\\\d*)")) {
				information.getChannel().sendMessage("Provided value must be a positive integer!").queue();
				return true;
			}
			
			int count = Integer.valueOf(args[1]);
			
			//Write to config
			GuildConfig.writeToConfig(information.getGuild().getIdLong(), "reportEmojiCount", count);
			
			//Inform the user of what we changed
			information.getChannel().sendMessage("**reportEmojiCount** has been set to **" + count + "**!").queue();
			
			//Log to the logging channel
			JdaHandler.getLogChannel().sendMessage("**reportEmojiCount** has been set to **" + count + "** by **" + information.getAuthor().getName() + "**!").queue();
			
			return true;
		}
		
		if(args[0].equalsIgnoreCase("enableReportEmoji")) {
			
			//Check if a value was passed to us
			//If not, send the help menu for the config command and return.
			if(args.length < 2) {
				information.getChannel().sendMessage(getConfigHelp().build()).queue();
				return true;
			}
			
			//The value is true
			//Edit the Guild's configuration file to reflect this command
			//After the changes have been made, inform the sender that this was done
			if(args[1].equalsIgnoreCase("true")) {
				
				//Before we allow the option to be enabled, check if reportEmoji and reportEmojiCount are set.
				boolean reportEmojiSet = GuildConfig.getConfigForGuild(information.getGuild().getIdLong()).containsKey("reportEmoji");
				boolean reportEmojiCount = GuildConfig.getConfigForGuild(information.getGuild().getIdLong()).containsKey("reportEmojiCount");
				
				if(!reportEmojiSet) {
					information.getChannel().sendMessage("The option **reportEmoji** has not been set. Cannot enable ReportEmoji!").queue();
					return true;
				}
				
				if(!reportEmojiCount) {
					information.getChannel().sendMessage("The option **reportEmojiCount** has not been set. Cannot enable ReportEmoji!").queue();
					return true;
				}
				
				//Write to the config, inform the user, and write to the log channel.
				GuildConfig.writeToConfig(information.getGuild().getIdLong(), "enableReportEmoji", true);
				information.getChannel().sendMessage("**enableReportEmoji** has been set to **true**").queue();
				JdaHandler.getLogChannel().sendMessage("Option **enableReportEmoji** has been set to **true** by **" + information.getAuthor().getName() + "**!").queue();
			}
			
			//Value is false
			//Edit the Guild's configuration file to reflect this command
			//After the changes have been made, inform the sender that this was done
			//Also log to the log channel
			else if(args[1].equalsIgnoreCase("false")) {
				GuildConfig.writeToConfig(information.getGuild().getIdLong(), "enableReportEmoji", false);
				information.getChannel().sendMessage("**enableReportEmoji** has been set to **false**").queue();
				JdaHandler.getLogChannel().sendMessage("Option **enableReportEmoji** has been set to **false** by **" + information.getAuthor().getName() + "**!").queue();
			}
			
			//Neither true nor false was given as value
			else {
				information.getChannel().sendMessage("Invalid option! May only be **true** or **false**!").queue();
				return true;
			}
		}
		
		return true;
	}

	//Help menu for the config command.
	private EmbedBuilder getConfigHelp() {
		return new EmbedBuilder()
				.setTitle("ModBot Config Help Menu")
				.setColor(Color.cyan)
				.appendDescription("**$mb config help** Shows you the configuration help page\n")
				.appendDescription("**$mb config enableTickets <true/false>** Enable/Disable the Ticket system\n")
				.appendDescription("**$mb config moderator <@Moderator>** Set the Moderator role. **Note**: You should tag the Moderator role here!\n")
				.appendDescription("**$mb config reportEmojiCount <integer> ** Set the threshold for when a message should be deleted by ReportEmoji\n")
				.appendDescription("**$mb config setReportEmoji <emoji>** Set the emoji to be watched by ReportEmoji\n")
				.appendDescription("**$mb config enableReportEmoji <true/false>** Enable or disable ReportEmoji. To enable **setReportEmoji** and **reportEmojiCount** must be set.\n");
	}
}