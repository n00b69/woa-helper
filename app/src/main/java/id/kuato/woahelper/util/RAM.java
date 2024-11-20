package id.kuato.woahelper.util;

import android.app.ActivityManager;
import android.content.Context;

public class RAM {

    public final long getTotalMemory(final Context context) {
        final ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        assert null != actManager;
        actManager.getMemoryInfo(memInfo);
        return memInfo.totalMem;
    }

    public final String getMemory(final Context context) {
        final String mem;
        mem = new MemoryUtils().bytesToHuman(getTotalMemory(context));
        return mem;
    }
}
