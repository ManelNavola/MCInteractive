package com.manelnavola.mcinteractive;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
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
import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.manelnavola.mcinteractive.adventure.BitsGUI;
import com.manelnavola.mcinteractive.adventure.BitsNatural;
import com.manelnavola.mcinteractive.adventure.CustomItemInfo;
import com.manelnavola.mcinteractive.adventure.CustomItemManager;
import com.manelnavola.mcinteractive.adventure.CustomItemsGUI;
import com.manelnavola.mcinteractive.adventure.EventManager;
import com.manelnavola.mcinteractive.command.CommandValidator;
import com.manelnavola.mcinteractive.command.MCICommand;
import com.manelnavola.mcinteractive.command.MCITabCompleter;
import com.manelnavola.mcinteractive.generic.ConfigGUI;
import com.manelnavola.mcinteractive.generic.ConfigManager;
import com.manelnavola.mcinteractive.generic.ConnectionManager;
import com.manelnavola.mcinteractive.generic.PlayerData;
import com.manelnavola.mcinteractive.generic.PlayerManager;
import com.manelnavola.mcinteractive.utils.Log;
import com.manelnavola.mcinteractive.utils.MessageSender;
import com.manelnavola.mcinteractive.voting.VoteManager;

public class Main extends JavaPlugin implements Listener {

	public static Plugin plugin;
	private static boolean ON_1_13 = false;
	public static String versionMismatch;
	public static final String INTERNAL_NAME = "Beta-1.0.4";

	private static HashMap<Player, Integer> lastArrowSlot = new HashMap<>();

	@Override
	public void onEnable() {
		plugin = this;

		ON_1_13 = Bukkit.getVersion().contains("1.13");

		// Init managers
		ConfigManager.init();
		PlayerManager.init(this);
		//LoggingManager.init(this);
		CustomItemManager.init(this);
		ConnectionManager.init(this);
		VoteManager.init(this);
		CommandValidator.init(this);
		BitsNatural.init();
		EventManager.init(this);
		//LoggingManager.l("Registered managers");
		
		// Register commands
		MCICommand mcic = new MCICommand(this);
		this.getCommand("mci").setExecutor(mcic);
		this.getCommand("mci").setTabCompleter(new MCITabCompleter(mcic));
		//LoggingManager.l("Registered commands");
		
		// Register events
		getServer().getPluginManager().registerEvents(this, this);
		//LoggingManager.l("Registered events");
		
		//LoggingManager.l("Try joining already existing players:");
		for (Player p : Bukkit.getOnlinePlayers()) {
			join(p);
		}

		// Nice!
		Log.nice("Enabled MCInteractive successfully!");
		//LoggingManager.l("Enabled MCInteractive successfully!");
		
		try {
			URL url = new URL("https://api.spiget.org/v2/resources/75068/versions?sort=-releaseDate&fields=name");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("User-Agent", "ManelNavola"); // Set User-Agent

			// If you're not sure if the request will be successful,
			// you need to check the response code and use getErrorStream if it returned an
			// error code
			InputStream inputStream = connection.getInputStream();
			InputStreamReader reader = new InputStreamReader(inputStream);

			// This could be either a JsonArray or JsonObject
			JsonElement element = new JsonParser().parse(reader);
			String latestVersion = element.getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
			if (latestVersion.equals(INTERNAL_NAME)) {
				Log.nice("Running on latest version " + INTERNAL_NAME);
			} else {
				versionMismatch = "Outdated version! Current version is " + INTERNAL_NAME + ", found new version " + latestVersion;
				Log.warn(versionMismatch);
			}
		} catch (Exception e) {
			Log.error("Could not fetch latest version!");
			e.printStackTrace();
		}
		 
	}

	private void join(Player p) {
		//LoggingManager.l("Performed join event on " + p.getName());
		String ch = PlayerManager.getConfigString("channellock");
		if (ch != null) {
			ConnectionManager.listen(p, ch);
		}
		PlayerManager.playerJoin(p);
		CommandValidator.addPlayer(p);
		clearEventEffects(p);
		if (p.isOp() && versionMismatch != null) {
			MessageSender.warn(p, versionMismatch);
		}
	}

	public static void clearEventEffects(Player p) {
		for (MetadataValue mv : p.getMetadata("MCI_WALKSPEED")) {
			if (mv.getOwningPlugin().equals(plugin)) {
				p.setWalkSpeed(0.2F);
				break;
			}
		}
		for (MetadataValue mv : p.getMetadata("MCI_POTIONEFFECT")) {
			if (mv.getOwningPlugin().equals(plugin)) {
				for (String s : mv.asString().split(",")) {
					PotionEffectType pet = PotionEffectType.getByName(s);
					if (pet != null)
						p.removePotionEffect(pet);
				}
				break;
			}
		}
	}

	@Override
	public void onDisable() {
		//LoggingManager.l("Disabling plugin...");
		PlayerManager.dispose();
		ConnectionManager.dispose();
		CustomItemManager.dispose();
		CommandValidator.dispose();
		VoteManager.dispose();
		EventManager.dispose();
		//LoggingManager.l("Plugin disabled");
		//LoggingManager.dispose();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent e) {
		LivingEntity le = e.getEntity();

		if (le.getKiller() != null) {
			Player p = le.getKiller();
			if (p == null) return;
			PlayerData pd = PlayerManager.getPlayerData(p);
			if (pd != null) {
				if (pd.getConfig("bitdrops")) {
					BitsNatural.killEvent(p, le.getType());
				}
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onFurnaceBurn(FurnaceBurnEvent e) {
		Furnace f = (Furnace) e.getBlock().getState();
		CustomItemInfo cii = new CustomItemInfo(f.getInventory().getFuel());
		if (cii.isValid() && !cii.isEnchant()) {
			CustomItemManager.onFurnaceBurn(cii, f, e);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockDispense(BlockDispenseEvent e) {
		CustomItemInfo cii = new CustomItemInfo(e.getItem());
		if (cii.isValid() && !cii.isEnchant() && e.getBlock().getType() == Material.DISPENSER) {
			CustomItemManager.onBlockDispense(cii, e);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityExplode(EntityExplodeEvent e) {
		if (e.getEntityType() == EntityType.PRIMED_TNT) {
			Entity ent = e.getEntity();
			for (MetadataValue mv : ent.getMetadata("MCI_FallEventDamageTnt")) {
				if (mv.getOwningPlugin().equals(this)) {
					float[] data = (float[]) mv.value();
					Location l = ent.getLocation();
					for (Entity ent2 : ent.getWorld().getNearbyEntities(l, data[0], data[0], data[0])) {
						if (ent2 instanceof Monster || ent2.getType() == EntityType.PLAYER) {
							if (ent2.getLocation().distance(l) <= data[0]) {
								((LivingEntity) ent2).damage(data[1]);
							}
						}
					}
					l.getWorld().playSound(l, Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);
					e.getEntity().remove();
					e.setCancelled(true);
					break;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityChangeBlockEvent(EntityChangeBlockEvent e) {
		if (e.getEntityType() == EntityType.FALLING_BLOCK) {
			FallingBlock fb = (FallingBlock) e.getEntity();
			for (MetadataValue mv : fb.getMetadata("MCI_FallEventDamage")) {
				if (mv.getOwningPlugin().equals(this)) {
					Location l = fb.getLocation();
					fb.remove();
					e.setCancelled(true);

					float[] data = (float[]) mv.value();
					for (Entity ent : fb.getWorld().getNearbyEntities(l, data[0], data[0], data[0])) {
						if (ent instanceof Monster || ent.getType() == EntityType.PLAYER) {
							if (ent.getLocation().distance(l) <= data[0]) {
								((LivingEntity) ent).damage(data[1]);
							}
						}
					}
					if (fb.getName().equals("stone")) {
						l.getWorld().playSound(l, Sound.BLOCK_STONE_FALL, 1F, 1F);
					} else if (fb.getName().equals("anvil")) {
						l.getWorld().playSound(l, Sound.BLOCK_ANVIL_LAND, 1F, 1F);
					} else {
						l.getWorld().playSound(l, Sound.BLOCK_SAND_FALL, 1F, 1F);
					}
					break;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if (damager.getType() == EntityType.PLAYER) {
			Player playerDamager = (Player) damager;
			CustomItemInfo cii = new CustomItemInfo(playerDamager.getInventory().getItemInMainHand());
			if (cii.isValid() && !cii.isEnchant()) {
				CustomItemManager.onEntityDamageByEntity(cii, playerDamager, e.getEntity());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onProjectileHit(ProjectileHitEvent e) {
		Entity ent = e.getEntity();
		for (MetadataValue mv : ent.getMetadata("MCI")) {
			if (mv.getOwningPlugin().equals(this)) {
				CustomItemManager.onProjectileHit(mv, ent, e.getHitBlock(), e.getHitEntity());
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getView() != null) {
			if (e.getView().getTitle().equals(BitsGUI.getTitle())) {
				BitsGUI.click(e);
				return;
			}
			if (e.getView().getTitle().equals(ConfigGUI.getTitle())) {
				ConfigGUI.click(e);
				return;
			}
			if (e.getView().getTitle().equals(ConfigGUI.getGlobalTitle())) {
				ConfigGUI.clickGlobal(e);
				return;
			}
			if (e.getView().getTitle().startsWith(CustomItemsGUI.getTitle())) {
				CustomItemsGUI.click(e);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPrepareAnvil(PrepareAnvilEvent e) {
		CustomItemManager.checkCustomEnchant(e);
	}

	@EventHandler(priority = EventPriority.MONITOR)
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

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerEggThrow(PlayerEggThrowEvent e) {
		Entity ent = e.getEgg();
		for (MetadataValue mv : ent.getMetadata("MCI")) {
			if (mv.getOwningPlugin().equals(this)) {
				e.setHatching(false);
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityShootBow(EntityShootBowEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;
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

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoinEvent(PlayerJoinEvent e) {
		join(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuitEvent(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		p.removeMetadata("MCI_CE_TEMP", this);
		lastArrowSlot.remove(p);
		if (ConnectionManager.isConnected(p)) {
			ConnectionManager.leave(p);
		}
		PlayerManager.playerQuit(e.getPlayer());
		CommandValidator.removePlayer(p);
		VoteManager.removePlayer(p);
	}

	public static boolean isOn1_13() {
		return ON_1_13;
	}

}
