package src.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface Pnj {
    String speak(Player player, int proximityRange);

    boolean isPlayerClose(Player player, int proximityRange);

    String givingTask(Player player);

    String defaultText(Player player);

    String endTask(Player player);

    void completeTask();
}

