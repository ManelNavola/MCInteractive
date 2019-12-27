package com.manelnavola.mcinteractive.adventure;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.manelnavola.mcinteractive.adventure.customenchants.CustomEnchant;
import com.manelnavola.mcinteractive.adventure.customitems.CustomItem;
import com.manelnavola.mcinteractive.adventure.customitems.SubGift;
import com.manelnavola.mcinteractive.generic.PlayerData;
import com.manelnavola.mcinteractive.generic.PlayerManager;
import com.manelnavola.mcinteractive.utils.MessageSender;
import com.manelnavola.twitchbotx.events.TwitchSubscriptionEvent.SubPlan;

public class RewardManager {
	
	public static void process(List<Player> channelPlayers, int months, SubPlan subPlan, String gifterNickname) {
		int tier = 0;
		if (subPlan == SubPlan.LEVEL_2) {
			tier = 1;
		} else if (subPlan == SubPlan.LEVEL_3) {
			tier = 2;
		}
		
		gifterNickname = gifterNickname.replace(' ', '_');
		
		if (months >= 12) { tier++; }
		if (months >= 24) { tier++; }
		if (months >= 32) { tier++; }
		if (tier > 3) { tier = 3; }
		
		for (Player p : channelPlayers) {
			PlayerData pd = PlayerManager.getPlayerData(p);
			if (pd.getConfig("subgifts")) {
				giftCustomItem(p, new SubGift(), tier, gifterNickname);
			}
		}
	}
	
	public static ItemStack giftRandomCustomItem(Player p, String gifterNickname, int t) {
		List<CustomItem> cil = CustomItemManager.getCustomItemTiers(t);
		if (cil.isEmpty()) {
			p.playSound(p.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 1, 1);
			p.getLocation().getWorld().spawnParticle(Particle.SMOKE_NORMAL, 
					p.getLocation().add(0, 1, 0), 5, 0, 0, 0, 0.2);
			MessageSender.info(p, ChatColor.GRAY + "Sorry, nothing?");
			return null;
		}
		int rand = (int)(Math.random()*cil.size());
		CustomItem ci = cil.get(rand);
		if (ci instanceof CustomEnchant) {
			rand = (int)(Math.random()*cil.size());
			ci = cil.get(rand);
		}
		if (p != null) {
			giftCustomItem(p, ci, t, gifterNickname);
			return null;
		} else {
			ItemStack is = ci.getRarity(t);
			ci.fixDisplayName(is, gifterNickname, t);
			return is;
		}
	}
	
	public static void giftCustomItem(Player p, CustomItem ci, int tier, String gifterNickname) {
		ItemStack is = ci.getRarity(tier);
		ci.fixDisplayName(is, gifterNickname, tier);
		if (ci.getClass() != SubGift.class) {
			String cn = ci.getCustomName(gifterNickname, tier);
			if (cn != null) {
				MessageSender.info(p, "You found " + cn);
			} else {
				MessageSender.info(p, "You found " + is.getItemMeta().getDisplayName());
			}
		}
		if (p.getInventory().firstEmpty() == -1) {
			// Full
			p.getWorld().dropItemNaturally(p.getLocation(), is);
			MessageSender.info(p, "Inventory full! Sub reward dropped on ground");
		} else {
			p.getInventory().addItem(is);
			PlayerManager.updateInventory(p);
		}
	}
	
}
