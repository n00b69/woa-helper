package id.kuato.woahelper.main;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import id.kuato.woahelper.widgets.MountWidget;

public class MainActivity extends AppCompatActivity {

	public static ActivityMainBinding x;
	public static SetPanelBinding k;
	public static ToolboxBinding n;
	public static ScriptsBinding z;
	public static Context context;
    String panel, finduefi, device, model, dbkpmodel, boot;
    static String mounted, win, winpath;
	String grouplink = "https://t.me/woahelperchat";
	String guidelink = "https://github.com/n00b69";
	boolean unsupported = false;
	boolean tablet = false;
	static int blur = 0;
	List<View> views = new ArrayList<>();

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
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
		copyAssets();
		x = ActivityMainBinding.inflate(getLayoutInflater());
		k = SetPanelBinding.inflate(getLayoutInflater());
		n = ToolboxBinding.inflate(getLayoutInflater());
		z = ScriptsBinding.inflate(getLayoutInflater());

		context = this;

		setContentView(x.getRoot());

		views.clear();
		views.add(x.getRoot());

		ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
			Insets sysInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			x.tvAppCreator.setPadding(0, 0, 0, sysInsets.bottom);
			x.app.setPadding(0, 0, 0, sysInsets.bottom);
			n.app.setPadding(0, 0, 0, sysInsets.bottom);
			k.app.setPadding(0, 0, 0, sysInsets.bottom);
			z.app.setPadding(0, 0, 0, sysInsets.bottom);
			x.linearLayout.setPadding(sysInsets.left, sysInsets.top, sysInsets.right, 0);
			n.linearLayout.setPadding(sysInsets.left, sysInsets.top, sysInsets.right, 0);
			k.linearLayout.setPadding(sysInsets.left, sysInsets.top, sysInsets.right, 0);
			z.linearLayout.setPadding(sysInsets.left, sysInsets.top, sysInsets.right, 0);
			return insets;
		});

		List<String> languages = new ArrayList<>();
		List<String> locales = new ArrayList<>();
		locales.add("und");
		languages.add("System Default");
		for (String i : BuildConfig.LOCALES) {
			locales.add(i.toLowerCase());
			Locale locale = LocaleListCompat.forLanguageTags(i).get(0);
            String country = locale.getDisplayCountry(locale);
			boolean c = !country.isEmpty();
			String lang = locale.getDisplayLanguage(locale)+ (c?" (":"")+country+ (c?")":"");
			languages.add(lang);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_dropdown_item,
				languages
		);
		setSupportActionBar(x.toolbarlayout.toolbar);
		x.toolbarlayout.toolbar.setTitle(R.string.app_name);
		x.toolbarlayout.toolbar.setSubtitle("v" + BuildConfig.VERSION_NAME);
		x.toolbarlayout.toolbar.setNavigationIcon(R.drawable.ic_launcher_foreground);
		x.toolbarlayout.settings.setColorFilter(R.color.md_theme_primary);
		k.toolbarlayout.settings.setColorFilter(R.color.md_theme_primary);
		n.toolbarlayout.settings.setColorFilter(R.color.md_theme_primary);
		z.toolbarlayout.settings.setColorFilter(R.color.md_theme_primary);

		device = ShellUtils.fastCmd("getprop ro.product.device");
		model = ShellUtils.fastCmd("getprop ro.product.model");
		win = getWin();
		boot = getBoot();
		updateWinPath();
		updateMountText();
		x.tvDate.setText(String.format(getString(R.string.last), pref.getDATE(this)));

		String slot = ShellUtils.fastCmd("getprop ro.boot.slot_suffix");
		if (slot.isEmpty()) x.tvSlot.setVisibility(View.GONE);
		else x.tvSlot.setText(getString(R.string.slot, slot.substring(1, 2)).toUpperCase());

		x.deviceName.setText(String.format("%s (%s)", model, device));
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
				guidelink = "https://github.com/WaLoVayu/POCOX3Pro-Windows-Guides";
				grouplink = "https://t.me/windowsonvayu";
				x.DeviceImage.setImageResource(R.drawable.vayu);
				x.tvPanel.setVisibility(View.VISIBLE);
			}
			case "cepheus" -> {
				guidelink = "https://github.com/fbernkastel228/Port-Windows-XiaoMI-9";
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
				grouplink = "https://t.me/lisawoa";
				x.DeviceImage.setImageResource(R.drawable.lisa);
			}
			case "nabu" -> {
				guidelink = "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5";
				grouplink = "https://t.me/nabuwoa";
				x.NabuImage.setImageResource(R.drawable.nabu);
				x.tvPanel.setVisibility(View.VISIBLE);
				n.cvDbkp.setVisibility(View.VISIBLE);
				n.cvFlashUefi.setVisibility(View.GONE);
				tablet = true;
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
				tablet = true;
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
				n.cvFlashUefi.setVisibility(View.GONE);
			}
			case "guacamole", "guacamolet", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7ProTMO" -> {
				guidelink = "https://github.com/n00b69/woa-op7";
				grouplink = "https://t.me/onepluswoachat";
				x.DeviceImage.setImageResource(R.drawable.guacamole);
				n.cvDumpModem.setVisibility(View.VISIBLE);
				n.cvDbkp.setVisibility(View.VISIBLE);
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
				tablet = true;
			}
			case "q2q" -> {
				guidelink = "https://project-aloha.github.io/";
				grouplink = "https://t.me/project_aloha_issues";
				x.NabuImage.setImageResource(R.drawable.q2q);
				tablet = true;
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
				tablet = true;
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
			case "redfin", "herolte", "crownlte" -> {
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
			case "brepdugl" -> {
				guidelink = "https://github.com/Project-Silicium/Mu-Silicium";
				grouplink = "https://discord.gg/Dx2QgMx7Sv";
				x.DeviceImage.setImageResource(R.drawable.unknown);
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
		onConfigurationChanged(getResources().getConfiguration());
		if (tablet) {
			x.NabuImage.setVisibility(View.VISIBLE);
			x.DeviceImage.setVisibility(View.GONE);
		} else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		if (unsupported && !pref.getAGREE(this)) {
			Dlg.show(this, R.string.unsupported);
			Dlg.setYes(R.string.sure, () -> {
				pref.setAGREE(this, true);
				Dlg.close();
			});
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
		if (!pref.getAGREE(this))
			if (Objects.equals(panel, "f1p2_2") || Objects.equals(panel, "f1")) {
				Dlg.show(this, R.string.upanel);
				Dlg.setYes(R.string.chat, () -> {
					openLink(grouplink);
					pref.setAGREE(this, true);
					Dlg.close();
				});
				Dlg.setDismiss(R.string.nah, () -> {
					pref.setAGREE(this, true);
					Dlg.close();
				});
				Dlg.setNo(R.string.later, Dlg::close);
			}

		x.tvRamvalue.setText(getString(R.string.ramvalue, Double.parseDouble(new RAM().getMemory(this))));
		x.tvPanel.setText(getString(R.string.paneltype, panel));
		x.cvGuide.setOnClickListener(v -> openLink(guidelink));
		x.cvGroup.setOnClickListener(v -> openLink(grouplink));

		 if (!BuildConfig.DEBUG) {
			checkdbkpmodel();
			checkupdate();
		}

		x.cvBackup.setOnClickListener(a -> {
			Dlg.show(this, R.string.backup_boot_question, R.drawable.ic_disk);
			Dlg.setDismiss(R.string.no, Dlg::close);
			Dlg.setNo(R.string.android, () -> {
				Dlg.dialogLoading();
				updateLastBackupDate();
				new Thread(() -> {
					androidBackup();
					runOnUiThread(()->{
						Dlg.setText(R.string.backuped);
						Dlg.dismissButton();
					});
				}).start();
			});
			Dlg.setYes(R.string.windows, () -> {
				Dlg.dialogLoading();
				updateLastBackupDate();
				new Thread(() -> {
					winBackup();
					runOnUiThread(()->{
						Dlg.setText(R.string.backuped);
						Dlg.dismissButton();
					});
				}).start();
			});
		});
		
		x.cvMnt.setOnClickListener(a -> { mountUI(); });

		x.cvQuickBoot.setOnClickListener(a -> {
			Dlg.show(this, R.string.quickboot_question, R.drawable.ic_launcher_foreground);
			Dlg.setNo(R.string.no, Dlg::close);
			Dlg.setYes(R.string.yes, () -> {
				Dlg.dialogLoading();
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
						if (!isNetworkConnected(this)) {
							String finddevcfg = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name OOS11_devcfg_*");
							if (finddevcfg.isEmpty()) {
								nointernet();
								return;
							}
						}
						String devcfgDevice = "";
						if ("guacamole".equals(device) || "OnePlus7Pro".equals(device) || "OnePlus7Pro4G".equals(device)) devcfgDevice = "guacamole";
						else if ("hotdog".equals(device) || "OnePlus7TPro".equals(device) || "OnePlus7TPro4G".equals(device)) devcfgDevice = "hotdog";
						String findoriginaldevcfg = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name original-devcfg.img");
						if (findoriginaldevcfg.isEmpty()) {
							ShellUtils.fastCmd("dd bs=8M if=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix) of=/sdcard/original-devcfg.img");
							ShellUtils.fastCmd("cp /sdcard/original-devcfg.img " + getFilesDir() + "/original-devcfg.img");
						}
						String finddevcfg = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name OOS11_devcfg_*");
						if (finddevcfg.isEmpty()) {
							ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_%s.img -O /sdcard/OOS11_devcfg_%s.img\" | su -mm -c sh", devcfgDevice, devcfgDevice));
							ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_%s.img -O /sdcard/OOS12_devcfg_%s.img\" | su -mm -c sh", devcfgDevice, devcfgDevice));
							ShellUtils.fastCmd(String.format("cp /sdcard/OOS11_devcfg_%s.img %s", devcfgDevice, getFilesDir()));
							ShellUtils.fastCmd(String.format("cp /sdcard/OOS12_devcfg_%s.img %s", devcfgDevice, getFilesDir()));
							ShellUtils.fastCmd(String.format("dd bs=8M if=%s/OOS11_devcfg_%s.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)", getFilesDir(), devcfgDevice));
						} else {
							ShellUtils.fastCmd(String.format("dd bs=8M if=%s/OOS11_devcfg_%s.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)", getFilesDir(), devcfgDevice));
						}
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
					Dlg.setText(R.string.wrong);
					Dlg.dismissButton();
				}, 25);
			});
		});

		x.cvToolbox.setOnClickListener(v -> {
			views.add(n.getRoot());
			x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
			setContentView(n.getRoot());
			n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
			n.toolbarlayout.toolbar.setTitle(getString(R.string.toolbox_title));
			n.toolbarlayout.toolbar.setNavigationIcon(getDrawable(R.drawable.ic_launcher_foreground));
		});

		n.cvSta.setOnClickListener(a -> {
			Dlg.show(this, R.string.sta_question, R.drawable.android);
			Dlg.setNo(R.string.no, Dlg::close);
			Dlg.setYes(R.string.yes, () -> {
				ShellUtils.fastCmd("mkdir /sdcard/sta || true");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/sta.exe /sdcard/sta/sta.exe");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/sdd.exe /sdcard/sta/sdd.exe");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/sdd.conf /sdcard/sta/sdd.conf");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/boot_img_auto-flasher_V1.1.exe /sdcard/sta/boot_img_auto-flasher_V1.1.exe");
				mount();
				if (!isMounted()) {
					Dlg.close();
					mountfail();
					return;
				}
				ShellUtils.fastCmd("mkdir " + winpath + "/sta || true ");
				ShellUtils.fastCmd("mkdir " + winpath + "/ProgramData/sta || true ");
				ShellUtils.fastCmd("cp '" + getFilesDir() + "/Switch to Android.lnk' " + winpath + "/Users/Public/Desktop");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/sta.exe " + winpath + "/ProgramData/sta/sta.exe");
				ShellUtils.fastCmd("mv /sdcard/sta " + winpath );

				Dlg.clearButtons();
				Dlg.setText(R.string.done);
				Dlg.dismissButton();
			});
		});
		
		n.cvScripts.setOnClickListener(v -> {
			views.add(z.getRoot());
			n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
			setContentView(z.getRoot());
			z.scriptstab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
			z.toolbarlayout.toolbar.setTitle(R.string.script_title);
			z.toolbarlayout.toolbar.setNavigationIcon(R.drawable.ic_launcher_foreground);
		});
		
		n.cvDumpModem.setOnClickListener(a -> {
			Dlg.show(this, R.string.dump_modem_question, R.drawable.ic_modem);
			Dlg.setNo(R.string.no, Dlg::close);
			Dlg.setYes(R.string.yes, () -> {
				Dlg.dialogLoading();
				if (!isMounted()) mount();
				new Thread(() -> {
					dump();
					runOnUiThread(()-> {
						Dlg.setText(R.string.lte);
						Dlg.dismissButton();
					});
				}).start();
			});
		});
		
		n.cvFlashUefi.setOnClickListener(a -> {
			Dlg.show(this, R.string.flash_uefi_question, R.drawable.ic_uefi);
			Dlg.setNo(R.string.no, Dlg::close);
			Dlg.setYes(R.string.yes,  () -> {
				Dlg.dialogLoading();
				new Thread(() -> {
					try {
						flash(finduefi);
						runOnUiThread(()->{
							Dlg.setText(R.string.flash);
							Dlg.dismissButton();
						});
					} catch (Exception error) {
						error.printStackTrace();
					}
				}).start();
			});
		});
		
		n.cvDbkp.setOnClickListener(a -> {
			if (!isNetworkConnected(this)) {
				nointernet();
				return;
			}
			if ("nabu".equals(device)) {
				Dlg.show(this, getString(R.string.dbkp_question, "MI PAD 5"), R.drawable.ic_uefi);
				Dlg.setNo(R.string.no, Dlg::close);
				Dlg.setYes(R.string.nabu, () -> {
					ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp.nabu.bin /sdcard/dbkp/dbkp.bin");
					ShellUtils.fastCmd("cp " + getFilesDir() + "/dbkp8150.cfg /sdcard/dbkp/dbkp.cfg");
					Dlg.dialogLoading();
					kernelPatch(getString(R.string.nabu),"https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/releases/download/1.0/nabu.fd");
				});
				Dlg.setDismiss(R.string.nabu2, () -> {
					Dlg.dialogLoading();
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
							Dlg.setText(getString(R.string.dbkp, getString(R.string.nabu2)));
							Dlg.setDismiss(R.string.dismiss, Dlg::close);
							Dlg.setNo(R.string.reboot, () -> {
								ShellUtils.fastCmd("su -c svc power reboot");
							});
						});
					}).start();
				});
			} else {
				Dlg.show(this, getString(R.string.dbkp_question, dbkpmodel), R.drawable.ic_uefi);
				Dlg.setNo(R.string.no, Dlg::close);
				Dlg.setYes(R.string.yes, () -> {
					Dlg.dialogLoading();
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
						if ("guacamole".equals(device) || "OnePlus7Pro".equals(device) || "OnePlus7Pro4G".equals(device)) {
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
						} else if ("hotdog".equals(device) || "OnePlus7TPro".equals(device) || "OnePlus7TPro4G".equals(device)) {
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
						} else if ("cepheus".equals(device)) {
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
						}
						runOnUiThread(()->{
							if ("guacamole".equals(device) || "OnePlus7Pro".equals(device) || "OnePlus7Pro4G".equals(device) || "hotdog".equals(device) || "OnePlus7TPro".equals(device) || "OnePlus7TPro4G".equals(device)) {
								Dlg.setText(getString(R.string.dbkp, getString(R.string.op7)));
							} else if ("cepheus".equals(device)) {
								Dlg.setText(getString(R.string.dbkp, getString(R.string.cepheus)));
							}
							Dlg.setDismiss(R.string.dismiss, Dlg::close);
							Dlg.setNo(R.string.reboot, () -> {
								ShellUtils.fastCmd("su -c svc power reboot");
							});
						});
					}).start();
				});
			}
		});
		
		n.cvDevcfg.setOnClickListener(a -> {
			if (!isNetworkConnected(this)) {
				String finddevcfg = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name OOS11_devcfg_*");
				if (finddevcfg.isEmpty()) {
					nointernet();
					return;
				}
			}
			Dlg.show(this, getString(R.string.devcfg_question, dbkpmodel), R.drawable.ic_uefi);
			Dlg.setNo(R.string.no, Dlg::close);
			Dlg.setYes(R.string.yes, () -> {
				Dlg.dialogLoading();
				new Thread(()->{
					String devcfgDevice = "";
					if ("guacamole".equals(device) || "OnePlus7Pro".equals(device) || "OnePlus7Pro4G".equals(device)) devcfgDevice = "guacamole";
					else if ("hotdog".equals(device) || "OnePlus7TPro".equals(device) || "OnePlus7TPro4G".equals(device)) devcfgDevice = "hotdog";
					String findoriginaldevcfg = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name original-devcfg.img");
					if (findoriginaldevcfg.isEmpty()) {
						ShellUtils.fastCmd("dd bs=8M if=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix) of=/sdcard/original-devcfg.img");
						ShellUtils.fastCmd("cp /sdcard/original-devcfg.img " + getFilesDir() + "/original-devcfg.img");
					}
					String finddevcfg = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name OOS11_devcfg_*");
					if (finddevcfg.isEmpty()) {
						ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_%s.img -O /sdcard/OOS11_devcfg_%s.img\" | su -mm -c sh", devcfgDevice, devcfgDevice));
						ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_%s.img -O /sdcard/OOS12_devcfg_%s.img\" | su -mm -c sh", devcfgDevice, devcfgDevice));
						ShellUtils.fastCmd(String.format("cp /sdcard/OOS11_devcfg_%s.img %s", devcfgDevice, getFilesDir()));
						ShellUtils.fastCmd(String.format("cp /sdcard/OOS12_devcfg_%s.img %s", devcfgDevice, getFilesDir()));
						ShellUtils.fastCmd(String.format("dd bs=8M if=%s/OOS11_devcfg_%s.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)", getFilesDir(), devcfgDevice));
					} else {
						ShellUtils.fastCmd(String.format("dd bs=8M if=%s/OOS11_devcfg_%s.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)", getFilesDir(), devcfgDevice));
					}
					runOnUiThread(()->{
						mount();
						String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
						if (mnt_stat.isEmpty()) {
							ShellUtils.fastCmd("cp " + getFilesDir() + "/sdd.exe /sdcard/sdd.exe");
							ShellUtils.fastCmd("cp " + getFilesDir() + "/devcfg-sdd.conf /sdcard/sdd.conf");
							Dlg.close();
							mountfail();
						} else {
							ShellUtils.fastCmd("mkdir " + winpath + "/sta || true ");
							ShellUtils.fastCmd("cp '" + getFilesDir() + "/Flash Devcfg.lnk' " + winpath + "/Users/Public/Desktop");
							ShellUtils.fastCmd("cp " + getFilesDir() + "/sdd.exe " + winpath + "/sta/sdd.exe");
							ShellUtils.fastCmd("cp " + getFilesDir() + "/devcfg-sdd.conf " + winpath + "/sta/sdd.conf");
							ShellUtils.fastCmd("cp /sdcard/original-devcfg.img " + winpath + "/original-devcfg.img");
						}
						Dlg.setText(R.string.devcfg);
						Dlg.setDismiss(R.string.dismiss, Dlg::close);
						Dlg.setYes(R.string.reboot, () -> {
							ShellUtils.fastCmd("su -c svc power reboot");
						});
					});
				}).start();
			});
		});
		
		n.cvSoftware.setOnClickListener(a -> {
			Dlg.show(this, R.string.software_question, R.drawable.ic_sensor);
			Dlg.setNo(R.string.no, Dlg::close);
			Dlg.setYes(R.string.yes, () -> {
				Dlg.dialogLoading();
				ShellUtils.fastCmd("cp " + getFilesDir() + "/WorksOnWoa.url /sdcard");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/TestedSoftware.url /sdcard");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/ARMSoftware.url /sdcard");
				ShellUtils.fastCmd("cp " + getFilesDir() + "/ARMRepo.url /sdcard");
				mount();
				if (!isMounted()) {
					Dlg.close();
					mountfail();
					return;
				}
				ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
				ShellUtils.fastCmd("cp /sdcard/WorksOnWoa.url " + winpath + "/Toolbox");
				ShellUtils.fastCmd("cp /sdcard/TestedSoftware.url " + winpath + "/Toolbox");
				ShellUtils.fastCmd("cp /sdcard/ARMSoftware.url " + winpath + "/Toolbox");
				ShellUtils.fastCmd("cp /sdcard/ARMRepo.url " + winpath + "/Toolbox");
				ShellUtils.fastCmd("rm /sdcard/WorksOnWoa.url");
				ShellUtils.fastCmd("rm /sdcard/TestedSoftware.url");
				ShellUtils.fastCmd("rm /sdcard/ARMSoftware.url");
				ShellUtils.fastCmd("rm /sdcard/ARMRepo.url");
				Dlg.setText(R.string.done);
				Dlg.dismissButton();
			});
		});

		n.cvAtlasos.setOnClickListener(a -> {
			if (!isNetworkConnected(this)) {
				nointernet();
				return;
			}
			Dlg.show(this, R.string.atlasos_question, R.drawable.ic_ar_mainactivity);
			Dlg.dismissButton();
			Dlg.setNo(R.string.revios, () -> {
				Dlg.dialogLoading();
				Dlg.setBar(0);
				Dlg.setIcon(R.drawable.ic_download);
				new Thread(() -> {
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/modified-playbooks/releases/download/ReviOS/ReviPlaybook.apbx -O /sdcard/ReviPlaybook.apbx\" | su -mm -c sh");
					Dlg.setBar(50);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://download.ameliorated.io/AME%20Wizard%20Beta.zip -O /sdcard/AMEWizardBeta.zip\" | su -mm -c sh");
					Dlg.setBar(80);
					runOnUiThread(() -> {
						mount();
						if (!isMounted()) {
							Dlg.close();
							mountfail();
							return;
						}
						Dlg.setIcon(R.drawable.ic_ar_mainactivity);
						Dlg.hideBar();
						ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
						ShellUtils.fastCmd("cp /sdcard/ReviPlaybook.apbx " + winpath + "/Toolbox/ReviPlaybook.apbx");
						ShellUtils.fastCmd("cp /sdcard/AMEWizardBeta.zip " + winpath + "/Toolbox");
						ShellUtils.fastCmd("su -mm -c rm /sdcard/ReviPlaybook.apbx");
						ShellUtils.fastCmd("su -mm -c rm /sdcard/AMEWizardBeta.zip");
						Dlg.setText(R.string.done);
						Dlg.dismissButton();
					});
				}).start();
			});
			Dlg.setYes(R.string.atlasos, () -> {
				Dlg.dialogLoading();
				Dlg.setBar(0);
				Dlg.setIcon(R.drawable.ic_download);
				new Thread(() -> {
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/modified-playbooks/releases/download/AtlasOS/AtlasPlaybook.apbx -O /sdcard/AtlasPlaybook_v0.4.1.apbx\" | su -mm -c sh");
					Dlg.setBar(35);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/modified-playbooks/releases/download/AtlasOS/AtlasPlaybook_v0.4.0_23H2Only.apbx -O /sdcard/AtlasPlaybook_v0.4.0_23H2Only.apbx\" | su -mm -c sh");
					Dlg.setBar(60);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://download.ameliorated.io/AME%20Wizard%20Beta.zip -O /sdcard/AMEWizardBeta.zip\" | su -mm -c sh");
					Dlg.setBar(80);
					runOnUiThread(() -> {
						mount();
						if (!isMounted()) {
							Dlg.close();
							mountfail();
							return;
						}
						Dlg.setIcon(R.drawable.ic_ar_mainactivity);
						Dlg.hideBar();
						ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
						ShellUtils.fastCmd("cp /sdcard/AtlasPlaybook.apbx " + winpath + "/Toolbox/AtlasPlaybook_v0.4.1.apbx");
						ShellUtils.fastCmd("cp /sdcard/AtlasPlaybook.apbx " + winpath + "/Toolbox/AtlasPlaybook_v0.4.0_23H2Only.apbx");
						ShellUtils.fastCmd("cp /sdcard/AMEWizardBeta.zip " + winpath + "/Toolbox");
						ShellUtils.fastCmd("su -mm -c rm /sdcard/AtlasPlaybook_v0.4.1.apbx");
						ShellUtils.fastCmd("su -mm -c rm /sdcard/AtlasPlaybook_v0.4.0_23H2Only.apbx");
						ShellUtils.fastCmd("su -mm -c rm /sdcard/AMEWizardBeta.zip");
						Dlg.setText(R.string.done);
						Dlg.dismissButton();
					});
				}).start();
			});
		});

		z.cvUsbhost.setOnClickListener(a -> {
			Dlg.show(this, R.string.usbhost_question, R.drawable.ic_mnt);
			Dlg.setNo(R.string.no, Dlg::close);
			Dlg.setYes(R.string.yes, () -> {
				Dlg.dialogLoading();
				ShellUtils.fastCmd("cp " + getFilesDir() + "/usbhostmode.exe /sdcard");
				ShellUtils.fastCmd("cp '" + getFilesDir() + "/USB Host Mode.lnk' /sdcard");
				mount();
				if (!isMounted()) {
					Dlg.close();
					mountfail();
					return;
				}
				ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
				ShellUtils.fastCmd("cp /sdcard/usbhostmode.exe " + winpath + "/Toolbox");
				ShellUtils.fastCmd("cp '" + getFilesDir() + "/USB Host Mode.lnk' " + winpath + "/Users/Public/Desktop");
				ShellUtils.fastCmd("rm /sdcard/usbhostmode.exe");
				Dlg.setText(R.string.done);
				Dlg.dismissButton();
			});
		});
		
		z.cvRotation.setOnClickListener(a -> {
			Dlg.show(this, R.string.rotation_question, R.drawable.ic_disk);
			Dlg.setNo(R.string.no, Dlg::close);
			Dlg.setYes(R.string.yes, () -> {
				Dlg.dialogLoading();
				ShellUtils.fastCmd("cp " + getFilesDir() + "/QuickRotate_V3.0.exe /sdcard/");
				mount();
				if (!isMounted()) {
					Dlg.close();
					mountfail();
					return;
				}
				ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
				ShellUtils.fastCmd("cp /sdcard/QuickRotate_V3.0.exe " + winpath + "/Toolbox");
				ShellUtils.fastCmd("cp /sdcard/QuickRotate_V3.0.exe " + winpath + "/Users/Public/Desktop");
				ShellUtils.fastCmd("rm /sdcard/QuickRotate_V3.0.exe");
				Dlg.setText(R.string.done);
				Dlg.dismissButton();
			});
		});
		
		z.cvTablet.setOnClickListener(a -> {
			Dlg.show(this, R.string.tablet_question, R.drawable.ic_sensor);
			Dlg.setNo(R.string.no, Dlg::close);
			Dlg.setYes(R.string.yes, () -> {
				Dlg.dialogLoading();
				ShellUtils.fastCmd("cp " + getFilesDir() + "/Optimized_Taskbar_Control_V3.1.exe /sdcard/");
				mount();
				if (!isMounted()) {
					Dlg.close();
					mountfail();
					return;
				}
				ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
				ShellUtils.fastCmd("cp /sdcard/Optimized_Taskbar_Control_V3.1.exe " + winpath + "/Toolbox");
				ShellUtils.fastCmd("rm /sdcard/Optimized_Taskbar_Control_V3.1.exe");
				Dlg.setText(R.string.done);
				Dlg.dismissButton();
			});
		});

		z.cvSetup.setOnClickListener(a -> {
			if (!isNetworkConnected(this)) {
				nointernet();
				return;
			}
			Dlg.show(this, R.string.setup_question, R.drawable.ic_mnt);
			Dlg.setNo(R.string.no, Dlg::close);
			Dlg.setYes(R.string.yes, () -> {
				Dlg.dialogLoading();
				Dlg.setIcon(R.drawable.ic_download);
				Dlg.setBar(0);
				new Thread(() -> {
					ShellUtils.fastCmd("mkdir /sdcard/Frameworks || true");
					ShellUtils.fastCmd("cp " + getFilesDir() + "/install.bat /sdcard/Frameworks/install.bat");
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/PhysX-9.13.0604-SystemSoftware-Legacy.msi -O /sdcard/Frameworks/PhysX-9.13.0604-SystemSoftware-Legacy.msi\" | su -mm -c sh");
					Dlg.setBar(10);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/PhysX_9.23.1019_SystemSoftware.exe -O /sdcard/Frameworks/PhysX_9.23.1019_SystemSoftware.exe\" | su -mm -c sh");
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/xnafx40_redist.msi -O /sdcard/Frameworks/xnafx40_redist.msi\" | su -mm -c sh");
					Dlg.setBar(20);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/opengl.appx -O /sdcard/Frameworks/opengl.1.2409.2.0.appx\" | su -mm -c sh");
					Dlg.setBar(30);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2005vcredist_x64.EXE -O /sdcard/Frameworks/2005vcredist_x64.EXE\" | su -mm -c sh");
					Dlg.setBar(35);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2005vcredist_x86.EXE -O /sdcard/Frameworks/2005vcredist_x86.EXE\" | su -mm -c sh");
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2008vcredist_x64.exe -O /sdcard/Frameworks/2008vcredist_x64.exe\" | su -mm -c sh");
					Dlg.setBar(40);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2008vcredist_x86.exe -O /sdcard/Frameworks/2008vcredist_x86.exe\" | su -mm -c sh");
					Dlg.setBar(45);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2010vcredist_x64.exe -O /sdcard/Frameworks/2010vcredist_x64.exe\" | su -mm -c sh");
					Dlg.setBar(50);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2010vcredist_x86.exe -O /sdcard/Frameworks/2010vcredist_x86.exe\" | su -mm -c sh");
					Dlg.setBar(55);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2012vcredist_x64.exe -O /sdcard/Frameworks/2012vcredist_x64.exe\" | su -mm -c sh");
					Dlg.setBar(60);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2012vcredist_x86.exe -O /sdcard/Frameworks/2012vcredist_x86.exe\" | su -mm -c sh");
					Dlg.setBar(65);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2013vcredist_x64.exe -O /sdcard/Frameworks/2013vcredist_x64.exe\" | su -mm -c sh");
					Dlg.setBar(70);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2013vcredist_x86.exe -O /sdcard/Frameworks/2013vcredist_x86.exe\" | su -mm -c sh");
					Dlg.setBar(75);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2015VC_redist.x64.exe -O /sdcard/Frameworks/2015VC_redist.x64.exe\" | su -mm -c sh");
					Dlg.setBar(80);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2015VC_redist.x86.exe -O /sdcard/Frameworks/2015VC_redist.x86.exe\" | su -mm -c sh");
					Dlg.setBar(85);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/2022VC_redist.arm64.exe -O /sdcard/Frameworks/2022VC_redist.arm64.exe\" | su -mm -c sh");
					Dlg.setBar(90);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/dxwebsetup.exe -O /sdcard/Frameworks/dxwebsetup.exe\" | su -mm -c sh");
					Dlg.setBar(95);
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/oalinst.exe -O /sdcard/Frameworks/oalinst.exe\" | su -mm -c sh");
					Dlg.setBar(1);
					runOnUiThread(() -> {
						mount();
						if (!isMounted()) {
							Dlg.close();
							mountfail();
							return;
						}
						ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
						ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox/Frameworks || true ");
						ShellUtils.fastCmd("cp /sdcard/Frameworks/* " + winpath + "/Toolbox/Frameworks");
						ShellUtils.fastCmd("rm -r /sdcard/Frameworks");
						Dlg.setIcon(R.drawable.ic_mnt);
						Dlg.hideBar();
						Dlg.setText(R.string.done);
						Dlg.dismissButton();
					});
				}).start();
			});
		});

		z.cvDefender.setOnClickListener(a -> {
			Dlg.show(this, R.string.defender_question, R.drawable.edge2);
			Dlg.setNo(R.string.no, Dlg::close);
			Dlg.setYes(R.string.yes, () -> {
				Dlg.dialogLoading();
				String finddefenderremover = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name DefenderRemover.exe");
				if (finddefenderremover.isEmpty()) {
					if (!isNetworkConnected(this)) {
						Dlg.close();
						nointernet();
						return;
					}
					ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/DefenderRemover.exe -O /sdcard/DefenderRemover.exe\" | su -mm -c sh");
					ShellUtils.fastCmd("cp /sdcard/DefenderRemover.exe " + getFilesDir());
					ShellUtils.fastCmd("cp " + getFilesDir() + "/RemoveEdge.bat /sdcard");
					mount();
					if (!isMounted()) {
						Dlg.close();
						mountfail();
						return;
					}
					ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
					ShellUtils.fastCmd("cp " + getFilesDir() + "/DefenderRemover.exe " + winpath + "/Toolbox");
					ShellUtils.fastCmd("rm /sdcard/DefenderRemover.exe");
					ShellUtils.fastCmd("cp /sdcard/RemoveEdge.bat " + winpath + "/Toolbox");
					ShellUtils.fastCmd("rm /sdcard/RemoveEdge.bat");
					Dlg.setText(R.string.done);
					Dlg.dismissButton();
				} else {
					ShellUtils.fastCmd("cp " + getFilesDir() + "/RemoveEdge.bat /sdcard");
					ShellUtils.fastCmd("cp " + getFilesDir() + "/DefenderRemover.exe /sdcard");
					mount();
					if (!isMounted()) {
						Dlg.close();
						mountfail();
						return;
					}
					ShellUtils.fastCmd("mkdir " + winpath + "/Toolbox || true ");
					ShellUtils.fastCmd("cp " + getFilesDir() + "/DefenderRemover.exe " + winpath + "/Toolbox");
					ShellUtils.fastCmd("cp " + getFilesDir() + "/RemoveEdge.bat " + winpath + "/Toolbox");
					ShellUtils.fastCmd("rm /sdcard/DefenderRemover.exe");
					ShellUtils.fastCmd("rm /sdcard/RemoveEdge.bat");
					Dlg.setText(R.string.done);
					Dlg.dismissButton();
				}
			});
		});
		
		k.toolbarlayout.toolbar.setTitle(R.string.preferences);
		k.toolbarlayout.toolbar.setNavigationIcon(R.drawable.ic_launcher_foreground);
		View.OnClickListener settingsIconClick = v -> {
			views.add(k.getRoot());
			views.get(views.size() - 2).startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out));
			setContentView(k.getRoot());
			views.get(views.size() - 1).startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in));
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
			AppCompatSpinner langSpinner = findViewById(R.id.languages);
			langSpinner.setAdapter(adapter);
			Locale l=AppCompatDelegate.getApplicationLocales().get(0);
			if(l!=null) {
				l=new Locale(l.toLanguageTag());
				int index = locales.indexOf(l.toString());
				langSpinner.setSelection(index);
			}
			langSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if(languages.get(position)=="System Default")
						AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
					else
						AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locales.get(position)));

				}
				@Override
				public void onNothingSelected(AdapterView<?> parent) {}
			});
		};
		x.toolbarlayout.settings.setOnClickListener(settingsIconClick);
		n.toolbarlayout.settings.setOnClickListener(settingsIconClick);
		z.toolbarlayout.settings.setOnClickListener(settingsIconClick);
		
		k.mountLocation.setOnCheckedChangeListener((compoundButton, b) -> {
			pref.setMountLocation(this, b);
			updateWinPath();
		});
		
		k.backupQB.setOnCheckedChangeListener((compoundButton, b) -> {
			if (pref.getBACKUP(this)) {
				pref.setBACKUP(this, false);
				k.autobackup.setVisibility(View.VISIBLE);
				return;
			}
			Dlg.show(this, R.string.bwarn);
			Dlg.onCancel(() -> k.backupQB.setChecked(false));
			Dlg.setDismiss(R.string.cancel, () -> {
				k.backupQB.setChecked(false);
				Dlg.close();
			});
			Dlg.setYes(R.string.agree, () -> {
				pref.setBACKUP(this, true);
				k.autobackup.setVisibility(View.GONE);
				Dlg.close();
			});
		});

		k.backupQBA.setOnCheckedChangeListener((compoundButton, b) -> {
			if (pref.getBACKUP_A(this)) {
				pref.setBACKUP_A(this, false);
				k.autobackupA.setVisibility(View.VISIBLE);
				return;
			}
			Dlg.show(this, R.string.bwarn);
			Dlg.onCancel(() -> k.backupQBA.setChecked(false));
			Dlg.setDismiss(R.string.cancel, () -> {
				k.backupQBA.setChecked(false);
				Dlg.close();
			});
			Dlg.setYes(R.string.agree, () -> {
				pref.setBACKUP_A(this, true);
				k.autobackupA.setVisibility(View.GONE);
				Dlg.close();
			});
		});
		


		x.cvInfo.setOnClickListener(a -> {
			if (BuildConfig.DEBUG) return;
			if (!isNetworkConnected(this)) {
				nointernet();
				return;
			}
			Dlg.show(this, R.string.please_wait);
			String version = ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget -q -O - https://raw.githubusercontent.com/n00b69/woa-helper-update/main/README.md\" | su -mm -c sh");
			if (BuildConfig.VERSION_NAME.equals(version)) {
				Dlg.setText(getString(R.string.no) + " " + getString(R.string.update1));
				Dlg.dismissButton();
				return;
			}
			Dlg.setText(R.string.update1);
			Dlg.setNo(R.string.later, Dlg::close);
			Dlg.setYes(R.string.update, () -> {
				Dlg.clearButtons();
				Dlg.setText(getString(R.string.update2) + "\n" + getString(R.string.please_wait));
				update();
			});
		});
			
		k.autobackup.setOnCheckedChangeListener((compoundButton, b) -> pref.setAUTO(this, !b));
		k.autobackupA.setOnCheckedChangeListener((compoundButton, b) -> pref.setAUTO_A(this, !b));
		k.confirmation.setOnCheckedChangeListener((compoundButton, b) -> pref.setCONFIRM(this, b));
		k.securelock.setOnCheckedChangeListener((compoundButton, b) -> pref.setSecure(this, !b));
		k.automount.setOnCheckedChangeListener((compoundButton, b) -> pref.setAutoMount(this, b));
		k.appUpdate.setOnCheckedChangeListener((compoundButton, b) -> pref.setAppUpdate(this, b));
		String op7funny = ShellUtils.fastCmd("getprop ro.boot.vendor.lge.model.name");
		if (("guacamole".equals(device) || "guacamolet".equals(device) || "OnePlus7Pro".equals(device) || "OnePlus7Pro4G".equals(device) || "OnePlus7ProTMO".equals(device) || "hotdog".equals(device) || "OnePlus7TPro".equals(device) || "OnePlus7TPro4G".equals(device)) && (op7funny.contains("LM") || op7funny.contains("OPPO"))) {
		//String op7funny = ShellUtils.fastCmd("getprop persist.camera.privapp.list");
		//if (("guacamole".equals(device) || "guacamolet".equals(device) || "OnePlus7Pro".equals(device) || "OnePlus7Pro4G".equals(device) || "OnePlus7ProTMO".equals(device) || "hotdog".equals(device) || "OnePlus7TPro".equals(device) || "OnePlus7TPro4G".equals(device)) && (op7funny.contains("oppo") || op7funny.contains("OPPO"))) {
			k.devcfg1.setOnCheckedChangeListener((compoundButton, b) -> {pref.setDevcfg1(this, b);if(b)k.devcfg2.setVisibility(View.VISIBLE); else k.devcfg2.setVisibility(View.GONE);pref.setDevcfg2(this, false);});
			k.devcfg2.setOnCheckedChangeListener((compoundButton, b) -> pref.setDevcfg2(this, b));
			n.cvDevcfg.setVisibility(View.VISIBLE);
		} else {
			k.devcfg1.setVisibility(View.GONE);
			k.devcfg2.setVisibility(View.GONE);
			pref.setDevcfg1(this, false);
			pref.setDevcfg2(this, false);
		}
		k.button.setOnClickListener(v -> {
			onBackPressed();
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		checkwin();
		checkuefi();
		if (!Shell.isAppGrantedRoot()) {
			Dlg.show(this, R.string.nonroot);
			Dlg.setCancelable(false);
		}
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, MountWidget.class));
		new MountWidget().onUpdate(this, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onBackPressed() {
		if (views.size() - 1 == 0) {
			super.onBackPressed();
			finish();
			return;
		}
		views.get(views.size() - 1).startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_out));
		views.remove(views.size() - 1);
		setContentView(views.get(views.size() - 1));
		views.get(views.size() - 1).startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_in));
	}

	public void flash(String uefi) {
		ShellUtils.fastCmd("dd if=" + uefi + " of=/dev/block/bootdevice/by-name/boot$(getprop ro.boot.slot_suffix) bs=16m");
	}

	public static void mountUI() {
		if (winpath == null) updateWinPath();
		Dlg.show(context, isMounted() ?  context.getString(R.string.unmount_question) : context.getString(R.string.mount_question, winpath), R.drawable.ic_mnt);
		Dlg.setNo(R.string.no, Dlg::close);
		Dlg.setYes(R.string.yes, () -> {
			Dlg.dialogLoading();
			new Handler().postDelayed(() -> {
				Log.d("debug", winpath);
				if (isMounted()) {
					unmount();
					Dlg.setText(R.string.unmounted);
					Dlg.dismissButton();
					return;
				}
				mount();
				if (isMounted()) {
					Dlg.setText(String.format("%s\n%s", context.getString(R.string.mounted), winpath));
					MountWidget.updateText(context, context.getString(R.string.mnt_title, context.getString(R.string.unmountt)));
					Dlg.dismissButton();
					return;
				}
				Dlg.hideIcon();
				Dlg.setText(R.string.mountfail);
				Dlg.setYes(R.string.chat, () -> openLink("https://t.me/woahelperchat"));
			}, 25);
		});
	}
	
	public static void mount() {
		if (win == null) win = getWin();
		ShellUtils.fastCmd("mkdir " + winpath + " || true");
		ShellUtils.fastCmd("cd " + context.getFilesDir());
		ShellUtils.fastCmd("su -mm -c ./mount.ntfs " + win + " " + winpath);
		String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
		if (mnt_stat.isEmpty()) {
			pref.setMountLocation(context,true);
			updateWinPath();
			ShellUtils.fastCmd("mkdir " + winpath + " || true");
			ShellUtils.fastCmd("cd " + context.getFilesDir());
			ShellUtils.fastCmd("su -mm -c ./mount.ntfs " + win + " " + winpath);
		}
		updateMountText();
	}
	
	public static void unmount() {
		ShellUtils.fastCmd("su -mm -c umount " + winpath);
		ShellUtils.fastCmd("rmdir " + winpath);
		updateMountText();
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

	public void checkdbkpmodel() {
		dbkpmodel = ShellUtils.fastCmd("getprop ro.product.device");
        switch (dbkpmodel) {
            case "guacamole", "guacamolet", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7ProTMO" -> dbkpmodel = "ONEPLUS 7 PRO";
            case "hotdog", "OnePlus7TPro", "OnePlus7TPro4G" -> dbkpmodel = "ONEPLUS 7T PRO";
            case "cepheus" -> dbkpmodel = "XIAOMI MI 9";
            case "nabu" -> dbkpmodel = "XIAOMI PAD 5";
        //    case "pipa" -> dbkpmodel = "XIAOMI PAD 6";
            default -> dbkpmodel = "UNSUPPORTED";
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
		if (!win.isEmpty()) return;
		Dlg.show(this, R.string.partition);
		x.cvMnt.setEnabled(false);
		x.cvToolbox.setEnabled(false);
		x.cvQuickBoot.setEnabled(false);
		n.cvFlashUefi.setEnabled(false);
	}

	public void checkupdate() {
		if (pref.getAppUpdate(this)) return;
		if (!MainActivity.isNetworkConnected(this)) return;

		String version = ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget -q -O - https://raw.githubusercontent.com/n00b69/woa-helper-update/main/README.md\" | su -mm -c sh");
		if (BuildConfig.VERSION_NAME.equals(version)) return;

		Dlg.show(this, R.string.update1);
		Dlg.setNo(R.string.later, Dlg::close);
		Dlg.setYes(R.string.update, () -> {
			Dlg.clearButtons();
			Dlg.setText(getString(R.string.update2) + "\n" + getString(R.string.please_wait));
			update();
		});
	}
	
	public void update() {
		new Thread(() -> {
			ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://raw.githubusercontent.com/n00b69/woa-helper-update/main/woahelper.apk -O %s\" | su -mm -c sh", getFilesDir() + "/woahelper.apk"));
			ShellUtils.fastCmd("pm install " + getFilesDir() + "/woahelper.apk && rm " + getFilesDir() + "/woahelper.apk");
		}).start();
	}
	
	public void mountfail() {
		Dlg.show(this, getString(R.string.mountfail) + "\n" + getString(R.string.internalstorage));
		Dlg.setDismiss(R.string.cancel, Dlg::close);
		Dlg.setYes(R.string.chat, () -> openLink("https://t.me/woahelperchat"));
	}
	
	public void nointernet() {
		Dlg.show(this, R.string.internet);
		Dlg.dismissButton();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (getTargetViewGroup().getId() != R.id.mainlayout)
			return;
		if (Configuration.ORIENTATION_PORTRAIT == newConfig.orientation && tablet) {
			x.app.setOrientation(LinearLayout.VERTICAL);
			x.top.setOrientation(LinearLayout.HORIZONTAL);
		}
		else if (Configuration.ORIENTATION_LANDSCAPE == newConfig.orientation && tablet) {
			x.app.setOrientation(LinearLayout.HORIZONTAL);
			x.top.setOrientation(LinearLayout.VERTICAL);
		}
	}

	static void showBlur() {
		blur++;
		try {
			x.blur.setVisibility(View.VISIBLE);
			k.blur.setVisibility(View.VISIBLE);
			n.blur.setVisibility(View.VISIBLE);
			z.blur.setVisibility(View.VISIBLE);
		} catch (Exception ignored) {};
	}
	static void hideBlur(boolean check) {
		if (!check) blur = 1;
		blur--;
		if (blur > 0) return;
		try {
			x.blur.setVisibility(View.GONE);
			k.blur.setVisibility(View.GONE);
			n.blur.setVisibility(View.GONE);
			z.blur.setVisibility(View.GONE);
		} catch (Exception ignored) {};
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

	public static void updateMountText() {
		if (isMounted()) mounted = context.getString(R.string.unmountt);
		else mounted = context.getString(R.string.mountt);
		if (x != null) x.tvMnt.setText(String.format(context.getString(R.string.mnt_title), mounted));
		MountWidget.updateText(context, String.format(context.getString(R.string.mnt_title), mounted));
	}
	public static String getWin() {
		String partition = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1");
		return ShellUtils.fastCmd("realpath " + partition);
	}
	public static String updateWinPath() {
		winpath = pref.getMountLocation(context) ? "/mnt/Windows" : "/mnt/sdcard/Windows";
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
	static void openLink(String link) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(link));
		context.startActivity(i);
	}

	void kernelPatch(String message,String link) {
		new Thread(new Runnable() {
			private String message;
			private String link;
			public Runnable init(String parameter, String parameter2) {
				this.message = parameter;
				this.link = parameter2;
				return this;
			}

			@Override
			public void run() {
				ShellUtils.fastCmd("mkdir /sdcard/dbkp || true");
				androidBackup();
				ShellUtils.fastCmd("cp /sdcard/boot.img /sdcard/dbkp/boot.img");
				ShellUtils.fastCmd("mv /sdcard/boot.img /sdcard/original-boot.img");
				ShellUtils.fastCmd("$(find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp");
				ShellUtils.fastCmd("cp /sdcard/dbkp/dbkp " + getFilesDir());
				ShellUtils.fastCmd("chmod 777 " + getFilesDir() + "/dbkp");
				ShellUtils.fastCmd("$(find /data/adb -name busybox) wget "+link+" -O /sdcard/dbkp/file.fd");
				ShellUtils.fastCmd("cd /sdcard/dbkp");
				ShellUtils.fastCmd("$(find /data/adb -name magiskboot) unpack boot.img");
				ShellUtils.fastCmd(getFilesDir() + "/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/file.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp.cfg /sdcard/dbkp/dbkp.bin");
				ShellUtils.fastCmd("mv output kernel");
				ShellUtils.fastCmd("$(su -c find /data/adb -name magiskboot) repack boot.img");
				ShellUtils.fastCmd("cp new-boot.img /sdcard/patched-boot.img");
				ShellUtils.fastCmd("rm -r /sdcard/dbkp");
				ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_a bs=16m");
				ShellUtils.fastCmd("dd if=/sdcard/patched-boot.img of=/dev/block/by-name/boot_b bs=16m");
				runOnUiThread(() -> {
					Dlg.setText(getString(R.string.dbkp, message));
					Dlg.setDismiss(R.string.dismiss, Dlg::close);
					Dlg.setNo(R.string.reboot, () -> {
						ShellUtils.fastCmd("su -c svc power reboot");
					});
				});
			}
		}.init(message,link)).start();
	}
}
