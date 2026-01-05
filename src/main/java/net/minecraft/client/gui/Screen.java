package net.minecraft.client.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class Screen extends GuiComponent {
   protected Minecraft minecraft;
   public int width;
   public int height;
   protected List<Button> buttons = new ArrayList();
   public boolean passEvents = false;
   protected Font font;
   private Button clickedButton = null;

   public Screen() {
   }

   public void render(int xm, int ym, float a) {
      for(int i = 0; i < this.buttons.size(); ++i) {
         Button button = (Button)this.buttons.get(i);
         button.render(this.minecraft, xm, ym);
      }

   }

   protected void keyPressed(char eventCharacter, int eventKey) {
      if (eventKey == 1) {
         this.minecraft.setScreen((Screen)null);
         this.minecraft.grabMouse();
      }

   }

   public static String getClipboard() {
      try {
         Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents((Object)null);
         if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            String text = (String)t.getTransferData(DataFlavor.stringFlavor);
            return text;
         }
      } catch (Exception var2) {
      }

      return null;
   }

   public static void setClipboard(String str) {
      try {
         StringSelection ss = new StringSelection(str);
         Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, (ClipboardOwner)null);
      } catch (Exception var2) {
      }

   }

   protected void mouseClicked(int x, int y, int buttonNum) {
      if (buttonNum == 0) {
         for(int i = 0; i < this.buttons.size(); ++i) {
            Button button = (Button)this.buttons.get(i);
            if (button.clicked(this.minecraft, x, y)) {
               this.clickedButton = button;
               this.minecraft.soundEngine.playUI("random.click", 1.0F, 1.0F);
               this.buttonClicked(button);
            }
         }
      }

   }

   protected void mouseReleased(int x, int y, int buttonNum) {
      if (this.clickedButton != null && buttonNum == 0) {
         this.clickedButton.released(x, y);
         this.clickedButton = null;
      }

   }

   protected void buttonClicked(Button button) {
   }

   public void init(Minecraft minecraft, int width, int height) {
      this.minecraft = minecraft;
      this.font = minecraft.font;
      this.width = width;
      this.height = height;
      this.buttons.clear();
      this.init();
   }

   public void setSize(int width, int height) {
      this.width = width;
      this.height = height;
   }

   public void init() {
   }

   public void updateEvents() {
      while(Mouse.next()) {
         this.mouseEvent();
      }

      while(Keyboard.next()) {
         this.keyboardEvent();
      }

   }

   public void mouseEvent() {
      if (Mouse.getEventButtonState()) {
         int xm = Mouse.getEventX() * this.width / this.minecraft.width;
         int ym = this.height - Mouse.getEventY() * this.height / this.minecraft.height - 1;
         this.mouseClicked(xm, ym, Mouse.getEventButton());
      } else {
         int xm = Mouse.getEventX() * this.width / this.minecraft.width;
         int ym = this.height - Mouse.getEventY() * this.height / this.minecraft.height - 1;
         this.mouseReleased(xm, ym, Mouse.getEventButton());
      }

   }

   public void keyboardEvent() {
      if (Keyboard.getEventKeyState()) {
         if (Keyboard.getEventKey() == 87) {
            this.minecraft.toggleFullScreen();
            return;
         }

         this.keyPressed(Keyboard.getEventCharacter(), Keyboard.getEventKey());
      }

   }

   public void tick() {
   }

   public void removed() {
   }

   public void renderBackground() {
      this.renderBackground(0);
   }

   public void renderBackground(int vo) {
      if (this.minecraft.level != null) {
         this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
      } else {
         this.renderDirtBackground(vo);
      }

   }

   public void renderDirtBackground(int vo) {
      GL11.glDisable(2896);
      GL11.glDisable(2912);
      Tesselator t = Tesselator.instance;
      GL11.glBindTexture(3553, this.minecraft.textures.loadTexture("/gui/background.png"));
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      float s = 32.0F;
      t.begin();
      t.color(4210752);
      t.vertexUV((double)0.0F, (double)this.height, (double)0.0F, (double)0.0F, (double)((float)this.height / s + (float)vo));
      t.vertexUV((double)this.width, (double)this.height, (double)0.0F, (double)((float)this.width / s), (double)((float)this.height / s + (float)vo));
      t.vertexUV((double)this.width, (double)0.0F, (double)0.0F, (double)((float)this.width / s), (double)(0 + vo));
      t.vertexUV((double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)(0 + vo));
      t.end();
   }

   public boolean isPauseScreen() {
      return true;
   }

   public void confirmResult(boolean result, int id) {
   }
}
