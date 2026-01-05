package net.minecraft.client.model;

import util.Mth;

public class SpiderModel extends Model {
   public Cube head;
   public Cube body0;
   public Cube body1;
   public Cube leg0;
   public Cube leg1;
   public Cube leg2;
   public Cube leg3;
   public Cube leg4;
   public Cube leg5;
   public Cube leg6;
   public Cube leg7;

   public SpiderModel() {
      float g = 0.0F;
      int yo = 15;
      this.head = new Cube(32, 4);
      this.head.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, g);
      this.head.setPos(0.0F, (float)(0 + yo), -3.0F);
      this.body0 = new Cube(0, 0);
      this.body0.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6, g);
      this.body0.setPos(0.0F, (float)yo, 0.0F);
      this.body1 = new Cube(0, 12);
      this.body1.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, g);
      this.body1.setPos(0.0F, (float)(0 + yo), 9.0F);
      this.leg0 = new Cube(18, 0);
      this.leg0.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, g);
      this.leg0.setPos(-4.0F, (float)(0 + yo), 2.0F);
      this.leg1 = new Cube(18, 0);
      this.leg1.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, g);
      this.leg1.setPos(4.0F, (float)(0 + yo), 2.0F);
      this.leg2 = new Cube(18, 0);
      this.leg2.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, g);
      this.leg2.setPos(-4.0F, (float)(0 + yo), 1.0F);
      this.leg3 = new Cube(18, 0);
      this.leg3.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, g);
      this.leg3.setPos(4.0F, (float)(0 + yo), 1.0F);
      this.leg4 = new Cube(18, 0);
      this.leg4.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, g);
      this.leg4.setPos(-4.0F, (float)(0 + yo), 0.0F);
      this.leg5 = new Cube(18, 0);
      this.leg5.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, g);
      this.leg5.setPos(4.0F, (float)(0 + yo), 0.0F);
      this.leg6 = new Cube(18, 0);
      this.leg6.addBox(-15.0F, -1.0F, -1.0F, 16, 2, 2, g);
      this.leg6.setPos(-4.0F, (float)(0 + yo), -1.0F);
      this.leg7 = new Cube(18, 0);
      this.leg7.addBox(-1.0F, -1.0F, -1.0F, 16, 2, 2, g);
      this.leg7.setPos(4.0F, (float)(0 + yo), -1.0F);
   }

   public void render(float time, float r, float bob, float yRot, float xRot, float scale) {
      this.setupAnim(time, r, bob, yRot, xRot, scale);
      this.head.render(scale);
      this.body0.render(scale);
      this.body1.render(scale);
      this.leg0.render(scale);
      this.leg1.render(scale);
      this.leg2.render(scale);
      this.leg3.render(scale);
      this.leg4.render(scale);
      this.leg5.render(scale);
      this.leg6.render(scale);
      this.leg7.render(scale);
   }

   public void setupAnim(float time, float r, float bob, float yRot, float xRot, float scale) {
      this.head.yRot = yRot / (180F / (float)Math.PI);
      this.head.xRot = xRot / (180F / (float)Math.PI);
      float sr = ((float)Math.PI / 4F);
      this.leg0.zRot = -sr;
      this.leg1.zRot = sr;
      this.leg2.zRot = -sr * 0.74F;
      this.leg3.zRot = sr * 0.74F;
      this.leg4.zRot = -sr * 0.74F;
      this.leg5.zRot = sr * 0.74F;
      this.leg6.zRot = -sr;
      this.leg7.zRot = sr;
      float ro = -0.0F;
      float ur = ((float)Math.PI / 8F);
      this.leg0.yRot = ur * 2.0F + ro;
      this.leg1.yRot = -ur * 2.0F - ro;
      this.leg2.yRot = ur * 1.0F + ro;
      this.leg3.yRot = -ur * 1.0F - ro;
      this.leg4.yRot = -ur * 1.0F + ro;
      this.leg5.yRot = ur * 1.0F - ro;
      this.leg6.yRot = -ur * 2.0F + ro;
      this.leg7.yRot = ur * 2.0F - ro;
      float c0 = -(Mth.cos(time * 0.6662F * 2.0F + 0.0F) * 0.4F) * r;
      float c1 = -(Mth.cos(time * 0.6662F * 2.0F + (float)Math.PI) * 0.4F) * r;
      float c2 = -(Mth.cos(time * 0.6662F * 2.0F + ((float)Math.PI / 2F)) * 0.4F) * r;
      float c3 = -(Mth.cos(time * 0.6662F * 2.0F + ((float)Math.PI * 1.5F)) * 0.4F) * r;
      float s0 = Math.abs(Mth.sin(time * 0.6662F + 0.0F) * 0.4F) * r;
      float s1 = Math.abs(Mth.sin(time * 0.6662F + (float)Math.PI) * 0.4F) * r;
      float s2 = Math.abs(Mth.sin(time * 0.6662F + ((float)Math.PI / 2F)) * 0.4F) * r;
      float s3 = Math.abs(Mth.sin(time * 0.6662F + ((float)Math.PI * 1.5F)) * 0.4F) * r;
      Cube var10000 = this.leg0;
      var10000.yRot += c0;
      var10000 = this.leg1;
      var10000.yRot += -c0;
      var10000 = this.leg2;
      var10000.yRot += c1;
      var10000 = this.leg3;
      var10000.yRot += -c1;
      var10000 = this.leg4;
      var10000.yRot += c2;
      var10000 = this.leg5;
      var10000.yRot += -c2;
      var10000 = this.leg6;
      var10000.yRot += c3;
      var10000 = this.leg7;
      var10000.yRot += -c3;
      var10000 = this.leg0;
      var10000.zRot += s0;
      var10000 = this.leg1;
      var10000.zRot += -s0;
      var10000 = this.leg2;
      var10000.zRot += s1;
      var10000 = this.leg3;
      var10000.zRot += -s1;
      var10000 = this.leg4;
      var10000.zRot += s2;
      var10000 = this.leg5;
      var10000.zRot += -s2;
      var10000 = this.leg6;
      var10000.zRot += s3;
      var10000 = this.leg7;
      var10000.zRot += -s3;
   }
}
