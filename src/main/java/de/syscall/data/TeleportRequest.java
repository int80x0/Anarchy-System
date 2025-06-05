package de.syscall.data;

import org.bukkit.entity.Player;

public class TeleportRequest {

    private final Player requester;
    private final Player target;
    private final TeleportType type;
    private final long timestamp;

    public TeleportRequest(Player requester, Player target, TeleportType type) {
        this.requester = requester;
        this.target = target;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public Player getRequester() {
        return requester;
    }

    public Player getTarget() {
        return target;
    }

    public TeleportType getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isExpired(long timeoutMs) {
        return System.currentTimeMillis() - timestamp > timeoutMs;
    }

    public enum TeleportType {
        TPA,
        TPAHERE
    }
}