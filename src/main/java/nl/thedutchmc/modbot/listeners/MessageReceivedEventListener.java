package nl.thedutchmc.modbot.listeners;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageReceivedEventListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		
		if(event.getAuthor().isBot()) return;
		
		if(!event.getMessage().getContentDisplay().startsWith("$mb")) return;
		
		//Get the command
		String command = event.getMessage().getContentDisplay().replace("$mb ", "").split(" ")[0].replaceAll("\\s+", "");
		
		//Get a list of the arguments, replace empty string and whitespaces if they occur
		List<String> args = new LinkedList<String>(Arrays.asList(event.getMessage().getContentDisplay().replaceAll("\\$mb " + command, "").split(" ")));
		args.remove("");
		args.remove(" ");

		//$mb help
		if(command.equalsIgnoreCase("help")) {
			
			EmbedBuilder embedBuilder = new EmbedBuilder()
					.setTitle("ModBot Help Menu")
					.setColor(Color.cyan)
					.appendDescription("**$mb help** Shows you the help page\n")
					.appendDescription("**$mb config** Configuration base command\n");
			
			event.getChannel().sendMessage(embedBuilder.build()).queue();
		}
		
		//$mb config
		if(command.equalsIgnoreCase("config")) {
						
			//No arguments were passed
			if(args.size() == 0) {
				MessageEmbed e = getConfigHelp().build();
				event.getChannel().sendMessage(e).queue();
				return;
			} else {
				
				//$mb config help
				if(args.get(0).equalsIgnoreCase("help")) {
					MessageEmbed e = getConfigHelp().build();
					event.getChannel().sendMessage(e).queue();
					return;
				}
				
				//$mb config enableTickets <true/false>
				if(args.get(0).equalsIgnoreCase("enableTickets")) {
					
					//No true/false was passed to us
					if(args.size() < 2) {
						MessageEmbed e = getConfigHelp().build();
						event.getChannel().sendMessage(e).queue();
						return;
					} else {
						
						
					}
				}
			}
		}
	}
	
	//Menu for: $mb config help
	public EmbedBuilder getConfigHelp() {
		return new EmbedBuilder()
				.setTitle("ModBot Config Help Menu")
				.setColor(Color.cyan)
				.appendDescription("**$mb config help** Shows you the configuration help page\n")
				.appendDescription("**$mb config enableTickets <true/false>** Enable/Disable the Ticket system");
	}
}
