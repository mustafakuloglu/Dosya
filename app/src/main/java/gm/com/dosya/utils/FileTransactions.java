package gm.com.dosya.utils;

import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import gm.com.dosya.fragments.DirectoryFragment;


public class FileTransactions {
    DirectoryFragment frag;
    static Storage storage;
    public FileTransactions()
    {
        storage= SimpleStorage.getExternalStorage();
        frag=new DirectoryFragment();
    }
    public static void copyFileOrDirectory(File src, String dstDir) {
        storage= SimpleStorage.getExternalStorage();
        try {
            File dst = new File(dstDir);

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath()+"/"+src.getName();
                    storage.createDirectory(dst1.substring(20));
                    copyFileOrDirectory(new File(src1), dst1);

                }
            } else {
                storage.copy(src, dst.getAbsolutePath().substring(20),src.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
    public static void DeleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();

    }



}