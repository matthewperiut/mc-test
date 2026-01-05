package net.minecraft.client.model;

import util.Mth;

public class CreeperModel extends Model {
   public Cube head;
   public Cube hair;
   public Cube body;
   public Cube leg0;
   public Cube leg1;
   public Cube leg2;
   public Cube leg3;

   public CreeperModel() {
      float g = 0.0F;
      int yo = 4;
      this.head = new Cube(0, 0);
      this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, g);
      this.head.setPos(0.0F, (float)yo, 0.0F);
      this.hair = new Cube(32, 0);
      this.hair.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, g + 0.5F);
      this.hair.setPos(0.0F, (float)yo, 0.0F);
      this.body = new Cube(16, 16);
      this.body.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, g);
      this.body.setPos(0.0F, (float)yo, 0.0F);
      this.leg0 = new Cube(0, 16);
      this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, g);
      this.leg0.setPos(-2.0F, (float)(12 + yo), 4.0F);
      this.leg1 = new Cube(0, 16);
      this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, g);
      this.leg1.setPos(2.0F, (float)(12 + yo), 4.0F);
      this.leg2 = new Cube(0, 16);
      this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, g);
      this.leg2.setPos(-2.0F, (float)(12 + yo), -4.0F);
      this.leg3 = new Cube(0, 16);
      this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, g);
      this.leg3.setPos(2.0F, (float)(12 + yo), -4.0F);
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
      this.head.yRot = yRot / (180F / (float)Math.PI);
      this.head.xRot = xRot / (180F / (float)Math.PI);
      this.leg0.xRot = Mth.cos(time * 0.6662F) * 1.4F * r;
      this.leg1.xRot = Mth.cos(time * 0.6662F + (float)Math.PI) * 1.4F * r;
      this.leg2.xRot = Mth.cos(time * 0.6662F + (float)Math.PI) * 1.4F * r;
      this.leg3.xRot = Mth.cos(time * 0.6662F) * 1.4F * r;
   }
}
