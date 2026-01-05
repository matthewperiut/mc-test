package net.minecraft.client.model;

public class SquidModel extends Model {
   Cube body;
   Cube[] tentacles = new Cube[8];

   public SquidModel() {
      int yoffs = -16;
      this.body = new Cube(0, 0);
      this.body.addBox(-6.0F, -8.0F, -6.0F, 12, 16, 12);
      Cube var10000 = this.body;
      var10000.y += (float)(24 + yoffs);

      for(int i = 0; i < this.tentacles.length; ++i) {
         this.tentacles[i] = new Cube(48, 0);
         double angle = (double)i * Math.PI * (double)2.0F / (double)this.tentacles.length;
         float xo = (float)Math.cos(angle) * 5.0F;
         float yo = (float)Math.sin(angle) * 5.0F;
         this.tentacles[i].addBox(-1.0F, 0.0F, -1.0F, 2, 18, 2);
         this.tentacles[i].x = xo;
         this.tentacles[i].z = yo;
         this.tentacles[i].y = (float)(31 + yoffs);
         angle = (double)i * Math.PI * (double)-2.0F / (double)this.tentacles.length + (Math.PI / 2D);
         this.tentacles[i].yRot = (float)angle;
      }

   }

   public void setupAnim(float time, float r, float bob, float yRot, float xRot, float scale) {
      for(int i = 0; i < this.tentacles.length; ++i) {
         this.tentacles[i].xRot = bob;
      }

   }

   public void render(float time, float r, float bob, float yRot, float xRot, float scale) {
      this.setupAnim(time, r, bob, yRot, xRot, scale);
      this.body.render(scale);

      for(int i = 0; i < this.tentacles.length; ++i) {
         this.tentacles[i].render(scale);
      }

   }
}
