package net.minecraft.world.entity.animal;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.entity.Creature;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import util.Mth;

public abstract class Animal extends PathfinderMob implements Creature {
   public Animal(Level var1) {
      super(var1);
   }

   protected float getWalkTargetValue(int var1, int var2, int var3) {
      return this.level.getTile(var1, var2 - 1, var3) == Tile.grass.id ? 10.0F : this.level.getBrightness(var1, var2, var3) - 0.5F;
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
   }

   public boolean canSpawn() {
      int var1 = Mth.floor(this.x);
      int var2 = Mth.floor(this.bb.y0);
      int var3 = Mth.floor(this.z);
      return this.level.getTile(var1, var2 - 1, var3) == Tile.grass.id && this.level.getRawBrightness(var1, var2, var3) > 8 && super.canSpawn();
   }

   public int getAmbientSoundInterval() {
      return 120;
   }
}
