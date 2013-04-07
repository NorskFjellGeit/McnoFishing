package no.mcno.fishing;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class McnoFishing extends JavaPlugin
{

	public final Logger logger = Logger.getLogger("Minecraft");
	
	public String chatPrefix = "" + ChatColor.GOLD + ChatColor.BOLD + "[" + ChatColor.BLUE + ChatColor.BOLD + "FISK" + ChatColor.GOLD + ChatColor.BOLD + "] " + ChatColor.RESET + ChatColor.WHITE;

	private final PlayerListener playerListener = new PlayerListener(this);
	private final Countdown Countdown = new Countdown(this);

	public HashMap<String, Integer> fishCount = new HashMap<String, Integer>();

	public HashMap<String, Boolean> participants = new HashMap<String, Boolean>();

	public HashMap<String, Location> playerPosition = new HashMap<String, Location>();
	public HashMap<String, ItemStack[]> playerInventory = new HashMap<String, ItemStack[]>();
	public HashMap<String, ItemStack[]> playerArmor = new HashMap<String, ItemStack[]>();

	public static boolean fishingActive = false;
	public static boolean fishingRunning = false;
	
	public static String topFisher = null;
	public static int topFishercount = 0;

	public static int fishDuration = 300;
	public static int fishJoinDuration = 60;
	
	
	public HashMap<String, Integer> caughtFish = new HashMap<String, Integer>();
	public HashMap<String, Integer> ignoreWarning = new HashMap<String, Integer>();
	

	@Override
	public void onDisable()
	{
		logger.info(getName() + " is disabled!");
		cancelFishing();
	}

	@Override
	public void onEnable()
	{
		
		logger.info(getName() + " is enabled!");
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(playerListener, this);

		this.getConfig().addDefault("fishing-duration", 300);
		this.getConfig().addDefault("fishing-join-duration", 60);
		this.getDataFolder().mkdirs();
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();

		fishDuration = this.getConfig().getInt("fishing-duration");
		fishJoinDuration = this.getConfig().getInt("fishing-join-duration");

	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] arg)
	{

		Player player = null;
		if((sender instanceof Player))
		{
			player = (Player)sender;
		}

		if((cmd.getName().equalsIgnoreCase("setfishpos")))
		{

			getConfig().createSection("fishpos");
			getConfig().getConfigurationSection("fishpos").set("world", player.getWorld().getName());
			getConfig().getConfigurationSection("fishpos").set("x", player.getLocation().getX());
			getConfig().getConfigurationSection("fishpos").set("y", player.getLocation().getY());
			getConfig().getConfigurationSection("fishpos").set("z", player.getLocation().getZ());

			saveConfig();

			player.sendMessage(chatPrefix+"Satt fiskeposisjonen!");

			return true;

		}

		if((cmd.getName().equalsIgnoreCase("fishing")))
		{

			boolean status = toggleSystem();

			if(status)
			{
				//player.sendMessage(chatPrefix + ChatColor.GREEN + "Fisking systemet startet!");
			}
			else
			{
				player.sendMessage(chatPrefix + ChatColor.RED + "Fisking systemet stoppet!");
			}
			return true;
		}

		if((cmd.getName().equalsIgnoreCase("fisk")))
		{
			joinFishing(player.getName());
			return true;
		}
		
		if((cmd.getName().equalsIgnoreCase("testing")))
		{
			//getWinner(player.getName());
			return true;
		}

		return false;
	}

	public int countFish(Player player)
	{
		if(this.fishCount.containsKey(player.getName()))
		{
			return this.fishCount.get(player.getName());
		}
		else
		{
			return 0;
		}
	}

	public boolean toggleSystem()
	{

		if(fishingRunning)
		{
			fishingRunning = false;
			stopSystem();
			return false;
		}
		else
		{
			fishingRunning = true;
			fishingActive = true;
			startSystem();
			return true;
		}

	}

	public void startSystem()
	{

		Countdown.ticker(getConfig().getInt("fishing-join-duration"));

	}

	public void stopSystem()
	{
		this.getServer().broadcastMessage(chatPrefix + ChatColor.RED + "FISKINGEN BLE AVBRUTT!");
		cancelFishing();
	}

	public boolean joinFishing(String playername)
	{
		if(fishingActive)
		{
			
			Player player = getServer().getPlayer(playername);
			
			if(!(player instanceof Player)) { return false; }
			
			String pName = player.getName();

			if(!(participants.containsKey(pName)))
			{

				player.sendMessage(chatPrefix + "Du meldte deg på fiskekonkurransen!");
				participants.put(pName, true);

				this.getServer().broadcastMessage(chatPrefix + player.getDisplayName() + ChatColor.GREEN + " meldte seg på! " + participants.size() + " er påmeldt.");

			}
			else
			{
				player.sendMessage(chatPrefix + "Du er allerede påmeldt!");
			}

		}
		else
		{

			getServer().getPlayer(playername).sendMessage(chatPrefix + ChatColor.RED + "Kan ikke melde seg på fisking nå!");

		}

		return false;

	}

	public void pickFishers()
	{
		
		logger.info("Fisking: "+participants.size());		
		
		if(participants.size() < 3)
		{

			getServer().broadcastMessage(chatPrefix + ChatColor.RED + "Det var ikke nok påmeldte til fisking... Kanselert.");

			fishingActive = false;
			fishingRunning = false;
			participants.clear();
			return;
		}
		else
		{

			Object[] joinedPlayers = participants.keySet().toArray();

			for(int i = 0; i < joinedPlayers.length; i++)
			{

				if((getServer().getPlayer(joinedPlayers[i].toString()) instanceof Player))
				{

					if((getServer().getPlayer(joinedPlayers[i].toString()) != null))
					{
						moveToFishing(getServer().getPlayer(joinedPlayers[i].toString()));
					}

				}

			}

			getServer().broadcastMessage(chatPrefix + ChatColor.GREEN + "FISKINGEN HAR STARTET!");
			
			fishingActive = false;
			fishingRunning = true;
			
			Countdown.fishingTicker(this.getConfig().getInt("fishing-duration"));
			
			return;
		}

	}

	public void moveToFishing(Player player)
	{

		World fishWorld = getServer().getWorld(getConfig().getConfigurationSection("fishpos").getString("world"));

		double fishX = getConfig().getConfigurationSection("fishpos").getDouble("x");
		double fishY = getConfig().getConfigurationSection("fishpos").getDouble("y");
		double fishZ = getConfig().getConfigurationSection("fishpos").getDouble("z");

		Location fishLoc = new Location(fishWorld, fishX, fishY, fishZ);

		playerPosition.put(player.getName(), player.getLocation());
		playerInventory.put(player.getName(), player.getInventory().getContents());
		playerArmor.put(player.getName(), player.getInventory().getArmorContents());

		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.teleport(fishLoc);

		ItemStack fishingRod = new ItemStack(Material.FISHING_ROD);

		player.getInventory().addItem(fishingRod);

	}
	
	public void restorePlayer(String playerName) {
		
		Player player = this.getServer().getPlayer(playerName);
		
		if(player == null) { logger.warning("Kunne ikke restore spilleren "+playerName); return; }
		
		
		if(participants.containsKey(player.getName())) {
		
			Location oldLocation = playerPosition.get(player.getName());
			
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			
			player.teleport(oldLocation);
			
			if(playerInventory.containsKey(player.getName())) {
				
			for(int i1 = 0; i1 < playerInventory.get(player.getName()).length; i1++)
			{
				if(playerInventory.get(player.getName())[i1] != null)
				{
					player.getInventory().addItem(playerInventory.get(player.getName())[i1]);
				}
			}
			
			playerInventory.remove(player.getName());
			}
			
			if(playerArmor.containsKey(player.getName())) {			
				player.getInventory().setArmorContents(playerArmor.get(player.getName()));
				playerArmor.remove(player.getName());
			}
			
			participants.remove(player.getName());
			fishCount.remove(player.getName());
			playerPosition.remove(player.getName());
			
			if(McnoFishing.topFisher == player.getName()) {
				McnoFishing.topFisher = null;
			}
			
			
		}
	}
	
	public void completeFishing() {
		
		this.getServer().broadcastMessage(chatPrefix + ChatColor.GOLD + "FISKINGEN ER OVER!");
		
		Object[] joinedPlayers = participants.keySet().toArray();

		for(int i = 0; i < joinedPlayers.length; i++)
		{
			
			returnPlayer(joinedPlayers[i].toString());

		}
		
		getWinner(topFisher);
		
		fishingRunning = false;
		fishingActive = false;
		participants.clear();
		fishCount.clear();
		playerPosition.clear();
		topFishercount = 0;
		topFisher = null;
				
	}
	
	public void cancelFishing() {
				
		Object[] joinedPlayers = participants.keySet().toArray();

		for(int i = 0; i < joinedPlayers.length; i++)
		{
			
			returnPlayer(joinedPlayers[i].toString());

		}
		
		fishingRunning = false;
		fishingActive = false;
		participants.clear();
		fishCount.clear();
		playerPosition.clear();
		topFishercount = 0;
		topFisher = null;
				
	}
	
	public void returnPlayer(String playername) {
		
		Player player = null;
		
		if((getServer().getPlayer(playername) instanceof Player))
		{
			player = getServer().getPlayer(playername);			
		}

			if((player == null)) { return; }
				
				Location oldLocation = playerPosition.get(playername);
				
				player.getInventory().clear();
				player.getInventory().setArmorContents(null);
				
				player.teleport(oldLocation);
				
				
				if(playerInventory.containsKey(playername)) {
					
				for(int i1 = 0; i1 < playerInventory.get(playername).length; i1++)
				{
					if(playerInventory.get(playername)[i1] != null)
					{
						player.getInventory().addItem(playerInventory.get(playername)[i1]);
					}
				}
				
				playerInventory.remove(playername);
				}
				
				if(playerArmor.containsKey(playername)) {
					player.getInventory().setArmorContents(playerArmor.get(playername));
					playerArmor.remove(playername);
				}

	}
	
	public void getWinner(String winner) {
		
		if((winner == null)) {
			this.getServer().broadcastMessage(chatPrefix + ChatColor.RED + "Ingen som vant denne runden!");
			return;
		}		
		
		Player player = null;
		
		if((getServer().getPlayer(winner) instanceof Player)) {
			player = getServer().getPlayer(winner);
		}
		
		if((McnoFishing.topFishercount == 0)) {
			this.getServer().broadcastMessage(chatPrefix + ChatColor.RED + "Ingen som vant denne runden!");
			return;
		}
		
		if((player == null)) {
			this.getServer().broadcastMessage(chatPrefix + ChatColor.RED + "Ingen som vant denne runden!");
			return;
		}
		
		logger.info("FINN VINNER");
		
		player = getServer().getPlayer(winner);
		
		int fishesCatched = McnoFishing.topFishercount;
		
		this.getServer().broadcastMessage(chatPrefix + ChatColor.GREEN + player.getDisplayName() + " vant fiskekonkurransen!");
		this.getServer().broadcastMessage(chatPrefix + ChatColor.GREEN + "Og vant 3 slimeballs og "+fishesCatched+" ferdigstekt fisk! :D");
		
		int totalFish = 0;		
		totalFish = (int) fishesCatched;
		
		logger.info("GI PREMIE");
		
		ItemStack premie1 = new ItemStack(Material.SLIME_BALL, 3);
		//ItemStack premie2 = new ItemStack(Material.DIAMOND, 3);
		ItemStack premie3 = new ItemStack(Material.COOKED_FISH, totalFish);
		
		player.getInventory().addItem(premie1);
		//player.getInventory().addItem(premie2);
		player.getInventory().addItem(premie3);
		
		logger.info("GA PREMIE");
		
	}
	

}
