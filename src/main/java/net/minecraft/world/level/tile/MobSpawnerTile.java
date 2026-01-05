package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.entity.MobSpawnerTileEntity;
import net.minecraft.world.level.tile.entity.TileEntity;

public class MobSpawnerTile extends EntityTile {
   protected MobSpawnerTile(int var1, int var2) {
      super(var1, var2, Material.stone);
   }

   protected TileEntity newTileEntity() {
      return new MobSpawnerTileEntity();
   }

   public int getResource(int var1, Random var2) {
      return 0;
   }

   public int getResourceCount(Random var1) {
      return 0;
   }

   public boolean isSolidRender() {
      return false;
   }

   public boolean blocksLight() {
      return false;
   }
}
