package schauweg.smoothswapping.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schauweg.smoothswapping.SmoothSwapping;
import schauweg.smoothswapping.SwapStacks;
import schauweg.smoothswapping.SwapUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static schauweg.smoothswapping.SmoothSwapping.clickSwap;
import static schauweg.smoothswapping.SmoothSwapping.clickSwapStack;
import static schauweg.smoothswapping.SwapUtil.getCount;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {


    @Shadow
    @Final
    protected AbstractContainerMenu menu;
    private Screen currentScreen = null;

    @Inject(method = "render", at = @At("HEAD"))
    public void onRender(CallbackInfo cbi) {

        @SuppressWarnings("rawtypes")
        AbstractContainerScreen handledScreen = (AbstractContainerScreen) (Object) this;

        if (handledScreen instanceof CreativeModeInventoryScreen) return;

        Minecraft client = Minecraft.getInstance();

        if (client.player == null || client.player.containerMenu == null) {
            return;
        }

        List<ItemStack> stacks = client.player.containerMenu.getItems();

        Screen screen = client.screen;

        if (clickSwap) {
            clickSwap = false;
            addAll(SmoothSwapping.oldStacks, stacks);
            return;
        }

        if (currentScreen != screen) {
            SmoothSwapping.swaps.clear();
            addAll(SmoothSwapping.oldStacks, stacks);
            currentScreen = screen;
            return;
        }

        Map<Integer, ItemStack> changedStacks = getChangedStacks(SmoothSwapping.oldStacks, stacks);
        if (changedStacks.size() > 1 && !clickSwap) {

            List<SwapStacks> moreStacks = new ArrayList<>();
            List<SwapStacks> lessStacks = new ArrayList<>();

            int totalAmount = 0;
            for (Map.Entry<Integer, ItemStack> stackEntry : changedStacks.entrySet()) {
                int slotID = stackEntry.getKey();
                ItemStack newStack = stackEntry.getValue();
                ItemStack oldStack = SmoothSwapping.oldStacks.get(slotID);

                //whether the stack got more items or less and if slot is output slot
                if (getCount(newStack) > getCount(oldStack) && menu.getSlot(slotID).allowModification(Minecraft.getInstance().player)) {
                    moreStacks.add(new SwapStacks(slotID, oldStack, newStack, getCount(oldStack) - getCount(newStack)));
                    totalAmount += getCount(newStack) - getCount(oldStack);
                } else if (getCount(newStack) < getCount(oldStack) && menu.getSlot(slotID).allowModification(Minecraft.getInstance().player) && SmoothSwapping.clickSwapStack == null) {
                    lessStacks.add(new SwapStacks(slotID, oldStack, newStack, getCount(oldStack) - getCount(newStack)));
                }
            }
            if (SmoothSwapping.clickSwapStack != null){
                lessStacks.clear();
                ItemStack newStack = menu.getSlot(clickSwapStack).getItem();
                ItemStack oldStack = SmoothSwapping.oldStacks.get(clickSwapStack);
                lessStacks.add(new SwapStacks(clickSwapStack, oldStack, newStack, totalAmount));
                SmoothSwapping.clickSwapStack = null;
            }
            SwapUtil.assignSwaps(moreStacks, lessStacks, menu);
        }

        if (!areStacksEqual(SmoothSwapping.oldStacks, stacks)) {
            addAll(SmoothSwapping.oldStacks, stacks);
        }
    }

    private Map<Integer, ItemStack> getChangedStacks(List<ItemStack> oldStacks, List<ItemStack> newStacks) {
        Map<Integer, ItemStack> changedStacks = new HashMap<>();
        for (int slotID = 0; slotID < oldStacks.size(); slotID++) {
            ItemStack newStack = newStacks.get(slotID);
            ItemStack oldStack = oldStacks.get(slotID);
            if (!ItemStack.isSame(oldStack, newStack)) {
                changedStacks.put(slotID, newStack.copy());
            }
        }
        return changedStacks;
    }

    private boolean areStacksEqual(List<ItemStack> oldStacks, List<ItemStack> newStacks) {
        if (oldStacks == null || newStacks == null || (oldStacks.size() != newStacks.size())) {
            return false;
        } else {
            for (int slotID = 0; slotID < oldStacks.size(); slotID++) {
                ItemStack newStack = newStacks.get(slotID);
                ItemStack oldStack = oldStacks.get(slotID);
                if (!ItemStack.isSame(oldStack, newStack)) {
                    return false;
                }
            }
        }
        return true;
    }


    private void addAll(List<ItemStack> oldStacks, List<ItemStack> newStacks) {
        oldStacks.clear();
        newStacks.stream().map(ItemStack::copy).forEach(oldStacks::add);
    }
}
