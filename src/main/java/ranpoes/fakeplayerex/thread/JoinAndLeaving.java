package ranpoes.fakeplayerex.thread;

import ranpoes.fakeplayerex.FakePlayerEx;
import ranpoes.fakeplayerex.function.FakePlayerAct;

import java.util.Queue;
import java.util.Random;

/**
 * 假玩家的指定时间段内随机登录与离线实现线程
 * 此功能的存在因于需要模拟多个玩家在时间刻内“陆续”加入或退出
 * 玩家聊天线程强依赖于此线程维护的在线人数
 */
public class JoinAndLeaving extends Thread{

    private final FakePlayerEx plugin;
    private final Queue<String> playerNamesJoin;
    private final Queue<String> playerNamesLeave;
    private final FakePlayerAct fakePlayerAct;
    private final int MIN_TIME_MILLIS;
    private int schedule;

    /**
     * 引用方式初始化成员变量，因此这里异步更新的在线玩家情况会同步回Clocking线程
     */
    public JoinAndLeaving(int MIN_TIME_MILLIS, FakePlayerEx plugin, int schedule, Queue<String> playerNamesJoin, Queue<String> playerNamesLeave){
        this.MIN_TIME_MILLIS = MIN_TIME_MILLIS;
        this.plugin = plugin;
        this.schedule = schedule;
        this.playerNamesJoin = playerNamesJoin;
        this.playerNamesLeave = playerNamesLeave;
        this.fakePlayerAct = new FakePlayerAct(plugin);
    }

    /**
     * 半个小时（1时间刻内）陆续完成登录或者离线任务
     * schedule的正负为当前在线假玩家数与期望假玩家数之差
     * 使用概率来实现
     */
    public void run(){
        //计算每分钟期望玩家加入或离线的概率，略微调低分母以希望提前完成任务规划
        float p = Math.abs(schedule)/20f;
        //使用概率实现的玩家随机计划登录离线
        try {
            //30min结束直接退出线程，29min时进行收尾，即计划任务没有完成的在此分钟全部完成
            int stp = 0;
            while (schedule != 0 && stp < 29) {
                //假玩家加入的实际实现
                if (tryJoinLeave(p)) {
                    if(schedule < 0){
                        playerJoin(playerNamesLeave.poll());
                        schedule+=1;
                    }else{
                        playerLeave(playerNamesJoin.poll());
                        schedule-=1;
                    }
                }
                stp+=1;
                try {
                    Thread.sleep(MIN_TIME_MILLIS);
                } catch (Exception e) {
                    return;
                }
            }
            //最后1min的收尾工作
            for(int i = 0;i<Math.abs(schedule);i++){
                if(schedule < 0){
                    playerJoin(playerNamesLeave.poll());
                }else{
                    playerLeave(playerNamesJoin.poll());
                }
            }

        }catch(Exception e){
            System.out.println(e);
        }
    }

    public boolean tryJoinLeave(float p) {
        return new Random().nextFloat() <= p;
    }

    public void playerJoin(String name) {
        playerNamesJoin.add(name);
        fakePlayerAct.fakePlayerJoin(name);
    }

    public void playerLeave(String name) {
        playerNamesLeave.add(name);
        fakePlayerAct.fakePlayerLeave(name);
    }

}
