package net.minecraft.world.level.tile;

import java.util.List;
import java.util.Random;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

public class PressurePlateTile extends Tile {
   private Sensitivity sensitivity;

   protected PressurePlateTile(int var1, int var2, Sensitivity var3) {
      super(var1, var2, Material.stone);
      this.sensitivity = var3;
      this.setTicking(true);
      float var4 = 0.0625F;
      this.setShape(var4, 0.0F, var4, 1.0F - var4, 0.03125F, 1.0F - var4);
   }

   public int getTickDelay() {
      return 20;
   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      return null;
   }

   public boolean isSolidRender() {
      return false;
   }

   public boolean blocksLight() {
      return false;
   }

   public boolean isCubeShaped() {
      return false;
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      return var1.isSolidTile(var2, var3 - 1, var4);
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      boolean var6 = false;
      if (!var1.isSolidTile(var2, var3 - 1, var4)) {
         var6 = true;
      }

      if (var6) {
         this.spawnResources(var1, var2, var3, var4, var1.getData(var2, var3, var4));
         var1.setTile(var2, var3, var4, 0);
      }

   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
      if (!var1.isOnline) {
         if (var1.getData(var2, var3, var4) != 0) {
            this.checkPressed(var1, var2, var3, var4);
         }
      }
   }

   public void entityInside(Level var1, int var2, int var3, int var4, Entity var5) {
      if (!var1.isOnline) {
         if (var1.getData(var2, var3, var4) != 1) {
            this.checkPressed(var1, var2, var3, var4);
         }
      }
   }

   private void checkPressed(Level var1, int var2, int var3, int var4) {
      boolean var5 = var1.getData(var2, var3, var4) == 1;
      boolean var6 = false;
      float var7 = 0.125F;
      List var8 = null;
      if (this.sensitivity == PressurePlateTile.Sensitivity.everything) {
         var8 = var1.getEntities((Entity)null, AABB.newTemp((double)((float)var2 + var7), (double)var3, (double)((float)var4 + var7), (double)((float)(var2 + 1) - var7), (double)var3 + (double)0.25F, (double)((float)(var4 + 1) - var7)));
      }

      if (this.sensitivity == PressurePlateTile.Sensitivity.mobs) {
         var8 = var1.getEntitiesOfClass(Mob.class, AABB.newTemp((double)((float)var2 + var7), (double)var3, (double)((float)var4 + var7), (double)((float)(var2 + 1) - var7), (double)var3 + (double)0.25F, (double)((float)(var4 + 1) - var7)));
      }

      if (this.sensitivity == PressurePlateTile.Sensitivity.players) {
         var8 = var1.getEntitiesOfClass(Player.class, AABB.newTemp((double)((float)var2 + var7), (double)var3, (double)((float)var4 + var7), (double)((float)(var2 + 1) - var7), (double)var3 + (double)0.25F, (double)((float)(var4 + 1) - var7)));
      }

      if (var8.size() > 0) {
         var6 = true;
      }

      if (var6 && !var5) {
         var1.setData(var2, var3, var4, 1);
         var1.updateNeighborsAt(var2, var3, var4, this.id);
         var1.updateNeighborsAt(var2, var3 - 1, var4, this.id);
         var1.setTilesDirty(var2, var3, var4, var2, var3, var4);
         var1.playSound((double)var2 + (double)0.5F, (double)var3 + 0.1, (double)var4 + (double)0.5F, "random.click", 0.3F, 0.6F);
      }

      if (!var6 && var5) {
         var1.setData(var2, var3, var4, 0);
         var1.updateNeighborsAt(var2, var3, var4, this.id);
         var1.updateNeighborsAt(var2, var3 - 1, var4, this.id);
         var1.setTilesDirty(var2, var3, var4, var2, var3, var4);
         var1.playSound((double)var2 + (double)0.5F, (double)var3 + 0.1, (double)var4 + (double)0.5F, "random.click", 0.3F, 0.5F);
      }

      if (var6) {
         var1.addToTickNextTick(var2, var3, var4, this.id);
      }

   }

   public void onRemove(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getData(var2, var3, var4);
      if (var5 > 0) {
         var1.updateNeighborsAt(var2, var3, var4, this.id);
         var1.updateNeighborsAt(var2, var3 - 1, var4, this.id);
      }

      super.onRemove(var1, var2, var3, var4);
   }

   public void updateShape(LevelSource var1, int var2, int var3, int var4) {
      boolean var5 = var1.getData(var2, var3, var4) == 1;
      float var6 = 0.0625F;
      if (var5) {
         this.setShape(var6, 0.0F, var6, 1.0F - var6, 0.03125F, 1.0F - var6);
      } else {
         this.setShape(var6, 0.0F, var6, 1.0F - var6, 0.0625F, 1.0F - var6);
      }

   }

   public boolean getSignal(LevelSource var1, int var2, int var3, int var4, int var5) {
      return var1.getData(var2, var3, var4) > 0;
   }

   public boolean getDirectSignal(Level var1, int var2, int var3, int var4, int var5) {
      if (var1.getData(var2, var3, var4) == 0) {
         return false;
      } else {
         return var5 == 1;
      }
   }

   public boolean isSignalSource() {
      return true;
   }

   public void updateDefaultShape() {
      float var1 = 0.5F;
      float var2 = 0.125F;
      float var3 = 0.5F;
      this.setShape(0.5F - var1, 0.5F - var2, 0.5F - var3, 0.5F + var1, 0.5F + var2, 0.5F + var3);
   }

   public static enum Sensitivity {
      everything,
      mobs,
      players;

      private Sensitivity() {
      }
   }
}
