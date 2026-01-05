package net.minecraft.world.item.crafting;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemInstance;

public class ShapedRecipy implements Recipy {
   private int width;
   private int height;
   private ItemInstance[] recipeItems;
   private ItemInstance result;
   public final int resultId;

   public ShapedRecipy(int var1, int var2, ItemInstance[] var3, ItemInstance var4) {
      this.resultId = var4.id;
      this.width = var1;
      this.height = var2;
      this.recipeItems = var3;
      this.result = var4;
   }

   public boolean matches(CraftingContainer var1) {
      for(int var2 = 0; var2 <= 3 - this.width; ++var2) {
         for(int var3 = 0; var3 <= 3 - this.height; ++var3) {
            if (this.matches(var1, var2, var3, true)) {
               return true;
            }

            if (this.matches(var1, var2, var3, false)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean matches(CraftingContainer var1, int var2, int var3, boolean var4) {
      for(int var5 = 0; var5 < 3; ++var5) {
         for(int var6 = 0; var6 < 3; ++var6) {
            int var7 = var5 - var2;
            int var8 = var6 - var3;
            ItemInstance var9 = null;
            if (var7 >= 0 && var8 >= 0 && var7 < this.width && var8 < this.height) {
               if (var4) {
                  var9 = this.recipeItems[this.width - var7 - 1 + var8 * this.width];
               } else {
                  var9 = this.recipeItems[var7 + var8 * this.width];
               }
            }

            ItemInstance var10 = var1.getItem(var5, var6);
            if (var10 != null || var9 != null) {
               if (var10 == null && var9 != null || var10 != null && var9 == null) {
                  return false;
               }

               if (var9.id != var10.id) {
                  return false;
               }

               if (var9.getAuxValue() != -1 && var9.getAuxValue() != var10.getAuxValue()) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   public ItemInstance assemble(CraftingContainer var1) {
      return new ItemInstance(this.result.id, this.result.count, this.result.getAuxValue());
   }

   public int size() {
      return this.width * this.height;
   }
}
