package id.kuato.woahelper.main;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import id.kuato.woahelper.R;

import com.topjohnwu.superuser.ShellUtils;

import id.kuato.woahelper.preference.pref;

public class quickboot_tile extends TileService {

    String finduefi;
    String device;
    String win;

    // Called when your app can update your tile.
    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = this.getQsTile();
        this.checkdevice();
        this.checkuefi();
        if (this.finduefi.isEmpty() || (this.isSecure() && !pref.getSecure(this)))
            tile.setState(0);
        else
            tile.setState(1);
        if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT) {
            tile.setSubtitle("");
        }
        tile.updateTile();

    }

    // Called when the user taps on your tile in an active or inactive state.
    @Override
    public void onClick() {
        super.onClick();
        Tile tile = this.getQsTile();
        if (2 == tile.getState() || !pref.getCONFIRM(this)) {
            this.mount();
            if (!(Objects.equals(this.device, "nabu") || Objects.equals(this.device, "mh2lm")) || !(pref.getMODEM(this))) {
                this.dump();
            }
            String found = ShellUtils.fastCmd("ls " + (pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows") + " | grep boot.img");
            if (pref.getMountLocation(this)) {
                if (pref.getBACKUP(this)
                        || (!pref.getAUTO(this) && found.isEmpty())) {
                    ShellUtils.fastCmd(this.getString(R.string.backup2));
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
                    String currentDateAndTime = sdf.format(new Date());
                    pref.setDATE(this, currentDateAndTime);
                }
            } else {
                if (pref.getBACKUP(this)
                        || (!pref.getAUTO(this) && found.isEmpty())) {
                    ShellUtils.fastCmd(this.getString(R.string.backup1));
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
                    String currentDateAndTime = sdf.format(new Date());
                    pref.setDATE(this, currentDateAndTime);
                }
            }
            found = ShellUtils.fastCmd("find /sdcard | grep boot.img");
            if (pref.getBACKUP_A(this)
                    || (!pref.getAUTO_A(this) && found.isEmpty())) {
                ShellUtils.fastCmd(this.getString(R.string.backup));
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM HH:mm", Locale.US);
                String currentDateAndTime = sdf.format(new Date());
                pref.setDATE(this, currentDateAndTime);
            }
            this.flash();
            ShellUtils.fastCmd("su -c svc power reboot");
        } else {
            tile.setState(2);
            if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT) {
                tile.setSubtitle("Press again");
            }
            tile.updateTile();
        }
    }
    //private void mount() {
    //	ShellUtils.fastCmd(getString(R.string.mk));
    //	ShellUtils.fastCmd(getString(R.string.mount));
    //}

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

    private void dump() {
        if (pref.getMountLocation(this)) {
            ShellUtils.fastCmd(this.getString(R.string.modem12));
            ShellUtils.fastCmd(this.getString(R.string.modem22));
        } else {
            ShellUtils.fastCmd(this.getString(R.string.modem1));
            ShellUtils.fastCmd(this.getString(R.string.modem2));
        }
    }

    public void flash() {
        ShellUtils.fastCmd("su -c dd if=" + this.finduefi + " of=/dev/block/bootdevice/by-name/boot$(getprop ro.boot.slot_suffix) bs=16M");
    }

    public void checkdevice() {
        this.device = ShellUtils.fastCmd("getprop ro.product.device");
    }

    public void checkuefi() {
        this.checkdevice();
        this.finduefi = ShellUtils.fastCmd(this.getString(R.string.uefiChk));
        this.win = ShellUtils.fastCmd("realpath /dev/block/by-name/win");
    }

}
