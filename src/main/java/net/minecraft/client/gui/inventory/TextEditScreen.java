package net.minecraft.client.gui.inventory;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Button;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.renderer.tileentity.TileEntityRenderDispatcher;
import net.minecraft.network.packet.SignUpdatePacket;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.SignTileEntity;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class TextEditScreen extends Screen {
   protected String title = "Edit sign message:";
   private SignTileEntity sign;
   private int frame;
   private int line = 0;
   private static final String allowedChars;

   static {
      allowedChars = SharedConstants.acceptableLetters;
   }

   public TextEditScreen(SignTileEntity sign) {
      this.sign = sign;
   }

   public void init() {
      this.buttons.clear();
      Keyboard.enableRepeatEvents(true);
      this.buttons.add(new Button(0, this.width / 2 - 100, this.height / 4 + 120, "Done"));
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
      if (this.minecraft.level.isOnline) {
         this.minecraft.getConnection().send(new SignUpdatePacket(this.sign.x, this.sign.y, this.sign.z, this.sign.messages));
      }

   }

   public void tick() {
      ++this.frame;
   }

   protected void buttonClicked(Button button) {
      if (button.active) {
         if (button.id == 0) {
            this.sign.setChanged();
            this.minecraft.setScreen((Screen)null);
         }

      }
   }

   protected void keyPressed(char ch, int eventKey) {
      if (eventKey == 200) {
         this.line = this.line - 1 & 3;
      }

      if (eventKey == 208 || eventKey == 28) {
         this.line = this.line + 1 & 3;
      }

      if (eventKey == 14 && this.sign.messages[this.line].length() > 0) {
         this.sign.messages[this.line] = this.sign.messages[this.line].substring(0, this.sign.messages[this.line].length() - 1);
      }

      if (allowedChars.indexOf(ch) >= 0 && this.sign.messages[this.line].length() < 15) {
         String[] var10000 = this.sign.messages;
         int var10001 = this.line;
         var10000[var10001] = var10000[var10001] + ch;
      }

   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);
      GL11.glPushMatrix();
      GL11.glTranslatef((float)(this.width / 2), (float)(this.height / 2), 50.0F);
      float ss = 93.75F;
      GL11.glScalef(-ss, -ss, -ss);
      GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
      Tile tile = this.sign.getTile();
      if (tile == Tile.sign) {
         float rot = (float)(this.sign.getData() * 360) / 16.0F;
         GL11.glRotatef(rot, 0.0F, 1.0F, 0.0F);
         GL11.glTranslatef(0.0F, 0.3125F, 0.0F);
      } else {
         int face = this.sign.getData();
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

         GL11.glRotatef(rot, 0.0F, 1.0F, 0.0F);
         GL11.glTranslatef(0.0F, 0.3125F, 0.0F);
      }

      if (this.frame / 6 % 2 == 0) {
         this.sign.selectedLine = this.line;
      }

      TileEntityRenderDispatcher.instance.render(this.sign, (double)-0.5F, (double)-0.75F, (double)-0.5F, 0.0F);
      this.sign.selectedLine = -1;
      GL11.glPopMatrix();
      super.render(xm, ym, a);
   }
}
