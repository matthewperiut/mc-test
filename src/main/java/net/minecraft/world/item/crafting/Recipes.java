package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.tile.Tile;

public class Recipes {
   public static final int ANY_AUX_VALUE = -1;
   private static final Recipes instance = new Recipes();
   private List<Recipy> recipies = new ArrayList();

   public static final Recipes getInstance() {
      return instance;
   }

   private Recipes() {
      (new ToolRecipies()).addRecipes(this);
      (new WeaponRecipies()).addRecipes(this);
      (new OreRecipies()).addRecipes(this);
      (new FoodRecipies()).addRecipes(this);
      (new StructureRecipies()).addRecipes(this);
      (new ArmorRecipes()).addRecipes(this);
      (new ClothDyeRecipes()).addRecipes(this);
      this.addShapedRecipy(new ItemInstance(Item.paper, 3), "###", '#', Item.reeds);
      this.addShapedRecipy(new ItemInstance(Item.book, 1), "#", "#", "#", '#', Item.paper);
      this.addShapedRecipy(new ItemInstance(Tile.fence, 2), "###", "###", '#', Item.stick);
      this.addShapedRecipy(new ItemInstance(Tile.recordPlayer, 1), "###", "#X#", "###", '#', Tile.wood, 'X', Item.emerald);
      this.addShapedRecipy(new ItemInstance(Tile.musicBlock, 1), "###", "#X#", "###", '#', Tile.wood, 'X', Item.redStone);
      this.addShapedRecipy(new ItemInstance(Tile.bookshelf, 1), "###", "XXX", "###", '#', Tile.wood, 'X', Item.book);
      this.addShapedRecipy(new ItemInstance(Tile.snow, 1), "##", "##", '#', Item.snowBall);
      this.addShapedRecipy(new ItemInstance(Tile.clay, 1), "##", "##", '#', Item.clay);
      this.addShapedRecipy(new ItemInstance(Tile.redBrick, 1), "##", "##", '#', Item.brick);
      this.addShapedRecipy(new ItemInstance(Tile.lightGem, 1), "###", "###", "###", '#', Item.yellowDust);
      this.addShapedRecipy(new ItemInstance(Tile.cloth, 1), "###", "###", "###", '#', Item.string);
      this.addShapedRecipy(new ItemInstance(Tile.tnt, 1), "X#X", "#X#", "X#X", 'X', Item.sulphur, '#', Tile.sand);
      this.addShapedRecipy(new ItemInstance(Tile.stoneSlabHalf, 3), "###", '#', Tile.stoneBrick);
      this.addShapedRecipy(new ItemInstance(Tile.ladder, 1), "# #", "###", "# #", '#', Item.stick);
      this.addShapedRecipy(new ItemInstance(Item.door_wood, 1), "##", "##", "##", '#', Tile.wood);
      this.addShapedRecipy(new ItemInstance(Item.door_iron, 1), "##", "##", "##", '#', Item.ironIngot);
      this.addShapedRecipy(new ItemInstance(Item.sign, 1), "###", "###", " X ", '#', Tile.wood, 'X', Item.stick);
      this.addShapedRecipy(new ItemInstance(Item.cake, 1), "AAA", "BEB", "CCC", 'A', Item.milk, 'B', Item.sugar, 'C', Item.wheat, 'E', Item.egg);
      this.addShapedRecipy(new ItemInstance(Item.sugar, 1), "#", '#', Item.reeds);
      this.addShapedRecipy(new ItemInstance(Tile.wood, 4), "#", '#', Tile.treeTrunk);
      this.addShapedRecipy(new ItemInstance(Item.stick, 4), "#", "#", '#', Tile.wood);
      this.addShapedRecipy(new ItemInstance(Tile.torch, 4), "X", "#", 'X', Item.coal, '#', Item.stick);
      this.addShapedRecipy(new ItemInstance(Tile.torch, 4), "X", "#", 'X', new ItemInstance(Item.coal, 1, 1), '#', Item.stick);
      this.addShapedRecipy(new ItemInstance(Item.bowl, 4), "# #", " # ", '#', Tile.wood);
      this.addShapedRecipy(new ItemInstance(Tile.rail, 16), "X X", "X#X", "X X", 'X', Item.ironIngot, '#', Item.stick);
      this.addShapedRecipy(new ItemInstance(Item.minecart, 1), "# #", "###", '#', Item.ironIngot);
      this.addShapedRecipy(new ItemInstance(Tile.litPumpkin, 1), "A", "B", 'A', Tile.pumpkin, 'B', Tile.torch);
      this.addShapedRecipy(new ItemInstance(Item.minecart_chest, 1), "A", "B", 'A', Tile.chest, 'B', Item.minecart);
      this.addShapedRecipy(new ItemInstance(Item.minecart_furnace, 1), "A", "B", 'A', Tile.furnace, 'B', Item.minecart);
      this.addShapedRecipy(new ItemInstance(Item.boat, 1), "# #", "###", '#', Tile.wood);
      this.addShapedRecipy(new ItemInstance(Item.bucket_empty, 1), "# #", " # ", '#', Item.ironIngot);
      this.addShapedRecipy(new ItemInstance(Item.flintAndSteel, 1), "A ", " B", 'A', Item.ironIngot, 'B', Item.flint);
      this.addShapedRecipy(new ItemInstance(Item.bread, 1), "###", '#', Item.wheat);
      this.addShapedRecipy(new ItemInstance(Tile.stairs_wood, 4), "#  ", "## ", "###", '#', Tile.wood);
      this.addShapedRecipy(new ItemInstance(Item.fishingRod, 1), "  #", " #X", "# X", '#', Item.stick, 'X', Item.string);
      this.addShapedRecipy(new ItemInstance(Tile.stairs_stone, 4), "#  ", "## ", "###", '#', Tile.stoneBrick);
      this.addShapedRecipy(new ItemInstance(Item.painting, 1), "###", "#X#", "###", '#', Item.stick, 'X', Tile.cloth);
      this.addShapedRecipy(new ItemInstance(Item.apple_gold, 1), "###", "#X#", "###", '#', Tile.goldBlock, 'X', Item.apple);
      this.addShapedRecipy(new ItemInstance(Tile.lever, 1), "X", "#", '#', Tile.stoneBrick, 'X', Item.stick);
      this.addShapedRecipy(new ItemInstance(Tile.notGate_on, 1), "X", "#", '#', Item.stick, 'X', Item.redStone);
      this.addShapedRecipy(new ItemInstance(Item.clock, 1), " # ", "#X#", " # ", '#', Item.goldIngot, 'X', Item.redStone);
      this.addShapedRecipy(new ItemInstance(Item.compass, 1), " # ", "#X#", " # ", '#', Item.ironIngot, 'X', Item.redStone);
      this.addShapedRecipy(new ItemInstance(Tile.button, 1), "#", "#", '#', Tile.rock);
      this.addShapedRecipy(new ItemInstance(Tile.pressurePlate_stone, 1), "###", '#', Tile.rock);
      this.addShapedRecipy(new ItemInstance(Tile.pressurePlate_wood, 1), "###", '#', Tile.wood);
      this.addShapedRecipy(new ItemInstance(Tile.dispenser, 1), "###", "#X#", "#R#", '#', Tile.stoneBrick, 'X', Item.bow, 'R', Item.redStone);
      Collections.sort(this.recipies, new Comparator<Recipy>() {
         public int compare(Recipy var1, Recipy var2) {
            if (var1 instanceof ShapelessRecipy && var2 instanceof ShapedRecipy) {
               return 1;
            } else if (var2 instanceof ShapelessRecipy && var1 instanceof ShapedRecipy) {
               return -1;
            } else if (var2.size() < var1.size()) {
               return -1;
            } else {
               return var2.size() > var1.size() ? 1 : 0;
            }
         }
      });
      System.out.println(this.recipies.size() + " recipes");
   }

   void addShapedRecipy(ItemInstance var1, Object... var2) {
      String var3 = "";
      int var4 = 0;
      int var5 = 0;
      int var6 = 0;
      if (var2[var4] instanceof String[]) {
         String[] var11 = (String[])var2[var4++];

         for(int var8 = 0; var8 < var11.length; ++var8) {
            String var9 = var11[var8];
            ++var6;
            var5 = var9.length();
            var3 = var3 + var9;
         }
      } else {
         while(var2[var4] instanceof String) {
            String var7 = (String)var2[var4++];
            ++var6;
            var5 = var7.length();
            var3 = var3 + var7;
         }
      }

      HashMap var12;
      for(var12 = new HashMap(); var4 < var2.length; var4 += 2) {
         Character var13 = (Character)var2[var4];
         ItemInstance var15 = null;
         if (var2[var4 + 1] instanceof Item) {
            var15 = new ItemInstance((Item)var2[var4 + 1]);
         } else if (var2[var4 + 1] instanceof Tile) {
            var15 = new ItemInstance((Tile)var2[var4 + 1], 1, -1);
         } else if (var2[var4 + 1] instanceof ItemInstance) {
            var15 = (ItemInstance)var2[var4 + 1];
         }

         var12.put(var13, var15);
      }

      ItemInstance[] var14 = new ItemInstance[var5 * var6];

      for(int var16 = 0; var16 < var5 * var6; ++var16) {
         char var10 = var3.charAt(var16);
         if (var12.containsKey(var10)) {
            var14[var16] = ((ItemInstance)var12.get(var10)).copy();
         } else {
            var14[var16] = null;
         }
      }

      this.recipies.add(new ShapedRecipy(var5, var6, var14, var1));
   }

   void addShapelessRecipy(ItemInstance var1, Object... var2) {
      ArrayList var3 = new ArrayList();

      for(Object var7 : var2) {
         if (var7 instanceof ItemInstance) {
            var3.add(((ItemInstance)var7).copy());
         } else if (var7 instanceof Item) {
            var3.add(new ItemInstance((Item)var7));
         } else {
            if (!(var7 instanceof Tile)) {
               throw new RuntimeException("Invalid shapeless recipy!");
            }

            var3.add(new ItemInstance((Tile)var7));
         }
      }

      this.recipies.add(new ShapelessRecipy(var1, var3));
   }

   public ItemInstance getItemFor(CraftingContainer var1) {
      for(int var2 = 0; var2 < this.recipies.size(); ++var2) {
         Recipy var3 = (Recipy)this.recipies.get(var2);
         if (var3.matches(var1)) {
            return var3.assemble(var1);
         }
      }

      return null;
   }
}
