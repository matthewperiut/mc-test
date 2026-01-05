package net.minecraft.client.gui;

public class ConfirmScreen extends Screen {
   private Screen parent;
   private String title1;
   private String title2;
   private int id;

   public ConfirmScreen(Screen parent, String title1, String title2, int id) {
      this.parent = parent;
      this.title1 = title1;
      this.title2 = title2;
      this.id = id;
   }

   public void init() {
      this.buttons.add(new SmallButton(0, this.width / 2 - 155 + 0, this.height / 6 + 96, "Yes"));
      this.buttons.add(new SmallButton(1, this.width / 2 - 155 + 160, this.height / 6 + 96, "No"));
   }

   protected void buttonClicked(Button button) {
      this.parent.confirmResult(button.id == 0, this.id);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(this.font, this.title1, this.width / 2, 70, 16777215);
      this.drawCenteredString(this.font, this.title2, this.width / 2, 90, 16777215);
      super.render(xm, ym, a);
   }
}
