package net.minecraft.client;

public class GuiMessage {
   public String string;
   public int ticks;

   public GuiMessage(String string) {
      this.string = string;
      this.ticks = 0;
   }
}
