package ranpoes.fakeplayerex.function;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import ranpoes.fakeplayerex.FakePlayerEx;

/**
 * 对接Fakeplayer插件，命令执行需要与服务端主线程同步，使用Bukkit自带runable方法
 */
public class FakePlayerAct {

    private final FakePlayerEx plugin;
    private final FakePlayerRefresh fakePlayerRefresh = new FakePlayerRefresh();


    public FakePlayerAct(FakePlayerEx plugin){
        this.plugin = plugin;
    }

    public void fakePlayerJoin(String name){
        new BukkitRunnable() {
            @Override
            public void run(){
                String cmd = "fakeplayer add ";
                String cmd2 = "fakeplayer toggle ";
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),cmd+name);
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),cmd2+name);
                fakePlayerRefresh.refresh();
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void fakePlayerLeave(String name){
        new BukkitRunnable() {
            @Override
            public void run(){
                String cmd = "fakeplayer remove ";
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),cmd+name);
                fakePlayerRefresh.refresh();
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void fakePlayerChat(String name, String chat){
        new BukkitRunnable() {
            @Override
            public void run(){
                String cmd = "fakeplayer chat ";
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),cmd+name+' '+chat);
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void fakePlayerReload(){
        new BukkitRunnable() {
            @Override
            public void run(){
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),"fakeplayer reload");
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

}
