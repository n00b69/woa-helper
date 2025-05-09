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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import id.kuato.woahelper.BuildConfig;
import id.kuato.woahelper.R;
import id.kuato.woahelper.databinding.ActivityMainBinding;
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
	public SetPanelBinding k;
	public ToolboxBinding n;
	public ScriptsBinding z;
	String winpath, panel, mounted, finduefi, device, model, dbkpmodel, win, boot;
	String grouplink = "https://t.me/woahelperchat";
	String guidelink = "https://github.com/n00b69";
	boolean unsupported = false;
    boolean blur = false;

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Network activeNetwork = connectivityManager.getActiveNetwork();
		NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
		return null != capabilities && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
	}

	private void copyAssets() {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try { files = assetManager.list(""); } catch (IOException ignored) { }
		assert null != files;
		for (String filename : files) {
			@Nullable final InputStream in;
			@Nullable final OutputStream out;
			try {
				in = assetManager.open(filename);
				String outDir = String.valueOf(getFilesDir());
				File outFile = new File(outDir, filename);
				out = new FileOutputStream(outFile);
				byte[] buffer = new byte[1024];
				int read;
				while (-1 != (read = in.read(buffer)))
					out.write(buffer, 0, read);
				in.close();
				out.flush();
				out.close();
			} catch (IOException ignored) { }
		}
		ShellUtils.fastCmd("chmod 777 " + getFilesDir() + "/mount.ntfs");
		ShellUtils.fastCmd("chmod 777 " + getFilesDir() + "/libfuse-lite.so");
		ShellUtils.fastCmd("chmod 777 " + getFilesDir() + "/libntfs-3g.so");
	}

	@SuppressLint("UseCompatLoadingForDrawables")
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		copyAssets();

		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		MaterialButton yesButton = dialog.findViewById(R.id.yes);
		MaterialButton noButton = dialog.findViewById(R.id.no);
		MaterialButton dismissButton = dialog.findViewById(R.id.dismiss);
		TextView messages = dialog.findViewById(R.id.messages);
		ImageView icons = dialog.findViewById(R.id.icon);
		ProgressBar bar = dialog.findViewById(R.id.progress);
		x = ActivityMainBinding.inflate(getLayoutInflater());
		k = SetPanelBinding.inflate(getLayoutInflater());
		n = ToolboxBinding.inflate(getLayoutInflater());
		z = ScriptsBinding.inflate(getLayoutInflater());

		setContentView(x.getRoot());

		setSupportActionBar(x.toolbarlayout.toolbar);
		x.toolbarlayout.toolbar.setTitle(R.string.app_name);
		x.toolbarlayout.toolbar.setSubtitle("v" + BuildConfig.VERSION_NAME);
		x.toolbarlayout.toolbar.setNavigationIcon(R.drawable.ic_launcher_foreground);
		x.toolbarlayout.settings.setImageResource(R.drawable.settings);
		x.toolbarlayout.settings.setColorFilter(R.color.md_theme_primary);

		device = ShellUtils.fastCmd("getprop ro.product.device");
		model = ShellUtils.fastCmd("getprop ro.product.model");
		win = getWin();
		boot = getBoot();
		updateWinPath();
		updateMountText();
		x.tvDate.setText(String.format(getString(R.string.last), pref.getDATE(this)));

		Drawable android = getDrawable(R.drawable.android);
		Drawable atlasrevi = getDrawable(R.drawable.ic_ar_mainactivity);
		Drawable boot = getDrawable(R.drawable.ic_disk);
		Drawable download = getDrawable(R.drawable.ic_download);
		Drawable edge = getDrawable(R.drawable.edge2);
		Drawable icon = getDrawable(R.drawable.ic_launcher_foreground);
		Drawable modem = getDrawable(R.drawable.ic_modem);
		Drawable mnt = getDrawable(R.drawable.ic_mnt);
		Drawable sensors = getDrawable(R.drawable.ic_sensor);
		Drawable uefi = getDrawable(R.drawable.ic_uefi);

		String slot = ShellUtils.fastCmd("getprop ro.boot.slot_suffix");
		if (slot.isEmpty()) x.tvSlot.setVisibility(View.GONE);
		else x.tvSlot.setText(getString(R.string.slot, slot.substring(1, 2)).toUpperCase());

		x.deviceName.setText(String.format("%s (%s)", model, device));
		x.deviceName.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
		x.tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
		x.tvPanel.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
		x.tvRamvalue.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
		x.guideText.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
		x.groupText.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
		x.tvSlot.setTextSize(TypedValue.COMPLEX_UNIT_SP, SIZE);
		if (Objects.equals(device, "nabu") || Objects.equals(device, "pipa") || Objects.equals(device, "winner") || Objects.equals(device, "winnerx") || Objects.equals(device, "q2q") || Objects.equals(device, "gts6l") || Objects.equals(device, "gts6lwifi")) {
			x.NabuImage.setVisibility(View.VISIBLE);
			x.DeviceImage.setVisibility(View.GONE);
			int orientation = getResources().getConfiguration().orientation;
			if (Configuration.ORIENTATION_PORTRAIT == orientation) {
				x.app.setOrientation(LinearLayout.VERTICAL);
				x.top.setOrientation(LinearLayout.HORIZONTAL);
			} else {
				x.app.setOrientation(LinearLayout.HORIZONTAL);
				x.top.setOrientation(LinearLayout.VERTICAL);
			}
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			x.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE1);
		}
			switch (device) {
				// LG
				case "alphalm", "alphaplus", "alpha_lao_com", "alphalm_lao_com", "alphaplus_lao_com" -> {
					guidelink = "https://github.com/n00b69/woa-alphaplus";
					grouplink = "https://t.me/lgedevices";
					x.DeviceImage.setImageResource(R.drawable.alphaplus);
				}
				case "betalm", "betalm_lao_com" -> {
					guidelink = "https://github.com/n00b69/woa-betalm";
					grouplink = "https://t.me/lgedevices";
					x.DeviceImage.setImageResource(R.drawable.betalm);
				}
				case "flashlmdd", "flash_lao_com", "flashlm", "flashlmdd_lao_com" -> {
					guidelink = "https://github.com/n00b69/woa-flashlmdd";
					grouplink = "https://t.me/lgedevices";
					x.DeviceImage.setImageResource(R.drawable.flashlmdd);
				}
				case "mh2lm", "mh2lm_lao_com" -> {
					guidelink = "https://github.com/n00b69/woa-mh2lm";
					grouplink = "https://t.me/lgedevices";
					x.DeviceImage.setImageResource(R.drawable.mh2lm);
				}
				case "mh2lm5g", "mh2lm5g_lao_com" -> {
					guidelink = "https://github.com/n00b69/woa-mh2lm5g";
					grouplink = "https://t.me/lgedevices";
					x.DeviceImage.setImageResource(R.drawable.mh2lm);
				}
				case "judyln", "judyp", "judypn" -> {
					guidelink = "https://github.com/n00b69/woa-everything";
					grouplink = "https://t.me/lgedevices";
					x.DeviceImage.setImageResource(R.drawable.unknown);
				}
				case "joan" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://t.me/lgedevices";
					x.DeviceImage.setImageResource(R.drawable.unknown);
				}
				// Xiaomi
				case "andromeda" -> {
					guidelink = "https://project-aloha.github.io/";
					grouplink = "https://t.me/project_aloha_issues";
					x.DeviceImage.setImageResource(R.drawable.unknown);
				}
				case "beryllium" -> {
					guidelink = "https://github.com/n00b69/woa-beryllium";
					grouplink = "https://t.me/WinOnF1";
					x.DeviceImage.setImageResource(R.drawable.beryllium);
					x.tvPanel.setVisibility(View.VISIBLE);
				}
				case "bhima", "vayu" -> {
					guidelink = "https://github.com/woa-vayu/POCOX3Pro-Guides";
					grouplink = "https://t.me/windowsonvayu";
					x.DeviceImage.setImageResource(R.drawable.vayu);
					x.tvPanel.setVisibility(View.VISIBLE);
				}
				case "cepheus" -> {
					guidelink = "https://github.com/ivnvrvnn/Port-Windows-XiaoMI-9";
					grouplink = "http://t.me/woacepheus";
					x.DeviceImage.setImageResource(R.drawable.cepheus);
					x.tvPanel.setVisibility(View.VISIBLE);
					n.cvDbkp.setVisibility(View.VISIBLE);
					n.cvDumpModem.setVisibility(View.VISIBLE);
					n.cvFlashUefi.setVisibility(View.GONE);
				}
				case "chiron" -> {
					guidelink = "https://renegade-project.tech/";
					grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					x.DeviceImage.setImageResource(R.drawable.chiron);
				}
				case "curtana", "curtana2", "curtana_india", "curtana_cn", "curtanacn", "durandal", "durandal_india", "excalibur", "excalibur2", "excalibur_india", "gram", "joyeuse", "miatoll" -> {
					guidelink = "https://github.com/woa-miatoll/Port-Windows-11-Redmi-Note-9-Pro";
					grouplink = "http://t.me/woamiatoll";
					x.DeviceImage.setImageResource(R.drawable.miatoll);
					x.tvPanel.setVisibility(View.VISIBLE);
				}
				case "dipper" -> {
					guidelink = "https://github.com/n00b69/woa-dipper";
					grouplink = "https://t.me/woadipper";
					x.DeviceImage.setImageResource(R.drawable.dipper);
				}
				case "equuleus", "ursa" -> {
					guidelink = "https://github.com/n00b69/woa-equuleus";
					grouplink = "https://t.me/woaequuleus";
					x.DeviceImage.setImageResource(R.drawable.equuleus);
				}
				case "lisa" -> {
					guidelink = "https://github.com/n00b69/woa-lisa";
					grouplink = "https://t.me/woalisa";
					x.DeviceImage.setImageResource(R.drawable.lisa);
				}
				case "nabu" -> {
					guidelink = "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5";
					grouplink = "https://t.me/nabuwoa";
					x.NabuImage.setImageResource(R.drawable.nabu);
					x.tvPanel.setVisibility(View.VISIBLE);
					n.cvDbkp.setVisibility(View.VISIBLE);
					n.cvFlashUefi.setVisibility(View.GONE);
				}
				case "perseus" -> {
					guidelink = "https://github.com/n00b69/woa-perseus";
					grouplink = "https://t.me/woaperseus";
					x.DeviceImage.setImageResource(R.drawable.perseus);
				}
				case "pipa" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://t.me/xiaomi_pipa";
					x.NabuImage.setImageResource(R.drawable.pipa);
					x.tvPanel.setVisibility(View.VISIBLE);
					n.cvDbkp.setVisibility(View.VISIBLE);
					n.cvFlashUefi.setVisibility(View.GONE);
				}
				case "polaris" -> {
					guidelink = "https://github.com/n00b69/woa-polaris";
					grouplink = "https://t.me/WinOnMIX2S";
					x.DeviceImage.setImageResource(R.drawable.polaris);
					x.tvPanel.setVisibility(View.VISIBLE);
				}
				case "raphael", "raphaelin", "raphaels" -> {
					guidelink = "https://github.com/new-WoA-Raphael/woa-raphael";
					grouplink = "https://t.me/woaraphael";
					x.DeviceImage.setImageResource(R.drawable.raphael);
					x.tvPanel.setVisibility(View.VISIBLE);
					n.cvDumpModem.setVisibility(View.VISIBLE);
				}
				case "surya", "karna" -> {
					guidelink = "https://github.com/woa-surya/POCOX3NFC-Guides";
					grouplink = "https://t.me/windows_on_pocox3_nfc";
					x.DeviceImage.setImageResource(R.drawable.vayu);
					x.tvPanel.setVisibility(View.VISIBLE);
				}
				case "sagit" -> {
					guidelink = "https://renegade-project.tech/";
					grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					x.DeviceImage.setImageResource(R.drawable.unknown);
				}
				case "ingres" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://discord.gg/Dx2QgMx7Sv";
					x.DeviceImage.setImageResource(R.drawable.ingres);
				}
				case "vili", "lavender" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://discord.gg/Dx2QgMx7Sv";
					x.DeviceImage.setImageResource(R.drawable.unknown);
				}
				// OnePlus
				case "OnePlus5", "cheeseburger" -> {
					guidelink = "https://renegade-project.tech/";
					grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					x.DeviceImage.setImageResource(R.drawable.cheeseburger);
				}
				case "OnePlus5T", "dumpling" -> {
					guidelink = "https://renegade-project.tech/";
					grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					x.DeviceImage.setImageResource(R.drawable.dumpling);
				}
				case "OnePlus6", "fajita" -> {
					guidelink = "https://github.com/n00b69/woa-op6";
					grouplink = "https://t.me/WinOnOP6";
					x.DeviceImage.setImageResource(R.drawable.fajita);
				}
				case "OnePlus6T", "OnePlus6TSingle", "enchilada" -> {
					guidelink = "https://github.com/n00b69/woa-op6";
					grouplink = "https://t.me/WinOnOP6";
					x.DeviceImage.setImageResource(R.drawable.enchilada);
				}
				case "hotdog", "OnePlus7TPro", "OnePlus7TPro4G" -> {
					guidelink = "https://github.com/n00b69/woa-op7";
					grouplink = "https://t.me/onepluswoachat";
					x.DeviceImage.setImageResource(R.drawable.hotdog);
					n.cvDumpModem.setVisibility(View.VISIBLE);
					n.cvDbkp.setVisibility(View.VISIBLE);
					n.cvDevcfg.setVisibility(View.VISIBLE);
					n.cvFlashUefi.setVisibility(View.GONE);
				}
				case "guacamole", "guacamolet", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7ProTMO" -> {
					guidelink = "https://github.com/n00b69/woa-op7";
					grouplink = "https://t.me/onepluswoachat";
					x.DeviceImage.setImageResource(R.drawable.guacamole);
					n.cvDumpModem.setVisibility(View.VISIBLE);
					n.cvDbkp.setVisibility(View.VISIBLE);
					n.cvDevcfg.setVisibility(View.VISIBLE);
					n.cvFlashUefi.setVisibility(View.GONE);
				}
				case "guacamoleb", "hotdogb", "OnePlus7T", "OnePlus7" -> {
					guidelink = "https://project-aloha.github.io/";
					grouplink = "https://t.me/onepluswoachat";
					x.DeviceImage.setImageResource(R.drawable.unknown);
					n.cvDumpModem.setVisibility(View.VISIBLE);
				}
				case "OnePlus7TPro5G", "OnePlus7TProNR", "hotdogg" -> {
					guidelink = "https://project-aloha.github.io/";
					grouplink = "https://t.me/onepluswoachat";
					x.DeviceImage.setImageResource(R.drawable.hotdog);
				}
				case "OP7ProNRSpr", "OnePlus7ProNR", "guacamoleg", "guacamoles" -> {
					guidelink = "https://project-aloha.github.io/";
					grouplink = "https://t.me/onepluswoachat";
					x.DeviceImage.setImageResource(R.drawable.guacamole);
				}
				// Samsung
				case "a52sxq" -> {
					guidelink = "https://github.com/n00b69/woa-a52s";
					grouplink = "https://t.me/a52sxq_uefi";
					x.DeviceImage.setImageResource(R.drawable.a52sxq);
				}
				case "beyond1lte", "beyond1qlte", "beyond1" -> {
					guidelink = "https://github.com/sonic011gamer/Mu-Samsung";
					grouplink = "https://t.me/woahelperchat";
					x.DeviceImage.setImageResource(R.drawable.beyond1);
				}
				case "dm3q", "dm3" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://t.me/dumanthecat";
					x.DeviceImage.setImageResource(R.drawable.dm3q);
				}
				case "e3q" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://t.me/biskupmuf";
					x.DeviceImage.setImageResource(R.drawable.e3q);
				}
				case "gts6l", "gts6lwifi" -> {
					guidelink = "https://project-aloha.github.io/";
					grouplink = "https://t.me/project_aloha_issues";
					x.NabuImage.setImageResource(R.drawable.gts6l);
				}
				case "q2q" -> {
					guidelink = "https://project-aloha.github.io/";
					grouplink = "https://t.me/project_aloha_issues";
					x.NabuImage.setImageResource(R.drawable.q2q);
				}
				case "star2qlte", "star2qltechn", "r3q" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://discord.gg/Dx2QgMx7Sv";
					x.DeviceImage.setImageResource(R.drawable.unknown);
				}
				case "winnerx", "winner" -> {
					guidelink = "https://github.com/n00b69/woa-winner";
					grouplink = "https://t.me/project_aloha_issues";
					x.NabuImage.setImageResource(R.drawable.winner);
				}
				// Meme devices / devices with cat pictures / devices which will never have proper support lmao
				case "venus" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://discord.gg/Dx2QgMx7Sv";
					x.DeviceImage.setImageResource(R.drawable.venus);
				}
				case "alioth" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://discord.gg/Dx2QgMx7Sv";
					x.DeviceImage.setImageResource(R.drawable.alioth);
				}
				case "davinci" -> {
					guidelink = "https://github.com/zxcwsurx/woa-davinci";
					grouplink = "https://t.me/woa_davinci";
					x.DeviceImage.setImageResource(R.drawable.raphael);
				}
				case "marble" -> {
					guidelink = "https://github.com/Xhdsos/woa-marble";
					grouplink = "https://t.me/woa_marble";
					x.DeviceImage.setImageResource(R.drawable.marble);
				}
				case "Pong", "pong" -> {
					guidelink = "https://github.com/index986/woa-pong";
					grouplink = "https://t.me/WoA_spacewar_pong";
					x.DeviceImage.setImageResource(R.drawable.pong);
				}
				case "xpeng" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://t.me/woahelperchat";
					x.DeviceImage.setImageResource(R.drawable.xpeng);
				}
				case "RMX2061" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://t.me/realme6PROwindowsARM64";
					x.DeviceImage.setImageResource(R.drawable.rmx2061);
				}
				case "RMX2170" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://t.me/realme6PROwindowsARM64";
					x.DeviceImage.setImageResource(R.drawable.rmx2170);
				}
				case "cmi" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://t.me/dumanthecat";
					x.DeviceImage.setImageResource(R.drawable.cmi);
				}
				case "houji" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://t.me/dumanthecat";
					x.DeviceImage.setImageResource(R.drawable.houji);
				}
				case "meizu20pro", "meizu20Pro" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://t.me/dumanthecat";
					x.DeviceImage.setImageResource(R.drawable.meizu20pro);
				}
				case "husky" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://t.me/dumanthecat";
					x.DeviceImage.setImageResource(R.drawable.husky);
				}
				case "redfin", "herolte" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://t.me/dumanthecat";
					x.DeviceImage.setImageResource(R.drawable.redfin);
				}
				case "haotian" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://t.me/dumanthecat";
					x.DeviceImage.setImageResource(R.drawable.haotian);
				}
				case "Nord", "nord" -> {
					guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md";
					grouplink = "https://t.me/dikeckaan";
					x.DeviceImage.setImageResource(R.drawable.nord);
				}
				case "nx729j", "NX729J" -> {
					guidelink = "https://github.com/Project-Silicium/Mu-Silicium";
					grouplink = "https://t.me/woahelperchat";
					x.DeviceImage.setImageResource(R.drawable.nx729j);
				}
				default -> {
					guidelink = "https://renegade-project.tech/";
					grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA";
					x.DeviceImage.setImageResource(R.drawable.unknown);
					x.deviceName.setText(device);
					n.cvDumpModem.setVisibility(View.VISIBLE);
					unsupported = true;
				}
			}
		if (unsupported && !pref.getAGREE(this))	 {
			dialog.setCancelable(false);
			messages.setText(R.string.unsupported);
			yesButton.setVisibility(View.VISIBLE);
			yesButton.setText(R.string.sure);
			yesButton.setOnClickListener(v -> {
				dialog.dismiss();
				pref.setAGREE(this, true);
			});
			dialog.show();
		}

		String run = ShellUtils.fastCmd(" su -c cat /proc/cmdline ");
		Map<String, List<String>> panelMap = Map.of(
				"Huaxing", Arrays.asList("j20s_42_02_0b", "k82_42", "ft8756_huaxing", "huaxing"),
				"Tianma", Arrays.asList("j20s_36_02_0a", "k82_36", "nt36675_tianma", "tianma_fhd_nt36672a", "tianma"),
				"EBBG", List.of("ebbg_fhd_ft8719"),
				"global", List.of("fhd_ea8076_global"),
				"f1mp", List.of("fhd_ea8076_f1mp_cmd"),
				"f1p2", List.of("fhd_ea8076_f1p2_cmd"),
				"f1p2_2", List.of("fhd_ea8076_f1p2_2"),
				"f1", List.of("fhd_ea8076_f1_cmd"),
				"ea8076_cmd", List.of("fhd_ea8076_cmd")
		);

		for (Map.Entry<String, List<String>> entry : panelMap.entrySet()) {
			for (String keyword : entry.getValue()) {
				if (!run.contains(keyword)) continue;
				panel = entry.getKey();
				break;
			}
			if (panel != null) break;
		}
		if (panel == null) {
			panel = ShellUtils.fastCmd("su -c cat /proc/cmdline | tr ' :=' '\n'|grep dsi|tr ' _' '\n'|tail -3|head -1 ");
		}
		if (!pref.getAGREE(this) || (Objects.equals(panel, "f1p2_2") || Objects.equals(panel, "f1"))) {
			messages.setText(R.string.upanel);
			yesButton.setText(R.string.chat);
			dismissButton.setText(R.string.nah);
			noButton.setText(R.string.later);
			noButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			yesButton.setOnClickListener(v -> {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(grouplink));
				startActivity(i);
				dialog.dismiss();
				pref.setAGREE(this, true);
			});
			noButton.setOnClickListener(v -> { dialog.dismiss(); });
			dismissButton.setOnClickListener(v -> {
				dialog.dismiss();
				pref.setAGREE(this, true);
			});
			dialog.show();
		}

		x.tvRamvalue.setText(getString(R.string.ramvalue, Double.parseDouble(new RAM().getMemory(this))));
		x.tvPanel.setText(getString(R.string.paneltype, panel));
		x.cvGuide.setOnClickListener(v -> {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(guidelink));
			startActivity(i);
		});

		x.cvGroup.setOnClickListener(v -> {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(grouplink));
			startActivity(i);
		});

		if (Boolean.FALSE.equals(Shell.isAppGrantedRoot())) {
			checkRoot();
		} else {
			checkupdate();
		}

		x.cvBackup.setOnClickListener(a -> {
			showBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.VISIBLE);
			noButton.setText(R.string.android);
			yesButton.setText(R.string.windows);
			dismissButton.setText(R.string.no);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(boot);
			messages.setText(R.string.backup_boot_question);
			yesButton.setOnClickListener(v -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				dismissButton.setVisibility(View.GONE);
				messages.setText(R.string.please_wait);
				new Handler().postDelayed(() -> {
					updateLastBackupDate();
					winBackup();
					messages.setText(R.string.backuped);
					dismissButton.setText(R.string.dismiss);
					dismissButton.setVisibility(View.VISIBLE);
				}, 50);
			});
			dismissButton.setOnClickListener(v -> {
				hideBlur();
				dialog.dismiss();
			});
			noButton.setOnClickListener(v -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				dismissButton.setVisibility(View.GONE);
				messages.setText(R.string.please_wait);
				new Handler().postDelayed(() -> {
					updateLastBackupDate();
					androidBackup();
					messages.setText(R.string.backuped);
					dismissButton.setText(R.string.dismiss);
					dismissButton.setVisibility(View.VISIBLE);
				}, 25);
			});
			dialog.setCancelable(false);
			dialog.show();
		});
		
		x.cvMnt.setOnClickListener(a -> {
			showBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(mnt);
			noButton.setText(R.string.no);
			yesButton.setText(R.string.yes);
			dismissButton.setText(R.string.dismiss);
			if (!isMounted()) messages.setText(String.format(getString(R.string.mount_question), winpath));
			else messages.setText(R.string.unmount_question);
			yesButton.setOnClickListener(b -> {
				yesButton.setVisibility(View.GONE);
				noButton.setVisibility(View.GONE);
				messages.setText(R.string.please_wait);
				new Handler().postDelayed(() -> {
						Log.d("debug", winpath);
						if (isMounted()) {
							unmount();
							messages.setText(R.string.unmounted);
							dismissButton.setText(R.string.dismiss);
							dismissButton.setVisibility(View.VISIBLE);
							return;
						}
						mount();
						if (isMounted()) {
							messages.setText(String.format("%s\n%s", getString(R.string.mounted), winpath));
							dismissButton.setText(R.string.dismiss);
							dismissButton.setVisibility(View.VISIBLE);
							return;
						}
						noButton.setVisibility(View.GONE);
						yesButton.setText(R.string.chat);
						dismissButton.setText(R.string.cancel);
						yesButton.setVisibility(View.VISIBLE);
						dismissButton.setVisibility(View.VISIBLE);
						icons.setVisibility(View.GONE);
						showBlur();
						messages.setText(R.string.mountfail);
						dismissButton.setOnClickListener(v -> {
							hideBlur();
							dialog.dismiss();
							icons.setVisibility(View.VISIBLE);
						});
						yesButton.setOnClickListener(v -> openGroupLink());
						dialog.setCancelable(false);

				}, 25);
			});
			dismissButton.setOnClickListener(v -> {
				hideBlur();
				dialog.dismiss();
			});
			noButton.setOnClickListener(v -> {
				hideBlur();
				dialog.dismiss();
			});
			dialog.setCancelable(false);
			dialog.show();
		});

		x.cvQuickBoot.setOnClickListener(a -> {
			showBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(icon);
			messages.setText(R.string.quickboot_question);
			noButton.setText(R.string.no);
			yesButton.setText(R.string.yes);
			dismissButton.setText(R.string.dismiss);
			if (pref.getDevcfg1(this) && !isNetworkConnected(this)) {
				dialog.dismiss();
				nointernet();
				return;
			}
			yesButton.setOnClickListener(b -> {
				yesButton.setVisibility(View.GONE);
				noButton.setVisibility(View.GONE);
				messages.setText(R.string.please_wait);
				new Handler().postDelayed(() -> {
					mount();
					String found = ShellUtils.fastCmd(String.format("ls %s | grep boot.img", updateWinPath()));
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
					messages.setText(R.string.wrong);
					dismissButton.setVisibility(View.VISIBLE);
				}, 25);
			});
			dismissButton.setOnClickListener(v -> {
				hideBlur();
				dialog.dismiss();
			});
			noButton.setOnClickListener(v -> {
				hideBlur();
				dialog.dismiss();
			});
			dialog.setCancelable(false);
			dialog.show();
		});

		x.cvToolbox.setOnClickListener(v -> {
			x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
			setContentView(n.getRoot());
			n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
			n.toolbarlayout.settings.setVisibility(View.GONE);
			n.toolbarlayout.toolbar.setTitle(getString(R.string.toolbox_title));
			n.toolbarlayout.toolbar.setNavigationIcon(getDrawable(R.drawable.ic_launcher_foreground));
		});

		n.cvSta.setOnClickListener(a -> {
			showBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(android);
			messages.setText(R.string.sta_question);
			noButton.setText(R.string.no);
			yesButton.setText(R.string.yes);
			dismissButton.setText(R.string.dismiss);
			noButton.setOnClickListener(v -> {
				hideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				ShellUtils.fastCmd("mkdir /sdcard/sta || true");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/sta.exe /sdcard/sta/sta.exe");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/sdd.exe /sdcard/sta/sdd.exe");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/sdd.conf /sdcard/sta/sdd.conf");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/boot_img_auto-flasher_V1.1.exe /sdcard/sta/boot_img_auto-flasher_V1.1.exe");
				mount();
				if (!isMounted()) {
					dialog.dismiss();
					mountfail();
					return;
				}
				ShellUtils.fastCmd("mkdir " + winpath + "/sta || true ");
				ShellUtils.fastCmd("mkdir " + winpath + "/ProgramData/sta || true ");
				ShellUtils.fastCmd("cp '" + getFilesDir() + "/Switch to Android.lnk' " + winpath + "/Users/Public/Desktop");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/sta.exe " + winpath + "/ProgramData/sta/sta.exe");
				ShellUtils.fastCmd("cp /sdcard/sta/* " + winpath + "/sta");
				ShellUtils.fastCmd("rm -r /sdcard/sta");
				messages.setText(R.string.done);
				dismissButton.setVisibility(View.VISIBLE);
				dismissButton.setOnClickListener(v1 -> {
					hideBlur();
					dialog.dismiss();
				});
			});
			dialog.setCancelable(false);
			dialog.show();
		});
		
		n.cvScripts.setOnClickListener(v -> {
			n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
			setContentView(z.getRoot());
			z.scriptstab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
			z.toolbarlayout.settings.setVisibility(View.GONE);
			z.toolbarlayout.toolbar.setTitle(getString(R.string.script_title));
			z.toolbarlayout.toolbar.setNavigationIcon(R.drawable.ic_launcher_foreground);
		});
		
		n.cvDumpModem.setOnClickListener(a -> {
			showBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(modem);
			messages.setText(R.string.dump_modem_question);
			noButton.setText(R.string.no);
			yesButton.setText(R.string.yes);
			yesButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					noButton.setVisibility(View.GONE);
					yesButton.setVisibility(View.GONE);
					messages.setText(R.string.please_wait);
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
							if (mnt_stat.isEmpty()) {
								mount();
								dump();
							} else dump();
							messages.setText(R.string.lte);
							noButton.setVisibility(View.VISIBLE);
							noButton.setText(R.string.dismiss);
						}
					}, 50);
				}
			});
			noButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					hideBlur();
					dialog.dismiss();
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});
		
		n.cvFlashUefi.setOnClickListener(v -> {
			showBlur();
			dismissButton.setVisibility(View.GONE);
			yesButton.setVisibility(View.VISIBLE);
			noButton.setVisibility(View.VISIBLE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(uefi);
			messages.setText(R.string.flash_uefi_question);
			noButton.setText(R.string.no);
			yesButton.setText(R.string.yes);
			dismissButton.setText(R.string.dismiss);
			yesButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					noButton.setVisibility(View.GONE);
					yesButton.setVisibility(View.GONE);
					messages.setText(R.string.please_wait);
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							try {
								flash(finduefi);
								messages.setText(R.string.flash);
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
					hideBlur();
					dialog.dismiss();
				}
			});
			noButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					hideBlur();
					dialog.dismiss();
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});
		
		n.cvDbkp.setOnClickListener(v -> {
			if (!isNetworkConnected(this)) {
				dialog.dismiss();
				nointernet();
			} else {
				String bedan = ShellUtils.fastCmd("getprop ro.product.device");
				if ("nabu".equals(bedan)) {
					showBlur();
					noButton.setVisibility(View.VISIBLE);
					yesButton.setVisibility(View.VISIBLE);
					dismissButton.setVisibility(View.VISIBLE);
					icons.setVisibility(View.VISIBLE);
					icons.setImageDrawable(uefi);
					messages.setText(getString(R.string.dbkp_question, "MI PAD 5"));
					noButton.setText(R.string.no);
					yesButton.setText(R.string.nabu);
					dismissButton.setText(R.string.nabu2);
					noButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							hideBlur();
							dialog.dismiss();
						}
					});
					yesButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							noButton.setVisibility(View.GONE);
							yesButton.setVisibility(View.GONE);
							dismissButton.setVisibility(View.GONE);
							messages.setText(R.string.please_wait);
							icons.setVisibility(View.VISIBLE);
							new Thread(()->{
								ShellUtils.fastCmd("mkdir /sdcard/dbkp || true");
								androidBackup();
								ShellUtils.fastCmd("rm /sdcard/original-boot.img || true");
								ShellUtils.fastCmd("cp /sdcard/boot.img /sdcard/dbkp/boot.img");
								ShellUtils.fastCmd("su -mm -c mv /sdcard/boot.img /sdcard/original-boot.img");
								ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp8150.cfg /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp\" | su -mm -c sh");
								ShellUtils.fastCmd("cp /sdcard/dbkp/dbkp " + getFilesDir());
								ShellUtils.fastCmd("chmod 777 " + getFilesDir() + "/dbkp");
								ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp.nabu.bin /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/releases/download/1.0/nabu.fd -O /sdcard/dbkp/nabu.fd\" | su -mm -c sh");
								ShellUtils.fastCmd("cd /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c " + getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/nabu.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.nabu.bin");
								ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
								ShellUtils.fastCmd("su -mm -c mv output kernel");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
								ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
								ShellUtils.fastCmd("rm -r /sdcard/dbkp");
								ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_a bs=16m");
								ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_b bs=16m");
								runOnUiThread(()->{
									messages.setText(getString(R.string.dbkp, getString(R.string.nabu)));
									dismissButton.setText(R.string.dismiss);
									dismissButton.setVisibility(View.VISIBLE);
									dismissButton.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View v) {
											hideBlur();
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
						messages.setText(R.string.please_wait);
						icons.setVisibility(View.VISIBLE);
						new Thread(()->{
							ShellUtils.fastCmd("mkdir /sdcard/dbkp || true");
							androidBackup();
							ShellUtils.fastCmd("rm /sdcard/original-boot.img || true");
							ShellUtils.fastCmd("cp /sdcard/boot.img /sdcard/dbkp/boot.img");
							ShellUtils.fastCmd("su -mm -c mv /sdcard/boot.img /sdcard/original-boot.img");
							ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp8150.cfg /sdcard/dbkp");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp\" | su -mm -c sh");
							ShellUtils.fastCmd("cp /sdcard/dbkp/dbkp " + getFilesDir());
							ShellUtils.fastCmd("chmod 777 " + getFilesDir() + "/dbkp");
							ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp.nabu2.bin /sdcard/dbkp");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/releases/download/1.0/nabuVolumebuttons.fd -O /sdcard/dbkp/nabuVolumebuttons.fd\" | su -mm -c sh");
							ShellUtils.fastCmd("cd /sdcard/dbkp");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
							ShellUtils.fastCmd("su -mm -c " + getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/nabuVolumebuttons.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.nabu2.bin");
							ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
							ShellUtils.fastCmd("su -mm -c mv output kernel");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
							ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
							ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
							ShellUtils.fastCmd("rm -r /sdcard/dbkp");
							ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_a bs=16m");
							ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_b bs=16m");
							runOnUiThread(()->{
								messages.setText(getString(R.string.dbkp, getString(R.string.nabu2)));
								dismissButton.setText(R.string.dismiss);
								dismissButton.setVisibility(View.VISIBLE);
								dismissButton.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										hideBlur();
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
				showBlur();
				noButton.setVisibility(View.VISIBLE);
				yesButton.setVisibility(View.VISIBLE);
				dismissButton.setVisibility(View.GONE);
				icons.setVisibility(View.VISIBLE);
				icons.setImageDrawable(uefi);
				messages.setText(getString(R.string.dbkp_question, dbkpmodel));
				noButton.setText(R.string.no);
				yesButton.setText(R.string.yes);
				noButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						hideBlur();
						dialog.dismiss();
					}
				});
				yesButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						noButton.setVisibility(View.GONE);
						yesButton.setVisibility(View.GONE);
						dismissButton.setVisibility(View.GONE);
						messages.setText(R.string.please_wait);
						icons.setVisibility(View.VISIBLE);
						new Thread(()->{
							ShellUtils.fastCmd("mkdir /sdcard/dbkp || true");
							androidBackup();
							ShellUtils.fastCmd("rm /sdcard/original-boot.img || true");
							ShellUtils.fastCmd("cp /sdcard/boot.img /sdcard/dbkp/boot.img");
							ShellUtils.fastCmd("su -mm -c mv /sdcard/boot.img /sdcard/original-boot.img");
							ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp8150.cfg /sdcard/dbkp");
							ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp\" | su -mm -c sh");
							ShellUtils.fastCmd("cp /sdcard/dbkp/dbkp "+getFilesDir());
							ShellUtils.fastCmd("chmod 777 " + getFilesDir() + "/dbkp");
							String bedan = ShellUtils.fastCmd("getprop ro.product.device");
							if ("guacamole".equals(bedan) || "OnePlus7Pro".equals(bedan) || "OnePlus7Pro4G".equals(bedan)) {
								ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp.hotdog.bin /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/DBKP/guacamole.fd -O /sdcard/dbkp/guacamole.fd\" | su -mm -c sh");
								ShellUtils.fastCmd("cd /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c " + getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/guacamole.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.hotdog.bin");
								ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
								ShellUtils.fastCmd("su -mm -c mv output kernel");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
								ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
								ShellUtils.fastCmd("rm -r /sdcard/dbkp");
								ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_a bs=16m");
								ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_b bs=16m");
							} else if ("hotdog".equals(bedan) || "OnePlus7TPro".equals(bedan) || "OnePlus7TPro4G".equals(bedan)) {
								ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp.hotdog.bin /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/DBKP/hotdog.fd -O /sdcard/dbkp/hotdog.fd\" | su -mm -c sh");
								ShellUtils.fastCmd("cd /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c " + getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/hotdog.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.hotdog.bin");
								ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
								ShellUtils.fastCmd("su -mm -c mv output kernel");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
								ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
								ShellUtils.fastCmd("rm -r /sdcard/dbkp");
								ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_a bs=16m");
								ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_b bs=16m");
							} else if ("cepheus".equals(bedan)) {
								ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp.cepheus.bin /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-everything/releases/download/Files/cepheus.fd -O /sdcard/dbkp/cepheus.fd\" | su -mm -c sh");
								ShellUtils.fastCmd("cd /sdcard/dbkp");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c " + getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/cepheus.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.cepheus.bin");
								ShellUtils.fastCmd("su -mm -c rm /sdcard/dbkp/kernel");
								ShellUtils.fastCmd("su -mm -c mv output kernel");
								ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) repack boot.img\" | su -c sh");
								ShellUtils.fastCmd("su -mm -c cp new-boot.img /sdcard/new-boot.img");
								ShellUtils.fastCmd("su -mm -c mv /sdcard/new-boot.img /sdcard/patched-boot.img");
								ShellUtils.fastCmd("rm -r /sdcard/dbkp");
								ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot bs=16m");
						//	} else if ("pipa".equals(bedan)) {
						//		ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp.pipa.bin /sdcard/dbkp");
						//		ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-everything/releases/download/Files/pipa.fd -O /sdcard/dbkp/pipa.fd\" | su -mm -c sh");
						//		ShellUtils.fastCmd("cd /sdcard/dbkp");
						//		ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name magiskboot) unpack boot.img\" | su -c sh");
						//		ShellUtils.fastCmd("su -mm -c " + getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/pipa.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp8150.cfg /sdcard/dbkp/dbkp.pipa.bin");
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
									messages.setText(getString(R.string.dbkp, getString(R.string.op7)));
								} else if ("cepheus".equals(bedan)) {
									messages.setText(getString(R.string.dbkp, getString(R.string.cepheus)));
						//		} else if ("pipa".equals(bedan)) {
						//			String nabu = getString(R.string.nabu);
						//			messages.setText(String.format(getString(R.string.dbkp), nabu));
								} 
								dismissButton.setText(R.string.dismiss);
								dismissButton.setVisibility(View.VISIBLE);
								dismissButton.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										hideBlur();
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
		
		n.cvDevcfg.setOnClickListener(v -> {
			if (!isNetworkConnected(this)) {
				dialog.dismiss();
				nointernet();
			} else {
				showBlur();
				noButton.setVisibility(View.VISIBLE);
				yesButton.setVisibility(View.VISIBLE);
				dismissButton.setVisibility(View.GONE);
				icons.setVisibility(View.VISIBLE);
				icons.setImageDrawable(uefi);
				checkdbkpmodel();
				messages.setText(getString(R.string.devcfg_question, dbkpmodel));
				noButton.setText(R.string.no);
				yesButton.setText(R.string.yes);
				noButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						hideBlur();
						dialog.dismiss();
					}
				});
				yesButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						noButton.setVisibility(View.GONE);
						yesButton.setVisibility(View.GONE);
						dismissButton.setVisibility(View.GONE);
						messages.setText(R.string.please_wait);
						icons.setVisibility(View.VISIBLE);
						new Thread(()->{
							String findoriginaldevcfg = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name original-devcfg.img");
							if (findoriginaldevcfg.isEmpty()) {
								ShellUtils.fastCmd("dd bs=8M if=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix) of=" + getFilesDir() + "/original-devcfg.img");
							}
							ShellUtils.fastCmd("cp " + getFilesDir() + "/original-devcfg.img /sdcard");
							if (!isNetworkConnected(MainActivity.this)) {
								String finddevcfg = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name OOS11_devcfg_*");
								if (!finddevcfg.isEmpty()) {
									dialog.dismiss();
									nointernet();
								} else {
									String bedan = ShellUtils.fastCmd("getprop ro.product.device");
									if ("guacamole".equals(bedan) || "OnePlus7Pro".equals(bedan) || "OnePlus7Pro4G".equals(bedan)) {
										ShellUtils.fastCmd("dd bs=8M if=" + getFilesDir() + "/OOS11_devcfg_guacamole.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)");
									} else if ("hotdog".equals(bedan) || "OnePlus7TPro".equals(bedan) || "OnePlus7TPro4G".equals(bedan)) {
										ShellUtils.fastCmd("dd bs=8M if=" + getFilesDir() + "/OOS11_devcfg_hotdog.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)");
									}
								}
							} else {
								String bedan = ShellUtils.fastCmd("getprop ro.product.device");
								if ("guacamole".equals(bedan) || "OnePlus7Pro".equals(bedan) || "OnePlus7Pro4G".equals(bedan)) {
									ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_guacamole.img -O /sdcard/OOS11_devcfg_guacamole.img\" | su -mm -c sh");
									ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_guacamole.img -O /sdcard/OOS12_devcfg_guacamole.img\" | su -mm -c sh");
									ShellUtils.fastCmd("cp /sdcard/OOS11_devcfg_guacamole.img " + getFilesDir());
									ShellUtils.fastCmd("cp /sdcard/OOS12_devcfg_guacamole.img " + getFilesDir());
									ShellUtils.fastCmd("dd bs=8M if=" + getFilesDir() + "/OOS11_devcfg_guacamole.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)");
								} else if ("hotdog".equals(bedan) || "OnePlus7TPro".equals(bedan) || "OnePlus7TPro4G".equals(bedan)) {
									ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_hotdog.img -O /sdcard/OOS11_devcfg_hotdog.img\" | su -mm -c sh");
									ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_hotdog.img -O /sdcard/OOS12_devcfg_hotdog.img\" | su -mm -c sh");
									ShellUtils.fastCmd("cp /sdcard/OOS11_devcfg_hotdog.img " + getFilesDir());
									ShellUtils.fastCmd("cp /sdcard/OOS12_devcfg_hotdog.img " + getFilesDir());
									ShellUtils.fastCmd("dd bs=8M if=" + getFilesDir() + "/OOS11_devcfg_hotdog.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)");
								}
							}
							runOnUiThread(()->{
								mount();
								String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
								if (mnt_stat.isEmpty()) {
									ShellUtils.fastCmd("cp " + getFilesDir() + "/sdd.exe /sdcard/sdd.exe");
									ShellUtils.fastCmd("cp " + getFilesDir() + "/devcfg-sdd.conf /sdcard/sdd.conf");
									dialog.dismiss();
									mountfail();
								} else {
									ShellUtils.fastCmd("mkdir " + winpath + "/sta || true ");
									ShellUtils.fastCmd("cp '" + getFilesDir() + "/Flash Devcfg.lnk' " + winpath + "/Users/Public/Desktop");
									ShellUtils.fastCmd("cp " + getFilesDir() + "/sdd.exe " + winpath + "/sta/sdd.exe");
									ShellUtils.fastCmd("cp " + getFilesDir() + "/devcfg-sdd.conf " + winpath + "/sta/sdd.conf");
									ShellUtils.fastCmd("cp /sdcard/original-devcfg.img " + winpath + "/original-devcfg.img");
								}
								messages.setText(R.string.devcfg);
								dismissButton.setText(R.string.dismiss);
								dismissButton.setVisibility(View.VISIBLE);
								dismissButton.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										hideBlur();
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
		
		n.cvSoftware.setOnClickListener(v -> {
			showBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(sensors);
			messages.setText(R.string.software_question);
			noButton.setText(R.string.no);
			yesButton.setText(R.string.yes);
			dismissButton.setText(R.string.dismiss);
			noButton.setOnClickListener(v4 -> {
				hideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v5 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				messages.setText(R.string.please_wait);
				ShellUtils.fastCmd("cp " + getFilesDir() + "/WorksOnWoa.url /sdcard");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/TestedSoftware.url /sdcard");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/ARMSoftware.url /sdcard");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/ARMRepo.url /sdcard");
				mount();
				String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
				if (mnt_stat.isEmpty()) {
					dialog.dismiss();
					mountfail();
				} else {
					ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
					ShellUtils.fastCmd("cp /sdcard/WorksOnWoa.url " + winpath + "/Toolbox");
					ShellUtils.fastCmd("cp /sdcard/TestedSoftware.url " + winpath + "/Toolbox");
					ShellUtils.fastCmd("cp /sdcard/ARMSoftware.url " + winpath + "/Toolbox");
					ShellUtils.fastCmd("cp /sdcard/ARMRepo.url " + winpath + "/Toolbox");
					ShellUtils.fastCmd("rm /sdcard/WorksOnWoa.url");
					ShellUtils.fastCmd("rm /sdcard/TestedSoftware.url");
					ShellUtils.fastCmd("rm /sdcard/ARMSoftware.url");
					ShellUtils.fastCmd("rm /sdcard/ARMRepo.url");
					messages.setText(R.string.done);
					dismissButton.setVisibility(View.VISIBLE);
					dismissButton.setOnClickListener(v6 -> {
						hideBlur();
						dialog.dismiss();
					});
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});

		n.cvAtlasos.setOnClickListener(v -> {
			if (!isNetworkConnected(this)) {
				dialog.dismiss();
				nointernet();
			} else {
				showBlur();
				noButton.setVisibility(View.VISIBLE);
				yesButton.setVisibility(View.VISIBLE);
				dismissButton.setVisibility(View.VISIBLE);
				icons.setVisibility(View.VISIBLE);
				icons.setImageDrawable(atlasrevi);
				messages.setText(R.string.atlasos_question);
				noButton.setText(R.string.revios);
				yesButton.setText(R.string.atlasos);
				dismissButton.setText(R.string.dismiss);
				dismissButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						hideBlur();
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
						messages.setText(R.string.please_wait);
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
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										mount();
										String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
										if (mnt_stat.isEmpty()) {
											dialog.dismiss();
											mountfail();
										} else {
											icons.setImageDrawable(atlasrevi);
											ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
											ShellUtils.fastCmd("cp /sdcard/ReviPlaybook.apbx " + winpath + "/Toolbox/ReviPlaybook.apbx");
											ShellUtils.fastCmd("cp /sdcard/AMEWizardBeta.zip " + winpath + "/Toolbox");
											ShellUtils.fastCmd("su -mm -c rm /sdcard/ReviPlaybook.apbx");
											ShellUtils.fastCmd("su -mm -c rm /sdcard/AMEWizardBeta.zip");
											bar.setVisibility(View.GONE);
											messages.setText(R.string.done);
											dismissButton.setVisibility(View.VISIBLE);
											dismissButton.setOnClickListener(v7 -> {
												hideBlur();
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
						messages.setText(R.string.please_wait);
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
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										mount();
										String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
										if (mnt_stat.isEmpty()) {
											dialog.dismiss();
											mountfail();
										} else {
											icons.setImageDrawable(atlasrevi);
											ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
											ShellUtils.fastCmd("cp /sdcard/AtlasPlaybook.apbx " + winpath + "/Toolbox/AtlasPlaybook_v0.4.1.apbx");
											ShellUtils.fastCmd("cp /sdcard/AtlasPlaybook.apbx " + winpath + "/Toolbox/AtlasPlaybook_v0.4.0_23H2Only.apbx");
											ShellUtils.fastCmd("cp /sdcard/AMEWizardBeta.zip " + winpath + "/Toolbox");
											ShellUtils.fastCmd("su -mm -c rm /sdcard/AtlasPlaybook_v0.4.1.apbx");
											ShellUtils.fastCmd("su -mm -c rm /sdcard/AtlasPlaybook_v0.4.0_23H2Only.apbx");
											ShellUtils.fastCmd("su -mm -c rm /sdcard/AMEWizardBeta.zip");
											bar.setVisibility(View.GONE);
											messages.setText(R.string.done);
											dismissButton.setVisibility(View.VISIBLE);
											dismissButton.setOnClickListener(v8 -> {
												hideBlur();
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

		z.cvUsbhost.setOnClickListener(v -> {
			showBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(mnt);
			messages.setText(R.string.usbhost_question);
			noButton.setText(R.string.no);
			yesButton.setText(R.string.yes);
			dismissButton.setText(R.string.dismiss);
			noButton.setOnClickListener(v9 -> {
				hideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v10 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				messages.setText(R.string.please_wait);
				ShellUtils.fastCmd("cp " + getFilesDir() + "/usbhostmode.exe /sdcard");
				ShellUtils.fastCmd("cp '" + getFilesDir() + "/USB Host Mode.lnk' /sdcard");
				mount();
				String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
				if (mnt_stat.isEmpty()) {
					dialog.dismiss();
					mountfail();
				} else {
					ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
					ShellUtils.fastCmd("cp /sdcard/usbhostmode.exe " + winpath + "/Toolbox");
					ShellUtils.fastCmd("cp '" + getFilesDir() + "/USB Host Mode.lnk' " + winpath + "/Users/Public/Desktop");
					ShellUtils.fastCmd("rm /sdcard/usbhostmode.exe");
					messages.setText(R.string.done);
					dismissButton.setVisibility(View.VISIBLE);
					dismissButton.setOnClickListener(v11 -> {
						hideBlur();
						dialog.dismiss();
					});
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});
		
		z.cvRotation.setOnClickListener(v -> {
			showBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(boot);
			messages.setText(R.string.rotation_question);
			noButton.setText(R.string.no);
			yesButton.setText(R.string.yes);
			dismissButton.setText(R.string.dismiss);
			noButton.setOnClickListener(v12 -> {
				hideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v13 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				messages.setText(R.string.please_wait);
				ShellUtils.fastCmd("cp " + getFilesDir() + "/QuickRotate_V3.0.exe /sdcard/");
				mount();
				String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
				if (mnt_stat.isEmpty()) {
					dialog.dismiss();
					mountfail();
				} else {
					ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
					ShellUtils.fastCmd("cp /sdcard/QuickRotate_V3.0.exe " + winpath + "/Toolbox");
					ShellUtils.fastCmd("cp /sdcard/QuickRotate_V3.0.exe " + winpath + "/Users/Public/Desktop");
					ShellUtils.fastCmd("rm /sdcard/QuickRotate_V3.0.exe");
					messages.setText(R.string.done);
					dismissButton.setVisibility(View.VISIBLE);
					dismissButton.setOnClickListener(v14 -> {
					hideBlur();
					dialog.dismiss();
					});
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});
		
		z.cvTablet.setOnClickListener(v -> {
			showBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(sensors);
			messages.setText(R.string.tablet_question);
			noButton.setText(R.string.no);
			yesButton.setText(R.string.yes);
			dismissButton.setText(R.string.dismiss);
			noButton.setOnClickListener(v16 -> {
				hideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v17 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				messages.setText(R.string.please_wait);
				ShellUtils.fastCmd("cp " + getFilesDir() + "/Optimized_Taskbar_Control_V3.1.exe /sdcard/");
				mount();
				String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
				if (mnt_stat.isEmpty()) {
					dialog.dismiss();
					mountfail();
				} else {
					ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
					ShellUtils.fastCmd("cp /sdcard/Optimized_Taskbar_Control_V3.1.exe " + winpath + "/Toolbox");
					ShellUtils.fastCmd("rm /sdcard/Optimized_Taskbar_Control_V3.1.exe");
					messages.setText(R.string.done);
					dismissButton.setVisibility(View.VISIBLE);
					dismissButton.setOnClickListener(v18 -> {
					hideBlur();
					dialog.dismiss();
					});
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});

		z.cvSetup.setOnClickListener(v -> {
			if (!isNetworkConnected(this)) {
				dialog.dismiss();
				nointernet();
			} else {
				showBlur();
				noButton.setVisibility(View.VISIBLE);
				yesButton.setVisibility(View.VISIBLE);
				dismissButton.setVisibility(View.GONE);
				icons.setVisibility(View.VISIBLE);
				icons.setImageDrawable(mnt);
				messages.setText(R.string.setup_question);
				noButton.setText(R.string.no);
				yesButton.setText(R.string.yes);
				dismissButton.setText(R.string.dismiss);
				noButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						hideBlur();
						dialog.dismiss();
					}
				});
				yesButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						noButton.setVisibility(View.GONE);
						yesButton.setVisibility(View.GONE);
						messages.setText(R.string.please_wait);
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
								ShellUtils.fastCmd("cp " + getFilesDir() + "/install.bat /sdcard/Frameworks/install.bat");
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
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										mount();
										String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
										Log.d("path", mnt_stat);
										if (mnt_stat.isEmpty()) {
											dialog.dismiss();
											mountfail();
										} else {
											icons.setImageDrawable(mnt);
											ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
											ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox/Frameworks || true ");
											ShellUtils.fastCmd("cp /sdcard/Frameworks/* " + winpath + "/Toolbox/Frameworks");
											ShellUtils.fastCmd("rm -r /sdcard/Frameworks");
											bar.setVisibility(View.GONE);
											messages.setText(R.string.done);
											dismissButton.setVisibility(View.VISIBLE);
											dismissButton.setOnClickListener(v15 -> {
												hideBlur();
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

		z.cvDefender.setOnClickListener(v -> {
			showBlur();
			noButton.setVisibility(View.VISIBLE);
			yesButton.setVisibility(View.VISIBLE);
			dismissButton.setVisibility(View.GONE);
			icons.setVisibility(View.VISIBLE);
			icons.setImageDrawable(edge);
			messages.setText(R.string.defender_question);
			noButton.setText(R.string.no);
			yesButton.setText(R.string.yes);
			dismissButton.setText(R.string.dismiss);
			noButton.setOnClickListener(v16 -> {
				hideBlur();
				dialog.dismiss();
			});
			yesButton.setOnClickListener(v17 -> {
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				messages.setText(R.string.please_wait);
				String finddefenderremover = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name DefenderRemover.exe");
				if (finddefenderremover.isEmpty()) {
					if (!isNetworkConnected(MainActivity.this)) {
						dialog.dismiss();
						nointernet();
					} else {
						ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/DefenderRemover.exe -O /sdcard/DefenderRemover.exe\" | su -mm -c sh");
						ShellUtils.fastCmd("cp /sdcard/DefenderRemover.exe " + getFilesDir());
						ShellUtils.fastCmd("cp " + getFilesDir() + "/RemoveEdge.bat /sdcard");
						mount();
						String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
						if (mnt_stat.isEmpty()) {
							dialog.dismiss();
							mountfail();
						} else {
							ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
							ShellUtils.fastCmd("cp " + getFilesDir() + "/DefenderRemover.exe " + winpath + "/Toolbox");
							ShellUtils.fastCmd("rm /sdcard/DefenderRemover.exe");
							ShellUtils.fastCmd("cp /sdcard/RemoveEdge.bat " + winpath + "/Toolbox");
							ShellUtils.fastCmd("rm /sdcard/RemoveEdge.bat");
							messages.setText(R.string.done);
							dismissButton.setVisibility(View.VISIBLE);
							dismissButton.setOnClickListener(v18 -> {
								hideBlur();
								dialog.dismiss();
							});
						}
					}
				} else {
					ShellUtils.fastCmd("cp " + getFilesDir() + "/RemoveEdge.bat /sdcard");
					ShellUtils.fastCmd("cp " + getFilesDir() + "/DefenderRemover.exe /sdcard");
					mount();
					String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
					if (mnt_stat.isEmpty()) {
						dialog.dismiss();
						mountfail();
					} else {
						ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
						ShellUtils.fastCmd("cp " + getFilesDir() + "/DefenderRemover.exe " + winpath + "/Toolbox");
						ShellUtils.fastCmd("cp " + getFilesDir() + "/RemoveEdge.bat " + winpath + "/Toolbox");
						ShellUtils.fastCmd("rm /sdcard/DefenderRemover.exe");
						ShellUtils.fastCmd("rm /sdcard/RemoveEdge.bat");
						messages.setText(R.string.done);
						dismissButton.setVisibility(View.VISIBLE);
						dismissButton.setOnClickListener(v19 -> {
							hideBlur();
							dialog.dismiss();
						});
					}
				}
			});
			dialog.setCancelable(false);
			dialog.show();
		});
		
		k.toolbarlayout.toolbar.setTitle(R.string.preferences);
		k.toolbarlayout.toolbar.setNavigationIcon(R.drawable.ic_launcher_foreground);
		//overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		x.toolbarlayout.settings.setOnClickListener(v -> {
		x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
		setContentView(k.getRoot());
		k.settingsPanel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
		k.backupQB.setChecked(pref.getBACKUP(this));
		k.backupQBA.setChecked(pref.getBACKUP_A(this));
		k.autobackup.setChecked(!pref.getAUTO(this));
		k.autobackupA.setChecked(!pref.getAUTO(this));
		k.confirmation.setChecked(pref.getCONFIRM(this));
		k.automount.setChecked(pref.getAutoMount(this));
		k.securelock.setChecked(!pref.getSecure(this));
		k.mountLocation.setChecked(pref.getMountLocation(this));
		k.appUpdate.setChecked(pref.getAppUpdate(this));
		k.devcfg1.setChecked(pref.getDevcfg1(this)&&k.devcfg1.getVisibility()==View.VISIBLE);
		k.devcfg2.setChecked(pref.getDevcfg2(this));
		k.toolbarlayout.settings.setVisibility(View.GONE);
		});
		
		k.mountLocation.setOnCheckedChangeListener((compoundButton, b) -> {
			pref.setMountLocation(this, b);
			winpath = (b ? "/mnt/Windows" : "/mnt/sdcard/Windows");
		});
		
		k.backupQB.setOnCheckedChangeListener((compoundButton, b) -> {
			if (pref.getBACKUP(this)) {
				pref.setBACKUP(this, false);
				k.autobackup.setVisibility(View.VISIBLE);
			} else {
				showBlur();
				dialog.show();
				dialog.setCancelable(false);
				messages.setText(R.string.bwarn);
				yesButton.setVisibility(View.VISIBLE);
				noButton.setVisibility(View.GONE);
				dismissButton.setVisibility(View.VISIBLE);
				icons.setVisibility(View.GONE);
				yesButton.setText(R.string.agree);
				dismissButton.setText(R.string.cancel);
				yesButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						pref.setBACKUP(MainActivity.this, true);
						k.autobackup.setVisibility(View.GONE);
						dialog.dismiss();
						hideBlur();
					}
				});
				dismissButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						k.backupQB.setChecked(false);
						dialog.dismiss();
						hideBlur();
					}
				});
			}
		});

		k.backupQBA.setOnCheckedChangeListener((compoundButton, b) -> {
			if (pref.getBACKUP_A(this)) {
				pref.setBACKUP_A(this, false);
				k.autobackupA.setVisibility(View.VISIBLE);
			} else {
				showBlur();
				dialog.show();
				dialog.setCancelable(false);
				messages.setText(R.string.bwarn);
				yesButton.setVisibility(View.VISIBLE);
				noButton.setVisibility(View.GONE);
				dismissButton.setVisibility(View.VISIBLE);
				icons.setVisibility(View.GONE);
				yesButton.setText(R.string.agree);
				dismissButton.setText(R.string.cancel);
				yesButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						pref.setBACKUP_A(MainActivity.this, true);
						k.autobackupA.setVisibility(View.GONE);
						dialog.dismiss();
						hideBlur();
					}
				});
				dismissButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						k.backupQBA.setChecked(false);
						dialog.dismiss();
						hideBlur();
					}
				});
			}
		});
		


		x.cvInfo.setOnClickListener(v -> {
			if (id.kuato.woahelper.BuildConfig.BUILD_TYPE != "release") return;
			if (!isNetworkConnected(this)) {
				dialog.dismiss();
				nointernet();
			} else {
				showBlur();
				noButton.setVisibility(View.GONE);
				yesButton.setVisibility(View.GONE);
				dismissButton.setVisibility(View.GONE);
				icons.setVisibility(View.GONE);
				messages.setText(R.string.please_wait);
				dismissButton.setText(R.string.dismiss);
				String version = ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget -q -O - https://raw.githubusercontent.com/n00b69/woa-helper-update/main/README.md\" | su -mm -c sh");
				if (BuildConfig.VERSION_NAME.equals(version)) {
					dismissButton.setVisibility(View.VISIBLE);
					messages.setText(getString(R.string.no) + " " + getString(R.string.update1));
				} else {
					yesButton.setVisibility(View.VISIBLE);
					noButton.setVisibility(View.VISIBLE);
					dismissButton.setVisibility(View.GONE);
					messages.setText(R.string.update1);
					yesButton.setText(R.string.update);
					noButton.setText(R.string.later);
					noButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							hideBlur();
							dialog.dismiss();
						}
					});
					yesButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							yesButton.setVisibility(View.GONE);
							noButton.setVisibility(View.GONE);
							messages.setText(getString(R.string.update2) + "\n" + getString(R.string.please_wait));
							update();
						}
					});
				}
				dismissButton.setOnClickListener(v20 -> {
					hideBlur();
					dialog.dismiss();
				});
				dialog.setCancelable(false);
				dialog.show();
			}
		});
			
		k.autobackup.setOnCheckedChangeListener((compoundButton, b) -> pref.setAUTO(this, !b));
		k.autobackupA.setOnCheckedChangeListener((compoundButton, b) -> pref.setAUTO_A(this, !b));
		k.confirmation.setOnCheckedChangeListener((compoundButton, b) -> pref.setCONFIRM(this, b));
		k.securelock.setOnCheckedChangeListener((compoundButton, b) -> pref.setSecure(this, !b));
		k.automount.setOnCheckedChangeListener((compoundButton, b) -> pref.setAutoMount(this, b));
		k.appUpdate.setOnCheckedChangeListener((compoundButton, b) -> pref.setAppUpdate(this, b));
		String op7funny = ShellUtils.fastCmd("getprop ro.product.device");
		if ("guacamole".equals(op7funny) || "guacamolet".equals(op7funny) || "OnePlus7Pro".equals(op7funny) || "OnePlus7Pro4G".equals(op7funny) || "OnePlus7ProTMO".equals(op7funny) || "hotdog".equals(op7funny) || "OnePlus7TPro".equals(op7funny) || "OnePlus7TPro4G".equals(op7funny)) {
			k.devcfg1.setOnCheckedChangeListener((compoundButton, b) -> {pref.setDevcfg1(this, b);if(b)k.devcfg2.setVisibility(View.VISIBLE); else k.devcfg2.setVisibility(View.GONE);pref.setDevcfg2(this, false);});
			k.devcfg2.setOnCheckedChangeListener((compoundButton, b) -> pref.setDevcfg2(this, b));
		} else {
			k.devcfg1.setVisibility(View.GONE);
			k.devcfg2.setVisibility(View.GONE);
			pref.setDevcfg1(this, false);
			pref.setDevcfg2(this, false);
		}
		k.button.setOnClickListener(v -> {
			k.settingsPanel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_out));
			setContentView(x.getRoot());
			x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_in));
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		checkwin();
		checkuefi();
	}

	@Override
	public void onBackPressed() {
		final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
		switch (viewGroup.getId()) {
			case R.id.scriptstab:
				z.scriptstab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_out));
				setContentView(n.getRoot());
				n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_in));
				break;
			case R.id.toolboxtab:
				n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_out));
				setContentView(x.getRoot());
				x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_in));
				break;
			case R.id.settingsPanel:
				k.settingsPanel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_out));
				setContentView(x.getRoot());
				x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_in));
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

	public void flash(String uefi) {
		ShellUtils.fastCmd("dd if=" + uefi + " of=/dev/block/bootdevice/by-name/boot$(getprop ro.boot.slot_suffix) bs=16m");
	}
	
	public void mount() {
		ShellUtils.fastCmd("mkdir " + winpath + " || true");
		ShellUtils.fastCmd("cd " + getFilesDir());
		ShellUtils.fastCmd("su -mm -c ./mount.ntfs " + win + " " + winpath);
		String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
		if (mnt_stat.isEmpty()) k.mountLocation.setChecked(!pref.getMountLocation(this));
			ShellUtils.fastCmd("mkdir " + winpath + " || true");
			ShellUtils.fastCmd("cd " + getFilesDir());
			ShellUtils.fastCmd("su -mm -c ./mount.ntfs " + win + " " + winpath);
			String mnt_stat2 = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
			if (!mnt_stat2.isEmpty()) {
				mounted = getString(R.string.unmountt);
				x.tvMnt.setText(getString(R.string.mnt_title, mounted));
			}
	}
	
	public void unmount() {
		ShellUtils.fastCmd("su -mm -c umount " + winpath);
		ShellUtils.fastCmd("rmdir " + winpath);
		mounted = getString(R.string.mountt);
		x.tvMnt.setText(getString(R.string.mnt_title, mounted));
	}
	
	public void winBackup() {
		mount();
		ShellUtils.fastCmd("su -mm -c dd bs=8m if=" + boot + " of=" + winpath + "/boot.img");
	}
	
	public void androidBackup() {
		ShellUtils.fastCmd("su -mm -c dd bs=8m if=" + boot + " of=/sdcard/boot.img");
	}

	public void dump() {
		ShellUtils.fastCmd("su -mm -c dd if=/dev/block/by-name/modemst1 of=$(find " + winpath + "/Windows/System32/DriverStore/FileRepository -name qcremotefs8150.inf_arm64_*)/bootmodem_fs1");
		ShellUtils.fastCmd("su -mm -c dd if=/dev/block/by-name/modemst2 of=$(find " + winpath + "/Windows/System32/DriverStore/FileRepository -name qcremotefs8150.inf_arm64_*)/bootmodem_fs2");
	}

	public void checkRoot(){
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		TextView messages = dialog.findViewById(R.id.messages);
		messages.setText(R.string.nonroot);
		dialog.show();
		dialog.setCancelable(false);
	}

	public void checkdbkpmodel() {
		String dbkpmodel = ShellUtils.fastCmd("getprop ro.product.device");
		if ("guacamole".equals(dbkpmodel) || "guacamolet".equals(dbkpmodel) || "OnePlus7Pro".equals(dbkpmodel) || "OnePlus7Pro4G".equals(dbkpmodel) || "OnePlus7ProTMO".equals(dbkpmodel)) {
			dbkpmodel = "ONEPLUS 7 PRO";
		} else if ("hotdog".equals(dbkpmodel) || "OnePlus7TPro".equals(dbkpmodel) || "OnePlus7TPro4G".equals(dbkpmodel)) {
			dbkpmodel = "ONEPLUS 7T PRO";
		} else if ("cepheus".equals(dbkpmodel)) {
			dbkpmodel = "XIAOMI MI 9";
			k.devcfg1.setVisibility(View.GONE);
			k.devcfg2.setVisibility(View.GONE);
		} else if ("nabu".equals(dbkpmodel)) {
			dbkpmodel = "XIAOMI PAD 5";
			k.devcfg1.setVisibility(View.GONE);
			k.devcfg2.setVisibility(View.GONE);
		} else if ("pipa".equals(dbkpmodel)) {
			dbkpmodel = "XIAOMI PAD 6";
			k.devcfg1.setVisibility(View.GONE);
			k.devcfg2.setVisibility(View.GONE);
		} else {
			dbkpmodel = "UNSUPPORTED";
			k.devcfg1.setVisibility(View.GONE);
			k.devcfg2.setVisibility(View.GONE);
		}
	}
	public void checkuefi() {
		ShellUtils.fastCmd("su -c mkdir /sdcard/UEFI");
		finduefi = "\""+ShellUtils.fastCmd(getString(R.string.uefiChk))+"\"";
		if (finduefi.contains("img")) {
			x.tvQuickBoot.setText(R.string.quickboot_title);
			x.cvQuickBoot.setEnabled(true);
			x.tvQuickBoot.setText(R.string.quickboot_title);
			x.tvBootSubtitle.setText(R.string.quickboot_subtitle_nabu);
			n.tvFlashUefi.setText(R.string.flash_uefi_title);
			n.tvUefiSubtitle.setText(R.string.flash_uefi_subtitle);
			n.cvFlashUefi.setEnabled(true);
		} else {
			x.cvQuickBoot.setEnabled(false);
			x.tvQuickBoot.setText(R.string.uefi_not_found);
			x.tvBootSubtitle.setText(getString(R.string.uefi_not_found_subtitle, device));
			n.tvFlashUefi.setText(R.string.uefi_not_found);
			n.tvUefiSubtitle.setText(getString(R.string.uefi_not_found_subtitle, device));
			n.cvFlashUefi.setEnabled(false);
		}
	}
	
	public void checkwin() {
		if (win.isEmpty()) {
			final Dialog dialog = new Dialog(this);
			dialog.setContentView(R.layout.dialog);
			dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
			TextView messages = dialog.findViewById(R.id.messages);
			messages.setText(R.string.partition);
			dialog.show();
			dialog.setCancelable(false);
			x.cvMnt.setEnabled(false);
			x.cvToolbox.setEnabled(false);
			x.cvQuickBoot.setEnabled(false);
			n.cvFlashUefi.setEnabled(false);
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
					messages.setText(R.string.update1);
					yesButton.setText(R.string.update);
					noButton.setText(R.string.later);
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
							messages.setText(getString(R.string.update2) + "\n" + getString(R.string.please_wait));
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
				ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://raw.githubusercontent.com/n00b69/woa-helper-update/main/woahelper.apk -O %s\" | su -mm -c sh", getFilesDir() + "/woahelper.apk"));
				ShellUtils.fastCmd("pm install " + getFilesDir() + "/woahelper.apk && rm " + getFilesDir() + "/woahelper.apk");
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
		yesButton.setText(R.string.chat);
		dismissButton.setText(R.string.cancel);
		showBlur();
		messages.setText(getString(R.string.mountfail) + "\n" + getString(R.string.internalstorage));
		dismissButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideBlur();
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
	}
	
	public void nointernet() {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog);
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		MaterialButton dismissButton = dialog.findViewById(R.id.dismiss);
		TextView messages = dialog.findViewById(R.id.messages);
		dismissButton.setVisibility(View.VISIBLE);
		dismissButton.setText(R.string.dismiss);
		showBlur();
		messages.setText(R.string.internet);
		dismissButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideBlur();
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
		if (Configuration.ORIENTATION_LANDSCAPE != newConfig.orientation && Objects.equals(device, "nabu") || Objects.equals(device, "pipa") || Objects.equals(device, "winner") || Objects.equals(device, "winnerx") || Objects.equals(device, "q2q") || Objects.equals(device, "gts6l") || Objects.equals(device, "gts6lwifi")) {
			x.app.setOrientation(LinearLayout.VERTICAL);
			x.top.setOrientation(LinearLayout.HORIZONTAL);
			x.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE2);
			x.deviceName.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
			x.tvPanel.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
			x.tvRamvalue.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
			x.guideText.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
			x.groupText.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
			x.tvSlot.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
			x.tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE3);
		} else if (Configuration.ORIENTATION_PORTRAIT != newConfig.orientation && Objects.equals(device, "nabu") || Objects.equals(device, "pipa") || Objects.equals(device, "winner") || Objects.equals(device, "winnerx") || Objects.equals(device, "q2q") || Objects.equals(device, "gts6l") || Objects.equals(device, "gts6lwifi")) {
			x.app.setOrientation(LinearLayout.HORIZONTAL);
			x.top.setOrientation(LinearLayout.VERTICAL);
			x.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
			x.deviceName.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
			x.tvPanel.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
			x.tvRamvalue.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
			x.guideText.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
			x.groupText.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
			x.tvSlot.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
			x.tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainActivity.SIZE);
		}
	}

	void showBlur() {
		x.blur.setVisibility(View.VISIBLE);
		k.blur.setVisibility(View.VISIBLE);
		n.blur.setVisibility(View.VISIBLE);
		z.blur.setVisibility(View.VISIBLE);
	}
	void hideBlur() {
		x.blur.setVisibility(View.GONE);
		k.blur.setVisibility(View.GONE);
		n.blur.setVisibility(View.GONE);
		z.blur.setVisibility(View.GONE);
	}

	ViewGroup getTargetViewGroup() {
		return (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
	}

	void updateLastBackupDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
		String currentDateAndTime = sdf.format(new Date());
		pref.setDATE(this, currentDateAndTime);
		x.tvDate.setText(getString(R.string.last, pref.getDATE(this)));
	}

	void updateMountText() {
		if (isMounted()) mounted = getString(R.string.unmountt);
		else mounted = getString(R.string.mountt);
		x.tvMnt.setText(String.format(getString(R.string.mnt_title), mounted));
	}
	public static String getWin() {
		String partition = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1");
		return ShellUtils.fastCmd("realpath " + partition);
	}
	String updateWinPath() {
		winpath = pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows";
		return winpath;
	}

	public static String getBoot() {
		String partition = ShellUtils.fastCmd("find /dev/block | grep \"boot$(getprop ro.boot.slot_suffix)$\" | grep -E \"(/boot|/BOOT|/boot_a|/boot_b|/BOOT_a|/BOOT_b)$\" | head -1 ");
		Log.d("INFO", partition);
		return ShellUtils.fastCmd("realpath " + partition);
	}
	public static Boolean isMounted() {
		return !ShellUtils.fastCmd("su -mm -c mount | grep " + getWin()).isEmpty();
	}
	void openGroupLink() {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse("https://t.me/woahelperchat"));
		startActivity(i);
	}
}
