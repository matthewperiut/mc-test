package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Material;

public class LightGemTile extends Tile {
   public LightGemTile(int var1, int var2, Material var3) {
      super(var1, var2, var3);
   }

   public int getResource(int var1, Random var2) {
      return Item.yellowDust.id;
   }
}
