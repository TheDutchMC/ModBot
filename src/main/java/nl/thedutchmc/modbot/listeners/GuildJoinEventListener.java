package nl.thedutchmc.modbot.listeners;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import nl.thedutchmc.modbot.GuildSetup;

public class GuildJoinEventListener extends ListenerAdapter {

	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		
		//Setup the Guild
		GuildSetup setup = new GuildSetup();
		setup.setupGuild(event.getGuild());

	}
}
