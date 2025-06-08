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
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class FakePlayerCorpse {

    private final UUID entityId;
    private final String playerName;
    private final GameProfile gameProfile;
    private final Set<Player> viewers;
    private Location currentLocation;

    public FakePlayerCorpse(String playerName, Location location) {
        this.entityId = UUID.randomUUID();
        this.playerName = playerName;
        this.gameProfile = createGameProfile(playerName);
        this.viewers = new HashSet<>();
        this.currentLocation = location.clone();
    }

    public void show(Player viewer) {
        if (viewers.contains(viewer)) return;

        try {
            sendSpawnPlayer(viewer);
            sendSleepingPose(viewer);
            viewers.add(viewer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hide(Player viewer) {
        if (!viewers.contains(viewer)) return;

        try {
            sendDestroyEntity(viewer);
            viewers.remove(viewer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideFromAll() {
        for (Player viewer : new HashSet<>(viewers)) {
            hide(viewer);
        }
    }

    public void teleport(Location newLocation) {
        this.currentLocation = newLocation.clone();

        for (Player viewer : viewers) {
            try {
                sendTeleportPacket(viewer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendPlayerInfoAdd(Player viewer) throws InvocationTargetException {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);

        WrappedGameProfile wrappedProfile = WrappedGameProfile.fromHandle(gameProfile);
        PlayerInfoData data = new PlayerInfoData(wrappedProfile, 0, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(playerName));

        List<PlayerInfoData> dataList = new ArrayList<>();
        dataList.add(data);
        packet.getPlayerInfoDataLists().write(0, dataList);
        ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
    }

    private void sendSpawnPlayer(Player viewer) throws InvocationTargetException {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);

        packet.getIntegers().write(0, entityId.hashCode());
        packet.getUUIDs().write(0, entityId);
        packet.getEntityTypeModifier().write(0, EntityType.PLAYER);
        packet.getDoubles().write(0, currentLocation.getX());
        packet.getDoubles().write(1, currentLocation.getY());
        packet.getDoubles().write(2, currentLocation.getZ());
        packet.getIntegers().write(1, (int) (currentLocation.getPitch() * 256.0F / 360.0F));
        packet.getIntegers().write(2, (int) (currentLocation.getYaw() * 256.0F / 360.0F));
        packet.getIntegers().write(3, (int) (currentLocation.getYaw() * 256.0F / 360.0F));
        packet.getIntegers().write(4, 0);
        packet.getShorts().write(0, (short) 0);
        packet.getShorts().write(1, (short) 0);
        packet.getShorts().write(2, (short) 0);

        ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
    }

    private void sendSleepingPose(Player viewer) throws InvocationTargetException {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entityId.hashCode());

        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();

        WrappedDataWatcher.Serializer poseSerializer = WrappedDataWatcher.Registry.get(EnumWrappers.EntityPose.class);
        dataWatcher.setObject(6, poseSerializer, EnumWrappers.EntityPose.SLEEPING);

        packet.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());
        ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
    }

    private void sendTeleportPacket(Player viewer) throws InvocationTargetException {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_TELEPORT);

        packet.getIntegers().write(0, entityId.hashCode());
        packet.getDoubles().write(0, currentLocation.getX());
        packet.getDoubles().write(1, currentLocation.getY());
        packet.getDoubles().write(2, currentLocation.getZ());
        packet.getBytes().write(0, (byte) (currentLocation.getYaw() * 256.0F / 360.0F));
        packet.getBytes().write(1, (byte) (currentLocation.getPitch() * 256.0F / 360.0F));
        packet.getBooleans().write(0, true);

        ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
    }

    private void sendDestroyEntity(Player viewer) throws InvocationTargetException {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getIntegerArrays().write(0, new int[]{entityId.hashCode()});
        ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
    }

    private void sendPlayerInfoRemove(Player viewer) throws InvocationTargetException {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);

        WrappedGameProfile wrappedProfile = WrappedGameProfile.fromHandle(gameProfile);
        PlayerInfoData data = new PlayerInfoData(wrappedProfile, 0, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(playerName));

        List<PlayerInfoData> dataList = new ArrayList<>();
        dataList.add(data);
        packet.getPlayerInfoDataLists().write(0, dataList);
        ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
    }

    private GameProfile createGameProfile(String playerName) {
        GameProfile profile = new GameProfile(entityId, playerName);

        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            try {
                Object craftPlayer = onlinePlayer.getClass().getMethod("getHandle").invoke(onlinePlayer);

                try {
                    GameProfile originalProfile = (GameProfile) craftPlayer.getClass().getMethod("getGameProfile").invoke(craftPlayer);
                    for (Property property : originalProfile.getProperties().values()) {
                        profile.getProperties().put(property.name(), property);
                    }
                } catch (Exception e1) {
                    try {
                        GameProfile originalProfile = (GameProfile) craftPlayer.getClass().getMethod("getProfile").invoke(craftPlayer);
                        for (Property property : originalProfile.getProperties().values()) {
                            profile.getProperties().put(property.name(), property);
                        }
                    } catch (Exception e2) {
                        Object profileField = craftPlayer.getClass().getField("gameProfile").get(craftPlayer);
                        if (profileField instanceof GameProfile) {
                            GameProfile originalProfile = (GameProfile) profileField;
                            for (Property property : originalProfile.getProperties().values()) {
                                profile.getProperties().put(property.name(), property);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("Could not get GameProfile for player: " + playerName);
            }
        }

        return profile;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Location getCurrentLocation() {
        return currentLocation.clone();
    }

    public Set<Player> getViewers() {
        return new HashSet<>(viewers);
    }
}