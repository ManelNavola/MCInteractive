package com.manelnavola.mcinteractive.adventure;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class CustomTrail {
	
	private Particle particle;
	private int count;
	private double speed;
	private Vector offset;
	private int timeActive = 0;
	
	public CustomTrail(Particle p, int c, double spd, Vector off) {
		particle = p;
		count = c;
		speed = spd;
		offset = off;
	}
	
	public CustomTrail(Particle p, int c, double spd) {
		this(p, c, spd, new Vector(0, 0, 0));
	}
	
	public void showParticleEffect(Location loc) {
		loc.getWorld().spawnParticle(particle,
				loc, count,
				offset.getX(), offset.getY(), offset.getZ(),
				speed);
	}
	
	public boolean timeDue() {
		timeActive += 1;
		return timeActive > 400;
	}
	
}
