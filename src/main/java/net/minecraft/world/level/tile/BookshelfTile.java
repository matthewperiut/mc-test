package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.material.Material;

public class BookshelfTile extends Tile {
   public BookshelfTile(int var1, int var2) {
      super(var1, var2, Material.wood);
   }

   public int getTexture(int var1) {
      return var1 <= 1 ? 4 : this.tex;
   }

   public int getResourceCount(Random var1) {
      return 0;
   }
}
