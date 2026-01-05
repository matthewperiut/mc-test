package net.minecraft.client.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class ParticleEngine {
   public static final int MISC_TEXTURE = 0;
   public static final int TERRAIN_TEXTURE = 1;
   public static final int ITEM_TEXTURE = 2;
   public static final int ENTITY_PARTICLE_TEXTURE = 3;
   public static final int TEXTURE_COUNT = 4;
   protected Level level;
   private List<Particle>[] particles = new List[4];
   private Textures textures;
   private Random random = new Random();

   public ParticleEngine(Level level, Textures textures) {
      if (level != null) {
         this.level = level;
      }

      this.textures = textures;

      for(int i = 0; i < 4; ++i) {
         this.particles[i] = new ArrayList();
      }

   }

   public void add(Particle p) {
      int t = p.getParticleTexture();
      this.particles[t].add(p);
   }

   public void tick() {
      for(int tt = 0; tt < 4; ++tt) {
         for(int i = 0; i < this.particles[tt].size(); ++i) {
            Particle p = (Particle)this.particles[tt].get(i);
            p.tick();
            if (p.removed) {
               this.particles[tt].remove(i--);
            }
         }
      }

   }

   public void render(Entity player, float a) {
      float xa = Mth.cos(player.yRot * (float)Math.PI / 180.0F);
      float za = Mth.sin(player.yRot * (float)Math.PI / 180.0F);
      float xa2 = -za * Mth.sin(player.xRot * (float)Math.PI / 180.0F);
      float za2 = xa * Mth.sin(player.xRot * (float)Math.PI / 180.0F);
      float ya = Mth.cos(player.xRot * (float)Math.PI / 180.0F);
      Particle.xOff = player.xOld + (player.x - player.xOld) * (double)a;
      Particle.yOff = player.yOld + (player.y - player.yOld) * (double)a;
      Particle.zOff = player.zOld + (player.z - player.zOld) * (double)a;

      for(int tt = 0; tt < 3; ++tt) {
         if (this.particles[tt].size() != 0) {
            int id = 0;
            if (tt == 0) {
               id = this.textures.loadTexture("/particles.png");
            }

            if (tt == 1) {
               id = this.textures.loadTexture("/terrain.png");
            }

            if (tt == 2) {
               id = this.textures.loadTexture("/gui/items.png");
            }

            GL11.glBindTexture(3553, id);
            Tesselator t = Tesselator.instance;
            t.begin();

            for(int i = 0; i < this.particles[tt].size(); ++i) {
               Particle p = (Particle)this.particles[tt].get(i);
               p.render(t, a, xa, ya, za, xa2, za2);
            }

            t.end();
         }
      }

   }

   public void renderLit(Entity player, float a) {
      int tt = 3;
      if (this.particles[tt].size() != 0) {
         Tesselator t = Tesselator.instance;

         for(int i = 0; i < this.particles[tt].size(); ++i) {
            Particle p = (Particle)this.particles[tt].get(i);
            p.render(t, a, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
         }

      }
   }

   public void setLevel(Level level) {
      this.level = level;

      for(int tt = 0; tt < 4; ++tt) {
         this.particles[tt].clear();
      }

   }

   public void destroy(int x, int y, int z) {
      int tid = this.level.getTile(x, y, z);
      if (tid != 0) {
         Tile tile = Tile.tiles[tid];
         int SD = 4;

         for(int xx = 0; xx < SD; ++xx) {
            for(int yy = 0; yy < SD; ++yy) {
               for(int zz = 0; zz < SD; ++zz) {
                  double xp = (double)x + ((double)xx + (double)0.5F) / (double)SD;
                  double yp = (double)y + ((double)yy + (double)0.5F) / (double)SD;
                  double zp = (double)z + ((double)zz + (double)0.5F) / (double)SD;
                  this.add((new TerrainParticle(this.level, xp, yp, zp, xp - (double)x - (double)0.5F, yp - (double)y - (double)0.5F, zp - (double)z - (double)0.5F, tile)).init(x, y, z));
               }
            }
         }

      }
   }

   public void crack(int x, int y, int z, int face) {
      int tid = this.level.getTile(x, y, z);
      if (tid != 0) {
         Tile tile = Tile.tiles[tid];
         float r = 0.1F;
         double xp = (double)x + this.random.nextDouble() * (tile.xx1 - tile.xx0 - (double)(r * 2.0F)) + (double)r + tile.xx0;
         double yp = (double)y + this.random.nextDouble() * (tile.yy1 - tile.yy0 - (double)(r * 2.0F)) + (double)r + tile.yy0;
         double zp = (double)z + this.random.nextDouble() * (tile.zz1 - tile.zz0 - (double)(r * 2.0F)) + (double)r + tile.zz0;
         if (face == 0) {
            yp = (double)y + tile.yy0 - (double)r;
         }

         if (face == 1) {
            yp = (double)y + tile.yy1 + (double)r;
         }

         if (face == 2) {
            zp = (double)z + tile.zz0 - (double)r;
         }

         if (face == 3) {
            zp = (double)z + tile.zz1 + (double)r;
         }

         if (face == 4) {
            xp = (double)x + tile.xx0 - (double)r;
         }

         if (face == 5) {
            xp = (double)x + tile.xx1 + (double)r;
         }

         this.add((new TerrainParticle(this.level, xp, yp, zp, (double)0.0F, (double)0.0F, (double)0.0F, tile)).init(x, y, z).setPower(0.2F).scale(0.6F));
      }
   }

   public String countParticles() {
      return "" + (this.particles[0].size() + this.particles[1].size() + this.particles[2].size());
   }
}
