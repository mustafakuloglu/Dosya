package gm.com.dosya.utils;

import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gm.com.dosya.fragments.DirectoryFragment;


public class FileTransactions {
    DirectoryFragment frag;
    static Storage storage;
    private static ArrayList<String> mediaPathList;
    private static ArrayList<File> listPic;
    private static ArrayList<File> listSound;
    private static ArrayList<File> listVideo;
    private static ArrayList<File> listDownload;
    private static ArrayList<File> listDoc;
    private static ArrayList<File> listCompress;
    private DirectoryFragment directoryFragment;
    public FileTransactions()
    {
        storage= SimpleStorage.getExternalStorage();
        frag=new DirectoryFragment();
        directoryFragment=new DirectoryFragment();
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


    public static void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();

    }
    public static void move(File file,String directoryname,String filename)
    {
        storage= SimpleStorage.getExternalStorage();
        storage.move(file,directoryname,filename);

    }
    public static void moveFileOrDirectory(File src, String dstDir) {
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
                    moveFileOrDirectory(new File(src1), dst1);

                }

            } else {
                storage.move(src, dst.getAbsolutePath().substring(20),src.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private ArrayList<String> getItemPtah(ArrayList<String> paths)
    {
        ArrayList<String> son=new ArrayList<>();

return son;
    }
    public static void catagoryMedia() {
        mediaPathList = (ArrayList<String>) UtilityMethods.getMediaPath();
        File media;

        for(String path:mediaPathList)
        {
            media=new File(path);
            if(media.getPath().endsWith(".jpg") ||media.getPath().endsWith(".png") ||media.getPath().endsWith(".jpeg" )||media.getPath().endsWith(".gif" )||media.getPath().endsWith(".raw" ))
            {
                listPic.add(media);
            }
            if(media.getPath().endsWith(".zip") ||media.getPath().endsWith(".rar") ||media.getPath().endsWith(".7z" )||media.getPath().endsWith(".tar" )||media.getPath().endsWith(".apk" ))
            {
                listCompress.add(media);
            }
            if(media.getPath().endsWith(".ppt") ||media.getPath().endsWith(".pptx") ||media.getPath().endsWith(".doc" )||media.getPath().endsWith(".docx" )||media.getPath().endsWith(".xls" )||media.getPath().endsWith(".xlsx" )||media.getPath().endsWith(".txt" )||media.getPath().endsWith(".opt" ))
            {
                listDoc.add(media);
            }
            if(media.getParent().endsWith("/Download") )
            {
                listDownload.add(media);
            }
            if(media.getPath().endsWith(".mp4") ||media.getPath().endsWith(".mpeg") ||media.getPath().endsWith(".avi" )||media.getPath().endsWith(".3gp" )||media.getPath().endsWith(".mkv" )||media.getPath().endsWith(".flv" )||media.getPath().endsWith(".ogg" )||media.getPath().endsWith(".ogv" )||media.getPath().endsWith(".wmp" )||media.getPath().endsWith(".amv" ))
            {
                listVideo.add(media);
            }
            if(media.getPath().endsWith(".mp3") ||media.getPath().endsWith(".flac") ||media.getPath().endsWith(".aac" )||media.getPath().endsWith(".amr" )||media.getPath().endsWith(".tta" )||media.getPath().endsWith(".wav" )||media.getPath().endsWith(".wma" )||media.getPath().endsWith(".webm" ))
            {
                listSound.add(media);
            }
        }
    }
    public static ArrayList<File> getListVideo() {
        return listVideo;
    }

    public static void setListVideo(ArrayList<File> listVideo) {
        FileTransactions.listVideo = listVideo;
    }

    public static ArrayList<File> getListSound() {
        return listSound;
    }

    public static void setListSound(ArrayList<File> listSound) {
        FileTransactions.listSound = listSound;
    }

    public static ArrayList<File> getListDownload() {
        return listDownload;
    }

    public static void setListDownload(ArrayList<File> listDownload) {
        FileTransactions.listDownload = listDownload;
    }

    public static ArrayList<File> getListPic() {
        return listPic;
    }

    public static void setListPic(ArrayList<File> listPic) {
        FileTransactions.listPic = listPic;
    }

    public static ArrayList<File> getListDoc() {
        return listDoc;
    }

    public static void setListDoc(ArrayList<File> listDoc) {
        FileTransactions.listDoc = listDoc;
    }

    public static ArrayList<File> getListCompress() {
        return listCompress;
    }

    public static void setListCompress(ArrayList<File> listCompress) {
        FileTransactions.listCompress = listCompress;
    }


}