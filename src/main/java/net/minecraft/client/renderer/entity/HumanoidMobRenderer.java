package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.tile.Tile;
import org.lwjgl.opengl.GL11;

public class HumanoidMobRenderer<T extends Mob> extends MobRenderer<T> {
   protected HumanoidModel humanoidModel;

   public HumanoidMobRenderer(HumanoidModel humanoidModel, float shadow) {
      super(humanoidModel, shadow);
      this.humanoidModel = humanoidModel;
   }

   protected void additionalRendering(Mob mob, float a) {
      ItemInstance item = mob.getCarriedItem();
      if (item != null) {
         GL11.glPushMatrix();
         this.humanoidModel.arm0.translateTo(0.0625F);
         GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);
         if (item.id < 256 && TileRenderer.canRender(Tile.tiles[item.id].getRenderShape())) {
            float s = 0.5F;
            GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
            s *= 0.75F;
            GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(s, -s, s);
         } else if (Item.items[item.id].isHandEquipped()) {
            float s = 0.625F;
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
}
