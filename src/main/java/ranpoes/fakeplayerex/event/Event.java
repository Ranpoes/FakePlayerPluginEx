package ranpoes.fakeplayerex.event;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ranpoes.fakeplayerex.FakePlayerEx;
import ranpoes.fakeplayerex.file.PlayerNameFile;

import java.util.Queue;

public class Event implements Listener {

    private FakePlayerEx plugin;
    private PlayerNameFile playerNameFile;
    private Queue<String> fakeplayers;

    public Event(FakePlayerEx plugin){
        this.plugin = plugin;
        this.playerNameFile = new PlayerNameFile(plugin);
        this.fakeplayers = playerNameFile.getPlayers();
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
            if(arr[0].equals("tpa")||
               arr[0].equals("tp")||
               arr[0].equals("m")||
               arr[0].equals("message")) {
                //如果命令指向假玩家
                if(fakeplayers.contains(arr[1])){
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED+"目标玩家屏蔽了所有的新人消息");
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }

}