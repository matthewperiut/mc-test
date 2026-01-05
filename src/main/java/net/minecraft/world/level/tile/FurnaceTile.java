package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.entity.FurnaceTileEntity;
import net.minecraft.world.level.tile.entity.TileEntity;
import util.Mth;

public class FurnaceTile extends EntityTile {
   private final boolean lit;

   protected FurnaceTile(int var1, boolean var2) {
      super(var1, Material.stone);
      this.lit = var2;
      this.tex = 45;
   }

   public int getResource(int var1, Random var2) {
      return Tile.furnace.id;
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
         if (var5 != var6) {
            return this.tex;
         } else {
            return this.lit ? this.tex + 16 : this.tex - 1;
         }
      }
   }

   public void animateTick(Level var1, int var2, int var3, int var4, Random var5) {
      if (this.lit) {
         int var6 = var1.getData(var2, var3, var4);
         float var7 = (float)var2 + 0.5F;
         float var8 = (float)var3 + 0.0F + var5.nextFloat() * 6.0F / 16.0F;
         float var9 = (float)var4 + 0.5F;
         float var10 = 0.52F;
         float var11 = var5.nextFloat() * 0.6F - 0.3F;
         if (var6 == 4) {
            var1.addParticle("smoke", (double)(var7 - var10), (double)var8, (double)(var9 + var11), (double)0.0F, (double)0.0F, (double)0.0F);
            var1.addParticle("flame", (double)(var7 - var10), (double)var8, (double)(var9 + var11), (double)0.0F, (double)0.0F, (double)0.0F);
         } else if (var6 == 5) {
            var1.addParticle("smoke", (double)(var7 + var10), (double)var8, (double)(var9 + var11), (double)0.0F, (double)0.0F, (double)0.0F);
            var1.addParticle("flame", (double)(var7 + var10), (double)var8, (double)(var9 + var11), (double)0.0F, (double)0.0F, (double)0.0F);
         } else if (var6 == 2) {
            var1.addParticle("smoke", (double)(var7 + var11), (double)var8, (double)(var9 - var10), (double)0.0F, (double)0.0F, (double)0.0F);
            var1.addParticle("flame", (double)(var7 + var11), (double)var8, (double)(var9 - var10), (double)0.0F, (double)0.0F, (double)0.0F);
         } else if (var6 == 3) {
            var1.addParticle("smoke", (double)(var7 + var11), (double)var8, (double)(var9 + var10), (double)0.0F, (double)0.0F, (double)0.0F);
            var1.addParticle("flame", (double)(var7 + var11), (double)var8, (double)(var9 + var10), (double)0.0F, (double)0.0F, (double)0.0F);
         }

      }
   }

   public int getTexture(int var1) {
      if (var1 == 1) {
         return this.tex + 17;
      } else if (var1 == 0) {
         return this.tex + 17;
      } else {
         return var1 == 3 ? this.tex - 1 : this.tex;
      }
   }

   public boolean use(Level var1, int var2, int var3, int var4, Player var5) {
      if (var1.isOnline) {
         return true;
      } else {
         FurnaceTileEntity var6 = (FurnaceTileEntity)var1.getTileEntity(var2, var3, var4);
         var5.openFurnace(var6);
         return true;
      }
   }

   public static void setLit(boolean var0, Level var1, int var2, int var3, int var4) {
      int var5 = var1.getData(var2, var3, var4);
      TileEntity var6 = var1.getTileEntity(var2, var3, var4);
      if (var0) {
         var1.setTile(var2, var3, var4, Tile.furnace_lit.id);
      } else {
         var1.setTile(var2, var3, var4, Tile.furnace.id);
      }

      var1.setData(var2, var3, var4, var5);
      var1.setTileEntity(var2, var3, var4, var6);
   }

   protected TileEntity newTileEntity() {
      return new FurnaceTileEntity();
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
