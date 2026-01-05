package net.minecraft.world.level.material;

public class LiquidMaterial extends Material {
   public LiquidMaterial() {
   }

   public boolean isLiquid() {
      return true;
   }

   public boolean blocksMotion() {
      return false;
   }

   public boolean isSolid() {
      return false;
   }
}
