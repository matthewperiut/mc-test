package net.minecraft.world.level.tile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class HellSandTile extends Tile {
   public HellSandTile(int var1, int var2) {
      super(var1, var2, Material.sand);
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      float var5 = 0.125F;
      return AABB.newTemp((double)var2, (double)var3, (double)var4, (double)(var2 + 1), (double)((float)(var3 + 1) - var5), (double)(var4 + 1));
   }

   public void entityInside(Level var1, int var2, int var3, int var4, Entity var5) {
      var5.xd *= 0.4;
      var5.zd *= 0.4;
   }
}
