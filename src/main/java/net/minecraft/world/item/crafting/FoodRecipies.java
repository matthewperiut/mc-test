package net.minecraft.world.item.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.tile.Tile;

public class FoodRecipies {
   public FoodRecipies() {
   }

   public void addRecipes(Recipes var1) {
      var1.addShapedRecipy(new ItemInstance(Item.mushroomStew), "Y", "X", "#", 'X', Tile.mushroom1, 'Y', Tile.mushroom2, '#', Item.bowl);
      var1.addShapedRecipy(new ItemInstance(Item.mushroomStew), "Y", "X", "#", 'X', Tile.mushroom2, 'Y', Tile.mushroom1, '#', Item.bowl);
   }
}
