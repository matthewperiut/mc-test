package net.minecraft.client.model;

public class BoatModel extends Model {
   public Cube[] cubes = new Cube[5];

   public BoatModel() {
      this.cubes[0] = new Cube(0, 8);
      this.cubes[1] = new Cube(0, 0);
      this.cubes[2] = new Cube(0, 0);
      this.cubes[3] = new Cube(0, 0);
      this.cubes[4] = new Cube(0, 0);
      int w = 24;
      int d = 6;
      int h = 20;
      int yOff = 4;
      this.cubes[0].addBox((float)(-w / 2), (float)(-h / 2 + 2), -3.0F, w, h - 4, 4, 0.0F);
      this.cubes[0].setPos(0.0F, (float)(0 + yOff), 0.0F);
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
   }

   public void render(float time, float r, float bob, float yRot, float xRot, float scale) {
      for(int i = 0; i < 5; ++i) {
         this.cubes[i].render(scale);
      }

   }

   public void setupAnim(float time, float r, float bob, float yRot, float xRot, float scale) {
   }
}
