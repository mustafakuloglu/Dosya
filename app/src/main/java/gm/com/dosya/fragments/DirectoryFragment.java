package gm.com.dosya.fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.util.StateSet;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import gm.com.dosya.R;
import gm.com.dosya.adapters.BaseFragmentAdapter;
import gm.com.dosya.models.ListItem;

public class DirectoryFragment extends Fragment {

    private static String title_ = "";
    public String ilkelPath = null;
    Button yapis;
    String rename = null;
    String copyPath = null;
    String  createpath=null;
    String itemname = null;
    String targetPath = null;
    String renamePath=null;
    CheckBox check;
    boolean cpy = false,paste=false;
    File[] files = null;
    Toolbar toolbar;
    private View fragmentView;
    private boolean receiverRegistered = false;
    private File currentDir;
    private ListView listView;
    private BaseFragmentAdapter baseAdapter;
    private TextView emptyView;
    private DocumentSelectActivityDelegate delegate;
    private ArrayList<ListItem> items = new ArrayList<ListItem>();
    private ArrayList<HistoryEntry> history = new ArrayList<HistoryEntry>();
    private HashMap<String, ListItem> selectedFiles = new HashMap<String, ListItem>();
    private long sizeLimit = 1024 * 1024 * 1024;
    private String[] chhosefileType = {".pdf", ".doc", ".docx", ".DOC", ".DOCX"};
    private boolean click=true;
    public ArrayList<String> copyList;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        if (currentDir == null) {
                            listRoots();
                        } else {
                            listFiles(currentDir);
                        }
                    } catch (Exception e) {
                        Log.e("tmessages", e.toString());
                    }
                }
            };
            if (Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) {
                listView.postDelayed(r, 1000);
            } else {
                r.run();
            }
        }
    };

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

    public static void clearDrawableAnimation(View view) {
        if (Build.VERSION.SDK_INT < 21 || view == null) {
            return;
        }
        Drawable drawable = null;
        if (view instanceof ListView) {
            drawable = ((ListView) view).getSelector();
            if (drawable != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    drawable.setState(StateSet.NOTHING);
                }
            }
        } else {
            drawable = view.getBackground();
            if (drawable != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    drawable.setState(StateSet.NOTHING);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    drawable.jumpToCurrentState();
                }
            }
        }
    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

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

    public boolean onBackPressed_() {
        if (history.size() > 0) {
            HistoryEntry he = history.remove(history.size() - 1);
            title_ = he.title;
            updateName(title_);
            if (he.dir != null) {
                listFiles(he.dir);
            } else {
                listRoots();
            }
            listView.setSelectionFromTop(he.scrollItem, he.scrollOffset);
            return false;
        } else {
            return true;
        }
    }

    private void updateName(String title_) {
        if (delegate != null) {
            delegate.updateToolBarName(title_);
        }
    }

    public void onFragmentDestroy() {
        try {
            if (receiverRegistered) {
                getActivity().unregisterReceiver(receiver);
            }
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
    }

    public void setDelegate(DocumentSelectActivityDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!receiverRegistered) {
            receiverRegistered = true;
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
            getActivity().registerReceiver(receiver, filter);
        }
        copyList=new ArrayList<String>();
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.document_select_layout,
                    container, false);

            baseAdapter = new BaseFragmentAdapter(getActivity(), items);
            emptyView = (TextView) fragmentView
                    .findViewById(R.id.searchEmptyView);
            emptyView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            listView = (ListView) fragmentView.findViewById(R.id.listView);
            listView.setEmptyView(emptyView);
            listView.setAdapter(baseAdapter);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


                    ilkelPath = items.get(position).getThumb();
                    itemname = items.get(position).getTitle();
                    rename = items.get(position).getTitle();

                    return false;

                }
            });

            registerForContextMenu(listView);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view,
                                        int position, long l) {
                    if(click){

                    if (position < 0 || position >= items.size()) {
                        return;
                    }
                    ListItem item = items.get(position);
                    File file = item.getFile();
//                    if (file == null) {
//                        HistoryEntry he = history.remove(history.size() - 1);
//                        title_ = he.title;
//                        updateName(title_);
//                        if (he.dir != null) {
//                            listFiles(he.dir);
//                        } else {
//                            listRoots();
//                        }
//                        listView.setSelectionFromTop(he.scrollItem,
//                                he.scrollOffset);
//                    } else
                        if (file.isDirectory()) {
                        HistoryEntry he = new HistoryEntry();
                        he.scrollItem = listView.getFirstVisiblePosition();
                        he.scrollOffset = listView.getChildAt(0).getTop();
                        he.dir = currentDir;
                        he.title = title_.toString();
                        updateName(title_);
                        if (!listFiles(file)) {
                            return;
                        }
                        history.add(he);
                        title_ = item.getTitle();
                        updateName(title_);
                        listView.setSelection(0);
                    } else {

                        if (!file.canRead()) {
                            showErrorBox("Yetkiniz bulunmamaktadır.");
                            return;
                        }
                        MimeTypeMap myMime = MimeTypeMap.getSingleton();
                        String mimeType = myMime.getMimeTypeFromExtension(getExtension(file));

                        Uri path = Uri.fromFile(file);

                        Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                        pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        pdfOpenintent.setDataAndType(path, mimeType);

                        getActivity().startActivity(pdfOpenintent);

                    }

                    if (history.size() > 0) {

                        toolbar.setNavigationIcon(R.drawable.arrow);

                    } else {
                        toolbar.setNavigationIcon(R.drawable.back);

                    }

                }
                    else
                    {
                     if(items.get(position).getCheck()) {
                         items.get(position).setCheck(false);
                         baseAdapter.notifyDataSetChanged();
                     }
                        else
                     {
                         items.get(position).setCheck(true);
                         baseAdapter.notifyDataSetChanged();
                     }

                    }

                }

            });


            listRoots();
        } else {
            ViewGroup parent = (ViewGroup) fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        toolbar = (Toolbar) getActivity().findViewById(R.id.tool_bar);
        new DrawerBuilder().withActivity(getActivity()).build();
        drawerProcesses();
        return fragmentView;
    }

    private void listRoots() {
        currentDir = null;
        items.clear();
        String extStorage = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        ListItem ext = new ListItem();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if (Build.VERSION.SDK_INT < 9
                    || Environment.isExternalStorageRemovable()) {
                ext.setTitle("Internal Storage");
            } else {
                ext.setTitle("Internal Storage");
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            ext.setIcon(Build.VERSION.SDK_INT < 9
                    || Environment.isExternalStorageRemovable() ? R.drawable.ic_external_storage
                    : R.drawable.ic_storage);
        }

        ext.setSubtitle(getRootSubtitle(extStorage));
        ext.setFile(Environment.getExternalStorageDirectory());
        items.add(ext);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(
                    "/proc/mounts"));
            String line;
            HashMap<String, ArrayList<String>> aliases = new HashMap<String, ArrayList<String>>();
            ArrayList<String> result = new ArrayList<String>();
            String extDevice = null;
            while ((line = reader.readLine()) != null) {
                if ((!line.contains("/mnt") && !line.contains("/storage") && !line
                        .contains("/sdcard"))
                        || line.contains("asec")
                        || line.contains("tmpfs") || line.contains("none")) {
                    continue;
                }
                String[] info = line.split(" ");
                if (!aliases.containsKey(info[0])) {
                    aliases.put(info[0], new ArrayList<String>());
                }
                aliases.get(info[0]).add(info[1]);
                if (info[1].equals(extStorage)) {
                    extDevice = info[0];
                }
                result.add(info[1]);
            }
            reader.close();
            if (extDevice != null) {
                result.removeAll(aliases.get(extDevice));
                for (String path : result) {
                    try {
                        ListItem item = new ListItem();
                        if (path.toLowerCase().contains("sd")) {
                            ext.setTitle("Internal Storage");
                        } else {
                            ext.setTitle("ExternalStorage");
                        }
                        item.setIcon(R.drawable.ic_external_storage);
                        item.setTitle("Sdcard");
                        item.setSubtitle(getRootSubtitle(path));
                        item.setFile(new File(path));
                        items.add(item);
                    } catch (Exception e) {
                        Log.e("tmessages", e.toString());
                    }
                }
            }
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
        }
        ListItem fs = new ListItem();
        fs.setTitle("/");
        fs.setSubtitle("SystemRoot");
        fs.setIcon(R.drawable.ic_directory);
        fs.setFile(new File("/"));
        items.add(fs);

        // try {
        // File telegramPath = new
        // File(Environment.getExternalStorageDirectory(), "Telegram");
        // if (telegramPath.exists()) {
        // fs = new ListItem();
        // fs.title = "Telegram";
        // fs.subtitle = telegramPath.toString();
        // fs.icon = R.drawable.ic_directory;
        // fs.file = telegramPath;
        // items.add(fs);
        // }
        // } catch (Exception e) {
        // FileLog.e("tmessages", e);
        // }

        // AndroidUtilities.clearDrawableAnimation(listView);
        // scrolling = true;
        baseAdapter.notifyDataSetChanged();
    }

    private boolean listFiles(File dir) {
        if (!dir.canRead()) {
            if (dir.getAbsolutePath().startsWith(
                    Environment.getExternalStorageDirectory().toString())
                    || dir.getAbsolutePath().startsWith("/sdcard")
                    || dir.getAbsolutePath().startsWith("/mnt/sdcard")) {
                if (!Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)
                        && !Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED_READ_ONLY)) {
                    currentDir = dir;
                    items.clear();
                    String state = Environment.getExternalStorageState();
                    if (Environment.MEDIA_SHARED.equals(state)) {
                        emptyView.setText("UsbActive");
                    } else {
                        emptyView.setText("NotMounted");
                    }
                    clearDrawableAnimation(listView);
                    // scrolling = true;
                    baseAdapter.notifyDataSetChanged();
                    return true;
                }
            }
            showErrorBox("AccessError");
            return false;
        }
        emptyView.setText("NoFiles");

        try {
            files = dir.listFiles();
        } catch (Exception e) {
            showErrorBox(e.getLocalizedMessage());
            return false;
        }
        if (files == null) {
            showErrorBox("UnknownError");
            return false;
        }
        currentDir = dir;
        items.clear();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                if (lhs.isDirectory() != rhs.isDirectory()) {
                    return lhs.isDirectory() ? -1 : 1;
                }
                return lhs.getName().compareToIgnoreCase(rhs.getName());
                /*
                 * long lm = lhs.lastModified(); long rm = lhs.lastModified();
             * if (lm == rm) { return 0; } else if (lm > rm) { return -1; }
             * else { return 1; }
             */
            }
        });
        for (File file : files) {

            if (file.getName().startsWith(".") || file.getName().endsWith(".dm")) {
                continue;
            }
            ListItem item = new ListItem();
            item.setTitle(file.getName());
            ;
            item.setFile(file);
            item.setThumb(file.getAbsolutePath());
            if (file.isDirectory()) {
                item.setIcon(R.drawable.ic_directory);
                item.setSubtitle("Folder");
            } else {
                String fname = file.getName();
                String[] sp = fname.split("\\.");
                item.setExt(sp.length > 1 ? sp[sp.length - 1] : "?");
                item.setSubtitle(formatFileSize(file.length()));
                fname = fname.toLowerCase();
                if (fname.endsWith(".jpg") || fname.endsWith(".png")
                        || fname.endsWith(".gif") || fname.endsWith(".jpeg")) {
                    item.setThumb(file.getAbsolutePath());
                    item.setIcon(R.drawable.foto);
                }


                if (fname.endsWith(".mp3") || (fname.endsWith(".amr"))) {
                    item.setIcon(R.drawable.music);
                }
                if (fname.endsWith(".zip") || (fname.endsWith(".rar"))) {
                    item.setIcon(R.drawable.zip);
                }
                if (fname.endsWith(".docx")) {
                    item.setIcon(R.drawable.word);
                }
                if (fname.endsWith(".mp4") || (fname.endsWith(".mpg")) || (fname.endsWith(".avi")) || (fname.endsWith(".3gp"))) {
                    item.setIcon(R.drawable.video);
                }
                if (fname.endsWith(".pdf")) {
                    item.setIcon(R.drawable.pdf);
                }
            }


            items.add(item);
        }
        ListItem item = new ListItem();
        item.setTitle("Go Back");
        item.setSubtitle("Folder");
        item.setIcon(R.drawable.back);
        item.setFile(null);
        items.add(0, item);
        clearDrawableAnimation(listView);
        // scrolling = true;
        baseAdapter.notifyDataSetChanged();
        return true;
    }

    public void showErrorBox(String error) {
        if (getActivity() == null) {
            return;
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.app_name))
                .setMessage(error).setPositiveButton("OK", null).show();
    }

//    private class ListAdapter extends BaseFragmentAdapter {
//        private Context mContext;
//
//        public ListAdapter(Context context) {
//            mContext = context;
//        }
//
//        @Override
//        public int getCount() {
//            return items.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return items.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return 0;
//        }
//
//        public int getViewTypeCount() {
//            return 2;
//        }
//
//        public int getItemViewType(int pos) {
//            return items.get(pos).subtitle.length() > 0 ? 0 : 1;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = new TextDetailDocumentsCell(mContext);
//            }
//            TextDetailDocumentsCell textDetailCell = (TextDetailDocumentsCell) convertView;
//            ListItem item = items.get(position);
//            if (item.icon != 0) {
//                ((TextDetailDocumentsCell) convertView)
//                        .setTextAndValueAndTypeAndThumb(item.title,
//                                item.subtitle, null, null, item.icon,getActivity().getContentResolver());
//            } else {
//                String type = item.ext.toUpperCase().substring(0,
//                        Math.min(item.ext.length(), 4));
//                ((TextDetailDocumentsCell) convertView)
//                        .setTextAndValueAndTypeAndThumb(item.title,
//                                item.subtitle, type, item.thumb, 0,getActivity().getContentResolver());
//            }
//            // if (item.file != null && actionBar.isActionModeShowed()) {
//            // textDetailCell.setChecked(selectedFiles.containsKey(item.file.toString()),
//            // !scrolling);
//            // } else {
//            // textDetailCell.setChecked(false, !scrolling);
//            // }
//            return convertView;
//        }
//    }

    private String getRootSubtitle(String path) {
        StatFs stat = new StatFs(path);
        long total = (long) stat.getBlockCount() * (long) stat.getBlockSize();
        long free = (long) stat.getAvailableBlocks()
                * (long) stat.getBlockSize();
        if (total == 0) {
            return "";
        }
        return "Free " + formatFileSize(free) + " of " + formatFileSize(total);
    }

    public void finishFragment() {

    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Context Menu");
        menu.add(0, v.getId(), 0, "Kopyala");
        menu.add(0, v.getId(), 0, "Sil");
        menu.add(0, v.getId(), 0, "Create Folder");
        menu.add(0, v.getId(), 0, "Düzenle");
        menu.add(0,v.getId(),  0, "ZipFolder");

        if (cpy == true) {
            menu.add(0, v.getId(), 0, "Yapıştır");

        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    /*
    public void zip(String _files, String zipFileName) {
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                Log.v("Compress", "Adding: " + _files[i]);
                FileInputStream fi = new FileInputStream(_files[i]);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);


        final MenuItem yapistirmenu = menu.findItem(R.id.yapistir);
        final MenuItem copymenu = menu.findItem(R.id.copy);

        yapistirmenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                yapistir();
                yapistirmenu.setVisible(false);
                copymenu.setVisible(true);
                return false;
            }
        });
        copymenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                kopyala();
                 if(cpy&&copyList.size()>0) {
                     yapistirmenu.setVisible(true);
                     for(int count=0;count<items.size();count++) {
                         items.get(count).setVisible(false);
                                              }
                     baseAdapter.notifyDataSetChanged();
                     click=true;
                     cpy=false;
                     copymenu.setVisible(false);
                 }

                return false;
            }
        });

    }

    @Override
    public boolean onContextItemSelected(final MenuItem itemr) {
            // TODO Auto-generated method stub
            final int position;

            if (itemr.getTitle() == "Sil") {
                final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) itemr
                        .getMenuInfo();
                position = (int) info.id;

                File sil = new File(ilkelPath);
                boolean deleted = sil.delete();
                items.remove(position);

            }
            if (itemr.getTitle() == "Kopyala") {
                copyPath = ilkelPath;
                cpy = true;

            }
            if (itemr.getTitle() == "Düzenle") {


                MaterialDialog builder = new MaterialDialog.Builder(getActivity())
                        .title("Add Item")
                        .widgetColor(getResources().getColor(R.color.colorPrimaryDark))
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null,null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                renamePath=ilkelPath;
                                String newName = input.toString();

                                File konum = new File(renamePath,rename);
                                File yenisim = new File(renamePath,newName);
                                konum.renameTo(yenisim);

                            }
                        }).negativeText("Cancel").show();

            }

            if (itemr.getTitle() == "Create Folder")
            {
                MaterialDialog builder = new MaterialDialog.Builder(getActivity())
                        .title("Add Item")
                        .widgetColor(getResources().getColor(R.color.colorPrimaryDark))
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null,null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence inputs) {
                                String newfolder = inputs.toString();
                                createpath=ilkelPath;

                                File folder = new File(createpath +
                                        File.separator + newfolder);
                                boolean success = true;
                                if (!folder.exists()) {
                                    success = folder.mkdir();
                                }
                                if (success) {
                                    // Do something on success
                                } else {
                                    // Do something else on failure
                                }
                            }
                        }).negativeText("Cancel").show();

            }

        if (itemr.getTitle() == "ZipFolder")
        {
            MaterialDialog builderzip = new MaterialDialog.Builder(getActivity())
                    .title("Add Item")
                    .widgetColor(getResources().getColor(R.color.colorPrimaryDark))
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input(null,null, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence inputzip){
                            String newzipfolder = inputzip.toString();
                            createpath=ilkelPath;
                          // zip(c);

                        }
                    }).negativeText("Cancel").show();

        }


            if (itemr.getTitle() == "Yapıştır") {
               for(int count=0;count<items.size();count++)
                {
                    if(items.get(count).getCheck())
                    {
                       copyPath=items.get(count).getThumb();

                        File control = new File(ilkelPath);
                        if (control.isDirectory()) {
                            targetPath = ilkelPath;
                        } else {

                            targetPath = ilkelPath.substring(0, ilkelPath.length() - itemname.length());
                        }
                        copyFileOrDirectory(copyPath, targetPath);
                        cpy = false;


                    }
                }

            }

        baseAdapter.notifyDataSetChanged();
            return super.onContextItemSelected(itemr);


    }

    public static abstract interface DocumentSelectActivityDelegate {
        public void didSelectFiles(DirectoryFragment activity, ArrayList<String> files);

        public void startDocumentSelectActivity();

        public void updateToolBarName(String name);
    }

    private class HistoryEntry {
        int scrollItem, scrollOffset;
        File dir;
        String title;
    }



    private void drawerProcesses()
    {
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("item 1");
        SecondaryDrawerItem item2 = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(2).withName(R.string.app_name);
        Drawer result = new DrawerBuilder()
                .withActivity(getActivity())
                .withToolbar(toolbar)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        new SecondaryDrawerItem().withName("string")
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        return false;
                    }

                })
                .build();

    }

    private void kopyala()
    {
        if(cpy){
        for(int count=0;count<items.size();count++)
        {
            if(items.get(count).getCheck())
            {
                copyList.add(items.get(count).getThumb());
            }

        }
            if(copyList.size()>0)
        {
            //kopyalama işlemleri yapılacak yani yapıştır çağırılacak
        }
        else
        {
            showErrorBox("Lütfen kopyalanacak dosya veya klasör seçiniz.");
        }
        }
        else
        {
            cpy=true;
            click=false;
            for(int count=0;count<items.size();count++) {
                items.get(count).setVisible(true);
                cpy=true;
            }

            baseAdapter.notifyDataSetChanged();
        }
    }
    private void yapistir()
    {
       paste=true;

        for(int count=0;count<copyList.size();count++)
        {
            copyFileOrDirectory(copyList.get(count),currentDir.getAbsolutePath());
        }


    }

}