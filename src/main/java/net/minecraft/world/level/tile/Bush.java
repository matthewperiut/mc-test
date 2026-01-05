package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class Bush extends Tile {
   protected Bush(int var1, int var2) {
      super(var1, Material.plant);
      this.tex = var2;
      this.setTicking(true);
      float var3 = 0.2F;
      this.setShape(0.5F - var3, 0.0F, 0.5F - var3, 0.5F + var3, var3 * 3.0F, 0.5F + var3);
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      return this.mayPlaceOn(var1.getTile(var2, var3 - 1, var4));
   }

   protected boolean mayPlaceOn(int var1) {
      return var1 == Tile.grass.id || var1 == Tile.dirt.id || var1 == Tile.farmland.id;
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      super.neighborChanged(var1, var2, var3, var4, var5);
      this.checkAlive(var1, var2, var3, var4);
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      this.checkAlive(var1, var2, var3, var4);
   }

   protected final void checkAlive(Level var1, int var2, int var3, int var4) {
      if (!this.canSurvive(var1, var2, var3, var4)) {
         this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
         var1.setTile(var2, var3, var4, 0);
      }

   }

   public boolean canSurvive(Level var1, int var2, int var3, int var4) {
      return (var1.getRawBrightness(var2, var3, var4) >= 8 || var1.canSeeSky(var2, var3, var4)) && this.mayPlaceOn(var1.getTile(var2, var3 - 1, var4));
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      return null;
   }

   public boolean blocksLight() {
      return false;
   }

   public boolean isSolidRender() {
      return false;
   }

   public boolean isCubeShaped() {
      return false;
   }

   public int getRenderShape() {
      return 1;
   }
}
