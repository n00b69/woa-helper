package id.kuato.woahelper.main;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.topjohnwu.superuser.ShellUtils;

import id.kuato.woahelper.R;
import id.kuato.woahelper.preference.pref;

public class mount_tile extends TileService {

    String win;
    String mnt_stat;

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
        win = ShellUtils.fastCmd("su -c realpath /dev/block/by-name/win");
        if (win.isEmpty()) win = ShellUtils.fastCmd("su -c realpath /dev/block/by-name/mindows");
        mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
        if (mnt_stat.isEmpty()) tile.setState(1);
        else tile.setState(2);
        tile.updateTile();
    }

    private void mount() {
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
    }

    public void unmount() {
        if (pref.getMountLocation(this)) {
            ShellUtils.fastCmd(getString(R.string.unmount2));
            ShellUtils.fastCmd(getString(R.string.rm2));
        } else {
            ShellUtils.fastCmd(getString(R.string.unmount));
            ShellUtils.fastCmd(getString(R.string.rm));
        }
    }


}
