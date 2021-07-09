package ranpoes.fakeplayerex.file;

import ranpoes.fakeplayerex.FakePlayerEx;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatDataFile {

    private final FakePlayerEx plugin;
    private final ArrayList<String> chats = new ArrayList<>();
    private final ArrayList<String[]> chatsPlayerText = new ArrayList<>();

    public ChatDataFile(FakePlayerEx plugin){
        this.plugin = plugin;
        try{
            String configPath = plugin.getConfig().getString("chat_data_path");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configPath)));
            String data;
            while((data = br.readLine())!=null){
                this.chats.add(data);
            }
            // 按指定模式在字符串查找
            String pattern = "(.*) \\$ (.*)";
            // 创建 Pattern 对象
            Pattern r = Pattern.compile(pattern);
            for(String i : this.chats){
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

    public ArrayList<String> getChats(){
        return this.chats;
    }

    public ArrayList<String[]> getChatsPlayerText(){
        return this.chatsPlayerText;
    }

}
