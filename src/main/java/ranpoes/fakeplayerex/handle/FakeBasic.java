package ranpoes.fakeplayerex.handle;

import ranpoes.fakeplayerex.api.Fake;
import ranpoes.fakeplayerex.api.FakeCreated;
import ranpoes.fakeplayerex.api.INPC;
import fakeplayer.FakeCreated1_12_R1;
import java.util.Optional;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FakeBasic implements Fake {

  private static final FakeCreated FAKE_CREATED = new FakeCreated1_12_R1();

  @NotNull
  private final String name;

  @NotNull
  private final Location spawnpoint;

  @Nullable
  private INPC npc;

  public FakeBasic(@NotNull final String name, @NotNull final Location spawnpoint) {
    this.name = name;
    this.spawnpoint = spawnpoint;
  }

  @NotNull
  @Override
  public String getName() {
    return this.name;
  }

  @NotNull
  @Override
  public Location getSpawnPoint() {
    return this.spawnpoint;
  }

  @Override
  public void spawn() {
    Optional.ofNullable(this.spawnpoint.getWorld()).ifPresent(world -> {
      if (!Optional.ofNullable(this.npc).isPresent()) {
        this.npc = FakeBasic.FAKE_CREATED.create(this.name, this.name, world);
      }
      this.npc.spawn(this.spawnpoint);
    });
  }

  @Override
  public void deSpawn() {
    Optional.ofNullable(this.npc).ifPresent(INPC::deSpawn);
  }

  @Override
  public void toggleVisible() {
    Optional.ofNullable(this.npc).ifPresent(INPC::toggleVisible);
  }
}
