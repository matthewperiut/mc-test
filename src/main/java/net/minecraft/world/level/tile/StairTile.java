package net.minecraft.world.level.tile;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import util.Mth;

public class StairTile extends Tile {
   private Tile base;

   protected StairTile(int var1, Tile var2) {
      super(var1, var2.tex, var2.material);
      this.base = var2;
      this.setDestroyTime(var2.destroySpeed);
      this.setExplodeable(var2.explosionResistance / 3.0F);
      this.setSoundType(var2.soundType);
   }

   public void updateShape(LevelSource var1, int var2, int var3, int var4) {
      this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      return super.getAABB(var1, var2, var3, var4);
   }

   public boolean isSolidRender() {
      return false;
   }

   public boolean isCubeShaped() {
      return false;
   }

   public int getRenderShape() {
      return 10;
   }

   public boolean shouldRenderFace(LevelSource var1, int var2, int var3, int var4, int var5) {
      return super.shouldRenderFace(var1, var2, var3, var4, var5);
   }

   public void addAABBs(Level var1, int var2, int var3, int var4, AABB var5, ArrayList<AABB> var6) {
      int var7 = var1.getData(var2, var3, var4);
      if (var7 == 0) {
         this.setShape(0.0F, 0.0F, 0.0F, 0.5F, 0.5F, 1.0F);
         super.addAABBs(var1, var2, var3, var4, var5, var6);
         this.setShape(0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         super.addAABBs(var1, var2, var3, var4, var5, var6);
      } else if (var7 == 1) {
         this.setShape(0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 1.0F);
         super.addAABBs(var1, var2, var3, var4, var5, var6);
         this.setShape(0.5F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
         super.addAABBs(var1, var2, var3, var4, var5, var6);
      } else if (var7 == 2) {
         this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 0.5F);
         super.addAABBs(var1, var2, var3, var4, var5, var6);
         this.setShape(0.0F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F);
         super.addAABBs(var1, var2, var3, var4, var5, var6);
      } else if (var7 == 3) {
         this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F);
         super.addAABBs(var1, var2, var3, var4, var5, var6);
         this.setShape(0.0F, 0.0F, 0.5F, 1.0F, 0.5F, 1.0F);
         super.addAABBs(var1, var2, var3, var4, var5, var6);
      }

      this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
   }

   public void addLights(Level var1, int var2, int var3, int var4) {
      this.base.addLights(var1, var2, var3, var4);
   }

   public void animateTick(Level var1, int var2, int var3, int var4, Random var5) {
      this.base.animateTick(var1, var2, var3, var4, var5);
   }

   public void attack(Level var1, int var2, int var3, int var4, Player var5) {
      this.base.attack(var1, var2, var3, var4, var5);
   }

   public void destroy(Level var1, int var2, int var3, int var4, int var5) {
      this.base.destroy(var1, var2, var3, var4, var5);
   }

   public float getBrightness(LevelSource var1, int var2, int var3, int var4) {
      return this.base.getBrightness(var1, var2, var3, var4);
   }

   public float getExplosionResistance(Entity var1) {
      return this.base.getExplosionResistance(var1);
   }

   public int getRenderLayer() {
      return this.base.getRenderLayer();
   }

   public int getResource(int var1, Random var2) {
      return this.base.getResource(var1, var2);
   }

   public int getResourceCount(Random var1) {
      return this.base.getResourceCount(var1);
   }

   public int getTexture(int var1, int var2) {
      return this.base.getTexture(var1, var2);
   }

   public int getTexture(int var1) {
      return this.base.getTexture(var1);
   }

   public int getTexture(LevelSource var1, int var2, int var3, int var4, int var5) {
      return this.base.getTexture(var1, var2, var3, var4, var5);
   }

   public int getTickDelay() {
      return this.base.getTickDelay();
   }

   public AABB getTileAABB(Level var1, int var2, int var3, int var4) {
      return this.base.getTileAABB(var1, var2, var3, var4);
   }

   public void handleEntityInside(Level var1, int var2, int var3, int var4, Entity var5, Vec3 var6) {
      this.base.handleEntityInside(var1, var2, var3, var4, var5, var6);
   }

   public boolean mayPick() {
      return this.base.mayPick();
   }

   public boolean mayPick(int var1, boolean var2) {
      return this.base.mayPick(var1, var2);
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      return this.base.mayPlace(var1, var2, var3, var4);
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      this.neighborChanged(var1, var2, var3, var4, 0);
      this.base.onPlace(var1, var2, var3, var4);
   }

   public void onRemove(Level var1, int var2, int var3, int var4) {
      this.base.onRemove(var1, var2, var3, var4);
   }

   public void prepareRender(Level var1, int var2, int var3, int var4) {
      this.base.prepareRender(var1, var2, var3, var4);
   }

   public void spawnResources(Level var1, int var2, int var3, int var4, int var5, float var6) {
      this.base.spawnResources(var1, var2, var3, var4, var5, var6);
   }

   public void spawnResources(Level var1, int var2, int var3, int var4, int var5) {
      this.base.spawnResources(var1, var2, var3, var4, var5);
   }

   public void stepOn(Level var1, int var2, int var3, int var4, Entity var5) {
      this.base.stepOn(var1, var2, var3, var4, var5);
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      this.base.tick(var1, var2, var3, var4, var5);
   }

   public boolean use(Level var1, int var2, int var3, int var4, Player var5) {
      return this.base.use(var1, var2, var3, var4, var5);
   }

   public void wasExploded(Level var1, int var2, int var3, int var4) {
      this.base.wasExploded(var1, var2, var3, var4);
   }

   public void setPlacedBy(Level var1, int var2, int var3, int var4, Mob var5) {
      int var6 = Mth.floor((double)(var5.yRot * 4.0F / 360.0F) + (double)0.5F) & 3;
      if (var6 == 0) {
         var1.setData(var2, var3, var4, 2);
      }

      if (var6 == 1) {
         var1.setData(var2, var3, var4, 1);
      }

      if (var6 == 2) {
         var1.setData(var2, var3, var4, 3);
      }

      if (var6 == 3) {
         var1.setData(var2, var3, var4, 0);
      }

   }
}
