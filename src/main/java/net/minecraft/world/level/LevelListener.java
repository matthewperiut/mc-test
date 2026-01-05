package net.minecraft.world.level;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.tile.entity.TileEntity;

public interface LevelListener {
   void tileChanged(int var1, int var2, int var3);

   void setTilesDirty(int var1, int var2, int var3, int var4, int var5, int var6);

   void allChanged();

   void playSound(String var1, double var2, double var4, double var6, float var8, float var9);

   void addParticle(String var1, double var2, double var4, double var6, double var8, double var10, double var12);

   void playMusic(String var1, double var2, double var4, double var6, float var8);

   void entityAdded(Entity var1);

   void entityRemoved(Entity var1);

   void skyColorChanged();

   void playStreamingMusic(String var1, int var2, int var3, int var4);

   void tileEntityChanged(int var1, int var2, int var3, TileEntity var4);
}
