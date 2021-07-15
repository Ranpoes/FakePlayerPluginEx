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
    private ArrayList<ArrayList<String[]>> chatsPlayerText = new ArrayList<>();
    //回调时同步删除语料内容，保存选段下标
    int context_loc;

    public ChatDataFile(FakePlayerEx plugin, Logger logger){
        this.plugin = plugin;
        this.logger = logger;
        ArrayList<String[]> chatsPlayerTextTmp = new ArrayList<>();
        // 按指定模式在字符串查找
        String pattern = "(.*) \\$ (.*)";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        try{
            String chatPath = plugin.getConfig().getString("chat_data_path");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(chatPath)));
            String data;
            ArrayList<String> chats = new ArrayList<>();
            while((data = br.readLine())!=null){
                chats.add(data);
            }
            for(String i : chats){
                //语料数据集中，具有时间距离的两段交流以@分隔，以此体现对话逻辑性
                if(i.contains("@")){
                    chatsPlayerText.add(new ArrayList<>(chatsPlayerTextTmp));
                    chatsPlayerTextTmp.clear();
                    continue;
                }
                Matcher m = r.matcher(i);
                if(m.find()){
                    chatsPlayerTextTmp.add(new String[]{m.group(1), m.group(2)});
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
    public ArrayList<ArrayList<String[]>> readChatBack(){
        ArrayList<ArrayList<String[]>> chatsPlayerTextBack = new ArrayList<>();
        ArrayList<String[]> chatsPlayerTextTmp = new ArrayList<>();
        // 按指定模式在字符串查找
        String pattern = "(.*) \\$ (.*)";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        try{
            String chatPath = plugin.getConfig().getString("chat_data_path_back");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(chatPath)));
            String data;
            ArrayList<String> chats = new ArrayList<>();
            while((data = br.readLine())!=null){
                chats.add(data);
            }
            for(String i : chats){
                //语料数据集中，具有时间距离的两段交流以@分隔，以此体现对话逻辑性
                if(i.contains("@")){
                    chatsPlayerTextBack.add(new ArrayList<>(chatsPlayerTextTmp));
                    chatsPlayerTextTmp.clear();
                    continue;
                }
                Matcher m = r.matcher(i);
                if(m.find()){
                    chatsPlayerTextTmp.add(new String[]{m.group(1), m.group(2)});
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
     * 删除使用过的语料需要回调，因为这里产生的文本不一定能够在聊天线程中匹配到id，而可能直接被弃用
     */
    public ArrayList<String[]> findContext(){
        int chatLinesMax = chatsPlayerText.size();
        context_loc = (int) (Math.random()*(chatLinesMax));
        //每次匹配文本都会更新起始终止下标，只有匹配成功时的回调synDelete才会用其进行语料删除
        //语段容量过少时直接拿备份重置,触发阈值不能太小，因为剩下的可能都是被拒取的脏数据
        if(chatLinesMax<5){
            chatsPlayerText = new ArrayList<>(readChatBack());
            logger.log(Level.INFO, TITLE+ChatColor.GOLD+"一轮周期的语料内容已使用完毕，现已重置");
        }
        return chatsPlayerText.get(context_loc);
    }

    /**
     * 同步删除语料
     */
    public void synDelete(){
        chatsPlayerText.remove(context_loc);
    }

    /**
     * 插件关闭时将未使用的语料写回文件，以备下次使用
     * 考虑服务器目前只部署在win上，编码使用GB2312
     */
    public void writeBack(FakePlayerEx plugin){
        String chatPath = plugin.getConfig().getString("chat_data_path");
        try{
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(chatPath),"GB2312");
            for(ArrayList<String[]> i : chatsPlayerText){
                for(String[] j : i){
                    out.write(j[0]+" $ "+j[1]+"\n");
                }
                //写回语段时间性分隔标识符！！！
                out.write("@\n");
            }
            //使用OutputStreamWriter要close，不然写不完整
            out.close();
            logger.log(Level.INFO, TITLE+ChatColor.GOLD+"成功写回了"+chatsPlayerText.size()+"段未使用的语料");
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public ArrayList<ArrayList<String[]>> getChatsPlayerText(){
        return this.chatsPlayerText;
    }

}
