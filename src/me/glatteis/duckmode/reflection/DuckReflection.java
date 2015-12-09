package me.glatteis.duckmode.reflection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DuckReflection {


    public static Class<?> getNMSClass(String className) {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.split("\\.")[3]; //$NON-NLS-1$
        String path = "net.minecraft.server." + version + "." + className; //$NON-NLS-1$ //$NON-NLS-2$
        try {
            return Class.forName(path);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getConnection(Player p) {
        try {
            Method getHandle = p.getClass().getMethod("getHandle"); //$NON-NLS-1$
            Object nmsPlayer = getHandle.invoke(p);
            Field field;
            field = nmsPlayer.getClass().getField("playerConnection"); //$NON-NLS-1$
            return field.get(nmsPlayer);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sendPacket(Object connection, Object packet) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method sendPacket = connection.getClass().getMethod("sendPacket", getNMSClass("Packet")); //$NON-NLS-1$ //$NON-NLS-2$
        sendPacket.invoke(connection, packet);
    }

}
