package ranpoes.fakeplayerex.thread;

import ranpoes.fakeplayerex.file.PlanFile;
import ranpoes.fakeplayerex.file.PlayerNameFile;
import ranpoes.fakeplayerex.FakePlayerEx;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;


/**
 * 插件维护的主线程，负责整个玩家计划的时刻推进
 */
public class Clocking extends Thread{

    private final FakePlayerEx plugin;
    private int clock;
    private final Queue<Integer> timePlayerNums;
    private final Queue<String> playerNamesJoin;
    private final Queue<String> playerNamesLeave;

    /**
     * @param clock 起始时间刻
     */
    public Clocking(FakePlayerEx plugin, int clock){
        this.plugin = plugin;
        this.clock = clock;
        this.timePlayerNums = new PlanFile(plugin, clock).getTimePlayerNums();
        this.playerNamesJoin = new LinkedList<>();
        this.playerNamesLeave = new PlayerNameFile(plugin).getPlayers();
    }

    public void run(){
        //等待服务端启动完毕,同时删除fakeplayer的保存假玩家文件
        try{
            Thread.sleep(10000);
            File file = new File(plugin.getConfig().getString("fakes_path"));
            if(file.exists()) {
                file.delete();
                System.out.println("fakes.json删除成功");
            }
        }catch( Exception e){
            return;
        }
        int playerJoinNums = 0;  //维护当前时刻假玩家人数
        int num;                 //队列取得计划玩家数量的临时变量
        Queue<Integer> timePlayerNumsTmp;
        //外循环为一天日期变更，重置玩家人数计划队列
        while(true){
            timePlayerNumsTmp = new LinkedList<>(timePlayerNums);
            //异步模拟玩家聊天线程
            Chating threadChating = new Chating(plugin, playerNamesJoin);
            threadChating.start();
            //内循环为24小时的时刻推演，半小时为一刻
            while(clock!=48){
                if(clock%2 == 0) System.out.println("规划时间刻："+clock/2+" 点");
                else System.out.println("规划时间刻："+clock/2+" 点半");
                //获取期望的玩家数量
                num = timePlayerNumsTmp.poll();
                System.out.println("期望的玩家数量: "+num);
                //异步模拟玩家陆续登录、离线线程。与计划的人数差和现有在线离线名单交由其处理
                JoinAndLeaving threadJoinLeaving = new JoinAndLeaving(plugin,playerJoinNums - num, playerNamesJoin, playerNamesLeave);
                threadJoinLeaving.start();
                //异步线程会在本时刻结束之前完成玩家人数变动，所以可以直接更新当前时刻的假玩家人数
                //即使真的没有完成，但也问题不大，此只导致与计划中的玩家人数产生细微差别
                playerJoinNums = num;
                //睡眠计时,时长为一刻度
                try{
                    //Thread.sleep(30*60000);
                    Thread.sleep(30*600);
                }catch( Exception e){
                    return;
                }
                //规划时间刻推进
                clock+=1;
            }
            threadChating.interrupt();
            clock = 0;
        }

    }
}
