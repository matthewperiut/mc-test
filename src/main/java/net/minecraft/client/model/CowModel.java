package net.minecraft.client.model;

public class CowModel extends QuadrupedModel {
   Cube udder;
   Cube horn1;
   Cube horn2;

   public CowModel() {
      super(12, 0.0F);
      this.head = new Cube(0, 0);
      this.head.addBox(-4.0F, -4.0F, -6.0F, 8, 8, 6, 0.0F);
      this.head.setPos(0.0F, 4.0F, -8.0F);
      this.horn1 = new Cube(22, 0);
      this.horn1.addBox(-4.0F, -5.0F, -4.0F, 1, 3, 1, 0.0F);
      this.horn1.setPos(0.0F, 3.0F, -7.0F);
      this.horn2 = new Cube(22, 0);
      this.horn2.addBox(4.0F, -5.0F, -4.0F, 1, 3, 1, 0.0F);
      this.horn2.setPos(0.0F, 3.0F, -7.0F);
      this.udder = new Cube(52, 0);
      this.udder.addBox(-2.0F, -3.0F, 0.0F, 4, 6, 2, 0.0F);
      this.udder.setPos(0.0F, 14.0F, 6.0F);
      this.udder.xRot = ((float)Math.PI / 2F);
      this.body = new Cube(18, 4);
      this.body.addBox(-6.0F, -10.0F, -7.0F, 12, 18, 10, 0.0F);
      this.body.setPos(0.0F, 5.0F, 2.0F);
      --this.leg0.x;
      ++this.leg1.x;
      Cube var10000 = this.leg0;
      var10000.z += 0.0F;
      var10000 = this.leg1;
      var10000.z += 0.0F;
      --this.leg2.x;
      ++this.leg3.x;
      --this.leg2.z;
      --this.leg3.z;
   }

   public void render(float time, float r, float bob, float yRot, float xRot, float scale) {
      super.render(time, r, bob, yRot, xRot, scale);
      this.horn1.render(scale);
      this.horn2.render(scale);
      this.udder.render(scale);
   }

   public void setupAnim(float time, float r, float bob, float yRot, float xRot, float scale) {
      super.setupAnim(time, r, bob, yRot, xRot, scale);
      this.horn1.yRot = this.head.yRot;
      this.horn1.xRot = this.head.xRot;
      this.horn2.yRot = this.head.yRot;
      this.horn2.xRot = this.head.xRot;
   }
}
