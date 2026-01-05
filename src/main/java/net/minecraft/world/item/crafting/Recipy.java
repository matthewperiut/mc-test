package net.minecraft.world.item.crafting;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemInstance;

public interface Recipy {
   boolean matches(CraftingContainer var1);

   ItemInstance assemble(CraftingContainer var1);

   int size();
}
