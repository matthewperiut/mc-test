package net.minecraft.world.item.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.tile.Tile;

public class ToolRecipies {
   private String[][] shapes = new String[][]{{"XXX", " # ", " # "}, {"X", "#", "#"}, {"XX", "X#", " #"}, {"XX", " #", " #"}};
   private Object[][] map;

   public ToolRecipies() {
      this.map = new Object[][]{{Tile.wood, Tile.stoneBrick, Item.ironIngot, Item.emerald, Item.goldIngot}, {Item.pickAxe_wood, Item.pickAxe_stone, Item.pickAxe_iron, Item.pickAxe_emerald, Item.pickAxe_gold}, {Item.shovel_wood, Item.shovel_stone, Item.shovel_iron, Item.shovel_emerald, Item.shovel_gold}, {Item.hatchet_wood, Item.hatchet_stone, Item.hatchet_iron, Item.hatchet_emerald, Item.hatchet_gold}, {Item.hoe_wood, Item.hoe_stone, Item.hoe_iron, Item.hoe_emerald, Item.hoe_gold}};
   }

   public void addRecipes(Recipes var1) {
      for(int var2 = 0; var2 < this.map[0].length; ++var2) {
         Object var3 = this.map[0][var2];

         for(int var4 = 0; var4 < this.map.length - 1; ++var4) {
            Item var5 = (Item)this.map[var4 + 1][var2];
            var1.addShapedRecipy(new ItemInstance(var5), this.shapes[var4], '#', Item.stick, 'X', var3);
         }
      }

   }
}
