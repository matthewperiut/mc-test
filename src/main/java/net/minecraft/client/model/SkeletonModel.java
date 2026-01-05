package net.minecraft.client.model;

public class SkeletonModel extends ZombieModel {
   public SkeletonModel() {
      float g = 0.0F;
      this.arm0 = new Cube(40, 16);
      this.arm0.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, g);
      this.arm0.setPos(-5.0F, 2.0F, 0.0F);
      this.arm1 = new Cube(40, 16);
      this.arm1.mirror = true;
      this.arm1.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, g);
      this.arm1.setPos(5.0F, 2.0F, 0.0F);
      this.leg0 = new Cube(0, 16);
      this.leg0.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, g);
      this.leg0.setPos(-2.0F, 12.0F, 0.0F);
      this.leg1 = new Cube(0, 16);
      this.leg1.mirror = true;
      this.leg1.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, g);
      this.leg1.setPos(2.0F, 12.0F, 0.0F);
   }
}
