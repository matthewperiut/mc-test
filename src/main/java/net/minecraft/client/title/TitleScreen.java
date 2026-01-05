package net.minecraft.client.title;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Button;
import net.minecraft.client.gui.OptionsScreen;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ScreenSizeCalculator;
import net.minecraft.client.gui.SelectWorldScreen;
import net.minecraft.client.locale.Language;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.client.skins.TexturePackSelectScreen;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import util.Mth;

public class TitleScreen extends Screen {
   private static final Random random = new Random();
   String[] logo = new String[]{" *   * * *   * *** *** *** *** *** ***", " ** ** * **  * *   *   * * * * *    * ", " * * * * * * * **  *   **  *** **   * ", " *   * * *  ** *   *   * * * * *    * ", " *   * * *   * *   *** * * * * *    * "};
   private LetterBlock[][] letterBlocks;
   private float vo = 0.0F;
   private String splash = "missingno";

   public TitleScreen() {
      try {
         ArrayList var1 = new ArrayList();
         BufferedReader var2 = new BufferedReader(new InputStreamReader(TitleScreen.class.getResourceAsStream("/title/splashes.txt")));
         String var3 = "";

         while((var3 = var2.readLine()) != null) {
            var3 = var3.trim();
            if (var3.length() > 0) {
               var1.add(var3);
            }
         }

         this.splash = (String)var1.get(random.nextInt(var1.size()));
      } catch (Exception var4) {
      }

   }

   public void tick() {
      ++this.vo;
      if (this.letterBlocks != null) {
         for(int var1 = 0; var1 < this.letterBlocks.length; ++var1) {
            for(int var2 = 0; var2 < this.letterBlocks[var1].length; ++var2) {
               this.letterBlocks[var1][var2].tick();
            }
         }
      }

   }

   protected void keyPressed(char var1, int var2) {
   }

   public void init() {
      Calendar var1 = Calendar.getInstance();
      var1.setTime(new Date());
      if (var1.get(2) + 1 == 11 && var1.get(5) == 9) {
         this.splash = "Happy birthday, ez!";
      } else if (var1.get(2) + 1 == 6 && var1.get(5) == 1) {
         this.splash = "Happy birthday, Notch!";
      } else if (var1.get(2) + 1 == 12 && var1.get(5) == 24) {
         this.splash = "Merry X-mas!";
      } else if (var1.get(2) + 1 == 1 && var1.get(5) == 1) {
         this.splash = "Happy new year!";
      }

      Language var2 = Language.getInstance();
      int var4 = this.height / 4 + 48;
      this.buttons.add(new Button(1, this.width / 2 - 100, var4, var2.getElement("menu.singleplayer")));
      this.buttons.add(new Button(2, this.width / 2 - 100, var4 + 24, var2.getElement("menu.multiplayer")));
      this.buttons.add(new Button(3, this.width / 2 - 100, var4 + 48, var2.getElement("menu.mods")));
      if (this.minecraft.appletMode) {
         this.buttons.add(new Button(0, this.width / 2 - 100, var4 + 72, var2.getElement("menu.options")));
      } else {
         this.buttons.add(new Button(0, this.width / 2 - 100, var4 + 72 + 12, 98, 20, var2.getElement("menu.options")));
         this.buttons.add(new Button(4, this.width / 2 + 2, var4 + 72 + 12, 98, 20, var2.getElement("menu.quit")));
      }

      if (this.minecraft.user == null) {
         ((Button)this.buttons.get(1)).active = false;
      }
   }

   protected void buttonClicked(Button var1) {
      if (var1.id == 0) {
         this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
      }

      if (var1.id == 1) {
         this.minecraft.setScreen(new SelectWorldScreen(this));
      }

      if (var1.id == 2) {
      }

      if (var1.id == 3) {
         this.minecraft.setScreen(new TexturePackSelectScreen(this));
      }

      if (var1.id == 4) {
         this.minecraft.stop();
      }

   }

   public void render(int var1, int var2, float var3) {
      this.renderBackground();
      Tesselator var4 = Tesselator.instance;
      this.renderMinecraftLogo(var3);
      GL11.glBindTexture(3553, this.minecraft.textures.loadTexture("/gui/logo.png"));
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      var4.color(16777215);
      GL11.glPushMatrix();
      GL11.glTranslatef((float)(this.width / 2 + 90), 70.0F, 0.0F);
      GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
      float var5 = 1.8F - Mth.abs(Mth.sin((float)(System.currentTimeMillis() % 1000L) / 1000.0F * (float)Math.PI * 2.0F) * 0.1F);
      var5 = var5 * 100.0F / (float)(this.font.width(this.splash) + 32);
      GL11.glScalef(var5, var5, var5);
      this.drawCenteredString(this.font, this.splash, 0, -8, 16776960);
      GL11.glPopMatrix();
      this.drawString(this.font, "Minecraft Beta 1.2_02", 2, 2, 5263440);
      String var6 = "Copyright Mojang AB. Do not distribute.";
      this.drawString(this.font, var6, this.width - this.font.width(var6) - 2, this.height - 10, 16777215);
      super.render(var1, var2, var3);
   }

   private void renderMinecraftLogo(float var1) {
      if (this.letterBlocks == null) {
         this.letterBlocks = new LetterBlock[this.logo[0].length()][this.logo.length];

         for(int var2 = 0; var2 < this.letterBlocks.length; ++var2) {
            for(int var3 = 0; var3 < this.letterBlocks[var2].length; ++var3) {
               this.letterBlocks[var2][var3] = new LetterBlock(var2, var3);
            }
         }
      }

      GL11.glMatrixMode(5889);
      GL11.glPushMatrix();
      GL11.glLoadIdentity();
      ScreenSizeCalculator var14 = new ScreenSizeCalculator(this.minecraft.width, this.minecraft.height);
      int var15 = 120 * var14.scale;
      GLU.gluPerspective(70.0F, (float)this.minecraft.width / (float)var15, 0.05F, 100.0F);
      GL11.glViewport(0, this.minecraft.height - var15, this.minecraft.width, var15);
      GL11.glMatrixMode(5888);
      GL11.glPushMatrix();
      GL11.glLoadIdentity();
      GL11.glDisable(2884);
      GL11.glCullFace(1029);
      GL11.glDepthMask(true);
      TileRenderer var4 = new TileRenderer();

      for(int var5 = 0; var5 < 3; ++var5) {
         GL11.glPushMatrix();
         GL11.glTranslatef(0.4F, 0.6F, -13.0F);
         if (var5 == 0) {
            GL11.glClear(256);
            GL11.glTranslatef(0.0F, -0.4F, 0.0F);
            GL11.glScalef(0.98F, 1.0F, 1.0F);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
         }

         if (var5 == 1) {
            GL11.glDisable(3042);
            GL11.glClear(256);
         }

         if (var5 == 2) {
            GL11.glEnable(3042);
            GL11.glBlendFunc(768, 1);
         }

         GL11.glScalef(1.0F, -1.0F, 1.0F);
         GL11.glRotatef(15.0F, 1.0F, 0.0F, 0.0F);
         GL11.glScalef(0.89F, 1.0F, 0.4F);
         GL11.glTranslatef((float)(-this.logo[0].length()) * 0.5F, (float)(-this.logo.length) * 0.5F, 0.0F);
         GL11.glBindTexture(3553, this.minecraft.textures.loadTexture("/terrain.png"));
         if (var5 == 0) {
            GL11.glBindTexture(3553, this.minecraft.textures.loadTexture("/title/black.png"));
         }

         for(int var6 = 0; var6 < this.logo.length; ++var6) {
            for(int var7 = 0; var7 < this.logo[var6].length(); ++var7) {
               char var8 = this.logo[var6].charAt(var7);
               if (var8 != ' ') {
                  GL11.glPushMatrix();
                  LetterBlock var9 = this.letterBlocks[var7][var6];
                  float var10 = (float)(var9.yO + (var9.y - var9.yO) * (double)var1);
                  float var11 = 1.0F;
                  float var12 = 1.0F;
                  float var13 = 0.0F;
                  if (var5 == 0) {
                     var11 = var10 * 0.04F + 1.0F;
                     var12 = 1.0F / var11;
                     var10 = 0.0F;
                  }

                  GL11.glTranslatef((float)var7, (float)var6, var10);
                  GL11.glScalef(var11, var11, var11);
                  GL11.glRotatef(var13, 0.0F, 1.0F, 0.0F);
                  var4.renderCube(Tile.rock, var12);
                  GL11.glPopMatrix();
               }
            }
         }

         GL11.glPopMatrix();
      }

      GL11.glDisable(3042);
      GL11.glMatrixMode(5889);
      GL11.glPopMatrix();
      GL11.glMatrixMode(5888);
      GL11.glPopMatrix();
      GL11.glViewport(0, 0, this.minecraft.width, this.minecraft.height);
      GL11.glEnable(2884);
   }

   private class LetterBlock {
      public double y;
      public double yO;
      public double ya;

      public LetterBlock(int var2, int var3) {
         this.y = this.yO = (double)(10 + var3) + TitleScreen.random.nextDouble() * (double)32.0F + (double)var2;
      }

      public void tick() {
         this.yO = this.y;
         if (this.y > (double)0.0F) {
            this.ya -= 0.6;
         }

         this.y += this.ya;
         this.ya *= 0.9;
         if (this.y < (double)0.0F) {
            this.y = (double)0.0F;
            this.ya = (double)0.0F;
         }

      }
   }
}
