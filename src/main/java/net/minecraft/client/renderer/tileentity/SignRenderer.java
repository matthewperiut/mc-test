package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.SignModel;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.SignTileEntity;
import org.lwjgl.opengl.GL11;

public class SignRenderer extends TileEntityRenderer<SignTileEntity> {
   private SignModel signModel = new SignModel();

   public SignRenderer() {
   }

   public void render(SignTileEntity sign, double x, double y, double z, float a) {
      Tile tile = sign.getTile();
      GL11.glPushMatrix();
      float size = 0.6666667F;
      if (tile == Tile.sign) {
         GL11.glTranslatef((float)x + 0.5F, (float)y + 0.75F * size, (float)z + 0.5F);
         float rot = (float)(sign.getData() * 360) / 16.0F;
         GL11.glRotatef(-rot, 0.0F, 1.0F, 0.0F);
         this.signModel.cube2.visible = true;
      } else {
         int face = sign.getData();
         float rot = 0.0F;
         if (face == 2) {
            rot = 180.0F;
         }

         if (face == 4) {
            rot = 90.0F;
         }

         if (face == 5) {
            rot = -90.0F;
         }

         GL11.glTranslatef((float)x + 0.5F, (float)y + 0.75F * size, (float)z + 0.5F);
         GL11.glRotatef(-rot, 0.0F, 1.0F, 0.0F);
         GL11.glTranslatef(0.0F, -0.3125F, -0.4375F);
         this.signModel.cube2.visible = false;
      }

      this.bindTexture("/item/sign.png");
      GL11.glPushMatrix();
      GL11.glScalef(size, -size, -size);
      this.signModel.render();
      GL11.glPopMatrix();
      Font font = this.getFont();
      float s = 0.016666668F * size;
      GL11.glTranslatef(0.0F, 0.5F * size, 0.07F * size);
      GL11.glScalef(s, -s, s);
      GL11.glNormal3f(0.0F, 0.0F, -1.0F * s);
      GL11.glDepthMask(false);
      int col = 0;

      for(int i = 0; i < sign.messages.length; ++i) {
         String msg = sign.messages[i];
         if (i == sign.selectedLine) {
            msg = "> " + msg + " <";
            font.draw(msg, -font.width(msg) / 2, i * 10 - sign.messages.length * 5, col);
         } else {
            font.draw(msg, -font.width(msg) / 2, i * 10 - sign.messages.length * 5, col);
         }
      }

      GL11.glDepthMask(true);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPopMatrix();
   }
}
