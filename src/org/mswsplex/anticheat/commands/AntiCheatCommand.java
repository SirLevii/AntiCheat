package org.mswsplex.anticheat.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.mswsplex.anticheat.checks.Check;
import org.mswsplex.anticheat.checks.CheckType;
import org.mswsplex.anticheat.data.CPlayer;
import org.mswsplex.anticheat.msws.AntiCheat;
import org.mswsplex.anticheat.utils.MSG;
import org.mswsplex.anticheat.utils.Sounds;

public class AntiCheatCommand implements CommandExecutor, TabCompleter {
	private AntiCheat plugin;

	public AntiCheatCommand(AntiCheat plugin) {
		this.plugin = plugin;
		plugin.getCommand("anticheat").setExecutor(this);
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			MSG.sendHelp(sender, 0, "default");
			return true;
		}
		OfflinePlayer off;
		CPlayer cp;

		switch (args[0].toLowerCase()) {
		case "clear":
			if (!sender.hasPermission("anticheat.command.clear")) {
				MSG.noPerm(sender, "anticheat.command.clear");
				return true;
			}
			if (args.length < 3) {
				MSG.tell(sender, "/anticheat clear [player/all] [hack/all]");
				return true;
			}
			String target = "", hack = "";

			if (args[1].equalsIgnoreCase("all")) {
				target = "everyone's";

				for (OfflinePlayer p : plugin.getPlayerManager().getLoadedPlayers()) {
					cp = plugin.getCPlayer(p);
					if (args[2].equalsIgnoreCase("all")) {
						cp.clearVls();
						MSG.sendPluginMessage(null, "clearvl:" + p.getName());
						hack = "all hacks";
					} else {
						for (String h : cp.getHackVls()) {
							if (args[2].equalsIgnoreCase(h)) {
								cp.setSaveData("vls." + h, 0);
								MSG.sendPluginMessage(null, "setvl:" + p.getName() + " " + h + " 0");
								hack = h;
								break;
							}
						}
					}
				}
			} else {
				cp = plugin.getCPlayer(Bukkit.getOfflinePlayer(args[1]));
				target = cp.getPlayer().getName() + "'"
						+ (cp.getPlayer().getName().toLowerCase().endsWith("s") ? "" : "s");

				if (args[2].equalsIgnoreCase("all")) {
					cp.clearVls();
					MSG.sendPluginMessage(null, "clearvl:" + cp.getPlayer().getName());
					hack = "all hacks";
				} else {
					for (String h : cp.getHackVls()) {
						if (args[2].equalsIgnoreCase(h)) {
							cp.setSaveData("vls." + h, 0);
							MSG.sendPluginMessage(null, "setvl:" + cp.getPlayer().getName() + " " + h + " 0");
							hack = h;
							break;
						}
					}
				}
			}

			MSG.tell(sender, "&7You cleared &e" + target + "&7 VLs for &c" + hack);
			break;
		case "vl":
			if (!sender.hasPermission("anticheat.command.vl")) {
				MSG.noPerm(sender, "anticheat.command.vl");
				return true;
			}
			if (args.length == 1) {
				boolean shown = false;
				for (OfflinePlayer p : plugin.getPlayerManager().getLoadedPlayers()) {
					cp = plugin.getCPlayer(p);
					String vls = formatVls(p);
					if (vls.isEmpty())
						continue;
					shown = true;
					MSG.tell(sender, "&5[&d" + cp.getTotalVL() + "&5] &e" + p.getName() + "&7: " + formatVls(p));
				}
				if (!shown) {
					MSG.tell(sender, "&5[&dVLS&5] &cThere are no VLs.");
				}
				return true;
			}

			OfflinePlayer t = Bukkit.getOfflinePlayer(args[1]);

			cp = plugin.getCPlayer(t);

			if (formatVls(t).isEmpty()) {
				MSG.tell(sender, "&5[&d" + cp.getTotalVL() + "&5] &e" + t.getName() + " &chas no VLs.");
				return true;
			}

			MSG.tell(sender, "&5[&d" + cp.getTotalVL() + "&5] &e" + t.getName() + "&7: " + formatVls(t));
			break;
		case "reload":
			if (!sender.hasPermission("anticheat.command.reload")) {
				MSG.noPerm(sender, "anticheat.command.reload");
				return true;
			}
			plugin.configYml = new File(plugin.getDataFolder(), "config.yml");
			plugin.config = YamlConfiguration.loadConfiguration(plugin.configYml);
			plugin.langYml = new File(plugin.getDataFolder(), "lang.yml");
			plugin.lang = YamlConfiguration.loadConfiguration(plugin.langYml);
			plugin.guiYml = new File(plugin.getDataFolder(), "guis.yml");
			plugin.gui = YamlConfiguration.loadConfiguration(plugin.guiYml);
			MSG.tell(sender, MSG.getString("Reloaded", "Successfully reloaded."));
			break;
		case "toggle":
			if (!sender.hasPermission("anticheat.command.toggle")) {
				MSG.noPerm(sender, "anticheat.command.toggle");
				return true;
			}
			if (args.length < 2) {
				MSG.sendHelp(sender, 0, "default");
				return true;
			}
			switch (args[1].toLowerCase()) {
			case "dev":
				if (!sender.hasPermission("anticheat.command.toggle.dev")) {
					MSG.noPerm(sender, "anticheat.command.toggle.dev");
					return true;
				}
				plugin.config.set("DevMode", !plugin.devMode());
				MSG.tell(sender, "dev: " + MSG.TorF(plugin.devMode()));
				plugin.saveConfig();
				break;
			case "lagback":
			case "cancel":
				if (!sender.hasPermission("anticheat.command.toggle.cancel")) {
					MSG.noPerm(sender, "anticheat.command.toggle.cancel");
					return true;
				}
				plugin.config.set("LagBack", !plugin.config.getBoolean("LagBack"));
				MSG.tell(sender, "cancel: " + MSG.TorF(plugin.config.getBoolean("LagBack")));
				plugin.saveConfig();
				break;
			case "logs":
				if (!sender.hasPermission("anticheat.command.toggle.logs")) {
					MSG.noPerm(sender, "anticheat.command.toggle.logs");
					return true;
				}
				plugin.config.set("Log", !plugin.config.getBoolean("Log"));
				MSG.tell(sender, "logs: " + MSG.TorF(plugin.config.getBoolean("Log")));
				plugin.saveConfig();
				break;
			case "global":
				if (!sender.hasPermission("anticheat.command.toggle.global")) {
					MSG.noPerm(sender, "anticheat.command.toggle.global");
					return true;
				}
				plugin.config.set("Global", !plugin.config.getBoolean("Global"));
				MSG.tell(sender, "global: " + MSG.TorF(plugin.config.getBoolean("Global")));
				plugin.saveConfig();
				break;
			case "scoreboard":
				if (!sender.hasPermission("anticheat.command.toggle.scoreboard")) {
					MSG.noPerm(sender, "anticheat.command.toggle.scoreboard");
					return true;
				}
				if (!(sender instanceof Player)) {
					MSG.tell(sender, "no scoreboard 4 u");
					return true;
				}
				cp = plugin.getCPlayer(((Player) sender));
				cp.setSaveData("scoreboard",
						cp.hasSaveData("scoreboard") ? !cp.getSaveData("scoreboard", Boolean.class) : true);
				MSG.tell(sender, "Scoreboard: " + MSG.TorF(cp.getSaveData("scoreboard", Boolean.class)));
				((Player) sender).setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
				break;
			case "pastebin":
				if (!sender.hasPermission("anticheat.command.toggle.pastebin")) {
					MSG.noPerm(sender, "anticheat.command.toggle.pastebin");
					return true;
				}
				plugin.config.set("Pastebin", !plugin.config.getBoolean("Pastebin"));
				MSG.tell(sender, "Pastebin: " + MSG.TorF(plugin.config.getBoolean("Pastebin")));
				plugin.saveConfig();
				break;
			}
			break;
		case "reset":
			if (!sender.hasPermission("anticheat.command.reset")) {
				MSG.noPerm(sender, "anticheat.command.reset");
				return true;
			}
			plugin.saveResource("config.yml", true);
			plugin.saveResource("lang.yml", true);
			plugin.saveResource("guis.yml", true);
			plugin.configYml = new File(plugin.getDataFolder(), "config.yml");
			plugin.langYml = new File(plugin.getDataFolder(), "lang.yml");
			plugin.config = YamlConfiguration.loadConfiguration(plugin.configYml);
			plugin.lang = YamlConfiguration.loadConfiguration(plugin.langYml);
			plugin.guiYml = new File(plugin.getDataFolder(), "guis.yml");
			plugin.gui = YamlConfiguration.loadConfiguration(plugin.guiYml);
			MSG.tell(sender, "Succesfully reset.");
			break;
		case "time":
			if (!sender.hasPermission("anticheat.command.time")) {
				MSG.noPerm(sender, "anticheat.command.time");
				return true;
			}
			MSG.tell(sender, "&4&l[&c&lNOPE&4&l] &7Next banwave: &e"
					+ MSG.getTime((double) plugin.getBanwave().timeToNextBanwave()));
			break;
		case "banwave":
			if (!sender.hasPermission("anticheat.command.banwave")) {
				MSG.noPerm(sender, "anticheat.command.banwave");
				return true;
			}
			if (args.length > 1) {
				if (!sender.hasPermission("anticheat.command.banwave.addplayer")) {
					MSG.noPerm(sender, "anticheat.command.banwave.addplayer");
					return true;
				}
				off = Bukkit.getOfflinePlayer(args[1]);
				cp = plugin.getCPlayer(off);
				cp.setSaveData("isBanwaved", "Manual [" + sender.getName() + "]");
				MSG.sendPluginMessage(null, "banwave:" + off.getName() + " Manual");
				MSG.tell(sender, "Added " + off.getName() + " to the banwave.");
				return true;
			}
			MSG.sendPluginMessage(null, "banwave");
			plugin.getBanwave().runBanwave(true).run();
			MSG.tell(sender, "&cSuccessfully initiated banwave.");
			break;
		case "removebanwave":
			if (!sender.hasPermission("anticheat.command.removebanwave")) {
				MSG.noPerm(sender, "anticheat.command.removebanwave");
				return true;
			}
			if (args.length < 2) {
				MSG.tell(sender, "You must specify a player");
				return true;
			}
			off = Bukkit.getOfflinePlayer(args[1]);

			cp = plugin.getCPlayer(off);
			if (!cp.hasSaveData("isBanwaved")) {
				MSG.tell(sender, off.getName() + " is not banwaved.");
				return true;
			}
			MSG.sendPluginMessage(null, "removebanwave:" + off.getName());
			cp.removeSaveData("isBanwaved");
			MSG.tell(sender, "Removed " + off.getName() + " from the banwave.");
			break;
		case "warn":
		case "flag":
			if (!sender.hasPermission("anticheat.command.warn")) {
				MSG.noPerm(sender, "anticheat.command.warn");
				return true;
			}
			if (args.length < 4) {
				MSG.sendHelp(sender, 0, "default");
				return true;
			}
			t = Bukkit.getOfflinePlayer(args[1]);
			cp = plugin.getCPlayer(t);

			String hackName = "", stringVl = "", current = "";
			for (int i = 2; i < args.length; i++) {
				String arg = args[i];
				if (arg.startsWith("h:")) {
					current = "hack";
					hackName += arg.substring(2);
					continue;
				} else if (arg.startsWith("v:")) {
					current = "vl";
					stringVl += arg.substring(2);
					continue;
				}

				if (current.equals("hack")) {
					hackName += " " + arg;
				} else if (current.equals("vl")) {
					stringVl += " " + arg;
				}
			}

			final String hackNameFinal = hackName;

			cp.flagHack(new Check() {
				@Override
				public boolean lagBack() {
					return true;
				}

				@Override
				public CheckType getType() {
					return CheckType.MISC;
				}

				@Override
				public String getDebugName() {
					return "ManuallyIssued";
				}

				@Override
				public String getCategory() {
					return hackNameFinal;
				}

				@Override
				public void register(AntiCheat plugin) {
				}

				@Override
				public boolean onlyLegacy() {
					// TODO Auto-generated method stub
					return false;
				}

			}, Integer.parseInt(stringVl));

			MSG.tell(sender, "Warned " + t.getName() + " for " + hackName + " (vl: " + stringVl + ")");
			break;
		case "checks":
			if (!sender.hasPermission("anticheat.command.checks")) {
				MSG.noPerm(sender, "anticheat.command.checks");
				return true;
			}
			for (CheckType type : plugin.getChecks().getCheckTypes()) {
				HashMap<String, Integer> checks = new HashMap<>();
				for (Check check : plugin.getChecks().getChecksWithType(type))
					checks.put(check.getCategory(),
							checks.containsKey(check.getCategory()) ? checks.get(check.getCategory()) + 1 : 1);

				if (checks.isEmpty())
					continue;
				MSG.tell(sender, " ");

				StringBuilder builder = new StringBuilder();

				String[] colors = { "&b", "&a" };

				for (int i = 0; i < checks.keySet().size(); i++) {
					builder.append(colors[i % colors.length] + checks.keySet().toArray()[i] + " "
							+ checks.values().toArray()[i] + " ");
				}

				MSG.tell(sender, "&6&l" + MSG.camelCase(type.toString()) + " &7(&e&l"
						+ plugin.getChecks().getChecksWithType(type).size() + "&7) " + type.getDescription());
				MSG.tell(sender, builder.toString());
			}
			break;
		case "stats":
			if (!(sender instanceof Player)) {
				MSG.tell(sender, "You must be a player.");
				return true;
			}
			if (!sender.hasPermission("anticheat.command.stats")) {
				MSG.noPerm(sender, "anticheat.command.stats");
				return true;
			}

			Player player = (Player) sender;
			cp = plugin.getCPlayer(player);
			player.openInventory(plugin.getStats().getInventory());
			player.playSound(player.getLocation(), Sounds.CHEST_OPEN.bukkitSound(), 2, 1);
			cp.setTempData("openInventory", "stats");
			break;
		case "enablechecks":
			plugin.config.set("Checks", null);
			for (Check check : plugin.getChecks().getAllChecks()) {
				plugin.config.set("Checks." + MSG.camelCase(check.getType() + "") + ".Enabled", true);
				plugin.config.set(
						"Checks." + MSG.camelCase(check.getType() + "") + "." + check.getCategory() + ".Enabled", true);
				plugin.config.set("Checks." + MSG.camelCase(check.getType() + "") + "." + check.getCategory() + "."
						+ check.getDebugName() + ".Enabled", true);
			}
			plugin.saveConfig();
			MSG.tell(sender, "&aSuccessfully enabled all checks.");
			break;
		default:
			MSG.sendHelp(sender, 0, "default");
			return true;
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> result = new ArrayList<>();
		if (args.length <= 1) {
			for (String res : new String[] { "clear", "vl", "toggle", "reset", "flag", "checks", "banwave",
					"removebanwave", "time", "stats", "enablechecks" }) {
				if (res.toLowerCase().startsWith(args[0].toLowerCase())
						&& sender.hasPermission("anticheat.command." + res))
					result.add(res);
			}
		}

		if (args.length >= 2 && args.length <= 3) {
			if (args[0].matches("(?i)(clear|removebanwave|banwave|flag)")) {
				for (Player target : Bukkit.getOnlinePlayers()) {
					if (target.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
						result.add(target.getName());
				}
			}
			if (args[0].equalsIgnoreCase("clear")) {

				if ("all".startsWith(args[args.length - 1].toLowerCase())) {
					result.add("all");
				}
				for (Check c : plugin.getChecks().getAllChecks()) {
					if (c.getCategory().toLowerCase().startsWith(args[args.length - 1])
							&& !result.contains(c.getCategory()))
						result.add(c.getCategory());
				}
			}
		}

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("toggle")) {
				for (String res : new String[] { "cancel", "dev", "logs", "global", "scoreboard", "pastebin" }) {
					if (sender.hasPermission("anticheat.command.toggle." + res)
							&& res.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(res);
				}
			}
		}
		return result;
	}

	private String formatVls(OfflinePlayer player) {
		CPlayer cp = plugin.getPlayerManager().getPlayer(player);
		HashMap<String, Integer> vls = new HashMap<>();
		ConfigurationSection vlSection = cp.getDataFile().getConfigurationSection("vls");
		if (vlSection == null)
			return "";

		for (String hack : vlSection.getKeys(false)) {
			if (vlSection.getInt(hack) > 0)
				vls.put(MSG.camelCase(hack), vlSection.getInt(hack));
		}

		String result = "";
		for (String hack : vls.keySet()) {
			result += MSG.getVlColor(vls.get(hack)) + hack + " " + vls.get(hack) + "&7, ";
		}
		result = result.substring(0, Math.max(result.length() - 2, 0));
		return result;
	}
}
