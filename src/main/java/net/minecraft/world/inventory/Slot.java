package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemInstance;

public class Slot {
   private final int slot;
   private final Container container;
   public int index;
   public int x;
   public int y;

   public Slot(Container var1, int var2, int var3, int var4) {
      this.container = var1;
      this.slot = var2;
      this.x = var3;
      this.y = var4;
   }

   public void swap(Slot var1) {
      ItemInstance var2 = this.container.getItem(this.slot);
      ItemInstance var3 = var1.container.getItem(var1.slot);
      if (var2 != null && var2.count > var1.getMaxStackSize()) {
         if (var3 != null) {
            return;
         }

         var3 = var2.remove(var2.count - var1.getMaxStackSize());
      }

      if (var3 != null && var3.count > this.getMaxStackSize()) {
         if (var2 != null) {
            return;
         }

         var2 = var3.remove(var3.count - this.getMaxStackSize());
      }

      var1.container.setItem(var1.slot, var2);
      this.container.setItem(this.slot, var3);
      this.setChanged();
   }

   public void onTake() {
      this.setChanged();
   }

   public boolean mayPlace(ItemInstance var1) {
      return true;
   }

   public ItemInstance getItem() {
      return this.container.getItem(this.slot);
   }

   public boolean hasItem() {
      return this.getItem() != null;
   }

   public void set(ItemInstance var1) {
      this.container.setItem(this.slot, var1);
      this.setChanged();
   }

   public void setChanged() {
      this.container.setChanged();
   }

   public int getMaxStackSize() {
      return this.container.getMaxStackSize();
   }

   public int getNoItemIcon() {
      return -1;
   }

   public ItemInstance remove(int var1) {
      return this.container.removeItem(this.slot, var1);
   }

   public boolean isAt(Container var1, int var2) {
      return var1 == this.container && var2 == this.slot;
   }
}
