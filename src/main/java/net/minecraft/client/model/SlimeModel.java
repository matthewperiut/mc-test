package net.minecraft.client.model;

public class SlimeModel extends Model {
   Cube cube;
   Cube eye0;
   Cube eye1;
   Cube mouth;

   public SlimeModel(int vOffs) {
      this.cube = new Cube(0, vOffs);
      this.cube.addBox(-4.0F, 16.0F, -4.0F, 8, 8, 8);
      if (vOffs > 0) {
         this.cube = new Cube(0, vOffs);
         this.cube.addBox(-3.0F, 17.0F, -3.0F, 6, 6, 6);
         this.eye0 = new Cube(32, 0);
         this.eye0.addBox(-3.25F, 18.0F, -3.5F, 2, 2, 2);
         this.eye1 = new Cube(32, 4);
         this.eye1.addBox(1.25F, 18.0F, -3.5F, 2, 2, 2);
         this.mouth = new Cube(32, 8);
         this.mouth.addBox(0.0F, 21.0F, -3.5F, 1, 1, 1);
      }

   }

   public void setupAnim(float time, float r, float bob, float yRot, float xRot, float scale) {
   }

   public void render(float time, float r, float bob, float yRot, float xRot, float scale) {
      this.setupAnim(time, r, bob, yRot, xRot, scale);
      this.cube.render(scale);
      if (this.eye0 != null) {
         this.eye0.render(scale);
         this.eye1.render(scale);
         this.mouth.render(scale);
      }

   }
}
