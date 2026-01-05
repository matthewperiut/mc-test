package net.minecraft.world.entity;

import com.mojang.nbt.CompoundTag;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import util.Mth;

public class Painting extends Entity {
   private int checkInterval;
   public int dir;
   public int xTile;
   public int yTile;
   public int zTile;
   public Motive motive;

   public Painting(Level var1) {
      super(var1);
      this.checkInterval = 0;
      this.dir = 0;
      this.heightOffset = 0.0F;
      this.setSize(0.5F, 0.5F);
   }

   public Painting(Level var1, int var2, int var3, int var4, int var5) {
      this(var1);
      this.xTile = var2;
      this.yTile = var3;
      this.zTile = var4;
      ArrayList var6 = new ArrayList();

      for(Motive var10 : Painting.Motive.values()) {
         this.motive = var10;
         this.setDir(var5);
         if (this.survives()) {
            var6.add(var10);
         }
      }

      if (var6.size() > 0) {
         this.motive = (Motive)var6.get(this.random.nextInt(var6.size()));
      }

      this.setDir(var5);
   }

   public Painting(Level var1, int var2, int var3, int var4, int var5, String var6) {
      this(var1);
      this.xTile = var2;
      this.yTile = var3;
      this.zTile = var4;

      for(Motive var10 : Painting.Motive.values()) {
         if (var10.name.equals(var6)) {
            this.motive = var10;
            break;
         }
      }

      this.setDir(var5);
   }

   protected void defineSynchedData() {
   }

   public void setDir(int var1) {
      this.dir = var1;
      this.yRotO = this.yRot = (float)(var1 * 90);
      float var2 = (float)this.motive.w;
      float var3 = (float)this.motive.h;
      float var4 = (float)this.motive.w;
      if (var1 != 0 && var1 != 2) {
         var2 = 0.5F;
      } else {
         var4 = 0.5F;
      }

      var2 /= 32.0F;
      var3 /= 32.0F;
      var4 /= 32.0F;
      float var5 = (float)this.xTile + 0.5F;
      float var6 = (float)this.yTile + 0.5F;
      float var7 = (float)this.zTile + 0.5F;
      float var8 = 0.5625F;
      if (var1 == 0) {
         var7 -= var8;
      }

      if (var1 == 1) {
         var5 -= var8;
      }

      if (var1 == 2) {
         var7 += var8;
      }

      if (var1 == 3) {
         var5 += var8;
      }

      if (var1 == 0) {
         var5 -= this.offs(this.motive.w);
      }

      if (var1 == 1) {
         var7 += this.offs(this.motive.w);
      }

      if (var1 == 2) {
         var5 += this.offs(this.motive.w);
      }

      if (var1 == 3) {
         var7 -= this.offs(this.motive.w);
      }

      var6 += this.offs(this.motive.h);
      this.setPos((double)var5, (double)var6, (double)var7);
      float var9 = -0.00625F;
      this.bb.set((double)(var5 - var2 - var9), (double)(var6 - var3 - var9), (double)(var7 - var4 - var9), (double)(var5 + var2 + var9), (double)(var6 + var3 + var9), (double)(var7 + var4 + var9));
   }

   private float offs(int var1) {
      if (var1 == 32) {
         return 0.5F;
      } else {
         return var1 == 64 ? 0.5F : 0.0F;
      }
   }

   public void tick() {
      if (this.checkInterval++ == 100 && !this.level.isOnline) {
         this.checkInterval = 0;
         if (!this.survives()) {
            this.remove();
            this.level.addEntity(new ItemEntity(this.level, this.x, this.y, this.z, new ItemInstance(Item.painting)));
         }
      }

   }

   public boolean survives() {
      if (this.level.getCubes(this, this.bb).size() > 0) {
         return false;
      } else {
         int var1 = this.motive.w / 16;
         int var2 = this.motive.h / 16;
         int var3 = this.xTile;
         int var4 = this.yTile;
         int var5 = this.zTile;
         if (this.dir == 0) {
            var3 = Mth.floor(this.x - (double)((float)this.motive.w / 32.0F));
         }

         if (this.dir == 1) {
            var5 = Mth.floor(this.z - (double)((float)this.motive.w / 32.0F));
         }

         if (this.dir == 2) {
            var3 = Mth.floor(this.x - (double)((float)this.motive.w / 32.0F));
         }

         if (this.dir == 3) {
            var5 = Mth.floor(this.z - (double)((float)this.motive.w / 32.0F));
         }

         var4 = Mth.floor(this.y - (double)((float)this.motive.h / 32.0F));

         for(int var6 = 0; var6 < var1; ++var6) {
            for(int var7 = 0; var7 < var2; ++var7) {
               Material var8;
               if (this.dir != 0 && this.dir != 2) {
                  var8 = this.level.getMaterial(this.xTile, var4 + var7, var5 + var6);
               } else {
                  var8 = this.level.getMaterial(var3 + var6, var4 + var7, this.zTile);
               }

               if (!var8.isSolid()) {
                  return false;
               }
            }
         }

         List var10 = this.level.getEntities(this, this.bb);

         for(int var11 = 0; var11 < var10.size(); ++var11) {
            if (var10.get(var11) instanceof Painting) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isPickable() {
      return true;
   }

   public boolean hurt(Entity var1, int var2) {
      if (!this.removed && !this.level.isOnline) {
         this.remove();
         this.markHurt();
         this.level.addEntity(new ItemEntity(this.level, this.x, this.y, this.z, new ItemInstance(Item.painting)));
      }

      return true;
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      var1.putByte("Dir", (byte)this.dir);
      var1.putString("Motive", this.motive.name);
      var1.putInt("TileX", this.xTile);
      var1.putInt("TileY", this.yTile);
      var1.putInt("TileZ", this.zTile);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      this.dir = var1.getByte("Dir");
      this.xTile = var1.getInt("TileX");
      this.yTile = var1.getInt("TileY");
      this.zTile = var1.getInt("TileZ");
      String var2 = var1.getString("Motive");

      for(Motive var6 : Painting.Motive.values()) {
         if (var6.name.equals(var2)) {
            this.motive = var6;
         }
      }

      if (this.motive == null) {
         this.motive = Painting.Motive.Kebab;
      }

      this.setDir(this.dir);
   }

   public static Motive randomMotive() {
      return Painting.Motive.values()[(new Random()).nextInt(Painting.Motive.values().length)];
   }

   public static enum Motive {
      Kebab("Kebab", 16, 16, 0, 0),
      Aztec("Aztec", 16, 16, 16, 0),
      Alban("Alban", 16, 16, 32, 0),
      Aztec2("Aztec2", 16, 16, 48, 0),
      Bomb("Bomb", 16, 16, 64, 0),
      Plant("Plant", 16, 16, 80, 0),
      Wasteland("Wasteland", 16, 16, 96, 0),
      Pool("Pool", 32, 16, 0, 32),
      Courbet("Courbet", 32, 16, 32, 32),
      Sea("Sea", 32, 16, 64, 32),
      Sunset("Sunset", 32, 16, 96, 32),
      Creebet("Creebet", 32, 16, 128, 32),
      Wanderer("Wanderer", 16, 32, 0, 64),
      Graham("Graham", 16, 32, 16, 64),
      Match("Match", 32, 32, 0, 128),
      Bust("Bust", 32, 32, 32, 128),
      Stage("Stage", 32, 32, 64, 128),
      Void("Void", 32, 32, 96, 128),
      SkullAndRoses("SkullAndRoses", 32, 32, 128, 128),
      Fighters("Fighters", 64, 32, 0, 96),
      Pointer("Pointer", 64, 64, 0, 192),
      Pigscene("Pigscene", 64, 64, 64, 192),
      BurningSkull("BurningSkull", 64, 64, 128, 192),
      Skeleton("Skeleton", 64, 48, 192, 64),
      DonkeyKong("DonkeyKong", 64, 48, 192, 112);

      public final String name;
      public final int w;
      public final int h;
      public final int uo;
      public final int vo;

      private Motive(String var3, int var4, int var5, int var6, int var7) {
         this.name = var3;
         this.w = var4;
         this.h = var5;
         this.uo = var6;
         this.vo = var7;
      }
   }
}
