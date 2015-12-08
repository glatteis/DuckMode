package me.glatteis.duckmode;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class GameEnd {

    public static void win() {
        Queue<Duck> ducks = new LinkedList<Duck>();
        Collection<Integer> duckScores = new ArrayList<Integer>(WinTracker.wins.values());
        for (int i = 0; i < DuckMain.ducks.size(); i++) {
            Integer maxValue = Collections.max(duckScores);
            if (maxValue == null) {
                break;
            }
            Duck thisDuck = null;
            for (Duck d : WinTracker.wins.keySet()) {
                if (WinTracker.wins.get(d) == maxValue && !ducks.contains(d)) {
                    thisDuck = d;
                    break;
                }
            }
            if (thisDuck == null) {
                break;
            }
            ducks.add(thisDuck);
        }
        try {
            runCommands();
        } catch (IOException e) {
        }

        Bukkit.broadcastMessage(ChatColor.GREEN + "===================="); //$NON-NLS-1$
        Bukkit.broadcastMessage(Messages.getString("scores_in_game")); //$NON-NLS-1$
        int counter = 0;
        while (!ducks.isEmpty()) {
            counter++;
            Duck d = ducks.poll();
            Bukkit.broadcastMessage(ChatColor.BOLD.toString() + counter + ". " + ChatColor.RESET + d.getPlayer().getName() + " - " + WinTracker.wins.get(d) + Messages.getString("games_won")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        Bukkit.broadcastMessage(ChatColor.GREEN + "===================="); //$NON-NLS-1$
        Bukkit.broadcastMessage(Messages.getString("server_restarts")); //$NON-NLS-1$
        new BukkitRunnable() {
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.kickPlayer(ChatColor.DARK_GREEN + "DuckMode " + ChatColor.RESET + Messages.getString("ended")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                Bukkit.shutdown();
            }
        }.runTaskLater(DuckMain.getPlugin(), 400);
    }

    private static void runCommands() throws IOException {
        String path = new File(System.getProperty("java.class.path")).getAbsoluteFile().getParentFile().toString() + "/plugins/DuckMode/end_commands.txt"; //$NON-NLS-1$ //$NON-NLS-2$
        FileInputStream stream = new FileInputStream(path);
        Scanner s = new Scanner(stream);
        List<String> commands = new ArrayList<String>();
        while (s.hasNext()) {
            commands.add(s.next());
        }
        for (String thisCommand : commands) {
            Bukkit.dispatchCommand(null, thisCommand);
        }
    }

}