package net.minecraft.world.level.chunk;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class EmptyLevelChunk extends LevelChunk {
   public EmptyLevelChunk(Level var1, int var2, int var3) {
      super(var1, var2, var3);
      this.dontSave = true;
   }

   public EmptyLevelChunk(Level var1, byte[] var2, int var3, int var4) {
      super(var1, var2, var3, var4);
      this.dontSave = true;
   }

   public boolean isAt(int var1, int var2) {
      return var1 == this.x && var2 == this.z;
   }

   public int getHeightmap(int var1, int var2) {
      return 0;
   }

   public void recalcBlockLights() {
   }

   public void recalcHeightmapOnly() {
   }

   public void recalcHeightmap() {
   }

   public void lightLava() {
   }

   public int getTile(int var1, int var2, int var3) {
      return 0;
   }

   public boolean setTileAndData(int var1, int var2, int var3, int var4, int var5) {
      return true;
   }

   public boolean setTile(int var1, int var2, int var3, int var4) {
      return true;
   }

   public int getData(int var1, int var2, int var3) {
      return 0;
   }

   public void setData(int var1, int var2, int var3, int var4) {
   }

   public int getBrightness(LightLayer var1, int var2, int var3, int var4) {
      return 0;
   }

   public void setBrightness(LightLayer var1, int var2, int var3, int var4, int var5) {
   }

   public int getRawBrightness(int var1, int var2, int var3, int var4) {
      return 0;
   }

   public void addEntity(Entity var1) {
   }

   public void removeEntity(Entity var1) {
   }

   public void removeEntity(Entity var1, int var2) {
   }

   public boolean isSkyLit(int var1, int var2, int var3) {
      return false;
   }

   public void skyBrightnessChanged() {
   }

   public TileEntity getTileEntity(int var1, int var2, int var3) {
      return null;
   }

   public void addTileEntity(TileEntity var1) {
   }

   public void setTileEntity(int var1, int var2, int var3, TileEntity var4) {
   }

   public void removeTileEntity(int var1, int var2, int var3) {
   }

   public void load() {
   }

   public void unload() {
   }

   public void markUnsaved() {
   }

   public void getEntities(Entity var1, AABB var2, List<Entity> var3) {
   }

   public void getEntitiesOfClass(Class<? extends Entity> var1, AABB var2, List<Entity> var3) {
   }

   public int countEntities() {
      return 0;
   }

   public boolean shouldSave(boolean var1) {
      return false;
   }

   public void setBlocks(byte[] var1, int var2) {
   }

   public int getBlocksAndData(byte[] var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      int var9 = var5 - var2;
      int var10 = var6 - var3;
      int var11 = var7 - var4;
      int var12 = var9 * var10 * var11;
      int var13 = var12 + var12 / 2 * 3;
      Arrays.fill(var1, var8, var8 + var13, (byte)0);
      return var13;
   }

   public int setBlocksAndData(byte[] var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      int var9 = var5 - var2;
      int var10 = var6 - var3;
      int var11 = var7 - var4;
      int var12 = var9 * var10 * var11;
      return var12 + var12 / 2 * 3;
   }

   public Random getRandom(long var1) {
      return new Random(this.level.seed + (long)(this.x * this.x * 4987142) + (long)(this.x * 5947611) + (long)(this.z * this.z) * 4392871L + (long)(this.z * 389711) ^ var1);
   }

   public boolean isEmpty() {
      return true;
   }
}
