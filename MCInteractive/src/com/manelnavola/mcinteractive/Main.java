package com.manelnavola.mcinteractive;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.adventure.CustomItemManager;
import com.manelnavola.mcinteractive.adventure.RewardManager;
import com.manelnavola.mcinteractive.chat.VoteManager;
import com.manelnavola.mcinteractive.command.MCICommand;
import com.manelnavola.mcinteractive.command.MCITabCompleter;
import com.manelnavola.mcinteractive.generic.ConfigGUI;
import com.manelnavola.mcinteractive.generic.ConfigManager;
import com.manelnavola.mcinteractive.generic.ConnectionManager;
import com.manelnavola.mcinteractive.generic.PlayerManager;
import com.manelnavola.mcinteractive.utils.Log;
import com.manelnavola.twitchbotx.events.TwitchSubscriptionEvent.SubPlan;

public class Main extends JavaPlugin implements Listener {
	
	public static Plugin plugin;

	@Override
	public void onEnable() {
		plugin = this;
		
		// Register events
		getServer().getPluginManager().registerEvents(this, this);
		
		// Init managers
		ConfigManager.init();
		PlayerManager.init(this);
		CustomItemManager.init(this);
		ConnectionManager.init(this);
		VoteManager.init(this);
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			PlayerManager.playerJoin(p);
		}
		
		// Register commands
		this.getCommand("mci").setExecutor(new MCICommand());
		this.getCommand("mci").setTabCompleter(new MCITabCompleter(this));
		
		// Nice!
		Log.nice("Enabled MCInteractive successfully!");
	}
	
	@Override
	public void onDisable() {
		PlayerManager.dispose();
		ConnectionManager.dispose();
	}
	
	/*@EventHandler(priority=EventPriority.MONITOR)
	public void onEntityMount(EntityMountEvent e) {
		if (e.getEntity() instanceof Player) {
			if (ChatColor.stripColor(e.getMount().getCustomName()).startsWith("MCI46193762")) {
				CustomItemManager.onEntityMount((Player) e.getEntity(), e.getMount());
			}
		}
	}*/
	
	@EventHandler(priority=EventPriority.HIGH)
    public void onFurnaceBurn(FurnaceBurnEvent e) {
		Furnace f = (Furnace) e.getBlock().getState();
		f.setCookTimeTotal((short) 200);
		f.update();
		CustomItemInfo cii = new CustomItemInfo(f.getInventory().getFuel());
		if (cii.isValid()) {
			CustomItemManager.onFurnaceBurn(cii, f, e);
		}
    }
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onBlockDispense(BlockDispenseEvent e) {
		CustomItemInfo cii = new CustomItemInfo(e.getItem());
		if (cii.isValid() && e.getBlock().getType() == Material.DISPENSER) {
			CustomItemManager.onBlockDispense(cii, e);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if (damager instanceof Player) {
			Player playerDamager = (Player) damager;
			CustomItemInfo cii = new CustomItemInfo(playerDamager.getInventory().getItemInMainHand());
			if (cii.isValid()) {
				CustomItemManager.onEntityDamageByEntity(cii, playerDamager, e.getEntity());
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onProjectileHit(ProjectileHitEvent e) {
		Entity ent = e.getEntity();
		for (MetadataValue mv : ent.getMetadata("MCI")) {
			CustomItemManager.onProjectileHit(mv, ent, e.getHitBlock(), e.getHitEntity());
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent e) {
		if (e.getView().getTitle().equals(ConfigGUI.getTitle())) {
			ConfigGUI.click(e);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent e) {
		CustomItemInfo cii = new CustomItemInfo(e.getItem());
		if (cii.isValid()) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR) {
	        	CustomItemManager.onPlayerInteract(cii, e.getPlayer(), e);
	        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
	        	// Check if block has inventory
	        	if (!(e.getClickedBlock().getState() instanceof InventoryHolder)) {
	        		CustomItemManager.onPlayerInteract(cii, e.getPlayer(), e);
	        	}
	        }
		}
    }
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerEggThrow(PlayerEggThrowEvent e) {
		Entity ent = e.getEgg();
		if (ent.hasMetadata("MCI")) {
			e.setHatching(false);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerJoinEvent(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		PlayerManager.playerJoin(p);
		List<Player> thisPlayer = new ArrayList<>();
		thisPlayer.add(p);
		RewardManager.process(thisPlayer, 8, SubPlan.LEVEL_1, "antonio");
		RewardManager.process(thisPlayer, 8, SubPlan.LEVEL_2, "antonio");
		RewardManager.process(thisPlayer, 8, SubPlan.LEVEL_3, "antonio");
		RewardManager.process(thisPlayer, 14, SubPlan.LEVEL_3, "antonio");
		PlayerManager.updateInventory(p);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerQuitEvent(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (ConnectionManager.isConnected(p)) {
			ConnectionManager.leave(p);
		}
		PlayerManager.playerQuit(e.getPlayer());
	}
	
}
