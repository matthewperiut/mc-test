package net.minecraft.world.level.biome;

import java.util.Random;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.PineFeature;
import net.minecraft.world.level.levelgen.feature.SpruceFeature;

public class TaigaBiome extends Biome {
   public TaigaBiome() {
   }

   public Feature getTreeFeature(Random var1) {
      return (Feature)(var1.nextInt(3) == 0 ? new PineFeature() : new SpruceFeature());
   }
}
