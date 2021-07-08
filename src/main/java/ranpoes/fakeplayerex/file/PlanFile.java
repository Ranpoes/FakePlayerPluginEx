package ranpoes.fakeplayerex.file;

import ranpoes.fakeplayerex.FakePlayerEx;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


/**
 *
 * 玩家时间-人数规划文件读取类
 * 以队列形式按时间顺序保存每一刻的玩家人数
 * 默认从0点开始，半小时一刻，则有48刻
 *
 * */
public class PlanFile {
    private final FakePlayerEx plugin;
    private Queue<Integer> timePlayerNums = new LinkedList<>();

    /**
     * 带开始时间刻的构造函数
     * 比如服务器3点重启，初始化本插件时读取的每日时间规划自身则是从0点开始的
     * 为便于对齐则使用此构造函数
     * n为启动时间24小时制的小时位减去0后乘以2
     */
    public PlanFile(FakePlayerEx plugin, int n){
        this.plugin = plugin;
        try{
            String configPath = plugin.getConfig().getString("plan_path");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configPath)));
            String data;
            //调整时间队列首为n时刻，实现为n刻分割两个Array，从后者开始入队，再前者
            ArrayList<String> a1 = new ArrayList<>();
            ArrayList<String> a2 = new ArrayList<>();
            for(int step=0;step<n;step+=1){
                a1.add(br.readLine());
            }
            for(int step=n;step<48;step+=1){
                data = br.readLine();
                a2.add(data);
                this.timePlayerNums.add(Integer.parseInt(data));
            }
            for(String playerNum : a1){
                this.timePlayerNums.add(Integer.parseInt(playerNum));
            }
        }catch(Exception e){
            return;
        }
    }

    public Queue<Integer> getTimePlayerNums(){
        return this.timePlayerNums;
    }

}
