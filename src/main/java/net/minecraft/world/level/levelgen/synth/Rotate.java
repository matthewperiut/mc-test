package net.minecraft.world.level.levelgen.synth;

public class Rotate extends Synth {
   private Synth synth;
   private double sin;
   private double cos;

   public Rotate(Synth var1, float var2) {
      this.synth = var1;
      this.sin = Math.sin((double)var2);
      this.cos = Math.cos((double)var2);
   }

   public double getValue(double var1, double var3) {
      return this.synth.getValue(var1 * this.cos + var3 * this.sin, var3 * this.cos - var1 * this.sin);
   }
}
