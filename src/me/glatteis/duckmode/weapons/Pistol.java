package me.glatteis.duckmode.weapons;

import me.glatteis.duckmode.Duck;
import me.glatteis.duckmode.DuckMain;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

public class Pistol extends DuckWeapon implements Listener {

    public Pistol() {
        super(Material.BLAZE_POWDER);
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        if (e.getMaterial().equals(Material.BLAZE_ROD)) {
            if (!WeaponWatch.durability.containsKey(e.getItem().getItemMeta().getLore())) {
                WeaponWatch.durability.put(e.getItem().getItemMeta().getLore(), 6);
            }
        } else {
            return;
        }
        if (WeaponWatch.durability.get(e.getItem().getItemMeta().getLore()) > 0 && (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) &&
                (!WeaponWatch.cooldown.contains(e.getItem().getItemMeta().getLore()))) {
            WeaponWatch.durability.put(e.getItem().getItemMeta().getLore(), WeaponWatch.durability.get(e.getItem().getItemMeta().getLore()) - 1);
            DuckMain.getWorld().playSound(e.getPlayer().getLocation(), Sound.BLAZE_HIT, 10, 1);
            Arrow a = e.getPlayer().launchProjectile(Arrow.class);
            a.setShooter(e.getPlayer());
            a.setVelocity(a.getVelocity().multiply(4));
            a.setCustomName("Pistol"); //$NON-NLS-1$
            WeaponWatch.cooldown.add(e.getItem().getItemMeta().getLore());
            new BukkitRunnable() {
                @Override
                public void run() {
                    WeaponWatch.cooldown.remove(e.getItem().getItemMeta().getLore());
                }
            }.runTaskLater(DuckMain.getPlugin(), 10L);
        }
    }

    @EventHandler
    public void onArrowImpact(ProjectileHitEvent e) {
        if (e.getEntity().getCustomName() == null) return;
        if (e.getEntity().getCustomName().equals("Pistol")) { //$NON-NLS-1$
            BlockIterator iterator = new BlockIterator(e.getEntity().getWorld(), e.getEntity().getLocation().toVector(), e.getEntity().getVelocity().normalize(), 0.0D, 4);
            Block hitBlock = null;
            while (iterator.hasNext()) {
                hitBlock = iterator.next();
                if (!hitBlock.getType().equals(Material.AIR)) {
                    break;
                }
            }
            if (hitBlock.getType().equals(Material.GLASS) || hitBlock.getType().equals(Material.STAINED_GLASS)) {
                hitBlock.setType(Material.AIR);
                for (Duck d : DuckMain.ducks) {
                    d.getPlayer().playSound(hitBlock.getLocation(), Sound.GLASS, 10, 1);
                }
            }
        }
    }

}