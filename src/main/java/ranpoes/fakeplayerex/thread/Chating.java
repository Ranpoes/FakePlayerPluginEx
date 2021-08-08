package ranpoes.fakeplayerex.thread;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ranpoes.fakeplayerex.FakePlayerEx;
import ranpoes.fakeplayerex.file.ChatDataFile;
import ranpoes.fakeplayerex.utils.FakePlayerAct;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 模拟假玩家聊天的线程
 */
public class Chating extends Thread{

    private final FakePlayerEx plugin;
    private final Queue<String> playerNamesJoin;
    private final ChatDataFile chatDataFile;
    private final FakePlayerAct fakePlayerAct;
    private final Logger logger;
    private final int MIN_TIME_MILLIS;

    /**
     * 引用传递的构造函数
     * 线程将会使用Clocking线程不断更新的玩家在线列表
     * 线程不安全：如果玩家聊天进行中，Clocking执行了该玩家下线，则其发言将失败
     */
    public Chating(int MIN_TIME_MILLIS, FakePlayerEx plugin, Queue<String> playerNamesJoin, Logger logger){
        this.MIN_TIME_MILLIS = MIN_TIME_MILLIS;
        this.plugin = plugin;
        this.logger = logger;
        this.playerNamesJoin = playerNamesJoin;
        this.chatDataFile = new ChatDataFile(plugin, logger);
        this.fakePlayerAct = new FakePlayerAct(plugin);
    }

    /**
     * 语料库玩家ID与假玩家ID匹配
     * 需要甄别Op的语料，其必须和普通玩家分开不然破绽极大
     */
    public HashMap<String, String> matchID(ArrayList<String[]> contextList){
        String opName = "Op_AA";
        HashMap<String, String> map = new HashMap<>();
        //服务器在线假玩家的名字队列需打乱，否则一直是排头几人在聊天
        List<String> playerNamesJoinList = new ArrayList<>(this.playerNamesJoin);
        shuffle(playerNamesJoinList);
        Queue<String> playerNamesJoin = new LinkedList<>(playerNamesJoinList);
        String fakeName;
        //遍历获取的短文本，分配角色
        for(String[] i : contextList){
            if(!map.containsKey(i[0])){
                fakeName = playerNamesJoin.poll();
                if(null == fakeName){
                    //语料库随机得语段的玩家数量大于当前服务器假玩家数量，匹配失败，重新选取语段
                    //以返回null表示匹配失败
                    return null;
                }
                //上下文中如果出现op，直接绑定假玩家中的op角色
                if(i[0].equals(opName)){
                    boolean findFlag = false;
                    for(String j : playerNamesJoin.toArray(new String[0])){
                        if(j.equals(opName)){
                            map.put(i[0],opName);
                            playerNamesJoin.remove(opName);
                            findFlag = true;
                            break;
                        }
                    }
                    if(findFlag) {
                        continue;
                    }
                    else{
                        //上下文中如果出现op，在线假玩家中都没有op，那直接匹配失败
                        return null;
                    }
                }
                map.put(i[0],fakeName);
            }
        }
        return map;
    }

    /**
     * 原地打乱列表
     */
    public <T> void shuffle(List<T> list) {
        int size = list.size();
        Random random = new Random();
        for(int i = 0; i < size; i++) {
            // 获取随机位置
            int randomPos = random.nextInt(size);
            // 当前元素与随机元素交换
            T temp = list.get(i);
            list.set(i, list.get(randomPos));
            list.set(randomPos, temp);
        }
    }

    /**
     * 关闭线程时通知写回剩余未用语料
     */
    public void close(){
        chatDataFile.writeBack(plugin);
    }

    public boolean isInFakePlayerNames(String s){
        for(String i : playerNamesJoin){
            if (i.equals(s)){
                return true;
            }
        }
        return false;
    }

    public boolean isRealPlayer(){
        for(Player i : Bukkit.getOnlinePlayers()){
            if(!isInFakePlayerNames(i.getName())){
                return true;
            }
        }
        logger.log(Level.INFO, ChatColor.GOLD+"当前没有真人玩家在线，舍弃了一次模拟聊天");
        return false;
    }

    public void run(){
        int SEC_TIME_MILLIS = MIN_TIME_MILLIS/60;
        while(true){
            //大于1人时才能有对话，有活人时才能产生对话
            try{
                if(playerNamesJoin.size()>1 && isRealPlayer()){
                    HashMap<String, String> IDS;
                    ArrayList<String[]> context = chatDataFile.findContext();
                    //重复获取语段到有效为止
                    while((IDS = matchID(context)) == null){
                        context = chatDataFile.findContext();
                    }
                    chatDataFile.synDelete();
                    logger.log(Level.INFO, ChatColor.GOLD+"成功获取了一段聊天，总长："+ChatColor.RED+context.size());
                    logger.log(Level.INFO, ChatColor.GOLD+"剩余文本容量："+ChatColor.RED+chatDataFile.getChatsPlayerText().size());
                    //聊天逻辑在这里实现
                    for(String[] i: context){
                        //模拟打字延迟
                        try{
                            //每个字打3秒，全时间有-3~3秒的浮动
                            int time = i[1].length()*3*SEC_TIME_MILLIS+(int)(Math.random()*(6)-3)*SEC_TIME_MILLIS;
                            //全时间不可以小于3秒
                            time = Math.max(3*SEC_TIME_MILLIS, time);
                            Thread.sleep(time);
                        }catch( Exception e){
                            e.printStackTrace();
                            return;
                        }
                        //线程不安全，IDS的map内存放的玩家可能已异步下线，判断再发言
                        if(playerNamesJoin.contains(IDS.get(i[0]))){
                            fakePlayerAct.chatFakes(IDS.get(i[0]),i[1]);
                            logger.log(Level.INFO, IDS.get(i[0])+" : "+i[1]);
                        }else{
                            //直接掐掉会话，不然可能会出现独角戏的情况
                            break;
                        }
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            //睡眠计时,时长为两个聊天会话的间隔，建议10min到15min
            try{
                Thread.sleep((int) (Math.random()*(5)+10)*MIN_TIME_MILLIS);
            }catch( Exception e){
                e.printStackTrace();
            }
        }
    }

}
