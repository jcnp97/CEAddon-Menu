package asia.virtualmc.CEAddonMenu.utilities;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AsyncUtils {


    /**
     * Runs a task asynchronously and then schedules a follow-up task on the main server thread with the result.
     * Use this for workflows like:
     * - Retrieving data from a database or external source (off-thread)
     * - Updating in-memory state or using Bukkit API (main thread)
     *
     * @param plugin        The plugin instance used to schedule both async and sync tasks.
     * @param asyncTask     A Supplier that returns the result of an asynchronous operation.
     *                      This will be executed off the main thread using Paper's async scheduler.
     * @param syncCallback  A Consumer that accepts the result returned by asyncTask and runs on the main thread.
     *                      This is where you update Bukkit state or shared memory safely.
     * @param <T>           The type of result produced by the async task and consumed by the sync callback.
     */
    public static <T> void runAsyncThenSync(Plugin plugin, Supplier<T> asyncTask, Consumer<T> syncCallback) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            T result;

            try {
                result = asyncTask.get();
            } catch (Exception e) {
                plugin.getLogger().severe("Async task failed: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> syncCallback.accept(result));
        });
    }

    /**
     * Runs the given task asynchronously using Paper's async scheduler.
     * This is suitable for IO-bound or long-running tasks that do not require any
     * interaction with Bukkit API or the main server thread.
     *
     * @param plugin    The plugin instance used to schedule the task.
     * @param asyncTask A Runnable containing the logic to be executed asynchronously.
     */
    public static void runAsync(Plugin plugin, Runnable asyncTask) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                asyncTask.run();
            } catch (Exception e) {
                plugin.getLogger().severe("Async task failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
