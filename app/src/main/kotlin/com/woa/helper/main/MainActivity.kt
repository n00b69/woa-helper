package com.woa.helper.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Pair
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatSpinner
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
import com.woa.helper.widgets.MountWidget
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

@SuppressLint("StaticFieldLeak")
class MainActivity : AppCompatActivity() {
    private var grouplink = "https://t.me/woahelperchat"
    private var guidelink = "https://github.com/n00b69"
    private var unsupported = false
    private var tablet = false
    private val views: MutableList<View> = ArrayList()
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

    private fun aspectRatio() : Float{
        val metrics = Resources.getSystem().displayMetrics
        var size1 = metrics.widthPixels
        var size2 = metrics.heightPixels
        if (size1>size2)
            size1=size2.also{size2 =size1}
        return size2.toFloat() / size1.toFloat()
    }

    private fun isTablet(): Boolean{
        return aspectRatio() < 1.7
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        this.enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Pref.setFilesDir(this, filesDir.toString())
        shellInit(this.filesDir)
        onBackPressedDispatcher.addCallback(this) {
            if (0 == views.size - 1) {
                moveTaskToBack(true)
                exitProcess(0)
            } else {
                if (BuildConfig.DEBUG) {
                    if (views[views.size - 1] === k.root) {
                        val textbox = findViewById<TextView>(R.id.codename)
                        if (textbox.text.isNotBlank() && (textbox.text.toString() != Pref.codenameChanger(
                                false,
                                context!!,
                                ""
                            ))
                        ) {
                            Pref.codenameChanger(true, context!!, textbox.text.toString())
                        }
                    }
                }
                views[views.size - 1].startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_back_out))
                views.removeAt(views.size - 1)
                setContentView(views[views.size - 1])
                views[views.size - 1].startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_back_in))
            }
        }

        copyAssets()
        x = ActivityMainBinding.inflate(layoutInflater)
        k = SetPanelBinding.inflate(layoutInflater)
        n = ToolboxBinding.inflate(layoutInflater)
        z = ScriptsBinding.inflate(layoutInflater)

        context = this
        download.permission(this)
        setContentView(x.root)

        views.clear()
        views.add(x.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _: View?, insets: WindowInsetsCompat ->
            val sysInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            arrayOf(x.app, n.app, k.app, z.app).forEach { it.setPadding(0, 0, 0, sysInsets.bottom) }
            arrayOf(x.linearLayout, n.linearLayout, k.linearLayout, z.linearLayout).forEach { it.setPadding(sysInsets.left, sysInsets.top, sysInsets.right, 0) }
            insets
        }

        val languages: MutableList<String> = mutableListOf(getString(R.string.default1))
        val locales: MutableList<String> = mutableListOf("und")
        for (i in BuildConfig.LOCALES) {
            locales.add(i!!.lowercase(Locale.getDefault()))
            val locale = checkNotNull(LocaleListCompat.forLanguageTags(i)[0])
            val country = locale.getDisplayCountry(locale)
            val lang = locale.getDisplayLanguage(locale) + (if (!country.isEmpty()) " ($country)" else "")
            languages.add(lang)
        }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            languages
        )
        setSupportActionBar(x.toolbarlayout.toolbar)
        x.toolbarlayout.toolbar.setTitle(R.string.app_name)
        x.toolbarlayout.toolbar.subtitle = "v${BuildConfig.VERSION_NAME}"+if (BuildConfig.DEBUG) " (Debug)" else ""
        x.toolbarlayout.toolbar.setNavigationIcon(R.drawable.ic_launcher_foreground)
        arrayOf(x.toolbarlayout.settings, k.toolbarlayout.settings, n.toolbarlayout.settings, z.toolbarlayout.settings).forEach { it.setColorFilter(R.color.md_theme_primary) }

        win = getWin()
        boot = getBoot()
        updateDevice()
        updateWinPath()
        updateMountText()
        x.tvDate.text = String.format(getString(R.string.last), Pref.getDate(this))

        val slot = rootCommand("getprop ro.boot.slot_suffix")
        if (slot.isEmpty()) x.tvSlot.visibility = View.GONE
        else x.tvSlot.text = getString(R.string.slot, slot[1]).uppercase(Locale.getDefault())

        x.deviceName.text = "${Build.MODEL} ($device)"
        val props = Device.getVars(device)
        guidelink = props.guideLink
        grouplink = props.groupLink
        x.DeviceImage.setImageResource(props.image)
        x.tvPanel.visibility = props.panel
        n.dbkp.visibility = props.dbkp
        n.flashUefi.visibility = if (props.dbkp == View.VISIBLE) View.GONE else View.VISIBLE
        unsupported = props.unsupported
        tablet= isTablet()
        onConfigurationChanged(resources.configuration)
        //if (!tablet) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        if (unsupported && !Pref.getAGREE(this)) {
            Dlg.show(this, R.string.unsupported)
            Dlg.setYes(R.string.sure) {
                Pref.setAGREE(this, true)
                Dlg.close()
            }
        }
        var panel = rootCommand("cat /proc/cmdline")
        panel = when {
			panel.contains("tianmamd_dv2") -> "Tianma DV2"

            panel.contains("tianmamd_pp1") -> "Tianma PP1"

            panel.contains("tianmamd_pv") -> "Tianma PV"
			
            panel.contains("j20s_42")
                    || panel.contains("k82_42")
					|| panel.contains("k9d_42")
                    || panel.contains("huaxing") -> "Huaxing"

            panel.contains("j20s_36")
                    || panel.contains("tianma")
					|| panel.contains("k9d_36")
                    || panel.contains("k82_36") -> "Tianma"

            panel.contains("ebbg") -> "EBBG"

            panel.contains("samsung")
                    || panel.contains("ea8076_f1mp")
                    || panel.contains("ea8076_f1p2")
                    || panel.contains("ea8076_global")
                    || panel.contains("S6E3FC3")
                    || panel.contains("AMS646YD01") -> "Samsung"

            else -> rootCommand("cat /proc/cmdline | tr ' :=' '\n'|grep dsi|tr ' _' '\n'|tail -3|head -1 ")
        }
        if (!Pref.getAGREE(this) && (panel.contains("f1p2_2") || panel.contains("f1_cmd"))) {
            Dlg.show(this, R.string.upanel)
            Dlg.setYes(R.string.chat) {
                openLink(grouplink)
                Pref.setAGREE(this, true)
                Dlg.close()
            }
            Dlg.setDismiss(R.string.nah) {
                Pref.setAGREE(this, true)
                Dlg.close()
            }
            Dlg.setNo(R.string.later) { Dlg.close() }
        }
        arrayOf(Pair.create(x.tvRamvalue, getString(R.string.ramvalue, RAM().getMemory(this).toDouble())), Pair.create(x.tvPanel, getString(R.string.paneltype, panel))).forEach {
            it.first.text =
                it.second
        }
        arrayOf(Pair.create(x.guide, guidelink), Pair.create(x.group, grouplink)).forEach {
            it.first.setOnClickListener { _: View? ->
                openLink(
                    it.second
                )
            }
        }

        checkupdate()

        if (!BuildConfig.DEBUG) {
            checkdbkpmodel()
            k.codename.visibility = View.GONE
        }

        x.backup.setOnClickListener { _: View? ->
            Dlg.show(this, R.string.backup_boot_question, R.drawable.ic_disk)
            Dlg.setDismiss(R.string.no) { Dlg.close() }
            Dlg.setNo(R.string.android) {
                Dlg.dialogLoading()
                updateLastBackupDate()
                Thread {
                    androidBackup()
					modemBackup()
                    runOnUiThread {
                        Dlg.setText(R.string.backuped)
                        Dlg.dismissButton()
                    }
                }.start()
            }
            Dlg.setYes(R.string.windows) {
                Dlg.dialogLoading()
                updateLastBackupDate()
                Thread {
                    winBackup()
                    runOnUiThread {
                        Dlg.setText(R.string.backuped)
                        Dlg.dismissButton()
                    }
                }.start()
            }
        }

        x.mnt.setOnClickListener { _: View? -> mountUI() }

        x.quickBoot.setOnClickListener { _: View? -> quickbootUI() }

        x.toolbox.setOnClickListener { _: View? ->
            views.add(n.root)
            x.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out))
            setContentView(n.root)
            n.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in))
            n.toolbarlayout.toolbar.title = getString(R.string.toolbox_title)
            n.toolbarlayout.toolbar.navigationIcon = getDrawable(R.drawable.ic_launcher_foreground)
        }

        n.sta.setOnClickListener { _: View? ->
            Dlg.show(this, R.string.sta_question, R.drawable.android)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                rootCommand("mkdir -p /sdcard/WOAHelper/sta || true")
                arrayOf("sta.exe", "sdd.exe", "sdd.conf", "boot.img_auto-flasher_V1.4.exe").forEach { rootCommand("cp $filesDir/$it /sdcard/WOAHelper/sta/") }
                mount()
                if (!isMounted()) {
                    Dlg.close()
                    mountfail()
                    return@setYes
                }
                rootCommand("mkdir $winpath/sta")
                rootCommand("cp '$filesDir/Switch to Android.lnk' $winpath/Users/Public/Desktop")
                rootCommand("cp $filesDir/sta.exe $winpath/ProgramData/sta/sta.exe")
                rootCommand("cp /sdcard/WOAHelper/sta/* $winpath/sta/")

                Dlg.clearButtons()
                Dlg.setText(R.string.done)
                Dlg.dismissButton()
            }
        }

        n.dumpModem.setOnClickListener { _: View? ->
            Dlg.show(this, R.string.dump_modem_question, R.drawable.ic_modem)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                if (!isMounted()) mount()
                Thread {
                    dump()
                    runOnUiThread {
                        Dlg.setText(R.string.lte)
                        Dlg.dismissButton()
                    }
                }.start()
            }
        }

        n.flashUefi.setOnClickListener { _: View? ->
            Dlg.show(this, R.string.flash_uefi_question, R.drawable.ic_uefi)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                Thread {
                    try {
                        flash(finduefi)
                        runOnUiThread {
                            Dlg.setText(R.string.flash)
                            Dlg.dismissButton()
                        }
                    } catch (error: RuntimeException) {
                        error.printStackTrace()
                    }
                }.start()
            }
        }

        n.dbkp.setOnClickListener { _: View? ->
		val finddetector = rootCommand("find $filesDir -maxdepth 1 -name detector")
			if (finddetector.isEmpty()) {
				if (!isNetworkConnected(this)) {
                	nointernet()
                	return@setOnClickListener
            	} else {
					Thread {
						Dlg.show(this, R.string.please_wait)
						rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/DBKP/detector -O /sdcard/detector")
            			rootCommand("mv /sdcard/detector $filesDir")
						rootCommand("chmod 777 $filesDir/detector")
						Dlg.close()
					}.start()
				}
			}
			androidBackup()
			val dbkpbootimg = rootCommand("./detector /sdcard/WOAHelper/Backups/boot.img")
				if (dbkpbootimg.contains("clean")) {
					Dlg.show(this, getString(R.string.dbkp_question, dbkpmodel), R.drawable.ic_uefi)
            		Dlg.setNo(R.string.no) { Dlg.close() }
            		Dlg.setYes(R.string.yes) {
                		rootCommand(String.format("cp $filesDir/dbkp.%s.bin /sdcard/dbkp/dbkp.bin", if ("nabu" == device) "nabu" else if (arrayOf("guacamole", "OnePlus7Pro", "OnePlus7Pro4G", "hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device)) "hotdog" else if ("cepheus" == device) "cepheus" else null))
                		Dlg.dialogLoading()
                		kernelPatch(
                    		(if ("nabu" == device) getString(R.string.nabu) else if (arrayOf("guacamole", "OnePlus7Pro", "OnePlus7Pro4G", "hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device)) getString(R.string.op7) else if ("cepheus" == device) getString(R.string.cepheus) else null)!!,
                    		(if ("nabu" == device) "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/releases/download/1.0/nabu.fd" else if (arrayOf("guacamole", "OnePlus7Pro", "OnePlus7Pro4G").contains(device)) "https://github.com/n00b69/woa-op7/releases/download/DBKP/guacamole.fd" else if (arrayOf("hotdog", "OnePlus7TPro", "OnePlus7TPro4G")
                           		.contains(device)
                    		) "https://github.com/n00b69/woa-op7/releases/download/DBKP/hotdog.fd" else if ("cepheus" == device) "https://github.com/n00b69/woa-cepheus/releases/download/Files/cepheus.fd" else null)!!
                		)
            		}
                } else if (dbkpbootimg.contains("DBKP")) {
					Dlg.show(this, getString(R.string.dbkp_question2), R.drawable.ic_uefi)
            		Dlg.setNo(R.string.no) { Dlg.close() }
            		Dlg.setYes(R.string.reinstall) {
						rootCommand(String.format("cp $filesDir/dbkp.%s.bin /sdcard/dbkp/dbkp.bin", if ("nabu" == device) "nabu" else if (arrayOf("guacamole", "OnePlus7Pro", "OnePlus7Pro4G", "hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device)) "hotdog" else if ("cepheus" == device) "cepheus" else null))
                		Dlg.dialogLoading()
                		kernelReinstall(
                    		(if ("nabu" == device) getString(R.string.nabu) else if (arrayOf("guacamole", "OnePlus7Pro", "OnePlus7Pro4G", "hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device)) getString(R.string.op7) else if ("cepheus" == device) getString(R.string.cepheus) else null)!!,
                    		(if ("nabu" == device) "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/releases/download/1.0/nabu.fd" else if (arrayOf("guacamole", "OnePlus7Pro", "OnePlus7Pro4G").contains(device)) "https://github.com/n00b69/woa-op7/releases/download/DBKP/guacamole.fd" else if (arrayOf("hotdog", "OnePlus7TPro", "OnePlus7TPro4G")
                           		.contains(device)
                    		) "https://github.com/n00b69/woa-op7/releases/download/DBKP/hotdog.fd" else if ("cepheus" == device) "https://github.com/n00b69/woa-cepheus/releases/download/Files/cepheus.fd" else null)!!
                		)
					}
					Dlg.setDismiss(R.string.uninstall) {
						kernelRemove()
					}
				}	
		}


        n.devcfg.setOnClickListener { _: View? ->
            if (!isNetworkConnected(this)) {
                val finddevcfg = rootCommand("find $filesDir -maxdepth 1 -name OOS11_devcfg_*")
                if (finddevcfg.isEmpty()) {
                    nointernet()
                    return@setOnClickListener
                }
            }
            Dlg.show(this, getString(R.string.devcfg_question, dbkpmodel), R.drawable.ic_uefi)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                Thread {
                    rootCommand("mkdir -p /sdcard/WOAHelper/Backups || true")
                    val devcfgDevice = if (arrayOf("guacamole", "OnePlus7Pro", "OnePlus7Pro4G").contains(device)) "guacamole" else (if (arrayOf("hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device)) "hotdog" else null)!!
                    val findoriginaldevcfg = rootCommand("find $filesDir -maxdepth 1 -name original-devcfg.img")
                    if (findoriginaldevcfg.isEmpty()) {
                        rootCommand("dd bs=8M if=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix) of=/sdcard/WOAHelper/Backups/original-devcfg.img")
                        rootCommand("cp /sdcard/WOAHelper/Backups/original-devcfg.img $filesDir/original-devcfg.img")
                    }
                    val finddevcfg = rootCommand("find $filesDir -maxdepth 1 -name OOS11_devcfg_*")
                    if (finddevcfg.isEmpty()) {
                        rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_$devcfgDevice.img -O /sdcard/WOAHelper/Backups/OOS11_devcfg_$devcfgDevice.img")
                        rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_$devcfgDevice.img -O /sdcard/WOAHelper/Backups/OOS12_devcfg_$devcfgDevice.img")
                        rootCommand("cp /sdcard/WOAHelper/Backups/OOS11_devcfg_$devcfgDevice.img $filesDir")
                        rootCommand("cp /sdcard/WOAHelper/Backups/OOS12_devcfg_$devcfgDevice.img $filesDir")
                        rootCommand("dd bs=8M if=$filesDir/OOS11_devcfg_$devcfgDevice.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)")
                    } else {
                        rootCommand("dd bs=8M if=$filesDir/OOS11_devcfg_$devcfgDevice.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)")
                    }
                    runOnUiThread {
                        rootCommand("mkdir -p /sdcard/WOAHelper/staDevcfg || true")
                        rootCommand("cp $filesDir/sdd.exe /sdcard/WOAHelper/staDevcfg/sdd.exe")
                        rootCommand("cp $filesDir/devcfg-sdd.conf /sdcard/WOAHelper/staDevcfg/sdd.conf")
                        mount()
                        if (!isMounted()) {
                            Dlg.close()
                            mountfail()
                        } else {
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
                }.start()
            }
        }

        n.software.setOnClickListener { _: View? ->
            Dlg.show(this, R.string.software_question, R.drawable.ic_sensor)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                if (!isMounted()) mount()
                if (!isMounted()) {
                    Dlg.close()
                    mountfail()
                    return@setYes
                }
                rootCommand("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                arrayOf("WorksOnWoa.url", "TestedSoftware.url", "ARMSoftware.url", "ARMRepo.url").forEach { rootCommand("cp $filesDir/$it /sdcard/WOAHelper/Toolbox") }
                rootCommand("mkdir $winpath/Toolbox || true ")
                arrayOf("WorksOnWoa.url", "TestedSoftware.url", "ARMSoftware.url", "ARMRepo.url").forEach { rootCommand("cp $filesDir/$it $winpath/Toolbox") }
                Dlg.setText(R.string.done)
                Dlg.dismissButton()
            }
        }

        n.atlasos.setOnClickListener { _: View? ->
            if (!isNetworkConnected(this)) {
                nointernet()
                return@setOnClickListener
            }
            Dlg.show(this, R.string.atlasos_question, R.drawable.ic_ar)
            Dlg.dismissButton()
            Dlg.setNo(R.string.revios) {
                Dlg.dialogLoading()
                Dlg.setBar(0)
                Dlg.setIcon(R.drawable.ic_download)
                Thread {
                    rootCommand("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                    rootCommand("wget https://github.com/n00b69/modified-playbooks/releases/download/ReviOS/ReviPlaybook.apbx -O /sdcard/WOAHelper/Toolbox/ReviPlaybook.apbx")
                    Dlg.setBar(50)
                    rootCommand("wget https://download.ameliorated.io/AME%20Beta.zip -O /sdcard/WOAHelper/Toolbox/AMEWizardBeta.zip")
                    Dlg.setBar(80)
                    runOnUiThread {
                        mount()
                        if (!isMounted()) {
                            Dlg.close()
                            mountfail()
                            return@runOnUiThread
                        }
                        Dlg.setIcon(R.drawable.ic_ar)
                        Dlg.hideBar()
                        rootCommand("mkdir $winpath/Toolbox || true ")
                        rootCommand("cp /sdcard/WOAHelper/Toolbox/ReviPlaybook.apbx $winpath/Toolbox/ReviPlaybook.apbx")
                        rootCommand("cp /sdcard/WOAHelper/Toolbox/AMEWizardBeta.zip $winpath/Toolbox")
                        Dlg.setText(R.string.done)
                        Dlg.dismissButton()
                    }
                }.start()
            }
            Dlg.setYes(R.string.atlasos) {
                Dlg.dialogLoading()
                Dlg.setBar(0)
                Dlg.setIcon(R.drawable.ic_download)
                Thread {
                    rootCommand("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                    rootCommand("wget https://github.com/n00b69/modified-playbooks/releases/download/AtlasOS/AtlasPlaybook.apbx -O /sdcard/WOAHelper/Toolbox/AtlasPlaybook.apbx")
                    Dlg.setBar(50)
                    rootCommand("wget https://download.ameliorated.io/AME%20Beta.zip -O /sdcard/WOAHelper/Toolbox/AMEWizardBeta.zip")
                    Dlg.setBar(80)
                    runOnUiThread {
                        mount()
                        if (!isMounted()) {
                            Dlg.close()
                            mountfail()
                            return@runOnUiThread
                        }
                        Dlg.setIcon(R.drawable.ic_ar)
                        Dlg.hideBar()
                        rootCommand("mkdir $winpath/Toolbox || true ")
                        rootCommand("cp /sdcard/WOAHelper/Toolbox/AtlasPlaybook.apbx $winpath/Toolbox/AtlasPlaybook_v0.5.0.apbx")
                        rootCommand("cp /sdcard/WOAHelper/Toolbox/AMEWizardBeta.zip $winpath/Toolbox")
                        Dlg.setText(R.string.done)
                        Dlg.dismissButton()
                    }
                }.start()
            }
        }

        n.usbhost.setOnClickListener { _: View? ->
            Dlg.show(this, R.string.usbhost_question, R.drawable.ic_mnt)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                rootCommand("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                rootCommand("cp $filesDir/usbhostmode.exe /sdcard/WOAHelper/Toolbox/")
                mount()
                if (!isMounted()) {
                    Dlg.close()
                    mountfail()
                    return@setYes
                }
                rootCommand("mkdir $winpath/Toolbox || true ")
                rootCommand("cp /sdcard/WOAHelper/Toolbox/usbhostmode.exe $winpath/Toolbox")
                rootCommand("cp '$filesDir/USB Host Mode.lnk' $winpath/Users/Public/Desktop")
                Dlg.setText(R.string.done)
                Dlg.dismissButton()
            }
        }

        n.rotation.setOnClickListener { _: View? ->
            Dlg.show(this, R.string.rotation_question, R.drawable.ic_disk)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                rootCommand("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                rootCommand("cp $filesDir/QuickRotate_V6.1.4.exe /sdcard/WOAHelper/Toolbox/")
                mount()
                if (!isMounted()) {
                    Dlg.close()
                    mountfail()
                    return@setYes
                }
                rootCommand("mkdir $winpath/Toolbox || true ")
                arrayOf("/Toolbox", "/Users/Public/Desktop").forEach { rootCommand("cp /sdcard/WOAHelper/Toolbox/QuickRotate_V6.1.4.exe $winpath$it") }
                Dlg.setText(R.string.done)
                Dlg.dismissButton()
            }
        }

        n.tablet.setOnClickListener { _: View? ->
            Dlg.show(this, R.string.tablet_question, R.drawable.ic_sensor)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                rootCommand("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                rootCommand("cp $filesDir/Optimized_Taskbar_Control_V3.1.exe /sdcard/WOAHelper/Toolbox/")
                mount()
                if (!isMounted()) {
                    Dlg.close()
                    mountfail()
                    return@setYes
                }
                rootCommand("mkdir $winpath/Toolbox || true ")
                rootCommand("cp /sdcard/WOAHelper/Toolbox/Optimized_Taskbar_Control_V3.1.exe $winpath/Toolbox")
                Dlg.setText(R.string.done)
                Dlg.dismissButton()
            }
        }

        n.setup.setOnClickListener { _: View? ->
            if (!isNetworkConnected(this)) {
                nointernet()
                return@setOnClickListener
            }
            Dlg.show(this, R.string.setup_question, R.drawable.ic_mnt)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                Dlg.setIcon(R.drawable.ic_download)
                Dlg.setBar(0)
                Thread {
                    rootCommand("mkdir -p /sdcard/WOAHelper/Frameworks || true")
                    rootCommand("cp $filesDir/install.bat /sdcard/WOAHelper/Frameworks/install.bat")
                    arrayOf(
                        "PhysX-9.13.0604-SystemSoftware-Legacy.msi",
                        "PhysX_9.23.1019_SystemSoftware.exe",
                        "xnafx40_redist.msi",
                        "opengl.appx",
                        "2005vcredist_x64.EXE",
                        "2005vcredist_x86.EXE",
                        "2008vcredist_x64.exe",
                        "2008vcredist_x86.exe",
                        "2010vcredist_x64.exe",
                        "2010vcredist_x86.exe",
                        "2012vcredist_x64.exe",
                        "2012vcredist_x86.exe",
                        "2013vcredist_x64.exe",
                        "2013vcredist_x86.exe",
                        "2015VC_redist.x64.exe",
                        "2015VC_redist.x86.exe",
                        "2022VC_redist.arm64.exe",
                        "dxwebsetup.exe",
                        "oalinst.exe"
                    ).forEach {
                        rootCommand("wget https://github.com/n00b69/woasetup/releases/download/Installers/$it -O /sdcard/WOAHelper/Frameworks/$it")
                        Dlg.setBar(Dlg.bar!!.progress + 5)
                    }
                    runOnUiThread {
                        mount()
                        if (!isMounted()) {
                            Dlg.close()
                            mountfail()
                            return@runOnUiThread
                        }
                        rootCommand("mkdir -p $winpath/Toolbox/Frameworks || true ")
                        rootCommand("cp /sdcard/WOAHelper/Frameworks/* $winpath/Toolbox/Frameworks")
                        Dlg.setIcon(R.drawable.ic_mnt)
                        Dlg.hideBar()
                        Dlg.setText(R.string.done)
                        Dlg.dismissButton()
                    }
                }.start()
            }
        }

        n.defender.setOnClickListener { _: View? ->
            Dlg.show(this, R.string.defender_question, R.drawable.edge2)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                if (rootCommand("find $filesDir -maxdepth 1 -name DefenderRemover.exe").isEmpty()) {
                    if (!isNetworkConnected(this)) {
                        Dlg.close()
                        nointernet()
                        return@setYes
                    }
                    rootCommand("wget https://github.com/n00b69/woasetup/releases/download/Installers/DefenderRemover.exe -O /sdcard/WOAHelper/Toolbox/DefenderRemover.exe")
                    rootCommand("cp /sdcard/WOAHelper/Toolbox/DefenderRemover.exe $filesDir/DefenderRemover.exe")
                }
                else
                    rootCommand("cp $filesDir/DefenderRemover.exe /sdcard/WOAHelper/Toolbox/DefenderRemover.exe")
                mount()
                if (!isMounted()) {
                    Dlg.close()
                    mountfail()
                    return@setYes
                }
                rootCommand("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                rootCommand("mkdir $winpath/Toolbox || true ")
                rootCommand("cp $filesDir/RemoveEdge.bat /sdcard/WOAHelper/Toolbox")
                rootCommand("cp $filesDir/DefenderRemover.exe $winpath/Toolbox")
                rootCommand("cp $filesDir/RemoveEdge.bat $winpath/Toolbox")
                Dlg.setText(R.string.done)
                Dlg.dismissButton()
            }
        }

        k.toolbarlayout.toolbar.setTitle(R.string.preferences)
        k.toolbarlayout.toolbar.setNavigationIcon(R.drawable.ic_launcher_foreground)
        val settingsIconClick = View.OnClickListener { _: View? ->
            views.add(k.root)
            views[views.size - 2].startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out))
            setContentView(k.root)
            views[views.size - 1].startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in))
            // Scary big line! (Victoria)
            arrayOf(
                Pair.create(k.backupQB, Pref.getBackup(this)),
                Pair.create(k.backupQBA, Pref.getBackupA(this)),
                Pair.create(k.autobackup, !Pref.getAuto(this)),
                Pair.create(k.autobackupA, !Pref.getAutoA(this)),
                Pair.create(k.confirmation, Pref.getConfirm(this)),
                Pair.create(k.automount, Pref.getAutoMount(this)),
                Pair.create(k.securelock, !Pref.getSecure(this)),
                Pair.create(k.mountLocation, Pref.getMountLocation(this)),
                Pair.create(k.appUpdate, Pref.getAppUpdate(this)),
                Pair.create(k.devcfg1, Pref.getDevcfg1(this) && View.VISIBLE == k.devcfg1.visibility),
                Pair.create(k.devcfg2, Pref.getDevcfg2(this))
            ).forEach { it.first.setChecked(it.second) }
            k.toolbarlayout.settings.visibility = View.GONE
            val langSpinner = findViewById<AppCompatSpinner>(R.id.languages)
            langSpinner.adapter = adapter
            val l = AppCompatDelegate.getApplicationLocales()[0]
            if (null != l) {
                val index = locales.indexOf(l.toLanguageTag().lowercase())
                langSpinner.setSelection(index)
            }
            langSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                    AppCompatDelegate.setApplicationLocales(if (languages[position] == getString(R.string.default1)) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(locales[position]))
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
        arrayOf(x.toolbarlayout.settings, n.toolbarlayout.settings, z.toolbarlayout.settings).forEach { it.setOnClickListener(settingsIconClick) }

        k.mountLocation.setOnChangeListener { b: Boolean ->
            Pref.setMountLocation(this, b)
            updateWinPath()
        }

        k.backupQB.setOnChangeListener { _: Boolean ->
            if (Pref.getBackup(this)) {
                Pref.setBackup(this, false)
                k.autobackup.visibility = View.VISIBLE
                return@setOnChangeListener
            }
            Dlg.show(this, R.string.bwarn)
            Dlg.onCancel { k.backupQB.setChecked(false) }
            Dlg.setDismiss(R.string.cancel) {
                k.backupQB.setChecked(false)
                Dlg.close()
            }
            Dlg.setYes(R.string.agree) {
                Pref.setBackup(this, true)
                k.autobackup.visibility = View.GONE
                Dlg.close()
            }
        }

        k.backupQBA.setOnChangeListener { _: Boolean ->
            if (Pref.getBackupA(this)) {
                Pref.setBackupA(this, false)
                k.autobackupA.visibility = View.VISIBLE
                return@setOnChangeListener
            }
            Dlg.show(this, R.string.bwarn)
            Dlg.onCancel { k.backupQBA.setChecked(false) }
            Dlg.setDismiss(R.string.cancel) {
                k.backupQBA.setChecked(false)
                Dlg.close()
            }
            Dlg.setYes(R.string.agree) {
                Pref.setBackupA(this, true)
                k.autobackupA.visibility = View.GONE
                Dlg.close()
            }
        }

        x.cvInfo.setOnClickListener { _: View? ->
            checkupdate(true)
        }

        k.autobackup.setOnChangeListener { b: Boolean -> Pref.setAuto(this, !b) }
        k.autobackupA.setOnChangeListener { b: Boolean -> Pref.setAutoA(this, !b) }
        k.confirmation.setOnChangeListener { b: Boolean -> Pref.setConfirm(this, b) }
        k.securelock.setOnChangeListener { b: Boolean -> Pref.setSecure(this, !b) }
        k.automount.setOnChangeListener { b: Boolean -> Pref.setAutoMount(this, b) }
        k.appUpdate.setOnChangeListener { b: Boolean -> Pref.setAppUpdate(this, b) }
        val op7funny = rootCommand("cat /proc/cmdline | grep oplus")
        if (arrayOf("guacamole", "guacamolet", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7ProTMO", "hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device) && !op7funny.isEmpty()) {
            k.devcfg1.setOnChangeListener { b: Boolean ->
                Pref.setDevcfg1(this, b)
                k.devcfg2.visibility = if (b) View.VISIBLE else View.GONE
                Pref.setDevcfg2(this, false)
            }
            k.devcfg2.setOnChangeListener { b: Boolean -> Pref.setDevcfg2(this, b) }
            n.devcfg.visibility = View.VISIBLE
        } else {
            arrayOf(k.devcfg1, k.devcfg2).forEach { it.visibility = View.GONE }
            Pref.setDevcfg1(this, false)
            Pref.setDevcfg2(this, false)
        }
    }

    public override fun onResume() {
        super.onResume()
        runSilently { context = this }
        checkwin()
        checkuefi()
        if (true != Shell.isAppGrantedRoot()) {
            Dlg.show(this, R.string.nonroot)
            Dlg.setCancelable(false)
        }
    }

    private fun dump() {
        arrayOf(Pair.create("modemst1", "bootmodem_fs1"), Pair.create("modemst2", "bootmodem_fs2")).forEach { rootCommand(String.format("dd if=/dev/block/by-name/%s of=$(find $winpath/Windows/System32/DriverStore/FileRepository -name qcremotefs8150.inf_arm64_*)/%s", it.first, it.second)) }
    }

    private fun checkdbkpmodel() {
        dbkpmodel = if (arrayOf("guacamole", "guacamolet", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7ProTMO").contains(device)) "ONEPLUS 7 PRO" else if (arrayOf("hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device)) "ONEPLUS 7T PRO" else if ("cepheus" == device) "XIAOMI MI 9" else if ("nabu" == device) "XIAOMI PAD 5" else "UNSUPPORTED"
    }

    private fun checkuefi() {
        rootCommand("mkdir /sdcard/UEFI")
        finduefi = "\"" + rootCommand(getString(R.string.uefiChk)) + "\""
        val found = finduefi.contains("img")
        arrayOf(x.quickBoot, n.flashUefi).forEach { it.isEnabled = found }
        arrayOf(Pair.create(x.quickBoot, if (found) R.string.quickboot_title else R.string.uefi_not_found), Pair.create(n.flashUefi, if (found) R.string.flash_uefi_title else R.string.uefi_not_found)).forEach { it.first.setTitle(it.second) }
        arrayOf(Pair.create(x.quickBoot, if (found) getString(R.string.quickboot_subtitle_nabu) else getString(R.string.uefi_not_found_subtitle, device)), Pair.create(n.flashUefi, if (found) getString(R.string.flash_uefi_subtitle) else getString(R.string.uefi_not_found_subtitle, device))).forEach { it.first.setSubtitle(it.second) }
    }

    private fun checkwin() {
        if (!win.isEmpty() || BuildConfig.DEBUG) return
        Dlg.show(this, R.string.partition)
        Dlg.setCancelable(false)
        Dlg.setYes(R.string.guide) { openLink(guidelink) }
        arrayOf(x.mnt, x.toolbox, x.quickBoot, n.flashUefi).forEach { it.isEnabled = false }
    }

    private fun checkupdate(){
        checkupdate(false)
    }
    private fun checkupdate(manual :  Boolean) {
        if (!isNetworkConnected(this)){
            if (manual) nointernet()
            return
        }
        if (Pref.getAppUpdate(this)&&!manual) return
		if (manual) {
            Dlg.show(this, R.string.please_wait)
		    Dlg.setCancelable(false)
		}
        val version = download.text("https://raw.githubusercontent.com/n00b69/woa-helper-update/main"+ (if (BuildConfig.DEBUG) "/debug" else "")+"/README.md")
        val changelog = download.text("https://raw.githubusercontent.com/n00b69/woa-helper-update/main"+ (if (BuildConfig.DEBUG) "/debug" else "")+"/changelog.md")
        if (version.isEmpty()) {
            if (manual) nointernet()
            return
        }
        if (BuildConfig.VERSION_NAME == version) {
			if (manual) {
            	Dlg.setText(getString(R.string.no) + " " + getString(R.string.update1))
            	Dlg.dismissButton()
			}
            return
        }
        if (!manual) Dlg.show(this,"")
        Dlg.setText(getString(R.string.update1)+": "+version+"\n"+changelog)
        Dlg.setNo(R.string.later) { Dlg.close() }
        Dlg.setYes(R.string.update) {
            Dlg.clearButtons()
			Dlg.setCancelable(false)
            Dlg.setText(
                """
                    ${getString(R.string.update2)}
                    ${getString(R.string.please_wait)}
                    """.trimIndent()
            )
            update()
        }
    }

    private fun update() {
        Thread {
            rootCommand("wget https://raw.githubusercontent.com/n00b69/woa-helper-update/main/woahelper.apk -O $filesDir/woahelper.apk")
            rootCommand("pm install $filesDir/woahelper.apk && rm $filesDir/woahelper.apk")
        }.start()
    }

    private fun mountfail() {
        Dlg.show(
            this, """
     ${getString(R.string.mountfail)}
     ${getString(R.string.internalstorage)}
     """.trimIndent()
        )
        Dlg.dismissButton()
        Dlg.setYes(R.string.chat) { openLink("https://t.me/woahelperchat") }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        var params = arrayOf(LinearLayout.HORIZONTAL, View.GONE, LinearLayout.LayoutParams.MATCH_PARENT, 100,
            LinearLayout.VERTICAL)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
            params = arrayOf(LinearLayout.VERTICAL, View.VISIBLE, LinearLayout.LayoutParams.WRAP_CONTENT, 0,
                LinearLayout.HORIZONTAL)

        x.app.orientation = params[0]
        x.top.orientation = params[4]
        if (tablet) return
        x.DeviceImage.visibility = params[1]
        x.up.layoutParams.height = params[2]
        x.infoText.setPadding(params[3],0,params[3],0)

    }

    private fun kernelPatch(message: String, link: String) {
        Thread(object : Runnable {
            private var message: String? = null
            private var link: String? = null

            fun init(parameter: String?, parameter2: String?): Runnable {
                this.message = parameter
                this.link = parameter2
                return this
            }

            override fun run() {
                androidBackup()
				rootCommand("mkdir /sdcard/dbkp || true")
                rootCommand("dd bs=8M if=/sdcard/WOAHelper/Backups/original-boot.img of=/dev/block/by-name/boot$(getprop ro.boot.slot_suffix)")
                rootCommand("rm /sdcard/WOAHelper/Backups/original-boot.img /sdcard/WOAHelper/Backups/patched-boot.img || true")
                rootCommand("mv /sdcard/WOAHelper/Backups/boot.img /sdcard/dbkp/boot.img")
                rootCommand("cp /sdcard/dbkp/boot.img /sdcard/WOAHelper/Backups/original-boot.img")
                rootCommand("cp $filesDir/dbkp8150.cfg /sdcard/dbkp/dbkp.cfg")
                rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp")
                rootCommand("cp /sdcard/dbkp/dbkp $filesDir")
				rootCommand("chmod 777 $filesDir/dbkp")
                rootCommand("wget $link -O /sdcard/dbkp/file.fd")
                rootCommand("cd /sdcard/dbkp")
                rootCommand("$(find /data/adb -name magiskboot) unpack boot.img")
                rootCommand("$filesDir/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/file.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp.cfg /sdcard/dbkp/dbkp.bin")
                rootCommand("mv output kernel")
                rootCommand("$(find /data/adb -name magiskboot) repack boot.img")
                rootCommand("cp new-boot.img /sdcard/WOAHelper/Backups/patched-boot.img")
                rootCommand("cd $filesDir")
                //rootCommand("rm -r /sdcard/dbkp")
                if ("cepheus" == device) {
                    rootCommand("dd if=/sdcard/WOAHelper/Backups/patched-boot.img of=/dev/block/by-name/boot bs=16M")
                } else {
                    rootCommand("dd if=/sdcard/WOAHelper/Backups/patched-boot.img of=/dev/block/by-name/boot_a bs=16M")
                    rootCommand("dd if=/sdcard/WOAHelper/Backups/patched-boot.img of=/dev/block/by-name/boot_b bs=16M")
                }
                runOnUiThread {
                    Dlg.setText(getString(R.string.dbkp, message))
                    Dlg.setDismiss(R.string.dismiss) { Dlg.close() }
                    Dlg.setNo(R.string.reboot) { rootCommand("/system/bin/svc power reboot") }
                }
            }
        }.init(message, link)).start()
    }
	
	private fun kernelRemove() {
        androidBackup()
		rootCommand("mkdir /sdcard/dbkp || true")
        rootCommand("rm /sdcard/WOAHelper/Backups/unpatched-boot.img || true")
        rootCommand("mv /sdcard/WOAHelper/Backups/boot.img /sdcard/dbkp/boot.img")
		rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/DBKP/remover -O /sdcard/dbkp/remover")
        rootCommand("cp /sdcard/dbkp/remover $filesDir")
        rootCommand("chmod 777 $filesDir/remover")
        rootCommand("cd /sdcard/dbkp")
        rootCommand("$(find /data/adb -name magiskboot) unpack boot.img")
        rootCommand("$filesDir/remover /sdcard/dbkp/kernel /sdcard/dbkp/unpatchedkernel")
		rootCommand("rm kernel")
        rootCommand("mv unpatchedkernel kernel")
        rootCommand("$(find /data/adb -name magiskboot) repack boot.img")
        rootCommand("cp new-boot.img /sdcard/WOAHelper/Backups/unpatched-boot.img")
        rootCommand("cd $filesDir")
        //rootCommand("rm -r /sdcard/dbkp")
        if ("cepheus" == device) {
            rootCommand("dd if=/sdcard/WOAHelper/Backups/unpatched-boot.img of=/dev/block/by-name/boot bs=16M")
        } else {
            rootCommand("dd if=/sdcard/WOAHelper/Backups/unpatched-boot.img of=/dev/block/by-name/boot_a bs=16M")
            rootCommand("dd if=/sdcard/WOAHelper/Backups/unpatched-boot.img of=/dev/block/by-name/boot_b bs=16M")
        }
        runOnUiThread {
            Dlg.setText(getString(R.string.dbkpuninstall))
            Dlg.setDismiss(R.string.reboot) { rootCommand("/system/bin/svc power reboot") }
        }
    }
	
	private fun kernelReinstall(message: String, link: String) {
        Thread(object : Runnable {
            private var message: String? = null
            private var link: String? = null

            fun init(parameter: String?, parameter2: String?): Runnable {
                this.message = parameter
                this.link = parameter2
                return this
            }

            override fun run() {
                androidBackup()
				rootCommand("mkdir /sdcard/dbkp || true")
				rootCommand("rm /sdcard/WOAHelper/Backups/unpatched-boot.img || true")
        		rootCommand("mv /sdcard/WOAHelper/Backups/boot.img /sdcard/dbkp/boot.img")
				rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/DBKP/remover -O /sdcard/dbkp/remover")
        		rootCommand("cp /sdcard/dbkp/remover $filesDir")
        		rootCommand("chmod 777 $filesDir/remover")
        		rootCommand("cd /sdcard/dbkp")
        		rootCommand("$(find /data/adb -name magiskboot) unpack boot.img")
        		rootCommand("$filesDir/remover /sdcard/dbkp/kernel /sdcard/dbkp/unpatchedkernel")
				rootCommand("rm kernel")
        		rootCommand("mv unpatchedkernel kernel")
        		rootCommand("$(find /data/adb -name magiskboot) repack boot.img")
        		rootCommand("cp new-boot.img /sdcard/WOAHelper/Backups/unpatched-boot.img")
                rootCommand("cp $filesDir/dbkp8150.cfg /sdcard/dbkp/dbkp.cfg")
                rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp")
                rootCommand("cp /sdcard/dbkp/dbkp $filesDir")
				rootCommand("chmod 777 $filesDir/dbkp")
                rootCommand("wget $link -O /sdcard/dbkp/file.fd")
                rootCommand("$(find /data/adb -name magiskboot) unpack new-boot.img")
                rootCommand("$filesDir/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/file.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp.cfg /sdcard/dbkp/dbkp.bin")
                rootCommand("mv output kernel")
                rootCommand("$(find /data/adb -name magiskboot) repack new-boot.img")
                rootCommand("cp new-boot.img /sdcard/WOAHelper/Backups/patched-boot.img")
                rootCommand("cd $filesDir")
                //rootCommand("rm -r /sdcard/dbkp")
                if ("cepheus" == device) {
                    rootCommand("dd if=/sdcard/WOAHelper/Backups/patched-boot.img of=/dev/block/by-name/boot bs=16M")
                } else {
                    rootCommand("dd if=/sdcard/WOAHelper/Backups/patched-boot.img of=/dev/block/by-name/boot_a bs=16M")
                    rootCommand("dd if=/sdcard/WOAHelper/Backups/patched-boot.img of=/dev/block/by-name/boot_b bs=16M")
                }
                runOnUiThread {
                    Dlg.setText(getString(R.string.dbkp, message))
                    Dlg.setDismiss(R.string.dismiss) { Dlg.close() }
                    Dlg.setNo(R.string.reboot) { rootCommand("/system/bin/svc power reboot") }
                }
            }
        }.init(message, link)).start()
    }

    companion object {
        private lateinit var x: ActivityMainBinding
        private lateinit var k: SetPanelBinding
        private lateinit var n: ToolboxBinding
        private lateinit var z: ScriptsBinding

        @JvmField
        var context: AppCompatActivity? = null
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
        private lateinit var userShell: Shell

        @JvmStatic
        fun isNetworkConnected(context: Context): Boolean {
            val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            return null != capabilities && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        }

        internal fun flash(uefi: String?) {
            rootCommand("dd if=$uefi of=/dev/block/bootdevice/by-name/boot$(getprop ro.boot.slot_suffix) bs=16M")
        }

        @JvmStatic
        fun mountUI() {
            if (winpath.isEmpty()) updateWinPath()
            Dlg.show(context!!, if (isMounted()) context!!.getString(R.string.unmount_question) else context!!.getString(R.string.mount_question, winpath), R.drawable.ic_mnt)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                Handler(Looper.getMainLooper()).postDelayed({
                    if (BuildConfig.DEBUG) Log.d("debug", winpath)
                    if (isMounted()) {
                        unmount()
                        Dlg.setText(R.string.unmounted)
                        Dlg.dismissButton()
                        return@postDelayed
                    }
                    mount()
                    if (isMounted()) {
                        Dlg.setText("${context!!.getString(R.string.mounted)}\n$winpath")
                        MountWidget.updateText(context!!, context!!.getString(R.string.mnt_title, context!!.getString(R.string.unmountt)))
                        Dlg.dismissButton()
                        return@postDelayed
                    }
                    Dlg.hideIcon()
                    Dlg.setText(R.string.mountfail)
                    Dlg.setYes(R.string.chat) { openLink("https://t.me/woahelperchat") }
                    Dlg.setNo(R.string.dismiss) { Dlg.close() }
                }, 25L)
            }
        }

        @JvmStatic
        fun quickbootUI() {
            if (boot.isEmpty()) boot = getBoot()
            if (winpath.isEmpty()) updateWinPath()
            if (device.isEmpty()) updateDevice()
            Dlg.show(context!!, R.string.quickboot_question, R.drawable.ic_launcher_foreground)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                Handler(Looper.getMainLooper()).postDelayed({
                    mount()
                    var found = rootCommand("ls ${updateWinPath()} | grep boot.img")
                    if (Pref.getBackup(context!!) || (!Pref.getAuto(context!!) && found.isEmpty())) {
                        winBackup()
                        updateLastBackupDate()
                    }
                    found = rootCommand("find /sdcard/WOAHelper/Backups | grep boot.img")
                    if (Pref.getBackupA(context!!) || (!Pref.getAutoA(context!!) && found.isEmpty())) {
                        androidBackup()
                        updateLastBackupDate()
                    }
                    if (Pref.getDevcfg1(context!!)) {
                        if (!isNetworkConnected(context!!)) {
                            val finddevcfg = rootCommand("find ${context!!.filesDir} -maxdepth 1 -name OOS11_devcfg_*")
                            if (finddevcfg.isEmpty()) {
                                nointernet()
                                return@postDelayed
                            }
                        }
                        val devcfgDevice = if (arrayOf("guacamole", "OnePlus7Pro", "OnePlus7Pro4G").contains(device)) "guacamole" else (if (arrayOf("hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device)) "hotdog" else null)!!
                        val findoriginaldevcfg = rootCommand("find ${context!!.filesDir} -maxdepth 1 -name original-devcfg.img")
                        if (findoriginaldevcfg.isEmpty()) {
                            rootCommand("dd bs=8M if=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix) of=/sdcard/original-devcfg.img")
                            rootCommand("cp /sdcard/original-devcfg.img ${context!!.filesDir}/original-devcfg.img")
                        }
                        val finddevcfg = rootCommand("find ${context!!.filesDir} -maxdepth 1 -name OOS11_devcfg_*")
                        if (finddevcfg.isEmpty()) {
                            rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_$devcfgDevice.img -O /sdcard/OOS11_devcfg_$devcfgDevice.img")
                            rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_$devcfgDevice.img -O /sdcard/OOS12_devcfg_$devcfgDevice.img")
                            rootCommand("cp /sdcard/OOS11_devcfg_$devcfgDevice.img ${context!!.filesDir}")
                            rootCommand("cp /sdcard/OOS12_devcfg_$devcfgDevice.img ${context!!.filesDir}")
                            rootCommand("dd bs=8M if=${context!!.filesDir}/OOS11_devcfg_$devcfgDevice.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)")
                        } else {
                            rootCommand("dd bs=8M if=${context!!.filesDir}/OOS11_devcfg_$devcfgDevice.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)")
                        }
                    }
                    if (Pref.getDevcfg2(context!!) && Pref.getDevcfg1(context!!)) {
                        rootCommand("mkdir $winpath/sta || true ")
                        rootCommand("cp '${context!!.filesDir}/Flash Devcfg.lnk' $winpath/Users/Public/Desktop")
                        rootCommand("cp ${context!!.filesDir}/sdd.exe $winpath/sta/sdd.exe")
                        rootCommand("cp ${context!!.filesDir}/devcfg-boot-sdd.conf $winpath/sta/sdd.conf")
                        rootCommand("cp ${context!!.filesDir}/original-devcfg.img $winpath/original-devcfg.img")
                    }
                    flash(finduefi)
                    val findmodem = rootCommand("find /sdcard/WOAHelper/Backups | grep modemst1.img")
					if (findmodem.isEmpty()) {
               			modemBackup()
            		}
                    rootCommand("/system/bin/svc power reboot")
                    Dlg.setText(R.string.wrong)
                    Dlg.dismissButton()
                }, 25L)
            }
        }

        internal fun mount() {
            if (win.isEmpty()) win = getWin()
            if (isMounted()) return
            updateWinPath()
            rootCommand("mkdir $winpath || true")
            rootCommand("cd ${context!!.filesDir}")
            rootCommand("./mount.ntfs $win $winpath", true)
            updateMountText()
        }

        private fun unmount() {
            rootCommand("umount $winpath",true)
            rootCommand("rmdir $winpath")
            updateMountText()
        }

        internal fun winBackup() {
            mount()
            rootCommand("dd bs=8M if=$boot of=$winpath/boot.img")
        }

        internal fun androidBackup() {
		    rootCommand("mkdir -p /sdcard/WOAHelper/Backups || true")
            rootCommand("dd bs=8M if=$boot of=/sdcard/WOAHelper/Backups/boot.img")
        }
		
		internal fun modemBackup() {
            rootCommand("dd bs=8M if=/dev/block/by-name/modemst1 of=/sdcard/WOAHelper/Backups/modemst1.img")
			rootCommand("dd bs=8M if=/dev/block/by-name/modemst2 of=/sdcard/WOAHelper/Backups/modemst2.img")
			rootCommand("dd bs=8M if=/dev/block/by-name/fsc of=/sdcard/WOAHelper/Backups/fsc.img")
			rootCommand("dd bs=8M if=/dev/block/by-name/fsg of=/sdcard/WOAHelper/Backups/fsg.img")
			rootCommand("dd bs=8M if=/dev/block/by-name/ftm of=/sdcard/WOAHelper/Backups/ftm.img")
			rootCommand("dd bs=8M if=/dev/block/by-name/persist of=/sdcard/WOAHelper/Backups/persist.img")
			rootCommand("dd bs=8M if=/dev/block/by-name/efs of=/sdcard/WOAHelper/Backups/efs.img")
        }

        internal fun nointernet() {
            Dlg.show(context!!, R.string.internet)
            Dlg.dismissButton()
        }

        @JvmStatic
        fun showBlur() {
            blur++
            runSilently { arrayOf(x.blur, k.blur, n.blur, z.blur).forEach { it.visibility = View.VISIBLE } }
        }

        @JvmStatic
        fun hideBlur(check: Boolean) {
            if (!check) blur = 1
            blur--
            if (0 < blur) return
            runSilently { arrayOf(x.blur, k.blur, n.blur, z.blur).forEach { it.visibility = View.GONE } }
        }

        @JvmStatic
        fun runSilently(action: Runnable) {
            try {
                action.run()
            } catch (_: RuntimeException) {
            }
        }

        internal fun updateLastBackupDate() {
            val sdf = SimpleDateFormat("dd-MM HH:mm", Locale.US)
            val currentDateAndTime = sdf.format(Date())
            Pref.setDate(context!!, currentDateAndTime)
            x.tvDate.text = context!!.getString(R.string.last, Pref.getDate(context!!))
        }

        internal fun updateMountText() {
            mounted = if (isMounted()) context!!.getString(R.string.unmountt) else context!!.getString(R.string.mountt)
            context!!.runOnUiThread {
                x.mnt.setTitle(String.format(context!!.getString(R.string.mnt_title), mounted))
            }
            MountWidget.updateText(context!!, String.format(context!!.getString(R.string.mnt_title), mounted))
        }

        internal fun getWin(): String {
            val partition = rootCommand("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1")
            return rootCommand("realpath $partition")
        }

        internal fun updateWinPath(): String {
            winpath = if (Pref.getMountLocation(context!!)) "/mnt/Windows" else "${Environment.getExternalStorageDirectory().path}/Windows"
            return winpath
        }

        internal fun updateDevice() {
            rootCommand("pm uninstall id.kuato.woahelper")
            device = Pref.codenameChanger(false, context!!, Build.DEVICE)
        }

        internal fun getBoot(): String {
            val partition = rootCommand("find /dev/block | grep -i \"/boot$(getprop ro.boot.slot_suffix)$\" | head -1")
            Log.d("INFO", partition)
            return rootCommand("realpath $partition")
        }
        fun shellInit(dir : File) {
            if (::rootShell.isInitialized)
                return
            rootShell = Shell.Builder.create().build()
            masterShell = Shell.Builder.create().setFlags(Shell.FLAG_MOUNT_MASTER).build()
            userShell = Shell.Builder.create().setFlags(Shell.FLAG_NON_ROOT_SHELL).build()
            for (i in listOf(rootShell, masterShell,userShell)){
                rootCommand("ASH_STANDALONE=1 $(find /data/adb/ -name busybox) ash", i)
                rootCommand("cd $dir", i)
                Log.d("storage", dir.toString())
            }
        }

        fun rootCommand(command: String, master : Boolean = false, user : Boolean = false) : String{
            return rootCommand(command,if (master) masterShell else if(user) userShell else rootShell)
        }

        fun rootCommand(command: String, shell: Shell): String {

            if (BuildConfig.DEBUG)
                Log.d("debug stdout",command)
            val out=ArrayList<String>()
            val err=ArrayList<String>()
            shell.newJob().add(command).to(out,err).exec()
            if (BuildConfig.DEBUG) {
                if (out.isNotEmpty())
                    Log.d("debug stdout",out.last())
                if (err.isNotEmpty())
                    Log.w("debug stderr",err.toString())
            }
            if (out.isNotEmpty())
                return out.last()
            return ""
        }

        @JvmStatic
        fun isMounted(): Boolean {
            return !rootCommand("mount | grep ${getWin()}").isEmpty()
        }

        internal fun openLink(link: String) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = link.toUri()
            context!!.startActivity(i)
        }
    }
}
