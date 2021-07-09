package ranpoes.fakeplayerex.thread;

import ranpoes.fakeplayerex.FakePlayerEx;
import ranpoes.fakeplayerex.function.FakePlayerAct;

import java.util.Queue;

/**
 * 假玩家的指定时间段内随机登录与离线实现线程
 * 此功能的存在因于需要模拟多个玩家在时间刻内“陆续”加入或退出
 * 玩家聊天线程强依赖于此线程维护的在线人数
 */
public class JoinAndLeaving extends Thread{

    private final FakePlayerEx plugin;
    private final int schedule;
    private final Queue<String> playerNamesJoin;
    private final Queue<String> playerNamesLeave;
    private final FakePlayerAct fakePlayerAct;

    /**
     * 引用方式初始化成员变量，因此这里异步更新的在线玩家情况会同步回Clocking线程
     */
    public JoinAndLeaving(FakePlayerEx plugin, int schedule, Queue<String> playerNamesJoin, Queue<String> playerNamesLeave){
        this.plugin = plugin;
        this.schedule = schedule;
        this.playerNamesJoin = playerNamesJoin;
        this.playerNamesLeave = playerNamesLeave;
        this.fakePlayerAct = new FakePlayerAct(plugin);
    }

    /**
     * 半个小时（1时间刻内）陆续完成登录或者离线任务
     * schedule的正负为当前在线假玩家数与期望假玩家数之差
     */
    public void run(){
        int avgWait;
        if(schedule==0){
            avgWait = 29; //避免均分时间时分母为0。如果人数无变动的话线程就持续29分钟
        }else{
            avgWait = 29/Math.abs(schedule);
        }
        //于平均登录、离线时长上增加-2min的随机浮动
        avgWait = (int) (Math.random()*(2)+avgWait-2);
        String name;
        //在线玩家人数小于期望，执行陆续登录
        try {
            if (schedule < 0) {
                //假玩家加入的实际实现
                for (int i = 0; i < -schedule; i++) {
                    name = playerNamesLeave.poll();
                    playerNamesJoin.add(name);
                    fakePlayerAct.fakePlayerJoin(name);
                    try {
                        Thread.sleep(avgWait * 60000);
                    } catch (Exception e) {
                        return;
                    }
                }
            } else if (schedule > 0) {
                //假玩家离线的实际实现
                for (int i = 0; i < schedule; i++) {
                    name = playerNamesJoin.poll();
                    playerNamesLeave.add(name);
                    fakePlayerAct.fakePlayerLeave(name);
                    try {
                        Thread.sleep(avgWait * 60000);
                    } catch (Exception e) {
                        return;
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }

}
