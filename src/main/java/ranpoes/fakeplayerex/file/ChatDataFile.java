package ranpoes.fakeplayerex.file;

import org.bukkit.ChatColor;
import ranpoes.fakeplayerex.FakePlayerEx;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatDataFile {

    private final FakePlayerEx plugin;
    private final Logger logger;
    private static String TITLE = ChatColor.RED+"["+ChatColor.GOLD+"FakePlayerEx"+ChatColor.RED+"] ";
    //整个插件运行的生命周期内都会被维护的，未被使用语料库，在插件运行结束后会写回文件
    private ArrayList<String[]> chatsPlayerText = new ArrayList<>();

    public ChatDataFile(FakePlayerEx plugin, Logger logger){
        this.plugin = plugin;
        this.logger = logger;
        try{
            String chatPath = plugin.getConfig().getString("chat_data_path");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(chatPath)));
            String data;
            ArrayList<String> chats = new ArrayList<>();
            while((data = br.readLine())!=null){
                chats.add(data);
            }
            // 按指定模式在字符串查找
            String pattern = "(.*) \\$ (.*)";
            // 创建 Pattern 对象
            Pattern r = Pattern.compile(pattern);
            for(String i : chats){
                Matcher m = r.matcher(i);
                if(m.find()){
                    this.chatsPlayerText.add(new String[]{m.group(1), m.group(2)});
                }
            }
        }catch(Exception e){
            System.out.println(e);
            return;
        }
    }

    /**
     * 读取聊天语料备份
     * 备份数据是不会被修改的文件！
     */
    public ArrayList<String[]> readChatBack(FakePlayerEx plugin){
        try{
            ArrayList<String[]> chatsPlayerTextBack = new ArrayList<>();
            String chatPath = plugin.getConfig().getString("chat_data_path_back");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(chatPath)));
            String data;
            ArrayList<String> chats = new ArrayList<>();
            while((data = br.readLine())!=null){
                chats.add(data);
            }
            // 按指定模式在字符串查找
            String pattern = "(.*) \\$ (.*)";
            // 创建 Pattern 对象
            Pattern r = Pattern.compile(pattern);
            for(String i : chats){
                Matcher m = r.matcher(i);
                if(m.find()){
                    chatsPlayerTextBack.add(new String[]{m.group(1), m.group(2)});
                }
            }
            return chatsPlayerTextBack;
        }catch(Exception e){
            System.out.println(e);
            return null;
        }
    }


    /**
     * 随机获取聊天上下文函数
     * 维护两份语料库，其中一份为备份，另一份不断提取语句并原地删除，直至容量不足再拿备份重置
     * 这样可以避免一轮启动周期内可能的复读现象
     */
    public ArrayList<String[]> findContext(int takeWideMin, int takeWideMax){
        int chatLinesMax = chatsPlayerText.size();
        int ran1 = (int) (Math.random()*(chatLinesMax));
        int ran2 = (int) (Math.random()*(takeWideMax-takeWideMin)+takeWideMin);
        ArrayList<String[]> list = new ArrayList<>();
        try {
            for (int i = (ran1 - ran2<0 ? 0 : ran1-ran2); i < (ran1 + ran2>=chatLinesMax? chatLinesMax:ran1+ran2); i++) {
                list.add(chatsPlayerText.get(i));
                chatsPlayerText.remove(i);
            }
        }catch(Exception e){
            return findContext(takeWideMin, takeWideMax);
        }
        if(chatLinesMax<50){
            chatsPlayerText = new ArrayList<>(readChatBack(plugin));
            logger.log(Level.INFO, TITLE+ChatColor.GOLD+"一轮周期的语料内容已使用完毕，现已重置");
        }
        return list;
    }


    /**
     * 插件关闭时将未使用的语料写回文件，以备下次使用
     * 考虑服务器目前只部署在win上，编码使用GB2312
     */
    public void writeBack(FakePlayerEx plugin){
        String chatPath = plugin.getConfig().getString("chat_data_path");
        try{
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(chatPath),"GB2312");
            for(String[] i : chatsPlayerText){
                out.write(i[0]+" $ "+i[1]+"\n");
            }
            logger.log(Level.INFO, TITLE+ChatColor.GOLD+"成功写回了未使用的语料");
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public ArrayList<String[]> getChatsPlayerText(){
        return this.chatsPlayerText;
    }

}
