package net.minecraft.client.gamemode.secret;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;

public class SecretBuilderInventory {
   public ItemInstance item;

   public SecretBuilderInventory() {
      this.item = new ItemInstance(Item.pickAxe_iron);
   }

   public ItemInstance getSelected() {
      return this.item;
   }
}
