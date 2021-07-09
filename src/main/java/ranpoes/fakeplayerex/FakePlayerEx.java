package ranpoes.fakeplayerex;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ranpoes.fakeplayerex.event.Event;
import ranpoes.fakeplayerex.thread.Clocking;

import java.util.Date;

public class FakePlayerEx extends JavaPlugin {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        Clocking clock = new Clocking(this);
        clock.start();
        Bukkit.getPluginManager().registerEvents(new Event(this), this);
        getLogger().info("FakePlayerEx已启动");
        /*Bukkit.getPluginCommand("rlgtpall").setExecutor(new TpAllByWorldCommand());
        Bukkit.getPluginCommand("rlgscore").setExecutor(new ScoreOperatorCommand());*/
    }

    @Override
    public void onDisable() {
        getLogger().info("FakePlayerEx已关闭");
    }


}
