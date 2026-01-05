package net.minecraft.world.level.tile;

import net.minecraft.world.level.material.Material;

public class MetalTile extends Tile {
   public MetalTile(int var1, int var2) {
      super(var1, Material.metal);
      this.tex = var2;
   }

   public int getTexture(int var1) {
      return this.tex;
   }
}
