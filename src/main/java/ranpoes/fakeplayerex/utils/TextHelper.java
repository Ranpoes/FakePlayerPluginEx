package ranpoes.fakeplayerex.utils;

import org.bukkit.ChatColor;

public class TextHelper {

    public static String LOG_TITLE = ChatColor.RED+"["+ChatColor.GOLD+"FakePlayerEx"+ChatColor.RED+"] ";

    public String toServerChatFormat(String name,String chats){
        return "§a生存世界§d丨§e冒险家§f"+name+"§a§l :§f "+chats;
    }

    public String toServerJoinFormat(String name){
        return "§8[§a+§8]§d玩家 §a"+name+" §d加入游戏";
    }

    public String toServerLeaveFormat(String name){
        return "§8[§c-§8]§d玩家 §a"+name+" §d退出游戏";
    }

}
