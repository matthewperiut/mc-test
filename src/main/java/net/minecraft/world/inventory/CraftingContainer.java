package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;

public class CraftingContainer implements Container {
   private ItemInstance[] items;
   private int width;
   private AbstractContainerMenu menu;

   public CraftingContainer(AbstractContainerMenu var1, int var2, int var3) {
      int var4 = var2 * var3;
      this.items = new ItemInstance[var4];
      this.menu = var1;
      this.width = var2;
   }

   public int getContainerSize() {
      return this.items.length;
   }

   public ItemInstance getItem(int var1) {
      return var1 >= this.getContainerSize() ? null : this.items[var1];
   }

   public ItemInstance getItem(int var1, int var2) {
      if (var1 >= 0 && var1 < this.width) {
         int var3 = var1 + var2 * this.width;
         return this.getItem(var3);
      } else {
         return null;
      }
   }

   public String getName() {
      return "Crafting";
   }

   public ItemInstance removeItem(int var1, int var2) {
      if (this.items[var1] != null) {
         if (this.items[var1].count <= var2) {
            ItemInstance var4 = this.items[var1];
            this.items[var1] = null;
            this.menu.slotsChanged(this);
            return var4;
         } else {
            ItemInstance var3 = this.items[var1].remove(var2);
            if (this.items[var1].count == 0) {
               this.items[var1] = null;
            }

            this.menu.slotsChanged(this);
            return var3;
         }
      } else {
         return null;
      }
   }

   public void setItem(int var1, ItemInstance var2) {
      this.items[var1] = var2;
      this.menu.slotsChanged(this);
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
