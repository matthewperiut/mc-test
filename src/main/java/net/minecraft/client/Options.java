package net.minecraft.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import net.minecraft.client.locale.Language;
import org.lwjgl.input.Keyboard;

public class Options {
   private static final String[] RENDER_DISTANCE_NAMES = new String[]{"options.renderDistance.far", "options.renderDistance.normal", "options.renderDistance.short", "options.renderDistance.tiny"};
   private static final String[] DIFFICULTY_NAMES = new String[]{"options.difficulty.peaceful", "options.difficulty.easy", "options.difficulty.normal", "options.difficulty.hard"};
   public float music = 1.0F;
   public float sound = 1.0F;
   public float sensitivity = 0.5F;
   public boolean invertYMouse = false;
   public int viewDistance = 0;
   public boolean bobView = true;
   public boolean anaglyph3d = false;
   public boolean limitFramerate = false;
   public boolean fancyGraphics = true;
   public String skin = "Default";
   public KeyMapping keyUp = new KeyMapping("key.forward", 17);
   public KeyMapping keyLeft = new KeyMapping("key.left", 30);
   public KeyMapping keyDown = new KeyMapping("key.back", 31);
   public KeyMapping keyRight = new KeyMapping("key.right", 32);
   public KeyMapping keyJump = new KeyMapping("key.jump", 57);
   public KeyMapping keyFog = new KeyMapping("key.fog", 33);
   public KeyMapping keySneak = new KeyMapping("key.sneak", 42);
   public KeyMapping[] keyMappings;
   protected Minecraft minecraft;
   private File optionsFile;
   public int difficulty;
   public boolean thirdPersonView;
   public String lastMpIp;

   public Options(Minecraft var1, File var2) {
      this.keyMappings = new KeyMapping[]{this.keyUp, this.keyLeft, this.keyDown, this.keyRight, this.keyJump, this.keySneak, this.keyFog};
      this.difficulty = 2;
      this.thirdPersonView = false;
      this.lastMpIp = "";
      this.minecraft = var1;
      this.optionsFile = new File(var2, "options.txt");
      this.load();
   }

   public Options() {
      this.keyMappings = new KeyMapping[]{this.keyUp, this.keyLeft, this.keyDown, this.keyRight, this.keyJump, this.keySneak, this.keyFog};
      this.difficulty = 2;
      this.thirdPersonView = false;
      this.lastMpIp = "";
   }

   public String getKeyDescription(int var1) {
      Language var2 = Language.getInstance();
      return var2.getElement(this.keyMappings[var1].name);
   }

   public String getKeyMessage(int var1) {
      return Keyboard.getKeyName(this.keyMappings[var1].key);
   }

   public void setKey(int var1, int var2) {
      this.keyMappings[var1].key = var2;
      this.save();
   }

   public void set(Option var1, float var2) {
      if (var1 == Options.Option.MUSIC) {
         this.music = var2;
         this.minecraft.soundEngine.updateOptions();
      }

      if (var1 == Options.Option.SOUND) {
         this.sound = var2;
         this.minecraft.soundEngine.updateOptions();
      }

      if (var1 == Options.Option.SENSITIVITY) {
         this.sensitivity = var2;
      }

   }

   public void toggle(Option var1, int var2) {
      if (var1 == Options.Option.INVERT_MOUSE) {
         this.invertYMouse = !this.invertYMouse;
      }

      if (var1 == Options.Option.RENDER_DISTANCE) {
         this.viewDistance = this.viewDistance + var2 & 3;
      }

      if (var1 == Options.Option.VIEW_BOBBING) {
         this.bobView = !this.bobView;
      }

      if (var1 == Options.Option.ANAGLYPH) {
         this.anaglyph3d = !this.anaglyph3d;
         this.minecraft.textures.reloadAll();
      }

      if (var1 == Options.Option.LIMIT_FRAMERATE) {
         this.limitFramerate = !this.limitFramerate;
      }

      if (var1 == Options.Option.DIFFICULTY) {
         this.difficulty = this.difficulty + var2 & 3;
      }

      if (var1 == Options.Option.GRAPHICS) {
         this.fancyGraphics = !this.fancyGraphics;
         this.minecraft.levelRenderer.allChanged();
      }

      this.save();
   }

   public float getProgressValue(Option var1) {
      if (var1 == Options.Option.MUSIC) {
         return this.music;
      } else if (var1 == Options.Option.SOUND) {
         return this.sound;
      } else {
         return var1 == Options.Option.SENSITIVITY ? this.sensitivity : 0.0F;
      }
   }

   public boolean getBooleanValue(Option var1) {
      switch (var1) {
         case INVERT_MOUSE:
            return this.invertYMouse;
         case VIEW_BOBBING:
            return this.bobView;
         case ANAGLYPH:
            return this.anaglyph3d;
         case LIMIT_FRAMERATE:
            return this.limitFramerate;
         default:
            return false;
      }
   }

   public String getMessage(Option var1) {
      Language var2 = Language.getInstance();
      String var3 = var2.getElement(var1.getCaptionId()) + ": ";
      if (var1.isProgress()) {
         float var5 = this.getProgressValue(var1);
         if (var1 == Options.Option.SENSITIVITY) {
            if (var5 == 0.0F) {
               return var3 + var2.getElement("options.sensitivity.min");
            } else {
               return var5 == 1.0F ? var3 + var2.getElement("options.sensitivity.max") : var3 + (int)(var5 * 200.0F) + "%";
            }
         } else {
            return var5 == 0.0F ? var3 + var2.getElement("options.off") : var3 + (int)(var5 * 100.0F) + "%";
         }
      } else if (var1.isBoolean()) {
         boolean var4 = this.getBooleanValue(var1);
         return var4 ? var3 + var2.getElement("options.on") : var3 + var2.getElement("options.off");
      } else if (var1 == Options.Option.RENDER_DISTANCE) {
         return var3 + var2.getElement(RENDER_DISTANCE_NAMES[this.viewDistance]);
      } else if (var1 == Options.Option.DIFFICULTY) {
         return var3 + var2.getElement(DIFFICULTY_NAMES[this.difficulty]);
      } else if (var1 == Options.Option.GRAPHICS) {
         return this.fancyGraphics ? var3 + var2.getElement("options.graphics.fancy") : var3 + var2.getElement("options.graphics.fast");
      } else {
         return var3;
      }
   }

   public void load() {
      try {
         if (!this.optionsFile.exists()) {
            return;
         }

         BufferedReader var1 = new BufferedReader(new FileReader(this.optionsFile));
         String var2 = "";

         while((var2 = var1.readLine()) != null) {
            String[] var3 = var2.split(":");
            if (var3[0].equals("music")) {
               this.music = this.readFloat(var3[1]);
            }

            if (var3[0].equals("sound")) {
               this.sound = this.readFloat(var3[1]);
            }

            if (var3[0].equals("mouseSensitivity")) {
               this.sensitivity = this.readFloat(var3[1]);
            }

            if (var3[0].equals("invertYMouse")) {
               this.invertYMouse = var3[1].equals("true");
            }

            if (var3[0].equals("viewDistance")) {
               this.viewDistance = Integer.parseInt(var3[1]);
            }

            if (var3[0].equals("bobView")) {
               this.bobView = var3[1].equals("true");
            }

            if (var3[0].equals("anaglyph3d")) {
               this.anaglyph3d = var3[1].equals("true");
            }

            if (var3[0].equals("limitFramerate")) {
               this.limitFramerate = var3[1].equals("true");
            }

            if (var3[0].equals("difficulty")) {
               this.difficulty = Integer.parseInt(var3[1]);
            }

            if (var3[0].equals("fancyGraphics")) {
               this.fancyGraphics = var3[1].equals("true");
            }

            if (var3[0].equals("skin")) {
               this.skin = var3[1];
            }

            if (var3[0].equals("lastServer")) {
               this.lastMpIp = var3[1];
            }

            for(int var4 = 0; var4 < this.keyMappings.length; ++var4) {
               if (var3[0].equals("key_" + this.keyMappings[var4].name)) {
                  this.keyMappings[var4].key = Integer.parseInt(var3[1]);
               }
            }
         }

         var1.close();
      } catch (Exception var5) {
         System.out.println("Failed to load options");
         var5.printStackTrace();
      }

   }

   private float readFloat(String var1) {
      if (var1.equals("true")) {
         return 1.0F;
      } else {
         return var1.equals("false") ? 0.0F : Float.parseFloat(var1);
      }
   }

   public void save() {
      try {
         PrintWriter var1 = new PrintWriter(new FileWriter(this.optionsFile));
         var1.println("music:" + this.music);
         var1.println("sound:" + this.sound);
         var1.println("invertYMouse:" + this.invertYMouse);
         var1.println("mouseSensitivity:" + this.sensitivity);
         var1.println("viewDistance:" + this.viewDistance);
         var1.println("bobView:" + this.bobView);
         var1.println("anaglyph3d:" + this.anaglyph3d);
         var1.println("limitFramerate:" + this.limitFramerate);
         var1.println("difficulty:" + this.difficulty);
         var1.println("fancyGraphics:" + this.fancyGraphics);
         var1.println("skin:" + this.skin);
         var1.println("lastServer:" + this.lastMpIp);

         for(int var2 = 0; var2 < this.keyMappings.length; ++var2) {
            var1.println("key_" + this.keyMappings[var2].name + ":" + this.keyMappings[var2].key);
         }

         var1.close();
      } catch (Exception var3) {
         System.out.println("Failed to save options");
         var3.printStackTrace();
      }

   }

   public static enum Option {
      MUSIC("options.music", true, false),
      SOUND("options.sound", true, false),
      INVERT_MOUSE("options.invertMouse", false, true),
      SENSITIVITY("options.sensitivity", true, false),
      RENDER_DISTANCE("options.renderDistance", false, false),
      VIEW_BOBBING("options.viewBobbing", false, true),
      ANAGLYPH("options.anaglyph", false, true),
      LIMIT_FRAMERATE("options.limitFramerate", false, true),
      DIFFICULTY("options.difficulty", false, false),
      GRAPHICS("options.graphics", false, false);

      private final boolean isProgress;
      private final boolean isBoolean;
      private final String captionId;

      public static Option getItem(int var0) {
         for(Option var4 : values()) {
            if (var4.getId() == var0) {
               return var4;
            }
         }

         return null;
      }

      private Option(String var3, boolean var4, boolean var5) {
         this.captionId = var3;
         this.isProgress = var4;
         this.isBoolean = var5;
      }

      public boolean isProgress() {
         return this.isProgress;
      }

      public boolean isBoolean() {
         return this.isBoolean;
      }

      public int getId() {
         return this.ordinal();
      }

      public String getCaptionId() {
         return this.captionId;
      }
   }
}
