/*
 * Copyright (C) 2024 MAIJSOFT Dev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */

package com.maijsoft.ChunkNeighbor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    private final Map<UUID, Map<Environment, Chunk>> playerChunks = new HashMap<>();
    private int lastX = 0;
    private int lastZ = 0;
    private int layer = 1;
    private int stepCount = 0;
    private int direction = 0;

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ChunkInteractionListener(this), this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        assignChunkToPlayer(player, Bukkit.getWorlds().get(0).getEnvironment());
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        Environment targetEnv = event.getTo().getWorld().getEnvironment();
        assignChunkToPlayer(player, targetEnv);
        player.teleport(getPlayerChunkLocation(player, targetEnv));
    }

    private void assignChunkToPlayer(Player player, Environment environment) {
        UUID playerUUID = player.getUniqueId();
        playerChunks.putIfAbsent(playerUUID, new HashMap<>());
        Map<Environment, Chunk> envChunks = playerChunks.get(playerUUID);

        if (!envChunks.containsKey(environment)) {
            Chunk nextChunk = getNextChunk(environment);
            envChunks.put(environment, nextChunk);
            player.teleport(getPlayerChunkLocation(player, environment));
            player.sendMessage("당신의 청크는 (" + nextChunk.getX() + ", " + nextChunk.getZ() + ")입니다.");
        }
    }

    private Chunk getNextChunk(Environment environment) {
        // 스파이럴 방식으로 할당된 좌표를 사용하여 차원에 맞는 월드에서 청크를 가져옵니다.
        World world = Bukkit.getWorlds().stream()
                .filter(w -> w.getEnvironment() == environment)
                .findFirst()
                .orElseThrow();

        switch (direction) {
            case 0 -> lastX++;
            case 1 -> lastZ++;
            case 2 -> lastX--;
            case 3 -> lastZ--;
        }
        stepCount++;
        if (stepCount >= layer) {
            direction = (direction + 1) % 4;
            if (direction == 0 || direction == 2) {
                layer++;
            }
            stepCount = 0;
        }
        return world.getChunkAt(lastX, lastZ);
    }

    public Chunk getPlayerChunk(Player player, Environment environment) {
        return playerChunks.getOrDefault(player.getUniqueId(), new HashMap<>()).get(environment);
    }

    public Location getPlayerChunkLocation(Player player, Environment environment) {
        Chunk chunk = getPlayerChunk(player, environment);
        if (chunk != null) {
            World world = Bukkit.getWorlds().stream()
                    .filter(w -> w.getEnvironment() == environment)
                    .findFirst()
                    .orElseThrow();
            return new Location(world, chunk.getX() * 16 + 8, 64, chunk.getZ() * 16 + 8);
        }
        return null;
    }
}
