package net.minecraft.client;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Screenshot {
   private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
   private static ByteBuffer pixels;
   private static byte[] buffer;
   private static int[] pixelBuffer;
   private int rowHeight;
   private DataOutputStream dos;
   private byte[] pb;
   private int w;
   private int h;
   private File file;

   public static String grab(File workDir, int width, int height) {
      try {
         File picDir = new File(workDir, "screenshots");
         picDir.mkdir();
         if (pixels == null || pixels.capacity() < width * height) {
            pixels = BufferUtils.createByteBuffer(width * height * 3);
         }

         if (pixelBuffer == null || pixelBuffer.length < width * height * 3) {
            buffer = new byte[width * height * 3];
            pixelBuffer = new int[width * height];
         }

         GL11.glPixelStorei(3333, 1);
         GL11.glPixelStorei(3317, 1);
         pixels.clear();
         GL11.glReadPixels(0, 0, width, height, 6407, 5121, pixels);
         pixels.clear();
         String picName = df.format(new Date());

         File file;
         for(int count = 1; (file = new File(picDir, picName + (count == 1 ? "" : "_" + count) + ".png")).exists(); ++count) {
         }

         pixels.get(buffer);

         for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
               int i = x + (height - y - 1) * width;
               int r = buffer[i * 3 + 0] & 255;
               int g = buffer[i * 3 + 1] & 255;
               int b = buffer[i * 3 + 2] & 255;
               int col = -16777216 | r << 16 | g << 8 | b;
               pixelBuffer[x + y * width] = col;
            }
         }

         BufferedImage image = new BufferedImage(width, height, 1);
         image.setRGB(0, 0, width, height, pixelBuffer, 0, width);
         ImageIO.write(image, "png", file);
         return "Saved screenshot as " + file.getName();
      } catch (Exception e) {
         e.printStackTrace();
         return "Failed to save: " + e;
      }
   }

   public Screenshot(File workDir, int w, int h, int rowHeight) throws IOException {
      this.w = w;
      this.h = h;
      this.rowHeight = rowHeight;
      File picDir = new File(workDir, "screenshots");
      picDir.mkdir();
      String picName = "huge_" + df.format(new Date());

      for(int count = 1; (this.file = new File(picDir, picName + (count == 1 ? "" : "_" + count) + ".tga")).exists(); ++count) {
      }

      byte[] header = new byte[18];
      header[2] = 2;
      header[12] = (byte)(w % 256);
      header[13] = (byte)(w / 256);
      header[14] = (byte)(h % 256);
      header[15] = (byte)(h / 256);
      header[16] = 24;
      this.pb = new byte[w * rowHeight * 3];
      this.dos = new DataOutputStream(new FileOutputStream(this.file));
      this.dos.write(header);
   }

   public void addRegion(ByteBuffer pixels, int xo, int yo, int rw, int rh) {
      int ww = rw;
      int hh = rh;
      if (rw > this.w - xo) {
         ww = this.w - xo;
      }

      if (rh > this.h - yo) {
         hh = this.h - yo;
      }

      this.rowHeight = hh;

      for(int y = 0; y < hh; ++y) {
         pixels.position((rh - hh) * rw * 3 + y * rw * 3);
         int dp = (xo + y * this.w) * 3;
         pixels.get(this.pb, dp, ww * 3);
      }

   }

   public void saveRow() throws IOException {
      this.dos.write(this.pb, 0, this.w * 3 * this.rowHeight);
   }

   public String close() throws IOException {
      this.dos.close();
      return "Saved screenshot as " + this.file.getName();
   }
}
