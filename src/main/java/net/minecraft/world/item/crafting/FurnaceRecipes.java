package net.minecraft.world.item.crafting;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.tile.Tile;

public class FurnaceRecipes {
   private static final FurnaceRecipes instance = new FurnaceRecipes();
   private Map<Integer, ItemInstance> recipies = new HashMap();

   public static final FurnaceRecipes getInstance() {
      return instance;
   }

   private FurnaceRecipes() {
      this.addFurnaceRecipy(Tile.ironOre.id, new ItemInstance(Item.ironIngot));
      this.addFurnaceRecipy(Tile.goldOre.id, new ItemInstance(Item.goldIngot));
      this.addFurnaceRecipy(Tile.emeraldOre.id, new ItemInstance(Item.emerald));
      this.addFurnaceRecipy(Tile.sand.id, new ItemInstance(Tile.glass));
      this.addFurnaceRecipy(Item.porkChop_raw.id, new ItemInstance(Item.porkChop_cooked));
      this.addFurnaceRecipy(Item.fish_raw.id, new ItemInstance(Item.fish_cooked));
      this.addFurnaceRecipy(Tile.stoneBrick.id, new ItemInstance(Tile.rock));
      this.addFurnaceRecipy(Item.clay.id, new ItemInstance(Item.brick));
      this.addFurnaceRecipy(Tile.cactus.id, new ItemInstance(Item.dye_powder, 1, 2));
      this.addFurnaceRecipy(Tile.treeTrunk.id, new ItemInstance(Item.coal, 1, 1));
   }

   public void addFurnaceRecipy(int var1, ItemInstance var2) {
      this.recipies.put(var1, var2);
   }

   public boolean isFurnaceItem(int var1) {
      return this.recipies.containsKey(var1);
   }

   public ItemInstance getResult(int var1) {
      return (ItemInstance)this.recipies.get(var1);
   }
}
