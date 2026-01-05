package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.Material;

public class SnowTile extends Tile {
   protected SnowTile(int var1, int var2) {
      super(var1, var2, Material.snow);
      this.setTicking(true);
   }

   public int getResource(int var1, Random var2) {
      return Item.snowBall.id;
   }

   public int getResourceCount(Random var1) {
      return 4;
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      if (var1.getBrightness(LightLayer.Block, var2, var3, var4) > 11) {
         this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
         var1.setTile(var2, var3, var4, 0);
      }

   }
}
