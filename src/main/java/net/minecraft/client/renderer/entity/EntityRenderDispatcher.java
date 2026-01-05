package net.minecraft.client.renderer.entity;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Options;
import net.minecraft.client.gamemode.secret.Builder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.Textures;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Painting;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.item.Boat;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.Minecart;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL11;

public class EntityRenderDispatcher {
   private Map<Class<? extends Entity>, EntityRenderer<? extends Entity>> renderers = new HashMap();
   public static EntityRenderDispatcher instance = new EntityRenderDispatcher();
   private Font font;
   public static double xOff;
   public static double yOff;
   public static double zOff;
   public Textures textures;
   public ItemInHandRenderer itemInHandRenderer;
   public Level level;
   public Player player;
   public float playerRotY;
   public float playerRotX;
   public Options options;
   public double xPlayer;
   public double yPlayer;
   public double zPlayer;

   private EntityRenderDispatcher() {
      this.renderers.put(Spider.class, new SpiderRenderer());
      this.renderers.put(Pig.class, new PigRenderer(new PigModel(), new PigModel(0.5F), 0.7F));
      this.renderers.put(Sheep.class, new SheepRenderer(new SheepModel(), new SheepFurModel(), 0.7F));
      this.renderers.put(Cow.class, new CowRenderer(new CowModel(), 0.7F));
      this.renderers.put(Chicken.class, new ChickenRenderer(new ChickenModel(), 0.3F));
      this.renderers.put(Creeper.class, new CreeperRenderer());
      this.renderers.put(Skeleton.class, new HumanoidMobRenderer(new SkeletonModel(), 0.5F));
      this.renderers.put(Zombie.class, new HumanoidMobRenderer(new ZombieModel(), 0.5F));
      this.renderers.put(Builder.class, new HumanoidMobRenderer(new HumanoidModel(), 0.5F));
      this.renderers.put(Slime.class, new SlimeRenderer(new SlimeModel(16), new SlimeModel(0), 0.25F));
      this.renderers.put(Player.class, new PlayerRenderer());
      this.renderers.put(Giant.class, new GiantMobRenderer(new ZombieModel(), 0.5F, 6.0F));
      this.renderers.put(Ghast.class, new GhastRenderer());
      this.renderers.put(Squid.class, new SquidRenderer(new SquidModel(), 0.7F));
      this.renderers.put(Mob.class, new MobRenderer(new HumanoidModel(), 0.5F));
      this.renderers.put(Entity.class, new DefaultRenderer());
      this.renderers.put(Painting.class, new PaintingRenderer());
      this.renderers.put(Arrow.class, new ArrowRenderer());
      this.renderers.put(Snowball.class, new ItemSpriteRenderer(Item.snowBall.getIcon((ItemInstance)null)));
      this.renderers.put(ThrownEgg.class, new ItemSpriteRenderer(Item.egg.getIcon((ItemInstance)null)));
      this.renderers.put(Fireball.class, new FireballRenderer());
      this.renderers.put(ItemEntity.class, new ItemRenderer());
      this.renderers.put(PrimedTnt.class, new TntRenderer());
      this.renderers.put(FallingTile.class, new FallingTileRenderer());
      this.renderers.put(Minecart.class, new MinecartRenderer());
      this.renderers.put(Boat.class, new BoatRenderer());
      this.renderers.put(FishingHook.class, new FishingHookRenderer());

      for(EntityRenderer var2 : this.renderers.values()) {
         var2.init(this);
      }

   }

   @SuppressWarnings("unchecked")
   public <T extends Entity> EntityRenderer<T> getRenderer(Class<? extends Entity> var1) {
      EntityRenderer var2 = (EntityRenderer)this.renderers.get(var1);
      if (var2 == null && var1 != Entity.class) {
         var2 = this.getRenderer((Class<? extends Entity>)var1.getSuperclass());
         this.renderers.put(var1, var2);
      }

      return var2;
   }

   public <T extends Entity> EntityRenderer<T> getRenderer(Entity var1) {
      return this.getRenderer(var1.getClass());
   }

   public void prepare(Level var1, Textures var2, Font var3, Player var4, Options var5, float var6) {
      this.level = var1;
      this.textures = var2;
      this.options = var5;
      this.player = var4;
      this.font = var3;
      this.playerRotY = var4.yRotO + (var4.yRot - var4.yRotO) * var6;
      this.playerRotX = var4.xRotO + (var4.xRot - var4.xRotO) * var6;
      this.xPlayer = var4.xOld + (var4.x - var4.xOld) * (double)var6;
      this.yPlayer = var4.yOld + (var4.y - var4.yOld) * (double)var6;
      this.zPlayer = var4.zOld + (var4.z - var4.zOld) * (double)var6;
   }

   public void render(Entity var1, float var2) {
      double var3 = var1.xOld + (var1.x - var1.xOld) * (double)var2;
      double var5 = var1.yOld + (var1.y - var1.yOld) * (double)var2;
      double var7 = var1.zOld + (var1.z - var1.zOld) * (double)var2;
      float var9 = var1.yRotO + (var1.yRot - var1.yRotO) * var2;
      float var10 = var1.getBrightness(var2);
      GL11.glColor3f(var10, var10, var10);
      this.render(var1, var3 - xOff, var5 - yOff, var7 - zOff, var9, var2);
   }

   public void render(Entity var1, double var2, double var4, double var6, float var8, float var9) {
      EntityRenderer var10 = this.getRenderer(var1);
      if (var10 != null) {
         var10.render(var1, var2, var4, var6, var8, var9);
         var10.postRender(var1, var2, var4, var6, var8, var9);
      }

   }

   public void setLevel(Level var1) {
      this.level = var1;
   }

   public double distanceToSqr(double var1, double var3, double var5) {
      double var7 = var1 - this.xPlayer;
      double var9 = var3 - this.yPlayer;
      double var11 = var5 - this.zPlayer;
      return var7 * var7 + var9 * var9 + var11 * var11;
   }

   public Font getFont() {
      return this.font;
   }
}
