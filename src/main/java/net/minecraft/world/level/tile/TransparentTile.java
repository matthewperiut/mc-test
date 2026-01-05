package net.minecraft.world.level.tile;

import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;

public class TransparentTile extends Tile {
   protected boolean allowSame;

   protected TransparentTile(int var1, int var2, Material var3, boolean var4) {
      super(var1, var2, var3);
      this.allowSame = var4;
   }

   public boolean isSolidRender() {
      return false;
   }

   public boolean shouldRenderFace(LevelSource var1, int var2, int var3, int var4, int var5) {
      int var6 = var1.getTile(var2, var3, var4);
      return !this.allowSame && var6 == this.id ? false : super.shouldRenderFace(var1, var2, var3, var4, var5);
   }

   public boolean blocksLight() {
      return false;
   }
}
