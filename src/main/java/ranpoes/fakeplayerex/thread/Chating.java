package ranpoes.fakeplayerex.thread;

import ranpoes.fakeplayerex.FakePlayerEx;
import ranpoes.fakeplayerex.file.ChatDataFile;
import ranpoes.fakeplayerex.function.FakePlayerAct;

import java.util.*;

/**
 * 模拟假玩家聊天的线程
 */
public class Chating extends Thread{

    private final FakePlayerEx plugin;
    private final Queue<String> playerNamesJoin;
    private final ArrayList<String[]> chatsPlayerText;
    private final FakePlayerAct fakePlayerAct;

    /**
     * 引用传递的构造函数
     * 线程将会使用Clocking线程不断更新的玩家在线列表
     * 线程不安全：如果玩家聊天进行中，Clocking执行了该玩家下线，则其发言将失败
     */
    public Chating(FakePlayerEx plugin, Queue<String> playerNamesJoin){
        this.plugin = plugin;
        this.playerNamesJoin = playerNamesJoin;
        this.chatsPlayerText = new ChatDataFile(plugin).getChatsPlayerText();
        this.fakePlayerAct = new FakePlayerAct(plugin);
    }

    /**
     * 随机获取聊天上下文函数
     */
    public ArrayList<String[]> findContext(int takeWideMin, int takeWideMax){
        int ran1 = (int) (Math.random()*(chatsPlayerText.size()));
        int ran2 = (int) (Math.random()*(takeWideMax-takeWideMin)+takeWideMin);
        ArrayList<String[]> list = new ArrayList<>();
        try {
            for (int i = ran1 - ran2; i < ran1 + ran2; i++) {
                list.add(this.chatsPlayerText.get(i));
            }
        }catch(Exception e){
            return findContext(takeWideMin, takeWideMax);
        }
        return list;
    }

    /**
     * 语料库玩家ID与假玩家ID匹配
     */
    public HashMap<String, String> matchID(ArrayList<String[]> contextList){
        HashMap<String, String> map = new HashMap<>();
        //服务器在线假玩家的名字队列需打乱，否则一直是排头几人在聊天
        List<String> playerNamesJoinList = new ArrayList<>(this.playerNamesJoin);
        shuffle(playerNamesJoinList);
        Queue<String> playerNamesJoin = new LinkedList<>(playerNamesJoinList);
        String fakeName;
        for(String[] i : contextList){
            if(!map.containsKey(i[0])){
                fakeName = playerNamesJoin.poll();
                if(null == fakeName){
                    //语料库随机得语段的玩家数量大于当前服务器假玩家数量，匹配失败，重新选取语段
                    //以返回null表示匹配失败
                    return null;
                }else{
                    map.put(i[0],fakeName);
                }
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


    public void run(){
        fakePlayerAct.fakePlayerReload();
        while(true){
            //大于1人时才能有对话
            if(playerNamesJoin.size()>1){
                HashMap<String, String> IDS;
                ArrayList<String[]> context = findContext(3,8);
                //重复获取语段到有效为止
                while((IDS = matchID(context)) == null){
                    context = findContext(3,8);
                }
                //聊天逻辑在这里实现
                for(String[] i: context){
                    //模拟打字延迟
                    try{
                        //Thread.sleep(i[1].length()*1000+(int) (Math.random()*(6)-3)*1000);
                        Thread.sleep(100);
                    }catch( Exception e){
                        return;
                    }
                    //线程不安全，IDS的map内存放的玩家可能已异步下线，判断再发言
                    if(playerNamesJoin.contains(IDS.get(i[0]))){
                        fakePlayerAct.fakePlayerChat(IDS.get(i[0]),i[1]);
                    }
                }
            }
            //睡眠计时,时长为两个聊天会话的间隔，建议10min到15min
            try{
                //int ran1 = (int) (Math.random()*(5)+10)*60000;
                int ran1 = 2000;
                Thread.sleep(ran1);
            }catch( Exception e){
                return;
            }
        }
    }

}
