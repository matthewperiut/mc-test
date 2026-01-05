package net.minecraft.client.gui.inventory;

import net.minecraft.client.Lighting;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.opengl.GL11;

public class InventoryScreen extends AbstractContainerScreen {
   private float xMouse;
   private float yMouse;

   public InventoryScreen(Player player) {
      super(player.inventoryMenu);
      this.passEvents = true;
   }

   protected void renderLabels() {
      this.font.draw("Crafting", 86, 16, 4210752);
   }

   public void render(int xm, int ym, float a) {
      super.render(xm, ym, a);
      this.xMouse = (float)xm;
      this.yMouse = (float)ym;
   }

   protected void renderBg(float a) {
      int tex = this.minecraft.textures.loadTexture("/gui/inventory.png");
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.textures.bind(tex);
      int xo = (this.width - this.imageWidth) / 2;
      int yo = (this.height - this.imageHeight) / 2;
      this.blit(xo, yo, 0, 0, this.imageWidth, this.imageHeight);
      GL11.glEnable(32826);
      GL11.glEnable(2903);
      GL11.glPushMatrix();
      GL11.glTranslatef((float)(xo + 51), (float)(yo + 75), 50.0F);
      float ss = 30.0F;
      GL11.glScalef(-ss, ss, ss);
      GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
      float oybr = this.minecraft.player.yBodyRot;
      float oyr = this.minecraft.player.yRot;
      float oxr = this.minecraft.player.xRot;
      float xd = (float)(xo + 51) - this.xMouse;
      float yd = (float)(yo + 75 - 50) - this.yMouse;
      GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
      Lighting.turnOn();
      GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(-((float)Math.atan((double)(yd / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
      this.minecraft.player.yBodyRot = (float)Math.atan((double)(xd / 40.0F)) * 20.0F;
      this.minecraft.player.yRot = (float)Math.atan((double)(xd / 40.0F)) * 40.0F;
      this.minecraft.player.xRot = -((float)Math.atan((double)(yd / 40.0F))) * 20.0F;
      GL11.glTranslatef(0.0F, this.minecraft.player.heightOffset, 0.0F);
      EntityRenderDispatcher.instance.render(this.minecraft.player, (double)0.0F, (double)0.0F, (double)0.0F, 0.0F, 1.0F);
      this.minecraft.player.yBodyRot = oybr;
      this.minecraft.player.yRot = oyr;
      this.minecraft.player.xRot = oxr;
      GL11.glPopMatrix();
      Lighting.turnOff();
      GL11.glDisable(32826);
   }
}
