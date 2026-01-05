package net.minecraft.world.level.tile;

import java.util.Random;

public class ObsidianTile extends StoneTile {
   public ObsidianTile(int var1, int var2) {
      super(var1, var2);
   }

   public int getResourceCount(Random var1) {
      return 1;
   }

   public int getResource(int var1, Random var2) {
      return Tile.obsidian.id;
   }
}
