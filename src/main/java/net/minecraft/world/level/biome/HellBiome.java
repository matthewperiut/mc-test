package net.minecraft.world.level.biome;

import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.PigZombie;

public class HellBiome extends Biome {
   public HellBiome() {
      this.enemies = new Class[]{Ghast.class, PigZombie.class};
      this.friendlies = new Class[0];
   }
}
