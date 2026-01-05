package net.minecraft.client.model;

public class LocalPlayerModel extends HumanoidModel {
   public LocalPlayerModel() {
      this(0.0F);
   }

   public LocalPlayerModel(float g) {
      super(g);
      this.head.neverRender = true;
   }
}
