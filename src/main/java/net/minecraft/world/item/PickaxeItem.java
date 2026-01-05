package net.minecraft.world.item;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;

public class PickaxeItem extends DiggerItem {
   private static Tile[] diggables;

   protected PickaxeItem(int var1, Item.Tier var2) {
      super(var1, 2, var2, diggables);
   }

   public boolean canDestroySpecial(Tile var1) {
      if (var1 == Tile.obsidian) {
         return this.tier.getLevel() == 3;
      } else if (var1 != Tile.emeraldBlock && var1 != Tile.emeraldOre) {
         if (var1 != Tile.goldBlock && var1 != Tile.goldOre) {
            if (var1 != Tile.ironBlock && var1 != Tile.ironOre) {
               if (var1 != Tile.lapisBlock && var1 != Tile.lapisOre) {
                  if (var1 != Tile.redStoneOre && var1 != Tile.redStoneOre_lit) {
                     if (var1.material == Material.stone) {
                        return true;
                     } else {
                        return var1.material == Material.metal;
                     }
                  } else {
                     return this.tier.getLevel() >= 2;
                  }
               } else {
                  return this.tier.getLevel() >= 1;
               }
            } else {
               return this.tier.getLevel() >= 1;
            }
         } else {
            return this.tier.getLevel() >= 2;
         }
      } else {
         return this.tier.getLevel() >= 2;
      }
   }

   static {
      diggables = new Tile[]{Tile.stoneBrick, Tile.stoneSlab, Tile.stoneSlabHalf, Tile.rock, Tile.mossStone, Tile.ironOre, Tile.ironBlock, Tile.coalOre, Tile.goldBlock, Tile.goldOre, Tile.emeraldOre, Tile.emeraldBlock, Tile.ice, Tile.hellRock, Tile.lapisOre, Tile.lapisBlock};
   }
}
