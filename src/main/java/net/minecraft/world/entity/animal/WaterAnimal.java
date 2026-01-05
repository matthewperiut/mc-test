package net.minecraft.world.entity.animal;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.entity.Creature;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class WaterAnimal extends PathfinderMob implements Creature {
   public WaterAnimal(Level var1) {
      super(var1);
   }

   public boolean isWaterMob() {
      return true;
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
   }

   public boolean canSpawn() {
      return this.level.isUnobstructed(this.bb);
   }

   public int getAmbientSoundInterval() {
      return 120;
   }
}
