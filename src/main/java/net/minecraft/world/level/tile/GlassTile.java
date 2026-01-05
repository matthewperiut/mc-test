package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.material.Material;

public class GlassTile extends HalfTransparentTile {
   public GlassTile(int var1, int var2, Material var3, boolean var4) {
      super(var1, var2, var3, var4);
   }

   public int getResourceCount(Random var1) {
      return 0;
   }
}
