package me.glatteis.duckmode;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import me.glatteis.duckmode.reflection.DuckReflectionMethods;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Intermission implements Listener {

    private static ArrayList<Block> toRemove = new ArrayList<Block>();

    public static void create() {
        final int pointsToWin = SettingDatabase.settingSwitches.get(SettingTypes.POINTS_TO_WIN.toString()).get(SettingDatabase.intSetting.get(SettingTypes.POINTS_TO_WIN.toString()));
        new BukkitRunnable() {
            public void run() {
                int ducks = DuckMain.ducks.size();
                BukkitWorld bw = new BukkitWorld(DuckMain.getWorld());
                com.sk89q.worldedit.Vector v = new com.sk89q.worldedit.Vector(0, 20, 1000);
                for (int i = 0; i < ducks; i++) {
                    SchematicLoad.addSchematic(new SchematicToLoad(bw, v.add(0, 0, i * 4), "Static", "intermission", "start"));
                }
                v = v.add(5, 0, 0);
                for (int i1 = 0; i1 < ducks; i1++) {
                    for (int i = 1; i < pointsToWin + 1; i++) {
                        SchematicLoad.addSchematic(new SchematicToLoad(bw, v.add(i, 0, i1 * 4), "Static", "intermission", "middle"));
                    }
                }
                for (int i = 0; i < ducks; i++) {
                    SchematicLoad.addSchematic(new SchematicToLoad(bw, v.add(pointsToWin + 1, 0, i * 4), "Static", "intermission", "end"));
                }
                SchematicLoad.loadAllSchematics();
            }
        }.runTask(DuckMain.getPlugin());
    }

    private static void removeRocks() {
        for (Block b : toRemove) {
            b.setType(Material.AIR);
        }
        toRemove.clear();
    }

    public static void intermission() {
        DuckMain.state = GameState.INTERMISSION;
        ContinueGame.setRoundHasEnded(false);
        removeRocks();
        for (int i = 0; i < DuckMain.ducks.size(); i++) {
            Duck d = DuckMain.duckCount.get(i);
            if (d == null) {
                continue;
            }
            StaticMethods.prepareInventory(d);
            DuckReflectionMethods.subtitle(d.getPlayer(), ChatColor.RESET.toString(), 3, 30, 3);
            DuckReflectionMethods.title(d.getPlayer(), ChatColor.RED + Messages.getString("intermission_big_title"), 3, 30, 3);
            d.getPlayer().setGameMode(GameMode.ADVENTURE);
            d.getPlayer().teleport(new Location(DuckMain.getWorld(), 3, 22, 1002.5 + i * 4));
            StaticMethods.disableJumping(d.getPlayer());
        }

        new BukkitRunnable() {
            int i = -1;
            int pointsToWin = SettingDatabase.settingSwitches.get(SettingTypes.POINTS_TO_WIN.toString()).get(SettingDatabase.intSetting.get(SettingTypes.POINTS_TO_WIN.toString()));

            @Override
            public void run() {
                i++;
                if (i == DuckMain.ducks.size()) {
                    for (Duck d : WinTracker.wins.keySet()) {
                        if (WinTracker.wins.get(d) >= pointsToWin) {
                            new BukkitRunnable() {
                                public void run() {
                                    GameEnd.win();
                                }
                            }.runTaskLater(DuckMain.getPlugin(), 70L);
                            this.cancel();
                            return;
                        }
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            DuckMain.state = GameState.INGAME;
                            ContinueGame.setRoundHasEnded(true);
                        }
                    }.runTaskLater(DuckMain.getPlugin(), 100L);
                    this.cancel();
                    return;
                }
                Duck d = DuckMain.duckCount.get(i);
                if (WinTracker.wins.get(d) == null) {
                    WinTracker.wins.put(d, 0);
                }
                Location l = new Location(DuckMain.getWorld(), 6 + WinTracker.wins.get(d), 22, 1002.5 + i * 4);
                @SuppressWarnings("deprecation")
                FallingBlock b = DuckMain.getWorld().spawnFallingBlock(l, Material.STONE, (byte) 0);
                b.setCustomName("" + WinTracker.wins.get(d));
                b.setCustomNameVisible(true);
            }
        }.runTaskTimer(DuckMain.getPlugin(), 50L, 15L);
    }

    @EventHandler
    public void onFallingBlockLand(EntityChangeBlockEvent e) {
        if (e.getEntityType().equals(EntityType.FALLING_BLOCK) && e.getEntity().getCustomName() != null) {
            ArmorStand a = DuckMain.getWorld().spawn(e.getBlock().getLocation().clone().add(0, 0, .5), ArmorStand.class);
            a.setVisible(false);
            a.setCustomName(e.getEntity().getCustomName());
            a.setCustomNameVisible(true);
            a.setCanPickupItems(false);
            a.setGravity(false);
            toRemove.add(e.getBlock());
        }
    }

}
