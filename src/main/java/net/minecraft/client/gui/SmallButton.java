package net.minecraft.client.gui;

import net.minecraft.client.Options;

public class SmallButton extends Button {
   private final Options.Option option;

   public SmallButton(int id, int x, int y, String msg) {
      this(id, x, y, (Options.Option)null, msg);
   }

   public SmallButton(int id, int x, int y, int width, int height, String msg) {
      super(id, x, y, width, height, msg);
      this.option = null;
   }

   public SmallButton(int id, int x, int y, Options.Option item, String msg) {
      super(id, x, y, 150, 20, msg);
      this.option = item;
   }

   public Options.Option getOption() {
      return this.option;
   }
}
