package nl.thedutchmc.modbot.listeners;

import java.util.concurrent.atomic.AtomicInteger;

import com.vdurmont.emoji.EmojiParser;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import nl.thedutchmc.modbot.JdaHandler;
import nl.thedutchmc.modbot.guildConfig.GuildConfig;

public class GuildMessageReactionAddEventListener extends ListenerAdapter {

	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		
		//Retrieve the Member who reacted
		Member memberAuthor = event.retrieveMember().complete();
		
		//Check if the Member is a bot
		//If yes, return
		if(memberAuthor.getUser().isBot()) {
			return;
		}
				
		//Check if we have the ReportEmoji feature enabled.
		//If we never set it, it will be null, so we check this too.
		Boolean enableReportEmoji = (Boolean) GuildConfig.getConfigForGuild(event.getGuild().getIdLong()).get("enableReportEmoji");
		if(enableReportEmoji == null || !enableReportEmoji) {
			return;
		}
		
		//Parse the emoji into the form of ':emoji:', then strip off the colons into the form of 'emoji'
		String emojiParsed = EmojiParser.parseToAliases(event.getReactionEmote().getName()).replace(":", "");
		
		//Fetch the emoji we configured for the ReportEmoji feature from the config
		String reportEmojiParsed = (String) GuildConfig.getConfigForGuild(event.getGuild().getIdLong()).get("reportEmoji");
		
		//Check if the two emojis match
		//If they do, check if there are enough emojis for it
		//to go above the threshold.
		if(reportEmojiParsed.equalsIgnoreCase(emojiParsed)) {
	
			//Get the message to which the reaction was applied
			Message m = event.getChannel().retrieveMessageById(event.getMessageIdLong()).complete();
									
			//We need to use an AtomicInteger because we will be modifying it inside the forEach loop.
			AtomicInteger count = new AtomicInteger(0);
			
			//Check if the reaction is an Emote or Emoji
			if(event.getReactionEmote().isEmote()) {
				
				//Retrieve a list of users who reacted with the Emote
				ReactionPaginationAction users = m.retrieveReactionUsers(event.getReactionEmote().getEmote());
				
				//Loop over the list and increment the counter
				users.forEach((user)-> {
					count.getAndIncrement();
				});				
			} else {				
				
				//Retrieve a list of users who reacted with the Emoji
				ReactionPaginationAction users = m.retrieveReactionUsers(event.getReactionEmote().getEmoji());
				
				//Loop over the list and increment the counter
				users.forEach((user) -> {
					count.getAndIncrement();
				});
			}
			
			//Fetch the threshold from the config
			int threshold = (int) GuildConfig.getConfigForGuild(event.getGuild().getIdLong()).get("reportEmojiCount");
			
			//Check if the counter is >= the threshold
			//If this is the case we want to inform the author of the message, 
			//put a log message in the logging channel and delete the message.
			if(count.get() >= threshold) {
				
				//Inform the user that their message has been removed.
				event.getChannel().sendMessage(m.getAuthor().getAsMention() + "Your message has been reported using ReportEmoji, and has thus been removed!\n"
						+ "You may open a ticket with **$ticket** if you feel this is not correct.").queue();
				
				//Log to the logging channel
				JdaHandler.getLogChannel().sendMessage("Message ``" + m.getContentDisplay() + "`` by **" + m.getAuthor().getName() + "** has been deleted using ReportEmoji").queue();
				
				//Lastly, delete the message
				m.delete().queue();
				
				return;
			}
		}
	}
}