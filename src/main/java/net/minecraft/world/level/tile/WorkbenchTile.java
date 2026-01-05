package net.minecraft.world.level.tile;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;

public class WorkbenchTile extends Tile {
   protected WorkbenchTile(int var1) {
      super(var1, Material.wood);
      this.tex = 59;
   }

   public int getTexture(int var1) {
      if (var1 == 1) {
         return this.tex - 16;
      } else if (var1 == 0) {
         return Tile.wood.getTexture(0);
      } else {
         return var1 != 2 && var1 != 4 ? this.tex : this.tex + 1;
      }
   }

   public boolean use(Level var1, int var2, int var3, int var4, Player var5) {
      if (var1.isOnline) {
         return true;
      } else {
         var5.startCrafting(var2, var3, var4);
         return true;
      }
   }
}
