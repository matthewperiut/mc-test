package net.minecraft.world.inventory;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemInstance;

public class MenuBackup {
   private Map<Short, ItemInstance[]> backups = new HashMap();
   private Inventory inventory;
   private AbstractContainerMenu menu;

   public MenuBackup(Inventory var1, AbstractContainerMenu var2) {
      this.inventory = var1;
      this.menu = var2;
   }

   public void save(short var1) {
      ItemInstance[] var2 = new ItemInstance[this.menu.slots.size() + 1];
      var2[0] = ItemInstance.clone(this.inventory.getCarried());

      for(int var3 = 0; var3 < this.menu.slots.size(); ++var3) {
         var2[var3 + 1] = ItemInstance.clone(((Slot)this.menu.slots.get(var3)).getItem());
      }

      this.backups.put(var1, var2);
   }

   public void delete(short var1) {
      this.backups.remove(var1);
   }

   public void rollback(short var1) {
      ItemInstance[] var2 = (ItemInstance[])this.backups.get(var1);
      this.backups.clear();
      this.inventory.setCarried(var2[0]);

      for(int var3 = 0; var3 < this.menu.slots.size(); ++var3) {
         ((Slot)this.menu.slots.get(var3)).set(var2[var3 + 1]);
      }

   }
}
