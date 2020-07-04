package com.manelnavola.mcinteractive.adventure;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.manelnavola.mcinteractive.generic.PlayerData;
import com.manelnavola.mcinteractive.generic.PlayerManager;
import com.manelnavola.mcinteractive.utils.MessageSender;

public class BitsNatural {
	
	private static Map<String, EntityBitsData> entityBitsDataMap;
	private static Map<String, Map<EntityType, EntityPenaltyTrack>> delays;
	
	public static void init() {
		entityBitsDataMap = new HashMap<>();
		
		// bitsMin, bitsMax, penalty(s), increasedPenalty(s), increasePenaltyWhen, chancePerThousand(x/1000)
		
		// Common mobs
		entityBitsDataMap.put("SILVERFISH",			new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put("ZOMBIE",				new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put("ZOMBIE_VILLAGER",	new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put("SKELETON",			new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put("SPIDER",				new EntityBitsData( 2,  6,  2,  20, 4,  150));
	 	
		entityBitsDataMap.put("HUSK",				new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put("STRAY",				new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put("CAVE_SPIDER",		new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put("ENDERMITE",			new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put("VEX",				new EntityBitsData( 2,  6,  2,  20, 8,  150));
		
		entityBitsDataMap.put("PHANTOM",			new EntityBitsData( 2,  8,  4,  20, 4,  200));
		// Uncommon mobs
		entityBitsDataMap.put("PIG_ZOMBIE",			new EntityBitsData( 2,  8,  4,  20, 4,  200));
		entityBitsDataMap.put("PIGLIN",				new EntityBitsData( 2,  8,  4,  20, 4,  200));		// >1.15
		entityBitsDataMap.put("ZOMBIFIED_PIGLIN",	new EntityBitsData( 2,  8,  4,  20, 4,  200));		// >1.15
		entityBitsDataMap.put("ZOGLIN",				new EntityBitsData( 2,  8,  4,  20, 4,  200));		// >1.15
		entityBitsDataMap.put("HOGLIN",				new EntityBitsData( 2,  8,  4,  20, 4,  200));		// >1.15
		entityBitsDataMap.put("DROWNED",			new EntityBitsData( 2,  8,  4,  20, 4,  200));
		entityBitsDataMap.put("PILLAGER",			new EntityBitsData( 2,  8,  4,  20, 4,  200)); 		// >1.13
		entityBitsDataMap.put("VINDICATOR",			new EntityBitsData( 2,  8,  4,  20, 4,  200));
		// Mid rare mobs
		entityBitsDataMap.put("CREEPER",			new EntityBitsData( 2,  8,  4,  20, 4,  300));
		entityBitsDataMap.put("ENDERMAN",			new EntityBitsData( 2,  8,  4,  20, 4,  300));
		
		entityBitsDataMap.put("SLIME",				new EntityBitsData( 3,  12,  4,  20, 4,  150));
		entityBitsDataMap.put("MAGMA_CUBE",			new EntityBitsData( 3,  12,  4,  20, 4,  150));
		
		entityBitsDataMap.put("BLAZE",				new EntityBitsData( 3,  12,  4,  20, 4,  300));
		entityBitsDataMap.put("WITCH",				new EntityBitsData( 3,  12,  4,  20, 4,  300));
		entityBitsDataMap.put("WITHER_SKELETON",	new EntityBitsData( 3,  12,  4,  20, 4,  300));
		
		entityBitsDataMap.put("SHULKER",			new EntityBitsData( 5, 30,  4,  20, 4,  300));
		entityBitsDataMap.put("GUARDIAN",			new EntityBitsData( 5, 30,  4,  20, 4,  300));
		// Rare mobs
		entityBitsDataMap.put("GHAST",				new EntityBitsData( 5, 30,  4, 100, 3,  500));
		entityBitsDataMap.put("RAVAGER",			new EntityBitsData( 5, 30,  4, 100, 3,  500)); 	// >1.13
		
		entityBitsDataMap.put("EVOKER",				new EntityBitsData(50, 120,  8, 600, 2,  500));
		
		// Bosses
		entityBitsDataMap.put("ELDER_GUARDIAN",		new EntityBitsData(50, 120, 600, 600, 1, 1000));
		entityBitsDataMap.put("WITHER",				new EntityBitsData(800, 1600, 1800, 1800, 1, 1000));
		entityBitsDataMap.put("ENDER_DRAGON",		new EntityBitsData(800, 1600, 1800, 1800, 1, 1000));
		
		delays = new HashMap<>();
	}

	public static void killEvent(Player p, EntityType et) {
		if (!getEntityBitsDataMap().containsKey(et.name())) return;
		Map<EntityType, EntityPenaltyTrack> eptm = delays.get(p.getUniqueId().toString());
		if (eptm == null) {
			eptm = new HashMap<>();
			delays.put(p.getUniqueId().toString(), eptm);
		} else {
			EntityPenaltyTrack ept = eptm.get(et);
			if (ept != null) {
				if (ept.valid()) {
					tryRewardBits(p, et);
					return;
				}
				MessageSender.info(p, "Issued invalid kill!");
				ept.issueKill();
				return;
			}
		}
		
		tryRewardBits(p, et);
		eptm.put(et, new EntityPenaltyTrack(et));
	}

	public static Map<String, EntityBitsData> getEntityBitsDataMap() {
		return entityBitsDataMap;
	}
	
	private static void tryRewardBits(Player p, EntityType et) {
		EntityBitsData ebd = getEntityBitsDataMap().get(et.name());
		if (ebd == null) {
			return;
		}
		int r = (int) (Math.random()*1000);
		if (r <= ebd.getChancePerThousand()) {
			int min = ebd.getBitsMin();
			int range = ebd.getBitsMax() - min;
			PlayerData pd = PlayerManager.getPlayerData(p);
			int fb = min + (int) (Math.random()*(range+1));
			pd.addBits(fb);
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 2);
			MessageSender.send(p, "You found " + ChatColor.AQUA + fb + " bits" + ChatColor.RESET + "!");
		}
	}
}

class EntityBitsData {
	
	private int bitsMin, bitsMax, penaltyMillis, increasedPenaltyMillis, increasePenaltyWhen, chancePerThousand;
	
	public EntityBitsData(int bitsMin, int bitsMax,
			int penalty, int increasedPenalty, int increasePenaltyWhen, int chancePerThousand) {
		this.bitsMin = bitsMin;
		this.bitsMax = bitsMax;
		this.chancePerThousand = chancePerThousand;
		this.penaltyMillis = penalty*1000;
		this.increasedPenaltyMillis = increasedPenalty*1000;
		this.increasePenaltyWhen = increasePenaltyWhen;
	}
	
	public int getPenaltyMillis() {
		return penaltyMillis;
	}

	public int getChancePerThousand() {
		return chancePerThousand;
	}

	public int getBitsMin() {
		return bitsMin;
	}

	public int getBitsMax() {
		return bitsMax;
	}

	public int getIncreasePenaltyWhen() {
		return increasePenaltyWhen;
	}

	public int getIncreasedPenaltyMillis() {
		return increasedPenaltyMillis;
	}
	
}

class EntityPenaltyTrack {
	
	private long nextValidMillis = -1;
	private EntityBitsData ebd;
	private int kills = 0;
	
	public EntityPenaltyTrack(EntityType et) {
		ebd = BitsNatural.getEntityBitsDataMap().get(et.name());
		if (ebd == null) {
			return;
		}
		issueKill();
	}
	
	public void issueKill() {
		if (valid()) {
			kills = 0;
		} else {
			kills++;
			if (kills >= ebd.getIncreasePenaltyWhen()) {
				nextValidMillis = System.currentTimeMillis() + ebd.getIncreasedPenaltyMillis();
				return;
			}
		}
		nextValidMillis = System.currentTimeMillis() + ebd.getPenaltyMillis();
	}
	
	public boolean valid() {
		return System.currentTimeMillis() > nextValidMillis;
	}
	
}
