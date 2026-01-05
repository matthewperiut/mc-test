package net.minecraft.client.model;

public class MinecartModel extends Model {
   public Cube[] cubes = new Cube[7];

   public MinecartModel() {
      this.cubes[0] = new Cube(0, 10);
      this.cubes[1] = new Cube(0, 0);
      this.cubes[2] = new Cube(0, 0);
      this.cubes[3] = new Cube(0, 0);
      this.cubes[4] = new Cube(0, 0);
      this.cubes[5] = new Cube(44, 10);
      int w = 20;
      int d = 8;
      int h = 16;
      int yOff = 4;
      this.cubes[0].addBox((float)(-w / 2), (float)(-h / 2), -1.0F, w, h, 2, 0.0F);
      this.cubes[0].setPos(0.0F, (float)(0 + yOff), 0.0F);
      this.cubes[5].addBox((float)(-w / 2 + 1), (float)(-h / 2 + 1), -1.0F, w - 2, h - 2, 1, 0.0F);
      this.cubes[5].setPos(0.0F, (float)(0 + yOff), 0.0F);
      this.cubes[1].addBox((float)(-w / 2 + 2), (float)(-d - 1), -1.0F, w - 4, d, 2, 0.0F);
      this.cubes[1].setPos((float)(-w / 2 + 1), (float)(0 + yOff), 0.0F);
      this.cubes[2].addBox((float)(-w / 2 + 2), (float)(-d - 1), -1.0F, w - 4, d, 2, 0.0F);
      this.cubes[2].setPos((float)(w / 2 - 1), (float)(0 + yOff), 0.0F);
      this.cubes[3].addBox((float)(-w / 2 + 2), (float)(-d - 1), -1.0F, w - 4, d, 2, 0.0F);
      this.cubes[3].setPos(0.0F, (float)(0 + yOff), (float)(-h / 2 + 1));
      this.cubes[4].addBox((float)(-w / 2 + 2), (float)(-d - 1), -1.0F, w - 4, d, 2, 0.0F);
      this.cubes[4].setPos(0.0F, (float)(0 + yOff), (float)(h / 2 - 1));
      this.cubes[0].xRot = ((float)Math.PI / 2F);
      this.cubes[1].yRot = ((float)Math.PI * 1.5F);
      this.cubes[2].yRot = ((float)Math.PI / 2F);
      this.cubes[3].yRot = (float)Math.PI;
      this.cubes[5].xRot = (-(float)Math.PI / 2F);
   }

   public void render(float time, float r, float bob, float yRot, float xRot, float scale) {
      this.cubes[5].y = 4.0F - bob;

      for(int i = 0; i < 6; ++i) {
         this.cubes[i].render(scale);
      }

   }

   public void setupAnim(float time, float r, float bob, float yRot, float xRot, float scale) {
   }
}
