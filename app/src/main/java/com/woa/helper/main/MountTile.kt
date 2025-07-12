package com.woa.helper.main

import android.os.Environment
import android.service.quicksettings.TileService
import com.topjohnwu.superuser.ShellUtils
import com.woa.helper.preference.pref

class MountTile : TileService() {
    private var win: String? = null
    private var findWin: String? = null
    private var mntStat: String? = null
    private var winPath: String? = null

    override fun onStartListening() {
        super.onStartListening()
        update()
    }

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        super.onClick()
        if (mntStat!!.isEmpty()) mount()
        else unmount()
        update()
    }

    private fun update() {
        val tile = qsTile
        if (isSecure && !pref.getSecure(this)) {
            tile.state = 0
            return
        }
        findWin = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1")
        if (findWin!!.isEmpty()) {
            tile.state = 0
            tile.updateTile()
            return
        }
        win = ShellUtils.fastCmd("realpath $findWin")
        winPath = (if (pref.getMountLocation(this)) "/mnt/Windows" else Environment.getExternalStorageDirectory().path + "/Windows")
        mntStat = ShellUtils.fastCmd("mount | grep $win")
        if (mntStat!!.isEmpty()) tile.state = 1
        else tile.state = 2
        tile.updateTile()
    }

    private fun mount() {
        val mntstat = ShellUtils.fastCmd("su -mm -c mount | grep $win")
        if (mntstat.isEmpty()) {
            ShellUtils.fastCmd("mkdir $winPath || true")
            ShellUtils.fastCmd("cd $filesDir")
            ShellUtils.fastCmd("su -mm -c ./mount.ntfs $win $winPath")
        }
    }

    private fun unmount() {
        ShellUtils.fastCmd("su -mm -c umount $winPath")
        ShellUtils.fastCmd("rmdir $winPath")
    }
}
