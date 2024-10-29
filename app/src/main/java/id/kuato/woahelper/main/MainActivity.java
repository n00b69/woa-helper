package id.kuato.woahelper.main;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
    public ActivityMainBinding x;
    public DevicesBinding d;
    public SetPanelBinding k;
    public ToolboxBinding n;
    public ScriptsBinding z;
    public LangaugesBinding l;
    public String winpath;
    public int backable;
    String panel;
    String mounted;
    String finduefi;
    String device;
    String model;
    String win;
    String grouplink = "https://t.me/woahelperchat";
    String guidelink = "";
    String currentVersion = BuildConfig.VERSION_NAME;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    private double ramvalue;

    public static String extractNumberFromString(String source) {
        StringBuilder result = new StringBuilder(100);
        for (char ch : source.toCharArray()) {
            if ('0' <= ch && '9' >= ch) {
                result.append(ch);
            }
        }
        return result.toString();
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Network activeNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);

        return null != capabilities && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    private void copyAssets() {
        ShellUtils.fastCmd(String.format("rm %s/*", this.getFilesDir()));
        AssetManager assetManager = this.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException ignored) {
        }
        assert null != files;
        for (String filename : files) {
            @Nullable final InputStream in;
            @Nullable final OutputStream out;
            try {
                in = assetManager.open(filename);

                String outDir = String.valueOf(this.getFilesDir());

                File outFile = new File(outDir, filename);

                out = new FileOutputStream(outFile);
                this.copyFile(in, out);
                in.close();
                out.flush();
                out.close();
            } catch (IOException ignored) {
            }
        }
        ShellUtils.fastCmd("chmod 777 " + this.getFilesDir() + "/busybox");
        pref.setbusybox(this, this.getFilesDir() + "/busybox ");
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while (-1 != (read = in.read(buffer))) {
            out.write(buffer, 0, read);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Objects.equals(currentVersion, pref.getVersion(this))) {
            String[] files = {"tabletmode.vbs", "guacamole.fd", "hotdog.fd", "dbkp8150.cfg", "dbkp.hotdog.bin", "busybox", "sta.exe", "Switch to Android.lnk", "usbhostmode.exe", "ARMSoftware.url", "TestedSoftware.url", "WorksOnWoa.url", "RotationShortcut.lnk", "display.exe", "RemoveEdge.ps1", "autoflasher.lnk", "DefenderRemover.exe"};
            for (int i = 0; !files[i].isEmpty(); i++) {
                if (ShellUtils.fastCmd(String.format("ls %1$s |grep %2$s", getFilesDir(), files[i])).isEmpty()) {
                    copyAssets();
                    break;
                }
            }
        } else {
            copyAssets();
            pref.setVersion(this, currentVersion);
        }

        super.onCreate(savedInstanceState);


        //createNotificationChannel();
        final Dialog dialog = new Dialog(this);
        final Dialog languages = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        languages.setContentView(R.layout.langauges);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        languages.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        this.x = ActivityMainBinding.inflate(this.getLayoutInflater());
        this.setContentView(this.x.getRoot());
        this.backable = 0;
        this.d = DevicesBinding.inflate(this.getLayoutInflater());
        this.x.toolbarlayout.settings.setVisibility(View.VISIBLE);
        this.k = SetPanelBinding.inflate(this.getLayoutInflater());
        this.n = ToolboxBinding.inflate(this.getLayoutInflater());
        this.z = ScriptsBinding.inflate(this.getLayoutInflater());
        Drawable iconToolbar = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_launcher_foreground, null);
        this.setSupportActionBar(this.x.toolbarlayout.toolbar);
        this.x.toolbarlayout.toolbar.setTitle(this.getString(R.string.app_name));
        this.x.toolbarlayout.toolbar.setSubtitle("v" + this.currentVersion);
        this.x.toolbarlayout.toolbar.setNavigationIcon(iconToolbar);
        this.checkdevice();
        this.checkuefi();
        this.win = ShellUtils.fastCmd("realpath /dev/block/by-name/win");
        if (this.win.isEmpty()) this.win = ShellUtils.fastCmd("realpath /dev/block/by-name/mindows");
        this.winpath = (pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows");
        String mount_stat = ShellUtils.fastCmd("su -c mount | grep " + this.win);
        if (mount_stat.isEmpty()) this.mounted = getString(R.string.mountt);
        else this.mounted = getString(R.string.unmountt);
        if (pref.getMODEM(this)) this.n.cvDumpModem.setVisibility(View.GONE);
        this.x.tvMnt.setText(String.format(this.getString(R.string.mnt_title), this.mounted));
        this.x.tvBackup.setText(this.getString(R.string.backup_boot_title));
        this.x.tvBackupSub.setText(this.getString(R.string.backup_boot_subtitle));
        this.x.tvMntSubtitle.setText(this.getString(R.string.mnt_subtitle));
        this.n.tvModemSubtitle.setText(this.getString(R.string.dump_modem_subtitle));
        this.x.tvDate.setText(String.format(this.getString(R.string.last), pref.getDATE(this)));
        MaterialButton yesButton = dialog.findViewById(R.id.yes);
        MaterialButton noButton = dialog.findViewById(R.id.no);
        MaterialButton dismissButton = dialog.findViewById(R.id.dismiss);
        TextView messages = dialog.findViewById(R.id.messages);
        ImageView icons = dialog.findViewById(R.id.icon);
        ProgressBar bar = dialog.findViewById(R.id.progress);
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        Drawable androidlogo1 = ResourcesCompat.getDrawable(this.getResources(), R.drawable.androidlogo1, null);
        Drawable androidlogo2 = ResourcesCompat.getDrawable(this.getResources(), R.drawable.androidlogo2, null);
        Drawable atlasos = ResourcesCompat.getDrawable(this.getResources(), R.drawable.atlasos2, null);
        Drawable uefi = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_uefi, null);
        Drawable boot = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_disk, null);
        Drawable sensors = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_sensor, null);
        Drawable edge = ResourcesCompat.getDrawable(this.getResources(), R.drawable.edge2, null);
        Drawable download = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_download, null);
        Drawable modem = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_modem, null);
        Drawable mnt = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_mnt, null);
        Drawable icon = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_launcher_foreground, null);
        Drawable settings = ResourcesCompat.getDrawable(this.getResources(), R.drawable.settings, null);
        settings.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(this.getColor(R.color.colorPrimary), BlendModeCompat.SRC_IN));
        Drawable back = ResourcesCompat.getDrawable(this.getResources(), R.drawable.back_arrow, null);
        back.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(this.getColor(R.color.colorPrimary), BlendModeCompat.SRC_IN));




        this.d.toolbarlayout.back.setImageDrawable(back);
        this.x.toolbarlayout.settings.setImageDrawable(settings);

        String slot = ShellUtils.fastCmd("getprop ro.boot.slot_suffix");
        if (slot.isEmpty()) this.x.tvSlot.setVisibility(View.GONE);
        else this.x.tvSlot.setText(String.format(this.getString(R.string.slot), slot.substring(1, 2).toUpperCase()));

        TextView myTextView00 = this.findViewById(R.id.tv_date);
        myTextView00.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
        this.x.deviceName.setText(this.model + " (" + this.device + ")");

        TextView deviceNameView = this.findViewById(R.id.deviceName);
        TextView tvPanelView = this.findViewById(R.id.tv_panel);
        TextView tvRamValueView = this.findViewById(R.id.tv_ramvalue);
        TextView guideTextView = this.findViewById(R.id.guide_text);
        TextView groupTextView = this.findViewById(R.id.group_text);
        TextView tvSlotView = this.findViewById(R.id.tv_slot);
        TextView textView = this.findViewById(R.id.text);

        if (Objects.equals(this.device, "nabu")) {
			this.guidelink = "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/tree/main";
            this.grouplink = "https://t.me/nabuwoa";
            this.x.NabuImage.setVisibility(View.VISIBLE);
            this.x.DeviceImage.setVisibility(View.GONE);
            this.x.tvPanel.setVisibility(View.VISIBLE);
            this.n.cvDumpModem.setVisibility(View.GONE);
			this.n.cvDbkp.setVisibility(View.GONE);

            deviceNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);

            tvPanelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);

            tvRamValueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);

            guideTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);

            groupTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);

            tvSlotView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);

            int orientation = this.getResources().getConfiguration().orientation;
            if (Configuration.ORIENTATION_PORTRAIT == orientation) {
                this.x.app.setOrientation(LinearLayout.VERTICAL);
                this.x.top.setOrientation(LinearLayout.HORIZONTAL);
            } else {
                this.x.app.setOrientation(LinearLayout.HORIZONTAL);
                this.x.top.setOrientation(LinearLayout.VERTICAL);
            }
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE1);

            deviceNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);

            tvPanelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);

            tvRamValueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);

            guideTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);

            groupTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);

            tvSlotView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);

            this.n.cvDbkp.setVisibility(View.GONE);

            switch (this.device) {
                case "nabu" -> {
                    this.guidelink = "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/tree/main";
                    this.grouplink = "https://t.me/nabuwoa";
                    this.x.NabuImage.setVisibility(View.VISIBLE);
                    this.x.DeviceImage.setVisibility(View.GONE);
                    this.x.tvPanel.setVisibility(View.VISIBLE);
                    this.n.cvDumpModem.setVisibility(View.GONE);
					this.n.cvDbkp.setVisibility(View.GONE);
                }
                case "cepheus" -> {
                    this.guidelink = "https://github.com/woacepheus/Port-Windows-11-Xiaomi-Mi-9";
                    this.grouplink = "http://t.me/woacepheus";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.cepheus, null));
                    this.x.tvPanel.setVisibility(View.VISIBLE);
                }
                case "raphael", "raphaelin", "raphaels" -> {
                    this.guidelink = "https://github.com/woa-raphael/woa-raphael";
                    this.grouplink = "https://t.me/woaraphael";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.raphael, null));
                    this.x.tvPanel.setVisibility(View.VISIBLE);
                }
                case "mh2lm", "mh2plus", "mh2plus_lao_com", "mh2lm_lao_com" -> {
                    this.guidelink = "https://github.com/n00b69/woa-mh2lm";
                    this.grouplink = "https://t.me/woahelperchat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.mh2lm, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "mh2lm5g", "mh2lm5g_lao_com" -> {
                    this.guidelink = "https://github.com/n00b69/woa-mh2lm5g";
                    this.grouplink = "https://t.me/woahelperchat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.mh2lm, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "flashlmdd", "flashlmdd_lao_com" -> {
                    this.guidelink = "https://github.com/n00b69/woa-flashlmdd";
                    this.grouplink = "https://t.me/woahelperchat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.flashlmdd, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "alphalm", "alphaplus", "alphalm_lao_com", "alphaplus_lao_com" -> {
                    this.guidelink = "https://github.com/n00b69/woa-alphaplus";
                    this.grouplink = "https://t.me/woahelperchat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.alphaplus, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "betaplus", "betalm", "betaplus_lao_com", "betalm_lao_com" -> {
                    this.guidelink = "https://github.com/n00b69/woa-betalm";
                    this.grouplink = "https://t.me/woahelperchat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.betalm, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "bhima", "vayu" -> {
                    this.guidelink = "https://github.com/woa-vayu/POCOX3Pro-Guides";
                    this.grouplink = "https://t.me/woahelperchat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.vayu, null));
                    this.x.tvPanel.setVisibility(View.VISIBLE);
                }
                case "beryllium" -> {
                    this.guidelink = "https://github.com/n00b69/woa-beryllium";
                    this.grouplink = "https://t.me/WinOnF1";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.beryllium, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "chiron" -> {
                    this.guidelink = "https://renegade-project.tech/";
                    this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.chiron, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "dipper" -> {
                    this.guidelink = "https://github.com/n00b69/woa-dipper";
                    this.grouplink = "https://t.me/woadipper";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.dipper, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "equuleus" -> {
                    this.guidelink = "https://github.com/n00b69/woa-equuleus";
                    this.grouplink = "https://t.me/woaequuleus";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.equuleus, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "lisa" -> {
                    this.guidelink = "https://github.com/ETCHDEV/Port-Windows-11-Xiaomi-11-Lite-NE";
                    this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.lisa, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "curtana", "curtana2", "curtana_india", "curtana_cn", "curtanacn", "durandal", "durandal_india", "excalibur", "excalibur2", "excalibur_india", "gram", "joyeuse", "miatoll" -> {
                    this.guidelink = "https://github.com/woa-miatoll/Port-Windows-11-Redmi-Note-9-Pro";
                    this.grouplink = "http://t.me/woamiatoll";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.miatoll, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                    this.x.tvPanel.setVisibility(View.VISIBLE);
                }
                case "polaris" -> {
                    this.guidelink = "https://github.com/n00b69/woa-polaris";
                    this.grouplink = "https://t.me/WinOnMIX2S";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.polaris, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                    this.x.tvPanel.setVisibility(View.VISIBLE);
                }
                case "venus" -> {
                    this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    this.grouplink = "https://t.me/woahelperchat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.venus, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "xpeng" -> {
                    this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    this.grouplink = "https://t.me/woahelperchat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.xpeng, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "alioth" -> {
                    this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    this.grouplink = "https://t.me/woahelperchat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.alioth, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "pipa" -> {
                    this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    this.grouplink = "https://t.me/pad6_pipa";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.pipa, null));
                    this.x.tvPanel.setVisibility(View.VISIBLE);
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "perseus" -> {
                    this.guidelink = "https://github.com/n00b69/woa-perseus";
                    this.grouplink = "https://t.me/woaperseus";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.perseus, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "Pong", "pong" -> {
                    this.guidelink = "https://github.com/Govro150/woa-pong";
                    this.grouplink = "https://t.me/woa_pong";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.pong, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "surya" -> {
                    this.guidelink = "https://github.com/SebastianZSXS/Poco-X3-NFC-WindowsARM";
                    this.grouplink = "https://t.me/windows_on_pocox3_nfc";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.vayu, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "cheeseburger" -> {
                    this.guidelink = "https://renegade-project.tech/";
                    this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.cheeseburger, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "dumpling" -> {
                    this.guidelink = "https://renegade-project.tech/";
                    this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.dumpling, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "G973F", "SM-G973F", "beyond1lte", "beyond1qlte", "G973U", "G973U1", "SM-G973U", "SM-G973U1", "G9730", "SM-G9730", "G973N", "SM-G973N", "G973X", "SM-G973X", "G973C", "SM-G973C", "SCV41", "SM-SC41", "beyond1" -> {
                    this.guidelink = "https://github.com/sonic011gamer/Mu-Samsung";
                    this.grouplink = "https://t.me/woahelperchat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.beyond1, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "a52sxq" -> {
                    this.guidelink = "https://github.com/arminask/Port-Windows-11-Galaxy-A52s-5G";
                    this.grouplink = "https://t.me/a52sxq_uefi";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.a52sxq, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "judyln", "judyp", "judypn" -> {
                    this.guidelink = "https://github.com/n00b69/woa-everything";
                    this.grouplink = "https://t.me/lgedevices";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.unknown, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "joan" -> {
                    this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    this.grouplink = "https://t.me/lgedevices";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.unknown, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "andromeda" -> {
                    this.guidelink = "https://woa-msmnile.github.io/";
                    this.grouplink = "https://t.me/woa_msmnile_issues";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.unknown, null));
					this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "OnePlus6", "fajita" -> {
                    this.guidelink = "https://github.com/n00b69/woa-op6";
                    this.grouplink = "https://t.me/WinOnOP6";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.fajita, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "OnePlus6T", "enchilada" -> {
                    this.guidelink = "https://github.com/n00b69/woa-op6";
                    this.grouplink = "https://t.me/WinOnOP6";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.enchilada, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "hotdog", "OnePlus7TPro", "OnePlus7TPro4G" -> {
                    this.guidelink = "https://github.com/n00b69/woa-op7";
                    this.grouplink = "https://t.me/onepluswoachat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.hotdog, null));
                    this.n.cvDbkp.setVisibility(View.VISIBLE);
                    this.n.cvFlashUefi.setVisibility(View.GONE);
                }
                case "guacamole", "OnePlus7Pro", "OnePlus7Pro4G" -> {
                    this.guidelink = "https://github.com/n00b69/woa-op7";
                    this.grouplink = "https://t.me/onepluswoachat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.guacamole, null));
                    this.n.cvDbkp.setVisibility(View.VISIBLE);
                    this.n.cvFlashUefi.setVisibility(View.GONE);
                }
                case "guacamoleb", "hotdogb", "OnePlus7T", "OnePlus7" -> {
                    this.guidelink = "https://woa-msmnile.github.io/";
                    this.grouplink = "https://t.me/onepluswoachat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.unknown, null));
                }
                case "OnePlus7TPro5G", "OnePlus7TProNR" -> {
                    this.guidelink = "https://woa-msmnile.github.io/";
                    this.grouplink = "https://t.me/onepluswoachat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.hotdog, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "hotdogg", "OP7ProNRSpr" -> {
                    this.guidelink = "https://woa-msmnile.github.io/";
                    this.grouplink = "https://t.me/onepluswoachat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.guacamole, null));
                }
                case "sagit" -> {
                    this.guidelink = "https://renegade-project.tech/";
                    this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.unknown, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "winnerx", "winner" -> {
                    this.guidelink = "https://github.com/n00b69/woa-winner";
                    this.grouplink = "https://t.me/woa_msmnile_issues";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.winner, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "q2q" -> {
                    this.guidelink = "https://woa-msmnile.github.io/";
                    this.grouplink = "https://t.me/woa_msmnile_issues";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.q2q, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "RMX2061" -> {
                    this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    this.grouplink = "https://t.me/realme6PROwindowsARM64";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.rmx2061, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "RMX2170" -> {
                    this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    this.grouplink = "https://t.me/realme6PROwindowsARM64";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.rmx2170, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "cmi" -> {
                    this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    this.grouplink = "https://t.me/dumanthecat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.cmi, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "houji" -> {
                    this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
                    this.grouplink = "https://t.me/dumanthecat";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.houji, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "marble" -> {
                    this.guidelink = "https://github.com/Xhdsos/woa-marble";
                    this.grouplink = "https://t.me/woa_marble";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.marble, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                case "davinci" -> {
                    this.guidelink = "https://github.com/zxcwsurx/woa-davinci";
                    this.grouplink = "https://t.me/woa_davinci";
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.raphael, null));
                    this.n.cvDumpModem.setVisibility(View.GONE);
                }
                default -> {
                    this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.unknown, null));
                    this.x.deviceName.setText(this.device);
                    this.guidelink = "https://renegade-project.tech/";
                    this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
                }
            }
        }
        this.x.tvRamvalue.setText(String.format(this.getString(R.string.ramvalue), this.ramvalue));
        this.x.tvPanel.setText(String.format(this.getString(R.string.paneltype), this.panel));
        this.x.cvGuide.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(guidelink));
            this.startActivity(i);
        });

        this.x.cvGroup.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(grouplink));
            this.startActivity(i);
        });

        this.x.cvQuickBoot.setOnClickListener(v -> {
            this.ShowBlur();
            noButton.setText(this.getString(R.string.no));
            yesButton.setText(this.getString(R.string.yes));
            yesButton.setVisibility(View.VISIBLE);
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(icon);
            messages.setText(this.getString(R.string.quickboot_question));
            yesButton.setOnClickListener(v27 -> {
                yesButton.setVisibility(View.GONE);
                noButton.setVisibility(View.GONE);
                messages.setText(this.getString(R.string.please_wait));
                new Handler().postDelayed(() -> {
                    try {
                        this.mount();
                        String[] dumped = {"bhima", "cepheus", "guacamole", "guacamoleb", "hotdog", "hotdogb", "OnePlus7", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7T", "OnePlus7TPro", "OnePlus7TPro4G", "raphael", "raphaelin", "raphaels", "vayu"};
                        if (Arrays.asList(dumped).contains(this.device) || !(pref.getMODEM(this))) {
                            this.dump();
                        }
                        String found = ShellUtils.fastCmd("ls " + (pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows") + " | grep boot.img");
                        if (pref.getMountLocation(this)) {
                            if (pref.getBACKUP(this) || (!pref.getAUTO(this) && found.isEmpty())) {
                                ShellUtils.fastCmd(this.getString(R.string.backup2));
                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
                                String currentDateAndTime = sdf.format(new Date());
                                pref.setDATE(this, currentDateAndTime);
                            }
                        } else {
                            if (pref.getBACKUP(this) || (!pref.getAUTO(this) && found.isEmpty())) {
                                ShellUtils.fastCmd(this.getString(R.string.backup1));
                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
                                String currentDateAndTime = sdf.format(new Date());
                                pref.setDATE(this, currentDateAndTime);
                            }
                        }
                        found = ShellUtils.fastCmd("find /sdcard | grep boot.img");
                        if (pref.getBACKUP_A(this) || (!pref.getAUTO_A(this) && found.isEmpty())) {
                            ShellUtils.fastCmd(this.getString(R.string.backup));
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
                            String currentDateAndTime = sdf.format(new Date());
                            pref.setDATE(this, currentDateAndTime);
                        }

                        this.flash(this.finduefi);
                        ShellUtils.fastCmd("su -c svc power reboot");
                        messages.setText(this.getString(R.string.wrong));
                        dismissButton.setVisibility(View.VISIBLE);
                    } catch (Exception error) {
                        error.printStackTrace();
                    }
                }, 500);
            });
            dismissButton.setText(this.getString(R.string.dismiss));
            dismissButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.HideBlur();
                    dialog.dismiss();
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.HideBlur();
                    dialog.dismiss();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

        this.x.cvMnt.setOnClickListener(v -> {
            noButton.setText(this.getString(R.string.no));
            noButton.setVisibility(View.VISIBLE);
            yesButton.setText(this.getString(R.string.yes));
            dismissButton.setText(this.getString(R.string.dismiss));
            yesButton.setVisibility(View.VISIBLE);
            dismissButton.setVisibility(View.GONE);
            this.ShowBlur();
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(mnt);
            String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + this.win);
            if (mnt_stat.isEmpty()) messages.setText(String.format(this.getString(R.string.mount_question), this.winpath));
            else messages.setText(this.getString(R.string.unmount_question));

            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    yesButton.setVisibility(View.GONE);
                    noButton.setVisibility(View.GONE);
                    messages.setText(MainActivity.this.getString(R.string.please_wait));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d("debug", MainActivity.this.winpath);
                                String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + MainActivity.this.win);
                                if (mnt_stat.isEmpty()) {
                                    MainActivity.this.mount();
                                    mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + MainActivity.this.win);
                                    if (mnt_stat.isEmpty()) {
                                        noButton.setVisibility(View.GONE);
                                        yesButton.setText(MainActivity.this.getString(R.string.chat));
                                        dismissButton.setText(MainActivity.this.getString(R.string.cancel));
                                        yesButton.setVisibility(View.VISIBLE);
                                        dismissButton.setVisibility(View.VISIBLE);
                                        icons.setVisibility(View.GONE);
                                        MainActivity.this.ShowBlur();
                                        messages.setText(getString(R.string.mountfail));
                                        dismissButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                MainActivity.this.HideBlur();
                                                dialog.dismiss();
                                                icons.setVisibility(View.VISIBLE);
                                            }
                                        });
                                        yesButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent i = new Intent(Intent.ACTION_VIEW);
                                                i.setData(Uri.parse("https://t.me/woahelperchat"));
                                                MainActivity.this.startActivity(i);
                                            }
                                        });

                                        dialog.setCancelable(false);
                                        dialog.show();
                                    } else {
                                        Log.d("debug", pref.getMountLocation(MainActivity.this) ? "/mnt/Windows" : "/mnt/sdcard/Windows");
                                        messages.setText(MainActivity.this.getString(R.string.mounted) + "\n" + MainActivity.this.winpath);
                                    }
                                } else {
                                    MainActivity.this.unmount();
                                    messages.setText(MainActivity.this.getString(R.string.unmounted));
                                }
                                dismissButton.setText(MainActivity.this.getString(R.string.dismiss));
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
                    MainActivity.this.HideBlur();
                    dialog.dismiss();
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.HideBlur();
                    dialog.dismiss();
                }
            });
            dialog.setCancelable(false);
            dialog.show();

        });

        this.n.cvDumpModem.setOnClickListener(v -> {
            this.ShowBlur();
            noButton.setText(this.getString(R.string.no));
            dismissButton.setText(this.getString(R.string.dismiss));
            yesButton.setText(this.getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            yesButton.setVisibility(View.VISIBLE);
            noButton.setVisibility(View.VISIBLE);
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(modem);
            messages.setText(this.getString(R.string.dump_modem_question));
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    noButton.setVisibility(View.GONE);
                    yesButton.setVisibility(View.GONE);
                    messages.setText(MainActivity.this.getString(R.string.please_wait));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + MainActivity.this.win);
                            if (mnt_stat.isEmpty()) {
                                MainActivity.this.mount();
                                MainActivity.this.dump();
                                MainActivity.this.unmount();
                            } else MainActivity.this.dump();
                            messages.setText(MainActivity.this.getString(R.string.lte));
                            dismissButton.setVisibility(View.VISIBLE);
                        }
                    }, 500);
                }
            });
            dismissButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.HideBlur();
                    dialog.dismiss();
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.HideBlur();
                    dialog.dismiss();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

        this.n.cvScripts.setOnClickListener(v -> {
            this.n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
            this.setContentView(this.z.getRoot());
            this.z.scriptstab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
            this.backable = 1;
            this.z.toolbarlayout.settings.setVisibility(View.GONE);
            this.z.toolbarlayout.toolbar.setTitle(this.getString(R.string.script_title));
            this.z.toolbarlayout.toolbar.setNavigationIcon(iconToolbar);
        });


        this.x.cvToolbox.setOnClickListener(v -> {
            this.x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
            this.setContentView(this.n.getRoot());
            this.n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
            this.backable = 1;
            this.n.toolbarlayout.settings.setVisibility(View.GONE);
            this.n.toolbarlayout.toolbar.setTitle(this.getString(R.string.toolbox_title));
            this.n.toolbarlayout.toolbar.setNavigationIcon(iconToolbar);
        });

        this.n.cvSta.setOnClickListener(v -> {
            this.ShowBlur();
            noButton.setText(this.getString(R.string.no));
            yesButton.setText(this.getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(this.getString(R.string.sta_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(androidlogo1);
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.HideBlur();
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
                        MainActivity.this.mount();
                        // ShellUtils.fastCmd("su -c 'if [ -e /dev/block/by-name/boot_a ] && [ -e /dev/block/by-name/boot_b ]; then boot_a=$(basename $(readlink -f /dev/block/by-name/boot_a)); boot_b=$(basename $(readlink -f /dev/block/by-name/boot_b)); printf \"%s\n%s\n%s\n%s\n\" \"$boot_a\" \"C:\\boot.img\" \"$boot_b\" \"C:\\boot.img\" > /sdcard/sta.conf; else boot=$(basename $(readlink -f /dev/block/by-name/boot)); printf \"%s\n%s\n\" \"$boot\" \"C:\\boot.img\" > /sdcard/sta.conf; fi'");
                        ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sta.exe /sdcard/sta.exe");
                        String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + MainActivity.this.win);
                        if (mnt_stat.isEmpty()) {
                            noButton.setVisibility(View.GONE);
                            yesButton.setText(MainActivity.this.getString(R.string.chat));
                            dismissButton.setText(MainActivity.this.getString(R.string.cancel));
                            yesButton.setVisibility(View.VISIBLE);
                            dismissButton.setVisibility(View.VISIBLE);
                            icons.setVisibility(View.GONE);
                            MainActivity.this.ShowBlur();
                            messages.setText(getString(R.string.mountfail));
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MainActivity.this.HideBlur();
                                    icons.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                }
                            });
                            yesButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse("https://t.me/woahelperchat"));
                                    MainActivity.this.startActivity(i);
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.show();
                        } else {
                            ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/sta || true ");
                            // ShellUtils.fastCmd("cp /sdcard/sta.conf " + MainActivity.this.winpath + "/sta");
                            ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sta.exe " + MainActivity.this.winpath + "/sta/sta.exe");
                            ShellUtils.fastCmd("cp '" + MainActivity.this.getFilesDir() + "/Switch to Android.lnk' " + MainActivity.this.winpath + "/Users/Public/Desktop");
                            messages.setText(MainActivity.this.getString(R.string.done));
                            dismissButton.setText(MainActivity.this.getString(R.string.dismiss));
                            dismissButton.setVisibility(View.VISIBLE);
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MainActivity.this.HideBlur();
                                    dialog.dismiss();
                                }
                            });
                        }
                        dismissButton.setText(MainActivity.this.getString(R.string.dismiss));
                        dismissButton.setVisibility(View.VISIBLE);
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MainActivity.this.HideBlur();
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
        });

        this.n.cvAtlasos.setOnClickListener(v -> {
            this.ShowBlur();
            yesButton.setText("AtlasOS");
            noButton.setText("ReviOS");
            dismissButton.setText(this.getString(R.string.dismiss));
            dismissButton.setVisibility(View.VISIBLE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(this.getString(R.string.atlasos_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(atlasos);
            if (!isNetworkConnected(this)) {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.VISIBLE);
                dismissButton.setText(this.getString(R.string.dismiss));
                messages.setText(this.getString(R.string.internet));
            }
            dismissButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.HideBlur();
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
                    messages.setText(MainActivity.this.getString(R.string.please_wait));
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
                            bar.setProgress((int) (bar.getMax() * 0.00), true);
                            ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/modified-playbooks/releases/download/ReviOS/ReviPlaybook.apbx -O /sdcard/ReviPlaybook.apbx");
                            bar.setProgress((int) (bar.getMax() * 0.5), true);
                            ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://download.ameliorated.io/AME%20Wizard%20Beta.zip -O /sdcard/AMEWizardBeta.zip");
                            bar.setProgress((int) (bar.getMax() * 0.8), true);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.this.mount();
                                    String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + MainActivity.this.win);
                                    if (mnt_stat.isEmpty()) {
                                        icons.setVisibility(View.GONE);
                                        yesButton.setVisibility(View.VISIBLE);
                                        messages.setText(MainActivity.this.getString(R.string.mountfail) + "\nFiles downloaded in internal storage");
                                    } else {
                                        icons.setImageDrawable(atlasos);
                                        ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/Toolbox || true ");
                                        ShellUtils.fastCmd("cp /sdcard/ReviPlaybook.apbx " + MainActivity.this.winpath + "/Toolbox/ReviPlaybook.apbx");
                                        ShellUtils.fastCmd("cp /sdcard/AMEWizardBeta.zip " + MainActivity.this.winpath + "/Toolbox");
                                        ShellUtils.fastCmd("su -mm -c rm /sdcard/ReviPlaybook.apbx");
                                        ShellUtils.fastCmd("su -mm -c rm /sdcard/AMEWizardBeta.zip");
                                        bar.setProgress(bar.getMax(), true);
                                        messages.setText(MainActivity.this.getString(R.string.done));
                                    }
                                    dismissButton.setVisibility(View.VISIBLE);
                                    bar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).start();
                    try {
                        MainActivity.this.mount();
                        String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + MainActivity.this.win);
                        if (mnt_stat.isEmpty()) {
                            noButton.setVisibility(View.GONE);
                            yesButton.setText(MainActivity.this.getString(R.string.chat));
                            dismissButton.setText(MainActivity.this.getString(R.string.dismiss));

                            MainActivity.this.ShowBlur();
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MainActivity.this.HideBlur();
                                    icons.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                }
                            });
                            yesButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse("https://t.me/woahelperchat"));
                                    MainActivity.this.startActivity(i);
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.show();
                        } else {
                            dismissButton.setText(MainActivity.this.getString(R.string.dismiss));
                            //dismissButton.setVisibility(View.VISIBLE);
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MainActivity.this.HideBlur();
                                    icons.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                }
                            });
                        }
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MainActivity.this.HideBlur();
                                icons.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        });
                    } catch (Exception error) {
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
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    bar.setProgress((int) (bar.getMax() * 0.00), true);
                    ShellUtils.fastCmd(this.getFilesDir() + "/busybox wget https://github.com/n00b69/modified-playbooks/releases/download/AtlasOS/AtlasPlaybook.apbx -O /sdcard/AtlasPlaybook.apbx");
                    bar.setProgress((int) (bar.getMax() * 0.5), true);
                    ShellUtils.fastCmd(this.getFilesDir() + "/busybox wget https://download.ameliorated.io/AME%20Wizard%20Beta.zip -O /sdcard/AMEWizardBeta.zip");
                    bar.setProgress((int) (bar.getMax() * 0.8), true);
                    this.runOnUiThread(() -> {
                        this.mount();
                        String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + this.win);
                        Log.d("path", mnt_stat);
                        if (mnt_stat.isEmpty()) {
                            yesButton.setVisibility(View.VISIBLE);
                            icons.setVisibility(View.GONE);
                            messages.setText(this.getString(R.string.mountfail) + "\nFiles downloaded in internal storage");
                        } else {
                            icons.setImageDrawable(atlasos);
                            ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox || true ");
                            ShellUtils.fastCmd("cp /sdcard/AtlasPlaybook.apbx " + this.winpath + "/Toolbox/AtlasPlaybook.apbx");
                            ShellUtils.fastCmd("cp /sdcard/AMEWizardBeta.zip " + this.winpath + "/Toolbox");
                            ShellUtils.fastCmd("su -mm -c rm /sdcard/AtlasPlaybook.apbx");
                            ShellUtils.fastCmd("su -mm -c rm /sdcard/AMEWizardBeta.zip");
                            bar.setProgress(bar.getMax(), true);
                            messages.setText(this.getString(R.string.done));
                        }
                        dismissButton.setVisibility(View.VISIBLE);
                        bar.setVisibility(View.GONE);
                    });
                }).start();
                messages.setText(this.getString(R.string.please_wait));
                try {
                    this.mount();
                    String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + this.win);
                    if (mnt_stat.isEmpty()) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setText(this.getString(R.string.chat));
                        dismissButton.setText(this.getString(R.string.dismiss));
                        this.ShowBlur();
                        dismissButton.setOnClickListener(v24 -> {
                            this.HideBlur();
                            icons.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        });
                        yesButton.setOnClickListener(v25 -> {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("https://t.me/woahelperchat"));
                            this.startActivity(i);
                        });
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        dismissButton.setText(this.getString(R.string.dismiss));
                        dismissButton.setOnClickListener(v23 -> {
                            this.HideBlur();
                            icons.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        });
                    }
                    dismissButton.setText(this.getString(R.string.dismiss));
                    dismissButton.setOnClickListener(v22 -> {
                        this.HideBlur();
                        icons.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    });
                } catch (Exception error) {
                    error.printStackTrace();
                }
            });

            dialog.setCancelable(false);
            dialog.show();
        });

        this.z.cvUsbhost.setOnClickListener(v -> {
            this.ShowBlur();
            noButton.setText(this.getString(R.string.no));
            yesButton.setText(this.getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(this.getString(R.string.usbhost_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(mnt);
            noButton.setOnClickListener(v21 -> {
                this.HideBlur();
                dialog.dismiss();
            });
            yesButton.setOnClickListener(v20 -> {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.GONE);
                try {
                    this.mount();
                    String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + this.win);

                    ShellUtils.fastCmd("cp " + this.getFilesDir() + "/usbhostmode.exe /sdcard");
                    if (mnt_stat.isEmpty()) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setText(this.getString(R.string.chat));
                        dismissButton.setText(this.getString(R.string.cancel));
                        yesButton.setVisibility(View.VISIBLE);
                        dismissButton.setVisibility(View.VISIBLE);
                        icons.setVisibility(View.GONE);
                        this.ShowBlur();
                        messages.setText(this.getString(R.string.mountfail));
                        dismissButton.setOnClickListener(v19 -> {
                            this.HideBlur();
                            icons.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        });
                        yesButton.setOnClickListener(v18 -> {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("https://t.me/woahelperchat"));
                            this.startActivity(i);
                        });
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox || true ");
                        ShellUtils.fastCmd("cp /sdcard/usbhostmode.exe " + this.winpath + "/Toolbox");
                        ShellUtils.fastCmd("rm /sdcard/usbhostmode.exe");
                        messages.setText(this.getString(R.string.done));
                        dismissButton.setText(this.getString(R.string.dismiss));
                        dismissButton.setVisibility(View.VISIBLE);
                        dismissButton.setOnClickListener(v17 -> {
                            this.HideBlur();
                            dialog.dismiss();
                        });
                    }
                    dismissButton.setText(this.getString(R.string.dismiss));
                    dismissButton.setVisibility(View.VISIBLE);
                    dismissButton.setOnClickListener(v16 -> {
                        this.HideBlur();
                        dialog.dismiss();
                    });
                } catch (Exception error) {
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
//								 messages.setText(getString(R.string.mountfail));
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

        this.z.cvSetup.setOnClickListener(v -> {
            this.ShowBlur();
            noButton.setText(this.getString(R.string.no));
            yesButton.setText(this.getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(this.getString(R.string.setup_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(mnt);
            if (!MainActivity.isNetworkConnected(this)) {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.VISIBLE);
                dismissButton.setText(this.getString(R.string.dismiss));
                messages.setText(this.getString(R.string.internet));
            }
            dismissButton.setOnClickListener(v1 -> {
                this.HideBlur();
                dialog.dismiss();
            });
            noButton.setOnClickListener(v2 -> {
                this.HideBlur();
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
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    String[] files = {
                            "2005vcredist_x64.EXE",
                            "2005vcredist_x86.EXE",
                            "2008vcredist_x64.exe",
                            "2008vcredist_x86.exe",
                            "2010vcredist_x64.exe",
                            "2010vcredist_x86.exe",
                            "2012vcredist_x64.exe",
                            "2012vcredist_x86.exe",
                            "2013vcredist_x64.exe",
                            "2013vcredist_x86.exe",
                            "2015VC_redist.x64.exe",
                            "2015VC_redist.x86.exe",
                            "2022VC_redist.arm64.exe",
                            "dxwebsetup.exe",
                            "oalinst.exe",
                            "PhysX-9.13.0604-SystemSoftware-Legacy.msi",
                            "PhysX-9.19.0218-SystemSoftware.exe",
                            "xnafx40_redist.msi"
                    };

                    bar.setProgress((int) (bar.getMax() * 0.00), true);

                    // Base URL for downloads
                    String baseUrl = "https://github.com/n00b69/woasetup/releases/download/Installers/";

                    // Iterate through the files and download each one
                    for (int i = 0; i < files.length; i++) {
                        String fileName = files[i];
                        String downloadUrl = baseUrl + fileName;
                        String localPath = "/sdcard/" + fileName;

                        // Execute the download command
                        ShellUtils.fastCmd(this.getFilesDir() + "/busybox wget " + downloadUrl + " -O " + localPath);

                        // Update the progress bar
                        bar.setProgress((int) (bar.getMax() * ((i + 1) * 0.05)), true);
                    }
                    this.runOnUiThread(() -> {
                        this.mount();
                        String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + this.win);
                        Log.d("path", mnt_stat);
                        if (mnt_stat.isEmpty()) {
                            yesButton.setVisibility(View.VISIBLE);
                            icons.setVisibility(View.GONE);
                            messages.setText(this.getString(R.string.mountfail) + "\nFiles downloaded in internal storage");
                        } else {
                            icons.setImageDrawable(mnt);


                            // Copy files to the toolbox
                            ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox || true ");
                            for (String file : files) {
                                ShellUtils.fastCmd("cp /sdcard/" + file + " " + this.winpath + "/Toolbox/");
                            }

                            // Remove files from the source directory
                            for (String file : files) {
                                ShellUtils.fastCmd("rm /sdcard/" + file);
                            }
                            bar.setProgress(bar.getMax(), true);
                            messages.setText(this.getString(R.string.done));
                        }
                        dismissButton.setVisibility(View.VISIBLE);
                        bar.setVisibility(View.GONE);
                    });
                }).start();
                messages.setText(this.getString(R.string.please_wait));
                try {
                    this.mount();
                    String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + this.win);
                    if (mnt_stat.isEmpty()) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setText(this.getString(R.string.chat));
                        dismissButton.setText(this.getString(R.string.dismiss));
                        this.ShowBlur();
                        dismissButton.setOnClickListener(v4 -> {
                            this.HideBlur();
                            icons.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        });
                        yesButton.setOnClickListener(v5 -> {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("https://t.me/woahelperchat"));
                            this.startActivity(i);
                        });
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        dismissButton.setText(this.getString(R.string.dismiss));
                        dismissButton.setOnClickListener(v6 -> {
                            this.HideBlur();
                            icons.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        });
                    }
                    dismissButton.setText(this.getString(R.string.dismiss));
                    dismissButton.setOnClickListener(v7 -> {
                        this.HideBlur();
                        icons.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    });
                } catch (Exception error) {
                    error.printStackTrace();
                }
            });

            dialog.setCancelable(false);
            dialog.show();
        });

        this.z.cvDefender.setOnClickListener(v -> {
            this.ShowBlur();
            noButton.setText(this.getString(R.string.no));
            yesButton.setText(this.getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(this.getString(R.string.defender_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(edge);
            noButton.setOnClickListener(v8 -> {
                this.HideBlur();
                dialog.dismiss();
            });
            yesButton.setOnClickListener(v9 -> {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.GONE);
                try {
                    this.mount();
                    String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + this.win);
                    ShellUtils.fastCmd("cp " + this.getFilesDir() + "/DefenderRemover.exe /sdcard");
					ShellUtils.fastCmd("cp " + this.getFilesDir() + "/RemoveEdge.ps1 /sdcard");
                    if (mnt_stat.isEmpty()) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setText(this.getString(R.string.chat));
                        dismissButton.setText(this.getString(R.string.cancel));
                        yesButton.setVisibility(View.VISIBLE);
                        dismissButton.setVisibility(View.VISIBLE);
                        icons.setVisibility(View.GONE);
                        this.ShowBlur();
                        messages.setText(this.getString(R.string.mountfail));
                        dismissButton.setOnClickListener(v10 -> {
                            this.HideBlur();
                            icons.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        });
                        yesButton.setOnClickListener(v11 -> {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("https://t.me/woahelperchat"));
                            this.startActivity(i);
                        });
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox || true ");
                        ShellUtils.fastCmd("cp /sdcard/DefenderRemover.exe " + this.winpath + "/Toolbox");
                        ShellUtils.fastCmd("rm /sdcard/DefenderRemover.exe");
						ShellUtils.fastCmd("cp /sdcard/RemoveEdge.ps1 " + this.winpath + "/Toolbox");
                        ShellUtils.fastCmd("rm /sdcard/RemoveEdge.ps1");
                        messages.setText(this.getString(R.string.done));
                        dismissButton.setText(this.getString(R.string.dismiss));
                        dismissButton.setVisibility(View.VISIBLE);
                        dismissButton.setOnClickListener(v12 -> {
                            this.HideBlur();
                            dialog.dismiss();
                        });
                    }
                    dismissButton.setText(this.getString(R.string.dismiss));
                    dismissButton.setVisibility(View.VISIBLE);
                    dismissButton.setOnClickListener(v13 -> {
                        this.HideBlur();
                        dialog.dismiss();
                    });
                } catch (Exception error) {
                    error.printStackTrace();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });


        this.z.cvRotation.setOnClickListener(v -> {
            this.ShowBlur();
            noButton.setText(this.getString(R.string.no));
            yesButton.setText(this.getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(this.getString(R.string.rotation_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(boot);
            noButton.setOnClickListener(v14 -> {
                this.HideBlur();
                dialog.dismiss();
            });
            yesButton.setOnClickListener(v15 -> {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.GONE);
                try {
                    this.mount();
                    String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + this.win);
                    ShellUtils.fastCmd("cp " + this.getFilesDir() + "/display.exe /sdcard/");
                    ShellUtils.fastCmd("cp " + this.getFilesDir() + "/RotationShortcut.lnk /sdcard/");
                    if (mnt_stat.isEmpty()) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setText(this.getString(R.string.chat));
                        dismissButton.setText(this.getString(R.string.cancel));
                        yesButton.setVisibility(View.VISIBLE);
                        dismissButton.setVisibility(View.VISIBLE);
                        icons.setVisibility(View.GONE);
                        this.ShowBlur();
                        messages.setText(this.getString(R.string.mountfail));
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v15) {
                                MainActivity.this.HideBlur();
                                icons.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        });
                        yesButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v15) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse("https://t.me/woahelperchat"));
                                MainActivity.this.startActivity(i);
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox || true ");
                        ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox/Rotation || true ");
                        ShellUtils.fastCmd("cp /sdcard/display.exe " + this.winpath + "/Toolbox/Rotation");
                        ShellUtils.fastCmd("cp /sdcard/RotationShortcut.lnk " + this.winpath + "/Toolbox/Rotation");
                        ShellUtils.fastCmd("rm /sdcard/display.exe");
                        ShellUtils.fastCmd("rm /sdcard/RotationShortcut.lnk");
                        messages.setText(this.getString(R.string.done));
                        dismissButton.setText(this.getString(R.string.dismiss));
                        dismissButton.setVisibility(View.VISIBLE);
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v15) {
                                MainActivity.this.HideBlur();
                                dialog.dismiss();
                            }
                        });
                    }
                    dismissButton.setText(this.getString(R.string.dismiss));
                    dismissButton.setVisibility(View.VISIBLE);
                    dismissButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v15) {
                            MainActivity.this.HideBlur();
                            dialog.dismiss();
                        }
                    });
                } catch (Exception error) {
                    error.printStackTrace();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });
		
		this.z.cvTablet.setOnClickListener(v -> {
            this.ShowBlur();
            noButton.setText(this.getString(R.string.no));
            yesButton.setText(this.getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(this.getString(R.string.tablet_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(sensors);
            noButton.setOnClickListener(v14 -> {
                this.HideBlur();
                dialog.dismiss();
            });
            yesButton.setOnClickListener(v15 -> {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.GONE);
                try {
                    this.mount();
                    String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + this.win);
                    ShellUtils.fastCmd("cp " + this.getFilesDir() + "/tabletmode.vbs /sdcard/");
                    if (mnt_stat.isEmpty()) {
                        noButton.setVisibility(View.GONE);
                        yesButton.setText(this.getString(R.string.chat));
                        dismissButton.setText(this.getString(R.string.cancel));
                        yesButton.setVisibility(View.VISIBLE);
                        dismissButton.setVisibility(View.VISIBLE);
                        icons.setVisibility(View.GONE);
                        this.ShowBlur();
                        messages.setText(this.getString(R.string.mountfail));
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v15) {
                                MainActivity.this.HideBlur();
                                icons.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        });
                        yesButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v15) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse("https://t.me/woahelperchat"));
                                MainActivity.this.startActivity(i);
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox || true ");
                        ShellUtils.fastCmd("cp /sdcard/tabletmode.vbs " + this.winpath + "/Toolbox");
                        ShellUtils.fastCmd("rm /sdcard/tabletmode.vbs");
                        messages.setText(this.getString(R.string.done));
                        dismissButton.setText(this.getString(R.string.dismiss));
                        dismissButton.setVisibility(View.VISIBLE);
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v15) {
                                MainActivity.this.HideBlur();
                                dialog.dismiss();
                            }
                        });
                    }
                    dismissButton.setText(this.getString(R.string.dismiss));
                    dismissButton.setVisibility(View.VISIBLE);
                    dismissButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v15) {
                            MainActivity.this.HideBlur();
                            dialog.dismiss();
                        }
                    });
                } catch (Exception error) {
                    error.printStackTrace();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });


        this.n.cvSoftware.setOnClickListener(v -> {
            this.ShowBlur();
            noButton.setText(this.getString(R.string.no));
            yesButton.setText(this.getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(this.getString(R.string.software_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(sensors);
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.HideBlur();
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
                        MainActivity.this.mount();
                        String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + MainActivity.this.win);
                        ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/WorksOnWoa.url /sdcard");
                        ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/TestedSoftware.url /sdcard");
                        ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/ARMSoftware.url /sdcard");
                        if (mnt_stat.isEmpty()) {
                            noButton.setVisibility(View.GONE);
                            yesButton.setText(MainActivity.this.getString(R.string.chat));
                            dismissButton.setText(MainActivity.this.getString(R.string.cancel));
                            yesButton.setVisibility(View.VISIBLE);
                            dismissButton.setVisibility(View.VISIBLE);
                            icons.setVisibility(View.GONE);
                            MainActivity.this.ShowBlur();
                            messages.setText(getString(R.string.mountfail));
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MainActivity.this.HideBlur();
                                    icons.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                }
                            });
                            yesButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse("https://t.me/woahelperchat"));
                                    MainActivity.this.startActivity(i);
                                }
                            });
                            dialog.setCancelable(false);
                            dialog.show();
                        } else {
                            ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/Toolbox || true ");
                            ShellUtils.fastCmd("cp /sdcard/WorksOnWoa.url " + MainActivity.this.winpath + "/Toolbox");
                            ShellUtils.fastCmd("cp /sdcard/TestedSoftware.url " + MainActivity.this.winpath + "/Toolbox");
                            ShellUtils.fastCmd("cp /sdcard/ARMSoftware.url " + MainActivity.this.winpath + "/Toolbox");
                            ShellUtils.fastCmd("rm /sdcard/WorksOnWoa.url");
                            ShellUtils.fastCmd("rm /sdcard/TestedSoftware.url");
                            ShellUtils.fastCmd("rm /sdcard/ARMSoftware.url");
                            messages.setText(MainActivity.this.getString(R.string.done));
                            dismissButton.setText(MainActivity.this.getString(R.string.dismiss));
                            dismissButton.setVisibility(View.VISIBLE);
                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MainActivity.this.HideBlur();
                                    dialog.dismiss();
                                }
                            });
                        }
                        dismissButton.setText(MainActivity.this.getString(R.string.dismiss));
                        dismissButton.setVisibility(View.VISIBLE);
                        dismissButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MainActivity.this.HideBlur();
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
        });

        this.n.cvFlashUefi.setOnClickListener(v -> {
            this.ShowBlur();
            noButton.setText(this.getString(R.string.no));
            dismissButton.setText(this.getString(R.string.dismiss));
            yesButton.setText(this.getString(R.string.yes));
            dismissButton.setVisibility(View.GONE);
            yesButton.setVisibility(View.VISIBLE);
            noButton.setVisibility(View.VISIBLE);
            messages.setText(this.getString(R.string.flash_uefi_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(uefi);
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    noButton.setVisibility(View.GONE);
                    yesButton.setVisibility(View.GONE);
                    messages.setText(MainActivity.this.getString(R.string.please_wait));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MainActivity.this.flash(MainActivity.this.finduefi);
                                messages.setText(MainActivity.this.getString(R.string.flash));
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
                    MainActivity.this.HideBlur();
                    dialog.dismiss();
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.HideBlur();
                    dialog.dismiss();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

        this.n.cvDbkp.setOnClickListener(v -> {
            this.ShowBlur();
            dismissButton.setVisibility(View.GONE);
            yesButton.setVisibility(View.VISIBLE);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setText(this.getString(R.string.yes));
            noButton.setText(this.getString(R.string.no));
            dismissButton.setText(this.getString(R.string.dismiss));
            messages.setText(this.getString(R.string.dbkp_question));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(uefi);
            if (!isNetworkConnected(this)) {
                noButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                dismissButton.setVisibility(View.VISIBLE);
                dismissButton.setText(this.getString(R.string.dismiss));
				messages.setText(this.getString(R.string.internet));
            }
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    noButton.setVisibility(View.GONE);
                    yesButton.setVisibility(View.GONE);
                    messages.setText(MainActivity.this.getString(R.string.please_wait));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String bedan = ShellUtils.fastCmd("getprop ro.product.device");
                                if ("guacamole".equals(bedan) || "OnePlus7Pro".equals(bedan) || "OnePlus7Pro4G".equals(bedan)) {
                                    // Do guacamole logic here
                                    ShellUtils.fastCmd("mkdir /sdcard/dbkp");
                                    ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woa-op7/releases/download/DBKP/guacamole.fd -O /sdcard/dbkp/guacamole.fd");
                                    ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp");
                                    ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp8150.cfg /sdcard/dbkp");
                                    ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp.hotdog.bin /sdcard/dbkp");
                                    ShellUtils.fastCmd(MainActivity.this.getString(R.string.backup));
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
                                    messages.setText(getString(R.string.op7));
                                    dismissButton.setVisibility(View.VISIBLE);
                                } else if ("hotdog".equals(bedan) || "OnePlus7TPro".equals(bedan) || "OnePlus7TPro4G".equals(bedan)) {
                                    // Do hotdog logic here
                                    ShellUtils.fastCmd("mkdir /sdcard/dbkp");
                                    ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woa-op7/releases/download/DBKP/hotdog.fd -O /sdcard/dbkp/hotdog.fd");
                                    ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp");
                                    ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp8150.cfg /sdcard/dbkp");
                                    ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp.hotdog.bin /sdcard/dbkp");
                                    ShellUtils.fastCmd(MainActivity.this.getString(R.string.backup));
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
                                    messages.setText(getString(R.string.op7));
                                    dismissButton.setVisibility(View.VISIBLE);
                                }
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
                    MainActivity.this.HideBlur();
                    dialog.dismiss();
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.HideBlur();
                    dialog.dismiss();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

        this.x.cvBackup.setOnClickListener(v -> {
            this.ShowBlur();
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            dismissButton.setVisibility(View.VISIBLE);
            noButton.setText("Android");
            yesButton.setText("Windows");
            dismissButton.setText(this.getString(R.string.no));
            icons.setVisibility(View.VISIBLE);
            icons.setImageDrawable(boot);
            messages.setText(this.getString(R.string.backup_boot_question));
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    noButton.setVisibility(View.GONE);
                    yesButton.setVisibility(View.GONE);
                    dismissButton.setVisibility(View.GONE);
                    messages.setText(MainActivity.this.getString(R.string.please_wait));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
                            String currentDateAndTime = sdf.format(new Date());
                            pref.setDATE(MainActivity.this, currentDateAndTime);
                            MainActivity.this.x.tvDate.setText(String.format(MainActivity.this.getString(R.string.last), pref.getDATE(MainActivity.this)));
                            String mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + MainActivity.this.win);
                            MainActivity.this.winBackup();
                            messages.setText(MainActivity.this.getString(R.string.backuped));
                            dismissButton.setText(R.string.dismiss);
                            dismissButton.setVisibility(View.VISIBLE);
                        }
                    }, 4000);

                }
            });
            dismissButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.this.HideBlur();
                    dialog.dismiss();
                }
            });
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    noButton.setVisibility(View.GONE);
                    yesButton.setVisibility(View.GONE);
                    dismissButton.setVisibility(View.GONE);
                    messages.setText(MainActivity.this.getString(R.string.please_wait));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
                            String currentDateAndTime = sdf.format(new Date());
                            pref.setDATE(MainActivity.this, currentDateAndTime);
                            MainActivity.this.x.tvDate.setText(String.format(MainActivity.this.getString(R.string.last), pref.getDATE(MainActivity.this)));
                            ShellUtils.fastCmd(MainActivity.this.getString(R.string.backup));
                            messages.setText(MainActivity.this.getString(R.string.backuped));
                            dismissButton.setText(R.string.dismiss);
                            dismissButton.setVisibility(View.VISIBLE);
                        }
                    }, 500);
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        });

        this.k.toolbarlayout.toolbar.setTitle(this.getString(R.string.preferences));
        this.k.toolbarlayout.toolbar.setNavigationIcon(iconToolbar);

        //MainActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        this.x.toolbarlayout.settings.setOnClickListener(v -> {
            this.backable = 1;
            this.x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
            this.setContentView(this.k.getRoot());
            this.k.settingsPanel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
            this.k.modemdump.setChecked(pref.getMODEM(this));
            this.k.backupQB.setChecked(pref.getBACKUP(this));
            this.k.backupQBA.setChecked(pref.getBACKUP_A(this));
            this.k.autobackup.setChecked(!pref.getAUTO(this));
            this.k.autobackupA.setChecked(!pref.getAUTO(this));
            this.k.confirmation.setChecked(pref.getCONFIRM(this));
            this.k.automount.setChecked(pref.getAutoMount(this));
            this.k.securelock.setChecked(!pref.getSecure(this));
            this.k.mountLocation.setChecked(pref.getMountLocation(this));
            this.k.toolbarlayout.settings.setVisibility(View.GONE);
            //k.language.setText(R.string.language);
        });

        this.k.mountLocation.setOnCheckedChangeListener((compoundButton, b) -> {
            pref.setMountLocation(this, b);
            this.winpath = (b ? "/mnt/Windows" : "/mnt/sdcard/Windows");
        });

        this.k.modemdump.setOnCheckedChangeListener((compoundButton, b) -> {
            pref.setMODEM(this, b);
            if (b) this.n.cvDumpModem.setVisibility(View.GONE);
            else this.n.cvDumpModem.setVisibility(View.VISIBLE);
        });

        this.d.toolbarlayout.back.setVisibility(View.VISIBLE);
        this.d.toolbarlayout.back.setOnClickListener(v -> {
            this.getWindow().getCurrentFocus().startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
            this.setContentView(this.x.getRoot());
            this.x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));

            this.backable = 0;
        });
        this.k.backupQB.setOnCheckedChangeListener((compoundButton, b) -> {
            if (pref.getBACKUP(this)) {
                pref.setBACKUP(this, false);
                k.autobackup.setVisibility(View.VISIBLE);
            } else {
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
            }
        });

        this.k.backupQBA.setOnCheckedChangeListener((compoundButton, b) -> {
            if (pref.getBACKUP_A(this)) {
                pref.setBACKUP_A(this, false);
                k.autobackupA.setVisibility(View.VISIBLE);
            } else {
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
            }
        });

        this.x.cvInfo.setOnClickListener(v -> new Thread(new Runnable() {
            @Override
            public void run() {
                //if (!BuildConfig.VERSION_NAME.equals(ShellUtils.fastCmd(getFilesDir()+"/busybox wget -q -O - https://raw.githubusercontent.com/Marius586/WoA-Helper-update/main/README.md"))){
                //update();
                //}
            }
        }).start());

        this.k.autobackup.setOnCheckedChangeListener((compoundButton, b) -> pref.setAUTO(this, !b));
        this.k.autobackupA.setOnCheckedChangeListener((compoundButton, b) -> pref.setAUTO_A(this, !b));

        this.k.confirmation.setOnCheckedChangeListener((compoundButton, b) -> pref.setCONFIRM(this, b));

        this.k.securelock.setOnCheckedChangeListener((compoundButton, b) -> pref.setSecure(this, !b));

        this.k.automount.setOnCheckedChangeListener((compoundButton, b) -> pref.setAutoMount(this, b));

        this.k.button.setOnClickListener(v -> {
            this.k.settingsPanel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_out));
            this.setContentView(this.x.getRoot());
            this.x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_in));
            this.backable = 0;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        this.checkuefi();
    }

    @Override
    public void onBackPressed() {
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        switch (viewGroup.getId()) {
            case R.id.scriptstab:
                this.z.scriptstab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_out));
                this.setContentView(this.n.getRoot());
                this.n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_in));
                this.backable++;
                break;
            case R.id.toolboxtab:
                this.n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_out));
                this.setContentView(this.x.getRoot());
                this.x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_in));
                break;
            case R.id.settingsPanel:
                this.k.settingsPanel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_out));
                this.setContentView(this.x.getRoot());
                this.x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_in));
                break;
            case R.id.mainlayout:
                this.finish();
                break;
            default:
                this.x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
                this.setContentView(this.x.getRoot());
                break;
        }
    }

    public void update() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!"true".equals(ShellUtils.fastCmd("if [ -e " + MainActivity.this.getFilesDir() + "/woahelper.apk ] ; then echo true ; fi"))) {
                    ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://raw.githubusercontent.com/Marius586/WoA-Helper-update/main/woahelper.apk -O " + MainActivity.this.getFilesDir() + "/woahelper.apk");
                    ShellUtils.fastCmd("pm install " + MainActivity.this.getFilesDir() + "/woahelper.apk && rm " + MainActivity.this.getFilesDir() + "/woahelper.apk");
                }

            }
        }).start();
    }

    public void flash(String uefi) {
        ShellUtils.fastCmd("dd if=" + uefi + " of=/dev/block/bootdevice/by-name/boot$(getprop ro.boot.slot_suffix) bs=16M");
    }

    public void mount() {
        String c;
        if (pref.getMountLocation(this)) {
            c = String.format("su -mm -c " + pref.getbusybox(this) + this.getString(R.string.mount2), this.win);
            ShellUtils.fastCmd(this.getString(R.string.mk2));
            ShellUtils.fastCmd(c);
        } else {
            c = String.format("su -mm -c " + pref.getbusybox(this) + this.getString(R.string.mount), this.win);
            ShellUtils.fastCmd(this.getString(R.string.mk));
            ShellUtils.fastCmd(c);
        }
        this.mounted = this.getString(R.string.unmountt);
        this.x.tvMnt.setText(String.format(this.getString(R.string.mnt_title), this.mounted));
    }

    public void winBackup() {
        if (pref.getMountLocation(this)) {
            ShellUtils.fastCmd(this.getString(R.string.mk2));
            ShellUtils.fastCmd(String.format(pref.getbusybox(this) + this.getString(R.string.mount2), this.win));
            ShellUtils.fastCmd(this.getString(R.string.backup2));
            ShellUtils.fastCmd(this.getString(R.string.unmount2));
            ShellUtils.fastCmd(this.getString(R.string.rm2));
        } else {
            ShellUtils.fastCmd(this.getString(R.string.mk));
            ShellUtils.fastCmd(String.format(pref.getbusybox(this) + this.getString(R.string.mount), this.win));
            ShellUtils.fastCmd(this.getString(R.string.backup1));
            ShellUtils.fastCmd(this.getString(R.string.unmount));
            ShellUtils.fastCmd(this.getString(R.string.rm));
        }
    }

    public void unmount() {
        if (pref.getMountLocation(this)) {
            ShellUtils.fastCmd(this.getString(R.string.unmount2));
            ShellUtils.fastCmd(this.getString(R.string.rm2));
        } else {
            ShellUtils.fastCmd(this.getString(R.string.unmount));
            ShellUtils.fastCmd(this.getString(R.string.rm));
        }
        this.mounted = this.getString(R.string.mountt);
        this.x.tvMnt.setText(String.format(this.getString(R.string.mnt_title), this.mounted));
    }

    public void dump() {
        if (pref.getMountLocation(this)) {
            ShellUtils.fastCmd(this.getString(R.string.modem12));
            ShellUtils.fastCmd(this.getString(R.string.modem22));
        } else {
            ShellUtils.fastCmd(this.getString(R.string.modem1));
            ShellUtils.fastCmd(this.getString(R.string.modem2));
        }
    }

    public void checkdevice() {
        final Dialog dialog = new Dialog(this);
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
            messages.setText(this.getString(R.string.nonroot));
            dialog.show();
            dialog.setCancelable(false);
        }
        String[] supported = {"a52sxq", "alphalm_lao_com", "alphaplus_lao_com", "alioth", "alphalm", "alphaplus", "andromeda", "betalm", "betaplus_lao_com", "betalm_lao_com", "beryllium", "bhima", "cepheus", "cheeseburger", "curtana2", "chiron", "curtana", "curtana_india", "joyeuse", "curtana_cn", "curtanacn", "cmi", "davinci", "dumpling", "dipper", "durandal", "durandal_india", "enchilada", "equuleus", "excalibur", "excalibur_india", "flashlmdd", "flashlmdd_lao_com", "fajita", "houji", "joan", "judyln", "judyp", "judypn", "guacamole", "guacamoleb", "gram", "hotdog", "hotdogb", "hotdogg", "lisa", "marble", "mh2lm", "mh2plus_lao_com", "mh2lm_lao_com", "mh2lm5g", "mh2lm5g_lao_com", "miatoll", "nabu", "pipa", "OnePlus6", "OnePlus6T", "OnePlus7", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7T", "OnePlus7TPro", "OnePlus7TPro4G", "OnePlus7TPro5G", "OP7ProNRSpr", "OnePlus7TProNR", "perseus", "polaris", "Pong", "pong", "q2q", "raphael", "raphaelin", "raphaels", "RMX2170", "RMX2061", "sagit", "surya", "vayu", "venus", "winner", "winnerx", "xpeng", "G973F", "SM-G973F", "beyond1lte", "beyond1qlte", "G973U", "G973U1", "SM-G973U", "SM-G973U1", "G9730", "SM-G9730", "G973N", "SM-G973N", "G973X", "SM-G973X", "G973C", "SM-G973C", "SCV41", "SM-SC41", "beyond1"};
        this.device = ShellUtils.fastCmd("getprop ro.product.device ");
        this.model = ShellUtils.fastCmd("getprop ro.product.model");
        if (!Arrays.asList(supported).contains(this.device)) {
            this.k.modemdump.setVisibility(View.VISIBLE);
            if (!pref.getAGREE(this)) {
                messages.setText(this.getString(R.string.unsupported));
                this.device = "unknown";
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
            this.device = "unknown";
        }

        String stram = MainActivity.extractNumberFromString(new RAM().getMemory(this.getApplicationContext()));
        this.ramvalue = Double.parseDouble(stram) / 100;

        String run = ShellUtils.fastCmd(" su -c cat /proc/cmdline ");
        if (run.isEmpty()) panel = "Unknown";
        else {
            if (run.contains("j20s_42") || run.contains("k82_42")) {
                panel = "Huaxing";
            } else if (run.contains("j20s_36") || run.contains("k82_36")) {
                panel = "Tianma";
            } else {
                panel = ShellUtils.fastCmd("su -c cat /proc/cmdline | tr ' :=' '\n'|grep dsi|tr ' _' '\n'|tail -3|head -1 ");
                if (!panel.isEmpty()) if ((!(Objects.equals(panel, "f1mp") || Objects.equals(panel, "f1p2") || Objects.equals(panel, "global") || pref.getAGREE(this))) && ("raphael".equals(this.device) || "raphaelin".equals(this.device) || "raphaels".equals(this.device) || "cepheus".equals(this.device))) {
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
            if (Objects.equals(panel, "2")) panel = "f1p2_2";
        }

    }

//    public void checkntfs() {
//		this.findntfs = ShellUtils.fastCmd(this.getString(R.string.ntfsChk));
//		if (!findntfs.isEmpty()) {
//			#### DO NOTHING I GUESS
//		} else {
//			dialog.show();
//			messages.setText(getString(R.string.ntfs));
//			yesButton.setText(getString(R.string.yes));
//			dismissButton.setText(getString(R.string.nah));
//			noButton.setText(getString(R.string.later));
//			yesButton.setVisibility(View.VISIBLE);
//			noButton.setVisibility(View.VISIBLE);
//			dismissButton.setVisibility(View.VISIBLE);
//			yesButton.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					Intent i = new Intent(Intent.ACTION_VIEW);
//					#### INSTALL NTFS3G MAGISK MODULE HERE
//                    dialog.dismiss();
//                    pref.setAGREE(MainActivity.this, true);
//				}
//            });
//            noButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                    dialog.dismiss();
//                }
//            });
//            dismissButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dialog.dismiss();
//                    pref.setAGREE(MainActivity.this, true);
//                }
//            });
//        }
	
	public void checkuefi() {
        ShellUtils.fastCmd("su -c mkdir /sdcard/UEFI");
        this.finduefi = "\""+ShellUtils.fastCmd(this.getString(R.string.uefiChk))+"\"";
        this.device = ShellUtils.fastCmd("getprop ro.product.device ");
        if (finduefi.contains("img")) {
            this.x.tvQuickBoot.setText(getString(R.string.quickboot_title));
            this.x.cvQuickBoot.setEnabled(true);
            this.x.cvQuickBoot.setVisibility(View.VISIBLE);
            this.x.tvQuickBoot.setText(getString(R.string.quickboot_title));
            this.n.tvFlashUefi.setText(this.getString(R.string.flash_uefi_title));
            this.n.tvUefiSubtitle.setText(this.getString(R.string.flash_uefi_subtitle));
            this.n.cvFlashUefi.setEnabled(true);
            this.x.tvToolbox.setText(this.getString(R.string.toolbox_title));
            this.x.cvToolbox.setEnabled(true);
            this.x.tvToolboxSubtitle.setText(this.getString(R.string.toolbox_subtitle));
            this.x.tvToolboxSubtitle.setVisibility(View.VISIBLE);
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
        } else {
            this.x.cvQuickBoot.setEnabled(false);
            this.x.cvQuickBoot.setVisibility(View.VISIBLE);
            this.x.tvQuickBoot.setText(getString(R.string.uefi_not_found));
            this.x.tvBootSubtitle.setText(String.format(getString(R.string.uefi_not_found_subtitle), device));
            this.n.tvFlashUefi.setText(this.getString(R.string.uefi_not_found));
            this.n.tvUefiSubtitle.setText(String.format(this.getString(R.string.uefi_not_found_subtitle), this.device));
            this.n.cvFlashUefi.setEnabled(false);
            this.x.tvToolbox.setText(this.getString(R.string.toolbox_title));
            this.x.cvToolbox.setEnabled(true);
            this.x.tvToolboxSubtitle.setText(this.getString(R.string.toolbox_subtitle));
            this.x.tvToolboxSubtitle.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        TextView myTextView1 = this.findViewById(R.id.text);
        TextView myTextView2 = this.findViewById(R.id.deviceName);
        TextView myTextView3 = this.findViewById(R.id.tv_panel);
        TextView myTextView4 = this.findViewById(R.id.tv_ramvalue);
        TextView myTextView5 = this.findViewById(R.id.guide_text);
        TextView myTextView6 = this.findViewById(R.id.group_text);
        TextView myTextView7 = this.findViewById(R.id.tv_slot);
        TextView myTextView8 = this.findViewById(R.id.tv_date);
        if (Configuration.ORIENTATION_LANDSCAPE != newConfig.orientation && Objects.equals(this.device, "nabu")) {
            this.x.app.setOrientation(LinearLayout.VERTICAL);
            this.x.top.setOrientation(LinearLayout.HORIZONTAL);
            myTextView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE2);
            myTextView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
            myTextView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
            myTextView4.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
            myTextView5.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
            myTextView6.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
            myTextView7.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
            myTextView8.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
        } else if (Configuration.ORIENTATION_PORTRAIT != newConfig.orientation && Objects.equals(this.device, "nabu")) {
            this.x.app.setOrientation(LinearLayout.HORIZONTAL);
            this.x.top.setOrientation(LinearLayout.VERTICAL);
            myTextView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
            myTextView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
            myTextView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
            myTextView4.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
            myTextView5.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
            myTextView6.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
            myTextView7.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
            myTextView8.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
        }
    }

    public void ShowBlur() {
        this.x.blur.setVisibility(View.VISIBLE);
    }

    public void HideBlur() {
        this.x.blur.setVisibility(View.GONE);
    }

    @SuppressLint("AppBundleLocaleChanges")
    private void setApplicationLocale(String locale) {
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.setLocale(new Locale(locale.toLowerCase()));
        resources.updateConfiguration(config, dm);
        pref.setlocale(this, locale);
    }

    public ExecutorService getExecutorService() {
        return this.executorService;
    }

    public void setExecutorService(final ExecutorService executorService) {
        this.executorService = executorService;
    }
}
