package gm.com.dosya.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by musta on 19.07.2016.
 */
public class FileTransactions {

    public static void copyFileOrDirectory(String srcDir, String dstDir) {

        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);

                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void deleteFileOrDirectory(String deletepath) {

        try {
            File src = new File(deletepath);


            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    deleteFileOrDirectory(src1);

                }
            } else {
                deleteFile(src.getPath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static ArrayList<File> sortDirectory(String deletepath)
    {
        ArrayList<File> directories=new ArrayList<>();
        try {
            File src = new File(deletepath);


            if (src.isDirectory()) {
                directories.add(src);
                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    sortDirectory(src1);
       }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
return directories;
    }
    public static void deleteDirectory (ArrayList<File> directories)
    {
        int size=directories.size();
        for(int count=1;count<=size;count++)
        {File deleting = new File(directories.get(size-count).getPath());
            deleting.delete();
        }
    }
    private static void deleteFile(String path)
    {
        File sil = new File(path);
        boolean deleted = sil.delete();
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

}
