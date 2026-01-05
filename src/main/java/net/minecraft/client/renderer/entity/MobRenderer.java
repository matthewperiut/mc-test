package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.Model;
import net.minecraft.world.entity.Mob;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class MobRenderer<T extends Mob> extends EntityRenderer<T> {
   private static final int MAX_ARMOR_LAYERS = 4;
   protected Model model;
   protected Model armor;

   public MobRenderer(Model model, float shadow) {
      this.model = model;
      this.shadowRadius = shadow;
   }

   public void setArmor(Model armor) {
      this.armor = armor;
   }

   public void render(T mob, double x, double y, double z, float rot, float a) {
      GL11.glPushMatrix();
      GL11.glDisable(2884);
      this.model.attackTime = this.getAttackAnim(mob, a);
      this.model.riding = mob.isRiding();
      if (this.armor != null) {
         this.armor.riding = this.model.riding;
      }

      try {
         float bodyRot = mob.yBodyRotO + (mob.yBodyRot - mob.yBodyRotO) * a;
         float headRot = mob.yRotO + (mob.yRot - mob.yRotO) * a;
         float headRotx = mob.xRotO + (mob.xRot - mob.xRotO) * a;
         GL11.glTranslatef((float)x, (float)y, (float)z);
         float bob = this.getBob(mob, a);
         this.setupRotations(mob, bob, bodyRot, a);
         float scale = 0.0625F;
         GL11.glEnable(32826);
         GL11.glScalef(-1.0F, -1.0F, 1.0F);
         this.scale(mob, a);
         GL11.glTranslatef(0.0F, -24.0F * scale - 0.0078125F, 0.0F);
         float ws = mob.walkAnimSpeedO + (mob.walkAnimSpeed - mob.walkAnimSpeedO) * a;
         float wp = mob.walkAnimPos - mob.walkAnimSpeed * (1.0F - a);
         if (ws > 1.0F) {
            ws = 1.0F;
         }

         this.bindTexture(mob.customTextureUrl, mob.getTexture());
         GL11.glEnable(3008);
         this.model.render(wp, ws, bob, headRot - bodyRot, headRotx, scale);

         for(int i = 0; i < 4; ++i) {
            if (this.prepareArmor(mob, i, a)) {
               this.armor.render(wp, ws, bob, headRot - bodyRot, headRotx, scale);
               GL11.glDisable(3042);
               GL11.glEnable(3008);
            }
         }

         this.additionalRendering(mob, a);
         float br = mob.getBrightness(a);
         int overlayColor = this.getOverlayColor(mob, br, a);
         if ((overlayColor >> 24 & 255) > 0 || mob.hurtTime > 0 || mob.deathTime > 0) {
            GL11.glDisable(3553);
            GL11.glDisable(3008);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glDepthFunc(514);
            if (mob.hurtTime > 0 || mob.deathTime > 0) {
               GL11.glColor4f(br, 0.0F, 0.0F, 0.4F);
               this.model.render(wp, ws, bob, headRot - bodyRot, headRotx, scale);

               for(int i = 0; i < 4; ++i) {
                  if (this.prepareArmor(mob, i, a)) {
                     GL11.glColor4f(br, 0.0F, 0.0F, 0.4F);
                     this.armor.render(wp, ws, bob, headRot - bodyRot, headRotx, scale);
                  }
               }
            }

            if ((overlayColor >> 24 & 255) > 0) {
               float r = (float)(overlayColor >> 16 & 255) / 255.0F;
               float g = (float)(overlayColor >> 8 & 255) / 255.0F;
               float b = (float)(overlayColor & 255) / 255.0F;
               float aa = (float)(overlayColor >> 24 & 255) / 255.0F;
               GL11.glColor4f(r, g, b, aa);
               this.model.render(wp, ws, bob, headRot - bodyRot, headRotx, scale);

               for(int i = 0; i < 4; ++i) {
                  if (this.prepareArmor(mob, i, a)) {
                     GL11.glColor4f(r, g, b, aa);
                     this.armor.render(wp, ws, bob, headRot - bodyRot, headRotx, scale);
                  }
               }
            }

            GL11.glDepthFunc(515);
            GL11.glDisable(3042);
            GL11.glEnable(3008);
            GL11.glEnable(3553);
         }

         GL11.glDisable(32826);
      } catch (Exception e) {
         e.printStackTrace();
      }

      GL11.glEnable(2884);
      GL11.glPopMatrix();
   }

   protected void setupRotations(T mob, float bob, float bodyRot, float a) {
      GL11.glRotatef(180.0F - bodyRot, 0.0F, 1.0F, 0.0F);
      if (mob.deathTime > 0) {
         float fall = ((float)mob.deathTime + a - 1.0F) / 20.0F * 1.6F;
         fall = Mth.sqrt(fall);
         if (fall > 1.0F) {
            fall = 1.0F;
         }

         GL11.glRotatef(fall * this.getFlipDegrees(mob), 0.0F, 0.0F, 1.0F);
      }

   }

   protected float getAttackAnim(T mob, float a) {
      return mob.getAttackAnim(a);
   }

   protected float getBob(T mob, float a) {
      return (float)mob.tickCount + a;
   }

   protected void additionalRendering(T mob, float a) {
   }

   protected boolean prepareArmor(T mob, int layer, float a) {
      return false;
   }

   protected float getFlipDegrees(T mob) {
      return 90.0F;
   }

   protected int getOverlayColor(T mob, float br, float a) {
      return 0;
   }

   protected void scale(T mob, float a) {
   }
}
