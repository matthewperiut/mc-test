package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DoorTile extends Tile {
   protected DoorTile(int var1, Material var2) {
      super(var1, var2);
      this.tex = 97;
      if (var2 == Material.metal) {
         ++this.tex;
      }

      float var3 = 0.5F;
      float var4 = 1.0F;
      this.setShape(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, var4, 0.5F + var3);
   }

   public int getTexture(int var1, int var2) {
      if (var1 != 0 && var1 != 1) {
         int var3 = this.getDir(var2);
         if ((var3 == 0 || var3 == 2) ^ var1 <= 3) {
            return this.tex;
         } else {
            int var4 = var3 / 2 + (var1 & 1 ^ var3);
            var4 += (var2 & 4) / 4;
            int var5 = this.tex - (var2 & 8) * 2;
            if ((var4 & 1) != 0) {
               var5 = -var5;
            }

            return var5;
         }
      } else {
         return this.tex;
      }
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
      return 7;
   }

   public AABB getTileAABB(Level var1, int var2, int var3, int var4) {
      this.updateShape(var1, var2, var3, var4);
      return super.getTileAABB(var1, var2, var3, var4);
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      this.updateShape(var1, var2, var3, var4);
      return super.getAABB(var1, var2, var3, var4);
   }

   public void updateShape(LevelSource var1, int var2, int var3, int var4) {
      this.setShape(this.getDir(var1.getData(var2, var3, var4)));
   }

   public void setShape(int var1) {
      float var2 = 0.1875F;
      this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
      if (var1 == 0) {
         this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, var2);
      }

      if (var1 == 1) {
         this.setShape(1.0F - var2, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      }

      if (var1 == 2) {
         this.setShape(0.0F, 0.0F, 1.0F - var2, 1.0F, 1.0F, 1.0F);
      }

      if (var1 == 3) {
         this.setShape(0.0F, 0.0F, 0.0F, var2, 1.0F, 1.0F);
      }

   }

   public void attack(Level var1, int var2, int var3, int var4, Player var5) {
      this.use(var1, var2, var3, var4, var5);
   }

   public boolean use(Level var1, int var2, int var3, int var4, Player var5) {
      if (this.material == Material.metal) {
         return true;
      } else {
         int var6 = var1.getData(var2, var3, var4);
         if ((var6 & 8) != 0) {
            if (var1.getTile(var2, var3 - 1, var4) == this.id) {
               this.use(var1, var2, var3 - 1, var4, var5);
            }

            return true;
         } else {
            if (var1.getTile(var2, var3 + 1, var4) == this.id) {
               var1.setData(var2, var3 + 1, var4, (var6 ^ 4) + 8);
            }

            var1.setData(var2, var3, var4, var6 ^ 4);
            var1.setTilesDirty(var2, var3 - 1, var4, var2, var3, var4);
            if (Math.random() < (double)0.5F) {
               var1.playSound((double)var2 + (double)0.5F, (double)var3 + (double)0.5F, (double)var4 + (double)0.5F, "random.door_open", 1.0F, var1.random.nextFloat() * 0.1F + 0.9F);
            } else {
               var1.playSound((double)var2 + (double)0.5F, (double)var3 + (double)0.5F, (double)var4 + (double)0.5F, "random.door_close", 1.0F, var1.random.nextFloat() * 0.1F + 0.9F);
            }

            return true;
         }
      }
   }

   public void setOpen(Level var1, int var2, int var3, int var4, boolean var5) {
      int var6 = var1.getData(var2, var3, var4);
      if ((var6 & 8) != 0) {
         if (var1.getTile(var2, var3 - 1, var4) == this.id) {
            this.setOpen(var1, var2, var3 - 1, var4, var5);
         }

      } else {
         boolean var7 = (var1.getData(var2, var3, var4) & 4) > 0;
         if (var7 != var5) {
            if (var1.getTile(var2, var3 + 1, var4) == this.id) {
               var1.setData(var2, var3 + 1, var4, (var6 ^ 4) + 8);
            }

            var1.setData(var2, var3, var4, var6 ^ 4);
            var1.setTilesDirty(var2, var3 - 1, var4, var2, var3, var4);
            if (Math.random() < (double)0.5F) {
               var1.playSound((double)var2 + (double)0.5F, (double)var3 + (double)0.5F, (double)var4 + (double)0.5F, "random.door_open", 1.0F, var1.random.nextFloat() * 0.1F + 0.9F);
            } else {
               var1.playSound((double)var2 + (double)0.5F, (double)var3 + (double)0.5F, (double)var4 + (double)0.5F, "random.door_close", 1.0F, var1.random.nextFloat() * 0.1F + 0.9F);
            }

         }
      }
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      int var6 = var1.getData(var2, var3, var4);
      if ((var6 & 8) != 0) {
         if (var1.getTile(var2, var3 - 1, var4) != this.id) {
            var1.setTile(var2, var3, var4, 0);
         }

         if (var5 > 0 && Tile.tiles[var5].isSignalSource()) {
            this.neighborChanged(var1, var2, var3 - 1, var4, var5);
         }
      } else {
         boolean var7 = false;
         if (var1.getTile(var2, var3 + 1, var4) != this.id) {
            var1.setTile(var2, var3, var4, 0);
            var7 = true;
         }

         if (!var1.isSolidTile(var2, var3 - 1, var4)) {
            var1.setTile(var2, var3, var4, 0);
            var7 = true;
            if (var1.getTile(var2, var3 + 1, var4) == this.id) {
               var1.setTile(var2, var3 + 1, var4, 0);
            }
         }

         if (var7) {
            this.spawnResources(var1, var2, var3, var4, var6);
         } else if (var5 > 0 && Tile.tiles[var5].isSignalSource()) {
            boolean var8 = var1.hasNeighborSignal(var2, var3, var4) || var1.hasNeighborSignal(var2, var3 + 1, var4);
            this.setOpen(var1, var2, var3, var4, var8);
         }
      }

   }

   public int getResource(int var1, Random var2) {
      if ((var1 & 8) != 0) {
         return 0;
      } else {
         return this.material == Material.metal ? Item.door_iron.id : Item.door_wood.id;
      }
   }

   public HitResult clip(Level var1, int var2, int var3, int var4, Vec3 var5, Vec3 var6) {
      this.updateShape(var1, var2, var3, var4);
      return super.clip(var1, var2, var3, var4, var5, var6);
   }

   public int getDir(int var1) {
      return (var1 & 4) == 0 ? var1 - 1 & 3 : var1 & 3;
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      if (var3 >= 127) {
         return false;
      } else {
         return var1.isSolidTile(var2, var3 - 1, var4) && super.mayPlace(var1, var2, var3, var4) && super.mayPlace(var1, var2, var3 + 1, var4);
      }
   }
}
