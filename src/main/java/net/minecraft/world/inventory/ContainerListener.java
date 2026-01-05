package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.world.item.ItemInstance;

public interface ContainerListener {
   void refreshContainer(AbstractContainerMenu var1, List<ItemInstance> var2);

   void slotChanged(AbstractContainerMenu var1, int var2, ItemInstance var3);

   void setContainerData(AbstractContainerMenu var1, int var2, int var3);
}
