package gm.com.dosya.models;

import java.io.File;

/**
 * Created by musta on 11.07.2016.
 */

public class ListItem {

    private String title;
    private String subtitle = "";
    private String ext = "";
    private String thumb;
    private File file;
    private boolean visible =false;
    public boolean select = false;
    private boolean check =false;
    private int icon;

    public boolean getVisible() {return visible;}

    public void setVisible(boolean visible) {this.visible = visible;}

    public boolean getCheck() {return check;}

    public void setCheck(boolean check) {this.check = check;}

    public boolean getSelect() {return select;}

    public void setSelect(boolean select) {this.select=select;}

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }



}
