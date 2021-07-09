package  ranpoes.fakeplayerex.file;

import ranpoes.fakeplayerex.FakePlayerEx;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;

public class PlayerNameFile {

    private final FakePlayerEx plugin;
    private final Queue<String> players = new LinkedList<>();

    public PlayerNameFile(FakePlayerEx plugin){
        this.plugin = plugin;
        try{
            String configPath = plugin.getConfig().getString("names_path");
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configPath)));
            String data;
            while((data = br.readLine())!=null){
                players.add(data);
            }
        }catch(Exception e){
            System.out.println(e);
            return;
        }
    }

    public Queue<String> getPlayers(){
        return this.players;
    }

}
