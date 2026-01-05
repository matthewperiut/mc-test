package net.minecraft.world.entity.item;

import com.mojang.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import util.Mth;

public class ItemEntity extends Entity {
   private static final int LIFETIME = 6000;
   public ItemInstance item;
   private int tickCount;
   public int age = 0;
   public int throwTime;
   private int health = 5;
   public float bobOffs = (float)(Math.random() * Math.PI * (double)2.0F);

   public ItemEntity(Level var1, double var2, double var4, double var6, ItemInstance var8) {
      super(var1);
      this.setSize(0.25F, 0.25F);
      this.heightOffset = this.bbHeight / 2.0F;
      this.setPos(var2, var4, var6);
      this.item = var8;
      this.yRot = (float)(Math.random() * (double)360.0F);
      this.xd = (double)((float)(Math.random() * (double)0.2F - (double)0.1F));
      this.yd = (double)0.2F;
      this.zd = (double)((float)(Math.random() * (double)0.2F - (double)0.1F));
      this.makeStepSound = false;
   }

   public ItemEntity(Level var1) {
      super(var1);
      this.setSize(0.25F, 0.25F);
      this.heightOffset = this.bbHeight / 2.0F;
   }

   protected void defineSynchedData() {
   }

   public void tick() {
      super.tick();
      if (this.throwTime > 0) {
         --this.throwTime;
      }

      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.yd -= (double)0.04F;
      if (this.level.getMaterial(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z)) == Material.lava) {
         this.yd = (double)0.2F;
         this.xd = (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         this.zd = (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         this.level.playSound(this, "random.fizz", 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
      }

      this.checkInTile(this.x, this.y, this.z);
      this.move(this.xd, this.yd, this.zd);
      float var1 = 0.98F;
      if (this.onGround) {
         var1 = 0.58800006F;
         int var2 = this.level.getTile(Mth.floor(this.x), Mth.floor(this.bb.y0) - 1, Mth.floor(this.z));
         if (var2 > 0) {
            var1 = Tile.tiles[var2].friction * 0.98F;
         }
      }

      this.xd *= (double)var1;
      this.yd *= (double)0.98F;
      this.zd *= (double)var1;
      if (this.onGround) {
         this.yd *= (double)-0.5F;
      }

      ++this.tickCount;
      ++this.age;
      if (this.age >= 6000) {
         this.remove();
      }

   }

   public boolean isInWater() {
      return this.level.checkAndHandleWater(this.bb, Material.water, this);
   }

   private boolean checkInTile(double var1, double var3, double var5) {
      int var7 = Mth.floor(var1);
      int var8 = Mth.floor(var3);
      int var9 = Mth.floor(var5);
      double var10 = var1 - (double)var7;
      double var12 = var3 - (double)var8;
      double var14 = var5 - (double)var9;
      if (Tile.solid[this.level.getTile(var7, var8, var9)]) {
         boolean var16 = !Tile.solid[this.level.getTile(var7 - 1, var8, var9)];
         boolean var17 = !Tile.solid[this.level.getTile(var7 + 1, var8, var9)];
         boolean var18 = !Tile.solid[this.level.getTile(var7, var8 - 1, var9)];
         boolean var19 = !Tile.solid[this.level.getTile(var7, var8 + 1, var9)];
         boolean var20 = !Tile.solid[this.level.getTile(var7, var8, var9 - 1)];
         boolean var21 = !Tile.solid[this.level.getTile(var7, var8, var9 + 1)];
         byte var22 = -1;
         double var23 = (double)9999.0F;
         if (var16 && var10 < var23) {
            var23 = var10;
            var22 = 0;
         }

         if (var17 && (double)1.0F - var10 < var23) {
            var23 = (double)1.0F - var10;
            var22 = 1;
         }

         if (var18 && var12 < var23) {
            var23 = var12;
            var22 = 2;
         }

         if (var19 && (double)1.0F - var12 < var23) {
            var23 = (double)1.0F - var12;
            var22 = 3;
         }

         if (var20 && var14 < var23) {
            var23 = var14;
            var22 = 4;
         }

         if (var21 && (double)1.0F - var14 < var23) {
            var23 = (double)1.0F - var14;
            var22 = 5;
         }

         float var25 = this.random.nextFloat() * 0.2F + 0.1F;
         if (var22 == 0) {
            this.xd = (double)(-var25);
         }

         if (var22 == 1) {
            this.xd = (double)var25;
         }

         if (var22 == 2) {
            this.yd = (double)(-var25);
         }

         if (var22 == 3) {
            this.yd = (double)var25;
         }

         if (var22 == 4) {
            this.zd = (double)(-var25);
         }

         if (var22 == 5) {
            this.zd = (double)var25;
         }
      }

      return false;
   }

   protected void burn(int var1) {
      this.hurt((Entity)null, var1);
   }

   public boolean hurt(Entity var1, int var2) {
      this.markHurt();
      this.health -= var2;
      if (this.health <= 0) {
         this.remove();
      }

      return false;
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      var1.putShort("Health", (short)((byte)this.health));
      var1.putShort("Age", (short)this.age);
      var1.putCompound("Item", this.item.save(new CompoundTag()));
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      this.health = var1.getShort("Health") & 255;
      this.age = var1.getShort("Age");
      CompoundTag var2 = var1.getCompound("Item");
      this.item = new ItemInstance(var2);
   }

   public void playerTouch(Player var1) {
      if (!this.level.isOnline) {
         int var2 = this.item.count;
         if (this.throwTime == 0 && var1.inventory.add(this.item)) {
            this.level.playSound(this, "random.pop", 0.2F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            var1.take(this, var2);
            this.remove();
         }

      }
   }
}
