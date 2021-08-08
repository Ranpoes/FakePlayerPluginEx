package ranpoes.fakeplayerex.event;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ranpoes.fakeplayerex.FakePlayerEx;
import ranpoes.fakeplayerex.file.PlayerNameFile;

import java.util.Random;


public class Event implements Listener {

    private final FakePlayerEx plugin;
    private final PlayerNameFile playerNameFile;
    private final String[] fakeplayers;

    public Event(FakePlayerEx plugin){
        this.plugin = plugin;
        this.playerNameFile = new PlayerNameFile(plugin);
        this.fakeplayers = playerNameFile.getPlayers().toArray(new String[0]);
    }

    /**
     * 玩家指令拦截器
     * 防止玩家对假玩家进行tpa等询问指向操作而暴露
     * 用伪造信息进行回应
     */
    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent event) {
        String [] arr = event.getMessage().split("\\s+");
        if(arr.length<2){
            return;
        }
        try{
            if(arr[0].equals("/tpa")||
               arr[0].equals("/tp")||
               arr[0].equals("/m")||
               arr[0].equals("/message")||
               arr[0].equals("/tell")) {
                //如果命令指向假玩家
                for(String i : fakeplayers){
                    if(i.equals(arr[1])){
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(ChatColor.RED+"目标玩家设置了勿扰模式");
                        break;
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 1.12 spigot的BUG导致使用Server.Entity类时会巨量刷生物，这里使用概率来取消其中一部分生物生成事件
     */
    @EventHandler
    public void onWorldSpawn(CreatureSpawnEvent event) {
        try{
            //只针对自然生成
            if(event.getSpawnReason().toString().equals("NATURAL")){
                if(new Random().nextFloat() <= 0.95){
                    event.setCancelled(true);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
