package com.woa.helper.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.topjohnwu.superuser.Shell
import com.woa.helper.BuildConfig
import com.woa.helper.R
import com.woa.helper.databinding.ActivityMainBinding
import com.woa.helper.databinding.ScriptsBinding
import com.woa.helper.databinding.SetPanelBinding
import com.woa.helper.databinding.ToolboxBinding
import com.woa.helper.preference.Pref
import com.woa.helper.util.RAM
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.woa.helper.dbkp.Dbkp
import androidx.core.view.isVisible

@SuppressLint("StaticFieldLeak")
class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var settingsBinding: SetPanelBinding
    private lateinit var toolboxBinding: ToolboxBinding
    private lateinit var scriptsBinding: ScriptsBinding

    private var grouplink = "https://t.me/woahelperchat"
    private var guidelink = "https://github.com/n00b69"
    private var unsupported = false
    private var tablet = false
    private val views: MutableList<View> = ArrayList()

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        this.enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        initBase()
        setupNavigation()
        inflateLayouts()
        setupWindowInsets()
        setupLanguageSpinner()
        setupToolbar()
        initDeviceData()
        setupClickListeners()
        checkUpdatesAndModels()
    }

    private fun initBase() {
        Pref.setFilesDir(this, filesDir.toString())
        shellInit(this.filesDir)
    }

    private fun setupNavigation() {
        onBackPressedDispatcher.addCallback(this) {
            if (views.size <= 1) {
                moveTaskToBack(true)
                finish()
            } else {
                handleBackNavigation()
            }
        }
    }

    private fun handleBackNavigation() {
        val currentView = views.last()
        if (BuildConfig.DEBUG && currentView === settingsBinding.root) {
            saveCodenameIfChanged()
        }

        currentView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_out))
        views.removeAt(views.lastIndex)
        
        val previousView = views.last()
        setContentView(previousView)
        previousView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_back_in))
    }

    private fun saveCodenameIfChanged() {
        val textbox = settingsBinding.codename
        val currentCodename = textbox.text.toString()
        val savedCodename = Pref.codenameChanger(false, this, "")
        if (currentCodename.isNotBlank() && currentCodename != savedCodename) {
            Pref.codenameChanger(true, this, currentCodename)
        }
    }

    private fun inflateLayouts() {
        copyAssets()
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        settingsBinding = SetPanelBinding.inflate(layoutInflater)
        toolboxBinding = ToolboxBinding.inflate(layoutInflater)
        scriptsBinding = ScriptsBinding.inflate(layoutInflater)

        Download.permission(this)
        setContentView(mainBinding.root)

        views.clear()
        views.add(mainBinding.root)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val sysInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            listOf(mainBinding.app, toolboxBinding.app, settingsBinding.app, scriptsBinding.app).forEach {
                it.setPadding(0, 0, 0, sysInsets.bottom)
            }
            
            listOf(mainBinding.linearLayout, toolboxBinding.linearLayout, settingsBinding.linearLayout, scriptsBinding.linearLayout).forEach {
                it.setPadding(sysInsets.left, sysInsets.top, sysInsets.right, 0)
            }
            insets
        }
    }

    private fun setupLanguageSpinner() {
        val languages = mutableListOf(getString(R.string.default1))
        val locales = mutableListOf("und")
        
        for (tag in BuildConfig.LOCALES) {
            locales.add(tag!!.lowercase(Locale.getDefault()))
            val locale = checkNotNull(LocaleListCompat.forLanguageTags(tag)[0])
            val country = locale.getDisplayCountry(locale)
            val lang = locale.getDisplayLanguage(locale) + (if (country.isNotEmpty()) " ($country)" else "")
            languages.add(lang)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        settingsBinding.languages.adapter = adapter
        
        val currentLocale = AppCompatDelegate.getApplicationLocales()[0]
        if (currentLocale != null) {
            val index = locales.indexOf(currentLocale.toLanguageTag().lowercase())
            if (index != -1) settingsBinding.languages.setSelection(index)
        }

        settingsBinding.languages.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val localeList = if (languages[position] == getString(R.string.default1)) {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(locales[position])
                }
                AppCompatDelegate.setApplicationLocales(localeList)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(mainBinding.toolbarlayout.toolbar)
        
        val toolbarBindings = listOf(mainBinding.toolbarlayout, settingsBinding.toolbarlayout, toolboxBinding.toolbarlayout, scriptsBinding.toolbarlayout)
        toolbarBindings.forEach { binding ->
            binding.toolbar.setNavigationIcon(R.drawable.ic_launcher_foreground)
            binding.settings.setColorFilter(getColor(R.color.md_theme_primary))
        }

        mainBinding.toolbarlayout.toolbar.setTitle(R.string.app_name)
        mainBinding.toolbarlayout.toolbar.subtitle = "v${BuildConfig.VERSION_NAME}${if (BuildConfig.DEBUG) " (Debug)" else ""}"
        
        settingsBinding.toolbarlayout.toolbar.setTitle(R.string.preferences)
    }

    private fun initDeviceData() {
        win = getWin()
        boot = getBoot()
        updateDevice(this)
        updateWinPath(this)
        updateMountText()
        
        mainBinding.tvDate.text = getString(R.string.last, Pref.getDate(this))

        val slot = rootCommand("getprop ro.boot.slot_suffix")
        if (slot.isEmpty()) {
            mainBinding.tvSlot.visibility = View.GONE
        } else {
            mainBinding.tvSlot.text = getString(R.string.slot, slot).uppercase(Locale.getDefault())
        }

        mainBinding.deviceName.text = "${Build.MODEL} ($device)"
        val props = Device.getVars(device)
        guidelink = props.guideLink
        grouplink = props.groupLink
        
        mainBinding.DeviceImage.setImageResource(props.image)
        mainBinding.tvPanel.visibility = props.panel
        toolboxBinding.dbkp.visibility = props.dbkp
        toolboxBinding.flashUefi.visibility = if (props.dbkp == View.VISIBLE) View.GONE else View.VISIBLE
        
        unsupported = props.unsupported
        tablet = isTablet()
        onConfigurationChanged(resources.configuration)

        if (unsupported && !Pref.getAGREE(this)) {
            Dlg.show(this, R.string.unsupported)
            Dlg.setYes(R.string.sure) {
                Pref.setAGREE(this, true)
                Dlg.close()
            }
        }

        val panel = detectPanelType()
        if (!Pref.getAGREE(this) && (panel.contains("f1p2_2") || panel.contains("f1_cmd"))) {
            showPanelWarning()
        }

        mainBinding.tvRamvalue.text = getString(R.string.ramvalue, RAM().getMemory(this).toDouble())
        mainBinding.tvPanel.text = getString(R.string.paneltype, panel)
    }

    private fun detectPanelType(): String {
        val cmdline = rootCommand("cat /proc/cmdline")
        return when {
            cmdline.contains("tianmamd_dv2") -> "Tianma DV2"
            cmdline.contains("tianmamd_pp1") -> "Tianma PP1"
            cmdline.contains("tianmamd_pv") -> "Tianma PV"
            cmdline.contains("j20s_42") || cmdline.contains("k82_42") || cmdline.contains("k9d_42") || cmdline.contains("huaxing") -> "Huaxing"
            cmdline.contains("j20s_36") || cmdline.contains("tianma") || cmdline.contains("k9d_36") || cmdline.contains("k82_36") -> "Tianma"
            cmdline.contains("ebbg") -> "EBBG"
            cmdline.contains("samsung") || cmdline.contains("ea8076_f1mp") || cmdline.contains("ea8076_f1p2") || 
                cmdline.contains("ea8076_global") || cmdline.contains("S6E3FC3") || cmdline.contains("AMS646YD01") -> "Samsung"
            else -> rootCommand("cat /proc/cmdline | tr ' :=' '\n' | grep dsi | tr ' _' '\n' | tail -3 | head -1")
        }
    }

    private fun showPanelWarning() {
        Dlg.show(this, R.string.upanel)
        Dlg.setYes(R.string.chat) {
            openLink(this, grouplink)
            Pref.setAGREE(this, true)
            Dlg.close()
        }
        Dlg.setDismiss(R.string.nah) {
            Pref.setAGREE(this, true)
            Dlg.close()
        }
        Dlg.setNo(R.string.later) { Dlg.close() }
    }

    private fun setupClickListeners() {
        mainBinding.guide.setOnClickListener { openLink(this, guidelink) }
        mainBinding.group.setOnClickListener { openLink(this, grouplink) }
        mainBinding.cvInfo.setOnClickListener { checkupdate(true) }
        mainBinding.mnt.setOnClickListener { mountUI(this, filesDir) }
        mainBinding.quickBoot.setOnClickListener { quickbootUI(this, filesDir) }
        
        setupBackupListener()
        setupToolboxListeners()
        setupSettingsListeners()
    }

    private fun setupBackupListener() {
        mainBinding.backup.setOnClickListener {
            Dlg.show(this, R.string.backup_boot_question, R.drawable.ic_disk)
            Dlg.setDismiss(R.string.no) { Dlg.close() }
            Dlg.setNo(R.string.android) {
                performBackup(isAndroid = true)
            }
            Dlg.setYes(R.string.windows) {
                performBackup(isAndroid = false)
            }
        }
    }

    private fun performBackup(isAndroid: Boolean) {
        Dlg.dialogLoading()
        updateLastBackupDate()
        Thread {
            if (isAndroid) {
                androidBackup()
                modemBackup()
            } else {
                winBackup(filesDir)
            }
            runOnUiThread {
                Dlg.setText(R.string.backuped)
                Dlg.dismissButton()
            }
        }.start()
    }

    private fun setupToolboxListeners() {
        mainBinding.toolbox.setOnClickListener {
            navigateToView(toolboxBinding.root, R.string.toolbox_title)
        }

        toolboxBinding.sta.setOnClickListener { setupSta() }
        toolboxBinding.dumpModem.setOnClickListener { setupDumpModem() }
        toolboxBinding.flashUefi.setOnClickListener { setupFlashUefi() }
        toolboxBinding.dbkp.setOnClickListener { setupDbkp() }
        toolboxBinding.devcfg.setOnClickListener { setupDevcfg() }
        toolboxBinding.software.setOnClickListener { setupSoftware() }
        toolboxBinding.atlasos.setOnClickListener { setupAtlasOS() }
        toolboxBinding.usbhost.setOnClickListener { setupUsbHost() }
        toolboxBinding.rotation.setOnClickListener { setupRotation() }
        toolboxBinding.tablet.setOnClickListener { setupTabletMode() }
        toolboxBinding.setup.setOnClickListener { setupFrameworks() }
        toolboxBinding.defender.setOnClickListener { setupDefenderEdge() }
    }

    private fun navigateToView(targetView: View, titleRes: Int) {
        views.add(targetView)
        views[views.size - 2].startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out))
        setContentView(targetView)
        targetView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in))
        
        val toolbar = when(targetView) {
            toolboxBinding.root -> toolboxBinding.toolbarlayout.toolbar
            settingsBinding.root -> settingsBinding.toolbarlayout.toolbar
            scriptsBinding.root -> scriptsBinding.toolbarlayout.toolbar
            else -> mainBinding.toolbarlayout.toolbar
        }
        toolbar.title = getString(titleRes)
        toolbar.navigationIcon = AppCompatResources.getDrawable(this, R.drawable.ic_launcher_foreground)
    }

    private fun setupSettingsListeners() {
        val settingsClick = View.OnClickListener {
            navigateToView(settingsBinding.root, R.string.preferences)
            updateSettingsCheckboxes()
            settingsBinding.toolbarlayout.settings.visibility = View.GONE
        }

        listOf(mainBinding.toolbarlayout.settings, toolboxBinding.toolbarlayout.settings, scriptsBinding.toolbarlayout.settings).forEach {
            it.setOnClickListener(settingsClick)
        }

        settingsBinding.mountLocation.setOnChangeListener { b ->
            Pref.setMountLocation(this, b)
            updateWinPath(this)
        }

        setupQuickBootCheckboxes()
        
        settingsBinding.autobackup.setOnChangeListener { b -> Pref.setAuto(this, !b) }
        settingsBinding.autobackupA.setOnChangeListener { b -> Pref.setAutoA(this, !b) }
        settingsBinding.confirmation.setOnChangeListener { b -> Pref.setConfirm(this, b) }
        settingsBinding.securelock.setOnChangeListener { b -> Pref.setSecure(this, !b) }
        settingsBinding.automount.setOnChangeListener { b -> Pref.setAutoMount(this, b) }
        settingsBinding.appUpdate.setOnChangeListener { b -> Pref.setAppUpdate(this, b) }
        
        setupDevcfgSettings()
    }

    private fun updateSettingsCheckboxes() {
        val pairs = listOf(
            settingsBinding.backupQB to Pref.getBackup(this),
            settingsBinding.backupQBA to Pref.getBackupA(this),
            settingsBinding.autobackup to !Pref.getAuto(this),
            settingsBinding.autobackupA to !Pref.getAutoA(this),
            settingsBinding.confirmation to Pref.getConfirm(this),
            settingsBinding.automount to Pref.getAutoMount(this),
            settingsBinding.securelock to !Pref.getSecure(this),
            settingsBinding.mountLocation to Pref.getMountLocation(this),
            settingsBinding.appUpdate to Pref.getAppUpdate(this),
            settingsBinding.devcfg1 to (Pref.getDevcfg1(this) && settingsBinding.devcfg1.isVisible),
            settingsBinding.devcfg2 to Pref.getDevcfg2(this)
        )
        pairs.forEach { it.first.isChecked = it.second }
    }

    private fun setupQuickBootCheckboxes() {
        settingsBinding.backupQB.setOnChangeListener { _ ->
            if (Pref.getBackup(this)) {
                Pref.setBackup(this, false)
                settingsBinding.autobackup.visibility = View.VISIBLE
            } else {
                showBackupWarning {
                    Pref.setBackup(this, true)
                    settingsBinding.autobackup.visibility = View.GONE
                    settingsBinding.backupQB.isChecked = true
                }
                settingsBinding.backupQB.isChecked = false
            }
        }

        settingsBinding.backupQBA.setOnChangeListener { _ ->
            if (Pref.getBackupA(this)) {
                Pref.setBackupA(this, false)
                settingsBinding.autobackupA.visibility = View.VISIBLE
            } else {
                showBackupWarning {
                    Pref.setBackupA(this, true)
                    settingsBinding.autobackupA.visibility = View.GONE
                    settingsBinding.backupQBA.isChecked = true
                }
                settingsBinding.backupQBA.isChecked = false
            }
        }
    }

    private fun showBackupWarning(onAgree: () -> Unit) {
        Dlg.show(this, R.string.bwarn)
        Dlg.onCancel { /* Handled by UI state */ }
        Dlg.setDismiss(R.string.cancel) { Dlg.close() }
        Dlg.setYes(R.string.agree) {
            onAgree()
            Dlg.close()
        }
    }

    private fun setupDevcfgSettings() {
        val op7funny = rootCommand("cat /proc/cmdline | grep oplus")
        val isOP7Variant = listOf("guacamole", "guacamolet", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7ProTMO", "hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device)
        
        if (isOP7Variant && op7funny.isNotEmpty()) {
            settingsBinding.devcfg1.setOnChangeListener { b ->
                Pref.setDevcfg1(this, b)
                settingsBinding.devcfg2.visibility = if (b) View.VISIBLE else View.GONE
                Pref.setDevcfg2(this, false)
            }
            settingsBinding.devcfg2.setOnChangeListener { b -> Pref.setDevcfg2(this, b) }
            toolboxBinding.devcfg.visibility = View.VISIBLE
        } else {
            settingsBinding.devcfg1.visibility = View.GONE
            settingsBinding.devcfg2.visibility = View.GONE
            Pref.setDevcfg1(this, false)
            Pref.setDevcfg2(this, false)
        }
    }

    private fun checkUpdatesAndModels() {
        checkupdate()
        if (!BuildConfig.DEBUG) {
            checkdbkpmodel()
            settingsBinding.codename.visibility = View.GONE
        }
    }

    private fun setupSta() {
        Dlg.show(this, R.string.sta_question, R.drawable.android)
        Dlg.setNo(R.string.no) { Dlg.close() }
        Dlg.setYes(R.string.yes) {
            Dlg.dialogLoading()
            Thread {
                rootCommand("mkdir -p /sdcard/WOAHelper/sta || true")
                listOf("sta.exe", "sdd.exe", "sdd.conf", "boot.img_auto-flasher_V2.0.exe").forEach {
                    rootCommand("cp $filesDir/$it /sdcard/WOAHelper/sta/")
                }
                mount(filesDir)
                if (!isMounted()) {
                    runOnUiThread { Dlg.close(); mountfail() }
                    return@Thread
                }
                rootCommand("mkdir $winpath/sta")
                rootCommand("cp '$filesDir/Switch to Android.lnk' $winpath/Users/Public/Desktop")
                rootCommand("cp $filesDir/sta.exe $winpath/ProgramData/sta/sta.exe")
                rootCommand("cp /sdcard/WOAHelper/sta/* $winpath/sta/")
                runOnUiThread { Dlg.clearButtons(); Dlg.setText(R.string.done); Dlg.dismissButton() }
            }.start()
        }
    }

    private fun setupDumpModem() {
        Dlg.show(this, R.string.dump_modem_question, R.drawable.ic_modem)
        Dlg.setNo(R.string.no) { Dlg.close() }
        Dlg.setYes(R.string.yes) {
            Dlg.dialogLoading()
            Thread {
                if (!isMounted()) mount(filesDir)
                dump()
                runOnUiThread { Dlg.setText(R.string.lte); Dlg.dismissButton() }
            }.start()
        }
    }

    private fun setupFlashUefi() {
        Dlg.show(this, R.string.flash_uefi_question, R.drawable.ic_uefi)
        Dlg.setNo(R.string.no) { Dlg.close() }
        Dlg.setYes(R.string.yes) {
            Dlg.dialogLoading()
            Thread {
                try {
                    flash(finduefi)
                    runOnUiThread { Dlg.setText(R.string.flash); Dlg.dismissButton() }
                } catch (e: Exception) { e.printStackTrace() }
            }.start()
        }
    }

    private fun setupDbkp() {
        Dlg.dialogLoading()
        Thread {
            unpackKernel()
            val patched = Dbkp.isPatched(File("$filesDir/temp/kernel"))
            val props = Device.getVars(device)
            
            runOnUiThread {
                if (!patched) {
                    checkdbkpmodel()
                    Dlg.show(this, getString(R.string.dbkp_question, dbkpmodel), R.drawable.ic_uefi)
                    Dlg.setNo(R.string.no) { Dlg.close(); Thread { rootCommand("rm -rf $filesDir/temp") }.start() }
                    Dlg.setYes(R.string.yes) {
                        Thread {
                            rootCommand("cp $filesDir/dbkp.${props.dbkpCodename}.bin $filesDir/temp/dbkp.bin")
                            runOnUiThread { Dlg.dialogLoading() }
                            kernelPatch(getDbkpMessage(props.dbkpCodename), props.dbkpLink)
                        }.start()
                    }
                } else {
                    Dlg.show(this, getString(R.string.dbkp_question2), R.drawable.ic_uefi)
                    Dlg.setNo(R.string.no) { Dlg.close(); Thread { rootCommand("rm -rf $filesDir/temp") }.start() }
                    Dlg.setYes(R.string.reinstall) {
                        Thread {
                            rootCommand("cp $filesDir/dbkp.${props.dbkpCodename}.bin $filesDir/temp/dbkp.bin")
                            runOnUiThread { Dlg.dialogLoading() }
                            kernelReinstall(getDbkpMessage(props.dbkpCodename), props.dbkpLink)
                        }.start()
                    }
                    Dlg.setDismiss(R.string.uninstall) {
                        Dlg.dialogLoading()
                        kernelRemove()
                    }
                }
            }
        }.start()
    }

    private fun getDbkpMessage(codename: String) = when (codename) {
        "nabu" -> getString(R.string.nabu)
        "hotdog" -> getString(R.string.op7)
        "cepheus" -> getString(R.string.cepheus)
        else -> ""
    }

    private fun setupDevcfg() {
        if (!isNetworkConnected(this)) {
            if (rootCommand("find $filesDir -maxdepth 1 -name OOS11_devcfg_*").isEmpty()) {
                nointernet(); return
            }
        }
        Dlg.show(this, getString(R.string.devcfg_question, dbkpmodel), R.drawable.ic_uefi)
        Dlg.setNo(R.string.no) { Dlg.close() }
        Dlg.setYes(R.string.yes) {
            Dlg.dialogLoading()
            Thread {
                performDevcfgFlash()
            }.start()
        }
    }

    private fun performDevcfgFlash() {
        rootCommand("mkdir -p /sdcard/WOAHelper/Backups || true")
        val devcfgDevice = if (listOf("guacamole", "OnePlus7Pro", "OnePlus7Pro4G").contains(device)) "guacamole" else "hotdog"
        
        if (rootCommand("find $filesDir -maxdepth 1 -name original-devcfg.img").isEmpty()) {
            rootCommand("dd bs=8M if=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix) of=/sdcard/WOAHelper/Backups/original-devcfg.img")
            rootCommand("cp /sdcard/WOAHelper/Backups/original-devcfg.img $filesDir/original-devcfg.img")
        }

        if (rootCommand("find $filesDir -maxdepth 1 -name OOS11_devcfg_*").isEmpty()) {
            rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_$devcfgDevice.img -O /sdcard/WOAHelper/Backups/OOS11_devcfg_$devcfgDevice.img")
            rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_$devcfgDevice.img -O /sdcard/WOAHelper/Backups/OOS12_devcfg_$devcfgDevice.img")
            rootCommand("cp /sdcard/WOAHelper/Backups/OOS11_devcfg_$devcfgDevice.img $filesDir")
            rootCommand("cp /sdcard/WOAHelper/Backups/OOS12_devcfg_$devcfgDevice.img $filesDir")
        }
        rootCommand("dd bs=8M if=$filesDir/OOS11_devcfg_$devcfgDevice.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)")

        runOnUiThread {
            rootCommand("mkdir -p /sdcard/WOAHelper/staDevcfg || true")
            rootCommand("cp $filesDir/sdd.exe /sdcard/WOAHelper/staDevcfg/sdd.exe")
            rootCommand("cp $filesDir/devcfg-sdd.conf /sdcard/WOAHelper/staDevcfg/sdd.conf")
            mount(filesDir)
            if (isMounted()) {
                rootCommand("mkdir $winpath/sta || true ")
                rootCommand("cp '$filesDir/Flash Devcfg.lnk' $winpath/Users/Public/Desktop")
                rootCommand("cp $filesDir/sdd.exe $winpath/sta/sdd.exe")
                rootCommand("cp $filesDir/devcfg-sdd.conf $winpath/sta/sdd.conf")
                rootCommand("cp /sdcard/WOAHelper/Backups/original-devcfg.img $winpath/original-devcfg.img")
            }
            Dlg.setText(R.string.devcfg)
            Dlg.setDismiss(R.string.dismiss) { Dlg.close() }
            Dlg.setYes(R.string.reboot) { rootCommand("/system/bin/svc power reboot") }
        }
    }

    private fun setupSoftware() {
        Dlg.show(this, R.string.software_question, R.drawable.ic_sensor)
        Dlg.setNo(R.string.no) { Dlg.close() }
        Dlg.setYes(R.string.yes) {
            Dlg.dialogLoading()
            Thread {
                if (!isMounted()) mount(filesDir)
                if (!isMounted()) {
                    runOnUiThread { Dlg.close(); mountfail() }
                    return@Thread
                }
                rootCommand("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                val files = listOf("WorksOnWoa.url", "TestedSoftware.url", "ARMSoftware.url", "ARMRepo.url")
                files.forEach { rootCommand("cp $filesDir/$it /sdcard/WOAHelper/Toolbox") }
                rootCommand("mkdir $winpath/Toolbox || true ")
                files.forEach { rootCommand("cp $filesDir/$it $winpath/Toolbox") }
                runOnUiThread { Dlg.setText(R.string.done); Dlg.dismissButton() }
            }.start()
        }
    }

    private fun setupAtlasOS() {
        if (!isNetworkConnected(this)) { nointernet(); return }
        Dlg.show(this, R.string.atlasos_question, R.drawable.ic_ar)
        Dlg.dismissButton()
        
        val downloadPlaybook = { _: String, url: String, targetName: String ->
            Dlg.dialogLoading(); Dlg.setBar(0); Dlg.setIcon(R.drawable.ic_download)
            Thread {
                rootCommand("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                rootCommand("wget $url -O /sdcard/WOAHelper/Toolbox/$targetName")
                Dlg.setBar(50)
                rootCommand("wget https://download.ameliorated.io/AME%20Beta.zip -O /sdcard/WOAHelper/Toolbox/AMEWizardBeta.zip")
                Dlg.setBar(80)
                runOnUiThread {
                    mount(filesDir)
                    if (!isMounted()) { Dlg.close(); mountfail(); return@runOnUiThread }
                    Dlg.setIcon(R.drawable.ic_ar); Dlg.hideBar()
                    rootCommand("mkdir $winpath/Toolbox || true ")
                    rootCommand("cp /sdcard/WOAHelper/Toolbox/$targetName $winpath/Toolbox/$targetName")
                    rootCommand("cp /sdcard/WOAHelper/Toolbox/AMEWizardBeta.zip $winpath/Toolbox")
                    Dlg.setText(R.string.done); Dlg.dismissButton()
                }
            }.start()
        }

        Dlg.setNo(R.string.revios) {
            downloadPlaybook("ReviOS", "https://github.com/n00b69/modified-playbooks/releases/download/ReviOS/ReviPlaybook.apbx", "ReviPlaybook.apbx")
        }
        Dlg.setYes(R.string.atlasos) {
            downloadPlaybook("AtlasOS", "https://github.com/n00b69/modified-playbooks/releases/download/AtlasOS/AtlasPlaybook.apbx", "AtlasPlaybook_v0.5.0.apbx")
        }
    }

    private fun setupUsbHost() {
        Dlg.show(this, R.string.usbhost_question, R.drawable.ic_mnt)
        Dlg.setNo(R.string.no) { Dlg.close() }
        Dlg.setYes(R.string.yes) {
            Dlg.dialogLoading()
            Thread {
                rootCommand("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                rootCommand("cp $filesDir/usbhostmode.exe /sdcard/WOAHelper/Toolbox/")
                mount(filesDir)
                if (!isMounted()) {
                    runOnUiThread { Dlg.close(); mountfail() }
                    return@Thread
                }
                rootCommand("mkdir $winpath/Toolbox || true ")
                rootCommand("cp /sdcard/WOAHelper/Toolbox/usbhostmode.exe $winpath/Toolbox")
                rootCommand("cp '$filesDir/USB Host Mode.lnk' $winpath/Users/Public/Desktop")
                runOnUiThread { Dlg.setText(R.string.done); Dlg.dismissButton() }
            }.start()
        }
    }

    private fun setupRotation() {
        Dlg.show(this, R.string.rotation_question, R.drawable.ic_disk)
        Dlg.setNo(R.string.no) { Dlg.close() }
        Dlg.setYes(R.string.yes) {
            Dlg.dialogLoading()
            Thread {
                rootCommand("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                rootCommand("cp $filesDir/QuickRotate_V6.1.6.exe /sdcard/WOAHelper/Toolbox/")
                mount(filesDir)
                if (!isMounted()) {
                    runOnUiThread { Dlg.close(); mountfail() }
                    return@Thread
                }
                rootCommand("mkdir $winpath/Toolbox || true ")
                listOf("/Toolbox", "/Users/Public/Desktop").forEach {
                    rootCommand("cp /sdcard/WOAHelper/Toolbox/QuickRotate_V6.1.6.exe $winpath$it")
                }
                runOnUiThread { Dlg.setText(R.string.done); Dlg.dismissButton() }
            }.start()
        }
    }

    private fun setupTabletMode() {
        Dlg.show(this, R.string.tablet_question, R.drawable.ic_sensor)
        Dlg.setNo(R.string.no) { Dlg.close() }
        Dlg.setYes(R.string.yes) {
            Dlg.dialogLoading()
            Thread {
                rootCommand("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                rootCommand("cp $filesDir/Optimized_Taskbar_Control_V3.2.exe /sdcard/WOAHelper/Toolbox/")
                mount(filesDir)
                if (!isMounted()) {
                    runOnUiThread { Dlg.close(); mountfail() }
                    return@Thread
                }
                rootCommand("mkdir $winpath/Toolbox || true ")
                rootCommand("cp /sdcard/WOAHelper/Toolbox/Optimized_Taskbar_Control_V3.2.exe $winpath/Toolbox")
                runOnUiThread { Dlg.setText(R.string.done); Dlg.dismissButton() }
            }.start()
        }
    }

    private fun setupFrameworks() {
        if (!isNetworkConnected(this)) { nointernet(); return }
        Dlg.show(this, R.string.setup_question, R.drawable.ic_mnt)
        Dlg.setNo(R.string.no) { Dlg.close() }
        Dlg.setYes(R.string.yes) {
            Dlg.dialogLoading(); Dlg.setIcon(R.drawable.ic_download); Dlg.setBar(0)
            Thread {
                rootCommand("mkdir -p /sdcard/WOAHelper/Frameworks || true")
                rootCommand("cp $filesDir/install.bat /sdcard/WOAHelper/Frameworks/install.bat")
                val installers = listOf(
                    "PhysX-9.13.0604-SystemSoftware-Legacy.msi", "PhysX_9.23.1019_SystemSoftware.exe", "xnafx40_redist.msi",
                    "opengl.appx", "2005vcredist_x64.EXE", "2005vcredist_x86.EXE", "2008vcredist_x64.exe", "2008vcredist_x86.exe",
                    "2010vcredist_x64.exe", "2010vcredist_x86.exe", "2012vcredist_x64.exe", "2012vcredist_x86.exe",
                    "2013vcredist_x64.exe", "2013vcredist_x86.exe", "2015VC_redist.x64.exe", "2015VC_redist.x86.exe",
                    "2022VC_redist.arm64.exe", "dxwebsetup.exe", "oalinst.exe"
                )
                installers.forEach {
                    rootCommand("wget https://github.com/n00b69/woasetup/releases/download/Installers/$it -O /sdcard/WOAHelper/Frameworks/$it")
                    runOnUiThread { Dlg.setBar(Dlg.bar!!.progress + 5) }
                }
                runOnUiThread {
                    mount(filesDir)
                    if (isMounted()) {
                        rootCommand("mkdir -p $winpath/Toolbox/Frameworks || true ")
                        rootCommand("cp /sdcard/WOAHelper/Frameworks/* $winpath/Toolbox/Frameworks")
                    }
                    Dlg.setIcon(R.drawable.ic_mnt); Dlg.hideBar(); Dlg.setText(R.string.done); Dlg.dismissButton()
                }
            }.start()
        }
    }

    private fun setupDefenderEdge() {
        Dlg.show(this, R.string.defender_question, R.drawable.edge2)
        Dlg.setNo(R.string.no) { Dlg.close() }
        Dlg.setYes(R.string.yes) {
            Dlg.dialogLoading()
            Thread {
                if (rootCommand("find $filesDir -maxdepth 1 -name DefenderRemover.exe").isEmpty()) {
                    if (!isNetworkConnected(this)) {
                        runOnUiThread { Dlg.close(); nointernet() }
                        return@Thread
                    }
                    rootCommand("wget https://github.com/n00b69/woasetup/releases/download/Installers/DefenderRemover.exe -O /sdcard/WOAHelper/Toolbox/DefenderRemover.exe")
                    rootCommand("cp /sdcard/WOAHelper/Toolbox/DefenderRemover.exe $filesDir/DefenderRemover.exe")
                } else {
                    rootCommand("cp $filesDir/DefenderRemover.exe /sdcard/WOAHelper/Toolbox/DefenderRemover.exe")
                }
                mount(filesDir)
                if (isMounted()) {
                    rootCommand("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                    rootCommand("mkdir $winpath/Toolbox || true ")
                    rootCommand("cp $filesDir/RemoveEdge.bat /sdcard/WOAHelper/Toolbox")
                    rootCommand("cp $filesDir/DefenderRemover.exe $winpath/Toolbox")
                    rootCommand("cp $filesDir/RemoveEdge.bat $winpath/Toolbox")
                }
                runOnUiThread { Dlg.setText(R.string.done); Dlg.dismissButton() }
            }.start()
        }
    }

    public override fun onResume() {
        super.onResume()
        updateWinPath(this)
        updateMountText()
        checkwin()
        checkuefi()
        if (Shell.isAppGrantedRoot() != true) {
            Dlg.show(this, R.string.nonroot)
            Dlg.setCancelable(false)
        }
    }

    private fun copyAssets() {
        assets.list("")?.forEach { filename ->
            try {
                assets.open(filename).use { input ->
                    File(filesDir, filename).outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (_: IOException) {
                // Ignore directories
            }
        }

        rootCommand("chmod 644 $filesDir/libfuse-lite.so && chown root:root $filesDir/libfuse-lite.so")
        rootCommand("chmod 644 $filesDir/libntfs-3g.so && chown root:root $filesDir/libntfs-3g.so")
        rootCommand("chmod 755 $filesDir/mount.ntfs && chown root:root $filesDir/mount.ntfs")
    }

    private fun aspectRatio(): Float {
        val metrics = Resources.getSystem().displayMetrics
        val width = metrics.widthPixels.toFloat()
        val height = metrics.heightPixels.toFloat()
        return if (width > height) width / height else height / width
    }

    private fun isTablet(): Boolean = aspectRatio() < 1.7

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
        
        mainBinding.app.orientation = if (isPortrait) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
        mainBinding.top.orientation = if (isPortrait) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
        
        if (!tablet) {
            mainBinding.DeviceImage.setImageResource(Device.getVars(device).image)
            mainBinding.DeviceImage.visibility = if (isPortrait) View.VISIBLE else View.GONE
            mainBinding.up.layoutParams.height = if (isPortrait) LinearLayout.LayoutParams.WRAP_CONTENT else LinearLayout.LayoutParams.MATCH_PARENT
            
            val padding = if (isPortrait) 0 else 100
            mainBinding.infoText.setPadding(padding, 0, padding, 0)
        }
    }

    private fun unpackKernel(bootIMG: String = "/dev/block/by-name/boot$(getprop ro.boot.slot_suffix)") {
        File("$filesDir/temp").mkdir()
        rootCommand("( cd $filesDir/temp ; $(find /data/adb -name magiskboot) unpack $bootIMG)")
    }

    private fun repackKernel(bootIMG: String = "/dev/block/by-name/boot$(getprop ro.boot.slot_suffix)") {
        rootCommand("( cd $filesDir/temp ; $(find /data/adb -name magiskboot) repack $bootIMG)")
    }

    private fun kernelPatch(message: String, link: String) {
        Thread {
            androidBackup()
            rootCommand("wget $link -O $filesDir/temp/file.fd")
            val succ = Dbkp.patch(
                File("$filesDir/temp/kernel"), File("$filesDir/temp/file.fd"),
                File("$filesDir/temp/dbkp.bin"), File("$filesDir/temp/output"),
                File("$filesDir/dbkp8150.cfg")
            )
            
            if (succ != 0) {
                runOnUiThread {
                    Dlg.clearButtons(); Dlg.setText(R.string.wrong); Dlg.setDismiss(R.string.dismiss) { Dlg.close() }
                }
                return@Thread
            }
            
            rootCommand("mv $filesDir/temp/output $filesDir/temp/kernel")
            repackKernel()
            rootCommand("cp $filesDir/temp/new-boot.img /sdcard/WOAHelper/Backups/patched-boot.img")
            
            val partition = if ("cepheus" == device) "boot" else "boot_a bs=16M && dd if=/sdcard/WOAHelper/Backups/patched-boot.img of=/dev/block/by-name/boot_b"
            rootCommand("dd if=/sdcard/WOAHelper/Backups/patched-boot.img of=/dev/block/by-name/$partition bs=16M")
            
            rootCommand("rm -rf $filesDir/temp")
            runOnUiThread {
                Dlg.clearButtons(); Dlg.setText(getString(R.string.dbkp, message))
                Dlg.setDismiss(R.string.dismiss) { Dlg.close() }
                Dlg.setNo(R.string.reboot) { rootCommand("/system/bin/svc power reboot") }
            }
        }.start()
    }

    private fun kernelRemove() {
        Thread {
            androidBackup()
            val succ = Dbkp.removePatch(File("$filesDir/temp/kernel"), File("$filesDir/temp/out"))
            if (succ != 0) {
                runOnUiThread {
                    Dlg.clearButtons(); Dlg.setText(R.string.wrong); Dlg.setDismiss(R.string.dismiss) { Dlg.close() }
                }
                return@Thread
            }
            
            rootCommand("mv $filesDir/temp/out $filesDir/temp/kernel")
            repackKernel()
            rootCommand("cp temp/new-boot.img /sdcard/WOAHelper/Backups/unpatched-boot.img")
            
            val partition = if ("cepheus" == device) "boot" else "boot_a bs=16M && dd if=/sdcard/WOAHelper/Backups/unpatched-boot.img of=/dev/block/by-name/boot_b"
            rootCommand("dd if=/sdcard/WOAHelper/Backups/unpatched-boot.img of=/dev/block/by-name/$partition bs=16M")
            
            rootCommand("rm -rf $filesDir/temp")
            runOnUiThread {
                Dlg.clearButtons(); Dlg.setText(getString(R.string.dbkpuninstall))
                Dlg.setNo(R.string.reboot) { rootCommand("/system/bin/svc power reboot") }
                Dlg.setDismiss(R.string.dismiss) { Dlg.close() }
            }
        }.start()
    }

    private fun kernelReinstall(message: String, link: String) {
        Thread {
            androidBackup()
            rootCommand("wget $link -O $filesDir/temp/file.fd")
            val succ = Dbkp.updateFD(File("$filesDir/temp/kernel"), File("$filesDir/temp/file.fd"), File("$filesDir/temp/out"))
            if (succ != 0) {
                runOnUiThread {
                    Dlg.clearButtons(); Dlg.setText(R.string.wrong); Dlg.setDismiss(R.string.dismiss) { Dlg.close() }
                }
                return@Thread
            }
            repackKernel()
            rootCommand("cp temp/new-boot.img /sdcard/WOAHelper/Backups/patched-boot.img")
            
            val partition = if ("cepheus" == device) "boot" else "boot_a bs=16M && dd if=/sdcard/WOAHelper/Backups/patched-boot.img of=/dev/block/by-name/boot_b"
            rootCommand("dd if=/sdcard/WOAHelper/Backups/patched-boot.img of=/dev/block/by-name/$partition bs=16M")
            
            rootCommand("rm -rf $filesDir/temp")
            runOnUiThread {
                Dlg.clearButtons(); Dlg.setText(getString(R.string.dbkp, message))
                Dlg.setDismiss(R.string.dismiss) { Dlg.close() }
                Dlg.setNo(R.string.reboot) { rootCommand("/system/bin/svc power reboot") }
            }
        }.start()
    }

    private fun dump() {
        listOf("modemst1" to "bootmodem_fs1", "modemst2" to "bootmodem_fs2").forEach {
            rootCommand("dd if=/dev/block/by-name/${it.first} of=$(find $winpath/Windows/System32/DriverStore/FileRepository -name qcremotefs8150.inf_arm64_*)/${it.second}")
        }
    }

    private fun checkdbkpmodel() {
        dbkpmodel = when {
            listOf("guacamole", "guacamolet", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7ProTMO").contains(device) -> "ONEPLUS 7 PRO"
            listOf("hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device) -> "ONEPLUS 7T PRO"
            device == "cepheus" -> "XIAOMI MI 9"
            device == "nabu" -> "XIAOMI PAD 5"
            else -> "UNSUPPORTED"
        }
    }

    private fun checkuefi() {
        rootCommand("mkdir /sdcard/UEFI")
        finduefi = "\"" + rootCommand(getString(R.string.uefiChk)) + "\""
        val found = finduefi.contains("img")
        
        listOf(mainBinding.quickBoot, toolboxBinding.flashUefi).forEach { it.isEnabled = found }
        
        mainBinding.quickBoot.setTitle(if (found) R.string.quickboot_title else R.string.uefi_not_found)
        toolboxBinding.flashUefi.setTitle(if (found) R.string.flash_uefi_title else R.string.uefi_not_found)
        
        mainBinding.quickBoot.setSubtitle(if (found) getString(R.string.quickboot_subtitle_nabu) else getString(R.string.uefi_not_found_subtitle, device))
        toolboxBinding.flashUefi.setSubtitle(if (found) getString(R.string.flash_uefi_subtitle) else getString(R.string.uefi_not_found_subtitle, device))
    }

    private fun checkwin() {
        if (win.isNotEmpty() || BuildConfig.DEBUG) return
        Dlg.show(this, R.string.partition)
        Dlg.setCancelable(false)
        Dlg.setYes(R.string.guide) { openLink(this, guidelink) }
        listOf(mainBinding.mnt, mainBinding.toolbox, mainBinding.quickBoot, toolboxBinding.flashUefi).forEach { it.isEnabled = false }
    }

    private fun checkupdate() {
        checkupdate(false)
    }

    private fun checkupdate(manual: Boolean) {
        if (!isNetworkConnected(this)) {
            if (manual) nointernet()
            return
        }
        if (Pref.getAppUpdate(this) && !manual) return
        if (manual) {
            Dlg.show(this, R.string.please_wait)
            Dlg.setCancelable(false)
        }
        
        val version = Download.text("https://raw.githubusercontent.com/n00b69/woa-helper-update/main${if (BuildConfig.DEBUG) "/debug" else ""}/README.md")
        val changelog = Download.text("https://raw.githubusercontent.com/n00b69/woa-helper-update/main${if (BuildConfig.DEBUG) "/debug" else ""}/changelog.md")
        
        if (version.isEmpty()) {
            if (manual) nointernet()
            return
        }
        
        if (BuildConfig.VERSION_NAME == version) {
            if (manual) {
                Dlg.setText(getString(R.string.update3))
                Dlg.dismissButton()
            }
            return
        }
        
        if (!manual) Dlg.show(this, "")
        Dlg.setText("${getString(R.string.update1)}: $version\n$changelog")
        Dlg.setNo(R.string.later) { Dlg.close() }
        Dlg.setYes(R.string.update) { openLink(this, "https://github.com/n00b69/woa-helper/releases/tag/APK") }
    }

    private fun mountfail() {
        Dlg.show(this, "${getString(R.string.mountfail)}\n${getString(R.string.internalstorage)}")
        Dlg.dismissButton()
        Dlg.setYes(R.string.chat) { openLink(this, "https://t.me/woahelperchat") }
    }

    companion object {
        private var mounted: String = ""
        private var win: String = ""
        private var winpath: String = ""
        private var finduefi: String = ""
        private var device: String = ""
        private var dbkpmodel: String = ""
        private var boot: String = ""
        private var blur = 0
        private lateinit var rootShell: Shell
        private lateinit var masterShell: Shell

        @JvmStatic
        fun isNetworkConnected(context: Context): Boolean {
            val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || 
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || 
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        }

        internal fun flash(uefi: String?) {
            rootCommand("dd if=$uefi of=/dev/block/bootdevice/by-name/boot$(getprop ro.boot.slot_suffix) bs=16M")
        }

        @JvmStatic
        fun mountUI(activity: AppCompatActivity, filesDir: File) {
            updateWinPath(activity)
            val question = if (isMounted()) R.string.unmount_question else R.string.mount_question
            Dlg.show(activity, if (isMounted()) activity.getString(question) else activity.getString(question, winpath), R.drawable.ic_mnt)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                Thread {
                    val wasMounted = isMounted()
                    if (wasMounted) {
                        unmount()
                    } else {
                        mount(filesDir)
                    }
                    val isNowMounted = isMounted()
                    
                    activity.runOnUiThread {
                        (activity as? MainActivity)?.updateMountText()
                        if (wasMounted) {
                            Dlg.setText(R.string.unmounted)
                            Dlg.dismissButton()
                        } else if (isNowMounted) {
                            Dlg.setText("${activity.getString(R.string.mounted)}\n$winpath")
                            Dlg.dismissButton()
                        } else {
                            Dlg.hideIcon()
                            Dlg.setText(R.string.mountfail)
                            Dlg.setYes(R.string.chat) { openLink(activity, "https://t.me/woahelperchat") }
                            Dlg.setNo(R.string.dismiss) { Dlg.close() }
                        }
                    }
                }.start()
            }
        }

        @JvmStatic
        fun quickbootUI(activity: AppCompatActivity, filesDir: File) {
            if (boot.isEmpty()) boot = getBoot()
            updateWinPath(activity)
            if (device.isEmpty()) updateDevice(activity)
            Dlg.show(activity, R.string.quickboot_question, R.drawable.ic_launcher_foreground)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                Thread {
                    performQuickBoot(activity, filesDir)
                }.start()
            }
        }

        private fun performQuickBoot(activity: AppCompatActivity, filesDir: File) {
            mount(filesDir)
            val currentWinPath = updateWinPath(activity)
            
            if (Pref.getBackup(activity) || (!Pref.getAuto(activity) && rootCommand("ls $currentWinPath | grep boot.img").isEmpty())) {
                winBackup(filesDir)
                activity.runOnUiThread { (activity as? MainActivity)?.updateLastBackupDate() }
            }
            if (Pref.getBackupA(activity) || (!Pref.getAutoA(activity) && rootCommand("find /sdcard/WOAHelper/Backups | grep boot.img").isEmpty())) {
                androidBackup()
                activity.runOnUiThread { (activity as? MainActivity)?.updateLastBackupDate() }
            }
            
            if (Pref.getDevcfg1(activity)) {
                if (!isNetworkConnected(activity)) { 
                    activity.runOnUiThread { (activity as? MainActivity)?.nointernet() }
                    return 
                }
                flashDevcfgQuickBoot(activity, filesDir)
            }
            
            flash(finduefi)
            if (rootCommand("find /sdcard/WOAHelper/Backups | grep modemst1.img").isEmpty()) modemBackup()
            rootCommand("/system/bin/svc power reboot")
            
            activity.runOnUiThread {
                Dlg.setText(R.string.wrong)
                Dlg.dismissButton()
            }
        }

        private fun flashDevcfgQuickBoot(activity: Context, filesDir: File) {
            val devcfgDevice = if (listOf("guacamole", "OnePlus7Pro", "OnePlus7Pro4G").contains(device)) "guacamole" else "hotdog"
            if (rootCommand("find $filesDir -maxdepth 1 -name original-devcfg.img").isEmpty()) {
                rootCommand("dd bs=8M if=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix) of=/sdcard/original-devcfg.img")
                rootCommand("cp /sdcard/original-devcfg.img $filesDir/original-devcfg.img")
            }
            if (rootCommand("find $filesDir -maxdepth 1 -name OOS11_devcfg_*").isEmpty()) {
                rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_$devcfgDevice.img -O /sdcard/OOS11_devcfg_$devcfgDevice.img")
                rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_$devcfgDevice.img -O /sdcard/OOS12_devcfg_$devcfgDevice.img")
                rootCommand("cp /sdcard/OOS11_devcfg_$devcfgDevice.img $filesDir")
                rootCommand("cp /sdcard/OOS12_devcfg_$devcfgDevice.img $filesDir")
            }
            rootCommand("dd bs=8M if=${filesDir}/OOS11_devcfg_$devcfgDevice.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)")
            
            if (Pref.getDevcfg2(activity)) {
                rootCommand("mkdir $winpath/sta || true ")
                rootCommand("cp '${filesDir}/Flash Devcfg.lnk' $winpath/Users/Public/Desktop")
                rootCommand("cp $filesDir/sdd.exe $winpath/sta/sdd.exe")
                rootCommand("cp $filesDir/devcfg-boot-sdd.conf $winpath/sta/sdd.conf")
                rootCommand("cp $filesDir/original-devcfg.img $winpath/original-devcfg.img")
            }
        }

        internal fun mount(filesDir: File) {
            if (win.isEmpty()) win = getWin()
            if (isMounted()) return
            rootCommand("mkdir $winpath || true")
            rootCommand("cd $filesDir")
            rootCommand("./mount.ntfs $win $winpath", true)
        }

        private fun unmount() {
            rootCommand("umount $winpath", true)
            rootCommand("rmdir $winpath")
        }

        internal fun winBackup(filesDir: File) {
            mount(filesDir)
            rootCommand("dd bs=8M if=$boot of=$winpath/boot.img")
        }

        internal fun androidBackup() {
            rootCommand("mkdir -p /sdcard/WOAHelper/Backups || true")
            rootCommand("dd bs=8M if=$boot of=/sdcard/WOAHelper/Backups/boot.img")
        }

        internal fun modemBackup() {
            listOf("modemst1", "modemst2", "fsc", "fsg", "ftm", "persist", "efs").forEach {
                rootCommand("dd bs=8M if=/dev/block/by-name/$it of=/sdcard/WOAHelper/Backups/$it.img")
            }
        }

        internal fun MainActivity.nointernet() {
            Dlg.show(this, R.string.internet)
            Dlg.dismissButton()
        }

        @JvmStatic
        fun showBlur(activity: MainActivity) {
            blur++
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val blurEffect = RenderEffect.createBlurEffect(15f, 15f, Shader.TileMode.CLAMP)
                activity.findViewById<View>(android.R.id.content).setRenderEffect(blurEffect)
            } else {
                runSilently {
                    listOf(activity.mainBinding.blur, activity.settingsBinding.blur, activity.toolboxBinding.blur, activity.scriptsBinding.blur).forEach {
                        it.visibility = View.VISIBLE
                    }
                }
            }
        }

        @JvmStatic
        fun hideBlur(activity: MainActivity, check: Boolean) {
            if (!check) blur = 1
            blur--
            if (blur > 0) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                activity.findViewById<View>(android.R.id.content).setRenderEffect(null)
            } else {
                runSilently {
                    listOf(activity.mainBinding.blur, activity.settingsBinding.blur, activity.toolboxBinding.blur, activity.scriptsBinding.blur).forEach {
                        it.visibility = View.GONE
                    }
                }
            }
        }

        @JvmStatic
        fun runSilently(action: () -> Unit) {
            try { action() } catch (_: Exception) {}
        }

        internal fun MainActivity.updateLastBackupDate() {
            val date = SimpleDateFormat("dd-MM HH:mm", Locale.US).format(Date())
            Pref.setDate(this, date)
            mainBinding.tvDate.text = getString(R.string.last, date)
        }

        internal fun MainActivity.updateMountText() {
            mounted = getString(if (isMounted()) R.string.unmountt else R.string.mountt)
            runOnUiThread {
                mainBinding.mnt.setTitle(getString(R.string.mnt_title, mounted))
            }
        }

        internal fun getWin(): String {
            val partition = rootCommand("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1")
            return rootCommand("realpath $partition")
        }

        internal fun updateWinPath(context: Context): String {
            winpath = if (Pref.getMountLocation(context)) "/mnt/Windows" else "${Environment.getExternalStorageDirectory().path}/Windows"
            return winpath
        }

        internal fun updateDevice(context: Context) {
            rootCommand("pm uninstall id.kuato.woahelper")
            device = Pref.codenameChanger(false, context, Build.DEVICE)
        }

        internal fun getBoot(): String {
            val partition = rootCommand("find /dev/block | grep -i \"/boot$(getprop ro.boot.slot_suffix)$\" | head -1")
            return rootCommand("realpath $partition")
        }

        fun shellInit(dir: File) {
            if (::rootShell.isInitialized) return
            rootShell = Shell.Builder.create().build()
            masterShell = Shell.Builder.create().setFlags(Shell.FLAG_MOUNT_MASTER).build()
            listOf(rootShell, masterShell).forEach { shell ->
                rootCommand("ASH_STANDALONE=1 $(find /data/adb/ -name busybox) ash", shell)
                rootCommand("cd $dir", shell)
            }
        }

        fun rootCommand(command: String, master: Boolean = false): String = rootCommand(command, if (master) masterShell else rootShell)

        fun rootCommand(command: String, shell: Shell): String {
            if (BuildConfig.DEBUG) Log.d("debug stdout", command)
            val out = ArrayList<String>()
            val err = ArrayList<String>()
            shell.newJob().add(command).to(out, err).exec()
            if (BuildConfig.DEBUG && out.isNotEmpty()) Log.d("debug stdout", out.toString())
            if (BuildConfig.DEBUG && err.isNotEmpty()) Log.w("debug stderr", err.toString())
            return out.lastOrNull() ?: ""
        }

        @JvmStatic
        fun isMounted(): Boolean = rootCommand("mount | grep ${getWin()}").isNotEmpty()

        internal fun openLink(context: Context, link: String) {
            context.startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
        }
    }
}
