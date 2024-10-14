package id.kuato.woahelper.main;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import id.kuato.woahelper.R;

import com.topjohnwu.superuser.ShellUtils;

import id.kuato.woahelper.preference.pref;

public class mount_tile extends TileService {

    String win;
    String mnt_stat;

    @Override
    public void onStartListening() {
        super.onStartListening();
        this.update();
    }

    // Called when the user taps on your tile in an active or inactive state.
    @Override
    public void onClick() {
        super.onClick();
        if (this.mnt_stat.isEmpty())
            this.mount();
        else
            this.unmount();
        this.update();
    }

    private void update() {
        Tile tile = this.getQsTile();
        this.win = ShellUtils.fastCmd("su -c realpath /dev/block/by-name/win");
        if (this.win.isEmpty())
            this.win = ShellUtils.fastCmd("su -c realpath /dev/block/by-name/mindows");
        this.mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + this.win);
        if (this.mnt_stat.isEmpty())
            tile.setState(1);
        else
            tile.setState(2);
        tile.updateTile();
    }

    private void mount() {
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
    }

    public void unmount() {
        if (pref.getMountLocation(this)) {
            ShellUtils.fastCmd(this.getString(R.string.unmount2));
            ShellUtils.fastCmd(this.getString(R.string.rm2));
        } else {
            ShellUtils.fastCmd(this.getString(R.string.unmount));
            ShellUtils.fastCmd(this.getString(R.string.rm));
        }
    }


}
