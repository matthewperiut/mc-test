package net.minecraft.world.level.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.TilePos;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RailTile extends Tile {
   protected RailTile(int var1, int var2) {
      super(var1, var2, Material.decoration);
      this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
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

   public HitResult clip(Level var1, int var2, int var3, int var4, Vec3 var5, Vec3 var6) {
      this.updateShape(var1, var2, var3, var4);
      return super.clip(var1, var2, var3, var4, var5, var6);
   }

   public void updateShape(LevelSource var1, int var2, int var3, int var4) {
      int var5 = var1.getData(var2, var3, var4);
      if (var5 >= 2 && var5 <= 5) {
         this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.625F, 1.0F);
      } else {
         this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
      }

   }

   public int getTexture(int var1, int var2) {
      return var2 >= 6 ? this.tex - 16 : this.tex;
   }

   public boolean isCubeShaped() {
      return false;
   }

   public int getRenderShape() {
      return 9;
   }

   public int getResourceCount(Random var1) {
      return 1;
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      return var1.isSolidTile(var2, var3 - 1, var4);
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      if (!var1.isOnline) {
         var1.setData(var2, var3, var4, 15);
         this.updateDir(var1, var2, var3, var4);
      }

   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      if (!var1.isOnline) {
         int var6 = var1.getData(var2, var3, var4);
         boolean var7 = false;
         if (!var1.isSolidTile(var2, var3 - 1, var4)) {
            var7 = true;
         }

         if (var6 == 2 && !var1.isSolidTile(var2 + 1, var3, var4)) {
            var7 = true;
         }

         if (var6 == 3 && !var1.isSolidTile(var2 - 1, var3, var4)) {
            var7 = true;
         }

         if (var6 == 4 && !var1.isSolidTile(var2, var3, var4 - 1)) {
            var7 = true;
         }

         if (var6 == 5 && !var1.isSolidTile(var2, var3, var4 + 1)) {
            var7 = true;
         }

         if (var7) {
            this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
            var1.setTile(var2, var3, var4, 0);
         } else if (var5 > 0 && Tile.tiles[var5].isSignalSource() && (new Rail(var1, var2, var3, var4)).countPotentialConnections() == 3) {
            this.updateDir(var1, var2, var3, var4);
         }

      }
   }

   private void updateDir(Level var1, int var2, int var3, int var4) {
      if (!var1.isOnline) {
         (new Rail(var1, var2, var3, var4)).place(var1.hasNeighborSignal(var2, var3, var4));
      }
   }

   private class Rail {
      private Level level;
      private int x;
      private int y;
      private int z;
      private int data;
      private List<TilePos> connections = new ArrayList();

      public Rail(Level var2, int var3, int var4, int var5) {
         this.level = var2;
         this.x = var3;
         this.y = var4;
         this.z = var5;
         this.data = var2.getData(var3, var4, var5);
         this.updateConnections();
      }

      private void updateConnections() {
         this.connections.clear();
         if (this.data == 0) {
            this.connections.add(new TilePos(this.x, this.y, this.z - 1));
            this.connections.add(new TilePos(this.x, this.y, this.z + 1));
         } else if (this.data == 1) {
            this.connections.add(new TilePos(this.x - 1, this.y, this.z));
            this.connections.add(new TilePos(this.x + 1, this.y, this.z));
         } else if (this.data == 2) {
            this.connections.add(new TilePos(this.x - 1, this.y, this.z));
            this.connections.add(new TilePos(this.x + 1, this.y + 1, this.z));
         } else if (this.data == 3) {
            this.connections.add(new TilePos(this.x - 1, this.y + 1, this.z));
            this.connections.add(new TilePos(this.x + 1, this.y, this.z));
         } else if (this.data == 4) {
            this.connections.add(new TilePos(this.x, this.y + 1, this.z - 1));
            this.connections.add(new TilePos(this.x, this.y, this.z + 1));
         } else if (this.data == 5) {
            this.connections.add(new TilePos(this.x, this.y, this.z - 1));
            this.connections.add(new TilePos(this.x, this.y + 1, this.z + 1));
         } else if (this.data == 6) {
            this.connections.add(new TilePos(this.x + 1, this.y, this.z));
            this.connections.add(new TilePos(this.x, this.y, this.z + 1));
         } else if (this.data == 7) {
            this.connections.add(new TilePos(this.x - 1, this.y, this.z));
            this.connections.add(new TilePos(this.x, this.y, this.z + 1));
         } else if (this.data == 8) {
            this.connections.add(new TilePos(this.x - 1, this.y, this.z));
            this.connections.add(new TilePos(this.x, this.y, this.z - 1));
         } else if (this.data == 9) {
            this.connections.add(new TilePos(this.x + 1, this.y, this.z));
            this.connections.add(new TilePos(this.x, this.y, this.z - 1));
         }

      }

      private void removeSoftConnections() {
         for(int var1 = 0; var1 < this.connections.size(); ++var1) {
            Rail var2 = this.getRail((TilePos)this.connections.get(var1));
            if (var2 != null && var2.connectsTo(this)) {
               this.connections.set(var1, new TilePos(var2.x, var2.y, var2.z));
            } else {
               this.connections.remove(var1--);
            }
         }

      }

      private boolean hasRail(int var1, int var2, int var3) {
         if (this.level.getTile(var1, var2, var3) == RailTile.this.id) {
            return true;
         } else if (this.level.getTile(var1, var2 + 1, var3) == RailTile.this.id) {
            return true;
         } else {
            return this.level.getTile(var1, var2 - 1, var3) == RailTile.this.id;
         }
      }

      private Rail getRail(TilePos var1) {
         if (this.level.getTile(var1.x, var1.y, var1.z) == RailTile.this.id) {
            return RailTile.this.new Rail(this.level, var1.x, var1.y, var1.z);
         } else if (this.level.getTile(var1.x, var1.y + 1, var1.z) == RailTile.this.id) {
            return RailTile.this.new Rail(this.level, var1.x, var1.y + 1, var1.z);
         } else {
            return this.level.getTile(var1.x, var1.y - 1, var1.z) == RailTile.this.id ? RailTile.this.new Rail(this.level, var1.x, var1.y - 1, var1.z) : null;
         }
      }

      private boolean connectsTo(Rail var1) {
         for(int var2 = 0; var2 < this.connections.size(); ++var2) {
            TilePos var3 = (TilePos)this.connections.get(var2);
            if (var3.x == var1.x && var3.z == var1.z) {
               return true;
            }
         }

         return false;
      }

      private boolean hasConnection(int var1, int var2, int var3) {
         for(int var4 = 0; var4 < this.connections.size(); ++var4) {
            TilePos var5 = (TilePos)this.connections.get(var4);
            if (var5.x == var1 && var5.z == var3) {
               return true;
            }
         }

         return false;
      }

      private int countPotentialConnections() {
         int var1 = 0;
         if (this.hasRail(this.x, this.y, this.z - 1)) {
            ++var1;
         }

         if (this.hasRail(this.x, this.y, this.z + 1)) {
            ++var1;
         }

         if (this.hasRail(this.x - 1, this.y, this.z)) {
            ++var1;
         }

         if (this.hasRail(this.x + 1, this.y, this.z)) {
            ++var1;
         }

         return var1;
      }

      private boolean canConnectTo(Rail var1) {
         if (this.connectsTo(var1)) {
            return true;
         } else if (this.connections.size() == 2) {
            return false;
         } else if (this.connections.size() == 0) {
            return true;
         } else {
            TilePos var2 = (TilePos)this.connections.get(0);
            return var1.y == this.y && var2.y == this.y ? true : true;
         }
      }

      private void connectTo(Rail var1) {
         this.connections.add(new TilePos(var1.x, var1.y, var1.z));
         boolean var2 = this.hasConnection(this.x, this.y, this.z - 1);
         boolean var3 = this.hasConnection(this.x, this.y, this.z + 1);
         boolean var4 = this.hasConnection(this.x - 1, this.y, this.z);
         boolean var5 = this.hasConnection(this.x + 1, this.y, this.z);
         byte var6 = -1;
         if (var2 || var3) {
            var6 = 0;
         }

         if (var4 || var5) {
            var6 = 1;
         }

         if (var3 && var5 && !var2 && !var4) {
            var6 = 6;
         }

         if (var3 && var4 && !var2 && !var5) {
            var6 = 7;
         }

         if (var2 && var4 && !var3 && !var5) {
            var6 = 8;
         }

         if (var2 && var5 && !var3 && !var4) {
            var6 = 9;
         }

         if (var6 == 0) {
            if (this.level.getTile(this.x, this.y + 1, this.z - 1) == RailTile.this.id) {
               var6 = 4;
            }

            if (this.level.getTile(this.x, this.y + 1, this.z + 1) == RailTile.this.id) {
               var6 = 5;
            }
         }

         if (var6 == 1) {
            if (this.level.getTile(this.x + 1, this.y + 1, this.z) == RailTile.this.id) {
               var6 = 2;
            }

            if (this.level.getTile(this.x - 1, this.y + 1, this.z) == RailTile.this.id) {
               var6 = 3;
            }
         }

         if (var6 < 0) {
            var6 = 0;
         }

         this.level.setData(this.x, this.y, this.z, var6);
      }

      private boolean hasNeighborRail(int var1, int var2, int var3) {
         Rail var4 = this.getRail(new TilePos(var1, var2, var3));
         if (var4 == null) {
            return false;
         } else {
            var4.removeSoftConnections();
            return var4.canConnectTo(this);
         }
      }

      public void place(boolean var1) {
         boolean var2 = this.hasNeighborRail(this.x, this.y, this.z - 1);
         boolean var3 = this.hasNeighborRail(this.x, this.y, this.z + 1);
         boolean var4 = this.hasNeighborRail(this.x - 1, this.y, this.z);
         boolean var5 = this.hasNeighborRail(this.x + 1, this.y, this.z);
         byte var6 = -1;
         if ((var2 || var3) && !var4 && !var5) {
            var6 = 0;
         }

         if ((var4 || var5) && !var2 && !var3) {
            var6 = 1;
         }

         if (var3 && var5 && !var2 && !var4) {
            var6 = 6;
         }

         if (var3 && var4 && !var2 && !var5) {
            var6 = 7;
         }

         if (var2 && var4 && !var3 && !var5) {
            var6 = 8;
         }

         if (var2 && var5 && !var3 && !var4) {
            var6 = 9;
         }

         if (var6 == -1) {
            if (var2 || var3) {
               var6 = 0;
            }

            if (var4 || var5) {
               var6 = 1;
            }

            if (var1) {
               if (var3 && var5) {
                  var6 = 6;
               }

               if (var4 && var3) {
                  var6 = 7;
               }

               if (var5 && var2) {
                  var6 = 9;
               }

               if (var2 && var4) {
                  var6 = 8;
               }
            } else {
               if (var2 && var4) {
                  var6 = 8;
               }

               if (var5 && var2) {
                  var6 = 9;
               }

               if (var4 && var3) {
                  var6 = 7;
               }

               if (var3 && var5) {
                  var6 = 6;
               }
            }
         }

         if (var6 == 0) {
            if (this.level.getTile(this.x, this.y + 1, this.z - 1) == RailTile.this.id) {
               var6 = 4;
            }

            if (this.level.getTile(this.x, this.y + 1, this.z + 1) == RailTile.this.id) {
               var6 = 5;
            }
         }

         if (var6 == 1) {
            if (this.level.getTile(this.x + 1, this.y + 1, this.z) == RailTile.this.id) {
               var6 = 2;
            }

            if (this.level.getTile(this.x - 1, this.y + 1, this.z) == RailTile.this.id) {
               var6 = 3;
            }
         }

         if (var6 < 0) {
            var6 = 0;
         }

         this.data = var6;
         this.updateConnections();
         this.level.setData(this.x, this.y, this.z, var6);

         for(int var7 = 0; var7 < this.connections.size(); ++var7) {
            Rail var8 = this.getRail((TilePos)this.connections.get(var7));
            if (var8 != null) {
               var8.removeSoftConnections();
               if (var8.canConnectTo(this)) {
                  var8.connectTo(this);
               }
            }
         }

      }
   }
}
