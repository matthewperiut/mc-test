package net.minecraft.world;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;

public class CompoundContainer implements Container {
   private String name;
   private Container c1;
   private Container c2;

   public CompoundContainer(String var1, Container var2, Container var3) {
      this.name = var1;
      this.c1 = var2;
      this.c2 = var3;
   }

   public int getContainerSize() {
      return this.c1.getContainerSize() + this.c2.getContainerSize();
   }

   public String getName() {
      return this.name;
   }

   public ItemInstance getItem(int var1) {
      return var1 >= this.c1.getContainerSize() ? this.c2.getItem(var1 - this.c1.getContainerSize()) : this.c1.getItem(var1);
   }

   public ItemInstance removeItem(int var1, int var2) {
      return var1 >= this.c1.getContainerSize() ? this.c2.removeItem(var1 - this.c1.getContainerSize(), var2) : this.c1.removeItem(var1, var2);
   }

   public void setItem(int var1, ItemInstance var2) {
      if (var1 >= this.c1.getContainerSize()) {
         this.c2.setItem(var1 - this.c1.getContainerSize(), var2);
      } else {
         this.c1.setItem(var1, var2);
      }

   }

   public int getMaxStackSize() {
      return this.c1.getMaxStackSize();
   }

   public void setChanged() {
      this.c1.setChanged();
      this.c2.setChanged();
   }

   public boolean stillValid(Player var1) {
      return this.c1.stillValid(var1) && this.c2.stillValid(var1);
   }
}
