package net.minecraft.world.entity.player;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.DispenserTileEntity;
import net.minecraft.world.level.tile.entity.FurnaceTileEntity;
import net.minecraft.world.level.tile.entity.SignTileEntity;
import util.Mth;

public abstract class Player extends Mob {
   public static final int MAX_HEALTH = 20;
   public static final int SWING_DURATION = 8;
   public Inventory inventory = new Inventory(this);
   public AbstractContainerMenu inventoryMenu;
   public AbstractContainerMenu containerMenu;
   public byte userType = 0;
   public int score = 0;
   public float oBob;
   public float bob;
   public boolean swinging = false;
   public int swingTime = 0;
   public String name;
   public int dimension;
   public String cloakTexture;
   public double xCloakO;
   public double yCloakO;
   public double zCloakO;
   public double xCloak;
   public double yCloak;
   public double zCloak;
   private int dmgSpill = 0;
   public FishingHook fishing = null;

   public Player(Level var1) {
      super(var1);
      this.inventoryMenu = new InventoryMenu(this.inventory, !var1.isOnline);
      this.containerMenu = this.inventoryMenu;
      this.heightOffset = 1.62F;
      this.moveTo((double)var1.xSpawn + (double)0.5F, (double)(var1.ySpawn + 1), (double)var1.zSpawn + (double)0.5F, 0.0F, 0.0F);
      this.health = 20;
      this.modelName = "humanoid";
      this.rotOffs = 180.0F;
      this.flameTime = 20;
      this.textureName = "/mob/char.png";
   }

   public void tick() {
      super.tick();
      if (!this.level.isOnline && this.containerMenu != null && !this.containerMenu.stillValid(this)) {
         this.closeContainer();
         this.containerMenu = this.inventoryMenu;
      }

      this.xCloakO = this.xCloak;
      this.yCloakO = this.yCloak;
      this.zCloakO = this.zCloak;
      double var1 = this.x - this.xCloak;
      double var3 = this.y - this.yCloak;
      double var5 = this.z - this.zCloak;
      double var7 = (double)10.0F;
      if (var1 > var7) {
         this.xCloakO = this.xCloak = this.x;
      }

      if (var5 > var7) {
         this.zCloakO = this.zCloak = this.z;
      }

      if (var3 > var7) {
         this.yCloakO = this.yCloak = this.y;
      }

      if (var1 < -var7) {
         this.xCloakO = this.xCloak = this.x;
      }

      if (var5 < -var7) {
         this.zCloakO = this.zCloak = this.z;
      }

      if (var3 < -var7) {
         this.yCloakO = this.yCloak = this.y;
      }

      this.xCloak += var1 * (double)0.25F;
      this.zCloak += var5 * (double)0.25F;
      this.yCloak += var3 * (double)0.25F;
   }

   protected void closeContainer() {
      this.containerMenu = this.inventoryMenu;
   }

   public void prepareCustomTextures() {
      this.cloakTexture = "http://s3.amazonaws.com/MinecraftCloaks/" + this.name + ".png";
      this.customTextureUrl2 = this.cloakTexture;
   }

   public void rideTick() {
      super.rideTick();
      this.oBob = this.bob;
      this.bob = 0.0F;
   }

   public void resetPos() {
      this.heightOffset = 1.62F;
      this.setSize(0.6F, 1.8F);
      super.resetPos();
      this.health = 20;
      this.deathTime = 0;
   }

   protected void updateAi() {
      if (this.swinging) {
         ++this.swingTime;
         if (this.swingTime == 8) {
            this.swingTime = 0;
            this.swinging = false;
         }
      } else {
         this.swingTime = 0;
      }

      this.attackAnim = (float)this.swingTime / 8.0F;
   }

   public void aiStep() {
      if (this.level.difficulty == 0 && this.health < 20 && this.tickCount % 20 * 12 == 0) {
         this.heal(1);
      }

      this.inventory.tick();
      this.oBob = this.bob;
      super.aiStep();
      float var1 = Mth.sqrt(this.xd * this.xd + this.zd * this.zd);
      float var2 = (float)Math.atan(-this.yd * (double)0.2F) * 15.0F;
      if (var1 > 0.1F) {
         var1 = 0.1F;
      }

      if (!this.onGround || this.health <= 0) {
         var1 = 0.0F;
      }

      if (this.onGround || this.health <= 0) {
         var2 = 0.0F;
      }

      this.bob += (var1 - this.bob) * 0.4F;
      this.tilt += (var2 - this.tilt) * 0.8F;
      if (this.health > 0) {
         List var3 = this.level.getEntities(this, this.bb.grow((double)1.0F, (double)0.0F, (double)1.0F));
         if (var3 != null) {
            for(int var4 = 0; var4 < var3.size(); ++var4) {
               Entity var5 = (Entity)var3.get(var4);
               if (!var5.removed) {
                  this.touch(var5);
               }
            }
         }
      }

   }

   private void touch(Entity var1) {
      var1.playerTouch(this);
   }

   public boolean addResource(int var1) {
      return this.inventory.add(new ItemInstance(var1, 1, 0));
   }

   public int getScore() {
      return this.score;
   }

   public void die(Entity var1) {
      super.die(var1);
      this.setSize(0.2F, 0.2F);
      this.setPos(this.x, this.y, this.z);
      this.yd = (double)0.1F;
      if (this.name.equals("Notch")) {
         this.drop(new ItemInstance(Item.apple, 1), true);
      }

      this.inventory.dropAll();
      if (var1 != null) {
         this.xd = (double)(-Mth.cos((this.hurtDir + this.yRot) * (float)Math.PI / 180.0F) * 0.1F);
         this.zd = (double)(-Mth.sin((this.hurtDir + this.yRot) * (float)Math.PI / 180.0F) * 0.1F);
      } else {
         this.xd = this.zd = (double)0.0F;
      }

      this.heightOffset = 0.1F;
   }

   public void awardKillScore(Entity var1, int var2) {
      this.score += var2;
   }

   public boolean isShootable() {
      return true;
   }

   public boolean isCreativeModeAllowed() {
      return true;
   }

   public void drop() {
      this.drop(this.inventory.removeItem(this.inventory.selected, 1), false);
   }

   public void drop(ItemInstance var1) {
      this.drop(var1, false);
   }

   public void drop(ItemInstance var1, boolean var2) {
      if (var1 != null) {
         ItemEntity var3 = new ItemEntity(this.level, this.x, this.y - (double)0.3F + (double)this.getHeadHeight(), this.z, var1);
         var3.throwTime = 40;
         float var4 = 0.1F;
         if (var2) {
            float var5 = this.random.nextFloat() * 0.5F;
            float var6 = this.random.nextFloat() * (float)Math.PI * 2.0F;
            var3.xd = (double)(-Mth.sin(var6) * var5);
            var3.zd = (double)(Mth.cos(var6) * var5);
            var3.yd = (double)0.2F;
         } else {
            var4 = 0.3F;
            var3.xd = (double)(-Mth.sin(this.yRot / 180.0F * (float)Math.PI) * Mth.cos(this.xRot / 180.0F * (float)Math.PI) * var4);
            var3.zd = (double)(Mth.cos(this.yRot / 180.0F * (float)Math.PI) * Mth.cos(this.xRot / 180.0F * (float)Math.PI) * var4);
            var3.yd = (double)(-Mth.sin(this.xRot / 180.0F * (float)Math.PI) * var4 + 0.1F);
            var4 = 0.02F;
            float var10 = this.random.nextFloat() * (float)Math.PI * 2.0F;
            var4 *= this.random.nextFloat();
            var3.xd += Math.cos((double)var10) * (double)var4;
            var3.yd += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F);
            var3.zd += Math.sin((double)var10) * (double)var4;
         }

         this.reallyDrop(var3);
      }
   }

   protected void reallyDrop(ItemEntity var1) {
      this.level.addEntity(var1);
   }

   public float getDestroySpeed(Tile var1) {
      float var2 = this.inventory.getDestroySpeed(var1);
      if (this.isUnderLiquid(Material.water)) {
         var2 /= 5.0F;
      }

      if (!this.onGround) {
         var2 /= 5.0F;
      }

      return var2;
   }

   public boolean canDestroy(Tile var1) {
      return this.inventory.canDestroy(var1);
   }

   public void readAdditionalSaveData(CompoundTag var1) {
      super.readAdditionalSaveData(var1);
      ListTag var2 = var1.getList("Inventory");
      this.inventory.load(var2);
      this.dimension = var1.getInt("Dimension");
   }

   public void addAdditonalSaveData(CompoundTag var1) {
      super.addAdditonalSaveData(var1);
      var1.put("Inventory", this.inventory.save(new ListTag()));
      var1.putInt("Dimension", this.dimension);
   }

   public void openContainer(Container var1) {
   }

   public void startCrafting(int var1, int var2, int var3) {
   }

   public void take(Entity var1, int var2) {
   }

   public float getHeadHeight() {
      return 0.12F;
   }

   public boolean hurt(Entity var1, int var2) {
      this.noActionTime = 0;
      if (this.health <= 0) {
         return false;
      } else {
         if (var1 instanceof Monster || var1 instanceof Arrow) {
            if (this.level.difficulty == 0) {
               var2 = 0;
            }

            if (this.level.difficulty == 1) {
               var2 = var2 / 3 + 1;
            }

            if (this.level.difficulty == 3) {
               var2 = var2 * 3 / 2;
            }
         }

         return var2 == 0 ? false : super.hurt(var1, var2);
      }
   }

   protected void actuallyHurt(int var1) {
      int var2 = 25 - this.inventory.getArmorValue();
      int var3 = var1 * var2 + this.dmgSpill;
      this.inventory.hurtArmor(var1);
      var1 = var3 / 25;
      this.dmgSpill = var3 % 25;
      super.actuallyHurt(var1);
   }

   public void openFurnace(FurnaceTileEntity var1) {
   }

   public void openTrap(DispenserTileEntity var1) {
   }

   public void openTextEdit(SignTileEntity var1) {
   }

   public void interact(Entity var1) {
      if (!var1.interact(this)) {
         ItemInstance var2 = this.getSelectedItem();
         if (var2 != null && var1 instanceof Mob) {
            var2.interactEnemy((Mob)var1);
            if (var2.count <= 0) {
               var2.snap(this);
               this.removeSelectedItem();
            }
         }

      }
   }

   public ItemInstance getSelectedItem() {
      return this.inventory.getSelected();
   }

   public void removeSelectedItem() {
      this.inventory.setItem(this.inventory.selected, (ItemInstance)null);
   }

   public double getRidingHeight() {
      return (double)(this.heightOffset - 0.5F);
   }

   public void swing() {
      this.swingTime = -1;
      this.swinging = true;
   }

   public void attack(Entity var1) {
      int var2 = this.inventory.getAttackDamage(var1);
      if (var2 > 0) {
         var1.hurt(this, var2);
         ItemInstance var3 = this.getSelectedItem();
         if (var3 != null && var1 instanceof Mob) {
            var3.hurtEnemy((Mob)var1);
            if (var3.count <= 0) {
               var3.snap(this);
               this.removeSelectedItem();
            }
         }
      }

   }

   public void respawn() {
   }

   public Slot getInventorySlot(int var1) {
      return null;
   }

   public void carriedChanged(ItemInstance var1) {
   }

   public void remove() {
      super.remove();
      this.inventoryMenu.removed(this);
      if (this.containerMenu != null) {
         this.containerMenu.removed(this);
      }

   }
}
