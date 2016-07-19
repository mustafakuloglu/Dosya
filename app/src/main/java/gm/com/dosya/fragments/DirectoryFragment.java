package gm.com.dosya.fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.StateSet;
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

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import gm.com.dosya.R;
import gm.com.dosya.adapters.BaseFragmentAdapter;
import gm.com.dosya.models.HistoryEntry;
import gm.com.dosya.models.ListItem;
import gm.com.dosya.utils.FileTransactions;
import gm.com.dosya.utils.UtilityMethods;

public class DirectoryFragment extends Fragment {

    private static String title_ = "";
    public String ilkelPath = null;
    public ArrayList<String> copyList;
    Button yapis;
    String rename = null;
    String copyPath = null;
    String createpath = null;
    String itemname = null;
    String targetPath = null;
    String renamePath = null;
    String silPath = null;
    CheckBox check;
    boolean click = true;
    File[] files = null;
    Toolbar toolbar;
    boolean cpy = false, paste = false;
    Drawer result;
    UtilityMethods util;
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

    public boolean onBackPressed_() {
        for (int count = 0; count < items.size(); count++) {
            items.get(count).setVisible(false);
        }
        click = true;

        if (history.size() > 0) {
            HistoryEntry he = history.remove(history.size() - 1);
            toolbar.setNavigationIcon(R.drawable.arrow);
            if (history.size() == 0) {
                toolbar.setNavigationIcon(R.drawable.back);
            }

            title_ = he.getTitle();
            updateName(title_);
            if (he.getDir() != null) {
                listFiles(he.getDir());
            } else {
                listRoots();
            }
            listView.setSelectionFromTop(he.getScrollItem(), he.getScrollOffset());
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
        util = new UtilityMethods();
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
        copyList = new ArrayList<String>();

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

                    for (int count = 0; count < items.size(); count++) {
                        items.get(count).setVisible(true);

                        cpy = true;

                    }

                    click = false;
                    baseAdapter.notifyDataSetChanged();
                    return false;

                }
            });

            registerForContextMenu(listView);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view,
                                        int position, long l) {
                    if (click) {
                        if (history.size() == 0) {
                            toolbar.setNavigationIcon(R.drawable.arrow);
                        }
                        if (position < 0 || position >= items.size()) {
                            return;
                        }
                        ListItem item = items.get(position);
                        File file = item.getFile();

                        if (file.isDirectory()) {
                            HistoryEntry he = new HistoryEntry();
                            he.setScrollItem(listView.getFirstVisiblePosition());
                            he.setScrollOffset(listView.getChildAt(0).getTop());
                            he.setDir(currentDir);
                            he.setTitle(title_.toString());
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

                    } else {
                        if (items.get(position).getCheck()) {
                            items.get(position).setCheck(false);
                        } else {
                            items.get(position).setCheck(true);
                        }
                        baseAdapter.notifyDataSetChanged();
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

        View.OnClickListener tool = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (history.size() > 0) {
                    onBackPressed_();
                } else {
                    result.openDrawer();
                }
            }
        };
        toolbar.setNavigationOnClickListener(tool);
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

        ext.setSubtitle(util.getRootSubtitle(extStorage));
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
                        item.setSubtitle(util.getRootSubtitle(path));
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
                item.setSubtitle(UtilityMethods.formatFileSize(file.length()));
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


    public void finishFragment() {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final MenuItem silmenu = menu.findItem(R.id.delete);
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
                if (cpy && copyList.size() > 0) {
                    yapistirmenu.setVisible(true);
                    for (int count = 0; count < items.size(); count++) {
                        items.get(count).setVisible(false);
                    }
                    baseAdapter.notifyDataSetChanged();
                    click = true;
                    cpy = false;
                    copymenu.setVisible(false);
                }

                return false;
            }
        });


        silmenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                for (int count = 0; count < items.size(); count++) {

                    if (items.get(count).getCheck()) {
                        silPath = items.get(count).getThumb();
                        File sil = new File(silPath);
                        boolean deleted = sil.delete();
                    }
                    items.get(count).setVisible(false);
                }
                click = true;
                listFiles(currentDir);
                return false;
            }
        });

    }

    private void drawerProcesses() {
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("item 1");
        SecondaryDrawerItem item2 = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(2).withName(R.string.app_name);
        result = new DrawerBuilder()

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

    private void kopyala() {
        if (cpy) {
            for (int count = 0; count < items.size(); count++) {
                if (items.get(count).getCheck()) {
                    copyList.add(items.get(count).getThumb());
                }

            }
            if (copyList.size() > 0) {
                //kopyalama işlemleri yapılacak yani yapıştır çağırılacak
            } else {
                showErrorBox("Lütfen kopyalanacak dosya veya klasör seçiniz.");
            }
        } else {
            cpy = true;
            click = false;
            for (int count = 0; count < items.size(); count++) {
                items.get(count).setVisible(true);
                cpy = true;
            }

            baseAdapter.notifyDataSetChanged();
        }
    }

    private void yapistir() {
        FileTransactions tran = new FileTransactions();
        paste = true;

        for (int count = 0; count < copyList.size(); count++) {
            tran.copyFileOrDirectory(copyList.get(count), currentDir.getAbsolutePath());
        }
        listFiles(currentDir);

    }

    public static abstract interface DocumentSelectActivityDelegate {
        public void didSelectFiles(DirectoryFragment activity, ArrayList<String> files);

        public void startDocumentSelectActivity();

        public void updateToolBarName(String name);
    }

}
