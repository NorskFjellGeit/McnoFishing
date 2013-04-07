package no.mcno.fishing;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Countdown
{

	public static McnoFishing plugin;

	public Countdown(McnoFishing mcnoFishing)
	{
		plugin = mcnoFishing;
	}

	public void ticker(final int fromTime)
	{

		if(!(McnoFishing.fishingRunning))
		{
			return;
		}

		if(fromTime <= 0)
		{
			McnoFishing.fishingActive = false;
			plugin.pickFishers();
			return;
		}

		if((fromTime == 60))
		{
			plugin.getServer().broadcastMessage(plugin.chatPrefix + ChatColor.AQUA + "" + ChatColor.ITALIC + "Fiskepåmelding startet! Skriv /fisk for å melde deg på!");
		}

		if((fromTime == 45) || (fromTime == 25) || (fromTime == 15))
		{
			plugin.getServer().broadcastMessage(plugin.chatPrefix + ChatColor.AQUA + "" + ChatColor.ITALIC + "Skriv /fisk for å melde deg på!");
		}

		if((fromTime == 60) || (fromTime == 30) || (fromTime == 10) || (fromTime == 5) || (fromTime == 4) || (fromTime == 3) || (fromTime == 2) || (fromTime == 1))
		{

			plugin.getServer().broadcastMessage(plugin.chatPrefix + ChatColor.GRAY + "Starter fisking om " + fromTime + " sekunder..");

		}

		final int newTime = fromTime - 1;

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{

			public void run()
			{
				ticker(newTime);
			}
		}, 20L);

	}

	public void fishingTicker(final int fromTime)
	{

		if(plugin.participants.size() == 0)
		{
			plugin.getServer().broadcastMessage(plugin.chatPrefix + "Fiskingen ble avsluttet, ingen spillere!");
			return;
		}

		if(!(McnoFishing.fishingRunning))
		{
			return;
		}

		if(fromTime <= 0)
		{

			plugin.completeFishing();
			return;

		}

		if((fromTime == 300) || (fromTime == 240) || (fromTime == 180) || (fromTime == 120) || (fromTime == 60) || (fromTime == 30) || (fromTime <= 10))
		{

			String timeLeftMsg = "";

			if(fromTime == 300)
			{
				timeLeftMsg = "5 minutter";
			}
			if(fromTime == 240)
			{
				timeLeftMsg = "4 minutter";
			}
			if(fromTime == 180)
			{
				timeLeftMsg = "3 minutter";
			}
			if(fromTime == 120)
			{
				timeLeftMsg = "2 minutter";
			}
			if(fromTime == 60)
			{
				timeLeftMsg = "1 minutt";
			}
			if(fromTime <= 30 && fromTime > 1)
			{
				timeLeftMsg = fromTime + " sekunder";
			}
			if(fromTime == 1)
			{
				timeLeftMsg = "1 sekund";
			}
			
			Object[] partic = plugin.participants.keySet().toArray();
			
			for(int i = 0; i < partic.length; i++) {
				
				if(plugin.getServer().getPlayer(partic[i].toString()) instanceof Player) {
					plugin.getServer().getPlayer(partic[i].toString()).sendMessage(plugin.chatPrefix + "Det er " + timeLeftMsg + " igjen av fisketiden...");
				}
				
			}
		}

		final int newTime = fromTime - 1;

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
		{

			public void run()
			{
				fishingTicker(newTime);
			}
		}, 20L);

	}

}
