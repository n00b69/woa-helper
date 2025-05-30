package id.kuato.woahelper.main;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.topjohnwu.superuser.ShellUtils;

import id.kuato.woahelper.R;
import id.kuato.woahelper.preference.pref;

public class mount_tile extends TileService {

	String win;
	String findwin;
	String mnt_stat;
	String winpath;

	@Override
	public void onStartListening() {
		super.onStartListening();
		update();
	}

	// Called when the user taps on your tile in an active or inactive state.
	@Override
	public void onClick() {
		super.onClick();
		if (mnt_stat.isEmpty()) mount();
		else unmount();
		update();
	}

	private void update() {
		final Tile tile = getQsTile();
		if (isSecure() && !pref.getSecure(this)){ tile.setState(0);return;}
		findwin = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1");
		if (findwin.isEmpty()) {
			tile.setState(0);
			tile.updateTile();
			return;
		}
		win = ShellUtils.fastCmd("realpath " + findwin);
		winpath = (pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows");
		mnt_stat = ShellUtils.fastCmd("mount | grep " + win);
		if (mnt_stat.isEmpty()) tile.setState(1);
		else tile.setState(2);
		tile.updateTile();
	}

	private void mount() {
		String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
		if (mnt_stat.isEmpty()) {
			ShellUtils.fastCmd("mkdir " + winpath + " || true");
			ShellUtils.fastCmd("cd " + getFilesDir());
			ShellUtils.fastCmd("su -mm -c ./mount.ntfs " + win + " " + winpath);
		}
	}
	
	public void unmount() {
		ShellUtils.fastCmd("su -mm -c umount " + winpath);
		ShellUtils.fastCmd("rmdir " + winpath);
	}


}
