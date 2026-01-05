package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Material;

public class OreTile extends Tile {
   public OreTile(int var1, int var2) {
      super(var1, var2, Material.stone);
   }

   public int getResource(int var1, Random var2) {
      if (this.id == Tile.coalOre.id) {
         return Item.coal.id;
      } else if (this.id == Tile.emeraldOre.id) {
         return Item.emerald.id;
      } else {
         return this.id == Tile.lapisOre.id ? Item.dye_powder.id : this.id;
      }
   }

   public int getResourceCount(Random var1) {
      return this.id == Tile.lapisOre.id ? 4 + var1.nextInt(5) : 1;
   }

   protected int getSpawnResourcesAuxValue(int var1) {
      return this.id == Tile.lapisOre.id ? 4 : 0;
   }
}
