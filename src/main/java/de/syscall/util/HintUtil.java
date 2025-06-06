package de.syscall.util;

import de.syscall.AnarchySystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class HintUtil {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static void sendHint(Player player, String baseMessage, String hintKey, AnarchySystem plugin) {
        sendHint(player, baseMessage, hintKey, plugin, null, null);
    }

    public static void sendHint(Player player, String baseMessage, String hintKey, AnarchySystem plugin, String placeholder, String value) {
        if (!plugin.getConfigManager().isHintsEnabled()) {
            String message = baseMessage;
            if (placeholder != null && value != null) {
                message = message.replace("%" + placeholder + "%", value);
            }
            player.sendMessage(message.replace("&", "§"));
            return;
        }

        String processedMessage = baseMessage;
        if (placeholder != null && value != null) {
            processedMessage = processedMessage.replace("%" + placeholder + "%", value);
        }

        String hintText = plugin.getConfigManager().getHintText();
        String hoverMessage = plugin.getConfigManager().getHintMessage(hintKey);

        Component baseComponent = SERIALIZER.deserialize(processedMessage);
        Component hintComponent = Component.text(" " + hintText)
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC)
                .hoverEvent(HoverEvent.showText(SERIALIZER.deserialize(hoverMessage)));

        Component finalMessage = baseComponent.append(hintComponent);
        player.sendMessage(finalMessage);
    }

    public static Component createHintComponent(String hintText, String hoverMessage) {
        return Component.text(" " + hintText)
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC)
                .hoverEvent(HoverEvent.showText(SERIALIZER.deserialize(hoverMessage)));
    }

    public static Component appendHint(Component baseComponent, String hintKey, AnarchySystem plugin) {
        String hintText = plugin.getConfigManager().getHintText();
        String hoverMessage = plugin.getConfigManager().getHintMessage(hintKey);

        Component hintComponent = createHintComponent(hintText, hoverMessage);
        return baseComponent.append(hintComponent);
    }
}