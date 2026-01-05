package net.minecraft.client.gamemode.secret;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;

public class BuilderInventory {
   public ItemInstance item;

   public BuilderInventory() {
      this.item = new ItemInstance(Item.pickAxe_iron);
   }

   public ItemInstance getSelected() {
      return this.item;
   }
}
