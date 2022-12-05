package schauweg.smoothswapping.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schauweg.smoothswapping.SmoothSwapping;
import schauweg.smoothswapping.SwapUtil;
import schauweg.smoothswapping.config.Config;
import schauweg.smoothswapping.swaps.InventorySwap;

import java.util.List;

import static schauweg.smoothswapping.SwapUtil.getSlotIndex;
import static schauweg.smoothswapping.SwapUtil.hasArrived;
import static schauweg.smoothswapping.SwapUtil.setRenderToTrue;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {


	@Shadow
	public float blitOffset;

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void onRenderItem(ItemStack stack, ItemTransforms.TransformType renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo cbi) {
		if (blitOffset < 100) return; //fix so hotbar won't be affected

		if (renderMode == ItemTransforms.TransformType.GUI) {
			Minecraft client = Minecraft.getInstance();

			if (client.player == null)
				return;


			doSwap(client, stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model, blitOffset, cbi);
		}
	}

	@Inject(method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "HEAD"), cancellable = true)
	private void onRenderOverlay(Font renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo cbi) {
		if (blitOffset < 100) return; //fix so hotbar won't be affected

		doOverlayRender((ItemRenderer) (Object) this, stack, renderer, x, y, cbi);
	}

	private void doSwap(Minecraft client, ItemStack stack, ItemTransforms.TransformType renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, float blitOffset, CallbackInfo ci) {

		float lastFrameDuration = client.getFrameTime();
		ItemRenderer renderer = client.getItemRenderer();
		int index = getSlotIndex(stack);

		// TODO proper fix (https://github.com/Schauweg/Smooth-Swapping/issues/4)
		try {
			if (SmoothSwapping.swaps.containsKey(index)) {
				//Get all swaps for one slot
				List<InventorySwap> swapList = SmoothSwapping.swaps.get(index);
				
				boolean renderDestinationSlot = true;
				
				//render all swaps for one slot
				for (int i = 0; i < swapList.size(); i++) {
					InventorySwap swap = swapList.get(i);

					swap.setRenderDestinationSlot(swap.isChecked());

					if (!swap.renderDestinationSlot()) {
						renderDestinationSlot = false;
					}
					
					//render swap
					renderSwap(renderer, swap, lastFrameDuration, stack.copy(), leftHanded, vertexConsumers, light, overlay, model, blitOffset);
					
					if (hasArrived(swap)) {
						setRenderToTrue(swapList);
						swapList.remove(swap);
					}
				}
				
				//whether the destination slot should be rendered
				if (renderDestinationSlot) {
					renderer.render(stack.copy(), renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
				}
				if (swapList.size() == 0)
					SmoothSwapping.swaps.remove(index);
				
				ci.cancel();
			}
		} catch (StackOverflowError e) {
			SmoothSwapping.LOGGER.warn("StackOverflowError just happened while trying to render an item swap. This message is a reminder to properly fix an issue #4 described on SmoothSwapping's GitHub");
			SmoothSwapping.swaps.remove(index);
		}
	}

	private void doOverlayRender(ItemRenderer itemRenderer, ItemStack stack, Font renderer, int x, int y, CallbackInfo cbi) {
		int index = getSlotIndex(stack);

		// TODO proper fix (https://github.com/Schauweg/Smooth-Swapping/issues/4)
		try {
			if (SmoothSwapping.swaps.containsKey(index)) {
				List<InventorySwap> swapList = SmoothSwapping.swaps.get(index);
				int stackCount = stack.getCount();
				boolean renderToSlot = true;

				for (InventorySwap swap : swapList) {

					if (!ItemStack.matches(stack, swap.getSwapItem())) {
						SmoothSwapping.swaps.remove(index);
						return;
					}

					stackCount -= swap.getAmount();
					if (!swap.renderDestinationSlot()) {
						renderToSlot = false;
					}

					if (swap.getAmount() > 1) {
						String amount = String.valueOf(swap.getAmount());
						MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

						PoseStack textStack = new PoseStack();
						textStack.pushPose();
						textStack.translate(-swap.getX(), -swap.getY(), blitOffset + 250);
						renderer.drawInBatch(amount, (float) (x + 19 - 2 - renderer.width(amount)), (float) (y + 6 + 3), 16777215, true, textStack.last().pose(), immediate, false, 0, 15728880);
						immediate.endBatch();
						textStack.popPose();
					}
            
                }
        
                if (renderToSlot && stackCount > 1) {
					itemRenderer.renderGuiItemDecorations(renderer, stack.copy(), x, y, String.valueOf(stackCount));
				}
				cbi.cancel();
			}
		} catch (StackOverflowError e) {
			SmoothSwapping.LOGGER.warn("StackOverflowError just happened while trying to render an overlay. This message is a reminder to properly fix an issue #4 described on SmoothSwapping's GitHub");
			SmoothSwapping.swaps.remove(index);
		}
	}

	private static void renderSwap(ItemRenderer itemRenderer, InventorySwap swap, float lastFrameDuration, ItemStack stack, boolean leftHanded, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, float blitOffset) {
		PoseStack matrices = new PoseStack();
		matrices.pushPose();

		double x = swap.getX();
		double y = swap.getY();
		double angle = swap.getAngle();

		float progress = SwapUtil.map((float) Math.hypot(x, y), 0, (float) swap.getDistance(), 0.95f, 0.05f);

		matrices.translate(-x / 16, y / 16, blitOffset - 145); //blitOffset 5

		itemRenderer.render(stack, ItemTransforms.TransformType.GUI, leftHanded, matrices, vertexConsumers, light, overlay, model);

		float ease = SwapUtil.getEase(progress);

		double speed = swap.getDistance() / 10 * ease * Config.CLIENT.getAnimationSpeedFormatted();
		
		swap.setX(x + lastFrameDuration * speed * Math.cos(angle));
		swap.setY(y + lastFrameDuration * speed * Math.sin(angle));

		matrices.popPose();
	}
	
}
