package net.minecraft.world.level.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;

public class NotGateTile extends TorchTile {
   private static final int RECENT_TOGGLE_TIMER = 100;
   private static final int MAX_RECENT_TOGGLES = 8;
   private boolean on = false;
   private static List<Toggle> recentToggles = new ArrayList();

   public int getTexture(int var1, int var2) {
      return var1 == 1 ? Tile.redStoneDust.getTexture(var1, var2) : super.getTexture(var1, var2);
   }

   private boolean isToggledTooFrequently(Level var1, int var2, int var3, int var4, boolean var5) {
      if (var5) {
         recentToggles.add(new Toggle(var2, var3, var4, var1.time));
      }

      int var6 = 0;

      for(int var7 = 0; var7 < recentToggles.size(); ++var7) {
         Toggle var8 = (Toggle)recentToggles.get(var7);
         if (var8.x == var2 && var8.y == var3 && var8.z == var4) {
            ++var6;
            if (var6 >= 8) {
               return true;
            }
         }
      }

      return false;
   }

   protected NotGateTile(int var1, int var2, boolean var3) {
      super(var1, var2);
      this.on = var3;
      this.setTicking(true);
   }

   public int getTickDelay() {
      return 2;
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
      if (var1.getData(var2, var3, var4) == 0) {
         super.onPlace(var1, var2, var3, var4);
      }

      if (this.on) {
         var1.updateNeighborsAt(var2, var3 - 1, var4, this.id);
         var1.updateNeighborsAt(var2, var3 + 1, var4, this.id);
         var1.updateNeighborsAt(var2 - 1, var3, var4, this.id);
         var1.updateNeighborsAt(var2 + 1, var3, var4, this.id);
         var1.updateNeighborsAt(var2, var3, var4 - 1, this.id);
         var1.updateNeighborsAt(var2, var3, var4 + 1, this.id);
      }

   }

   public void onRemove(Level var1, int var2, int var3, int var4) {
      if (this.on) {
         var1.updateNeighborsAt(var2, var3 - 1, var4, this.id);
         var1.updateNeighborsAt(var2, var3 + 1, var4, this.id);
         var1.updateNeighborsAt(var2 - 1, var3, var4, this.id);
         var1.updateNeighborsAt(var2 + 1, var3, var4, this.id);
         var1.updateNeighborsAt(var2, var3, var4 - 1, this.id);
         var1.updateNeighborsAt(var2, var3, var4 + 1, this.id);
      }

   }

   public boolean getSignal(LevelSource var1, int var2, int var3, int var4, int var5) {
      if (!this.on) {
         return false;
      } else {
         int var6 = var1.getData(var2, var3, var4);
         if (var6 == 5 && var5 == 1) {
            return false;
         } else if (var6 == 3 && var5 == 3) {
            return false;
         } else if (var6 == 4 && var5 == 2) {
            return false;
         } else if (var6 == 1 && var5 == 5) {
            return false;
         } else {
            return var6 != 2 || var5 != 4;
         }
      }
   }

   private boolean hasNeighborSignal(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getData(var2, var3, var4);
      if (var5 == 5 && var1.getSignal(var2, var3 - 1, var4, 0)) {
         return true;
      } else if (var5 == 3 && var1.getSignal(var2, var3, var4 - 1, 2)) {
         return true;
      } else if (var5 == 4 && var1.getSignal(var2, var3, var4 + 1, 3)) {
         return true;
      } else if (var5 == 1 && var1.getSignal(var2 - 1, var3, var4, 4)) {
         return true;
      } else {
         return var5 == 2 && var1.getSignal(var2 + 1, var3, var4, 5);
      }
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      boolean var6 = this.hasNeighborSignal(var1, var2, var3, var4);

      while(recentToggles.size() > 0 && var1.time - ((Toggle)recentToggles.get(0)).when > 100L) {
         recentToggles.remove(0);
      }

      if (this.on) {
         if (var6) {
            var1.setTileAndData(var2, var3, var4, Tile.notGate_off.id, var1.getData(var2, var3, var4));
            if (this.isToggledTooFrequently(var1, var2, var3, var4, true)) {
               var1.playSound((double)((float)var2 + 0.5F), (double)((float)var3 + 0.5F), (double)((float)var4 + 0.5F), "random.fizz", 0.5F, 2.6F + (var1.random.nextFloat() - var1.random.nextFloat()) * 0.8F);

               for(int var7 = 0; var7 < 5; ++var7) {
                  double var8 = (double)var2 + var5.nextDouble() * 0.6 + 0.2;
                  double var10 = (double)var3 + var5.nextDouble() * 0.6 + 0.2;
                  double var12 = (double)var4 + var5.nextDouble() * 0.6 + 0.2;
                  var1.addParticle("smoke", var8, var10, var12, (double)0.0F, (double)0.0F, (double)0.0F);
               }
            }
         }
      } else if (!var6 && !this.isToggledTooFrequently(var1, var2, var3, var4, false)) {
         var1.setTileAndData(var2, var3, var4, Tile.notGate_on.id, var1.getData(var2, var3, var4));
      }

   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      super.neighborChanged(var1, var2, var3, var4, var5);
      var1.addToTickNextTick(var2, var3, var4, this.id);
   }

   public boolean getDirectSignal(Level var1, int var2, int var3, int var4, int var5) {
      return var5 == 0 ? this.getSignal(var1, var2, var3, var4, var5) : false;
   }

   public int getResource(int var1, Random var2) {
      return Tile.notGate_on.id;
   }

   public boolean isSignalSource() {
      return true;
   }

   public void animateTick(Level var1, int var2, int var3, int var4, Random var5) {
      if (this.on) {
         int var6 = var1.getData(var2, var3, var4);
         double var7 = (double)((float)var2 + 0.5F) + (double)(var5.nextFloat() - 0.5F) * 0.2;
         double var9 = (double)((float)var3 + 0.7F) + (double)(var5.nextFloat() - 0.5F) * 0.2;
         double var11 = (double)((float)var4 + 0.5F) + (double)(var5.nextFloat() - 0.5F) * 0.2;
         double var13 = (double)0.22F;
         double var15 = (double)0.27F;
         if (var6 == 1) {
            var1.addParticle("reddust", var7 - var15, var9 + var13, var11, (double)0.0F, (double)0.0F, (double)0.0F);
         } else if (var6 == 2) {
            var1.addParticle("reddust", var7 + var15, var9 + var13, var11, (double)0.0F, (double)0.0F, (double)0.0F);
         } else if (var6 == 3) {
            var1.addParticle("reddust", var7, var9 + var13, var11 - var15, (double)0.0F, (double)0.0F, (double)0.0F);
         } else if (var6 == 4) {
            var1.addParticle("reddust", var7, var9 + var13, var11 + var15, (double)0.0F, (double)0.0F, (double)0.0F);
         } else {
            var1.addParticle("reddust", var7, var9, var11, (double)0.0F, (double)0.0F, (double)0.0F);
         }

      }
   }

   private static class Toggle {
      int x;
      int y;
      int z;
      long when;

      public Toggle(int var1, int var2, int var3, long var4) {
         this.x = var1;
         this.y = var2;
         this.z = var3;
         this.when = var4;
      }
   }
}
