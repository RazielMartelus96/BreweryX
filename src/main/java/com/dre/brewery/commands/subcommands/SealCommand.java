package com.dre.brewery.commands.subcommands;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.commands.SubCommand;
import com.dre.brewery.model.sealer.SealerFactory;
import com.dre.brewery.utility.MinecraftVersion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SealCommand implements SubCommand {
    @Override
    public void execute(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
        if (BreweryPlugin.getMCVersion().isOrEarlier(MinecraftVersion.V1_13)) {
            BreweryPlugin.getInstance().msg(sender, "Sealing requires minecraft 1.13 or higher");
            return;
        }
        Player player = (Player) sender;

        player.openInventory(new SealerFactory(player).create().getInventory());
    }

    @Override
    public List<String> tabComplete(BreweryPlugin breweryPlugin, CommandSender sender, String label, String[] args) {
        return null;
    }

    @Override
    public String permission() {
        return "brewery.cmd.seal";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }
}
