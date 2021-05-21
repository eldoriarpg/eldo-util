package de.eldoria.eldoutilities.database.querybuilder;

import de.eldoria.eldoutilities.threading.BukkitAsyncAction;

public interface UpdateStage {
    /**
     * Execute the update async.
     *
     * @return bukkit action which returns the number of changed rows
     */
    BukkitAsyncAction<Integer> executeUpdateAsync();

    /**
     * Execute the update synced.
     * @return number of changed rows
     */
    int executeUpdate();
}
