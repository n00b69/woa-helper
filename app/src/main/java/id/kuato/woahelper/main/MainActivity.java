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

	public final class BuildConfig {
	public static final String VERSION_NAME = "1.8.4_BETA35";
	}
	static final Object lock = new Object();
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
	String dbkpmodel;
	String win;
	String grouplink = "https://t.me/woahelperchat";
	String guidelink = "https://github.com/n00b69";
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
		ShellUtils.fastCmd("chmod 777 " + this.getFilesDir() + "/mount.ntfs");
		ShellUtils.fastCmd("chmod 777 " + this.getFilesDir() + "/libfuse-lite.so");
		ShellUtils.fastCmd("chmod 777 " + this.getFilesDir() + "/libntfs-3g.so");
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
			String[] files = {"guacamole.fd", "hotdog.fd", "dbkp8150.cfg", "dbkp.hotdog.bin", "dbkp.cepheus.bin", "dbkp.nabu.bin", "busybox", "install.bat", "sta.exe", "sdd.exe", "sdd.conf", "Switch to Android.lnk", "usbhostmode.exe", "ARMSoftware.url", "ARMRepo.url", "TestedSoftware.url", "WorksOnWoa.url", "RotationShortcut.lnk", "display.exe", "RemoveEdge.bat"};
			int i = 0;
			while (!files[i].isEmpty()) {
				if (ShellUtils.fastCmd(String.format("ls %1$s |grep %2$s", getFilesDir(), files[i])).isEmpty()) {
					copyAssets();
					break;
				}
				i++;
			}
		} else {
			copyAssets();
			pref.setVersion(this, currentVersion);
		}
		super.onCreate(savedInstanceState);
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
		this.checkupdate();
		this.checkRoot();
		this.checkdbkpmodel();
		this.win = ShellUtils.fastCmd("realpath /dev/block/by-name/win");
		if ("/dev/block/by-name/win".equals(this.win)) this.win = ShellUtils.fastCmd("realpath /dev/block/by-name/mindows");
		if ("/dev/block/by-name/mindows".equals(this.win)) this.win = ShellUtils.fastCmd("realpath /dev/block/by-name/windows");
		this.winpath = (pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows");
		String mount_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + this.win);
		if (mount_stat.isEmpty()) this.mounted = getString(R.string.mountt);
		else this.mounted = getString(R.string.unmountt);
		this.x.tvMnt.setText(String.format(this.getString(R.string.mnt_title), this.mounted));
		this.x.tvDate.setText(String.format(this.getString(R.string.last), pref.getDATE(this)));
		MaterialButton yesButton = dialog.findViewById(R.id.yes);
		MaterialButton noButton = dialog.findViewById(R.id.no);
		MaterialButton dismissButton = dialog.findViewById(R.id.dismiss);
		TextView messages = dialog.findViewById(R.id.messages);
		ImageView icons = dialog.findViewById(R.id.icon);
		ProgressBar bar = dialog.findViewById(R.id.progress);
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		Drawable android = ResourcesCompat.getDrawable(this.getResources(), R.drawable.android, null);
		Drawable atlasos = ResourcesCompat.getDrawable(this.getResources(), R.drawable.atlasos2, null);
		Drawable boot = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_disk, null);
		Drawable download = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_download, null);
		Drawable edge = ResourcesCompat.getDrawable(this.getResources(), R.drawable.edge2, null);
		Drawable icon = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_launcher_foreground, null);
		Drawable modem = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_modem, null);
		Drawable mnt = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_mnt, null);
		Drawable sensors = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_sensor, null);
		Drawable settings = ResourcesCompat.getDrawable(this.getResources(), R.drawable.settings, null);
		Drawable uefi = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_uefi, null);
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
		TextView myTextView2 = this.findViewById(R.id.deviceName);
		myTextView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
		TextView myTextView3 = this.findViewById(R.id.tv_panel);
		myTextView3.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
		TextView myTextView4 = this.findViewById(R.id.tv_ramvalue);
		myTextView4.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
		TextView myTextView5 = this.findViewById(R.id.guide_text);
		myTextView5.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
		TextView myTextView6 = this.findViewById(R.id.group_text);
		myTextView6.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
		TextView myTextView7 = this.findViewById(R.id.tv_slot);
		myTextView7.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
		if (Objects.equals(this.device, "nabu") || Objects.equals(this.device, "pipa") || Objects.equals(this.device, "winner") || Objects.equals(this.device, "winnerx") || Objects.equals(this.device, "q2q") || Objects.equals(this.device, "t860") || Objects.equals(this.device, "t865")) {
			this.x.NabuImage.setVisibility(View.VISIBLE);
			this.x.DeviceImage.setVisibility(View.GONE);
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
			TextView myTextView1 = this.findViewById(R.id.text);
			myTextView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE1);
		}
			switch (this.device) {
				case "andromeda" -> {
					this.guidelink = "https://project-aloha.github.io/";
					this.grouplink = "https://t.me/project_aloha_issues";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.unknown, null));
				}
				case "alphalm", "alphaplus", "alpha_lao_com", "alphalm_lao_com", "alphaplus_lao_com" -> {
					this.guidelink = "https://github.com/n00b69/woa-alphaplus";
					this.grouplink = "https://t.me/woahelperchat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.alphaplus, null));
				}
				case "betaplus", "betalm", "beta_lao_com", "betaplus_lao_com", "betalm_lao_com" -> {
					this.guidelink = "https://github.com/n00b69/woa-betalm";
					this.grouplink = "https://t.me/woahelperchat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.betalm, null));
				}
				case "flashlmdd", "flash_lao_com", "flashlm", "flashlmdd_lao_com" -> {
					this.guidelink = "https://github.com/n00b69/woa-flashlmdd";
					this.grouplink = "https://t.me/woahelperchat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.flashlmdd, null));
				}
				case "mh2lm", "mh2plus", "mh2plus_lao_com", "mh2lm_lao_com" -> {
					this.guidelink = "https://github.com/n00b69/woa-mh2lm";
					this.grouplink = "https://t.me/woahelperchat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.mh2lm, null));
				}
				case "mh2lm5g", "mh2lm5g_lao_com" -> {
					this.guidelink = "https://github.com/n00b69/woa-mh2lm5g";
					this.grouplink = "https://t.me/woahelperchat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.mh2lm, null));
				}
				case "beryllium" -> {
					this.guidelink = "https://github.com/n00b69/woa-beryllium";
					this.grouplink = "https://t.me/WinOnF1";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.beryllium, null));
				}
				case "bhima", "vayu" -> {
					this.guidelink = "https://github.com/woa-vayu/POCOX3Pro-Guides";
					this.grouplink = "https://t.me/woahelperchat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.vayu, null));
					this.x.tvPanel.setVisibility(View.VISIBLE);
					this.n.cvDumpModem.setVisibility(View.VISIBLE);
				}
				case "cepheus" -> {
					this.guidelink = "https://github.com/ivanvorvanin/Port-Windows-XiaoMI-9";
					this.grouplink = "http://t.me/woacepheus";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.cepheus, null));
					this.x.tvPanel.setVisibility(View.VISIBLE);
					this.n.cvDbkp.setVisibility(View.VISIBLE);
					this.n.cvDumpModem.setVisibility(View.VISIBLE);
					this.n.cvFlashUefi.setVisibility(View.GONE);
				}
				case "cheeseburger" -> {
					this.guidelink = "https://renegade-project.tech/";
					this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.cheeseburger, null));
				}
				case "dumpling" -> {
					this.guidelink = "https://renegade-project.tech/";
					this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.dumpling, null));
				}
				case "chiron" -> {
					this.guidelink = "https://renegade-project.tech/";
					this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.chiron, null));
				}
				case "curtana", "curtana2", "curtana_india", "curtana_cn", "curtanacn", "durandal", "durandal_india", "excalibur", "excalibur2", "excalibur_india", "gram", "joyeuse", "miatoll" -> {
					this.guidelink = "https://github.com/woa-miatoll/Port-Windows-11-Redmi-Note-9-Pro";
					this.grouplink = "http://t.me/woamiatoll";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.miatoll, null));
					this.x.tvPanel.setVisibility(View.VISIBLE);
				}
				case "dipper" -> {
					this.guidelink = "https://github.com/n00b69/woa-dipper";
					this.grouplink = "https://t.me/woadipper";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.dipper, null));
				}
				case "equuleus" -> {
					this.guidelink = "https://github.com/n00b69/woa-equuleus";
					this.grouplink = "https://t.me/woaequuleus";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.equuleus, null));
				}
				case "G973F", "SM-G973F", "beyond1lte", "beyond1qlte", "G973U", "G973U1", "SM-G973U", "SM-G973U1", "G9730", "SM-G9730", "G973N", "SM-G973N", "G973X", "SM-G973X", "G973C", "SM-G973C", "SCV41", "SM-SC41", "beyond1" -> {
					this.guidelink = "https://github.com/sonic011gamer/Mu-Samsung";
					this.grouplink = "https://t.me/woahelperchat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.beyond1, null));
				}
				case "lisa" -> {
					this.guidelink = "https://github.com/ETCHDEV/Port-Windows-11-Xiaomi-11-Lite-NE";
					this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.lisa, null));
				}
				case "nabu" -> {
					this.guidelink = "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/tree/main";
					this.grouplink = "https://t.me/nabuwoa";
					this.x.NabuImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.nabu, null));
					this.x.tvPanel.setVisibility(View.VISIBLE);
					this.n.cvDbkp.setVisibility(View.VISIBLE);
					this.n.cvFlashUefi.setVisibility(View.GONE);
				}
				case "perseus" -> {
					this.guidelink = "https://github.com/n00b69/woa-perseus";
					this.grouplink = "https://t.me/woaperseus";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.perseus, null));
				}
				case "pipa" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/pad6_pipa";
					this.x.NabuImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.pipa, null));
					this.x.tvPanel.setVisibility(View.VISIBLE);
				}
				case "polaris" -> {
					this.guidelink = "https://github.com/n00b69/woa-polaris";
					this.grouplink = "https://t.me/WinOnMIX2S";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.polaris, null));
					this.x.tvPanel.setVisibility(View.VISIBLE);
				}
				case "Pong", "pong" -> {
					this.guidelink = "https://github.com/Govro150/woa-pong";
					this.grouplink = "https://t.me/woa_pong";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.pong, null));
				}
				case "raphael", "raphaelin", "raphaels" -> {
					this.guidelink = "https://github.com/woa-raphael/woa-raphael";
					this.grouplink = "https://t.me/woaraphael";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.raphael, null));
					this.x.tvPanel.setVisibility(View.VISIBLE);
					this.n.cvDumpModem.setVisibility(View.VISIBLE);
				}
				case "surya" -> {
					this.guidelink = "https://github.com/woa-surya/pocox3nfc-guides";
					this.grouplink = "https://t.me/windows_on_pocox3_nfc";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.vayu, null));
					this.x.tvPanel.setVisibility(View.VISIBLE);
				}
				case "a52sxq" -> {
					this.guidelink = "https://github.com/arminask/Port-Windows-11-Galaxy-A52s-5G";
					this.grouplink = "https://t.me/a52sxq_uefi";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.a52sxq, null));
				}
				case "judyln", "judyp", "judypn" -> {
					this.guidelink = "https://github.com/n00b69/woa-everything";
					this.grouplink = "https://t.me/lgedevices";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.unknown, null));
				}
				case "joan" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/lgedevices";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.unknown, null));
				}
				case "OnePlus6", "fajita" -> {
					this.guidelink = "https://github.com/n00b69/woa-op6";
					this.grouplink = "https://t.me/WinOnOP6";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.fajita, null));
				}
				case "OnePlus6T", "enchilada" -> {
					this.guidelink = "https://github.com/n00b69/woa-op6";
					this.grouplink = "https://t.me/WinOnOP6";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.enchilada, null));
				}
				case "hotdog", "OnePlus7TPro", "OnePlus7TPro4G" -> {
					this.guidelink = "https://github.com/n00b69/woa-op7";
					this.grouplink = "https://t.me/onepluswoachat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.hotdog, null));
					this.n.cvDumpModem.setVisibility(View.VISIBLE);
					this.n.cvDbkp.setVisibility(View.VISIBLE);
					this.n.cvFlashUefi.setVisibility(View.GONE);
				}
				case "guacamole", "OnePlus7Pro", "OnePlus7Pro4G" -> {
					this.guidelink = "https://github.com/n00b69/woa-op7";
					this.grouplink = "https://t.me/onepluswoachat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.guacamole, null));
					this.n.cvDumpModem.setVisibility(View.VISIBLE);
					this.n.cvDbkp.setVisibility(View.VISIBLE);
					this.n.cvFlashUefi.setVisibility(View.GONE);
				}
				case "guacamoleb", "hotdogb", "OnePlus7T", "OnePlus7" -> {
					this.guidelink = "https://project-aloha.github.io/";
					this.grouplink = "https://t.me/onepluswoachat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.unknown, null));
					this.n.cvDumpModem.setVisibility(View.VISIBLE);
				}
				case "OnePlus7TPro5G", "OnePlus7TProNR" -> {
					this.guidelink = "https://project-aloha.github.io/";
					this.grouplink = "https://t.me/onepluswoachat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.hotdog, null));
				}
				case "hotdogg", "OP7ProNRSpr" -> {
					this.guidelink = "https://project-aloha.github.io/";
					this.grouplink = "https://t.me/onepluswoachat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.guacamole, null));
				}
				case "sagit" -> {
					this.guidelink = "https://renegade-project.tech/";
					this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.unknown, null));
				}
				case "t860", "t865" -> {
					this.guidelink = "https://project-aloha.github.io/";
					this.grouplink = "https://t.me/project_aloha_issues";
					this.x.NabuImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.unknown, null));
				}
				case "q2q" -> {
					this.guidelink = "https://project-aloha.github.io/";
					this.grouplink = "https://t.me/project_aloha_issues";
					this.x.NabuImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.q2q, null));
				}
				case "winnerx", "winner" -> {
					this.guidelink = "https://github.com/n00b69/woa-winner";
					this.grouplink = "https://t.me/project_aloha_issues";
					this.x.NabuImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.winner, null));
				}
				case "xpeng" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/woahelperchat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.xpeng, null));
				}
				case "venus" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/woahelperchat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.venus, null));
				}
				case "alioth" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/woahelperchat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.alioth, null));
				}
				case "davinci" -> {
					this.guidelink = "https://github.com/zxcwsurx/woa-davinci";
					this.grouplink = "https://t.me/woa_davinci";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.raphael, null));
				}
				case "marble" -> {
					this.guidelink = "https://github.com/Xhdsos/woa-marble";
					this.grouplink = "https://t.me/woa_marble";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.marble, null));
				}
				case "RMX2061" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/realme6PROwindowsARM64";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.rmx2061, null));
				}
				case "RMX2170" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/realme6PROwindowsARM64";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.rmx2170, null));
				}
				case "cmi" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/dumanthecat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.cmi, null));
				}
				case "houji" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/dumanthecat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.houji, null));
				}
				case "meizu20pro" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/dumanthecat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.meizu20pro, null));
				}
				case "husky" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/dumanthecat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.husky, null));
				}
				case "redfin" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/dumanthecat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.redfin, null));
				}
				case "e3q" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/biskupmuf";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.e3q, null));
				}
				case "dm3q", "dm3" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/dumanthecat";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.dm3q, null));
				}
				default -> {
					this.guidelink = "https://renegade-project.tech/";
					this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.unknown, null));
					this.x.deviceName.setText(this.device);
					this.n.cvDumpModem.setVisibility(View.VISIBLE);
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
							String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
							MainActivity.this.winBackup();
							messages.setText(MainActivity.this.getString(R.string.backuped));
							dismissButton.setText(R.string.dismiss);
							dismissButton.setVisibility(View.VISIBLE);
						}
					}, 50);
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
					}, 25);
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});
		
		this.x.cvMnt.setOnClickListener(v -> {
			this.ShowBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(mnt);
			noButton.setText(this.getString(R.string.no));
			yesButton.setText(this.getString(R.string.yes));
			dismissButton.setText(this.getString(R.string.dismiss));
			String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + this.win);
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
								String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
								if (mnt_stat.isEmpty()) {
									MainActivity.this.mount();
									mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
									if (mnt_stat.isEmpty()) {
										dialog.dismiss();
										mountfail();
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
					}, 25);
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

		this.x.cvQuickBoot.setOnClickListener(v -> {
			this.ShowBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(icon);
			messages.setText(this.getString(R.string.quickboot_question));
			noButton.setText(this.getString(R.string.no));
			yesButton.setText(this.getString(R.string.yes));
			dismissButton.setText(this.getString(R.string.dismiss));
			yesButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					yesButton.setVisibility(View.GONE);
					noButton.setVisibility(View.GONE);
					messages.setText(getString(R.string.please_wait));
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							MainActivity.this.mount();
							String found = ShellUtils.fastCmd("ls " + (pref.getMountLocation(MainActivity.this) ? "/mnt/Windows" : "/mnt/sdcard/Windows") + " | grep boot.img");
							if (pref.getBACKUP(MainActivity.this) || (!pref.getAUTO(MainActivity.this) && found.isEmpty())) {
								winBackup();
								SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
								String currentDateAndTime = sdf.format(new Date());
								pref.setDATE(MainActivity.this, currentDateAndTime);
							}
							found = ShellUtils.fastCmd("find /sdcard | grep boot.img");
							if (pref.getBACKUP_A(MainActivity.this) || (!pref.getAUTO_A(MainActivity.this) && found.isEmpty())) {
								ShellUtils.fastCmd(MainActivity.this.getString(R.string.backup));
								SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
								String currentDateAndTime = sdf.format(new Date());
								pref.setDATE(MainActivity.this, currentDateAndTime);
							}
							flash(finduefi);
							ShellUtils.fastCmd("su -c svc power reboot");
							messages.setText(getString(R.string.wrong));
							dismissButton.setVisibility(View.VISIBLE);
						}
					}, 25);
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
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(android);
			messages.setText(this.getString(R.string.sta_question));
			noButton.setText(this.getString(R.string.no));
			yesButton.setText(this.getString(R.string.yes));
			dismissButton.setText(MainActivity.this.getString(R.string.dismiss));
			noButton.setOnClickListener(v2 -> {
				this.HideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v3 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sta.exe /sdcard/sta.exe");
				ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sdd.exe /sdcard/sdd.exe");
				ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sdd.conf /sdcard/sdd.conf");
				mount();
				String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
				if (mnt_stat.isEmpty()) {
					dialog.dismiss();
					mountfail();
				} else {
					ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/sta || true ");
					ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/ProgramData/sta || true ");
					ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sta.exe " + MainActivity.this.winpath + "/ProgramData/sta/sta.exe");
					ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sta.exe " + MainActivity.this.winpath + "/sta/sta.exe");
					ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sdd.exe " + MainActivity.this.winpath + "/sta/sdd.exe");
					ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sdd.conf " + MainActivity.this.winpath + "/sta/sdd.conf");
					ShellUtils.fastCmd("cp '" + MainActivity.this.getFilesDir() + "/Switch to Android.lnk' " + MainActivity.this.winpath + "/Users/Public/Desktop");
					messages.setText(this.getString(R.string.done));
					dismissButton.setVisibility(View.VISIBLE);
					dismissButton.setOnClickListener(v4 -> {
						this.HideBlur();
						dialog.dismiss();
					});
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
		
		this.n.cvDumpModem.setOnClickListener(v -> {
			this.ShowBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(modem);
			messages.setText(this.getString(R.string.dump_modem_question));
			noButton.setText(this.getString(R.string.no));
			yesButton.setText(this.getString(R.string.yes));
			yesButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					noButton.setVisibility(View.GONE);
					yesButton.setVisibility(View.GONE);
					messages.setText(MainActivity.this.getString(R.string.please_wait));
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
							if (mnt_stat.isEmpty()) {
								MainActivity.this.mount();
								MainActivity.this.dump();
							} else MainActivity.this.dump();
							messages.setText(MainActivity.this.getString(R.string.lte));
							noButton.setVisibility(View.VISIBLE);
							noButton.setText(getString(R.string.dismiss));
						}
					}, 50);
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
		
		this.n.cvFlashUefi.setOnClickListener(v -> {
			this.ShowBlur();
			dismissButton.setVisibility(View.GONE);
			yesButton.setVisibility(View.VISIBLE);
			noButton.setVisibility(View.VISIBLE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(uefi);
			messages.setText(this.getString(R.string.flash_uefi_question));
			noButton.setText(this.getString(R.string.no));
			yesButton.setText(this.getString(R.string.yes));
			dismissButton.setText(this.getString(R.string.dismiss));
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
					}, 50);
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
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(uefi);
			messages.setText(this.getString(R.string.dbkp_question, this.dbkpmodel));
			noButton.setText(this.getString(R.string.no));
			yesButton.setText(this.getString(R.string.yes));
			if (!isNetworkConnected(this)) {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				dismissButton.setVisibility(View.VISIBLE);
				dismissButton.setText(this.getString(R.string.dismiss));
				messages.setText(this.getString(R.string.internet));
			}
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
					messages.setText(MainActivity.this.getString(R.string.please_wait));
					icons.setVisibility(View.VISIBLE);
					new Thread(()->{
						ShellUtils.fastCmd("mkdir /sdcard/dbkp || true");
						ShellUtils.fastCmd(MainActivity.this.getString(R.string.backup));
						ShellUtils.fastCmd("rm /sdcard/original-boot.img || true");
						ShellUtils.fastCmd("cp /sdcard/boot.img /sdcard/dbkp/boot.img");
						ShellUtils.fastCmd("su -mm -c mv /sdcard/boot.img /sdcard/original-boot.img");
						ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp8150.cfg /sdcard/dbkp");
						ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp.hotdog.bin /sdcard/dbkp");
						ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp.cepheus.bin /sdcard/dbkp");
						ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp.nabu.bin /sdcard/dbkp");
						ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp");
						ShellUtils.fastCmd("cp /sdcard/dbkp/dbkp "+getFilesDir());
						ShellUtils.fastCmd("chmod 777 " + MainActivity.this.getFilesDir() + "/dbkp");
						String bedan = ShellUtils.fastCmd("getprop ro.product.device");
						if ("guacamole".equals(bedan) || "OnePlus7Pro".equals(bedan) || "OnePlus7Pro4G".equals(bedan)) {
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woa-op7/releases/download/DBKP/guacamole.fd -O /sdcard/dbkp/guacamole.fd");
							ShellUtils.fastCmd("cd /sdcard/dbkp");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
							ShellUtils.fastCmd("su -mm -c " + MainActivity.this.getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/guacamole.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.hotdog.bin");
							ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
							ShellUtils.fastCmd("su -mm -c mv output kernel");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
							ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
							ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
							ShellUtils.fastCmd("rm -r /sdcard/dbkp");
							ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_a bs=16M");
							ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_b bs=16M");
						} else if ("hotdog".equals(bedan) || "OnePlus7TPro".equals(bedan) || "OnePlus7TPro4G".equals(bedan)) {
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woa-op7/releases/download/DBKP/hotdog.fd -O /sdcard/dbkp/hotdog.fd");
							ShellUtils.fastCmd("cd /sdcard/dbkp");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
							ShellUtils.fastCmd("su -mm -c " + MainActivity.this.getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/hotdog.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.hotdog.bin");
							ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
							ShellUtils.fastCmd("su -mm -c mv output kernel");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
							ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
							ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
							ShellUtils.fastCmd("rm -r /sdcard/dbkp");
							ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_a bs=16M");
							ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_b bs=16M");
						} else if ("cepheus".equals(bedan)) {
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woa-everything/releases/download/Files/cepheus.fd -O /sdcard/dbkp/cepheus.fd");
							ShellUtils.fastCmd("cd /sdcard/dbkp");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
							ShellUtils.fastCmd("su -mm -c " + MainActivity.this.getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/cepheus.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.cepheus.bin");
							ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
							ShellUtils.fastCmd("su -mm -c mv output kernel");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
							ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
							ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
							ShellUtils.fastCmd("rm -r /sdcard/dbkp");
							ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot bs=16M");
						} else if ("nabu".equals(bedan)) {
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/releases/download/1.0/nabu.fd -O /sdcard/dbkp/nabu.fd");
							ShellUtils.fastCmd("cd /sdcard/dbkp");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
							ShellUtils.fastCmd("su -mm -c " + MainActivity.this.getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/nabu.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.nabu.bin");
							ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
							ShellUtils.fastCmd("su -mm -c mv output kernel");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
							ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
							ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
							ShellUtils.fastCmd("rm -r /sdcard/dbkp");
							ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_a bs=16M");
							ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_b bs=16M");
						}
						runOnUiThread(()->{
							if ("guacamole".equals(bedan) || "OnePlus7Pro".equals(bedan) || "OnePlus7Pro4G".equals(bedan) || "hotdog".equals(bedan) || "OnePlus7TPro".equals(bedan) || "OnePlus7TPro4G".equals(bedan)) {
								String op7 = getString(R.string.op7);
								messages.setText(String.format(getString(R.string.dbkp), op7));
							} else if ("cepheus".equals(bedan)) {
								String cepheus = getString(R.string.cepheus);
								messages.setText(String.format(getString(R.string.dbkp), cepheus));
							} else if ("nabu".equals(bedan)) {
								String nabu = getString(R.string.nabu);
								messages.setText(String.format(getString(R.string.dbkp), nabu));
							} 
							dismissButton.setText(getString(R.string.dismiss));
							dismissButton.setVisibility(View.VISIBLE);
							dismissButton.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									MainActivity.this.HideBlur();
									dialog.dismiss();
								}
							});
						});
					}).start();
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});
		
		this.n.cvSoftware.setOnClickListener(v -> {
			this.ShowBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(sensors);
			messages.setText(this.getString(R.string.software_question));
			noButton.setText(this.getString(R.string.no));
			yesButton.setText(this.getString(R.string.yes));
			dismissButton.setText(this.getString(R.string.dismiss));
			noButton.setOnClickListener(v5 -> {
				this.HideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v6 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				messages.setText(this.getString(R.string.please_wait));
				ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/WorksOnWoa.url /sdcard");
				ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/TestedSoftware.url /sdcard");
				ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/ARMSoftware.url /sdcard");
				ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/ARMRepo.url /sdcard");
				mount();
				String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
				if (mnt_stat.isEmpty()) {
					dialog.dismiss();
					mountfail();
				} else {
					ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/Toolbox || true ");
					ShellUtils.fastCmd("cp /sdcard/WorksOnWoa.url " + MainActivity.this.winpath + "/Toolbox");
					ShellUtils.fastCmd("cp /sdcard/TestedSoftware.url " + MainActivity.this.winpath + "/Toolbox");
					ShellUtils.fastCmd("cp /sdcard/ARMSoftware.url " + MainActivity.this.winpath + "/Toolbox");
					ShellUtils.fastCmd("cp /sdcard/ARMRepo.url " + MainActivity.this.winpath + "/Toolbox");
					ShellUtils.fastCmd("rm /sdcard/WorksOnWoa.url");
					ShellUtils.fastCmd("rm /sdcard/TestedSoftware.url");
					ShellUtils.fastCmd("rm /sdcard/ARMSoftware.url");
					ShellUtils.fastCmd("rm /sdcard/ARMRepo.url");
					messages.setText(this.getString(R.string.done));
					dismissButton.setVisibility(View.VISIBLE);
					dismissButton.setOnClickListener(v7 -> {
						this.HideBlur();
						dialog.dismiss();
					});
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});

		this.n.cvAtlasos.setOnClickListener(v -> {
			this.ShowBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.VISIBLE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(atlasos);
			messages.setText(this.getString(R.string.atlasos_question));
			noButton.setText("ReviOS");
			yesButton.setText("AtlasOS");
			dismissButton.setText(this.getString(R.string.dismiss));
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
									String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
									if (mnt_stat.isEmpty()) {
										icons.setVisibility(View.GONE);
										yesButton.setVisibility(View.VISIBLE);
										messages.setText(MainActivity.this.getString(R.string.mountfail) + "\n" + (R.string.internalstorage));
									} else {
										icons.setImageDrawable(atlasos);
										ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/Toolbox || true ");
										ShellUtils.fastCmd("cp /sdcard/ReviPlaybook.apbx " + MainActivity.this.winpath + "/Toolbox/ReviPlaybook.apbx");
										ShellUtils.fastCmd("cp /sdcard/AMEWizardBeta.zip " + MainActivity.this.winpath + "/Toolbox");
										ShellUtils.fastCmd("su -mm -c rm /sdcard/ReviPlaybook.apbx");
										ShellUtils.fastCmd("su -mm -c rm /sdcard/AMEWizardBeta.zip");
										bar.setVisibility(View.GONE);
										messages.setText(getString(R.string.done));
										dismissButton.setVisibility(View.VISIBLE);
										dismissButton.setOnClickListener(v8 -> {
											HideBlur();
											dialog.dismiss();
										});
									}
								}
							});
						}
					}).start();
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
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/modified-playbooks/releases/download/AtlasOS/AtlasPlaybook.apbx -O /sdcard/AtlasPlaybook.apbx");
							bar.setProgress((int) (bar.getMax() * 0.5), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://download.ameliorated.io/AME%20Wizard%20Beta.zip -O /sdcard/AMEWizardBeta.zip");
							bar.setProgress((int) (bar.getMax() * 0.8), true);
							MainActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									MainActivity.this.mount();
									String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
									if (mnt_stat.isEmpty()) {
										icons.setVisibility(View.GONE);
										yesButton.setVisibility(View.VISIBLE);
										messages.setText(MainActivity.this.getString(R.string.mountfail) + "\n" + (R.string.internalstorage));
									} else {
										icons.setImageDrawable(atlasos);
										ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/Toolbox || true ");
										ShellUtils.fastCmd("cp /sdcard/AtlasPlaybook.apbx " + MainActivity.this.winpath + "/Toolbox/AtlasPlaybook.apbx");
										ShellUtils.fastCmd("cp /sdcard/AMEWizardBeta.zip " + MainActivity.this.winpath + "/Toolbox");
										ShellUtils.fastCmd("su -mm -c rm /sdcard/AtlasPlaybook.apbx");
										ShellUtils.fastCmd("su -mm -c rm /sdcard/AMEWizardBeta.zip");
										bar.setVisibility(View.GONE);
										messages.setText(getString(R.string.done));
										dismissButton.setVisibility(View.VISIBLE);
										dismissButton.setOnClickListener(v9 -> {
											HideBlur();
											dialog.dismiss();
										});
									}
								}
							});
						}
					}).start();
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});

		this.z.cvUsbhost.setOnClickListener(v -> {
			this.ShowBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(mnt);
			messages.setText(this.getString(R.string.usbhost_question));
			noButton.setText(this.getString(R.string.no));
			yesButton.setText(this.getString(R.string.yes));
			dismissButton.setText(this.getString(R.string.dismiss));
			noButton.setOnClickListener(v10 -> {
				this.HideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v11 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				messages.setText(this.getString(R.string.please_wait));
				ShellUtils.fastCmd("cp " + this.getFilesDir() + "/usbhostmode.exe /sdcard");
				mount();
				String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
				if (mnt_stat.isEmpty()) {
					dialog.dismiss();
					mountfail();
				} else {
					ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox || true ");
					ShellUtils.fastCmd("cp /sdcard/usbhostmode.exe " + this.winpath + "/Toolbox");
					ShellUtils.fastCmd("rm /sdcard/usbhostmode.exe");
					messages.setText(this.getString(R.string.done));
					dismissButton.setVisibility(View.VISIBLE);
					dismissButton.setOnClickListener(v12 -> {
						this.HideBlur();
						dialog.dismiss();
					});
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});
		
		this.z.cvRotation.setOnClickListener(v -> {
			this.ShowBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(boot);
			messages.setText(this.getString(R.string.rotation_question));
			noButton.setText(this.getString(R.string.no));
			yesButton.setText(this.getString(R.string.yes));
			dismissButton.setText(this.getString(R.string.dismiss));
			noButton.setOnClickListener(v13 -> {
				this.HideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v14 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				messages.setText(this.getString(R.string.please_wait));
				ShellUtils.fastCmd("cp " + this.getFilesDir() + "/display.exe /sdcard/");
				ShellUtils.fastCmd("cp " + this.getFilesDir() + "/RotationShortcut.lnk /sdcard/");
				mount();
				String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
				if (mnt_stat.isEmpty()) {
					dialog.dismiss();
					mountfail();
				} else {
					ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox || true ");
					ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox/Rotation || true ");
					ShellUtils.fastCmd("cp /sdcard/display.exe " + this.winpath + "/Toolbox/Rotation");
					ShellUtils.fastCmd("cp /sdcard/RotationShortcut.lnk " + this.winpath + "/Toolbox/Rotation");
					ShellUtils.fastCmd("rm /sdcard/display.exe");
					ShellUtils.fastCmd("rm /sdcard/RotationShortcut.lnk");
					messages.setText(this.getString(R.string.done));
					dismissButton.setVisibility(View.VISIBLE);
					dismissButton.setOnClickListener(v15 -> {
					this.HideBlur();
					dialog.dismiss();
					});
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});

		this.z.cvSetup.setOnClickListener(v -> {
			this.ShowBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(mnt);
			messages.setText(this.getString(R.string.setup_question));
			noButton.setText(this.getString(R.string.no));
			yesButton.setText(this.getString(R.string.yes));
			dismissButton.setText(this.getString(R.string.dismiss));
			if (!MainActivity.isNetworkConnected(this)) {
				yesButton.setVisibility(View.GONE);
				noButton.setText(this.getString(R.string.dismiss));
				messages.setText(this.getString(R.string.internet));
			}
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
					messages.setText(getString(R.string.please_wait));
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
							ShellUtils.fastCmd("mkdir /sdcard/Frameworks || true");
							ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/install.bat /sdcard/Frameworks/install.bat");
							bar.setProgress((int) (bar.getMax() * 0.00), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/PhysX-9.13.0604-SystemSoftware-Legacy.msi -O /sdcard/Frameworks/PhysX-9.13.0604-SystemSoftware-Legacy.msi");
							bar.setProgress((int) (bar.getMax() * 0.1), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/PhysX_9.23.1019_SystemSoftware.exe -O /sdcard/Frameworks/PhysX_9.23.1019_SystemSoftware.exe");
							bar.setProgress((int) (bar.getMax() * 0.2), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/xnafx40_redist.msi -O /sdcard/Frameworks/xnafx40_redist.msi");
							bar.setProgress((int) (bar.getMax() * 0.25), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2005vcredist_x64.EXE -O /sdcard/Frameworks/2005vcredist_x64.EXE");
							bar.setProgress((int) (bar.getMax() * 0.3), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2005vcredist_x86.EXE -O /sdcard/Frameworks/2005vcredist_x86.EXE");
							bar.setProgress((int) (bar.getMax() * 0.35), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2008vcredist_x64.exe -O /sdcard/Frameworks/2008vcredist_x64.exe");
							bar.setProgress((int) (bar.getMax() * 0.4), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2008vcredist_x86.exe -O /sdcard/Frameworks/2008vcredist_x86.exe");
							bar.setProgress((int) (bar.getMax() * 0.45), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2010vcredist_x64.exe -O /sdcard/Frameworks/2010vcredist_x64.exe");
							bar.setProgress((int) (bar.getMax() * 0.5), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2010vcredist_x86.exe -O /sdcard/Frameworks/2010vcredist_x86.exe");
							bar.setProgress((int) (bar.getMax() * 0.55), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2012vcredist_x64.exe -O /sdcard/Frameworks/2012vcredist_x64.exe");
							bar.setProgress((int) (bar.getMax() * 0.6), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2012vcredist_x86.exe -O /sdcard/Frameworks/2012vcredist_x86.exe");
							bar.setProgress((int) (bar.getMax() * 0.65), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2013vcredist_x64.exe -O /sdcard/Frameworks/2013vcredist_x64.exe");
							bar.setProgress((int) (bar.getMax() * 0.7), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2013vcredist_x86.exe -O /sdcard/Frameworks/2013vcredist_x86.exe");
							bar.setProgress((int) (bar.getMax() * 0.75), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2015VC_redist.x64.exe -O /sdcard/Frameworks/2015VC_redist.x64.exe");
							bar.setProgress((int) (bar.getMax() * 0.8), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2015VC_redist.x86.exe -O /sdcard/Frameworks/2015VC_redist.x86.exe");
							bar.setProgress((int) (bar.getMax() * 0.85), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/2022VC_redist.arm64.exe -O /sdcard/Frameworks/2022VC_redist.arm64.exe");
							bar.setProgress((int) (bar.getMax() * 0.9), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/dxwebsetup.exe -O /sdcard/Frameworks/dxwebsetup.exe");
							bar.setProgress((int) (bar.getMax() * 0.95), true);
							ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/oalinst.exe -O /sdcard/Frameworks/oalinst.exe");
							bar.setProgress((int) (bar.getMax() * 1.00), true);
							MainActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									MainActivity.this.mount();
									String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
									Log.d("path", mnt_stat);
									if (mnt_stat.isEmpty()) {
										yesButton.setVisibility(View.VISIBLE);
										icons.setVisibility(View.GONE);
										messages.setText(MainActivity.this.getString(R.string.mountfail) + "\n" + (R.string.internalstorage));
									} else {
										icons.setImageDrawable(mnt);
										ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/Toolbox || true ");
										ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/Toolbox/Frameworks || true ");
										ShellUtils.fastCmd("cp /sdcard/Frameworks/* " + MainActivity.this.winpath + "/Toolbox/Frameworks");
										ShellUtils.fastCmd("rm -r /sdcard/Frameworks");
										bar.setVisibility(View.GONE);
										messages.setText(getString(R.string.done));
										dismissButton.setVisibility(View.VISIBLE);
										dismissButton.setOnClickListener(v19 -> {
											HideBlur();
											dialog.dismiss();
										});
									}
								}
							});
						}
					}).start();
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});

		this.z.cvDefender.setOnClickListener(v -> {
			this.ShowBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(edge);
			messages.setText(this.getString(R.string.defender_question));
			noButton.setText(this.getString(R.string.no));
			yesButton.setText(this.getString(R.string.yes));
			dismissButton.setText(this.getString(R.string.dismiss));
			noButton.setOnClickListener(v20 -> {
				this.HideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v21 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				if (!MainActivity.isNetworkConnected(this)) {
					dismissButton.setVisibility(View.VISIBLE);
					messages.setText(this.getString(R.string.internet));
				} else {
					messages.setText(this.getString(R.string.please_wait));
					ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://github.com/n00b69/woasetup/releases/download/Installers/DefenderRemover.exe -O /sdcard/DefenderRemover.exe");
					ShellUtils.fastCmd("cp " + this.getFilesDir() + "/RemoveEdge.bat /sdcard");
					mount();
					String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + this.win);
					if (mnt_stat.isEmpty()) {
						dialog.dismiss();
						mountfail();
					} else {
						ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox || true ");
						ShellUtils.fastCmd("cp /sdcard/DefenderRemover.exe " + this.winpath + "/Toolbox");
						ShellUtils.fastCmd("rm /sdcard/DefenderRemover.exe");
						ShellUtils.fastCmd("cp /sdcard/RemoveEdge.bat " + this.winpath + "/Toolbox");
						ShellUtils.fastCmd("rm /sdcard/RemoveEdge.bat");
						messages.setText(this.getString(R.string.done));
						dismissButton.setVisibility(View.VISIBLE);
						dismissButton.setOnClickListener(v22 -> {
							this.HideBlur();
							dialog.dismiss();
						});
					}
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
		this.k.backupQB.setChecked(pref.getBACKUP(this));
		this.k.backupQBA.setChecked(pref.getBACKUP_A(this));
		this.k.autobackup.setChecked(!pref.getAUTO(this));
		this.k.autobackupA.setChecked(!pref.getAUTO(this));
		this.k.confirmation.setChecked(pref.getCONFIRM(this));
		this.k.automount.setChecked(pref.getAutoMount(this));
		this.k.securelock.setChecked(!pref.getSecure(this));
		this.k.mountLocation.setChecked(pref.getMountLocation(this));
		this.k.appUpdate.setChecked(pref.getAppUpdate(this));
		this.k.toolbarlayout.settings.setVisibility(View.GONE);
		//k.language.setText(R.string.language);
		});

		this.k.mountLocation.setOnCheckedChangeListener((compoundButton, b) -> {
			pref.setMountLocation(this, b);
			this.winpath = (b ? "/mnt/Windows" : "/mnt/sdcard/Windows");
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
				icons.setVisibility(View.GONE);
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
				icons.setVisibility(View.GONE);
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

		this.x.cvInfo.setOnClickListener(v -> {
			this.ShowBlur();
			noButton.setVisibility(View.GONE);
			yesButton.setVisibility(View.GONE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.GONE);
			messages.setText(this.getString(R.string.please_wait));
			dismissButton.setText(this.getString(R.string.dismiss));
			if (!MainActivity.isNetworkConnected(this)) {
				dismissButton.setVisibility(View.VISIBLE);
				messages.setText(this.getString(R.string.internet));
			} else {
				String version = ShellUtils.fastCmd(getFilesDir()+"/busybox wget -q -O - https://raw.githubusercontent.com/n00b69/woa-helper-update/main/README.md");
				if (BuildConfig.VERSION_NAME.equals(version)) {
					dismissButton.setVisibility(View.VISIBLE);
					messages.setText(MainActivity.this.getString(R.string.no) + " " + MainActivity.this.getString(R.string.update1));
				} else {
					yesButton.setVisibility(View.VISIBLE);
					noButton.setVisibility(View.VISIBLE);
					dismissButton.setVisibility(View.GONE);
					messages.setText(getString(R.string.update1));
					yesButton.setText(getString(R.string.update));
					noButton.setText(getString(R.string.later));
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
							yesButton.setVisibility(View.GONE);
							noButton.setVisibility(View.GONE);
							messages.setText(MainActivity.this.getString(R.string.update2) + "\n" + MainActivity.this.getString(R.string.please_wait));
							update();
						}
					});
				}
			}
			dismissButton.setOnClickListener(v23 -> {
				this.HideBlur();
				dialog.dismiss();
			});
			dialog.setCancelable(false);
			dialog.show();
		});
			
		this.k.autobackup.setOnCheckedChangeListener((compoundButton, b) -> pref.setAUTO(this, !b));
		this.k.autobackupA.setOnCheckedChangeListener((compoundButton, b) -> pref.setAUTO_A(this, !b));
		this.k.confirmation.setOnCheckedChangeListener((compoundButton, b) -> pref.setCONFIRM(this, b));
		this.k.securelock.setOnCheckedChangeListener((compoundButton, b) -> pref.setSecure(this, !b));
		this.k.automount.setOnCheckedChangeListener((compoundButton, b) -> pref.setAutoMount(this, b));
		this.k.appUpdate.setOnCheckedChangeListener((compoundButton, b) -> pref.setAppUpdate(this, b));
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
	
	public void flash(String uefi) {
		ShellUtils.fastCmd("dd if=" + uefi + " of=/dev/block/bootdevice/by-name/boot$(getprop ro.boot.slot_suffix) bs=16M");
	}
	
	public void mount() {
		ShellUtils.fastCmd("mkdir " + this.winpath + " || true");
		ShellUtils.fastCmd("cd /data/data/id.kuato.woahelper/files");
		ShellUtils.fastCmd("su -mm -c ./mount.ntfs " + this.win + " " + this.winpath);
		this.mounted = this.getString(R.string.unmountt);
		this.x.tvMnt.setText(String.format(this.getString(R.string.mnt_title), this.mounted));
	}
	
	public void unmount() {
		ShellUtils.fastCmd("su -mm -c umount " + this.winpath);
		ShellUtils.fastCmd("rmdir " + this.winpath);
		this.mounted = this.getString(R.string.mountt);
		this.x.tvMnt.setText(String.format(this.getString(R.string.mnt_title), this.mounted));
	}
	
	public void winBackup() {
		mount();
		ShellUtils.fastCmd("dd bs=8M if=/dev/block/by-name/boot$(getprop ro.boot.slot_suffix) of=" + this.winpath + "/boot.img");
	}

	public void dump() {
		ShellUtils.fastCmd("su -c dd if=/dev/block/by-name/modemst1 of=$(find " + this.winpath + "/Windows/System32/DriverStore/FileRepository -name qcremotefs8150.inf_arm64_*)/bootmodem_fs1");
		ShellUtils.fastCmd("su -c dd if=/dev/block/by-name/modemst2 of=$(find " + this.winpath + "/Windows/System32/DriverStore/FileRepository -name qcremotefs8150.inf_arm64_*)/bootmodem_fs2");
	}

	public void checkRoot(){
		if (Boolean.FALSE.equals(Shell.isAppGrantedRoot())) {
			final Dialog dialog = new Dialog(this);
			dialog.setContentView(R.layout.dialog);
			dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
			TextView messages = dialog.findViewById(R.id.messages);
			messages.setText(this.getString(R.string.nonroot));
			dialog.show();
			dialog.setCancelable(false);
		} else {
			checkwin();
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
		String[] supported = {"a52sxq", "alpha_lao_com", "alphalm_lao_com", "alphaplus_lao_com", "alphalm", "alphaplus", "alioth", "andromeda", "betalm", "beta_lao_com", "betaplus_lao_com", "betalm_lao_com", "beryllium", "bhima", "cepheus", "cheeseburger", "chiron", "curtana2", "curtana", "curtana_india", "curtana_cn", "curtanacn", "cmi", "davinci", "dumpling", "dipper", "durandal", "durandal_india", "dm3q", "dm3", "enchilada", "equuleus", "excalibur", "excalibur_india", "e3q", "flashlmdd", "flash_lao_com", "flashlm", "flashlmdd_lao_com", "fajita", "guacamole", "guacamoleb", "gram", "hotdog", "hotdogb", "hotdogg", "houji", "husky", "joan", "joyeuse", "judyln", "judyp", "judypn", "lisa", "marble", "meizu20pro", "mh2lm", "mh2plus_lao_com", "mh2lm_lao_com", "mh2lm5g", "mh2lm5g_lao_com", "miatoll", "nabu", "OnePlus6", "OnePlus6T", "OnePlus7", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7T", "OnePlus7TPro", "OnePlus7TPro4G", "OnePlus7TPro5G", "OP7ProNRSpr", "OnePlus7TProNR", "pipa", "perseus", "polaris", "Pong", "pong", "q2q", "raphael", "raphaelin", "raphaels", "redfin", "RMX2170", "RMX2061", "sagit", "surya", "t860", "t865", "vayu", "venus", "winner", "winnerx", "xpeng", "G973F", "SM-G973F", "beyond1lte", "beyond1qlte", "G973U", "G973U1", "SM-G973U", "SM-G973U1", "G9730", "SM-G9730", "G973N", "SM-G973N", "G973X", "SM-G973X", "G973C", "SM-G973C", "SCV41", "SM-SC41", "beyond1"};
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
						checkRoot();
					}
				});
			}
			else
				checkRoot();
			this.device = "unknown";
		}
		String stram = MainActivity.extractNumberFromString(new RAM().getMemory(this.getApplicationContext()));
		this.ramvalue = Double.parseDouble(stram) / 100;
		String run = ShellUtils.fastCmd(" su -c cat /proc/cmdline ");
		if (run.isEmpty()) panel = "Unknown";
		else {
			if (run.contains("j20s_42") || run.contains("k82_42") || run.contains("42_02_0b")) {
				panel = "Huaxing";
			} else if (run.contains("j20s_36") || run.contains("k82_36") || run.contains("36_02_0a")) {
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
	
	public void checkdbkpmodel() {
		String dbkpmodel = ShellUtils.fastCmd("getprop ro.product.device");
		if ("guacamole".equals(dbkpmodel) || "OnePlus7Pro".equals(dbkpmodel) || "OnePlus7Pro4G".equals(dbkpmodel)) {
			this.dbkpmodel = "ONEPLUS 7 PRO";
		} else if ("hotdog".equals(dbkpmodel) || "OnePlus7TPro".equals(dbkpmodel) || "OnePlus7TPro4G".equals(dbkpmodel)) {
			this.dbkpmodel = "ONEPLUS 7T PRO";
		} else if ("cepheus".equals(dbkpmodel)) {
			this.dbkpmodel = "XIAOMI MI 9";
		} else if ("nabu".equals(dbkpmodel)) {
			this.dbkpmodel = "XIAOMI PAD 5";
		} else {
			this.dbkpmodel = "UNSUPPORTED";
		}
	}

	public void checkuefi() {
		ShellUtils.fastCmd("su -c mkdir /sdcard/UEFI");
		this.finduefi = "\""+ShellUtils.fastCmd(this.getString(R.string.uefiChk))+"\"";
		this.device = ShellUtils.fastCmd("getprop ro.product.device ");
		if (finduefi.contains("img")) {
			this.x.tvQuickBoot.setText(getString(R.string.quickboot_title));
			this.x.cvQuickBoot.setEnabled(true);
			this.x.tvQuickBoot.setText(getString(R.string.quickboot_title));
			this.x.tvBootSubtitle.setText(getString(R.string.quickboot_subtitle_nabu));
			this.n.tvFlashUefi.setText(this.getString(R.string.flash_uefi_title));
			this.n.tvUefiSubtitle.setText(this.getString(R.string.flash_uefi_subtitle));
			this.n.cvFlashUefi.setEnabled(true);
		} else {
			this.x.cvQuickBoot.setEnabled(false);
			this.x.tvQuickBoot.setText(getString(R.string.uefi_not_found));
			this.x.tvBootSubtitle.setText(this.getString(R.string.uefi_not_found_subtitle, this.device));
			this.n.tvFlashUefi.setText(this.getString(R.string.uefi_not_found));
			this.n.tvUefiSubtitle.setText(this.getString(R.string.uefi_not_found_subtitle, this.device));
			this.n.cvFlashUefi.setEnabled(false);
		}
	}

	public void checkwin() {
		String checkingforwin = ShellUtils.fastCmd("find /dev/block/by-name | grep win");
		if (checkingforwin.isEmpty()) {
			String checkingformindows = ShellUtils.fastCmd("find /dev/block/by-name | grep mindows");
			if (checkingformindows.isEmpty()) {
				final Dialog dialog = new Dialog(this);
				dialog.setContentView(R.layout.dialog);
				dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
				TextView messages = dialog.findViewById(R.id.messages);
				messages.setText(getString(R.string.partition));
				dialog.show();
				dialog.setCancelable(false);
				this.x.cvMnt.setEnabled(false);
				this.x.cvToolbox.setEnabled(false);
				this.x.cvQuickBoot.setEnabled(false);
			} else {
				this.x.cvMnt.setEnabled(true);
				this.x.cvToolbox.setEnabled(true);
				this.x.cvQuickBoot.setEnabled(true);
			}
		} else {
			this.x.cvMnt.setEnabled(true);
			this.x.cvToolbox.setEnabled(true);
			this.x.cvQuickBoot.setEnabled(true); 
		}
	}
	
	public void checkupdate() {
		if (!pref.getAppUpdate(this)) {
			if (!MainActivity.isNetworkConnected(this)) {
				checkwin();
			} else {
				if (!BuildConfig.VERSION_NAME.equals(ShellUtils.fastCmd(getFilesDir()+"/busybox wget -q -O - https://raw.githubusercontent.com/n00b69/woa-helper-update/main/README.md"))){
					final Dialog dialog = new Dialog(this);
					dialog.setContentView(R.layout.dialog);
					dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
					MaterialButton yesButton = dialog.findViewById(R.id.yes);
					MaterialButton noButton = dialog.findViewById(R.id.no);
					TextView messages = dialog.findViewById(R.id.messages);
					yesButton.setVisibility(View.VISIBLE);
					noButton.setVisibility(View.VISIBLE);
					messages.setText(getString(R.string.update1));
					yesButton.setText(getString(R.string.update));
					noButton.setText(getString(R.string.later));
					dialog.show();
					dialog.setCancelable(false);
					noButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
					yesButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							yesButton.setVisibility(View.GONE);
							noButton.setVisibility(View.GONE);
							messages.setText(MainActivity.this.getString(R.string.update2) + "\n" + MainActivity.this.getString(R.string.please_wait));
							update();
						}
					});
				}
			}
		}	
	}
	
	public void update() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				ShellUtils.fastCmd(MainActivity.this.getFilesDir() + "/busybox wget https://raw.githubusercontent.com/n00b69/woa-helper-update/main/woahelper.apk -O " + MainActivity.this.getFilesDir() + "/woahelper.apk");
				ShellUtils.fastCmd("pm install " + MainActivity.this.getFilesDir() + "/woahelper.apk && rm " + MainActivity.this.getFilesDir() + "/woahelper.apk");
			}
		}).start();
	}
	
	public void mountfail() {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		MaterialButton yesButton = dialog.findViewById(R.id.yes);
		MaterialButton dismissButton = dialog.findViewById(R.id.dismiss);
		TextView messages = dialog.findViewById(R.id.messages);
		yesButton.setVisibility(View.VISIBLE);
		dismissButton.setVisibility(View.VISIBLE);
		yesButton.setText(MainActivity.this.getString(R.string.chat));
		dismissButton.setText(MainActivity.this.getString(R.string.cancel));
		MainActivity.this.ShowBlur();
		messages.setText(getString(R.string.mountfail));
		dismissButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.this.HideBlur();
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
		if (Configuration.ORIENTATION_LANDSCAPE != newConfig.orientation && Objects.equals(this.device, "nabu") || Objects.equals(this.device, "pipa") || Objects.equals(this.device, "winner") || Objects.equals(this.device, "winnerx") || Objects.equals(this.device, "q2q") || Objects.equals(this.device, "t860") || Objects.equals(this.device, "t865")) {
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
		} else if (Configuration.ORIENTATION_PORTRAIT != newConfig.orientation && Objects.equals(this.device, "nabu") || Objects.equals(this.device, "pipa") || Objects.equals(this.device, "winner") || Objects.equals(this.device, "winnerx") || Objects.equals(this.device, "q2q") || Objects.equals(this.device, "t860") || Objects.equals(this.device, "t865")) {
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