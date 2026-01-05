package net.minecraft.client.renderer;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Lighting;
import net.minecraft.client.MemoryTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gamemode.CreativeMode;
import net.minecraft.client.gui.ScreenSizeCalculator;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.FrustumCuller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkCache;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;
import util.Mth;

public class GameRenderer {
   private Minecraft mc;
   private float renderDistance = 0.0F;
   public ItemInHandRenderer itemInHandRenderer;
   private int tick;
   private Entity hovered = null;
   private double zoom = (double)1.0F;
   private double zoom_x = (double)0.0F;
   private double zoom_y = (double)0.0F;
   private long lastActiveTime = System.currentTimeMillis();
   private Random random = new Random();
   volatile int xMod = 0;
   volatile int yMod = 0;
   FloatBuffer lb = MemoryTracker.createFloatBuffer(16);
   float fr;
   float fg;
   float fb;
   private float fogBrO;
   private float fogBr;

   public GameRenderer(Minecraft var1) {
      this.mc = var1;
      this.itemInHandRenderer = new ItemInHandRenderer(var1);
   }

   public void tick() {
      this.fogBrO = this.fogBr;
      float var1 = this.mc.level.getBrightness(Mth.floor(this.mc.player.x), Mth.floor(this.mc.player.y), Mth.floor(this.mc.player.z));
      float var2 = (float)(3 - this.mc.options.viewDistance) / 3.0F;
      float var3 = var1 * (1.0F - var2) + var2;
      this.fogBr += (var3 - this.fogBr) * 0.1F;
      ++this.tick;
      this.itemInHandRenderer.tick();
      if (this.mc.isRaining) {
         this.tickRain();
      }

   }

   public void pick(float var1) {
      if (this.mc.player != null) {
         double var2 = (double)this.mc.gameMode.getPickRange();
         this.mc.hitResult = this.mc.player.pick(var2, var1);
         double var4 = var2;
         Vec3 var6 = this.mc.player.getPos(var1);
         if (this.mc.hitResult != null) {
            var4 = this.mc.hitResult.pos.distanceTo(var6);
         }

         if (this.mc.gameMode instanceof CreativeMode) {
            var2 = (double)32.0F;
            var4 = (double)32.0F;
         } else {
            if (var4 > (double)3.0F) {
               var4 = (double)3.0F;
            }

            var2 = var4;
         }

         Vec3 var7 = this.mc.player.getViewVector(var1);
         Vec3 var8 = var6.add(var7.x * var2, var7.y * var2, var7.z * var2);
         this.hovered = null;
         float var9 = 1.0F;
         List var10 = this.mc.level.getEntities(this.mc.player, this.mc.player.bb.expand(var7.x * var2, var7.y * var2, var7.z * var2).grow((double)var9, (double)var9, (double)var9));
         double var11 = (double)0.0F;

         for(int var13 = 0; var13 < var10.size(); ++var13) {
            Entity var14 = (Entity)var10.get(var13);
            if (var14.isPickable()) {
               float var15 = var14.getPickRadius();
               AABB var16 = var14.bb.grow((double)var15, (double)var15, (double)var15);
               HitResult var17 = var16.clip(var6, var8);
               if (var16.contains(var6)) {
                  if ((double)0.0F < var11 || var11 == (double)0.0F) {
                     this.hovered = var14;
                     var11 = (double)0.0F;
                  }
               } else if (var17 != null) {
                  double var18 = var6.distanceTo(var17.pos);
                  if (var18 < var11 || var11 == (double)0.0F) {
                     this.hovered = var14;
                     var11 = var18;
                  }
               }
            }
         }

         if (this.hovered != null && !(this.mc.gameMode instanceof CreativeMode)) {
            this.mc.hitResult = new HitResult(this.hovered);
         }

      }
   }

   private float getFov(float var1) {
      LocalPlayer var2 = this.mc.player;
      float var3 = 70.0F;
      if (((Player)var2).isUnderLiquid(Material.water)) {
         var3 = 60.0F;
      }

      if (var2.health <= 0) {
         float var4 = (float)var2.deathTime + var1;
         var3 /= (1.0F - 500.0F / (var4 + 500.0F)) * 2.0F + 1.0F;
      }

      return var3;
   }

   private void bobHurt(float var1) {
      LocalPlayer var2 = this.mc.player;
      float var3 = (float)var2.hurtTime - var1;
      if (var2.health <= 0) {
         float var4 = (float)var2.deathTime + var1;
         GL11.glRotatef(40.0F - 8000.0F / (var4 + 200.0F), 0.0F, 0.0F, 1.0F);
      }

      if (!(var3 < 0.0F)) {
         var3 /= (float)var2.hurtDuration;
         var3 = Mth.sin(var3 * var3 * var3 * var3 * (float)Math.PI);
         float var7 = var2.hurtDir;
         GL11.glRotatef(-var7, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(-var3 * 14.0F, 0.0F, 0.0F, 1.0F);
         GL11.glRotatef(var7, 0.0F, 1.0F, 0.0F);
      }
   }

   private void bobView(float var1) {
      if (!this.mc.options.thirdPersonView) {
         LocalPlayer var2 = this.mc.player;
         float var3 = var2.walkDist - var2.walkDistO;
         float var4 = var2.walkDist + var3 * var1;
         float var5 = var2.oBob + (var2.bob - var2.oBob) * var1;
         float var6 = var2.oTilt + (var2.tilt - var2.oTilt) * var1;
         GL11.glTranslatef(Mth.sin(var4 * (float)Math.PI) * var5 * 0.5F, -Math.abs(Mth.cos(var4 * (float)Math.PI) * var5), 0.0F);
         GL11.glRotatef(Mth.sin(var4 * (float)Math.PI) * var5 * 3.0F, 0.0F, 0.0F, 1.0F);
         GL11.glRotatef(Math.abs(Mth.cos(var4 * (float)Math.PI + 0.2F) * var5) * 5.0F, 1.0F, 0.0F, 0.0F);
         GL11.glRotatef(var6, 1.0F, 0.0F, 0.0F);
      }
   }

   private void moveCameraToPlayer(float var1) {
      LocalPlayer var2 = this.mc.player;
      double var3 = var2.xo + (var2.x - var2.xo) * (double)var1;
      double var5 = var2.yo + (var2.y - var2.yo) * (double)var1;
      double var7 = var2.zo + (var2.z - var2.zo) * (double)var1;
      if (this.mc.options.thirdPersonView) {
         double var9 = (double)4.0F;
         float var11 = var2.yRot;
         float var12 = var2.xRot;
         if (Keyboard.isKeyDown(59)) {
            var12 += 180.0F;
            var9 += (double)2.0F;
         }

         double var13 = (double)(-Mth.sin(var11 / 180.0F * (float)Math.PI) * Mth.cos(var12 / 180.0F * (float)Math.PI)) * var9;
         double var15 = (double)(Mth.cos(var11 / 180.0F * (float)Math.PI) * Mth.cos(var12 / 180.0F * (float)Math.PI)) * var9;
         double var17 = (double)(-Mth.sin(var12 / 180.0F * (float)Math.PI)) * var9;

         for(int var19 = 0; var19 < 8; ++var19) {
            float var20 = (float)((var19 & 1) * 2 - 1);
            float var21 = (float)((var19 >> 1 & 1) * 2 - 1);
            float var22 = (float)((var19 >> 2 & 1) * 2 - 1);
            var20 *= 0.1F;
            var21 *= 0.1F;
            var22 *= 0.1F;
            HitResult var23 = this.mc.level.clip(Vec3.newTemp(var3 + (double)var20, var5 + (double)var21, var7 + (double)var22), Vec3.newTemp(var3 - var13 + (double)var20 + (double)var22, var5 - var17 + (double)var21, var7 - var15 + (double)var22));
            if (var23 != null) {
               double var24 = var23.pos.distanceTo(Vec3.newTemp(var3, var5, var7));
               if (var24 < var9) {
                  var9 = var24;
               }
            }
         }

         if (Keyboard.isKeyDown(59)) {
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
         }

         GL11.glRotatef(var2.xRot - var12, 1.0F, 0.0F, 0.0F);
         GL11.glRotatef(var2.yRot - var11, 0.0F, 1.0F, 0.0F);
         GL11.glTranslatef(0.0F, 0.0F, (float)(-var9));
         GL11.glRotatef(var11 - var2.yRot, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(var12 - var2.xRot, 1.0F, 0.0F, 0.0F);
      } else {
         GL11.glTranslatef(0.0F, 0.0F, -0.1F);
      }

      GL11.glRotatef(var2.xRotO + (var2.xRot - var2.xRotO) * var1, 1.0F, 0.0F, 0.0F);
      GL11.glRotatef(var2.yRotO + (var2.yRot - var2.yRotO) * var1 + 180.0F, 0.0F, 1.0F, 0.0F);
   }

   public void zoomRegion(double var1, double var3, double var5) {
      this.zoom = var1;
      this.zoom_x = var3;
      this.zoom_y = var5;
   }

   public void unZoomRegion() {
      this.zoom = (double)1.0F;
   }

   private void setupCamera(float var1, int var2) {
      this.renderDistance = (float)(256 >> this.mc.options.viewDistance);
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      float var3 = 0.07F;
      if (this.mc.options.anaglyph3d) {
         GL11.glTranslatef((float)(-(var2 * 2 - 1)) * var3, 0.0F, 0.0F);
      }

      if (this.zoom != (double)1.0F) {
         GL11.glTranslatef((float)this.zoom_x, (float)(-this.zoom_y), 0.0F);
         GL11.glScaled(this.zoom, this.zoom, (double)1.0F);
         GLU.gluPerspective(this.getFov(var1), (float)this.mc.width / (float)this.mc.height, 0.05F, this.renderDistance);
      } else {
         GLU.gluPerspective(this.getFov(var1), (float)this.mc.width / (float)this.mc.height, 0.05F, this.renderDistance);
      }

      GL11.glMatrixMode(5888);
      GL11.glLoadIdentity();
      if (this.mc.options.anaglyph3d) {
         GL11.glTranslatef((float)(var2 * 2 - 1) * 0.1F, 0.0F, 0.0F);
      }

      this.bobHurt(var1);
      if (this.mc.options.bobView) {
         this.bobView(var1);
      }

      float var4 = this.mc.player.oPortalTime + (this.mc.player.portalTime - this.mc.player.oPortalTime) * var1;
      if (var4 > 0.0F) {
         float var5 = 5.0F / (var4 * var4 + 5.0F) - var4 * 0.04F;
         var5 *= var5;
         GL11.glRotatef(var4 * var4 * 1500.0F, 0.0F, 1.0F, 1.0F);
         GL11.glScalef(1.0F / var5, 1.0F, 1.0F);
         GL11.glRotatef(-var4 * var4 * 1500.0F, 0.0F, 1.0F, 1.0F);
      }

      this.moveCameraToPlayer(var1);
   }

   private void renderItemInHand(float var1, int var2) {
      GL11.glLoadIdentity();
      if (this.mc.options.anaglyph3d) {
         GL11.glTranslatef((float)(var2 * 2 - 1) * 0.1F, 0.0F, 0.0F);
      }

      GL11.glPushMatrix();
      this.bobHurt(var1);
      if (this.mc.options.bobView) {
         this.bobView(var1);
      }

      if (!this.mc.options.thirdPersonView && !Keyboard.isKeyDown(59)) {
         this.itemInHandRenderer.render(var1);
      }

      GL11.glPopMatrix();
      if (!this.mc.options.thirdPersonView) {
         this.itemInHandRenderer.renderScreenEffect(var1);
         this.bobHurt(var1);
      }

      if (this.mc.options.bobView) {
         this.bobView(var1);
      }

   }

   public void render(float var1) {
      if (!Display.isActive()) {
         if (System.currentTimeMillis() - this.lastActiveTime > 500L) {
         }
      } else {
         this.lastActiveTime = System.currentTimeMillis();
      }

      if (this.mc.mouseGrabbed) {
         this.mc.mouseHandler.poll();
         float var2 = this.mc.options.sensitivity * 0.6F + 0.2F;
         float var3 = var2 * var2 * var2 * 8.0F;
         float var4 = (float)this.mc.mouseHandler.xd * var3;
         float var5 = (float)this.mc.mouseHandler.yd * var3;
         byte var6 = 1;
         if (this.mc.options.invertYMouse) {
            var6 = -1;
         }

         this.mc.player.turn(var4, var5 * (float)var6);
      }

      if (!this.mc.noRender) {
         ScreenSizeCalculator var7 = new ScreenSizeCalculator(this.mc.width, this.mc.height);
         int var8 = var7.getWidth();
         int var9 = var7.getHeight();
         int var10 = Mouse.getX() * var8 / this.mc.width;
         int var11 = var9 - Mouse.getY() * var9 / this.mc.height - 1;
         if (this.mc.level != null) {
            this.renderLevel(var1);
            if (!Keyboard.isKeyDown(59)) {
               this.mc.gui.render(var1, this.mc.screen != null, var10, var11);
            }
         } else {
            GL11.glViewport(0, 0, this.mc.width, this.mc.height);
            GL11.glMatrixMode(5889);
            GL11.glLoadIdentity();
            GL11.glMatrixMode(5888);
            GL11.glLoadIdentity();
            this.setupGuiScreen();
         }

         if (this.mc.screen != null) {
            GL11.glClear(256);
            this.mc.screen.render(var10, var11, var1);
         }

      }
   }

   public void renderLevel(float var1) {
      this.pick(var1);
      LocalPlayer var2 = this.mc.player;
      LevelRenderer var3 = this.mc.levelRenderer;
      ParticleEngine var4 = this.mc.particleEngine;
      double var5 = var2.xOld + (var2.x - var2.xOld) * (double)var1;
      double var7 = var2.yOld + (var2.y - var2.yOld) * (double)var1;
      double var9 = var2.zOld + (var2.z - var2.zOld) * (double)var1;
      ChunkSource var11 = this.mc.level.getChunkSource();
      if (var11 instanceof ChunkCache) {
         ChunkCache var12 = (ChunkCache)var11;
         int var13 = Mth.floor((float)((int)var5)) >> 4;
         int var14 = Mth.floor((float)((int)var9)) >> 4;
         var12.centerOn(var13, var14);
      }

      for(int var15 = 0; var15 < 2; ++var15) {
         if (this.mc.options.anaglyph3d) {
            if (var15 == 0) {
               GL11.glColorMask(false, true, true, false);
            } else {
               GL11.glColorMask(true, false, false, false);
            }
         }

         GL11.glViewport(0, 0, this.mc.width, this.mc.height);
         this.setupClearColor(var1);
         GL11.glClear(16640);
         GL11.glEnable(2884);
         this.setupCamera(var1, var15);
         Frustum.getFrustum();
         if (this.mc.options.viewDistance < 2) {
            this.setupFog(-1);
            var3.renderSky(var1);
         }

         GL11.glEnable(2912);
         this.setupFog(1);
         FrustumCuller var16 = new FrustumCuller();
         var16.prepare(var5, var7, var9);
         this.mc.levelRenderer.cull(var16, var1);
         this.mc.levelRenderer.updateDirtyChunks(var2, false);
         this.setupFog(0);
         GL11.glEnable(2912);
         GL11.glBindTexture(3553, this.mc.textures.loadTexture("/terrain.png"));
         Lighting.turnOff();
         var3.render(var2, 0, (double)var1);
         Lighting.turnOn();
         var3.renderEntities(((Player)var2).getPos(var1), var16, var1);
         var4.renderLit(var2, var1);
         Lighting.turnOff();
         this.setupFog(0);
         var4.render(var2, var1);
         if (this.mc.hitResult != null && ((Player)var2).isUnderLiquid(Material.water)) {
            GL11.glDisable(3008);
            var3.renderHit(var2, this.mc.hitResult, 0, var2.inventory.getSelected(), var1);
            var3.renderHitOutline(var2, this.mc.hitResult, 0, var2.inventory.getSelected(), var1);
            GL11.glEnable(3008);
         }

         GL11.glBlendFunc(770, 771);
         this.setupFog(0);
         GL11.glEnable(3042);
         GL11.glDisable(2884);
         GL11.glBindTexture(3553, this.mc.textures.loadTexture("/terrain.png"));
         if (this.mc.options.fancyGraphics) {
            GL11.glColorMask(false, false, false, false);
            int var17 = var3.render(var2, 1, (double)var1);
            GL11.glColorMask(true, true, true, true);
            if (this.mc.options.anaglyph3d) {
               if (var15 == 0) {
                  GL11.glColorMask(false, true, true, false);
               } else {
                  GL11.glColorMask(true, false, false, false);
               }
            }

            if (var17 > 0) {
               var3.renderSameAsLast(1, (double)var1);
            }
         } else {
            var3.render(var2, 1, (double)var1);
         }

         GL11.glDepthMask(true);
         GL11.glEnable(2884);
         GL11.glDisable(3042);
         if (this.zoom == (double)1.0F && this.mc.hitResult != null && !((Player)var2).isUnderLiquid(Material.water)) {
            GL11.glDisable(3008);
            var3.renderHit(var2, this.mc.hitResult, 0, var2.inventory.getSelected(), var1);
            var3.renderHitOutline(var2, this.mc.hitResult, 0, var2.inventory.getSelected(), var1);
            GL11.glEnable(3008);
         }

         GL11.glDisable(2912);
         if (this.hovered != null) {
         }

         this.setupFog(0);
         GL11.glEnable(2912);
         var3.renderClouds(var1);
         GL11.glDisable(2912);
         this.setupFog(1);
         if (this.zoom == (double)1.0F) {
            GL11.glClear(256);
            this.renderItemInHand(var1, var15);
         }

         if (!this.mc.options.anaglyph3d) {
            return;
         }
      }

      GL11.glColorMask(true, true, true, false);
   }

   private void tickRain() {
      if (this.mc.options.fancyGraphics) {
         LocalPlayer var1 = this.mc.player;
         Level var2 = this.mc.level;
         int var3 = Mth.floor(var1.x);
         int var4 = Mth.floor(var1.y);
         int var5 = Mth.floor(var1.z);
         byte var6 = 16;

         for(int var7 = 0; var7 < 150; ++var7) {
            int var8 = var3 + this.random.nextInt(var6) - this.random.nextInt(var6);
            int var9 = var5 + this.random.nextInt(var6) - this.random.nextInt(var6);
            int var10 = var2.getLightDepth(var8, var9);
            int var11 = var2.getTile(var8, var10 - 1, var9);
            if (var10 <= var4 + var6 && var10 >= var4 - var6) {
               float var12 = this.random.nextFloat();
               float var13 = this.random.nextFloat();
               if (var11 > 0) {
                  this.mc.particleEngine.add(new WaterDropParticle(var2, (double)((float)var8 + var12), (double)((float)var10 + 0.1F) - Tile.tiles[var11].yy0, (double)((float)var9 + var13)));
               }
            }
         }

      }
   }

   protected void renderSnow(float var1) {
      LocalPlayer var2 = this.mc.player;
      Level var3 = this.mc.level;
      int var4 = Mth.floor(var2.x);
      int var5 = Mth.floor(var2.y);
      int var6 = Mth.floor(var2.z);
      Tesselator var7 = Tesselator.instance;
      GL11.glDisable(2884);
      GL11.glNormal3f(0.0F, 1.0F, 0.0F);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glBindTexture(3553, this.mc.textures.loadTexture("/environment/snow.png"));
      double var8 = var2.xOld + (var2.x - var2.xOld) * (double)var1;
      double var10 = var2.yOld + (var2.y - var2.yOld) * (double)var1;
      double var12 = var2.zOld + (var2.z - var2.zOld) * (double)var1;
      byte var14 = 5;
      if (this.mc.options.fancyGraphics) {
         var14 = 10;
      }

      for(int var15 = var4 - var14; var15 <= var4 + var14; ++var15) {
         for(int var16 = var6 - var14; var16 <= var6 + var14; ++var16) {
            int var17 = var3.getTopSolidBlock(var15, var16);
            if (var17 < 0) {
               var17 = 0;
            }

            int var18 = var5 - var14;
            int var19 = var5 + var14;
            if (var18 < var17) {
               var18 = var17;
            }

            if (var19 < var17) {
               var19 = var17;
            }

            float var20 = 2.0F;
            if (var18 != var19) {
               this.random.setSeed((long)(var15 * var15 * 3121 + var15 * 45238971 + var16 * var16 * 418711 + var16 * 13761));
               float var21 = (float)this.tick + var1;
               float var22 = ((float)(this.tick & 511) + var1) / 512.0F;
               float var23 = this.random.nextFloat() + var21 * 0.01F * (float)this.random.nextGaussian();
               float var24 = this.random.nextFloat() + var21 * (float)this.random.nextGaussian() * 0.001F;
               double var25 = (double)((float)var15 + 0.5F) - var2.x;
               double var27 = (double)((float)var16 + 0.5F) - var2.z;
               float var29 = Mth.sqrt(var25 * var25 + var27 * var27) / (float)var14;
               var7.begin();
               float var30 = var3.getBrightness(var15, 128, var16);
               GL11.glColor4f(var30, var30, var30, (1.0F - var29 * var29) * 0.7F);
               var7.offset(-var8 * (double)1.0F, -var10 * (double)1.0F, -var12 * (double)1.0F);
               var7.vertexUV((double)(var15 + 0), (double)var18, (double)(var16 + 0), (double)(0.0F * var20 + var23), (double)((float)var18 * var20 / 8.0F + var22 * var20 + var24));
               var7.vertexUV((double)(var15 + 1), (double)var18, (double)(var16 + 1), (double)(1.0F * var20 + var23), (double)((float)var18 * var20 / 8.0F + var22 * var20 + var24));
               var7.vertexUV((double)(var15 + 1), (double)var19, (double)(var16 + 1), (double)(1.0F * var20 + var23), (double)((float)var19 * var20 / 8.0F + var22 * var20 + var24));
               var7.vertexUV((double)(var15 + 0), (double)var19, (double)(var16 + 0), (double)(0.0F * var20 + var23), (double)((float)var19 * var20 / 8.0F + var22 * var20 + var24));
               var7.vertexUV((double)(var15 + 0), (double)var18, (double)(var16 + 1), (double)(0.0F * var20 + var23), (double)((float)var18 * var20 / 8.0F + var22 * var20 + var24));
               var7.vertexUV((double)(var15 + 1), (double)var18, (double)(var16 + 0), (double)(1.0F * var20 + var23), (double)((float)var18 * var20 / 8.0F + var22 * var20 + var24));
               var7.vertexUV((double)(var15 + 1), (double)var19, (double)(var16 + 0), (double)(1.0F * var20 + var23), (double)((float)var19 * var20 / 8.0F + var22 * var20 + var24));
               var7.vertexUV((double)(var15 + 0), (double)var19, (double)(var16 + 1), (double)(0.0F * var20 + var23), (double)((float)var19 * var20 / 8.0F + var22 * var20 + var24));
               var7.offset((double)0.0F, (double)0.0F, (double)0.0F);
               var7.end();
            }
         }
      }

      GL11.glEnable(2884);
      GL11.glDisable(3042);
   }

   protected void renderRain(float var1) {
      LocalPlayer var2 = this.mc.player;
      Level var3 = this.mc.level;
      int var4 = Mth.floor(var2.x);
      int var5 = Mth.floor(var2.y);
      int var6 = Mth.floor(var2.z);
      Tesselator var7 = Tesselator.instance;
      GL11.glDisable(2884);
      GL11.glNormal3f(0.0F, 1.0F, 0.0F);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glBindTexture(3553, this.mc.textures.loadTexture("/environment/rain.png"));
      double var8 = var2.xOld + (var2.x - var2.xOld) * (double)var1;
      double var10 = var2.yOld + (var2.y - var2.yOld) * (double)var1;
      double var12 = var2.zOld + (var2.z - var2.zOld) * (double)var1;
      byte var14 = 5;
      if (this.mc.options.fancyGraphics) {
         var14 = 10;
      }

      for(int var15 = var4 - var14; var15 <= var4 + var14; ++var15) {
         for(int var16 = var6 - var14; var16 <= var6 + var14; ++var16) {
            int var17 = var3.getLightDepth(var15, var16);
            int var18 = var5 - var14;
            int var19 = var5 + var14;
            if (var18 < var17) {
               var18 = var17;
            }

            if (var19 < var17) {
               var19 = var17;
            }

            float var20 = 2.0F;
            if (var18 != var19) {
               float var21 = ((float)(this.tick + var15 * var15 * 3121 + var15 * 45238971 + var16 * var16 * 418711 + var16 * 13761 & 31) + var1) / 32.0F;
               double var22 = (double)((float)var15 + 0.5F) - var2.x;
               double var24 = (double)((float)var16 + 0.5F) - var2.z;
               float var26 = Mth.sqrt(var22 * var22 + var24 * var24) / (float)var14;
               var7.begin();
               float var27 = var3.getBrightness(var15, 128, var16);
               GL11.glColor4f(var27, var27, var27, (1.0F - var26 * var26) * 0.7F);
               var7.offset(-var8 * (double)1.0F, -var10 * (double)1.0F, -var12 * (double)1.0F);
               var7.vertexUV((double)(var15 + 0), (double)var18, (double)(var16 + 0), (double)(0.0F * var20), (double)((float)var18 * var20 / 8.0F + var21 * var20));
               var7.vertexUV((double)(var15 + 1), (double)var18, (double)(var16 + 1), (double)(1.0F * var20), (double)((float)var18 * var20 / 8.0F + var21 * var20));
               var7.vertexUV((double)(var15 + 1), (double)var19, (double)(var16 + 1), (double)(1.0F * var20), (double)((float)var19 * var20 / 8.0F + var21 * var20));
               var7.vertexUV((double)(var15 + 0), (double)var19, (double)(var16 + 0), (double)(0.0F * var20), (double)((float)var19 * var20 / 8.0F + var21 * var20));
               var7.vertexUV((double)(var15 + 0), (double)var18, (double)(var16 + 1), (double)(0.0F * var20), (double)((float)var18 * var20 / 8.0F + var21 * var20));
               var7.vertexUV((double)(var15 + 1), (double)var18, (double)(var16 + 0), (double)(1.0F * var20), (double)((float)var18 * var20 / 8.0F + var21 * var20));
               var7.vertexUV((double)(var15 + 1), (double)var19, (double)(var16 + 0), (double)(1.0F * var20), (double)((float)var19 * var20 / 8.0F + var21 * var20));
               var7.vertexUV((double)(var15 + 0), (double)var19, (double)(var16 + 1), (double)(0.0F * var20), (double)((float)var19 * var20 / 8.0F + var21 * var20));
               var7.offset((double)0.0F, (double)0.0F, (double)0.0F);
               var7.end();
            }
         }
      }

      GL11.glEnable(2884);
      GL11.glDisable(3042);
   }

   public void setupGuiScreen() {
      ScreenSizeCalculator var1 = new ScreenSizeCalculator(this.mc.width, this.mc.height);
      int var2 = var1.getWidth();
      int var3 = var1.getHeight();
      GL11.glClear(256);
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      GL11.glOrtho((double)0.0F, (double)var2, (double)var3, (double)0.0F, (double)1000.0F, (double)3000.0F);
      GL11.glMatrixMode(5888);
      GL11.glLoadIdentity();
      GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
   }

   private void setupClearColor(float var1) {
      Level var2 = this.mc.level;
      LocalPlayer var3 = this.mc.player;
      float var4 = 1.0F / (float)(4 - this.mc.options.viewDistance);
      var4 = 1.0F - (float)Math.pow((double)var4, (double)0.25F);
      Vec3 var5 = var2.getSkyColor(this.mc.player, var1);
      float var6 = (float)var5.x;
      float var7 = (float)var5.y;
      float var8 = (float)var5.z;
      Vec3 var9 = var2.getFogColor(var1);
      this.fr = (float)var9.x;
      this.fg = (float)var9.y;
      this.fb = (float)var9.z;
      this.fr += (var6 - this.fr) * var4;
      this.fg += (var7 - this.fg) * var4;
      this.fb += (var8 - this.fb) * var4;
      if (((Player)var3).isUnderLiquid(Material.water)) {
         this.fr = 0.02F;
         this.fg = 0.02F;
         this.fb = 0.2F;
      } else if (((Player)var3).isUnderLiquid(Material.lava)) {
         this.fr = 0.6F;
         this.fg = 0.1F;
         this.fb = 0.0F;
      }

      float var10 = this.fogBrO + (this.fogBr - this.fogBrO) * var1;
      this.fr *= var10;
      this.fg *= var10;
      this.fb *= var10;
      if (this.mc.options.anaglyph3d) {
         float var11 = (this.fr * 30.0F + this.fg * 59.0F + this.fb * 11.0F) / 100.0F;
         float var12 = (this.fr * 30.0F + this.fg * 70.0F) / 100.0F;
         float var13 = (this.fr * 30.0F + this.fb * 70.0F) / 100.0F;
         this.fr = var11;
         this.fg = var12;
         this.fb = var13;
      }

      GL11.glClearColor(this.fr, this.fg, this.fb, 0.0F);
   }

   private void setupFog(int var1) {
      LocalPlayer var2 = this.mc.player;
      GL11.glFog(2918, this.getBuffer(this.fr, this.fg, this.fb, 1.0F));
      GL11.glNormal3f(0.0F, -1.0F, 0.0F);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      if (((Player)var2).isUnderLiquid(Material.water)) {
         GL11.glFogi(2917, 2048);
         GL11.glFogf(2914, 0.1F);
         float var3 = 0.4F;
         float var4 = 0.4F;
         float var5 = 0.9F;
         if (this.mc.options.anaglyph3d) {
            float var6 = (var3 * 30.0F + var4 * 59.0F + var5 * 11.0F) / 100.0F;
            float var7 = (var3 * 30.0F + var4 * 70.0F) / 100.0F;
            float var8 = (var3 * 30.0F + var5 * 70.0F) / 100.0F;
         }
      } else if (((Player)var2).isUnderLiquid(Material.lava)) {
         GL11.glFogi(2917, 2048);
         GL11.glFogf(2914, 2.0F);
         float var9 = 0.4F;
         float var10 = 0.3F;
         float var11 = 0.3F;
         if (this.mc.options.anaglyph3d) {
            float var12 = (var9 * 30.0F + var10 * 59.0F + var11 * 11.0F) / 100.0F;
            float var13 = (var9 * 30.0F + var10 * 70.0F) / 100.0F;
            float var14 = (var9 * 30.0F + var11 * 70.0F) / 100.0F;
         }
      } else {
         GL11.glFogi(2917, 9729);
         GL11.glFogf(2915, this.renderDistance * 0.25F);
         GL11.glFogf(2916, this.renderDistance);
         if (var1 < 0) {
            GL11.glFogf(2915, 0.0F);
            GL11.glFogf(2916, this.renderDistance * 0.8F);
         }

         if (GLContext.getCapabilities().GL_NV_fog_distance) {
            GL11.glFogi(34138, 34139);
         }

         if (this.mc.level.dimension.foggy) {
            GL11.glFogf(2915, 0.0F);
         }
      }

      GL11.glEnable(2903);
      GL11.glColorMaterial(1028, 4608);
   }

   private FloatBuffer getBuffer(float var1, float var2, float var3, float var4) {
      this.lb.clear();
      this.lb.put(var1).put(var2).put(var3).put(var4);
      this.lb.flip();
      return this.lb;
   }

   public void updateAllChunks() {
      this.mc.levelRenderer.updateDirtyChunks(this.mc.player, true);
   }
}
