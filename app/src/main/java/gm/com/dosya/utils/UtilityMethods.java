package gm.com.dosya.utils;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.StatFs;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by musta on 19.07.2016.
 */
public class UtilityMethods {
    private static ArrayList<String> mediaPathList;

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public String getRootSubtitle(String path) {
        StatFs stat = new StatFs(path);
        long total = (long) stat.getBlockCount() * (long) stat.getBlockSize();
        long free = (long) stat.getAvailableBlocks()
                * (long) stat.getBlockSize();
        if (total == 0) {
            return "";
        }
        return "Boş: " + formatFileSize(free) + " Toplam: " + formatFileSize(total);
    }

    public static String formatFileSize(long size) {
        if (size < 1024) {
            return String.format("%d B", size);
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0f);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / 1024.0f / 1024.0f);
        } else {
            return String.format("%.1f GB", size / 1024.0f / 1024.0f / 1024.0f);
        }
    }

    public IntentFilter getIntent()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_NOFS);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        return filter;
    }

    public static List<String> getMediaPath()
    {
        mediaPathList=new ArrayList<>();
        mediaPathList.add("/storage/emulated/0/DCIM");
        mediaPathList.add("/storage/emulated/0/Download");
        mediaPathList.add("/storage/emulated/0/CamScanner");
        mediaPathList.add("/storage/emulated/0/Facebook Messenger");
        mediaPathList.add("/storage/emulated/0/Documents");
        mediaPathList.add("/storage/emulated/0/Movies");
        mediaPathList.add("/storage/emulated/0/Music");
        mediaPathList.add("/storage/emulated/0/Notifications");
        mediaPathList.add("/storage/emulated/0/Pictures");
        mediaPathList.add("/storage/emulated/0/Podcasts");
        mediaPathList.add("/storage/emulated/0/Snapchat");
        mediaPathList.add("/storage/emulated/0/Telegram");
        mediaPathList.add("/storage/emulated/0/WhatsApp");

        return mediaPathList;
    }


}
