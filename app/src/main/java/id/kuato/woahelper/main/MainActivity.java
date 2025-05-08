package id.kuato.woahelper.main;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.appcompat.content.res.AppCompatResources;
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
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import id.kuato.woahelper.BuildConfig;
import id.kuato.woahelper.R;
import id.kuato.woahelper.databinding.ActivityMainBinding;
import id.kuato.woahelper.databinding.ScriptsBinding;
import id.kuato.woahelper.databinding.SetPanelBinding;
import id.kuato.woahelper.databinding.ToolboxBinding;
import id.kuato.woahelper.preference.pref;
import id.kuato.woahelper.util.RAM;
import jp.wasabeef.blurry.Blurry;

public class MainActivity extends AppCompatActivity {

	private static final float SIZE = 12.0F;
	private static final float SIZE1 = 15.0F;
	private static final float SIZE2 = 30.0F;
	private static final float SIZE3 = 18.0F;
	public ActivityMainBinding x;
	public SetPanelBinding k;
	public ToolboxBinding n;
	public ScriptsBinding z;
	public String winpath;
	String panel;
	String mounted;
	String finduefi;
	String device;
	String model;
	String dbkpmodel;
	String win;
	String findboot;
	String boot;
	String grouplink = "https://t.me/woahelperchat";
	String guidelink = "https://github.com/n00b69";
	private double ramvalue;
	boolean unsupported = false;
    boolean blur = false;

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Network activeNetwork = connectivityManager.getActiveNetwork();
		NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
		return null != capabilities && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
	}

	private void copyAssets() {
		//ShellUtils.fastCmd(String.format("rm %s/*", this.getFilesDir()));
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
				byte[] buffer = new byte[1024];
				int read;
				while (-1 != (read = in.read(buffer))) {
					out.write(buffer, 0, read);
				}
				in.close();
				out.flush();
				out.close();
			} catch (IOException ignored) {
			}
		}
		ShellUtils.fastCmd("chmod 777 " + this.getFilesDir() + "/mount.ntfs");
		ShellUtils.fastCmd("chmod 777 " + this.getFilesDir() + "/libfuse-lite.so");
		ShellUtils.fastCmd("chmod 777 " + this.getFilesDir() + "/libntfs-3g.so");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		copyAssets();

		super.onCreate(savedInstanceState);
		final Dialog dialog = new Dialog(this);
		final Dialog languages = new Dialog(this);
		dialog.setContentView(R.layout.dialog);
		languages.setContentView(R.layout.langauges);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		languages.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		this.x = ActivityMainBinding.inflate(this.getLayoutInflater());
		this.setContentView(this.x.getRoot());
		this.x.toolbarlayout.settings.setVisibility(View.VISIBLE);
		this.k = SetPanelBinding.inflate(this.getLayoutInflater());
		this.n = ToolboxBinding.inflate(this.getLayoutInflater());
		this.z = ScriptsBinding.inflate(this.getLayoutInflater());
		Drawable iconToolbar = ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_launcher_foreground, null);
		this.setSupportActionBar(this.x.toolbarlayout.toolbar);
		this.x.toolbarlayout.toolbar.setTitle(this.getString(R.string.app_name));
		this.x.toolbarlayout.toolbar.setSubtitle("v" + BuildConfig.VERSION_NAME);
		this.x.toolbarlayout.toolbar.setNavigationIcon(iconToolbar);
		this.device = ShellUtils.fastCmd("getprop ro.product.device ");
		this.model = ShellUtils.fastCmd("getprop ro.product.model");
		this.win = getWin();
		this.boot = getBoot();
		this.winpath = getWinPath();
		if (isMounted()) mounted = getString(R.string.unmountt);
		else mounted = getString(R.string.mountt);
		this.x.tvMnt.setText(String.format(this.getString(R.string.mnt_title), this.mounted));
		this.x.tvDate.setText(String.format(this.getString(R.string.last), pref.getDATE(this)));
		MaterialButton yesButton = dialog.findViewById(R.id.yes);
		MaterialButton noButton = dialog.findViewById(R.id.no);
		MaterialButton dismissButton = dialog.findViewById(R.id.dismiss);
		TextView messages = dialog.findViewById(R.id.messages);
		ImageView icons = dialog.findViewById(R.id.icon);
		ProgressBar bar = dialog.findViewById(R.id.progress);
		Drawable android = AppCompatResources.getDrawable(this, R.drawable.android);
		Drawable atlasrevi = AppCompatResources.getDrawable(this, R.drawable.ic_ar_mainactivity);
		Drawable boot = AppCompatResources.getDrawable(this, R.drawable.ic_disk);
		Drawable download = AppCompatResources.getDrawable(this, R.drawable.ic_download);
		Drawable edge = AppCompatResources.getDrawable(this, R.drawable.edge2);
		Drawable icon = AppCompatResources.getDrawable(this, R.drawable.ic_launcher_foreground);
		Drawable modem = AppCompatResources.getDrawable(this, R.drawable.ic_modem);
		Drawable mnt = AppCompatResources.getDrawable(this, R.drawable.ic_mnt);
		Drawable sensors = AppCompatResources.getDrawable(this, R.drawable.ic_sensor);
		Drawable settings = AppCompatResources.getDrawable(this, R.drawable.settings);
		Drawable uefi = AppCompatResources.getDrawable(this, R.drawable.ic_uefi);
		Drawable back = AppCompatResources.getDrawable(this, R.drawable.back_arrow);
		settings.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(this.getColor(R.color.md_theme_primary), BlendModeCompat.SRC_IN));
		back.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(this.getColor(R.color.md_theme_primary), BlendModeCompat.SRC_IN));
		this.x.toolbarlayout.settings.setImageDrawable(settings);

		String slot = ShellUtils.fastCmd("getprop ro.boot.slot_suffix");
		if (slot.isEmpty()) this.x.tvSlot.setVisibility(View.GONE);
		else this.x.tvSlot.setText(String.format(this.getString(R.string.slot), slot.substring(1, 2).toUpperCase()));
		
		TextView myTextView00 = this.findViewById(R.id.tv_date);
		myTextView00.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
		Log.d("INFO", String.format("%s (%s)", model, device));
		this.x.deviceName.setText(String.format("%s (%s)", model, device));
		this.x.deviceName.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
		this.x.tvPanel.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
		this.x.tvRamvalue.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
		this.x.guideText.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
		this.x.groupText.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
		this.x.tvSlot.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
		if (Objects.equals(this.device, "nabu") || Objects.equals(this.device, "pipa") || Objects.equals(this.device, "winner") || Objects.equals(this.device, "winnerx") || Objects.equals(this.device, "q2q") || Objects.equals(this.device, "gts6l") || Objects.equals(this.device, "gts6lwifi")) {
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
			this.x.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE1);
		}
			switch (this.device) {
				// LG
				case "alphalm", "alphaplus", "alpha_lao_com", "alphalm_lao_com", "alphaplus_lao_com" -> {
					this.guidelink = "https://github.com/n00b69/woa-alphaplus";
					this.grouplink = "https://t.me/lgedevices";
					this.x.DeviceImage.setImageResource(R.drawable.alphaplus);
				}
				case "betalm", "betalm_lao_com" -> {
					this.guidelink = "https://github.com/n00b69/woa-betalm";
					this.grouplink = "https://t.me/lgedevices";
					this.x.DeviceImage.setImageResource(R.drawable.betalm);
				}
				case "flashlmdd", "flash_lao_com", "flashlm", "flashlmdd_lao_com" -> {
					this.guidelink = "https://github.com/n00b69/woa-flashlmdd";
					this.grouplink = "https://t.me/lgedevices";
					this.x.DeviceImage.setImageResource(R.drawable.flashlmdd);
				}
				case "mh2lm", "mh2lm_lao_com" -> {
					this.guidelink = "https://github.com/n00b69/woa-mh2lm";
					this.grouplink = "https://t.me/lgedevices";
					this.x.DeviceImage.setImageResource(R.drawable.mh2lm);
				}
				case "mh2lm5g", "mh2lm5g_lao_com" -> {
					this.guidelink = "https://github.com/n00b69/woa-mh2lm5g";
					this.grouplink = "https://t.me/lgedevices";
					this.x.DeviceImage.setImageResource(R.drawable.mh2lm);
				}
				case "judyln", "judyp", "judypn" -> {
					this.guidelink = "https://github.com/n00b69/woa-everything";
					this.grouplink = "https://t.me/lgedevices";
					this.x.DeviceImage.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.unknown, null));
				}
				case "joan" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/lgedevices";
					this.x.DeviceImage.setImageResource(R.drawable.unknown);
				}
				// Xiaomi
				case "andromeda" -> {
					this.guidelink = "https://project-aloha.github.io/";
					this.grouplink = "https://t.me/project_aloha_issues";
					this.x.DeviceImage.setImageResource(R.drawable.unknown);
				}
				case "beryllium" -> {
					this.guidelink = "https://github.com/n00b69/woa-beryllium";
					this.grouplink = "https://t.me/WinOnF1";
					this.x.DeviceImage.setImageResource(R.drawable.beryllium);
					this.x.tvPanel.setVisibility(View.VISIBLE);
				}
				case "bhima", "vayu" -> {
					this.guidelink = "https://github.com/woa-vayu/POCOX3Pro-Guides";
					this.grouplink = "https://t.me/windowsonvayu";
					this.x.DeviceImage.setImageResource(R.drawable.vayu);
					this.x.tvPanel.setVisibility(View.VISIBLE);
				}
				case "cepheus" -> {
					this.guidelink = "https://github.com/ivnvrvnn/Port-Windows-XiaoMI-9";
					this.grouplink = "http://t.me/woacepheus";
					this.x.DeviceImage.setImageResource(R.drawable.cepheus);
					this.x.tvPanel.setVisibility(View.VISIBLE);
					this.n.cvDbkp.setVisibility(View.VISIBLE);
					this.n.cvDumpModem.setVisibility(View.VISIBLE);
					this.n.cvFlashUefi.setVisibility(View.GONE);
				}
				case "chiron" -> {
					this.guidelink = "https://renegade-project.tech/";
					this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					this.x.DeviceImage.setImageResource(R.drawable.chiron);
				}
				case "curtana", "curtana2", "curtana_india", "curtana_cn", "curtanacn", "durandal", "durandal_india", "excalibur", "excalibur2", "excalibur_india", "gram", "joyeuse", "miatoll" -> {
					this.guidelink = "https://github.com/woa-miatoll/Port-Windows-11-Redmi-Note-9-Pro";
					this.grouplink = "http://t.me/woamiatoll";
					this.x.DeviceImage.setImageResource(R.drawable.miatoll);
					this.x.tvPanel.setVisibility(View.VISIBLE);
				}
				case "dipper" -> {
					this.guidelink = "https://github.com/n00b69/woa-dipper";
					this.grouplink = "https://t.me/woadipper";
					this.x.DeviceImage.setImageResource(R.drawable.dipper);
				}
				case "equuleus", "ursa" -> {
					this.guidelink = "https://github.com/n00b69/woa-equuleus";
					this.grouplink = "https://t.me/woaequuleus";
					this.x.DeviceImage.setImageResource(R.drawable.equuleus);
				}
				case "lisa" -> {
					this.guidelink = "https://github.com/n00b69/woa-lisa";
					this.grouplink = "https://t.me/woalisa";
					this.x.DeviceImage.setImageResource(R.drawable.lisa);
				}
				case "nabu" -> {
					this.guidelink = "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5";
					this.grouplink = "https://t.me/nabuwoa";
					this.x.NabuImage.setImageResource(R.drawable.nabu);
					this.x.tvPanel.setVisibility(View.VISIBLE);
					this.n.cvDbkp.setVisibility(View.VISIBLE);
					this.n.cvFlashUefi.setVisibility(View.GONE);
				}
				case "perseus" -> {
					this.guidelink = "https://github.com/n00b69/woa-perseus";
					this.grouplink = "https://t.me/woaperseus";
					this.x.DeviceImage.setImageResource(R.drawable.perseus);
				}
				case "pipa" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/xiaomi_pipa";
					this.x.NabuImage.setImageResource(R.drawable.pipa);
					this.x.tvPanel.setVisibility(View.VISIBLE);
					this.n.cvDbkp.setVisibility(View.VISIBLE);
					this.n.cvFlashUefi.setVisibility(View.GONE);
				}
				case "polaris" -> {
					this.guidelink = "https://github.com/n00b69/woa-polaris";
					this.grouplink = "https://t.me/WinOnMIX2S";
					this.x.DeviceImage.setImageResource(R.drawable.polaris);
					this.x.tvPanel.setVisibility(View.VISIBLE);
				}
				case "raphael", "raphaelin", "raphaels" -> {
					this.guidelink = "https://github.com/new-WoA-Raphael/woa-raphael";
					this.grouplink = "https://t.me/woaraphael";
					this.x.DeviceImage.setImageResource(R.drawable.raphael);
					this.x.tvPanel.setVisibility(View.VISIBLE);
					this.n.cvDumpModem.setVisibility(View.VISIBLE);
				}
				case "surya", "karna" -> {
					this.guidelink = "https://github.com/woa-surya/POCOX3NFC-Guides";
					this.grouplink = "https://t.me/windows_on_pocox3_nfc";
					this.x.DeviceImage.setImageResource(R.drawable.vayu);
					this.x.tvPanel.setVisibility(View.VISIBLE);
				}
				case "sagit" -> {
					this.guidelink = "https://renegade-project.tech/";
					this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					this.x.DeviceImage.setImageResource(R.drawable.unknown);
				}
				case "ingres" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://discord.gg/Dx2QgMx7Sv";
					this.x.DeviceImage.setImageResource(R.drawable.ingres);
				}
				case "vili", "lavender" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://discord.gg/Dx2QgMx7Sv";
					this.x.DeviceImage.setImageResource(R.drawable.unknown);
				}
				// OnePlus
				case "OnePlus5", "cheeseburger" -> {
					this.guidelink = "https://renegade-project.tech/";
					this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					this.x.DeviceImage.setImageResource(R.drawable.cheeseburger);
				}
				case "OnePlus5T", "dumpling" -> {
					this.guidelink = "https://renegade-project.tech/";
					this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					this.x.DeviceImage.setImageResource(R.drawable.dumpling);
				}
				case "OnePlus6", "fajita" -> {
					this.guidelink = "https://github.com/n00b69/woa-op6";
					this.grouplink = "https://t.me/WinOnOP6";
					this.x.DeviceImage.setImageResource(R.drawable.fajita);
				}
				case "OnePlus6T", "OnePlus6TSingle", "enchilada" -> {
					this.guidelink = "https://github.com/n00b69/woa-op6";
					this.grouplink = "https://t.me/WinOnOP6";
					this.x.DeviceImage.setImageResource(R.drawable.enchilada);
				}
				case "hotdog", "OnePlus7TPro", "OnePlus7TPro4G" -> {
					this.guidelink = "https://github.com/n00b69/woa-op7";
					this.grouplink = "https://t.me/onepluswoachat";
					this.x.DeviceImage.setImageResource(R.drawable.hotdog);
					this.n.cvDumpModem.setVisibility(View.VISIBLE);
					this.n.cvDbkp.setVisibility(View.VISIBLE);
					this.n.cvDevcfg.setVisibility(View.VISIBLE);
					this.n.cvFlashUefi.setVisibility(View.GONE);
				}
				case "guacamole", "guacamolet", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7ProTMO" -> {
					this.guidelink = "https://github.com/n00b69/woa-op7";
					this.grouplink = "https://t.me/onepluswoachat";
					this.x.DeviceImage.setImageResource(R.drawable.guacamole);
					this.n.cvDumpModem.setVisibility(View.VISIBLE);
					this.n.cvDbkp.setVisibility(View.VISIBLE);
					this.n.cvDevcfg.setVisibility(View.VISIBLE);
					this.n.cvFlashUefi.setVisibility(View.GONE);
				}
				case "guacamoleb", "hotdogb", "OnePlus7T", "OnePlus7" -> {
					this.guidelink = "https://project-aloha.github.io/";
					this.grouplink = "https://t.me/onepluswoachat";
					this.x.DeviceImage.setImageResource(R.drawable.unknown);
					this.n.cvDumpModem.setVisibility(View.VISIBLE);
				}
				case "OnePlus7TPro5G", "OnePlus7TProNR", "hotdogg" -> {
					this.guidelink = "https://project-aloha.github.io/";
					this.grouplink = "https://t.me/onepluswoachat";
					this.x.DeviceImage.setImageResource(R.drawable.hotdog);
				}
				case "OP7ProNRSpr", "OnePlus7ProNR", "guacamoleg", "guacamoles" -> {
					this.guidelink = "https://project-aloha.github.io/";
					this.grouplink = "https://t.me/onepluswoachat";
					this.x.DeviceImage.setImageResource(R.drawable.guacamole);
				}
				// Samsung
				case "a52sxq" -> {
					this.guidelink = "https://github.com/n00b69/woa-a52s";
					this.grouplink = "https://t.me/a52sxq_uefi";
					this.x.DeviceImage.setImageResource(R.drawable.a52sxq);
				}
				case "beyond1lte", "beyond1qlte", "beyond1" -> {
					this.guidelink = "https://github.com/sonic011gamer/Mu-Samsung";
					this.grouplink = "https://t.me/woahelperchat";
					this.x.DeviceImage.setImageResource(R.drawable.beyond1);
				}
				case "dm3q", "dm3" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/dumanthecat";
					this.x.DeviceImage.setImageResource(R.drawable.dm3q);
				}
				case "e3q" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/biskupmuf";
					this.x.DeviceImage.setImageResource(R.drawable.e3q);
				}
				case "gts6l", "gts6lwifi" -> {
					this.guidelink = "https://project-aloha.github.io/";
					this.grouplink = "https://t.me/project_aloha_issues";
					this.x.NabuImage.setImageResource(R.drawable.gts6l);
				}
				case "q2q" -> {
					this.guidelink = "https://project-aloha.github.io/";
					this.grouplink = "https://t.me/project_aloha_issues";
					this.x.NabuImage.setImageResource(R.drawable.q2q);
				}
				case "star2qlte", "star2qltechn", "r3q" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://discord.gg/Dx2QgMx7Sv";
					this.x.DeviceImage.setImageResource(R.drawable.unknown);
				}
				case "winnerx", "winner" -> {
					this.guidelink = "https://github.com/n00b69/woa-winner";
					this.grouplink = "https://t.me/project_aloha_issues";
					this.x.NabuImage.setImageResource(R.drawable.winner);
				}
				// Meme devices / devices with cat pictures / devices which will never have proper support lmao
				case "venus" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://discord.gg/Dx2QgMx7Sv";
					this.x.DeviceImage.setImageResource(R.drawable.venus);
				}
				case "alioth" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://discord.gg/Dx2QgMx7Sv";
					this.x.DeviceImage.setImageResource(R.drawable.alioth);
				}
				case "davinci" -> {
					this.guidelink = "https://github.com/zxcwsurx/woa-davinci";
					this.grouplink = "https://t.me/woa_davinci";
					this.x.DeviceImage.setImageResource(R.drawable.raphael);
				}
				case "marble" -> {
					this.guidelink = "https://github.com/Xhdsos/woa-marble";
					this.grouplink = "https://t.me/woa_marble";
					this.x.DeviceImage.setImageResource(R.drawable.marble);
				}
				case "Pong", "pong" -> {
					this.guidelink = "https://github.com/index986/woa-pong";
					this.grouplink = "https://t.me/WoA_spacewar_pong";
					this.x.DeviceImage.setImageResource(R.drawable.pong);
				}
				case "xpeng" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/woahelperchat";
					this.x.DeviceImage.setImageResource(R.drawable.xpeng);
				}
				case "RMX2061" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/realme6PROwindowsARM64";
					this.x.DeviceImage.setImageResource(R.drawable.rmx2061);
				}
				case "RMX2170" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/realme6PROwindowsARM64";
					this.x.DeviceImage.setImageResource(R.drawable.rmx2170);
				}
				case "cmi" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/dumanthecat";
					this.x.DeviceImage.setImageResource(R.drawable.cmi);
				}
				case "houji" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/dumanthecat";
					this.x.DeviceImage.setImageResource(R.drawable.houji);
				}
				case "meizu20pro", "meizu20Pro" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/dumanthecat";
					this.x.DeviceImage.setImageResource(R.drawable.meizu20pro);
				}
				case "husky" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/dumanthecat";
					this.x.DeviceImage.setImageResource(R.drawable.husky);
				}
				case "redfin", "herolte" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/dumanthecat";
					this.x.DeviceImage.setImageResource(R.drawable.redfin);
				}
				case "haotian" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/dumanthecat";
					this.x.DeviceImage.setImageResource(R.drawable.haotian);
				}
				case "Nord", "nord" -> {
					this.guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					this.grouplink = "https://t.me/dikeckaan";
					this.x.DeviceImage.setImageResource(R.drawable.nord);
				}
				case "nx729j", "NX729J" -> {
					this.guidelink = "https://github.com/Project-Silicium/Mu-Silicium";
					this.grouplink = "https://t.me/woahelperchat";
					this.x.DeviceImage.setImageResource(R.drawable.nx729j);
				}
				default -> {
					this.guidelink = "https://renegade-project.tech/";
					this.grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					this.x.DeviceImage.setImageResource(R.drawable.unknown);
					this.x.deviceName.setText(this.device);
					this.n.cvDumpModem.setVisibility(View.VISIBLE);
					this.unsupported = true;
				}
			}
		this.checkdevice();
		this.x.tvRamvalue.setText(String.format(getString(R.string.ramvalue), ramvalue));
		this.x.tvPanel.setText(String.format(getString(R.string.paneltype), panel));
		this.x.cvGuide.setOnClickListener(v -> {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(guidelink));
			startActivity(i);
		});

		this.x.cvGroup.setOnClickListener(v -> {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(grouplink));
			startActivity(i);
		});

		if (Boolean.FALSE.equals(Shell.isAppGrantedRoot())) {
			checkRoot();
		} else {
			checkupdate();
		}

		this.x.cvBackup.setOnClickListener(a -> {
			this.ShowBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.VISIBLE);
			noButton.setText(getString(R.string.android));
			yesButton.setText(getString(R.string.windows));
			dismissButton.setText(getString(R.string.no));
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(boot);
			messages.setText(getString(R.string.backup_boot_question));
			yesButton.setOnClickListener(v -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				dismissButton.setVisibility(View.GONE);
				messages.setText(getString(R.string.please_wait));
				new Handler().postDelayed(() -> {
					updateLastBackupDate();
					winBackup();
					messages.setText(getString(R.string.backuped));
					dismissButton.setText(R.string.dismiss);
					dismissButton.setVisibility(View.VISIBLE);
				}, 50);
			});
			dismissButton.setOnClickListener(v -> {
				HideBlur();
				dialog.dismiss();
			});
			noButton.setOnClickListener(v -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				dismissButton.setVisibility(View.GONE);
				messages.setText(getString(R.string.please_wait));
				new Handler().postDelayed(() -> {
					updateLastBackupDate();
					androidBackup();
					messages.setText(getString(R.string.backuped));
					dismissButton.setText(R.string.dismiss);
					dismissButton.setVisibility(View.VISIBLE);
				}, 25);
			});
			dialog.setCancelable(false);
			dialog.show();
		});
		
		this.x.cvMnt.setOnClickListener(a -> {
			this.ShowBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(mnt);
			noButton.setText(getString(R.string.no));
			yesButton.setText(getString(R.string.yes));
			dismissButton.setText(getString(R.string.dismiss));
			if (!isMounted()) messages.setText(String.format(getString(R.string.mount_question), winpath));
			else messages.setText(getString(R.string.unmount_question));
			yesButton.setOnClickListener(b -> {
				yesButton.setVisibility(View.GONE);
				noButton.setVisibility(View.GONE);
				messages.setText(getString(R.string.please_wait));
				new Handler().postDelayed(() -> {
						Log.d("debug", winpath);
						if (isMounted()) {
							unmount();
							messages.setText(getString(R.string.unmounted));
							dismissButton.setText(getString(R.string.dismiss));
							dismissButton.setVisibility(View.VISIBLE);
							return;
						}
						mount();
						if (isMounted()) {
							messages.setText(String.format("%s\n%s", getString(R.string.mounted), winpath));
							dismissButton.setText(getString(R.string.dismiss));
							dismissButton.setVisibility(View.VISIBLE);
							return;
						}
						noButton.setVisibility(View.GONE);
						yesButton.setText(getString(R.string.chat));
						dismissButton.setText(getString(R.string.cancel));
						yesButton.setVisibility(View.VISIBLE);
						dismissButton.setVisibility(View.VISIBLE);
						icons.setVisibility(View.GONE);
						ShowBlur();
						messages.setText(getString(R.string.mountfail));
						dismissButton.setOnClickListener(v -> {
							HideBlur();
							dialog.dismiss();
							icons.setVisibility(View.VISIBLE);
						});
						yesButton.setOnClickListener(v -> openGroupLink());
						dialog.setCancelable(false);

				}, 25);
			});
			dismissButton.setOnClickListener(v -> {
				MainActivity.this.HideBlur();
				dialog.dismiss();
			});
			noButton.setOnClickListener(v -> {
				MainActivity.this.HideBlur();
				dialog.dismiss();
			});
			dialog.setCancelable(false);
			dialog.show();
		});

		this.x.cvQuickBoot.setOnClickListener(a -> {
			this.ShowBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(icon);
			messages.setText(getString(R.string.quickboot_question));
			noButton.setText(getString(R.string.no));
			yesButton.setText(getString(R.string.yes));
			dismissButton.setText(getString(R.string.dismiss));
			if (pref.getDevcfg1(this) && !isNetworkConnected(this)) {
				dialog.dismiss();
				nointernet();
				return;
			}
			yesButton.setOnClickListener(b -> {
				yesButton.setVisibility(View.GONE);
				noButton.setVisibility(View.GONE);
				messages.setText(getString(R.string.please_wait));
				new Handler().postDelayed(() -> {
					mount();
					String found = ShellUtils.fastCmd(String.format("ls %s | grep boot.img", getWinPath()));
					if (pref.getBACKUP(this) || (!pref.getAUTO(this) && found.isEmpty())) {
						winBackup();
						updateLastBackupDate();
					}
					found = ShellUtils.fastCmd("find /sdcard | grep boot.img");
					if (pref.getBACKUP_A(this) || (!pref.getAUTO_A(this) && found.isEmpty())) {
						androidBackup();
						updateLastBackupDate();
					}
					if (pref.getDevcfg1(this)) {
						String find = ShellUtils.fastCmd(String.format("find %s -maxdepth 1 -name OOS11_devcfg_*", getFilesDir()));
						if (!find.isEmpty()) {
							ShellUtils.fastCmd("dd bs=8M if=" + find + " of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)");
							return;
						}
						find = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name original-devcfg.img");
						if (find.isEmpty()) {
							ShellUtils.fastCmd("dd bs=8M if=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix) of=" + getFilesDir() + "/original-devcfg.img");
							ShellUtils.fastCmd("cp " + getFilesDir() + "/original-devcfg.img /sdcard");
						}
						new Thread(()->{
							String bedan = ShellUtils.fastCmd("getprop ro.product.device");
							String devcfgDevice = "";
							if ("guacamole".equals(bedan) || "OnePlus7Pro".equals(bedan) || "OnePlus7Pro4G".equals(bedan)) devcfgDevice = "guacamole";
							else if ("hotdog".equals(bedan) || "OnePlus7TPro".equals(bedan) || "OnePlus7TPro4G".equals(bedan)) devcfgDevice = "hotdog";
							ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_%s.img -O /sdcard/OOS11_devcfg_%s.img\" | su -mm -c sh", devcfgDevice, devcfgDevice));
							ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_%s.img -O /sdcard/OOS12_devcfg_%s.img\" | su -mm -c sh", devcfgDevice, devcfgDevice));
							ShellUtils.fastCmd(String.format("cp /sdcard/OOS11_devcfg_%s.img %s", devcfgDevice, getFilesDir()));
							ShellUtils.fastCmd(String.format("cp /sdcard/OOS12_devcfg_%s.img %s", devcfgDevice, getFilesDir()));
							ShellUtils.fastCmd(String.format("dd bs=8M if=%s/OOS11_devcfg_%s.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)", getFilesDir(), devcfgDevice));
						}).start();
					}
					if (pref.getDevcfg2(this) && pref.getDevcfg1(this)) {
						ShellUtils.fastCmd("mkdir " + winpath + "/sta || true ");
						ShellUtils.fastCmd("cp '" + getFilesDir() + "/Flash Devcfg.lnk' " + winpath + "/Users/Public/Desktop");
						ShellUtils.fastCmd("cp " + getFilesDir() + "/sdd.exe " + winpath + "/sta/sdd.exe");
						ShellUtils.fastCmd("cp " + getFilesDir() + "/devcfg-boot-sdd.conf " + winpath + "/sta/sdd.conf");
						ShellUtils.fastCmd("cp " + getFilesDir() + "/original-devcfg.img " + winpath + "/original-devcfg.img");
					}
					flash(finduefi);
					ShellUtils.fastCmd("su -c svc power reboot");
					messages.setText(getString(R.string.wrong));
					dismissButton.setVisibility(View.VISIBLE);
				}, 25);
			});
			dismissButton.setOnClickListener(v -> {
				MainActivity.this.HideBlur();
				dialog.dismiss();
			});
			noButton.setOnClickListener(v -> {
				MainActivity.this.HideBlur();
				dialog.dismiss();
			});
			dialog.setCancelable(false);
			dialog.show();
		});

		this.x.cvToolbox.setOnClickListener(v -> {
			this.x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
			this.setContentView(this.n.getRoot());
			this.n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
			this.n.toolbarlayout.settings.setVisibility(View.GONE);
			this.n.toolbarlayout.toolbar.setTitle(getString(R.string.toolbox_title));
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
			noButton.setOnClickListener(v1 -> {
				this.HideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v2 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				ShellUtils.fastCmd("mkdir /sdcard/sta || true");
				ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sta.exe /sdcard/sta/sta.exe");
				ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sdd.exe /sdcard/sta/sdd.exe");
				ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sdd.conf /sdcard/sta/sdd.conf");
				ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/boot_img_auto-flasher_V1.1.exe /sdcard/sta/boot_img_auto-flasher_V1.1.exe");
				mount();
				String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
				if (mnt_stat.isEmpty()) {
					dialog.dismiss();
					mountfail();
				} else {
					ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/sta || true ");
					ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/ProgramData/sta || true ");
					ShellUtils.fastCmd("cp '" + MainActivity.this.getFilesDir() + "/Switch to Android.lnk' " + MainActivity.this.winpath + "/Users/Public/Desktop");
					ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sta.exe " + MainActivity.this.winpath + "/ProgramData/sta/sta.exe");
					ShellUtils.fastCmd("cp /sdcard/sta/* " + MainActivity.this.winpath + "/sta");
					ShellUtils.fastCmd("rm -r /sdcard/sta");
					messages.setText(this.getString(R.string.done));
					dismissButton.setVisibility(View.VISIBLE);
					dismissButton.setOnClickListener(v3 -> {
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
			if (!isNetworkConnected(this)) {
				dialog.dismiss();
				nointernet();
			} else {
				String bedan = ShellUtils.fastCmd("getprop ro.product.device");
				if ("nabu".equals(bedan)) {
					this.ShowBlur();
					noButton.setVisibility(View.VISIBLE);
					yesButton.setVisibility(View.VISIBLE);
					dismissButton.setVisibility(View.VISIBLE);
					icons.setVisibility(View.VISIBLE);
					icons.setImageDrawable(uefi);
					messages.setText(this.getString(R.string.dbkp_question, "MI PAD 5"));
					noButton.setText(this.getString(R.string.no));
					yesButton.setText(this.getString(R.string.nabu));
					dismissButton.setText(this.getString(R.string.nabu2));
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
								MainActivity.this.androidBackup();
								ShellUtils.fastCmd("rm /sdcard/original-boot.img || true");
								ShellUtils.fastCmd("cp /sdcard/boot.img /sdcard/dbkp/boot.img");
								ShellUtils.fastCmd("su -mm -c mv /sdcard/boot.img /sdcard/original-boot.img");
								ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp8150.cfg /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp\" | su -mm -c sh");
								ShellUtils.fastCmd("cp /sdcard/dbkp/dbkp "+getFilesDir());
								ShellUtils.fastCmd("chmod 777 " + MainActivity.this.getFilesDir() + "/dbkp");
								ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp.nabu.bin /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/releases/download/1.0/nabu.fd -O /sdcard/dbkp/nabu.fd\" | su -mm -c sh");
								ShellUtils.fastCmd("cd /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c " + MainActivity.this.getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/nabu.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.nabu.bin");
								ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
								ShellUtils.fastCmd("su -mm -c mv output kernel");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
								ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
								ShellUtils.fastCmd("rm -r /sdcard/dbkp");
								ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_a bs=16m");
								ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_b bs=16m");
								runOnUiThread(()->{
									String nabu = getString(R.string.nabu);
									messages.setText(String.format(getString(R.string.dbkp), nabu));
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
				dismissButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						noButton.setVisibility(View.GONE);
						yesButton.setVisibility(View.GONE);
						dismissButton.setVisibility(View.GONE);
						messages.setText(MainActivity.this.getString(R.string.please_wait));
						icons.setVisibility(View.VISIBLE);
						new Thread(()->{
							ShellUtils.fastCmd("mkdir /sdcard/dbkp || true");
							MainActivity.this.androidBackup();
							ShellUtils.fastCmd("rm /sdcard/original-boot.img || true");
							ShellUtils.fastCmd("cp /sdcard/boot.img /sdcard/dbkp/boot.img");
							ShellUtils.fastCmd("su -mm -c mv /sdcard/boot.img /sdcard/original-boot.img");
							ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp8150.cfg /sdcard/dbkp");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp\" | su -mm -c sh");
							ShellUtils.fastCmd("cp /sdcard/dbkp/dbkp "+getFilesDir());
							ShellUtils.fastCmd("chmod 777 " + MainActivity.this.getFilesDir() + "/dbkp");
							ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp.nabu2.bin /sdcard/dbkp");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/releases/download/1.0/nabuVolumebuttons.fd -O /sdcard/dbkp/nabuVolumebuttons.fd\" | su -mm -c sh");
							ShellUtils.fastCmd("cd /sdcard/dbkp");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
							ShellUtils.fastCmd("su -mm -c " + MainActivity.this.getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/nabuVolumebuttons.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.nabu2.bin");
							ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
							ShellUtils.fastCmd("su -mm -c mv output kernel");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
							ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
							ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
							ShellUtils.fastCmd("rm -r /sdcard/dbkp");
							ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_a bs=16m");
							ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_b bs=16m");
							runOnUiThread(()->{
								String nabu2 = getString(R.string.nabu2);
								messages.setText(String.format(getString(R.string.dbkp), nabu2));
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
			} else {
				this.ShowBlur();
				noButton.setVisibility(View.VISIBLE);
				yesButton.setVisibility(View.VISIBLE);
				dismissButton.setVisibility(View.GONE);
				icons.setVisibility(View.VISIBLE);
				icons.setImageDrawable(uefi);
				messages.setText(this.getString(R.string.dbkp_question, this.dbkpmodel));
				noButton.setText(this.getString(R.string.no));
				yesButton.setText(this.getString(R.string.yes));
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
							MainActivity.this.androidBackup();
							ShellUtils.fastCmd("rm /sdcard/original-boot.img || true");
							ShellUtils.fastCmd("cp /sdcard/boot.img /sdcard/dbkp/boot.img");
							ShellUtils.fastCmd("su -mm -c mv /sdcard/boot.img /sdcard/original-boot.img");
							ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp8150.cfg /sdcard/dbkp");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp\" | su -mm -c sh");
							ShellUtils.fastCmd("cp /sdcard/dbkp/dbkp "+getFilesDir());
							ShellUtils.fastCmd("chmod 777 " + MainActivity.this.getFilesDir() + "/dbkp");
							String bedan = ShellUtils.fastCmd("getprop ro.product.device");
							if ("guacamole".equals(bedan) || "OnePlus7Pro".equals(bedan) || "OnePlus7Pro4G".equals(bedan)) {
								ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp.hotdog.bin /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/DBKP/guacamole.fd -O /sdcard/dbkp/guacamole.fd\" | su -mm -c sh");
								ShellUtils.fastCmd("cd /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c " + MainActivity.this.getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/guacamole.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.hotdog.bin");
								ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
								ShellUtils.fastCmd("su -mm -c mv output kernel");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
								ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
								ShellUtils.fastCmd("rm -r /sdcard/dbkp");
								ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_a bs=16m");
								ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_b bs=16m");
							} else if ("hotdog".equals(bedan) || "OnePlus7TPro".equals(bedan) || "OnePlus7TPro4G".equals(bedan)) {
								ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp.hotdog.bin /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/DBKP/hotdog.fd -O /sdcard/dbkp/hotdog.fd\" | su -mm -c sh");
								ShellUtils.fastCmd("cd /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c " + MainActivity.this.getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/hotdog.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.hotdog.bin");
								ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
								ShellUtils.fastCmd("su -mm -c mv output kernel");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
								ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
								ShellUtils.fastCmd("rm -r /sdcard/dbkp");
								ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_a bs=16m");
								ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_b bs=16m");
							} else if ("cepheus".equals(bedan)) {
								ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp.cepheus.bin /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-everything/releases/download/Files/cepheus.fd -O /sdcard/dbkp/cepheus.fd\" | su -mm -c sh");
								ShellUtils.fastCmd("cd /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c " + MainActivity.this.getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/cepheus.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.cepheus.bin");
								ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
								ShellUtils.fastCmd("su -mm -c mv output kernel");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
								ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
								ShellUtils.fastCmd("rm -r /sdcard/dbkp");
								ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot bs=16m");
						//	} else if ("pipa".equals(bedan)) {
						//		ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/dbkp.pipa.bin /sdcard/dbkp");
						//		ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-everything/releases/download/Files/pipa.fd -O /sdcard/dbkp/pipa.fd\" | su -mm -c sh");
						//		ShellUtils.fastCmd("cd /sdcard/dbkp");
						//		ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
						//		ShellUtils.fastCmd("su -mm -c " + MainActivity.this.getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/pipa.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.pipa.bin");
						//		ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
						//		ShellUtils.fastCmd("su -mm -c mv output kernel");
						//		ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
						//		ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
						//		ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
						//		ShellUtils.fastCmd("rm -r /sdcard/dbkp");
						//		ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_a bs=16m");
						//		ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_b bs=16m");
							}
							runOnUiThread(()->{
								if ("guacamole".equals(bedan) || "OnePlus7Pro".equals(bedan) || "OnePlus7Pro4G".equals(bedan) || "hotdog".equals(bedan) || "OnePlus7TPro".equals(bedan) || "OnePlus7TPro4G".equals(bedan)) {
									String op7 = getString(R.string.op7);
									messages.setText(String.format(getString(R.string.dbkp), op7));
								} else if ("cepheus".equals(bedan)) {
									String cepheus = getString(R.string.cepheus);
									messages.setText(String.format(getString(R.string.dbkp), cepheus));
						//		} else if ("pipa".equals(bedan)) {
						//			String nabu = getString(R.string.nabu);
						//			messages.setText(String.format(getString(R.string.dbkp), nabu));
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
			}
			}
		});
		
		this.n.cvDevcfg.setOnClickListener(v -> {
			if (!isNetworkConnected(this)) {
				dialog.dismiss();
				nointernet();
			} else {
				this.ShowBlur();
				noButton.setVisibility(View.VISIBLE);
				yesButton.setVisibility(View.VISIBLE);
				dismissButton.setVisibility(View.GONE);
				icons.setVisibility(View.VISIBLE);
				icons.setImageDrawable(uefi);
				checkdbkpmodel();
				messages.setText(this.getString(R.string.devcfg_question, this.dbkpmodel));
				noButton.setText(this.getString(R.string.no));
				yesButton.setText(this.getString(R.string.yes));
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
							String findoriginaldevcfg = ShellUtils.fastCmd("find " + MainActivity.this.getFilesDir() + " -maxdepth 1 -name original-devcfg.img");
							if (findoriginaldevcfg.isEmpty()) {
								ShellUtils.fastCmd("dd bs=8M if=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix) of=" + MainActivity.this.getFilesDir() + "/original-devcfg.img");
							}
							ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/original-devcfg.img /sdcard");
							if (!isNetworkConnected(MainActivity.this)) {
								String finddevcfg = ShellUtils.fastCmd("find " + MainActivity.this.getFilesDir() + " -maxdepth 1 -name OOS11_devcfg_*");
								if (!finddevcfg.isEmpty()) {
									dialog.dismiss();
									nointernet();
								} else {
									String bedan = ShellUtils.fastCmd("getprop ro.product.device");
									if ("guacamole".equals(bedan) || "OnePlus7Pro".equals(bedan) || "OnePlus7Pro4G".equals(bedan)) {
										ShellUtils.fastCmd("dd bs=8M if=" + MainActivity.this.getFilesDir() + "/OOS11_devcfg_guacamole.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)");
									} else if ("hotdog".equals(bedan) || "OnePlus7TPro".equals(bedan) || "OnePlus7TPro4G".equals(bedan)) {
										ShellUtils.fastCmd("dd bs=8M if=" + MainActivity.this.getFilesDir() + "/OOS11_devcfg_hotdog.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)");
									}
								}
							} else {
								String bedan = ShellUtils.fastCmd("getprop ro.product.device");
								if ("guacamole".equals(bedan) || "OnePlus7Pro".equals(bedan) || "OnePlus7Pro4G".equals(bedan)) {
									ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_guacamole.img -O /sdcard/OOS11_devcfg_guacamole.img\" | su -mm -c sh");
									ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_guacamole.img -O /sdcard/OOS12_devcfg_guacamole.img\" | su -mm -c sh");
									ShellUtils.fastCmd("cp /sdcard/OOS11_devcfg_guacamole.img " + MainActivity.this.getFilesDir());
									ShellUtils.fastCmd("cp /sdcard/OOS12_devcfg_guacamole.img " + MainActivity.this.getFilesDir());
									ShellUtils.fastCmd("dd bs=8M if=" + MainActivity.this.getFilesDir() + "/OOS11_devcfg_guacamole.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)");
								} else if ("hotdog".equals(bedan) || "OnePlus7TPro".equals(bedan) || "OnePlus7TPro4G".equals(bedan)) {
									ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_hotdog.img -O /sdcard/OOS11_devcfg_hotdog.img\" | su -mm -c sh");
									ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_hotdog.img -O /sdcard/OOS12_devcfg_hotdog.img\" | su -mm -c sh");
									ShellUtils.fastCmd("cp /sdcard/OOS11_devcfg_hotdog.img " + MainActivity.this.getFilesDir());
									ShellUtils.fastCmd("cp /sdcard/OOS12_devcfg_hotdog.img " + MainActivity.this.getFilesDir());
									ShellUtils.fastCmd("dd bs=8M if=" + MainActivity.this.getFilesDir() + "/OOS11_devcfg_hotdog.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)");
								}
							}
							runOnUiThread(()->{
								mount();
								String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
								if (mnt_stat.isEmpty()) {
									ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sdd.exe /sdcard/sdd.exe");
									ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/devcfg-sdd.conf /sdcard/sdd.conf");
									dialog.dismiss();
									mountfail();
								} else {
									ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/sta || true ");
									ShellUtils.fastCmd("cp '" + MainActivity.this.getFilesDir() + "/Flash Devcfg.lnk' " + MainActivity.this.winpath + "/Users/Public/Desktop");
									ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/sdd.exe " + MainActivity.this.winpath + "/sta/sdd.exe");
									ShellUtils.fastCmd("cp " + MainActivity.this.getFilesDir() + "/devcfg-sdd.conf " + MainActivity.this.winpath + "/sta/sdd.conf");
									ShellUtils.fastCmd("cp /sdcard/original-devcfg.img " + MainActivity.this.winpath + "/original-devcfg.img");
								}
								messages.setText(getString(R.string.devcfg));
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
			}
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
			noButton.setOnClickListener(v4 -> {
				this.HideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v5 -> {
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
					dismissButton.setOnClickListener(v6 -> {
						this.HideBlur();
						dialog.dismiss();
					});
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});

		this.n.cvAtlasos.setOnClickListener(v -> {
			if (!isNetworkConnected(this)) {
				dialog.dismiss();
				nointernet();
			} else {
				this.ShowBlur();
				noButton.setVisibility(View.VISIBLE);
				yesButton.setVisibility(View.VISIBLE);
				dismissButton.setVisibility(View.VISIBLE);
				icons.setVisibility(View.VISIBLE);
				icons.setImageDrawable(atlasrevi);
				messages.setText(this.getString(R.string.atlasos_question));
				noButton.setText(this.getString(R.string.revios));
				yesButton.setText(this.getString(R.string.atlasos));
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
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/modified-playbooks/releases/download/ReviOS/ReviPlaybook.apbx -O /sdcard/ReviPlaybook.apbx\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.5), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://download.ameliorated.io/AME%20Wizard%20Beta.zip -O /sdcard/AMEWizardBeta.zip\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.8), true);
								MainActivity.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										MainActivity.this.mount();
										String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
										if (mnt_stat.isEmpty()) {
											dialog.dismiss();
											mountfail();
										} else {
											icons.setImageDrawable(atlasrevi);
											ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/Toolbox || true ");
											ShellUtils.fastCmd("cp /sdcard/ReviPlaybook.apbx " + MainActivity.this.winpath + "/Toolbox/ReviPlaybook.apbx");
											ShellUtils.fastCmd("cp /sdcard/AMEWizardBeta.zip " + MainActivity.this.winpath + "/Toolbox");
											ShellUtils.fastCmd("su -mm -c rm /sdcard/ReviPlaybook.apbx");
											ShellUtils.fastCmd("su -mm -c rm /sdcard/AMEWizardBeta.zip");
											bar.setVisibility(View.GONE);
											messages.setText(getString(R.string.done));
											dismissButton.setVisibility(View.VISIBLE);
											dismissButton.setOnClickListener(v7 -> {
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
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/modified-playbooks/releases/download/AtlasOS/AtlasPlaybook.apbx -O /sdcard/AtlasPlaybook_v0.4.1.apbx\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.35), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/modified-playbooks/releases/download/AtlasOS/AtlasPlaybook_v0.4.0_23H2Only.apbx -O /sdcard/AtlasPlaybook_v0.4.0_23H2Only.apbx\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.6), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://download.ameliorated.io/AME%20Wizard%20Beta.zip -O /sdcard/AMEWizardBeta.zip\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.8), true);
								MainActivity.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										MainActivity.this.mount();
										String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
										if (mnt_stat.isEmpty()) {
											dialog.dismiss();
											mountfail();
										} else {
											icons.setImageDrawable(atlasrevi);
											ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/Toolbox || true ");
											ShellUtils.fastCmd("cp /sdcard/AtlasPlaybook.apbx " + MainActivity.this.winpath + "/Toolbox/AtlasPlaybook_v0.4.1.apbx");
											ShellUtils.fastCmd("cp /sdcard/AtlasPlaybook.apbx " + MainActivity.this.winpath + "/Toolbox/AtlasPlaybook_v0.4.0_23H2Only.apbx");
											ShellUtils.fastCmd("cp /sdcard/AMEWizardBeta.zip " + MainActivity.this.winpath + "/Toolbox");
											ShellUtils.fastCmd("su -mm -c rm /sdcard/AtlasPlaybook_v0.4.1.apbx");
											ShellUtils.fastCmd("su -mm -c rm /sdcard/AtlasPlaybook_v0.4.0_23H2Only.apbx");
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
				dialog.setCancelable(false);
				dialog.show();
			}
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
			noButton.setOnClickListener(v9 -> {
				this.HideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v10 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				messages.setText(this.getString(R.string.please_wait));
				ShellUtils.fastCmd("cp " + this.getFilesDir() + "/usbhostmode.exe /sdcard");
				ShellUtils.fastCmd("cp '" + this.getFilesDir() + "/USB Host Mode.lnk' /sdcard");
				mount();
				String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
				if (mnt_stat.isEmpty()) {
					dialog.dismiss();
					mountfail();
				} else {
					ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox || true ");
					ShellUtils.fastCmd("cp /sdcard/usbhostmode.exe " + this.winpath + "/Toolbox");
					ShellUtils.fastCmd("cp '" + MainActivity.this.getFilesDir() + "/USB Host Mode.lnk' " + MainActivity.this.winpath + "/Users/Public/Desktop");
					ShellUtils.fastCmd("rm /sdcard/usbhostmode.exe");
					messages.setText(this.getString(R.string.done));
					dismissButton.setVisibility(View.VISIBLE);
					dismissButton.setOnClickListener(v11 -> {
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
			noButton.setOnClickListener(v12 -> {
				this.HideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v13 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				messages.setText(this.getString(R.string.please_wait));
				ShellUtils.fastCmd("cp " + this.getFilesDir() + "/QuickRotate_V3.0.exe /sdcard/");
				mount();
				String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
				if (mnt_stat.isEmpty()) {
					dialog.dismiss();
					mountfail();
				} else {
					ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox || true ");
					ShellUtils.fastCmd("cp /sdcard/QuickRotate_V3.0.exe " + this.winpath + "/Toolbox");
					ShellUtils.fastCmd("cp /sdcard/QuickRotate_V3.0.exe " + this.winpath + "/Users/Public/Desktop");
					ShellUtils.fastCmd("rm /sdcard/QuickRotate_V3.0.exe");
					messages.setText(this.getString(R.string.done));
					dismissButton.setVisibility(View.VISIBLE);
					dismissButton.setOnClickListener(v14 -> {
					this.HideBlur();
					dialog.dismiss();
					});
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});
		
		this.z.cvTablet.setOnClickListener(v -> {
			this.ShowBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(sensors);
			messages.setText(this.getString(R.string.tablet_question));
			noButton.setText(this.getString(R.string.no));
			yesButton.setText(this.getString(R.string.yes));
			dismissButton.setText(MainActivity.this.getString(R.string.dismiss));
			noButton.setOnClickListener(v16 -> {
				this.HideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v17 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				messages.setText(this.getString(R.string.please_wait));
				ShellUtils.fastCmd("cp " + this.getFilesDir() + "/Optimized_Taskbar_Control_V3.1.exe /sdcard/");
				mount();
				String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
				if (mnt_stat.isEmpty()) {
					dialog.dismiss();
					mountfail();
				} else {
					ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox || true ");
					ShellUtils.fastCmd("cp /sdcard/Optimized_Taskbar_Control_V3.1.exe " + this.winpath + "/Toolbox");
					ShellUtils.fastCmd("rm /sdcard/Optimized_Taskbar_Control_V3.1.exe");
					messages.setText(this.getString(R.string.done));
					dismissButton.setVisibility(View.VISIBLE);
					dismissButton.setOnClickListener(v18 -> {
					this.HideBlur();
					dialog.dismiss();
					});
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});

		this.z.cvSetup.setOnClickListener(v -> {
			if (!isNetworkConnected(this)) {
				dialog.dismiss();
				nointernet();
			} else {
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
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/PhysX-9.13.0604-SystemSoftware-Legacy.msi -O /sdcard/Frameworks/PhysX-9.13.0604-SystemSoftware-Legacy.msi\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.1), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/PhysX_9.23.1019_SystemSoftware.exe -O /sdcard/Frameworks/PhysX_9.23.1019_SystemSoftware.exe\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.2), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/xnafx40_redist.msi -O /sdcard/Frameworks/xnafx40_redist.msi\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.2), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/opengl.appx -O /sdcard/Frameworks/opengl.1.2409.2.0.appx\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.3), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2005vcredist_x64.EXE -O /sdcard/Frameworks/2005vcredist_x64.EXE\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.35), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2005vcredist_x86.EXE -O /sdcard/Frameworks/2005vcredist_x86.EXE\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.4), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2008vcredist_x64.exe -O /sdcard/Frameworks/2008vcredist_x64.exe\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.4), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2008vcredist_x86.exe -O /sdcard/Frameworks/2008vcredist_x86.exe\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.45), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2010vcredist_x64.exe -O /sdcard/Frameworks/2010vcredist_x64.exe\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.5), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2010vcredist_x86.exe -O /sdcard/Frameworks/2010vcredist_x86.exe\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.55), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2012vcredist_x64.exe -O /sdcard/Frameworks/2012vcredist_x64.exe\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.6), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2012vcredist_x86.exe -O /sdcard/Frameworks/2012vcredist_x86.exe\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.65), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2013vcredist_x64.exe -O /sdcard/Frameworks/2013vcredist_x64.exe\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.7), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2013vcredist_x86.exe -O /sdcard/Frameworks/2013vcredist_x86.exe\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.75), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2015VC_redist.x64.exe -O /sdcard/Frameworks/2015VC_redist.x64.exe\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.8), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2015VC_redist.x86.exe -O /sdcard/Frameworks/2015VC_redist.x86.exe\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.85), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2022VC_redist.arm64.exe -O /sdcard/Frameworks/2022VC_redist.arm64.exe\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.9), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/dxwebsetup.exe -O /sdcard/Frameworks/dxwebsetup.exe\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 0.95), true);
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/oalinst.exe -O /sdcard/Frameworks/oalinst.exe\" | su -mm -c sh");
								bar.setProgress((int) (bar.getMax() * 1.00), true);
								MainActivity.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										MainActivity.this.mount();
										String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
										Log.d("path", mnt_stat);
										if (mnt_stat.isEmpty()) {
											dialog.dismiss();
											mountfail();
										} else {
											icons.setImageDrawable(mnt);
											ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/Toolbox || true ");
											ShellUtils.fastCmd("mkdir " + MainActivity.this.winpath + "/Toolbox/Frameworks || true ");
											ShellUtils.fastCmd("cp /sdcard/Frameworks/* " + MainActivity.this.winpath + "/Toolbox/Frameworks");
											ShellUtils.fastCmd("rm -r /sdcard/Frameworks");
											bar.setVisibility(View.GONE);
											messages.setText(getString(R.string.done));
											dismissButton.setVisibility(View.VISIBLE);
											dismissButton.setOnClickListener(v15 -> {
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
			}
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
			noButton.setOnClickListener(v16 -> {
				this.HideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v17 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				messages.setText(this.getString(R.string.please_wait));
				String finddefenderremover = ShellUtils.fastCmd("find " + this.getFilesDir() + " -maxdepth 1 -name DefenderRemover.exe");
				if (finddefenderremover.isEmpty()) {
					if (!isNetworkConnected(MainActivity.this)) {
						dialog.dismiss();
						nointernet();
					} else {
						ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/DefenderRemover.exe -O /sdcard/DefenderRemover.exe\" | su -mm -c sh");
						ShellUtils.fastCmd("cp /sdcard/DefenderRemover.exe " + this.getFilesDir());
						ShellUtils.fastCmd("cp " + this.getFilesDir() + "/RemoveEdge.bat /sdcard");
						mount();
						String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
						if (mnt_stat.isEmpty()) {
							dialog.dismiss();
							mountfail();
						} else {
							ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox || true ");
							ShellUtils.fastCmd("cp " + this.getFilesDir() + "/DefenderRemover.exe " + this.winpath + "/Toolbox");
							ShellUtils.fastCmd("rm /sdcard/DefenderRemover.exe");
							ShellUtils.fastCmd("cp /sdcard/RemoveEdge.bat " + this.winpath + "/Toolbox");
							ShellUtils.fastCmd("rm /sdcard/RemoveEdge.bat");
							messages.setText(this.getString(R.string.done));
							dismissButton.setVisibility(View.VISIBLE);
							dismissButton.setOnClickListener(v18 -> {
								this.HideBlur();
								dialog.dismiss();
							});
						}
					}
				} else {
					ShellUtils.fastCmd("cp " + this.getFilesDir() + "/RemoveEdge.bat /sdcard");
					ShellUtils.fastCmd("cp " + this.getFilesDir() + "/DefenderRemover.exe /sdcard");
					mount();
					String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.this.win);
					if (mnt_stat.isEmpty()) {
						dialog.dismiss();
						mountfail();
					} else {
						ShellUtils.fastCmd("mkdir " + this.winpath + "/Toolbox || true ");
						ShellUtils.fastCmd("cp " + this.getFilesDir() + "/DefenderRemover.exe " + this.winpath + "/Toolbox");
						ShellUtils.fastCmd("cp " + this.getFilesDir() + "/RemoveEdge.bat " + this.winpath + "/Toolbox");
						ShellUtils.fastCmd("rm /sdcard/DefenderRemover.exe");
						ShellUtils.fastCmd("rm /sdcard/RemoveEdge.bat");
						messages.setText(this.getString(R.string.done));
						dismissButton.setVisibility(View.VISIBLE);
						dismissButton.setOnClickListener(v19 -> {
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
		this.k.devcfg1.setChecked(pref.getDevcfg1(this)&&this.k.devcfg1.getVisibility()==View.VISIBLE);
		this.k.devcfg2.setChecked(pref.getDevcfg2(this));
		this.k.toolbarlayout.settings.setVisibility(View.GONE);
		});
		
		this.k.mountLocation.setOnCheckedChangeListener((compoundButton, b) -> {
			pref.setMountLocation(this, b);
			this.winpath = (b ? "/mnt/Windows" : "/mnt/sdcard/Windows");
		});
		
		this.k.backupQB.setOnCheckedChangeListener((compoundButton, b) -> {
			if (pref.getBACKUP(this)) {
				pref.setBACKUP(this, false);
				k.autobackup.setVisibility(View.VISIBLE);
			} else {
				ShowBlur();
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
						HideBlur();
					}
				});
				dismissButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						k.backupQB.setChecked(false);
						dialog.dismiss();
						HideBlur();
					}
				});
			}
		});

		this.k.backupQBA.setOnCheckedChangeListener((compoundButton, b) -> {
			if (pref.getBACKUP_A(this)) {
				pref.setBACKUP_A(this, false);
				k.autobackupA.setVisibility(View.VISIBLE);
			} else {
				ShowBlur();
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
						HideBlur();
					}
				});
				dismissButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						k.backupQBA.setChecked(false);
						dialog.dismiss();
						HideBlur();
					}
				});
			}
		});
		


		this.x.cvInfo.setOnClickListener(v -> {
			if (id.kuato.woahelper.BuildConfig.BUILD_TYPE!="release")
				return;
			if (!isNetworkConnected(this)) {
				dialog.dismiss();
				nointernet();
			} else {
				this.ShowBlur();
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				dismissButton.setVisibility(View.GONE);
				icons.setVisibility(View.GONE);
				messages.setText(this.getString(R.string.please_wait));
				dismissButton.setText(this.getString(R.string.dismiss));
				String version = ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget -q -O - https://raw.githubusercontent.com/n00b69/woa-helper-update/main/README.md\" | su -mm -c sh");
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
				dismissButton.setOnClickListener(v20 -> {
					this.HideBlur();
					dialog.dismiss();
				});
				dialog.setCancelable(false);
				dialog.show();
			}
		});
			
		this.k.autobackup.setOnCheckedChangeListener((compoundButton, b) -> pref.setAUTO(this, !b));
		this.k.autobackupA.setOnCheckedChangeListener((compoundButton, b) -> pref.setAUTO_A(this, !b));
		this.k.confirmation.setOnCheckedChangeListener((compoundButton, b) -> pref.setCONFIRM(this, b));
		this.k.securelock.setOnCheckedChangeListener((compoundButton, b) -> pref.setSecure(this, !b));
		this.k.automount.setOnCheckedChangeListener((compoundButton, b) -> pref.setAutoMount(this, b));
		this.k.appUpdate.setOnCheckedChangeListener((compoundButton, b) -> pref.setAppUpdate(this, b));
		String op7funny = ShellUtils.fastCmd("getprop ro.product.device");
		if ("guacamole".equals(op7funny) || "guacamolet".equals(op7funny) || "OnePlus7Pro".equals(op7funny) || "OnePlus7Pro4G".equals(op7funny) || "OnePlus7ProTMO".equals(op7funny) || "hotdog".equals(op7funny) || "OnePlus7TPro".equals(op7funny) || "OnePlus7TPro4G".equals(op7funny)) {
			this.k.devcfg1.setOnCheckedChangeListener((compoundButton, b) -> {pref.setDevcfg1(this, b);if(b)k.devcfg2.setVisibility(View.VISIBLE); else k.devcfg2.setVisibility(View.GONE);pref.setDevcfg2(this, false);});
			this.k.devcfg2.setOnCheckedChangeListener((compoundButton, b) -> pref.setDevcfg2(this, b));
		} else {
			this.k.devcfg1.setVisibility(View.GONE);
			this.k.devcfg2.setVisibility(View.GONE);
			pref.setDevcfg1(this, false);
			pref.setDevcfg2(this, false);
		}
		this.k.button.setOnClickListener(v -> {
			this.k.settingsPanel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_out));
			this.setContentView(this.x.getRoot());
			this.x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_in));
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		this.checkwin();
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
		ShellUtils.fastCmd("dd if=" + uefi + " of=/dev/block/bootdevice/by-name/boot$(getprop ro.boot.slot_suffix) bs=16m");
	}
	
	public void mount() {
		ShellUtils.fastCmd("mkdir " + this.winpath + " || true");
		ShellUtils.fastCmd("cd " + MainActivity.this.getFilesDir());
		ShellUtils.fastCmd("su -mm -c ./mount.ntfs " + this.win + " " + this.winpath);
		String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + this.win);
		if (mnt_stat.isEmpty()) this.k.mountLocation.setChecked(!pref.getMountLocation(this));
			ShellUtils.fastCmd("mkdir " + this.winpath + " || true");
			ShellUtils.fastCmd("cd " + MainActivity.this.getFilesDir());
			ShellUtils.fastCmd("su -mm -c ./mount.ntfs " + this.win + " " + this.winpath);
			String mnt_stat2 = ShellUtils.fastCmd("su -mm -c mount | grep " + this.win);
			if (!mnt_stat2.isEmpty()) {
				this.mounted = this.getString(R.string.unmountt);
				this.x.tvMnt.setText(String.format(this.getString(R.string.mnt_title), this.mounted));
			}
	}
	
	public void unmount() {
		ShellUtils.fastCmd("su -mm -c umount " + this.winpath);
		ShellUtils.fastCmd("rmdir " + this.winpath);
		this.mounted = this.getString(R.string.mountt);
		this.x.tvMnt.setText(String.format(this.getString(R.string.mnt_title), this.mounted));
	}
	
	public void winBackup() {
		mount();
		ShellUtils.fastCmd("su -mm -c dd bs=8m if=" + this.boot + " of=" + this.winpath + "/boot.img");
	}
	
	public void androidBackup() {
		ShellUtils.fastCmd("su -mm -c dd bs=8m if=" + this.boot + " of=/sdcard/boot.img");
	}

	public void dump() {
		ShellUtils.fastCmd("su -mm -c dd if=/dev/block/by-name/modemst1 of=$(find " + this.winpath + "/Windows/System32/DriverStore/FileRepository -name qcremotefs8150.inf_arm64_*)/bootmodem_fs1");
		ShellUtils.fastCmd("su -mm -c dd if=/dev/block/by-name/modemst2 of=$(find " + this.winpath + "/Windows/System32/DriverStore/FileRepository -name qcremotefs8150.inf_arm64_*)/bootmodem_fs2");
	}

	public void checkRoot(){
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		TextView messages = dialog.findViewById(R.id.messages);
		messages.setText(this.getString(R.string.nonroot));
		dialog.show();
		dialog.setCancelable(false);
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
		if (this.unsupported) {
			this.device = "unknown";
			if (!pref.getAGREE(this)) {
				messages.setText(this.getString(R.string.unsupported));
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
		}
		this.ramvalue = Double.parseDouble(new RAM().getMemory(this));
		String run = ShellUtils.fastCmd(" su -c cat /proc/cmdline ");
		if (run.isEmpty()) panel = "Unknown";
		else {
			if (run.contains("j20s_42_02_0b") || run.contains("k82_42") || run.contains("ft8756_huaxing") || run.contains("huaxing")) {
				panel = "Huaxing";
			} else if (run.contains("j20s_36_02_0a") || run.contains("k82_36") || run.contains("nt36675_tianma") || run.contains("tianma_fhd_nt36672a") || run.contains("tianma")) {
				panel = "Tianma";
			} else if (run.contains("ebbg_fhd_ft8719")) {
				panel = "EBBG";
			} else if (run.contains("fhd_ea8076_global")) {
				panel = "global";
			} else if (run.contains("fhd_ea8076_f1mp_cmd")) {
				panel = "f1mp";
			} else if (run.contains("fhd_ea8076_f1p2_cmd")) {
				panel = "f1p2";
			} else if (run.contains("fhd_ea8076_f1p2_2")) {
				panel = "f1p2_2";
			} else if (run.contains("fhd_ea8076_f1_cmd")) {
				panel = "f1";
			} else if (run.contains("fhd_ea8076_cmd")) {
				panel = "ea8076_cmd";
			} else {
				panel = ShellUtils.fastCmd("su -c cat /proc/cmdline | tr ' :=' '\n'|grep dsi|tr ' _' '\n'|tail -3|head -1 ");
			}
		}
		if (!pref.getAGREE(this)) if ((Objects.equals(panel, "f1p2_2") || Objects.equals(panel, "f1"))) {
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
	
	public void checkdbkpmodel() {
		String dbkpmodel = ShellUtils.fastCmd("getprop ro.product.device");
		if ("guacamole".equals(dbkpmodel) || "guacamolet".equals(dbkpmodel) || "OnePlus7Pro".equals(dbkpmodel) || "OnePlus7Pro4G".equals(dbkpmodel) || "OnePlus7ProTMO".equals(dbkpmodel)) {
			this.dbkpmodel = "ONEPLUS 7 PRO";
		} else if ("hotdog".equals(dbkpmodel) || "OnePlus7TPro".equals(dbkpmodel) || "OnePlus7TPro4G".equals(dbkpmodel)) {
			this.dbkpmodel = "ONEPLUS 7T PRO";
		} else if ("cepheus".equals(dbkpmodel)) {
			this.dbkpmodel = "XIAOMI MI 9";
			this.k.devcfg1.setVisibility(View.GONE);
			this.k.devcfg2.setVisibility(View.GONE);
		} else if ("nabu".equals(dbkpmodel)) {
			this.dbkpmodel = "XIAOMI PAD 5";
			this.k.devcfg1.setVisibility(View.GONE);
			this.k.devcfg2.setVisibility(View.GONE);
		} else if ("pipa".equals(dbkpmodel)) {
			this.dbkpmodel = "XIAOMI PAD 6";
			this.k.devcfg1.setVisibility(View.GONE);
			this.k.devcfg2.setVisibility(View.GONE);
		} else {
			this.dbkpmodel = "UNSUPPORTED";
			this.k.devcfg1.setVisibility(View.GONE);
			this.k.devcfg2.setVisibility(View.GONE);
		}
	}
	public void checkuefi() {
		ShellUtils.fastCmd("su -c mkdir /sdcard/UEFI");
		this.finduefi = "\""+ShellUtils.fastCmd(this.getString(R.string.uefiChk))+"\"";
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
		if (this.win.isEmpty()) {
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
			this.n.cvFlashUefi.setEnabled(false);
		}
	}

	public void checkupdate() {
		if (!pref.getAppUpdate(this)) {
			if (!MainActivity.isNetworkConnected(this)) {
				checkdbkpmodel();
			} else {
				String version = ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget -q -O - https://raw.githubusercontent.com/n00b69/woa-helper-update/main/README.md\" | su -mm -c sh");
				if (BuildConfig.VERSION_NAME.equals(version)) {
					checkdbkpmodel();
				} else {
					checkdbkpmodel();
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
		} else {
			checkdbkpmodel();
		}
	}
	
	public void update() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://raw.githubusercontent.com/n00b69/woa-helper-update/main/woahelper.apk -O %s\" | su -mm -c sh", MainActivity.this.getFilesDir() + "/woahelper.apk"));
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
		messages.setText(MainActivity.this.getString(R.string.mountfail) + "\n" + MainActivity.this.getString(R.string.internalstorage));
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
	
	public void nointernet() {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		MaterialButton dismissButton = dialog.findViewById(R.id.dismiss);
		TextView messages = dialog.findViewById(R.id.messages);
		dismissButton.setVisibility(View.VISIBLE);
		dismissButton.setText(MainActivity.this.getString(R.string.dismiss));
		MainActivity.this.ShowBlur();
		messages.setText(MainActivity.this.getString(R.string.internet));
		dismissButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.this.HideBlur();
				dialog.dismiss();
			}
		});
		dialog.setCancelable(false);
		dialog.show();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (getTargetViewGroup().getId() != R.id.mainlayout)
			return;
		TextView myTextView1 = this.findViewById(R.id.text);
		TextView myTextView2 = this.findViewById(R.id.deviceName);
		TextView myTextView3 = this.findViewById(R.id.tv_panel);
		TextView myTextView4 = this.findViewById(R.id.tv_ramvalue);
		TextView myTextView5 = this.findViewById(R.id.guide_text);
		TextView myTextView6 = this.findViewById(R.id.group_text);
		TextView myTextView7 = this.findViewById(R.id.tv_slot);
		TextView myTextView8 = this.findViewById(R.id.tv_date);
		if (Configuration.ORIENTATION_LANDSCAPE != newConfig.orientation && Objects.equals(this.device, "nabu") || Objects.equals(this.device, "pipa") || Objects.equals(this.device, "winner") || Objects.equals(this.device, "winnerx") || Objects.equals(this.device, "q2q") || Objects.equals(this.device, "gts6l") || Objects.equals(this.device, "gts6lwifi")) {
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
		} else if (Configuration.ORIENTATION_PORTRAIT != newConfig.orientation && Objects.equals(this.device, "nabu") || Objects.equals(this.device, "pipa") || Objects.equals(this.device, "winner") || Objects.equals(this.device, "winnerx") || Objects.equals(this.device, "q2q") || Objects.equals(this.device, "gts6l") || Objects.equals(this.device, "gts6lwifi")) {
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
		Log.d("INFO", String.valueOf(blur));
		if (blur) return;
		blur = true;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
		Blurry.with(this).radius(10).sampling(1).onto(getTargetViewGroup());
	}

	public void HideBlur() {
		Blurry.delete(getTargetViewGroup());
		blur = false;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	ViewGroup getTargetViewGroup() {
		return (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
	}

	public void updateLastBackupDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
		String currentDateAndTime = sdf.format(new Date());
		pref.setDATE(this, currentDateAndTime);
		this.x.tvDate.setText(String.format(getString(R.string.last), pref.getDATE(this)));
	}

	public static String getWin() {
		String partition = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1");
		return ShellUtils.fastCmd("realpath " + partition);
	}
	public String getWinPath() {
		winpath = pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows";
		return winpath;
	}

	public static String getBoot() {
		String partition = ShellUtils.fastCmd("find /dev/block | grep boot$(getprop ro.boot.slot_suffix) | grep -E \"boot|BOOT\" | head -1");
		return ShellUtils.fastCmd("realpath " + partition);
	}
	public static Boolean isMounted() {
		return !ShellUtils.fastCmd("su -mm -c mount | grep " + getWin()).isEmpty();
	}
	public void openGroupLink() {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse("https://t.me/woahelperchat"));
		startActivity(i);
	}
}
