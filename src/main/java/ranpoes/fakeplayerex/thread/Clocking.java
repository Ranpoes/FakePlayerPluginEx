package ranpoes.fakeplayerex.thread;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import ranpoes.fakeplayerex.file.PlanFile;
import ranpoes.fakeplayerex.file.PlayerNameFile;
import ranpoes.fakeplayerex.FakePlayerEx;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Queue;
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
    private static String TITLE = ChatColor.RED+"["+ChatColor.GOLD+"FakePlayerEx"+ChatColor.RED+"] ";

    /**
     *
     */
    public Clocking(FakePlayerEx plugin){
        //等待服务端启动完毕,删除fakeplayer的保存假玩家文件,初始化开始时间刻
        try{
            File file = new File(plugin.getConfig().getString("fakes_path"));
            if(file.exists()) {
                file.delete();
                logger.log(Level.INFO, TITLE+ChatColor.BLUE+"fakes.json删除成功");
            }
            int h = new GregorianCalendar().get(Calendar.HOUR_OF_DAY);
            int m = new GregorianCalendar().get(Calendar.MINUTE);
            this.clock = h * 2 + ( m>=30 ? 1 : 0);
            logger.log(Level.INFO, TITLE+ChatColor.BLUE+"启动的时间刻："+ChatColor.YELLOW);
        }catch(Exception e){
            logger.log(Level.SEVERE, TITLE+ChatColor.RED+"FakePlayerEx主线程启动失败！");
            logger.log(Level.SEVERE, ChatColor.RED+e.toString());
        }
        this.plugin = plugin;
        this.timePlayerNums = new PlanFile(plugin, clock).getTimePlayerNums();
        this.playerNamesJoin = new LinkedList<>();
        this.playerNamesLeave = new PlayerNameFile(plugin).getPlayers();
    }

    public void run(){
        int playerJoinNums = 0;  //维护当前时刻假玩家人数
        int num;                 //队列取得计划玩家数量的临时变量
        Queue<Integer> timePlayerNumsTmp;
        //外循环为一天日期变更，重置玩家人数计划队列
        while(true){
            timePlayerNumsTmp = new LinkedList<>(timePlayerNums);
            //异步模拟玩家聊天线程
            Chating threadChating = new Chating(plugin, playerNamesJoin, logger);
            threadChating.start();
            //内循环为24小时的时刻推演，半小时为一刻
            while(clock!=48){
                //获取期望的玩家数量
                num = timePlayerNumsTmp.poll();
                if(clock%2 == 0)
                    logger.log(Level.INFO, TITLE+ChatColor.BLUE+"规划时间："+ChatColor.YELLOW+clock/2+ChatColor.BLUE+" 点，期望的玩家数量: "+ChatColor.YELLOW+num);
                else
                    logger.log(Level.INFO, TITLE+ChatColor.BLUE+"规划时间："+ChatColor.YELLOW+clock/2+ChatColor.BLUE+" 点半，期望的玩家数量: "+ChatColor.YELLOW+num);
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
