package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public class ContainerMenu extends AbstractContainerMenu {
   private Container container;

   public ContainerMenu(Container var1, Container var2) {
      this.container = var2;
      int var3 = var2.getContainerSize() / 9;
      int var4 = (var3 - 4) * 18;

      for(int var5 = 0; var5 < var3; ++var5) {
         for(int var6 = 0; var6 < 9; ++var6) {
            this.addSlot(new Slot(var2, var6 + var5 * 9, 8 + var6 * 18, 18 + var5 * 18));
         }
      }

      for(int var7 = 0; var7 < 3; ++var7) {
         for(int var9 = 0; var9 < 9; ++var9) {
            this.addSlot(new Slot(var1, var9 + var7 * 9 + 9, 8 + var9 * 18, 103 + var7 * 18 + var4));
         }
      }

      for(int var8 = 0; var8 < 9; ++var8) {
         this.addSlot(new Slot(var1, var8, 8 + var8 * 18, 161 + var4));
      }

   }

   public boolean stillValid(Player var1) {
      return this.container.stillValid(var1);
   }
}
