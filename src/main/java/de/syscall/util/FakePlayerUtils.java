package de.syscall.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FakePlayerUtils {

    public static void spawnFakePlayer(Player viewer, String playerName, Location location, UUID entityId) {
        GameProfile gameProfile = createGameProfile(playerName, entityId);

        PacketContainer spawnPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        spawnPacket.getIntegers().write(0, entityId.hashCode());
        spawnPacket.getUUIDs().write(0, entityId);
        spawnPacket.getEntityTypeModifier().write(0, EntityType.PLAYER);
        spawnPacket.getDoubles().write(0, location.getX());
        spawnPacket.getDoubles().write(1, location.getY());
        spawnPacket.getDoubles().write(2, location.getZ());
        spawnPacket.getIntegers().write(1, (int) (location.getPitch() * 256.0F / 360.0F));
        spawnPacket.getIntegers().write(2, (int) (location.getYaw() * 256.0F / 360.0F));
        spawnPacket.getIntegers().write(3, (int) (location.getYaw() * 256.0F / 360.0F));
        spawnPacket.getIntegers().write(4, 0);
        spawnPacket.getShorts().write(0, (short) 0);
        spawnPacket.getShorts().write(1, (short) 0);
        spawnPacket.getShorts().write(2, (short) 0);

        PacketContainer infoPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        infoPacket.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);

        WrappedGameProfile wrappedProfile = WrappedGameProfile.fromHandle(gameProfile);
        PlayerInfoData playerInfoData = new PlayerInfoData(wrappedProfile, 0, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(playerName));
        List<PlayerInfoData> playerInfoDataList = Collections.singletonList(playerInfoData);
        infoPacket.getPlayerInfoDataLists().write(0, playerInfoDataList);

        ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, infoPacket);
        ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, spawnPacket);

    }

    public static void removeFakePlayer(Player viewer, UUID entityId) {
        PacketContainer removeInfoPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        removeInfoPacket.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);

        PacketContainer destroyPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntegerArrays().write(0, new int[]{entityId.hashCode()});

        ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, destroyPacket);
        ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, removeInfoPacket);

    }

    public static void moveFakePlayer(Player viewer, UUID entityId, Location newLocation) {
        PacketContainer teleportPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        teleportPacket.getIntegers().write(0, entityId.hashCode());
        teleportPacket.getDoubles().write(0, newLocation.getX());
        teleportPacket.getDoubles().write(1, newLocation.getY());
        teleportPacket.getDoubles().write(2, newLocation.getZ());
        teleportPacket.getBytes().write(0, (byte) ((int) (newLocation.getYaw() * 256.0F / 360.0F)));
        teleportPacket.getBytes().write(1, (byte) ((int) (newLocation.getPitch() * 256.0F / 360.0F)));
        teleportPacket.getBooleans().write(0, true);

        ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, teleportPacket);

    }

    public static void setPose(Player viewer, UUID entityId, EnumWrappers.EntityPose pose) {
        PacketContainer posePacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        posePacket.getIntegers().write(0, entityId.hashCode());

        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(EnumWrappers.EntityPose.class);
        dataWatcher.setObject(6, serializer, pose);

        posePacket.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());

        ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, posePacket);

    }

    private static GameProfile createGameProfile(String playerName, UUID entityId) {
        GameProfile gameProfile = new GameProfile(entityId, playerName);

        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            try {
                Object craftPlayer = onlinePlayer.getClass().getMethod("getHandle").invoke(onlinePlayer);

                try {
                    GameProfile originalProfile = (GameProfile) craftPlayer.getClass().getMethod("getGameProfile").invoke(craftPlayer);
                    for (Property property : originalProfile.getProperties().values()) {
                        gameProfile.getProperties().put(property.name(), property);
                    }
                } catch (Exception e1) {
                    try {
                        GameProfile originalProfile = (GameProfile) craftPlayer.getClass().getMethod("getProfile").invoke(craftPlayer);
                        for (Property property : originalProfile.getProperties().values()) {
                            gameProfile.getProperties().put(property.name(), property);
                        }
                    } catch (Exception e2) {
                        Object gameProfileField = craftPlayer.getClass().getField("gameProfile").get(craftPlayer);
                        if (gameProfileField instanceof GameProfile) {
                            GameProfile originalProfile = (GameProfile) gameProfileField;
                            for (Property property : originalProfile.getProperties().values()) {
                                gameProfile.getProperties().put(property.name(), property);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("Could not get GameProfile for player: " + playerName);
            }
        }

        return gameProfile;
    }
}