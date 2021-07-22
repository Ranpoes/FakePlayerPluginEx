package ranpoes.fakeplayerex.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.event.player.*;

import java.util.UUID;

/**
 * 使用玩家登录事件来刷新MOTD，以修复服务器无真实玩家时人数不更新问题
 */
public class FakePlayerRefresh {
    private static String name = "AA";
    private static UUID uuid = UUID.randomUUID();

    public void refresh() {

        WorldServer worldServer = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();

        EntityPlayer entityPlayer = createEntityPlayer(uuid, name, worldServer);

        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(((CraftServer) Bukkit.getServer()).getPlayer(entityPlayer),  "");;

        Bukkit.getPluginManager().callEvent(playerJoinEvent);

    }

    private static EntityPlayer createEntityPlayer(UUID uuid, String name, WorldServer worldServer) {
        MinecraftServer mcServer = ((CraftServer) Bukkit.getServer()).getServer();
        GameProfile gameProfile = new GameProfile(uuid, name);
        return new EntityPlayer(mcServer, worldServer, gameProfile, new PlayerInteractManager(worldServer));
    }


}
