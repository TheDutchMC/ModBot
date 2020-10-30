package nl.thedutchmc.modbot.commands.executors;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import nl.thedutchmc.modbot.commands.CommandExecutor;
import nl.thedutchmc.modbot.commands.CommandInformation;

public class HelpCommandExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandInformation information) {
		
		//Create an embed to send to the user
		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTitle("ModBot Help Menu")
				.setColor(Color.cyan)
				.appendDescription("**$mb help** Shows you the help page\n")
				.appendDescription("**$mb config** Configuration base command\n")
				.appendDescription("**$mb ticket** Create a ticket\n")
				.appendDescription("**$mb ticket help** Get the ticket help menu\n");
		
		//Send it the created embed
		information.getChannel().sendMessage(embedBuilder.build()).queue();
		
		return true;
	}

}
