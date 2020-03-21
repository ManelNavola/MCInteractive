package com.manelnavola.mcinteractive.adventure;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.manelnavola.mcinteractive.Main;
import com.manelnavola.mcinteractive.generic.PlayerData;
import com.manelnavola.mcinteractive.generic.PlayerManager;
import com.manelnavola.mcinteractive.utils.MessageSender;

public class BitsNatural {
	
	private static Map<EntityType, EntityBitsData> entityBitsDataMap;
	private static Map<String, Map<EntityType, EntityPenaltyTrack>> delays;
	
	public static void init() {
		entityBitsDataMap = new HashMap<>();
		
		// bitsMin, bitsMax, penalty(s), increasedPenalty(s), increasePenaltyWhen, chancePerThousand(x/1000)
		
		// Common mobs
		entityBitsDataMap.put(EntityType.SILVERFISH,		new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put(EntityType.ZOMBIE,			new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put(EntityType.ZOMBIE_VILLAGER,	new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put(EntityType.SKELETON,			new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put(EntityType.SPIDER,			new EntityBitsData( 2,  6,  2,  20, 4,  150));
	 	
		entityBitsDataMap.put(EntityType.HUSK,				new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put(EntityType.STRAY,				new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put(EntityType.CAVE_SPIDER,		new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put(EntityType.ENDERMITE,			new EntityBitsData( 2,  6,  2,  20, 4,  150));
		entityBitsDataMap.put(EntityType.VEX,				new EntityBitsData( 2,  6,  2,  20, 8,  150));
		
		entityBitsDataMap.put(EntityType.PHANTOM,			new EntityBitsData( 2,  8,  4,  20, 4,  200));
		// Uncommon mobs
		entityBitsDataMap.put(EntityType.PIG_ZOMBIE,		new EntityBitsData( 2,  8,  4,  20, 4,  200));
		entityBitsDataMap.put(EntityType.DROWNED,			new EntityBitsData( 2,  8,  4,  20, 4,  200));
		if (!Main.isOn1_13()) {
			entityBitsDataMap.put(EntityType.PILLAGER,			new EntityBitsData( 2,  8,  4,  20, 4,  200));
		}
		entityBitsDataMap.put(EntityType.VINDICATOR,		new EntityBitsData( 2,  8,  4,  20, 4,  200));
		// Mid rare mobs
		entityBitsDataMap.put(EntityType.CREEPER,			new EntityBitsData( 2,  8,  4,  20, 4,  300));
		entityBitsDataMap.put(EntityType.ENDERMAN,			new EntityBitsData( 2,  8,  4,  20, 4,  300));
		
		entityBitsDataMap.put(EntityType.SLIME,				new EntityBitsData( 3,  12,  4,  20, 4,  150));
		entityBitsDataMap.put(EntityType.MAGMA_CUBE,		new EntityBitsData( 3,  12,  4,  20, 4,  150));
		
		entityBitsDataMap.put(EntityType.BLAZE,				new EntityBitsData( 3,  12,  4,  20, 4,  300));
		entityBitsDataMap.put(EntityType.WITCH,				new EntityBitsData( 3,  12,  4,  20, 4,  300));
		entityBitsDataMap.put(EntityType.WITHER_SKELETON,	new EntityBitsData( 3,  12,  4,  20, 4,  300));
		
		entityBitsDataMap.put(EntityType.SHULKER,			new EntityBitsData( 5, 30,  4,  20, 4,  300));
		entityBitsDataMap.put(EntityType.GUARDIAN,			new EntityBitsData( 5, 30,  4,  20, 4,  300));
		// Rare mobs
		entityBitsDataMap.put(EntityType.GHAST,				new EntityBitsData( 5, 30,  4, 100, 3,  500));
		if (!Main.isOn1_13())
		entityBitsDataMap.put(EntityType.RAVAGER,			new EntityBitsData( 5, 30,  4, 100, 3,  500));
		
		entityBitsDataMap.put(EntityType.EVOKER,			new EntityBitsData(50, 120,  8, 600, 2,  500));
		
		// Bosses
		entityBitsDataMap.put(EntityType.ELDER_GUARDIAN,	new EntityBitsData(50, 120, 600, 600, 1, 1000));
		entityBitsDataMap.put(EntityType.WITHER,			new EntityBitsData(800, 1600, 1800, 1800, 1, 1000));
		entityBitsDataMap.put(EntityType.ENDER_DRAGON,		new EntityBitsData(800, 1600, 1800, 1800, 1, 1000));
		
		delays = new HashMap<>();
	}

	public static void killEvent(Player p, EntityType et) {
		if (!getEntityBitsDataMap().containsKey(et)) return;
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

	public static Map<EntityType, EntityBitsData> getEntityBitsDataMap() {
		return entityBitsDataMap;
	}
	
	private static void tryRewardBits(Player p, EntityType et) {
		EntityBitsData ebd = getEntityBitsDataMap().get(et);
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
		ebd = BitsNatural.getEntityBitsDataMap().get(et);
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
