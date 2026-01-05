package net.minecraft.world.item.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.tile.Tile;

public class ArmorRecipes {
   private String[][] shapes = new String[][]{{"XXX", "X X"}, {"X X", "XXX", "XXX"}, {"XXX", "X X", "X X"}, {"X X", "X X"}};
   private Object[][] map;

   public ArmorRecipes() {
      this.map = new Object[][]{{Item.leather, Tile.fire, Item.ironIngot, Item.emerald, Item.goldIngot}, {Item.helmet_cloth, Item.helmet_chain, Item.helmet_iron, Item.helmet_diamond, Item.helmet_gold}, {Item.chestplate_cloth, Item.chestplate_chain, Item.chestplate_iron, Item.chestplate_diamond, Item.chestplate_gold}, {Item.leggings_cloth, Item.leggings_chain, Item.leggings_iron, Item.leggings_diamond, Item.leggings_gold}, {Item.boots_cloth, Item.boots_chain, Item.boots_iron, Item.boots_diamond, Item.boots_gold}};
   }

   public void addRecipes(Recipes var1) {
      for(int var2 = 0; var2 < this.map[0].length; ++var2) {
         Object var3 = this.map[0][var2];

         for(int var4 = 0; var4 < this.map.length - 1; ++var4) {
            Item var5 = (Item)this.map[var4 + 1][var2];
            var1.addShapedRecipy(new ItemInstance(var5), this.shapes[var4], 'X', var3);
         }
      }

   }
}
