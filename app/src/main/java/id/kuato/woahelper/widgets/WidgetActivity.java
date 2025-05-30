package id.kuato.woahelper.widgets;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.topjohnwu.superuser.ShellUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import id.kuato.woahelper.databinding.ActivityMainBinding;
import id.kuato.woahelper.R;
import id.kuato.woahelper.main.MainActivity;
import id.kuato.woahelper.main.WidgetConfigActivity;
import id.kuato.woahelper.widgets.WidgetProvider;
import id.kuato.woahelper.preference.pref;

public class WidgetActivity extends AppCompatActivity {
	
	String finduefi;
	String device;
	String win;
	String findwin;
	String winpath;
	String findboot;
	String boot;
	
	@SuppressLint({"StringFormatInvalid", "SdCardPath"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String widget_type = intent.getStringExtra("WIDGET_TYPE");

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		if (Objects.equals(widget_type, "quickboot")) {
			checkuefi();
			device = ShellUtils.fastCmd("getprop ro.product.device");
			findwin = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1");
			finduefi = "\""+ ShellUtils.fastCmd(getString(R.string.uefiChk))+"\"";
			if (!finduefi.contains("img")) {
				alertDialog
					.setTitle(getString(R.string.uefi_not_found))
					.setMessage(getString(R.string.uefi_not_found_subtitle, device))
					.setPositiveButton(getString(R.string.dismiss), (dialog, which) -> {
						dialog.dismiss();
						finish();
					});
			} else {
			alertDialog
				.setTitle(getString(R.string.quickboot_question))
				.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
					// Everything about this is absolutely disgusting
					mount();
					String found = ShellUtils.fastCmd("ls " + (pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows") + " | grep boot.img");
					if (pref.getMountLocation(this)) {
						if (pref.getBACKUP(this) || (!pref.getAUTO(this) && found.isEmpty())) {
							ShellUtils.fastCmd("dd bs=8m if=" + boot + " of=/mnt/Windows/boot.img");
							final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
							final String currentDateAndTime = sdf.format(new Date());
							pref.setDATE(this, currentDateAndTime);
						}
					} else {
						if (pref.getBACKUP(this) || (!pref.getAUTO(this) && found.isEmpty())) {
							ShellUtils.fastCmd("dd bs=8m if=" + boot + " of=/mnt/sdcard/Windows/boot.img");
							final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
							final String currentDateAndTime = sdf.format(new Date());
							pref.setDATE(this, currentDateAndTime);
						}
					}
					found = ShellUtils.fastCmd("find /sdcard | grep boot.img");
					if (pref.getBACKUP_A(this) || (!pref.getAUTO_A(this) && found.isEmpty())) {
						ShellUtils.fastCmd("dd bs=8m if=" + boot + " of=/sdcard/boot.img");
						final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
						final String currentDateAndTime = sdf.format(new Date());
						pref.setDATE(this, currentDateAndTime);
					}
					if (pref.getDevcfg1(this)) {
						if (!MainActivity.isNetworkConnected(this)) {
							String finddevcfg = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name OOS11_devcfg_*");
							if (finddevcfg.isEmpty()) {
							alertDialog
								.setTitle(getString(R.string.internet));
								// Can't have a dialog button here for some reason
						//		.setNegativeButton(getString(R.string.dismiss), (dialog, which) -> {
						//			dialog.dismiss();
						//			finish();
						//		});
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
					flash();
					ShellUtils.fastCmd("su -c svc power reboot");
				})
				.setNegativeButton(getString(R.string.no), (dialog, which) -> {
					dialog.dismiss();
					finish();
				});
			}
		}
		else if (Objects.equals(widget_type, "mount")) {
			String checkingforwin = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1");
			if (checkingforwin.isEmpty()) {
			alertDialog
				.setTitle(getString(R.string.partition))
				.setPositiveButton(getString(R.string.dismiss), (dialog, which) -> {
					dialog.dismiss();
					finish();
				});
			}
			if (!checkingforwin.isEmpty()) {
				findwin = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1");
				win = ShellUtils.fastCmd("realpath " + findwin);
				winpath = (pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows");
				String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
				if (mnt_stat.isEmpty())
				alertDialog
					.setTitle(getString(R.string.mount_question, pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows"))
					.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
						// SOG ADD MOUNTING!!
						// Added
						ShellUtils.fastCmd("mkdir " + winpath + " || true");
						ShellUtils.fastCmd("cd " + getFilesDir());
						ShellUtils.fastCmd("su -mm -c ./mount.ntfs " + win + " " + winpath);
						// This shit won't let me build the app
			//			WidgetProvider.views.setTextViewText(R.id.text, WidgetProvider.context.getString(R.string.unmountt));
			//			WidgetProvider.appWidgetManager.updateAppWidget(WidgetProvider.appWidgetId, WidgetProvider.views);
						finish();
					});
				else
				alertDialog
					.setTitle(getString(R.string.unmount_question))
					.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
						// SOG ADD UNMOUNTING!!
						// Added
						ShellUtils.fastCmd("su -mm -c umount " + winpath);
						ShellUtils.fastCmd("rmdir " + winpath);
						// This shit won't let me build the app
			//			WidgetProvider.views.setTextViewText(R.id.text, WidgetProvider.context.getString(R.string.mountt));
			//			WidgetProvider.appWidgetManager.updateAppWidget(WidgetProvider.appWidgetId, WidgetProvider.views);
						finish();
					});
					alertDialog.setNegativeButton(getString(R.string.no), (dialog, which) -> {
						dialog.dismiss();
						finish();
					});
			}
		}
		else if (Objects.equals(widget_type, "devcfg")) {
			if (!MainActivity.isNetworkConnected(this)) {
			String finddevcfg = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name OOS11_devcfg_*");
				if (finddevcfg.isEmpty()) {
				alertDialog
					.setTitle(getString(R.string.internet))
					.setPositiveButton(getString(R.string.dismiss), (dialog, which) -> {
						dialog.dismiss();
						finish();
					});
				}
			} else {
				alertDialog
					.setTitle(getString(R.string.devcfg_question))
					.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
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
						mount();
						String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
						if (mnt_stat.isEmpty()) {
							ShellUtils.fastCmd("cp " + getFilesDir() + "/sdd.exe /sdcard/sdd.exe");
							ShellUtils.fastCmd("cp " + getFilesDir() + "/devcfg-sdd.conf /sdcard/sdd.conf");
							alertDialog
							.setTitle(getString(R.string.mountfail));
							//	Can't have a dialog button here for some reason
							//	.setNegativeButton(getString(R.string.dismiss), (dialog, which) -> {
							//		dialog.dismiss();
							//		finish()
							//	});		
						} else {
							findwin = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1");
							win = ShellUtils.fastCmd("realpath " + findwin);
							winpath = (pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows");
							ShellUtils.fastCmd("mkdir " + winpath + "/sta || true ");
							ShellUtils.fastCmd("cp '" + getFilesDir() + "/Flash Devcfg.lnk' " + winpath + "/Users/Public/Desktop");
							ShellUtils.fastCmd("cp " + getFilesDir() + "/sdd.exe " + winpath + "/sta/sdd.exe");
							ShellUtils.fastCmd("cp " + getFilesDir() + "/devcfg-sdd.conf " + winpath + "/sta/sdd.conf");
							ShellUtils.fastCmd("cp /sdcard/original-devcfg.img " + winpath + "/original-devcfg.img");
						}
						alertDialog
							.setTitle(getString(R.string.devcfg));
						//	Can't have a dialog button here for some reason
						//	.setNegativeButton(getString(R.string.dismiss), (dialog, which) -> {
						//		dialog.dismiss();
						//		finish()
						//	});
					})
					.setNegativeButton(getString(R.string.no), (dialog, which) -> {
						dialog.dismiss();
						finish();
					});
				}
		}

		alertDialog.setOnCancelListener((dialog) -> {
			dialog.dismiss();
			finish();
		});
		alertDialog.show();
	}
	
	private void mount() {
		String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
		if (mnt_stat.isEmpty()) {
			ShellUtils.fastCmd("mkdir " + winpath + " || true");
			ShellUtils.fastCmd("cd " + getFilesDir());
			ShellUtils.fastCmd("su -mm -c ./mount.ntfs " + win + " " + winpath);
		}
	}

	public void flash() {
		ShellUtils.fastCmd("dd if=" + finduefi + " of=/dev/block/bootdevice/by-name/boot$(getprop ro.boot.slot_suffix) bs=16m");
	}

	public void checkuefi() {
		finduefi = ShellUtils.fastCmd(getString(R.string.uefiChk));
		findwin = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1");
		win = ShellUtils.fastCmd("realpath " + findwin);
		winpath = (pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows");
		findboot = ShellUtils.fastCmd("find /dev/block | grep boot$(getprop ro.boot.slot_suffix)");
		if (findboot.isEmpty()) findboot = ShellUtils.fastCmd("find /dev/block | grep BOOT$(getprop ro.boot.slot_suffix)");
		boot = ShellUtils.fastCmd("realpath " + findboot);
	}	
}