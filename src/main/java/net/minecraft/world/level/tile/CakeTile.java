package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class CakeTile extends Tile {
   protected CakeTile(int var1, int var2) {
      super(var1, var2, Material.cake);
      this.setTicking(true);
   }

   public void updateShape(LevelSource var1, int var2, int var3, int var4) {
      int var5 = var1.getData(var2, var3, var4);
      float var6 = 0.0625F;
      float var7 = (float)(1 + var5 * 2) / 16.0F;
      float var8 = 0.5F;
      this.setShape(var7, 0.0F, var6, 1.0F - var6, var8, 1.0F - var6);
   }

   public void updateDefaultShape() {
      float var1 = 0.0625F;
      float var2 = 0.5F;
      this.setShape(var1, 0.0F, var1, 1.0F - var1, var2, 1.0F - var1);
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getData(var2, var3, var4);
      float var6 = 0.0625F;
      float var7 = (float)(1 + var5 * 2) / 16.0F;
      float var8 = 0.5F;
      return AABB.newTemp((double)((float)var2 + var7), (double)var3, (double)((float)var4 + var6), (double)((float)(var2 + 1) - var6), (double)((float)var3 + var8 - var6), (double)((float)(var4 + 1) - var6));
   }

   public AABB getTileAABB(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getData(var2, var3, var4);
      float var6 = 0.0625F;
      float var7 = (float)(1 + var5 * 2) / 16.0F;
      float var8 = 0.5F;
      return AABB.newTemp((double)((float)var2 + var7), (double)var3, (double)((float)var4 + var6), (double)((float)(var2 + 1) - var6), (double)((float)var3 + var8), (double)((float)(var4 + 1) - var6));
   }

   public int getTexture(int var1, int var2) {
      if (var1 == 1) {
         return this.tex;
      } else if (var1 == 0) {
         return this.tex + 3;
      } else {
         return var2 > 0 && var1 == 4 ? this.tex + 2 : this.tex + 1;
      }
   }

   public int getTexture(int var1) {
      if (var1 == 1) {
         return this.tex;
      } else {
         return var1 == 0 ? this.tex + 3 : this.tex + 1;
      }
   }

   public boolean isCubeShaped() {
      return false;
   }

   public boolean isSolidRender() {
      return false;
   }

   public boolean use(Level var1, int var2, int var3, int var4, Player var5) {
      this.eat(var1, var2, var3, var4, var5);
      return true;
   }

   public void attack(Level var1, int var2, int var3, int var4, Player var5) {
      this.eat(var1, var2, var3, var4, var5);
   }

   private void eat(Level var1, int var2, int var3, int var4, Player var5) {
      if (var5.health < 20) {
         var5.heal(3);
         int var6 = var1.getData(var2, var3, var4) + 1;
         if (var6 >= 6) {
            var1.setTile(var2, var3, var4, 0);
         } else {
            var1.setData(var2, var3, var4, var6);
            var1.setTileDirty(var2, var3, var4);
         }
      }

   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      return !super.mayPlace(var1, var2, var3, var4) ? false : this.canSurvive(var1, var2, var3, var4);
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      if (!this.canSurvive(var1, var2, var3, var4)) {
         this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
         var1.setTile(var2, var3, var4, 0);
      }

   }

   public boolean canSurvive(Level var1, int var2, int var3, int var4) {
      return var1.getMaterial(var2, var3 - 1, var4).isSolid();
   }

   public int getResourceCount(Random var1) {
      return 0;
   }

   public int getResource(int var1, Random var2) {
      return 0;
   }
}
