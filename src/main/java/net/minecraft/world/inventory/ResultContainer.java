package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;

public class ResultContainer implements Container {
   private ItemInstance[] items = new ItemInstance[1];

   public ResultContainer() {
   }

   public int getContainerSize() {
      return 1;
   }

   public ItemInstance getItem(int var1) {
      return this.items[var1];
   }

   public String getName() {
      return "Result";
   }

   public ItemInstance removeItem(int var1, int var2) {
      if (this.items[var1] != null) {
         ItemInstance var3 = this.items[var1];
         this.items[var1] = null;
         return var3;
      } else {
         return null;
      }
   }

   public void setItem(int var1, ItemInstance var2) {
      this.items[var1] = var2;
   }

   public int getMaxStackSize() {
      return 64;
   }

   public void setChanged() {
   }

   public boolean stillValid(Player var1) {
      return true;
   }
}
