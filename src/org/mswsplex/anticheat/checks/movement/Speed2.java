package org.mswsplex.anticheat.checks.movement;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.mswsplex.anticheat.checks.Check;
import org.mswsplex.anticheat.checks.CheckType;
import org.mswsplex.anticheat.data.CPlayer;
import org.mswsplex.anticheat.msws.AntiCheat;
import org.mswsplex.anticheat.utils.MSG;

/**
 * Checks jumping speed
 * 
 * @author imodm
 *
 */
public class Speed2 implements Check, Listener {

	private AntiCheat plugin;

	@Override
	public CheckType getType() {
		return CheckType.MOVEMENT;
	}

	@Override
	public void register(AntiCheat plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		CPlayer cp = plugin.getCPlayer(player);
		if (player.isFlying() || player.isInsideVehicle())
			return;

		if (cp.timeSince("disableFlight") < 2000)
			return;
		if (cp.timeSince("iceAndTrapdoor") < 1000)
			return;
		if (cp.hasMovementRelatedPotion())
			return;
		if (player.getFallDistance() > 4)
			return;
		if(cp.usingElytra())
			return;

		Location to = event.getTo(), from = event.getFrom();

		double dist = to.distanceSquared(from);

		if (dist < .7)
			return;

		if (plugin.devMode())
			MSG.tell(player, "&e" + dist);

		cp.flagHack(this, (int) Math.round((dist - .7) * 20));
	}

	@Override
	public String getCategory() {
		return "Speed";
	}

	@Override
	public String getDebugName() {
		return "Speed#2";
	}

	@Override
	public boolean lagBack() {
		return true;
	}

	@Override
	public boolean onlyLegacy() {
		// TODO Auto-generated method stub
		return false;
	}
}
