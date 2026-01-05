package net.minecraft.world.item.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.tile.Tile;

public class WeaponRecipies {
   private String[][] shapes = new String[][]{{"X", "X", "#"}};
   private Object[][] map;

   public WeaponRecipies() {
      this.map = new Object[][]{{Tile.wood, Tile.stoneBrick, Item.ironIngot, Item.emerald, Item.goldIngot}, {Item.sword_wood, Item.sword_stone, Item.sword_iron, Item.sword_emerald, Item.sword_gold}};
   }

   public void addRecipes(Recipes var1) {
      for(int var2 = 0; var2 < this.map[0].length; ++var2) {
         Object var3 = this.map[0][var2];

         for(int var4 = 0; var4 < this.map.length - 1; ++var4) {
            Item var5 = (Item)this.map[var4 + 1][var2];
            var1.addShapedRecipy(new ItemInstance(var5), this.shapes[var4], '#', Item.stick, 'X', var3);
         }
      }

      var1.addShapedRecipy(new ItemInstance(Item.bow, 1), " #X", "# X", " #X", 'X', Item.string, '#', Item.stick);
      var1.addShapedRecipy(new ItemInstance(Item.arrow, 4), "X", "#", "Y", 'Y', Item.feather, 'X', Item.flint, '#', Item.stick);
   }
}
