package ff.demostorg;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "backup";
    static final String NEW_FILE_BASE = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "ApkBackup" + File.separator;
    File mBaseFile = new File(NEW_FILE_BASE);

    TextView mInfo;

    StringBuilder mInfoStr = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mInfo = (TextView) findViewById(R.id.info);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        updateView("Backup Start......");
                        try {
                            backupAllUserApp();
                            backupApp(MainActivity.this.getPackageManager().getApplicationInfo("net.canking.steplog", 0).sourceDir, "net.canking.steplog");

                        } catch (Exception e) {
                            Log.e(TAG, " backup failed " + e.getMessage());
                        }
                        updateView("Backup Finished!");
                    }
                }).start();

                Snackbar.make(view, "Backup Finish", Snackbar.LENGTH_LONG).show();
            }
        });


    }

    private void backupApp(String path, String outname) throws IOException {
        File in = new File(path);

        if (!mBaseFile.exists()) mBaseFile.mkdir();
        File out = new File(mBaseFile, outname + ".apk");
        if (!out.exists()) out.createNewFile();
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);

        int count;
        byte[] buffer = new byte[256 * 1024];
        while ((count = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, count);
        }

        fis.close();
        fos.flush();
        fos.close();
    }

    private void backupAllUserApp() {
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> allPackages = packageManager.getInstalledPackages(0);
        for (int i = 0; i < allPackages.size(); i++) {
            PackageInfo packageInfo = allPackages.get(i);
            String path = packageInfo.applicationInfo.sourceDir;
            String name = packageInfo.applicationInfo.loadLabel(packageManager).toString();
            Log.i(TAG, path);
            Log.i(TAG, name);

            try {
                if (isUserApp(packageInfo)) {
                    Log.e(TAG, name + " is not user app,skip...");
                    updateView(name + " is not user app,skip...");
                    continue;
                }
                updateView("Start backup:" + name + "...");
                backupApp(path, name);
                updateView("*****Succeed backup:" + name+"******");

                Toast.makeText(MainActivity.this, "Succeed backup:" + path, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Succeed backup:" + name);

            } catch (Exception e) {
                Log.e(TAG, path + "Failed backup  " + e.getMessage());
                e.printStackTrace();
                continue;
            }
        }
    }

    private void updateView(String info) {
        mInfoStr.append(info + "\n");
        mInfo.post(new Runnable() {
            @Override
            public void run() {
                mInfo.setText(mInfoStr.toString());
            }
        });
    }

    public boolean isUserApp(PackageInfo pInfo) {
        return (((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) && ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0));
    }

}
