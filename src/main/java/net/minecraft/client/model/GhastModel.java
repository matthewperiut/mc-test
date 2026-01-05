package net.minecraft.client.model;

import java.util.Random;
import util.Mth;

public class GhastModel extends Model {
   Cube body;
   Cube[] tentacles = new Cube[9];

   public GhastModel() {
      int yoffs = -16;
      this.body = new Cube(0, 0);
      this.body.addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16);
      Cube var10000 = this.body;
      var10000.y += (float)(24 + yoffs);
      Random random = new Random(1660L);

      for(int i = 0; i < this.tentacles.length; ++i) {
         this.tentacles[i] = new Cube(0, 0);
         float xo = (((float)(i % 3) - (float)(i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
         float yo = ((float)(i / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
         int len = random.nextInt(7) + 8;
         this.tentacles[i].addBox(-1.0F, 0.0F, -1.0F, 2, len, 2);
         this.tentacles[i].x = xo;
         this.tentacles[i].z = yo;
         this.tentacles[i].y = (float)(31 + yoffs);
      }

   }

   public void setupAnim(float time, float r, float bob, float yRot, float xRot, float scale) {
      for(int i = 0; i < this.tentacles.length; ++i) {
         this.tentacles[i].xRot = 0.2F * Mth.sin(bob * 0.3F + (float)i) + 0.4F;
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
