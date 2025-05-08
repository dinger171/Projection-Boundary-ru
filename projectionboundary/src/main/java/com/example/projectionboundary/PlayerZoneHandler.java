package com.example.projectionboundary;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerZoneHandler implements Listener {

    private final JavaPlugin plugin;
    private final int warningZone;
    private final int glitchZone;
    private final int resetZone;
    private final long messageCooldownTicks;

    private final Map<UUID, Long> lastMessageTime = new HashMap<>();

    public PlayerZoneHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.warningZone = config.getInt("boundary.warning", 49700);
        this.glitchZone = config.getInt("boundary.glitch", 49900);
        this.resetZone = config.getInt("boundary.reset", 50000);
        this.messageCooldownTicks = config.getLong("messageCooldownTicks", 200);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        double x = Math.abs(loc.getX());
        double z = Math.abs(loc.getZ());
        double border = Math.max(x, z);

        if (border >= resetZone) {
            handleReset(player);
        } else if (border >= glitchZone) {
            applyGlitchZone(player);
        } else if (border >= warningZone) {
            applyWarningZone(player);
        }
    }

    private void applyWarningZone(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));

        if (canSendMessage(player)) {
            player.sendMessage(ChatColor.GRAY + "*You're coming to the edge. There is no surveillance.*");
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 1f, 0.8f);
        }
    }

    private void applyGlitchZone(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));

        if (Math.random() < 0.01) {
            player.damage(1.0); // "сбой системы"
        }

        if (canSendMessage(player)) {
            player.sendMessage(ChatColor.RED + "*The data is unstable. Refund is recommended.*");
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1f, 0.5f);
        }
    }

    private void handleReset(Player player) {
        player.getInventory().clear();
        player.sendMessage(ChatColor.DARK_RED + "*The subject is lost. The projection is completed.*");

        World world = player.getWorld();
        Random rand = new Random();
        double randX = rand.nextInt(40000) - 20000;
        double randZ = rand.nextInt(40000) - 20000;
        Location newLoc = new Location(world, randX, world.getHighestBlockYAt((int)randX, (int)randZ) + 1, randZ);

        player.teleport(newLoc);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
    }

    private boolean canSendMessage(Player player) {
        long now = System.currentTimeMillis();
        long last = lastMessageTime.getOrDefault(player.getUniqueId(), 0L);
        long cooldownMs = messageCooldownTicks * 50;

        if (now - last >= cooldownMs) {
            lastMessageTime.put(player.getUniqueId(), now);
            return true;
        }
        return false;
    }
}
