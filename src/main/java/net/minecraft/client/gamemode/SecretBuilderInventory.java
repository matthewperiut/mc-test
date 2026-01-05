package net.minecraft.client.gamemode;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;

class SecretBuilderInventory {
   public ItemInstance item;

   SecretBuilderInventory() {
      this.item = new ItemInstance(Item.pickAxe_gold);
   }

   public ItemInstance getSelected() {
      return this.item;
   }
}
