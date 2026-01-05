package net.minecraft.world.level.tile;

import net.minecraft.world.level.Level;

public class Mushroom extends Bush {
   protected Mushroom(int var1, int var2) {
      super(var1, var2);
      float var3 = 0.2F;
      this.setShape(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, var3 * 2.0F, 0.5F + var3);
   }

   protected boolean mayPlaceOn(int var1) {
      return Tile.solid[var1];
   }

   public boolean canSurvive(Level var1, int var2, int var3, int var4) {
      return var1.getRawBrightness(var2, var3, var4) <= 13 && this.mayPlaceOn(var1.getTile(var2, var3 - 1, var4));
   }
}
