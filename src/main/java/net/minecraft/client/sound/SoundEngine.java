package net.minecraft.client.sound;

import java.io.File;
import java.util.Random;
import net.minecraft.client.Options;
import net.minecraft.world.entity.Mob;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import util.Mth;

public class SoundEngine {
   private static final int SOUND_DISTANCE = 16;
   private static SoundSystem soundSystem;
   private SoundRepository sounds = new SoundRepository();
   private SoundRepository streamingSounds = new SoundRepository();
   private SoundRepository songs = new SoundRepository();
   private int idCounter = 0;
   private Options options;
   private static boolean loaded = false;
   private Random random = new Random();
   private int noMusicDelay;

   public SoundEngine() {
      this.noMusicDelay = this.random.nextInt(12000);
   }

   public void init(Options options) {
      this.streamingSounds.trimDigits = false;
      this.options = options;
      if (!loaded && (options == null || options.sound != 0.0F || options.music != 0.0F)) {
         this.loadLibrary();
      }

   }

   private void loadLibrary() {
      try {
         float hadSound = this.options.sound;
         float hadMusic = this.options.music;
         this.options.sound = 0.0F;
         this.options.music = 0.0F;
         this.options.save();
         SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
         SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
         SoundSystemConfig.setCodec("mus", CodecMus.class);
         SoundSystemConfig.setCodec("wav", CodecWav.class);
         soundSystem = new SoundSystem();
         this.options.sound = hadSound;
         this.options.music = hadMusic;
         this.options.save();
      } catch (Throwable e) {
         e.printStackTrace();
         System.err.println("error linking with the LibraryJavaSound plug-in");
      }

      loaded = true;
   }

   public void updateOptions() {
      if (!loaded && (this.options.sound != 0.0F || this.options.music != 0.0F)) {
         this.loadLibrary();
      }

      if (loaded) {
         if (this.options.music == 0.0F) {
            soundSystem.stop("BgMusic");
         } else {
            soundSystem.setVolume("BgMusic", this.options.music);
         }
      }

   }

   public void destroy() {
      if (loaded) {
         soundSystem.cleanup();
      }

   }

   public void add(String name, File file) {
      this.sounds.add(name, file);
   }

   public void addStreaming(String name, File file) {
      this.streamingSounds.add(name, file);
   }

   public void addMusic(String name, File file) {
      this.songs.add(name, file);
   }

   public void playMusicTick() {
      if (loaded && this.options.music != 0.0F) {
         if (!soundSystem.playing("BgMusic") && !soundSystem.playing("streaming")) {
            if (this.noMusicDelay > 0) {
               --this.noMusicDelay;
               return;
            }

            Sound song = this.songs.any();
            if (song != null) {
               this.noMusicDelay = this.random.nextInt(12000) + 12000;
               soundSystem.backgroundMusic("BgMusic", song.url, song.name, false);
               soundSystem.setVolume("BgMusic", this.options.music);
               soundSystem.play("BgMusic");
            }
         }

      }
   }

   public void update(Mob player, float a) {
      if (loaded && this.options.sound != 0.0F) {
         if (player != null) {
            float yRot = player.yRotO + (player.yRot - player.yRotO) * a;
            double x = player.xo + (player.x - player.xo) * (double)a;
            double y = player.yo + (player.y - player.yo) * (double)a;
            double z = player.zo + (player.z - player.zo) * (double)a;
            float yCos = Mth.cos(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
            float ySin = Mth.sin(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
            float xa = -ySin;
            float ya = 0.0F;
            float za = -yCos;
            float xa2 = 0.0F;
            float ya2 = 1.0F;
            float za2 = 0.0F;
            soundSystem.setListenerPosition((float)x, (float)y, (float)z);
            soundSystem.setListenerOrientation(xa, ya, za, xa2, ya2, za2);
         }
      }
   }

   public void playStreaming(String name, float x, float y, float z, float volume, float pitch) {
      if (loaded && this.options.sound != 0.0F) {
         String id = "streaming";
         if (soundSystem.playing("streaming")) {
            soundSystem.stop("streaming");
         }

         if (name != null) {
            Sound sound = this.streamingSounds.get(name);
            if (sound != null && volume > 0.0F) {
               if (soundSystem.playing("BgMusic")) {
                  soundSystem.stop("BgMusic");
               }

               float dist = 16.0F;
               soundSystem.newStreamingSource(true, id, sound.url, sound.name, false, x, y, z, 2, dist * 4.0F);
               soundSystem.setVolume(id, 0.5F * this.options.sound);
               soundSystem.play(id);
            }

         }
      }
   }

   public void play(String name, float x, float y, float z, float volume, float pitch) {
      if (loaded && this.options.sound != 0.0F) {
         Sound sound = this.sounds.get(name);
         if (sound != null && volume > 0.0F) {
            this.idCounter = (this.idCounter + 1) % 256;
            String id = "sound_" + this.idCounter;
            float dist = 16.0F;
            if (volume > 1.0F) {
               dist *= volume;
            }

            soundSystem.newSource(volume > 1.0F, id, sound.url, sound.name, false, x, y, z, 2, dist);
            soundSystem.setPitch(id, pitch);
            if (volume > 1.0F) {
               volume = 1.0F;
            }

            soundSystem.setVolume(id, volume * this.options.sound);
            soundSystem.play(id);
         }

      }
   }

   public void playUI(String name, float volume, float pitch) {
      if (loaded && this.options.sound != 0.0F) {
         Sound sound = this.sounds.get(name);
         if (sound != null) {
            this.idCounter = (this.idCounter + 1) % 256;
            String id = "sound_" + this.idCounter;
            soundSystem.newSource(false, id, sound.url, sound.name, false, 0.0F, 0.0F, 0.0F, 0, 0.0F);
            if (volume > 1.0F) {
               volume = 1.0F;
            }

            volume *= 0.25F;
            soundSystem.setPitch(id, pitch);
            soundSystem.setVolume(id, volume * this.options.sound);
            soundSystem.play(id);
         }

      }
   }
}
