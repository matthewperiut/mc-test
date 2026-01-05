package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.item.Item;

public class GravelTile extends SandTile {
   public GravelTile(int var1, int var2) {
      super(var1, var2);
   }

   public int getResource(int var1, Random var2) {
      return var2.nextInt(10) == 0 ? Item.flint.id : this.id;
   }
}
