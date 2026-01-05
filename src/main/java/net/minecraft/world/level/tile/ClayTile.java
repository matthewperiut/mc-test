package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Material;

public class ClayTile extends Tile {
   public ClayTile(int var1, int var2) {
      super(var1, var2, Material.clay);
   }

   public int getResource(int var1, Random var2) {
      return Item.clay.id;
   }

   public int getResourceCount(Random var1) {
      return 4;
   }
}
