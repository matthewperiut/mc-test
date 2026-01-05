package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.BasicTree;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

public class Sapling extends Bush {
   protected Sapling(int var1, int var2) {
      super(var1, var2);
      float var3 = 0.4F;
      this.setShape(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, var3 * 2.0F, 0.5F + var3);
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      super.tick(var1, var2, var3, var4, var5);
      if (var1.getRawBrightness(var2, var3 + 1, var4) >= 9 && var5.nextInt(5) == 0) {
         int var6 = var1.getData(var2, var3, var4);
         if (var6 < 15) {
            var1.setData(var2, var3, var4, var6 + 1);
         } else {
            this.growTree(var1, var2, var3, var4, var5);
         }
      }

   }

   public void growTree(Level var1, int var2, int var3, int var4, Random var5) {
      var1.setTileNoUpdate(var2, var3, var4, 0);
      Object var6 = new TreeFeature();
      if (var5.nextInt(10) == 0) {
         var6 = new BasicTree();
      }

      if (!((Feature)var6).place(var1, var5, var2, var3, var4)) {
         var1.setTileNoUpdate(var2, var3, var4, this.id);
      }

   }
}
