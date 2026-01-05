package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.world.entity.item.Minecart;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class MinecartRenderer extends EntityRenderer<Minecart> {
   protected Model model;

   public MinecartRenderer() {
      this.shadowRadius = 0.5F;
      this.model = new MinecartModel();
   }

   public void render(Minecart cart, double x, double y, double z, float rot, float a) {
      GL11.glPushMatrix();
      double xx = cart.xOld + (cart.x - cart.xOld) * (double)a;
      double yy = cart.yOld + (cart.y - cart.yOld) * (double)a;
      double zz = cart.zOld + (cart.z - cart.zOld) * (double)a;
      double r = (double)0.3F;
      Vec3 p = cart.getPos(xx, yy, zz);
      float xRot = cart.xRotO + (cart.xRot - cart.xRotO) * a;
      if (p != null) {
         Vec3 p0 = cart.getPosOffs(xx, yy, zz, r);
         Vec3 p1 = cart.getPosOffs(xx, yy, zz, -r);
         if (p0 == null) {
            p0 = p;
         }

         if (p1 == null) {
            p1 = p;
         }

         x += p.x - xx;
         y += (p0.y + p1.y) / (double)2.0F - yy;
         z += p.z - zz;
         Vec3 dir = p1.add(-p0.x, -p0.y, -p0.z);
         if (dir.length() != (double)0.0F) {
            dir = dir.normalize();
            rot = (float)(Math.atan2(dir.z, dir.x) * (double)180.0F / Math.PI);
            xRot = (float)(Math.atan(dir.y) * (double)73.0F);
         }
      }

      GL11.glTranslatef((float)x, (float)y, (float)z);
      GL11.glRotatef(180.0F - rot, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(-xRot, 0.0F, 0.0F, 1.0F);
      float hurt = (float)cart.hurtTime - a;
      float dmg = (float)cart.damage - a;
      if (dmg < 0.0F) {
         dmg = 0.0F;
      }

      if (hurt > 0.0F) {
         GL11.glRotatef(Mth.sin(hurt) * hurt * dmg / 10.0F * (float)cart.hurtDir, 1.0F, 0.0F, 0.0F);
      }

      if (cart.type != 0) {
         this.bindTexture("/terrain.png");
         float ss = 0.75F;
         GL11.glScalef(ss, ss, ss);
         GL11.glTranslatef(0.0F, 0.3125F, 0.0F);
         GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
         if (cart.type == 1) {
            (new TileRenderer()).renderTile(Tile.chest, 0);
         } else if (cart.type == 2) {
            (new TileRenderer()).renderTile(Tile.furnace, 0);
         }

         GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
         GL11.glTranslatef(0.0F, -0.3125F, 0.0F);
         GL11.glScalef(1.0F / ss, 1.0F / ss, 1.0F / ss);
      }

      this.bindTexture("/item/cart.png");
      GL11.glScalef(-1.0F, -1.0F, 1.0F);
      this.model.render(0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
      GL11.glPopMatrix();
   }
}
