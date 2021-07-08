package ranpoes.fakeplayerex;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ranpoes.fakeplayerex.thread.Clocking;

public class FakePlayerEx extends JavaPlugin {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        Clocking clock = new Clocking(this,6);
        clock.start();
        /*getLogger().info("FakePlayerEx已启动");
        //Bukkit.getPluginManager().registerEvents(new Event(), this);
        Bukkit.getPluginCommand("rlgtpall").setExecutor(new TpAllByWorldCommand());
        Bukkit.getPluginCommand("rlgscore").setExecutor(new ScoreOperatorCommand());*/
    }

    @Override
    public void onDisable() {
        getLogger().info("FakePlayerEx已关闭");
    }


}
