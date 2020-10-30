package nl.thedutchmc.modbot.commands;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import nl.thedutchmc.modbot.ModBot;

public class CommandManager {

	private static List<BotCommand> registeredCommands = new ArrayList<>();
	
	/**
	 * Register a command with ModBot
	 * 
	 * @param name The name of the command. In Discord: $mb <command name>
	 * @return Returns the BotCommand object created for this command. You will need to set the CommandExecutor yourself. Returns null when the command already exists.
	 */
	@Nullable
	public static BotCommand registerCommand(String name) {
		
		//Check if the command has already been registered
		boolean exists = false;
		for(BotCommand botCommand : registeredCommands) {
			if(botCommand.getName().equalsIgnoreCase(name)) {
				exists = true;
				break;
			}
		}
		
		if(exists) {
			return null;
		}
		
		//Create the BotCommand object
		BotCommand command = new BotCommand(name);
		
		//Add it to the List of registered commands
		registeredCommands.add(command);
		
		ModBot.logInfo("Registered command: " + name);
		
		return command;
	}
	
	/**
	 * Execute a command.
	 * 
	 * @param name The name of the command to be executed
	 * @param information CommandInformation object.
	 * @return Returns true if the command was executed. Returns false if the command was not found.
	 */
	public static boolean executeCommand(String name, CommandInformation information) {
		
		boolean commandExists = false;
		
		//Loop over all BotCommands to find the correct one
		for(BotCommand botCommand : registeredCommands) {
			
			//We've found the correct BotCommand
			if(botCommand.getName().equals(name)) {
				
				commandExists = true;
				
				//Execute it
				//If it returns false, an error occurred. Log this to the console
				boolean success = botCommand.execute(information);
				if(!success) {
					ModBot.logWarn("There was an error executing the command '" + information.getCommand() + "' for guild '" + information.getGuild() + "'!");
				}
				
				break;
			}
		}
		
		//The command the user wants to run does not exist.
		//Inform them and return
		if(!commandExists) {
			information.getChannel().sendMessage("This command does not exist! Use **$mb help** for help.").queue();
			return true;
		}
		
		return false;
	}
}
