package net.minecraft.world.entity.item;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import util.Mth;

public class Minecart extends Entity implements Container {
   public static final int RIDEABLE = 0;
   public static final int CHEST = 1;
   public static final int FURNACE = 2;
   private ItemInstance[] items;
   public static final long serialVersionUID = 0L;
   public int damage;
   public int hurtTime;
   public int hurtDir;
   private boolean flipped;
   public int type;
   public int fuel;
   public double xPush;
   public double zPush;
   private static final int[][][] EXITS = new int[][][]{{{0, 0, -1}, {0, 0, 1}}, {{-1, 0, 0}, {1, 0, 0}}, {{-1, -1, 0}, {1, 0, 0}}, {{-1, 0, 0}, {1, -1, 0}}, {{0, 0, -1}, {0, -1, 1}}, {{0, -1, -1}, {0, 0, 1}}, {{0, 0, 1}, {1, 0, 0}}, {{0, 0, 1}, {-1, 0, 0}}, {{0, 0, -1}, {-1, 0, 0}}, {{0, 0, -1}, {1, 0, 0}}};
   private int lSteps;
   private double lx;
   private double ly;
   private double lz;
   private double lyr;
   private double lxr;
   private double lxd;
   private double lyd;
   private double lzd;

   public Minecart(Level var1) {
      super(var1);
      this.items = new ItemInstance[36];
      this.damage = 0;
      this.hurtTime = 0;
      this.hurtDir = 1;
      this.flipped = false;
      this.blocksBuilding = true;
      this.setSize(0.98F, 0.7F);
      this.heightOffset = this.bbHeight / 2.0F;
      this.makeStepSound = false;
   }

   protected void defineSynchedData() {
   }

   public AABB getCollideAgainstBox(Entity var1) {
      return var1.bb;
   }

   public AABB getCollideBox() {
      return null;
   }

   public boolean isPushable() {
      return true;
   }

   public Minecart(Level var1, double var2, double var4, double var6, int var8) {
      this(var1);
      this.setPos(var2, var4 + (double)this.heightOffset, var6);
      this.xd = (double)0.0F;
      this.yd = (double)0.0F;
      this.zd = (double)0.0F;
      this.xo = var2;
      this.yo = var4;
      this.zo = var6;
      this.type = var8;
   }

   public double getRideHeight() {
      return (double)this.bbHeight * (double)0.0F - (double)0.3F;
   }

   public boolean hurt(Entity var1, int var2) {
      if (!this.level.isOnline && !this.removed) {
         this.hurtDir = -this.hurtDir;
         this.hurtTime = 10;
         this.markHurt();
         this.damage += var2 * 10;
         if (this.damage > 40) {
            this.spawnAtLocation(Item.minecart.id, 1, 0.0F);
            if (this.type == 1) {
               this.spawnAtLocation(Tile.chest.id, 1, 0.0F);
            } else if (this.type == 2) {
               this.spawnAtLocation(Tile.furnace.id, 1, 0.0F);
            }

            this.remove();
         }

         return true;
      } else {
         return true;
      }
   }

   public void animateHurt() {
      System.out.println("Animating hurt");
      this.hurtDir = -this.hurtDir;
      this.hurtTime = 10;
      this.damage += this.damage * 10;
   }

   public boolean isPickable() {
      return !this.removed;
   }

   public void remove() {
      for(int var1 = 0; var1 < this.getContainerSize(); ++var1) {
         ItemInstance var2 = this.getItem(var1);
         if (var2 != null) {
            float var3 = this.random.nextFloat() * 0.8F + 0.1F;
            float var4 = this.random.nextFloat() * 0.8F + 0.1F;
            float var5 = this.random.nextFloat() * 0.8F + 0.1F;

            while(var2.count > 0) {
               int var6 = this.random.nextInt(21) + 10;
               if (var6 > var2.count) {
                  var6 = var2.count;
               }

               var2.count -= var6;
               ItemEntity var7 = new ItemEntity(this.level, this.x + (double)var3, this.y + (double)var4, this.z + (double)var5, new ItemInstance(var2.id, var6, var2.getAuxValue()));
               float var8 = 0.05F;
               var7.xd = (double)((float)this.random.nextGaussian() * var8);
               var7.yd = (double)((float)this.random.nextGaussian() * var8 + 0.2F);
               var7.zd = (double)((float)this.random.nextGaussian() * var8);
               this.level.addEntity(var7);
            }
         }
      }

      super.remove();
   }

   public void tick() {
      if (this.hurtTime > 0) {
         --this.hurtTime;
      }

      if (this.damage > 0) {
         --this.damage;
      }

      if (this.level.isOnline && this.lSteps > 0) {
         if (this.lSteps > 0) {
            double var41 = this.x + (this.lx - this.x) / (double)this.lSteps;
            double var42 = this.y + (this.ly - this.y) / (double)this.lSteps;
            double var5 = this.z + (this.lz - this.z) / (double)this.lSteps;

            double var43;
            for(var43 = this.lyr - (double)this.yRot; var43 < (double)-180.0F; var43 += (double)360.0F) {
            }

            while(var43 >= (double)180.0F) {
               var43 -= (double)360.0F;
            }

            this.yRot = (float)((double)this.yRot + var43 / (double)this.lSteps);
            this.xRot = (float)((double)this.xRot + (this.lxr - (double)this.xRot) / (double)this.lSteps);
            --this.lSteps;
            this.setPos(var41, var42, var5);
            this.setRot(this.yRot, this.xRot);
         } else {
            this.setPos(this.x, this.y, this.z);
            this.setRot(this.yRot, this.xRot);
         }

      } else {
         this.xo = this.x;
         this.yo = this.y;
         this.zo = this.z;
         this.yd -= (double)0.04F;
         int var1 = Mth.floor(this.x);
         int var2 = Mth.floor(this.y);
         int var3 = Mth.floor(this.z);
         if (this.level.getTile(var1, var2 - 1, var3) == Tile.rail.id) {
            --var2;
         }

         double var4 = 0.4;
         boolean var6 = false;
         double var7 = (double)0.0078125F;
         if (this.level.getTile(var1, var2, var3) == Tile.rail.id) {
            Vec3 var9 = this.getPos(this.x, this.y, this.z);
            int var10 = this.level.getData(var1, var2, var3);
            this.y = (double)var2;
            if (var10 >= 2 && var10 <= 5) {
               this.y = (double)(var2 + 1);
            }

            if (var10 == 2) {
               this.xd -= var7;
            }

            if (var10 == 3) {
               this.xd += var7;
            }

            if (var10 == 4) {
               this.zd += var7;
            }

            if (var10 == 5) {
               this.zd -= var7;
            }

            int[][] var11 = EXITS[var10];
            double var12 = (double)(var11[1][0] - var11[0][0]);
            double var14 = (double)(var11[1][2] - var11[0][2]);
            double var16 = Math.sqrt(var12 * var12 + var14 * var14);
            double var18 = this.xd * var12 + this.zd * var14;
            if (var18 < (double)0.0F) {
               var12 = -var12;
               var14 = -var14;
            }

            double var20 = Math.sqrt(this.xd * this.xd + this.zd * this.zd);
            this.xd = var20 * var12 / var16;
            this.zd = var20 * var14 / var16;
            double var22 = (double)0.0F;
            double var24 = (double)var1 + (double)0.5F + (double)var11[0][0] * (double)0.5F;
            double var26 = (double)var3 + (double)0.5F + (double)var11[0][2] * (double)0.5F;
            double var28 = (double)var1 + (double)0.5F + (double)var11[1][0] * (double)0.5F;
            double var30 = (double)var3 + (double)0.5F + (double)var11[1][2] * (double)0.5F;
            var12 = var28 - var24;
            var14 = var30 - var26;
            if (var12 == (double)0.0F) {
               this.x = (double)var1 + (double)0.5F;
               var22 = this.z - (double)var3;
            } else if (var14 == (double)0.0F) {
               this.z = (double)var3 + (double)0.5F;
               var22 = this.x - (double)var1;
            } else {
               double var32 = this.x - var24;
               double var34 = this.z - var26;
               double var36 = (var32 * var12 + var34 * var14) * (double)2.0F;
               var22 = var36;
            }

            this.x = var24 + var12 * var22;
            this.z = var26 + var14 * var22;
            this.setPos(this.x, this.y + (double)this.heightOffset, this.z);
            double var52 = this.xd;
            double var53 = this.zd;
            if (this.rider != null) {
               var52 *= (double)0.75F;
               var53 *= (double)0.75F;
            }

            if (var52 < -var4) {
               var52 = -var4;
            }

            if (var52 > var4) {
               var52 = var4;
            }

            if (var53 < -var4) {
               var53 = -var4;
            }

            if (var53 > var4) {
               var53 = var4;
            }

            this.move(var52, (double)0.0F, var53);
            if (var11[0][1] != 0 && Mth.floor(this.x) - var1 == var11[0][0] && Mth.floor(this.z) - var3 == var11[0][2]) {
               this.setPos(this.x, this.y + (double)var11[0][1], this.z);
            } else if (var11[1][1] != 0 && Mth.floor(this.x) - var1 == var11[1][0] && Mth.floor(this.z) - var3 == var11[1][2]) {
               this.setPos(this.x, this.y + (double)var11[1][1], this.z);
            }

            if (this.rider != null) {
               this.xd *= (double)0.997F;
               this.yd *= (double)0.0F;
               this.zd *= (double)0.997F;
            } else {
               if (this.type == 2) {
                  double var54 = (double)Mth.sqrt(this.xPush * this.xPush + this.zPush * this.zPush);
                  if (var54 > 0.01) {
                     var6 = true;
                     this.xPush /= var54;
                     this.zPush /= var54;
                     double var38 = 0.04;
                     this.xd *= (double)0.8F;
                     this.yd *= (double)0.0F;
                     this.zd *= (double)0.8F;
                     this.xd += this.xPush * var38;
                     this.zd += this.zPush * var38;
                  } else {
                     this.xd *= (double)0.9F;
                     this.yd *= (double)0.0F;
                     this.zd *= (double)0.9F;
                  }
               }

               this.xd *= (double)0.96F;
               this.yd *= (double)0.0F;
               this.zd *= (double)0.96F;
            }

            Vec3 var55 = this.getPos(this.x, this.y, this.z);
            if (var55 != null && var9 != null) {
               double var37 = (var9.y - var55.y) * 0.05;
               var20 = Math.sqrt(this.xd * this.xd + this.zd * this.zd);
               if (var20 > (double)0.0F) {
                  this.xd = this.xd / var20 * (var20 + var37);
                  this.zd = this.zd / var20 * (var20 + var37);
               }

               this.setPos(this.x, var55.y, this.z);
            }

            int var56 = Mth.floor(this.x);
            int var57 = Mth.floor(this.z);
            if (var56 != var1 || var57 != var3) {
               var20 = Math.sqrt(this.xd * this.xd + this.zd * this.zd);
               this.xd = var20 * (double)(var56 - var1);
               this.zd = var20 * (double)(var57 - var3);
            }

            if (this.type == 2) {
               double var39 = (double)Mth.sqrt(this.xPush * this.xPush + this.zPush * this.zPush);
               if (var39 > 0.01 && this.xd * this.xd + this.zd * this.zd > 0.001) {
                  this.xPush /= var39;
                  this.zPush /= var39;
                  if (this.xPush * this.xd + this.zPush * this.zd < (double)0.0F) {
                     this.xPush = (double)0.0F;
                     this.zPush = (double)0.0F;
                  } else {
                     this.xPush = this.xd;
                     this.zPush = this.zd;
                  }
               }
            }
         } else {
            if (this.xd < -var4) {
               this.xd = -var4;
            }

            if (this.xd > var4) {
               this.xd = var4;
            }

            if (this.zd < -var4) {
               this.zd = -var4;
            }

            if (this.zd > var4) {
               this.zd = var4;
            }

            if (this.onGround) {
               this.xd *= (double)0.5F;
               this.yd *= (double)0.5F;
               this.zd *= (double)0.5F;
            }

            this.move(this.xd, this.yd, this.zd);
            if (!this.onGround) {
               this.xd *= (double)0.95F;
               this.yd *= (double)0.95F;
               this.zd *= (double)0.95F;
            }
         }

         this.xRot = 0.0F;
         double var44 = this.xo - this.x;
         double var45 = this.zo - this.z;
         if (var44 * var44 + var45 * var45 > 0.001) {
            this.yRot = (float)(Math.atan2(var45, var44) * (double)180.0F / Math.PI);
            if (this.flipped) {
               this.yRot += 180.0F;
            }
         }

         double var13;
         for(var13 = (double)(this.yRot - this.yRotO); var13 >= (double)180.0F; var13 -= (double)360.0F) {
         }

         while(var13 < (double)-180.0F) {
            var13 += (double)360.0F;
         }

         if (var13 < (double)-170.0F || var13 >= (double)170.0F) {
            this.yRot += 180.0F;
            this.flipped = !this.flipped;
         }

         this.setRot(this.yRot, this.xRot);
         List var15 = this.level.getEntities(this, this.bb.grow((double)0.2F, (double)0.0F, (double)0.2F));
         if (var15 != null && var15.size() > 0) {
            for(int var48 = 0; var48 < var15.size(); ++var48) {
               Entity var17 = (Entity)var15.get(var48);
               if (var17 != this.rider && var17.isPushable() && var17 instanceof Minecart) {
                  var17.push(this);
               }
            }
         }

         if (this.rider != null && this.rider.removed) {
            this.rider = null;
         }

         if (var6 && this.random.nextInt(4) == 0) {
            --this.fuel;
            if (this.fuel < 0) {
               this.xPush = this.zPush = (double)0.0F;
            }

            this.level.addParticle("largesmoke", this.x, this.y + 0.8, this.z, (double)0.0F, (double)0.0F, (double)0.0F);
         }

      }
   }

   public Vec3 getPosOffs(double var1, double var3, double var5, double var7) {
      int var9 = Mth.floor(var1);
      int var10 = Mth.floor(var3);
      int var11 = Mth.floor(var5);
      if (this.level.getTile(var9, var10 - 1, var11) == Tile.rail.id) {
         --var10;
      }

      if (this.level.getTile(var9, var10, var11) == Tile.rail.id) {
         int var12 = this.level.getData(var9, var10, var11);
         var3 = (double)var10;
         if (var12 >= 2 && var12 <= 5) {
            var3 = (double)(var10 + 1);
         }

         int[][] var13 = EXITS[var12];
         double var14 = (double)(var13[1][0] - var13[0][0]);
         double var16 = (double)(var13[1][2] - var13[0][2]);
         double var18 = Math.sqrt(var14 * var14 + var16 * var16);
         var14 /= var18;
         var16 /= var18;
         var1 += var14 * var7;
         var5 += var16 * var7;
         if (var13[0][1] != 0 && Mth.floor(var1) - var9 == var13[0][0] && Mth.floor(var5) - var11 == var13[0][2]) {
            var3 += (double)var13[0][1];
         } else if (var13[1][1] != 0 && Mth.floor(var1) - var9 == var13[1][0] && Mth.floor(var5) - var11 == var13[1][2]) {
            var3 += (double)var13[1][1];
         }

         return this.getPos(var1, var3, var5);
      } else {
         return null;
      }
   }

   public Vec3 getPos(double var1, double var3, double var5) {
      int var7 = Mth.floor(var1);
      int var8 = Mth.floor(var3);
      int var9 = Mth.floor(var5);
      if (this.level.getTile(var7, var8 - 1, var9) == Tile.rail.id) {
         --var8;
      }

      if (this.level.getTile(var7, var8, var9) == Tile.rail.id) {
         int var10 = this.level.getData(var7, var8, var9);
         var3 = (double)var8;
         if (var10 >= 2 && var10 <= 5) {
            var3 = (double)(var8 + 1);
         }

         int[][] var11 = EXITS[var10];
         double var12 = (double)0.0F;
         double var14 = (double)var7 + (double)0.5F + (double)var11[0][0] * (double)0.5F;
         double var16 = (double)var8 + (double)0.5F + (double)var11[0][1] * (double)0.5F;
         double var18 = (double)var9 + (double)0.5F + (double)var11[0][2] * (double)0.5F;
         double var20 = (double)var7 + (double)0.5F + (double)var11[1][0] * (double)0.5F;
         double var22 = (double)var8 + (double)0.5F + (double)var11[1][1] * (double)0.5F;
         double var24 = (double)var9 + (double)0.5F + (double)var11[1][2] * (double)0.5F;
         double var26 = var20 - var14;
         double var28 = (var22 - var16) * (double)2.0F;
         double var30 = var24 - var18;
         if (var26 == (double)0.0F) {
            var1 = (double)var7 + (double)0.5F;
            var12 = var5 - (double)var9;
         } else if (var30 == (double)0.0F) {
            var5 = (double)var9 + (double)0.5F;
            var12 = var1 - (double)var7;
         } else {
            double var32 = var1 - var14;
            double var34 = var5 - var18;
            double var36 = (var32 * var26 + var34 * var30) * (double)2.0F;
            var12 = var36;
         }

         var1 = var14 + var26 * var12;
         var3 = var16 + var28 * var12;
         var5 = var18 + var30 * var12;
         if (var28 < (double)0.0F) {
            ++var3;
         }

         if (var28 > (double)0.0F) {
            var3 += (double)0.5F;
         }

         return Vec3.newTemp(var1, var3, var5);
      } else {
         return null;
      }
   }

   protected void addAdditonalSaveData(CompoundTag var1) {
      var1.putInt("Type", this.type);
      if (this.type == 2) {
         var1.putDouble("PushX", this.xPush);
         var1.putDouble("PushZ", this.zPush);
         var1.putShort("Fuel", (short)this.fuel);
      } else if (this.type == 1) {
         ListTag var2 = new ListTag();

         for(int var3 = 0; var3 < this.items.length; ++var3) {
            if (this.items[var3] != null) {
               CompoundTag var4 = new CompoundTag();
               var4.putByte("Slot", (byte)var3);
               this.items[var3].save(var4);
               var2.add(var4);
            }
         }

         var1.put("Items", var2);
      }

   }

   protected void readAdditionalSaveData(CompoundTag var1) {
      this.type = var1.getInt("Type");
      if (this.type == 2) {
         this.xPush = var1.getDouble("PushX");
         this.zPush = var1.getDouble("PushZ");
         this.fuel = var1.getShort("Fuel");
      } else if (this.type == 1) {
         ListTag var2 = var1.getList("Items");
         this.items = new ItemInstance[this.getContainerSize()];

         for(int var3 = 0; var3 < var2.size(); ++var3) {
            CompoundTag var4 = (CompoundTag)var2.get(var3);
            int var5 = var4.getByte("Slot") & 255;
            if (var5 >= 0 && var5 < this.items.length) {
               this.items[var5] = new ItemInstance(var4);
            }
         }
      }

   }

   public float getShadowHeightOffs() {
      return 0.0F;
   }

   public void push(Entity var1) {
      if (!this.level.isOnline) {
         if (var1 != this.rider) {
            if (var1 instanceof Mob && !(var1 instanceof Player) && this.type == 0 && this.xd * this.xd + this.zd * this.zd > 0.01 && this.rider == null && var1.riding == null) {
               var1.ride(this);
            }

            double var2 = var1.x - this.x;
            double var4 = var1.z - this.z;
            double var6 = var2 * var2 + var4 * var4;
            if (var6 >= (double)1.0E-4F) {
               var6 = (double)Mth.sqrt(var6);
               var2 /= var6;
               var4 /= var6;
               double var8 = (double)1.0F / var6;
               if (var8 > (double)1.0F) {
                  var8 = (double)1.0F;
               }

               var2 *= var8;
               var4 *= var8;
               var2 *= (double)0.1F;
               var4 *= (double)0.1F;
               var2 *= (double)(1.0F - this.pushthrough);
               var4 *= (double)(1.0F - this.pushthrough);
               var2 *= (double)0.5F;
               var4 *= (double)0.5F;
               if (var1 instanceof Minecart) {
                  double var10 = var1.xd + this.xd;
                  double var12 = var1.zd + this.zd;
                  if (((Minecart)var1).type == 2 && this.type != 2) {
                     this.xd *= (double)0.2F;
                     this.zd *= (double)0.2F;
                     this.push(var1.xd - var2, (double)0.0F, var1.zd - var4);
                     var1.xd *= (double)0.7F;
                     var1.zd *= (double)0.7F;
                  } else if (((Minecart)var1).type != 2 && this.type == 2) {
                     var1.xd *= (double)0.2F;
                     var1.zd *= (double)0.2F;
                     var1.push(this.xd + var2, (double)0.0F, this.zd + var4);
                     this.xd *= (double)0.7F;
                     this.zd *= (double)0.7F;
                  } else {
                     var10 /= (double)2.0F;
                     var12 /= (double)2.0F;
                     this.xd *= (double)0.2F;
                     this.zd *= (double)0.2F;
                     this.push(var10 - var2, (double)0.0F, var12 - var4);
                     var1.xd *= (double)0.2F;
                     var1.zd *= (double)0.2F;
                     var1.push(var10 + var2, (double)0.0F, var12 + var4);
                  }
               } else {
                  this.push(-var2, (double)0.0F, -var4);
                  var1.push(var2 / (double)4.0F, (double)0.0F, var4 / (double)4.0F);
               }
            }

         }
      }
   }

   public int getContainerSize() {
      return 27;
   }

   public ItemInstance getItem(int var1) {
      return this.items[var1];
   }

   public ItemInstance removeItem(int var1, int var2) {
      if (this.items[var1] != null) {
         if (this.items[var1].count <= var2) {
            ItemInstance var4 = this.items[var1];
            this.items[var1] = null;
            return var4;
         } else {
            ItemInstance var3 = this.items[var1].remove(var2);
            if (this.items[var1].count == 0) {
               this.items[var1] = null;
            }

            return var3;
         }
      } else {
         return null;
      }
   }

   public void setItem(int var1, ItemInstance var2) {
      this.items[var1] = var2;
      if (var2 != null && var2.count > this.getMaxStackSize()) {
         var2.count = this.getMaxStackSize();
      }

   }

   public String getName() {
      return "Minecart";
   }

   public void load(ListTag<CompoundTag> var1) {
   }

   public int getMaxStackSize() {
      return 64;
   }

   public void setChanged() {
   }

   public boolean interact(Player var1) {
      if (this.type == 0) {
         if (this.rider != null && this.rider instanceof Player && this.rider != var1) {
            return true;
         }

         if (!this.level.isOnline) {
            var1.ride(this);
         }
      } else if (this.type == 1) {
         if (!this.level.isOnline) {
            var1.openContainer(this);
         }
      } else if (this.type == 2) {
         ItemInstance var2 = var1.inventory.getSelected();
         if (var2 != null && var2.id == Item.coal.id) {
            if (--var2.count == 0) {
               var1.inventory.setItem(var1.inventory.selected, (ItemInstance)null);
            }

            this.fuel += 1200;
         }

         this.xPush = this.x - var1.x;
         this.zPush = this.z - var1.z;
      }

      return true;
   }

   public float getLootContent() {
      int var1 = 0;

      for(int var2 = 0; var2 < this.items.length; ++var2) {
         if (this.items[var2] != null) {
            ++var1;
         }
      }

      return (float)var1 / (float)this.items.length;
   }

   public void lerpTo(double var1, double var3, double var5, float var7, float var8, int var9) {
      this.lx = var1;
      this.ly = var3;
      this.lz = var5;
      this.lyr = (double)var7;
      this.lxr = (double)var8;
      this.lSteps = var9 + 2;
      this.xd = this.lxd;
      this.yd = this.lyd;
      this.zd = this.lzd;
   }

   public void lerpMotion(double var1, double var3, double var5) {
      this.lxd = this.xd = var1;
      this.lyd = this.yd = var3;
      this.lzd = this.zd = var5;
   }

   public boolean stillValid(Player var1) {
      if (this.removed) {
         return false;
      } else {
         return !(var1.distanceToSqr(this) > (double)64.0F);
      }
   }
}
