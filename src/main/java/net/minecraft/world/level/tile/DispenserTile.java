package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.entity.DispenserTileEntity;
import net.minecraft.world.level.tile.entity.TileEntity;
import util.Mth;

public class DispenserTile extends EntityTile {
   protected DispenserTile(int var1) {
      super(var1, Material.stone);
      this.tex = 45;
   }

   public int getTickDelay() {
      return 4;
   }

   public int getResource(int var1, Random var2) {
      return Tile.dispenser.id;
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      super.onPlace(var1, var2, var3, var4);
      this.recalcLockDir(var1, var2, var3, var4);
   }

   private void recalcLockDir(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getTile(var2, var3, var4 - 1);
      int var6 = var1.getTile(var2, var3, var4 + 1);
      int var7 = var1.getTile(var2 - 1, var3, var4);
      int var8 = var1.getTile(var2 + 1, var3, var4);
      byte var9 = 3;
      if (Tile.solid[var5] && !Tile.solid[var6]) {
         var9 = 3;
      }

      if (Tile.solid[var6] && !Tile.solid[var5]) {
         var9 = 2;
      }

      if (Tile.solid[var7] && !Tile.solid[var8]) {
         var9 = 5;
      }

      if (Tile.solid[var8] && !Tile.solid[var7]) {
         var9 = 4;
      }

      var1.setData(var2, var3, var4, var9);
   }

   public int getTexture(LevelSource var1, int var2, int var3, int var4, int var5) {
      if (var5 == 1) {
         return this.tex + 17;
      } else if (var5 == 0) {
         return this.tex + 17;
      } else {
         int var6 = var1.getData(var2, var3, var4);
         return var5 != var6 ? this.tex : this.tex + 1;
      }
   }

   public int getTexture(int var1) {
      if (var1 == 1) {
         return this.tex + 17;
      } else if (var1 == 0) {
         return this.tex + 17;
      } else {
         return var1 == 3 ? this.tex + 1 : this.tex;
      }
   }

   public boolean use(Level var1, int var2, int var3, int var4, Player var5) {
      if (var1.isOnline) {
         return true;
      } else {
         DispenserTileEntity var6 = (DispenserTileEntity)var1.getTileEntity(var2, var3, var4);
         var5.openTrap(var6);
         return true;
      }
   }

   private void fireArrow(Level var1, int var2, int var3, int var4, Random var5) {
      int var6 = var1.getData(var2, var3, var4);
      float var9 = 0.0F;
      float var10 = 0.0F;
      if (var6 == 3) {
         var10 = 1.0F;
      } else if (var6 == 2) {
         var10 = -1.0F;
      } else if (var6 == 5) {
         var9 = 1.0F;
      } else {
         var9 = -1.0F;
      }

      DispenserTileEntity var11 = (DispenserTileEntity)var1.getTileEntity(var2, var3, var4);
      ItemInstance var12 = var11.removeRandomItem();
      double var13 = (double)var2 + (double)var9 * (double)0.5F + (double)0.5F;
      double var15 = (double)var3 + (double)0.5F;
      double var17 = (double)var4 + (double)var10 * (double)0.5F + (double)0.5F;
      if (var12 == null) {
         var1.playSound((double)var2, (double)var3, (double)var4, "random.click", 1.0F, 1.2F);
      } else {
         if (var12.id == Item.arrow.id) {
            Arrow var19 = new Arrow(var1, var13, var15, var17);
            var19.shoot((double)var9, (double)0.1F, (double)var10, 1.1F, 6.0F);
            var1.addEntity(var19);
            var1.playSound((double)var2, (double)var3, (double)var4, "random.bow", 1.0F, 1.2F);
         } else if (var12.id == Item.egg.id) {
            ThrownEgg var34 = new ThrownEgg(var1, var13, var15, var17);
            var34.shoot((double)var9, (double)0.1F, (double)var10, 1.1F, 6.0F);
            var1.addEntity(var34);
            var1.playSound((double)var2, (double)var3, (double)var4, "random.bow", 1.0F, 1.2F);
         } else if (var12.id == Item.snowBall.id) {
            Snowball var35 = new Snowball(var1, var13, var15, var17);
            var35.shoot((double)var9, (double)0.1F, (double)var10, 1.1F, 6.0F);
            var1.addEntity(var35);
            var1.playSound((double)var2, (double)var3, (double)var4, "random.bow", 1.0F, 1.2F);
         } else {
            ItemEntity var36 = new ItemEntity(var1, var13, var15 - 0.3, var17, var12);
            double var20 = var5.nextDouble() * 0.1 + 0.2;
            var36.xd = (double)var9 * var20;
            var36.yd = (double)0.2F;
            var36.zd = (double)var10 * var20;
            var36.xd += var5.nextGaussian() * (double)0.0075F * (double)6.0F;
            var36.yd += var5.nextGaussian() * (double)0.0075F * (double)6.0F;
            var36.zd += var5.nextGaussian() * (double)0.0075F * (double)6.0F;
            var1.addEntity(var36);
            var1.playSound((double)var2, (double)var3, (double)var4, "random.click", 1.0F, 1.0F);
         }

         for(int var37 = 0; var37 < 10; ++var37) {
            double var38 = var5.nextDouble() * 0.2 + 0.01;
            double var22 = var13 + (double)var9 * 0.01 + (var5.nextDouble() - (double)0.5F) * (double)var10 * (double)0.5F;
            double var24 = var15 + (var5.nextDouble() - (double)0.5F) * (double)0.5F;
            double var26 = var17 + (double)var10 * 0.01 + (var5.nextDouble() - (double)0.5F) * (double)var9 * (double)0.5F;
            double var28 = (double)var9 * var38 + var5.nextGaussian() * 0.01;
            double var30 = -0.03 + var5.nextGaussian() * 0.01;
            double var32 = (double)var10 * var38 + var5.nextGaussian() * 0.01;
            var1.addParticle("smoke", var22, var24, var26, var28, var30, var32);
         }
      }

   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      if (var5 > 0 && Tile.tiles[var5].isSignalSource()) {
         boolean var6 = var1.hasNeighborSignal(var2, var3, var4) || var1.hasNeighborSignal(var2, var3 + 1, var4);
         if (var6) {
            var1.addToTickNextTick(var2, var3, var4, this.id);
         }
      }

   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      if (var1.hasNeighborSignal(var2, var3, var4) || var1.hasNeighborSignal(var2, var3 + 1, var4)) {
         this.fireArrow(var1, var2, var3, var4, var5);
      }

   }

   protected TileEntity newTileEntity() {
      return new DispenserTileEntity();
   }

   public void setPlacedBy(Level var1, int var2, int var3, int var4, Mob var5) {
      int var6 = Mth.floor((double)(var5.yRot * 4.0F / 360.0F) + (double)0.5F) & 3;
      if (var6 == 0) {
         var1.setData(var2, var3, var4, 2);
      }

      if (var6 == 1) {
         var1.setData(var2, var3, var4, 5);
      }

      if (var6 == 2) {
         var1.setData(var2, var3, var4, 3);
      }

      if (var6 == 3) {
         var1.setData(var2, var3, var4, 4);
      }

   }
}
