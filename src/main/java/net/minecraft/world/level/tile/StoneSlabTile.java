package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;

public class StoneSlabTile extends Tile {
   private boolean fullSize;

   public StoneSlabTile(int var1, boolean var2) {
      super(var1, 6, Material.stone);
      this.fullSize = var2;
      if (!var2) {
         this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
      }

      this.setLightBlock(255);
   }

   public int getTexture(int var1) {
      return var1 <= 1 ? 6 : 5;
   }

   public boolean isSolidRender() {
      return this.fullSize;
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      if (this == Tile.stoneSlabHalf) {
         ;
      }
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      if (this != Tile.stoneSlabHalf) {
         super.onPlace(var1, var2, var3, var4);
      }

      int var5 = var1.getTile(var2, var3 - 1, var4);
      if (var5 == stoneSlabHalf.id) {
         var1.setTile(var2, var3, var4, 0);
         var1.setTile(var2, var3 - 1, var4, Tile.stoneSlab.id);
      }

   }

   public int getResource(int var1, Random var2) {
      return Tile.stoneSlabHalf.id;
   }

   public boolean isCubeShaped() {
      return this.fullSize;
   }

   public boolean shouldRenderFace(LevelSource var1, int var2, int var3, int var4, int var5) {
      if (this != Tile.stoneSlabHalf) {
         super.shouldRenderFace(var1, var2, var3, var4, var5);
      }

      if (var5 == 1) {
         return true;
      } else if (!super.shouldRenderFace(var1, var2, var3, var4, var5)) {
         return false;
      } else if (var5 == 0) {
         return true;
      } else {
         return var1.getTile(var2, var3, var4) != this.id;
      }
   }
}
