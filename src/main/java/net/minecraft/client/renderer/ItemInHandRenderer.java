package net.minecraft.client.renderer;

import net.minecraft.client.Lighting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class ItemInHandRenderer {
   private Minecraft mc;
   private ItemInstance selectedItem = null;
   private float height = 0.0F;
   private float oHeight = 0.0F;
   private TileRenderer tileRenderer = new TileRenderer();
   private int lastSlot = -1;

   public ItemInHandRenderer(Minecraft mc) {
      this.mc = mc;
   }

   public void renderItem(ItemInstance item) {
      GL11.glPushMatrix();
      if (item.id < 256 && TileRenderer.canRender(Tile.tiles[item.id].getRenderShape())) {
         GL11.glBindTexture(3553, this.mc.textures.loadTexture("/terrain.png"));
         this.tileRenderer.renderTile(Tile.tiles[item.id], item.getAuxValue());
      } else {
         if (item.id < 256) {
            GL11.glBindTexture(3553, this.mc.textures.loadTexture("/terrain.png"));
         } else {
            GL11.glBindTexture(3553, this.mc.textures.loadTexture("/gui/items.png"));
         }

         Tesselator t = Tesselator.instance;
         float u1 = ((float)(item.getIcon() % 16 * 16) + 0.0F) / 256.0F;
         float u0 = ((float)(item.getIcon() % 16 * 16) + 15.99F) / 256.0F;
         float v0 = ((float)(item.getIcon() / 16 * 16) + 0.0F) / 256.0F;
         float v1 = ((float)(item.getIcon() / 16 * 16) + 15.99F) / 256.0F;
         float r = 1.0F;
         float xo = 0.0F;
         float yo = 0.3F;
         GL11.glEnable(32826);
         GL11.glTranslatef(-xo, -yo, 0.0F);
         float s = 1.5F;
         GL11.glScalef(s, s, s);
         GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
         GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
         float dd = 0.0625F;
         t.begin();
         t.normal(0.0F, 0.0F, 1.0F);
         t.vertexUV((double)0.0F, (double)0.0F, (double)0.0F, (double)u0, (double)v1);
         t.vertexUV((double)r, (double)0.0F, (double)0.0F, (double)u1, (double)v1);
         t.vertexUV((double)r, (double)1.0F, (double)0.0F, (double)u1, (double)v0);
         t.vertexUV((double)0.0F, (double)1.0F, (double)0.0F, (double)u0, (double)v0);
         t.end();
         t.begin();
         t.normal(0.0F, 0.0F, -1.0F);
         t.vertexUV((double)0.0F, (double)1.0F, (double)(0.0F - dd), (double)u0, (double)v0);
         t.vertexUV((double)r, (double)1.0F, (double)(0.0F - dd), (double)u1, (double)v0);
         t.vertexUV((double)r, (double)0.0F, (double)(0.0F - dd), (double)u1, (double)v1);
         t.vertexUV((double)0.0F, (double)0.0F, (double)(0.0F - dd), (double)u0, (double)v1);
         t.end();
         t.begin();
         t.normal(-1.0F, 0.0F, 0.0F);

         for(int i = 0; i < 16; ++i) {
            float p = (float)i / 16.0F;
            float uu = u0 + (u1 - u0) * p - 0.001953125F;
            float xx = r * p;
            t.vertexUV((double)xx, (double)0.0F, (double)(0.0F - dd), (double)uu, (double)v1);
            t.vertexUV((double)xx, (double)0.0F, (double)0.0F, (double)uu, (double)v1);
            t.vertexUV((double)xx, (double)1.0F, (double)0.0F, (double)uu, (double)v0);
            t.vertexUV((double)xx, (double)1.0F, (double)(0.0F - dd), (double)uu, (double)v0);
         }

         t.end();
         t.begin();
         t.normal(1.0F, 0.0F, 0.0F);

         for(int i = 0; i < 16; ++i) {
            float p = (float)i / 16.0F;
            float uu = u0 + (u1 - u0) * p - 0.001953125F;
            float xx = r * p + 0.0625F;
            t.vertexUV((double)xx, (double)1.0F, (double)(0.0F - dd), (double)uu, (double)v0);
            t.vertexUV((double)xx, (double)1.0F, (double)0.0F, (double)uu, (double)v0);
            t.vertexUV((double)xx, (double)0.0F, (double)0.0F, (double)uu, (double)v1);
            t.vertexUV((double)xx, (double)0.0F, (double)(0.0F - dd), (double)uu, (double)v1);
         }

         t.end();
         t.begin();
         t.normal(0.0F, 1.0F, 0.0F);

         for(int i = 0; i < 16; ++i) {
            float p = (float)i / 16.0F;
            float vv = v1 + (v0 - v1) * p - 0.001953125F;
            float yy = r * p + 0.0625F;
            t.vertexUV((double)0.0F, (double)yy, (double)0.0F, (double)u0, (double)vv);
            t.vertexUV((double)r, (double)yy, (double)0.0F, (double)u1, (double)vv);
            t.vertexUV((double)r, (double)yy, (double)(0.0F - dd), (double)u1, (double)vv);
            t.vertexUV((double)0.0F, (double)yy, (double)(0.0F - dd), (double)u0, (double)vv);
         }

         t.end();
         t.begin();
         t.normal(0.0F, -1.0F, 0.0F);

         for(int i = 0; i < 16; ++i) {
            float p = (float)i / 16.0F;
            float vv = v1 + (v0 - v1) * p - 0.001953125F;
            float yy = r * p;
            t.vertexUV((double)r, (double)yy, (double)0.0F, (double)u1, (double)vv);
            t.vertexUV((double)0.0F, (double)yy, (double)0.0F, (double)u0, (double)vv);
            t.vertexUV((double)0.0F, (double)yy, (double)(0.0F - dd), (double)u0, (double)vv);
            t.vertexUV((double)r, (double)yy, (double)(0.0F - dd), (double)u1, (double)vv);
         }

         t.end();
         GL11.glDisable(32826);
      }

      GL11.glPopMatrix();
   }

   public void render(float a) {
      float h = this.oHeight + (this.height - this.oHeight) * a;
      Player player = this.mc.player;
      GL11.glPushMatrix();
      GL11.glRotatef(player.xRotO + (player.xRot - player.xRotO) * a, 1.0F, 0.0F, 0.0F);
      GL11.glRotatef(player.yRotO + (player.yRot - player.yRotO) * a, 0.0F, 1.0F, 0.0F);
      Lighting.turnOn();
      GL11.glPopMatrix();
      float br = this.mc.level.getBrightness(Mth.floor(player.x), Mth.floor(player.y), Mth.floor(player.z));
      GL11.glColor4f(br, br, br, 1.0F);
      ItemInstance item = this.selectedItem;
      if (player.fishing != null) {
         item = new ItemInstance(Item.stick);
      }

      if (item != null) {
         GL11.glPushMatrix();
         float d = 0.8F;
         float swing = player.getAttackAnim(a);
         float swing1 = Mth.sin(swing * (float)Math.PI);
         float swing2 = Mth.sin(Mth.sqrt(swing) * (float)Math.PI);
         GL11.glTranslatef(-swing2 * 0.4F, Mth.sin(Mth.sqrt(swing) * (float)Math.PI * 2.0F) * 0.2F, -swing1 * 0.2F);
         GL11.glTranslatef(0.7F * d, -0.65F * d - (1.0F - h) * 0.6F, -0.9F * d);
         GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
         GL11.glEnable(32826);
         swing = player.getAttackAnim(a);
         swing1 = Mth.sin(swing * swing * (float)Math.PI);
         swing2 = Mth.sin(Mth.sqrt(swing) * (float)Math.PI);
         GL11.glRotatef(-swing1 * 20.0F, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(-swing2 * 20.0F, 0.0F, 0.0F, 1.0F);
         GL11.glRotatef(-swing2 * 80.0F, 1.0F, 0.0F, 0.0F);
         swing = 0.4F;
         GL11.glScalef(swing, swing, swing);
         if (item.getItem().isMirroredArt()) {
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
         }

         this.renderItem(item);
         GL11.glPopMatrix();
      } else {
         GL11.glPushMatrix();
         float d = 0.8F;
         float swing = player.getAttackAnim(a);
         float swing1 = Mth.sin(swing * (float)Math.PI);
         float swing2 = Mth.sin(Mth.sqrt(swing) * (float)Math.PI);
         GL11.glTranslatef(-swing2 * 0.3F, Mth.sin(Mth.sqrt(swing) * (float)Math.PI * 2.0F) * 0.4F, -swing1 * 0.4F);
         GL11.glTranslatef(0.8F * d, -0.75F * d - (1.0F - h) * 0.6F, -0.9F * d);
         GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
         GL11.glEnable(32826);
         swing = player.getAttackAnim(a);
         swing1 = Mth.sin(swing * swing * (float)Math.PI);
         swing2 = Mth.sin(Mth.sqrt(swing) * (float)Math.PI);
         GL11.glRotatef(swing2 * 70.0F, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(-swing1 * 20.0F, 0.0F, 0.0F, 1.0F);
         GL11.glBindTexture(3553, this.mc.textures.loadHttpTexture(this.mc.player.customTextureUrl, this.mc.player.getTexture()));
         GL11.glTranslatef(-1.0F, 3.6F, 3.5F);
         GL11.glRotatef(120.0F, 0.0F, 0.0F, 1.0F);
         GL11.glRotatef(200.0F, 1.0F, 0.0F, 0.0F);
         GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
         GL11.glScalef(1.0F, 1.0F, 1.0F);
         GL11.glTranslatef(5.6F, 0.0F, 0.0F);
         EntityRenderer<Player> er = EntityRenderDispatcher.instance.getRenderer(this.mc.player);
         PlayerRenderer playerRenderer = (PlayerRenderer)er;
         swing2 = 1.0F;
         GL11.glScalef(swing2, swing2, swing2);
         playerRenderer.renderHand();
         GL11.glPopMatrix();
      }

      GL11.glDisable(32826);
      Lighting.turnOff();
   }

   public void renderScreenEffect(float a) {
      GL11.glDisable(3008);
      if (this.mc.player.isOnFire()) {
         int id = this.mc.textures.loadTexture("/terrain.png");
         GL11.glBindTexture(3553, id);
         this.renderFire(a);
      }

      if (this.mc.player.isInWall()) {
         int x = Mth.floor(this.mc.player.x);
         int y = Mth.floor(this.mc.player.y);
         int z = Mth.floor(this.mc.player.z);
         int id = this.mc.textures.loadTexture("/terrain.png");
         GL11.glBindTexture(3553, id);
         int tile = this.mc.level.getTile(x, y, z);
         if (Tile.tiles[tile] != null) {
            this.renderTex(a, Tile.tiles[tile].getTexture(2));
         }
      }

      if (this.mc.player.isUnderLiquid(Material.water)) {
         int id = this.mc.textures.loadTexture("/misc/water.png");
         GL11.glBindTexture(3553, id);
         this.renderWater(a);
      }

      GL11.glEnable(3008);
   }

   private void renderTex(float a, int tex) {
      Tesselator t = Tesselator.instance;
      this.mc.player.getBrightness(a);
      float br = 0.1F;
      GL11.glColor4f(br, br, br, 0.5F);
      GL11.glPushMatrix();
      float x0 = -1.0F;
      float x1 = 1.0F;
      float y0 = -1.0F;
      float y1 = 1.0F;
      float z0 = -0.5F;
      float r = 0.0078125F;
      float u0 = (float)(tex % 16) / 256.0F - r;
      float u1 = ((float)(tex % 16) + 15.99F) / 256.0F + r;
      float v0 = (float)(tex / 16) / 256.0F - r;
      float v1 = ((float)(tex / 16) + 15.99F) / 256.0F + r;
      t.begin();
      t.vertexUV((double)x0, (double)y0, (double)z0, (double)u1, (double)v1);
      t.vertexUV((double)x1, (double)y0, (double)z0, (double)u0, (double)v1);
      t.vertexUV((double)x1, (double)y1, (double)z0, (double)u0, (double)v0);
      t.vertexUV((double)x0, (double)y1, (double)z0, (double)u1, (double)v0);
      t.end();
      GL11.glPopMatrix();
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderWater(float a) {
      Tesselator t = Tesselator.instance;
      float br = this.mc.player.getBrightness(a);
      GL11.glColor4f(br, br, br, 0.5F);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glPushMatrix();
      float size = 4.0F;
      float x0 = -1.0F;
      float x1 = 1.0F;
      float y0 = -1.0F;
      float y1 = 1.0F;
      float z0 = -0.5F;
      float uo = -this.mc.player.yRot / 64.0F;
      float vo = this.mc.player.xRot / 64.0F;
      t.begin();
      t.vertexUV((double)x0, (double)y0, (double)z0, (double)(size + uo), (double)(size + vo));
      t.vertexUV((double)x1, (double)y0, (double)z0, (double)(0.0F + uo), (double)(size + vo));
      t.vertexUV((double)x1, (double)y1, (double)z0, (double)(0.0F + uo), (double)(0.0F + vo));
      t.vertexUV((double)x0, (double)y1, (double)z0, (double)(size + uo), (double)(0.0F + vo));
      t.end();
      GL11.glPopMatrix();
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glDisable(3042);
   }

   private void renderFire(float a) {
      Tesselator t = Tesselator.instance;
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.9F);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      float size = 1.0F;

      for(int i = 0; i < 2; ++i) {
         GL11.glPushMatrix();
         int tex = Tile.fire.tex + i * 16;
         int xt = (tex & 15) << 4;
         int yt = tex & 240;
         float u0 = (float)xt / 256.0F;
         float u1 = ((float)xt + 15.99F) / 256.0F;
         float v0 = (float)yt / 256.0F;
         float v1 = ((float)yt + 15.99F) / 256.0F;
         float x0 = (0.0F - size) / 2.0F;
         float x1 = x0 + size;
         float y0 = 0.0F - size / 2.0F;
         float y1 = y0 + size;
         float z0 = -0.5F;
         GL11.glTranslatef((float)(-(i * 2 - 1)) * 0.24F, -0.3F, 0.0F);
         GL11.glRotatef((float)(i * 2 - 1) * 10.0F, 0.0F, 1.0F, 0.0F);
         t.begin();
         t.vertexUV((double)x0, (double)y0, (double)z0, (double)u1, (double)v1);
         t.vertexUV((double)x1, (double)y0, (double)z0, (double)u0, (double)v1);
         t.vertexUV((double)x1, (double)y1, (double)z0, (double)u0, (double)v0);
         t.vertexUV((double)x0, (double)y1, (double)z0, (double)u1, (double)v0);
         t.end();
         GL11.glPopMatrix();
      }

      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glDisable(3042);
   }

   public void tick() {
      this.oHeight = this.height;
      Player player = this.mc.player;
      ItemInstance selected = player.inventory.getSelected();
      boolean matches = this.lastSlot == player.inventory.selected && selected == this.selectedItem;
      if (this.selectedItem == null && selected == null) {
         matches = true;
      }

      if (selected != null && this.selectedItem != null && selected != this.selectedItem && selected.id == this.selectedItem.id) {
         this.selectedItem = selected;
         matches = true;
      }

      float max = 0.4F;
      float tHeight = (float)(matches ? 1 : 0);
      float dd = tHeight - this.height;
      if (dd < -max) {
         dd = -max;
      }

      if (dd > max) {
         dd = max;
      }

      this.height += dd;
      if (this.height < 0.1F) {
         this.selectedItem = selected;
         this.lastSlot = player.inventory.selected;
      }

   }

   public void itemPlaced() {
      this.height = 0.0F;
   }

   public void itemUsed() {
      this.height = 0.0F;
   }
}
