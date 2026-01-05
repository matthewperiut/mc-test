package net.minecraft.world.level.pathfinder;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import util.IntHashMap;
import util.Mth;

public class PathFinder {
   private LevelSource level;
   private BinaryHeap openSet = new BinaryHeap();
   private IntHashMap<Node> nodes = new IntHashMap<Node>();
   private Node[] neighbors = new Node[32];

   public PathFinder(LevelSource var1) {
      this.level = var1;
   }

   public Path findPath(Entity var1, Entity var2, float var3) {
      return this.findPath(var1, var2.x, var2.bb.y0, var2.z, var3);
   }

   public Path findPath(Entity var1, int var2, int var3, int var4, float var5) {
      return this.findPath(var1, (double)((float)var2 + 0.5F), (double)((float)var3 + 0.5F), (double)((float)var4 + 0.5F), var5);
   }

   private Path findPath(Entity var1, double var2, double var4, double var6, float var8) {
      this.openSet.clear();
      this.nodes.clear();
      Node var9 = this.getNode(Mth.floor(var1.bb.x0), Mth.floor(var1.bb.y0), Mth.floor(var1.bb.z0));
      Node var10 = this.getNode(Mth.floor(var2 - (double)(var1.bbWidth / 2.0F)), Mth.floor(var4), Mth.floor(var6 - (double)(var1.bbWidth / 2.0F)));
      Node var11 = new Node(Mth.floor(var1.bbWidth + 1.0F), Mth.floor(var1.bbHeight + 1.0F), Mth.floor(var1.bbWidth + 1.0F));
      Path var12 = this.findPath(var1, var9, var10, var11, var8);
      return var12;
   }

   private Path findPath(Entity var1, Node var2, Node var3, Node var4, float var5) {
      var2.g = 0.0F;
      var2.h = var2.distanceTo(var3);
      var2.f = var2.h;
      this.openSet.clear();
      this.openSet.insert(var2);
      Node var6 = var2;

      while(!this.openSet.isEmpty()) {
         Node var7 = this.openSet.pop();
         if (var7.hash == var3.hash) {
            return this.reconstruct_path(var2, var3);
         }

         if (var7.distanceTo(var3) < var6.distanceTo(var3)) {
            var6 = var7;
         }

         var7.closed = true;
         int var8 = this.getNeighbors(var1, var7, var4, var3, var5);

         for(int var9 = 0; var9 < var8; ++var9) {
            Node var10 = this.neighbors[var9];
            float var11 = var7.g + var7.distanceTo(var10);
            if (!var10.inOpenSet() || var11 < var10.g) {
               var10.cameFrom = var7;
               var10.g = var11;
               var10.h = var10.distanceTo(var3);
               if (var10.inOpenSet()) {
                  this.openSet.changeCost(var10, var10.g + var10.h);
               } else {
                  var10.f = var10.g + var10.h;
                  this.openSet.insert(var10);
               }
            }
         }
      }

      if (var6 == var2) {
         return null;
      } else {
         return this.reconstruct_path(var2, var6);
      }
   }

   private int getNeighbors(Entity var1, Node var2, Node var3, Node var4, float var5) {
      int var6 = 0;
      byte var7 = 0;
      if (this.isFree(var1, var2.x, var2.y + 1, var2.z, var3) > 0) {
         var7 = 1;
      }

      Node var8 = this.getNode(var1, var2.x, var2.y, var2.z + 1, var3, var7);
      Node var9 = this.getNode(var1, var2.x - 1, var2.y, var2.z, var3, var7);
      Node var10 = this.getNode(var1, var2.x + 1, var2.y, var2.z, var3, var7);
      Node var11 = this.getNode(var1, var2.x, var2.y, var2.z - 1, var3, var7);
      if (var8 != null && !var8.closed && var8.distanceTo(var4) < var5) {
         this.neighbors[var6++] = var8;
      }

      if (var9 != null && !var9.closed && var9.distanceTo(var4) < var5) {
         this.neighbors[var6++] = var9;
      }

      if (var10 != null && !var10.closed && var10.distanceTo(var4) < var5) {
         this.neighbors[var6++] = var10;
      }

      if (var11 != null && !var11.closed && var11.distanceTo(var4) < var5) {
         this.neighbors[var6++] = var11;
      }

      return var6;
   }

   private Node getNode(Entity var1, int var2, int var3, int var4, Node var5, int var6) {
      Node var7 = null;
      if (this.isFree(var1, var2, var3, var4, var5) > 0) {
         var7 = this.getNode(var2, var3, var4);
      }

      if (var7 == null && this.isFree(var1, var2, var3 + var6, var4, var5) > 0) {
         var7 = this.getNode(var2, var3 + var6, var4);
         var3 += var6;
      }

      if (var7 != null) {
         int var8 = 0;

         int var10;
         for(var10 = 0; var3 > 0 && (var10 = this.isFree(var1, var2, var3 - 1, var4, var5)) > 0; --var3) {
            if (var10 < 0) {
               return null;
            }

            ++var8;
            if (var8 >= 4) {
               return null;
            }
         }

         if (var3 > 0) {
            var7 = this.getNode(var2, var3, var4);
         }
      }

      return var7;
   }

   private final Node getNode(int var1, int var2, int var3) {
      int var4 = var1 | var2 << 10 | var3 << 20;
      Node var5 = this.nodes.get(var4);
      if (var5 == null) {
         var5 = new Node(var1, var2, var3);
         this.nodes.put(var4, var5);
      }

      return var5;
   }

   private int isFree(Entity var1, int var2, int var3, int var4, Node var5) {
      for(int var6 = var2; var6 < var2 + var5.x; ++var6) {
         for(int var7 = var3; var7 < var3 + var5.y; ++var7) {
            for(int var8 = var4; var8 < var4 + var5.z; ++var8) {
               Material var9 = this.level.getMaterial(var2, var3, var4);
               if (var9.blocksMotion()) {
                  return 0;
               }

               if (var9 == Material.water || var9 == Material.lava) {
                  return -1;
               }
            }
         }
      }

      return 1;
   }

   private Path reconstruct_path(Node var1, Node var2) {
      int var3 = 1;

      for(Node var4 = var2; var4.cameFrom != null; var4 = var4.cameFrom) {
         ++var3;
      }

      Node[] var5 = new Node[var3];
      Node var7 = var2;
      --var3;

      for(var5[var3] = var2; var7.cameFrom != null; var5[var3] = var7) {
         var7 = var7.cameFrom;
         --var3;
      }

      return new Path(var5);
   }
}
