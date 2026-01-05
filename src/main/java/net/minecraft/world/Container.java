package net.minecraft.world;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;

public interface Container {
   int LARGE_MAX_STACK_SIZE = 64;

   int getContainerSize();

   ItemInstance getItem(int var1);

   ItemInstance removeItem(int var1, int var2);

   void setItem(int var1, ItemInstance var2);

   String getName();

   int getMaxStackSize();

   void setChanged();

   boolean stillValid(Player var1);
}
