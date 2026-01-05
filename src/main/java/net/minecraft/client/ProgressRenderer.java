package net.minecraft.client;

import net.minecraft.client.gui.ScreenSizeCalculator;
import net.minecraft.client.renderer.Tesselator;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import util.ProgressListener;

public class ProgressRenderer implements ProgressListener {
   private String status = "";
   private Minecraft minecraft;
   private String title = "";
   private long lastTime = System.currentTimeMillis();
   private boolean noAbort = false;

   public ProgressRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void progressStart(String title) {
      this.noAbort = false;
      this._progressStart(title);
   }

   public void progressStartNoAbort(String string) {
      this.noAbort = true;
      this._progressStart(this.title);
   }

   public void _progressStart(String title) {
      if (!this.minecraft.running) {
         if (!this.noAbort) {
            throw new StopGameException();
         }
      } else {
         this.title = title;
         ScreenSizeCalculator ssc = new ScreenSizeCalculator(this.minecraft.width, this.minecraft.height);
         int screenWidth = ssc.getWidth();
         int screenHeight = ssc.getHeight();
         GL11.glClear(256);
         GL11.glMatrixMode(5889);
         GL11.glLoadIdentity();
         GL11.glOrtho((double)0.0F, (double)screenWidth, (double)screenHeight, (double)0.0F, (double)100.0F, (double)300.0F);
         GL11.glMatrixMode(5888);
         GL11.glLoadIdentity();
         GL11.glTranslatef(0.0F, 0.0F, -200.0F);
      }
   }

   public void progressStage(String status) {
      if (!this.minecraft.running) {
         if (!this.noAbort) {
            throw new StopGameException();
         }
      } else {
         this.lastTime = 0L;
         this.status = status;
         this.progressStagePercentage(-1);
         this.lastTime = 0L;
      }
   }

   public void progressStagePercentage(int i) {
      if (!this.minecraft.running) {
         if (!this.noAbort) {
            throw new StopGameException();
         }
      } else {
         long now = System.currentTimeMillis();
         if (now - this.lastTime >= 20L) {
            this.lastTime = now;
            ScreenSizeCalculator ssc = new ScreenSizeCalculator(this.minecraft.width, this.minecraft.height);
            int screenWidth = ssc.getWidth();
            int screenHeight = ssc.getHeight();
            GL11.glClear(256);
            GL11.glMatrixMode(5889);
            GL11.glLoadIdentity();
            GL11.glOrtho((double)0.0F, (double)screenWidth, (double)screenHeight, (double)0.0F, (double)100.0F, (double)300.0F);
            GL11.glMatrixMode(5888);
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0F, 0.0F, -200.0F);
            GL11.glClear(16640);
            Tesselator t = Tesselator.instance;
            int id = this.minecraft.textures.loadTexture("/gui/background.png");
            GL11.glBindTexture(3553, id);
            float s = 32.0F;
            t.begin();
            t.color(4210752);
            t.vertexUV((double)0.0F, (double)screenHeight, (double)0.0F, (double)0.0F, (double)((float)screenHeight / s));
            t.vertexUV((double)screenWidth, (double)screenHeight, (double)0.0F, (double)((float)screenWidth / s), (double)((float)screenHeight / s));
            t.vertexUV((double)screenWidth, (double)0.0F, (double)0.0F, (double)((float)screenWidth / s), (double)0.0F);
            t.vertexUV((double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
            t.end();
            if (i >= 0) {
               int w = 100;
               int h = 2;
               int x = screenWidth / 2 - w / 2;
               int y = screenHeight / 2 + 16;
               GL11.glDisable(3553);
               t.begin();
               t.color(8421504);
               t.vertex((double)x, (double)y, (double)0.0F);
               t.vertex((double)x, (double)(y + h), (double)0.0F);
               t.vertex((double)(x + w), (double)(y + h), (double)0.0F);
               t.vertex((double)(x + w), (double)y, (double)0.0F);
               t.color(8454016);
               t.vertex((double)x, (double)y, (double)0.0F);
               t.vertex((double)x, (double)(y + h), (double)0.0F);
               t.vertex((double)(x + i), (double)(y + h), (double)0.0F);
               t.vertex((double)(x + i), (double)y, (double)0.0F);
               t.end();
               GL11.glEnable(3553);
            }

            this.minecraft.font.drawShadow(this.title, (screenWidth - this.minecraft.font.width(this.title)) / 2, screenHeight / 2 - 4 - 16, 16777215);
            this.minecraft.font.drawShadow(this.status, (screenWidth - this.minecraft.font.width(this.status)) / 2, screenHeight / 2 - 4 + 8, 16777215);
            Display.update();

            try {
               Thread.yield();
            } catch (Exception var14) {
            }

         }
      }
   }
}
