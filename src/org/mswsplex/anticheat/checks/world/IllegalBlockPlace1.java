package org.mswsplex.anticheat.checks.world;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.mswsplex.anticheat.checks.Check;
import org.mswsplex.anticheat.checks.CheckType;
import org.mswsplex.anticheat.data.CPlayer;
import org.mswsplex.anticheat.msws.AntiCheat;

/**
 * Checks if the block that a player places a block against is liquid
 * 
 * @author imodm
 *
 */
public class IllegalBlockPlace1 implements Check, Listener {

	private AntiCheat plugin;

	@Override
	public CheckType getType() {
		return CheckType.WORLD;
	}

	@Override
	public void register(AntiCheat plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		CPlayer cp = plugin.getCPlayer(player);
		if (!event.getBlockAgainst().isLiquid())
			return;

		cp.flagHack(this, 50);
	}

	@Override
	public String getCategory() {
		return "IllegalBlockPlace";
	}

	@Override
	public String getDebugName() {
		return "IllegalBlockPlace#1";
	}

	@Override
	public boolean lagBack() {
		return false;
	}

	@Override
	public boolean onlyLegacy() {
		// TODO Auto-generated method stub
		return false;
	}
}
