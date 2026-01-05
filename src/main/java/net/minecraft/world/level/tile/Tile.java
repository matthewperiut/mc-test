package net.minecraft.world.level.tile;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.locale.Descriptive;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ClothTileItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.TileItem;
import net.minecraft.world.item.TreeTileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.entity.SignTileEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Tile implements Descriptive<Tile> {
   private static final String TILE_DESCRIPTION_PREFIX = "tile.";
   public static final SoundType SOUND_NORMAL = new SoundType("stone", 1.0F, 1.0F);
   public static final SoundType SOUND_WOOD = new SoundType("wood", 1.0F, 1.0F);
   public static final SoundType SOUND_GRAVEL = new SoundType("gravel", 1.0F, 1.0F);
   public static final SoundType SOUND_GRASS = new SoundType("grass", 1.0F, 1.0F);
   public static final SoundType SOUND_STONE = new SoundType("stone", 1.0F, 1.0F);
   public static final SoundType SOUND_METAL = new SoundType("stone", 1.0F, 1.5F);
   public static final SoundType SOUND_GLASS = new SoundType("stone", 1.0F, 1.0F) {
      public String getBreakSound() {
         return "random.glass";
      }
   };
   public static final SoundType SOUND_CLOTH = new SoundType("cloth", 1.0F, 1.0F);
   public static final SoundType SOUND_SAND = new SoundType("sand", 1.0F, 1.0F) {
      public String getBreakSound() {
         return "step.gravel";
      }
   };
   public static final int SHAPE_INVISIBLE = -1;
   public static final int SHAPE_BLOCK = 0;
   public static final int SHAPE_CROSS_TEXTURE = 1;
   public static final int SHAPE_TORCH = 2;
   public static final int SHAPE_FIRE = 3;
   public static final int SHAPE_WATER = 4;
   public static final int SHAPE_RED_DUST = 5;
   public static final int SHAPE_ROWS = 6;
   public static final int SHAPE_DOOR = 7;
   public static final int SHAPE_LADDER = 8;
   public static final int SHAPE_RAIL = 9;
   public static final int SHAPE_STAIRS = 10;
   public static final int SHAPE_FENCE = 11;
   public static final int SHAPE_LEVER = 12;
   public static final int SHAPE_CACTUS = 13;
   public static final Tile[] tiles = new Tile[256];
   public static final boolean[] shouldTick = new boolean[256];
   public static final boolean[] solid = new boolean[256];
   public static final boolean[] isEntityTile = new boolean[256];
   public static final int[] lightBlock = new int[256];
   public static final boolean[] transculent = new boolean[256];
   public static final int[] lightEmission = new int[256];
   public static final Tile rock;
   public static final GrassTile grass;
   public static final Tile dirt;
   public static final Tile stoneBrick;
   public static final Tile wood;
   public static final Tile sapling;
   public static final Tile unbreakable;
   public static final Tile water;
   public static final Tile calmWater;
   public static final Tile lava;
   public static final Tile calmLava;
   public static final Tile sand;
   public static final Tile gravel;
   public static final Tile goldOre;
   public static final Tile ironOre;
   public static final Tile coalOre;
   public static final Tile treeTrunk;
   public static final LeafTile leaves;
   public static final Tile sponge;
   public static final Tile glass;
   public static final Tile lapisOre;
   public static final Tile lapisBlock;
   public static final Tile dispenser;
   public static final Tile sandStone;
   public static final Tile musicBlock;
   public static final Tile unused_26;
   public static final Tile unused_27;
   public static final Tile unused_28;
   public static final Tile unused_29;
   public static final Tile unused_30;
   public static final Tile unused_31;
   public static final Tile unused_32;
   public static final Tile unused_33;
   public static final Tile unused_34;
   public static final Tile cloth;
   public static final Tile unused_36;
   public static final Bush flower;
   public static final Bush rose;
   public static final Bush mushroom1;
   public static final Bush mushroom2;
   public static final Tile goldBlock;
   public static final Tile ironBlock;
   public static final Tile stoneSlab;
   public static final Tile stoneSlabHalf;
   public static final Tile redBrick;
   public static final Tile tnt;
   public static final Tile bookshelf;
   public static final Tile mossStone;
   public static final Tile obsidian;
   public static final Tile torch;
   public static final FireTile fire;
   public static final Tile mobSpawner;
   public static final Tile stairs_wood;
   public static final Tile chest;
   public static final Tile redStoneDust;
   public static final Tile emeraldOre;
   public static final Tile emeraldBlock;
   public static final Tile workBench;
   public static final Tile crops;
   public static final Tile farmland;
   public static final Tile furnace;
   public static final Tile furnace_lit;
   public static final Tile sign;
   public static final Tile door_wood;
   public static final Tile ladder;
   public static final Tile rail;
   public static final Tile stairs_stone;
   public static final Tile wallSign;
   public static final Tile lever;
   public static final Tile pressurePlate_stone;
   public static final Tile door_iron;
   public static final Tile pressurePlate_wood;
   public static final Tile redStoneOre;
   public static final Tile redStoneOre_lit;
   public static final Tile notGate_off;
   public static final Tile notGate_on;
   public static final Tile button;
   public static final Tile topSnow;
   public static final Tile ice;
   public static final Tile snow;
   public static final Tile cactus;
   public static final Tile clay;
   public static final Tile reeds;
   public static final Tile recordPlayer;
   public static final Tile fence;
   public static final Tile pumpkin;
   public static final Tile hellRock;
   public static final Tile hellSand;
   public static final Tile lightGem;
   public static final PortalTile portalTile;
   public static final Tile litPumpkin;
   public static final Tile cake;
   public int tex;
   public final int id;
   protected float destroySpeed;
   protected float explosionResistance;
   public double xx0;
   public double yy0;
   public double zz0;
   public double xx1;
   public double yy1;
   public double zz1;
   public SoundType soundType;
   public float gravity;
   public final Material material;
   public float friction;
   private String descriptionId;

   protected Tile(int var1, Material var2) {
      this.soundType = SOUND_NORMAL;
      this.gravity = 1.0F;
      this.friction = 0.6F;
      if (tiles[var1] != null) {
         throw new IllegalArgumentException("Slot " + var1 + " is already occupied by " + tiles[var1] + " when adding " + this);
      } else {
         this.material = var2;
         tiles[var1] = this;
         this.id = var1;
         this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         solid[var1] = this.isSolidRender();
         lightBlock[var1] = this.isSolidRender() ? 255 : 0;
         transculent[var1] = this.isTransculent();
         isEntityTile[var1] = false;
      }
   }

   protected Tile(int var1, int var2, Material var3) {
      this(var1, var3);
      this.tex = var2;
   }

   protected Tile setSoundType(SoundType var1) {
      this.soundType = var1;
      return this;
   }

   protected Tile setLightBlock(int var1) {
      lightBlock[this.id] = var1;
      return this;
   }

   protected Tile setLightEmission(float var1) {
      lightEmission[this.id] = (int)(15.0F * var1);
      return this;
   }

   protected Tile setExplodeable(float var1) {
      this.explosionResistance = var1 * 3.0F;
      return this;
   }

   private boolean isTransculent() {
      return false;
   }

   public boolean isCubeShaped() {
      return true;
   }

   public int getRenderShape() {
      return 0;
   }

   protected Tile setDestroyTime(float var1) {
      this.destroySpeed = var1;
      if (this.explosionResistance < var1 * 5.0F) {
         this.explosionResistance = var1 * 5.0F;
      }

      return this;
   }

   protected void setTicking(boolean var1) {
      shouldTick[this.id] = var1;
   }

   public void setShape(float var1, float var2, float var3, float var4, float var5, float var6) {
      this.xx0 = (double)var1;
      this.yy0 = (double)var2;
      this.zz0 = (double)var3;
      this.xx1 = (double)var4;
      this.yy1 = (double)var5;
      this.zz1 = (double)var6;
   }

   public float getBrightness(LevelSource var1, int var2, int var3, int var4) {
      return var1.getBrightness(var2, var3, var4);
   }

   public static boolean isFaceVisible(Level var0, int var1, int var2, int var3, int var4) {
      if (var4 == 0) {
         --var2;
      }

      if (var4 == 1) {
         ++var2;
      }

      if (var4 == 2) {
         --var3;
      }

      if (var4 == 3) {
         ++var3;
      }

      if (var4 == 4) {
         --var1;
      }

      if (var4 == 5) {
         ++var1;
      }

      return !var0.isSolidTile(var1, var2, var3);
   }

   public boolean shouldRenderFace(LevelSource var1, int var2, int var3, int var4, int var5) {
      if (var5 == 0 && this.yy0 > (double)0.0F) {
         return true;
      } else if (var5 == 1 && this.yy1 < (double)1.0F) {
         return true;
      } else if (var5 == 2 && this.zz0 > (double)0.0F) {
         return true;
      } else if (var5 == 3 && this.zz1 < (double)1.0F) {
         return true;
      } else if (var5 == 4 && this.xx0 > (double)0.0F) {
         return true;
      } else if (var5 == 5 && this.xx1 < (double)1.0F) {
         return true;
      } else {
         return !var1.isSolidTile(var2, var3, var4);
      }
   }

   public int getTexture(LevelSource var1, int var2, int var3, int var4, int var5) {
      return this.getTexture(var5, var1.getData(var2, var3, var4));
   }

   public int getTexture(int var1, int var2) {
      return this.getTexture(var1);
   }

   public int getTexture(int var1) {
      return this.tex;
   }

   public AABB getTileAABB(Level var1, int var2, int var3, int var4) {
      return AABB.newTemp((double)var2 + this.xx0, (double)var3 + this.yy0, (double)var4 + this.zz0, (double)var2 + this.xx1, (double)var3 + this.yy1, (double)var4 + this.zz1);
   }

   public void addAABBs(Level var1, int var2, int var3, int var4, AABB var5, ArrayList<AABB> var6) {
      AABB var7 = this.getAABB(var1, var2, var3, var4);
      if (var7 != null && var5.intersects(var7)) {
         var6.add(var7);
      }

   }

   public AABB getAABB(Level var1, int var2, int var3, int var4) {
      return AABB.newTemp((double)var2 + this.xx0, (double)var3 + this.yy0, (double)var4 + this.zz0, (double)var2 + this.xx1, (double)var3 + this.yy1, (double)var4 + this.zz1);
   }

   public boolean isSolidRender() {
      return true;
   }

   public boolean mayPick(int var1, boolean var2) {
      return this.mayPick();
   }

   public boolean mayPick() {
      return true;
   }

   public void tick(Level var1, int var2, int var3, int var4, Random var5) {
   }

   public void animateTick(Level var1, int var2, int var3, int var4, Random var5) {
   }

   public void destroy(Level var1, int var2, int var3, int var4, int var5) {
   }

   public void neighborChanged(Level var1, int var2, int var3, int var4, int var5) {
   }

   public void addLights(Level var1, int var2, int var3, int var4) {
   }

   public int getTickDelay() {
      return 10;
   }

   public void onPlace(Level var1, int var2, int var3, int var4) {
   }

   public void onRemove(Level var1, int var2, int var3, int var4) {
   }

   public int getResourceCount(Random var1) {
      return 1;
   }

   public int getResource(int var1, Random var2) {
      return this.id;
   }

   public float getDestroyProgress(Player var1) {
      if (this.destroySpeed < 0.0F) {
         return 0.0F;
      } else {
         return !var1.canDestroy(this) ? 1.0F / this.destroySpeed / 100.0F : var1.getDestroySpeed(this) / this.destroySpeed / 30.0F;
      }
   }

   public void spawnResources(Level var1, int var2, int var3, int var4, int var5) {
      this.spawnResources(var1, var2, var3, var4, var5, 1.0F);
   }

   public void spawnResources(Level var1, int var2, int var3, int var4, int var5, float var6) {
      if (!var1.isOnline) {
         int var7 = this.getResourceCount(var1.random);

         for(int var8 = 0; var8 < var7; ++var8) {
            if (!(var1.random.nextFloat() > var6)) {
               int var9 = this.getResource(var5, var1.random);
               if (var9 > 0) {
                  float var10 = 0.7F;
                  double var11 = (double)(var1.random.nextFloat() * var10) + (double)(1.0F - var10) * (double)0.5F;
                  double var13 = (double)(var1.random.nextFloat() * var10) + (double)(1.0F - var10) * (double)0.5F;
                  double var15 = (double)(var1.random.nextFloat() * var10) + (double)(1.0F - var10) * (double)0.5F;
                  ItemEntity var17 = new ItemEntity(var1, (double)var2 + var11, (double)var3 + var13, (double)var4 + var15, new ItemInstance(var9, 1, this.getSpawnResourcesAuxValue(var5)));
                  var17.throwTime = 10;
                  var1.addEntity(var17);
               }
            }
         }

      }
   }

   protected int getSpawnResourcesAuxValue(int var1) {
      return 0;
   }

   public float getExplosionResistance(Entity var1) {
      return this.explosionResistance / 5.0F;
   }

   public HitResult clip(Level var1, int var2, int var3, int var4, Vec3 var5, Vec3 var6) {
      this.updateShape(var1, var2, var3, var4);
      var5 = var5.add((double)(-var2), (double)(-var3), (double)(-var4));
      var6 = var6.add((double)(-var2), (double)(-var3), (double)(-var4));
      Vec3 var7 = var5.clipX(var6, this.xx0);
      Vec3 var8 = var5.clipX(var6, this.xx1);
      Vec3 var9 = var5.clipY(var6, this.yy0);
      Vec3 var10 = var5.clipY(var6, this.yy1);
      Vec3 var11 = var5.clipZ(var6, this.zz0);
      Vec3 var12 = var5.clipZ(var6, this.zz1);
      if (!this.containsX(var7)) {
         var7 = null;
      }

      if (!this.containsX(var8)) {
         var8 = null;
      }

      if (!this.containsY(var9)) {
         var9 = null;
      }

      if (!this.containsY(var10)) {
         var10 = null;
      }

      if (!this.containsZ(var11)) {
         var11 = null;
      }

      if (!this.containsZ(var12)) {
         var12 = null;
      }

      Vec3 var13 = null;
      if (var7 != null && (var13 == null || var5.distanceTo(var7) < var5.distanceTo(var13))) {
         var13 = var7;
      }

      if (var8 != null && (var13 == null || var5.distanceTo(var8) < var5.distanceTo(var13))) {
         var13 = var8;
      }

      if (var9 != null && (var13 == null || var5.distanceTo(var9) < var5.distanceTo(var13))) {
         var13 = var9;
      }

      if (var10 != null && (var13 == null || var5.distanceTo(var10) < var5.distanceTo(var13))) {
         var13 = var10;
      }

      if (var11 != null && (var13 == null || var5.distanceTo(var11) < var5.distanceTo(var13))) {
         var13 = var11;
      }

      if (var12 != null && (var13 == null || var5.distanceTo(var12) < var5.distanceTo(var13))) {
         var13 = var12;
      }

      if (var13 == null) {
         return null;
      } else {
         byte var14 = -1;
         if (var13 == var7) {
            var14 = 4;
         }

         if (var13 == var8) {
            var14 = 5;
         }

         if (var13 == var9) {
            var14 = 0;
         }

         if (var13 == var10) {
            var14 = 1;
         }

         if (var13 == var11) {
            var14 = 2;
         }

         if (var13 == var12) {
            var14 = 3;
         }

         return new HitResult(var2, var3, var4, var14, var13.add((double)var2, (double)var3, (double)var4));
      }
   }

   private boolean containsX(Vec3 var1) {
      if (var1 == null) {
         return false;
      } else {
         return var1.y >= this.yy0 && var1.y <= this.yy1 && var1.z >= this.zz0 && var1.z <= this.zz1;
      }
   }

   private boolean containsY(Vec3 var1) {
      if (var1 == null) {
         return false;
      } else {
         return var1.x >= this.xx0 && var1.x <= this.xx1 && var1.z >= this.zz0 && var1.z <= this.zz1;
      }
   }

   private boolean containsZ(Vec3 var1) {
      if (var1 == null) {
         return false;
      } else {
         return var1.x >= this.xx0 && var1.x <= this.xx1 && var1.y >= this.yy0 && var1.y <= this.yy1;
      }
   }

   public void wasExploded(Level var1, int var2, int var3, int var4) {
   }

   public int getRenderLayer() {
      return 0;
   }

   public boolean mayPlace(Level var1, int var2, int var3, int var4) {
      int var5 = var1.getTile(var2, var3, var4);
      return var5 == 0 || tiles[var5].material.isLiquid();
   }

   public boolean use(Level var1, int var2, int var3, int var4, Player var5) {
      return false;
   }

   public final boolean spawnBurnResources(Level var1, float var2, float var3, float var4) {
      return false;
   }

   public void stepOn(Level var1, int var2, int var3, int var4, Entity var5) {
   }

   public void setPlacedOnFace(Level var1, int var2, int var3, int var4, int var5) {
   }

   public void prepareRender(Level var1, int var2, int var3, int var4) {
   }

   public void attack(Level var1, int var2, int var3, int var4, Player var5) {
   }

   public void handleEntityInside(Level var1, int var2, int var3, int var4, Entity var5, Vec3 var6) {
   }

   public void updateShape(LevelSource var1, int var2, int var3, int var4) {
   }

   public int getColor(LevelSource var1, int var2, int var3, int var4) {
      return 16777215;
   }

   public final boolean getSignal(LevelSource var1, int var2, int var3, int var4) {
      return false;
   }

   public boolean getSignal(LevelSource var1, int var2, int var3, int var4, int var5) {
      return false;
   }

   public boolean isSignalSource() {
      return false;
   }

   public void entityInside(Level var1, int var2, int var3, int var4, Entity var5) {
   }

   public boolean getDirectSignal(Level var1, int var2, int var3, int var4, int var5) {
      return false;
   }

   public void updateDefaultShape() {
   }

   public void playerDestroy(Level var1, int var2, int var3, int var4, int var5) {
      this.spawnResources(var1, var2, var3, var4, var5);
   }

   public boolean canSurvive(Level var1, int var2, int var3, int var4) {
      return true;
   }

   public void setPlacedBy(Level var1, int var2, int var3, int var4, Mob var5) {
   }

   public Tile setDescriptionId(String var1) {
      this.descriptionId = "tile." + var1;
      return this;
   }

   public String getDescriptionId() {
      return this.descriptionId;
   }

   public void triggerEvent(Level var1, int var2, int var3, int var4, int var5, int var6) {
   }

   static {
      rock = (new StoneTile(1, 1)).setDestroyTime(1.5F).setExplodeable(10.0F).setSoundType(SOUND_STONE).setDescriptionId("stone");
      grass = (GrassTile)(new GrassTile(2)).setDestroyTime(0.6F).setSoundType(SOUND_GRASS).setDescriptionId("grass");
      dirt = (new DirtTile(3, 2)).setDestroyTime(0.5F).setSoundType(SOUND_GRAVEL).setDescriptionId("dirt");
      stoneBrick = (new Tile(4, 16, Material.stone)).setDestroyTime(2.0F).setExplodeable(10.0F).setSoundType(SOUND_STONE).setDescriptionId("stonebrick");
      wood = (new Tile(5, 4, Material.wood)).setDestroyTime(2.0F).setExplodeable(5.0F).setSoundType(SOUND_WOOD).setDescriptionId("wood");
      sapling = (new Sapling(6, 15)).setDestroyTime(0.0F).setSoundType(SOUND_GRASS).setDescriptionId("sapling");
      unbreakable = (new Tile(7, 17, Material.stone)).setDestroyTime(-1.0F).setExplodeable(6000000.0F).setSoundType(SOUND_STONE).setDescriptionId("bedrock");
      water = (new LiquidTileDynamic(8, Material.water)).setDestroyTime(100.0F).setLightBlock(3).setDescriptionId("water");
      calmWater = (new LiquidTileStatic(9, Material.water)).setDestroyTime(100.0F).setLightBlock(3).setDescriptionId("water");
      lava = (new LiquidTileDynamic(10, Material.lava)).setDestroyTime(0.0F).setLightEmission(1.0F).setLightBlock(255).setDescriptionId("lava");
      calmLava = (new LiquidTileStatic(11, Material.lava)).setDestroyTime(100.0F).setLightEmission(1.0F).setLightBlock(255).setDescriptionId("lava");
      sand = (new SandTile(12, 18)).setDestroyTime(0.5F).setSoundType(SOUND_SAND).setDescriptionId("sand");
      gravel = (new GravelTile(13, 19)).setDestroyTime(0.6F).setSoundType(SOUND_GRAVEL).setDescriptionId("gravel");
      goldOre = (new OreTile(14, 32)).setDestroyTime(3.0F).setExplodeable(5.0F).setSoundType(SOUND_STONE).setDescriptionId("oreGold");
      ironOre = (new OreTile(15, 33)).setDestroyTime(3.0F).setExplodeable(5.0F).setSoundType(SOUND_STONE).setDescriptionId("oreIron");
      coalOre = (new OreTile(16, 34)).setDestroyTime(3.0F).setExplodeable(5.0F).setSoundType(SOUND_STONE).setDescriptionId("oreCoal");
      treeTrunk = (new TreeTile(17)).setDestroyTime(2.0F).setSoundType(SOUND_WOOD).setDescriptionId("log");
      leaves = (LeafTile)(new LeafTile(18, 52)).setDestroyTime(0.2F).setLightBlock(1).setSoundType(SOUND_GRASS).setDescriptionId("leaves");
      sponge = (new Sponge(19)).setDestroyTime(0.6F).setSoundType(SOUND_GRASS).setDescriptionId("sponge");
      glass = (new GlassTile(20, 49, Material.glass, false)).setDestroyTime(0.3F).setSoundType(SOUND_GLASS).setDescriptionId("glass");
      lapisOre = (new OreTile(21, 160)).setDestroyTime(3.0F).setExplodeable(5.0F).setSoundType(SOUND_STONE).setDescriptionId("oreLapis");
      lapisBlock = (new Tile(22, 144, Material.stone)).setDestroyTime(3.0F).setExplodeable(5.0F).setSoundType(SOUND_STONE).setDescriptionId("blockLapis");
      dispenser = (new DispenserTile(23)).setDestroyTime(3.5F).setSoundType(SOUND_STONE).setDescriptionId("dispenser");
      sandStone = (new SandStoneTile(24)).setSoundType(SOUND_STONE).setDestroyTime(0.8F).setDescriptionId("sandStone");
      musicBlock = (new MusicTile(25)).setDestroyTime(0.8F).setDescriptionId("musicBlock");
      unused_26 = null;
      unused_27 = null;
      unused_28 = null;
      unused_29 = null;
      unused_30 = null;
      unused_31 = null;
      unused_32 = null;
      unused_33 = null;
      unused_34 = null;
      cloth = (new ClothTile()).setDestroyTime(0.8F).setSoundType(SOUND_CLOTH).setDescriptionId("cloth");
      unused_36 = null;
      flower = (Bush)(new Bush(37, 13)).setDestroyTime(0.0F).setSoundType(SOUND_GRASS).setDescriptionId("flower");
      rose = (Bush)(new Bush(38, 12)).setDestroyTime(0.0F).setSoundType(SOUND_GRASS).setDescriptionId("rose");
      mushroom1 = (Bush)(new Mushroom(39, 29)).setDestroyTime(0.0F).setSoundType(SOUND_GRASS).setLightEmission(0.125F).setDescriptionId("mushroom");
      mushroom2 = (Bush)(new Mushroom(40, 28)).setDestroyTime(0.0F).setSoundType(SOUND_GRASS).setDescriptionId("mushroom");
      goldBlock = (new MetalTile(41, 23)).setDestroyTime(3.0F).setExplodeable(10.0F).setSoundType(SOUND_METAL).setDescriptionId("blockGold");
      ironBlock = (new MetalTile(42, 22)).setDestroyTime(5.0F).setExplodeable(10.0F).setSoundType(SOUND_METAL).setDescriptionId("blockIron");
      stoneSlab = (new StoneSlabTile(43, true)).setDestroyTime(2.0F).setExplodeable(10.0F).setSoundType(SOUND_STONE).setDescriptionId("stoneSlab");
      stoneSlabHalf = (new StoneSlabTile(44, false)).setDestroyTime(2.0F).setExplodeable(10.0F).setSoundType(SOUND_STONE).setDescriptionId("stoneSlab");
      redBrick = (new Tile(45, 7, Material.stone)).setDestroyTime(2.0F).setExplodeable(10.0F).setSoundType(SOUND_STONE).setDescriptionId("brick");
      tnt = (new TntTile(46, 8)).setDestroyTime(0.0F).setSoundType(SOUND_GRASS).setDescriptionId("tnt");
      bookshelf = (new BookshelfTile(47, 35)).setDestroyTime(1.5F).setSoundType(SOUND_WOOD).setDescriptionId("bookshelf");
      mossStone = (new Tile(48, 36, Material.stone)).setDestroyTime(2.0F).setExplodeable(10.0F).setSoundType(SOUND_STONE).setDescriptionId("stoneMoss");
      obsidian = (new ObsidianTile(49, 37)).setDestroyTime(10.0F).setExplodeable(2000.0F).setSoundType(SOUND_STONE).setDescriptionId("obsidian");
      torch = (new TorchTile(50, 80)).setDestroyTime(0.0F).setLightEmission(0.9375F).setSoundType(SOUND_WOOD).setDescriptionId("torch");
      fire = (FireTile)(new FireTile(51, 31)).setDestroyTime(0.0F).setLightEmission(1.0F).setSoundType(SOUND_WOOD).setDescriptionId("fire");
      mobSpawner = (new MobSpawnerTile(52, 65)).setDestroyTime(5.0F).setSoundType(SOUND_METAL).setDescriptionId("mobSpawner");
      stairs_wood = (new StairTile(53, wood)).setDescriptionId("stairsWood");
      chest = (new ChestTile(54)).setDestroyTime(2.5F).setSoundType(SOUND_WOOD).setDescriptionId("chest");
      redStoneDust = (new RedStoneDustTile(55, 84)).setDestroyTime(0.0F).setSoundType(SOUND_NORMAL).setDescriptionId("redstoneDust");
      emeraldOre = (new OreTile(56, 50)).setDestroyTime(3.0F).setExplodeable(5.0F).setSoundType(SOUND_STONE).setDescriptionId("oreDiamond");
      emeraldBlock = (new MetalTile(57, 24)).setDestroyTime(5.0F).setExplodeable(10.0F).setSoundType(SOUND_METAL).setDescriptionId("blockDiamond");
      workBench = (new WorkbenchTile(58)).setDestroyTime(2.5F).setSoundType(SOUND_WOOD).setDescriptionId("workbench");
      crops = (new CropTile(59, 88)).setDestroyTime(0.0F).setSoundType(SOUND_GRASS).setDescriptionId("crops");
      farmland = (new FarmTile(60)).setDestroyTime(0.6F).setSoundType(SOUND_GRAVEL).setDescriptionId("farmland");
      furnace = (new FurnaceTile(61, false)).setDestroyTime(3.5F).setSoundType(SOUND_STONE).setDescriptionId("furnace");
      furnace_lit = (new FurnaceTile(62, true)).setDestroyTime(3.5F).setSoundType(SOUND_STONE).setLightEmission(0.875F).setDescriptionId("furnace");
      sign = (new SignTile(63, SignTileEntity.class, true)).setDestroyTime(1.0F).setSoundType(SOUND_WOOD).setDescriptionId("sign");
      door_wood = (new DoorTile(64, Material.wood)).setDestroyTime(3.0F).setSoundType(SOUND_WOOD).setDescriptionId("doorWood");
      ladder = (new LadderTile(65, 83)).setDestroyTime(0.4F).setSoundType(SOUND_WOOD).setDescriptionId("ladder");
      rail = (new RailTile(66, 128)).setDestroyTime(0.7F).setSoundType(SOUND_METAL).setDescriptionId("rail");
      stairs_stone = (new StairTile(67, stoneBrick)).setDescriptionId("stairsStone");
      wallSign = (new SignTile(68, SignTileEntity.class, false)).setDestroyTime(1.0F).setSoundType(SOUND_WOOD).setDescriptionId("sign");
      lever = (new LeverTile(69, 96)).setDestroyTime(0.5F).setSoundType(SOUND_WOOD).setDescriptionId("lever");
      pressurePlate_stone = (new PressurePlateTile(70, rock.tex, PressurePlateTile.Sensitivity.mobs)).setDestroyTime(0.5F).setSoundType(SOUND_STONE).setDescriptionId("pressurePlate");
      door_iron = (new DoorTile(71, Material.metal)).setDestroyTime(5.0F).setSoundType(SOUND_METAL).setDescriptionId("doorIron");
      pressurePlate_wood = (new PressurePlateTile(72, wood.tex, PressurePlateTile.Sensitivity.everything)).setDestroyTime(0.5F).setSoundType(SOUND_WOOD).setDescriptionId("pressurePlate");
      redStoneOre = (new RedStoneOreTile(73, 51, false)).setDestroyTime(3.0F).setExplodeable(5.0F).setSoundType(SOUND_STONE).setDescriptionId("oreRedstone");
      redStoneOre_lit = (new RedStoneOreTile(74, 51, true)).setLightEmission(0.625F).setDestroyTime(3.0F).setExplodeable(5.0F).setSoundType(SOUND_STONE).setDescriptionId("oreRedstone");
      notGate_off = (new NotGateTile(75, 115, false)).setDestroyTime(0.0F).setSoundType(SOUND_WOOD).setDescriptionId("notGate");
      notGate_on = (new NotGateTile(76, 99, true)).setDestroyTime(0.0F).setLightEmission(0.5F).setSoundType(SOUND_WOOD).setDescriptionId("notGate");
      button = (new ButtonTile(77, rock.tex)).setDestroyTime(0.5F).setSoundType(SOUND_STONE).setDescriptionId("button");
      topSnow = (new TopSnowTile(78, 66)).setDestroyTime(0.1F).setSoundType(SOUND_CLOTH).setDescriptionId("snow");
      ice = (new IceTile(79, 67)).setDestroyTime(0.5F).setLightBlock(3).setSoundType(SOUND_GLASS).setDescriptionId("ice");
      snow = (new SnowTile(80, 66)).setDestroyTime(0.2F).setSoundType(SOUND_CLOTH).setDescriptionId("snow");
      cactus = (new CactusTile(81, 70)).setDestroyTime(0.4F).setSoundType(SOUND_CLOTH).setDescriptionId("cactus");
      clay = (new ClayTile(82, 72)).setDestroyTime(0.6F).setSoundType(SOUND_GRAVEL).setDescriptionId("clay");
      reeds = (new ReedTile(83, 73)).setDestroyTime(0.0F).setSoundType(SOUND_GRASS).setDescriptionId("reeds");
      recordPlayer = (new RecordPlayerTile(84, 74)).setDestroyTime(2.0F).setExplodeable(10.0F).setSoundType(SOUND_STONE).setDescriptionId("jukebox");
      fence = (new FenceTile(85, 4)).setDestroyTime(2.0F).setExplodeable(5.0F).setSoundType(SOUND_WOOD).setDescriptionId("fence");
      pumpkin = (new PumpkinTile(86, 102, false)).setDestroyTime(1.0F).setSoundType(SOUND_WOOD).setDescriptionId("pumpkin");
      hellRock = (new HellStoneTile(87, 103)).setDestroyTime(0.4F).setSoundType(SOUND_STONE).setDescriptionId("hellrock");
      hellSand = (new HellSandTile(88, 104)).setDestroyTime(0.5F).setSoundType(SOUND_SAND).setDescriptionId("hellsand");
      lightGem = (new LightGemTile(89, 105, Material.glass)).setDestroyTime(0.3F).setSoundType(SOUND_GLASS).setLightEmission(1.0F).setDescriptionId("lightgem");
      portalTile = (PortalTile)(new PortalTile(90, 14)).setDestroyTime(-1.0F).setSoundType(SOUND_GLASS).setLightEmission(0.75F).setDescriptionId("portal");
      litPumpkin = (new PumpkinTile(91, 102, true)).setDestroyTime(1.0F).setSoundType(SOUND_WOOD).setLightEmission(1.0F).setDescriptionId("litpumpkin");
      cake = (new CakeTile(92, 121)).setDestroyTime(0.5F).setSoundType(SOUND_CLOTH).setDescriptionId("cake");
      Item.items[cloth.id] = (new ClothTileItem(cloth.id - 256)).setDescriptionId("cloth");
      Item.items[treeTrunk.id] = (new TreeTileItem(treeTrunk.id - 256)).setDescriptionId("log");

      for(int var0 = 0; var0 < 256; ++var0) {
         if (tiles[var0] != null && Item.items[var0] == null) {
            Item.items[var0] = new TileItem(var0 - 256);
         }
      }

   }

   public static class SoundType {
      public final String name;
      public final float volume;
      public final float pitch;

      public SoundType(String var1, float var2, float var3) {
         this.name = var1;
         this.volume = var2;
         this.pitch = var3;
      }

      public float getVolume() {
         return this.volume;
      }

      public float getPitch() {
         return this.pitch;
      }

      public String getBreakSound() {
         return "step." + this.name;
      }

      public String getStepSound() {
         return "step." + this.name;
      }
   }
}
