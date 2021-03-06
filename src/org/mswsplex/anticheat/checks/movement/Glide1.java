package org.mswsplex.anticheat.checks.movement;

import org.bukkit.Bukkit;
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
 * Checks if a player's fall velocity is less than the player's previous fall
 * velocity
 * 
 * @author imodm
 *
 */
public class Glide1 implements Check, Listener {

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

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		CPlayer cp = plugin.getCPlayer(player);

		if (cp.isInClimbingBlock() || cp.isInWeirdBlock() || player.isFlying() || cp.isOnGround()
				|| player.isOnGround())
			return;

		if (cp.timeSince("wasFlying") < 1000)
			return;

		if (cp.timeSince("lastOnGround") < 500 || cp.timeSince("lastFlightGrounded") < 1000)
			return;

		if (cp.timeSince("lastLiquid") < 500)
			return;

		double fallDist = event.getFrom().getY() - event.getTo().getY();

		if (!cp.hasTempData("previousFall")) {
			cp.setTempData("previousFall", fallDist);
			return;
		}

		if (fallDist == 0 || player.getFallDistance() == 0) {
			cp.removeTempData("previousFall");
			return;
		}

		double previousFall = cp.getTempDouble("previousFall");

		if (fallDist >= previousFall)
			return;

		MSG.tell(player, "fall: " + player.getFallDistance() + " dist: " + fallDist + " previous: " + previousFall);
		cp.flagHack(this, 5);
	}

	@Override
	public String getCategory() {
		return "Glide";
	}

	@Override
	public String getDebugName() {
		return "Glide#1";
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
