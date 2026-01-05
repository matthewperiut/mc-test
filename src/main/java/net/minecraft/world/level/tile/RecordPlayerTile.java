package net.minecraft.world.level.tile;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;

public class RecordPlayerTile extends Tile {
   protected RecordPlayerTile(int var1, int var2) {
      super(var1, var2, Material.wood);
   }

   public int getTexture(int var1) {
      return this.tex + (var1 == 1 ? 1 : 0);
   }

   public boolean use(Level var1, int var2, int var3, int var4, Player var5) {
      int var6 = var1.getData(var2, var3, var4);
      if (var6 > 0) {
         this.dropRecording(var1, var2, var3, var4, var6);
         return true;
      } else {
         return false;
      }
   }

   public void dropRecording(Level var1, int var2, int var3, int var4, int var5) {
      var1.playStreamingMusic((String)null, var2, var3, var4);
      var1.setData(var2, var3, var4, 0);
      int var6 = Item.record_01.id + var5 - 1;
      float var7 = 0.7F;
      double var8 = (double)(var1.random.nextFloat() * var7) + (double)(1.0F - var7) * (double)0.5F;
      double var10 = (double)(var1.random.nextFloat() * var7) + (double)(1.0F - var7) * 0.2 + 0.6;
      double var12 = (double)(var1.random.nextFloat() * var7) + (double)(1.0F - var7) * (double)0.5F;
      ItemEntity var14 = new ItemEntity(var1, (double)var2 + var8, (double)var3 + var10, (double)var4 + var12, new ItemInstance(var6, 1, 0));
      var14.throwTime = 10;
      var1.addEntity(var14);
   }

   public void spawnResources(Level var1, int var2, int var3, int var4, int var5, float var6) {
      if (!var1.isOnline) {
         if (var5 > 0) {
            this.dropRecording(var1, var2, var3, var4, var5);
         }

         super.spawnResources(var1, var2, var3, var4, var5, var6);
      }
   }
}
