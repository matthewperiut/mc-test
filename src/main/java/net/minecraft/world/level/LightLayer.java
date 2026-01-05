package net.minecraft.world.level;

public enum LightLayer {
   Sky(15),
   Block(0);

   public final int surrounding;

   private LightLayer(int var3) {
      this.surrounding = var3;
   }
}
