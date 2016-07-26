package gm.com.dosya.activities;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.List;

import gm.com.dosya.R;
import gm.com.dosya.fragments.DirectoryFragment;
import gm.com.dosya.fragments.DirectoryFragment.DocumentSelectActivityDelegate;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 0;
    private Toolbar toolbar;
    private FragmentManager fragmentManager = null;
    private FragmentTransaction fragmentTransaction = null;
    private DirectoryFragment mDirectoryFragment;

    int bellekokumaizni;
    int bellekyazmaizni;
    List<String> izinler = new ArrayList<String>();
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        View.OnClickListener mCorkyListener = new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "basıldı", Toast.LENGTH_SHORT).show();
            }
        };
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("Directory");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View view) {
                                                     Toast.makeText(getApplicationContext(), "basıldı", Toast.LENGTH_SHORT).show();
                                                 }
                                             }

        );




        new DrawerBuilder().withActivity(this).build();
        drawerProcesses();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        mDirectoryFragment = new DirectoryFragment();
        mDirectoryFragment.setDelegate(new DocumentSelectActivityDelegate() {


            public void startDocumentSelectActivity() {

            }

            @Override
            public void didSelectFiles(DirectoryFragment activity,
                                       ArrayList<String> files) {
                mDirectoryFragment.showErrorBox(files.get(0).toString());
            }

            @Override
            public void updateToolBarName(String name) {
                toolbar.setTitle(name);

            }
        });
        fragmentTransaction.add(R.id.fragment_container, mDirectoryFragment, "" + mDirectoryFragment.toString());
        fragmentTransaction.commit();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            try {
                if(mDirectoryFragment.getHistory().size()==0&&mDirectoryFragment.catagory==false) {


                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this); //Mesaj Penceresini Yaratalım
                    alertDialogBuilder.setTitle("Programdan çıkılsın mı?").setCancelable(false).setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) { //Eğer evet butonuna basılırsa
                            dialog.dismiss();
                            android.os.Process.killProcess(android.os.Process.myPid());
//Uygulamamızı sonlandırıyoruz.
                        }

                    }).setNegativeButton("Hayır", new DialogInterface.OnClickListener() {

//Eğer hayır butonuna basılırsa

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "Programdan çıkmaktan vazgeçtiniz.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    alertDialogBuilder.create().show();
//son olarak alertDialogBuilder'ı oluşturup ekranda görüntületiyoruz.
                }return super.onKeyDown(keyCode, event);
            } catch (IllegalStateException e) {  //yapımızı try-catch blogu içerisine aldık
                //hata ihtimaline karşı.
                e.printStackTrace();
            }
            return super.onKeyDown(keyCode, event);
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch ( requestCode ) {
            case REQUEST_PERMISSIONS: {
                for( int i = 0; i < permissions.length; i++ ) { // İstediğimiz izinler dolaşalım
                    if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) { // Eğer izin verilmişse
                        Log.d( "Permissions", "İzin Verildi: " + permissions[i] ); // İsmiyle birlikte izin verildi yazıp log basalım.

// İzin verildiği için burada istediğiniz işlemleri yapabilirsiniz. Verilen izne göre sms okuyabilir ve belleğe yazabilirsiniz.
                    } else if( grantResults[i] == PackageManager.PERMISSION_DENIED ) { // Eğer izin reddedildiyse
                        Log.d( "Permissions", "İzin Reddedildi: " + permissions[i] ); // İsmiyle birlikte reddedildi yazıp log basalım.
// Burada bir toast mesajı gösterebilirsiniz. Mesela bu işlemi yapabilmek için izin vermeniz gereklidir. gibi..
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
    @Override
    protected void onDestroy() {
        mDirectoryFragment.onFragmentDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mDirectoryFragment.onBackPressed_()) {
            if(mDirectoryFragment.catagory==false){
                super.onBackPressed();
            }}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:  onBackPressed();
                break;
            case R.id.copy:

                break;

        }


        return true;
    }
    private void drawerProcesses()
    {
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("GENERAL MOBİLE");
        SecondaryDrawerItem item2 = (SecondaryDrawerItem) new SecondaryDrawerItem().withIdentifier(2).withName("Galeri");
        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        new SecondaryDrawerItem().withName("Cihaz Bilgisi")
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        return false;
                    }

                })
                .build();

    }
}