package net.minecraft.world.level.tile;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class LeverTile extends Tile {
   protected LeverTile(int var1, int var2) {
      super(var1, var2, Material.decoration);
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

   public int getRenderShape() {
      return 12;
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      if (var1.isSolidTile(var2 - 1, var3, var4)) {
         return true;
      } else if (var1.isSolidTile(var2 + 1, var3, var4)) {
         return true;
      } else if (var1.isSolidTile(var2, var3, var4 - 1)) {
         return true;
      } else if (var1.isSolidTile(var2, var3, var4 + 1)) {
         return true;
      } else {
         return var1.isSolidTile(var2, var3 - 1, var4);
      }
   }

   public void setPlacedOnFace(Level var1, int var2, int var3, int var4, int var5) {
      int var6 = var1.getData(var2, var3, var4);
      int var7 = var6 & 8;
      var6 &= 7;
      if (var5 == 1 && var1.isSolidTile(var2, var3 - 1, var4)) {
         var6 = 5 + var1.random.nextInt(2);
      }

      if (var5 == 2 && var1.isSolidTile(var2, var3, var4 + 1)) {
         var6 = 4;
      }

      if (var5 == 3 && var1.isSolidTile(var2, var3, var4 - 1)) {
         var6 = 3;
      }

      if (var5 == 4 && var1.isSolidTile(var2 + 1, var3, var4)) {
         var6 = 2;
      }

      if (var5 == 5 && var1.isSolidTile(var2 - 1, var3, var4)) {
         var6 = 1;
      }

      var1.setData(var2, var3, var4, var6 + var7);
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      if (var1.isSolidTile(var2 - 1, var3, var4)) {
         var1.setData(var2, var3, var4, 1);
      } else if (var1.isSolidTile(var2 + 1, var3, var4)) {
         var1.setData(var2, var3, var4, 2);
      } else if (var1.isSolidTile(var2, var3, var4 - 1)) {
         var1.setData(var2, var3, var4, 3);
      } else if (var1.isSolidTile(var2, var3, var4 + 1)) {
         var1.setData(var2, var3, var4, 4);
      } else if (var1.isSolidTile(var2, var3 - 1, var4)) {
         var1.setData(var2, var3, var4, 5 + var1.random.nextInt(2));
      }

      this.checkCanSurvive(var1, var2, var3, var4);
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      if (this.checkCanSurvive(var1, var2, var3, var4)) {
         int var6 = var1.getData(var2, var3, var4) & 7;
         boolean var7 = false;
         if (!var1.isSolidTile(var2 - 1, var3, var4) && var6 == 1) {
            var7 = true;
         }

         if (!var1.isSolidTile(var2 + 1, var3, var4) && var6 == 2) {
            var7 = true;
         }

         if (!var1.isSolidTile(var2, var3, var4 - 1) && var6 == 3) {
            var7 = true;
         }

         if (!var1.isSolidTile(var2, var3, var4 + 1) && var6 == 4) {
            var7 = true;
         }

         if (!var1.isSolidTile(var2, var3 - 1, var4) && var6 == 5) {
            var7 = true;
         }

         if (var7) {
            this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
            var1.setTile(var2, var3, var4, 0);
         }
      }

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

   public void updateShape(LevelSource var1, int var2, int var3, int var4) {
      int var5 = var1.getData(var2, var3, var4) & 7;
      float var6 = 0.1875F;
      if (var5 == 1) {
         this.setShape(0.0F, 0.2F, 0.5F - var6, var6 * 2.0F, 0.8F, 0.5F + var6);
      } else if (var5 == 2) {
         this.setShape(1.0F - var6 * 2.0F, 0.2F, 0.5F - var6, 1.0F, 0.8F, 0.5F + var6);
      } else if (var5 == 3) {
         this.setShape(0.5F - var6, 0.2F, 0.0F, 0.5F + var6, 0.8F, var6 * 2.0F);
      } else if (var5 == 4) {
         this.setShape(0.5F - var6, 0.2F, 1.0F - var6 * 2.0F, 0.5F + var6, 0.8F, 1.0F);
      } else {
         var6 = 0.25F;
         this.setShape(0.5F - var6, 0.0F, 0.5F - var6, 0.5F + var6, 0.6F, 0.5F + var6);
      }

   }

   public void attack(Level var1, int var2, int var3, int var4, Player var5) {
      this.use(var1, var2, var3, var4, var5);
   }

   public boolean use(Level var1, int var2, int var3, int var4, Player var5) {
      if (var1.isOnline) {
         return true;
      } else {
         int var6 = var1.getData(var2, var3, var4);
         int var7 = var6 & 7;
         int var8 = 8 - (var6 & 8);
         var1.setData(var2, var3, var4, var7 + var8);
         var1.setTilesDirty(var2, var3, var4, var2, var3, var4);
         var1.playSound((double)var2 + (double)0.5F, (double)var3 + (double)0.5F, (double)var4 + (double)0.5F, "random.click", 0.3F, var8 > 0 ? 0.6F : 0.5F);
         var1.updateNeighborsAt(var2, var3, var4, this.id);
         if (var7 == 1) {
            var1.updateNeighborsAt(var2 - 1, var3, var4, this.id);
         } else if (var7 == 2) {
            var1.updateNeighborsAt(var2 + 1, var3, var4, this.id);
         } else if (var7 == 3) {
            var1.updateNeighborsAt(var2, var3, var4 - 1, this.id);
         } else if (var7 == 4) {
            var1.updateNeighborsAt(var2, var3, var4 + 1, this.id);
         } else {
            var1.updateNeighborsAt(var2, var3 - 1, var4, this.id);
         }

         return true;
      }
   }

   public void onRemove(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getData(var2, var3, var4);
      if ((var5 & 8) > 0) {
         var1.updateNeighborsAt(var2, var3, var4, this.id);
         int var6 = var5 & 7;
         if (var6 == 1) {
            var1.updateNeighborsAt(var2 - 1, var3, var4, this.id);
         } else if (var6 == 2) {
            var1.updateNeighborsAt(var2 + 1, var3, var4, this.id);
         } else if (var6 == 3) {
            var1.updateNeighborsAt(var2, var3, var4 - 1, this.id);
         } else if (var6 == 4) {
            var1.updateNeighborsAt(var2, var3, var4 + 1, this.id);
         } else {
            var1.updateNeighborsAt(var2, var3 - 1, var4, this.id);
         }
      }

      super.onRemove(var1, var2, var3, var4);
   }

   public boolean getSignal(LevelSource var1, int var2, int var3, int var4, int var5) {
      return (var1.getData(var2, var3, var4) & 8) > 0;
   }

   public boolean getDirectSignal(Level var1, int var2, int var3, int var4, int var5) {
      int var6 = var1.getData(var2, var3, var4);
      if ((var6 & 8) == 0) {
         return false;
      } else {
         int var7 = var6 & 7;
         if (var7 == 5 && var5 == 1) {
            return true;
         } else if (var7 == 4 && var5 == 2) {
            return true;
         } else if (var7 == 3 && var5 == 3) {
            return true;
         } else if (var7 == 2 && var5 == 4) {
            return true;
         } else {
            return var7 == 1 && var5 == 5;
         }
      }
   }

   public boolean isSignalSource() {
      return true;
   }
}
