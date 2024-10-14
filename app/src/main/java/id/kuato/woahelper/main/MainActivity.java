package id.kuato.woahelper.main;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;

import com.google.android.material.button.MaterialButton;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id.kuato.woahelper.BuildConfig;
import id.kuato.woahelper.R;
import id.kuato.woahelper.databinding.ActivityMainBinding;
import id.kuato.woahelper.databinding.DevicesBinding;
import id.kuato.woahelper.databinding.LangaugesBinding;
import id.kuato.woahelper.databinding.ScriptsBinding;
import id.kuato.woahelper.databinding.SetPanelBinding;
import id.kuato.woahelper.databinding.ToolboxBinding;
import id.kuato.woahelper.preference.pref;
import id.kuato.woahelper.util.RAM;

public class MainActivity extends AppCompatActivity {


    private static final float SIZE = 12.0F;
    private static final float SIZE1 = 15.0F;
    private static final float SIZE2 = 30.0F;
    private static final float SIZE3 = 18.0F;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    public ActivityMainBinding x;
    public DevicesBinding d;
    public SetPanelBinding k;
    public ToolboxBinding n;
    public ScriptsBinding z;
    public LangaugesBinding l;
    private Context context;
    int ram;
    public String winpath;
    private double ramvalue;
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
    String guidelink = "";
    String currentVersion = BuildConfig.VERSION_NAME;
    static final Object lock = new Object();

    public static String extractNumberFromString(final String source) {
        final StringBuilder result = new StringBuilder(100);
        for (final char ch : source.toCharArray()) {
            if ('0' <= ch && '9' >= ch) {
                result.append(ch);
            }
        }
        return result.toString();
    }

    private void copyAssets() {
        ShellUtils.fastCmd(String.format("rm %s/*", getFilesDir()));
        final AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (final IOException ignored) {
        }
        assert null != files;
        for (final String filename : files) {
            @Nullable InputStream in;
            @Nullable OutputStream out;
            try {
                in = assetManager.open(filename);

                final String outDir = String.valueOf(getFilesDir());

                final File outFile = new File(outDir, filename);

                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                out.flush();
                out.close();
            } catch (final IOException ignored) {
            }
        }
        ShellUtils.fastCmd("chmod 777 " + getFilesDir() + "/busybox");
        pref.setbusybox(this, getFilesDir() + "/busybox ");
    }

    private void copyFile(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[1024];
        int read;
        while (-1 != (read = in.read(buffer))) {
            out.write(buffer, 0, read);
        }
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            final CharSequence name = "WoA helper";
            final String description = "Windows on ARM assistant";
            final int importance = NotificationManager.IMPORTANCE_LOW;
            final NotificationChannel channel = new NotificationChannel("IMPORTANCE_NONE", name, importance);
            channel.setDescription(description);
            final NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        if (Objects.equals(this.currentVersion, pref.getVersion(this))) {
            final String[] files = {"guacamole.fd", "hotdog.fd", "dbkp8150.cfg", "dbkp.hotdog.bin", "busybox", "sta.exe", "Switch to Android.lnk", "usbhostmode.exe", "ARMSoftware.url", "TestedSoftware.url", "WorksOnWoa.url", "RotationShortcut.lnk", "display.exe", "RemoveEdge.ps1", "autoflasher.lnk", "DefenderRemover.exe"};
            int i = 0;
            while (!files[i].isEmpty()) {
                if (ShellUtils.fastCmd(String.format("ls %1$s |grep %2$s", this.getFilesDir(), files[i])).isEmpty()) {
                    this.copyAssets();
                    break;
                }
                i++;
            }
        } else {
            this.copyAssets();
            pref.setVersion(this, this.currentVersion);
        }
        //createNotificationChannel();
        super.onCreate(savedInstanceState);
        Dialog dialog = new Dialog(this);
        Dialog languages = new Dialog(this);
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
        final Drawable iconToolbar = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_launcher_foreground, null);
        setSupportActionBar(x.toolbarlayout.toolbar);
        x.toolbarlayout.toolbar.setTitle(getString(R.string.app_name));
        x.toolbarlayout.toolbar.setSubtitle("v" + currentVersion);
        x.toolbarlayout.toolbar.setNavigationIcon(iconToolbar);
        checkdevice();
        checkuefi();
        win = ShellUtils.fastCmd("realpath /dev/block/by-name/win");
        if (win.isEmpty())
            win = ShellUtils.fastCmd("realpath /dev/block/by-name/mindows");
        Log.d("debug", win);
        winpath = (pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows");
        final String mount_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
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
        final MaterialButton yesButton = dialog.findViewById(R.id.yes);
        final MaterialButton noButton = dialog.findViewById(R.id.no);
        final MaterialButton dismissButton = dialog.findViewById(R.id.dismiss);
        final TextView messages = dialog.findViewById(R.id.messages);
        final ImageView icons = dialog.findViewById(R.id.icon);
        final ProgressBar bar = dialog.findViewById(R.id.progress);
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        final Drawable androidlogo1 = ResourcesCompat.getDrawable(getResources(), R.drawable.androidlogo1, null);
        final Drawable androidlogo2 = ResourcesCompat.getDrawable(getResources(), R.drawable.androidlogo2, null);
        final Drawable atlasos = ResourcesCompat.getDrawable(getResources(), R.drawable.atlasos2, null);
        final Drawable uefi = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_uefi, null);
        final Drawable boot = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_disk, null);
        final Drawable sensors = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sensor, null);
        final Drawable edge = ResourcesCompat.getDrawable(getResources(), R.drawable.edge2, null);
        final Drawable download = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_download, null);
        final Drawable modem = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_modem, null);
        final Drawable mnt = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_mnt, null);
        final Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_launcher_foreground, null);
        final Drawable settings = ResourcesCompat.getDrawable(getResources(), R.drawable.settings, null);
        settings.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(getColor(R.color.colorPrimary), BlendModeCompat.SRC_IN));
        final Drawable back = ResourcesCompat.getDrawable(getResources(), R.drawable.back_arrow, null);
        back.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(getColor(R.color.colorPrimary), BlendModeCompat.SRC_IN));


        d.toolbarlayout.back.setImageDrawable(back);
        x.toolbarlayout.settings.setImageDrawable(settings);

        final String slot = ShellUtils.fastCmd("getprop ro.boot.slot_suffix");
        if (slot.isEmpty()) x.tvSlot.setVisibility(View.GONE);
        else
            x.tvSlot.setText(String.format(getString(R.string.slot), slot.substring(1, 2).toUpperCase()));

        final TextView myTextView00 = findViewById(R.id.tv_date);
        myTextView00.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
        x.deviceName.setText(model + " (" + device + ")");
        if (Objects.equals(device, "nabu")) {
            final TextView myTextView2 = findViewById(R.id.deviceName);
            myTextView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            final TextView myTextView3 = findViewById(R.id.tv_panel);
            myTextView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            final TextView myTextView4 = findViewById(R.id.tv_ramvalue);
            myTextView4.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            final TextView myTextView5 = findViewById(R.id.guide_text);
            myTextView5.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            final TextView myTextView6 = findViewById(R.id.group_text);
            myTextView6.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            final TextView myTextView7 = findViewById(R.id.tv_slot);
            myTextView7.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);

            final int orientation = getResources().getConfiguration().orientation;
            if (Configuration.ORIENTATION_PORTRAIT == orientation) {
                x.app.setOrientation(LinearLayout.VERTICAL);
                x.top.setOrientation(LinearLayout.HORIZONTAL);
            } else {
                x.app.setOrientation(LinearLayout.HORIZONTAL);
                x.top.setOrientation(LinearLayout.VERTICAL);
            }
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            final TextView myTextView1 = findViewById(R.id.text);
            myTextView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE1);
            final TextView myTextView2 = findViewById(R.id.deviceName);
            myTextView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            final TextView myTextView3 = findViewById(R.id.tv_panel);
            myTextView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            final TextView myTextView4 = findViewById(R.id.tv_ramvalue);
            myTextView4.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            final TextView myTextView5 = findViewById(R.id.guide_text);
            myTextView5.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            final TextView myTextView6 = findViewById(R.id.group_text);
            myTextView6.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            final TextView myTextView7 = findViewById(R.id.tv_slot);
            myTextView7.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);

            n.cvDbkp.setVisibility(View.GONE);

            switch (device) {
                case "nabu" -> {
                    guidelink = "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/tree/main";
                    grouplink = "https://t.me/nabuwoa";
                    x.NabuImage.setVisibility(View.VISIBLE);
                    x.DeviceImage.setVisibility(View.GONE);
                    x.tvPanel.setVisibility(View.VISIBLE);
                    n.tvDumpModem.setVisibility(View.GONE);
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "cepheus" -> {
                    guidelink = "https://github.com/woacepheus/Port-Windows-11-Xiaomi-Mi-9";
                    grouplink = "http://t.me/woacepheus";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.cepheus, null));
                    x.tvPanel.setVisibility(View.VISIBLE);
                }
                case "raphael", "raphaelin", "raphaels" -> {
                    guidelink = "https://github.com/woa-raphael/woa-raphael";
                    grouplink = "https://t.me/woaraphael";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.raphael, null));
                    x.tvPanel.setVisibility(View.VISIBLE);
                }
                case "mh2lm", "mh2plus", "mh2plus_lao_com", "mh2lm_lao_com" -> {
                    guidelink = "https://github.com/n00b69/woa-mh2lm";
                    grouplink = "https://t.me/woahelperchat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.mh2lm, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "mh2lm5g", "mh2lm5g_lao_com" -> {
                    guidelink = "https://github.com/n00b69/woa-mh2lm5g";
                    grouplink = "https://t.me/woahelperchat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.mh2lm, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "flashlmdd", "flashlmdd_lao_com" -> {
                    guidelink = "https://github.com/n00b69/woa-flashlmdd";
                    grouplink = "https://t.me/woahelperchat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.flashlmdd, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                    n.cvDbkp.setVisibility(View.GONE);
                }
                case "alphalm", "alphaplus", "alphalm_lao_com", "alphaplus_lao_com" -> {
                    guidelink = "https://github.com/n00b69/woa-alphaplus";
                    grouplink = "https://t.me/woahelperchat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.alphaplus, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "betaplus", "betalm", "betaplus_lao_com", "betalm_lao_com" -> {
                    guidelink = "https://github.com/n00b69/woa-betalm";
                    grouplink = "https://t.me/woahelperchat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.betalm, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "bhima", "vayu" -> {
                    guidelink = "https://github.com/woa-vayu/POCOX3Pro-Guides";
                    grouplink = "https://t.me/woahelperchat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.vayu, null));
                    x.tvPanel.setVisibility(View.VISIBLE);
                }
                case "beryllium" -> {
                    guidelink = "https://github.com/n00b69/woa-beryllium";
                    grouplink = "https://t.me/WinOnF1";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.beryllium, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "chiron" -> {
                    guidelink = "https://renegade-project.tech/";
                    grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.chiron, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "dipper" -> {
                    guidelink = "https://github.com/n00b69/woa-dipper";
                    grouplink = "https://t.me/woadipper";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dipper, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "equuleus" -> {
                    guidelink = "https://github.com/n00b69/woa-equuleus";
                    grouplink = "https://t.me/woaequuleus";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.equuleus, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "lisa" -> {
                    guidelink = "https://github.com/ETCHDEV/Port-Windows-11-Xiaomi-11-Lite-NE";
                    grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.lisa, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "curtana", "curtana2", "curtana_india", "curtana_cn", "curtanacn", "durandal",
                     "durandal_india", "excalibur", "excalibur2", "excalibur_india", "gram",
                     "joyeuse", "miatoll" -> {
                    guidelink = "https://github.com/woa-miatoll/Port-Windows-11-Redmi-Note-9-Pro";
                    grouplink = "http://t.me/woamiatoll";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.miatoll, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                    x.tvPanel.setVisibility(View.VISIBLE);
                }
                case "polaris" -> {
                    guidelink = "https://github.com/n00b69/woa-polaris";
                    grouplink = "https://t.me/WinOnMIX2S";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.polaris, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                    x.tvPanel.setVisibility(View.VISIBLE);
                }
                case "venus" -> {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/woahelperchat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.venus, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "xpeng" -> {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/woahelperchat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.xpeng, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "alioth" -> {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/woahelperchat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.alioth, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "pipa" -> {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/pad6_pipa";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pipa, null));
                    x.tvPanel.setVisibility(View.VISIBLE);
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "perseus" -> {
                    guidelink = "https://github.com/n00b69/woa-perseus";
                    grouplink = "https://t.me/woaperseus";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.perseus, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "Pong", "pong" -> {
                    guidelink = "https://github.com/Govro150/woa-pong";
                    grouplink = "https://t.me/woa_pong";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pong, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "surya" -> {
                    guidelink = "https://github.com/SebastianZSXS/Poco-X3-NFC-WindowsARM";
                    grouplink = "https://t.me/windows_on_pocox3_nfc";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.vayu, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "cheeseburger" -> {
                    guidelink = "https://renegade-project.tech/";
                    grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.cheeseburger, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "dumpling" -> {
                    guidelink = "https://renegade-project.tech/";
                    grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dumpling, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "G973F", "SM-G973F", "beyond1lte", "beyond1qlte", "G973U", "G973U1",
                     "SM-G973U", "SM-G973U1", "G9730", "SM-G9730", "G973N", "SM-G973N", "G973X",
                     "SM-G973X", "G973C", "SM-G973C", "SCV41", "SM-SC41", "beyond1" -> {
                    guidelink = "https://github.com/sonic011gamer/Mu-Samsung";
                    grouplink = "https://t.me/woahelperchat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.beyond1, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "a52sxq" -> {
                    guidelink = "https://github.com/arminask/Port-Windows-11-Galaxy-A52s-5G";
                    grouplink = "https://t.me/a52sxq_uefi";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.a52sxq, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "judyln", "judyp", "judypn" -> {
                    guidelink = "https://github.com/n00b69/woa-everything";
                    grouplink = "https://t.me/lgedevices";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.unknown, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "joan" -> {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/lgedevices";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.unknown, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "andromeda" -> {
                    guidelink = "https://woa-msmnile.github.io/";
                    grouplink = "https://t.me/woa_msmnile_issues";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.unknown, null));
                }
                case "OnePlus6", "fajita" -> {
                    guidelink = "https://github.com/WoA-OnePlus-6-Series/WoA-on-OnePlus6-Series";
                    grouplink = "https://t.me/WinOnOP6";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.fajita, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "OnePlus6T", "enchilada" -> {
                    guidelink = "https://github.com/WoA-OnePlus-6-Series/WoA-on-OnePlus6-Series";
                    grouplink = "https://t.me/WinOnOP6";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.enchilada, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "hotdog", "OnePlus7TPro", "OnePlus7TPro4G" -> {
                    guidelink = "https://github.com/n00b69/woa-op7";
                    grouplink = "https://t.me/onepluswoachat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.hotdog, null));
                    n.cvFlashUefi.setVisibility(View.GONE);
                    n.cvDbkp.setVisibility(View.VISIBLE);
                }
                case "guacamole", "OnePlus7Pro", "OnePlus7Pro4G" -> {
                    guidelink = "https://github.com/n00b69/woa-op7";
                    grouplink = "https://t.me/onepluswoachat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.guacamole, null));
                    n.cvDbkp.setVisibility(View.VISIBLE);
                    n.cvFlashUefi.setVisibility(View.GONE);
                }
                case "guacamoleb", "hotdogb", "OnePlus7T", "OnePlus7" -> {
                    guidelink = "https://woa-msmnile.github.io/";
                    grouplink = "https://t.me/onepluswoachat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.unknown, null));
                }
                case "OnePlus7TPro5G", "OnePlus7TProNR" -> {
                    guidelink = "https://woa-msmnile.github.io/";
                    grouplink = "https://t.me/onepluswoachat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.hotdog, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "hotdogg", "OP7ProNRSpr" -> {
                    guidelink = "https://woa-msmnile.github.io/";
                    grouplink = "https://t.me/onepluswoachat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.guacamole, null));
                }
                case "sagit" -> {
                    guidelink = "https://renegade-project.tech/";
                    grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.unknown, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "winnerx", "winner" -> {
                    guidelink = "https://github.com/n00b69/woa-winner";
                    grouplink = "https://t.me/woa_msmnile_issues";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.winner, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "q2q" -> {
                    guidelink = "https://woa-msmnile.github.io/";
                    grouplink = "https://t.me/woa_msmnile_issues";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.q2q, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "RMX2061" -> {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/realme6PROwindowsARM64";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.rmx2061, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "RMX2170" -> {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/realme6PROwindowsARM64";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.rmx2170, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "cmi" -> {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/dumanthecat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.cmi, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "houji" -> {
                    guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    grouplink = "https://t.me/dumanthecat";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.houji, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "marble" -> {
                    guidelink = "https://github.com/Xhdsos/woa-marble";
                    grouplink = "https://t.me/woa_marble";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.marble, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                case "davinci" -> {
                    guidelink = "https://github.com/zxcwsurx/woa-davinci";
                    grouplink = "https://t.me/woa_davinci";
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.raphael, null));
                    n.cvDumpModem.setVisibility(View.GONE);
                }
                default -> {
                    x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.unknown, null));
                    x.deviceName.setText(device);
                    guidelink = "https://renegade-project.tech/";
                    grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                }
            }
        }
        x.tvRamvalue.setText(String.format(getString(R.string.ramvalue), ramvalue));
        x.tvPanel.setText(String.format(getString(R.string.paneltype), panel));
        x.cvGuide.setOnClickListener(v -> {
            final Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(this.guidelink));
            startActivity(i);
        });

        x.cvGroup.setOnClickListener(v -> {
            final Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(this.grouplink));
            startActivity(i);
        });

        x.cvQuickBoot.setOnClickListener(v -> {
            ShowBlur();
            noButton.setText(getString(R.string.no));
            yesButton.setText(getString(R.string.yes));
            yesButton.setVisibility(View.VISIBLE);
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(icon);
            messages.setText(getString(R.string.quickboot_question));
            yesButton.setOnClickListener(v27 -> {
                yesButton.setVisibility(View.GONE);
                noButton.setVisibility(View.GONE);
                messages.setText(getString(R.string.please_wait));
                new Handler().postDelayed(() -> {
                    try {
                        mount();
                        final String[] dumped = {"bhima", "cepheus", "guacamole", "guacamoleb", "hotdog", "hotdogb", "OnePlus7", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7T", "OnePlus7TPro", "OnePlus7TPro4G", "raphael", "raphaelin", "raphaels", "vayu"};
                        if (Arrays.asList(dumped).contains(device) || !(pref.getMODEM(this))) {
                            dump();
                        }
                        String found = ShellUtils.fastCmd("ls " + (pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows") + " | grep boot.img");
                        if (pref.getMountLocation(this)) {
                            if (pref.getBACKUP(this)
                                    || (!pref.getAUTO(this) && found.isEmpty())) {
                                ShellUtils.fastCmd(getString(R.string.backup2));
                                final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
                                final String currentDateAndTime = sdf.format(new Date());
                                pref.setDATE(this, currentDateAndTime);
                            }
                        } else {
                            if (pref.getBACKUP(this)
                                    || (!pref.getAUTO(this) && found.isEmpty())) {
                                ShellUtils.fastCmd(getString(R.string.backup1));
                                final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
                                final String currentDateAndTime = sdf.format(new Date());
                                pref.setDATE(this, currentDateAndTime);
                            }
                        }
                        found = ShellUtils.fastCmd("find /sdcard | grep boot.img");
                        if (pref.getBACKUP_A(this)
                                || (!pref.getAUTO_A(this) && found.isEmpty())) {
                            ShellUtils.fastCmd(getString(R.string.backup));
                            final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
                            final String currentDateAndTime = sdf.format(new Date());
                            pref.setDATE(this, currentDateAndTime);
                        }

                        flash(finduefi);
                        ShellUtils.fastCmd("su -c svc power reboot");
                        messages.setText(getString(R.string.wrong));
                        dismissButton.setVisibility(View.VISIBLE);
                    } catch (final Exception error) {
                        error.printStackTrace();
                    }
                }, 500);
            });
            dismissButton.setText(getString(R.string.dismiss));
            dismissButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    HideBlur();
                    dialog.dismiss();
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    HideBlur();
                    dialog.dismiss();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

        x.cvMnt.setOnClickListener(v -> {
            noButton.setText(getString(R.string.no));
            noButton.setVisibility(View.VISIBLE);
            yesButton.setText(getString(R.string.yes));
            dismissButton.setText(getString(R.string.dismiss));
            yesButton.setVisibility(View.VISIBLE);
            dismissButton.setVisibility(View.GONE);
            ShowBlur();
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(mnt);
            final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
            if (mnt_stat.isEmpty())
                messages.setText(String.format(getString(R.string.mount_question), winpath));
            else
                messages.setText(getString(R.string.unmount_question));

            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    yesButton.setVisibility(View.GONE);
                    noButton.setVisibility(View.GONE);
                    messages.setText(getString(R.string.please_wait));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d("debug", winpath);
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
                                            public void onClick(final View v) {
                                                HideBlur();
                                                dialog.dismiss();
                                                icons.setVisibility(View.VISIBLE);
                                            }
                                        });
                                        yesButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(final View v) {
                                                final Intent i = new Intent(Intent.ACTION_VIEW);
                                                i.setData(Uri.parse("https://t.me/woahelperchat"));
                                                startActivity(i);
                                            }
                                        });

                                        dialog.setCancelable(false);
                                        dialog.show();
                                    } else {
                                        Log.d("debug", pref.getMountLocation(MainActivity.this) ? "/mnt/Windows" : "/mnt/sdcard/Windows");
                                        messages.setText(getString(R.string.mounted) + "\n" + winpath);
                                    }
                                } else {
                                    unmount();
                                    messages.setText(getString(R.string.unmounted));
                                }
                                dismissButton.setText(getString(R.string.dismiss));
                                dismissButton.setVisibility(View.VISIBLE);
                            } catch (final Exception error) {
                                error.printStackTrace();
                            }
                        }

                    }, 500);
                }

            });

            dismissButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    HideBlur();
                    dialog.dismiss();
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    HideBlur();
                    dialog.dismiss();
                }
            });
            dialog.setCancelable(false);
            dialog.show();

        });

        n.cvDumpModem.setOnClickListener(v -> {
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
                public void onClick(final View v) {
                    noButton.setVisibility(View.GONE);
                    yesButton.setVisibility(View.GONE);
                    messages.setText(getString(R.string.please_wait));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
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
                public void onClick(final View v) {
                    HideBlur();
                    dialog.dismiss();
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    HideBlur();
                    dialog.dismiss();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

        n.cvScripts.setOnClickListener(v -> {
            n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
            setContentView(z.getRoot());
            z.scriptstab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
            backable = 1;
            z.toolbarlayout.settings.setVisibility(View.GONE);
            z.toolbarlayout.toolbar.setTitle(getString(R.string.script_title));
            z.toolbarlayout.toolbar.setNavigationIcon(iconToolbar);
        });


        x.cvToolbox.setOnClickListener(v -> {
            x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
            setContentView(n.getRoot());
            n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
            backable = 1;
            n.toolbarlayout.settings.setVisibility(View.GONE);
            n.toolbarlayout.toolbar.setTitle(getString(R.string.toolbox_title));
            n.toolbarlayout.toolbar.setNavigationIcon(iconToolbar);
        });

        n.cvSta.setOnClickListener(v -> {
            ShowBlur();
            noButton.setText(getString(R.string.no));
            yesButton.setText(getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(getString(R.string.sta_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(androidlogo1);
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    HideBlur();
                    dialog.dismiss();
                }
            });
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    noButton.setVisibility(View.GONE);
                    yesButton.setVisibility(View.GONE);
                    dismissButton.setVisibility(View.GONE);
                    try {
                        mount();
                        ShellUtils.fastCmd("su -c 'if [ -e /dev/block/by-name/boot_a ] && [ -e /dev/block/by-name/boot_b ]; then boot_a=$(basename $(readlink -f /dev/block/by-name/boot_a)); boot_b=$(basename $(readlink -f /dev/block/by-name/boot_b)); printf \"%s\n%s\n%s\n%s\n\" \"$boot_a\" \"C:\\boot.img\" \"$boot_b\" \"C:\\boot.img\" > /sdcard/sta.conf; else boot=$(basename $(readlink -f /dev/block/by-name/boot)); printf \"%s\n%s\n\" \"$boot\" \"C:\\boot.img\" > /sdcard/sta.conf; fi'");
                        ShellUtils.fastCmd("cp " + getFilesDir() + "/sta.exe /sdcard/sta.exe");
                        final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
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
                                public void onClick(final View v) {
                                    HideBlur();
                                    icons.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                }
                            });
                            yesButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    final Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse("https://t.me/woahelperchat"));
                                    startActivity(i);
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.show();
                        } else {
                            ShellUtils.fastCmd("mkdir " + winpath + "/sta || true ");
                            ShellUtils.fastCmd("cp /sdcard/sta.conf " + winpath + "/sta");
                            ShellUtils.fastCmd("cp " + getFilesDir() + "/sta.exe " + winpath + "/sta/sta.exe");
                            ShellUtils.fastCmd("cp '" + getFilesDir() + "/Switch to Android.lnk' " + winpath + "/Users/Default/Desktop");
                            ShellUtils.fastCmd("cp '" + getFilesDir() + "/Switch to Android.lnk' " + winpath + "/Users/*/Desktop");
                            messages.setText(getString(R.string.done));
                            dismissButton.setText(getString(R.string.dismiss));
                            dismissButton.setVisibility(View.VISIBLE);
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    HideBlur();
                                    dialog.dismiss();
                                }
                            });
                        }
                        dismissButton.setText(getString(R.string.dismiss));
                        dismissButton.setVisibility(View.VISIBLE);
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                HideBlur();
                                dialog.dismiss();
                            }
                        });
                    } catch (final Exception error) {
                        error.printStackTrace();
                    }
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

        n.cvAtlasos.setOnClickListener(v -> {
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
            if (!MainActivity.isNetworkConnected(this)) {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.VISIBLE);
                dismissButton.setText(getString(R.string.dismiss));
                messages.setText("No internet connection\nConnect to the internet and try again");
            }
            dismissButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    HideBlur();
                    dialog.dismiss();
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

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
                            } catch (final InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            bar.setProgress((int) (bar.getMax() * 0.00), true);
                            ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/modified-playbooks/releases/download/ReviOS/ReviPlaybook.apbx -O /sdcard/ReviPlaybook.apbx");
                            bar.setProgress((int) (bar.getMax() * 0.5), true);
                            ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://download.ameliorated.io/AME%20Wizard%20Beta.zip -O /sdcard/AMEWizardBeta.zip");
                            bar.setProgress((int) (bar.getMax() * 0.8), true);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mount();
                                    final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                                    if (mnt_stat.isEmpty()) {
                                        icons.setVisibility(View.GONE);
                                        yesButton.setVisibility(View.VISIBLE);
                                        messages.setText(getString(R.string.ntfs) + "\nFiles downloaded in internal storage");
                                    } else {
                                        icons.setImageDrawable(atlasos);
                                        ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
                                        ShellUtils.fastCmd("cp /sdcard/ReviPlaybook.apbx " + winpath + "/Toolbox/ReviPlaybook.apbx");
                                        ShellUtils.fastCmd("cp /sdcard/AMEWizardBeta.zip " + winpath + "/Toolbox");
                                        ShellUtils.fastCmd("su -mm -c rm /sdcard/ReviPlaybook.apbx");
                                        ShellUtils.fastCmd("su -mm -c rm /sdcard/AMEWizardBeta.zip");
                                        bar.setProgress(bar.getMax(), true);
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
                        final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                        if (mnt_stat.isEmpty()) {
                            noButton.setVisibility(View.GONE);
                            yesButton.setText(getString(R.string.chat));
                            dismissButton.setText(getString(R.string.dismiss));

                            ShowBlur();
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    HideBlur();
                                    icons.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                }
                            });
                            yesButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    final Intent i = new Intent(Intent.ACTION_VIEW);
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
                                public void onClick(final View v) {
                                    HideBlur();
                                    icons.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                }
                            });
                        }
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                HideBlur();
                                icons.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        });
                    } catch (final Exception error) {
                        error.printStackTrace();
                    }

                }
            });
            yesButton.setOnClickListener(v26 -> {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.GONE);
                bar.setVisibility(View.VISIBLE);
                bar.setProgress(0);
                new Thread(() -> {
                    icons.setVisibility(View.VISIBLE);
                    icons.setImageDrawable(download);
                    try {
                        Thread.sleep(100);
                    } catch (final InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    bar.setProgress((int) (bar.getMax() * 0.00), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/modified-playbooks/releases/download/AtlasOS/AtlasPlaybook.apbx -O /sdcard/AtlasPlaybook.apbx");
                    bar.setProgress((int) (bar.getMax() * 0.5), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://download.ameliorated.io/AME%20Wizard%20Beta.zip -O /sdcard/AMEWizardBeta.zip");
                    bar.setProgress((int) (bar.getMax() * 0.8), true);
                    runOnUiThread(() -> {
                        mount();
                        final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                        Log.d("path", mnt_stat);
                        if (mnt_stat.isEmpty()) {
                            yesButton.setVisibility(View.VISIBLE);
                            icons.setVisibility(View.GONE);
                            messages.setText(getString(R.string.ntfs) + "\nFiles downloaded in internal storage");
                        } else {
                            icons.setImageDrawable(atlasos);
                            ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
                            ShellUtils.fastCmd("cp /sdcard/AtlasPlaybook.apbx " + winpath + "/Toolbox/AtlasPlaybook.apbx");
                            ShellUtils.fastCmd("cp /sdcard/AMEWizardBeta.zip " + winpath + "/Toolbox");
                            ShellUtils.fastCmd("su -mm -c rm /sdcard/AtlasPlaybook.apbx");
                            ShellUtils.fastCmd("su -mm -c rm /sdcard/AMEWizardBeta.zip");
                            bar.setProgress(bar.getMax(), true);
                            messages.setText(getString(R.string.done));
                        }
                        dismissButton.setVisibility(View.VISIBLE);
                        bar.setVisibility(View.GONE);
                    });
                }).start();
                messages.setText(getString(R.string.please_wait));
                try {
                    mount();
                    final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                    if (mnt_stat.isEmpty()) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setText(getString(R.string.chat));
                        dismissButton.setText(getString(R.string.dismiss));
                        ShowBlur();
                        dismissButton.setOnClickListener(v24 -> {
                            HideBlur();
                            icons.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        });
                        yesButton.setOnClickListener(v25 -> {
                            final Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("https://t.me/woahelperchat"));
                            startActivity(i);
                        });
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        dismissButton.setText(getString(R.string.dismiss));
                        dismissButton.setOnClickListener(v23 -> {
                            HideBlur();
                            icons.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        });
                    }
                    dismissButton.setText(getString(R.string.dismiss));
                    dismissButton.setOnClickListener(v22 -> {
                        HideBlur();
                        icons.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    });
                } catch (final Exception error) {
                    error.printStackTrace();
                }
            });

            dialog.setCancelable(false);
            dialog.show();
        });

        z.cvUsbhost.setOnClickListener(v -> {
            ShowBlur();
            noButton.setText(getString(R.string.no));
            yesButton.setText(getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(getString(R.string.usbhost_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(mnt);
            noButton.setOnClickListener(v21 -> {
                HideBlur();
                dialog.dismiss();
            });
            yesButton.setOnClickListener(v20 -> {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.GONE);
                try {
                    mount();
                    final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);

                    ShellUtils.fastCmd("cp " + getFilesDir() + "/usbhostmode.exe /sdcard");
                    if (mnt_stat.isEmpty()) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setText(getString(R.string.chat));
                        dismissButton.setText(getString(R.string.cancel));
                        yesButton.setVisibility(View.VISIBLE);
                        dismissButton.setVisibility(View.VISIBLE);
                        icons.setVisibility(View.GONE);
                        ShowBlur();
                        messages.setText(getString(R.string.ntfs));
                        dismissButton.setOnClickListener(v19 -> {
                            HideBlur();
                            icons.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        });
                        yesButton.setOnClickListener(v18 -> {
                            final Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("https://t.me/woahelperchat"));
                            startActivity(i);
                        });
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
                        ShellUtils.fastCmd("cp /sdcard/usbhostmode.exe " + winpath + "/Toolbox");
                        ShellUtils.fastCmd("rm /sdcard/usbhostmode.exe");
                        messages.setText(getString(R.string.done));
                        dismissButton.setText(getString(R.string.dismiss));
                        dismissButton.setVisibility(View.VISIBLE);
                        dismissButton.setOnClickListener(v17 -> {
                            HideBlur();
                            dialog.dismiss();
                        });
                    }
                    dismissButton.setText(getString(R.string.dismiss));
                    dismissButton.setVisibility(View.VISIBLE);
                    dismissButton.setOnClickListener(v16 -> {
                        HideBlur();
                        dialog.dismiss();
                    });
                } catch (final Exception error) {
                    error.printStackTrace();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

//		 z.cvAutoflash.setOnClickListener(new View.OnClickListener() {
//			 @Override
//			 public void onClick(View v) {
//				 ShowBlur();
//				 noButton.setText(getString(R.string.no));
//				 yesButton.setText(getString(R.string.yes));
//				 dismissButton.setVisibility(View.GONE);
//				 noButton.setVisibility(View.VISIBLE);
//				 yesButton.setVisibility(View.VISIBLE);
//				 messages.setText(getString(R.string.autoflash_question));
//				 icons.setVisibility(View.VISIBLE);
//				 icons.setImageDrawable(adrod2);
//				 noButton.setOnClickListener(new View.OnClickListener() {
//					 @Override
//					 public void onClick(View v) {
//						 HideBlur();
//						 dialog.dismiss();
//					 }
//				 });
//				 yesButton.setOnClickListener(new View.OnClickListener() {
//					 @Override
//					 public void onClick(View v) {
//						 noButton.setVisibility(View.GONE);
//						 yesButton.setVisibility(View.GONE);
//						 dismissButton.setVisibility(View.GONE);
//						 try {
//							 mount();
//							 String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
//							 ShellUtils.fastCmd("cp "+getFilesDir()+"/autoflasher.lnk /sdcard");
//							 if (mnt_stat.isEmpty()) {
//								 noButton.setVisibility(View.GONE);
//								 yesButton.setText(getString(R.string.chat));
//								 dismissButton.setText(getString(R.string.cancel));
//								 yesButton.setVisibility(View.VISIBLE);
//								 dismissButton.setVisibility(View.VISIBLE);
//								 icons.setVisibility(View.GONE);
//								 ShowBlur();
//								 messages.setText(getString(R.string.ntfs));
//								 dismissButton.setOnClickListener(new View.OnClickListener() {
//									@Override
//									public void onClick(View v) {
//										HideBlur();
//										icons.setVisibility(View.VISIBLE);
//										dialog.dismiss();
//									}
//								});
//								yesButton.setOnClickListener(new View.OnClickListener() {
//									@Override
//									public void onClick(View v) {
//										Intent i = new Intent(Intent.ACTION_VIEW);
//										i.setData(Uri.parse("https://t.me/woahelperchat"));
//										startActivity(i);
//									}
//								});
//								dialog.setCancelable(false);
//								dialog.show();
//							} else {
//								ShellUtils.fastCmd("cp /sdcard/autoflasher.lnk \'"+winpath+"/ProgramData/Microsoft/Windows/Start Menu/Programs/Startup\'");
//								messages.setText(getString(R.string.done));
//								dismissButton.setText(getString(R.string.dismiss));
//								dismissButton.setVisibility(View.VISIBLE);
//								dismissButton.setOnClickListener(new View.OnClickListener() {
//									@Override
//									public void onClick(View v) {
//										HideBlur();
//										dialog.dismiss();
//									}
//								});
//							}
//							dismissButton.setText(getString(R.string.dismiss));
//							dismissButton.setVisibility(View.VISIBLE);
//							dismissButton.setOnClickListener(new View.OnClickListener() {
//								@Override
//								public void onClick(View v) {
//									HideBlur();
//									dialog.dismiss();
//								}
//							});
//						} catch (Exception error) {
//							error.printStackTrace();
//						}
//					}
//				});
//				dialog.setCancelable(false);
//				dialog.show();
//			}
//		});

        z.cvSetup.setOnClickListener(v -> {
            ShowBlur();
            noButton.setText(getString(R.string.no));
            yesButton.setText(getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(getString(R.string.setup_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(mnt);
            if (!isNetworkConnected(this)) {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.VISIBLE);
                dismissButton.setText(getString(R.string.dismiss));
                messages.setText("No internet connection\nConnect to the internet and try again");
            }
            dismissButton.setOnClickListener(v1 -> {
                HideBlur();
                dialog.dismiss();
            });
            noButton.setOnClickListener(v2 -> {
                HideBlur();
                dialog.dismiss();
            });
            yesButton.setOnClickListener(v3 -> {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.GONE);
                bar.setVisibility(View.VISIBLE);
                bar.setProgress(0);
                new Thread(() -> {
                    icons.setVisibility(View.VISIBLE);
                    icons.setImageDrawable(download);
                    try {
                        Thread.sleep(100);
                    } catch (final InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    bar.setProgress((int) (bar.getMax() * 0.00), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2005vcredist_x64.EXE /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.05), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2005vcredist_x86.EXE /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.1), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2008vcredist_x64.exe /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.15), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2008vcredist_x86.exe /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.2), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2010vcredist_x64.exe /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.25), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2010vcredist_x86.exe /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.3), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2012vcredist_x64.exe /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.35), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2012vcredist_x86.exe /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.4), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2013vcredist_x64.exe /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.45), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2013vcredist_x86.exe /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.5), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2015VC_redist.x64.exe /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.55), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2015VC_redist.x86.exe /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.6), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2022VC_redist.arm64.exe /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.65), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/dxwebsetup.exe /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.7), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/oalinst.exe /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.75), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/PhysX-9.13.0604-SystemSoftware-Legacy.msi /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.8), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/PhysX-9.19.0218-SystemSoftware.exe /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.85), true);
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/xnafx40_redist.msi /sdcard/");
                    bar.setProgress((int) (bar.getMax() * 0.9), true);
                    runOnUiThread(() -> {
                        mount();
                        final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                        Log.d("path", mnt_stat);
                        if (mnt_stat.isEmpty()) {
                            yesButton.setVisibility(View.VISIBLE);
                            icons.setVisibility(View.GONE);
                            messages.setText(getString(R.string.ntfs) + "\nFiles downloaded in internal storage");
                        } else {
                            icons.setImageDrawable(mnt);
                            ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
                            ShellUtils.fastCmd("cp /sdcard/2005vcredist_x64.EXE " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/2005vcredist_x86.EXE " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/2008vcredist_x64.exe " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/2008vcredist_x86.exe " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/2010vcredist_x64.exe " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/2010vcredist_x86.exe " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/2012vcredist_x64.exe " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/2012vcredist_x86.exe " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/2013vcredist_x64.exe " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/2013vcredist_x86.exe " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/2015VC_redist.x64.exe " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/2015VC_redist.x86.exe " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/2022VC_redist.arm64.exe " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/dxwebsetup.exe " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/oalinst.exe " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/PhysX-9.13.0604-SystemSoftware-Legacy.msi " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/PhysX-9.19.0218-SystemSoftware.exe " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("cp /sdcard/xnafx40_redist.msi " + winpath + "/Toolbox/");
                            ShellUtils.fastCmd("rm /sdcard/2005vcredist_x64.EXE");
                            ShellUtils.fastCmd("rm /sdcard/2005vcredist_x86.EXE");
                            ShellUtils.fastCmd("rm /sdcard/2008vcredist_x64.exe");
                            ShellUtils.fastCmd("rm /sdcard/2008vcredist_x86.exe");
                            ShellUtils.fastCmd("rm /sdcard/2010vcredist_x64.exe");
                            ShellUtils.fastCmd("rm /sdcard/2010vcredist_x86.exe");
                            ShellUtils.fastCmd("rm /sdcard/2012vcredist_x64.exe");
                            ShellUtils.fastCmd("rm /sdcard/2012vcredist_x86.exe");
                            ShellUtils.fastCmd("rm /sdcard/2013vcredist_x64.exe");
                            ShellUtils.fastCmd("rm /sdcard/2013vcredist_x86.exe");
                            ShellUtils.fastCmd("rm /sdcard/2015VC_redist.x64.exe");
                            ShellUtils.fastCmd("rm /sdcard/2015VC_redist.x86.exe");
                            ShellUtils.fastCmd("rm /sdcard/2022VC_redist.arm64.exe");
                            ShellUtils.fastCmd("rm /sdcard/dxwebsetup.exe");
                            ShellUtils.fastCmd("rm /sdcard/oalinst.exe");
                            ShellUtils.fastCmd("rm /sdcard/PhysX-9.13.0604-SystemSoftware-Legacy.msi");
                            ShellUtils.fastCmd("rm /sdcard/PhysX-9.19.0218-SystemSoftware.exe");
                            ShellUtils.fastCmd("rm /sdcard/xnafx40_redist.msi");
                            bar.setProgress(bar.getMax(), true);
                            messages.setText(getString(R.string.done));
                        }
                        dismissButton.setVisibility(View.VISIBLE);
                        bar.setVisibility(View.GONE);
                    });
                }).start();
                messages.setText(getString(R.string.please_wait));
                try {
                    mount();
                    final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                    if (mnt_stat.isEmpty()) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setText(getString(R.string.chat));
                        dismissButton.setText(getString(R.string.dismiss));
                        ShowBlur();
                        dismissButton.setOnClickListener(v4 -> {
                            HideBlur();
                            icons.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        });
                        yesButton.setOnClickListener(v5 -> {
                            final Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("https://t.me/woahelperchat"));
                            startActivity(i);
                        });
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        dismissButton.setText(getString(R.string.dismiss));
                        dismissButton.setOnClickListener(v6 -> {
                            HideBlur();
                            icons.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        });
                    }
                    dismissButton.setText(getString(R.string.dismiss));
                    dismissButton.setOnClickListener(v7 -> {
                        HideBlur();
                        icons.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    });
                } catch (final Exception error) {
                    error.printStackTrace();
                }
            });

            dialog.setCancelable(false);
            dialog.show();
        });

        z.cvDefender.setOnClickListener(v -> {
            ShowBlur();
            noButton.setText(getString(R.string.no));
            yesButton.setText(getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(getString(R.string.defender_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(sensors);
            noButton.setOnClickListener(v8 -> {
                HideBlur();
                dialog.dismiss();
            });
            yesButton.setOnClickListener(v9 -> {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.GONE);
                try {
                    mount();
                    final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                    ShellUtils.fastCmd("cp " + getFilesDir() + "/DefenderRemover.exe /sdcard");
                    if (mnt_stat.isEmpty()) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setText(getString(R.string.chat));
                        dismissButton.setText(getString(R.string.cancel));
                        yesButton.setVisibility(View.VISIBLE);
                        dismissButton.setVisibility(View.VISIBLE);
                        icons.setVisibility(View.GONE);
                        ShowBlur();
                        messages.setText(getString(R.string.ntfs));
                        dismissButton.setOnClickListener(v10 -> {
                            HideBlur();
                            icons.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        });
                        yesButton.setOnClickListener(v11 -> {
                            final Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("https://t.me/woahelperchat"));
                            startActivity(i);
                        });
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
                        ShellUtils.fastCmd("cp /sdcard/DefenderRemover.exe " + winpath + "/Toolbox");
                        ShellUtils.fastCmd("rm /sdcard/DefenderRemover.exe");
                        messages.setText(getString(R.string.done));
                        dismissButton.setText(getString(R.string.dismiss));
                        dismissButton.setVisibility(View.VISIBLE);
                        dismissButton.setOnClickListener(v12 -> {
                            HideBlur();
                            dialog.dismiss();
                        });
                    }
                    dismissButton.setText(getString(R.string.dismiss));
                    dismissButton.setVisibility(View.VISIBLE);
                    dismissButton.setOnClickListener(v13 -> {
                        HideBlur();
                        dialog.dismiss();
                    });
                } catch (final Exception error) {
                    error.printStackTrace();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });


        z.cvRotation.setOnClickListener(v -> {
            ShowBlur();
            noButton.setText(getString(R.string.no));
            yesButton.setText(getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(getString(R.string.rotation_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(boot);
            noButton.setOnClickListener(v14 -> {
                HideBlur();
                dialog.dismiss();
            });
            yesButton.setOnClickListener(v15 -> {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.GONE);
                try {
                    mount();
                    final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                    ShellUtils.fastCmd("cp " + getFilesDir() + "/display.exe /sdcard/");
                    ShellUtils.fastCmd("cp " + getFilesDir() + "/RotationShortcut.lnk /sdcard/");
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
                            public void onClick(final View v15) {
                                HideBlur();
                                icons.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        });
                        yesButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v15) {
                                final Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse("https://t.me/woahelperchat"));
                                startActivity(i);
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
                        ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox/Rotation || true ");
                        ShellUtils.fastCmd("cp /sdcard/display.exe " + winpath + "/Toolbox/Rotation");
                        ShellUtils.fastCmd("cp /sdcard/RotationShortcut.lnk " + winpath + "/Toolbox/Rotation");
                        ShellUtils.fastCmd("rm /sdcard/display.exe");
                        ShellUtils.fastCmd("rm /sdcard/RotationShortcut.lnk");
                        messages.setText(getString(R.string.done));
                        dismissButton.setText(getString(R.string.dismiss));
                        dismissButton.setVisibility(View.VISIBLE);
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v15) {
                                HideBlur();
                                dialog.dismiss();
                            }
                        });
                    }
                    dismissButton.setText(getString(R.string.dismiss));
                    dismissButton.setVisibility(View.VISIBLE);
                    dismissButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v15) {
                            HideBlur();
                            dialog.dismiss();
                        }
                    });
                } catch (final Exception error) {
                    error.printStackTrace();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

        z.cvEdge.setOnClickListener(v -> {
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
                public void onClick(final View v) {
                    HideBlur();
                    dialog.dismiss();
                }
            });
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    noButton.setVisibility(View.GONE);
                    yesButton.setVisibility(View.GONE);
                    dismissButton.setVisibility(View.GONE);
                    try {
                        mount();
                        final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                        ShellUtils.fastCmd("cp " + getFilesDir() + "/RemoveEdge.ps1 /sdcard");
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
                                public void onClick(final View v) {
                                    HideBlur();
                                    icons.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                }
                            });
                            yesButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    final Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse("https://t.me/woahelperchat"));
                                    startActivity(i);
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.show();
                        } else {
                            ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
                            ShellUtils.fastCmd("cp /sdcard/RemoveEdge.ps1 " + winpath + "/Toolbox");
                            ShellUtils.fastCmd("rm /sdcard/RemoveEdge.ps1");
                            messages.setText(getString(R.string.done));
                            dismissButton.setText(getString(R.string.dismiss));
                            dismissButton.setVisibility(View.VISIBLE);
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    HideBlur();
                                    dialog.dismiss();
                                }
                            });
                        }
                        dismissButton.setText(getString(R.string.dismiss));
                        dismissButton.setVisibility(View.VISIBLE);
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                HideBlur();
                                dialog.dismiss();
                            }
                        });
                    } catch (final Exception error) {
                        error.printStackTrace();
                    }
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

        n.cvSoftware.setOnClickListener(v -> {
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
                public void onClick(final View v) {
                    HideBlur();
                    dialog.dismiss();
                }
            });
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    noButton.setVisibility(View.GONE);
                    yesButton.setVisibility(View.GONE);
                    dismissButton.setVisibility(View.GONE);
                    try {
                        mount();
                        final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
                        ShellUtils.fastCmd("cp " + getFilesDir() + "/WorksOnWoa.url /sdcard");
                        ShellUtils.fastCmd("cp " + getFilesDir() + "/TestedSoftware.url /sdcard");
                        ShellUtils.fastCmd("cp " + getFilesDir() + "/ARMSoftware.url /sdcard");
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
                                public void onClick(final View v) {
                                    HideBlur();
                                    icons.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                }
                            });
                            yesButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    final Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse("https://t.me/woahelperchat"));
                                    startActivity(i);
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.show();
                        } else {
                            ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
                            ShellUtils.fastCmd("cp /sdcard/WorksOnWoa.url " + winpath + "/Toolbox");
                            ShellUtils.fastCmd("cp /sdcard/TestedSoftware.url " + winpath + "/Toolbox");
                            ShellUtils.fastCmd("cp /sdcard/ARMSoftware.url " + winpath + "/Toolbox");
                            ShellUtils.fastCmd("rm /sdcard/WorksOnWoa.url");
                            ShellUtils.fastCmd("rm /sdcard/TestedSoftware.url");
                            ShellUtils.fastCmd("rm /sdcard/ARMSoftware.url");
                            messages.setText(getString(R.string.done));
                            dismissButton.setText(getString(R.string.dismiss));
                            dismissButton.setVisibility(View.VISIBLE);
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    HideBlur();
                                    dialog.dismiss();
                                }
                            });
                        }
                        dismissButton.setText(getString(R.string.dismiss));
                        dismissButton.setVisibility(View.VISIBLE);
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                HideBlur();
                                dialog.dismiss();
                            }
                        });
                    } catch (final Exception error) {
                        error.printStackTrace();
                    }
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

        n.cvFlashUefi.setOnClickListener(v -> {
            ShowBlur();
            noButton.setText(getString(R.string.no));
            dismissButton.setText(getString(R.string.dismiss));
            yesButton.setText(getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            yesButton.setVisibility(View.VISIBLE);
            noButton.setVisibility(View.VISIBLE);
            messages.setText(getString(R.string.flash_uefi_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(uefi);
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
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
                            } catch (final Exception error) {
                                error.printStackTrace();
                            }
                        }
                    }, 500);
                }
            });
            dismissButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    HideBlur();
                    dialog.dismiss();
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    HideBlur();
                    dialog.dismiss();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

        n.cvDbkp.setOnClickListener(v -> {
            ShowBlur();
            dismissButton.setVisibility(View.GONE);
            yesButton.setVisibility(View.VISIBLE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setText(getString(R.string.yes));
            noButton.setText(getString(R.string.no));
            dismissButton.setText(getString(R.string.dismiss));
            messages.setText(getString(R.string.dbkp_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(uefi);
            if (!MainActivity.isNetworkConnected(this)) {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.VISIBLE);
                dismissButton.setText(getString(R.string.dismiss));
                messages.setText("No internet connection\nConnect to the internet and try again");
            }
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    noButton.setVisibility(View.GONE);
                    yesButton.setVisibility(View.GONE);
                    messages.setText(getString(R.string.please_wait));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String bedan = ShellUtils.fastCmd("getprop ro.product.device");
                                if ("guacamole".equals(bedan)
                                        || "OnePlus7Pro".equals(bedan)
                                        || "OnePlus7Pro4G".equals(bedan)) {
                                    // Do guacamole logic here
                                    ShellUtils.fastCmd("mkdir /sdcard/dbkp");
                                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woa-op7/releases/download/DBKP/guacamole.fd -O /sdcard/dbkp/guacamole.fd");
                                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp");
                                    ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp8150.cfg /sdcard/dbkp");
                                    ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp.hotdog.bin /sdcard/dbkp");
                                    ShellUtils.fastCmd(getString(R.string.backup));
                                    ShellUtils.fastCmd("cp /sdcard/boot.img /sdcard/dbkp/boot.img");
                                    ShellUtils.fastCmd("cd /sdcard/dbkp");
                                    ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
                                    ShellUtils.fastCmd("su -mm -c /sdcard/dbkp/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/guacamole.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.hotdog.bin");
                                    ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
                                    ShellUtils.fastCmd("su -mm -c mv output kernel");
                                    ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
                                    ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/dbkp8150.cfg || true & su -mm -c rm /sdcard/dbkp/dbkp.hotdog.bin || true & su -mm -c rm /sdcard/dbkp/ramdisk.cpio || true & su -mm -c rm /sdcard/dbkp/boot.img || true & su -mm -c rm /sdcard/dbkp/kernel_dtb || true & su -mm -c rm /sdcard/dbkp/kernel || true & su -mm -c rm /sdcard/dbkp/dbkp || true & su -mm -c rm /sdcard/dbkp/guacamole.fd || true");
                                    ShellUtils.fastCmd("su -mm -c mv new-boot.img patched-boot.img");
                                    ShellUtils.fastCmd("dd if=/sdcard/dbkp/patched-boot.img of=/dev/block/by-name/boot_a bs=16M");
                                    ShellUtils.fastCmd("dd if=/sdcard/dbkp/patched-boot.img of=/dev/block/by-name/boot_b bs=16M");
                                    messages.setText("Boot image has been patched and flashed.\nPatched image can also be found at /sdcard/patched-boot.img.\nUse your alert slider to switch between Windows and Android.");
                                    dismissButton.setVisibility(View.VISIBLE);
                                } else if ("hotdog".equals(bedan)
                                        || "OnePlus7TPro".equals(bedan)
                                        || "OnePlus7TPro4G".equals(bedan)) {
                                    // Do hotdog logic here
                                    ShellUtils.fastCmd("mkdir /sdcard/dbkp");
                                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woa-op7/releases/download/DBKP/hotdog.fd -O /sdcard/dbkp/hotdog.fd");
                                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp");
                                    ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp8150.cfg /sdcard/dbkp");
                                    ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp.hotdog.bin /sdcard/dbkp");
                                    ShellUtils.fastCmd(getString(R.string.backup));
                                    ShellUtils.fastCmd("cp /sdcard/boot.img /sdcard/dbkp/boot.img");
                                    ShellUtils.fastCmd("cd /sdcard/dbkp");
                                    ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
                                    ShellUtils.fastCmd("su -mm -c /sdcard/dbkp/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/hotdog.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.hotdog.bin");
                                    ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
                                    ShellUtils.fastCmd("su -mm -c mv output kernel");
                                    ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
                                    ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/dbkp8150.cfg || true & su -mm -c rm /sdcard/dbkp/dbkp.hotdog.bin || true & su -mm -c rm /sdcard/dbkp/ramdisk.cpio || true & su -mm -c rm /sdcard/dbkp/boot.img || true & su -mm -c rm /sdcard/dbkp/kernel_dtb || true & su -mm -c rm /sdcard/dbkp/kernel || true & su -mm -c rm /sdcard/dbkp/dbkp || true & su -mm -c rm /sdcard/dbkp/hotdog.fd || true");
                                    ShellUtils.fastCmd("su -mm -c mv new-boot.img patched-boot.img");
                                    ShellUtils.fastCmd("dd if=/sdcard/dbkp/patched-boot.img of=/dev/block/by-name/boot_a bs=16M");
                                    ShellUtils.fastCmd("dd if=/sdcard/dbkp/patched-boot.img of=/dev/block/by-name/boot_b bs=16M");
                                    messages.setText("Boot image has been patched and flashed.\nPatched image can also be found at /sdcard/patched-boot.img.\nUse your alert slider to switch between Windows and Android.");
                                    dismissButton.setVisibility(View.VISIBLE);
                                }
                            } catch (final Exception error) {
                                error.printStackTrace();
                            }
                        }
                    }, 500);
                }
            });
            dismissButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    HideBlur();
                    dialog.dismiss();
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    HideBlur();
                    dialog.dismiss();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

        x.cvBackup.setOnClickListener(
                v -> {
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
                        public void onClick(final View v) {
                            noButton.setVisibility(View.GONE);
                            yesButton.setVisibility(View.GONE);
                            dismissButton.setVisibility(View.GONE);
                            messages.setText(getString(R.string.please_wait));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
                                    final String currentDateAndTime = sdf.format(new Date());
                                    pref.setDATE(MainActivity.this, currentDateAndTime);
                                    x.tvDate.setText(String.format(getString(R.string.last), pref.getDATE(MainActivity.this)));
                                    final String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
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
                        public void onClick(final View v) {
                            HideBlur();
                            dialog.dismiss();
                        }
                    });
                    noButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            noButton.setVisibility(View.GONE);
                            yesButton.setVisibility(View.GONE);
                            dismissButton.setVisibility(View.GONE);
                            messages.setText(getString(R.string.please_wait));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
                                    final String currentDateAndTime = sdf.format(new Date());
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
                });

        k.toolbarlayout.toolbar.setTitle(getString(R.string.preferences));
        k.toolbarlayout.toolbar.setNavigationIcon(iconToolbar);

        //MainActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        x.toolbarlayout.settings.setOnClickListener(v -> {
            backable = 1;
            x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
            setContentView(k.getRoot());
            k.settingsPanel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
            k.modemdump.setChecked(pref.getMODEM(this));
            k.backupQB.setChecked(pref.getBACKUP(this));
            k.backupQBA.setChecked(pref.getBACKUP_A(this));
            k.autobackup.setChecked(!pref.getAUTO(this));
            k.autobackupA.setChecked(!pref.getAUTO(this));
            k.confirmation.setChecked(pref.getCONFIRM(this));
            k.automount.setChecked(pref.getAutoMount(this));
            k.securelock.setChecked(!pref.getSecure(this));
            k.mountLocation.setChecked(pref.getMountLocation(this));
            k.toolbarlayout.settings.setVisibility(View.GONE);
            //k.language.setText(R.string.language);
        });

        k.mountLocation.setOnCheckedChangeListener((compoundButton, b) -> {
            pref.setMountLocation(this, b);
            winpath = (b ? "/mnt/Windows" : "/mnt/sdcard/Windows");
        });

        k.modemdump.setOnCheckedChangeListener((compoundButton, b) -> {
            pref.setMODEM(this, b);
            if (b)
                n.cvDumpModem.setVisibility(View.GONE);
            else
                n.cvDumpModem.setVisibility(View.VISIBLE);
        });

        d.toolbarlayout.back.setVisibility(View.VISIBLE);
        d.toolbarlayout.back.setOnClickListener(v -> {
            getWindow().getCurrentFocus().startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
            setContentView(x.getRoot());
            x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));

            backable = 0;
        });
        k.backupQB.setOnCheckedChangeListener((compoundButton, b) -> {
            if (pref.getBACKUP(this)) {
                pref.setBACKUP(this, false);
                this.k.autobackup.setVisibility(View.VISIBLE);
            } else {
                dialog.show();
                dialog.setCancelable(false);
                messages.setText(this.getString(R.string.bwarn));
                yesButton.setVisibility(View.VISIBLE);
                noButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.VISIBLE);
                yesButton.setText(this.getString(R.string.agree));
                dismissButton.setText(this.getString(R.string.cancel));
                yesButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(final View v) {
                        pref.setBACKUP(MainActivity.this, true);
                        MainActivity.this.k.autobackup.setVisibility(View.GONE);
                        dialog.dismiss();
                    }
                });
                dismissButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(final View v) {
                        MainActivity.this.k.backupQB.setChecked(false);
                        dialog.dismiss();
                    }
                });
            }
        });

        k.backupQBA.setOnCheckedChangeListener((compoundButton, b) -> {
            if (pref.getBACKUP_A(this)) {
                pref.setBACKUP_A(this, false);
                this.k.autobackupA.setVisibility(View.VISIBLE);
            } else {
                dialog.show();
                dialog.setCancelable(false);
                messages.setText(this.getString(R.string.bwarn));
                yesButton.setVisibility(View.VISIBLE);
                noButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.VISIBLE);
                yesButton.setText(this.getString(R.string.agree));
                dismissButton.setText(this.getString(R.string.cancel));
                yesButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(final View v) {
                        pref.setBACKUP_A(MainActivity.this, true);
                        MainActivity.this.k.autobackupA.setVisibility(View.GONE);
                        dialog.dismiss();
                    }
                });
                dismissButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(final View v) {
                        MainActivity.this.k.backupQBA.setChecked(false);
                        dialog.dismiss();
                    }
                });
            }
        });

        x.cvInfo.setOnClickListener(v -> new Thread(new Runnable() {
            @Override
            public void run() {
                //if (!BuildConfig.VERSION_NAME.equals(ShellUtils.fastCmd(getFilesDir()+"/busybox wget -q -O - https://raw.githubusercontent.com/Marius586/WoA-Helper-update/main/README.md"))){
                //update();
                //}
            }
        }).start());

        k.autobackup.setOnCheckedChangeListener((compoundButton, b) -> pref.setAUTO(this, !b));
        k.autobackupA.setOnCheckedChangeListener((compoundButton, b) -> pref.setAUTO_A(this, !b));

        k.confirmation.setOnCheckedChangeListener((compoundButton, b) -> pref.setCONFIRM(this, b));

        k.securelock.setOnCheckedChangeListener((compoundButton, b) -> pref.setSecure(this, !b));

        k.automount.setOnCheckedChangeListener((compoundButton, b) -> pref.setAutoMount(this, b));

        k.button.setOnClickListener(v -> {
            k.settingsPanel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
            setContentView(x.getRoot());
            x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
            backable = 0;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkuefi();
    }

    @Override
    public void onBackPressed() {
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        switch (viewGroup.getId()) {
            case R.id.scriptstab:
                z.scriptstab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
                setContentView(n.getRoot());
                n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
                backable++;
                break;
            case R.id.toolboxtab:
                n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
                setContentView(x.getRoot());
                x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
                break;
            case R.id.settingsPanel:
                k.settingsPanel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
                setContentView(x.getRoot());
                x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
                break;
            case R.id.mainlayout:
                finish();
                break;
            default:
                x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
                setContentView(x.getRoot());
                break;
        }
    }

    public void update() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!"true".equals(ShellUtils.fastCmd("if [ -e " + getFilesDir() + "/woahelper.apk ] ; then echo true ; fi"))) {
                    ShellUtils.fastCmd(getFilesDir() + "/busybox wget https://raw.githubusercontent.com/Marius586/WoA-Helper-update/main/woahelper.apk -O " + getFilesDir() + "/woahelper.apk");
                    ShellUtils.fastCmd("pm install " + getFilesDir() + "/woahelper.apk && rm " + getFilesDir() + "/woahelper.apk");
                }

            }
        }).start();
    }

    public void flash(final String uefi) {
        ShellUtils.fastCmd(
                "dd if=" + uefi + " of=/dev/block/bootdevice/by-name/boot$(getprop ro.boot.slot_suffix) bs=16M");
    }

    public void mount() {
        final String c;
        if (pref.getMountLocation(this)) {
            c = String.format("su -mm -c " + pref.getbusybox(this) + getString(R.string.mount2), win);
            ShellUtils.fastCmd(getString(R.string.mk2));
            ShellUtils.fastCmd(c);
        } else {
            c = String.format("su -mm -c " + pref.getbusybox(this) + getString(R.string.mount), win);
            ShellUtils.fastCmd(getString(R.string.mk));
            ShellUtils.fastCmd(c);
        }
        mounted = getString(R.string.unmountt);
        x.tvMnt.setText(String.format(getString(R.string.mnt_title), mounted));
    }

    public void winBackup() {
        if (pref.getMountLocation(this)) {
            ShellUtils.fastCmd(getString(R.string.mk2));
            ShellUtils.fastCmd(String.format(pref.getbusybox(this) + getString(R.string.mount2), win));
            ShellUtils.fastCmd(getString(R.string.backup2));
            ShellUtils.fastCmd(getString(R.string.unmount2));
            ShellUtils.fastCmd(getString(R.string.rm2));
        } else {
            ShellUtils.fastCmd(getString(R.string.mk));
            ShellUtils.fastCmd(String.format(pref.getbusybox(this) + getString(R.string.mount), win));
            ShellUtils.fastCmd(getString(R.string.backup1));
            ShellUtils.fastCmd(getString(R.string.unmount));
            ShellUtils.fastCmd(getString(R.string.rm));
        }
    }

    public void unmount() {
        if (pref.getMountLocation(this)) {
            ShellUtils.fastCmd(getString(R.string.unmount2));
            ShellUtils.fastCmd(getString(R.string.rm2));
        } else {
            ShellUtils.fastCmd(getString(R.string.unmount));
            ShellUtils.fastCmd(getString(R.string.rm));
        }
        mounted = getString(R.string.mountt);
        x.tvMnt.setText(String.format(getString(R.string.mnt_title), mounted));
    }


    public void dump() {
        if (pref.getMountLocation(this)) {
            ShellUtils.fastCmd(getString(R.string.modem12));
            ShellUtils.fastCmd(getString(R.string.modem22));
        } else {
            ShellUtils.fastCmd(getString(R.string.modem1));
            ShellUtils.fastCmd(getString(R.string.modem2));
        }
    }

    public void checkdevice() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        final MaterialButton yesButton = dialog.findViewById(R.id.yes);
        final MaterialButton noButton = dialog.findViewById(R.id.no);
        final MaterialButton dismissButton = dialog.findViewById(R.id.dismiss);
        final TextView messages = dialog.findViewById(R.id.messages);
        yesButton.setVisibility(View.GONE);
        noButton.setVisibility(View.GONE);
        dismissButton.setVisibility(View.GONE);
        if (Boolean.FALSE.equals(Shell.isAppGrantedRoot())) {
            messages.setText(getString(R.string.nonroot));
            dialog.show();
            dialog.setCancelable(false);
        }
        final String[] supported = {"a52sxq", "alphalm_lao_com", "alphaplus_lao_com", "alioth", "alphalm", "alphaplus", "andromeda", "betalm", "betaplus_lao_com", "betalm_lao_com", "beryllium", "bhima", "cepheus", "cheeseburger", "curtana2", "chiron", "curtana", "curtana_india", "joyeuse", "curtana_cn", "curtanacn", "cmi", "davinci", "dumpling", "dipper", "durandal", "durandal_india", "enchilada", "equuleus", "excalibur", "excalibur_india", "flashlmdd", "flashlmdd_lao_com", "fajita", "houji", "joan", "judyln", "judyp", "judypn", "guacamole", "guacamoleb", "gram", "hotdog", "hotdogb", "hotdogg", "lisa", "marble", "mh2lm", "mh2plus_lao_com", "mh2lm_lao_com", "mh2lm5g", "mh2lm5g_lao_com", "miatoll", "nabu", "pipa", "OnePlus6", "OnePlus6T", "OnePlus7", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7T", "OnePlus7TPro", "OnePlus7TPro4G", "OnePlus7TPro5G", "OP7ProNRSpr", "OnePlus7TProNR", "perseus", "polaris", "Pong", "pong", "q2q", "raphael", "raphaelin", "raphaels", "RMX2170", "RMX2061", "sagit", "surya", "vayu", "venus", "winner", "winnerx", "xpeng", "G973F", "SM-G973F", "beyond1lte", "beyond1qlte", "G973U", "G973U1", "SM-G973U", "SM-G973U1", "G9730", "SM-G9730", "G973N", "SM-G973N", "G973X", "SM-G973X", "G973C", "SM-G973C", "SCV41", "SM-SC41", "beyond1"};
        device = ShellUtils.fastCmd("getprop ro.product.device ");
        model = ShellUtils.fastCmd("getprop ro.product.model");
        if (!Arrays.asList(supported).contains(device)) {
            k.modemdump.setVisibility(View.VISIBLE);
            if (!pref.getAGREE(this)) {
                messages.setText(getString(R.string.unsupported));
                device = "unknown";
                dialog.show();
                yesButton.setText(R.string.sure);
                yesButton.setVisibility(View.VISIBLE);
                yesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        dialog.dismiss();
                        pref.setAGREE(MainActivity.this, true);
                    }
                });
            }
            device = "unknown";
        }

        final String stram = extractNumberFromString(new RAM().getMemory(getApplicationContext()));
        ramvalue = Double.parseDouble(stram) / 100;

        final String run = ShellUtils.fastCmd(" su -c cat /proc/cmdline ");
        if (run.isEmpty()) this.panel = "Unknown";
        else {
            if (run.contains("j20s_42") || run.contains("k82_42")) {
                this.panel = "Huaxing";
            } else if (run.contains("j20s_36") || run.contains("k82_36")) {
                this.panel = "Tianma";
            } else {
                this.panel = ShellUtils.fastCmd("su -c cat /proc/cmdline | tr ' :=' '\n'|grep dsi|tr ' _' '\n'|tail -3|head -1 ");
                if (!this.panel.isEmpty())
                    if ((!(Objects.equals(this.panel, "f1mp") || Objects.equals(this.panel, "f1p2") || Objects.equals(this.panel, "global") || pref.getAGREE(this))) && ("raphael".equals(device) || "raphaelin".equals(device) || "raphaels".equals(device) || "cepheus".equals(device))) {
                        dialog.show();
                        messages.setText(this.getString(R.string.upanel));
                        yesButton.setText(this.getString(R.string.chat));
                        dismissButton.setText(this.getString(R.string.nah));
                        noButton.setText(this.getString(R.string.later));
                        yesButton.setVisibility(View.VISIBLE);
                        noButton.setVisibility(View.VISIBLE);
                        dismissButton.setVisibility(View.VISIBLE);
                        yesButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {

                                final Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(MainActivity.this.grouplink));
                                MainActivity.this.startActivity(i);
                                dialog.dismiss();
                                pref.setAGREE(MainActivity.this, true);
                            }
                        });
                        noButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                dialog.dismiss();
                            }
                        });
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                dialog.dismiss();
                                pref.setAGREE(MainActivity.this, true);
                            }
                        });
                    }
            }
            if (Objects.equals(this.panel, "2"))
                this.panel = "f1p2_2";
        }

    }

    public void checkuefi() {
        ShellUtils.fastCmd("su -c mkdir /sdcard/UEFI");
        finduefi = ShellUtils.fastCmd(getString(R.string.uefiChk));
        device = ShellUtils.fastCmd("getprop ro.product.device ");
        if (this.finduefi.contains("img")) {
            x.tvQuickBoot.setText(this.getString(R.string.quickboot_title));
            x.cvQuickBoot.setEnabled(true);
            x.cvQuickBoot.setVisibility(View.VISIBLE);
            x.tvQuickBoot.setText(this.getString(R.string.quickboot_title));
            n.tvFlashUefi.setText(getString(R.string.flash_uefi_title));
            n.tvUefiSubtitle.setText(getString(R.string.flash_uefi_subtitle));
            n.cvFlashUefi.setEnabled(true);
            x.tvToolbox.setText(getString(R.string.toolbox_title));
            x.cvToolbox.setEnabled(true);
            x.tvToolboxSubtitle.setText(getString(R.string.toolbox_subtitle));
            x.tvToolboxSubtitle.setVisibility(View.VISIBLE);
            try {
                final String[] dumpeddevices = {"bhima", "cepheus", "guacamole", "guacamoleb", "hotdog", "hotdogb", "OnePlus7", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7T", "OnePlus7TPro", "OnePlus7TPro4G", "OP7ProNRSpr", "raphael", "raphaelin", "raphaels", "vayu"};
                if (Arrays.asList(dumpeddevices).contains(this.device)) {
                    this.x.tvBootSubtitle.setText(this.getString(R.string.quickboot_subtitle));
                } else {
                    this.x.tvBootSubtitle.setText(this.getString(R.string.quickboot_subtitle_nabu));
                }

            } catch (final Exception error) {
                error.printStackTrace();
            }
        } else {
            x.cvQuickBoot.setEnabled(false);
            x.cvQuickBoot.setVisibility(View.VISIBLE);
            x.tvQuickBoot.setText(this.getString(R.string.uefi_not_found));
            x.tvBootSubtitle.setText(String.format(this.getString(R.string.uefi_not_found_subtitle), this.device));
            n.tvFlashUefi.setText(getString(R.string.uefi_not_found));
            n.tvUefiSubtitle.setText(String.format(getString(R.string.uefi_not_found_subtitle), device));
            n.cvFlashUefi.setEnabled(false);
            x.tvToolbox.setText(getString(R.string.toolbox_title));
            x.cvToolbox.setEnabled(true);
            x.tvToolboxSubtitle.setText(getString(R.string.toolbox_subtitle));
            x.tvToolboxSubtitle.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final TextView myTextView1 = findViewById(R.id.text);
        final TextView myTextView2 = findViewById(R.id.deviceName);
        final TextView myTextView3 = findViewById(R.id.tv_panel);
        final TextView myTextView4 = findViewById(R.id.tv_ramvalue);
        final TextView myTextView5 = findViewById(R.id.guide_text);
        final TextView myTextView6 = findViewById(R.id.group_text);
        final TextView myTextView7 = findViewById(R.id.tv_slot);
        final TextView myTextView8 = findViewById(R.id.tv_date);
        if (Configuration.ORIENTATION_LANDSCAPE != newConfig.orientation && Objects.equals(device, "nabu")) {
            x.app.setOrientation(LinearLayout.VERTICAL);
            x.top.setOrientation(LinearLayout.HORIZONTAL);
            myTextView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE2);
            myTextView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE3);
            myTextView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE3);
            myTextView4.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE3);
            myTextView5.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE3);
            myTextView6.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE3);
            myTextView7.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE3);
            myTextView8.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE3);
        } else if (Configuration.ORIENTATION_PORTRAIT != newConfig.orientation && Objects.equals(device, "nabu")) {
            x.app.setOrientation(LinearLayout.HORIZONTAL);
            x.top.setOrientation(LinearLayout.VERTICAL);
            myTextView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE3);
            myTextView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            myTextView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            myTextView4.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            myTextView5.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            myTextView6.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            myTextView7.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
            myTextView8.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
        }
    }

    public void ShowBlur() {
        x.blur.setVisibility(View.VISIBLE);
    }

    public void HideBlur() {
        x.blur.setVisibility(View.GONE);
    }

    public static boolean isNetworkConnected(final Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);

        final Network activeNetwork = connectivityManager.getActiveNetwork();
        final NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);

        return null != capabilities && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    @SuppressLint("AppBundleLocaleChanges")
    private void setApplicationLocale(final String locale) {
        final Resources resources = getResources();
        final DisplayMetrics dm = resources.getDisplayMetrics();
        final Configuration config = resources.getConfiguration();
        config.setLocale(new Locale(locale.toLowerCase()));
        resources.updateConfiguration(config, dm);
        pref.setlocale(this, locale);
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}
