package net.minecraft.world.level.levelgen.feature;

import java.util.Random;
import net.minecraft.world.level.Level;

public abstract class Feature {
   public Feature() {
   }

   public abstract boolean place(Level var1, Random var2, int var3, int var4, int var5);

   public void init(double var1, double var3, double var5) {
   }
}
