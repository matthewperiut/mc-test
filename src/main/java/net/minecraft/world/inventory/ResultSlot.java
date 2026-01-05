package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemInstance;

public class ResultSlot extends Slot {
   private final Container craftSlots;

   public ResultSlot(Container var1, Container var2, int var3, int var4, int var5) {
      super(var2, var3, var4, var5);
      this.craftSlots = var1;
   }

   public boolean mayPlace(ItemInstance var1) {
      return false;
   }

   public void onTake() {
      for(int var1 = 0; var1 < this.craftSlots.getContainerSize(); ++var1) {
         ItemInstance var2 = this.craftSlots.getItem(var1);
         if (var2 != null) {
            this.craftSlots.removeItem(var1, 1);
            if (var2.getItem().hasCraftingRemainingItem()) {
               this.craftSlots.setItem(var1, new ItemInstance(var2.getItem().getCraftingRemainingItem()));
            }
         }
      }

   }
}
