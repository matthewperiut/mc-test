package net.minecraft.client.model;

import util.Mth;

public class ChickenModel extends Model {
   public Cube head;
   public Cube hair;
   public Cube body;
   public Cube leg0;
   public Cube leg1;
   public Cube wing0;
   public Cube wing1;
   public Cube beak;
   public Cube redThing;

   public ChickenModel() {
      int yo = 16;
      this.head = new Cube(0, 0);
      this.head.addBox(-2.0F, -6.0F, -2.0F, 4, 6, 3, 0.0F);
      this.head.setPos(0.0F, (float)(-1 + yo), -4.0F);
      this.beak = new Cube(14, 0);
      this.beak.addBox(-2.0F, -4.0F, -4.0F, 4, 2, 2, 0.0F);
      this.beak.setPos(0.0F, (float)(-1 + yo), -4.0F);
      this.redThing = new Cube(14, 4);
      this.redThing.addBox(-1.0F, -2.0F, -3.0F, 2, 2, 2, 0.0F);
      this.redThing.setPos(0.0F, (float)(-1 + yo), -4.0F);
      this.body = new Cube(0, 9);
      this.body.addBox(-3.0F, -4.0F, -3.0F, 6, 8, 6, 0.0F);
      this.body.setPos(0.0F, (float)(0 + yo), 0.0F);
      this.leg0 = new Cube(26, 0);
      this.leg0.addBox(-1.0F, 0.0F, -3.0F, 3, 5, 3);
      this.leg0.setPos(-2.0F, (float)(3 + yo), 1.0F);
      this.leg1 = new Cube(26, 0);
      this.leg1.addBox(-1.0F, 0.0F, -3.0F, 3, 5, 3);
      this.leg1.setPos(1.0F, (float)(3 + yo), 1.0F);
      this.wing0 = new Cube(24, 13);
      this.wing0.addBox(0.0F, 0.0F, -3.0F, 1, 4, 6);
      this.wing0.setPos(-4.0F, (float)(-3 + yo), 0.0F);
      this.wing1 = new Cube(24, 13);
      this.wing1.addBox(-1.0F, 0.0F, -3.0F, 1, 4, 6);
      this.wing1.setPos(4.0F, (float)(-3 + yo), 0.0F);
   }

   public void render(float time, float r, float bob, float yRot, float xRot, float scale) {
      this.setupAnim(time, r, bob, yRot, xRot, scale);
      this.head.render(scale);
      this.beak.render(scale);
      this.redThing.render(scale);
      this.body.render(scale);
      this.leg0.render(scale);
      this.leg1.render(scale);
      this.wing0.render(scale);
      this.wing1.render(scale);
   }

   public void setupAnim(float time, float r, float bob, float yRot, float xRot, float scale) {
      this.head.xRot = -(xRot / (180F / (float)Math.PI));
      this.head.yRot = yRot / (180F / (float)Math.PI);
      this.beak.xRot = this.head.xRot;
      this.beak.yRot = this.head.yRot;
      this.redThing.xRot = this.head.xRot;
      this.redThing.yRot = this.head.yRot;
      this.body.xRot = ((float)Math.PI / 2F);
      this.leg0.xRot = Mth.cos(time * 0.6662F) * 1.4F * r;
      this.leg1.xRot = Mth.cos(time * 0.6662F + (float)Math.PI) * 1.4F * r;
      this.wing0.zRot = bob;
      this.wing1.zRot = -bob;
   }
}
