package com.tct.gallery3d.app.model;

public class TabItem {
    private int normalIconId = 0;

    private int selectedIconId = 0;

    private int titleStringId = 0;

    private boolean isSelected = false;

    private int normalTitleColorId = 0;

    private int selectedTitleColorId = 0;

    public TabItem() {
    }

    public TabItem(int normalIconId, int selectedIconId, int titleStringId,
            int normalTitleColorId, int selectedTitleColorId, boolean isSelected) {
        this.normalIconId = normalIconId;
        this.selectedIconId = selectedIconId;
        this.titleStringId = titleStringId;
        this.normalTitleColorId = normalTitleColorId;
        this.selectedTitleColorId = selectedTitleColorId;
        this.isSelected = isSelected;
    }

    public int getNormalTitleColorId() {
        return normalTitleColorId;
    }

    public void setNormalTitleColorId(int normalTitleColorId) {
        this.normalTitleColorId = normalTitleColorId;
    }

    public int getSelectedTitleColorId() {
        return selectedTitleColorId;
    }

    public void setSelectedTitleColorId(int selectedTitleColorId) {
        this.selectedTitleColorId = selectedTitleColorId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getNormalIconId() {
        return normalIconId;
    }

    public void setNormalIconId(int normalIconId) {
        this.normalIconId = normalIconId;
    }

    public int getSelectedIconId() {
        return selectedIconId;
    }

    public void setSelectedIconId(int selectedIconId) {
        this.selectedIconId = selectedIconId;
    }

    public int getTitleStringId() {
        return titleStringId;
    }

    public void setTitleStringId(int titleStringId) {
        this.titleStringId = titleStringId;
    }

    @Override
    public String toString() {
        return "TabItem [normalIconId=" + normalIconId + ", selectedIconId="
                + selectedIconId + ", titleStringId=" + titleStringId
                + ", isSelected=" + isSelected + ", normalTitleColorId="
                + normalTitleColorId + ", selectedTitleColorId="
                + selectedTitleColorId + "]";
    }
}
