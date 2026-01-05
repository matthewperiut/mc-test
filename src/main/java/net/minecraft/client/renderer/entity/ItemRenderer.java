package net.minecraft.client.renderer.entity;

import java.util.Random;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class ItemRenderer extends EntityRenderer<ItemEntity> {
   private TileRenderer tileRenderer = new TileRenderer();
   private Random random = new Random();

   public ItemRenderer() {
      this.shadowRadius = 0.15F;
      this.shadowStrength = 0.75F;
   }

   public void render(ItemEntity itemEntity, double x, double y, double z, float rot, float a) {
      this.random.setSeed(187L);
      ItemInstance item = itemEntity.item;
      GL11.glPushMatrix();
      float bob = Mth.sin(((float)itemEntity.age + a) / 10.0F + itemEntity.bobOffs) * 0.1F + 0.1F;
      float spin = (((float)itemEntity.age + a) / 20.0F + itemEntity.bobOffs) * (180F / (float)Math.PI);
      int count = 1;
      if (itemEntity.item.count > 1) {
         count = 2;
      }

      if (itemEntity.item.count > 5) {
         count = 3;
      }

      if (itemEntity.item.count > 20) {
         count = 4;
      }

      GL11.glTranslatef((float)x, (float)y + bob, (float)z);
      GL11.glEnable(32826);
      if (item.id < 256 && TileRenderer.canRender(Tile.tiles[item.id].getRenderShape())) {
         GL11.glRotatef(spin, 0.0F, 1.0F, 0.0F);
         this.bindTexture("/terrain.png");
         float s = 0.25F;
         if (!Tile.tiles[item.id].isCubeShaped() && item.id != Tile.stoneSlabHalf.id) {
            s = 0.5F;
         }

         GL11.glScalef(s, s, s);

         for(int i = 0; i < count; ++i) {
            GL11.glPushMatrix();
            if (i > 0) {
               float xo = (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F / s;
               float yo = (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F / s;
               float zo = (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F / s;
               GL11.glTranslatef(xo, yo, zo);
            }

            this.tileRenderer.renderTile(Tile.tiles[item.id], item.getAuxValue());
            GL11.glPopMatrix();
         }
      } else {
         GL11.glScalef(0.5F, 0.5F, 0.5F);
         int icon = item.getIcon();
         if (item.id < 256) {
            this.bindTexture("/terrain.png");
         } else {
            this.bindTexture("/gui/items.png");
         }

         Tesselator t = Tesselator.instance;
         float u0 = (float)(icon % 16 * 16 + 0) / 256.0F;
         float u1 = (float)(icon % 16 * 16 + 16) / 256.0F;
         float v0 = (float)(icon / 16 * 16 + 0) / 256.0F;
         float v1 = (float)(icon / 16 * 16 + 16) / 256.0F;
         float r = 1.0F;
         float xo = 0.5F;
         float yo = 0.25F;

         for(int i = 0; i < count; ++i) {
            GL11.glPushMatrix();
            if (i > 0) {
               float _xo = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
               float _yo = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
               float _zo = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
               GL11.glTranslatef(_xo, _yo, _zo);
            }

            GL11.glRotatef(180.0F - this.entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
            t.begin();
            t.normal(0.0F, 1.0F, 0.0F);
            t.vertexUV((double)(0.0F - xo), (double)(0.0F - yo), (double)0.0F, (double)u0, (double)v1);
            t.vertexUV((double)(r - xo), (double)(0.0F - yo), (double)0.0F, (double)u1, (double)v1);
            t.vertexUV((double)(r - xo), (double)(1.0F - yo), (double)0.0F, (double)u1, (double)v0);
            t.vertexUV((double)(0.0F - xo), (double)(1.0F - yo), (double)0.0F, (double)u0, (double)v0);
            t.end();
            GL11.glPopMatrix();
         }
      }

      GL11.glDisable(32826);
      GL11.glPopMatrix();
   }

   public void renderGuiItem(Font font, Textures textures, ItemInstance item, int x, int y) {
      if (item != null) {
         if (item.id < 256 && TileRenderer.canRender(Tile.tiles[item.id].getRenderShape())) {
            int paint = item.id;
            textures.bind(textures.loadTexture("/terrain.png"));
            Tile tile = Tile.tiles[paint];
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(x - 2), (float)(y + 3), 0.0F);
            GL11.glScalef(10.0F, 10.0F, 10.0F);
            GL11.glTranslatef(1.0F, 0.5F, 8.0F);
            GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glScalef(1.0F, 1.0F, 1.0F);
            this.tileRenderer.renderTile(tile, item.getAuxValue());
            GL11.glPopMatrix();
         } else if (item.getIcon() >= 0) {
            GL11.glDisable(2896);
            if (item.id < 256) {
               textures.bind(textures.loadTexture("/terrain.png"));
            } else {
               textures.bind(textures.loadTexture("/gui/items.png"));
            }

            this.blit(x, y, item.getIcon() % 16 * 16, item.getIcon() / 16 * 16, 16, 16);
            GL11.glEnable(2896);
         }

         GL11.glEnable(2884);
      }
   }

   public void renderGuiItemDecorations(Font font, Textures textures, ItemInstance item, int x, int y) {
      if (item != null) {
         if (item.count > 1) {
            String amount = "" + item.count;
            GL11.glDisable(2896);
            GL11.glDisable(2929);
            font.drawShadow(amount, x + 19 - 2 - font.width(amount), y + 6 + 3, 16777215);
            GL11.glEnable(2896);
            GL11.glEnable(2929);
         }

         if (item.isDamaged()) {
            int p = (int)Math.round((double)13.0F - (double)item.getDamageValue() * (double)13.0F / (double)item.getMaxDamage());
            int cc = (int)Math.round((double)255.0F - (double)item.getDamageValue() * (double)255.0F / (double)item.getMaxDamage());
            GL11.glDisable(2896);
            GL11.glDisable(2929);
            GL11.glDisable(3553);
            Tesselator t = Tesselator.instance;
            int ca = 255 - cc << 16 | cc << 8;
            int cb = (255 - cc) / 4 << 16 | 16128;
            this.fillRect(t, x + 2, y + 13, 13, 2, 0);
            this.fillRect(t, x + 2, y + 13, 12, 1, cb);
            this.fillRect(t, x + 2, y + 13, p, 1, ca);
            GL11.glEnable(3553);
            GL11.glEnable(2896);
            GL11.glEnable(2929);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         }

      }
   }

   private void fillRect(Tesselator t, int x, int y, int w, int h, int c) {
      t.begin();
      t.color(c);
      t.vertex((double)(x + 0), (double)(y + 0), (double)0.0F);
      t.vertex((double)(x + 0), (double)(y + h), (double)0.0F);
      t.vertex((double)(x + w), (double)(y + h), (double)0.0F);
      t.vertex((double)(x + w), (double)(y + 0), (double)0.0F);
      t.end();
   }

   public void blit(int x, int y, int sx, int sy, int w, int h) {
      float blitOffset = 0.0F;
      float us = 0.00390625F;
      float vs = 0.00390625F;
      Tesselator t = Tesselator.instance;
      t.begin();
      t.vertexUV((double)(x + 0), (double)(y + h), (double)blitOffset, (double)((float)(sx + 0) * us), (double)((float)(sy + h) * vs));
      t.vertexUV((double)(x + w), (double)(y + h), (double)blitOffset, (double)((float)(sx + w) * us), (double)((float)(sy + h) * vs));
      t.vertexUV((double)(x + w), (double)(y + 0), (double)blitOffset, (double)((float)(sx + w) * us), (double)((float)(sy + 0) * vs));
      t.vertexUV((double)(x + 0), (double)(y + 0), (double)blitOffset, (double)((float)(sx + 0) * us), (double)((float)(sy + 0) * vs));
      t.end();
   }
}
