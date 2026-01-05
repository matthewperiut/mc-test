package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemInstance;

public class ShapelessRecipy implements Recipy {
   private final ItemInstance result;
   private final List<ItemInstance> ingredients;

   public ShapelessRecipy(ItemInstance var1, List<ItemInstance> var2) {
      this.result = var1;
      this.ingredients = var2;
   }

   public boolean matches(CraftingContainer var1) {
      ArrayList var2 = new ArrayList(this.ingredients);

      for(int var3 = 0; var3 < 3; ++var3) {
         for(int var4 = 0; var4 < 3; ++var4) {
            ItemInstance var5 = var1.getItem(var4, var3);
            if (var5 != null) {
               boolean var6 = false;

               for(Object var8obj : var2) {
                  ItemInstance var8 = (ItemInstance)var8obj;
                  if (var5.id == var8.id && (var8.getAuxValue() == -1 || var5.getAuxValue() == var8.getAuxValue())) {
                     var6 = true;
                     var2.remove(var8);
                     break;
                  }
               }

               if (!var6) {
                  return false;
               }
            }
         }
      }

      return var2.isEmpty();
   }

   public ItemInstance assemble(CraftingContainer var1) {
      return this.result.copy();
   }

   public int size() {
      return this.ingredients.size();
   }
}
