package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.tile.entity.DispenserTileEntity;

public class TrapMenu extends AbstractContainerMenu {
   private DispenserTileEntity trap;

   public TrapMenu(Container var1, DispenserTileEntity var2) {
      this.trap = var2;

      for(int var3 = 0; var3 < 3; ++var3) {
         for(int var4 = 0; var4 < 3; ++var4) {
            this.addSlot(new Slot(var2, var4 + var3 * 3, 61 + var4 * 18, 17 + var3 * 18));
         }
      }

      for(int var5 = 0; var5 < 3; ++var5) {
         for(int var7 = 0; var7 < 9; ++var7) {
            this.addSlot(new Slot(var1, var7 + var5 * 9 + 9, 8 + var7 * 18, 84 + var5 * 18));
         }
      }

      for(int var6 = 0; var6 < 9; ++var6) {
         this.addSlot(new Slot(var1, var6, 8 + var6 * 18, 142));
      }

   }

   public boolean stillValid(Player var1) {
      return this.trap.stillValid(var1);
   }
}
