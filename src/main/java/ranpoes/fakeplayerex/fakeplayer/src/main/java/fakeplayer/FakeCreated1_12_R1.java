package fakeplayer;

import ranpoes.fakeplayerex.api.INPC;
import ranpoes.fakeplayerex.api.FakeCreated;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.jetbrains.annotations.NotNull;

public final class FakeCreated1_12_R1 implements FakeCreated {

  @NotNull
  @Override
  public INPC create(@NotNull final String name, @NotNull final String tabname, @NotNull final World world) {
    return new NPC(
      Bukkit.getServer().getOfflinePlayer(name).getUniqueId(),
      name,
      tabname,
      (CraftWorld) world
    );
  }
}