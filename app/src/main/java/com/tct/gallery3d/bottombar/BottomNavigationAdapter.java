package com.tct.gallery3d.bottombar;

import android.app.Activity;
import android.support.annotation.ColorInt;
import android.support.annotation.MenuRes;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;


public class BottomNavigationAdapter {

    private Menu mMenu;
    private List<BottomNavigationItem> navigationItems;

    /**
     * Constructor
     *
     * @param activity
     * @param menuRes
     */
    public BottomNavigationAdapter(Activity activity, @MenuRes int menuRes) {
        PopupMenu popupMenu = new PopupMenu(activity, null);
        mMenu = popupMenu.getMenu();
        activity.getMenuInflater().inflate(menuRes, mMenu);
    }

    /**
     * Setup bottom navigation
     *
     * @param bottomNavigation BottomNavigation: Bottom navigation
     */
    public void setupWithBottomNavigation(com.tct.gallery3d.bottombar.BottomNavigation bottomNavigation) {
        setupWithBottomNavigation(bottomNavigation, null);
    }

    /**
     * Setup bottom navigation (with colors)
     *
     * @param bottomNavigation BottomNavigation: Bottom navigation
     * @param colors             int[]: Colors of the item
     */
    public void setupWithBottomNavigation(com.tct.gallery3d.bottombar.BottomNavigation bottomNavigation, @ColorInt int[] colors) {
        if (navigationItems == null) {
            navigationItems = new ArrayList<>();
        } else {
            navigationItems.clear();
        }

        if (mMenu != null) {
            for (int i = 0; i < mMenu.size(); i++) {
                MenuItem item = mMenu.getItem(i);
                if (colors != null && colors.length >= mMenu.size() && colors[i] != 0) {
                    BottomNavigationItem navigationItem = new BottomNavigationItem(String.valueOf(item.getTitle()), item.getIcon(), colors[i]);
                    navigationItems.add(navigationItem);
                } else {
                    BottomNavigationItem navigationItem = new BottomNavigationItem(String.valueOf(item.getTitle()), item.getIcon());
                    navigationItems.add(navigationItem);
                }
            }
            bottomNavigation.removeAllItems();
            bottomNavigation.addItems(navigationItems);
        }
    }

    /**
     * Get Menu Item
     *
     * @param index
     * @return
     */
    public MenuItem getMenuItem(int index) {
        return mMenu.getItem(index);
    }

    /**
     * Get Navigation Item
     *
     * @param index
     * @return
     */
    public BottomNavigationItem getNavigationItem(int index) {
        return navigationItems.get(index);
    }

    /**
     * Get position by menu id
     *
     * @param menuId
     * @return
     */
    public Integer getPositionByMenuId(int menuId) {
        for (int i = 0; i < mMenu.size(); i++) {
            if (mMenu.getItem(i).getItemId() == menuId)
                return i;
        }
        return null;
    }
}