package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class SignTile extends EntityTile {
   private Class<? extends TileEntity> clas;
   private boolean onGround;

   protected SignTile(int var1, Class<? extends TileEntity> var2, boolean var3) {
      super(var1, Material.wood);
      this.onGround = var3;
      this.tex = 4;
      this.clas = var2;
      float var4 = 0.25F;
      float var5 = 1.0F;
      this.setShape(0.5F - var4, 0.0F, 0.5F - var4, 0.5F + var4, var5, 0.5F + var4);
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      return null;
   }

   public AABB getTileAABB(Level var1, int var2, int var3, int var4) {
      this.updateShape(var1, var2, var3, var4);
      return super.getTileAABB(var1, var2, var3, var4);
   }

   public void updateShape(LevelSource var1, int var2, int var3, int var4) {
      if (!this.onGround) {
         int var5 = var1.getData(var2, var3, var4);
         float var6 = 0.28125F;
         float var7 = 0.78125F;
         float var8 = 0.0F;
         float var9 = 1.0F;
         float var10 = 0.125F;
         this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         if (var5 == 2) {
            this.setShape(var8, var6, 1.0F - var10, var9, var7, 1.0F);
         }

         if (var5 == 3) {
            this.setShape(var8, var6, 0.0F, var9, var7, var10);
         }

         if (var5 == 4) {
            this.setShape(1.0F - var10, var6, var8, 1.0F, var7, var9);
         }

         if (var5 == 5) {
            this.setShape(0.0F, var6, var8, var10, var7, var9);
         }

      }
   }

   public int getRenderShape() {
      return -1;
   }

   public boolean isCubeShaped() {
      return false;
   }

   public boolean isSolidRender() {
      return false;
   }

   protected TileEntity newTileEntity() {
      try {
         return (TileEntity)this.clas.newInstance();
      } catch (Exception var2) {
         throw new RuntimeException(var2);
      }
   }

   public int getResource(int var1, Random var2) {
      return Item.sign.id;
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      boolean var6 = false;
      if (this.onGround) {
         if (!var1.getMaterial(var2, var3 - 1, var4).isSolid()) {
            var6 = true;
         }
      } else {
         int var7 = var1.getData(var2, var3, var4);
         var6 = true;
         if (var7 == 2 && var1.getMaterial(var2, var3, var4 + 1).isSolid()) {
            var6 = false;
         }

         if (var7 == 3 && var1.getMaterial(var2, var3, var4 - 1).isSolid()) {
            var6 = false;
         }

         if (var7 == 4 && var1.getMaterial(var2 + 1, var3, var4).isSolid()) {
            var6 = false;
         }

         if (var7 == 5 && var1.getMaterial(var2 - 1, var3, var4).isSolid()) {
            var6 = false;
         }
      }

      if (var6) {
         this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
         var1.setTile(var2, var3, var4, 0);
      }

      super.neighborChanged(var1, var2, var3, var4, var5);
   }
}
