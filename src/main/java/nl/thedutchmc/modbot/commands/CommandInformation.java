package nl.thedutchmc.modbot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class CommandInformation {

	private String command;
	private User author;
	private Member member;
	private Guild guild;
	private TextChannel channel;
	private String[] args;
	
	public CommandInformation(String command, User author, Member member, Guild guild, TextChannel channel, String[] args) {
		this.command = command;
		this.author = author;
		this.member = member;
		this.guild = guild;
		this.channel = channel;
		this.args = args;
	}
	
	public String getCommand() {
		return this.command;
	}
	
	public User getAuthor() {
		return this.author;
	}
	
	public Member getMember() {
		return this.member;
	}
	
	public Guild getGuild() {
		return this.guild;
	}
	
	public TextChannel getChannel() {
		return this.channel;
	}
	
	public String[] getArgs() {
		return this.args;
	}
	
}
