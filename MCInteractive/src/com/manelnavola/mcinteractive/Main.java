package com.manelnavola.mcinteractive;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.adventure.CustomItemManager;
import com.manelnavola.mcinteractive.chat.VoteManager;
import com.manelnavola.mcinteractive.command.CommandValidator;
import com.manelnavola.mcinteractive.command.MCICommand;
import com.manelnavola.mcinteractive.command.MCITabCompleter;
import com.manelnavola.mcinteractive.generic.ConfigGUI;
import com.manelnavola.mcinteractive.generic.ConfigManager;
import com.manelnavola.mcinteractive.generic.ConnectionManager;
import com.manelnavola.mcinteractive.generic.PlayerManager;
import com.manelnavola.mcinteractive.utils.Log;

public class Main extends JavaPlugin implements Listener {
	
	public static Plugin plugin;
	
	private static HashMap<Player, Integer> lastArrowSlot = new HashMap<>();

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
		CommandValidator.init(this);
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			PlayerManager.playerJoin(p);
			CommandValidator.addPlayer(p);
		}
		
		// Register commands
		MCICommand mcic = new MCICommand();
		this.getCommand("mci").setExecutor(mcic);
		this.getCommand("mci").setTabCompleter(new MCITabCompleter(mcic));
		
		// Nice!
		Log.nice("Enabled MCInteractive successfully!");
	}
	
	@Override
	public void onDisable() {
		PlayerManager.dispose();
		ConnectionManager.dispose();
		CustomItemManager.dispose();
		CommandValidator.dispose();
	}
	
	@EventHandler(priority=EventPriority.HIGH)
    public void onFurnaceBurn(FurnaceBurnEvent e) {
		Furnace f = (Furnace) e.getBlock().getState();
		f.setCookTimeTotal((short) 200);
		f.update();
		CustomItemInfo cii = new CustomItemInfo(f.getInventory().getFuel());
		if (cii.isValid() && !cii.isEnchant()) {
			CustomItemManager.onFurnaceBurn(cii, f, e);
		}
    }
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onBlockDispense(BlockDispenseEvent e) {
		CustomItemInfo cii = new CustomItemInfo(e.getItem());
		if (cii.isValid() && !cii.isEnchant() && e.getBlock().getType() == Material.DISPENSER) {
			CustomItemManager.onBlockDispense(cii, e);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if (damager instanceof Player) {
			Player playerDamager = (Player) damager;
			CustomItemInfo cii = new CustomItemInfo(playerDamager.getInventory().getItemInMainHand());
			if (cii.isValid() && !cii.isEnchant()) {
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
		if (e.getView() != null) {
			if (e.getView().getTitle().equals(ConfigGUI.getTitle()))
				ConfigGUI.click(e);
			if (e.getView().getTitle().equals(ConfigGUI.getGlobalTitle()))
				ConfigGUI.clickGlobal(e);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPrepareAnvil(PrepareAnvilEvent e) {
		CustomItemManager.checkCustomEnchant(e);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent e) {
		CustomItemInfo cii = new CustomItemInfo(e.getItem());
		if (cii.isValid() && !cii.isEnchant()) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR) {
	        	CustomItemManager.onPlayerInteract(cii, e.getPlayer(), e);
	        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
	        	// Check if block has inventory
	        	if (!(e.getClickedBlock().getState() instanceof InventoryHolder)) {
	        		CustomItemManager.onPlayerInteract(cii, e.getPlayer(), e);
	        	}
	        }
		}
		
		// Find first arrow itemStack
		Player p = e.getPlayer();
		if (p.getInventory().getItemInOffHand() != null
				&& p.getInventory().getItemInOffHand().getType() == Material.ARROW) {
			lastArrowSlot.put(p, -1); 
		} else {
			for (int i = 0; i < p.getInventory().getSize(); i++) {
				ItemStack is = p.getInventory().getItem(i);
				if (is != null && is.getType() == Material.ARROW) {
					lastArrowSlot.put(p, i);
					break;
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
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onEntityShootBow(EntityShootBowEvent e) {
		if (e.getEntityType() != EntityType.PLAYER) return;
		Player player = (Player) e.getEntity();
		if (lastArrowSlot.containsKey(player)) {
			int slot = lastArrowSlot.get(player);
			ItemStack is;
			if (slot == -1) {
				is = player.getInventory().getItemInOffHand();
			} else {
				is = player.getInventory().getItem(slot);
			}
			CustomItemInfo cii = new CustomItemInfo(is);
			if (cii.isValid() && !cii.isEnchant()) {
				CustomItemManager.onEntityShootBow(player, e.getProjectile(), cii);
			}
		}
	}
	
	@EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
		Player player = (Player) e.getPlayer();
		CustomItemInfo cii = new CustomItemInfo(player.getInventory().getItemInMainHand());
		if (cii.isValid() && !cii.isEnchant()) {
			CustomItemManager.onBlockBreak(cii, player, e);
		}
    }
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerJoinEvent(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		PlayerManager.playerJoin(p);
		CommandValidator.addPlayer(p);
		PlayerManager.updateInventory(p);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerQuitEvent(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		lastArrowSlot.remove(p);
		if (ConnectionManager.isConnected(p)) {
			ConnectionManager.leave(p);
		}
		PlayerManager.playerQuit(e.getPlayer());
		CommandValidator.removePlayer(p);
	}
	
}
