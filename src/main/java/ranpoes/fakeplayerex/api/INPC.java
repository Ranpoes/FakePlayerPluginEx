package ranpoes.fakeplayerex.api;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface INPC {

  void spawn(@NotNull Location location);

  void deSpawn();

  void toggleVisible();
}
