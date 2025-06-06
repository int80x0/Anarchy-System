package de.syscall.manager;

import de.syscall.AnarchySystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class HintManager {

    private final AnarchySystem plugin;
    private final LegacyComponentSerializer serializer;

    public HintManager(AnarchySystem plugin) {
        this.plugin = plugin;
        this.serializer = LegacyComponentSerializer.legacyAmpersand();
    }

    public void sendMessageWithHint(Player player, String messageKey, String hintKey) {
        sendMessageWithHint(player, messageKey, hintKey, null, null);
    }

    public void sendMessageWithHint(Player player, String messageKey, String hintKey, String placeholder, String value) {
        if (!plugin.getConfigManager().isHintsEnabled()) {
            String message = plugin.getConfigManager().getMessage(messageKey);
            if (placeholder != null && value != null) {
                message = message.replace("%" + placeholder + "%", value);
            }
            player.sendMessage(message.replace("&", "§"));
            return;
        }

        String baseMessage = plugin.getConfigManager().getMessage(messageKey);
        if (placeholder != null && value != null) {
            baseMessage = baseMessage.replace("%" + placeholder + "%", value);
        }

        String hintText = plugin.getConfigManager().getHintText();
        String hoverMessage = plugin.getConfigManager().getHintMessage(hintKey);

        Component baseComponent = serializer.deserialize(baseMessage);
        Component hintComponent = Component.text(" " + hintText)
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC)
                .hoverEvent(HoverEvent.showText(serializer.deserialize(hoverMessage)));

        Component finalMessage = baseComponent.append(hintComponent);
        player.sendMessage(finalMessage);
    }

    public void sendTeleportMessageWithHint(Player player, String messageKey, String hintKey) {
        sendTeleportMessageWithHint(player, messageKey, hintKey, null, null);
    }

    public void sendTeleportMessageWithHint(Player player, String messageKey, String hintKey, String placeholder, String value) {
        if (!plugin.getConfigManager().isHintsEnabled()) {
            String message = plugin.getConfigManager().getTeleportMessage(messageKey);
            if (placeholder != null && value != null) {
                message = message.replace("%" + placeholder + "%", value);
            }
            player.sendMessage(message.replace("&", "§"));
            return;
        }

        String baseMessage = plugin.getConfigManager().getTeleportMessage(messageKey);
        if (placeholder != null && value != null) {
            baseMessage = baseMessage.replace("%" + placeholder + "%", value);
        }

        String hintText = plugin.getConfigManager().getHintText();
        String hoverMessage = plugin.getConfigManager().getHintMessage(hintKey);

        Component baseComponent = serializer.deserialize(baseMessage);
        Component hintComponent = Component.text(" " + hintText)
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC)
                .hoverEvent(HoverEvent.showText(serializer.deserialize(hoverMessage)));

        Component finalMessage = baseComponent.append(hintComponent);
        player.sendMessage(finalMessage);
    }
}