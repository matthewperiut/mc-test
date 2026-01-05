package net.minecraft.world.entity.player;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;

public class Inventory implements Container {
   public static final int POP_TIME_DURATION = 5;
   public static final int MAX_INVENTORY_STACK_SIZE = 64;
   private static final int INVENTORY_SIZE = 36;
   public ItemInstance[] items = new ItemInstance[36];
   public ItemInstance[] armor = new ItemInstance[4];
   public int selected = 0;
   private Player player;
   private ItemInstance carried;
   public boolean changed = false;

   public Inventory(Player var1) {
      this.player = var1;
   }

   public ItemInstance getSelected() {
      return this.items[this.selected];
   }

   private int getSlot(int var1) {
      for(int var2 = 0; var2 < this.items.length; ++var2) {
         if (this.items[var2] != null && this.items[var2].id == var1) {
            return var2;
         }
      }

      return -1;
   }

   private int getSlotWithRemainingSpace(ItemInstance var1) {
      for(int var2 = 0; var2 < this.items.length; ++var2) {
         if (this.items[var2] != null && this.items[var2].id == var1.id && this.items[var2].isStackable() && this.items[var2].count < this.items[var2].getMaxStackSize() && this.items[var2].count < this.getMaxStackSize() && (!this.items[var2].isStackedByData() || this.items[var2].getAuxValue() == var1.getAuxValue())) {
            return var2;
         }
      }

      return -1;
   }

   private int getFreeSlot() {
      for(int var1 = 0; var1 < this.items.length; ++var1) {
         if (this.items[var1] == null) {
            return var1;
         }
      }

      return -1;
   }

   public void grabTexture(int var1, boolean var2) {
      int var3 = this.getSlot(var1);
      if (var3 >= 0 && var3 < 9) {
         this.selected = var3;
      }
   }

   public void swapPaint(int var1) {
      if (var1 > 0) {
         var1 = 1;
      }

      if (var1 < 0) {
         var1 = -1;
      }

      for(this.selected -= var1; this.selected < 0; this.selected += 9) {
      }

      while(this.selected >= 9) {
         this.selected -= 9;
      }

   }

   public void replaceSlot(int var1) {
   }

   public void replaceSlot(Tile var1) {
      if (var1 != null) {
         int var2 = this.getSlot(var1.id);
         if (var2 >= 0) {
            this.items[var2] = this.items[this.selected];
         }

         this.items[this.selected] = new ItemInstance(Item.items[var1.id]);
      }

   }

   private int addResource(ItemInstance var1) {
      int var2 = var1.id;
      int var3 = var1.count;
      int var4 = this.getSlotWithRemainingSpace(var1);
      if (var4 < 0) {
         var4 = this.getFreeSlot();
      }

      if (var4 < 0) {
         return var3;
      } else {
         if (this.items[var4] == null) {
            this.items[var4] = new ItemInstance(var2, 0, var1.getAuxValue());
         }

         int var5 = var3;
         if (var3 > this.items[var4].getMaxStackSize() - this.items[var4].count) {
            var5 = this.items[var4].getMaxStackSize() - this.items[var4].count;
         }

         if (var5 > this.getMaxStackSize() - this.items[var4].count) {
            var5 = this.getMaxStackSize() - this.items[var4].count;
         }

         if (var5 == 0) {
            return var3;
         } else {
            var3 -= var5;
            ItemInstance var10000 = this.items[var4];
            var10000.count += var5;
            this.items[var4].popTime = 5;
            return var3;
         }
      }
   }

   public void tick() {
      for(int var1 = 0; var1 < this.items.length; ++var1) {
         if (this.items[var1] != null && this.items[var1].popTime > 0) {
            --this.items[var1].popTime;
         }
      }

   }

   public boolean removeResource(int var1) {
      int var2 = this.getSlot(var1);
      if (var2 < 0) {
         return false;
      } else {
         if (--this.items[var2].count <= 0) {
            this.items[var2] = null;
         }

         return true;
      }
   }

   public void swapSlots(int var1, int var2) {
      ItemInstance var3 = this.items[var2];
      this.items[var2] = this.items[var1];
      this.items[var1] = var3;
   }

   public boolean add(ItemInstance var1) {
      if (!var1.isDamaged()) {
         var1.count = this.addResource(var1);
         if (var1.count == 0) {
            return true;
         }
      }

      int var2 = this.getFreeSlot();
      if (var2 >= 0) {
         this.items[var2] = var1;
         this.items[var2].popTime = 5;
         return true;
      } else {
         return false;
      }
   }

   public ItemInstance removeItem(int var1, int var2) {
      ItemInstance[] var3 = this.items;
      if (var1 >= this.items.length) {
         var3 = this.armor;
         var1 -= this.items.length;
      }

      if (var3[var1] != null) {
         if (var3[var1].count <= var2) {
            ItemInstance var5 = var3[var1];
            var3[var1] = null;
            return var5;
         } else {
            ItemInstance var4 = var3[var1].remove(var2);
            if (var3[var1].count == 0) {
               var3[var1] = null;
            }

            return var4;
         }
      } else {
         return null;
      }
   }

   public void setItem(int var1, ItemInstance var2) {
      ItemInstance[] var3 = this.items;
      if (var1 >= var3.length) {
         var1 -= var3.length;
         var3 = this.armor;
      }

      var3[var1] = var2;
   }

   public float getDestroySpeed(Tile var1) {
      float var2 = 1.0F;
      if (this.items[this.selected] != null) {
         var2 *= this.items[this.selected].getDestroySpeed(var1);
      }

      return var2;
   }

   public ListTag<CompoundTag> save(ListTag<CompoundTag> var1) {
      for(int var2 = 0; var2 < this.items.length; ++var2) {
         if (this.items[var2] != null) {
            CompoundTag var3 = new CompoundTag();
            var3.putByte("Slot", (byte)var2);
            this.items[var2].save(var3);
            var1.add(var3);
         }
      }

      for(int var4 = 0; var4 < this.armor.length; ++var4) {
         if (this.armor[var4] != null) {
            CompoundTag var5 = new CompoundTag();
            var5.putByte("Slot", (byte)(var4 + 100));
            this.armor[var4].save(var5);
            var1.add(var5);
         }
      }

      return var1;
   }

   public void load(ListTag<CompoundTag> var1) {
      this.items = new ItemInstance[36];
      this.armor = new ItemInstance[4];

      for(int var2 = 0; var2 < var1.size(); ++var2) {
         CompoundTag var3 = (CompoundTag)var1.get(var2);
         int var4 = var3.getByte("Slot") & 255;
         ItemInstance var5 = new ItemInstance(var3);
         if (var5.getItem() != null) {
            if (var4 >= 0 && var4 < this.items.length) {
               this.items[var4] = var5;
            }

            if (var4 >= 100 && var4 < this.armor.length + 100) {
               this.armor[var4 - 100] = var5;
            }
         }
      }

   }

   public int getContainerSize() {
      return this.items.length + 4;
   }

   public ItemInstance getItem(int var1) {
      ItemInstance[] var2 = this.items;
      if (var1 >= var2.length) {
         var1 -= var2.length;
         var2 = this.armor;
      }

      return var2[var1];
   }

   public String getName() {
      return "Inventory";
   }

   public int getMaxStackSize() {
      return 64;
   }

   public int getAttackDamage(Entity var1) {
      ItemInstance var2 = this.getItem(this.selected);
      return var2 != null ? var2.getAttackDamage(var1) : 1;
   }

   public boolean canDestroy(Tile var1) {
      if (var1.material != Material.stone && var1.material != Material.metal && var1.material != Material.snow && var1.material != Material.topSnow) {
         return true;
      } else {
         ItemInstance var2 = this.getItem(this.selected);
         return var2 != null ? var2.canDestroySpecial(var1) : false;
      }
   }

   public ItemInstance getArmor(int var1) {
      return this.armor[var1];
   }

   public int getArmorValue() {
      int var1 = 0;
      int var2 = 0;
      int var3 = 0;

      for(int var4 = 0; var4 < this.armor.length; ++var4) {
         if (this.armor[var4] != null && this.armor[var4].getItem() instanceof ArmorItem) {
            int var5 = this.armor[var4].getMaxDamage();
            int var6 = this.armor[var4].getDamageValue();
            int var7 = var5 - var6;
            var2 += var7;
            var3 += var5;
            int var8 = ((ArmorItem)this.armor[var4].getItem()).defense;
            var1 += var8;
         }
      }

      if (var3 == 0) {
         return 0;
      } else {
         return (var1 - 1) * var2 / var3 + 1;
      }
   }

   public void hurtArmor(int var1) {
      for(int var2 = 0; var2 < this.armor.length; ++var2) {
         if (this.armor[var2] != null && this.armor[var2].getItem() instanceof ArmorItem) {
            this.armor[var2].hurt(var1);
            if (this.armor[var2].count == 0) {
               this.armor[var2].snap(this.player);
               this.armor[var2] = null;
            }
         }
      }

   }

   public void dropAll() {
      for(int var1 = 0; var1 < this.items.length; ++var1) {
         if (this.items[var1] != null) {
            this.player.drop(this.items[var1], true);
            this.items[var1] = null;
         }
      }

      for(int var2 = 0; var2 < this.armor.length; ++var2) {
         if (this.armor[var2] != null) {
            this.player.drop(this.armor[var2], true);
            this.armor[var2] = null;
         }
      }

   }

   public void setChanged() {
      this.changed = true;
   }

   public boolean isSame(Inventory var1) {
      for(int var2 = 0; var2 < this.items.length; ++var2) {
         if (!this.isSame(var1.items[var2], this.items[var2])) {
            return false;
         }
      }

      for(int var3 = 0; var3 < this.armor.length; ++var3) {
         if (!this.isSame(var1.armor[var3], this.armor[var3])) {
            return false;
         }
      }

      return true;
   }

   private boolean isSame(ItemInstance var1, ItemInstance var2) {
      if (var1 == null && var2 == null) {
         return true;
      } else if (var1 != null && var2 != null) {
         return var1.id == var2.id && var1.count == var2.count && var1.getAuxValue() == var2.getAuxValue();
      } else {
         return false;
      }
   }

   public Inventory copy() {
      Inventory var1 = new Inventory((Player)null);

      for(int var2 = 0; var2 < this.items.length; ++var2) {
         var1.items[var2] = this.items[var2] != null ? this.items[var2].copy() : null;
      }

      for(int var3 = 0; var3 < this.armor.length; ++var3) {
         var1.armor[var3] = this.armor[var3] != null ? this.armor[var3].copy() : null;
      }

      return var1;
   }

   public void setCarried(ItemInstance var1) {
      this.carried = var1;
      this.player.carriedChanged(var1);
   }

   public ItemInstance getCarried() {
      return this.carried;
   }

   public boolean stillValid(Player var1) {
      if (this.player.removed) {
         return false;
      } else {
         return !(var1.distanceToSqr(this.player) > (double)64.0F);
      }
   }
}
