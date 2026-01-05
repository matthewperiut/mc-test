package net.minecraft.client;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;

public class MinecraftApplet extends Applet {
   private static final long serialVersionUID = 1L;
   private Canvas canvas;
   private Minecraft minecraft;
   private Thread thread = null;

   public MinecraftApplet() {
   }

   public void init() {
      this.canvas = new Canvas() {
         private static final long serialVersionUID = 1L;

         public synchronized void addNotify() {
            super.addNotify();
            MinecraftApplet.this.startGameThread();
         }

         public synchronized void removeNotify() {
            MinecraftApplet.this.stopGameThread();
            super.removeNotify();
         }
      };
      boolean fullscreen = false;
      if (this.getParameter("fullscreen") != null) {
         fullscreen = this.getParameter("fullscreen").equalsIgnoreCase("true");
      }

      this.minecraft = new Minecraft(this, this.canvas, this, this.getWidth(), this.getHeight(), fullscreen) {
         public void onCrash(CrashReport crashReport) {
            MinecraftApplet.this.removeAll();
            MinecraftApplet.this.setLayout(new BorderLayout());
            MinecraftApplet.this.add(new CrashInfoPanel(crashReport), "Center");
            MinecraftApplet.this.validate();
         }
      };
      this.minecraft.serverDomain = this.getDocumentBase().getHost();
      if (this.getDocumentBase().getPort() > 0) {
         Minecraft var10000 = this.minecraft;
         var10000.serverDomain = var10000.serverDomain + ":" + this.getDocumentBase().getPort();
      }

      if (this.getParameter("username") != null && this.getParameter("sessionid") != null) {
         this.minecraft.user = new User(this.getParameter("username"), this.getParameter("sessionid"));
         System.out.println("Setting user: " + this.minecraft.user.name + ", " + this.minecraft.user.sessionId);
         if (this.getParameter("mppass") != null) {
            this.minecraft.user.mpPassword = this.getParameter("mppass");
         }
      } else {
         this.minecraft.user = new User("Player", "");
      }

      if (this.getParameter("loadmap_user") != null && this.getParameter("loadmap_id") != null) {
         this.minecraft.autoLoad_user = this.getParameter("loadmap_user");
         this.minecraft.autoLoad_id = Integer.parseInt(this.getParameter("loadmap_id"));
      } else if (this.getParameter("server") != null && this.getParameter("port") != null) {
         this.minecraft.connectTo(this.getParameter("server"), Integer.parseInt(this.getParameter("port")));
      }

      this.minecraft.appletMode = true;
      this.setLayout(new BorderLayout());
      this.add(this.canvas, "Center");
      this.canvas.setFocusable(true);
      this.validate();
   }

   public void startGameThread() {
      if (this.thread == null) {
         this.thread = new Thread(this.minecraft, "Minecraft main thread");
         this.thread.start();
      }
   }

   public void start() {
      if (this.minecraft != null) {
         this.minecraft.pause = false;
      }

   }

   public void stop() {
      if (this.minecraft != null) {
         this.minecraft.pause = true;
      }

   }

   public void destroy() {
      this.stopGameThread();
   }

   public void stopGameThread() {
      if (this.thread != null) {
         this.minecraft.stop();

         try {
            this.thread.join(10000L);
         } catch (InterruptedException var4) {
            try {
               this.minecraft.destroy();
            } catch (Exception ee) {
               ee.printStackTrace();
            }
         }

         this.thread = null;
      }
   }

   public void clearMemory() {
      this.canvas = null;
      this.minecraft = null;
      this.thread = null;

      try {
         this.removeAll();
         this.validate();
      } catch (Exception var2) {
      }

   }
}
