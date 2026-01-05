package net.minecraft.world.level.material;

public class Material {
   public static final Material air = new GasMaterial();
   public static final Material dirt = new Material();
   public static final Material wood = (new Material()).flammable();
   public static final Material stone = new Material();
   public static final Material metal = new Material();
   public static final Material water = new LiquidMaterial();
   public static final Material lava = new LiquidMaterial();
   public static final Material leaves = (new Material()).flammable();
   public static final Material plant = new DecorationMaterial();
   public static final Material sponge = new Material();
   public static final Material cloth = (new Material()).flammable();
   public static final Material fire = new GasMaterial();
   public static final Material sand = new Material();
   public static final Material decoration = new DecorationMaterial();
   public static final Material glass = new Material();
   public static final Material explosive = (new Material()).flammable();
   public static final Material coral = new Material();
   public static final Material ice = new Material();
   public static final Material topSnow = new DecorationMaterial();
   public static final Material snow = new Material();
   public static final Material cactus = new Material();
   public static final Material clay = new Material();
   public static final Material vegetable = new Material();
   public static final Material portal = new Material();
   public static final Material cake = new Material();
   private boolean flammable;

   public Material() {
   }

   public boolean isLiquid() {
      return false;
   }

   public boolean letsWaterThrough() {
      return !this.isLiquid() && !this.isSolid();
   }

   public boolean isSolid() {
      return true;
   }

   public boolean blocksLight() {
      return true;
   }

   public boolean blocksMotion() {
      return true;
   }

   private Material flammable() {
      this.flammable = true;
      return this;
   }

   public boolean isFlammable() {
      return this.flammable;
   }
}
