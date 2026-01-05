package net.minecraft.world.level.biome;

import java.util.Random;
import net.minecraft.world.level.levelgen.feature.BasicTree;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

public class RainforestBiome extends Biome {
   public RainforestBiome() {
   }

   public Feature getTreeFeature(Random var1) {
      return (Feature)(var1.nextInt(3) == 0 ? new BasicTree() : new TreeFeature());
   }
}
