package gm.com.dosya.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import gm.com.dosya.R;
import gm.com.dosya.adapters.BaseFragmentAdapter;
import gm.com.dosya.models.HistoryEntry;
import gm.com.dosya.models.ListItem;
import gm.com.dosya.utils.FileTransactions;
import gm.com.dosya.utils.UtilityMethods;
import gm.com.dosya.utils.ZipUtility;

public class DirectoryFragment extends Fragment {

    private static String title_ = "";
    public String ilkelPath = null;
    public ArrayList<File> copyList;
    Button yapis;
    MenuItem silmenu;
    MenuItem yapistirmenu;
    MenuItem copymenu;
    MenuItem duzenlemenu;
    MenuItem tasimenu;
    MenuItem createmenu;
    MenuItem zipmenu;
    MenuItem infomenu;
    private ProgressDialog progress;
    String rename = null;
    String copyPath = null;
    String createpath = null;
    private ArrayList<ListItem> zipList;
    String itemname = null;
    String targetPath = null;
    String renamePath = null;
    String silPath = null;
    String newzipfolder = null;
    int counter = 0;
    CheckBox check;
    boolean click = true;
    boolean tasi = false;
    File[] files = null;
    Toolbar toolbar;
    boolean cpy = false, paste = false;
    Drawer result;
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
    private UtilityMethods util;
    private ArrayList<String> infoList;
    private String islem="";
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
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
            if (history.size() == 0) {
                toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
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

            getActivity().registerReceiver(receiver, util.getIntent());
        }
        copyList = new ArrayList<File>();
        zipList = new ArrayList<>();
        infoList = new ArrayList<>();
        progress = new ProgressDialog(getActivity());

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

                    counter = 0;
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
                            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
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
                           try {
                               MimeTypeMap myMime = MimeTypeMap.getSingleton();
                               String mimeType = myMime.getMimeTypeFromExtension(getExtension(file));

                               Uri path = Uri.fromFile(file);

                               Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                               pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                               pdfOpenintent.setDataAndType(path, mimeType);

                               getActivity().startActivity(pdfOpenintent);
                           }
                           catch (Exception e)
                           {
showErrorBox("Dosyayı açabilecek bir program bulunamadı!!");
                           }


                        }

                        if (history.size() > 0) {

                            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
                        } else {
                            toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
                        }
                    } else {
                        if (items.get(position).getCheck()) {
                            items.get(position).setCheck(false);
                            counter--;

                        } else {
                            items.get(position).setCheck(true);
                            counter++;


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
                ext.setTitle("Cihaz Belleği");
            } else {
                ext.setTitle("Cihaz Belleği");
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            ext.setIcon(Build.VERSION.SDK_INT < 9
                    || Environment.isExternalStorageRemovable() ? R.drawable.ic_inbox_black_24dp
                    : R.drawable.ic_inbox_black_24dp);
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
                            ext.setTitle("Cihaz Belleği");
                        } else {
                            ext.setTitle("ExternalStorage");
                        }
                        item.setIcon(R.drawable.ic_inbox_black_24dp);
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
                    baseAdapter.notifyDataSetChanged();
                    return true;
                }
            }
            showErrorBox("AccessError");
            return false;
        }
        emptyView.setText("Klasör Boş");

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
                item.setIcon(R.drawable.ic_folder_black_24dp);
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
                    item.setIcon(R.drawable.ic_photo_black_24dp);
                }


                else if (fname.endsWith(".mp3") || (fname.endsWith(".amr"))) {
                    item.setIcon(R.drawable.ic_audiotrack_black_24dp);
                }
                else if (fname.endsWith(".zip") || (fname.endsWith(".rar"))) {
                    item.setIcon(R.drawable.ic_gradient_black_24dp);
                }
                else if (fname.endsWith(".docx")) {
                    item.setIcon(R.drawable.word);
                }
                else if (fname.endsWith(".mp4") || (fname.endsWith(".mpg")) || (fname.endsWith(".avi")) || (fname.endsWith(".3gp"))) {
                    item.setIcon(R.drawable.ic_local_movies_black_24dp);
                }
                else if (fname.endsWith(".gif"))
                {
                    item.setIcon(R.drawable.ic_gif_black_24dp);
                }
                else if (fname.endsWith(".pdf")) {
                    item.setIcon(R.drawable.ic_picture_as_pdf_black_24dp);
                }
                else
                {
                    item.setIcon(R.drawable.ic_local_florist_black_24dp);
                }
            }


            items.add(item);
        }


        baseAdapter.notifyDataSetChanged();
        return true;
    }

    private boolean listList(ArrayList<File> mediaFile) {
        items.clear();

        for (File file : mediaFile) {

            if (file.getName().startsWith(".") || file.getName().endsWith(".dm")) {
                continue;
            }
            ListItem item = new ListItem();
            item.setTitle(file.getName());

            item.setFile(file);
            item.setThumb(file.getAbsolutePath());
            if (file.isDirectory()) {
                item.setIcon(R.drawable.ic_folder_black_24dp);
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
                    item.setIcon(R.drawable.ic_photo_black_24dp);
                }


                else if (fname.endsWith(".mp3") || (fname.endsWith(".amr"))) {
                    item.setIcon(R.drawable.ic_audiotrack_black_24dp);
                }
                else if (fname.endsWith(".zip") || (fname.endsWith(".rar"))) {
                    item.setIcon(R.drawable.ic_gradient_black_24dp);
                }
                else if (fname.endsWith(".docx")) {
                    item.setIcon(R.drawable.word);
                }
                else if (fname.endsWith(".mp4") || (fname.endsWith(".mpg")) || (fname.endsWith(".avi")) || (fname.endsWith(".3gp"))) {
                    item.setIcon(R.drawable.ic_local_movies_black_24dp);
                }
                else if (fname.endsWith(".gif"))
                {
                    item.setIcon(R.drawable.ic_gif_black_24dp);
                }
                else if (fname.endsWith(".pdf")) {
                    item.setIcon(R.drawable.ic_picture_as_pdf_black_24dp);
                }
                else
                {
                    item.setIcon(R.drawable.ic_local_florist_black_24dp);
                }
            }


            items.add(item);
        }


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

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
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
        yapistirmenu = menu.findItem(R.id.yapistir);
        copymenu = menu.findItem(R.id.copy);
        final MenuItem duzenlemenu = menu.findItem(R.id.duzenle);

        tasimenu = menu.findItem(R.id.move);
        final MenuItem createmenu = menu.findItem(R.id.create);
        final MenuItem zipmenu = menu.findItem(R.id.zip);
        final MenuItem infomenu = menu.findItem(R.id.info);
        if(counter==1)
        {duzenlemenu.setVisible(true);
         infomenu.setVisible(true);}
        else{duzenlemenu.setVisible(false);
             infomenu.setVisible(false);}
        if(counter>0)
        {createmenu.setVisible(false);}
        else{createmenu.setVisible(true);}
        yapistirmenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
              islem="yapistir";
                new AsyncClass().execute();

                return false;
            }
        });
        infomenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
               try{ infoliste();
                MaterialDialog builder = new MaterialDialog.Builder(getActivity())
                        .title("Özellikler")
                        .items(infoList)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            }
                        })
                        .negativeText("İPTAL")
                        .positiveText("OK")
                        .show();
                listFiles(currentDir);
                infoList.clear();
                click = true;
                return false;
               }
               catch (Exception e)
               {showErrorBox("İşlem gerçekleştirilemedi");
                   return false;
               }
            }
        });
        copymenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
               try{ kopyala();
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
               catch (Exception e)
               {showErrorBox("Kopyalama işlemi gerçekleştirilemedi");
                   return false;
               }
            }
        });
        tasimenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
               try{ kopyala();
                if (cpy && copyList.size() > 0) {
                    yapistirmenu.setVisible(true);
                    for (int count = 0; count < items.size(); count++) {
                        items.get(count).setVisible(false);
                    }
                    baseAdapter.notifyDataSetChanged();
                    click = true;
                    cpy = false;
                    copymenu.setVisible(false);
                    tasimenu.setVisible(false);
                    tasi = true;
                }

                return false;
               }
               catch (Exception e)
               {showErrorBox("Taşıma işlemi gerçekleştirilemedi");
                   return false;
               }
            }
        });

        silmenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    AlertDialog.Builder ad=new AlertDialog.Builder(getActivity());
                             ad.setTitle("Dosyayı sil");
                             ad.setMessage("Silinsin mi?");
                                     ad.setCancelable(false);
                                     ad.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                                islem="sil";
                    new AsyncClass().execute();

                        }
                                     });

                    ad.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {

                                        }
                        });

                        AlertDialog alertDialog =ad.create();
                                        alertDialog.show();
                    click = true;

                    return false;
                }
                catch (Exception e)
                {showErrorBox("Silme işlemi gerçekleştirilemedi");
                    return false;
                }
            }
        });

        duzenlemenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
               try{ String isim = null;
                for(ListItem gecici :items)
                {
                    if(gecici.getCheck())
                    {
                        isim=gecici.getTitle();
                    }
                }
                MaterialDialog builder = new MaterialDialog.Builder(getActivity())
                        .title("Add Item")
                        .widgetColor(getResources().getColor(R.color.colorPrimaryDark))
                        .inputType(InputType.TYPE_CLASS_TEXT)

                        .input(null, isim, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {

                                for (int count = 0; count < items.size(); count++) {
                                    if (items.get(count).getCheck()) {
                                        String newname = input.toString();
                                        renamePath = items.get(count).getThumb();
                                        File konum = new File(renamePath);
                                        File yeniisim = new File(konum.getParent(), newname);
                                        konum.renameTo(yeniisim);
                                    }
                                }
                                listFiles(currentDir);
                            }
                        }).negativeText("Cancel").show();


                click = true;
                return false;
               }
               catch (Exception e)
               {showErrorBox("İşlem gerçekleştirilemedi");
                   return false;
               }
            }
        });
        zipmenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
               try{
                   islem="zip";

                   MaterialDialog builder = new MaterialDialog.Builder(getActivity())
                           .title("Add Item")
                           .widgetColor(getResources().getColor(R.color.colorPrimaryDark))
                           .inputType(InputType.TYPE_CLASS_TEXT)
                           .input(null, null, new MaterialDialog.InputCallback() {
                               @Override
                               public void onInput(MaterialDialog dialog, CharSequence input) {
                                   String anewzipfolder = input.toString();
                                   new AsyncClass().execute();
                                   newzipfolder = anewzipfolder;

                               }
                           }).negativeText("Cancel").show();

                   click = true;
                return false;
               }
               catch (Exception e)
               {showErrorBox("Sıkıştırma işlemi gerçekleştirilemedi");
                   return false;
               }
            }
        });

        createmenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
               try{ MaterialDialog builder = new MaterialDialog.Builder(getActivity())
                        .title("Add Item")
                        .widgetColor(getResources().getColor(R.color.colorPrimaryDark))
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null, null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {

                                String newfolder = input.toString();
                                File folder = new File(currentDir.getAbsoluteFile() +
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

                                listFiles(currentDir);
                            }
                        }).negativeText("Cancel").show();


                click = true;
                return false;
               }
               catch (Exception e)
               {showErrorBox("İşlem gerçekleştirilemedi");
                   return false;
               }
            }
        });

    }

    public class AsyncClass extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setMessage("İşlem Sürüyor...");

            progress.show();
        }

        @Override
        protected String doInBackground(String... strings) {

           switch (islem){
               case "yapistir":
            if (tasi) {
                kes();
            } else {
                yapistir();
            }break;
               case "zip":
                   ziple();
                   break;
               case"sil":
                   for (int count = 0; count < items.size(); count++) {
                       if (items.get(count).getCheck()) {
                           File cont = new File(items.get(count).getThumb());
                           FileTransactions.DeleteRecursive(cont);

                       }
                       items.get(count).setVisible(false);
                   }
                   break;
           }



            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            switch (islem){
                case "yapistir":
                    if (tasi) {
                        yapistirmenu.setVisible(false);
                        copymenu.setVisible(true);
                        tasimenu.setVisible(true);
                        tasi = false;
                        copyList.clear();
                        listFiles(currentDir);
                    } else {
                        yapistirmenu.setVisible(false);
                        copymenu.setVisible(true);
                        copyList.clear();
                        listFiles(currentDir);
                    }

                    break;
                case "zip":

                    listFiles(currentDir);
                    break;
                case"sil":
                    listFiles(currentDir);
                    break;
            }

            progress.dismiss();
        }
    }
    private void drawerProcesses() {
        final PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("GENERAL MOBİLE");
        final SecondaryDrawerItem pics = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(2).withName("Görüntüler");
        final SecondaryDrawerItem docs = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(3).withName("Dökümanlar");
        final SecondaryDrawerItem comps = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(4).withName("Sıkıştırılmış Dosyalar");
        final SecondaryDrawerItem videos = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(5).withName("Videolar");
        final SecondaryDrawerItem sounds = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(6).withName("Sesler");


        item1.withIcon(R.drawable.gm);
        pics.withIcon(R.drawable.ic_photo_black_24dp);
        docs.withIcon(R.drawable.ic_folder_black_24dp);
        comps.withIcon(R.drawable.ic_gradient_black_24dp);
        sounds.withIcon(R.drawable.ic_audiotrack_black_24dp);
        videos.withIcon(R.drawable.ic_local_movies_black_24dp);
        result = new DrawerBuilder()
                .withActivity(getActivity())
                .withToolbar(toolbar)
                .addDrawerItems(
                        item1,
                        pics,
                        docs,
                        comps,
                        sounds,
                        videos


                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        ArrayList<File> control=new ArrayList<File>();
                        if (drawerItem == pics) {
                            FileTransactions.catagoryMedia();
                            control=FileTransactions.getListPic();
                            listList(FileTransactions.getListPic());
                            if (control.size() == 0) {
                                showErrorBox("Görüntü dosyası bulunamadı");
                                listRoots();
                                updateName("Directory");
                            }
                        }
                        if (drawerItem == docs) {
                            FileTransactions.catagoryMedia();
                            control=FileTransactions.getListDoc();

                            listList(FileTransactions.getListDoc());
                            if (control.size() == 0) {
                                showErrorBox("Döküman bulunamadı");
                                listRoots();
                                updateName("Directory");
                            }
                        }
                        if (drawerItem == sounds) {
                            FileTransactions.catagoryMedia();
                            listList(FileTransactions.getListSound());
                            control=FileTransactions.getListSound();

                            if (control.size() == 0) {
                                showErrorBox("Ses dosyası bulunamadı;");
                                listRoots();
                                updateName("Directory");
                            }
                        }
                        if (drawerItem == comps) {
                            FileTransactions.catagoryMedia();
                            listList(FileTransactions.getListCompress());
                            control=FileTransactions.getListCompress();

                            if (control.size() == 0) {
                                showErrorBox("Sıkıştırılmış arşiv dosyası bulunamadı;");
                                listRoots();
                                updateName("Directory");
                            }
                        }
                        if (drawerItem == videos) {
                            FileTransactions.catagoryMedia();
                            listList(FileTransactions.getListVideo());
                            control=FileTransactions.getListVideo();

                            if (control.size() == 0) {
                                showErrorBox("Video dosyası bulunamadı");
                                listRoots();
                                updateName("Directory");
                            }
                        }
                        if (drawerItem == item1) {
                          listRoots();
                             updateName("Directory");
                        }
                        return false;
                    }

                })
                .build();

    }

    private void kopyala() {
        if (cpy) {
            for (int count = 0; count < items.size(); count++) {
                if (items.get(count).getCheck()) {
                    copyList.add(items.get(count).getFile());
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

    private void kes() {

        String path = currentDir.getAbsolutePath();

        for (int count = 0; count < copyList.size(); count++) {
            if(copyList.get(count).getParent().equals(path))
            {continue;}
            FileTransactions.moveFileOrDirectory(copyList.get(count), path);
            if (copyList.get(count).isDirectory()) {
                FileTransactions.DeleteRecursive(copyList.get(count));
            }
          else{
              yapistir();
                for ( count = 0; count < copyList.size(); count++) {
                    File moving=new File(String.valueOf(copyList.get(count)));
                    FileTransactions.DeleteRecursive(moving);
                }}}
    }

    private void yapistir() {
        String path = currentDir.getAbsolutePath();
        FileTransactions tran = new FileTransactions();
        paste = true;

        for (int count = 0; count < copyList.size(); count++) {

if(copyList.get(count).getParent().equals(path))
{continue;}
            tran.copyFileOrDirectory(copyList.get(count), path);

        }

    }
    private void infoliste()
    {
        for (int count=0;count<items.size();count++)
        {
            if(items.get(count).getCheck()) {
                infoList.add("İsim:" + " " + items.get(count).getTitle());
                infoList.add("Konum:" + " " + items.get(count).getThumb());

                File infof=new File(items.get(count).getThumb());
                Date dd=new Date(infof.lastModified());
                if(infof.isDirectory())
                {   String files[] = infof.list();
                    int filesLength = files.length;
                    infoList.add("Boyut:" + " "+ filesLength + " "+ "Items");
                    infoList.add("Son Değişiklik:"+ " "+ dd);
                }
                else{
                    infoList.add("Boyut:"+" "+items.get(count).getSubtitle());
                    infoList.add("Son Değişiklik:"+ " "+ dd);
                }
            }
        }
    }
    public static abstract interface DocumentSelectActivityDelegate {
        public void didSelectFiles(DirectoryFragment activity, ArrayList<String> files);

        public void updateToolBarName(String name);
    }

    private void ziple() {
        try{
        for (int count = 0; count < items.size(); count++) {
            if (items.get(count).getCheck()) {
                zipList.add(items.get(count));
            }
        }
        newzipfolder = currentDir.getPath() + "/" + newzipfolder + ".zip";
        ZipUtility.zipFileOrFolder(zipList, newzipfolder);
        }
        catch (Exception e)
        {showErrorBox("Sıkıştırma işlemi gerçekleştirilemedi");

        } }
    public ArrayList<HistoryEntry> getHistory(){
        return history;
    }


 }