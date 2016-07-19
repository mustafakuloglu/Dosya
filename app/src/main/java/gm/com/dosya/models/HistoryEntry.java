package gm.com.dosya.models;

import java.io.File;

/**
 * Created by musta on 19.07.2016.
 */

public class HistoryEntry {
   private   int scrollItem, scrollOffset;
   private   File dir;
    private   String title;

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(int scrollOffset) {
        this.scrollOffset = scrollOffset;
    }

    public int getScrollItem() {
        return scrollItem;
    }

    public void setScrollItem(int scrollItem) {
        this.scrollItem = scrollItem;
    }





}

