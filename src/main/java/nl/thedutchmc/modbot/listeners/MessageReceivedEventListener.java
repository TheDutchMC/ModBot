package nl.thedutchmc.modbot.listeners;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.thedutchmc.modbot.commands.CommandInformation;
import nl.thedutchmc.modbot.commands.CommandManager;

public class MessageReceivedEventListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		
		if(event.getAuthor().isBot()) return;
		
		//Check if the message starts with '$'
		if(!event.getMessage().getContentDisplay().startsWith("$")) return;
		
		//Get the command
		String command = event.getMessage().getContentDisplay().replace("$mb ", "").split(" ")[0].replaceAll("\\s+", "");
		
		//Get a list of the arguments, replace empty string and whitespaces if they occur
		List<String> args = new LinkedList<String>(Arrays.asList(event.getMessage().getContentDisplay().replaceAll("\\$mb " + command, "").split(" ")));
		args.remove("");
		args.remove(" ");
				
		//Check if the command is empty, it'll then be equal to '$mb'
		//If this is the case we set the command to be 'help'
		if(command.equals("$mb")) {
			command = "help";
		}
				
		//Users are allowed to just use '$ticket'
		//We'll rewrite it for them.
		//Does *not* work for arguments
		if(command.equals("$ticket")) {
			command = "ticket";
			args = Arrays.asList(new String[] {});
		}
		
		//Execute the command with the CommandManager.
		CommandManager.executeCommand(command, new CommandInformation(
				command, 
				event.getAuthor(), 
				event.getMember(), 
				event.getGuild(), 
				event.getTextChannel(), 
				args.toArray(new String[args.size()])));
	}
}
