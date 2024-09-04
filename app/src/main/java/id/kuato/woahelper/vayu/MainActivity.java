package id.kuato.woahelper.vayu;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import com.google.android.material.button.MaterialButton;
import com.topjohnwu.superuser.Shell;
import id.kuato.woahelper.BuildConfig;
import id.kuato.woahelper.R;
import id.kuato.woahelper.databinding.ActivityMainBinding;
import id.kuato.woahelper.databinding.SetPanelBinding;
import id.kuato.woahelper.databinding.ToolboxBinding;
import id.kuato.woahelper.databinding.ScriptsBinding;
import id.kuato.woahelper.util.MemoryUtils;
import id.kuato.woahelper.util.RAM;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import com.topjohnwu.superuser.ShellUtils;
import id.kuato.woahelper.preference.pref;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import id.kuato.woahelper.databinding.DevicesBinding;
import id.kuato.woahelper.databinding.LangaugesBinding;

public class MainActivity extends AppCompatActivity  {


    ExecutorService executorService = Executors.newFixedThreadPool(4);
    public ActivityMainBinding x;
    public DevicesBinding d;
    public SetPanelBinding k;
    public ToolboxBinding n;
    public ScriptsBinding z;
    public LangaugesBinding l;
    private Context context;
    int ram;
    public String winpath;
    double ramvalue;
    String panel;
    String mounted;
    String finduefi;
    String device;
    String model;
    public int backable;
    public int depth;
    String win;
    boolean dual;
    String grouplink = "https://t.me/woahelperchat";
    String guidelink = "https://github.com/n00b69/woa-vayu";
    String currentVersion = BuildConfig.VERSION_NAME;
    static final Object lock = new Object();

    private void copyAssets() {
        ShellUtils.fastCmd(String.format("rm %s/*", getFilesDir()));
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException ignored) {
        }
        assert files != null;
        for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);

                String outDir = String.valueOf(getFilesDir());

                File outFile = new File(outDir, filename);

                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (IOException ignored) {
            }
        }
        ShellUtils.fastCmd("chmod 777 " + getFilesDir() + "/busybox");
        pref.setbusybox(this, getFilesDir() + "/busybox ");
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "WoA helper";
            String description = "Windows on ARM assistant";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("IMPORTANCE_NONE", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (!Objects.equals(currentVersion, pref.getVersion(this))) {
            copyAssets();
            pref.setVersion(this, currentVersion);
        } else {
            String[] files = {"busybox", "sta.exe", "Switch to Android.lnk", "usbhostmode.exe", "ARMSoftware.url", "TestedSoftware.url", "WorksOnWoa.url", "RotationShortcut.lnk", "display.exe", "RemoveEdge.ps1", "autoflasher.lnk", "DefenderRemover.exe"};
            int i = 0;
            while (!files[i].isEmpty()) {
                if (ShellUtils.fastCmd(String.format("ls %1$s |grep %2$s", getFilesDir(), files[i])).isEmpty()) {
                    copyAssets();
                    break;
                }
                i++;
            }
        }
        //createNotificationChannel();
        super.onCreate(savedInstanceState);
        final Dialog dialog = new Dialog(MainActivity.this);
        final Dialog languages = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog);
        languages.setContentView(R.layout.langauges);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        languages.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        x = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(x.getRoot());
        backable = 0;
        d = DevicesBinding.inflate(getLayoutInflater());
        x.toolbarlayout.settings.setVisibility(View.VISIBLE);
        k = SetPanelBinding.inflate(getLayoutInflater());
        n = ToolboxBinding.inflate(getLayoutInflater());
        z = ScriptsBinding.inflate(getLayoutInflater());
        Drawable iconToolbar = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_launcher_foreground, null);
        setSupportActionBar(x.toolbarlayout.toolbar);
        x.toolbarlayout.toolbar.setTitle(getString(R.string.app_name));
        x.toolbarlayout.toolbar.setSubtitle("v" + currentVersion);
        x.toolbarlayout.toolbar.setNavigationIcon(iconToolbar);
        checkdevice();
        checkuefi();
        win = ShellUtils.fastCmd("realpath /dev/block/by-name/win");
        if (win.isEmpty())
            win = ShellUtils.fastCmd("realpath /dev/block/by-name/mindows");
        Log.d("debug",win);
        winpath = (pref.getMountLocation(MainActivity.this)?"/mnt/Windows":"/mnt/sdcard/Windows");
        String mount_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
        if (mount_stat.isEmpty())
            mounted = "MOUNT";
        else
            mounted = "UNMOUNT";
        if (pref.getMODEM(this))
            n.cvDumpModem.setVisibility(View.GONE);
        x.tvMnt.setText(String.format(getString(R.string.mnt_title), mounted));
        x.tvBackup.setText(getString(R.string.backup_boot_title));
        x.tvBackupSub.setText(getString(R.string.backup_boot_subtitle));
        x.tvMntSubtitle.setText(getString(R.string.mnt_subtitle));
        n.tvModemSubtitle.setText(getString(R.string.dump_modem_subtitle));
        x.tvDate.setText(String.format(getString(R.string.last), pref.getDATE(this)));
        MaterialButton yesButton = dialog.findViewById(R.id.yes);
        MaterialButton noButton = dialog.findViewById(R.id.no);
        MaterialButton dismissButton = dialog.findViewById(R.id.dismiss);
        TextView messages = dialog.findViewById(R.id.messages);
        ImageView icons = dialog.findViewById(R.id.icon);
        ProgressBar bar = dialog.findViewById(R.id.progress);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        Drawable adrod = ResourcesCompat.getDrawable(getResources(), R.drawable.adrod2, null);
        Drawable adrod2 = ResourcesCompat.getDrawable(getResources(), R.drawable.adrod4, null);
        Drawable atlasos = ResourcesCompat.getDrawable(getResources(), R.drawable.atlasos2, null);
        Drawable uefi = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_uefi, null);
        Drawable boot = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_disk, null);
        Drawable sensors = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sensor, null);
        Drawable edge = ResourcesCompat.getDrawable(getResources(), R.drawable.edge2, null);
        Drawable download = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_download, null);
        Drawable modem = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_modem, null);
        Drawable mnt = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_mnt, null);
        Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_launcher_foreground, null);
        Drawable settings = ResourcesCompat.getDrawable(getResources(), R.drawable.settings, null);
        settings.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(getColor(R.color.colorPrimary), BlendModeCompat.SRC_IN));
        Drawable back = ResourcesCompat.getDrawable(getResources(), R.drawable.back_arrow, null);
        back.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(getColor(R.color.colorPrimary), BlendModeCompat.SRC_IN));


        d.toolbarlayout.back.setImageDrawable(back);
        x.toolbarlayout.settings.setImageDrawable(settings);
        String slot = ShellUtils.fastCmd("getprop ro.boot.slot_suffix");
        if (!slot.isEmpty())
            x.tvSlot.setText(String.format(getString(R.string.slot), slot.substring(1, 2).toUpperCase()));
        else
            x.tvSlot.setVisibility(View.GONE);

        TextView myTextView00 = findViewById(R.id.tv_date);
        myTextView00.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        x.deviceName.setText(model + " (" + device + ")");
        if (Objects.equals(device, "nabu")) {
            TextView myTextView2 = findViewById(R.id.deviceName);
            myTextView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            TextView myTextView3 = findViewById(R.id.tv_panel);
            myTextView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            TextView myTextView4 = findViewById(R.id.tv_ramvalue);
            myTextView4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            TextView myTextView5 = findViewById(R.id.guide_text);
            myTextView5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            TextView myTextView6 = findViewById(R.id.group_text);
            myTextView6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            TextView myTextView7 = findViewById(R.id.tv_slot);
            myTextView7.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            TextView myTextView8 = findViewById(R.id.tv_date);
            myTextView8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            TextView myTextView9 = findViewById(R.id.tv_slot);
            myTextView9.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            x.NabuImage.setVisibility(View.VISIBLE);
            x.NabuImage.setVisibility(View.VISIBLE);
            x.VayuImage.setVisibility(View.GONE);
            n.tvDumpModem.setVisibility(View.GONE);
            n.cvDumpModem.setVisibility(View.GONE);
            int orientation = this.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                x.app.setOrientation(LinearLayout.VERTICAL);
                x.top.setOrientation(LinearLayout.HORIZONTAL);
            } else {
                x.app.setOrientation(LinearLayout.HORIZONTAL);
                x.top.setOrientation(LinearLayout.VERTICAL);
            }
            x.tvPanel.setVisibility(View.VISIBLE);
            guidelink = "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/tree/main";
            grouplink = "https://t.me/nabuwoa";
        } else {

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            TextView myTextView1 = findViewById(R.id.text);
            myTextView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            TextView myTextView2 = findViewById(R.id.deviceName);
            myTextView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            TextView myTextView3 = findViewById(R.id.tv_panel);
            myTextView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            TextView myTextView4 = findViewById(R.id.tv_ramvalue);
            myTextView4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            TextView myTextView5 = findViewById(R.id.guide_text);
            myTextView5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            TextView myTextView6 = findViewById(R.id.group_text);
            myTextView6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

            switch (device) {
                case "cepheus": {
                    guidelink = "https://github.com/woacepheus/Port-Windows-11-Xiaomi-Mi-9";
                    grouplink = "http://t.me/woacepheus";
                    Drawable cepheus = ResourcesCompat.getDrawable(getResources(), R.drawable.cepheus, null);
                    x.VayuImage.setImageDrawable(cepheus);
                    x.tvPanel.setVisibility(View.VISIBLE);
                    break;
                }
                case "raphael":
                case "raphaelin":
                case "raphaels": {
                    guidelink = "https://github.com/woa-raphael/woa-raphael";
                    grouplink = "https://t.me/woaraphael";
                    Drawable raphael = ResourcesCompat.getDrawable(getResources(), R.drawable.raphael, null);
                    x.VayuImage.setImageDrawable(raphael);
                    x.tvPanel.setVisibility(View.VISIBLE);
                    break;
                }
                case "mh2lm":
                case "mh2plus":
                case "mh2plus_lao_com":
                case "mh2lm_lao_com": {
                    guidelink = "https://github.com/n00b69/woa-mh2lm";
                    grouplink = "https://t.me/woahelperchat";
                    Drawable g8x = ResourcesCompat.getDrawable(getResources(), R.drawable.mh2lm, null);
                    x.VayuImage.setImageDrawable(g8x);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "mh2lm5g":
                case "mh2lm5g_lao_com": {
                    guidelink = "https://github.com/n00b69/woa-mh2lm5g";
                    grouplink = "https://t.me/woahelperchat";
                    Drawable g8x = ResourcesCompat.getDrawable(getResources(), R.drawable.mh2lm, null);
                    x.VayuImage.setImageDrawable(g8x);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "flashlmdd":
                case "flashlmdd_lao_com": {
                    guidelink = "https://github.com/n00b69/woa-flashlmdd";
                    grouplink = "https://t.me/woahelperchat";
                    Drawable flashlmdd = ResourcesCompat.getDrawable(getResources(), R.drawable.flashlmdd, null);
                    x.VayuImage.setImageDrawable(flashlmdd);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "alphalm":
                case "alphaplus":
                case "alphalm_lao_com":
                case "alphaplus_lao_com": {
                    guidelink = "https://github.com/n00b69/woa-alphaplus";
                    grouplink = "https://t.me/woahelperchat";
                    Drawable alphaplus = ResourcesCompat.getDrawable(getResources(), R.drawable.alphaplus, null);
                    x.VayuImage.setImageDrawable(alphaplus);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "betaplus":
                case "betalm":
                case "betaplus_lao_com":
                case "betalm_lao_com": {
                    guidelink = "https://github.com/n00b69/woa-betalm";
                    grouplink = "https://t.me/woahelperchat";
                    Drawable betalm = ResourcesCompat.getDrawable(getResources(), R.drawable.betalm, null);
                    x.VayuImage.setImageDrawable(betalm);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "bhima":
                case "vayu": {
                    guidelink = "https://github.com/n00b69/woa-vayu";
                    grouplink = "https://t.me/woahelperchat";
                    Drawable vayu = ResourcesCompat.getDrawable(getResources(), R.drawable.vayu, null);
                    x.VayuImage.setImageDrawable(vayu);
                    x.tvPanel.setVisibility(View.VISIBLE);
                    break;
                }
                case "beryllium": {
                    guidelink = "https://github.com/n00b69/woa-beryllium";
                    grouplink = "https://t.me/WinOnF1";
                    Drawable beryllium = ResourcesCompat.getDrawable(getResources(), R.drawable.beryllium, null);//NO IMAGE
                    x.VayuImage.setImageDrawable(beryllium);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "chiron": {
                    guidelink = "https://renegade-project.tech/";
                    grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    Drawable chiron = ResourcesCompat.getDrawable(getResources(), R.drawable.chiron, null);
                    x.VayuImage.setImageDrawable(chiron);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "dipper": {
                    guidelink = "https://github.com/n00b69/woa-dipper";
                    grouplink = "https://t.me/woadipper";
                    Drawable dipper = ResourcesCompat.getDrawable(getResources(), R.drawable.dipper, null);
                    x.VayuImage.setImageDrawable(dipper);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "OnePlus6T":
                case "enchilada": {
                    guidelink = "https://github.com/WoA-OnePlus-6-Series/WoA-on-OnePlus6-Series";
                    grouplink = "https://t.me/WinOnOP6";
                    Drawable enchilada = ResourcesCompat.getDrawable(getResources(), R.drawable.enchilada, null);
                    x.VayuImage.setImageDrawable(enchilada);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "equuleus": {
                    guidelink = "https://github.com/n00b69/woa-equuleus";
                    grouplink = "https://t.me/woaequuleus";
                    Drawable equuleus = ResourcesCompat.getDrawable(getResources(), R.drawable.equuleus, null);
                    x.VayuImage.setImageDrawable(equuleus);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "OnePlus6":
                case "fajita": {
                    guidelink = "https://github.com/WoA-OnePlus-6-Series/WoA-on-OnePlus6-Series";
                    grouplink = "https://t.me/WinOnOP6";
                    Drawable fajita = ResourcesCompat.getDrawable(getResources(), R.drawable.fajita, null);
                    x.VayuImage.setImageDrawable(fajita);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "lisa": {
                    guidelink = "https://github.com/ETCHDEV/Port-Windows-11-Xiaomi-11-Lite-NE";
                    grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    Drawable lisa = ResourcesCompat.getDrawable(getResources(), R.drawable.lisa, null);
                    x.VayuImage.setImageDrawable(lisa);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "curtana":
                case "curtana2":
                case "curtana_india":
                case "curtana_cn":
                case "curtanacn":
                case "durandal":
                case "durandal_india":
                case "excalibur":
                case "excalibur2":
                case "excalibur_india":
                case "gram":
                case "joyeuse":
                case "miatoll": {
                    guidelink = "https://github.com/woa-miatoll/Port-Windows-11-Redmi-Note-9-Pro";
                    grouplink = "http://t.me/woamiatoll";
                    Drawable miatoll = ResourcesCompat.getDrawable(getResources(), R.drawable.miatoll, null);
                    x.VayuImage.setImageDrawable(miatoll);
                    n.cvDumpModem.setVisibility(View.GONE);
                    x.tvPanel.setVisibility(View.VISIBLE);
                    break;
                }
                case "polaris": {
                    guidelink = "https://github.com/n00b69/woa-polaris";
                    grouplink = "https://t.me/WinOnMIX2S";
                    Drawable polaris = ResourcesCompat.getDrawable(getResources(), R.drawable.polaris, null);
                    x.VayuImage.setImageDrawable(polaris);
                    n.cvDumpModem.setVisibility(View.GONE);
                    x.tvPanel.setVisibility(View.VISIBLE);
                    break;
                }
                case "venus": {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/woahelperchat";
                    Drawable venus = ResourcesCompat.getDrawable(getResources(), R.drawable.venus, null);
                    x.VayuImage.setImageDrawable(venus);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "xpeng": {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/woahelperchat";
                    Drawable xpeng = ResourcesCompat.getDrawable(getResources(), R.drawable.xpeng, null);
                    x.VayuImage.setImageDrawable(xpeng);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "alioth": {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/woahelperchat";
                    Drawable alioth = ResourcesCompat.getDrawable(getResources(), R.drawable.alioth, null);
                    x.VayuImage.setImageDrawable(alioth);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "pipa": {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/pad6_pipa";
                    Drawable pipa = ResourcesCompat.getDrawable(getResources(), R.drawable.pipa, null);
                    x.VayuImage.setImageDrawable(pipa);
                    x.tvPanel.setVisibility(View.VISIBLE);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "perseus": {
                    guidelink = "https://github.com/n00b69/woa-perseus";
                    grouplink = "https://t.me/woaperseus";
                    Drawable perseus = ResourcesCompat.getDrawable(getResources(), R.drawable.perseus, null);
                    x.VayuImage.setImageDrawable(perseus);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "Pong":
                case "pong": {
                    guidelink = "https://github.com/Govro150/woa-pong";
                    grouplink = "https://t.me/woa_pong";
                    Drawable pong = ResourcesCompat.getDrawable(getResources(), R.drawable.pong, null);
                    x.VayuImage.setImageDrawable(pong);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "surya": {
                    guidelink = "https://github.com/SebastianZSXS/Poco-X3-NFC-WindowsARM";
                    grouplink = "https://t.me/windows_on_pocox3_nfc";
                    Drawable vayu = ResourcesCompat.getDrawable(getResources(), R.drawable.vayu, null);
                    x.VayuImage.setImageDrawable(vayu);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "cheeseburger": {
                    guidelink = "https://renegade-project.tech/";
                    grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    Drawable cheeseburger = ResourcesCompat.getDrawable(getResources(), R.drawable.cheeseburger, null);
                    x.VayuImage.setImageDrawable(cheeseburger);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "dumpling": {
                    guidelink = "https://renegade-project.tech/";
                    grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    Drawable dumpling = ResourcesCompat.getDrawable(getResources(), R.drawable.dumpling, null);
                    x.VayuImage.setImageDrawable(dumpling);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "G973F":
                case "SM-G973F":
                case "beyond1lte":
                case "beyond1qlte":
                case "G973U":
                case "G973U1":
                case "SM-G973U":
                case "SM-G973U1":
                case "G9730":
                case "SM-G9730":
                case "G973N":
                case "SM-G973N":
                case "G973X":
                case "SM-G973X":
                case "G973C":
                case "SM-G973C":
                case "SCV41":
                case "SM-SC41":
                case "beyond1": {
                    guidelink = "https://github.com/sonic011gamer/Mu-Samsung";
                    grouplink = "https://t.me/woahelperchat";
                    Drawable beyond1 = ResourcesCompat.getDrawable(getResources(), R.drawable.beyond1, null);
                    x.VayuImage.setImageDrawable(beyond1);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "a52sxq": {
                    guidelink = "https://github.com/arminask/Port-Windows-11-Galaxy-A52s-5G";
                    grouplink = "https://t.me/a52sxq_uefi";
                    Drawable a52sxq = ResourcesCompat.getDrawable(getResources(), R.drawable.a52sxq, null);
                    x.VayuImage.setImageDrawable(a52sxq);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "judyln":
                case "judyp":
                case "judypn": {
                    guidelink = "https://github.com/n00b69/woa-everything";
                    grouplink = "https://t.me/lgedevices";
                    Drawable unknown = ResourcesCompat.getDrawable(getResources(), R.drawable.unknown, null);
                    x.VayuImage.setImageDrawable(unknown);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "joan": {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/lgedevices";
                    Drawable unknown = ResourcesCompat.getDrawable(getResources(), R.drawable.unknown, null);
                    x.VayuImage.setImageDrawable(unknown);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "andromeda": {
                    guidelink = "https://woa-msmnile.github.io/";
                    grouplink = "https://t.me/woa_msmnile_issues";
                    Drawable unknown = ResourcesCompat.getDrawable(getResources(), R.drawable.unknown, null);
                    x.VayuImage.setImageDrawable(unknown);
                    break;
                }
                case "guacamoleb":
                case "hotdogb":
                case "OnePlus7T":
                case "OnePlus7": {
                    guidelink = "https://woa-msmnile.github.io/";
                    grouplink = "https://t.me/onepluswoachat";
                    Drawable unknown = ResourcesCompat.getDrawable(getResources(), R.drawable.unknown, null);
                    x.VayuImage.setImageDrawable(unknown);
                    break;
                }
                case "hotdog":
                case "OnePlus7TPro":
                case "OnePlus7TPro4G": {
                    guidelink = "https://woa-msmnile.github.io/";
                    grouplink = "https://t.me/onepluswoachat";
                    Drawable hotdog = ResourcesCompat.getDrawable(getResources(), R.drawable.hotdog, null);
                    x.VayuImage.setImageDrawable(hotdog);
                    break;
                }
                case "OnePlus7TPro5G":
                case "OnePlus7TProNR": {
                    guidelink = "https://woa-msmnile.github.io/";
                    grouplink = "https://t.me/onepluswoachat";
                    Drawable hotdog = ResourcesCompat.getDrawable(getResources(), R.drawable.hotdog, null);
                    x.VayuImage.setImageDrawable(hotdog);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "guacamole":
                case "OnePlus7Pro":
                case "hotdogg":
                case "OnePlus7Pro4G":
                case "OP7ProNRSpr": {
                    guidelink = "https://woa-msmnile.github.io/";
                    grouplink = "https://t.me/onepluswoachat";
                    Drawable guacamole = ResourcesCompat.getDrawable(getResources(), R.drawable.guacamole, null);
                    x.VayuImage.setImageDrawable(guacamole);
                    break;
                }
                case "sagit": {
                    guidelink = "https://renegade-project.tech/";
                    grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    Drawable unknown = ResourcesCompat.getDrawable(getResources(), R.drawable.unknown, null);
                    x.VayuImage.setImageDrawable(unknown);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
				case "winnerx":
                case "winner": {
                    guidelink = "https://github.com/n00b69/woa-winner";
                    grouplink = "https://t.me/woa_msmnile_issues";
                    Drawable winner = ResourcesCompat.getDrawable(getResources(), R.drawable.winner, null);
                    x.VayuImage.setImageDrawable(winner);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "q2q": {
                    guidelink = "https://woa-msmnile.github.io/";
                    grouplink = "https://t.me/woa_msmnile_issues";
                    Drawable q2q = ResourcesCompat.getDrawable(getResources(), R.drawable.q2q, null);
                    x.VayuImage.setImageDrawable(q2q);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "RMX2061": {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/realme6PROwindowsARM64";
                    Drawable rmx2061 = ResourcesCompat.getDrawable(getResources(), R.drawable.rmx2061, null);
                    x.VayuImage.setImageDrawable(rmx2061);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "RMX2170": {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/realme6PROwindowsARM64";
                    Drawable rmx2170 = ResourcesCompat.getDrawable(getResources(), R.drawable.rmx2170, null);
                    x.VayuImage.setImageDrawable(rmx2170);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "cmi": {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/dumanthecat";
                    Drawable cmi = ResourcesCompat.getDrawable(getResources(), R.drawable.cmi, null);
                    x.VayuImage.setImageDrawable(cmi);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "houji": {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/dumanthecat";
                    Drawable houji = ResourcesCompat.getDrawable(getResources(), R.drawable.houji, null);
                    x.VayuImage.setImageDrawable(houji);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "marble": {
                    guidelink = "https://github.com/Xhdsos/woa-marble";
                    grouplink = "https://t.me/woa_marble";
                    Drawable marble = ResourcesCompat.getDrawable(getResources(), R.drawable.marble, null);
                    x.VayuImage.setImageDrawable(marble);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                case "davinci": {
                    guidelink = "https://github.com/zxcwsurx/woa-davinci";
                    grouplink = "https://t.me/woa_davinci";
                    Drawable raphael = ResourcesCompat.getDrawable(getResources(), R.drawable.raphael, null);
                    x.VayuImage.setImageDrawable(raphael);
                    n.cvDumpModem.setVisibility(View.GONE);
                    break;
                }
                default: {
                    Drawable unknown = ResourcesCompat.getDrawable(getResources(), R.drawable.unknown, null);
                    x.VayuImage.setImageDrawable(unknown);
                    x.deviceName.setText(device);
                    guidelink = "https://renegade-project.tech/";
                    grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    break;
                }
            }
        }
        x.tvRamvalue.setText(String.format(getString(R.string.ramvalue), ramvalue));
        x.tvPanel.setText(String.format(getString(R.string.paneltype), panel));
        x.cvGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(guidelink));
                startActivity(i);
            }
        });

        x.cvGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(grouplink));
                startActivity(i);
            }
        });

        x.cvQuickBoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowBlur();
                noButton.setText(getString(R.string.no));
                yesButton.setText(getString(R.string.yes));
                yesButton.setVisibility(View.VISIBLE);
                dismissButton.setVisibility(View.GONE);
                noButton.setVisibility(View.VISIBLE);
                icons.setVisibility(View.VISIBLE);
                icons.setImageDrawable(icon);
                messages.setText(getString(R.string.quickboot_question));
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        yesButton.setVisibility(View.GONE);
                        noButton.setVisibility(View.GONE);
                        messages.setText(getString(R.string.please_wait));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mount();
                                    String[] dumped = {"bhima", "cepheus", "guacamole", "guacamoleb", "hotdog", "hotdogb", "OnePlus7", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7T", "OnePlus7TPro", "OnePlus7TPro4G", "raphael", "raphaelin", "raphaels", "vayu"};
                                    if (Arrays.asList(dumped).contains(device) || !(pref.getMODEM(MainActivity.this))) {
                                        dump();
                                    }
                                    String found = ShellUtils.fastCmd("ls "+(pref.getMountLocation(MainActivity.this)?"/mnt/Windows":"/mnt/sdcard/Windows")+" | grep boot.img");
									if (!pref.getMountLocation(MainActivity.this)) {
           								if (pref.getBACKUP(MainActivity.this)
										|| (!pref.getAUTO(MainActivity.this) && found.isEmpty())) {
											ShellUtils.fastCmd(getString(R.string.backup1));
											SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm");
											String currentDateAndTime = sdf.format(new Date());
											pref.setDATE(MainActivity.this, currentDateAndTime);
        								}
										} else{
            							if (pref.getBACKUP(MainActivity.this)
										|| (!pref.getAUTO(MainActivity.this) && found.isEmpty())) {
											ShellUtils.fastCmd(getString(R.string.backup2));
											SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm");
											String currentDateAndTime = sdf.format(new Date());
											pref.setDATE(MainActivity.this, currentDateAndTime);
                                  	  }
									}
                                    found = ShellUtils.fastCmd("find /sdcard | grep boot.img");
                                    if (pref.getBACKUP_A(MainActivity.this)
                                            || (!pref.getAUTO_A(MainActivity.this) && found.isEmpty())) {
                                        	ShellUtils.fastCmd(getString(R.string.backup));
                                        	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm");
                                        	String currentDateAndTime = sdf.format(new Date());
                                        	pref.setDATE(MainActivity.this, currentDateAndTime);
                                    }
									
                                    flash(finduefi);
                                    ShellUtils.fastCmd("su -c svc power reboot");
                                    messages.setText(getString(R.string.wrong));
                                    dismissButton.setVisibility(View.VISIBLE);
                                } catch (Exception error) {
                                    error.printStackTrace();
                                }
                            }
                        }, 500);
                    }
                });
                dismissButton.setText(getString(R.string.dismiss));
                dismissButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        });
        x.cvMnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noButton.setText(getString(R.string.no));
                noButton.setVisibility(View.VISIBLE);
                yesButton.setText(getString(R.string.yes));
                dismissButton.setText(getString(R.string.dismiss));
                yesButton.setVisibility(View.VISIBLE);
                dismissButton.setVisibility(View.GONE);
                ShowBlur();
                icons.setVisibility(View.VISIBLE);
                icons.setImageDrawable(mnt);
                String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                if (mnt_stat.isEmpty())
                    messages.setText(getString(R.string.mount_question));
                else
                    messages.setText(getString(R.string.unmount_question));

                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        yesButton.setVisibility(View.GONE);
                        noButton.setVisibility(View.GONE);
                        messages.setText(getString(R.string.please_wait));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.d("debug",winpath);
                                    String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                                    if (mnt_stat.isEmpty()) {
                                        mount();
                                        mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                                        if (mnt_stat.isEmpty()) {
                                            noButton.setVisibility(View.GONE);
                                            yesButton.setText(getString(R.string.chat));
                                            dismissButton.setText(getString(R.string.cancel));
                                            yesButton.setVisibility(View.VISIBLE);
                                            dismissButton.setVisibility(View.VISIBLE);
                                            icons.setVisibility(View.GONE);
                                            ShowBlur();
                                            messages.setText(getString(R.string.ntfs));
                                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    HideBlur();
                                                    dialog.dismiss();
                                                    icons.setVisibility(View.VISIBLE);
                                                }
                                            });
                                            yesButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                                    i.setData(Uri.parse("https://t.me/woahelperchat"));
                                                    startActivity(i);
                                                }
                                            });

                                            dialog.setCancelable(false);
                                            dialog.show();
                                        } else {
                                            Log.d("debug",pref.getMountLocation(MainActivity.this) ? "/mnt/Windows" : "/mnt/sdcard/Windows");
                                            messages.setText(getString(R.string.mounted) + "\n" + winpath);
                                        }
                                    } else {
                                        unmount();
                                        messages.setText(getString(R.string.unmounted));
                                    }
                                    dismissButton.setText(getString(R.string.dismiss));
                                    dismissButton.setVisibility(View.VISIBLE);
                                } catch (Exception error) {
                                    error.printStackTrace();
                                }
                            }

                        }, 500);
                    }

                });

                dismissButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                dialog.setCancelable(false);
                dialog.show();

            }

        });

        n.cvDumpModem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowBlur();
                noButton.setText(getString(R.string.no));
                dismissButton.setText(getString(R.string.dismiss));
                yesButton.setText(getString(R.string.yes));
                dismissButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.VISIBLE);
                noButton.setVisibility(View.VISIBLE);
                icons.setVisibility(View.VISIBLE);
                icons.setImageDrawable(modem);
                messages.setText(getString(R.string.dump_modem_question));
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setVisibility(View.GONE);
                        messages.setText(getString(R.string.please_wait));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                                if (mnt_stat.isEmpty()) {
                                    mount();
                                    dump();
                                    unmount();
                                } else
                                    dump();
                                messages.setText(getString(R.string.lte));
                                dismissButton.setVisibility(View.VISIBLE);
                            }
                        }, 500);
                    }
                });
                dismissButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        });

        n.cvScripts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_out));
                setContentView(z.getRoot());
                z.scriptstab.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_in));
                backable=1;
                z.toolbarlayout.settings.setVisibility(View.GONE);
                z.toolbarlayout.toolbar.setTitle(getString(R.string.script_title));
                z.toolbarlayout.toolbar.setNavigationIcon(iconToolbar);
            }
        });


        x.cvToolbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x.mainlayout.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_out));
                setContentView(n.getRoot());
                n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_in));
                backable=1;
                n.toolbarlayout.settings.setVisibility(View.GONE);
                n.toolbarlayout.toolbar.setTitle(getString(R.string.toolbox_title));
                n.toolbarlayout.toolbar.setNavigationIcon(iconToolbar);
            }
        });

        n.cvSta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowBlur();
                noButton.setText(getString(R.string.no));
                yesButton.setText(getString(R.string.yes));
                dismissButton.setVisibility(View.GONE);
                noButton.setVisibility(View.VISIBLE);
                yesButton.setVisibility(View.VISIBLE);
                messages.setText(getString(R.string.sta_question));
                icons.setVisibility(View.VISIBLE);
                icons.setImageDrawable(adrod);
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setVisibility(View.GONE);
                        dismissButton.setVisibility(View.GONE);
                        try {
                            mount();
                            ShellUtils.fastCmd("su -c \'if [ -e /dev/block/by-name/boot_a ] && [ -e /dev/block/by-name/boot_b ]; then boot_a=$(basename $(readlink -f /dev/block/by-name/boot_a)); boot_b=$(basename $(readlink -f /dev/block/by-name/boot_b)); printf \"%s\n%s\n%s\n%s\n\" \"$boot_a\" \"C:\\boot.img\" \"$boot_b\" \"C:\\boot.img\" > /sdcard/sta.conf; else boot=$(basename $(readlink -f /dev/block/by-name/boot)); printf \"%s\n%s\n\" \"$boot\" \"C:\\boot.img\" > /sdcard/sta.conf; fi\'");
                            ShellUtils.fastCmd("cp "+getFilesDir()+"/sta.exe /sdcard/sta.exe");
                            String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                            if (mnt_stat.isEmpty()) {
                                noButton.setVisibility(View.GONE);
                                yesButton.setText(getString(R.string.chat));
                                dismissButton.setText(getString(R.string.cancel));
                                yesButton.setVisibility(View.VISIBLE);
                                dismissButton.setVisibility(View.VISIBLE);
                                icons.setVisibility(View.GONE);
                                ShowBlur();
                                messages.setText(getString(R.string.ntfs));
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        icons.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                });
                                yesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse("https://t.me/woahelperchat"));
                                        startActivity(i);
                                    }
                                });
                                dialog.setCancelable(false);
                                dialog.show();
                            } else {
                                ShellUtils.fastCmd("mkdir "+winpath+"/Windows/sta || true ");
                                ShellUtils.fastCmd("cp /sdcard/sta.conf "+winpath+"/Windows/sta");
                                ShellUtils.fastCmd("cp "+getFilesDir()+"/sta.exe "+winpath+"/sta/sta.exe");
                                ShellUtils.fastCmd("cp \'"+getFilesDir()+"/Switch to Android.lnk\' "+winpath+"/Users/Default/Desktop");
                                messages.setText(getString(R.string.done));
                                dismissButton.setText(getString(R.string.dismiss));
                                dismissButton.setVisibility(View.VISIBLE);
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        dialog.dismiss();
                                    }
                                });
                            }
                            dismissButton.setText(getString(R.string.dismiss));
                            dismissButton.setVisibility(View.VISIBLE);
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    HideBlur();
                                    dialog.dismiss();
                                }
                            });
                        } catch (Exception error) {
                            error.printStackTrace();
                        }
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        });

        n.cvAtlasos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowBlur();
                yesButton.setText("AtlasOS");
                noButton.setText("ReviOS");
                dismissButton.setText(getString(R.string.dismiss));
                dismissButton.setVisibility(View.VISIBLE);
                noButton.setVisibility(View.VISIBLE);
                yesButton.setVisibility(View.VISIBLE);
                messages.setText(getString(R.string.atlasos_question));
                icons.setVisibility(View.VISIBLE);
                icons.setImageDrawable(atlasos);
                if (!isNetworkConnected(MainActivity.this)){
                    noButton.setVisibility(View.GONE);
                    yesButton.setVisibility(View.GONE);
                    messages.setText("No internet connection\nConnect to internet and try again");
                }
                dismissButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        noButton.setVisibility(View.GONE);
                        yesButton.setVisibility(View.GONE);
                        dismissButton.setVisibility(View.GONE);
                        bar.setVisibility(View.VISIBLE);
                        bar.setProgress(0);
                        icons.setVisibility(View.VISIBLE);
                        icons.setImageDrawable(download);
                        messages.setText(getString(R.string.please_wait));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                icons.setVisibility(View.VISIBLE);
                                icons.setImageDrawable(download);
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/modified-playbooks/releases/download/ReviOS/Revi-PB-24.06.apbx -O /sdcard/Revi-PB-24.06.apbx");
                                bar.setProgress((int) (bar.getMax() * 0.5), true);
                                ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://download.ameliorated.io/AME%20Wizard%20Beta.zip -O /sdcard/AMEWizardBeta.zip");
                                bar.setProgress((int) (bar.getMax()*0.8), true);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mount();
                                        String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                                        if (mnt_stat.isEmpty()) {
                                            icons.setVisibility(View.GONE);
                                            yesButton.setVisibility(View.VISIBLE);
                                            messages.setText(getString(R.string.ntfs)+"\nFiles downloaded in internal storage");
                                        }
                                        else {
                                            icons.setImageDrawable(atlasos);
                                            ShellUtils.fastCmd("mkdir "+winpath+"/Toolbox || true ");
                                            ShellUtils.fastCmd("cp /sdcard/Revi-PB-24.06.apbx "+winpath+"/Toolbox/Revi-PB-24.06.apbx");
                                            ShellUtils.fastCmd("cp /sdcard/AMEWizardBeta.zip "+winpath+"/Toolbox");
                                            bar.setProgress((int) (bar.getMax()), true);
                                            messages.setText(getString(R.string.done));
                                        }
                                        dismissButton.setVisibility(View.VISIBLE);
                                        bar.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }).start();
                        try {
                            mount();
                            String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                            if (mnt_stat.isEmpty()) {
                                noButton.setVisibility(View.GONE);
                                yesButton.setText(getString(R.string.chat));
                                dismissButton.setText(getString(R.string.dismiss));

                                ShowBlur();
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        icons.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                });
                                yesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse("https://t.me/woahelperchat"));
                                        startActivity(i);
                                    }
                                });
                                dialog.setCancelable(false);
                                dialog.show();
                            } else {
                                dismissButton.setText(getString(R.string.dismiss));
                                //dismissButton.setVisibility(View.VISIBLE);
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        icons.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                });
                            }
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    HideBlur();
                                    icons.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                }
                            });
                        } catch (Exception error) {
                            error.printStackTrace();
                        }

                    }
                });
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setVisibility(View.GONE);
                        dismissButton.setVisibility(View.GONE);
                        bar.setVisibility(View.VISIBLE);
                        bar.setProgress(0);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                icons.setVisibility(View.VISIBLE);
                                icons.setImageDrawable(download);
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/modified-playbooks/releases/download/AtlasOS/AtlasPlaybook_v0.4.0.apbx -O /sdcard/AtlasPlaybook_v0.4.0.apbx");
                                bar.setProgress((int) (bar.getMax() * 0.5), true);
                                ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://download.ameliorated.io/AME%20Wizard%20Beta.zip -O /sdcard/AMEWizardBeta.zip");
                                bar.setProgress((int) (bar.getMax()*0.8), true);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mount();
                                        String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                                        Log.d("path",mnt_stat);
                                        if (mnt_stat.isEmpty()) {
                                            yesButton.setVisibility(View.VISIBLE);
                                            icons.setVisibility(View.GONE);
                                            messages.setText(getString(R.string.ntfs)+"\nFiles downloaded in internal storage");
                                        }
                                        else {
                                            icons.setImageDrawable(atlasos);
                                            ShellUtils.fastCmd("mkdir "+winpath+"/Toolbox || true ");
                                            ShellUtils.fastCmd("cp /sdcard/AtlasPlaybook_v0.4.0.apbx "+winpath+"/Toolbox/AtlasPlaybook_v0.4.0.apbx");
                                            ShellUtils.fastCmd("cp /sdcard/AMEWizardBeta.zip "+winpath+"/Toolbox");
                                            bar.setProgress(bar.getMax(),true);
                                            messages.setText(getString(R.string.done));
                                        }
                                        dismissButton.setVisibility(View.VISIBLE);
                                        bar.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }).start();
                        messages.setText(getString(R.string.please_wait));
                        try {
                            mount();
                            String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                            if (mnt_stat.isEmpty()) {
                                noButton.setVisibility(View.GONE);
                                yesButton.setText(getString(R.string.chat));
                                dismissButton.setText(getString(R.string.dismiss));
                                ShowBlur();
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        icons.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                });
                                yesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse("https://t.me/woahelperchat"));
                                        startActivity(i);
                                    }
                                });
                                dialog.setCancelable(false);
                                dialog.show();
                            } else {
                                dismissButton.setText(getString(R.string.dismiss));
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        icons.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                });
                            }
                            dismissButton.setText(getString(R.string.dismiss));
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    HideBlur();
                                    icons.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                }
                            });
                        } catch (Exception error) {
                            error.printStackTrace();
                        }
                    }
                });

                dialog.setCancelable(false);
                dialog.show();
            }
        });

        z.cvUsbhost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowBlur();
                noButton.setText(getString(R.string.no));
                yesButton.setText(getString(R.string.yes));
                dismissButton.setVisibility(View.GONE);
                noButton.setVisibility(View.VISIBLE);
                yesButton.setVisibility(View.VISIBLE);
                messages.setText(getString(R.string.usbhost_question));
                icons.setVisibility(View.VISIBLE);
                icons.setImageDrawable(mnt);
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setVisibility(View.GONE);
                        dismissButton.setVisibility(View.GONE);
                        try {
                            mount();String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);

                            ShellUtils.fastCmd("cp "+getFilesDir()+"/usbhostmode.exe /sdcard");
                            if (mnt_stat.isEmpty()) {
                                noButton.setVisibility(View.GONE);
                                yesButton.setText(getString(R.string.chat));
                                dismissButton.setText(getString(R.string.cancel));
                                yesButton.setVisibility(View.VISIBLE);
                                dismissButton.setVisibility(View.VISIBLE);
                                icons.setVisibility(View.GONE);
                                ShowBlur();
                                messages.setText(getString(R.string.ntfs));
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        icons.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                });
                                yesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse("https://t.me/woahelperchat"));
                                        startActivity(i);
                                    }
                                });
                                dialog.setCancelable(false);
                                dialog.show();
                            } else {
                                ShellUtils.fastCmd("mkdir "+winpath+"/Toolbox || true ");
                                ShellUtils.fastCmd("cp /sdcard/usbhostmode.exe "+winpath+"/Toolbox");
                                messages.setText(getString(R.string.done));
                                dismissButton.setText(getString(R.string.dismiss));
                                dismissButton.setVisibility(View.VISIBLE);
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        dialog.dismiss();
                                    }
                                });
                            }
                            dismissButton.setText(getString(R.string.dismiss));
                            dismissButton.setVisibility(View.VISIBLE);
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    HideBlur();
                                    dialog.dismiss();
                                }
                            });
                        } catch (Exception error) {
                            error.printStackTrace();
                        }
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        });

        z.cvAutoflash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowBlur();
                noButton.setText(getString(R.string.no));
                yesButton.setText(getString(R.string.yes));
                dismissButton.setVisibility(View.GONE);
                noButton.setVisibility(View.VISIBLE);
                yesButton.setVisibility(View.VISIBLE);
                messages.setText(getString(R.string.autoflash_question));
                icons.setVisibility(View.VISIBLE);
                icons.setImageDrawable(adrod2);
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setVisibility(View.GONE);
                        dismissButton.setVisibility(View.GONE);
                        try {
                            mount();
                            String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                            ShellUtils.fastCmd("cp "+getFilesDir()+"/autoflasher.lnk /sdcard");
                            if (mnt_stat.isEmpty()) {
                                noButton.setVisibility(View.GONE);
                                yesButton.setText(getString(R.string.chat));
                                dismissButton.setText(getString(R.string.cancel));
                                yesButton.setVisibility(View.VISIBLE);
                                dismissButton.setVisibility(View.VISIBLE);
                                icons.setVisibility(View.GONE);
                                ShowBlur();
                                messages.setText(getString(R.string.ntfs));
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        icons.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                });
                                yesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse("https://t.me/woahelperchat"));
                                        startActivity(i);
                                    }
                                });
                                dialog.setCancelable(false);
                                dialog.show();
                            } else {
                                ShellUtils.fastCmd("cp /sdcard/autoflasher.lnk \'"+winpath+"/ProgramData/Microsoft/Windows/Start Menu/Programs/Startup\'");
                                messages.setText(getString(R.string.done));
                                dismissButton.setText(getString(R.string.dismiss));
                                dismissButton.setVisibility(View.VISIBLE);
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        dialog.dismiss();
                                    }
                                });
                            }
                            dismissButton.setText(getString(R.string.dismiss));
                            dismissButton.setVisibility(View.VISIBLE);
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    HideBlur();
                                    dialog.dismiss();
                                }
                            });
                        } catch (Exception error) {
                            error.printStackTrace();
                        }
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        });

        z.cvDefender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowBlur();
                noButton.setText(getString(R.string.no));
                yesButton.setText(getString(R.string.yes));
                dismissButton.setVisibility(View.GONE);
                noButton.setVisibility(View.VISIBLE);
                yesButton.setVisibility(View.VISIBLE);
                messages.setText(getString(R.string.defender_question));
                icons.setVisibility(View.VISIBLE);
                icons.setImageDrawable(sensors);
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setVisibility(View.GONE);
                        dismissButton.setVisibility(View.GONE);
                        try {
                            mount();
                            String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                            ShellUtils.fastCmd("cp "+getFilesDir()+"/DefenderRemover.exe /sdcard");
                            if (mnt_stat.isEmpty()) {
                                noButton.setVisibility(View.GONE);
                                yesButton.setText(getString(R.string.chat));
                                dismissButton.setText(getString(R.string.cancel));
                                yesButton.setVisibility(View.VISIBLE);
                                dismissButton.setVisibility(View.VISIBLE);
                                icons.setVisibility(View.GONE);
                                ShowBlur();
                                messages.setText(getString(R.string.ntfs));
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        icons.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                });
                                yesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse("https://t.me/woahelperchat"));
                                        startActivity(i);
                                    }
                                });
                                dialog.setCancelable(false);
                                dialog.show();
                            } else {
                                ShellUtils.fastCmd("mkdir "+winpath+"/Toolbox || true ");
                                ShellUtils.fastCmd("cp /sdcard/DefenderRemover.exe "+winpath+"/Toolbox");
                                messages.setText(getString(R.string.done));
                                dismissButton.setText(getString(R.string.dismiss));
                                dismissButton.setVisibility(View.VISIBLE);
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        dialog.dismiss();
                                    }
                                });
                            }
                            dismissButton.setText(getString(R.string.dismiss));
                            dismissButton.setVisibility(View.VISIBLE);
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    HideBlur();
                                    dialog.dismiss();
                                }
                            });
                        } catch (Exception error) {
                            error.printStackTrace();
                        }
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        });


        z.cvRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowBlur();
                noButton.setText(getString(R.string.no));
                yesButton.setText(getString(R.string.yes));
                dismissButton.setVisibility(View.GONE);
                noButton.setVisibility(View.VISIBLE);
                yesButton.setVisibility(View.VISIBLE);
                messages.setText(getString(R.string.rotation_question));
                icons.setVisibility(View.VISIBLE);
                icons.setImageDrawable(boot);
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setVisibility(View.GONE);
                        dismissButton.setVisibility(View.GONE);
                        try {
                            mount();
                            String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                            ShellUtils.fastCmd("cp "+getFilesDir()+"/display.exe /sdcard/");
                            ShellUtils.fastCmd("cp "+getFilesDir()+"/RotationShortcut.lnk /sdcard/");
                            if (mnt_stat.isEmpty()) {
                                noButton.setVisibility(View.GONE);
                                yesButton.setText(getString(R.string.chat));
                                dismissButton.setText(getString(R.string.cancel));
                                yesButton.setVisibility(View.VISIBLE);
                                dismissButton.setVisibility(View.VISIBLE);
                                icons.setVisibility(View.GONE);
                                ShowBlur();
                                messages.setText(getString(R.string.ntfs));
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        icons.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                });
                                yesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse("https://t.me/woahelperchat"));
                                        startActivity(i);
                                    }
                                });
                                dialog.setCancelable(false);
                                dialog.show();
                            } else {
                                ShellUtils.fastCmd("mkdir "+winpath+"/Toolbox || true ");
                                ShellUtils.fastCmd("mkdir "+winpath+"/Toolbox/Rotation || true ");
                                ShellUtils.fastCmd("cp /sdcard/display.exe "+winpath+"/Toolbox/Rotation");
                                ShellUtils.fastCmd("cp /sdcard/RotationShortcut.lnk "+winpath+"/Toolbox/Rotation");
                                messages.setText(getString(R.string.done));
                                dismissButton.setText(getString(R.string.dismiss));
                                dismissButton.setVisibility(View.VISIBLE);
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        dialog.dismiss();
                                    }
                                });
                            }
                            dismissButton.setText(getString(R.string.dismiss));
                            dismissButton.setVisibility(View.VISIBLE);
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    HideBlur();
                                    dialog.dismiss();
                                }
                            });
                        } catch (Exception error) {
                            error.printStackTrace();
                        }
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        });

        z.cvEdge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowBlur();
                noButton.setText(getString(R.string.no));
                yesButton.setText(getString(R.string.yes));
                dismissButton.setVisibility(View.GONE);
                noButton.setVisibility(View.VISIBLE);
                yesButton.setVisibility(View.VISIBLE);
                messages.setText(getString(R.string.edge_question));
                icons.setVisibility(View.VISIBLE);
                icons.setImageDrawable(edge);
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setVisibility(View.GONE);
                        dismissButton.setVisibility(View.GONE);
                        try {
                            mount();
                            String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                            ShellUtils.fastCmd("cp "+getFilesDir()+"/RemoveEdge.ps1 /sdcard");
                            if (mnt_stat.isEmpty()) {
                                noButton.setVisibility(View.GONE);
                                yesButton.setText(getString(R.string.chat));
                                dismissButton.setText(getString(R.string.cancel));
                                yesButton.setVisibility(View.VISIBLE);
                                dismissButton.setVisibility(View.VISIBLE);
                                icons.setVisibility(View.GONE);
                                ShowBlur();
                                messages.setText(getString(R.string.ntfs));
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        icons.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                });
                                yesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse("https://t.me/woahelperchat"));
                                        startActivity(i);
                                    }
                                });
                                dialog.setCancelable(false);
                                dialog.show();
                            } else {
                                ShellUtils.fastCmd("mkdir "+winpath+"/Toolbox || true ");
                                ShellUtils.fastCmd("cp /sdcard/RemoveEdge.ps1 "+winpath+"/Toolbox");
                                messages.setText(getString(R.string.done));
                                dismissButton.setText(getString(R.string.dismiss));
                                dismissButton.setVisibility(View.VISIBLE);
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        dialog.dismiss();
                                    }
                                });
                            }
                            dismissButton.setText(getString(R.string.dismiss));
                            dismissButton.setVisibility(View.VISIBLE);
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    HideBlur();
                                    dialog.dismiss();
                                }
                            });
                        } catch (Exception error) {
                            error.printStackTrace();
                        }
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        });

        n.cvSoftware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowBlur();
                noButton.setText(getString(R.string.no));
                yesButton.setText(getString(R.string.yes));
                dismissButton.setVisibility(View.GONE);
                noButton.setVisibility(View.VISIBLE);
                yesButton.setVisibility(View.VISIBLE);
                messages.setText(getString(R.string.software_question));
                icons.setVisibility(View.VISIBLE);
                icons.setImageDrawable(sensors);
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setVisibility(View.GONE);
                        dismissButton.setVisibility(View.GONE);
                        try {
                            mount();
                            String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                            ShellUtils.fastCmd("cp "+getFilesDir()+"/WorksOnWoa.url /sdcard");
                            ShellUtils.fastCmd("cp "+getFilesDir()+"/TestedSoftware.url /sdcard");
                            ShellUtils.fastCmd("cp "+getFilesDir()+"/ARMSoftware.url /sdcard");
                            if (mnt_stat.isEmpty()) {
                                noButton.setVisibility(View.GONE);
                                yesButton.setText(getString(R.string.chat));
                                dismissButton.setText(getString(R.string.cancel));
                                yesButton.setVisibility(View.VISIBLE);
                                dismissButton.setVisibility(View.VISIBLE);
                                icons.setVisibility(View.GONE);
                                ShowBlur();
                                messages.setText(getString(R.string.ntfs));
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        icons.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                });
                                yesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse("https://t.me/woahelperchat"));
                                        startActivity(i);
                                    }
                                });
                                dialog.setCancelable(false);
                                dialog.show();
                            } else {
                                ShellUtils.fastCmd("mkdir "+winpath+"/Toolbox || true ");
                                ShellUtils.fastCmd("cp /sdcard/WorksOnWoa.url "+winpath+"/Toolbox");
                                ShellUtils.fastCmd("cp /sdcard/TestedSoftware.url "+winpath+"/Toolbox");
                                ShellUtils.fastCmd("cp /sdcard/ARMSoftware.url "+winpath+"/Toolbox");
                                messages.setText(getString(R.string.done));
                                dismissButton.setText(getString(R.string.dismiss));
                                dismissButton.setVisibility(View.VISIBLE);
                                dismissButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HideBlur();
                                        dialog.dismiss();
                                    }
                                });
                            }
                            dismissButton.setText(getString(R.string.dismiss));
                            dismissButton.setVisibility(View.VISIBLE);
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    HideBlur();
                                    dialog.dismiss();
                                }
                            });
                        } catch (Exception error) {
                            error.printStackTrace();
                        }
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        });

        n.cvFlashUefi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowBlur();
                noButton.setText(getString(R.string.no));
                dismissButton.setText(getString(R.string.dismiss));
                yesButton.setText(getString(R.string.yes));
                dismissButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.VISIBLE);
                noButton.setVisibility(View.VISIBLE);
                messages.setText(String.format(getString(R.string.flash_uefi_question), panel));
                icons.setVisibility(View.VISIBLE);
                icons.setImageDrawable(uefi);
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setVisibility(View.GONE);
                        messages.setText(getString(R.string.please_wait));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    flash(finduefi);
                                    messages.setText(getString(R.string.flash));
                                    dismissButton.setVisibility(View.VISIBLE);
                                } catch (Exception error) {
                                    error.printStackTrace();
                                }
                            }
                        }, 500);
                    }
                });
                dismissButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                noButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HideBlur();
                        dialog.dismiss();
                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        });

        x.cvBackup.setOnClickListener(

                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowBlur();
                        noButton.setVisibility(View.VISIBLE);
                        yesButton.setVisibility(View.VISIBLE);
                        dismissButton.setVisibility(View.VISIBLE);
                        noButton.setText("Android");
                        yesButton.setText("Windows");
                        dismissButton.setText(getString(R.string.no));
                        icons.setVisibility(View.VISIBLE);
                        icons.setImageDrawable(boot);
                        messages.setText(getString(R.string.backup_boot_question));
                        yesButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                noButton.setVisibility(View.GONE);
                                yesButton.setVisibility(View.GONE);
                                dismissButton.setVisibility(View.GONE);
                                messages.setText(getString(R.string.please_wait));
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm");
                                        String currentDateAndTime = sdf.format(new Date());
                                        pref.setDATE(MainActivity.this, currentDateAndTime);
                                        x.tvDate.setText(String.format(getString(R.string.last), pref.getDATE(MainActivity.this)));
                                        String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                                        winBackup();
                                        messages.setText(getString(R.string.backuped));
                                        dismissButton.setText(R.string.dismiss);
                                        dismissButton.setVisibility(View.VISIBLE);
                                    }
                                }, 4000);

                            }
                        });
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                HideBlur();
                                dialog.dismiss();
                            }
                        });
                        noButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                noButton.setVisibility(View.GONE);
                                yesButton.setVisibility(View.GONE);
                                dismissButton.setVisibility(View.GONE);
                                messages.setText(getString(R.string.please_wait));
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm");
                                        String currentDateAndTime = sdf.format(new Date());
                                        pref.setDATE(MainActivity.this, currentDateAndTime);
                                        x.tvDate.setText(String.format(getString(R.string.last), pref.getDATE(MainActivity.this)));
                                        ShellUtils.fastCmd(getString(R.string.backup));
                                        messages.setText(getString(R.string.backuped));
                                        dismissButton.setText(R.string.dismiss);
                                        dismissButton.setVisibility(View.VISIBLE);
                                    }
                                }, 500);
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                });

        k.toolbarlayout.toolbar.setTitle(getString(R.string.preferences));
        k.toolbarlayout.toolbar.setNavigationIcon(iconToolbar);

        //MainActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        x.toolbarlayout.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backable=1;
                x.mainlayout.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_out));
                setContentView(k.getRoot());
                k.settingsPanel.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_in));
                k.modemdump.setChecked(pref.getMODEM(MainActivity.this));
                k.backupQB.setChecked(pref.getBACKUP(MainActivity.this));
                k.backupQBA.setChecked(pref.getBACKUP_A(MainActivity.this));
                k.autobackup.setChecked(!pref.getAUTO(MainActivity.this));
                k.autobackupA.setChecked(!pref.getAUTO(MainActivity.this));
                k.confirmation.setChecked(pref.getCONFIRM(MainActivity.this));
                k.automount.setChecked(pref.getAutoMount(MainActivity.this));
                k.securelock.setChecked(!pref.getSecure(MainActivity.this));
                k.mountLocation.setChecked(pref.getMountLocation(MainActivity.this));
                k.toolbarlayout.settings.setVisibility(View.GONE);
                //k.language.setText(R.string.language);
            }
        });

        k.mountLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                pref.setMountLocation(MainActivity.this,b);
                winpath = (b?"/mnt/Windows":"/mnt/sdcard/Windows");
            }
        });

        k.modemdump.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                pref.setMODEM(MainActivity.this, b);
                if (b)
                    n.cvDumpModem.setVisibility(View.GONE);
                else
                    n.cvDumpModem.setVisibility(View.VISIBLE);
            }
        });

        d.toolbarlayout.back.setVisibility(View.VISIBLE);
        d.toolbarlayout.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().getCurrentFocus().startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_out));
                setContentView(x.getRoot());
                x.mainlayout.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_in));

                backable = 0;
            }
        });
        k.backupQB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!pref.getBACKUP(MainActivity.this)) {
                    dialog.show();
                    dialog.setCancelable(false);
                    messages.setText(getString(R.string.bwarn));
                    yesButton.setVisibility(View.VISIBLE);
                    noButton.setVisibility(View.GONE);
                    dismissButton.setVisibility(View.VISIBLE);
                    yesButton.setText(getString(R.string.agree));
                    dismissButton.setText(getString(R.string.cancel));
                    yesButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            pref.setBACKUP(MainActivity.this, true);
                            k.autobackup.setVisibility(View.GONE);
                            dialog.dismiss();
                        }
                    });
                    dismissButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            k.backupQB.setChecked(false);
                            dialog.dismiss();
                        }
                    });
                } else {
                    pref.setBACKUP(MainActivity.this, false);
                    k.autobackup.setVisibility(View.VISIBLE);
                }
            }
        });

        k.backupQBA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!pref.getBACKUP_A(MainActivity.this)) {
                    dialog.show();
                    dialog.setCancelable(false);
                    messages.setText(getString(R.string.bwarn));
                    yesButton.setVisibility(View.VISIBLE);
                    noButton.setVisibility(View.GONE);
                    dismissButton.setVisibility(View.VISIBLE);
                    yesButton.setText(getString(R.string.agree));
                    dismissButton.setText(getString(R.string.cancel));
                    yesButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            pref.setBACKUP_A(MainActivity.this, true);
                            k.autobackupA.setVisibility(View.GONE);
                            dialog.dismiss();
                        }
                    });
                    dismissButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            k.backupQBA.setChecked(false);
                            dialog.dismiss();
                        }
                    });
                } else {
                    pref.setBACKUP_A(MainActivity.this, false);
                    k.autobackupA.setVisibility(View.VISIBLE);
                }
            }
        });

        x.cvInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //if (!BuildConfig.VERSION_NAME.equals(ShellUtils.fastCmd(getFilesDir()+"/busybox wget -q -O - https://raw.githubusercontent.com/Marius586/WoA-Helper-update/main/README.md"))){
                            //update();
                        //}
                    }
                }).start();

            }

        });

        k.autobackup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                pref.setAUTO(MainActivity.this, !b);

            }
        });
        k.autobackupA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                pref.setAUTO_A(MainActivity.this, !b);

            }
        });

        k.confirmation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                pref.setCONFIRM(MainActivity.this, b);

            }
        });

        k.securelock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                pref.setSecure(MainActivity.this, !b);

            }
        });

        k.automount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                pref.setAutoMount(MainActivity.this, b);
            }
        });

        k.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                k.settingsPanel.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_out));
                setContentView(x.getRoot());
                x.mainlayout.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_in));
                backable = 0;
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        checkuefi();
    }

    @Override
    public void onBackPressed() {
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        switch (viewGroup.getId()) {
            case R.id.scriptstab:
                z.scriptstab.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_out));
                setContentView(n.getRoot());
                n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_in));
                backable++;
                break;
            case R.id.toolboxtab:
                n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_out));
                setContentView(x.getRoot());
                x.mainlayout.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_in));
                break;
            case R.id.settingsPanel:
                k.settingsPanel.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_out));
                setContentView(x.getRoot());
                x.mainlayout.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_in));
                break;
            case R.id.mainlayout:
                finish();
                break;
            default:
                x.mainlayout.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_in));
                setContentView(x.getRoot());
                break;
        }
    }

    public void update() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!ShellUtils.fastCmd("if [ -e " + getFilesDir() + "/woahelper.apk ] ; then echo true ; fi").equals("true")) {
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://raw.githubusercontent.com/Marius586/WoA-Helper-update/main/woahelper.apk -O " + getFilesDir() + "/woahelper.apk");
                    ShellUtils.fastCmd("pm install " + getFilesDir() + "/woahelper.apk && rm " + getFilesDir() + "/woahelper.apk");
                }

            }
        }).start();
    }

    public void flash(String uefi) {
        ShellUtils.fastCmd(
                "dd if=" + uefi + " of=/dev/block/bootdevice/by-name/boot$(getprop ro.boot.slot_suffix) bs=16M");
    }

    public void mount() {
        String c ;
        if (!pref.getMountLocation(MainActivity.this)) {
            c = String.format("su -mm -c " + pref.getbusybox(MainActivity.this) + getString(R.string.mount), win);
            ShellUtils.fastCmd(getString(R.string.mk));
            ShellUtils.fastCmd(c);
        } else{
            c = String.format("su -mm -c " + pref.getbusybox(MainActivity.this) + getString(R.string.mount2), win);
            ShellUtils.fastCmd(getString(R.string.mk2));
            ShellUtils.fastCmd(c);
        }
            mounted = getString(R.string.unmountt);
            x.tvMnt.setText(String.format(getString(R.string.mnt_title), mounted));
    }
	
	public void winBackup() {
		if (!pref.getMountLocation(MainActivity.this)) {
			ShellUtils.fastCmd(getString(R.string.mk));
			ShellUtils.fastCmd(String.format(pref.getbusybox(MainActivity.this) + getString(R.string.mount), win));
			ShellUtils.fastCmd(getString(R.string.backup1));
			ShellUtils.fastCmd(getString(R.string.unmount));
			ShellUtils.fastCmd(getString(R.string.rm));
        } else{
			ShellUtils.fastCmd(getString(R.string.mk2));
			ShellUtils.fastCmd(String.format(pref.getbusybox(MainActivity.this) + getString(R.string.mount2), win));
			ShellUtils.fastCmd(getString(R.string.backup2));
			ShellUtils.fastCmd(getString(R.string.unmount2));
			ShellUtils.fastCmd(getString(R.string.rm2));
        }
    }
	
//	public void winBackup(boolean m) {
//        new Thread(new Runnable() {
//			@Override
//            public void run() {
//				if (m) {
//        			if (!pref.getMountLocation(MainActivity.this)) {
//          				ShellUtils.fastCmd(getString(R.string.mk));
//           				ShellUtils.fastCmd(String.format(pref.getbusybox(MainActivity.this) + getString(R.string.mount), win));
//           				ShellUtils.fastCmd(getString(R.string.backup1));
//           				ShellUtils.fastCmd(getString(R.string.unmount));
//           				ShellUtils.fastCmd(getString(R.string.rm));
//        			} else{
//          				ShellUtils.fastCmd(getString(R.string.mk2));
//          				ShellUtils.fastCmd(String.format(pref.getbusybox(MainActivity.this) + getString(R.string.mount2), win));
//           				ShellUtils.fastCmd(getString(R.string.backup2));
//           				ShellUtils.fastCmd(getString(R.string.unmount2));
//           				ShellUtils.fastCmd(getString(R.string.rm2));
//						}}
//            }
//        }).start();
//    }
	
//    public void winBackup(boolean m) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if (m) {
//                    ShellUtils.fastCmd(getString(R.string.mk));
//                    ShellUtils.fastCmd(String.format(pref.getbusybox(MainActivity.this) + getString(R.string.mount), win));
//                    ShellUtils.fastCmd(getString(R.string.backup1));
//                    ShellUtils.fastCmd(getString(R.string.unmount));
//                    ShellUtils.fastCmd(getString(R.string.rm));
//                } else
//                    ShellUtils.fastCmd(getString(R.string.backup1));
//            }
//        }).start();
//    }

    public void unmount() {
        if (!pref.getMountLocation(MainActivity.this)) {
            ShellUtils.fastCmd(getString(R.string.unmount));
            ShellUtils.fastCmd(getString(R.string.rm));
        } else{
            ShellUtils.fastCmd(getString(R.string.unmount2));
            ShellUtils.fastCmd(getString(R.string.rm2));
        }
        mounted = getString(R.string.mountt);
        x.tvMnt.setText(String.format(getString(R.string.mnt_title), mounted));
    }



    public void dump() {
        ShellUtils.fastCmd(String.format(getString(R.string.modem1)),pref.getMountLocation(MainActivity.this)?"/mnt/Windows":"/mnt/sdcard/Windows");
        ShellUtils.fastCmd(String.format(getString(R.string.modem2)),pref.getMountLocation(MainActivity.this)?"/mnt/Windows":"/mnt/sdcard/Windows");
    }

    public void checkdevice() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        MaterialButton yesButton = dialog.findViewById(R.id.yes);
        MaterialButton noButton = dialog.findViewById(R.id.no);
        MaterialButton dismissButton = dialog.findViewById(R.id.dismiss);
        TextView messages = dialog.findViewById(R.id.messages);
        yesButton.setVisibility(View.GONE);
        noButton.setVisibility(View.GONE);
        dismissButton.setVisibility(View.GONE);
        if (Boolean.FALSE.equals(Shell.isAppGrantedRoot())) {
            messages.setText(getString(R.string.nonroot));
            dialog.show();
            dialog.setCancelable(false);
        }
        String[] supported = {"a52sxq", "alphalm_lao_com", "alphaplus_lao_com", "alioth", "alphalm", "alphaplus", "andromeda", "betalm", "betaplus_lao_com", "betalm_lao_com", "beryllium", "bhima", "cepheus", "cheeseburger", "curtana2", "chiron", "curtana", "curtana_india","joyeuse", "curtana_cn","curtanacn", "cmi", "davinci", "dumpling", "dipper", "durandal", "durandal_india", "enchilada", "equuleus", "excalibur", "excalibur_india", "flashlmdd", "flashlmdd_lao_com", "fajita", "houji", "joan", "judyln", "judyp", "judypn", "guacamole", "guacamoleb", "gram", "hotdog", "hotdogb", "hotdogg", "lisa", "marble", "mh2lm", "mh2plus_lao_com", "mh2lm_lao_com", "mh2lm5g", "mh2lm5g_lao_com", "miatoll", "nabu", "pipa", "OnePlus6", "OnePlus6T", "OnePlus7", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7T", "OnePlus7TPro", "OnePlus7TPro4G", "OnePlus7TPro5G", "OP7ProNRSpr", "OnePlus7TProNR", "perseus", "polaris", "Pong", "pong", "q2q", "raphael", "raphaelin", "raphaels", "RMX2170", "RMX2061", "sagit", "surya", "vayu", "venus", "winner", "winnerx", "xpeng", "G973F", "SM-G973F", "beyond1lte", "beyond1qlte", "G973U", "G973U1", "SM-G973U", "SM-G973U1", "G9730", "SM-G9730", "G973N", "SM-G973N", "G973X", "SM-G973X", "G973C", "SM-G973C", "SCV41", "SM-SC41", "beyond1"};
        device = ShellUtils.fastCmd("getprop ro.product.device ");
        model = ShellUtils.fastCmd("getprop ro.product.model");
        if (!Arrays.asList(supported).contains(device)) {
            k.modemdump.setVisibility(View.VISIBLE);
            if (!pref.getAGREE(MainActivity.this)) {
                messages.setText(getString(R.string.unsupported));
                device = "unknown";
                dialog.show();
                yesButton.setText(R.string.sure);
                yesButton.setVisibility(View.VISIBLE);
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        pref.setAGREE(MainActivity.this, true);
                    }
                });
            }
            device = "unknown";
        }

        String stram = MemoryUtils.extractNumberFromString(new RAM().getMemory(getApplicationContext()));
        ramvalue = Double.parseDouble(stram) / 100;

        String run = ShellUtils.fastCmd(" su -c cat /proc/cmdline ");
        if (!run.isEmpty()) {
            if (run.contains("j20s_42") || run.contains("k82_42")) {
                panel = "Huaxing";
            } else if (run.contains("j20s_36") || run.contains("k82_36")) {
                panel = "Tianma";
            } else {
                panel = ShellUtils.fastCmd("su -c cat /proc/cmdline | tr ' :=' '\n'|grep dsi|tr ' _' '\n'|tail -3|head -1 ");
                if (!panel.isEmpty())
                    if ((!(Objects.equals(panel, "f1mp") || Objects.equals(panel, "f1p2") || Objects.equals(panel, "global") || pref.getAGREE(MainActivity.this))) && (device.equals("raphael") || device.equals("raphaelin") || device.equals("raphaels") || device.equals("cepheus"))) {
                        dialog.show();
                        messages.setText(getString(R.string.upanel));
                        yesButton.setText(getString(R.string.chat));
                        dismissButton.setText(getString(R.string.nah));
                        noButton.setText(getString(R.string.later));
                        yesButton.setVisibility(View.VISIBLE);
                        noButton.setVisibility(View.VISIBLE);
                        dismissButton.setVisibility(View.VISIBLE);
                        yesButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(grouplink));
                                startActivity(i);
                                dialog.dismiss();
                                pref.setAGREE(MainActivity.this, true);
                            }
                        });
                        noButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                pref.setAGREE(MainActivity.this, true);
                            }
                        });
                    }
            }
            if (Objects.equals(panel, "2"))
                panel = "f1p2_2";
        } else
            panel = "Unknown";

    }

    public void checkuefi() {
        ShellUtils.fastCmd("su -c mkdir /sdcard/UEFI");
        finduefi = ShellUtils.fastCmd(getString(R.string.uefiChk));
        device = ShellUtils.fastCmd("getprop ro.product.device ");
        if (!finduefi.contains("img")) {
            x.cvQuickBoot.setEnabled(false);
            x.cvQuickBoot.setVisibility(View.VISIBLE);
            x.tvQuickBoot.setText(getString(R.string.uefi_not_found));
            x.tvBootSubtitle.setText(String.format(getString(R.string.uefi_not_found_subtitle), device));
            n.tvFlashUefi.setText(getString(R.string.uefi_not_found));
            n.tvUefiSubtitle.setText(String.format(getString(R.string.uefi_not_found_subtitle), device));
            n.cvFlashUefi.setEnabled(false);
            x.tvToolbox.setText(getString(R.string.toolbox_title));
            x.cvToolbox.setEnabled(true);
            x.tvToolboxSubtitle.setText(getString(R.string.toolbox_subtitle));
            x.tvToolboxSubtitle.setVisibility(View.VISIBLE);
        } else {
            x.tvQuickBoot.setText(getString(R.string.quickboot_title));
            x.cvQuickBoot.setEnabled(true);
            x.cvQuickBoot.setVisibility(View.VISIBLE);
            x.tvQuickBoot.setText(getString(R.string.quickboot_title));
            n.tvFlashUefi.setText(getString(R.string.flash_uefi_title));
            n.tvUefiSubtitle.setText(String.format(getString(R.string.flash_uefi_subtitle), device));
            n.cvFlashUefi.setEnabled(true);
            x.tvToolbox.setText(getString(R.string.toolbox_title));
            x.cvToolbox.setEnabled(true);
            x.tvToolboxSubtitle.setText(getString(R.string.toolbox_subtitle));
            x.tvToolboxSubtitle.setVisibility(View.VISIBLE);
            try {
                String[] dumpeddevices = {"bhima", "cepheus", "guacamole", "guacamoleb", "hotdog", "hotdogb", "OnePlus7", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7T", "OnePlus7TPro", "OnePlus7TPro4G", "OP7ProNRSpr", "raphael", "raphaelin", "raphaels", "vayu"};
                if (Arrays.asList(dumpeddevices).contains(device)) {
                    x.tvBootSubtitle.setText(getString(R.string.quickboot_subtitle));
                } else {
                    x.tvBootSubtitle.setText(getString(R.string.quickboot_subtitle_nabu));
                }

            } catch (Exception error) {
                error.printStackTrace();
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        TextView myTextView1 = findViewById(R.id.text);
        TextView myTextView2 = findViewById(R.id.deviceName);
        TextView myTextView3 = findViewById(R.id.tv_panel);
        TextView myTextView4 = findViewById(R.id.tv_ramvalue);
        TextView myTextView5 = findViewById(R.id.guide_text);
        TextView myTextView6 = findViewById(R.id.group_text);
        TextView myTextView7 = findViewById(R.id.tv_slot);
        TextView myTextView8 = findViewById(R.id.tv_date);
        TextView myTextView9 = findViewById(R.id.tv_slot);
        if (newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE && Objects.equals(device, "nabu")) {
            x.app.setOrientation(LinearLayout.VERTICAL);
            x.top.setOrientation(LinearLayout.HORIZONTAL);
            myTextView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            myTextView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            myTextView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            myTextView4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            myTextView5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            myTextView6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            myTextView7.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            myTextView8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            myTextView9.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        } else if (newConfig.orientation != Configuration.ORIENTATION_PORTRAIT && Objects.equals(device, "nabu")) {
            x.app.setOrientation(LinearLayout.HORIZONTAL);
            x.top.setOrientation(LinearLayout.VERTICAL);
            myTextView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            myTextView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            myTextView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            myTextView4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            myTextView5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            myTextView6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            myTextView7.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            myTextView8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            myTextView9.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        }
    }

    public void ShowBlur() {
        x.blur.setVisibility(View.VISIBLE);
    }

    public void HideBlur() {
        x.blur.setVisibility(View.GONE);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Network activeNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);

        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    private void setApplicationLocale(String locale) {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.setLocale(new Locale(locale.toLowerCase()));
        resources.updateConfiguration(config, dm);
        pref.setlocale(MainActivity.this, locale);
    }
}
