package net.minecraft.world.level.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.TilePos;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class RedStoneDustTile extends Tile {
   private boolean shouldSignal = true;
   private Set<TilePos> toUpdate = new HashSet();

   public RedStoneDustTile(int var1, int var2) {
      super(var1, var2, Material.decoration);
      this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
   }

   public int getTexture(int var1, int var2) {
      return this.tex + (var2 > 0 ? 16 : 0);
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      return null;
   }

   public boolean isSolidRender() {
      return false;
   }

   public boolean isCubeShaped() {
      return false;
   }

   public int getRenderShape() {
      return 5;
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      return var1.isSolidTile(var2, var3 - 1, var4);
   }

   private void updatePowerStrength(Level var1, int var2, int var3, int var4) {
      this.updatePowerStrength(var1, var2, var3, var4, var2, var3, var4);
      ArrayList var5 = new ArrayList(this.toUpdate);
      this.toUpdate.clear();

      for(int var6 = 0; var6 < var5.size(); ++var6) {
         TilePos var7 = (TilePos)var5.get(var6);
         var1.updateNeighborsAt(var7.x, var7.y, var7.z, this.id);
      }

   }

   private void updatePowerStrength(Level var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      int var8 = var1.getData(var2, var3, var4);
      int var9 = 0;
      this.shouldSignal = false;
      boolean var10 = var1.hasNeighborSignal(var2, var3, var4);
      this.shouldSignal = true;
      if (var10) {
         var9 = 15;
      } else {
         for(int var11 = 0; var11 < 4; ++var11) {
            int var12 = var2;
            int var13 = var4;
            if (var11 == 0) {
               var12 = var2 - 1;
            }

            if (var11 == 1) {
               ++var12;
            }

            if (var11 == 2) {
               var13 = var4 - 1;
            }

            if (var11 == 3) {
               ++var13;
            }

            if (var12 != var5 || var3 != var6 || var13 != var7) {
               var9 = this.checkTarget(var1, var12, var3, var13, var9);
            }

            if (var1.isSolidTile(var12, var3, var13) && !var1.isSolidTile(var2, var3 + 1, var4)) {
               if (var12 != var5 || var3 + 1 != var6 || var13 != var7) {
                  var9 = this.checkTarget(var1, var12, var3 + 1, var13, var9);
               }
            } else if (!var1.isSolidTile(var12, var3, var13) && (var12 != var5 || var3 - 1 != var6 || var13 != var7)) {
               var9 = this.checkTarget(var1, var12, var3 - 1, var13, var9);
            }
         }

         if (var9 > 0) {
            --var9;
         } else {
            var9 = 0;
         }
      }

      if (var8 != var9) {
         var1.noNeighborUpdate = true;
         var1.setData(var2, var3, var4, var9);
         var1.setTilesDirty(var2, var3, var4, var2, var3, var4);
         var1.noNeighborUpdate = false;

         for(int var18 = 0; var18 < 4; ++var18) {
            int var19 = var2;
            int var20 = var4;
            int var14 = var3 - 1;
            if (var18 == 0) {
               var19 = var2 - 1;
            }

            if (var18 == 1) {
               ++var19;
            }

            if (var18 == 2) {
               var20 = var4 - 1;
            }

            if (var18 == 3) {
               ++var20;
            }

            if (var1.isSolidTile(var19, var3, var20)) {
               var14 += 2;
            }

            int var15 = 0;
            var15 = this.checkTarget(var1, var19, var3, var20, -1);
            var9 = var1.getData(var2, var3, var4);
            if (var9 > 0) {
               --var9;
            }

            if (var15 >= 0 && var15 != var9) {
               this.updatePowerStrength(var1, var19, var3, var20, var2, var3, var4);
            }

            var15 = this.checkTarget(var1, var19, var14, var20, -1);
            var9 = var1.getData(var2, var3, var4);
            if (var9 > 0) {
               --var9;
            }

            if (var15 >= 0 && var15 != var9) {
               this.updatePowerStrength(var1, var19, var14, var20, var2, var3, var4);
            }
         }

         if (var8 == 0 || var9 == 0) {
            this.toUpdate.add(new TilePos(var2, var3, var4));
            this.toUpdate.add(new TilePos(var2 - 1, var3, var4));
            this.toUpdate.add(new TilePos(var2 + 1, var3, var4));
            this.toUpdate.add(new TilePos(var2, var3 - 1, var4));
            this.toUpdate.add(new TilePos(var2, var3 + 1, var4));
            this.toUpdate.add(new TilePos(var2, var3, var4 - 1));
            this.toUpdate.add(new TilePos(var2, var3, var4 + 1));
         }
      }

   }

   private void checkCornerChangeAt(Level var1, int var2, int var3, int var4) {
      if (var1.getTile(var2, var3, var4) == this.id) {
         var1.updateNeighborsAt(var2, var3, var4, this.id);
         var1.updateNeighborsAt(var2 - 1, var3, var4, this.id);
         var1.updateNeighborsAt(var2 + 1, var3, var4, this.id);
         var1.updateNeighborsAt(var2, var3, var4 - 1, this.id);
         var1.updateNeighborsAt(var2, var3, var4 + 1, this.id);
         var1.updateNeighborsAt(var2, var3 - 1, var4, this.id);
         var1.updateNeighborsAt(var2, var3 + 1, var4, this.id);
      }
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      super.onPlace(var1, var2, var3, var4);
      if (!var1.isOnline) {
         this.updatePowerStrength(var1, var2, var3, var4);
         var1.updateNeighborsAt(var2, var3 + 1, var4, this.id);
         var1.updateNeighborsAt(var2, var3 - 1, var4, this.id);
         this.checkCornerChangeAt(var1, var2 - 1, var3, var4);
         this.checkCornerChangeAt(var1, var2 + 1, var3, var4);
         this.checkCornerChangeAt(var1, var2, var3, var4 - 1);
         this.checkCornerChangeAt(var1, var2, var3, var4 + 1);
         if (var1.isSolidTile(var2 - 1, var3, var4)) {
            this.checkCornerChangeAt(var1, var2 - 1, var3 + 1, var4);
         } else {
            this.checkCornerChangeAt(var1, var2 - 1, var3 - 1, var4);
         }

         if (var1.isSolidTile(var2 + 1, var3, var4)) {
            this.checkCornerChangeAt(var1, var2 + 1, var3 + 1, var4);
         } else {
            this.checkCornerChangeAt(var1, var2 + 1, var3 - 1, var4);
         }

         if (var1.isSolidTile(var2, var3, var4 - 1)) {
            this.checkCornerChangeAt(var1, var2, var3 + 1, var4 - 1);
         } else {
            this.checkCornerChangeAt(var1, var2, var3 - 1, var4 - 1);
         }

         if (var1.isSolidTile(var2, var3, var4 + 1)) {
            this.checkCornerChangeAt(var1, var2, var3 + 1, var4 + 1);
         } else {
            this.checkCornerChangeAt(var1, var2, var3 - 1, var4 + 1);
         }

      }
   }

   public void onRemove(Level var1, int var2, int var3, int var4) {
      super.onRemove(var1, var2, var3, var4);
      if (!var1.isOnline) {
         var1.updateNeighborsAt(var2, var3 + 1, var4, this.id);
         var1.updateNeighborsAt(var2, var3 - 1, var4, this.id);
         this.updatePowerStrength(var1, var2, var3, var4);
         this.checkCornerChangeAt(var1, var2 - 1, var3, var4);
         this.checkCornerChangeAt(var1, var2 + 1, var3, var4);
         this.checkCornerChangeAt(var1, var2, var3, var4 - 1);
         this.checkCornerChangeAt(var1, var2, var3, var4 + 1);
         if (var1.isSolidTile(var2 - 1, var3, var4)) {
            this.checkCornerChangeAt(var1, var2 - 1, var3 + 1, var4);
         } else {
            this.checkCornerChangeAt(var1, var2 - 1, var3 - 1, var4);
         }

         if (var1.isSolidTile(var2 + 1, var3, var4)) {
            this.checkCornerChangeAt(var1, var2 + 1, var3 + 1, var4);
         } else {
            this.checkCornerChangeAt(var1, var2 + 1, var3 - 1, var4);
         }

         if (var1.isSolidTile(var2, var3, var4 - 1)) {
            this.checkCornerChangeAt(var1, var2, var3 + 1, var4 - 1);
         } else {
            this.checkCornerChangeAt(var1, var2, var3 - 1, var4 - 1);
         }

         if (var1.isSolidTile(var2, var3, var4 + 1)) {
            this.checkCornerChangeAt(var1, var2, var3 + 1, var4 + 1);
         } else {
            this.checkCornerChangeAt(var1, var2, var3 - 1, var4 + 1);
         }

      }
   }

   private int checkTarget(Level var1, int var2, int var3, int var4, int var5) {
      if (var1.getTile(var2, var3, var4) != this.id) {
         return var5;
      } else {
         int var6 = var1.getData(var2, var3, var4);
         return var6 > var5 ? var6 : var5;
      }
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      if (!var1.isOnline) {
         int var6 = var1.getData(var2, var3, var4);
         boolean var7 = this.mayPlace(var1, var2, var3, var4);
         if (!var7) {
            this.spawnResources(var1, var2, var3, var4, var6);
            var1.setTile(var2, var3, var4, 0);
         } else {
            this.updatePowerStrength(var1, var2, var3, var4);
         }

         super.neighborChanged(var1, var2, var3, var4, var5);
      }
   }

   public int getResource(int var1, Random var2) {
      return Item.redStone.id;
   }

   public boolean getDirectSignal(Level var1, int var2, int var3, int var4, int var5) {
      return !this.shouldSignal ? false : this.getSignal(var1, var2, var3, var4, var5);
   }

   public boolean getSignal(LevelSource var1, int var2, int var3, int var4, int var5) {
      if (!this.shouldSignal) {
         return false;
      } else if (var1.getData(var2, var3, var4) == 0) {
         return false;
      } else if (var5 == 1) {
         return true;
      } else {
         boolean var6 = shouldConnectTo(var1, var2 - 1, var3, var4) || !var1.isSolidTile(var2 - 1, var3, var4) && shouldConnectTo(var1, var2 - 1, var3 - 1, var4);
         boolean var7 = shouldConnectTo(var1, var2 + 1, var3, var4) || !var1.isSolidTile(var2 + 1, var3, var4) && shouldConnectTo(var1, var2 + 1, var3 - 1, var4);
         boolean var8 = shouldConnectTo(var1, var2, var3, var4 - 1) || !var1.isSolidTile(var2, var3, var4 - 1) && shouldConnectTo(var1, var2, var3 - 1, var4 - 1);
         boolean var9 = shouldConnectTo(var1, var2, var3, var4 + 1) || !var1.isSolidTile(var2, var3, var4 + 1) && shouldConnectTo(var1, var2, var3 - 1, var4 + 1);
         if (!var1.isSolidTile(var2, var3 + 1, var4)) {
            if (var1.isSolidTile(var2 - 1, var3, var4) && shouldConnectTo(var1, var2 - 1, var3 + 1, var4)) {
               var6 = true;
            }

            if (var1.isSolidTile(var2 + 1, var3, var4) && shouldConnectTo(var1, var2 + 1, var3 + 1, var4)) {
               var7 = true;
            }

            if (var1.isSolidTile(var2, var3, var4 - 1) && shouldConnectTo(var1, var2, var3 + 1, var4 - 1)) {
               var8 = true;
            }

            if (var1.isSolidTile(var2, var3, var4 + 1) && shouldConnectTo(var1, var2, var3 + 1, var4 + 1)) {
               var9 = true;
            }
         }

         if (!var8 && !var7 && !var6 && !var9 && var5 >= 2 && var5 <= 5) {
            return true;
         } else if (var5 == 2 && var8 && !var6 && !var7) {
            return true;
         } else if (var5 == 3 && var9 && !var6 && !var7) {
            return true;
         } else if (var5 == 4 && var6 && !var8 && !var9) {
            return true;
         } else {
            return var5 == 5 && var7 && !var8 && !var9;
         }
      }
   }

   public boolean isSignalSource() {
      return this.shouldSignal;
   }

   public void animateTick(Level var1, int var2, int var3, int var4, Random var5) {
      if (var1.getData(var2, var3, var4) > 0) {
         double var6 = (double)var2 + (double)0.5F + ((double)var5.nextFloat() - (double)0.5F) * 0.2;
         double var8 = (double)((float)var3 + 0.0625F);
         double var10 = (double)var4 + (double)0.5F + ((double)var5.nextFloat() - (double)0.5F) * 0.2;
         var1.addParticle("reddust", var6, var8, var10, (double)0.0F, (double)0.0F, (double)0.0F);
      }

   }

   public static boolean shouldConnectTo(LevelSource var0, int var1, int var2, int var3) {
      int var4 = var0.getTile(var1, var2, var3);
      if (var4 == Tile.redStoneDust.id) {
         return true;
      } else if (var4 == 0) {
         return false;
      } else {
         return Tile.tiles[var4].isSignalSource();
      }
   }
}
