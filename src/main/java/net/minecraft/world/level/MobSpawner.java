package net.minecraft.world.level;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Material;
import util.Mth;

public final class MobSpawner {
   private static final int MIN_SPAWN_DISTANCE = 24;
   private static Set<ChunkPos> chunksToPoll = new HashSet();

   public MobSpawner() {
   }

   protected static TilePos getRandomPosWithin(Level var0, int var1, int var2) {
      int var3 = var1 + var0.random.nextInt(16);
      int var4 = var0.random.nextInt(128);
      int var5 = var2 + var0.random.nextInt(16);
      return new TilePos(var3, var4, var5);
   }

   public static final int tick(Level var0, boolean var1, boolean var2) {
      if (!var1 && !var2) {
         return 0;
      } else {
         chunksToPoll.clear();

         for(int var3 = 0; var3 < var0.players.size(); ++var3) {
            Player var4 = (Player)var0.players.get(var3);
            int var5 = Mth.floor(var4.x / (double)16.0F);
            int var6 = Mth.floor(var4.z / (double)16.0F);
            byte var7 = 8;

            for(int var8 = -var7; var8 <= var7; ++var8) {
               for(int var9 = -var7; var9 <= var7; ++var9) {
                  chunksToPoll.add(new ChunkPos(var8 + var5, var9 + var6));
               }
            }
         }

         int var32 = 0;

         for(MobCategory var36 : MobCategory.values()) {
            if ((!var36.isFriendly() || var2) && (var36.isFriendly() || var1) && var0.countInstanceOf(var36.getBaseClass()) <= var36.getMaxInstancesPerChunk() * chunksToPoll.size() / 256) {
               label109:
               for(ChunkPos var38 : chunksToPoll) {
                  Biome var10 = var0.getBiomeSource().getBiome(var38);
                  Class[] var11 = var10.getMobs(var36);
                  if (var11 != null && var11.length != 0) {
                     int var12 = var0.random.nextInt(var11.length);
                     TilePos var13 = getRandomPosWithin(var0, var38.x * 16, var38.z * 16);
                     int var14 = var13.x;
                     int var15 = var13.y;
                     int var16 = var13.z;
                     if (!var0.isSolidTile(var14, var15, var16) && var0.getMaterial(var14, var15, var16) == var36.getSpawnPositionMaterial()) {
                        int var17 = 0;

                        for(int var18 = 0; var18 < 3; ++var18) {
                           int var19 = var14;
                           int var20 = var15;
                           int var21 = var16;
                           byte var22 = 6;

                           for(int var23 = 0; var23 < 4; ++var23) {
                              var19 += var0.random.nextInt(var22) - var0.random.nextInt(var22);
                              var20 += var0.random.nextInt(1) - var0.random.nextInt(1);
                              var21 += var0.random.nextInt(var22) - var0.random.nextInt(var22);
                              if (isSpawnPositionOk(var36, var0, var19, var20, var21)) {
                                 float var24 = (float)var19 + 0.5F;
                                 float var25 = (float)var20;
                                 float var26 = (float)var21 + 0.5F;
                                 if (var0.getNearestPlayer((double)var24, (double)var25, (double)var26, (double)24.0F) == null) {
                                    float var27 = var24 - (float)var0.xSpawn;
                                    float var28 = var25 - (float)var0.ySpawn;
                                    float var29 = var26 - (float)var0.zSpawn;
                                    float var30 = var27 * var27 + var28 * var28 + var29 * var29;
                                    if (!(var30 < 576.0F)) {
                                       Mob var39;
                                       try {
                                          var39 = (Mob)var11[var12].getConstructor(Level.class).newInstance(var0);
                                       } catch (Exception var31) {
                                          var31.printStackTrace();
                                          return var32;
                                       }

                                       var39.moveTo((double)var24, (double)var25, (double)var26, var0.random.nextFloat() * 360.0F, 0.0F);
                                       if (var39.canSpawn()) {
                                          ++var17;
                                          var0.addEntity(var39);
                                          finalizeMobSettings(var39, var0, var24, var25, var26);
                                          if (var17 >= var39.getMaxSpawnClusterSize()) {
                                             continue label109;
                                          }
                                       }

                                       var32 += var17;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return var32;
      }
   }

   private static boolean isSpawnPositionOk(MobCategory var0, Level var1, int var2, int var3, int var4) {
      if (var0.getSpawnPositionMaterial() == Material.water) {
         return var1.getMaterial(var2, var3, var4).isLiquid() && !var1.isSolidTile(var2, var3 + 1, var4);
      } else {
         return var1.isSolidTile(var2, var3 - 1, var4) && !var1.isSolidTile(var2, var3, var4) && !var1.getMaterial(var2, var3, var4).isLiquid() && !var1.isSolidTile(var2, var3 + 1, var4);
      }
   }

   private static void finalizeMobSettings(Mob var0, Level var1, float var2, float var3, float var4) {
      if (var0 instanceof Spider && var1.random.nextInt(100) == 0) {
         Skeleton var5 = new Skeleton(var1);
         var5.moveTo((double)var2, (double)var3, (double)var4, var0.yRot, 0.0F);
         var1.addEntity(var5);
         var5.ride(var0);
      } else if (var0 instanceof Sheep) {
         ((Sheep)var0).setColor(Sheep.getSheepColor(var1.random));
      }

   }
}
