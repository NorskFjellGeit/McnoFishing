package no.mcno.fishing;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class PlayerListener implements Listener
{	
	
	public static McnoFishing plugin;
	
	public PlayerListener(McnoFishing mcnoFishing)
	{
		plugin = mcnoFishing;
	}

	@EventHandler
	public void playerPickupItem(PlayerPickupItemEvent event) {
		
		if(!(event.getPlayer() instanceof Player)) { return; }
		if(!(plugin.participants.containsKey(event.getPlayer().getName()))) { return; }
				
		Player player = event.getPlayer();
		Item item = event.getItem();
		
		if(item.getItemStack().getType() == Material.RAW_FISH) {
			
			if(!(plugin.caughtFish.containsKey(player.getName()) && plugin.caughtFish.containsValue(item.getEntityId()))) {
				
				if(!(plugin.ignoreWarning.containsKey(player.getName()) && plugin.ignoreWarning.containsValue(item.getEntityId()))) {
					player.sendMessage(plugin.chatPrefix + ChatColor.RED + "Denne fisken har ikke du fisket i konkurransen!");
					plugin.ignoreWarning.put(player.getName(), item.getEntityId());
				}
				event.setCancelled(true);
				return;
			}
			
			int currentfish = 0;
			
			if(plugin.fishCount.containsKey(player.getName())) {
				int fishcounter = 0;
				fishcounter = plugin.fishCount.get(player.getName());
				fishcounter++;
				plugin.fishCount.put(player.getName(), fishcounter);
				currentfish = fishcounter;
			} else {
				plugin.fishCount.put(player.getName(), 1);
				currentfish = 1;
			}
			
			player.sendMessage(plugin.chatPrefix + "Du har " + ChatColor.GREEN + plugin.countFish(player) + ChatColor.RESET + " fisk!");
			
			if(currentfish > McnoFishing.topFishercount && currentfish > 0) {
				McnoFishing.topFisher = player.getName();
				McnoFishing.topFishercount = currentfish;
				plugin.getServer().broadcastMessage(plugin.chatPrefix + ChatColor.GREEN + McnoFishing.topFisher + " leder med " + currentfish + " fisk!");
			}
			
		}
		
	}

	@EventHandler
	public void playerChestInteraction(InventoryClickEvent event) {

		if(event.getInventory().getType().equals(InventoryType.PLAYER))
			return;
		
		if(!(event.getWhoClicked() instanceof Player)) { return; }
		if(!(plugin.participants.containsKey(event.getWhoClicked().getName()))) { return; }		
		
		Player player = (Player) event.getWhoClicked();
		
			event.setCancelled(true);
			player.sendMessage(plugin.chatPrefix + ChatColor.RED + "Ikke tillatt mens du fisker!");
		
	}
	
	@EventHandler
	public void playerDropItem(PlayerDropItemEvent event) {
		
		if(!(event.getPlayer() instanceof Player)) { return; }
		if(!(plugin.participants.containsKey(event.getPlayer().getName()))) { return; }
		
		
		Player player = event.getPlayer();
		
			event.setCancelled(true);
			player.sendMessage(plugin.chatPrefix + ChatColor.RED + "Dropping av ting er deaktivert mens du fisker!");
		
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{	
		if(McnoFishing.fishingActive) {
		
			if(plugin.participants.containsKey(event.getPlayer().getName())) {
				plugin.participants.remove(event.getPlayer().getName());
			}
			
		} else {
		plugin.restorePlayer(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onDeathEvent(EntityDeathEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			plugin.restorePlayer(player.getName());
		}
	}
	
	@EventHandler
	public void playerGetFish(PlayerFishEvent event) {
		
		if(event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
			
			event.getCaught().getWorld().playEffect(event.getCaught().getLocation(), Effect.ENDER_SIGNAL, 1);			
			plugin.caughtFish.put(event.getPlayer().getName(), event.getCaught().getEntityId());			
		}	
		
	}
	
	
}
