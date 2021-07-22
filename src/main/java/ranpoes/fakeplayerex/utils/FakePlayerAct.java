package ranpoes.fakeplayerex.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import ranpoes.fakeplayerex.FakePlayerEx;
import ranpoes.fakeplayerex.api.Fake;
import ranpoes.fakeplayerex.handle.FakeBasic;
import ranpoes.fakeplayerex.handle.ListenerBasic;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FakePlayerAct {

    public final Map<String, Fake> fakeplayers = new HashMap<>();
    private final FakePlayerEx plugin;
    private final FakePlayerRefresh fakePlayerRefresh;
    private final TextHelper textHelper;
    private final Logger logger = Bukkit.getLogger();
    private final String TITLE = TextHelper.LOG_TITLE;


    public FakePlayerAct(FakePlayerEx plugin){
        this.plugin = plugin;
        this.fakePlayerRefresh = new FakePlayerRefresh();
        this.textHelper = new TextHelper();
        //注册登录监听事件：由于每个玩家在不同时刻登录时，获得的假玩家在线情况不同，需要执行全假玩家重启来刷新
        new ListenerBasic<>(
                PlayerJoinEvent.class,
                event ->
                        this.fakeplayers.values().forEach(npc -> {
                            npc.deSpawn();
                            npc.spawn();
                        })
        ).register(this.plugin);
    }

    public void addFakes(@NotNull final String name, @NotNull final Location location) {
        final Fake fake = new FakeBasic(name, location);
        this.fakeplayers.put(name, fake);
        new BukkitRunnable() {
            @Override
            public void run(){
                fake.spawn();
                //默认隐身
                fake.toggleVisible();
                fakePlayerRefresh.refresh();
                logger.log(Level.INFO, TITLE+ChatColor.GREEN+"加入玩家 "+name);
                Bukkit.getOnlinePlayers().forEach(player ->
                        player.sendMessage(textHelper.toServerJoinFormat(name)));
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void removeFakes(@NotNull final String name) {
        final Fake fake = this.fakeplayers.remove(name);
        new BukkitRunnable() {
            @Override
            public void run(){
                fake.deSpawn();
                logger.log(Level.INFO, TITLE+ChatColor.RED+"下线玩家 "+name);
                Bukkit.getOnlinePlayers().forEach(player ->
                        player.sendMessage(textHelper.toServerLeaveFormat(name)));
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void chatFakes(@NotNull final String name, @NotNull final String chats) {
        new BukkitRunnable() {
            @Override
            public void run(){
                Bukkit.getOnlinePlayers().forEach(player ->
                        player.sendMessage(textHelper.toServerChatFormat(name, chats)));
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

}
