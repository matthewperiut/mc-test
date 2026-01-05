package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.tile.entity.FurnaceTileEntity;

public class FurnaceMenu extends AbstractContainerMenu {
   private FurnaceTileEntity furnace;
   private int tc = 0;
   private int lt = 0;
   private int ld = 0;

   public FurnaceMenu(Container var1, FurnaceTileEntity var2) {
      this.furnace = var2;
      this.addSlot(new Slot(var2, 0, 56, 17));
      this.addSlot(new Slot(var2, 1, 56, 53));
      this.addSlot(new Slot(var2, 2, 116, 35));

      for(int var3 = 0; var3 < 3; ++var3) {
         for(int var4 = 0; var4 < 9; ++var4) {
            this.addSlot(new Slot(var1, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
         }
      }

      for(int var5 = 0; var5 < 9; ++var5) {
         this.addSlot(new Slot(var1, var5, 8 + var5 * 18, 142));
      }

   }

   public void addSlotListener(ContainerListener var1) {
      super.addSlotListener(var1);
      var1.setContainerData(this, 0, this.furnace.tickCount);
      var1.setContainerData(this, 1, this.furnace.litTime);
      var1.setContainerData(this, 2, this.furnace.litDuration);
   }

   public void broadcastChanges() {
      super.broadcastChanges();

      for(int var1 = 0; var1 < this.containerListeners.size(); ++var1) {
         ContainerListener var2 = (ContainerListener)this.containerListeners.get(var1);
         if (this.tc != this.furnace.tickCount) {
            var2.setContainerData(this, 0, this.furnace.tickCount);
         }

         if (this.lt != this.furnace.litTime) {
            var2.setContainerData(this, 1, this.furnace.litTime);
         }

         if (this.ld != this.furnace.litDuration) {
            var2.setContainerData(this, 2, this.furnace.litDuration);
         }
      }

      this.tc = this.furnace.tickCount;
      this.lt = this.furnace.litTime;
      this.ld = this.furnace.litDuration;
   }

   public void setData(int var1, int var2) {
      if (var1 == 0) {
         this.furnace.tickCount = var2;
      }

      if (var1 == 1) {
         this.furnace.litTime = var2;
      }

      if (var1 == 2) {
         this.furnace.litDuration = var2;
      }

   }

   public boolean stillValid(Player var1) {
      return this.furnace.stillValid(var1);
   }
}
