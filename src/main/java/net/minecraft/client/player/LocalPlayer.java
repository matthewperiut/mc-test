package net.minecraft.client.player;

import com.mojang.nbt.CompoundTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.inventory.ContainerScreen;
import net.minecraft.client.gui.inventory.CraftingScreen;
import net.minecraft.client.gui.inventory.FurnaceScreen;
import net.minecraft.client.gui.inventory.TextEditScreen;
import net.minecraft.client.gui.inventory.TrapScreen;
import net.minecraft.client.particle.TakeAnimationParticle;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.entity.DispenserTileEntity;
import net.minecraft.world.level.tile.entity.FurnaceTileEntity;
import net.minecraft.world.level.tile.entity.SignTileEntity;

public class LocalPlayer extends Player {
   public Input input;
   protected Minecraft minecraft;
   public int changingDimensionDelay = 20;
   private boolean isInsidePortal = false;
   public float portalTime;
   public float oPortalTime;

   public LocalPlayer(Minecraft minecraft, Level level, User user, int dimension) {
      super(level);
      this.minecraft = minecraft;
      this.dimension = dimension;
      if (user != null && user.name != null && user.name.length() > 0) {
         this.customTextureUrl = "http://s3.amazonaws.com/MinecraftSkins/" + user.name + ".png";
         System.out.println("Loading texture " + this.customTextureUrl);
      }

      this.name = user.name;
   }

   public void updateAi() {
      super.updateAi();
      this.xxa = this.input.xa;
      this.yya = this.input.ya;
      this.jumping = this.input.jumping;
   }

   public void aiStep() {
      this.oPortalTime = this.portalTime;
      if (this.isInsidePortal) {
         if (this.portalTime == 0.0F) {
            this.minecraft.soundEngine.playUI("portal.trigger", 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
         }

         this.portalTime += 0.0125F;
         if (this.portalTime >= 1.0F) {
            this.portalTime = 1.0F;
            this.changingDimensionDelay = 10;
            this.minecraft.soundEngine.playUI("portal.travel", 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
            this.minecraft.toggleDimension();
         }

         this.isInsidePortal = false;
      } else {
         if (this.portalTime > 0.0F) {
            this.portalTime -= 0.05F;
         }

         if (this.portalTime < 0.0F) {
            this.portalTime = 0.0F;
         }
      }

      if (this.changingDimensionDelay > 0) {
         --this.changingDimensionDelay;
      }

      this.input.tick(this);
      if (this.input.sneaking && this.ySlideOffset < 0.2F) {
         this.ySlideOffset = 0.2F;
      }

      super.aiStep();
   }

   public void releaseAllKeys() {
      this.input.releaseAllKeys();
   }

   public void setKey(int eventKey, boolean eventKeyState) {
      this.input.setKey(eventKey, eventKeyState);
   }

   public void addAdditonalSaveData(CompoundTag entityTag) {
      super.addAdditonalSaveData(entityTag);
      entityTag.putInt("Score", this.score);
   }

   public void readAdditionalSaveData(CompoundTag entityTag) {
      super.readAdditionalSaveData(entityTag);
      this.score = entityTag.getInt("Score");
   }

   public void closeContainer() {
      super.closeContainer();
      this.minecraft.setScreen((Screen)null);
   }

   public void openTextEdit(SignTileEntity sign) {
      this.minecraft.setScreen(new TextEditScreen(sign));
   }

   public void openContainer(Container container) {
      this.minecraft.setScreen(new ContainerScreen(this.inventory, container));
   }

   public void startCrafting(int x, int y, int z) {
      this.minecraft.setScreen(new CraftingScreen(this.inventory, this.level, x, y, z));
   }

   public void openFurnace(FurnaceTileEntity furnace) {
      this.minecraft.setScreen(new FurnaceScreen(this.inventory, furnace));
   }

   public void openTrap(DispenserTileEntity trap) {
      this.minecraft.setScreen(new TrapScreen(this.inventory, trap));
   }

   public void take(Entity e, int orgCount) {
      this.minecraft.particleEngine.add(new TakeAnimationParticle(this.minecraft.level, e, this, -0.5F));
   }

   public int getArmor() {
      return this.inventory.getArmorValue();
   }

   public void chat(String message) {
   }

   public void prepareForTick() {
   }

   public boolean isSneaking() {
      return this.input.sneaking;
   }

   public void handleInsidePortal() {
      if (this.changingDimensionDelay > 0) {
         this.changingDimensionDelay = 10;
      } else {
         this.isInsidePortal = true;
      }
   }

   public void hurtTo(int newHealth) {
      int dmg = this.health - newHealth;
      if (dmg <= 0) {
         this.health = newHealth;
      } else {
         this.lastHurt = dmg;
         this.lastHealth = this.health;
         this.invulnerableTime = this.invulnerableDuration;
         this.actuallyHurt(dmg);
         this.hurtTime = this.hurtDuration = 10;
      }

   }

   public void respawn() {
      this.minecraft.respawnPlayer();
   }
}
