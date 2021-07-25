package ranpoes.fakeplayerex.thread;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import ranpoes.fakeplayerex.file.PlanFile;
import ranpoes.fakeplayerex.file.PlayerNameFile;
import ranpoes.fakeplayerex.FakePlayerEx;
import ranpoes.fakeplayerex.utils.FakePlayerAct;
import ranpoes.fakeplayerex.utils.TextHelper;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 插件维护的主线程，负责整个玩家计划的时刻推进
 */
public class Clocking extends Thread{

    private final FakePlayerEx plugin;
    private final Queue<Integer> timePlayerNums;
    private final Queue<String> playerNamesJoin;
    private final Queue<String> playerNamesLeave;
    private final Logger logger = Bukkit.getLogger();
    private int clock;
    private final static int MIN_TIME_MILLIS = 60000; // 调试用的1分钟的毫秒长度
    private final static String TITLE = TextHelper.LOG_TITLE;
    //异步模拟玩家聊天线程,需能够在插件onDisable时维护
    private final Chating threadChating;
    //维护有所有在线的假玩家NPC实例
    private final FakePlayerAct fakePlayerAct;

    public Clocking(FakePlayerEx plugin){
        //等待服务端启动完毕,删除fakeplayer的保存假玩家文件,初始化开始时间刻
        try{
            int h = new GregorianCalendar().get(Calendar.HOUR_OF_DAY);
            int m = new GregorianCalendar().get(Calendar.MINUTE);
            this.clock = h * 2 + ( m>=30 ? 1 : 0);
            logger.log(Level.INFO, TITLE+ChatColor.BLUE+"启动的时间刻："+ChatColor.YELLOW+this.clock);
        }catch(Exception e){
            logger.log(Level.SEVERE, TITLE+ChatColor.RED+"FakePlayerEx主线程启动失败！");
            logger.log(Level.SEVERE, ChatColor.RED+e.toString());
        }
        this.plugin = plugin;
        this.timePlayerNums = new PlanFile(plugin, clock).getTimePlayerNums();
        this.playerNamesJoin = new LinkedList<>();
        this.playerNamesLeave = new PlayerNameFile(plugin).getPlayers();
        this.fakePlayerAct = new FakePlayerAct(plugin);
        //启动异步模拟玩家聊天线程
        this.threadChating = new Chating(MIN_TIME_MILLIS, plugin, playerNamesJoin, logger);
        threadChating.start();
        //激活刷怪Bug，否则如果此时服务器没有假玩家，则刷怪率极低
        fakePlayerAct.addFakes("AA", new Location(Bukkit.getWorld("world"),0,0,0), true);
        fakePlayerAct.removeFakes("AA", true);
    }

    public void close(){
        this.threadChating.close();
    }

    public void logPlan(int num){
        if(clock%2 == 0)
            logger.log(Level.INFO, TITLE+ChatColor.BLUE+"规划时间："+ChatColor.YELLOW+(clock/2)%24+ChatColor.BLUE+" 点，期望的玩家数量: "+ChatColor.YELLOW+num);
        else
            logger.log(Level.INFO, TITLE+ChatColor.BLUE+"规划时间："+ChatColor.YELLOW+(clock/2)%24+ChatColor.BLUE+" 点半，期望的玩家数量: "+ChatColor.YELLOW+num);
    }

    public void run(){
        int playerJoinNums = 0;  //维护当前时刻假玩家人数
        int num;                 //队列取得计划玩家数量的临时变量
        int clock_start = clock;
        Queue<Integer> timePlayerNumsTmp;
        //外循环为一天日期变更，重置玩家人数计划队列
        while(true){
            timePlayerNumsTmp = new LinkedList<>(timePlayerNums);
            //内循环为自启动插件时刻开始的24小时推演，半小时为一刻
            while(clock-clock_start!=48){
                //获取期望的玩家数量
                num = Optional.ofNullable(timePlayerNumsTmp.poll()).orElse(0);
                logPlan(num);
                //异步模拟玩家陆续登录、离线线程。与计划的人数差和现有在线离线名单交由其处理
                JoinAndLeaving threadJoinLeaving = new JoinAndLeaving(MIN_TIME_MILLIS, plugin,playerJoinNums - num, playerNamesJoin, playerNamesLeave, fakePlayerAct);
                threadJoinLeaving.start();
                //异步线程会在本时刻结束之前完成玩家人数变动，所以可以直接更新当前时刻的假玩家人数
                //即使真的没有完成，但也问题不大，此只导致与计划中的玩家人数产生细微差别
                playerJoinNums = num;
                //睡眠计时,时长为一刻度
                try{
                    Thread.sleep(30*MIN_TIME_MILLIS);
                }catch( Exception e){
                    return;
                }
                //规划时间刻推进
                clock+=1;
            }
            clock = clock_start;
        }

    }
}
