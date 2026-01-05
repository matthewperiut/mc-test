package net.minecraft.world.level;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class FoliageColor {
   private static final int[] pixels = new int[65536];

   public FoliageColor() {
   }

   public static int get(double var0, double var2) {
      var2 *= var0;
      int var4 = (int)(((double)1.0F - var0) * (double)255.0F);
      int var5 = (int)(((double)1.0F - var2) * (double)255.0F);
      return pixels[var5 << 8 | var4];
   }

   public static int getEvergreenColor() {
      return 6396257;
   }

   public static int getBirchColor() {
      return 8431445;
   }

   static {
      try {
         BufferedImage var0 = ImageIO.read(FoliageColor.class.getResource("/misc/foliagecolor.png"));
         var0.getRGB(0, 0, 256, 256, pixels, 0, 256);
      } catch (Exception var1) {
         var1.printStackTrace();
      }

   }
}
