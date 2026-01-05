package net.minecraft.client;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;

public class CrashInfoPanel extends Panel {
   private static final long serialVersionUID = 1L;

   public CrashInfoPanel(CrashReport report) {
      this.setBackground(new Color(3028036));
      this.setLayout(new BorderLayout());
      StringWriter sw = new StringWriter();
      report.e.printStackTrace(new PrintWriter(sw));
      String stacktrace = sw.toString();
      String vendor = "";
      String msg = "";

      try {
         msg = msg + "Generated " + (new SimpleDateFormat()).format(new Date()) + "\n";
         msg = msg + "\n";
         msg = msg + "Minecraft: Minecraft Beta 1.2_02\n";
         msg = msg + "OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version") + "\n";
         msg = msg + "Java: " + System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + "\n";
         msg = msg + "VM: " + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor") + "\n";
         msg = msg + "LWJGL: " + Sys.getVersion() + "\n";
         vendor = GL11.glGetString(7936);
         msg = msg + "OpenGL: " + GL11.glGetString(7937) + " version " + GL11.glGetString(7938) + ", " + GL11.glGetString(7936) + "\n";
      } catch (Throwable e) {
         msg = msg + "[failed to get system properties (" + e + ")]\n";
      }

      msg = msg + "\n";
      msg = msg + stacktrace;
      String text = "";
      text = text + "\n";
      text = text + "\n";
      if (stacktrace.contains("Pixel format not accelerated")) {
         text = text + "      Bad video card drivers!      \n";
         text = text + "      -----------------------      \n";
         text = text + "\n";
         text = text + "Minecraft was unable to start because it failed to find an accelerated OpenGL mode.\n";
         text = text + "This can usually be fixed by updating the video card drivers.\n";
         if (vendor.toLowerCase().contains("nvidia")) {
            text = text + "\n";
            text = text + "You might be able to find drivers for your video card here:\n";
            text = text + "  http://www.nvidia.com/\n";
         } else if (vendor.toLowerCase().contains("ati")) {
            text = text + "\n";
            text = text + "You might be able to find drivers for your video card here:\n";
            text = text + "  http://www.amd.com/\n";
         }
      } else {
         text = text + "      Minecraft has crashed!      \n";
         text = text + "      ----------------------      \n";
         text = text + "\n";
         text = text + "Minecraft has stopped running because it encountered a problem.\n";
         text = text + "\n";
         text = text + "If you wish to report this, please copy this entire text and email it to support@mojang.com.\n";
         text = text + "Please include a description of what you did when the error occured.\n";
      }

      text = text + "\n";
      text = text + "\n";
      text = text + "\n";
      text = text + "--- BEGIN ERROR REPORT " + Integer.toHexString(text.hashCode()) + " --------\n";
      text = text + msg;
      text = text + "--- END ERROR REPORT " + Integer.toHexString(text.hashCode()) + " ----------\n";
      text = text + "\n";
      text = text + "\n";
      TextArea textArea = new TextArea(text, 0, 0, 1);
      textArea.setFont(new Font("Monospaced", 0, 12));
      this.add(new LogoBorder(), "North");
      this.add(new Border(80), "East");
      this.add(new Border(80), "West");
      this.add(new Border(100), "South");
      this.add(textArea, "Center");
   }

   private static class LogoBorder extends Canvas {
      private static final long serialVersionUID = 1L;
      private BufferedImage image;

      public LogoBorder() {
         try {
            this.image = ImageIO.read(CrashInfoPanel.class.getResource("/gui/logo.png"));
         } catch (IOException var2) {
         }

         int size = 100;
         this.setPreferredSize(new Dimension(size, size));
         this.setMinimumSize(new Dimension(size, size));
      }

      public void paint(Graphics g) {
         super.paint(g);
         g.drawImage(this.image, this.getWidth() / 2 - this.image.getWidth() / 2, 32, (ImageObserver)null);
      }
   }

   private static class Border extends Canvas {
      private static final long serialVersionUID = 1L;

      public Border(int size) {
         this.setPreferredSize(new Dimension(size, size));
         this.setMinimumSize(new Dimension(size, size));
      }
   }
}
