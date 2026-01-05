package net.minecraft.network.packet;

public class PacketListener {
   public PacketListener() {
   }

   public void handleBlockRegionUpdate(BlockRegionUpdatePacket var1) {
   }

   public void onUnhandledPacket(Packet var1) {
   }

   public void onDisconnect(String var1, Object[] var2) {
   }

   public void handleDisconnect(DisconnectPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleLogin(LoginPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleMovePlayer(MovePlayerPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleChunkTilesUpdate(ChunkTilesUpdatePacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handlePlayerAction(PlayerActionPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleTileUpdate(TileUpdatePacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleChunkVisibility(ChunkVisibilityPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleAddPlayer(AddPlayerPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleMoveEntity(MoveEntityPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleTeleportEntity(TeleportEntityPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleUseItem(UseItemPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleSetCarriedItem(SetCarriedItemPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleRemoveEntity(RemoveEntityPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleAddItemEntity(AddItemEntityPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleTakeItemEntity(TakeItemEntityPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleChat(ChatPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleAddEntity(AddEntityPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleAnimate(AnimatePacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handlePlayerCommand(PlayerCommandPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handlePreLogin(PreLoginPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleAddMob(AddMobPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleSetTime(SetTimePacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleSetSpawn(SetSpawnPositionPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleSetEntityMotion(SetEntityMotionPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleSetEntityData(SetEntityDataPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleRidePacket(SetRidingPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleInteract(InteractPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleEntityEvent(EntityEventPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleSetHealth(SetHealthPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleRespawn(RespawnPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleExplosion(ExplodePacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleContainerOpen(ContainerOpenPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleContainerClose(ContainerClosePacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleContainerClick(ContainerClickPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleContainerSetSlot(ContainerSetSlotPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleContainerContent(ContainerSetContentPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleSignUpdate(SignUpdatePacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleContainerSetData(ContainerSetDataPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleSetEquippedItem(SetEquippedItemPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleContainerAck(ContainerAckPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleAddPainting(AddPaintingPacket var1) {
      this.onUnhandledPacket(var1);
   }

   public void handleTileEvent(TileEventPacket var1) {
      this.onUnhandledPacket(var1);
   }
}
