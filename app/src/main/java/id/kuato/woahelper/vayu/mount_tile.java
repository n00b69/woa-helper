package id.kuato.woahelper.vayu;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.Nullable;

import java.util.Objects;

import id.kuato.woahelper.R;
import com.topjohnwu.superuser.ShellUtils;
import id.kuato.woahelper.preference.pref;
public class mount_tile extends TileService {


    String win;
    String mnt_stat;
    @Override
    public void onStartListening() {
        super.onStartListening();
        update();
}

    @Override
    public void onStopListening() {
        super.onStopListening();

    }

    // Called when the user taps on your tile in an active or inactive state.
    @Override
    public void onClick() {
        super.onClick();
        if (mnt_stat.isEmpty())
            mount();
        else
            unmount();
        update();
    }

private void update(){
Tile tile =getQsTile();
        win = ShellUtils.fastCmd("su -c realpath /dev/block/by-name/win");
        if(win.isEmpty())
            win = ShellUtils.fastCmd("su -c realpath /dev/block/by-name/mindows");
        mnt_stat = ShellUtils.fastCmd("su -c mount | grep " + win);
        if (mnt_stat.isEmpty())
            tile.setState(1);
		else
			tile.setState(2);
        tile.updateTile();
    }

    private void mount() {
        ShellUtils.fastCmd(getString(R.string.mk));
        ShellUtils.fastCmd(String.format("su -mm -c "+pref.getbusybox(this)+getString(R.string.mount),win));
    }
    public void unmount() {
        ShellUtils.fastCmd(getString(R.string.unmount));
        ShellUtils.fastCmd(getString(R.string.rm));
    }


    // Called when the user removes your tile.
    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

}
