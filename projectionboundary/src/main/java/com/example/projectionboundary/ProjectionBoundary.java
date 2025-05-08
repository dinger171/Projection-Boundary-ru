package com.example.projectionboundary;

import org.bukkit.plugin.java.JavaPlugin;

public class ProjectionBoundary extends JavaPlugin {

    private static ProjectionBoundary instance;

    public static ProjectionBoundary getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig(); // создаёт config.yml, если его нет
        getServer().getPluginManager().registerEvents(new PlayerZoneHandler(this), this);
        getLogger().info("ProjectionBoundary enable.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ProjectionBoundary disable.");
    }
}
