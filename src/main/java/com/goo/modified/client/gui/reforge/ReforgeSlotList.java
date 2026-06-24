package com.goo.modified.client.gui.reforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class ReforgeSlotList extends ObjectSelectionList<ReforgeSlotRowWidget> {
    public ReforgeSlotList(Minecraft minecraft, int width, int height, int top, int itemHeight) {
        super(minecraft, width, height, top, itemHeight);
    }


    @Nullable
    @Override
    public ReforgeSlotRowWidget getEntryAtPosition(double mouseX, double mouseY) {
        int halfWidth = this.getRowWidth() / 2;
        int centerX = this.getX() + this.width / 2;
        int leftBound = centerX - halfWidth;
        int rightBound = centerX + halfWidth;

        // remove the default vanilla '- 4' buffer offset so the click map lines up
        int relativeY = Mth.floor(mouseY - (double)this.getY()) - this.headerHeight + (int)this.getScrollAmount();
        int rowIndex = relativeY / this.itemHeight;

        return mouseX >= (double)leftBound && mouseX <= (double)rightBound && rowIndex >= 0 && relativeY >= 0 && rowIndex < this.getItemCount()
                ? this.children().get(rowIndex)
                : null;
    }

    @Override
    public int addEntry(ReforgeSlotRowWidget entry) {
        return super.addEntry(entry);
    }

    @Override
    public int getRowLeft() {
        return getX();
    }

    @Override
    protected int getRowTop(int index) {
        return this.getY() - (int) this.getScrollAmount() + index * this.itemHeight + this.headerHeight;
    }

    @Override
    public boolean scrollbarVisible() {
        return false;
    }

    @Override
    protected void renderSelection(GuiGraphics guiGraphics, int top, int width, int height, int outerColor, int innerColor) {
    }

    @Override
    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.height));
    }

    @Override
    public int getHeight() {
        return super.getHeight();
    }

    @Override
    public int getRowWidth() {
        return getWidth();
    }

    @Override
    public void clearEntries() {
        super.clearEntries();
    }

    @Override
    public void setRenderHeader(boolean renderHeader, int headerHeight) {
        super.setRenderHeader(renderHeader, headerHeight);
    }

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {

    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {

    }

}