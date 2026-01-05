package net.minecraft.world.inventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;

public abstract class AbstractContainerMenu {
   public List<ItemInstance> lastSlots = new ArrayList();
   public List<Slot> slots = new ArrayList();
   public int containerId = 0;
   private short changeUid = 0;
   protected List<ContainerListener> containerListeners = new ArrayList();
   private Set<Player> unSynchedPlayers = new HashSet();

   public AbstractContainerMenu() {
   }

   protected void addSlot(Slot var1) {
      var1.index = this.slots.size();
      this.slots.add(var1);
      this.lastSlots.add(null);
   }

   public void addSlotListener(ContainerListener var1) {
      this.containerListeners.add(var1);
      ArrayList var2 = new ArrayList();

      for(int var3 = 0; var3 < this.slots.size(); ++var3) {
         var2.add(((Slot)this.slots.get(var3)).getItem());
      }

      var1.refreshContainer(this, var2);
      this.broadcastChanges();
   }

   public void sendData(int var1, int var2) {
      for(int var3 = 0; var3 < this.containerListeners.size(); ++var3) {
         ((ContainerListener)this.containerListeners.get(var3)).setContainerData(this, var1, var2);
      }

   }

   public void broadcastChanges() {
      for(int var1 = 0; var1 < this.slots.size(); ++var1) {
         ItemInstance var2 = ((Slot)this.slots.get(var1)).getItem();
         ItemInstance var3 = (ItemInstance)this.lastSlots.get(var1);
         if (!ItemInstance.matches(var3, var2)) {
            var3 = var2 == null ? null : var2.copy();
            this.lastSlots.set(var1, var3);

            for(int var4 = 0; var4 < this.containerListeners.size(); ++var4) {
               ((ContainerListener)this.containerListeners.get(var4)).slotChanged(this, var1, var3);
            }
         }
      }

   }

   public Slot getSlotFor(Container var1, int var2) {
      for(int var3 = 0; var3 < this.slots.size(); ++var3) {
         Slot var4 = (Slot)this.slots.get(var3);
         if (var4.isAt(var1, var2)) {
            return var4;
         }
      }

      return null;
   }

   public Slot getSlot(int var1) {
      return (Slot)this.slots.get(var1);
   }

   public ItemInstance clicked(int var1, int var2, Player var3) {
      ItemInstance var4 = null;
      if (var2 == 0 || var2 == 1) {
         Inventory var5 = var3.inventory;
         if (var1 == -999) {
            if (var5.getCarried() != null && var1 == -999) {
               if (var2 == 0) {
                  var3.drop(var5.getCarried());
                  var5.setCarried((ItemInstance)null);
               }

               if (var2 == 1) {
                  var3.drop(var5.getCarried().remove(1));
                  if (var5.getCarried().count == 0) {
                     var5.setCarried((ItemInstance)null);
                  }
               }
            }
         } else {
            Slot var6 = (Slot)this.slots.get(var1);
            if (var6 != null) {
               var6.setChanged();
               ItemInstance var7 = var6.getItem();
               if (var7 != null) {
                  var4 = var7.copy();
               }

               if (var7 != null || var5.getCarried() != null) {
                  if (var7 != null && var5.getCarried() == null) {
                     int var13 = var2 == 0 ? var7.count : (var7.count + 1) / 2;
                     var5.setCarried(var6.remove(var13));
                     if (var7.count == 0) {
                        var6.set((ItemInstance)null);
                     }

                     var6.onTake();
                  } else if (var7 == null && var5.getCarried() != null && var6.mayPlace(var5.getCarried())) {
                     int var12 = var2 == 0 ? var5.getCarried().count : 1;
                     if (var12 > var6.getMaxStackSize()) {
                        var12 = var6.getMaxStackSize();
                     }

                     var6.set(var5.getCarried().remove(var12));
                     if (var5.getCarried().count == 0) {
                        var5.setCarried((ItemInstance)null);
                     }
                  } else if (var7 != null && var5.getCarried() != null) {
                     if (var6.mayPlace(var5.getCarried())) {
                        if (var7.id != var5.getCarried().id || var7.isStackedByData() && var7.getAuxValue() != var5.getCarried().getAuxValue()) {
                           if (var5.getCarried().count <= var6.getMaxStackSize()) {
                              var6.set(var5.getCarried());
                              var5.setCarried(var7);
                           }
                        } else if (var7.id == var5.getCarried().id) {
                           if (var2 == 0) {
                              int var8 = var5.getCarried().count;
                              if (var8 > var6.getMaxStackSize() - var7.count) {
                                 var8 = var6.getMaxStackSize() - var7.count;
                              }

                              if (var8 > var5.getCarried().getMaxStackSize() - var7.count) {
                                 var8 = var5.getCarried().getMaxStackSize() - var7.count;
                              }

                              var5.getCarried().remove(var8);
                              if (var5.getCarried().count == 0) {
                                 var5.setCarried((ItemInstance)null);
                              }

                              var7.count += var8;
                           } else if (var2 == 1) {
                              int var10 = 1;
                              if (var10 > var6.getMaxStackSize() - var7.count) {
                                 var10 = var6.getMaxStackSize() - var7.count;
                              }

                              if (var10 > var5.getCarried().getMaxStackSize() - var7.count) {
                                 var10 = var5.getCarried().getMaxStackSize() - var7.count;
                              }

                              var5.getCarried().remove(var10);
                              if (var5.getCarried().count == 0) {
                                 var5.setCarried((ItemInstance)null);
                              }

                              var7.count += var10;
                           }
                        }
                     } else {
                        ItemInstance var11 = var5.getCarried();
                        if (var7.id == var11.id && var11.getMaxStackSize() > 1 && (!var7.isStackedByData() || var7.getAuxValue() == var11.getAuxValue())) {
                           int var9 = var7.count;
                           if (var9 > 0 && var9 + var11.count <= var11.getMaxStackSize()) {
                              var11.count += var9;
                              var7.remove(var9);
                              if (var7.count == 0) {
                                 var6.set((ItemInstance)null);
                              }

                              var6.onTake();
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return var4;
   }

   public void removed(Player var1) {
      Inventory var2 = var1.inventory;
      if (var2.getCarried() != null) {
         var1.drop(var2.getCarried());
         var2.setCarried((ItemInstance)null);
      }

   }

   public void slotsChanged(Container var1) {
      this.broadcastChanges();
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void setItem(int var1, ItemInstance var2) {
      this.getSlot(var1).set(var2);
   }

   public void setAll(ItemInstance[] var1) {
      for(int var2 = 0; var2 < var1.length; ++var2) {
         this.getSlot(var2).set(var1[var2]);
      }

   }

   public void setData(int var1, int var2) {
   }

   public short backup(Inventory var1) {
      ++this.changeUid;
      return this.changeUid;
   }

   public void deleteBackup(short var1) {
   }

   public void rollbackToBackup(short var1) {
   }

   public boolean isSynched(Player var1) {
      return !this.unSynchedPlayers.contains(var1);
   }

   public void setSynched(Player var1, boolean var2) {
      if (var2) {
         this.unSynchedPlayers.remove(var1);
      } else {
         this.unSynchedPlayers.add(var1);
      }

   }

   public abstract boolean stillValid(Player var1);
}
