package net.minecraft.world.item.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.tile.Tile;

public class OreRecipies {
   private Object[][] map;

   public OreRecipies() {
      this.map = new Object[][]{{Tile.goldBlock, new ItemInstance(Item.goldIngot, 9)}, {Tile.ironBlock, new ItemInstance(Item.ironIngot, 9)}, {Tile.emeraldBlock, new ItemInstance(Item.emerald, 9)}, {Tile.lapisBlock, new ItemInstance(Item.dye_powder, 9, 4)}};
   }

   public void addRecipes(Recipes var1) {
      for(int var2 = 0; var2 < this.map.length; ++var2) {
         Tile var3 = (Tile)this.map[var2][0];
         ItemInstance var4 = (ItemInstance)this.map[var2][1];
         var1.addShapedRecipy(new ItemInstance(var3), "###", "###", "###", '#', var4);
         var1.addShapedRecipy(var4, "#", '#', var3);
      }

   }
}
