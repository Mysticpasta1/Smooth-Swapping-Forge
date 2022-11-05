package schauweg.smoothswapping.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schauweg.smoothswapping.SmoothSwapping;

import java.util.Map;

import static schauweg.smoothswapping.SwapUtil.addInventorySwap;

@Mixin(ServerboundContainerClickPacket.class)
public class ServerboundContainerClickPacketMixin {

    @Shadow
    @Final
    private ClickType clickType;

    @Shadow
    @Final
    private Int2ObjectMap<ItemStack> changedSlots;

    //id of slot that got clicked/hovered over
    @Shadow
    @Final
    private int slotNum;

    @Inject(method = "<init>(IIIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/item/ItemStack;Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;)V", at = @At("TAIL"))
    public void onInit(CallbackInfo cbi) {
        //remove swap when stack gets moved before it arrived
        SmoothSwapping.swaps.remove(slotNum);

        if ((clickType == ClickType.QUICK_MOVE || clickType == ClickType.SWAP) && changedSlots.size() > 1 && Minecraft.getInstance().screen instanceof AbstractContainerScreen) {
            assert Minecraft.getInstance().player != null;

            LocalPlayer player = Minecraft.getInstance().player;
            AbstractContainerMenu screenHandler = player.containerMenu;
            Slot mouseHoverSlot = screenHandler.getSlot(slotNum);

            if (clickType == ClickType.QUICK_MOVE && !mouseHoverSlot.allowModification(player)) {

                ItemStack newMouseStack = changedSlots.get(slotNum);
                ItemStack oldMouseStack = SmoothSwapping.oldStacks.get(slotNum);

                //only if new items are less or equal (crafting table output for example)
                if (newMouseStack.getCount() - oldMouseStack.getCount() <= 0) {
                    SmoothSwapping.clickSwapStack = slotNum;

                }
            } else if (clickType == ClickType.SWAP) {
                SmoothSwapping.clickSwap = true;
                for (Map.Entry<Integer, ItemStack> stackEntry : changedSlots.int2ObjectEntrySet()) {
                    int destinationSlotID = stackEntry.getKey();
                    if (destinationSlotID != slotNum) {
                        Slot destinationSlot = screenHandler.getSlot(destinationSlotID);
                        SmoothSwapping.swaps.remove(destinationSlotID);
                        //if mouse slot is output slot(crafting slot for example) and old destination stack is empty
                        if (!mouseHoverSlot.allowModification(player) && destinationSlot.allowModification(player) && SmoothSwapping.oldStacks.get(destinationSlotID).isEmpty()) {
                            addInventorySwap(destinationSlotID, mouseHoverSlot, destinationSlot, false, destinationSlot.getItem().getCount());
                        } else if (mouseHoverSlot.allowModification(player) && destinationSlot.allowModification(player)) {
                            if (destinationSlot.hasItem()) {
                                addInventorySwap(destinationSlotID, mouseHoverSlot, destinationSlot, false, destinationSlot.getItem().getCount());
                            }
                            if (mouseHoverSlot.hasItem()) {
                                addInventorySwap(slotNum, destinationSlot, mouseHoverSlot, false, mouseHoverSlot.getItem().getCount());
                            }
                        }
                    }
                }
            }
        }
    }
}
