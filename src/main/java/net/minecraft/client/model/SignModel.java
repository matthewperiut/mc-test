package net.minecraft.client.model;

public class SignModel {
   public Cube cube = new Cube(0, 0);
   public Cube cube2;

   public SignModel() {
      this.cube.addBox(-12.0F, -14.0F, -1.0F, 24, 12, 2, 0.0F);
      this.cube2 = new Cube(0, 14);
      this.cube2.addBox(-1.0F, -2.0F, -1.0F, 2, 14, 2, 0.0F);
   }

   public void render() {
      this.cube.render(0.0625F);
      this.cube2.render(0.0625F);
   }
}
