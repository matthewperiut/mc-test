package net.minecraft.client.renderer.entity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import org.lwjgl.opengl.GL11;
import util.Mth;

public abstract class EntityRenderer<T extends Entity> {
   protected EntityRenderDispatcher entityRenderDispatcher;
   private Model model = new HumanoidModel();
   private TileRenderer tileRenderer = new TileRenderer();
   protected float shadowRadius = 0.0F;
   protected float shadowStrength = 1.0F;

   public EntityRenderer() {
   }

   public abstract void render(T var1, double var2, double var4, double var6, float var8, float var9);

   protected void bindTexture(String resourceName) {
      Textures t = this.entityRenderDispatcher.textures;
      t.bind(t.loadTexture(resourceName));
   }

   protected boolean bindTexture(String urlTexture, String backupTexture) {
      Textures t = this.entityRenderDispatcher.textures;
      int id = t.loadHttpTexture(urlTexture, backupTexture);
      if (id >= 0) {
         t.bind(id);
         return true;
      } else {
         return false;
      }
   }

   private void renderFlame(Entity e, double x, double y, double z, float a) {
      GL11.glDisable(2896);
      int tex = Tile.fire.tex;
      int xt = (tex & 15) << 4;
      int yt = tex & 240;
      float u0 = (float)xt / 256.0F;
      float u1 = ((float)xt + 15.99F) / 256.0F;
      float v0 = (float)yt / 256.0F;
      float v1 = ((float)yt + 15.99F) / 256.0F;
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x, (float)y, (float)z);
      float s = e.bbWidth * 1.4F;
      GL11.glScalef(s, s, s);
      this.bindTexture("/terrain.png");
      Tesselator t = Tesselator.instance;
      float r = 1.0F;
      float xo = 0.5F;
      float yo = 0.0F;
      float h = e.bbHeight / e.bbWidth;
      GL11.glRotatef(-this.entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
      GL11.glTranslatef(0.0F, 0.0F, -0.4F + (float)((int)h) * 0.02F);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      t.begin();

      while(h > 0.0F) {
         t.vertexUV((double)(r - xo), (double)(0.0F - yo), (double)0.0F, (double)u1, (double)v1);
         t.vertexUV((double)(0.0F - xo), (double)(0.0F - yo), (double)0.0F, (double)u0, (double)v1);
         t.vertexUV((double)(0.0F - xo), (double)(1.4F - yo), (double)0.0F, (double)u0, (double)v0);
         t.vertexUV((double)(r - xo), (double)(1.4F - yo), (double)0.0F, (double)u1, (double)v0);
         --h;
         --yo;
         r *= 0.9F;
         GL11.glTranslatef(0.0F, 0.0F, -0.04F);
      }

      t.end();
      GL11.glPopMatrix();
      GL11.glEnable(2896);
   }

   private void renderShadow(Entity e, double x, double y, double z, float pow, float a) {
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      Textures textures = this.entityRenderDispatcher.textures;
      textures.bind(textures.loadTexture("%clamp%/misc/shadow.png"));
      Level level = this.getLevel();
      GL11.glDepthMask(false);
      float r = this.shadowRadius;
      double ex = e.xOld + (e.x - e.xOld) * (double)a;
      double ey = e.yOld + (e.y - e.yOld) * (double)a + (double)e.getShadowHeightOffs();
      double ez = e.zOld + (e.z - e.zOld) * (double)a;
      int x0 = Mth.floor(ex - (double)r);
      int x1 = Mth.floor(ex + (double)r);
      int y0 = Mth.floor(ey - (double)r);
      int y1 = Mth.floor(ey);
      int z0 = Mth.floor(ez - (double)r);
      int z1 = Mth.floor(ez + (double)r);
      double xo = x - ex;
      double yo = y - ey;
      double zo = z - ez;
      Tesselator tt = Tesselator.instance;
      tt.begin();

      for(int xt = x0; xt <= x1; ++xt) {
         for(int yt = y0; yt <= y1; ++yt) {
            for(int zt = z0; zt <= z1; ++zt) {
               int t = level.getTile(xt, yt - 1, zt);
               if (t > 0 && level.getRawBrightness(xt, yt, zt) > 3) {
                  this.renderTileShadow(Tile.tiles[t], x, y + (double)e.getShadowHeightOffs(), z, xt, yt, zt, pow, r, xo, yo + (double)e.getShadowHeightOffs(), zo);
               }
            }
         }
      }

      tt.end();
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glDisable(3042);
      GL11.glDepthMask(true);
   }

   private Level getLevel() {
      return this.entityRenderDispatcher.level;
   }

   private void renderTileShadow(Tile tt, double x, double y, double z, int xt, int yt, int zt, float pow, float r, double xo, double yo, double zo) {
      Tesselator t = Tesselator.instance;
      if (tt.isCubeShaped()) {
         double a = ((double)pow - (y - ((double)yt + yo)) / (double)2.0F) * (double)0.5F * (double)this.getLevel().getBrightness(xt, yt, zt);
         if (!(a < (double)0.0F)) {
            if (a > (double)1.0F) {
               a = (double)1.0F;
            }

            t.color(1.0F, 1.0F, 1.0F, (float)a);
            double x0 = (double)xt + tt.xx0 + xo;
            double x1 = (double)xt + tt.xx1 + xo;
            double y0 = (double)yt + tt.yy0 + yo + (double)0.015625F;
            double z0 = (double)zt + tt.zz0 + zo;
            double z1 = (double)zt + tt.zz1 + zo;
            float u0 = (float)((x - x0) / (double)2.0F / (double)r + (double)0.5F);
            float u1 = (float)((x - x1) / (double)2.0F / (double)r + (double)0.5F);
            float v0 = (float)((z - z0) / (double)2.0F / (double)r + (double)0.5F);
            float v1 = (float)((z - z1) / (double)2.0F / (double)r + (double)0.5F);
            t.vertexUV(x0, y0, z0, (double)u0, (double)v0);
            t.vertexUV(x0, y0, z1, (double)u0, (double)v1);
            t.vertexUV(x1, y0, z1, (double)u1, (double)v1);
            t.vertexUV(x1, y0, z0, (double)u1, (double)v0);
         }
      }
   }

   public static void render(AABB bb, double xo, double yo, double zo) {
      GL11.glDisable(3553);
      Tesselator t = Tesselator.instance;
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      t.begin();
      t.offset(xo, yo, zo);
      t.normal(0.0F, 0.0F, -1.0F);
      t.vertex(bb.x0, bb.y1, bb.z0);
      t.vertex(bb.x1, bb.y1, bb.z0);
      t.vertex(bb.x1, bb.y0, bb.z0);
      t.vertex(bb.x0, bb.y0, bb.z0);
      t.normal(0.0F, 0.0F, 1.0F);
      t.vertex(bb.x0, bb.y0, bb.z1);
      t.vertex(bb.x1, bb.y0, bb.z1);
      t.vertex(bb.x1, bb.y1, bb.z1);
      t.vertex(bb.x0, bb.y1, bb.z1);
      t.normal(0.0F, -1.0F, 0.0F);
      t.vertex(bb.x0, bb.y0, bb.z0);
      t.vertex(bb.x1, bb.y0, bb.z0);
      t.vertex(bb.x1, bb.y0, bb.z1);
      t.vertex(bb.x0, bb.y0, bb.z1);
      t.normal(0.0F, 1.0F, 0.0F);
      t.vertex(bb.x0, bb.y1, bb.z1);
      t.vertex(bb.x1, bb.y1, bb.z1);
      t.vertex(bb.x1, bb.y1, bb.z0);
      t.vertex(bb.x0, bb.y1, bb.z0);
      t.normal(-1.0F, 0.0F, 0.0F);
      t.vertex(bb.x0, bb.y0, bb.z1);
      t.vertex(bb.x0, bb.y1, bb.z1);
      t.vertex(bb.x0, bb.y1, bb.z0);
      t.vertex(bb.x0, bb.y0, bb.z0);
      t.normal(1.0F, 0.0F, 0.0F);
      t.vertex(bb.x1, bb.y0, bb.z0);
      t.vertex(bb.x1, bb.y1, bb.z0);
      t.vertex(bb.x1, bb.y1, bb.z1);
      t.vertex(bb.x1, bb.y0, bb.z1);
      t.offset((double)0.0F, (double)0.0F, (double)0.0F);
      t.end();
      GL11.glEnable(3553);
   }

   public static void renderFlat(AABB bb) {
      Tesselator t = Tesselator.instance;
      t.begin();
      t.vertex(bb.x0, bb.y1, bb.z0);
      t.vertex(bb.x1, bb.y1, bb.z0);
      t.vertex(bb.x1, bb.y0, bb.z0);
      t.vertex(bb.x0, bb.y0, bb.z0);
      t.vertex(bb.x0, bb.y0, bb.z1);
      t.vertex(bb.x1, bb.y0, bb.z1);
      t.vertex(bb.x1, bb.y1, bb.z1);
      t.vertex(bb.x0, bb.y1, bb.z1);
      t.vertex(bb.x0, bb.y0, bb.z0);
      t.vertex(bb.x1, bb.y0, bb.z0);
      t.vertex(bb.x1, bb.y0, bb.z1);
      t.vertex(bb.x0, bb.y0, bb.z1);
      t.vertex(bb.x0, bb.y1, bb.z1);
      t.vertex(bb.x1, bb.y1, bb.z1);
      t.vertex(bb.x1, bb.y1, bb.z0);
      t.vertex(bb.x0, bb.y1, bb.z0);
      t.vertex(bb.x0, bb.y0, bb.z1);
      t.vertex(bb.x0, bb.y1, bb.z1);
      t.vertex(bb.x0, bb.y1, bb.z0);
      t.vertex(bb.x0, bb.y0, bb.z0);
      t.vertex(bb.x1, bb.y0, bb.z0);
      t.vertex(bb.x1, bb.y1, bb.z0);
      t.vertex(bb.x1, bb.y1, bb.z1);
      t.vertex(bb.x1, bb.y0, bb.z1);
      t.end();
   }

   public void init(EntityRenderDispatcher entityRenderDispatcher) {
      this.entityRenderDispatcher = entityRenderDispatcher;
   }

   public void postRender(Entity entity, double x, double y, double z, float rot, float a) {
      if (this.entityRenderDispatcher.options.fancyGraphics && this.shadowRadius > 0.0F) {
         double dist = this.entityRenderDispatcher.distanceToSqr(entity.x, entity.y, entity.z);
         float pow = (float)(((double)1.0F - dist / (double)256.0F) * (double)this.shadowStrength);
         if (pow > 0.0F) {
            this.renderShadow(entity, x, y, z, pow, a);
         }
      }

      if (entity.isOnFire()) {
         this.renderFlame(entity, x, y, z, a);
      }

   }

   public Font getFont() {
      return this.entityRenderDispatcher.getFont();
   }
}
