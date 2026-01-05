package net.minecraft.world.level.tile.entity;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.crafting.FurnaceRecipes;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.FurnaceTile;
import net.minecraft.world.level.tile.Tile;

public class FurnaceTileEntity extends TileEntity implements Container {
   private static final int BURN_INTERVAL = 200;
   private ItemInstance[] items = new ItemInstance[3];
   public int litTime = 0;
   public int litDuration = 0;
   public int tickCount = 0;

   public FurnaceTileEntity() {
   }

   public int getContainerSize() {
      return this.items.length;
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
      return "Furnace";
   }

   public void load(CompoundTag var1) {
      super.load(var1);
      ListTag var2 = var1.getList("Items");
      this.items = new ItemInstance[this.getContainerSize()];

      for(int var3 = 0; var3 < var2.size(); ++var3) {
         CompoundTag var4 = (CompoundTag)var2.get(var3);
         byte var5 = var4.getByte("Slot");
         if (var5 >= 0 && var5 < this.items.length) {
            this.items[var5] = new ItemInstance(var4);
         }
      }

      this.litTime = var1.getShort("BurnTime");
      this.tickCount = var1.getShort("CookTime");
      this.litDuration = this.getBurnDuration(this.items[1]);
   }

   public void save(CompoundTag var1) {
      super.save(var1);
      var1.putShort("BurnTime", (short)this.litTime);
      var1.putShort("CookTime", (short)this.tickCount);
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

   public int getMaxStackSize() {
      return 64;
   }

   public int getBurnProgress(int var1) {
      return this.tickCount * var1 / 200;
   }

   public int getLitProgress(int var1) {
      if (this.litDuration == 0) {
         this.litDuration = 200;
      }

      return this.litTime * var1 / this.litDuration;
   }

   public boolean isLit() {
      return this.litTime > 0;
   }

   public void tick() {
      boolean var1 = this.litTime > 0;
      boolean var2 = false;
      if (this.litTime > 0) {
         --this.litTime;
      }

      if (!this.level.isOnline) {
         if (this.litTime == 0 && this.canBurn()) {
            this.litDuration = this.litTime = this.getBurnDuration(this.items[1]);
            if (this.litTime > 0) {
               var2 = true;
               if (this.items[1] != null) {
                  --this.items[1].count;
                  if (this.items[1].count == 0) {
                     this.items[1] = null;
                  }
               }
            }
         }

         if (this.isLit() && this.canBurn()) {
            ++this.tickCount;
            if (this.tickCount == 200) {
               this.tickCount = 0;
               this.burn();
               var2 = true;
            }
         } else {
            this.tickCount = 0;
         }

         if (var1 != this.litTime > 0) {
            var2 = true;
            FurnaceTile.setLit(this.litTime > 0, this.level, this.x, this.y, this.z);
         }
      }

      if (var2) {
         this.setChanged();
      }

   }

   private boolean canBurn() {
      if (this.items[0] == null) {
         return false;
      } else {
         ItemInstance var1 = FurnaceRecipes.getInstance().getResult(this.items[0].getItem().id);
         if (var1 == null) {
            return false;
         } else if (this.items[2] == null) {
            return true;
         } else if (!this.items[2].sameItem(var1)) {
            return false;
         } else if (this.items[2].count < this.getMaxStackSize() && this.items[2].count < this.items[2].getMaxStackSize()) {
            return true;
         } else {
            return this.items[2].count < var1.getMaxStackSize();
         }
      }
   }

   public void burn() {
      if (this.canBurn()) {
         ItemInstance var1 = FurnaceRecipes.getInstance().getResult(this.items[0].getItem().id);
         if (this.items[2] == null) {
            this.items[2] = var1.copy();
         } else if (this.items[2].id == var1.id) {
            ++this.items[2].count;
         }

         --this.items[0].count;
         if (this.items[0].count <= 0) {
            this.items[0] = null;
         }

      }
   }

   private int getBurnDuration(ItemInstance var1) {
      if (var1 == null) {
         return 0;
      } else {
         int var2 = var1.getItem().id;
         if (var2 < 256 && Tile.tiles[var2].material == Material.wood) {
            return 300;
         } else if (var2 == Item.stick.id) {
            return 100;
         } else if (var2 == Item.coal.id) {
            return 1600;
         } else {
            return var2 == Item.bucket_lava.id ? 20000 : 0;
         }
      }
   }

   public boolean stillValid(Player var1) {
      if (this.level.getTileEntity(this.x, this.y, this.z) != this) {
         return false;
      } else {
         return !(var1.distanceToSqr((double)this.x + (double)0.5F, (double)this.y + (double)0.5F, (double)this.z + (double)0.5F) > (double)64.0F);
      }
   }
}
