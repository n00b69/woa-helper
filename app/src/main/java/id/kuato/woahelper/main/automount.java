package id.kuato.woahelper.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.topjohnwu.superuser.ShellUtils;

import id.kuato.woahelper.preference.pref;

public class automount extends BroadcastReceiver {


    String win;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean ok = pref.getSharedPreference(context).getBoolean(pref.autoMount, false);
        if (ok) {
            String busyBox = pref.getSharedPreference(context).getString(pref.busybox, "");
            this.win = ShellUtils.fastCmd("su -c realpath /dev/block/by-name/win");
            if (this.win.isEmpty())
                this.win = ShellUtils.fastCmd("su -c realpath /dev/block/by-name/mindows");
            if (pref.getSharedPreference(context).getBoolean(pref.mountLocation, false)) {
                ShellUtils.fastCmd("su -c mkdir /mnt/Windows || true");
                ShellUtils.fastCmd(String.format("su -mm -c " + busyBox + " mount -t ntfs %s /mnt/Windows", this.win));
            } else {
                ShellUtils.fastCmd("su -c mkdir /mnt/sdcard/Windows || true");
                ShellUtils.fastCmd(String.format("su -mm -c " + busyBox + " mount -t ntfs %s /mnt/sdcard/Windows", this.win));
            }
        }
    }

}
