package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;

public class CropTile extends Bush {
   protected CropTile(int var1, int var2) {
      super(var1, var2);
      this.tex = var2;
      this.setTicking(true);
      float var3 = 0.5F;
      this.setShape(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, 0.25F, 0.5F + var3);
   }

   protected boolean mayPlaceOn(int var1) {
      return var1 == Tile.farmland.id;
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      super.tick(var1, var2, var3, var4, var5);
      if (var1.getRawBrightness(var2, var3 + 1, var4) >= 9) {
         int var6 = var1.getData(var2, var3, var4);
         if (var6 < 7) {
            float var7 = this.getGrowthSpeed(var1, var2, var3, var4);
            if (var5.nextInt((int)(100.0F / var7)) == 0) {
               ++var6;
               var1.setData(var2, var3, var4, var6);
            }
         }
      }

   }

   public void growCropsToMax(Level var1, int var2, int var3, int var4) {
      var1.setData(var2, var3, var4, 7);
   }

   private float getGrowthSpeed(Level var1, int var2, int var3, int var4) {
      float var5 = 1.0F;
      int var6 = var1.getTile(var2, var3, var4 - 1);
      int var7 = var1.getTile(var2, var3, var4 + 1);
      int var8 = var1.getTile(var2 - 1, var3, var4);
      int var9 = var1.getTile(var2 + 1, var3, var4);
      int var10 = var1.getTile(var2 - 1, var3, var4 - 1);
      int var11 = var1.getTile(var2 + 1, var3, var4 - 1);
      int var12 = var1.getTile(var2 + 1, var3, var4 + 1);
      int var13 = var1.getTile(var2 - 1, var3, var4 + 1);
      boolean var14 = var8 == this.id || var9 == this.id;
      boolean var15 = var6 == this.id || var7 == this.id;
      boolean var16 = var10 == this.id || var11 == this.id || var12 == this.id || var13 == this.id;

      for(int var17 = var2 - 1; var17 <= var2 + 1; ++var17) {
         for(int var18 = var4 - 1; var18 <= var4 + 1; ++var18) {
            int var19 = var1.getTile(var17, var3 - 1, var18);
            float var20 = 0.0F;
            if (var19 == Tile.farmland.id) {
               var20 = 1.0F;
               if (var1.getData(var17, var3 - 1, var18) > 0) {
                  var20 = 3.0F;
               }
            }

            if (var17 != var2 || var18 != var4) {
               var20 /= 4.0F;
            }

            var5 += var20;
         }
      }

      if (var16 || var14 && var15) {
         var5 /= 2.0F;
      }

      return var5;
   }

   public int getTexture(int var1, int var2) {
      if (var2 < 0) {
         var2 = 7;
      }

      return this.tex + var2;
   }

   public int getRenderShape() {
      return 6;
   }

   public void destroy(Level var1, int var2, int var3, int var4, int var5) {
      super.destroy(var1, var2, var3, var4, var5);
      if (!var1.isOnline) {
         for(int var6 = 0; var6 < 3; ++var6) {
            if (var1.random.nextInt(15) <= var5) {
               float var7 = 0.7F;
               float var8 = var1.random.nextFloat() * var7 + (1.0F - var7) * 0.5F;
               float var9 = var1.random.nextFloat() * var7 + (1.0F - var7) * 0.5F;
               float var10 = var1.random.nextFloat() * var7 + (1.0F - var7) * 0.5F;
               ItemEntity var11 = new ItemEntity(var1, (double)((float)var2 + var8), (double)((float)var3 + var9), (double)((float)var4 + var10), new ItemInstance(Item.seeds));
               var11.throwTime = 10;
               var1.addEntity(var11);
            }
         }
      }

   }

   public int getResource(int var1, Random var2) {
      return var1 == 7 ? Item.wheat.id : -1;
   }

   public int getResourceCount(Random var1) {
      return 1;
   }
}
