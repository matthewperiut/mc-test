package net.minecraft.world.level.tile;

import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;

public class CoralTile extends HalfTransparentTile {
   public CoralTile(int var1, int var2) {
      super(var1, var2, Material.coral, true);
      float var3 = 0.0625F;
      this.setShape(0.0F - var3, 0.0F - var3, 0.0F - var3, 1.0F + var3, 1.0F + var3, 1.0F + var3);
   }

   public int getColor(LevelSource var1, int var2, int var3, int var4) {
      return var2 * var2 * 3187961 + var2 * 987243 + var3 * var3 * 43297126 + var3 * 987121 + var4 * var4 * 927469861 + var4 * 1861 & 16777215;
   }
}
