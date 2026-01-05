package net.minecraft.world.level.biome;

import java.util.Random;
import net.minecraft.world.level.levelgen.feature.BasicTree;
import net.minecraft.world.level.levelgen.feature.BirchFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

public class ForestBiome extends Biome {
   public ForestBiome() {
   }

   public Feature getTreeFeature(Random var1) {
      if (var1.nextInt(5) == 0) {
         return new BirchFeature();
      } else {
         return (Feature)(var1.nextInt(3) == 0 ? new BasicTree() : new TreeFeature());
      }
   }
}
