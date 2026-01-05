package net.minecraft.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.tile.Tile;

public class User {
   public static List<Tile> allowedTiles = new ArrayList();
   public String name;
   public String sessionId;
   public String mpPassword;

   static {
      allowedTiles.add(Tile.rock);
      allowedTiles.add(Tile.stoneBrick);
      allowedTiles.add(Tile.redBrick);
      allowedTiles.add(Tile.dirt);
      allowedTiles.add(Tile.wood);
      allowedTiles.add(Tile.treeTrunk);
      allowedTiles.add(Tile.leaves);
      allowedTiles.add(Tile.torch);
      allowedTiles.add(Tile.stoneSlabHalf);
      allowedTiles.add(Tile.glass);
      allowedTiles.add(Tile.mossStone);
      allowedTiles.add(Tile.sapling);
      allowedTiles.add(Tile.flower);
      allowedTiles.add(Tile.rose);
      allowedTiles.add(Tile.mushroom1);
      allowedTiles.add(Tile.mushroom2);
      allowedTiles.add(Tile.sand);
      allowedTiles.add(Tile.gravel);
      allowedTiles.add(Tile.sponge);
      allowedTiles.add(Tile.cloth);
      allowedTiles.add(Tile.coalOre);
      allowedTiles.add(Tile.ironOre);
      allowedTiles.add(Tile.goldOre);
      allowedTiles.add(Tile.ironBlock);
      allowedTiles.add(Tile.goldBlock);
      allowedTiles.add(Tile.bookshelf);
      allowedTiles.add(Tile.tnt);
      allowedTiles.add(Tile.obsidian);
      System.out.println(allowedTiles.size());
   }

   public User(String name, String sessionId) {
      this.name = name;
      this.sessionId = sessionId;
   }
}
