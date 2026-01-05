package net.minecraft.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;

public class SimpleContainer implements Container {
   private String name;
   private int size;
   private ItemInstance[] items;
   private List<ContainerListener> listeners;

   public SimpleContainer(String var1, int var2) {
      this.name = var1;
      this.size = var2;
      this.items = new ItemInstance[var2];
   }

   public void addListener(ContainerListener var1) {
      if (this.listeners == null) {
         this.listeners = new ArrayList();
      }

      this.listeners.add(var1);
   }

   public void removeListener(ContainerListener var1) {
      this.listeners.remove(var1);
   }

   public ItemInstance getItem(int var1) {
      return this.items[var1];
   }

   public ItemInstance removeItem(int var1, int var2) {
      if (this.items[var1] != null) {
         if (this.items[var1].count <= var2) {
            ItemInstance var4 = this.items[var1];
            this.items[var1] = null;
            this.setChanged();
            return var4;
         } else {
            ItemInstance var3 = this.items[var1].remove(var2);
            if (this.items[var1].count == 0) {
               this.items[var1] = null;
            }

            this.setChanged();
            return var3;
         }
      } else {
         return null;
      }
   }

   public void setItem(int var1, ItemInstance var2) {
      this.items[var1] = var2;
      if (var2 != null && var2.count > this.getMaxStackSize()) {
         var2.count = this.getMaxStackSize();
      }

      this.setChanged();
   }

   public int getContainerSize() {
      return this.size;
   }

   public String getName() {
      return this.name;
   }

   public int getMaxStackSize() {
      return 64;
   }

   public void setChanged() {
      if (this.listeners != null) {
         for(int var1 = 0; var1 < this.listeners.size(); ++var1) {
            ((ContainerListener)this.listeners.get(var1)).containerChanged(this);
         }
      }

   }

   public boolean stillValid(Player var1) {
      return true;
   }
}
