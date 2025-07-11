package id.kuato.woahelper.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.topjohnwu.superuser.ShellUtils;

import id.kuato.woahelper.preference.pref;

public class automount extends BroadcastReceiver {

    String win;
    String findwin;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final boolean ok = pref.getSharedPreference(context).getBoolean(pref.autoMount, false);
        if (ok) {
            final String busyBox = pref.getSharedPreference(context).getString(pref.busybox, "");
            findwin = ShellUtils.fastCmd("find /dev/block | grep win");
            if (findwin.isEmpty()) findwin = ShellUtils.fastCmd("find /dev/block | grep mindows");
            if (findwin.isEmpty()) findwin = ShellUtils.fastCmd("find /dev/block | grep windows");
            if (findwin.isEmpty()) findwin = ShellUtils.fastCmd("find /dev/block | grep Win");
            if (findwin.isEmpty()) findwin = ShellUtils.fastCmd("find /dev/block | grep Mindows");
            if (findwin.isEmpty()) findwin = ShellUtils.fastCmd("find /dev/block | grep Windows");
            win = ShellUtils.fastCmd("realpath " + findwin);
            if (pref.getSharedPreference(context).getBoolean(pref.mountLocation, false)) {
                ShellUtils.fastCmd("su -mm -c mkdir /mnt/Windows || true");
                ShellUtils.fastCmd("cd /data/data/id.kuato.woahelper/files");
                ShellUtils.fastCmd("su -mm -c ./mount.ntfs " + win + " /mnt/Windows");
            } else {
                ShellUtils.fastCmd("su -mm -c mkdir /mnt/sdcard/Windows || true");
                ShellUtils.fastCmd("cd /data/data/id.kuato.woahelper/files");
                ShellUtils.fastCmd("su -mm -c mount.ntfs " + win + " /mnt/sdcard/Windows");
            }
        }
    }
}