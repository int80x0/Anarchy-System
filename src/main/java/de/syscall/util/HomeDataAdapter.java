package de.syscall.util;

import com.google.gson.*;
import de.syscall.data.HomeData;

import java.lang.reflect.Type;

public class HomeDataAdapter implements JsonSerializer<HomeData>, JsonDeserializer<HomeData> {

    @Override
    public JsonElement serialize(HomeData src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("world", src.getWorldName());
        obj.addProperty("x", src.getX());
        obj.addProperty("y", src.getY());
        obj.addProperty("z", src.getZ());
        obj.addProperty("yaw", src.getYaw());
        obj.addProperty("pitch", src.getPitch());
        return obj;
    }

    @Override
    public HomeData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        return new HomeData(
                obj.get("world").getAsString(),
                obj.get("x").getAsDouble(),
                obj.get("y").getAsDouble(),
                obj.get("z").getAsDouble(),
                obj.get("yaw").getAsFloat(),
                obj.get("pitch").getAsFloat()
        );
    }
}