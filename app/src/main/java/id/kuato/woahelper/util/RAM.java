package id.kuato.woahelper.util;

import android.app.ActivityManager;
import android.content.Context;

public class RAM {

    public final long getTotalMemory(Context context) {
        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        assert null != actManager;
        actManager.getMemoryInfo(memInfo);
        return memInfo.totalMem;
    }

    public final String getMemory(Context context) {
        String mem;
        mem = new MemoryUtils().bytesToHuman(this.getTotalMemory(context));
        return mem;
    }
}
