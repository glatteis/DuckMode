package me.glatteis.duckmode.reflection;

import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class DuckReflectionMethods {

    public static void actionbar(Player p, String label) {
        try {
            //Reflection formatting:

            //Needed Classes
            Class<?> iChatBaseComponentClass = DuckReflectionHelper.getNMSClass("IChatBaseComponent");
            Class<?> packetPlayOutChat = DuckReflectionHelper.getNMSClass("PacketPlayOutChat");
            Class<?> chatSerializer = DuckReflectionHelper.getNMSClass("IChatBaseComponent$ChatSerializer");

            //Get Packet
            Method a = chatSerializer.getDeclaredMethod("a", String.class);
            Object iChatBaseComponent = iChatBaseComponentClass.cast(a.invoke(chatSerializer, "{\"text\": \"" + label + "\"}"));
            Constructor<?> packetPlayOutChatConstructor = packetPlayOutChat.getConstructor(iChatBaseComponentClass, byte.class);
            Object bar = packetPlayOutChatConstructor.newInstance(iChatBaseComponent, (byte) 2);

            //Get Connection & Send
            Object connection = DuckReflectionHelper.getConnection(p);
            DuckReflectionHelper.sendPacket(connection, bar);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void title(Player p, String label, int in, int stay, int out) {

        try {
            Class<?> iChatBaseComponent = DuckReflectionHelper.getNMSClass("IChatBaseComponent");
            Class<?> packetPlayOutTitle = DuckReflectionHelper.getNMSClass("PacketPlayOutTitle");
            Class<?> enumTitleAction = DuckReflectionHelper.getNMSClass("PacketPlayOutTitle$EnumTitleAction");
            Class<?> chatSerializer = DuckReflectionHelper.getNMSClass("IChatBaseComponent$ChatSerializer");

            Method m1 = chatSerializer.getMethod("a", String.class);
            Object baseComponent = m1.invoke(null, "{\"text\": \"" + label + "\"}");

            Constructor<?> c1 = packetPlayOutTitle.getConstructor(enumTitleAction, iChatBaseComponent);
            Object title = c1.newInstance(enumTitleAction.getEnumConstants()[0], baseComponent);
            Object connection = DuckReflectionHelper.getConnection(p);
            DuckReflectionHelper.sendPacket(connection, title);

            Constructor<?> c2 = packetPlayOutTitle.getConstructor(enumTitleAction, iChatBaseComponent, int.class, int.class, int.class);
            Object times = c2.newInstance(enumTitleAction.getEnumConstants()[2], baseComponent, in, stay, out);

            DuckReflectionHelper.sendPacket(connection, times);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void subtitle(Player p, String label, int in, int stay, int out) {
        try {
            Class<?> iChatBaseComponent = DuckReflectionHelper.getNMSClass("IChatBaseComponent");
            Class<?> packetPlayOutTitle = DuckReflectionHelper.getNMSClass("PacketPlayOutTitle");
            Class<?> enumTitleAction = DuckReflectionHelper.getNMSClass("PacketPlayOutTitle$EnumTitleAction");
            Class<?> chatSerializer = DuckReflectionHelper.getNMSClass("IChatBaseComponent$ChatSerializer");

            Method m1 = chatSerializer.getMethod("a", String.class);
            Object baseComponent = m1.invoke(null, "{\"text\": \"" + label + "\"}");

            Constructor<?> c1 = packetPlayOutTitle.getConstructor(enumTitleAction, iChatBaseComponent);
            Object title = c1.newInstance(enumTitleAction.getEnumConstants()[1], baseComponent);
            Object connection = DuckReflectionHelper.getConnection(p);
            DuckReflectionHelper.sendPacket(connection, title);

            Constructor<?> c2 = packetPlayOutTitle.getConstructor(enumTitleAction, iChatBaseComponent, int.class, int.class, int.class);
            Object times = c2.newInstance(enumTitleAction.getEnumConstants()[2], baseComponent, in, stay, out);

            DuckReflectionHelper.sendPacket(connection, times);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
