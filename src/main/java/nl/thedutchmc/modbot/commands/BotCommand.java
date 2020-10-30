package nl.thedutchmc.modbot.commands;

public class BotCommand {

	private String name;
	private CommandExecutor executor;
	
	protected BotCommand(String name) {
		this.name = name;
	}
	
	public void setExecutor(CommandExecutor executor) {
		this.executor = executor;
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean execute(CommandInformation information) {
		return executor.onCommand(information);
	}
}
