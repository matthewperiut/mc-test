package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class TopSnowTile extends Tile {
   protected TopSnowTile(int var1, int var2) {
      super(var1, var2, Material.topSnow);
      this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
      this.setTicking(true);
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      return null;
   }

   public boolean blocksLight() {
      return false;
   }

   public boolean isSolidRender() {
      return false;
   }

   public boolean isCubeShaped() {
      return false;
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getTile(var2, var3 - 1, var4);
      return var5 != 0 && Tile.tiles[var5].isSolidRender() ? var1.getMaterial(var2, var3 - 1, var4).blocksMotion() : false;
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      this.checkCanSurvive(var1, var2, var3, var4);
   }

   private boolean checkCanSurvive(Level var1, int var2, int var3, int var4) {
      if (!this.mayPlace(var1, var2, var3, var4)) {
         this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
         var1.setTile(var2, var3, var4, 0);
         return false;
      } else {
         return true;
      }
   }

   public void playerDestroy(Level var1, int var2, int var3, int var4, int var5) {
      int var6 = Item.snowBall.id;
      float var7 = 0.7F;
      double var8 = (double)(var1.random.nextFloat() * var7) + (double)(1.0F - var7) * (double)0.5F;
      double var10 = (double)(var1.random.nextFloat() * var7) + (double)(1.0F - var7) * (double)0.5F;
      double var12 = (double)(var1.random.nextFloat() * var7) + (double)(1.0F - var7) * (double)0.5F;
      ItemEntity var14 = new ItemEntity(var1, (double)var2 + var8, (double)var3 + var10, (double)var4 + var12, new ItemInstance(var6, 1, 0));
      var14.throwTime = 10;
      var1.addEntity(var14);
      var1.setTile(var2, var3, var4, 0);
   }

   public int getResource(int var1, Random var2) {
      return Item.snowBall.id;
   }

   public int getResourceCount(Random var1) {
      return 0;
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      if (var1.getBrightness(LightLayer.Block, var2, var3, var4) > 11) {
         this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
         var1.setTile(var2, var3, var4, 0);
      }

   }

   public boolean shouldRenderFace(LevelSource var1, int var2, int var3, int var4, int var5) {
      Material var6 = var1.getMaterial(var2, var3, var4);
      if (var5 == 1) {
         return true;
      } else {
         return var6 == this.material ? false : super.shouldRenderFace(var1, var2, var3, var4, var5);
      }
   }
}
