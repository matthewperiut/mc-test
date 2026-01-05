package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.Material;

public class IceTile extends HalfTransparentTile {
   public IceTile(int var1, int var2) {
      super(var1, var2, Material.ice, false);
      this.friction = 0.98F;
      this.setTicking(true);
   }

   public int getRenderLayer() {
      return 1;
   }

   public boolean shouldRenderFace(LevelSource var1, int var2, int var3, int var4, int var5) {
      return super.shouldRenderFace(var1, var2, var3, var4, 1 - var5);
   }

   public void onRemove(Level var1, int var2, int var3, int var4) {
      Material var5 = var1.getMaterial(var2, var3 - 1, var4);
      if (var5.blocksMotion() || var5.isLiquid()) {
         var1.setTile(var2, var3, var4, Tile.water.id);
      }

   }

   public int getResourceCount(Random var1) {
      return 0;
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      if (var1.getBrightness(LightLayer.Block, var2, var3, var4) > 11 - Tile.lightBlock[this.id]) {
         this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
         var1.setTile(var2, var3, var4, Tile.calmWater.id);
      }

   }
}
