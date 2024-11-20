package id.kuato.woahelper.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.topjohnwu.superuser.ShellUtils;

import id.kuato.woahelper.preference.pref;

public class automount extends BroadcastReceiver {


    String win;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final boolean ok = pref.getSharedPreference(context).getBoolean(pref.autoMount, false);
        if (ok) {
            final String busyBox = pref.getSharedPreference(context).getString(pref.busybox, "");
            win = ShellUtils.fastCmd("su -c realpath /dev/block/by-name/win");
            if (win.isEmpty()) win = ShellUtils.fastCmd("su -c realpath /dev/block/by-name/mindows");
            if (pref.getSharedPreference(context).getBoolean(pref.mountLocation, false)) {
                ShellUtils.fastCmd("su -c mkdir /mnt/Windows || true");
                ShellUtils.fastCmd(String.format("su -mm -c " + busyBox + " mount -t ntfs %s /mnt/Windows", win));
            } else {
                ShellUtils.fastCmd("su -c mkdir /mnt/sdcard/Windows || true");
                ShellUtils.fastCmd(String.format("su -mm -c " + busyBox + " mount -t ntfs %s /mnt/sdcard/Windows", win));
            }
        }
    }

}
