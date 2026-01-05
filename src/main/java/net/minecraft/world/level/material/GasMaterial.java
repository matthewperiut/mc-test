package net.minecraft.world.level.material;

public class GasMaterial extends Material {
   public GasMaterial() {
   }

   public boolean isSolid() {
      return false;
   }

   public boolean blocksLight() {
      return false;
   }

   public boolean blocksMotion() {
      return false;
   }
}
