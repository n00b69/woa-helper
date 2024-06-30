package id.kuato.woahelper.vayu;

import static id.kuato.woahelper.preference.pref.autoMount;
import static id.kuato.woahelper.preference.pref.busybox;
import static id.kuato.woahelper.preference.pref.getSharedPreference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.topjohnwu.superuser.ShellUtils;

public class automount extends BroadcastReceiver {


    String win;
    @Override
    public void onReceive(Context context, Intent intent) {
        String busyBox=getSharedPreference(context).getString(busybox,"");
        win = ShellUtils.fastCmd("su -c realpath /dev/block/by-name/win");
        if(win.isEmpty())
            win = ShellUtils.fastCmd("su -c realpath /dev/block/by-name/mindows");
        boolean ok=getSharedPreference(context).getBoolean(autoMount,false);
        if(ok){
            ShellUtils.fastCmd("su -c mkdir /mnt/sdcard/Windows || true");
            ShellUtils.fastCmd(String.format("su -mm -c "+busyBox+" mount -t ntfs %s /mnt/sdcard/Windows", win));
        }
    };
}
