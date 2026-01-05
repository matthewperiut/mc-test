package net.minecraft.client.model;

public abstract class Model {
   public float attackTime;
   public boolean riding = false;

   public Model() {
   }

   public void render(float time, float r, float bob, float yRot, float xRot, float scale) {
   }

   public void setupAnim(float time, float r, float bob, float yRot, float xRot, float scale) {
   }
}
