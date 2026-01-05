package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.opengl.GL11;

public class TntRenderer extends EntityRenderer<PrimedTnt> {
   private TileRenderer tileRenderer = new TileRenderer();

   public TntRenderer() {
      this.shadowRadius = 0.5F;
   }

   public void render(PrimedTnt tnt, double x, double y, double z, float rot, float a) {
      GL11.glPushMatrix();
      GL11.glTranslatef((float)x, (float)y, (float)z);
      if ((float)tnt.life - a + 1.0F < 10.0F) {
         float g = 1.0F - ((float)tnt.life - a + 1.0F) / 10.0F;
         if (g < 0.0F) {
            g = 0.0F;
         }

         if (g > 1.0F) {
            g = 1.0F;
         }

         g *= g;
         g *= g;
         float s = 1.0F + g * 0.3F;
         GL11.glScalef(s, s, s);
      }

      float br = (1.0F - ((float)tnt.life - a + 1.0F) / 100.0F) * 0.8F;
      this.bindTexture("/terrain.png");
      this.tileRenderer.renderTile(Tile.tnt, 0);
      if (tnt.life / 5 % 2 == 0) {
         GL11.glDisable(3553);
         GL11.glDisable(2896);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 772);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, br);
         this.tileRenderer.renderTile(Tile.tnt, 0);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glDisable(3042);
         GL11.glEnable(2896);
         GL11.glEnable(3553);
      }

      GL11.glPopMatrix();
   }
}
