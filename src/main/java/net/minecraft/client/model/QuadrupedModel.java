package net.minecraft.client.model;

import util.Mth;

public class QuadrupedModel extends Model {
   public Cube head = new Cube(0, 0);
   public Cube hair;
   public Cube body;
   public Cube leg0;
   public Cube leg1;
   public Cube leg2;
   public Cube leg3;

   public QuadrupedModel(int legSize, float g) {
      this.head.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, g);
      this.head.setPos(0.0F, (float)(18 - legSize), -6.0F);
      this.body = new Cube(28, 8);
      this.body.addBox(-5.0F, -10.0F, -7.0F, 10, 16, 8, g);
      this.body.setPos(0.0F, (float)(17 - legSize), 2.0F);
      this.leg0 = new Cube(0, 16);
      this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4, legSize, 4, g);
      this.leg0.setPos(-3.0F, (float)(24 - legSize), 7.0F);
      this.leg1 = new Cube(0, 16);
      this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, legSize, 4, g);
      this.leg1.setPos(3.0F, (float)(24 - legSize), 7.0F);
      this.leg2 = new Cube(0, 16);
      this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4, legSize, 4, g);
      this.leg2.setPos(-3.0F, (float)(24 - legSize), -5.0F);
      this.leg3 = new Cube(0, 16);
      this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4, legSize, 4, g);
      this.leg3.setPos(3.0F, (float)(24 - legSize), -5.0F);
   }

   public void render(float time, float r, float bob, float yRot, float xRot, float scale) {
      this.setupAnim(time, r, bob, yRot, xRot, scale);
      this.head.render(scale);
      this.body.render(scale);
      this.leg0.render(scale);
      this.leg1.render(scale);
      this.leg2.render(scale);
      this.leg3.render(scale);
   }

   public void setupAnim(float time, float r, float bob, float yRot, float xRot, float scale) {
      this.head.xRot = -(xRot / (180F / (float)Math.PI));
      this.head.yRot = yRot / (180F / (float)Math.PI);
      this.body.xRot = ((float)Math.PI / 2F);
      this.leg0.xRot = Mth.cos(time * 0.6662F) * 1.4F * r;
      this.leg1.xRot = Mth.cos(time * 0.6662F + (float)Math.PI) * 1.4F * r;
      this.leg2.xRot = Mth.cos(time * 0.6662F + (float)Math.PI) * 1.4F * r;
      this.leg3.xRot = Mth.cos(time * 0.6662F) * 1.4F * r;
   }

   public void render(QuadrupedModel model, float scale) {
      this.head.yRot = model.head.yRot;
      this.head.xRot = model.head.xRot;
      this.head.y = model.head.y;
      this.head.x = model.head.x;
      this.body.yRot = model.body.yRot;
      this.body.xRot = model.body.xRot;
      this.leg0.xRot = model.leg0.xRot;
      this.leg1.xRot = model.leg1.xRot;
      this.leg2.xRot = model.leg2.xRot;
      this.leg3.xRot = model.leg3.xRot;
      this.head.render(scale);
      this.body.render(scale);
      this.leg0.render(scale);
      this.leg1.render(scale);
      this.leg2.render(scale);
      this.leg3.render(scale);
   }
}
