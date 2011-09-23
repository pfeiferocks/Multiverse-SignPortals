package com.onarandombox.MultiverseSignPortals.listeners;

import com.onarandombox.MultiverseCore.MVPermissions;
import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import com.onarandombox.MultiverseSignPortals.utils.PortalDetector;
import com.onarandombox.MultiverseSignPortals.utils.SignStatus;
import com.onarandombox.MultiverseSignPortals.utils.SignTools;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.permissions.PermissionDefault;

import java.util.logging.Level;

public class MVSPBlockListener extends BlockListener {
    private final String CREATE_PERM = "multiverse.signportal.create";
    private MultiverseSignPortals plugin;
    private MVPermissions permissions;

    public MVSPBlockListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
        this.permissions = this.plugin.getCore().getPermissions();
        this.permissions.addPermission(CREATE_PERM, PermissionDefault.OP);
    }

    @Override
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        this.plugin.log(Level.FINER, "Sign changed");
        if (event.getLine(0).equalsIgnoreCase("[mv]") || event.getLine(0).equalsIgnoreCase("[multiverse]")) {
            createMultiverseSignPortal(event);
        } else {
            checkForHack(event);
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        BlockState state = event.getBlock().getState();
        if (state instanceof Sign) {
            Sign s = (Sign) state;
            PortalDetector pd = this.plugin.getPortalDetector();
            if (pd.getSignStatus(s) == SignStatus.NetherPortalSign || pd.getSignStatus(s) == SignStatus.SignPortal) {
                if (!this.permissions.hasPermission(event.getPlayer(), CREATE_PERM, true)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("You don't have permission to destroy a SignPortal!");
                    event.getPlayer().sendMessage(ChatColor.GREEN + CREATE_PERM);
                }
            }
        }
    }

    private void checkForHack(SignChangeEvent event) {
        if (SignTools.isMVSign(event.getLine(0), ChatColor.DARK_GREEN) || SignTools.isMVSign(event.getLine(0), ChatColor.DARK_BLUE)) {
            this.plugin.log(Level.WARNING, "WOAH! Player: [" + event.getPlayer().getName() + "] tried to HACK a Multiverse SignPortal into existance!");
            this.warnOps("WOAH! Player: [" + event.getPlayer().getName() + "] tried to " + ChatColor.RED + "HACK" + ChatColor.WHITE + " a"
                    + ChatColor.AQUA + " Multiverse SignPortal" + ChatColor.WHITE + " into existance!");
            event.setCancelled(true);
        }
    }

    private void createMultiverseSignPortal(SignChangeEvent event) {
        if (this.plugin.getCore().getPermissions().hasPermission(event.getPlayer(), "multiverse.signportal.create", true)) {
            this.plugin.log(Level.FINER, "MV SignPortal Created");
            event.setLine(0, ChatColor.DARK_GREEN + event.getLine(0));
        } else {
            this.plugin.log(Level.FINER, "No Perms to create");
            event.setLine(0, ChatColor.DARK_RED + event.getLine(0));
            event.getPlayer().sendMessage("You don't have permission to create a SignPortal!");
            event.getPlayer().sendMessage(ChatColor.GREEN + CREATE_PERM);
        }
    }

    private void warnOps(String string) {
        for (Player p : this.plugin.getServer().getOnlinePlayers()) {
            if (p.isOp()) {
                p.sendMessage(string);
            }
        }
    }
}
