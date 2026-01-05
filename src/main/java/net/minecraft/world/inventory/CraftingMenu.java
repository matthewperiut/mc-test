package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.crafting.Recipes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class CraftingMenu extends AbstractContainerMenu {
   public CraftingContainer craftSlots = new CraftingContainer(this, 3, 3);
   public Container resultSlots = new ResultContainer();
   private Level level;
   private int x;
   private int y;
   private int z;

   public CraftingMenu(Inventory var1, Level var2, int var3, int var4, int var5) {
      this.level = var2;
      this.x = var3;
      this.y = var4;
      this.z = var5;
      this.addSlot(new ResultSlot(this.craftSlots, this.resultSlots, 0, 124, 35));

      for(int var6 = 0; var6 < 3; ++var6) {
         for(int var7 = 0; var7 < 3; ++var7) {
            this.addSlot(new Slot(this.craftSlots, var7 + var6 * 3, 30 + var7 * 18, 17 + var6 * 18));
         }
      }

      for(int var8 = 0; var8 < 3; ++var8) {
         for(int var10 = 0; var10 < 9; ++var10) {
            this.addSlot(new Slot(var1, var10 + var8 * 9 + 9, 8 + var10 * 18, 84 + var8 * 18));
         }
      }

      for(int var9 = 0; var9 < 9; ++var9) {
         this.addSlot(new Slot(var1, var9, 8 + var9 * 18, 142));
      }

      this.slotsChanged(this.craftSlots);
   }

   public void slotsChanged(Container var1) {
      this.resultSlots.setItem(0, Recipes.getInstance().getItemFor(this.craftSlots));
   }

   public void removed(Player var1) {
      super.removed(var1);

      for(int var2 = 0; var2 < 9; ++var2) {
         ItemInstance var3 = this.craftSlots.getItem(var2);
         if (var3 != null) {
            var1.drop(var3);
         }
      }

   }

   public boolean stillValid(Player var1) {
      if (this.level.getTile(this.x, this.y, this.z) != Tile.workBench.id) {
         return false;
      } else {
         return !(var1.distanceToSqr((double)this.x + (double)0.5F, (double)this.y + (double)0.5F, (double)this.z + (double)0.5F) > (double)64.0F);
      }
   }
}
