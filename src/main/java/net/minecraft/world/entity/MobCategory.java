package net.minecraft.world.entity;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.material.Material;

public enum MobCategory {
   monster(Enemy.class, 70, Material.air, false),
   creature(Animal.class, 15, Material.air, true),
   waterCreature(WaterAnimal.class, 5, Material.water, true);

   private final Class<? extends Creature> base;
   private final int max;
   private final Material spawnPositionMaterial;
   private final boolean isFriendly;

   private MobCategory(Class<? extends Creature> var3, int var4, Material var5, boolean var6) {
      this.base = var3;
      this.max = var4;
      this.spawnPositionMaterial = var5;
      this.isFriendly = var6;
   }

   public Class<? extends Creature> getBaseClass() {
      return this.base;
   }

   public int getMaxInstancesPerChunk() {
      return this.max;
   }

   public Material getSpawnPositionMaterial() {
      return this.spawnPositionMaterial;
   }

   public boolean isFriendly() {
      return this.isFriendly;
   }
}
