package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.tile.Tile;

public class StructureRecipies {
   public StructureRecipies() {
   }

   public void addRecipes(Recipes var1) {
      var1.addShapedRecipy(new ItemInstance(Tile.chest), "###", "# #", "###", '#', Tile.wood);
      var1.addShapedRecipy(new ItemInstance(Tile.furnace), "###", "# #", "###", '#', Tile.stoneBrick);
      var1.addShapedRecipy(new ItemInstance(Tile.workBench), "##", "##", '#', Tile.wood);
      var1.addShapedRecipy(new ItemInstance(Tile.sandStone), "##", "##", '#', Tile.sand);
   }
}
