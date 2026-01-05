package net.minecraft.world.level;

import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.entity.TileEntity;

public interface LevelSource {
   int getTile(int var1, int var2, int var3);

   TileEntity getTileEntity(int var1, int var2, int var3);

   float getBrightness(int var1, int var2, int var3);

   int getData(int var1, int var2, int var3);

   Material getMaterial(int var1, int var2, int var3);

   boolean isSolidTile(int var1, int var2, int var3);

   BiomeSource getBiomeSource();
}
