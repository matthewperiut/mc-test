package net.minecraft.client.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Lighting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class Gui extends GuiComponent {
   private static final int MAX_MESSAGE_WIDTH = 320;
   private static ItemRenderer itemRenderer = new ItemRenderer();
   private List<GuiMessage> guiMessages = new ArrayList();
   private Random random = new Random();
   private Minecraft minecraft;
   public String selectedName = null;
   private int tickCount = 0;
   private String nowPlayingString = "";
   private int nowPlayingTime = 0;
   public float progress;
   float tbr = 1.0F;

   public Gui(Minecraft var1) {
      this.minecraft = var1;
   }

   public void render(float var1, boolean var2, int var3, int var4) {
      ScreenSizeCalculator var5 = new ScreenSizeCalculator(this.minecraft.width, this.minecraft.height);
      int var6 = var5.getWidth();
      int var7 = var5.getHeight();
      Font var8 = this.minecraft.font;
      this.minecraft.gameRenderer.setupGuiScreen();
      GL11.glEnable(3042);
      if (this.minecraft.options.fancyGraphics) {
         this.renderVignette(this.minecraft.player.getBrightness(var1), var6, var7);
      }

      ItemInstance var9 = this.minecraft.player.inventory.getArmor(3);
      if (!this.minecraft.options.thirdPersonView && var9 != null && var9.id == Tile.pumpkin.id) {
         this.renderPumpkin(var6, var7);
      }

      float var10 = this.minecraft.player.oPortalTime + (this.minecraft.player.portalTime - this.minecraft.player.oPortalTime) * var1;
      if (var10 > 0.0F) {
         this.renderTp(var10, var6, var7);
      }

      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glBindTexture(3553, this.minecraft.textures.loadTexture("/gui/gui.png"));
      Inventory var11 = this.minecraft.player.inventory;
      this.blitOffset = -90.0F;
      this.blit(var6 / 2 - 91, var7 - 22, 0, 0, 182, 22);
      this.blit(var6 / 2 - 91 - 1 + var11.selected * 20, var7 - 22 - 1, 0, 22, 24, 22);
      GL11.glBindTexture(3553, this.minecraft.textures.loadTexture("/gui/icons.png"));
      GL11.glEnable(3042);
      GL11.glBlendFunc(775, 769);
      this.blit(var6 / 2 - 7, var7 / 2 - 7, 0, 0, 16, 16);
      GL11.glDisable(3042);
      boolean var12 = this.minecraft.player.invulnerableTime / 3 % 2 == 1;
      if (this.minecraft.player.invulnerableTime < 10) {
         var12 = false;
      }

      int var13 = this.minecraft.player.health;
      int var14 = this.minecraft.player.lastHealth;
      this.random.setSeed((long)(this.tickCount * 312871));
      if (this.minecraft.gameMode.canHurtPlayer()) {
         int var15 = this.minecraft.player.getArmor();

         for(int var16 = 0; var16 < 10; ++var16) {
            int var17 = var7 - 32;
            if (var15 > 0) {
               int var18 = var6 / 2 + 91 - var16 * 8 - 9;
               if (var16 * 2 + 1 < var15) {
                  this.blit(var18, var17, 34, 9, 9, 9);
               }

               if (var16 * 2 + 1 == var15) {
                  this.blit(var18, var17, 25, 9, 9, 9);
               }

               if (var16 * 2 + 1 > var15) {
                  this.blit(var18, var17, 16, 9, 9, 9);
               }
            }

            byte var37 = 0;
            if (var12) {
               var37 = 1;
            }

            int var19 = var6 / 2 - 91 + var16 * 8;
            if (var13 <= 4) {
               var17 += this.random.nextInt(2);
            }

            this.blit(var19, var17, 16 + var37 * 9, 0, 9, 9);
            if (var12) {
               if (var16 * 2 + 1 < var14) {
                  this.blit(var19, var17, 70, 0, 9, 9);
               }

               if (var16 * 2 + 1 == var14) {
                  this.blit(var19, var17, 79, 0, 9, 9);
               }
            }

            if (var16 * 2 + 1 < var13) {
               this.blit(var19, var17, 52, 0, 9, 9);
            }

            if (var16 * 2 + 1 == var13) {
               this.blit(var19, var17, 61, 0, 9, 9);
            }
         }

         if (this.minecraft.player.isUnderLiquid(Material.water)) {
            int var28 = (int)Math.ceil((double)(this.minecraft.player.airSupply - 2) * (double)10.0F / (double)300.0F);
            int var32 = (int)Math.ceil((double)this.minecraft.player.airSupply * (double)10.0F / (double)300.0F) - var28;

            for(int var38 = 0; var38 < var28 + var32; ++var38) {
               if (var38 < var28) {
                  this.blit(var6 / 2 - 91 + var38 * 8, var7 - 32 - 9, 16, 18, 9, 9);
               } else {
                  this.blit(var6 / 2 - 91 + var38 * 8, var7 - 32 - 9, 25, 18, 9, 9);
               }
            }
         }
      }

      GL11.glDisable(3042);
      GL11.glEnable(32826);
      GL11.glPushMatrix();
      GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
      Lighting.turnOn();
      GL11.glPopMatrix();

      for(int var24 = 0; var24 < 9; ++var24) {
         int var29 = var6 / 2 - 90 + var24 * 20 + 2;
         int var33 = var7 - 16 - 3;
         this.renderSlot(var24, var29, var33, var1);
      }

      Lighting.turnOff();
      GL11.glDisable(32826);
      if (Keyboard.isKeyDown(61)) {
         var8.drawShadow("Minecraft Beta 1.2_02 (" + this.minecraft.fpsString + ")", 2, 2, 16777215);
         var8.drawShadow(this.minecraft.gatherStats1(), 2, 12, 16777215);
         var8.drawShadow(this.minecraft.gatherStats2(), 2, 22, 16777215);
         var8.drawShadow(this.minecraft.gatherStats3(), 2, 32, 16777215);
         var8.drawShadow(this.minecraft.gatherStats4(), 2, 42, 16777215);
         long var25 = Runtime.getRuntime().maxMemory();
         long var34 = Runtime.getRuntime().totalMemory();
         long var43 = Runtime.getRuntime().freeMemory();
         long var21 = var34 - var43;
         String var23 = "Used memory: " + var21 * 100L / var25 + "% (" + var21 / 1024L / 1024L + "MB) of " + var25 / 1024L / 1024L + "MB";
         this.drawString(var8, var23, var6 - var8.width(var23) - 2, 2, 14737632);
         var23 = "Allocated memory: " + var34 * 100L / var25 + "% (" + var34 / 1024L / 1024L + "MB)";
         this.drawString(var8, var23, var6 - var8.width(var23) - 2, 12, 14737632);
         this.drawString(var8, "x: " + this.minecraft.player.x, 2, 64, 14737632);
         this.drawString(var8, "y: " + this.minecraft.player.y, 2, 72, 14737632);
         this.drawString(var8, "z: " + this.minecraft.player.z, 2, 80, 14737632);
         this.drawString(var8, "xRot: " + this.minecraft.player.xRot, 2, 88, 14737632);
         this.drawString(var8, "yRot: " + this.minecraft.player.yRot, 2, 96, 14737632);
         this.drawString(var8, "tilt: " + this.minecraft.player.tilt, 2, 104, 14737632);
      } else {
         var8.drawShadow("Minecraft Beta 1.2_02", 2, 2, 16777215);
      }

      if (this.nowPlayingTime > 0) {
         float var26 = (float)this.nowPlayingTime - var1;
         int var30 = (int)(var26 * 256.0F / 20.0F);
         if (var30 > 255) {
            var30 = 255;
         }

         if (var30 > 0) {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(var6 / 2), (float)(var7 - 48), 0.0F);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            int var35 = Color.HSBtoRGB(var26 / 50.0F, 0.7F, 0.6F) & 16777215;
            var8.draw(this.nowPlayingString, -var8.width(this.nowPlayingString) / 2, -4, var35 + (var30 << 24));
            GL11.glDisable(3042);
            GL11.glPopMatrix();
         }
      }

      byte var27 = 10;
      boolean var31 = false;
      if (this.minecraft.screen instanceof ChatScreen) {
         var27 = 20;
         var31 = true;
      }

      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glDisable(3008);
      GL11.glPushMatrix();
      GL11.glTranslatef(0.0F, (float)(var7 - 48), 0.0F);

      for(int var36 = 0; var36 < this.guiMessages.size() && var36 < var27; ++var36) {
         if (((GuiMessage)this.guiMessages.get(var36)).ticks < 200 || var31) {
            double var39 = (double)((GuiMessage)this.guiMessages.get(var36)).ticks / (double)200.0F;
            var39 = (double)1.0F - var39;
            var39 *= (double)10.0F;
            if (var39 < (double)0.0F) {
               var39 = (double)0.0F;
            }

            if (var39 > (double)1.0F) {
               var39 = (double)1.0F;
            }

            var39 *= var39;
            int var20 = (int)((double)255.0F * var39);
            if (var31) {
               var20 = 255;
            }

            if (var20 > 0) {
               byte var44 = 2;
               int var22 = -var36 * 9;
               String var46 = ((GuiMessage)this.guiMessages.get(var36)).string;
               this.fill(var44, var22 - 1, var44 + 320, var22 + 8, var20 / 2 << 24);
               GL11.glEnable(3042);
               var8.drawShadow(var46, var44, var22, 16777215 + (var20 << 24));
            }
         }
      }

      GL11.glPopMatrix();
      GL11.glEnable(3008);
      GL11.glDisable(3042);
   }

   private void renderPumpkin(int var1, int var2) {
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      GL11.glBlendFunc(770, 771);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glDisable(3008);
      GL11.glBindTexture(3553, this.minecraft.textures.loadTexture("%blur%/misc/pumpkinblur.png"));
      Tesselator var3 = Tesselator.instance;
      var3.begin();
      var3.vertexUV((double)0.0F, (double)var2, (double)-90.0F, (double)0.0F, (double)1.0F);
      var3.vertexUV((double)var1, (double)var2, (double)-90.0F, (double)1.0F, (double)1.0F);
      var3.vertexUV((double)var1, (double)0.0F, (double)-90.0F, (double)1.0F, (double)0.0F);
      var3.vertexUV((double)0.0F, (double)0.0F, (double)-90.0F, (double)0.0F, (double)0.0F);
      var3.end();
      GL11.glDepthMask(true);
      GL11.glEnable(2929);
      GL11.glEnable(3008);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderVignette(float var1, int var2, int var3) {
      var1 = 1.0F - var1;
      if (var1 < 0.0F) {
         var1 = 0.0F;
      }

      if (var1 > 1.0F) {
         var1 = 1.0F;
      }

      this.tbr = (float)((double)this.tbr + (double)(var1 - this.tbr) * 0.01);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      GL11.glBlendFunc(0, 769);
      GL11.glColor4f(this.tbr, this.tbr, this.tbr, 1.0F);
      GL11.glBindTexture(3553, this.minecraft.textures.loadTexture("%blur%/misc/vignette.png"));
      Tesselator var4 = Tesselator.instance;
      var4.begin();
      var4.vertexUV((double)0.0F, (double)var3, (double)-90.0F, (double)0.0F, (double)1.0F);
      var4.vertexUV((double)var2, (double)var3, (double)-90.0F, (double)1.0F, (double)1.0F);
      var4.vertexUV((double)var2, (double)0.0F, (double)-90.0F, (double)1.0F, (double)0.0F);
      var4.vertexUV((double)0.0F, (double)0.0F, (double)-90.0F, (double)0.0F, (double)0.0F);
      var4.end();
      GL11.glDepthMask(true);
      GL11.glEnable(2929);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glBlendFunc(770, 771);
   }

   private void renderTp(float var1, int var2, int var3) {
      var1 *= var1;
      var1 *= var1;
      var1 = var1 * 0.8F + 0.2F;
      GL11.glDisable(3008);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      GL11.glBlendFunc(770, 771);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, var1);
      GL11.glBindTexture(3553, this.minecraft.textures.loadTexture("/terrain.png"));
      float var4 = (float)(Tile.portalTile.tex % 16) / 16.0F;
      float var5 = (float)(Tile.portalTile.tex / 16) / 16.0F;
      float var6 = (float)(Tile.portalTile.tex % 16 + 1) / 16.0F;
      float var7 = (float)(Tile.portalTile.tex / 16 + 1) / 16.0F;
      Tesselator var8 = Tesselator.instance;
      var8.begin();
      var8.vertexUV((double)0.0F, (double)var3, (double)-90.0F, (double)var4, (double)var7);
      var8.vertexUV((double)var2, (double)var3, (double)-90.0F, (double)var6, (double)var7);
      var8.vertexUV((double)var2, (double)0.0F, (double)-90.0F, (double)var6, (double)var5);
      var8.vertexUV((double)0.0F, (double)0.0F, (double)-90.0F, (double)var4, (double)var5);
      var8.end();
      GL11.glDepthMask(true);
      GL11.glEnable(2929);
      GL11.glEnable(3008);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderSlot(int var1, int var2, int var3, float var4) {
      ItemInstance var5 = this.minecraft.player.inventory.items[var1];
      if (var5 != null) {
         float var6 = (float)var5.popTime - var4;
         if (var6 > 0.0F) {
            GL11.glPushMatrix();
            float var7 = 1.0F + var6 / 5.0F;
            GL11.glTranslatef((float)(var2 + 8), (float)(var3 + 12), 0.0F);
            GL11.glScalef(1.0F / var7, (var7 + 1.0F) / 2.0F, 1.0F);
            GL11.glTranslatef((float)(-(var2 + 8)), (float)(-(var3 + 12)), 0.0F);
         }

         itemRenderer.renderGuiItem(this.minecraft.font, this.minecraft.textures, var5, var2, var3);
         if (var6 > 0.0F) {
            GL11.glPopMatrix();
         }

         itemRenderer.renderGuiItemDecorations(this.minecraft.font, this.minecraft.textures, var5, var2, var3);
      }
   }

   public void tick() {
      if (this.nowPlayingTime > 0) {
         --this.nowPlayingTime;
      }

      ++this.tickCount;

      for(int var1 = 0; var1 < this.guiMessages.size(); ++var1) {
         ++((GuiMessage)this.guiMessages.get(var1)).ticks;
      }

   }

   public void addMessage(String var1) {
      while(this.minecraft.font.width(var1) > 320) {
         int var2;
         for(var2 = 1; var2 < var1.length() && this.minecraft.font.width(var1.substring(0, var2 + 1)) <= 320; ++var2) {
         }

         this.addMessage(var1.substring(0, var2));
         var1 = var1.substring(var2);
      }

      this.guiMessages.add(0, new GuiMessage(var1));

      while(this.guiMessages.size() > 50) {
         this.guiMessages.remove(this.guiMessages.size() - 1);
      }

   }

   public void setNowPlaying(String var1) {
      this.nowPlayingString = "Now playing: " + var1;
      this.nowPlayingTime = 60;
   }
}
