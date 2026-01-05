package net.minecraft.world.level.tile;

import java.util.ArrayList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class FenceTile extends Tile {
   public FenceTile(int var1, int var2) {
      super(var1, var2, Material.wood);
   }

   public void addAABBs(Level var1, int var2, int var3, int var4, AABB var5, ArrayList<AABB> var6) {
      var6.add(AABB.newTemp((double)var2, (double)var3, (double)var4, (double)(var2 + 1), (double)var3 + (double)1.5F, (double)(var4 + 1)));
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      if (var1.getTile(var2, var3 - 1, var4) == this.id) {
         return false;
      } else {
         return !var1.getMaterial(var2, var3 - 1, var4).isSolid() ? false : super.mayPlace(var1, var2, var3, var4);
      }
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
      return 11;
   }
}
