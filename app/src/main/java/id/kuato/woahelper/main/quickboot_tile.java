package id.kuato.woahelper.main;

import android.os.Build;
import android.os.Environment;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import com.topjohnwu.superuser.ShellUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import id.kuato.woahelper.R;
import id.kuato.woahelper.preference.pref;

public class quickboot_tile extends TileService {

	String finduefi;
	String device;
	String win;
	String findwin;
	String winpath;
	String findboot;
	String boot;

	// Called when your app can update your tile.
	@Override
	public void onStartListening() {
		super.onStartListening();
		final Tile tile = getQsTile();
		checkuefi();
		device = ShellUtils.fastCmd("getprop ro.product.device");
		findwin = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1");
		if (finduefi.isEmpty() || (isSecure() && !pref.getSecure(this)) || findwin.isEmpty()) tile.setState(0);
		else tile.setState(1);
		if (pref.getDevcfg1(this)) {
			if (!MainActivity.isNetworkConnected(this)) {
				String finddevcfg = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name OOS11_devcfg_*");
				if (finddevcfg.isEmpty()) {
					tile.setState(0);
					// These titles don't actually even seem to work and are obsolete I guess, is there even a way to change the titles?
					tile.setSubtitle(getString(R.string.qsinternet));
				}
			} else {
				tile.setState(1);
			}
		}
		if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT) {
			tile.setSubtitle("");
		}
		tile.updateTile();
	}

	// Called when the user taps on your tile in an active or inactive state.
	@Override
	public void onClick() {
		super.onClick();
		final Tile tile = getQsTile();
		if (2 == tile.getState() || !pref.getCONFIRM(this)) {
			mount();
			String found = ShellUtils.fastCmd("ls " + (pref.getMountLocation(this) ? "/mnt/Windows" : Environment.getExternalStorageDirectory().getPath()+"/Windows") + " | grep boot.img");
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
			// This whole feature feels very laggy to me in the QS tile, needs overhauling.
			if (pref.getDevcfg1(this)) {
				if (!MainActivity.isNetworkConnected(this)) {
					String finddevcfg = ShellUtils.fastCmd("find " + getFilesDir() + " -maxdepth 1 -name OOS11_devcfg_*");
					if (finddevcfg.isEmpty()) {
						Toast.makeText(this, (getString(R.string.internet)), Toast.LENGTH_LONG).show();
						tile.setState(0);
						tile.setSubtitle(getString(R.string.qsinternet));
						return;
					} else {
						tile.setState(1);
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
		//	Toast.makeText(this, "This should appear if it reaches the (disabled) flash uefi section", Toast.LENGTH_LONG).show();
			flash();
			ShellUtils.fastCmd("su -c svc power reboot");
		} else {
			tile.setState(2);
			if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT) {
				tile.setSubtitle(getString(R.string.qspressagain));
			}
			tile.updateTile();
		}
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
		winpath = (pref.getMountLocation(this) ? "/mnt/Windows" : Environment.getExternalStorageDirectory().getPath()+"/Windows");
		findboot = ShellUtils.fastCmd("find /dev/block | grep boot$(getprop ro.boot.slot_suffix)");
		if (findboot.isEmpty()) findboot = ShellUtils.fastCmd("find /dev/block | grep BOOT$(getprop ro.boot.slot_suffix)");
		boot = ShellUtils.fastCmd("realpath " + findboot);
	}

}
