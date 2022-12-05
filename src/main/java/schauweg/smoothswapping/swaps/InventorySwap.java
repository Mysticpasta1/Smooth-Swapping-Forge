package schauweg.smoothswapping.swaps;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InventorySwap {

    private double x, y;
    private final double distance, startX, startY, angle;
    private boolean renderDestinationSlot, checked;
    private final int amount;

    private final ItemStack swapItem;


    public InventorySwap(Slot fromSlot, Slot toSlot, boolean checked, int amount) {
        this.x = toSlot.x - fromSlot.x;
        this.y = toSlot.y - fromSlot.y;
        this.startX = toSlot.x - fromSlot.x;
        this.startY = toSlot.y - fromSlot.y;
        this.angle = (float) (Math.atan2(y, x) + Math.PI);
        this.distance = Math.hypot(x, y);
        this.renderDestinationSlot = false;
        this.checked = checked;
        this.amount = amount;
        this.swapItem = toSlot.getItem();
    }

    public double getAngle() {
        return angle;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getDistance() {
        return distance;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public boolean renderDestinationSlot() {
        return renderDestinationSlot;
    }

    public void setRenderDestinationSlot(boolean renderDestinationSlot) {
        this.renderDestinationSlot = renderDestinationSlot;
    }

    public boolean isChecked() {
        return checked;
    }

    public ItemStack getSwapItem() {
        return swapItem;
    }

    public int getAmount() {
        return amount;
    }
}
