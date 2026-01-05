package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class FireTile extends Tile {
   public static final int FLAME_INSTANT = 60;
   public static final int FLAME_EASY = 30;
   public static final int FLAME_MEDIUM = 15;
   public static final int FLAME_HARD = 5;
   public static final int BURN_INSTANT = 100;
   public static final int BURN_EASY = 60;
   public static final int BURN_MEDIUM = 20;
   public static final int BURN_HARD = 5;
   public static final int BURN_NEVER = 0;
   private int[] flameOdds = new int[256];
   private int[] burnOdds = new int[256];

   protected FireTile(int var1, int var2) {
      super(var1, var2, Material.fire);
      this.setFlammable(Tile.wood.id, 5, 20);
      this.setFlammable(Tile.treeTrunk.id, 5, 5);
      this.setFlammable(Tile.leaves.id, 30, 60);
      this.setFlammable(Tile.bookshelf.id, 30, 20);
      this.setFlammable(Tile.tnt.id, 15, 100);
      this.setFlammable(Tile.cloth.id, 30, 60);
      this.setTicking(true);
   }

   private void setFlammable(int var1, int var2, int var3) {
      this.flameOdds[var1] = var2;
      this.burnOdds[var1] = var3;
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
      return 3;
   }

   public int getResourceCount(Random var1) {
      return 0;
   }

   public int getTickDelay() {
      return 10;
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      boolean var6 = var1.getTile(var2, var3 - 1, var4) == Tile.hellRock.id;
      int var7 = var1.getData(var2, var3, var4);
      if (var7 < 15) {
         var1.setData(var2, var3, var4, var7 + 1);
         var1.addToTickNextTick(var2, var3, var4, this.id);
      }

      if (!var6 && !this.isValidFireLocation(var1, var2, var3, var4)) {
         if (!var1.isSolidTile(var2, var3 - 1, var4) || var7 > 3) {
            var1.setTile(var2, var3, var4, 0);
         }

      } else if (!var6 && !this.canBurn(var1, var2, var3 - 1, var4) && var7 == 15 && var5.nextInt(4) == 0) {
         var1.setTile(var2, var3, var4, 0);
      } else {
         if (var7 % 2 == 0 && var7 > 2) {
            this.checkBurn(var1, var2 + 1, var3, var4, 300, var5);
            this.checkBurn(var1, var2 - 1, var3, var4, 300, var5);
            this.checkBurn(var1, var2, var3 - 1, var4, 250, var5);
            this.checkBurn(var1, var2, var3 + 1, var4, 250, var5);
            this.checkBurn(var1, var2, var3, var4 - 1, 300, var5);
            this.checkBurn(var1, var2, var3, var4 + 1, 300, var5);

            for(int var8 = var2 - 1; var8 <= var2 + 1; ++var8) {
               for(int var9 = var4 - 1; var9 <= var4 + 1; ++var9) {
                  for(int var10 = var3 - 1; var10 <= var3 + 4; ++var10) {
                     if (var8 != var2 || var10 != var3 || var9 != var4) {
                        int var11 = 100;
                        if (var10 > var3 + 1) {
                           var11 += (var10 - (var3 + 1)) * 100;
                        }

                        int var12 = this.getFireOdds(var1, var8, var10, var9);
                        if (var12 > 0 && var5.nextInt(var11) <= var12) {
                           var1.setTile(var8, var10, var9, this.id);
                        }
                     }
                  }
               }
            }
         }

      }
   }

   private void checkBurn(Level var1, int var2, int var3, int var4, int var5, Random var6) {
      int var7 = this.burnOdds[var1.getTile(var2, var3, var4)];
      if (var6.nextInt(var5) < var7) {
         boolean var8 = var1.getTile(var2, var3, var4) == Tile.tnt.id;
         if (var6.nextInt(2) == 0) {
            var1.setTile(var2, var3, var4, this.id);
         } else {
            var1.setTile(var2, var3, var4, 0);
         }

         if (var8) {
            Tile.tnt.destroy(var1, var2, var3, var4, 0);
         }
      }

   }

   private boolean isValidFireLocation(Level var1, int var2, int var3, int var4) {
      if (this.canBurn(var1, var2 + 1, var3, var4)) {
         return true;
      } else if (this.canBurn(var1, var2 - 1, var3, var4)) {
         return true;
      } else if (this.canBurn(var1, var2, var3 - 1, var4)) {
         return true;
      } else if (this.canBurn(var1, var2, var3 + 1, var4)) {
         return true;
      } else if (this.canBurn(var1, var2, var3, var4 - 1)) {
         return true;
      } else {
         return this.canBurn(var1, var2, var3, var4 + 1);
      }
   }

   private int getFireOdds(Level var1, int var2, int var3, int var4) {
      int var5 = 0;
      if (!var1.isEmptyTile(var2, var3, var4)) {
         return 0;
      } else {
         var5 = this.getFlammability(var1, var2 + 1, var3, var4, var5);
         var5 = this.getFlammability(var1, var2 - 1, var3, var4, var5);
         var5 = this.getFlammability(var1, var2, var3 - 1, var4, var5);
         var5 = this.getFlammability(var1, var2, var3 + 1, var4, var5);
         var5 = this.getFlammability(var1, var2, var3, var4 - 1, var5);
         var5 = this.getFlammability(var1, var2, var3, var4 + 1, var5);
         return var5;
      }
   }

   public boolean mayPick() {
      return false;
   }

   public boolean canBurn(LevelSource var1, int var2, int var3, int var4) {
      return this.flameOdds[var1.getTile(var2, var3, var4)] > 0;
   }

   public int getFlammability(Level var1, int var2, int var3, int var4, int var5) {
      int var6 = this.flameOdds[var1.getTile(var2, var3, var4)];
      return var6 > var5 ? var6 : var5;
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      return var1.isSolidTile(var2, var3 - 1, var4) || this.isValidFireLocation(var1, var2, var3, var4);
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      if (!var1.isSolidTile(var2, var3 - 1, var4) && !this.isValidFireLocation(var1, var2, var3, var4)) {
         var1.setTile(var2, var3, var4, 0);
      }
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      if (var1.getTile(var2, var3 - 1, var4) != Tile.obsidian.id || !Tile.portalTile.trySpawnPortal(var1, var2, var3, var4)) {
         if (!var1.isSolidTile(var2, var3 - 1, var4) && !this.isValidFireLocation(var1, var2, var3, var4)) {
            var1.setTile(var2, var3, var4, 0);
         } else {
            var1.addToTickNextTick(var2, var3, var4, this.id);
         }
      }
   }

   public boolean isFlammable(int var1) {
      return this.flameOdds[var1] > 0;
   }

   public void ignite(Level var1, int var2, int var3, int var4) {
      boolean var5 = false;
      if (!var5) {
         var5 = this.tryIgnite(var1, var2, var3 + 1, var4);
      }

      if (!var5) {
         var5 = this.tryIgnite(var1, var2 - 1, var3, var4);
      }

      if (!var5) {
         var5 = this.tryIgnite(var1, var2 + 1, var3, var4);
      }

      if (!var5) {
         var5 = this.tryIgnite(var1, var2, var3, var4 - 1);
      }

      if (!var5) {
         var5 = this.tryIgnite(var1, var2, var3, var4 + 1);
      }

      if (!var5) {
         var5 = this.tryIgnite(var1, var2, var3 - 1, var4);
      }

      if (!var5) {
         var1.setTile(var2, var3, var4, Tile.fire.id);
      }

   }

   public void animateTick(Level var1, int var2, int var3, int var4, Random var5) {
      if (var5.nextInt(24) == 0) {
         var1.playSound((double)((float)var2 + 0.5F), (double)((float)var3 + 0.5F), (double)((float)var4 + 0.5F), "fire.fire", 1.0F + var5.nextFloat(), var5.nextFloat() * 0.7F + 0.3F);
      }

      if (!var1.isSolidTile(var2, var3 - 1, var4) && !Tile.fire.canBurn(var1, var2, var3 - 1, var4)) {
         if (Tile.fire.canBurn(var1, var2 - 1, var3, var4)) {
            for(int var10 = 0; var10 < 2; ++var10) {
               float var15 = (float)var2 + var5.nextFloat() * 0.1F;
               float var20 = (float)var3 + var5.nextFloat();
               float var25 = (float)var4 + var5.nextFloat();
               var1.addParticle("largesmoke", (double)var15, (double)var20, (double)var25, (double)0.0F, (double)0.0F, (double)0.0F);
            }
         }

         if (Tile.fire.canBurn(var1, var2 + 1, var3, var4)) {
            for(int var11 = 0; var11 < 2; ++var11) {
               float var16 = (float)(var2 + 1) - var5.nextFloat() * 0.1F;
               float var21 = (float)var3 + var5.nextFloat();
               float var26 = (float)var4 + var5.nextFloat();
               var1.addParticle("largesmoke", (double)var16, (double)var21, (double)var26, (double)0.0F, (double)0.0F, (double)0.0F);
            }
         }

         if (Tile.fire.canBurn(var1, var2, var3, var4 - 1)) {
            for(int var12 = 0; var12 < 2; ++var12) {
               float var17 = (float)var2 + var5.nextFloat();
               float var22 = (float)var3 + var5.nextFloat();
               float var27 = (float)var4 + var5.nextFloat() * 0.1F;
               var1.addParticle("largesmoke", (double)var17, (double)var22, (double)var27, (double)0.0F, (double)0.0F, (double)0.0F);
            }
         }

         if (Tile.fire.canBurn(var1, var2, var3, var4 + 1)) {
            for(int var13 = 0; var13 < 2; ++var13) {
               float var18 = (float)var2 + var5.nextFloat();
               float var23 = (float)var3 + var5.nextFloat();
               float var28 = (float)(var4 + 1) - var5.nextFloat() * 0.1F;
               var1.addParticle("largesmoke", (double)var18, (double)var23, (double)var28, (double)0.0F, (double)0.0F, (double)0.0F);
            }
         }

         if (Tile.fire.canBurn(var1, var2, var3 + 1, var4)) {
            for(int var14 = 0; var14 < 2; ++var14) {
               float var19 = (float)var2 + var5.nextFloat();
               float var24 = (float)(var3 + 1) - var5.nextFloat() * 0.1F;
               float var29 = (float)var4 + var5.nextFloat();
               var1.addParticle("largesmoke", (double)var19, (double)var24, (double)var29, (double)0.0F, (double)0.0F, (double)0.0F);
            }
         }
      } else {
         for(int var6 = 0; var6 < 3; ++var6) {
            float var7 = (float)var2 + var5.nextFloat();
            float var8 = (float)var3 + var5.nextFloat() * 0.5F + 0.5F;
            float var9 = (float)var4 + var5.nextFloat();
            var1.addParticle("largesmoke", (double)var7, (double)var8, (double)var9, (double)0.0F, (double)0.0F, (double)0.0F);
         }
      }

   }

   private boolean tryIgnite(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getTile(var2, var3, var4);
      if (var5 == Tile.fire.id) {
         return true;
      } else if (var5 == 0) {
         var1.setTile(var2, var3, var4, Tile.fire.id);
         return true;
      } else {
         return false;
      }
   }
}
