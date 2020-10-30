package nl.thedutchmc.modbot.commands.executors;

import java.util.EnumSet;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import nl.thedutchmc.modbot.JdaHandler;
import nl.thedutchmc.modbot.commands.CommandExecutor;
import nl.thedutchmc.modbot.commands.CommandInformation;
import nl.thedutchmc.modbot.guildConfig.GuildConfig;

public class TicketCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandInformation information) {

		long guildId = information.getGuild().getIdLong();
		
		//Check if the Ticket system is enabled
		//If not, inform the sender of this and return
		if((Boolean) GuildConfig.getConfigForGuild(guildId).get("enableTickets") != true) {
			information.getChannel().sendMessage("ModBot's Ticket System is not enabled in this Guild!").queue();
			return true;
		}
		
		String[] args = information.getArgs();
		
		//Check if the args length is 0
		//If this is the case we will create a ticket.
		if(args.length == 0) {
			
			//Get the category ID from the config.
			long ticketCategoryId = (long) GuildConfig.getConfigForGuild(guildId).get("ticketCategory");
			
			//Create a text channel for the sender.
			Guild g = information.getGuild();
			
			//Construct the channel name
			String channelName = information.getAuthor().getName() + "-" + randomNumber();
			
			//Create the channel, but don't complete yet.
			ChannelAction<TextChannel> actionCreateChannel = g.createTextChannel(channelName, g.getCategoryById(ticketCategoryId));
			
			//Permission to read the channel
			EnumSet<Permission> read = EnumSet.of(Permission.VIEW_CHANNEL);
			
			//Set the correct permission for the channels.
			//@everyone: Deny
			//@author: Allow
			actionCreateChannel.addPermissionOverride(g.getPublicRole(), null, read);
			actionCreateChannel.addMemberPermissionOverride(information.getMember().getIdLong(), read, null);
			
			//Finally, create the Channel
			TextChannel ticketCreated = actionCreateChannel.complete();
			
			//Send a message linking to the newly created Channel and tag the user who created it.
			information.getChannel().sendMessage("Ticket has been created " + ticketCreated.getAsMention() + " " + information.getMember().getAsMention()).queue();
			
			//Create the staff mention String.
			//First we check if any staff role is configured
			//If this is the case, we use that.
			//If not, we use the Guild owner.
			String staffMention = "";
			if(GuildConfig.getConfigForGuild(g.getIdLong()).containsKey("moderatorRole")) {
				Role moderatorRole = g.getRoleById((long) GuildConfig.getConfigForGuild(g.getIdLong()).get("moderatorRole"));
				staffMention = "A " + moderatorRole.getAsMention();
			} else {
				Member guildOwner = g.getOwner();
				staffMention = guildOwner.getAsMention();
			}
			
			//Also send a message in the just created ticket.
			ticketCreated.sendMessage("**Ticket " + channelName + "**\n"
					+ "\n"
					+ "Your ticket has been created " + information.getMember().getAsMention() + "\n"
					+ staffMention + " will be with you as soon as possible.").queue();
			
		}
		
		//Args length is more than one. We dont create a channel now.
		if(args.length >= 1) {
			
			//User wants the help menu
			//$mb ticket help
			if(args[0].equalsIgnoreCase("help")) {
				
				//Create an embed with the help menu
				EmbedBuilder embedBuilder = new EmbedBuilder()
						.setTitle("ModBot Ticket Help Menu")
						.appendDescription("**$mb ticket help** Shows you the ticket help page\n")
						.appendDescription("**$mb ticket close** Close the current ticket. Moderator only.")
						.appendDescription("**$mb ticket remove** Remove the current ticket. Moderator only. **Warning: This action is not reversable!**");
				
				//Send the just created embed.
				information.getChannel().sendMessage(embedBuilder.build()).queue();
				
				return true;
			}
			
			if(args[0].equalsIgnoreCase("close")) {
				
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
				
				//Get the TextChannel we're working in.
				TextChannel currentChannel = information.getChannel();
				
				//Check if the category of the TextChannel was send from is not the ticket category
				//If this is the case, we can't close the ticket, since it isn't a ticket
				//inform the user of this and return.
				if(!isChannelTicket(currentChannel, information.getGuild())) {
					currentChannel.sendMessage("This channel is not a ticket!").queue();
					return true;
				}
				
				//We now are sure the channel we are in is a Ticket.
				//Rename the channel to indicate it is closed.
				currentChannel.getManager().setName("closed-" + currentChannel.getName()).queue();
				
				//Now send a message informing the user that we closed it, and tell them how to reopen it.
				currentChannel.sendMessage("**This ticket has been closed** \n"
						+ "\n"
						+ "You can reopen it with `$mb ticket reopen`!").queue();
				
			}
			
			if(args[0].equalsIgnoreCase("reopen")) {
				
				//Get the TextChannel we're working in
				TextChannel currentChannel = information.getChannel();
				
				//Check if the category of the TextChannel was send from is not the ticket category
				//If this is the case, we can't reopen the ticket, since it isn't a ticket
				//inform the user of this and return.
				if(!isChannelTicket(currentChannel, information.getGuild())) {
					currentChannel.sendMessage("This channel is not a ticket!").queue();
					return true;
				}
				
				//Check if the ticket is closed or not (it should be)
				//If this is not the case, inform the user and return.
				String channelName = currentChannel.getName();
				if(!channelName.startsWith("closed-")) {
					currentChannel.sendMessage("This ticket is not closed, so it cannot be reopened!").queue();
					return true;
				}
				
				//We now know that this is a ticket, and that it is currently closed
				//Reopen it.
				String newChannelName = channelName.replaceAll("closed-", "");
				currentChannel.getManager().setName(newChannelName).queue();
				
				//Inform the user that the channel has been reopened
				currentChannel.sendMessage("**This ticket has been reopened!**").queue();
				
				return true;
			}
			
			if(args[0].equalsIgnoreCase("remove")) {
				
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
				
				//Get the TextChannel we're working in
				TextChannel currentChannel = information.getChannel();
				
				//Check if the category of the TextChannel was send from is not the ticket category
				//If this is the case, we can't reopen the ticket, since it isn't a ticket
				//inform the user of this and return.
				if(!isChannelTicket(currentChannel, information.getGuild())) {
					currentChannel.sendMessage("This channel is not a ticket!").queue();
					return true;
				}
				
				//Check if the ticket is closed or not (it should be)
				//If this is not the case, inform the user and return.
				String channelName = currentChannel.getName();
				if(!channelName.startsWith("closed-")) {
					currentChannel.sendMessage("This ticket cannot be removed! It must be closed first!").queue();
					return true;
				}
								
				//It is safe to remove the ticket.
				//Remove the channel
				currentChannel.delete().queue();
				
				//Log to the log channel
				JdaHandler.getLogChannel().sendMessage("Ticket **" + channelName + "** has been removed by **" + information.getAuthor().getName() + "**!").queue();
				
				return true;
			}
		}
		
		return true;
	}
	
	//Used to generate random numbers to append to the ticket name.
	private int randomNumber() {
		final Random rnd = new Random();
		final int n = 100000 + rnd.nextInt(900000);
		
		return n;
	}
	
	//Check if a channel is a ticket, by checking if it's parent category is the ticket category.
	private boolean isChannelTicket(TextChannel channel, Guild g) {

		//Get the Ticket category for this guild from the config file.
		long ticketCategoryId = (long) GuildConfig.getConfigForGuild(g.getIdLong()).get("ticketCategory");
		Category ticketCategory = g.getCategoryById(ticketCategoryId);
		
		return channel.getParent().equals(ticketCategory);
	}
	
}
