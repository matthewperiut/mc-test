package net.minecraft.world.level.biome;

import java.awt.Color;
import java.util.Random;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.levelgen.feature.BasicTree;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.tile.Tile;

public class Biome {
   public static final Biome rainForest = (new RainforestBiome()).setColor(588342).setName("Rainforest").setLeafColor(2094168);
   public static final Biome swampland = (new SwampBiome()).setColor(522674).setName("Swampland").setLeafColor(9154376);
   public static final Biome seasonalForest = (new Biome()).setColor(10215459).setName("Seasonal Forest");
   public static final Biome forest = (new ForestBiome()).setColor(353825).setName("Forest").setLeafColor(5159473);
   public static final Biome savanna = (new FlatBiome()).setColor(14278691).setName("Savanna");
   public static final Biome shrubland = (new Biome()).setColor(10595616).setName("Shrubland");
   public static final Biome taiga = (new TaigaBiome()).setColor(3060051).setName("Taiga").setSnowCovered().setLeafColor(8107825);
   public static final Biome desert = (new FlatBiome()).setColor(16421912).setName("Desert");
   public static final Biome plains = (new FlatBiome()).setColor(16767248).setName("Plains");
   public static final Biome iceDesert = (new FlatBiome()).setColor(16772499).setName("Ice Desert").setSnowCovered().setLeafColor(12899129);
   public static final Biome tundra = (new Biome()).setColor(5762041).setName("Tundra").setSnowCovered().setLeafColor(12899129);
   public static final Biome hell = (new HellBiome()).setColor(16711680).setName("Hell");
   public String name;
   public int color;
   public byte topMaterial;
   public byte material;
   public int leafColor;
   protected Class[] enemies;
   protected Class[] friendlies;
   protected Class[] waterFriendlies;
   private static Biome[] map = new Biome[4096];

   public Biome() {
      this.topMaterial = (byte)Tile.grass.id;
      this.material = (byte)Tile.dirt.id;
      this.leafColor = 5169201;
      this.enemies = new Class[]{Spider.class, Zombie.class, Skeleton.class, Creeper.class};
      this.friendlies = new Class[]{Sheep.class, Pig.class, Chicken.class, Cow.class};
      this.waterFriendlies = new Class[]{Squid.class};
   }

   public static void recalc() {
      for(int var0 = 0; var0 < 64; ++var0) {
         for(int var1 = 0; var1 < 64; ++var1) {
            map[var0 + var1 * 64] = _getBiome((float)var0 / 63.0F, (float)var1 / 63.0F);
         }
      }

      desert.topMaterial = desert.material = (byte)Tile.sand.id;
      iceDesert.topMaterial = iceDesert.material = (byte)Tile.sand.id;
   }

   public Feature getTreeFeature(Random var1) {
      return (Feature)(var1.nextInt(10) == 0 ? new BasicTree() : new TreeFeature());
   }

   protected Biome setSnowCovered() {
      return this;
   }

   protected Biome setName(String var1) {
      this.name = var1;
      return this;
   }

   protected Biome setLeafColor(int var1) {
      this.leafColor = var1;
      return this;
   }

   protected Biome setColor(int var1) {
      this.color = var1;
      return this;
   }

   public static Biome getBiome(double var0, double var2) {
      int var4 = (int)(var0 * (double)63.0F);
      int var5 = (int)(var2 * (double)63.0F);
      return map[var4 + var5 * 64];
   }

   public static Biome _getBiome(float var0, float var1) {
      var1 *= var0;
      if (var0 < 0.1F) {
         return tundra;
      } else if (var1 < 0.2F) {
         if (var0 < 0.5F) {
            return tundra;
         } else {
            return var0 < 0.95F ? savanna : desert;
         }
      } else if (var1 > 0.5F && var0 < 0.7F) {
         return swampland;
      } else if (var0 < 0.5F) {
         return taiga;
      } else if (var0 < 0.97F) {
         return var1 < 0.35F ? shrubland : forest;
      } else if (var1 < 0.45F) {
         return plains;
      } else {
         return var1 < 0.9F ? seasonalForest : rainForest;
      }
   }

   public double adjustScale(double var1) {
      return var1;
   }

   public double adjustDepth(double var1) {
      return var1;
   }

   public int getSkyColor(float var1) {
      var1 /= 3.0F;
      if (var1 < -1.0F) {
         var1 = -1.0F;
      }

      if (var1 > 1.0F) {
         var1 = 1.0F;
      }

      return Color.getHSBColor(0.62222224F - var1 * 0.05F, 0.5F + var1 * 0.1F, 1.0F).getRGB();
   }

   public Class<Mob>[] getMobs(MobCategory var1) {
      if (var1 == MobCategory.monster) {
         return this.enemies;
      } else if (var1 == MobCategory.creature) {
         return this.friendlies;
      } else {
         return var1 == MobCategory.waterCreature ? this.waterFriendlies : null;
      }
   }

   static {
      recalc();
   }
}
