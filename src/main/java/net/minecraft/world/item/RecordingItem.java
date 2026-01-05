package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class RecordingItem extends Item {
   private String recording;

   protected RecordingItem(int var1, String var2) {
      super(var1);
      this.recording = var2;
      this.maxStackSize = 1;
   }

   public boolean useOn(ItemInstance var1, Player var2, Level var3, int var4, int var5, int var6, int var7) {
      if (var3.getTile(var4, var5, var6) == Tile.recordPlayer.id && var3.getData(var4, var5, var6) == 0) {
         var3.setData(var4, var5, var6, this.id - Item.record_01.id + 1);
         var3.playStreamingMusic(this.recording, var4, var5, var6);
         --var1.count;
         return true;
      } else {
         return false;
      }
   }
}
