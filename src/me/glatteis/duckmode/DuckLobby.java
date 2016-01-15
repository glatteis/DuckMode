package me.glatteis.duckmode;

import me.glatteis.duckmode.hats.Hats;
import me.glatteis.duckmode.reflection.DuckReflectionMethods;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;

public class DuckLobby implements Listener {


    boolean activated = false;

    public static void configureLobby() {
        Location sourceLocation = new Location(DuckMain.getWorld(), 1, 23, 5);

        for (int i = 3; i < 8; i++) {
            sourceLocation.setZ(i);
            sourceLocation.getBlock().setType(Material.WALL_SIGN);
            BlockState state = sourceLocation.getBlock().getState();
            org.bukkit.material.Sign matSign = new org.bukkit.material.Sign(Material.WALL_SIGN);
            matSign.setFacingDirection(BlockFace.EAST);
            if (!(state instanceof Sign)) continue;
            Sign s = (Sign) state;
            s.setData(matSign);
            String thisSetting = SettingDatabase.settings.get(i - 3);
            SettingDatabase.settingsSigns.put(s, thisSetting);
            if (thisSetting.equals(SettingTypes.POINTS_TO_WIN.toString()) || thisSetting.equals(SettingTypes.ROUNDS.toString())) { //$NON-NLS-1$ //$NON-NLS-2$
                s.setLine(0, ChatColor.GREEN + thisSetting);
                SettingDatabase.intSetting.put(thisSetting, 0);
                s.setLine(1, ChatColor.GRAY + "=========="); //$NON-NLS-1$
                s.setLine(2, ChatColor.GOLD + String.valueOf(SettingDatabase.switchSettingsFor(thisSetting)));
                s.setLine(3, ChatColor.GRAY + "=========="); //$NON-NLS-1$
            } else if (thisSetting.equals(SettingTypes.HATS.toString())) { //$NON-NLS-1$
                s.setLine(0, ChatColor.GREEN + thisSetting);
                s.setLine(2, ChatColor.LIGHT_PURPLE + Messages.getString("hats_description")); //$NON-NLS-1$
            }
            s.update();
        }
    }

    @EventHandler
    public void signClick(PlayerInteractEvent e) {
        if (DuckMain.state.equals(GameState.LOBBY) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().equals(Material.WALL_SIGN)) {
            Sign s = (Sign) e.getClickedBlock().getState();
            String setting = SettingDatabase.settingsSigns.get(s);
            if (setting == null) return;
            Bukkit.getLogger().info("Setting:" + setting); //$NON-NLS-1$
            //Because this is Java 1.6, I can't make a setting switch. D:
            if (setting.equals(SettingTypes.POINTS_TO_WIN.toString()) || setting.equals(SettingTypes.ROUNDS.toString())) { //$NON-NLS-1$ //$NON-NLS-2$
                String switchSetting = String.valueOf(SettingDatabase.switchSettingsFor(setting));
                s.setLine(2, ChatColor.GOLD + switchSetting);
                s.update();
            }
            if (setting.equals(SettingTypes.HATS.toString())) { //$NON-NLS-1$
                Hats.openHatInventory(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void hatChoice(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        if (ChatColor.stripColor(e.getInventory().getTitle()).equals(SettingTypes.HATS.toString())) { //$NON-NLS-1$
            for (Duck d : DuckMain.ducks) {
                if (d.getPlayer().equals(e.getWhoClicked())) {
                    Hats.setHat(d, e.getCurrentItem());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (activated) {
            return;
        }
        if (DuckMain.state.equals(GameState.LOBBY)) {
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().equals(Material.WOOD_BUTTON) &&
                    e.getClickedBlock().getRelative(BlockFace.WEST).getType().equals(Material.WOOL) &&
                    ((Wool) e.getClickedBlock().getRelative(BlockFace.WEST).getState().getData()).getColor().equals(DyeColor.LIME)) {
                //Someone pressed the start button
                countdown();
            }
        }
    }

    public void countdown() {
        activated = true;
        new BukkitRunnable() {
            int countdown = 3;

            public void run() {
                for (Duck d : DuckMain.ducks) {
                    DuckReflectionMethods.title(d.getPlayer(), ChatColor.RED.toString() + countdown, 0, 20, 5);
                }
                countdown--;
                if (countdown == 0) {
                    Intermission.create();
                    ContinueGame.startRound();
                    this.cancel();
                }

            }
        }.runTaskTimer(DuckMain.getPlugin(), 20L, 20L);
    }


}
