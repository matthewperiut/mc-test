package net.minecraft.client.gui;

import net.minecraft.SharedConstants;
import org.lwjgl.input.Keyboard;

public class ChatScreen extends Screen {
   private String message = "";
   private int frame = 0;
   private static final String allowedChars;

   static {
      allowedChars = SharedConstants.acceptableLetters;
   }

   public ChatScreen() {
   }

   public void init() {
      Keyboard.enableRepeatEvents(true);
   }

   public void removed() {
      Keyboard.enableRepeatEvents(false);
   }

   public void tick() {
      ++this.frame;
   }

   protected void keyPressed(char ch, int eventKey) {
      if (eventKey == 1) {
         this.minecraft.setScreen((Screen)null);
      } else if (eventKey == 28) {
         String msg = this.message.trim();
         if (msg.length() > 0) {
            this.minecraft.player.chat(this.message.trim());
         }

         this.minecraft.setScreen((Screen)null);
      } else {
         if (eventKey == 14 && this.message.length() > 0) {
            this.message = this.message.substring(0, this.message.length() - 1);
         }

         if (allowedChars.indexOf(ch) >= 0 && this.message.length() < 100) {
            this.message = this.message + ch;
         }

      }
   }

   public void render(int xm, int ym, float a) {
      this.fill(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
      this.drawString(this.font, "> " + this.message + (this.frame / 6 % 2 == 0 ? "_" : ""), 4, this.height - 12, 14737632);
   }

   protected void mouseClicked(int x, int y, int buttonNum) {
      if (buttonNum == 0 && this.minecraft.gui.selectedName != null) {
         if (this.message.length() > 0 && !this.message.endsWith(" ")) {
            this.message = this.message + " ";
         }

         this.message = this.message + this.minecraft.gui.selectedName;
         int maxLength = 100;
         if (this.message.length() > maxLength) {
            this.message = this.message.substring(0, maxLength);
         }
      }

   }
}
