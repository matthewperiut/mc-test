package net.minecraft.world.level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import util.Mth;

public class Explosion {
   public boolean fire = false;
   private Random random = new Random();
   private Level level;
   public double x;
   public double y;
   public double z;
   public Entity source;
   public float r;
   public Set<TilePos> toBlow = new HashSet();

   public Explosion(Level var1, Entity var2, double var3, double var5, double var7, float var9) {
      this.level = var1;
      this.source = var2;
      this.r = var9;
      this.x = var3;
      this.y = var5;
      this.z = var7;
   }

   public void explode() {
      float var1 = this.r;
      byte var2 = 16;

      for(int var3 = 0; var3 < var2; ++var3) {
         for(int var4 = 0; var4 < var2; ++var4) {
            for(int var5 = 0; var5 < var2; ++var5) {
               if (var3 == 0 || var3 == var2 - 1 || var4 == 0 || var4 == var2 - 1 || var5 == 0 || var5 == var2 - 1) {
                  double var6 = (double)((float)var3 / ((float)var2 - 1.0F) * 2.0F - 1.0F);
                  double var8 = (double)((float)var4 / ((float)var2 - 1.0F) * 2.0F - 1.0F);
                  double var10 = (double)((float)var5 / ((float)var2 - 1.0F) * 2.0F - 1.0F);
                  double var12 = Math.sqrt(var6 * var6 + var8 * var8 + var10 * var10);
                  var6 /= var12;
                  var8 /= var12;
                  var10 /= var12;
                  float var14 = this.r * (0.7F + this.level.random.nextFloat() * 0.6F);
                  double var15 = this.x;
                  double var17 = this.y;
                  double var19 = this.z;

                  for(float var21 = 0.3F; var14 > 0.0F; var14 -= var21 * 0.75F) {
                     int var22 = Mth.floor(var15);
                     int var23 = Mth.floor(var17);
                     int var24 = Mth.floor(var19);
                     int var25 = this.level.getTile(var22, var23, var24);
                     if (var25 > 0) {
                        var14 -= (Tile.tiles[var25].getExplosionResistance(this.source) + 0.3F) * var21;
                     }

                     if (var14 > 0.0F) {
                        this.toBlow.add(new TilePos(var22, var23, var24));
                     }

                     var15 += var6 * (double)var21;
                     var17 += var8 * (double)var21;
                     var19 += var10 * (double)var21;
                  }
               }
            }
         }
      }

      this.r *= 2.0F;
      int var29 = Mth.floor(this.x - (double)this.r - (double)1.0F);
      int var30 = Mth.floor(this.x + (double)this.r + (double)1.0F);
      int var31 = Mth.floor(this.y - (double)this.r - (double)1.0F);
      int var33 = Mth.floor(this.y + (double)this.r + (double)1.0F);
      int var7 = Mth.floor(this.z - (double)this.r - (double)1.0F);
      int var35 = Mth.floor(this.z + (double)this.r + (double)1.0F);
      List var9 = this.level.getEntities(this.source, AABB.newTemp((double)var29, (double)var31, (double)var7, (double)var30, (double)var33, (double)var35));
      Vec3 var37 = Vec3.newTemp(this.x, this.y, this.z);

      for(int var11 = 0; var11 < var9.size(); ++var11) {
         Entity var39 = (Entity)var9.get(var11);
         double var13 = var39.distanceTo(this.x, this.y, this.z) / (double)this.r;
         if (var13 <= (double)1.0F) {
            double var43 = var39.x - this.x;
            double var46 = var39.y - this.y;
            double var49 = var39.z - this.z;
            double var51 = (double)Mth.sqrt(var43 * var43 + var46 * var46 + var49 * var49);
            var43 /= var51;
            var46 /= var51;
            var49 /= var51;
            double var52 = (double)this.level.getSeenPercent(var37, var39.bb);
            double var53 = ((double)1.0F - var13) * var52;
            var39.hurt(this.source, (int)((var53 * var53 + var53) / (double)2.0F * (double)8.0F * (double)this.r + (double)1.0F));
            var39.xd += var43 * var53;
            var39.yd += var46 * var53;
            var39.zd += var49 * var53;
         }
      }

      this.r = var1;
      ArrayList var38 = new ArrayList();
      var38.addAll(this.toBlow);
      if (this.fire) {
         for(int var40 = var38.size() - 1; var40 >= 0; --var40) {
            TilePos var41 = (TilePos)var38.get(var40);
            int var42 = var41.x;
            int var45 = var41.y;
            int var16 = var41.z;
            int var48 = this.level.getTile(var42, var45, var16);
            int var18 = this.level.getTile(var42, var45 - 1, var16);
            if (var48 == 0 && Tile.solid[var18] && this.random.nextInt(3) == 0) {
               this.level.setTile(var42, var45, var16, Tile.fire.id);
            }
         }
      }

   }

   public void addParticles() {
      this.level.playSound(this.x, this.y, this.z, "random.explode", 4.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F);
      ArrayList var1 = new ArrayList();
      var1.addAll(this.toBlow);

      for(int var2 = var1.size() - 1; var2 >= 0; --var2) {
         TilePos var3 = (TilePos)var1.get(var2);
         int var4 = var3.x;
         int var5 = var3.y;
         int var6 = var3.z;
         int var7 = this.level.getTile(var4, var5, var6);

         for(int var8 = 0; var8 < 1; ++var8) {
            double var9 = (double)((float)var4 + this.level.random.nextFloat());
            double var11 = (double)((float)var5 + this.level.random.nextFloat());
            double var13 = (double)((float)var6 + this.level.random.nextFloat());
            double var15 = var9 - this.x;
            double var17 = var11 - this.y;
            double var19 = var13 - this.z;
            double var21 = (double)Mth.sqrt(var15 * var15 + var17 * var17 + var19 * var19);
            var15 /= var21;
            var17 /= var21;
            var19 /= var21;
            double var23 = (double)0.5F / (var21 / (double)this.r + 0.1);
            var23 *= (double)(this.level.random.nextFloat() * this.level.random.nextFloat() + 0.3F);
            var15 *= var23;
            var17 *= var23;
            var19 *= var23;
            this.level.addParticle("explode", (var9 + this.x * (double)1.0F) / (double)2.0F, (var11 + this.y * (double)1.0F) / (double)2.0F, (var13 + this.z * (double)1.0F) / (double)2.0F, var15, var17, var19);
            this.level.addParticle("smoke", var9, var11, var13, var15, var17, var19);
         }

         if (var7 > 0) {
            Tile.tiles[var7].spawnResources(this.level, var4, var5, var6, this.level.getData(var4, var5, var6), 0.3F);
            this.level.setTile(var4, var5, var6, 0);
            Tile.tiles[var7].wasExploded(this.level, var4, var5, var6);
         }
      }

   }
}
