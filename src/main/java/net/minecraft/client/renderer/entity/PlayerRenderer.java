package net.minecraft.client.renderer.entity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.opengl.GL11;
import util.Mth;

public class PlayerRenderer extends MobRenderer<Player> {
   private HumanoidModel humanoidModel;
   private HumanoidModel armorParts1;
   private HumanoidModel armorParts2;
   private static final String[] MATERIAL_NAMES = new String[]{"cloth", "chain", "iron", "diamond", "gold"};

   public PlayerRenderer() {
      super(new HumanoidModel(0.0F), 0.5F);
      this.humanoidModel = (HumanoidModel)this.model;
      this.armorParts1 = new HumanoidModel(1.0F);
      this.armorParts2 = new HumanoidModel(0.5F);
   }

   protected boolean prepareArmor(Player player, int layer, float a) {
      ItemInstance itemInstance = player.inventory.getArmor(3 - layer);
      if (itemInstance != null) {
         Item item = itemInstance.getItem();
         if (item instanceof ArmorItem) {
            ArmorItem armorItem = (ArmorItem)item;
            this.bindTexture("/armor/" + MATERIAL_NAMES[armorItem.icon] + "_" + (layer == 2 ? 2 : 1) + ".png");
            HumanoidModel armor = layer == 2 ? this.armorParts2 : this.armorParts1;
            armor.head.visible = layer == 0;
            armor.hair.visible = layer == 0;
            armor.body.visible = layer == 1 || layer == 2;
            armor.arm0.visible = layer == 1;
            armor.arm1.visible = layer == 1;
            armor.leg0.visible = layer == 2 || layer == 3;
            armor.leg1.visible = layer == 2 || layer == 3;
            this.setArmor(armor);
            return true;
         }
      }

      return false;
   }

   public void render(Player mob, double x, double y, double z, float rot, float a) {
      ItemInstance item = mob.inventory.getSelected();
      this.armorParts1.holdingRightHand = this.armorParts2.holdingRightHand = this.humanoidModel.holdingRightHand = item != null;
      this.armorParts1.sneaking = this.armorParts2.sneaking = this.humanoidModel.sneaking = mob.isSneaking();
      double yp = y - (double)mob.heightOffset;
      if (mob.isSneaking()) {
         yp -= (double)0.125F;
      }

      super.render(mob, x, yp, z, rot, a);
      this.armorParts1.sneaking = this.armorParts2.sneaking = this.humanoidModel.sneaking = false;
      this.armorParts1.holdingRightHand = this.armorParts2.holdingRightHand = this.humanoidModel.holdingRightHand = false;
      float size = 1.6F;
      float s = 0.016666668F * size;
      float dist = mob.distanceTo(this.entityRenderDispatcher.player);
      float maxDist = (float)(mob.isSneaking() ? 32 : 64);
      if (dist < maxDist) {
         s = (float)((double)s * (Math.sqrt((double)dist) / (double)2.0F));
         Font font = this.getFont();
         GL11.glPushMatrix();
         GL11.glTranslatef((float)x + 0.0F, (float)y + 2.3F, (float)z);
         GL11.glNormal3f(0.0F, 1.0F, 0.0F);
         GL11.glRotatef(-this.entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(this.entityRenderDispatcher.playerRotX, 1.0F, 0.0F, 0.0F);
         GL11.glScalef(-s, -s, s);
         String msg = mob.name;
         GL11.glDisable(2896);
         if (!mob.isSneaking()) {
            GL11.glDepthMask(false);
            GL11.glDisable(2929);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            Tesselator t = Tesselator.instance;
            int offs = 0;
            if (mob.name.equals("deadmau5")) {
               offs = -10;
            }

            GL11.glDisable(3553);
            t.begin();
            int w = font.width(msg) / 2;
            t.color(0.0F, 0.0F, 0.0F, 0.25F);
            t.vertex((double)(-w - 1), (double)(-1 + offs), (double)0.0F);
            t.vertex((double)(-w - 1), (double)(8 + offs), (double)0.0F);
            t.vertex((double)(w + 1), (double)(8 + offs), (double)0.0F);
            t.vertex((double)(w + 1), (double)(-1 + offs), (double)0.0F);
            t.end();
            GL11.glEnable(3553);
            font.draw(msg, -font.width(msg) / 2, offs, 553648127);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            font.draw(msg, -font.width(msg) / 2, offs, -1);
            GL11.glEnable(2896);
            GL11.glDisable(3042);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
         } else {
            GL11.glTranslatef(0.0F, 0.25F / s, 0.0F);
            GL11.glDepthMask(false);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            Tesselator t = Tesselator.instance;
            GL11.glDisable(3553);
            t.begin();
            int w = font.width(msg) / 2;
            t.color(0.0F, 0.0F, 0.0F, 0.25F);
            t.vertex((double)(-w - 1), (double)-1.0F, (double)0.0F);
            t.vertex((double)(-w - 1), (double)8.0F, (double)0.0F);
            t.vertex((double)(w + 1), (double)8.0F, (double)0.0F);
            t.vertex((double)(w + 1), (double)-1.0F, (double)0.0F);
            t.end();
            GL11.glEnable(3553);
            GL11.glDepthMask(true);
            font.draw(msg, -font.width(msg) / 2, 0, 553648127);
            GL11.glEnable(2896);
            GL11.glDisable(3042);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
         }
      }

   }

   protected void additionalRendering(Player mob, float a) {
      ItemInstance headGear = mob.inventory.getArmor(3);
      if (headGear != null && headGear.getItem().id < 256) {
         GL11.glPushMatrix();
         this.humanoidModel.head.translateTo(0.0625F);
         if (TileRenderer.canRender(Tile.tiles[headGear.id].getRenderShape())) {
            float s = 0.625F;
            GL11.glTranslatef(0.0F, -0.25F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(s, -s, s);
         }

         this.entityRenderDispatcher.itemInHandRenderer.renderItem(headGear);
         GL11.glPopMatrix();
      }

      if (mob.name.equals("deadmau5") && this.bindTexture(mob.customTextureUrl, (String)null)) {
         for(int i = 0; i < 2; ++i) {
            float yr = mob.yRotO + (mob.yRot - mob.yRotO) * a - (mob.yBodyRotO + (mob.yBodyRot - mob.yBodyRotO) * a);
            float xr = mob.xRotO + (mob.xRot - mob.xRotO) * a;
            GL11.glPushMatrix();
            GL11.glRotatef(yr, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(xr, 1.0F, 0.0F, 0.0F);
            GL11.glTranslatef(0.375F * (float)(i * 2 - 1), 0.0F, 0.0F);
            GL11.glTranslatef(0.0F, -0.375F, 0.0F);
            GL11.glRotatef(-xr, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(-yr, 0.0F, 1.0F, 0.0F);
            float s = 1.3333334F;
            GL11.glScalef(s, s, s);
            this.humanoidModel.renderEars(0.0625F);
            GL11.glPopMatrix();
         }
      }

      if (this.bindTexture(mob.cloakTexture, (String)null)) {
         GL11.glPushMatrix();
         GL11.glTranslatef(0.0F, 0.0F, 0.125F);
         double xd = mob.xCloakO + (mob.xCloak - mob.xCloakO) * (double)a - (mob.xo + (mob.x - mob.xo) * (double)a);
         double yd = mob.yCloakO + (mob.yCloak - mob.yCloakO) * (double)a - (mob.yo + (mob.y - mob.yo) * (double)a);
         double zd = mob.zCloakO + (mob.zCloak - mob.zCloakO) * (double)a - (mob.zo + (mob.z - mob.zo) * (double)a);
         float yr = mob.yBodyRotO + (mob.yBodyRot - mob.yBodyRotO) * a;
         double xa = (double)Mth.sin(yr * (float)Math.PI / 180.0F);
         double za = (double)(-Mth.cos(yr * (float)Math.PI / 180.0F));
         float flap = (float)yd * 10.0F;
         if (flap < -6.0F) {
            flap = -6.0F;
         }

         if (flap > 32.0F) {
            flap = 32.0F;
         }

         float lean = (float)(xd * xa + zd * za) * 100.0F;
         float lean2 = (float)(xd * za - zd * xa) * 100.0F;
         if (lean < 0.0F) {
            lean = 0.0F;
         }

         float pow = mob.oBob + (mob.bob - mob.oBob) * a;
         flap += Mth.sin((mob.walkDistO + (mob.walkDist - mob.walkDistO) * a) * 6.0F) * 32.0F * pow;
         GL11.glRotatef(6.0F + lean / 2.0F + flap, 1.0F, 0.0F, 0.0F);
         GL11.glRotatef(lean2 / 2.0F, 0.0F, 0.0F, 1.0F);
         GL11.glRotatef(-lean2 / 2.0F, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
         this.humanoidModel.renderCloak(0.0625F);
         GL11.glPopMatrix();
      }

      ItemInstance item = mob.inventory.getSelected();
      if (item != null) {
         GL11.glPushMatrix();
         this.humanoidModel.arm0.translateTo(0.0625F);
         GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);
         if (mob.fishing != null) {
            item = new ItemInstance(Item.stick);
         }

         if (item.id < 256 && TileRenderer.canRender(Tile.tiles[item.id].getRenderShape())) {
            float s = 0.5F;
            GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
            s *= 0.75F;
            GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(s, -s, s);
         } else if (Item.items[item.id].isHandEquipped()) {
            float s = 0.625F;
            if (Item.items[item.id].isMirroredArt()) {
               GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
               GL11.glTranslatef(0.0F, -0.125F, 0.0F);
            }

            GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
            GL11.glScalef(s, -s, s);
            GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
         } else {
            float s = 0.375F;
            GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
            GL11.glScalef(s, s, s);
            GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
         }

         this.entityRenderDispatcher.itemInHandRenderer.renderItem(item);
         GL11.glPopMatrix();
      }

   }

   protected void scale(Player player, float a) {
      float s = 0.9375F;
      GL11.glScalef(s, s, s);
   }

   public void renderHand() {
      this.humanoidModel.attackTime = 0.0F;
      this.humanoidModel.setupAnim(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
      this.humanoidModel.arm0.render(0.0625F);
   }
}
