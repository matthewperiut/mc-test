package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.item.Boat;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class BoatRenderer extends EntityRenderer<Boat> {
   protected Model model;

   public BoatRenderer() {
      this.shadowRadius = 0.5F;
      this.model = new BoatModel();
   }

   public void render(Boat boat, double x, double y, double z, float rot, float a) {
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x, (float)y, (float)z);
      GL11.glRotatef(180.0F - rot, 0.0F, 1.0F, 0.0F);
      float hurt = (float)boat.hurtTime - a;
      float dmg = (float)boat.damage - a;
      if (dmg < 0.0F) {
         dmg = 0.0F;
      }

      if (hurt > 0.0F) {
         GL11.glRotatef(Mth.sin(hurt) * hurt * dmg / 10.0F * (float)boat.hurtDir, 1.0F, 0.0F, 0.0F);
      }

      this.bindTexture("/terrain.png");
      float ss = 0.75F;
      GL11.glScalef(ss, ss, ss);
      GL11.glScalef(1.0F / ss, 1.0F / ss, 1.0F / ss);
      this.bindTexture("/item/boat.png");
      GL11.glScalef(-1.0F, -1.0F, 1.0F);
      this.model.render(0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
      GL11.glPopMatrix();
   }
}
