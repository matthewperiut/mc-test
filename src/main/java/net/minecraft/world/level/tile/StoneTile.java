package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.material.Material;

public class StoneTile extends Tile {
   public StoneTile(int var1, int var2) {
      super(var1, var2, Material.stone);
   }

   public int getResource(int var1, Random var2) {
      return Tile.stoneBrick.id;
   }
}
