package com.woa.helper.util

object ToolboxDeployer {
    private const val TOOLBOX_DIR = "/sdcard/WOAHelper/Toolbox"

    fun deploySta(filesDir: String): ShellResult {
        val mkdir1 = ShellManager.execResult("mkdir -p /sdcard/WOAHelper/sta")
        if (mkdir1 is ShellResult.Error) return mkdir1
        listOf("sta.exe", "sdd.exe", "sdd.conf", "boot.img_auto-flasher_V2.0.exe").forEach { file ->
            val cp = ShellManager.execResult("cp $filesDir/$file /sdcard/WOAHelper/sta/")
            if (cp is ShellResult.Error) return cp
        }
        val mountResult = MountManager.mount()
        if (mountResult is ShellResult.Error) return mountResult
        val winPath = MountManager.getWinPath()
        if (winPath.isEmpty()) return ShellResult.Error("Mount path is empty")
        val mkdir2 = ShellManager.execResult("mkdir -p $winPath/sta")
        if (mkdir2 is ShellResult.Error) return mkdir2
        val cp1 = ShellManager.execResult("cp '$filesDir/Switch to Android.lnk' $winPath/Users/Public/Desktop")
        if (cp1 is ShellResult.Error) return cp1
        val cp2 = ShellManager.execResult("cp $filesDir/sta.exe $winPath/ProgramData/sta/sta.exe")
        if (cp2 is ShellResult.Error) return cp2
        return ShellManager.execResult("cp /sdcard/WOAHelper/sta/* $winPath/sta/")
    }

    fun deploySoftware(filesDir: String): ShellResult {
        val mountResult = MountManager.mount()
        if (mountResult is ShellResult.Error) return mountResult
        val winPath = MountManager.getWinPath()
        if (winPath.isEmpty()) return ShellResult.Error("Mount path is empty")
        val mkdir1 = ShellManager.execResult("mkdir -p $TOOLBOX_DIR")
        if (mkdir1 is ShellResult.Error) return mkdir1
        val files = listOf("WorksOnWoa.url", "TestedSoftware.url", "ARMSoftware.url", "ARMRepo.url")
        files.forEach { file ->
            val cp = ShellManager.execResult("cp $filesDir/$file $TOOLBOX_DIR")
            if (cp is ShellResult.Error) return cp
        }
        val mkdir2 = ShellManager.execResult("mkdir -p $winPath/Toolbox")
        if (mkdir2 is ShellResult.Error) return mkdir2
        files.forEach { file ->
            val cp = ShellManager.execResult("cp $filesDir/$file $winPath/Toolbox")
            if (cp is ShellResult.Error) return cp
        }
        return ShellResult.Success("")
    }

    fun deployAtlasOS(url: String, targetName: String, onProgress: (Int) -> Unit): ShellResult {
        val mkdir = ShellManager.execResult("mkdir -p $TOOLBOX_DIR")
        if (mkdir is ShellResult.Error) return mkdir
        val wget1 = ShellManager.execResult("wget $url -O $TOOLBOX_DIR/$targetName")
        if (wget1 is ShellResult.Error) return wget1
        onProgress(50)
        val wget2 = ShellManager.execResult("wget https://download.ameliorated.io/AME%20Beta.zip -O $TOOLBOX_DIR/AMEWizardBeta.zip")
        if (wget2 is ShellResult.Error) return wget2
        onProgress(80)
        val mountResult = MountManager.mount()
        if (mountResult is ShellResult.Error) return mountResult
        val winPath = MountManager.getWinPath()
        if (winPath.isEmpty()) return ShellResult.Error("Mount path is empty")
        val mkdir2 = ShellManager.execResult("mkdir -p $winPath/Toolbox")
        if (mkdir2 is ShellResult.Error) return mkdir2
        val cp1 = ShellManager.execResult("cp $TOOLBOX_DIR/$targetName $winPath/Toolbox/$targetName")
        if (cp1 is ShellResult.Error) return cp1
        return ShellManager.execResult("cp $TOOLBOX_DIR/AMEWizardBeta.zip $winPath/Toolbox")
    }

    fun deployUsbHost(filesDir: String): ShellResult {
        val mkdir1 = ShellManager.execResult("mkdir -p $TOOLBOX_DIR")
        if (mkdir1 is ShellResult.Error) return mkdir1
        val cp1 = ShellManager.execResult("cp $filesDir/usbhostmode.exe $TOOLBOX_DIR/")
        if (cp1 is ShellResult.Error) return cp1
        val mountResult = MountManager.mount()
        if (mountResult is ShellResult.Error) return mountResult
        val winPath = MountManager.getWinPath()
        if (winPath.isEmpty()) return ShellResult.Error("Mount path is empty")
        val mkdir2 = ShellManager.execResult("mkdir -p $winPath/Toolbox")
        if (mkdir2 is ShellResult.Error) return mkdir2
        val cp2 = ShellManager.execResult("cp $TOOLBOX_DIR/usbhostmode.exe $winPath/Toolbox")
        if (cp2 is ShellResult.Error) return cp2
        return ShellManager.execResult("cp '$filesDir/USB Host Mode.lnk' $winPath/Users/Public/Desktop")
    }

    fun deployRotation(filesDir: String): ShellResult {
        val mkdir1 = ShellManager.execResult("mkdir -p $TOOLBOX_DIR")
        if (mkdir1 is ShellResult.Error) return mkdir1
        val cp1 = ShellManager.execResult("cp $filesDir/QuickRotate_V6.1.6.exe $TOOLBOX_DIR/")
        if (cp1 is ShellResult.Error) return cp1
        val mountResult = MountManager.mount()
        if (mountResult is ShellResult.Error) return mountResult
        val winPath = MountManager.getWinPath()
        if (winPath.isEmpty()) return ShellResult.Error("Mount path is empty")
        val mkdir2 = ShellManager.execResult("mkdir -p $winPath/Toolbox")
        if (mkdir2 is ShellResult.Error) return mkdir2
        val cp2 = ShellManager.execResult("cp $TOOLBOX_DIR/QuickRotate_V6.1.6.exe $winPath/Toolbox")
        if (cp2 is ShellResult.Error) return cp2
        return ShellManager.execResult("cp $TOOLBOX_DIR/QuickRotate_V6.1.6.exe $winPath/Users/Public/Desktop")
    }

    fun deployTabletMode(filesDir: String): ShellResult {
        val mkdir1 = ShellManager.execResult("mkdir -p $TOOLBOX_DIR")
        if (mkdir1 is ShellResult.Error) return mkdir1
        val cp1 = ShellManager.execResult("cp $filesDir/OptimizedTaskbarControl_V3.2.exe $TOOLBOX_DIR/")
        if (cp1 is ShellResult.Error) return cp1
        val mountResult = MountManager.mount()
        if (mountResult is ShellResult.Error) return mountResult
        val winPath = MountManager.getWinPath()
        if (winPath.isEmpty()) return ShellResult.Error("Mount path is empty")
        val mkdir2 = ShellManager.execResult("mkdir -p $winPath/Toolbox")
        if (mkdir2 is ShellResult.Error) return mkdir2
        return ShellManager.execResult("cp $TOOLBOX_DIR/OptimizedTaskbarControl_V3.2.exe $winPath/Toolbox")
    }

    fun deployFrameworks(filesDir: String, onProgress: (Int) -> Unit): ShellResult {
        val frameworksDir = "/sdcard/WOAHelper/Frameworks"
        val mkdir1 = ShellManager.execResult("mkdir -p $frameworksDir")
        if (mkdir1 is ShellResult.Error) return mkdir1
        val cp1 = ShellManager.execResult("cp $filesDir/install.bat $frameworksDir/install.bat")
        if (cp1 is ShellResult.Error) return cp1
        val installers = listOf(
            "PhysX-9.13.0604-SystemSoftware-Legacy.msi", "PhysX_9.23.1019_SystemSoftware.exe", "xnafx40_redist.msi",
            "opengl.appx", "2005vcredist_x64.EXE", "2005vcredist_x86.EXE", "2008vcredist_x64.exe", "2008vcredist_x86.exe",
            "2010vcredist_x64.exe", "2010vcredist_x86.exe", "2012vcredist_x64.exe", "2012vcredist_x86.exe",
            "2013vcredist_x64.exe", "2013vcredist_x86.exe", "2015VC_redist.x64.exe", "2015VC_redist.x86.exe",
            "2022VC_redist.arm64.exe", "dxwebsetup.exe", "oalinst.exe"
        )
        val failures = mutableListOf<String>()
        installers.forEach { installer ->
            val result = ShellManager.execResult("wget https://github.com/n00b69/woasetup/releases/download/Installers/$installer -O $frameworksDir/$installer")
            if (result is ShellResult.Error) failures.add("$installer: ${result.message}")
            onProgress(5)
        }
        if (failures.isNotEmpty()) return ShellResult.Error("Download failed: ${failures.take(3).joinToString(", ")}${if (failures.size > 3) " (+${failures.size - 3} more)" else ""}")
        val mountResult = MountManager.mount()
        if (mountResult is ShellResult.Error) return mountResult
        val winPath = MountManager.getWinPath()
        if (winPath.isEmpty()) return ShellResult.Error("Mount path is empty")
        val mkdir2 = ShellManager.execResult("mkdir -p $winPath/Toolbox/Frameworks")
        if (mkdir2 is ShellResult.Error) return mkdir2
        return ShellManager.execResult("cp $frameworksDir/* $winPath/Toolbox/Frameworks")
    }

    fun deployDefenderEdge(filesDir: String, isNetworkConnected: Boolean): ShellResult {
        val mkdir = ShellManager.execResult("mkdir -p $TOOLBOX_DIR")
        if (mkdir is ShellResult.Error) return mkdir
        if (ShellManager.exec("find $filesDir -maxdepth 1 -name DefenderRemover.exe").isEmpty()) {
            if (!isNetworkConnected) return ShellResult.Error("DefenderRemover not found and no internet to download")
            val wget = ShellManager.execResult("wget https://github.com/n00b69/woasetup/releases/download/Installers/DefenderRemover.exe -O $TOOLBOX_DIR/DefenderRemover.exe")
            if (wget is ShellResult.Error) return wget
            val cp = ShellManager.execResult("cp $TOOLBOX_DIR/DefenderRemover.exe $filesDir/DefenderRemover.exe")
            if (cp is ShellResult.Error) return cp
        } else {
            val cp = ShellManager.execResult("cp $filesDir/DefenderRemover.exe $TOOLBOX_DIR/DefenderRemover.exe")
            if (cp is ShellResult.Error) return cp
        }
        val mountResult = MountManager.mount()
        if (mountResult is ShellResult.Error) return mountResult
        val winPath = MountManager.getWinPath()
        if (winPath.isEmpty()) return ShellResult.Error("Mount path is empty")
        val mkdir2 = ShellManager.execResult("mkdir -p $winPath/Toolbox")
        if (mkdir2 is ShellResult.Error) return mkdir2
        val cp1 = ShellManager.execResult("cp $filesDir/RemoveEdge.bat $TOOLBOX_DIR")
        if (cp1 is ShellResult.Error) return cp1
        val cp2 = ShellManager.execResult("cp $filesDir/DefenderRemover.exe $winPath/Toolbox")
        if (cp2 is ShellResult.Error) return cp2
        return ShellManager.execResult("cp $filesDir/RemoveEdge.bat $winPath/Toolbox")
    }

    fun dumpModem(): ShellResult {
        val mountResult = MountManager.mount()
        if (mountResult is ShellResult.Error) return mountResult
        val winPath = MountManager.getWinPath()
        if (winPath.isEmpty()) return ShellResult.Error("Mount path is empty")
        val failures = mutableListOf<String>()
        listOf("modemst1" to "bootmodem_fs1", "modemst2" to "bootmodem_fs2").forEach { (partition, target) ->
            val result = ShellManager.execResult("dd if=/dev/block/by-name/$partition of=\$(find $winPath/Windows/System32/DriverStore/FileRepository -name qcremotefs8150.inf_arm64_*)/$target")
            if (result is ShellResult.Error) failures.add("$partition: ${result.message}")
        }
        return if (failures.isEmpty()) ShellResult.Success("") else ShellResult.Error("Dump failed: ${failures.joinToString(", ")}")
    }
}
