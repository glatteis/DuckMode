package me.glatteis.duckmode.generation;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import me.glatteis.duckmode.DuckMain;
import me.glatteis.duckmode.generation.config.Dimension;
import me.glatteis.duckmode.generation.config.DimensionContainer;
import me.glatteis.duckmode.generation.config.SpawnNextTo;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class LevelGenerator {

    private static Location lastStartLocation = null;
    private static org.bukkit.util.Vector lastMax = null;
    private static Dimension lastDimensionData = null;


    private static File path = new File(new File(System.getProperty("java.class.path")).getAbsoluteFile().getParentFile().toString() + "/plugins/DuckMode/Generation/");

    public static void buildPlace(final boolean where) {
        Location startLocation;
        int providedSpawns = DuckMain.ducks.size();
        if (where) {
            startLocation = new Location(DuckMain.getWorld(), -1000, 20, 0);

        } else {
            startLocation = new Location(DuckMain.getWorld(), 1000, 20, 0);

        }

        System.out.println(startLocation.toString());

        int amount = path.list().length;
        int dimension = new Random().nextInt(amount - 1);
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.equals("Static");
            }
        };
        File dimensionFile = new File(path.toString() + "/" + path.list(filenameFilter)[dimension]);
        File config = new File(dimensionFile.toString() + "/" + "config.json");

        final Dimension dimensionData;

        try {
            dimensionData = JSONToDimensionParser.parse(config);
        } catch (IOException e) {
            Bukkit.getLogger().info("FATAL ERROR: There is no config.json inside " + dimensionFile.getName());
            e.printStackTrace();
            Bukkit.getServer().shutdown();
            return;
        }

        int maxHeight, maxX, maxZ;

        if (dimensionData.getMaxX() == null || dimensionData.getMinX() == null) {
            maxX = ((int) (Math.random() * 6) + 5);
        } else {
            maxX = ((int) (Math.random() * (dimensionData.getMaxX() - dimensionData.getMinX())) + dimensionData.getMinX());
        }

        if (dimensionData.getMaxY() == null || dimensionData.getMinY() == null) {
            maxHeight = ((int) (Math.random() * 6) + 5);
        } else {
            maxHeight = ((int) (Math.random() * (dimensionData.getMaxY() - dimensionData.getMinY())) + dimensionData.getMinY());
        }

        if (dimensionData.getMaxZ() == null || dimensionData.getMinZ() == null) {
            maxZ = ((int) (Math.random() * 6) + 5);
        } else {
            maxZ = ((int) (Math.random() * (dimensionData.getMaxZ() - dimensionData.getMinZ())) + dimensionData.getMinZ());
        }


        int minAnH = maxHeight;
        int maxAnH = 0;

        for (DimensionContainer container :  dimensionData.getDimensionContainers()) {
            if (container.getEndSpawnAt() == null) {
                maxAnH = maxHeight;
            }
            if (container.getStartSpawnFrom() == null) {
                minAnH = 0;
            }
            if (container.getStartSpawnFrom() != null &&
                    container.getStartSpawnFrom() < minAnH  && container.getEndSpawnAt() > 0) minAnH = container.getStartSpawnFrom();
            if (container.getEndSpawnAt() != null &&
                    container.getEndSpawnAt() > maxAnH && container.getEndSpawnAt() > 0) maxAnH = container.getEndSpawnAt();
        }

        BukkitWorld duckMainBukkitWorld = new BukkitWorld(DuckMain.getWorld());

        SchematicToLoad[][][] map = new SchematicToLoad[maxX][maxAnH][maxZ];

        ArrayList<org.bukkit.util.Vector> spawns = new ArrayList<org.bukkit.util.Vector>();

        dimensionData.init(maxAnH);

        for (int i = 0; i < providedSpawns; i++) {
            boolean found;
            int x, y, z;
            do {
                x = (int) (Math.random() * maxX - 1);
                y = (int) (Math.random() * maxAnH - minAnH - 1) + minAnH;
                z = (int) (Math.random() * maxZ - 1);
                found = !spawns.contains(new org.bukkit.util.Vector(x, y, z));
            } while (!found);
            spawns.add(new org.bukkit.util.Vector(x, y, z));
        }

        for (int y = minAnH; y < maxAnH; y++) {
            for (int x = 0; x < maxX; x++) {
                for (int z = 0; z < maxZ; z++) {

                    Location there = startLocation.clone().add(x * dimensionData.getSizeX(),
                            y * dimensionData.getSizeY(), z * dimensionData.getSizeZ());

                    DimensionContainer spawnThis = null;

                    boolean forceSpawn = false;


                    if (spawns.contains(new org.bukkit.util.Vector(x, y, z))) {
                        Bukkit.getLogger().info("Forcing spawn...");
                        forceSpawn = true;
                    }

                    int rotation = 0;
                    int axis = 0;

                    for (DimensionContainer container : dimensionData.getDimensionContainers()) {

                        //Check if container is in range of height.
                        if ((container.getStartSpawnFrom() == null || container.getStartSpawnFrom() <= y) &&
                                (container.getEndSpawnAt() == null || container.getEndSpawnAt() >= y)) {
                            //If the container is a spawn only if there has to be a spawn here:
                            if (forceSpawn && !container.getType().equals("spawn") ||
                                    !forceSpawn && container.getType().equals("spawn")) continue;
                            //If the container will spawn.
                            if ((forceSpawn && (spawnThis == null || (spawnThis.getPriority() < container.getPriority()))) ||
                                    (Math.random() <= container.getChance() && (spawnThis == null || (spawnThis.getPriority() < container.getPriority())))) {

                                int fAxis, fAngle;
                                fAxis = fAngle = 0;

                                boolean c = !container.getSpawnNextTo().isEmpty();

                                for (SpawnNextTo spawnNextTo : container.getSpawnNextTo()) {
                                    if (spawnNextTo.hasPlace(map, new org.bukkit.util.Vector(x, y, z), container, 0, 0)) {
                                        c = false;
                                    }
                                }

                                if (c && !container.getSpawnNextTo().isEmpty()) {
                                    for (int iAxis = 0; iAxis < 3; iAxis++) {
                                        if (!container.getRotate()[iAxis]) continue;
                                        for (int angle = 0; angle < 360; angle += 90) {
                                            boolean doesNotWork = false;
                                            for (SpawnNextTo spawnNextTo : container.getSpawnNextTo()) {
                                                if (!spawnNextTo.hasPlace(map, new org.bukkit.util.Vector(x, y, z), container, angle, iAxis)) {
                                                    doesNotWork = true;
                                                }
                                            }
                                            if (!doesNotWork) {
                                                c = false;
                                                fAxis = iAxis;
                                                fAngle = angle;
                                                break;
                                            }
                                        }
                                    }
                                }

                                if (c) continue;

                                //We found a container that could spawn here.
                                if (forceSpawn) Bukkit.getLogger().info("Spawn forced.");

                                rotation = fAngle;
                                axis = fAxis;

                                spawnThis = container;
                            }
                        }
                    }

                    if (spawnThis == null){
                        if (forceSpawn) {
                            Bukkit.shutdown();
                            throw new RuntimeException("MAP ERROR: There is no spawn to place here! " +
                            "{" + x + " " + y + " "+ z + "} " + dimensionData.getName());
                        }
                        continue;
                    }

                    SchematicToLoad schematicToLoad = new SchematicToLoad(duckMainBukkitWorld,
                            new Vector(there.getX(), there.getY(), there.getZ()), spawnThis, dimensionData, rotation, axis);

                    SchematicLoad.addSchematic(schematicToLoad);

                    map[x][y][z] = schematicToLoad;

                }
            }
        }

        if (lastStartLocation != null) {
            SchematicLoad.clearArea(lastStartLocation.toVector(), lastStartLocation.toVector().add(
                    new org.bukkit.util.Vector(lastDimensionData.getSizeX() * lastMax.getX(),
                            lastDimensionData.getSizeY() * lastMax.getY(), lastDimensionData.getSizeZ() * lastMax.getZ())));
        }

        lastStartLocation = startLocation;
        lastMax = new org.bukkit.util.Vector(maxX, maxAnH, maxZ);
        lastDimensionData = dimensionData;



        Bukkit.getLogger().info("Start Location: " + startLocation);
    }

    private static void info(int i) {
        Bukkit.getLogger().info(i + "");
    }
}