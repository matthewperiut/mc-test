package net.minecraft.world.item;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import net.minecraft.locale.Descriptive;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;

public class Item implements Descriptive<Item> {
   protected static final int ICON_COLUMNS = 16;
   protected static final String ICON_DESCRIPTION_PREFIX = "item.";
   protected static Random random = new Random();
   private static final int MAX_STACK_SIZE = 64;
   public static Item[] items = new Item[32000];
   public static Item shovel_iron;
   public static Item pickAxe_iron;
   public static Item hatchet_iron;
   public static Item flintAndSteel;
   public static Item apple;
   public static Item bow;
   public static Item arrow;
   public static Item coal;
   public static Item emerald;
   public static Item ironIngot;
   public static Item goldIngot;
   public static Item sword_iron;
   public static Item sword_wood;
   public static Item shovel_wood;
   public static Item pickAxe_wood;
   public static Item hatchet_wood;
   public static Item sword_stone;
   public static Item shovel_stone;
   public static Item pickAxe_stone;
   public static Item hatchet_stone;
   public static Item sword_emerald;
   public static Item shovel_emerald;
   public static Item pickAxe_emerald;
   public static Item hatchet_emerald;
   public static Item stick;
   public static Item bowl;
   public static Item mushroomStew;
   public static Item sword_gold;
   public static Item shovel_gold;
   public static Item pickAxe_gold;
   public static Item hatchet_gold;
   public static Item string;
   public static Item feather;
   public static Item sulphur;
   public static Item hoe_wood;
   public static Item hoe_stone;
   public static Item hoe_iron;
   public static Item hoe_emerald;
   public static Item hoe_gold;
   public static Item seeds;
   public static Item wheat;
   public static Item bread;
   public static Item helmet_cloth;
   public static Item chestplate_cloth;
   public static Item leggings_cloth;
   public static Item boots_cloth;
   public static Item helmet_chain;
   public static Item chestplate_chain;
   public static Item leggings_chain;
   public static Item boots_chain;
   public static Item helmet_iron;
   public static Item chestplate_iron;
   public static Item leggings_iron;
   public static Item boots_iron;
   public static Item helmet_diamond;
   public static Item chestplate_diamond;
   public static Item leggings_diamond;
   public static Item boots_diamond;
   public static Item helmet_gold;
   public static Item chestplate_gold;
   public static Item leggings_gold;
   public static Item boots_gold;
   public static Item flint;
   public static Item porkChop_raw;
   public static Item porkChop_cooked;
   public static Item painting;
   public static Item apple_gold;
   public static Item sign;
   public static Item door_wood;
   public static Item bucket_empty;
   public static Item bucket_water;
   public static Item bucket_lava;
   public static Item minecart;
   public static Item saddle;
   public static Item door_iron;
   public static Item redStone;
   public static Item snowBall;
   public static Item boat;
   public static Item leather;
   public static Item milk;
   public static Item brick;
   public static Item clay;
   public static Item reeds;
   public static Item paper;
   public static Item book;
   public static Item slimeBall;
   public static Item minecart_chest;
   public static Item minecart_furnace;
   public static Item egg;
   public static Item compass;
   public static Item fishingRod;
   public static Item clock;
   public static Item yellowDust;
   public static Item fish_raw;
   public static Item fish_cooked;
   public static Item dye_powder;
   public static Item bone;
   public static Item sugar;
   public static Item cake;
   public static Item record_01;
   public static Item record_02;
   public final int id;
   protected int maxStackSize = 64;
   protected int maxDamage = 32;
   protected int icon;
   protected boolean handEquipped = false;
   protected boolean isStackedByData = false;
   private Item craftingRemainingItem = null;
   private String descriptionId;

   protected Item(int var1) {
      this.id = 256 + var1;
      if (items[256 + var1] != null) {
         System.out.println("CONFLICT @ " + var1);
      }

      items[256 + var1] = this;
   }

   public Item setIcon(int var1) {
      this.icon = var1;
      return this;
   }

   public Item setMaxStackSize(int var1) {
      this.maxStackSize = var1;
      return this;
   }

   public Item setIcon(int var1, int var2) {
      this.icon = var1 + var2 * 16;
      return this;
   }

   public int getIcon(ItemInstance var1) {
      return this.icon;
   }

   public final boolean useOn(ItemInstance var1, Level var2, int var3, int var4, int var5, int var6) {
      return false;
   }

   public boolean useOn(ItemInstance var1, Player var2, Level var3, int var4, int var5, int var6, int var7) {
      return false;
   }

   public float getDestroySpeed(ItemInstance var1, Tile var2) {
      return 1.0F;
   }

   public ItemInstance use(ItemInstance var1, Level var2, Player var3) {
      return var1;
   }

   public int getMaxStackSize() {
      return this.maxStackSize;
   }

   public int getLevelDataForAuxValue(int var1) {
      return 0;
   }

   public boolean isStackedByData() {
      return this.isStackedByData;
   }

   protected Item setStackedByData(boolean var1) {
      this.isStackedByData = var1;
      return this;
   }

   public int getMaxDamage() {
      return this.maxDamage;
   }

   protected Item setMaxDamage(int var1) {
      this.maxDamage = var1;
      return this;
   }

   public void hurtEnemy(ItemInstance var1, Mob var2) {
   }

   public void mineBlock(ItemInstance var1, int var2, int var3, int var4, int var5) {
   }

   public int getAttackDamage(Entity var1) {
      return 1;
   }

   public boolean canDestroySpecial(Tile var1) {
      return false;
   }

   public void interactEnemy(ItemInstance var1, Mob var2) {
   }

   public Item handEquipped() {
      this.handEquipped = true;
      return this;
   }

   public boolean isHandEquipped() {
      return this.handEquipped;
   }

   public boolean isMirroredArt() {
      return false;
   }

   public Item setDescriptionId(String var1) {
      this.descriptionId = "item." + var1;
      return this;
   }

   public String getDescriptionId() {
      return this.descriptionId;
   }

   public String getDescriptionId(ItemInstance var1) {
      return this.descriptionId;
   }

   public static void main(String[] var0) {
      for(Item var4 : items) {
         if (var4 != null) {
            System.out.println(var4.getDescriptionId() + ".name=");
            System.out.println(var4.getDescriptionId() + ".desc=");
         }
      }

      System.out.println(System.currentTimeMillis());
      Calendar var5 = Calendar.getInstance();
      var5.set(5, 31);
      var5.set(11, 0);
      var5.set(12, 0);
      var5.set(13, 0);
      var5.set(14, 0);
      System.out.println(var5.getTimeInMillis());
      var5.set(1, 2011);
      var5.set(2, 0);
      var5.set(5, 1);
      var5.set(11, 23);
      var5.set(12, 59);
      var5.set(13, 59);
      var5.set(14, 999);
      System.out.println(var5.getTimeInMillis());
      var5.setTimeInMillis(1292798324310L);
      System.out.println((new SimpleDateFormat()).format(var5.getTime()));
   }

   public Item setCraftingRemainingItem(Item var1) {
      if (this.maxStackSize > 1) {
         throw new IllegalArgumentException("Max stack size must be 1 for items with crafting results");
      } else {
         this.craftingRemainingItem = var1;
         return this;
      }
   }

   public Item getCraftingRemainingItem() {
      return this.craftingRemainingItem;
   }

   public boolean hasCraftingRemainingItem() {
      return this.craftingRemainingItem != null;
   }

   static {
      shovel_iron = (new ShovelItem(0, Item.Tier.IRON)).setIcon(2, 5).setDescriptionId("shovelIron");
      pickAxe_iron = (new PickaxeItem(1, Item.Tier.IRON)).setIcon(2, 6).setDescriptionId("pickaxeIron");
      hatchet_iron = (new HatchetItem(2, Item.Tier.IRON)).setIcon(2, 7).setDescriptionId("hatchetIron");
      flintAndSteel = (new FlintAndSteelItem(3)).setIcon(5, 0).setDescriptionId("flintAndSteel");
      apple = (new FoodItem(4, 4)).setIcon(10, 0).setDescriptionId("apple");
      bow = (new BowItem(5)).setIcon(5, 1).setDescriptionId("bow");
      arrow = (new Item(6)).setIcon(5, 2).setDescriptionId("arrow");
      coal = (new CoalItem(7)).setIcon(7, 0).setDescriptionId("coal");
      emerald = (new Item(8)).setIcon(7, 3).setDescriptionId("emerald");
      ironIngot = (new Item(9)).setIcon(7, 1).setDescriptionId("ingotIron");
      goldIngot = (new Item(10)).setIcon(7, 2).setDescriptionId("ingotGold");
      sword_iron = (new WeaponItem(11, Item.Tier.IRON)).setIcon(2, 4).setDescriptionId("swordIron");
      sword_wood = (new WeaponItem(12, Item.Tier.WOOD)).setIcon(0, 4).setDescriptionId("swordWood");
      shovel_wood = (new ShovelItem(13, Item.Tier.WOOD)).setIcon(0, 5).setDescriptionId("shovelWood");
      pickAxe_wood = (new PickaxeItem(14, Item.Tier.WOOD)).setIcon(0, 6).setDescriptionId("pickaxeWood");
      hatchet_wood = (new HatchetItem(15, Item.Tier.WOOD)).setIcon(0, 7).setDescriptionId("hatchetWood");
      sword_stone = (new WeaponItem(16, Item.Tier.STONE)).setIcon(1, 4).setDescriptionId("swordStone");
      shovel_stone = (new ShovelItem(17, Item.Tier.STONE)).setIcon(1, 5).setDescriptionId("shovelStone");
      pickAxe_stone = (new PickaxeItem(18, Item.Tier.STONE)).setIcon(1, 6).setDescriptionId("pickaxeStone");
      hatchet_stone = (new HatchetItem(19, Item.Tier.STONE)).setIcon(1, 7).setDescriptionId("hatchetStone");
      sword_emerald = (new WeaponItem(20, Item.Tier.EMERALD)).setIcon(3, 4).setDescriptionId("swordDiamond");
      shovel_emerald = (new ShovelItem(21, Item.Tier.EMERALD)).setIcon(3, 5).setDescriptionId("shovelDiamond");
      pickAxe_emerald = (new PickaxeItem(22, Item.Tier.EMERALD)).setIcon(3, 6).setDescriptionId("pickaxeDiamond");
      hatchet_emerald = (new HatchetItem(23, Item.Tier.EMERALD)).setIcon(3, 7).setDescriptionId("hatchetDiamond");
      stick = (new Item(24)).setIcon(5, 3).handEquipped().setDescriptionId("stick");
      bowl = (new Item(25)).setIcon(7, 4).setDescriptionId("bowl");
      mushroomStew = (new BowlFoodItem(26, 10)).setIcon(8, 4).setDescriptionId("mushroomStew");
      sword_gold = (new WeaponItem(27, Item.Tier.GOLD)).setIcon(4, 4).setDescriptionId("swordGold");
      shovel_gold = (new ShovelItem(28, Item.Tier.GOLD)).setIcon(4, 5).setDescriptionId("shovelGold");
      pickAxe_gold = (new PickaxeItem(29, Item.Tier.GOLD)).setIcon(4, 6).setDescriptionId("pickaxeGold");
      hatchet_gold = (new HatchetItem(30, Item.Tier.GOLD)).setIcon(4, 7).setDescriptionId("hatchetGold");
      string = (new Item(31)).setIcon(8, 0).setDescriptionId("string");
      feather = (new Item(32)).setIcon(8, 1).setDescriptionId("feather");
      sulphur = (new Item(33)).setIcon(8, 2).setDescriptionId("sulphur");
      hoe_wood = (new HoeItem(34, Item.Tier.WOOD)).setIcon(0, 8).setDescriptionId("hoeWood");
      hoe_stone = (new HoeItem(35, Item.Tier.STONE)).setIcon(1, 8).setDescriptionId("hoeStone");
      hoe_iron = (new HoeItem(36, Item.Tier.IRON)).setIcon(2, 8).setDescriptionId("hoeIron");
      hoe_emerald = (new HoeItem(37, Item.Tier.EMERALD)).setIcon(3, 8).setDescriptionId("hoeDiamond");
      hoe_gold = (new HoeItem(38, Item.Tier.GOLD)).setIcon(4, 8).setDescriptionId("hoeGold");
      seeds = (new SeedItem(39, Tile.crops.id)).setIcon(9, 0).setDescriptionId("seeds");
      wheat = (new Item(40)).setIcon(9, 1).setDescriptionId("wheat");
      bread = (new FoodItem(41, 5)).setIcon(9, 2).setDescriptionId("bread");
      helmet_cloth = (new ArmorItem(42, 0, 0, 0)).setIcon(0, 0).setDescriptionId("helmetCloth");
      chestplate_cloth = (new ArmorItem(43, 0, 0, 1)).setIcon(0, 1).setDescriptionId("chestplateCloth");
      leggings_cloth = (new ArmorItem(44, 0, 0, 2)).setIcon(0, 2).setDescriptionId("leggingsCloth");
      boots_cloth = (new ArmorItem(45, 0, 0, 3)).setIcon(0, 3).setDescriptionId("bootsCloth");
      helmet_chain = (new ArmorItem(46, 1, 1, 0)).setIcon(1, 0).setDescriptionId("helmetChain");
      chestplate_chain = (new ArmorItem(47, 1, 1, 1)).setIcon(1, 1).setDescriptionId("chestplateChain");
      leggings_chain = (new ArmorItem(48, 1, 1, 2)).setIcon(1, 2).setDescriptionId("leggingsChain");
      boots_chain = (new ArmorItem(49, 1, 1, 3)).setIcon(1, 3).setDescriptionId("bootsChain");
      helmet_iron = (new ArmorItem(50, 2, 2, 0)).setIcon(2, 0).setDescriptionId("helmetIron");
      chestplate_iron = (new ArmorItem(51, 2, 2, 1)).setIcon(2, 1).setDescriptionId("chestplateIron");
      leggings_iron = (new ArmorItem(52, 2, 2, 2)).setIcon(2, 2).setDescriptionId("leggingsIron");
      boots_iron = (new ArmorItem(53, 2, 2, 3)).setIcon(2, 3).setDescriptionId("bootsIron");
      helmet_diamond = (new ArmorItem(54, 3, 3, 0)).setIcon(3, 0).setDescriptionId("helmetDiamond");
      chestplate_diamond = (new ArmorItem(55, 3, 3, 1)).setIcon(3, 1).setDescriptionId("chestplateDiamond");
      leggings_diamond = (new ArmorItem(56, 3, 3, 2)).setIcon(3, 2).setDescriptionId("leggingsDiamond");
      boots_diamond = (new ArmorItem(57, 3, 3, 3)).setIcon(3, 3).setDescriptionId("bootsDiamond");
      helmet_gold = (new ArmorItem(58, 1, 4, 0)).setIcon(4, 0).setDescriptionId("helmetGold");
      chestplate_gold = (new ArmorItem(59, 1, 4, 1)).setIcon(4, 1).setDescriptionId("chestplateGold");
      leggings_gold = (new ArmorItem(60, 1, 4, 2)).setIcon(4, 2).setDescriptionId("leggingsGold");
      boots_gold = (new ArmorItem(61, 1, 4, 3)).setIcon(4, 3).setDescriptionId("bootsGold");
      flint = (new Item(62)).setIcon(6, 0).setDescriptionId("flint");
      porkChop_raw = (new FoodItem(63, 3)).setIcon(7, 5).setDescriptionId("porkchopRaw");
      porkChop_cooked = (new FoodItem(64, 8)).setIcon(8, 5).setDescriptionId("porkchopCooked");
      painting = (new PaintingItem(65)).setIcon(10, 1).setDescriptionId("painting");
      apple_gold = (new FoodItem(66, 42)).setIcon(11, 0).setDescriptionId("appleGold");
      sign = (new SignItem(67)).setIcon(10, 2).setDescriptionId("sign");
      door_wood = (new DoorItem(68, Material.wood)).setIcon(11, 2).setDescriptionId("doorWood");
      bucket_empty = (new BucketItem(69, 0)).setIcon(10, 4).setDescriptionId("bucket");
      bucket_water = (new BucketItem(70, Tile.water.id)).setIcon(11, 4).setDescriptionId("bucketWater").setCraftingRemainingItem(bucket_empty);
      bucket_lava = (new BucketItem(71, Tile.lava.id)).setIcon(12, 4).setDescriptionId("bucketLava").setCraftingRemainingItem(bucket_empty);
      minecart = (new MinecartItem(72, 0)).setIcon(7, 8).setDescriptionId("minecart");
      saddle = (new SaddleItem(73)).setIcon(8, 6).setDescriptionId("saddle");
      door_iron = (new DoorItem(74, Material.metal)).setIcon(12, 2).setDescriptionId("doorIron");
      redStone = (new RedStoneItem(75)).setIcon(8, 3).setDescriptionId("redstone");
      snowBall = (new SnowballItem(76)).setIcon(14, 0).setDescriptionId("snowball");
      boat = (new BoatItem(77)).setIcon(8, 8).setDescriptionId("boat");
      leather = (new Item(78)).setIcon(7, 6).setDescriptionId("leather");
      milk = (new BucketItem(79, -1)).setIcon(13, 4).setDescriptionId("milk").setCraftingRemainingItem(bucket_empty);
      brick = (new Item(80)).setIcon(6, 1).setDescriptionId("brick");
      clay = (new Item(81)).setIcon(9, 3).setDescriptionId("clay");
      reeds = (new TilePlanterItem(82, Tile.reeds)).setIcon(11, 1).setDescriptionId("reeds");
      paper = (new Item(83)).setIcon(10, 3).setDescriptionId("paper");
      book = (new Item(84)).setIcon(11, 3).setDescriptionId("book");
      slimeBall = (new Item(85)).setIcon(14, 1).setDescriptionId("slimeball");
      minecart_chest = (new MinecartItem(86, 1)).setIcon(7, 9).setDescriptionId("minecartChest");
      minecart_furnace = (new MinecartItem(87, 2)).setIcon(7, 10).setDescriptionId("minecartFurnace");
      egg = (new EggItem(88)).setIcon(12, 0).setDescriptionId("egg");
      compass = (new Item(89)).setIcon(6, 3).setDescriptionId("compass");
      fishingRod = (new FishingRodItem(90)).setIcon(5, 4).setDescriptionId("fishingRod");
      clock = (new Item(91)).setIcon(6, 4).setDescriptionId("clock");
      yellowDust = (new Item(92)).setIcon(9, 4).setDescriptionId("yellowDust");
      fish_raw = (new FoodItem(93, 2)).setIcon(9, 5).setDescriptionId("fishRaw");
      fish_cooked = (new FoodItem(94, 5)).setIcon(10, 5).setDescriptionId("fishCooked");
      dye_powder = (new DyePowderItem(95)).setIcon(14, 4).setDescriptionId("dyePowder");
      bone = (new Item(96)).setIcon(12, 1).setDescriptionId("bone").handEquipped();
      sugar = (new Item(97)).setIcon(13, 0).setDescriptionId("sugar").handEquipped();
      cake = (new TilePlanterItem(98, Tile.cake)).setMaxStackSize(1).setIcon(13, 1).setDescriptionId("cake");
      record_01 = (new RecordingItem(2000, "13")).setIcon(0, 15).setDescriptionId("record");
      record_02 = (new RecordingItem(2001, "cat")).setIcon(1, 15).setDescriptionId("record");
   }

   protected static enum Tier {
      WOOD(0, 59, 2.0F, 0),
      STONE(1, 131, 4.0F, 1),
      IRON(2, 250, 6.0F, 2),
      EMERALD(3, 1561, 8.0F, 3),
      GOLD(0, 32, 12.0F, 0);

      private final int level;
      private final int uses;
      private final float speed;
      private final int damage;

      private Tier(int var3, int var4, float var5, int var6) {
         this.level = var3;
         this.uses = var4;
         this.speed = var5;
         this.damage = var6;
      }

      public int getUses() {
         return this.uses;
      }

      public float getSpeed() {
         return this.speed;
      }

      public int getAttackDamageBonus() {
         return this.damage;
      }

      public int getLevel() {
         return this.level;
      }
   }
}
