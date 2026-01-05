package net.minecraft.world.level.tile.entity;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;

public class ChestTileEntity extends TileEntity implements Container {
   private ItemInstance[] items = new ItemInstance[36];

   public ChestTileEntity() {
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
            this.setChanged();
            return var4;
         } else {
            ItemInstance var3 = this.items[var1].remove(var2);
            if (this.items[var1].count == 0) {
               this.items[var1] = null;
            }

            this.setChanged();
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

      this.setChanged();
   }

   public String getName() {
      return "Chest";
   }

   public void load(CompoundTag var1) {
      super.load(var1);
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

   public void save(CompoundTag var1) {
      super.save(var1);
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

   public void load(ListTag<CompoundTag> var1) {
   }

   public int getMaxStackSize() {
      return 64;
   }

   public boolean stillValid(Player var1) {
      if (this.level.getTileEntity(this.x, this.y, this.z) != this) {
         return false;
      } else {
         return !(var1.distanceToSqr((double)this.x + (double)0.5F, (double)this.y + (double)0.5F, (double)this.z + (double)0.5F) > (double)64.0F);
      }
   }
}
