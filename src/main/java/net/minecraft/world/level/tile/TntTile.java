package net.minecraft.world.level.tile;

import java.util.Random;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;

public class TntTile extends Tile {
   public TntTile(int var1, int var2) {
      super(var1, var2, Material.explosive);
   }

   public int getTexture(int var1) {
      if (var1 == 0) {
         return this.tex + 2;
      } else {
         return var1 == 1 ? this.tex + 1 : this.tex;
      }
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
      if (var5 > 0 && Tile.tiles[var5].isSignalSource() && var1.hasNeighborSignal(var2, var3, var4)) {
         this.destroy(var1, var2, var3, var4, 0);
         var1.setTile(var2, var3, var4, 0);
      }

   }

   public int getResourceCount(Random var1) {
      return 0;
   }

   public void wasExploded(Level var1, int var2, int var3, int var4) {
      PrimedTnt var5 = new PrimedTnt(var1, (double)((float)var2 + 0.5F), (double)((float)var3 + 0.5F), (double)((float)var4 + 0.5F));
      var5.life = var1.random.nextInt(var5.life / 4) + var5.life / 8;
      var1.addEntity(var5);
   }

   public void destroy(Level var1, int var2, int var3, int var4, int var5) {
      if (!var1.isOnline) {
         PrimedTnt var6 = new PrimedTnt(var1, (double)((float)var2 + 0.5F), (double)((float)var3 + 0.5F), (double)((float)var4 + 0.5F));
         var1.addEntity(var6);
         var1.playSound(var6, "random.fuse", 1.0F, 1.0F);
      }
   }
}
