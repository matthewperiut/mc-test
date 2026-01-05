package net.minecraft.client.renderer.entity;

import java.util.Random;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.entity.Painting;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class PaintingRenderer extends EntityRenderer<Painting> {
   private Random random = new Random();

   public PaintingRenderer() {
   }

   public void render(Painting painting, double x, double y, double z, float rot, float a) {
      this.random.setSeed(187L);
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x, (float)y, (float)z);
      GL11.glRotatef(rot, 0.0F, 1.0F, 0.0F);
      GL11.glEnable(32826);
      this.bindTexture("/art/kz.png");
      Painting.Motive motive = painting.motive;
      float s = 0.0625F;
      GL11.glScalef(s, s, s);
      this.renderPainting(painting, motive.w, motive.h, motive.uo, motive.vo);
      GL11.glDisable(32826);
      GL11.glPopMatrix();
   }

   private void renderPainting(Painting painting, int w, int h, int uo, int vo) {
      float xx0 = (float)(-w) / 2.0F;
      float yy0 = (float)(-h) / 2.0F;
      float z0 = -0.5F;
      float z1 = 0.5F;

      for(int xs = 0; xs < w / 16; ++xs) {
         for(int ys = 0; ys < h / 16; ++ys) {
            float x0 = xx0 + (float)((xs + 1) * 16);
            float x1 = xx0 + (float)(xs * 16);
            float y0 = yy0 + (float)((ys + 1) * 16);
            float y1 = yy0 + (float)(ys * 16);
            this.setBrightness(painting, (x0 + x1) / 2.0F, (y0 + y1) / 2.0F);
            float fu0 = (float)(uo + w - xs * 16) / 256.0F;
            float fu1 = (float)(uo + w - (xs + 1) * 16) / 256.0F;
            float fv0 = (float)(vo + h - ys * 16) / 256.0F;
            float fv1 = (float)(vo + h - (ys + 1) * 16) / 256.0F;
            float bu0 = 0.75F;
            float bu1 = 0.8125F;
            float bv0 = 0.0F;
            float bv1 = 0.0625F;
            float uu0 = 0.75F;
            float uu1 = 0.8125F;
            float uv0 = 0.001953125F;
            float uv1 = 0.001953125F;
            float su0 = 0.7519531F;
            float su1 = 0.7519531F;
            float sv0 = 0.0F;
            float sv1 = 0.0625F;
            Tesselator t = Tesselator.instance;
            t.begin();
            t.normal(0.0F, 0.0F, -1.0F);
            t.vertexUV((double)x0, (double)y1, (double)z0, (double)fu1, (double)fv0);
            t.vertexUV((double)x1, (double)y1, (double)z0, (double)fu0, (double)fv0);
            t.vertexUV((double)x1, (double)y0, (double)z0, (double)fu0, (double)fv1);
            t.vertexUV((double)x0, (double)y0, (double)z0, (double)fu1, (double)fv1);
            t.normal(0.0F, 0.0F, 1.0F);
            t.vertexUV((double)x0, (double)y0, (double)z1, (double)bu0, (double)bv0);
            t.vertexUV((double)x1, (double)y0, (double)z1, (double)bu1, (double)bv0);
            t.vertexUV((double)x1, (double)y1, (double)z1, (double)bu1, (double)bv1);
            t.vertexUV((double)x0, (double)y1, (double)z1, (double)bu0, (double)bv1);
            t.normal(0.0F, -1.0F, 0.0F);
            t.vertexUV((double)x0, (double)y0, (double)z0, (double)uu0, (double)uv0);
            t.vertexUV((double)x1, (double)y0, (double)z0, (double)uu1, (double)uv0);
            t.vertexUV((double)x1, (double)y0, (double)z1, (double)uu1, (double)uv1);
            t.vertexUV((double)x0, (double)y0, (double)z1, (double)uu0, (double)uv1);
            t.normal(0.0F, 1.0F, 0.0F);
            t.vertexUV((double)x0, (double)y1, (double)z1, (double)uu0, (double)uv0);
            t.vertexUV((double)x1, (double)y1, (double)z1, (double)uu1, (double)uv0);
            t.vertexUV((double)x1, (double)y1, (double)z0, (double)uu1, (double)uv1);
            t.vertexUV((double)x0, (double)y1, (double)z0, (double)uu0, (double)uv1);
            t.normal(-1.0F, 0.0F, 0.0F);
            t.vertexUV((double)x0, (double)y0, (double)z1, (double)su1, (double)sv0);
            t.vertexUV((double)x0, (double)y1, (double)z1, (double)su1, (double)sv1);
            t.vertexUV((double)x0, (double)y1, (double)z0, (double)su0, (double)sv1);
            t.vertexUV((double)x0, (double)y0, (double)z0, (double)su0, (double)sv0);
            t.normal(1.0F, 0.0F, 0.0F);
            t.vertexUV((double)x1, (double)y0, (double)z0, (double)su1, (double)sv0);
            t.vertexUV((double)x1, (double)y1, (double)z0, (double)su1, (double)sv1);
            t.vertexUV((double)x1, (double)y1, (double)z1, (double)su0, (double)sv1);
            t.vertexUV((double)x1, (double)y0, (double)z1, (double)su0, (double)sv0);
            t.end();
         }
      }

   }

   private void setBrightness(Painting painting, float ss, float ya) {
      int x = Mth.floor(painting.x);
      int y = Mth.floor(painting.y + (double)(ya / 16.0F));
      int z = Mth.floor(painting.z);
      if (painting.dir == 0) {
         x = Mth.floor(painting.x + (double)(ss / 16.0F));
      }

      if (painting.dir == 1) {
         z = Mth.floor(painting.z - (double)(ss / 16.0F));
      }

      if (painting.dir == 2) {
         x = Mth.floor(painting.x - (double)(ss / 16.0F));
      }

      if (painting.dir == 3) {
         z = Mth.floor(painting.z + (double)(ss / 16.0F));
      }

      float br = this.entityRenderDispatcher.level.getBrightness(x, y, z);
      GL11.glColor3f(br, br, br);
   }
}
