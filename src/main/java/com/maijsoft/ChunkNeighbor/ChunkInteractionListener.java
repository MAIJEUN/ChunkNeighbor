/*
 * Copyright (C) 2025 MAIJSOFT Dev
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

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class ChunkInteractionListener implements Listener {
    private final Main plugin;

    public ChunkInteractionListener(Main plugin) {
        this.plugin = plugin;
    }

    // 플레이어가 자신의 청크를 벗어나는지 확인하는 메소드
    private boolean isOutsidePlayerChunk(Player player, Location location) {
        Chunk playerChunk = plugin.getPlayerChunk(player, player.getWorld().getEnvironment());
        return playerChunk != null && !location.getChunk().equals(playerChunk);
    }

    // 플레이어 이동 제한
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;  // 크리에이티브 및 관전 모드는 제한하지 않음
        }

        if (isOutsidePlayerChunk(player, to)) {
            event.setCancelled(true);
            player.teleport(event.getFrom());
            player.sendMessage("당신은 자신의 청크를 벗어날 수 없습니다!");
        }
    }

    // 플레이어가 엔더펄이나 후렴과를 통해 이동하는 경우 제한
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;  // 크리에이티브 및 관전 모드는 제한하지 않음
        }

        if (isOutsidePlayerChunk(player, to)) {
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
                event.setCancelled(true);
                player.sendMessage("후렴과로 자신의 청크 외로 텔레포트할 수 없습니다!");
            } else if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
                event.setCancelled(true);
                player.sendMessage("엔더펄로 자신의 청크 외로 텔레포트할 수 없습니다!");
            }
        }
    }

    // 플레이어가 블록을 설치할 때 청크 제한
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        if (isOutsidePlayerChunk(player, location)) {
            event.setCancelled(true);
            player.sendMessage("자신의 청크 외부에서는 블록을 설치할 수 없습니다!");
        }
    }

    // 플레이어가 블록을 부술 때 청크 제한
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        if (isOutsidePlayerChunk(player, location)) {
            event.setCancelled(true);
            player.sendMessage("자신의 청크 외부에서는 블록을 부술 수 없습니다!");
        }
    }

    // 플레이어가 상호작용할 때 청크 제한
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Location location = event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : player.getLocation();

        if (isOutsidePlayerChunk(player, location)) {
            event.setCancelled(true);
            player.sendMessage("자신의 청크 외부에서는 상호작용할 수 없습니다!");
        }
    }

    // 플레이어가 자신의 청크 외부에 있는 엔티티에 탑승할 수 없도록 제한
    @EventHandler
    public void onEntityMount(EntityMountEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player player) {
            Location mountLocation = event.getMount().getLocation();

            if (isOutsidePlayerChunk(player, mountLocation)) {
                event.setCancelled(true);
                player.sendMessage("자신의 청크 외부에 있는 엔티티에 탑승할 수 없습니다!");
            }
        }
    }
}
