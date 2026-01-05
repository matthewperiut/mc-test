package net.minecraft.client.gamemode.secret;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class Waypoint extends Entity {
   public int targetTile;

   public Waypoint(Level var1) {
      super(var1);
   }

   protected void readAdditionalSaveData(CompoundTag var1) {
   }

   protected void addAdditonalSaveData(CompoundTag var1) {
   }

   protected void defineSynchedData() {
   }

   public boolean isAlive() {
      return true;
   }
}
