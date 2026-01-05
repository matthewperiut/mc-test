package net.minecraft.world.level.tile;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.entity.MusicTileEntity;
import net.minecraft.world.level.tile.entity.TileEntity;

public class MusicTile extends EntityTile {
   public MusicTile(int var1) {
      super(var1, 74, Material.wood);
   }

   public int getTexture(int var1) {
      return this.tex;
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      if (var5 > 0 && Tile.tiles[var5].isSignalSource()) {
         boolean var6 = var1.hasDirectSignal(var2, var3, var4);
         MusicTileEntity var7 = (MusicTileEntity)var1.getTileEntity(var2, var3, var4);
         if (var7.on != var6) {
            if (var6) {
               var7.playNote(var1, var2, var3, var4);
            }

            var7.on = var6;
         }
      }

   }

   public boolean use(Level var1, int var2, int var3, int var4, Player var5) {
      if (var1.isOnline) {
         return true;
      } else {
         MusicTileEntity var6 = (MusicTileEntity)var1.getTileEntity(var2, var3, var4);
         var6.tune();
         var6.playNote(var1, var2, var3, var4);
         return true;
      }
   }

   public void attack(Level var1, int var2, int var3, int var4, Player var5) {
      if (!var1.isOnline) {
         MusicTileEntity var6 = (MusicTileEntity)var1.getTileEntity(var2, var3, var4);
         var6.playNote(var1, var2, var3, var4);
      }
   }

   protected TileEntity newTileEntity() {
      return new MusicTileEntity();
   }

   public void triggerEvent(Level var1, int var2, int var3, int var4, int var5, int var6) {
      float var7 = (float)Math.pow((double)2.0F, (double)(var6 - 12) / (double)12.0F);
      String var8 = "harp";
      if (var5 == 1) {
         var8 = "bd";
      }

      if (var5 == 2) {
         var8 = "snare";
      }

      if (var5 == 3) {
         var8 = "hat";
      }

      if (var5 == 4) {
         var8 = "bassattack";
      }

      var1.playSound((double)var2 + (double)0.5F, (double)var3 + (double)0.5F, (double)var4 + (double)0.5F, "note." + var8, 3.0F, var7);
      var1.addParticle("note", (double)var2 + (double)0.5F, (double)var3 + 1.2, (double)var4 + (double)0.5F, (double)var6 / (double)24.0F, (double)0.0F, (double)0.0F);
   }
}
