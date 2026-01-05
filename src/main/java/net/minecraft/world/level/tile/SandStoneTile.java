package net.minecraft.world.level.tile;

import net.minecraft.world.level.material.Material;

public class SandStoneTile extends Tile {
   public SandStoneTile(int var1) {
      super(var1, 192, Material.stone);
   }

   public int getTexture(int var1) {
      if (var1 == 1) {
         return this.tex - 16;
      } else {
         return var1 == 0 ? this.tex + 16 : this.tex;
      }
   }
}
