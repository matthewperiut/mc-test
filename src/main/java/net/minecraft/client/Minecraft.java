package net.minecraft.client;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import net.minecraft.client.gamemode.CreativeMode;
import net.minecraft.client.gamemode.GameMode;
import net.minecraft.client.gamemode.SurvivalMode;
import net.minecraft.client.gui.DeathScreen;
import net.minecraft.client.gui.ErrorScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.LevelConflictScreen;
import net.minecraft.client.gui.PauseScreen;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ScreenSizeCalculator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.ClientConnection;
import net.minecraft.client.multiplayer.ConnectScreen;
import net.minecraft.client.multiplayer.MultiplayerLocalPlayer;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Chunk;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.ptexture.ClockTexture;
import net.minecraft.client.renderer.ptexture.CompassTexture;
import net.minecraft.client.renderer.ptexture.FireTexture;
import net.minecraft.client.renderer.ptexture.LavaSideTexture;
import net.minecraft.client.renderer.ptexture.LavaTexture;
import net.minecraft.client.renderer.ptexture.PortalTexture;
import net.minecraft.client.renderer.ptexture.WaterSideTexture;
import net.minecraft.client.renderer.ptexture.WaterTexture;
import net.minecraft.client.skins.TexturePackRepository;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.title.TitleScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelConflictException;
import net.minecraft.world.level.PortalForcer;
import net.minecraft.world.level.chunk.ChunkCache;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.HellDimension;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import util.Mth;

public abstract class Minecraft implements Runnable {
   public static final boolean FLYBY_MODE = false;
   public static final String VERSION_STRING = "Minecraft Beta 1.2_02";
   public GameMode gameMode;
   private boolean fullscreen = false;
   public int width;
   public int height;
   private OpenGLCapabilities openGLCapabilities;
   private Timer timer = new Timer(20.0F);
   public Level level;
   public LevelRenderer levelRenderer;
   public LocalPlayer player;
   public ParticleEngine particleEngine;
   public User user = null;
   public String serverDomain;
   public Canvas parent;
   public boolean appletMode = true;
   public volatile boolean pause = false;
   public Textures textures;
   public Font font;
   public Screen screen = null;
   public ProgressRenderer progressRenderer = new ProgressRenderer(this);
   public GameRenderer gameRenderer = new GameRenderer(this);
   private BackgroundDownloader bgLoader;
   private int ticks = 0;
   private int missTime = 0;
   private int orgWidth;
   private int orgHeight;
   public String autoLoad_user = null;
   public int autoLoad_id = 0;
   public Gui gui;
   public boolean noRender = false;
   public HumanoidModel humanoidModel = new HumanoidModel(0.0F);
   public HitResult hitResult = null;
   public Options options;
   protected MinecraftApplet minecraftApplet;
   public SoundEngine soundEngine = new SoundEngine();
   public MouseHandler mouseHandler;
   public TexturePackRepository skins;
   public File workingDirectory;
   public static long[] frameTimes = new long[512];
   public static long[] tickTimes = new long[512];
   public static int frameTimePos = 0;
   private String connectToIp;
   private int connectToPort;
   private WaterTexture waterTexture = new WaterTexture();
   private LavaTexture lavaTexture = new LavaTexture();
   private static File workDir = null;
   volatile boolean running = true;
   public String fpsString = "";
   boolean wasDown = false;
   long lastTimer = -1L;
   public boolean mouseGrabbed = false;
   private int lastClickTick = 0;
   public boolean isRaining = false;
   long lastTickTime = System.currentTimeMillis();
   private int recheckPlayerIn = 0;

   public Minecraft(Component var1, Canvas var2, MinecraftApplet var3, int var4, int var5, boolean var6) {
      this.orgHeight = var5;
      this.fullscreen = var6;
      this.minecraftApplet = var3;
      Thread var10001 = new Thread("Timer hack thread") {
         {
            this.setDaemon(true);
            this.start();
         }

         public void run() {
            while(Minecraft.this.running) {
               try {
                  Thread.sleep(2147483647L);
               } catch (InterruptedException var2) {
               }
            }

         }
      };
      this.parent = var2;
      this.width = var4;
      this.height = var5;
      this.fullscreen = var6;
   }

   public abstract void onCrash(CrashReport var1);

   public void connectTo(String var1, int var2) {
      this.connectToIp = var1;
      this.connectToPort = var2;
   }

   public void init() throws LWJGLException, IOException {
      if (this.parent != null) {
         Graphics var1 = this.parent.getGraphics();
         if (var1 != null) {
            var1.setColor(Color.BLACK);
            var1.fillRect(0, 0, this.width, this.height);
            var1.dispose();
         }

         Display.setParent(this.parent);
      } else if (this.fullscreen) {
         Display.setFullscreen(true);
         this.width = Display.getDisplayMode().getWidth();
         this.height = Display.getDisplayMode().getHeight();
         if (this.width <= 0) {
            this.width = 1;
         }

         if (this.height <= 0) {
            this.height = 1;
         }
      } else {
         Display.setDisplayMode(new DisplayMode(this.width, this.height));
      }

      Display.setTitle("Minecraft Minecraft Beta 1.2_02");

      try {
         Display.create();
      } catch (LWJGLException var6) {
         var6.printStackTrace();

         try {
            Thread.sleep(1000L);
         } catch (InterruptedException var5) {
         }

         Display.create();
      }

      EntityRenderDispatcher.instance.itemInHandRenderer = new ItemInHandRenderer(this);
      this.workingDirectory = getWorkingDirectory();
      this.options = new Options(this, this.workingDirectory);
      this.skins = new TexturePackRepository(this, this.workingDirectory);
      this.textures = new Textures(this.skins, this.options);
      this.font = new Font(this.options, "/font/default.png", this.textures);
      this.renderLoadingScreen();
      Keyboard.create();
      Mouse.create();
      this.mouseHandler = new MouseHandler(this.parent);

      try {
         Controllers.create();
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      this.checkGlError("Pre startup");
      GL11.glEnable(3553);
      GL11.glShadeModel(7425);
      GL11.glClearDepth((double)1.0F);
      GL11.glEnable(2929);
      GL11.glDepthFunc(515);
      GL11.glEnable(3008);
      GL11.glAlphaFunc(516, 0.1F);
      GL11.glCullFace(1029);
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      GL11.glMatrixMode(5888);
      this.checkGlError("Startup");
      this.openGLCapabilities = new OpenGLCapabilities();
      this.soundEngine.init(this.options);
      this.textures.addDynamicTexture(this.lavaTexture);
      this.textures.addDynamicTexture(this.waterTexture);
      this.textures.addDynamicTexture(new PortalTexture());
      this.textures.addDynamicTexture(new CompassTexture(this));
      this.textures.addDynamicTexture(new ClockTexture(this));
      this.textures.addDynamicTexture(new WaterSideTexture());
      this.textures.addDynamicTexture(new LavaSideTexture());
      this.textures.addDynamicTexture(new FireTexture(0));
      this.textures.addDynamicTexture(new FireTexture(1));
      this.levelRenderer = new LevelRenderer(this, this.textures);
      GL11.glViewport(0, 0, this.width, this.height);
      this.particleEngine = new ParticleEngine(this.level, this.textures);

      try {
         this.bgLoader = new BackgroundDownloader(this.workingDirectory, this);
         this.bgLoader.start();
      } catch (Exception var3) {
      }

      this.checkGlError("Post startup");
      this.gui = new Gui(this);
      if (this.connectToIp != null) {
         this.setScreen(new ConnectScreen(this, this.connectToIp, this.connectToPort));
      } else {
         this.setScreen(new TitleScreen());
      }

   }

   private void renderLoadingScreen() throws LWJGLException {
      ScreenSizeCalculator var1 = new ScreenSizeCalculator(this.width, this.height);
      int var2 = var1.getWidth();
      int var3 = var1.getHeight();
      GL11.glClear(16640);
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      GL11.glOrtho((double)0.0F, (double)var2, (double)var3, (double)0.0F, (double)1000.0F, (double)3000.0F);
      GL11.glMatrixMode(5888);
      GL11.glLoadIdentity();
      GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
      GL11.glViewport(0, 0, this.width, this.height);
      GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      Tesselator var4 = Tesselator.instance;
      GL11.glDisable(2896);
      GL11.glEnable(3553);
      GL11.glDisable(2912);
      GL11.glBindTexture(3553, this.textures.loadTexture("/title/mojang.png"));
      var4.begin();
      var4.color(16777215);
      var4.vertexUV((double)0.0F, (double)this.height, (double)0.0F, (double)0.0F, (double)0.0F);
      var4.vertexUV((double)this.width, (double)this.height, (double)0.0F, (double)0.0F, (double)0.0F);
      var4.vertexUV((double)this.width, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
      var4.vertexUV((double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
      var4.end();
      short var5 = 256;
      short var6 = 256;
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      var4.color(16777215);
      this.blit((this.width / 2 - var5) / 2, (this.height / 2 - var6) / 2, 0, 0, var5, var6);
      GL11.glDisable(2896);
      GL11.glDisable(2912);
      GL11.glEnable(3008);
      GL11.glAlphaFunc(516, 0.1F);
      Display.swapBuffers();
   }

   public void blit(int var1, int var2, int var3, int var4, int var5, int var6) {
      float var7 = 0.00390625F;
      float var8 = 0.00390625F;
      Tesselator var9 = Tesselator.instance;
      var9.begin();
      var9.vertexUV((double)(var1 + 0), (double)(var2 + var6), (double)0.0F, (double)((float)(var3 + 0) * var7), (double)((float)(var4 + var6) * var8));
      var9.vertexUV((double)(var1 + var5), (double)(var2 + var6), (double)0.0F, (double)((float)(var3 + var5) * var7), (double)((float)(var4 + var6) * var8));
      var9.vertexUV((double)(var1 + var5), (double)(var2 + 0), (double)0.0F, (double)((float)(var3 + var5) * var7), (double)((float)(var4 + 0) * var8));
      var9.vertexUV((double)(var1 + 0), (double)(var2 + 0), (double)0.0F, (double)((float)(var3 + 0) * var7), (double)((float)(var4 + 0) * var8));
      var9.end();
   }

   public static File getWorkingDirectory() {
      if (workDir == null) {
         workDir = getWorkingDirectory("minecraft");
      }

      return workDir;
   }

   public static File getWorkingDirectory(String var0) {
      String var1 = System.getProperty("user.home", ".");
      File var2;
      switch (getPlatform()) {
         case linux:
         case solaris:
            var2 = new File(var1, '.' + var0 + '/');
            break;
         case windows:
            String var3 = System.getenv("APPDATA");
            if (var3 != null) {
               var2 = new File(var3, "." + var0 + '/');
            } else {
               var2 = new File(var1, '.' + var0 + '/');
            }
            break;
         case macos:
            var2 = new File(var1, "Library/Application Support/" + var0);
            break;
         default:
            var2 = new File(var1, var0 + '/');
      }

      if (!var2.exists() && !var2.mkdirs()) {
         throw new RuntimeException("The working directory could not be created: " + var2);
      } else {
         return var2;
      }
   }

   private static OS getPlatform() {
      String var0 = System.getProperty("os.name").toLowerCase();
      if (var0.contains("win")) {
         return Minecraft.OS.windows;
      } else if (var0.contains("mac")) {
         return Minecraft.OS.macos;
      } else if (var0.contains("solaris")) {
         return Minecraft.OS.solaris;
      } else if (var0.contains("sunos")) {
         return Minecraft.OS.solaris;
      } else if (var0.contains("linux")) {
         return Minecraft.OS.linux;
      } else {
         return var0.contains("unix") ? Minecraft.OS.linux : Minecraft.OS.unknown;
      }
   }

   public void setScreen(Screen var1) {
      if (!(this.screen instanceof ErrorScreen)) {
         if (this.screen != null) {
            this.screen.removed();
         }

         if (var1 == null && this.level == null) {
            var1 = new TitleScreen();
         } else if (var1 == null && this.player.health <= 0) {
            var1 = new DeathScreen();
         }

         this.screen = (Screen)var1;
         if (var1 != null) {
            this.releaseMouse();
            ScreenSizeCalculator var2 = new ScreenSizeCalculator(this.width, this.height);
            int var3 = var2.getWidth();
            int var4 = var2.getHeight();
            ((Screen)var1).init(this, var3, var4);
            this.noRender = false;
         } else {
            this.grabMouse();
         }

      }
   }

   private void checkGlError(String var1) {
      int var2 = GL11.glGetError();
      if (var2 != 0) {
         String var3 = GLU.gluErrorString(var2);
         System.out.println("########## GL ERROR ##########");
         System.out.println("@ " + var1);
         System.out.println(var2 + ": " + var3);
         System.exit(0);
      }

   }

   public void destroy() {
      if (this.minecraftApplet != null) {
         this.minecraftApplet.clearMemory();
      }

      try {
         if (this.bgLoader != null) {
            this.bgLoader.halt();
         }
      } catch (Exception var8) {
      }

      try {
         System.out.println("Stopping!");
         this.setLevel((Level)null);

         try {
            MemoryTracker.release();
         } catch (Exception var6) {
         }

         this.soundEngine.destroy();
         Mouse.destroy();
         Keyboard.destroy();
      } finally {
         Display.destroy();
      }

      System.gc();
   }

   public void generateFlyby() {
      this.gameMode = new SurvivalMode(this);
      this.selectLevel("flyby");
      this.setScreen((Screen)null);
      double var1 = (double)0.0F;
      ByteBuffer var3 = BufferUtils.createByteBuffer(this.width * this.height * 3);
      File var4 = new File(getWorkingDirectory(), "flyby");
      var4.mkdir();
      byte[] var5 = new byte[18];
      var5[2] = 2;
      var5[12] = (byte)(this.width % 256);
      var5[13] = (byte)(this.width / 256);
      var5[14] = (byte)(this.height % 256);
      var5[15] = (byte)(this.height / 256);
      var5[16] = 24;
      byte[] var6 = new byte[this.width * this.height * 3];
      int var7 = -20;
      short var8 = 352;
      int var9 = var8 * 60;
      this.player.yRot = this.player.yRotO = 12.0F;
      double var10 = -Math.sin((double)this.player.yRot * Math.PI / (double)180.0F);
      double var12 = Math.cos((double)this.player.yRot * Math.PI / (double)180.0F);
      this.player.x = this.player.xo = this.player.xOld = (double)0.0F;
      this.player.z = this.player.zo = this.player.zOld = (double)0.0F;

      for(this.level.time = 0L; var7 < var9; ++var7) {
         if (var7 % 100 == 0) {
            System.out.println((double)var7 * (double)100.0F / (double)var9 + "%, free: " + (float)(Runtime.getRuntime().freeMemory() / 1024L) / 1024.0F + " MB");
            System.gc();
         }

         double var14 = (double)0.125F + (double)var7 / (double)var9 * (double)5.0F;
         AABB.resetPool();
         Vec3.resetPool();
         if (var7 < 0) {
            this.level.setSpawnSettings(this.options.difficulty > 0, true);
            this.level.tick();
         }

         this.gameRenderer.tick();
         GL11.glEnable(3553);

         while(this.level.updateLights()) {
         }

         LocalPlayer var10002 = this.player;
         this.player.x = this.player.xo = var10002.xOld += var10 * var14;
         var10002 = this.player;
         this.player.z = this.player.zo = var10002.zOld += var12 * var14;
         byte var16 = 100;
         double var17 = (double)0.0F;
         double var19 = (double)1.0F;

         for(double var21 = (double)-4.0F; var21 < (double)var16; var21 += var19) {
            for(int var23 = 0; var23 < 9; ++var23) {
               double var24 = (double)((float)(var23 % 3) / 2.0F) - (double)0.5F;
               double var26 = (double)((float)(var23 / 3) / 2.0F) - (double)0.5F;
               double var28 = (double)this.level.getHeightmap(Mth.floor(this.player.x + var10 * var21 + var24), Mth.floor(this.player.z + var12 * var21 + var26));
               if (var28 > var17) {
                  var17 = var28;
               }
            }
         }

         double var31 = var17 + (double)4.0F;
         if (var1 == (double)0.0F) {
            var1 = var31;
         } else {
            var1 += (var31 - var1) * var14 / (double)var16 * (double)4.0F;
         }

         this.player.xRot = this.player.xRotO = (float)(var1 - (double)64.0F) / 2.0F;
         this.player.y = this.player.yo = this.player.yOld = var1;
         this.gameRenderer.renderLevel(1.0F);
         GL11.glBindTexture(3553, this.textures.loadTexture("/terrain.png"));
         this.textures.tick();
         Display.update();
         var3.clear();
         GL11.glReadPixels(0, 0, this.width, this.height, 32992, 5121, var3);
         var3.clear();
         if (var7 >= 0) {
            String var32;
            for(var32 = "" + var7; var32.length() < 6; var32 = "0" + var32) {
            }

            try {
               var3.get(var6);
               File var33 = new File(var4, "img" + var32 + ".tga");
               DataOutputStream var25 = new DataOutputStream(new FileOutputStream(var33));
               var25.write(var5);
               var25.write(var6);
               var25.close();
            } catch (Exception var30) {
               var30.printStackTrace();
            }
         }
      }

   }

   public void run() {
      this.running = true;

      try {
         this.init();
      } catch (Exception var15) {
         var15.printStackTrace();
         this.onCrash(new CrashReport("Failed to start game", var15));
         return;
      }

      try {
         try {
            long var1 = System.currentTimeMillis();
            int var3 = 0;

            while(this.running && (this.minecraftApplet == null || this.minecraftApplet.isActive())) {
               AABB.resetPool();
               Vec3.resetPool();
               if (this.parent == null && Display.isCloseRequested()) {
                  this.stop();
               }

               if (this.pause && this.level != null) {
                  float var4 = this.timer.a;
                  this.timer.advanceTime();
                  this.timer.a = var4;
               } else {
                  this.timer.advanceTime();
               }

               long var19 = System.nanoTime();

               for(int var6 = 0; var6 < this.timer.ticks; ++var6) {
                  ++this.ticks;

                  try {
                     this.tick();
                  } catch (LevelConflictException var14) {
                     this.level = null;
                     this.setLevel((Level)null);
                     this.setScreen(new LevelConflictScreen());
                  }
               }

               long var20 = System.nanoTime() - var19;
               this.checkGlError("Pre render");
               this.soundEngine.update(this.player, this.timer.a);
               GL11.glEnable(3553);
               if (this.level != null && !this.level.isOnline) {
                  this.level.updateLights();
               }

               if (this.level != null && this.level.isOnline) {
                  this.level.updateLights();
               }

               if (this.options.limitFramerate) {
                  Thread.sleep(5L);
               }

               if (!Keyboard.isKeyDown(65)) {
                  Display.update();
               }

               if (!this.noRender) {
                  if (this.gameMode != null) {
                     this.gameMode.render(this.timer.a);
                  }

                  this.gameRenderer.render(this.timer.a);
               }

               if (!Display.isActive()) {
                  if (this.fullscreen) {
                     this.toggleFullScreen();
                  }

                  Thread.sleep(10L);
               }

               if (Keyboard.isKeyDown(61)) {
                  this.renderFpsMeter(var20);
               } else {
                  this.lastTimer = System.nanoTime();
               }

               Thread.yield();
               if (Keyboard.isKeyDown(65)) {
                  Display.update();
               }

               this.checkScreenshot();
               if (this.parent != null && !this.fullscreen && (this.parent.getWidth() != this.width || this.parent.getHeight() != this.height)) {
                  this.width = this.parent.getWidth();
                  this.height = this.parent.getHeight();
                  if (this.width <= 0) {
                     this.width = 1;
                  }

                  if (this.height <= 0) {
                     this.height = 1;
                  }

                  this.resize(this.width, this.height);
               }

               this.checkGlError("Post render");
               ++var3;

               for(this.pause = !this.isOnline() && this.screen != null && this.screen.isPauseScreen(); System.currentTimeMillis() >= var1 + 1000L; var3 = 0) {
                  this.fpsString = var3 + " fps, " + Chunk.updates + " chunk updates";
                  Chunk.updates = 0;
                  var1 += 1000L;
               }
            }
         } catch (StopGameException var16) {
         } catch (Throwable var17) {
            this.level = null;
            var17.printStackTrace();
            this.onCrash(new CrashReport("Unexpected error", var17));
         }

      } finally {
         ;
      }
   }

   private void checkScreenshot() {
      if (Keyboard.isKeyDown(60)) {
         if (!this.wasDown) {
            this.wasDown = true;
            if (Keyboard.isKeyDown(42)) {
               this.gui.addMessage(this.grabHugeScreenshot(workDir, this.width, this.height, 36450, 17700));
            } else {
               this.gui.addMessage(Screenshot.grab(workDir, this.width, this.height));
            }
         }
      } else {
         this.wasDown = false;
      }

   }

   private String grabHugeScreenshot(File var1, int var2, int var3, int var4, int var5) {
      try {
         ByteBuffer var6 = BufferUtils.createByteBuffer(var2 * var3 * 3);
         Screenshot var7 = new Screenshot(var1, var4, var5, var3);
         double var8 = (double)var4 / (double)var2;
         double var10 = (double)var5 / (double)var3;
         double var12 = var8 > var10 ? var8 : var10;

         for(int var14 = (var5 - 1) / var3 * var3; var14 >= 0; var14 -= var3) {
            for(int var15 = 0; var15 < var4; var15 += var2) {
               GL11.glBindTexture(3553, this.textures.loadTexture("/terrain.png"));
               double var18 = (double)(var4 - var2) / (double)2.0F * (double)2.0F - (double)(var15 * 2);
               double var20 = (double)(var5 - var3) / (double)2.0F * (double)2.0F - (double)(var14 * 2);
               var18 /= (double)var2;
               var20 /= (double)var3;
               this.gameRenderer.zoomRegion(var12, var18, var20);
               this.gameRenderer.renderLevel(1.0F);
               this.gameRenderer.unZoomRegion();
               Display.update();

               try {
                  Thread.sleep(10L);
               } catch (InterruptedException var23) {
                  var23.printStackTrace();
               }

               Display.update();
               var6.clear();
               GL11.glPixelStorei(3333, 1);
               GL11.glPixelStorei(3317, 1);
               GL11.glReadPixels(0, 0, var2, var3, 32992, 5121, var6);
               var7.addRegion(var6, var15, var14, var2, var3);
            }

            var7.saveRow();
         }

         return var7.close();
      } catch (Exception var24) {
         var24.printStackTrace();
         return "Failed to save image: " + var24;
      }
   }

   private void renderFpsMeter(long var1) {
      long var3 = 16666666L;
      if (this.lastTimer == -1L) {
         this.lastTimer = System.nanoTime();
      }

      long var5 = System.nanoTime();
      tickTimes[frameTimePos & frameTimes.length - 1] = var1;
      frameTimes[frameTimePos++ & frameTimes.length - 1] = var5 - this.lastTimer;
      this.lastTimer = var5;
      GL11.glClear(256);
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      GL11.glOrtho((double)0.0F, (double)this.width, (double)this.height, (double)0.0F, (double)1000.0F, (double)3000.0F);
      GL11.glMatrixMode(5888);
      GL11.glLoadIdentity();
      GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
      GL11.glLineWidth(1.0F);
      GL11.glDisable(3553);
      Tesselator var7 = Tesselator.instance;
      var7.begin(7);
      int var8 = (int)(var3 / 200000L);
      var7.color(536870912);
      var7.vertex((double)0.0F, (double)(this.height - var8), (double)0.0F);
      var7.vertex((double)0.0F, (double)this.height, (double)0.0F);
      var7.vertex((double)frameTimes.length, (double)this.height, (double)0.0F);
      var7.vertex((double)frameTimes.length, (double)(this.height - var8), (double)0.0F);
      var7.color(538968064);
      var7.vertex((double)0.0F, (double)(this.height - var8 * 2), (double)0.0F);
      var7.vertex((double)0.0F, (double)(this.height - var8), (double)0.0F);
      var7.vertex((double)frameTimes.length, (double)(this.height - var8), (double)0.0F);
      var7.vertex((double)frameTimes.length, (double)(this.height - var8 * 2), (double)0.0F);
      var7.end();
      long var9 = 0L;

      for(int var11 = 0; var11 < frameTimes.length; ++var11) {
         var9 += frameTimes[var11];
      }

      int var20 = (int)(var9 / 200000L / (long)frameTimes.length);
      var7.begin(7);
      var7.color(541065216);
      var7.vertex((double)0.0F, (double)(this.height - var20), (double)0.0F);
      var7.vertex((double)0.0F, (double)this.height, (double)0.0F);
      var7.vertex((double)frameTimes.length, (double)this.height, (double)0.0F);
      var7.vertex((double)frameTimes.length, (double)(this.height - var20), (double)0.0F);
      var7.end();
      var7.begin(1);

      for(int var12 = 0; var12 < frameTimes.length; ++var12) {
         int var13 = (var12 - frameTimePos & frameTimes.length - 1) * 255 / frameTimes.length;
         int var14 = var13 * var13 / 255;
         var14 = var14 * var14 / 255;
         int var15 = var14 * var14 / 255;
         var15 = var15 * var15 / 255;
         if (frameTimes[var12] > var3) {
            var7.color(-16777216 + var14 * 65536);
         } else {
            var7.color(-16777216 + var14 * 256);
         }

         long var16 = frameTimes[var12] / 200000L;
         long var18 = tickTimes[var12] / 200000L;
         var7.vertex((double)((float)var12 + 0.5F), (double)((float)((long)this.height - var16) + 0.5F), (double)0.0F);
         var7.vertex((double)((float)var12 + 0.5F), (double)((float)this.height + 0.5F), (double)0.0F);
         var7.color(-16777216 + var14 * 65536 + var14 * 256 + var14 * 1);
         var7.vertex((double)((float)var12 + 0.5F), (double)((float)((long)this.height - var16) + 0.5F), (double)0.0F);
         var7.vertex((double)((float)var12 + 0.5F), (double)((float)((long)this.height - (var16 - var18)) + 0.5F), (double)0.0F);
      }

      var7.end();
      GL11.glEnable(3553);
   }

   public void stop() {
      this.running = false;
   }

   public void grabMouse() {
      if (Display.isActive()) {
         if (!this.mouseGrabbed) {
            this.mouseGrabbed = true;
            this.mouseHandler.grab();
            this.setScreen((Screen)null);
            this.lastClickTick = this.ticks + 10000;
         }
      }
   }

   public void releaseMouse() {
      if (this.mouseGrabbed) {
         if (this.player != null) {
            this.player.releaseAllKeys();
         }

         this.mouseGrabbed = false;
         this.mouseHandler.release();
      }
   }

   public void pauseGame() {
      if (this.screen == null) {
         this.setScreen(new PauseScreen());
      }
   }

   private void handleMouseDown(int var1, boolean var2) {
      if (!this.gameMode.instaBuild) {
         if (var1 != 0 || this.missTime <= 0) {
            if (var2 && this.hitResult != null && this.hitResult.type == HitResult.Type.TILE && var1 == 0) {
               int var3 = this.hitResult.x;
               int var4 = this.hitResult.y;
               int var5 = this.hitResult.z;
               this.gameMode.continueDestroyBlock(var3, var4, var5, this.hitResult.f);
               this.particleEngine.crack(var3, var4, var5, this.hitResult.f);
            } else {
               this.gameMode.stopDestroyBlock();
            }

         }
      }
   }

   private void handleMouseClick(int var1) {
      if (var1 != 0 || this.missTime <= 0) {
         if (var1 == 0) {
            this.player.swing();
         }

         boolean var2 = true;
         if (this.hitResult == null) {
            if (var1 == 0 && !(this.gameMode instanceof CreativeMode)) {
               this.missTime = 10;
            }
         } else if (this.hitResult.type == HitResult.Type.ENTITY) {
            if (var1 == 0) {
               this.gameMode.attack(this.player, this.hitResult.entity);
            }

            if (var1 == 1) {
               this.gameMode.interact(this.player, this.hitResult.entity);
            }
         } else if (this.hitResult.type == HitResult.Type.TILE) {
            int var3 = this.hitResult.x;
            int var4 = this.hitResult.y;
            int var5 = this.hitResult.z;
            int var6 = this.hitResult.f;
            Tile var7 = Tile.tiles[this.level.getTile(var3, var4, var5)];
            if (var1 == 0) {
               this.level.extinguishFire(var3, var4, var5, this.hitResult.f);
               if (var7 != Tile.unbreakable || this.player.userType >= 100) {
                  this.gameMode.startDestroyBlock(var3, var4, var5, this.hitResult.f);
               }
            } else {
               ItemInstance var8 = this.player.inventory.getSelected();
               int var9 = var8 != null ? var8.count : 0;
               if (this.gameMode.useItemOn(this.player, this.level, var8, var3, var4, var5, var6)) {
                  var2 = false;
                  this.player.swing();
               }

               if (var8 == null) {
                  return;
               }

               if (var8.count == 0) {
                  this.player.inventory.items[this.player.inventory.selected] = null;
               } else if (var8.count != var9) {
                  this.gameRenderer.itemInHandRenderer.itemPlaced();
               }
            }
         }

         if (var2 && var1 == 1) {
            ItemInstance var10 = this.player.inventory.getSelected();
            if (var10 != null && this.gameMode.useItem(this.player, this.level, var10)) {
               this.gameRenderer.itemInHandRenderer.itemUsed();
            }
         }

      }
   }

   public void toggleFullScreen() {
      try {
         this.fullscreen = !this.fullscreen;
         System.out.println("Toggle fullscreen!");
         if (this.fullscreen) {
            Display.setDisplayMode(Display.getDesktopDisplayMode());
            this.width = Display.getDisplayMode().getWidth();
            this.height = Display.getDisplayMode().getHeight();
            if (this.width <= 0) {
               this.width = 1;
            }

            if (this.height <= 0) {
               this.height = 1;
            }
         } else {
            if (this.parent != null) {
               this.width = this.parent.getWidth();
               this.height = this.parent.getHeight();
            } else {
               this.width = this.orgWidth;
               this.height = this.orgHeight;
            }

            if (this.width <= 0) {
               this.width = 1;
            }

            if (this.height <= 0) {
               this.height = 1;
            }

            Display.setDisplayMode(new DisplayMode(this.orgWidth, this.orgHeight));
         }

         this.releaseMouse();
         Display.setFullscreen(this.fullscreen);
         Display.update();
         Thread.sleep(1000L);
         if (this.fullscreen) {
            this.grabMouse();
         }

         if (this.screen != null) {
            this.releaseMouse();
            this.resize(this.width, this.height);
         }

         System.out.println("Size: " + this.width + ", " + this.height);
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }

   private void resize(int var1, int var2) {
      if (var1 <= 0) {
         var1 = 1;
      }

      if (var2 <= 0) {
         var2 = 1;
      }

      this.width = var1;
      this.height = var2;
      if (this.screen != null) {
         ScreenSizeCalculator var3 = new ScreenSizeCalculator(var1, var2);
         int var4 = var3.getWidth();
         int var5 = var3.getHeight();
         this.screen.init(this, var4, var5);
      }

   }

   private void handleGrabTexture() {
      if (this.hitResult != null) {
         int var1 = this.level.getTile(this.hitResult.x, this.hitResult.y, this.hitResult.z);
         if (var1 == Tile.grass.id) {
            var1 = Tile.dirt.id;
         }

         if (var1 == Tile.stoneSlab.id) {
            var1 = Tile.stoneSlabHalf.id;
         }

         if (var1 == Tile.unbreakable.id) {
            var1 = Tile.rock.id;
         }

         this.player.inventory.grabTexture(var1, this.gameMode instanceof CreativeMode);
      }

   }

   public void tick() {
      this.gui.tick();
      this.gameRenderer.pick(1.0F);
      if (this.player != null) {
         this.player.prepareForTick();
         ChunkSource var1 = this.level.getChunkSource();
         if (var1 instanceof ChunkCache) {
            ChunkCache var2 = (ChunkCache)var1;
            int var3 = Mth.floor((float)((int)this.player.x)) >> 4;
            int var4 = Mth.floor((float)((int)this.player.z)) >> 4;
            var2.centerOn(var3, var4);
         }
      }

      if (!this.pause && this.level != null) {
         this.gameMode.tick();
      }

      GL11.glBindTexture(3553, this.textures.loadTexture("/terrain.png"));
      if (!this.pause) {
         this.textures.tick();
      }

      if (this.screen == null && this.player != null && this.player.health <= 0) {
         this.setScreen((Screen)null);
      }

      if (this.screen != null) {
         this.lastClickTick = this.ticks + 10000;
      }

      if (this.screen != null) {
         this.screen.updateEvents();
         if (this.screen != null) {
            this.screen.tick();
         }
      }

      if (this.screen == null || this.screen.passEvents) {
         while(Mouse.next()) {
            long var5 = System.currentTimeMillis() - this.lastTickTime;
            if (var5 <= 200L) {
               int var7 = Mouse.getEventDWheel();
               if (var7 != 0) {
                  this.player.inventory.swapPaint(var7);
               }

               if (this.screen == null) {
                  if (!this.mouseGrabbed && Mouse.getEventButtonState()) {
                     this.grabMouse();
                  } else {
                     if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
                        this.handleMouseClick(0);
                        this.lastClickTick = this.ticks;
                     }

                     if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
                        this.handleMouseClick(1);
                        this.lastClickTick = this.ticks;
                     }

                     if (Mouse.getEventButton() == 2 && Mouse.getEventButtonState()) {
                        this.handleGrabTexture();
                     }
                  }
               } else if (this.screen != null) {
                  this.screen.mouseEvent();
               }
            }
         }

         if (this.missTime > 0) {
            --this.missTime;
         }

         while(Keyboard.next()) {
            this.player.setKey(Keyboard.getEventKey(), Keyboard.getEventKeyState());
            if (Keyboard.getEventKeyState()) {
               if (Keyboard.getEventKey() == 87) {
                  this.toggleFullScreen();
               } else {
                  if (this.screen != null) {
                     this.screen.keyboardEvent();
                  } else {
                     if (Keyboard.getEventKey() == 1) {
                        this.pauseGame();
                     }

                     if (Keyboard.getEventKey() == 31 && Keyboard.isKeyDown(61)) {
                        this.reloadSound();
                     }

                     if (Keyboard.getEventKey() == 63) {
                        this.options.thirdPersonView = !this.options.thirdPersonView;
                     }

                     // Open inventory with inventory key (E by default)
                     if (Keyboard.getEventKey() == this.options.keyInventory.key && this.player != null) {
                        this.setScreen(new net.minecraft.client.gui.inventory.InventoryScreen(this.player));
                     }

                     // Drop item with drop key (Q by default)
                     if (Keyboard.getEventKey() == this.options.keyDrop.key && this.player != null) {
                        this.player.drop();
                     }
                  }

                  for(int var6 = 0; var6 < 9; ++var6) {
                     if (Keyboard.getEventKey() == 2 + var6) {
                        this.player.inventory.selected = var6;
                     }
                  }

                  if (Keyboard.getEventKey() == this.options.keyFog.key) {
                     this.options.toggle(Options.Option.RENDER_DISTANCE, !Keyboard.isKeyDown(42) && !Keyboard.isKeyDown(54) ? 1 : -1);
                  }
               }
            }
         }

         if (this.screen == null) {
            if (Mouse.isButtonDown(0) && (float)(this.ticks - this.lastClickTick) >= this.timer.ticksPerSecond / 4.0F && this.mouseGrabbed) {
               this.handleMouseClick(0);
               this.lastClickTick = this.ticks;
            }

            if (Mouse.isButtonDown(1) && (float)(this.ticks - this.lastClickTick) >= this.timer.ticksPerSecond / 4.0F && this.mouseGrabbed) {
               this.handleMouseClick(1);
               this.lastClickTick = this.ticks;
            }
         }

         this.handleMouseDown(0, this.screen == null && Mouse.isButtonDown(0) && this.mouseGrabbed);
      }

      if (this.level != null) {
         if (this.player != null) {
            ++this.recheckPlayerIn;
            if (this.recheckPlayerIn == 30) {
               this.recheckPlayerIn = 0;
               this.level.ensureAdded(this.player);
            }
         }

         this.level.difficulty = this.options.difficulty;
         if (this.level.isOnline) {
            this.level.difficulty = 3;
         }

         if (!this.pause) {
            this.gameRenderer.tick();
         }

         if (!this.pause) {
            this.levelRenderer.tick();
         }

         if (!this.pause) {
            this.level.tickEntities();
         }

         if (!this.pause || this.isOnline()) {
            this.level.setSpawnSettings(this.options.difficulty > 0, true);
            this.level.tick();
         }

         if (!this.pause && this.level != null) {
            this.level.animateTick(Mth.floor(this.player.x), Mth.floor(this.player.y), Mth.floor(this.player.z));
         }

         if (!this.pause) {
            this.particleEngine.tick();
         }
      }

      this.lastTickTime = System.currentTimeMillis();
   }

   private void reloadSound() {
      System.out.println("FORCING RELOAD!");
      this.soundEngine = new SoundEngine();
      this.soundEngine.init(this.options);
      this.bgLoader.forceReload();
   }

   public boolean isOnline() {
      return this.level != null && this.level.isOnline;
   }

   public static void checkError() {
   }

   public void selectLevel(String var1) {
      this.setLevel((Level)null);
      System.gc();
      Level var2 = new Level(new File(getWorkingDirectory(), "saves"), var1);
      if (var2.isNew) {
         this.setLevel(var2, "Generating level");
      } else {
         this.setLevel(var2, "Loading level");
      }

   }

   public void toggleDimension() {
      if (this.player.dimension == -1) {
         this.player.dimension = 0;
      } else {
         this.player.dimension = -1;
      }

      this.level.removeEntity(this.player);
      this.player.removed = false;
      double var1 = this.player.x;
      double var3 = this.player.z;
      double var5 = (double)8.0F;
      if (this.player.dimension == -1) {
         var1 /= var5;
         var3 /= var5;
         this.player.moveTo(var1, this.player.y, var3, this.player.yRot, this.player.xRot);
         this.level.tick(this.player, false);
         Level var7 = new Level(this.level, new HellDimension());
         this.setLevel(var7, "Entering the Nether", this.player);
      } else {
         var1 *= var5;
         var3 *= var5;
         this.player.moveTo(var1, this.player.y, var3, this.player.yRot, this.player.xRot);
         this.level.tick(this.player, false);
         Level var10 = new Level(this.level, new Dimension());
         this.setLevel(var10, "Leaving the Nether", this.player);
      }

      this.player.level = this.level;
      this.player.moveTo(var1, this.player.y, var3, this.player.yRot, this.player.xRot);
      this.level.tick(this.player, false);
      (new PortalForcer()).force(this.level, this.player);
   }

   public boolean saveSlot(int var1, String var2) {
      return false;
   }

   public boolean loadSlot(String var1, int var2) {
      return false;
   }

   public void setLevel(Level var1) {
      this.setLevel(var1, "");
   }

   public void setLevel(Level var1, String var2) {
      this.setLevel(var1, var2, (Player)null);
   }

   public void setLevel(Level var1, String var2, Player var3) {
      this.progressRenderer.progressStart(var2);
      this.progressRenderer.progressStage("");
      this.soundEngine.playStreaming((String)null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      if (this.level != null) {
         this.level.forceSave(this.progressRenderer);
      }

      this.level = var1;
      System.out.println("Player is " + this.player);
      if (var1 != null) {
         this.gameMode.initLevel(var1);
         if (!this.isOnline()) {
            if (var3 == null) {
               this.player = (LocalPlayer)var1.findSubclassOf(LocalPlayer.class);
            }
         } else if (this.player != null) {
            this.player.resetPos();
            if (var1 != null) {
               var1.addEntity(this.player);
            }
         }

         if (!var1.isOnline) {
            this.prepareLevel(var2);
         }

         System.out.println("Player is now " + this.player);
         if (this.player == null) {
            this.player = (LocalPlayer)this.gameMode.createPlayer(var1);
            this.player.resetPos();
            this.gameMode.initPlayer(this.player);
         }

         this.player.input = new KeyboardInput(this.options);
         if (this.levelRenderer != null) {
            this.levelRenderer.setLevel(var1);
         }

         if (this.particleEngine != null) {
            this.particleEngine.setLevel(var1);
         }

         this.gameMode.adjustPlayer(this.player);
         if (var3 != null) {
            var1.clearLoadedPlayerData();
         }

         ChunkSource var4 = var1.getChunkSource();
         if (var4 instanceof ChunkCache) {
            ChunkCache var5 = (ChunkCache)var4;
            int var6 = Mth.floor((float)((int)this.player.x)) >> 4;
            int var7 = Mth.floor((float)((int)this.player.z)) >> 4;
            var5.centerOn(var6, var7);
         }

         var1.loadPlayer(this.player);
         if (var1.isNew) {
            var1.forceSave(this.progressRenderer);
         }
      } else {
         this.player = null;
      }

      System.gc();
      this.lastTickTime = 0L;
   }

   private void prepareLevel(String param1) {
      // $FF: Couldn't be decompiled
   }

   public void playSound(String var1, Entity var2, float var3, float var4) {
   }

   public void playSound(String var1, float var2, float var3, float var4, float var5, float var6) {
   }

   public void fileDownloaded(String var1, File var2) {
      int var3 = var1.indexOf("/");
      String var4 = var1.substring(0, var3);
      var1 = var1.substring(var3 + 1);
      if (var4.equalsIgnoreCase("sound")) {
         this.soundEngine.add(var1, var2);
      } else if (var4.equalsIgnoreCase("newsound")) {
         this.soundEngine.add(var1, var2);
      } else if (var4.equalsIgnoreCase("streaming")) {
         this.soundEngine.addStreaming(var1, var2);
      } else if (var4.equalsIgnoreCase("music")) {
         this.soundEngine.addMusic(var1, var2);
      } else if (var4.equalsIgnoreCase("newmusic")) {
         this.soundEngine.addMusic(var1, var2);
      }

   }

   public OpenGLCapabilities getOpenGLCapabilities() {
      return this.openGLCapabilities;
   }

   public String gatherStats1() {
      return this.levelRenderer.gatherStats1();
   }

   public String gatherStats2() {
      return this.levelRenderer.gatherStats2();
   }

   public String gatherStats4() {
      return this.level.gatherChunkSourceStats();
   }

   public String gatherStats3() {
      return "P: " + this.particleEngine.countParticles() + ". T: " + this.level.gatherStats();
   }

   public void respawnPlayer() {
      if (!this.level.dimension.mayRespawn()) {
         this.toggleDimension();
      }

      int var1 = this.level.xSpawn;
      int var2 = this.level.zSpawn;
      ChunkSource var3 = this.level.getChunkSource();
      if (var3 instanceof ChunkCache) {
         ChunkCache var4 = (ChunkCache)var3;
         var4.centerOn(var1 >> 4, var2 >> 4);
      }

      this.level.validateSpawn();
      this.level.removeAllPendingEntityRemovals();
      int var5 = 0;
      if (this.player != null) {
         var5 = this.player.entityId;
         this.level.removeEntity(this.player);
      }

      this.player = (LocalPlayer)this.gameMode.createPlayer(this.level);
      this.player.resetPos();
      this.gameMode.initPlayer(this.player);
      this.level.loadPlayer(this.player);
      this.player.input = new KeyboardInput(this.options);
      this.player.entityId = var5;
      this.gameMode.adjustPlayer(this.player);
      this.prepareLevel("Respawning");
      if (this.screen instanceof DeathScreen) {
         this.setScreen((Screen)null);
      }

   }

   public static void start(String var0, String var1) throws LWJGLException {
      startAndConnectTo(var0, var1, (String)null);
   }

   public static void startAndConnectTo(String var0, String var1, String var2) {
      boolean var3 = false;
      final Frame var5 = new Frame("Minecraft");
      Canvas var6 = new Canvas();
      var5.setLayout(new BorderLayout());
      var5.add(var6, "Center");
      var6.setPreferredSize(new java.awt.Dimension(854, 480));
      var5.pack();
      var5.setLocationRelativeTo((Component)null);
      final Minecraft var7 = new Minecraft(var5, var6, (MinecraftApplet)null, 854, 480, var3) {
         public void onCrash(CrashReport var1) {
            var5.removeAll();
            var5.add(new CrashInfoPanel(var1), "Center");
            var5.validate();
         }
      };
      final Thread var8 = new Thread(var7, "Minecraft main thread");
      var8.setPriority(10);
      var7.serverDomain = "www.minecraft.net";
      if (var0 != null && var1 != null) {
         var7.user = new User(var0, var1);
      } else {
         var7.user = new User("Player" + System.currentTimeMillis() % 1000L, "");
      }

      if (var2 != null) {
         String[] var9 = var2.split(":");
         var7.connectTo(var9[0], Integer.parseInt(var9[1]));
      }

      var5.setVisible(true);
      var5.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent var1) {
            var7.stop();

            try {
               var8.join();
            } catch (InterruptedException var3) {
               var3.printStackTrace();
            }

            System.exit(0);
         }
      });
      var8.start();
   }

   public ClientConnection getConnection() {
      return this.player instanceof MultiplayerLocalPlayer ? ((MultiplayerLocalPlayer)this.player).connection : null;
   }

   public static void main(String[] var0) throws LWJGLException {
      String var1 = "Player" + System.currentTimeMillis() % 1000L;
      if (var0.length > 0) {
         var1 = var0[0];
      }

      String var2 = "-";
      if (var0.length > 1) {
         var2 = var0[1];
      }

      start(var1, var2);
   }

   private static enum OS {
      linux,
      solaris,
      windows,
      macos,
      unknown;

      private OS() {
      }
   }
}
