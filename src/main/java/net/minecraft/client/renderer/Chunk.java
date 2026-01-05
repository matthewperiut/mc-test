package net.minecraft.client.renderer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.Region;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class Chunk {
   public Level level;
   private int lists = -1;
   private static Tesselator t;
   public static int updates;
   public int x;
   public int y;
   public int z;
   public int xs;
   public int ys;
   public int zs;
   public int xRender;
   public int yRender;
   public int zRender;
   public int xRenderOffs;
   public int yRenderOffs;
   public int zRenderOffs;
   public boolean visible = false;
   public boolean[] empty = new boolean[2];
   public int xm;
   public int ym;
   public int zm;
   public float radius;
   public boolean dirty;
   public AABB bb;
   public int id;
   public boolean occlusion_visible = true;
   public boolean occlusion_querying;
   public int occlusion_id;
   public boolean skyLit;
   private boolean compiled = false;
   public List<TileEntity> renderableTileEntities = new ArrayList();
   private List<TileEntity> globalRenderableTileEntities;

   static {
      t = Tesselator.instance;
      updates = 0;
   }

   public Chunk(Level level, List<TileEntity> globalRenderableTileEntities, int x, int y, int z, int size, int lists) {
      this.level = level;
      this.globalRenderableTileEntities = globalRenderableTileEntities;
      this.xs = this.ys = this.zs = size;
      this.radius = Mth.sqrt((float)(this.xs * this.xs + this.ys * this.ys + this.zs * this.zs)) / 2.0F;
      this.lists = lists;
      this.x = -999;
      this.setPos(x, y, z);
      this.dirty = false;
   }

   public void setPos(int x, int y, int z) {
      if (x != this.x || y != this.y || z != this.z) {
         this.reset();
         this.x = x;
         this.y = y;
         this.z = z;
         this.xm = x + this.xs / 2;
         this.ym = y + this.ys / 2;
         this.zm = z + this.zs / 2;
         this.xRenderOffs = x & 1023;
         this.yRenderOffs = y;
         this.zRenderOffs = z & 1023;
         this.xRender = x - this.xRenderOffs;
         this.yRender = y - this.yRenderOffs;
         this.zRender = z - this.zRenderOffs;
         float g = 6.0F;
         this.bb = AABB.newPermanent((double)((float)x - g), (double)((float)y - g), (double)((float)z - g), (double)((float)(x + this.xs) + g), (double)((float)(y + this.ys) + g), (double)((float)(z + this.zs) + g));
         GL11.glNewList(this.lists + 2, 4864);
         ItemRenderer.renderFlat(AABB.newTemp((double)((float)this.xRenderOffs - g), (double)((float)this.yRenderOffs - g), (double)((float)this.zRenderOffs - g), (double)((float)(this.xRenderOffs + this.xs) + g), (double)((float)(this.yRenderOffs + this.ys) + g), (double)((float)(this.zRenderOffs + this.zs) + g)));
         GL11.glEndList();
         this.setDirty();
      }
   }

   private void translateToPos() {
      GL11.glTranslatef((float)this.xRenderOffs, (float)this.yRenderOffs, (float)this.zRenderOffs);
   }

   public void rebuild() {
      if (this.dirty) {
         ++updates;
         int x0 = this.x;
         int y0 = this.y;
         int z0 = this.z;
         int x1 = this.x + this.xs;
         int y1 = this.y + this.ys;
         int z1 = this.z + this.zs;

         for(int l = 0; l < 2; ++l) {
            this.empty[l] = true;
         }

         LevelChunk.touchedSky = false;
         Set<TileEntity> oldTileEntities = new HashSet();
         oldTileEntities.addAll(this.renderableTileEntities);
         this.renderableTileEntities.clear();
         int r = 1;
         LevelSource region = new Region(this.level, x0 - r, y0 - r, z0 - r, x1 + r, y1 + r, z1 + r);
         TileRenderer tileRenderer = new TileRenderer(region);

         for(int l = 0; l < 2; ++l) {
            boolean renderNextLayer = false;
            boolean rendered = false;
            boolean started = false;

            for(int y = y0; y < y1; ++y) {
               for(int z = z0; z < z1; ++z) {
                  for(int x = x0; x < x1; ++x) {
                     int tileId = region.getTile(x, y, z);
                     if (tileId > 0) {
                        if (!started) {
                           started = true;
                           GL11.glNewList(this.lists + l, 4864);
                           GL11.glPushMatrix();
                           this.translateToPos();
                           float ss = 1.000001F;
                           GL11.glTranslatef((float)(-this.zs) / 2.0F, (float)(-this.ys) / 2.0F, (float)(-this.zs) / 2.0F);
                           GL11.glScalef(ss, ss, ss);
                           GL11.glTranslatef((float)this.zs / 2.0F, (float)this.ys / 2.0F, (float)this.zs / 2.0F);
                           t.begin();
                           t.offset((double)(-this.x), (double)(-this.y), (double)(-this.z));
                        }

                        if (l == 0 && Tile.isEntityTile[tileId]) {
                           TileEntity et = region.getTileEntity(x, y, z);
                           if (TileEntityRenderDispatcher.instance.hasRenderer(et)) {
                              this.renderableTileEntities.add(et);
                           }
                        }

                        Tile tile = Tile.tiles[tileId];
                        int renderLayer = tile.getRenderLayer();
                        if (renderLayer != l) {
                           renderNextLayer = true;
                        } else if (renderLayer == l) {
                           rendered |= tileRenderer.tesselateInWorld(tile, x, y, z);
                        }
                     }
                  }
               }
            }

            if (started) {
               t.end();
               GL11.glPopMatrix();
               GL11.glEndList();
               t.offset((double)0.0F, (double)0.0F, (double)0.0F);
            } else {
               rendered = false;
            }

            if (rendered) {
               this.empty[l] = false;
            }

            if (!renderNextLayer) {
               break;
            }
         }

         Set<TileEntity> newTileEntities = new HashSet();
         newTileEntities.addAll(this.renderableTileEntities);
         newTileEntities.removeAll(oldTileEntities);
         this.globalRenderableTileEntities.addAll(newTileEntities);
         oldTileEntities.removeAll(this.renderableTileEntities);
         this.globalRenderableTileEntities.removeAll(oldTileEntities);
         this.skyLit = LevelChunk.touchedSky;
         this.compiled = true;
      }
   }

   public float distanceToSqr(Entity player) {
      float xd = (float)(player.x - (double)this.xm);
      float yd = (float)(player.y - (double)this.ym);
      float zd = (float)(player.z - (double)this.zm);
      return xd * xd + yd * yd + zd * zd;
   }

   public float squishedDistanceToSqr(Entity player) {
      float xd = (float)(player.x - (double)this.xm);
      float yd = (float)(player.y - (double)this.ym) * 2.0F;
      float zd = (float)(player.z - (double)this.zm);
      return xd * xd + yd * yd + zd * zd;
   }

   public void reset() {
      for(int i = 0; i < 2; ++i) {
         this.empty[i] = true;
      }

      this.visible = false;
      this.compiled = false;
   }

   public void delete() {
      this.reset();
      this.level = null;
   }

   public int getList(int layer) {
      if (!this.visible) {
         return -1;
      } else {
         return !this.empty[layer] ? this.lists + layer : -1;
      }
   }

   public int getAllLists(int[] displayLists, int p, int layer) {
      if (!this.visible) {
         return p;
      } else {
         if (!this.empty[layer]) {
            displayLists[p++] = this.lists + layer;
         }

         return p;
      }
   }

   public void cull(Culler culler) {
      this.visible = culler.isVisible(this.bb);
   }

   public void renderBB() {
      GL11.glCallList(this.lists + 2);
   }

   public boolean isEmpty() {
      if (!this.compiled) {
         return false;
      } else {
         return this.empty[0] && this.empty[1];
      }
   }

   public void setDirty() {
      this.dirty = true;
   }
}
