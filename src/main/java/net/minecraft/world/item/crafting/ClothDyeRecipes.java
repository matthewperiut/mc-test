package net.minecraft.world.item.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.tile.ClothTile;
import net.minecraft.world.level.tile.Tile;

public class ClothDyeRecipes {
   public ClothDyeRecipes() {
   }

   public void addRecipes(Recipes var1) {
      for(int var2 = 0; var2 < 16; ++var2) {
         var1.addShapelessRecipy(new ItemInstance(Tile.cloth, 1, ClothTile.getItemAuxValueForTileData(var2)), new ItemInstance(Item.dye_powder, 1, var2), new ItemInstance(Item.items[Tile.cloth.id], 1, 0));
      }

      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 2, 11), Tile.flower);
      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 2, 1), Tile.rose);
      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 3, 15), Item.bone);
      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 2, 9), new ItemInstance(Item.dye_powder, 1, 1), new ItemInstance(Item.dye_powder, 1, 15));
      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 2, 14), new ItemInstance(Item.dye_powder, 1, 1), new ItemInstance(Item.dye_powder, 1, 11));
      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 2, 10), new ItemInstance(Item.dye_powder, 1, 2), new ItemInstance(Item.dye_powder, 1, 15));
      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 2, 8), new ItemInstance(Item.dye_powder, 1, 0), new ItemInstance(Item.dye_powder, 1, 15));
      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 2, 7), new ItemInstance(Item.dye_powder, 1, 8), new ItemInstance(Item.dye_powder, 1, 15));
      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 3, 7), new ItemInstance(Item.dye_powder, 1, 0), new ItemInstance(Item.dye_powder, 1, 15), new ItemInstance(Item.dye_powder, 1, 15));
      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 2, 12), new ItemInstance(Item.dye_powder, 1, 4), new ItemInstance(Item.dye_powder, 1, 15));
      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 2, 6), new ItemInstance(Item.dye_powder, 1, 4), new ItemInstance(Item.dye_powder, 1, 2));
      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 2, 5), new ItemInstance(Item.dye_powder, 1, 4), new ItemInstance(Item.dye_powder, 1, 1));
      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 2, 13), new ItemInstance(Item.dye_powder, 1, 5), new ItemInstance(Item.dye_powder, 1, 9));
      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 3, 13), new ItemInstance(Item.dye_powder, 1, 4), new ItemInstance(Item.dye_powder, 1, 1), new ItemInstance(Item.dye_powder, 1, 9));
      var1.addShapelessRecipy(new ItemInstance(Item.dye_powder, 4, 13), new ItemInstance(Item.dye_powder, 1, 4), new ItemInstance(Item.dye_powder, 1, 1), new ItemInstance(Item.dye_powder, 1, 1), new ItemInstance(Item.dye_powder, 1, 15));
   }
}
