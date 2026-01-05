package net.minecraft.world.entity;

import com.mojang.nbt.CompoundTag;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.item.Boat;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.Minecart;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.PigZombie;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;

public class EntityIO {
   private static Map<String, Class<? extends Entity>> idClassMap = new HashMap();
   private static Map<Class<? extends Entity>, String> classIdMap = new HashMap();
   private static Map<Integer, Class<? extends Entity>> numClassMap = new HashMap();
   private static Map<Class<? extends Entity>, Integer> classNumMap = new HashMap();

   public EntityIO() {
   }

   private static void setId(Class<? extends Entity> var0, String var1, int var2) {
      idClassMap.put(var1, var0);
      classIdMap.put(var0, var1);
      numClassMap.put(var2, var0);
      classNumMap.put(var0, var2);
   }

   public static Entity newEntity(String var0, Level var1) {
      Entity var2 = null;

      try {
         Class var3 = (Class)idClassMap.get(var0);
         if (var3 != null) {
            var2 = (Entity)var3.getConstructor(Level.class).newInstance(var1);
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return var2;
   }

   public static Entity loadStatic(CompoundTag var0, Level var1) {
      Entity var2 = null;

      try {
         Class var3 = (Class)idClassMap.get(var0.getString("id"));
         if (var3 != null) {
            var2 = (Entity)var3.getConstructor(Level.class).newInstance(var1);
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      if (var2 != null) {
         var2.load(var0);
      } else {
         System.out.println("Skipping Entity with id " + var0.getString("id"));
      }

      return var2;
   }

   public static Entity newById(int var0, Level var1) {
      Entity var2 = null;

      try {
         Class var3 = (Class)numClassMap.get(var0);
         if (var3 != null) {
            var2 = (Entity)var3.getConstructor(Level.class).newInstance(var1);
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      if (var2 == null) {
         System.out.println("Skipping Entity with id " + var0);
      }

      return var2;
   }

   public static int getId(Entity var0) {
      return (Integer)classNumMap.get(var0.getClass());
   }

   public static String getEncodeId(Entity var0) {
      return (String)classIdMap.get(var0.getClass());
   }

   static {
      setId(Arrow.class, "Arrow", 10);
      setId(Snowball.class, "Snowball", 11);
      setId(ItemEntity.class, "Item", 1);
      setId(Painting.class, "Painting", 9);
      setId(Mob.class, "Mob", 48);
      setId(Monster.class, "Monster", 49);
      setId(Creeper.class, "Creeper", 50);
      setId(Skeleton.class, "Skeleton", 51);
      setId(Spider.class, "Spider", 52);
      setId(Giant.class, "Giant", 53);
      setId(Zombie.class, "Zombie", 54);
      setId(Slime.class, "Slime", 55);
      setId(Ghast.class, "Ghast", 56);
      setId(PigZombie.class, "PigZombie", 57);
      setId(Pig.class, "Pig", 90);
      setId(Sheep.class, "Sheep", 91);
      setId(Cow.class, "Cow", 92);
      setId(Chicken.class, "Chicken", 93);
      setId(Squid.class, "Squid", 94);
      setId(PrimedTnt.class, "PrimedTnt", 20);
      setId(FallingTile.class, "FallingSand", 21);
      setId(Minecart.class, "Minecart", 40);
      setId(Boat.class, "Boat", 41);
   }
}
