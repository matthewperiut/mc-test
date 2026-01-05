package net.minecraft.client.gui;

public class ErrorScreen extends Screen {
   private String title;
   private String message;

   public ErrorScreen(String title, String message) {
      this.title = title;
      this.message = message;
   }

   public void init() {
   }

   public void render(int xm, int ym, float a) {
      this.fillGradient(0, 0, this.width, this.height, -12574688, -11530224);
      this.drawCenteredString(this.font, this.title, this.width / 2, 90, 16777215);
      this.drawCenteredString(this.font, this.message, this.width / 2, 110, 16777215);
      super.render(xm, ym, a);
   }

   protected void keyPressed(char eventCharacter, int eventKey) {
   }
}
