package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.entity.ChestTileEntity;
import net.minecraft.world.level.tile.entity.TileEntity;

public class ChestTile extends EntityTile {
   private Random random = new Random();

   protected ChestTile(int var1) {
      super(var1, Material.wood);
      this.tex = 26;
   }

   public int getTexture(LevelSource var1, int var2, int var3, int var4, int var5) {
      if (var5 == 1) {
         return this.tex - 1;
      } else if (var5 == 0) {
         return this.tex - 1;
      } else {
         int var6 = var1.getTile(var2, var3, var4 - 1);
         int var7 = var1.getTile(var2, var3, var4 + 1);
         int var8 = var1.getTile(var2 - 1, var3, var4);
         int var9 = var1.getTile(var2 + 1, var3, var4);
         if (var6 != this.id && var7 != this.id) {
            if (var8 != this.id && var9 != this.id) {
               byte var15 = 3;
               if (Tile.solid[var6] && !Tile.solid[var7]) {
                  var15 = 3;
               }

               if (Tile.solid[var7] && !Tile.solid[var6]) {
                  var15 = 2;
               }

               if (Tile.solid[var8] && !Tile.solid[var9]) {
                  var15 = 5;
               }

               if (Tile.solid[var9] && !Tile.solid[var8]) {
                  var15 = 4;
               }

               return var5 == var15 ? this.tex + 1 : this.tex;
            } else if (var5 != 4 && var5 != 5) {
               int var14 = 0;
               if (var8 == this.id) {
                  var14 = -1;
               }

               int var16 = var1.getTile(var8 == this.id ? var2 - 1 : var2 + 1, var3, var4 - 1);
               int var17 = var1.getTile(var8 == this.id ? var2 - 1 : var2 + 1, var3, var4 + 1);
               if (var5 == 3) {
                  var14 = -1 - var14;
               }

               byte var18 = 3;
               if ((Tile.solid[var6] || Tile.solid[var16]) && !Tile.solid[var7] && !Tile.solid[var17]) {
                  var18 = 3;
               }

               if ((Tile.solid[var7] || Tile.solid[var17]) && !Tile.solid[var6] && !Tile.solid[var16]) {
                  var18 = 2;
               }

               return (var5 == var18 ? this.tex + 16 : this.tex + 32) + var14;
            } else {
               return this.tex;
            }
         } else if (var5 != 2 && var5 != 3) {
            int var10 = 0;
            if (var6 == this.id) {
               var10 = -1;
            }

            int var11 = var1.getTile(var2 - 1, var3, var6 == this.id ? var4 - 1 : var4 + 1);
            int var12 = var1.getTile(var2 + 1, var3, var6 == this.id ? var4 - 1 : var4 + 1);
            if (var5 == 4) {
               var10 = -1 - var10;
            }

            byte var13 = 5;
            if ((Tile.solid[var8] || Tile.solid[var11]) && !Tile.solid[var9] && !Tile.solid[var12]) {
               var13 = 5;
            }

            if ((Tile.solid[var9] || Tile.solid[var12]) && !Tile.solid[var8] && !Tile.solid[var11]) {
               var13 = 4;
            }

            return (var5 == var13 ? this.tex + 16 : this.tex + 32) + var10;
         } else {
            return this.tex;
         }
      }
   }

   public int getTexture(int var1) {
      if (var1 == 1) {
         return this.tex - 1;
      } else if (var1 == 0) {
         return this.tex - 1;
      } else {
         return var1 == 3 ? this.tex + 1 : this.tex;
      }
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      int var5 = 0;
      if (var1.getTile(var2 - 1, var3, var4) == this.id) {
         ++var5;
      }

      if (var1.getTile(var2 + 1, var3, var4) == this.id) {
         ++var5;
      }

      if (var1.getTile(var2, var3, var4 - 1) == this.id) {
         ++var5;
      }

      if (var1.getTile(var2, var3, var4 + 1) == this.id) {
         ++var5;
      }

      if (var5 > 1) {
         return false;
      } else if (this.isFullChest(var1, var2 - 1, var3, var4)) {
         return false;
      } else if (this.isFullChest(var1, var2 + 1, var3, var4)) {
         return false;
      } else if (this.isFullChest(var1, var2, var3, var4 - 1)) {
         return false;
      } else {
         return !this.isFullChest(var1, var2, var3, var4 + 1);
      }
   }

   private boolean isFullChest(Level var1, int var2, int var3, int var4) {
      if (var1.getTile(var2, var3, var4) != this.id) {
         return false;
      } else if (var1.getTile(var2 - 1, var3, var4) == this.id) {
         return true;
      } else if (var1.getTile(var2 + 1, var3, var4) == this.id) {
         return true;
      } else if (var1.getTile(var2, var3, var4 - 1) == this.id) {
         return true;
      } else {
         return var1.getTile(var2, var3, var4 + 1) == this.id;
      }
   }

   public void onRemove(Level var1, int var2, int var3, int var4) {
      ChestTileEntity var5 = (ChestTileEntity)var1.getTileEntity(var2, var3, var4);

      for(int var6 = 0; var6 < var5.getContainerSize(); ++var6) {
         ItemInstance var7 = var5.getItem(var6);
         if (var7 != null) {
            float var8 = this.random.nextFloat() * 0.8F + 0.1F;
            float var9 = this.random.nextFloat() * 0.8F + 0.1F;
            float var10 = this.random.nextFloat() * 0.8F + 0.1F;

            while(var7.count > 0) {
               int var11 = this.random.nextInt(21) + 10;
               if (var11 > var7.count) {
                  var11 = var7.count;
               }

               var7.count -= var11;
               ItemEntity var12 = new ItemEntity(var1, (double)((float)var2 + var8), (double)((float)var3 + var9), (double)((float)var4 + var10), new ItemInstance(var7.id, var11, var7.getAuxValue()));
               float var13 = 0.05F;
               var12.xd = (double)((float)this.random.nextGaussian() * var13);
               var12.yd = (double)((float)this.random.nextGaussian() * var13 + 0.2F);
               var12.zd = (double)((float)this.random.nextGaussian() * var13);
               var1.addEntity(var12);
            }
         }
      }

      super.onRemove(var1, var2, var3, var4);
   }

   public boolean use(Level var1, int var2, int var3, int var4, Player var5) {
      Object var6 = (ChestTileEntity)var1.getTileEntity(var2, var3, var4);
      if (var1.isSolidTile(var2, var3 + 1, var4)) {
         return true;
      } else if (var1.getTile(var2 - 1, var3, var4) == this.id && var1.isSolidTile(var2 - 1, var3 + 1, var4)) {
         return true;
      } else if (var1.getTile(var2 + 1, var3, var4) == this.id && var1.isSolidTile(var2 + 1, var3 + 1, var4)) {
         return true;
      } else if (var1.getTile(var2, var3, var4 - 1) == this.id && var1.isSolidTile(var2, var3 + 1, var4 - 1)) {
         return true;
      } else if (var1.getTile(var2, var3, var4 + 1) == this.id && var1.isSolidTile(var2, var3 + 1, var4 + 1)) {
         return true;
      } else {
         if (var1.getTile(var2 - 1, var3, var4) == this.id) {
            var6 = new CompoundContainer("Large chest", (ChestTileEntity)var1.getTileEntity(var2 - 1, var3, var4), (Container)var6);
         }

         if (var1.getTile(var2 + 1, var3, var4) == this.id) {
            var6 = new CompoundContainer("Large chest", (Container)var6, (ChestTileEntity)var1.getTileEntity(var2 + 1, var3, var4));
         }

         if (var1.getTile(var2, var3, var4 - 1) == this.id) {
            var6 = new CompoundContainer("Large chest", (ChestTileEntity)var1.getTileEntity(var2, var3, var4 - 1), (Container)var6);
         }

         if (var1.getTile(var2, var3, var4 + 1) == this.id) {
            var6 = new CompoundContainer("Large chest", (Container)var6, (ChestTileEntity)var1.getTileEntity(var2, var3, var4 + 1));
         }

         if (var1.isOnline) {
            return true;
         } else {
            var5.openContainer((Container)var6);
            return true;
         }
      }
   }

   protected TileEntity newTileEntity() {
      return new ChestTileEntity();
   }
}
