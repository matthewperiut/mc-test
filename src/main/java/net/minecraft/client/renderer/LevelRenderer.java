package net.minecraft.client.renderer;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.client.MemoryTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.BubbleParticle;
import net.minecraft.client.particle.ExplodeParticle;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.LavaParticle;
import net.minecraft.client.particle.NoteParticle;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.RedDustParticle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SplashParticle;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelListener;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.ARBOcclusionQuery;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class LevelRenderer implements LevelListener {
   public static final int CHUNK_SIZE = 16;
   public static final int MAX_VISIBLE_REBUILDS_PER_FRAME = 3;
   public static final int MAX_INVISIBLE_REBUILDS_PER_FRAME = 1;
   public List<TileEntity> renderableTileEntities = new ArrayList();
   private Level level;
   private Textures textures;
   private List<Chunk> dirtyChunks = new ArrayList();
   private Chunk[] sortedChunks;
   private Chunk[] chunks;
   private int xChunks;
   private int yChunks;
   private int zChunks;
   private int chunkLists;
   private Minecraft mc;
   private TileRenderer tileRenderer;
   private IntBuffer occlusionCheckIds;
   private boolean occlusionCheck = false;
   private int ticks = 0;
   private int starList;
   private int skyList;
   private int darkList;
   private int xMinChunk;
   private int yMinChunk;
   private int zMinChunk;
   private int xMaxChunk;
   private int yMaxChunk;
   private int zMaxChunk;
   private int lastViewDistance = -1;
   private int noEntityRenderFrames = 2;
   private int totalEntities;
   private int renderedEntities;
   private int culledEntities;
   int[] toRender = new int['\uc350'];
   IntBuffer resultBuffer = MemoryTracker.createIntBuffer(64);
   private int totalChunks;
   private int offscreenChunks;
   private int occludedChunks;
   private int renderedChunks;
   private int emptyChunks;
   private int chunkFixOffs;
   private List<Chunk> renderChunks = new ArrayList();
   private OffsettedRenderList[] renderLists = new OffsettedRenderList[]{new OffsettedRenderList(), new OffsettedRenderList(), new OffsettedRenderList(), new OffsettedRenderList()};
   int frame = 0;
   int repeatList = MemoryTracker.genLists(1);
   double xOld = (double)-9999.0F;
   double yOld = (double)-9999.0F;
   double zOld = (double)-9999.0F;
   public float destroyProgress;
   int cullStep = 0;

   public LevelRenderer(Minecraft mc, Textures textures) {
      this.mc = mc;
      this.textures = textures;
      int maxChunksWidth = 64;
      this.chunkLists = MemoryTracker.genLists(maxChunksWidth * maxChunksWidth * maxChunksWidth * 3);
      this.occlusionCheck = mc.getOpenGLCapabilities().hasOcclusionChecks();
      if (this.occlusionCheck) {
         this.resultBuffer.clear();
         this.occlusionCheckIds = MemoryTracker.createIntBuffer(maxChunksWidth * maxChunksWidth * maxChunksWidth);
         this.occlusionCheckIds.clear();
         this.occlusionCheckIds.position(0);
         this.occlusionCheckIds.limit(maxChunksWidth * maxChunksWidth * maxChunksWidth);
         ARBOcclusionQuery.glGenQueriesARB(this.occlusionCheckIds);
      }

      this.starList = MemoryTracker.genLists(3);
      GL11.glPushMatrix();
      GL11.glNewList(this.starList, 4864);
      this.renderStars();
      GL11.glEndList();
      GL11.glPopMatrix();
      Tesselator t = Tesselator.instance;
      this.skyList = this.starList + 1;
      GL11.glNewList(this.skyList, 4864);
      int s = 64;
      int d = 256 / s + 2;
      float yy = 16.0F;

      for(int xx = -s * d; xx <= s * d; xx += s) {
         for(int zz = -s * d; zz <= s * d; zz += s) {
            t.begin();
            t.vertex((double)(xx + 0), (double)yy, (double)(zz + 0));
            t.vertex((double)(xx + s), (double)yy, (double)(zz + 0));
            t.vertex((double)(xx + s), (double)yy, (double)(zz + s));
            t.vertex((double)(xx + 0), (double)yy, (double)(zz + s));
            t.end();
         }
      }

      GL11.glEndList();
      this.darkList = this.starList + 2;
      GL11.glNewList(this.darkList, 4864);
      yy = -16.0F;
      t.begin();

      for(int xx = -s * d; xx <= s * d; xx += s) {
         for(int zz = -s * d; zz <= s * d; zz += s) {
            t.vertex((double)(xx + s), (double)yy, (double)(zz + 0));
            t.vertex((double)(xx + 0), (double)yy, (double)(zz + 0));
            t.vertex((double)(xx + 0), (double)yy, (double)(zz + s));
            t.vertex((double)(xx + s), (double)yy, (double)(zz + s));
         }
      }

      t.end();
      GL11.glEndList();
   }

   private void renderStars() {
      Random random = new Random(10842L);
      Tesselator t = Tesselator.instance;
      t.begin();

      for(int i = 0; i < 1500; ++i) {
         double x = (double)(random.nextFloat() * 2.0F - 1.0F);
         double y = (double)(random.nextFloat() * 2.0F - 1.0F);
         double z = (double)(random.nextFloat() * 2.0F - 1.0F);
         double ss = (double)(0.25F + random.nextFloat() * 0.25F);
         double d = x * x + y * y + z * z;
         if (d < (double)1.0F && d > 0.01) {
            d = (double)1.0F / Math.sqrt(d);
            x *= d;
            y *= d;
            z *= d;
            double xp = x * (double)100.0F;
            double yp = y * (double)100.0F;
            double zp = z * (double)100.0F;
            double yRot = Math.atan2(x, z);
            double ySin = Math.sin(yRot);
            double yCos = Math.cos(yRot);
            double xRot = Math.atan2(Math.sqrt(x * x + z * z), y);
            double xSin = Math.sin(xRot);
            double xCos = Math.cos(xRot);
            double zRot = random.nextDouble() * Math.PI * (double)2.0F;
            double zSin = Math.sin(zRot);
            double zCos = Math.cos(zRot);

            for(int c = 0; c < 4; ++c) {
               double ___xo = (double)0.0F;
               double ___yo = (double)((c & 2) - 1) * ss;
               double ___zo = (double)((c + 1 & 2) - 1) * ss;
               double __yo = ___yo * zCos - ___zo * zSin;
               double __zo = ___zo * zCos + ___yo * zSin;
               double _yo = __yo * xSin + ___xo * xCos;
               double _xo = ___xo * xSin - __yo * xCos;
               double xo = _xo * ySin - __zo * yCos;
               double zo = __zo * ySin + _xo * yCos;
               t.vertex(xp + xo, yp + _yo, zp + zo);
            }
         }
      }

      t.end();
   }

   public void setLevel(Level level) {
      if (this.level != null) {
         this.level.removeListener(this);
      }

      this.xOld = (double)-9999.0F;
      this.yOld = (double)-9999.0F;
      this.zOld = (double)-9999.0F;
      EntityRenderDispatcher.instance.setLevel(level);
      this.level = level;
      this.tileRenderer = new TileRenderer(level);
      if (level != null) {
         level.addListener(this);
         this.allChanged();
      }

   }

   public void allChanged() {
      Tile.leaves.setFancy(this.mc.options.fancyGraphics);
      this.lastViewDistance = this.mc.options.viewDistance;
      if (this.chunks != null) {
         for(int i = 0; i < this.chunks.length; ++i) {
            this.chunks[i].delete();
         }
      }

      int dist = 64 << 3 - this.lastViewDistance;
      if (dist > 400) {
         dist = 400;
      }

      this.xChunks = dist / 16 + 1;
      this.yChunks = 8;
      this.zChunks = dist / 16 + 1;
      this.chunks = new Chunk[this.xChunks * this.yChunks * this.zChunks];
      this.sortedChunks = new Chunk[this.xChunks * this.yChunks * this.zChunks];
      int id = 0;
      int count = 0;
      this.xMinChunk = 0;
      this.yMinChunk = 0;
      this.zMinChunk = 0;
      this.xMaxChunk = this.xChunks;
      this.yMaxChunk = this.yChunks;
      this.zMaxChunk = this.zChunks;

      for(int i = 0; i < this.dirtyChunks.size(); ++i) {
         ((Chunk)this.dirtyChunks.get(i)).dirty = false;
      }

      this.dirtyChunks.clear();
      this.renderableTileEntities.clear();

      for(int x = 0; x < this.xChunks; ++x) {
         for(int y = 0; y < this.yChunks; ++y) {
            for(int z = 0; z < this.zChunks; ++z) {
               this.chunks[(z * this.yChunks + y) * this.xChunks + x] = new Chunk(this.level, this.renderableTileEntities, x * 16, y * 16, z * 16, 16, this.chunkLists + id);
               if (this.occlusionCheck) {
                  this.chunks[(z * this.yChunks + y) * this.xChunks + x].occlusion_id = this.occlusionCheckIds.get(count);
               }

               this.chunks[(z * this.yChunks + y) * this.xChunks + x].occlusion_querying = false;
               this.chunks[(z * this.yChunks + y) * this.xChunks + x].occlusion_visible = true;
               this.chunks[(z * this.yChunks + y) * this.xChunks + x].visible = true;
               this.chunks[(z * this.yChunks + y) * this.xChunks + x].id = count++;
               this.chunks[(z * this.yChunks + y) * this.xChunks + x].setDirty();
               this.sortedChunks[(z * this.yChunks + y) * this.xChunks + x] = this.chunks[(z * this.yChunks + y) * this.xChunks + x];
               this.dirtyChunks.add(this.chunks[(z * this.yChunks + y) * this.xChunks + x]);
               id += 3;
            }
         }
      }

      if (this.level != null) {
         Entity player = this.mc.player;
         if (player != null) {
            this.resortChunks(Mth.floor(player.x), Mth.floor(player.y), Mth.floor(player.z));
            Arrays.sort(this.sortedChunks, new DistanceChunkSorter(player));
         }
      }

      this.noEntityRenderFrames = 2;
   }

   public void renderEntities(Vec3 cam, Culler culler, float a) {
      if (this.noEntityRenderFrames > 0) {
         --this.noEntityRenderFrames;
      } else {
         TileEntityRenderDispatcher.instance.prepare(this.level, this.textures, this.mc.font, this.mc.player, a);
         EntityRenderDispatcher.instance.prepare(this.level, this.textures, this.mc.font, this.mc.player, this.mc.options, a);
         this.totalEntities = 0;
         this.renderedEntities = 0;
         this.culledEntities = 0;
         Entity player = this.mc.player;
         EntityRenderDispatcher.xOff = player.xOld + (player.x - player.xOld) * (double)a;
         EntityRenderDispatcher.yOff = player.yOld + (player.y - player.yOld) * (double)a;
         EntityRenderDispatcher.zOff = player.zOld + (player.z - player.zOld) * (double)a;
         TileEntityRenderDispatcher.xOff = player.xOld + (player.x - player.xOld) * (double)a;
         TileEntityRenderDispatcher.yOff = player.yOld + (player.y - player.yOld) * (double)a;
         TileEntityRenderDispatcher.zOff = player.zOld + (player.z - player.zOld) * (double)a;
         List<Entity> entities = this.level.getAllEntities();
         this.totalEntities = entities.size();

         for(int i = 0; i < entities.size(); ++i) {
            Entity entity = (Entity)entities.get(i);
            if (entity.shouldRender(cam) && culler.isVisible(entity.bb) && (entity != this.mc.player || this.mc.options.thirdPersonView) && this.level.hasChunkAt(Mth.floor(entity.x), Mth.floor(entity.y), Mth.floor(entity.z))) {
               ++this.renderedEntities;
               EntityRenderDispatcher.instance.render(entity, a);
            }
         }

         for(int i = 0; i < this.renderableTileEntities.size(); ++i) {
            TileEntityRenderDispatcher.instance.render((TileEntity)this.renderableTileEntities.get(i), a);
         }

      }
   }

   public String gatherStats1() {
      return "C: " + this.renderedChunks + "/" + this.totalChunks + ". F: " + this.offscreenChunks + ", O: " + this.occludedChunks + ", E: " + this.emptyChunks;
   }

   public String gatherStats2() {
      return "E: " + this.renderedEntities + "/" + this.totalEntities + ". B: " + this.culledEntities + ", I: " + (this.totalEntities - this.culledEntities - this.renderedEntities);
   }

   private void resortChunks(int xc, int yc, int zc) {
      xc -= 8;
      yc -= 8;
      zc -= 8;
      this.xMinChunk = Integer.MAX_VALUE;
      this.yMinChunk = Integer.MAX_VALUE;
      this.zMinChunk = Integer.MAX_VALUE;
      this.xMaxChunk = Integer.MIN_VALUE;
      this.yMaxChunk = Integer.MIN_VALUE;
      this.zMaxChunk = Integer.MIN_VALUE;
      int s2 = this.xChunks * 16;
      int s1 = s2 / 2;

      for(int x = 0; x < this.xChunks; ++x) {
         int xx = x * 16;
         int xOff = xx + s1 - xc;
         if (xOff < 0) {
            xOff -= s2 - 1;
         }

         xOff /= s2;
         xx -= xOff * s2;
         if (xx < this.xMinChunk) {
            this.xMinChunk = xx;
         }

         if (xx > this.xMaxChunk) {
            this.xMaxChunk = xx;
         }

         for(int z = 0; z < this.zChunks; ++z) {
            int zz = z * 16;
            int zOff = zz + s1 - zc;
            if (zOff < 0) {
               zOff -= s2 - 1;
            }

            zOff /= s2;
            zz -= zOff * s2;
            if (zz < this.zMinChunk) {
               this.zMinChunk = zz;
            }

            if (zz > this.zMaxChunk) {
               this.zMaxChunk = zz;
            }

            for(int y = 0; y < this.yChunks; ++y) {
               int yy = y * 16;
               if (yy < this.yMinChunk) {
                  this.yMinChunk = yy;
               }

               if (yy > this.yMaxChunk) {
                  this.yMaxChunk = yy;
               }

               Chunk chunk = this.chunks[(z * this.yChunks + y) * this.xChunks + x];
               boolean wasDirty = chunk.dirty;
               chunk.setPos(xx, yy, zz);
               if (!wasDirty && chunk.dirty) {
                  this.dirtyChunks.add(chunk);
               }
            }
         }
      }

   }

   public int render(Player player, int layer, double alpha) {
      for(int i = 0; i < 10; ++i) {
         this.chunkFixOffs = (this.chunkFixOffs + 1) % this.chunks.length;
         Chunk c = this.chunks[this.chunkFixOffs];
         if (c.dirty && !this.dirtyChunks.contains(c)) {
            this.dirtyChunks.add(c);
         }
      }

      if (this.mc.options.viewDistance != this.lastViewDistance) {
         this.allChanged();
      }

      if (layer == 0) {
         this.totalChunks = 0;
         this.offscreenChunks = 0;
         this.occludedChunks = 0;
         this.renderedChunks = 0;
         this.emptyChunks = 0;
      }

      double xOff = player.xOld + (player.x - player.xOld) * alpha;
      double yOff = player.yOld + (player.y - player.yOld) * alpha;
      double zOff = player.zOld + (player.z - player.zOld) * alpha;
      double xd = player.x - this.xOld;
      double yd = player.y - this.yOld;
      double zd = player.z - this.zOld;
      if (xd * xd + yd * yd + zd * zd > (double)16.0F) {
         this.xOld = player.x;
         this.yOld = player.y;
         this.zOld = player.z;
         this.resortChunks(Mth.floor(player.x), Mth.floor(player.y), Mth.floor(player.z));
         Arrays.sort(this.sortedChunks, new DistanceChunkSorter(player));
      }

      int count = 0;
      if (this.occlusionCheck && !this.mc.options.anaglyph3d && layer == 0) {
         int from = 0;
         int to = 16;
         this.checkQueryResults(from, to);

         for(int i = from; i < to; ++i) {
            this.sortedChunks[i].occlusion_visible = true;
         }

         count += this.renderChunks(from, to, layer, alpha);

         do {
            from = to;
            to *= 2;
            if (to > this.sortedChunks.length) {
               to = this.sortedChunks.length;
            }

            GL11.glDisable(3553);
            GL11.glDisable(2896);
            GL11.glDisable(3008);
            GL11.glDisable(2912);
            GL11.glColorMask(false, false, false, false);
            GL11.glDepthMask(false);
            this.checkQueryResults(from, to);
            GL11.glPushMatrix();
            float xo = 0.0F;
            float yo = 0.0F;
            float zo = 0.0F;

            for(int i = from; i < to; ++i) {
               if (this.sortedChunks[i].isEmpty()) {
                  this.sortedChunks[i].visible = false;
               } else {
                  if (!this.sortedChunks[i].visible) {
                     this.sortedChunks[i].occlusion_visible = true;
                  }

                  if (this.sortedChunks[i].visible && !this.sortedChunks[i].occlusion_querying) {
                     float dist = Mth.sqrt(this.sortedChunks[i].distanceToSqr(player));
                     int frequency = (int)(1.0F + dist / 128.0F);
                     if (this.ticks % frequency == i % frequency) {
                        Chunk chunk = this.sortedChunks[i];
                        float xt = (float)((double)chunk.xRender - xOff);
                        float yt = (float)((double)chunk.yRender - yOff);
                        float zt = (float)((double)chunk.zRender - zOff);
                        float xdd = xt - xo;
                        float ydd = yt - yo;
                        float zdd = zt - zo;
                        if (xdd != 0.0F || ydd != 0.0F || zdd != 0.0F) {
                           GL11.glTranslatef(xdd, ydd, zdd);
                           xo += xdd;
                           yo += ydd;
                           zo += zdd;
                        }

                        ARBOcclusionQuery.glBeginQueryARB(35092, this.sortedChunks[i].occlusion_id);
                        this.sortedChunks[i].renderBB();
                        ARBOcclusionQuery.glEndQueryARB(35092);
                        this.sortedChunks[i].occlusion_querying = true;
                     }
                  }
               }
            }

            GL11.glPopMatrix();
            GL11.glColorMask(true, true, true, true);
            GL11.glDepthMask(true);
            GL11.glEnable(3553);
            GL11.glEnable(3008);
            GL11.glEnable(2912);
            count += this.renderChunks(from, to, layer, alpha);
         } while(to < this.sortedChunks.length);
      } else {
         count += this.renderChunks(0, this.sortedChunks.length, layer, alpha);
      }

      return count;
   }

   private void checkQueryResults(int from, int to) {
      for(int i = from; i < to; ++i) {
         if (this.sortedChunks[i].occlusion_querying) {
            this.resultBuffer.clear();
            ARBOcclusionQuery.glGetQueryObjectuARB(this.sortedChunks[i].occlusion_id, 34919, this.resultBuffer);
            if (this.resultBuffer.get(0) != 0) {
               this.sortedChunks[i].occlusion_querying = false;
               this.resultBuffer.clear();
               ARBOcclusionQuery.glGetQueryObjectuARB(this.sortedChunks[i].occlusion_id, 34918, this.resultBuffer);
               this.sortedChunks[i].occlusion_visible = this.resultBuffer.get(0) != 0;
            }
         }
      }

   }

   private int renderChunks(int from, int to, int layer, double alpha) {
      this.renderChunks.clear();
      int count = 0;

      for(int i = from; i < to; ++i) {
         if (layer == 0) {
            ++this.totalChunks;
            if (this.sortedChunks[i].empty[layer]) {
               ++this.emptyChunks;
            } else if (!this.sortedChunks[i].visible) {
               ++this.offscreenChunks;
            } else if (this.occlusionCheck && !this.sortedChunks[i].occlusion_visible) {
               ++this.occludedChunks;
            } else {
               ++this.renderedChunks;
            }
         }

         if (!this.sortedChunks[i].empty[layer] && this.sortedChunks[i].visible && this.sortedChunks[i].occlusion_visible) {
            int list = this.sortedChunks[i].getList(layer);
            if (list >= 0) {
               this.renderChunks.add(this.sortedChunks[i]);
               ++count;
            }
         }
      }

      Player player = this.mc.player;
      double xOff = player.xOld + (player.x - player.xOld) * alpha;
      double yOff = player.yOld + (player.y - player.yOld) * alpha;
      double zOff = player.zOld + (player.z - player.zOld) * alpha;
      int lists = 0;

      for(int l = 0; l < this.renderLists.length; ++l) {
         this.renderLists[l].clear();
      }

      for(int i = 0; i < this.renderChunks.size(); ++i) {
         Chunk chunk = (Chunk)this.renderChunks.get(i);
         int list = -1;

         for(int l = 0; l < lists; ++l) {
            if (this.renderLists[l].isAt(chunk.xRender, chunk.yRender, chunk.zRender)) {
               list = l;
            }
         }

         if (list < 0) {
            list = lists++;
            this.renderLists[list].init(chunk.xRender, chunk.yRender, chunk.zRender, xOff, yOff, zOff);
         }

         this.renderLists[list].add(chunk.getList(layer));
      }

      this.renderSameAsLast(layer, alpha);
      return count;
   }

   public void renderSameAsLast(int layer, double alpha) {
      for(int i = 0; i < this.renderLists.length; ++i) {
         this.renderLists[i].render();
      }

   }

   public void tick() {
      ++this.ticks;
   }

   public void renderSky(float alpha) {
      if (!this.mc.level.dimension.foggy) {
         GL11.glDisable(3553);
         Vec3 sc = this.level.getSkyColor(this.mc.player, alpha);
         float sr = (float)sc.x;
         float sg = (float)sc.y;
         float sb = (float)sc.z;
         if (this.mc.options.anaglyph3d) {
            float srr = (sr * 30.0F + sg * 59.0F + sb * 11.0F) / 100.0F;
            float sgg = (sr * 30.0F + sg * 70.0F) / 100.0F;
            float sbb = (sr * 30.0F + sb * 70.0F) / 100.0F;
            sr = srr;
            sg = sgg;
            sb = sbb;
         }

         GL11.glColor3f(sr, sg, sb);
         Tesselator t = Tesselator.instance;
         GL11.glDepthMask(false);
         GL11.glEnable(2912);
         GL11.glColor3f(sr, sg, sb);
         GL11.glCallList(this.skyList);
         GL11.glDisable(2912);
         GL11.glDisable(3008);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         float[] c = this.level.dimension.getSunriseColor(this.level.getTimeOfDay(alpha), alpha);
         if (c != null) {
            GL11.glDisable(3553);
            GL11.glShadeModel(7425);
            GL11.glPushMatrix();
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            float now = this.level.getTimeOfDay(alpha);
            GL11.glRotatef((float)(now > 0.5F ? 180 : 0), 0.0F, 0.0F, 1.0F);
            t.begin(6);
            t.color(c[0], c[1], c[2], c[3]);
            t.vertex((double)0.0F, (double)100.0F, (double)0.0F);
            int steps = 16;
            t.color(c[0], c[1], c[2], 0.0F);

            for(int i = 0; i <= steps; ++i) {
               float a = (float)i * (float)Math.PI * 2.0F / (float)steps;
               float sin = Mth.sin(a);
               float cos = Mth.cos(a);
               t.vertex((double)(sin * 120.0F), (double)(cos * 120.0F), (double)(-cos * 40.0F * c[3]));
            }

            t.end();
            GL11.glPopMatrix();
            GL11.glShadeModel(7424);
         }

         GL11.glEnable(3553);
         GL11.glBlendFunc(1, 1);
         GL11.glPushMatrix();
         float xp = 0.0F;
         float yp = 0.0F;
         float zp = 0.0F;
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glTranslatef(xp, yp, zp);
         GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
         GL11.glRotatef(this.level.getTimeOfDay(alpha) * 360.0F, 1.0F, 0.0F, 0.0F);
         float ss = 30.0F;
         GL11.glBindTexture(3553, this.textures.loadTexture("/terrain/sun.png"));
         t.begin();
         t.vertexUV((double)(-ss), (double)100.0F, (double)(-ss), (double)0.0F, (double)0.0F);
         t.vertexUV((double)ss, (double)100.0F, (double)(-ss), (double)1.0F, (double)0.0F);
         t.vertexUV((double)ss, (double)100.0F, (double)ss, (double)1.0F, (double)1.0F);
         t.vertexUV((double)(-ss), (double)100.0F, (double)ss, (double)0.0F, (double)1.0F);
         t.end();
         ss = 20.0F;
         GL11.glBindTexture(3553, this.textures.loadTexture("/terrain/moon.png"));
         t.begin();
         t.vertexUV((double)(-ss), (double)-100.0F, (double)ss, (double)1.0F, (double)1.0F);
         t.vertexUV((double)ss, (double)-100.0F, (double)ss, (double)0.0F, (double)1.0F);
         t.vertexUV((double)ss, (double)-100.0F, (double)(-ss), (double)0.0F, (double)0.0F);
         t.vertexUV((double)(-ss), (double)-100.0F, (double)(-ss), (double)1.0F, (double)0.0F);
         t.end();
         GL11.glDisable(3553);
         float br = this.level.getStarBrightness(alpha);
         if (br > 0.0F) {
            GL11.glColor4f(br, br, br, br);
            GL11.glCallList(this.starList);
         }

         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glDisable(3042);
         GL11.glEnable(3008);
         GL11.glEnable(2912);
         GL11.glPopMatrix();
         GL11.glColor3f(sr * 0.2F + 0.04F, sg * 0.2F + 0.04F, sb * 0.6F + 0.1F);
         GL11.glDisable(3553);
         GL11.glCallList(this.darkList);
         GL11.glEnable(3553);
         GL11.glDepthMask(true);
      }
   }

   public void renderClouds(float alpha) {
      if (!this.mc.level.dimension.foggy) {
         if (this.mc.options.fancyGraphics) {
            this.renderAdvancedClouds(alpha);
         } else {
            GL11.glDisable(2884);
            float yOffs = (float)(this.mc.player.yOld + (this.mc.player.y - this.mc.player.yOld) * (double)alpha);
            int s = 32;
            int d = 256 / s;
            Tesselator t = Tesselator.instance;
            GL11.glBindTexture(3553, this.textures.loadTexture("/environment/clouds.png"));
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            Vec3 cc = this.level.getCloudColor(alpha);
            float cr = (float)cc.x;
            float cg = (float)cc.y;
            float cb = (float)cc.z;
            if (this.mc.options.anaglyph3d) {
               float crr = (cr * 30.0F + cg * 59.0F + cb * 11.0F) / 100.0F;
               float cgg = (cr * 30.0F + cg * 70.0F) / 100.0F;
               float cbb = (cr * 30.0F + cb * 70.0F) / 100.0F;
               cr = crr;
               cg = cgg;
               cb = cbb;
            }

            float scale = 4.8828125E-4F;
            double xo = this.mc.player.xo + (this.mc.player.x - this.mc.player.xo) * (double)alpha + (double)(((float)this.ticks + alpha) * 0.03F);
            double zo = this.mc.player.zo + (this.mc.player.z - this.mc.player.zo) * (double)alpha;
            int xOffs = Mth.floor(xo / (double)2048.0F);
            int zOffs = Mth.floor(zo / (double)2048.0F);
            xo -= (double)(xOffs * 2048);
            zo -= (double)(zOffs * 2048);
            float yy = 120.0F - yOffs + 0.33F;
            float uo = (float)(xo * (double)scale);
            float vo = (float)(zo * (double)scale);
            t.begin();
            t.color(cr, cg, cb, 0.8F);

            for(int xx = -s * d; xx < s * d; xx += s) {
               for(int zz = -s * d; zz < s * d; zz += s) {
                  t.vertexUV((double)(xx + 0), (double)yy, (double)(zz + s), (double)((float)(xx + 0) * scale + uo), (double)((float)(zz + s) * scale + vo));
                  t.vertexUV((double)(xx + s), (double)yy, (double)(zz + s), (double)((float)(xx + s) * scale + uo), (double)((float)(zz + s) * scale + vo));
                  t.vertexUV((double)(xx + s), (double)yy, (double)(zz + 0), (double)((float)(xx + s) * scale + uo), (double)((float)(zz + 0) * scale + vo));
                  t.vertexUV((double)(xx + 0), (double)yy, (double)(zz + 0), (double)((float)(xx + 0) * scale + uo), (double)((float)(zz + 0) * scale + vo));
               }
            }

            t.end();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(3042);
            GL11.glEnable(2884);
         }
      }
   }

   public void renderAdvancedClouds(float alpha) {
      GL11.glDisable(2884);
      float yOffs = (float)(this.mc.player.yOld + (this.mc.player.y - this.mc.player.yOld) * (double)alpha);
      Tesselator t = Tesselator.instance;
      float ss = 12.0F;
      float h = 4.0F;
      double xo = (this.mc.player.xo + (this.mc.player.x - this.mc.player.xo) * (double)alpha + (double)(((float)this.ticks + alpha) * 0.03F)) / (double)ss;
      double zo = (this.mc.player.zo + (this.mc.player.z - this.mc.player.zo) * (double)alpha) / (double)ss + (double)0.33F;
      float yy = 108.0F - yOffs + 0.33F;
      int xOffs = Mth.floor(xo / (double)2048.0F);
      int zOffs = Mth.floor(zo / (double)2048.0F);
      xo -= (double)(xOffs * 2048);
      zo -= (double)(zOffs * 2048);
      GL11.glBindTexture(3553, this.textures.loadTexture("/environment/clouds.png"));
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      Vec3 cc = this.level.getCloudColor(alpha);
      float cr = (float)cc.x;
      float cg = (float)cc.y;
      float cb = (float)cc.z;
      if (this.mc.options.anaglyph3d) {
         float crr = (cr * 30.0F + cg * 59.0F + cb * 11.0F) / 100.0F;
         float cgg = (cr * 30.0F + cg * 70.0F) / 100.0F;
         float cbb = (cr * 30.0F + cb * 70.0F) / 100.0F;
         cr = crr;
         cg = cgg;
         cb = cbb;
      }

      float uo = (float)(xo * (double)0.0F);
      float vo = (float)(zo * (double)0.0F);
      float scale = 0.00390625F;
      uo = (float)Mth.floor(xo) * scale;
      vo = (float)Mth.floor(zo) * scale;
      float xoffs = (float)(xo - (double)Mth.floor(xo));
      float zoffs = (float)(zo - (double)Mth.floor(zo));
      int D = 8;
      int radius = 3;
      float e = 9.765625E-4F;
      GL11.glScalef(ss, 1.0F, ss);

      for(int pass = 0; pass < 2; ++pass) {
         if (pass == 0) {
            GL11.glColorMask(false, false, false, false);
         } else {
            GL11.glColorMask(true, true, true, true);
         }

         for(int xPos = -radius + 1; xPos <= radius; ++xPos) {
            for(int zPos = -radius + 1; zPos <= radius; ++zPos) {
               t.begin();
               float xx = (float)(xPos * D);
               float zz = (float)(zPos * D);
               float xp = xx - xoffs;
               float zp = zz - zoffs;
               if (yy > -h - 1.0F) {
                  t.color(cr * 0.7F, cg * 0.7F, cb * 0.7F, 0.8F);
                  t.normal(0.0F, -1.0F, 0.0F);
                  t.vertexUV((double)(xp + 0.0F), (double)(yy + 0.0F), (double)(zp + (float)D), (double)((xx + 0.0F) * scale + uo), (double)((zz + (float)D) * scale + vo));
                  t.vertexUV((double)(xp + (float)D), (double)(yy + 0.0F), (double)(zp + (float)D), (double)((xx + (float)D) * scale + uo), (double)((zz + (float)D) * scale + vo));
                  t.vertexUV((double)(xp + (float)D), (double)(yy + 0.0F), (double)(zp + 0.0F), (double)((xx + (float)D) * scale + uo), (double)((zz + 0.0F) * scale + vo));
                  t.vertexUV((double)(xp + 0.0F), (double)(yy + 0.0F), (double)(zp + 0.0F), (double)((xx + 0.0F) * scale + uo), (double)((zz + 0.0F) * scale + vo));
               }

               if (yy <= h + 1.0F) {
                  t.color(cr, cg, cb, 0.8F);
                  t.normal(0.0F, 1.0F, 0.0F);
                  t.vertexUV((double)(xp + 0.0F), (double)(yy + h - e), (double)(zp + (float)D), (double)((xx + 0.0F) * scale + uo), (double)((zz + (float)D) * scale + vo));
                  t.vertexUV((double)(xp + (float)D), (double)(yy + h - e), (double)(zp + (float)D), (double)((xx + (float)D) * scale + uo), (double)((zz + (float)D) * scale + vo));
                  t.vertexUV((double)(xp + (float)D), (double)(yy + h - e), (double)(zp + 0.0F), (double)((xx + (float)D) * scale + uo), (double)((zz + 0.0F) * scale + vo));
                  t.vertexUV((double)(xp + 0.0F), (double)(yy + h - e), (double)(zp + 0.0F), (double)((xx + 0.0F) * scale + uo), (double)((zz + 0.0F) * scale + vo));
               }

               t.color(cr * 0.9F, cg * 0.9F, cb * 0.9F, 0.8F);
               if (xPos > -1) {
                  t.normal(-1.0F, 0.0F, 0.0F);

                  for(int i = 0; i < D; ++i) {
                     t.vertexUV((double)(xp + (float)i + 0.0F), (double)(yy + 0.0F), (double)(zp + (float)D), (double)((xx + (float)i + 0.5F) * scale + uo), (double)((zz + (float)D) * scale + vo));
                     t.vertexUV((double)(xp + (float)i + 0.0F), (double)(yy + h), (double)(zp + (float)D), (double)((xx + (float)i + 0.5F) * scale + uo), (double)((zz + (float)D) * scale + vo));
                     t.vertexUV((double)(xp + (float)i + 0.0F), (double)(yy + h), (double)(zp + 0.0F), (double)((xx + (float)i + 0.5F) * scale + uo), (double)((zz + 0.0F) * scale + vo));
                     t.vertexUV((double)(xp + (float)i + 0.0F), (double)(yy + 0.0F), (double)(zp + 0.0F), (double)((xx + (float)i + 0.5F) * scale + uo), (double)((zz + 0.0F) * scale + vo));
                  }
               }

               if (xPos <= 1) {
                  t.normal(1.0F, 0.0F, 0.0F);

                  for(int i = 0; i < D; ++i) {
                     t.vertexUV((double)(xp + (float)i + 1.0F - e), (double)(yy + 0.0F), (double)(zp + (float)D), (double)((xx + (float)i + 0.5F) * scale + uo), (double)((zz + (float)D) * scale + vo));
                     t.vertexUV((double)(xp + (float)i + 1.0F - e), (double)(yy + h), (double)(zp + (float)D), (double)((xx + (float)i + 0.5F) * scale + uo), (double)((zz + (float)D) * scale + vo));
                     t.vertexUV((double)(xp + (float)i + 1.0F - e), (double)(yy + h), (double)(zp + 0.0F), (double)((xx + (float)i + 0.5F) * scale + uo), (double)((zz + 0.0F) * scale + vo));
                     t.vertexUV((double)(xp + (float)i + 1.0F - e), (double)(yy + 0.0F), (double)(zp + 0.0F), (double)((xx + (float)i + 0.5F) * scale + uo), (double)((zz + 0.0F) * scale + vo));
                  }
               }

               t.color(cr * 0.8F, cg * 0.8F, cb * 0.8F, 0.8F);
               if (zPos > -1) {
                  t.normal(0.0F, 0.0F, -1.0F);

                  for(int i = 0; i < D; ++i) {
                     t.vertexUV((double)(xp + 0.0F), (double)(yy + h), (double)(zp + (float)i + 0.0F), (double)((xx + 0.0F) * scale + uo), (double)((zz + (float)i + 0.5F) * scale + vo));
                     t.vertexUV((double)(xp + (float)D), (double)(yy + h), (double)(zp + (float)i + 0.0F), (double)((xx + (float)D) * scale + uo), (double)((zz + (float)i + 0.5F) * scale + vo));
                     t.vertexUV((double)(xp + (float)D), (double)(yy + 0.0F), (double)(zp + (float)i + 0.0F), (double)((xx + (float)D) * scale + uo), (double)((zz + (float)i + 0.5F) * scale + vo));
                     t.vertexUV((double)(xp + 0.0F), (double)(yy + 0.0F), (double)(zp + (float)i + 0.0F), (double)((xx + 0.0F) * scale + uo), (double)((zz + (float)i + 0.5F) * scale + vo));
                  }
               }

               if (zPos <= 1) {
                  t.normal(0.0F, 0.0F, 1.0F);

                  for(int i = 0; i < D; ++i) {
                     t.vertexUV((double)(xp + 0.0F), (double)(yy + h), (double)(zp + (float)i + 1.0F - e), (double)((xx + 0.0F) * scale + uo), (double)((zz + (float)i + 0.5F) * scale + vo));
                     t.vertexUV((double)(xp + (float)D), (double)(yy + h), (double)(zp + (float)i + 1.0F - e), (double)((xx + (float)D) * scale + uo), (double)((zz + (float)i + 0.5F) * scale + vo));
                     t.vertexUV((double)(xp + (float)D), (double)(yy + 0.0F), (double)(zp + (float)i + 1.0F - e), (double)((xx + (float)D) * scale + uo), (double)((zz + (float)i + 0.5F) * scale + vo));
                     t.vertexUV((double)(xp + 0.0F), (double)(yy + 0.0F), (double)(zp + (float)i + 1.0F - e), (double)((xx + 0.0F) * scale + uo), (double)((zz + (float)i + 0.5F) * scale + vo));
                  }
               }

               t.end();
            }
         }
      }

      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glDisable(3042);
      GL11.glEnable(2884);
   }

   public boolean updateDirtyChunks(Player player, boolean force) {
      boolean slow = false;
      if (slow) {
         Collections.sort(this.dirtyChunks, new DirtyChunkSorter(player));
         int s = this.dirtyChunks.size() - 1;
         int amount = this.dirtyChunks.size();

         for(int i = 0; i < amount; ++i) {
            Chunk chunk = (Chunk)this.dirtyChunks.get(s - i);
            if (!force) {
               if (chunk.distanceToSqr(player) > 1024.0F) {
                  if (chunk.visible) {
                     if (i >= 3) {
                        return false;
                     }
                  } else if (i >= 1) {
                     return false;
                  }
               }
            } else if (!chunk.visible) {
               continue;
            }

            chunk.rebuild();
            this.dirtyChunks.remove(chunk);
            chunk.dirty = false;
         }

         if (this.dirtyChunks.size() == 0) {
            return true;
         } else {
            return false;
         }
      } else {
         DirtyChunkSorter dirtyChunkSorter = new DirtyChunkSorter(player);
         Chunk[] toAdd = new Chunk[3];
         ArrayList<Chunk> nearChunks = null;
         int pendingChunkSize = this.dirtyChunks.size();
         int pendingChunkRemoved = 0;

         label158:
         for(int i = 0; i < pendingChunkSize; ++i) {
            Chunk chunk = (Chunk)this.dirtyChunks.get(i);
            if (!force) {
               if (chunk.distanceToSqr(player) > 1024.0F) {
                  int index;
                  for(index = 0; index < 3 && (toAdd[index] == null || dirtyChunkSorter.compare(toAdd[index], chunk) <= 0); ++index) {
                  }

                  --index;
                  if (index <= 0) {
                     continue;
                  }

                  int x = index;

                  while(true) {
                     --x;
                     if (x == 0) {
                        toAdd[index] = chunk;
                        continue label158;
                     }

                     toAdd[x - 1] = toAdd[x];
                  }
               }
            } else if (!chunk.visible) {
               continue;
            }

            if (nearChunks == null) {
               nearChunks = new ArrayList();
            }

            ++pendingChunkRemoved;
            nearChunks.add(chunk);
            this.dirtyChunks.set(i, null);
         }

         if (nearChunks != null) {
            if (nearChunks.size() > 1) {
               Collections.sort(nearChunks, dirtyChunkSorter);
            }

            for(int i = nearChunks.size() - 1; i >= 0; --i) {
               Chunk chunk = (Chunk)nearChunks.get(i);
               chunk.rebuild();
               chunk.dirty = false;
            }
         }

         int secondaryRemoved = 0;

         for(int i = 2; i >= 0; --i) {
            Chunk chunk = toAdd[i];
            if (chunk != null) {
               if (!chunk.visible && i != 2) {
                  toAdd[i] = null;
                  toAdd[0] = null;
                  break;
               }

               toAdd[i].rebuild();
               toAdd[i].dirty = false;
               ++secondaryRemoved;
            }
         }

         int cursor = 0;
         int target = 0;

         for(int arraySize = this.dirtyChunks.size(); cursor != arraySize; ++cursor) {
            Chunk chunk = (Chunk)this.dirtyChunks.get(cursor);
            if (chunk != null && chunk != toAdd[0] && chunk != toAdd[1] && chunk != toAdd[2]) {
               if (target != cursor) {
                  this.dirtyChunks.set(target, chunk);
               }

               ++target;
            }
         }

         while(true) {
            --cursor;
            if (cursor < target) {
               if (pendingChunkSize == pendingChunkRemoved + secondaryRemoved) {
                  return true;
               } else {
                  return false;
               }
            }

            this.dirtyChunks.remove(cursor);
         }
      }
   }

   public void renderHit(Player player, HitResult h, int mode, ItemInstance inventoryItem, float a) {
      Tesselator t = Tesselator.instance;
      GL11.glEnable(3042);
      GL11.glEnable(3008);
      GL11.glBlendFunc(770, 1);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, (Mth.sin((float)System.currentTimeMillis() / 100.0F) * 0.2F + 0.4F) * 0.5F);
      if (mode == 0) {
         if (this.destroyProgress > 0.0F) {
            GL11.glBlendFunc(774, 768);
            int id = this.textures.loadTexture("/terrain.png");
            GL11.glBindTexture(3553, id);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
            GL11.glPushMatrix();
            int tileId = this.level.getTile(h.x, h.y, h.z);
            Tile tile = tileId > 0 ? Tile.tiles[tileId] : null;
            GL11.glDisable(3008);
            GL11.glPolygonOffset(-3.0F, -3.0F);
            GL11.glEnable(32823);
            t.begin();
            double xo = player.xOld + (player.x - player.xOld) * (double)a;
            double yo = player.yOld + (player.y - player.yOld) * (double)a;
            double zo = player.zOld + (player.z - player.zOld) * (double)a;
            t.offset(-xo, -yo, -zo);
            t.noColor();
            if (tile == null) {
               tile = Tile.rock;
            }

            this.tileRenderer.tesselateInWorld(tile, h.x, h.y, h.z, 240 + (int)(this.destroyProgress * 10.0F));
            t.end();
            t.offset((double)0.0F, (double)0.0F, (double)0.0F);
            GL11.glPolygonOffset(0.0F, 0.0F);
            GL11.glDisable(32823);
            GL11.glEnable(3008);
            GL11.glDepthMask(true);
            GL11.glPopMatrix();
         }
      } else if (inventoryItem != null) {
         GL11.glBlendFunc(770, 771);
         float br = Mth.sin((float)System.currentTimeMillis() / 100.0F) * 0.2F + 0.8F;
         GL11.glColor4f(br, br, br, Mth.sin((float)System.currentTimeMillis() / 200.0F) * 0.2F + 0.5F);
         int id = this.textures.loadTexture("/terrain.png");
         GL11.glBindTexture(3553, id);
         int x = h.x;
         int y = h.y;
         int z = h.z;
         if (h.f == 0) {
            --y;
         }

         if (h.f == 1) {
            ++y;
         }

         if (h.f == 2) {
            --z;
         }

         if (h.f == 3) {
            ++z;
         }

         if (h.f == 4) {
            --x;
         }

         if (h.f == 5) {
            ++x;
         }
      }

      GL11.glDisable(3042);
      GL11.glDisable(3008);
   }

   public void renderHitOutline(Player player, HitResult h, int mode, ItemInstance inventoryItem, float a) {
      if (mode == 0 && h.type == HitResult.Type.TILE) {
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
         GL11.glLineWidth(2.0F);
         GL11.glDisable(3553);
         GL11.glDepthMask(false);
         float ss = 0.002F;
         int tileId = this.level.getTile(h.x, h.y, h.z);
         if (tileId > 0) {
            Tile.tiles[tileId].updateShape(this.level, h.x, h.y, h.z);
            double xo = player.xOld + (player.x - player.xOld) * (double)a;
            double yo = player.yOld + (player.y - player.yOld) * (double)a;
            double zo = player.zOld + (player.z - player.zOld) * (double)a;
            this.render(Tile.tiles[tileId].getTileAABB(this.level, h.x, h.y, h.z).grow((double)ss, (double)ss, (double)ss).cloneMove(-xo, -yo, -zo));
         }

         GL11.glDepthMask(true);
         GL11.glEnable(3553);
         GL11.glDisable(3042);
      }

   }

   private void render(AABB b) {
      Tesselator t = Tesselator.instance;
      t.begin(3);
      t.vertex(b.x0, b.y0, b.z0);
      t.vertex(b.x1, b.y0, b.z0);
      t.vertex(b.x1, b.y0, b.z1);
      t.vertex(b.x0, b.y0, b.z1);
      t.vertex(b.x0, b.y0, b.z0);
      t.end();
      t.begin(3);
      t.vertex(b.x0, b.y1, b.z0);
      t.vertex(b.x1, b.y1, b.z0);
      t.vertex(b.x1, b.y1, b.z1);
      t.vertex(b.x0, b.y1, b.z1);
      t.vertex(b.x0, b.y1, b.z0);
      t.end();
      t.begin(1);
      t.vertex(b.x0, b.y0, b.z0);
      t.vertex(b.x0, b.y1, b.z0);
      t.vertex(b.x1, b.y0, b.z0);
      t.vertex(b.x1, b.y1, b.z0);
      t.vertex(b.x1, b.y0, b.z1);
      t.vertex(b.x1, b.y1, b.z1);
      t.vertex(b.x0, b.y0, b.z1);
      t.vertex(b.x0, b.y1, b.z1);
      t.end();
   }

   public void setDirty(int x0, int y0, int z0, int x1, int y1, int z1) {
      int _x0 = Mth.intFloorDiv(x0, 16);
      int _y0 = Mth.intFloorDiv(y0, 16);
      int _z0 = Mth.intFloorDiv(z0, 16);
      int _x1 = Mth.intFloorDiv(x1, 16);
      int _y1 = Mth.intFloorDiv(y1, 16);
      int _z1 = Mth.intFloorDiv(z1, 16);

      for(int x = _x0; x <= _x1; ++x) {
         int xx = x % this.xChunks;
         if (xx < 0) {
            xx += this.xChunks;
         }

         for(int y = _y0; y <= _y1; ++y) {
            int yy = y % this.yChunks;
            if (yy < 0) {
               yy += this.yChunks;
            }

            for(int z = _z0; z <= _z1; ++z) {
               int zz = z % this.zChunks;
               if (zz < 0) {
                  zz += this.zChunks;
               }

               int p = (zz * this.yChunks + yy) * this.xChunks + xx;
               Chunk chunk = this.chunks[p];
               if (!chunk.dirty) {
                  this.dirtyChunks.add(chunk);
                  chunk.setDirty();
               }
            }
         }
      }

   }

   public void tileChanged(int x, int y, int z) {
      this.setDirty(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
   }

   public void setTilesDirty(int x0, int y0, int z0, int x1, int y1, int z1) {
      this.setDirty(x0 - 1, y0 - 1, z0 - 1, x1 + 1, y1 + 1, z1 + 1);
   }

   public void cull(Culler culler, float a) {
      for(int i = 0; i < this.chunks.length; ++i) {
         if (!this.chunks[i].isEmpty() && (!this.chunks[i].visible || (i + this.cullStep & 15) == 0)) {
            this.chunks[i].cull(culler);
         }
      }

      ++this.cullStep;
   }

   public void playStreamingMusic(String name, int x, int y, int z) {
      if (name != null) {
         this.mc.gui.setNowPlaying("C418 - " + name);
      }

      this.mc.soundEngine.playStreaming(name, (float)x, (float)y, (float)z, 1.0F, 1.0F);
   }

   public void playSound(String name, double x, double y, double z, float volume, float pitch) {
      float dd = 16.0F;
      if (volume > 1.0F) {
         dd *= volume;
      }

      if (this.mc.player.distanceToSqr(x, y, z) < (double)(dd * dd)) {
         this.mc.soundEngine.play(name, (float)x, (float)y, (float)z, volume, pitch);
      }

   }

   public void addParticle(String name, double x, double y, double z, double xa, double ya, double za) {
      double xd = this.mc.player.x - x;
      double yd = this.mc.player.y - y;
      double zd = this.mc.player.z - z;
      double particleDistance = (double)16.0F;
      if (!(xd * xd + yd * yd + zd * zd > particleDistance * particleDistance)) {
         if (name == "bubble") {
            this.mc.particleEngine.add(new BubbleParticle(this.level, x, y, z, xa, ya, za));
         } else if (name == "smoke") {
            this.mc.particleEngine.add(new SmokeParticle(this.level, x, y, z, xa, ya, za));
         } else if (name == "note") {
            this.mc.particleEngine.add(new NoteParticle(this.level, x, y, z, xa, ya, za));
         } else if (name == "portal") {
            this.mc.particleEngine.add(new PortalParticle(this.level, x, y, z, xa, ya, za));
         } else if (name == "explode") {
            this.mc.particleEngine.add(new ExplodeParticle(this.level, x, y, z, xa, ya, za));
         } else if (name == "flame") {
            this.mc.particleEngine.add(new FlameParticle(this.level, x, y, z, xa, ya, za));
         } else if (name == "lava") {
            this.mc.particleEngine.add(new LavaParticle(this.level, x, y, z));
         } else if (name == "splash") {
            this.mc.particleEngine.add(new SplashParticle(this.level, x, y, z, xa, ya, za));
         } else if (name == "largesmoke") {
            this.mc.particleEngine.add(new SmokeParticle(this.level, x, y, z, xa, ya, za, 2.5F));
         } else if (name == "reddust") {
            this.mc.particleEngine.add(new RedDustParticle(this.level, x, y, z));
         } else if (name == "snowballpoof") {
            this.mc.particleEngine.add(new BreakingItemParticle(this.level, x, y, z, Item.snowBall));
         } else if (name == "slime") {
            this.mc.particleEngine.add(new BreakingItemParticle(this.level, x, y, z, Item.slimeBall));
         }

      }
   }

   public void playMusic(String name, double x, double y, double z, float songOffset) {
   }

   public void entityAdded(Entity entity) {
      entity.prepareCustomTextures();
      if (entity.customTextureUrl != null) {
         this.textures.addHttpTexture(entity.customTextureUrl, new MobSkinTextureProcessor());
      }

      if (entity.customTextureUrl2 != null) {
         this.textures.addHttpTexture(entity.customTextureUrl2, new MobSkinTextureProcessor());
      }

   }

   public void entityRemoved(Entity entity) {
      if (entity.customTextureUrl != null) {
         this.textures.removeHttpTexture(entity.customTextureUrl);
      }

      if (entity.customTextureUrl2 != null) {
         this.textures.removeHttpTexture(entity.customTextureUrl2);
      }

   }

   public void skyColorChanged() {
      for(int i = 0; i < this.chunks.length; ++i) {
         if (this.chunks[i].skyLit && !this.chunks[i].dirty) {
            this.dirtyChunks.add(this.chunks[i]);
            this.chunks[i].setDirty();
         }
      }

   }

   public void tileEntityChanged(int x, int y, int z, TileEntity te) {
   }
}
