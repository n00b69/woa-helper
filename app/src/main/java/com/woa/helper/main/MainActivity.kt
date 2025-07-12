package com.woa.helper.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.util.Pair
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
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
import com.github.mmin18.widget.RealtimeBlurView
import com.google.android.material.card.MaterialCardView
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import com.woa.helper.BuildConfig
import com.woa.helper.R
import com.woa.helper.databinding.ActivityMainBinding
import com.woa.helper.databinding.ScriptsBinding
import com.woa.helper.databinding.SetPanelBinding
import com.woa.helper.databinding.ToolboxBinding
import com.woa.helper.preference.pref
import com.woa.helper.util.RAM
import com.woa.helper.widgets.MountWidget
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.function.Consumer
import java.util.stream.Stream

@SuppressLint("StaticFieldLeak")
class MainActivity : AppCompatActivity() {
    private var grouplink = "https://t.me/woahelperchat"
    private var guidelink = "https://github.com/n00b69"
    private var unsupported = false
    private var tablet = false
    private val views: MutableList<View> = ArrayList()

    private fun copyAssets() {
        val assetManager = assets
        var files: Array<String>? = null
        try {
            files = assetManager.list("")
        } catch (_: IOException) {
        }
        checkNotNull(files)
        for (filename in files) {
            val `in`: InputStream
            val out: OutputStream?
            try {
                `in` = assetManager.open(filename)
                val outDir = filesDir.toString()
                val outFile = File(outDir, filename)
                out = FileOutputStream(outFile)
                val buffer = ByteArray(1024)
                var read: Int
                while (-1 != (`in`.read(buffer).also { read = it })) out.write(buffer, 0, read)
                `in`.close()
                out.flush()
                out.close()
            } catch (_: FileNotFoundException) {
//                throw new RuntimeException(e);
            } catch (_: IOException) {
            }
        }
        listOf("mount.ntfs", "libfuse-lite.so", "libntfs-3g.so").forEach(Consumer { v: String? -> ShellUtils.fastCmd(String.format("chmod 777 %s/%s", filesDir, v)) })
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        this.enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this) {
            if (0 == views.size - 1) {
                finish()
            }
            views[views.size - 1].startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_back_out))
            views.removeAt(views.size - 1)
            setContentView(views[views.size - 1])
            views[views.size - 1].startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_back_in))
        }

        copyAssets()
        x = ActivityMainBinding.inflate(layoutInflater)
        k = SetPanelBinding.inflate(layoutInflater)
        n = ToolboxBinding.inflate(layoutInflater)
        z = ScriptsBinding.inflate(layoutInflater)

        context = this

        setContentView(x!!.root)

        views.clear()
        views.add(x!!.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v: View?, insets: WindowInsetsCompat ->
            val sysInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            x!!.tvAppCreator.setPadding(0, 0, 0, sysInsets.bottom)
            listOf(x!!.app, n!!.app, k!!.app, z!!.app).forEach(Consumer { a: ViewGroup -> a.setPadding(0, 0, 0, sysInsets.bottom) })
            listOf(x!!.linearLayout, n!!.linearLayout, k!!.linearLayout, z!!.linearLayout).forEach(Consumer { a: LinearLayout -> a.setPadding(sysInsets.left, sysInsets.top, sysInsets.right, 0) })
            insets
        }

        val languages: MutableList<String> = ArrayList()
        val locales: MutableList<String> = ArrayList()
        locales.add("und")
        languages.add("System Default")
        for (i in BuildConfig.LOCALES) {
            locales.add(i!!.lowercase(Locale.getDefault()))
            val locale = checkNotNull(LocaleListCompat.forLanguageTags(i)[0])
            val country = locale.getDisplayCountry(locale)
            val c = !country.isEmpty()
            val lang = locale.getDisplayLanguage(locale) + (if (c) " (" else "") + country + (if (c) ")" else "")
            languages.add(lang)
        }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            languages
        )
        setSupportActionBar(x!!.toolbarlayout.toolbar)
        x!!.toolbarlayout.toolbar.setTitle(R.string.app_name)
        x!!.toolbarlayout.toolbar.subtitle = "v" + BuildConfig.VERSION_NAME
        x!!.toolbarlayout.toolbar.setNavigationIcon(R.drawable.ic_launcher_foreground)
        listOf(x!!.toolbarlayout.settings, k!!.toolbarlayout.settings, n!!.toolbarlayout.settings, z!!.toolbarlayout.settings).forEach(Consumer { v: ImageButton -> v.setColorFilter(R.color.md_theme_primary) })

        model = ShellUtils.fastCmd("getprop ro.product.model")
        win = getWin()
        boot = getBoot()
        updateDevice()
        updateWinPath()
        updateMountText()
        x!!.tvDate.text = String.format(getString(R.string.last), pref.getDATE(this))

        val slot = ShellUtils.fastCmd("getprop ro.boot.slot_suffix")
        if (slot.isEmpty()) x!!.tvSlot.visibility = View.GONE
        else x!!.tvSlot.text = getString(R.string.slot, slot.substring(1, 2)).uppercase(Locale.getDefault())

        x!!.deviceName.text = String.format("%s (%s)", model, device)
        when (device) {
            "alphalm", "alphaplus", "alpha_lao_com", "alphalm_lao_com", "alphaplus_lao_com" -> {
                guidelink = "https://github.com/n00b69/woa-alphaplus"
                grouplink = "https://t.me/lgedevices"
                x!!.DeviceImage.setImageResource(R.drawable.alphaplus)
            }

            "betalm", "betalm_lao_com" -> {
                guidelink = "https://github.com/n00b69/woa-betalm"
                grouplink = "https://t.me/lgedevices"
                x!!.DeviceImage.setImageResource(R.drawable.betalm)
            }

            "flashlmdd", "flash_lao_com", "flashlm", "flashlmdd_lao_com" -> {
                guidelink = "https://github.com/n00b69/woa-flashlmdd"
                grouplink = "https://t.me/lgedevices"
                x!!.DeviceImage.setImageResource(R.drawable.flashlmdd)
            }

            "mh2lm", "mh2lm_lao_com" -> {
                guidelink = "https://github.com/n00b69/woa-mh2lm"
                grouplink = "https://t.me/lgedevices"
                x!!.DeviceImage.setImageResource(R.drawable.mh2lm)
            }

            "mh2lm5g", "mh2lm5g_lao_com" -> {
                guidelink = "https://github.com/n00b69/woa-mh2lm5g"
                grouplink = "https://t.me/lgedevices"
                x!!.DeviceImage.setImageResource(R.drawable.mh2lm)
            }

            "judyln", "judyp", "judypn" -> {
                guidelink = "https://github.com/n00b69/woa-everything"
                grouplink = "https://t.me/lgedevices"
                x!!.DeviceImage.setImageResource(R.drawable.unknown)
            }

            "joan" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://t.me/lgedevices"
                x!!.DeviceImage.setImageResource(R.drawable.unknown)
            }

            "andromeda" -> {
                guidelink = "https://project-aloha.github.io/"
                grouplink = "https://t.me/project_aloha_issues"
                x!!.DeviceImage.setImageResource(R.drawable.unknown)
            }

            "beryllium" -> {
                guidelink = "https://github.com/n00b69/woa-beryllium"
                grouplink = "https://t.me/WinOnF1"
                x!!.DeviceImage.setImageResource(R.drawable.beryllium)
                x!!.tvPanel.visibility = View.VISIBLE
            }

            "bhima", "vayu" -> {
                guidelink = "https://github.com/WaLoVayu/POCOX3Pro-Windows-Guides"
                grouplink = "https://t.me/windowsonvayu"
                x!!.DeviceImage.setImageResource(R.drawable.vayu)
                x!!.tvPanel.visibility = View.VISIBLE
            }

            "cepheus" -> {
                guidelink = "https://github.com/fbernkastel228/Port-Windows-XiaoMI-9"
                grouplink = "http://t.me/woacepheus"
                x!!.DeviceImage.setImageResource(R.drawable.cepheus)
                listOf(
                    Pair.create(x!!.tvPanel, View.VISIBLE), Pair.create(n!!.dbkp, View.VISIBLE), Pair.create(n!!.dumpModem, View.VISIBLE), Pair.create(
                        n!!.flashUefi, View.GONE
                    )
                ).forEach(
                    Consumer { v: Pair<out View, Int> -> v.first.visibility = v.second })
            }

            "chiron" -> {
                guidelink = "https://renegade-project.tech/"
                grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA"
                x!!.DeviceImage.setImageResource(R.drawable.chiron)
            }

            "curtana", "curtana2", "curtana_india", "curtana_cn", "curtanacn", "durandal", "durandal_india", "excalibur", "excalibur2", "excalibur_india", "gram", "joyeuse", "miatoll" -> {
                guidelink = "https://github.com/woa-miatoll/Port-Windows-11-Redmi-Note-9-Pro"
                grouplink = "http://t.me/woamiatoll"
                x!!.DeviceImage.setImageResource(R.drawable.miatoll)
                x!!.tvPanel.visibility = View.VISIBLE
            }

            "dipper" -> {
                guidelink = "https://github.com/n00b69/woa-dipper"
                grouplink = "https://t.me/woadipper"
                x!!.DeviceImage.setImageResource(R.drawable.dipper)
            }

            "equuleus", "ursa" -> {
                guidelink = "https://github.com/n00b69/woa-equuleus"
                grouplink = "https://t.me/woaequuleus"
                x!!.DeviceImage.setImageResource(R.drawable.equuleus)
            }

            "lisa" -> {
                guidelink = "https://github.com/n00b69/woa-lisa"
                grouplink = "https://t.me/lisawoa"
                x!!.DeviceImage.setImageResource(R.drawable.lisa)
            }

            "nabu" -> {
                guidelink = "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5"
                grouplink = "https://t.me/nabuwoa"
                x!!.DeviceImage.setImageResource(R.drawable.nabu)
                listOf(Pair.create(x!!.tvPanel, View.VISIBLE), Pair.create(n!!.dbkp, View.VISIBLE), Pair.create(n!!.flashUefi, View.GONE)).forEach(Consumer { v: Pair<out View, Int> ->
                    v.first.visibility =
                        v.second
                })
                tablet = true
            }

            "perseus" -> {
                guidelink = "https://github.com/n00b69/woa-perseus"
                grouplink = "https://t.me/woaperseus"
                x!!.DeviceImage.setImageResource(R.drawable.perseus)
            }

            "pipa" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://t.me/xiaomi_pipa"
                x!!.DeviceImage.setImageResource(R.drawable.pipa)
                listOf(Pair.create(x!!.tvPanel, View.VISIBLE), Pair.create(n!!.dbkp, View.VISIBLE), Pair.create(n!!.flashUefi, View.GONE)).forEach(Consumer { v: Pair<out View, Int> ->
                    v.first.visibility =
                        v.second
                })
                tablet = true
            }

            "polaris" -> {
                guidelink = "https://github.com/n00b69/woa-polaris"
                grouplink = "https://t.me/WinOnMIX2S"
                x!!.DeviceImage.setImageResource(R.drawable.polaris)
                x!!.tvPanel.visibility = View.VISIBLE
            }

            "raphael", "raphaelin", "raphaels" -> {
                guidelink = "https://github.com/new-WoA-Raphael/woa-raphael"
                grouplink = "https://t.me/woaraphael"
                x!!.DeviceImage.setImageResource(R.drawable.raphael)
                listOf(x!!.tvPanel, n!!.dumpModem).forEach(Consumer { v: View -> v.visibility = View.VISIBLE })
            }

            "surya", "karna" -> {
                guidelink = "https://github.com/woa-surya/POCOX3NFC-Guides"
                grouplink = "https://t.me/windows_on_pocox3_nfc"
                x!!.DeviceImage.setImageResource(R.drawable.vayu)
                x!!.tvPanel.visibility = View.VISIBLE
            }

            "sagit" -> {
                guidelink = "https://renegade-project.tech/"
                grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA"
                x!!.DeviceImage.setImageResource(R.drawable.unknown)
            }

            "ingres" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://discord.gg/Dx2QgMx7Sv"
                x!!.DeviceImage.setImageResource(R.drawable.ingres)
            }

            "vili", "lavender" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://discord.gg/Dx2QgMx7Sv"
                x!!.DeviceImage.setImageResource(R.drawable.unknown)
            }

            "OnePlus5", "cheeseburger" -> {
                guidelink = "https://renegade-project.tech/"
                grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA"
                x!!.DeviceImage.setImageResource(R.drawable.cheeseburger)
            }

            "OnePlus5T", "dumpling" -> {
                guidelink = "https://renegade-project.tech/"
                grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA"
                x!!.DeviceImage.setImageResource(R.drawable.dumpling)
            }

            "OnePlus6", "fajita" -> {
                guidelink = "https://github.com/n00b69/woa-op6"
                grouplink = "https://t.me/WinOnOP6"
                x!!.DeviceImage.setImageResource(R.drawable.fajita)
            }

            "OnePlus6T", "OnePlus6TSingle", "enchilada" -> {
                guidelink = "https://github.com/n00b69/woa-op6"
                grouplink = "https://t.me/WinOnOP6"
                x!!.DeviceImage.setImageResource(R.drawable.enchilada)
            }

            "hotdog", "OnePlus7TPro", "OnePlus7TPro4G" -> {
                guidelink = "https://github.com/n00b69/woa-op7"
                grouplink = "https://t.me/onepluswoachat"
                x!!.DeviceImage.setImageResource(R.drawable.hotdog)
                listOf(Pair.create(n!!.dumpModem, View.VISIBLE), Pair.create(n!!.dbkp, View.VISIBLE), Pair.create(n!!.flashUefi, View.GONE)).forEach(Consumer { v: Pair<Button, Int> ->
                    v.first.visibility =
                        v.second
                })
            }

            "guacamole", "guacamolet", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7ProTMO" -> {
                guidelink = "https://github.com/n00b69/woa-op7"
                grouplink = "https://t.me/onepluswoachat"
                x!!.DeviceImage.setImageResource(R.drawable.guacamole)
                listOf(Pair.create(n!!.dumpModem, View.VISIBLE), Pair.create(n!!.dbkp, View.VISIBLE), Pair.create(n!!.flashUefi, View.GONE)).forEach(Consumer { v: Pair<Button, Int> ->
                    v.first.visibility =
                        v.second
                })
            }

            "guacamoleb", "hotdogb", "OnePlus7T", "OnePlus7" -> {
                guidelink = "https://project-aloha.github.io/"
                grouplink = "https://t.me/onepluswoachat"
                x!!.DeviceImage.setImageResource(R.drawable.unknown)
                n!!.dumpModem.visibility = View.VISIBLE
            }

            "OnePlus7TPro5G", "OnePlus7TProNR", "hotdogg" -> {
                guidelink = "https://project-aloha.github.io/"
                grouplink = "https://t.me/onepluswoachat"
                x!!.DeviceImage.setImageResource(R.drawable.hotdog)
            }

            "OP7ProNRSpr", "OnePlus7ProNR", "guacamoleg", "guacamoles" -> {
                guidelink = "https://project-aloha.github.io/"
                grouplink = "https://t.me/onepluswoachat"
                x!!.DeviceImage.setImageResource(R.drawable.guacamole)
            }

            "a52sxq" -> {
                guidelink = "https://github.com/n00b69/woa-a52s"
                grouplink = "https://t.me/a52sxq_uefi"
                x!!.DeviceImage.setImageResource(R.drawable.a52sxq)
            }

            "beyond1lte", "beyond1qlte", "beyond1" -> {
                guidelink = "https://github.com/sonic011gamer/Mu-Samsung"
                grouplink = "https://t.me/woahelperchat"
                x!!.DeviceImage.setImageResource(R.drawable.beyond1)
            }

            "dm3q", "dm3" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://t.me/dumanthecat"
                x!!.DeviceImage.setImageResource(R.drawable.dm3q)
            }

            "e3q" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://t.me/biskupmuf"
                x!!.DeviceImage.setImageResource(R.drawable.e3q)
            }

            "gts6l", "gts6lwifi" -> {
                guidelink = "https://project-aloha.github.io/"
                grouplink = "https://t.me/project_aloha_issues"
                x!!.DeviceImage.setImageResource(R.drawable.gts6l)
                tablet = true
            }

            "q2q" -> {
                guidelink = "https://project-aloha.github.io/"
                grouplink = "https://t.me/project_aloha_issues"
                x!!.DeviceImage.setImageResource(R.drawable.q2q)
                tablet = true
            }

            "star2qlte", "star2qltechn", "r3q" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://discord.gg/Dx2QgMx7Sv"
                x!!.DeviceImage.setImageResource(R.drawable.unknown)
            }

            "winnerx", "winner" -> {
                guidelink = "https://github.com/n00b69/woa-winner"
                grouplink = "https://t.me/project_aloha_issues"
                x!!.DeviceImage.setImageResource(R.drawable.winner)
                tablet = true
            }

            "venus" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://discord.gg/Dx2QgMx7Sv"
                x!!.DeviceImage.setImageResource(R.drawable.venus)
            }

            "alioth" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://discord.gg/Dx2QgMx7Sv"
                x!!.DeviceImage.setImageResource(R.drawable.alioth)
            }

            "davinci" -> {
                guidelink = "https://github.com/zxcwsurx/woa-davinci"
                grouplink = "https://t.me/woa_davinci"
                x!!.DeviceImage.setImageResource(R.drawable.raphael)
            }

            "marble" -> {
                guidelink = "https://github.com/Xhdsos/woa-marble"
                grouplink = "https://t.me/woa_marble"
                x!!.DeviceImage.setImageResource(R.drawable.marble)
            }

            "Pong", "pong" -> {
                guidelink = "https://github.com/index986/woa-pong"
                grouplink = "https://t.me/WoA_spacewar_pong"
                x!!.DeviceImage.setImageResource(R.drawable.pong)
            }

            "xpeng" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://t.me/woahelperchat"
                x!!.DeviceImage.setImageResource(R.drawable.xpeng)
            }

            "RMX2061" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://t.me/realme6PROwindowsARM64"
                x!!.DeviceImage.setImageResource(R.drawable.rmx2061)
            }

            "RMX2170" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://t.me/realme6PROwindowsARM64"
                x!!.DeviceImage.setImageResource(R.drawable.rmx2170)
            }

            "cmi" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://t.me/dumanthecat"
                x!!.DeviceImage.setImageResource(R.drawable.cmi)
            }

            "houji" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://t.me/dumanthecat"
                x!!.DeviceImage.setImageResource(R.drawable.houji)
            }

            "meizu20pro", "meizu20Pro" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://t.me/dumanthecat"
                x!!.DeviceImage.setImageResource(R.drawable.meizu20pro)
            }

            "husky" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://t.me/dumanthecat"
                x!!.DeviceImage.setImageResource(R.drawable.husky)
            }

            "redfin", "herolte", "crownlte" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://t.me/dumanthecat"
                x!!.DeviceImage.setImageResource(R.drawable.redfin)
            }

            "haotian" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://t.me/dumanthecat"
                x!!.DeviceImage.setImageResource(R.drawable.haotian)
            }

            "Nord", "nord" -> {
                guidelink = "https://github.com/Robotix22/WoA-Guides/blob/main/Mu-Qcom/README.md"
                grouplink = "https://t.me/dikeckaan"
                x!!.DeviceImage.setImageResource(R.drawable.nord)
            }

            "nx729j", "NX729J", "NX729J-UN" -> {
                guidelink = "https://github.com/Project-Silicium/Mu-Silicium"
                grouplink = "https://t.me/woahelperchat"
                x!!.DeviceImage.setImageResource(R.drawable.nx729j)
            }

            "brepdugl" -> {
                guidelink = "https://github.com/Project-Silicium/Mu-Silicium"
                grouplink = "https://discord.gg/Dx2QgMx7Sv"
                x!!.DeviceImage.setImageResource(R.drawable.unknown)
            }

            else -> {
                guidelink = "https://renegade-project.tech/"
                grouplink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA"
                n!!.dumpModem.visibility = View.VISIBLE
                unsupported = true
            }
        }
        onConfigurationChanged(resources.configuration)
        if (!tablet) requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        if (unsupported && !pref.getAGREE(this)) {
            Dlg.show(this, R.string.unsupported)
            Dlg.setYes(R.string.sure) {
                pref.setAGREE(this, true)
                Dlg.close()
            }
        }
        val run = ShellUtils.fastCmd("su -c cat /proc/cmdline")
        panel = if (Stream.of("j20s_42_02_0b", "k82_42", "ft8756_huaxing", "huaxing").anyMatch { s: String? -> run.contains(s!!) }) "Huaxing" else if (Stream.of("j20s_36_02_0a", "k82_36", "nt36675_tianma", "tianma_fhd_nt36672a", "tianma")
                .anyMatch { s: String? -> run.contains(s!!) }
        ) "Tianma" else if (run.contains("ebbg_fhd_ft8719")) "EBBG" else if (run.contains("fhd_ea8076_global")) "global" else if (run.contains("fhd_ea8076_f1mp_cmd")) "f1mp" else if (run.contains("fhd_ea8076_f1p2_cmd")) "f1p2" else if (run.contains("fhd_ea8076_f1p2_2")) "f1p2_2" else if (run.contains("fhd_ea8076_f1_cmd")) "f1" else if (run.contains("fhd_ea8076_cmd")) "ea8076_cmd" else ShellUtils.fastCmd("su -c cat /proc/cmdline | tr ' :=' '\n'|grep dsi|tr ' _' '\n'|tail -3|head -1 ")
        if (!pref.getAGREE(this) && (panel == "f1p2_2" || panel == "f1")) {
            Dlg.show(this, R.string.upanel)
            Dlg.setYes(R.string.chat) {
                openLink(grouplink)
                pref.setAGREE(this, true)
                Dlg.close()
            }
            Dlg.setDismiss(R.string.nah) {
                pref.setAGREE(this, true)
                Dlg.close()
            }
            Dlg.setNo(R.string.later) { Dlg.close() }
        }
        listOf(Pair.create(x!!.tvRamvalue, getString(R.string.ramvalue, RAM().getMemory(this).toDouble())), Pair.create(x!!.tvPanel, getString(R.string.paneltype, panel))).forEach(Consumer { v: Pair<TextView, String> ->
            v.first.text =
                v.second
        })
        listOf(Pair.create(x!!.guide, guidelink), Pair.create(x!!.group, grouplink)).forEach(Consumer { v: Pair<MaterialCardView, String> ->
            v.first.setOnClickListener { a: View? ->
                openLink(
                    v.second
                )
            }
        })

        if (!BuildConfig.DEBUG) {
            checkdbkpmodel()
            checkupdate()
        }

        x!!.backup.setOnClickListener { a: View? ->
            Dlg.show(this, R.string.backup_boot_question, R.drawable.ic_disk)
            Dlg.setDismiss(R.string.no) { Dlg.close() }
            Dlg.setNo(R.string.android) {
                Dlg.dialogLoading()
                updateLastBackupDate()
                Thread {
                    androidBackup()
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

        x!!.mnt.setOnClickListener { a: View? -> mountUI() }

        x!!.quickBoot.setOnClickListener { a: View? -> quickbootUI() }

        x!!.toolbox.setOnClickListener { v: View? ->
            views.add(n!!.root)
            x!!.mainlayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out))
            setContentView(n!!.root)
            n!!.toolboxtab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in))
            n!!.toolbarlayout.toolbar.title = getString(R.string.toolbox_title)
            n!!.toolbarlayout.toolbar.navigationIcon = getDrawable(R.drawable.ic_launcher_foreground)
        }

        n!!.sta.setOnClickListener { a: View? ->
            Dlg.show(this, R.string.sta_question, R.drawable.android)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                ShellUtils.fastCmd("mkdir -p /sdcard/WOAHelper/sta || true")
                listOf("sta.exe", "sdd.exe", "sdd.conf", "boot_img_auto-flasher_V1.2.exe").forEach(Consumer { file: String -> ShellUtils.fastCmd("cp $filesDir/sta.exe /sdcard/WOAHelper/sta/$file") })
                mount()
                if (!isMounted()) {
                    Dlg.close()
                    mountfail()
                    return@setYes
                }
                ShellUtils.fastCmd(String.format("mkdir %s/sta %s/ProgramData/sta", winpath, winpath))
                ShellUtils.fastCmd("cp '$filesDir/Switch to Android.lnk' $winpath/Users/Public/Desktop")
                ShellUtils.fastCmd("cp $filesDir/sta.exe $winpath/ProgramData/sta/sta.exe")
                ShellUtils.fastCmd("cp /sdcard/WOAHelper/sta $winpath")

                Dlg.clearButtons()
                Dlg.setText(R.string.done)
                Dlg.dismissButton()
            }
        }

        n!!.dumpModem.setOnClickListener { a: View? ->
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

        n!!.flashUefi.setOnClickListener { a: View? ->
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

        n!!.dbkp.setOnClickListener { a: View? ->
            if (!isNetworkConnected(this)) {
                nointernet()
                return@setOnClickListener
            }
            Dlg.show(this, getString(R.string.dbkp_question, dbkpmodel), R.drawable.ic_uefi)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(if ("nabu" == device) R.string.nabu else R.string.yes) {
                ShellUtils.fastCmd(String.format("cp %s/dbkp.%s.bin /sdcard/dbkp/dbkp.bin", filesDir, if ("nabu" == device) "nabu" else if (listOf<String?>("guacamole", "OnePlus7Pro", "OnePlus7Pro4G", "hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device)) "hotdog" else if ("cepheus" == device) "cepheus" else null))
                Dlg.dialogLoading()
                kernelPatch(
                    (if ("nabu" == device) getString(R.string.nabu) else if (listOf<String?>("guacamole", "OnePlus7Pro", "OnePlus7Pro4G", "hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device)) getString(R.string.op7) else if ("cepheus" == device) getString(R.string.cepheus) else null)!!,
                    (if ("nabu" == device) "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/releases/download/1.0/nabu.fd" else if (listOf<String?>("guacamole", "OnePlus7Pro", "OnePlus7Pro4G").contains(device)) "https://github.com/n00b69/woa-op7/releases/download/DBKP/guacamole.fd" else if (listOf<String?>("hotdog", "OnePlus7TPro", "OnePlus7TPro4G")
                            .contains(device)
                    ) "https://github.com/n00b69/woa-op7/releases/download/DBKP/hotdog.fd" else if ("cepheus" == device) "https://github.com/n00b69/woa-everything/releases/download/Files/cepheus.fd" else null)!!
                )
            }
            if ("nabu" == device) {
                Dlg.setDismiss(R.string.nabu2) {
                    ShellUtils.fastCmd("cp $filesDir/dbkp.nabu2.bin /sdcard/dbkp/dbkp.bin")
                    Dlg.dialogLoading()
                    kernelPatch(getString(R.string.nabu), "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/releases/download/1.0/nabuVolumebuttons.fd")
                }
            }
        }

        n!!.devcfg.setOnClickListener { a: View? ->
            if (!isNetworkConnected(this)) {
                val finddevcfg = ShellUtils.fastCmd("find $filesDir -maxdepth 1 -name OOS11_devcfg_*")
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
                    ShellUtils.fastCmd("mkdir -p /sdcard/WOAHelper/Backups || true")
                    val devcfgDevice = if (listOf<String?>("guacamole", "OnePlus7Pro", "OnePlus7Pro4G").contains(device)) "guacamole" else (if (listOf<String?>("hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device)) "hotdog" else null)!!
                    val findoriginaldevcfg = ShellUtils.fastCmd("find $filesDir -maxdepth 1 -name original-devcfg.img")
                    if (findoriginaldevcfg.isEmpty()) {
                        ShellUtils.fastCmd("dd bs=8M if=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix) of=/sdcard/WOAHelper/Backups/original-devcfg.img")
                        ShellUtils.fastCmd("cp /sdcard/WOAHelper/Backups/original-devcfg.img $filesDir/original-devcfg.img")
                    }
                    val finddevcfg = ShellUtils.fastCmd("find $filesDir -maxdepth 1 -name OOS11_devcfg_*")
                    if (finddevcfg.isEmpty()) {
                        ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_%s.img -O /sdcard/WOAHelper/Backups/OOS11_devcfg_%s.img\" | su -mm -c sh", devcfgDevice, devcfgDevice))
                        ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_%s.img -O /sdcard/WOAHelper/Backups/OOS12_devcfg_%s.img\" | su -mm -c sh", devcfgDevice, devcfgDevice))
                        ShellUtils.fastCmd(String.format("cp /sdcard/WOAHelper/Backups/OOS11_devcfg_%s.img %s", devcfgDevice, filesDir))
                        ShellUtils.fastCmd(String.format("cp /sdcard/WOAHelper/Backups/OOS12_devcfg_%s.img %s", devcfgDevice, filesDir))
                        ShellUtils.fastCmd(String.format("dd bs=8M if=%s/OOS11_devcfg_%s.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)", filesDir, devcfgDevice))
                    } else {
                        ShellUtils.fastCmd(String.format("dd bs=8M if=%s/OOS11_devcfg_%s.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)", filesDir, devcfgDevice))
                    }
                    runOnUiThread {
                        ShellUtils.fastCmd("mkdir -p /sdcard/WOAHelper/staDevcfg || true")
                        ShellUtils.fastCmd("cp $filesDir/sdd.exe /sdcard/WOAHelper/staDevcfg/sdd.exe")
                        ShellUtils.fastCmd("cp $filesDir/devcfg-sdd.conf /sdcard/WOAHelper/staDevcfg/sdd.conf")
                        mount()
                        val mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep $win")
                        if (mnt_stat.isEmpty()) {
                            Dlg.close()
                            mountfail()
                        } else {
                            ShellUtils.fastCmd("mkdir $winpath/sta || true ")
                            ShellUtils.fastCmd("cp '$filesDir/Flash Devcfg.lnk' $winpath/Users/Public/Desktop")
                            ShellUtils.fastCmd("cp $filesDir/sdd.exe $winpath/sta/sdd.exe")
                            ShellUtils.fastCmd("cp $filesDir/devcfg-sdd.conf $winpath/sta/sdd.conf")
                            ShellUtils.fastCmd("cp /sdcard/WOAHelper/Backups/original-devcfg.img $winpath/original-devcfg.img")
                        }
                        Dlg.setText(R.string.devcfg)
                        Dlg.setDismiss(R.string.dismiss) { Dlg.close() }
                        Dlg.setYes(R.string.reboot) { ShellUtils.fastCmd("su -c svc power reboot") }
                    }
                }.start()
            }
        }

        n!!.software.setOnClickListener { a: View? ->
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
                ShellUtils.fastCmd("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                listOf("WorksOnWoa.url", "TestedSoftware.url", "ARMSoftware.url", "ARMRepo.url").forEach(Consumer { file: String? -> ShellUtils.fastCmd(String.format("cp %s/%s /sdcard/WOAHelper/Toolbox", filesDir, file)) })
                ShellUtils.fastCmd("mkdir $winpath/Toolbox || true ")
                listOf("WorksOnWoa.url", "TestedSoftware.url", "ARMSoftware.url", "ARMRepo.url").forEach(Consumer { file: String? -> ShellUtils.fastCmd(String.format("cp %s/%s %s/Toolbox", filesDir, file, winpath)) })
                Dlg.setText(R.string.done)
                Dlg.dismissButton()
            }
        }

        n!!.atlasos.setOnClickListener { a: View? ->
            if (!isNetworkConnected(this)) {
                nointernet()
                return@setOnClickListener
            }
            Dlg.show(this, R.string.atlasos_question, R.drawable.ic_ar_mainactivity)
            Dlg.dismissButton()
            Dlg.setNo(R.string.revios) {
                Dlg.dialogLoading()
                Dlg.setBar(0)
                Dlg.setIcon(R.drawable.ic_download)
                Thread {
                    ShellUtils.fastCmd("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                    ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/modified-playbooks/releases/download/ReviOS/ReviPlaybook.apbx -O /sdcard/WOAHelper/Toolbox/ReviPlaybook.apbx\" | su -mm -c sh")
                    Dlg.setBar(50)
                    ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://download.ameliorated.io/AME%20Wizard%20Beta.zip -O /sdcard/WOAHelper/Toolbox/AMEWizardBeta.zip\" | su -mm -c sh")
                    Dlg.setBar(80)
                    runOnUiThread {
                        mount()
                        if (!isMounted()) {
                            Dlg.close()
                            mountfail()
                            return@runOnUiThread
                        }
                        Dlg.setIcon(R.drawable.ic_ar_mainactivity)
                        Dlg.hideBar()
                        ShellUtils.fastCmd("mkdir $winpath/Toolbox || true ")
                        ShellUtils.fastCmd("cp /sdcard/WOAHelper/Toolbox/ReviPlaybook.apbx $winpath/Toolbox/ReviPlaybook.apbx")
                        ShellUtils.fastCmd("cp /sdcard/WOAHelper/Toolbox/AMEWizardBeta.zip $winpath/Toolbox")
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
                    ShellUtils.fastCmd("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                    ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/modified-playbooks/releases/download/AtlasOS/AtlasPlaybook.apbx -O /sdcard/WOAHelper/Toolbox/AtlasPlaybook_v0.4.1.apbx\" | su -mm -c sh")
                    Dlg.setBar(35)
                    ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/modified-playbooks/releases/download/AtlasOS/AtlasPlaybook_v0.4.0_23H2Only.apbx -O /sdcard/WOAHelper/Toolbox/AtlasPlaybook_v0.4.0_23H2Only.apbx\" | su -mm -c sh")
                    Dlg.setBar(60)
                    ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget https://download.ameliorated.io/AME%20Wizard%20Beta.zip -O /sdcard/WOAHelper/Toolbox/AMEWizardBeta.zip\" | su -mm -c sh")
                    Dlg.setBar(80)
                    runOnUiThread {
                        mount()
                        if (!isMounted()) {
                            Dlg.close()
                            mountfail()
                            return@runOnUiThread
                        }
                        Dlg.setIcon(R.drawable.ic_ar_mainactivity)
                        Dlg.hideBar()
                        ShellUtils.fastCmd("mkdir $winpath/Toolbox || true ")
                        ShellUtils.fastCmd("cp /sdcard/WOAHelper/Toolbox/AtlasPlaybook.apbx $winpath/Toolbox/AtlasPlaybook_v0.4.1.apbx")
                        ShellUtils.fastCmd("cp /sdcard/WOAHelper/Toolbox/AtlasPlaybook_v0.4.0_23H2Only.apbx $winpath/Toolbox/AtlasPlaybook_v0.4.0_23H2Only.apbx")
                        ShellUtils.fastCmd("cp /sdcard/WOAHelper/Toolbox/AMEWizardBeta.zip $winpath/Toolbox")
                        Dlg.setText(R.string.done)
                        Dlg.dismissButton()
                    }
                }.start()
            }
        }

        n!!.usbhost.setOnClickListener { a: View? ->
            Dlg.show(this, R.string.usbhost_question, R.drawable.ic_mnt)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                ShellUtils.fastCmd("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                ShellUtils.fastCmd("cp $filesDir/usbhostmode.exe /sdcard/WOAHelper/Toolbox/")
                mount()
                if (!isMounted()) {
                    Dlg.close()
                    mountfail()
                    return@setYes
                }
                ShellUtils.fastCmd("mkdir $winpath/Toolbox || true ")
                ShellUtils.fastCmd("cp /sdcard/WOAHelper/Toolbox/usbhostmode.exe $winpath/Toolbox")
                ShellUtils.fastCmd("cp '$filesDir/USB Host Mode.lnk' $winpath/Users/Public/Desktop")
                Dlg.setText(R.string.done)
                Dlg.dismissButton()
            }
        }

        n!!.rotation.setOnClickListener { a: View? ->
            Dlg.show(this, R.string.rotation_question, R.drawable.ic_disk)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                ShellUtils.fastCmd("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                ShellUtils.fastCmd("cp $filesDir/QuickRotate_V3.0.exe /sdcard/WOAHelper/Toolbox/")
                mount()
                if (!isMounted()) {
                    Dlg.close()
                    mountfail()
                    return@setYes
                }
                ShellUtils.fastCmd("mkdir $winpath/Toolbox || true ")
                listOf("/Toolbox", "/Users/Public/Desktop").forEach(Consumer { v: String -> ShellUtils.fastCmd("cp /sdcard/WOAHelper/Toolbox/QuickRotate_V3.0.exe $winpath$v") })
                Dlg.setText(R.string.done)
                Dlg.dismissButton()
            }
        }

        n!!.tablet.setOnClickListener { a: View? ->
            Dlg.show(this, R.string.tablet_question, R.drawable.ic_sensor)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                ShellUtils.fastCmd("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                ShellUtils.fastCmd("cp $filesDir/Optimized_Taskbar_Control_V3.1.exe /sdcard/WOAHelper/Toolbox/")
                mount()
                if (!isMounted()) {
                    Dlg.close()
                    mountfail()
                    return@setYes
                }
                ShellUtils.fastCmd("mkdir $winpath/Toolbox || true ")
                ShellUtils.fastCmd("cp /sdcard/WOAHelper/Toolbox/Optimized_Taskbar_Control_V3.1.exe $winpath/Toolbox")
                Dlg.setText(R.string.done)
                Dlg.dismissButton()
            }
        }

        n!!.setup.setOnClickListener { a: View? ->
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
                    ShellUtils.fastCmd("mkdir -p /sdcard/WOAHelper/Frameworks || true")
                    ShellUtils.fastCmd("cp $filesDir/install.bat /sdcard/WOAHelper/Frameworks/install.bat")
                    listOf(
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
                    ).forEach(
                        Consumer { file: String? ->
                            ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/%s -O /sdcard/WOAHelper/Frameworks/%s\" | su -mm -c sh", file, file))
                            Dlg.setBar(Dlg.bar!!.progress + 5)
                        })
                    runOnUiThread {
                        mount()
                        if (!isMounted()) {
                            Dlg.close()
                            mountfail()
                            return@runOnUiThread
                        }
                        ShellUtils.fastCmd("mkdir -p $winpath/Toolbox/Frameworks || true ")
                        ShellUtils.fastCmd("cp /sdcard/WOAHelper/Frameworks/* $winpath/Toolbox/Frameworks")
                        Dlg.setIcon(R.drawable.ic_mnt)
                        Dlg.hideBar()
                        Dlg.setText(R.string.done)
                        Dlg.dismissButton()
                    }
                }.start()
            }
        }

        n!!.defender.setOnClickListener { a: View? ->
            Dlg.show(this, R.string.defender_question, R.drawable.edge2)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                if (ShellUtils.fastCmd("find $filesDir -maxdepth 1 -name DefenderRemover.exe").isEmpty()) {
                    if (!isNetworkConnected(this)) {
                        Dlg.close()
                        nointernet()
                        return@setYes
                    }
                    ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woasetup/releases/download/Installers/DefenderRemover.exe -O %s/DefenderRemover.exe\" | su -mm -c sh", filesDir))
                }
                mount()
                if (!isMounted()) {
                    Dlg.close()
                    mountfail()
                    return@setYes
                }
                ShellUtils.fastCmd("mkdir -p /sdcard/WOAHelper/Toolbox || true")
                ShellUtils.fastCmd("mkdir $winpath/Toolbox || true ")
                ShellUtils.fastCmd("cp $filesDir/DefenderRemover.exe /sdcard/WOAHelper/Toolbox")
                ShellUtils.fastCmd("cp $filesDir/RemoveEdge.bat /sdcard/WOAHelper/Toolbox")
                ShellUtils.fastCmd("cp $filesDir/DefenderRemover.exe $winpath/Toolbox")
                ShellUtils.fastCmd("cp $filesDir/RemoveEdge.bat $winpath/Toolbox")
                Dlg.setText(R.string.done)
                Dlg.dismissButton()
            }
        }

        k!!.toolbarlayout.toolbar.setTitle(R.string.preferences)
        k!!.toolbarlayout.toolbar.setNavigationIcon(R.drawable.ic_launcher_foreground)
        val settingsIconClick = View.OnClickListener { v: View? ->
            views.add(k!!.root)
            views[views.size - 2].startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out))
            setContentView(k!!.root)
            views[views.size - 1].startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in))
            // Scary big line! (Victoria)
            listOf(
                Pair.create(k!!.backupQB, pref.getBACKUP(this)), Pair.create(k!!.backupQBA, pref.getBACKUP_A(this)), Pair.create(k!!.autobackup, !pref.getAUTO(this)), Pair.create(
                    k!!.autobackupA, !pref.getAUTO(this)
                ), Pair.create(k!!.autobackupA, !pref.getAUTO(this)), Pair.create(k!!.confirmation, pref.getCONFIRM(this)), Pair.create(k!!.automount, pref.getAutoMount(this)), Pair.create(
                    k!!.securelock, !pref.getSecure(this)
                ), Pair.create(k!!.mountLocation, pref.getMountLocation(this)), Pair.create(k!!.appUpdate, pref.getAppUpdate(this)), Pair.create(k!!.devcfg1, pref.getDevcfg1(this) && View.VISIBLE == k!!.devcfg1.visibility), Pair.create(
                    k!!.devcfg2, pref.getDevcfg2(this)
                )
            ).forEach(
                Consumer { a: Pair<SettingsButton, Boolean> -> a.first.setChecked(a.second) })
            k!!.toolbarlayout.settings.visibility = View.GONE
            val langSpinner = findViewById<AppCompatSpinner>(R.id.languages)
            langSpinner.adapter = adapter
            var l = AppCompatDelegate.getApplicationLocales()[0]
            if (null != l) {
                l = Locale(l.toLanguageTag())
                val index = locales.indexOf(l.toString())
                langSpinner.setSelection(index)
            }
            langSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                    AppCompatDelegate.setApplicationLocales(if (languages[position] == "System Default") LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(locales[position]))
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
        listOf(x!!.toolbarlayout.settings, n!!.toolbarlayout.settings, z!!.toolbarlayout.settings).forEach(Consumer { v: ImageButton -> v.setOnClickListener(settingsIconClick) })

        k!!.mountLocation.setOnChangeListener { b: Boolean ->
            pref.setMountLocation(this, b)
            updateWinPath()
        }

        k!!.backupQB.setOnChangeListener { b: Boolean ->
            if (pref.getBACKUP(this)) {
                pref.setBACKUP(this, false)
                k!!.autobackup.visibility = View.VISIBLE
                return@setOnChangeListener
            }
            Dlg.show(this, R.string.bwarn)
            Dlg.onCancel { k!!.backupQB.setChecked(false) }
            Dlg.setDismiss(R.string.cancel) {
                k!!.backupQB.setChecked(false)
                Dlg.close()
            }
            Dlg.setYes(R.string.agree) {
                pref.setBACKUP(this, true)
                k!!.autobackup.visibility = View.GONE
                Dlg.close()
            }
        }

        k!!.backupQBA.setOnChangeListener { b: Boolean ->
            if (pref.getBACKUP_A(this)) {
                pref.setBACKUP_A(this, false)
                k!!.autobackupA.visibility = View.VISIBLE
                return@setOnChangeListener
            }
            Dlg.show(this, R.string.bwarn)
            Dlg.onCancel { k!!.backupQBA.setChecked(false) }
            Dlg.setDismiss(R.string.cancel) {
                k!!.backupQBA.setChecked(false)
                Dlg.close()
            }
            Dlg.setYes(R.string.agree) {
                pref.setBACKUP_A(this, true)
                k!!.autobackupA.visibility = View.GONE
                Dlg.close()
            }
        }

        x!!.cvInfo.setOnClickListener { a: View? ->
            if (BuildConfig.DEBUG) return@setOnClickListener
            if (!isNetworkConnected(this)) {
                nointernet()
                return@setOnClickListener
            }
            Dlg.show(this, R.string.please_wait)
            val version = ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget -q -O - https://raw.githubusercontent.com/n00b69/woa-helper-update/main/README.md\" | su -mm -c sh")
            if (BuildConfig.VERSION_NAME == version) {
                Dlg.setText(getString(R.string.no) + " " + getString(R.string.update1))
                Dlg.dismissButton()
                return@setOnClickListener
            }
            Dlg.setText(R.string.update1)
            Dlg.setNo(R.string.later) { Dlg.close() }
            Dlg.setYes(R.string.update) {
                Dlg.clearButtons()
                Dlg.setText(
                    """
                        ${getString(R.string.update2)}
                        ${getString(R.string.please_wait)}
                        """.trimIndent()
                )
                update()
            }
        }

        k!!.autobackup.setOnChangeListener { b: Boolean -> pref.setAUTO(this, !b) }
        k!!.autobackupA.setOnChangeListener { b: Boolean -> pref.setAUTO_A(this, !b) }
        k!!.confirmation.setOnChangeListener { b: Boolean -> pref.setCONFIRM(this, b) }
        k!!.securelock.setOnChangeListener { b: Boolean -> pref.setSecure(this, !b) }
        k!!.automount.setOnChangeListener { b: Boolean -> pref.setAutoMount(this, b) }
        k!!.appUpdate.setOnChangeListener { b: Boolean -> pref.setAppUpdate(this, b) }
        //String op7funny = ShellUtils.fastCmd("getprop ro.boot.vendor.lge.model.name");
        //if (("guacamole".equals(device) || "guacamolet".equals(device) || "OnePlus7Pro".equals(device) || "OnePlus7Pro4G".equals(device) || "OnePlus7ProTMO".equals(device) || "hotdog".equals(device) || "OnePlus7TPro".equals(device) || "OnePlus7TPro4G".equals(device)) && (op7funny.contains("LM") || op7funny.contains("OPPO"))) {
        val op7funny = ShellUtils.fastCmd("getprop persist.camera.privapp.list")
        if (listOf<String?>("guacamole", "guacamolet", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7ProTMO", "hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device) && op7funny.lowercase(Locale.getDefault()).contains("oppo")) {
            k!!.devcfg1.setOnChangeListener { b: Boolean ->
                pref.setDevcfg1(this, b)
                k!!.devcfg2.visibility = if (b) View.VISIBLE else View.GONE
                pref.setDevcfg2(this, false)
            }
            k!!.devcfg2.setOnChangeListener { b: Boolean -> pref.setDevcfg2(this, b) }
            n!!.devcfg.visibility = View.VISIBLE
        } else {
            listOf(k!!.devcfg1, k!!.devcfg2).forEach(Consumer { v: SettingsButton -> v.visibility = View.GONE })
            pref.setDevcfg1(this, false)
            pref.setDevcfg2(this, false)
        }
    }

    public override fun onResume() {
        super.onResume()
        runSilently { context = this }
        checkwin()
        checkuefi()
        if (java.lang.Boolean.FALSE == Shell.isAppGrantedRoot()) {
            Dlg.show(this, R.string.nonroot)
            Dlg.setCancelable(false)
        }
    }

    private fun dump() {
        listOf(Pair.create("modemst1", "bootmodem_fs1"), Pair.create("modemst2", "bootmodem_fs2")).forEach(Consumer { v: Pair<String, String> -> ShellUtils.fastCmd(String.format("su -mm -c dd if=/dev/block/by-name/%s of=$(find %s/Windows/System32/DriverStore/FileRepository -name qcremotefs8150.inf_arm64_*)/%s", v.first, winpath, v.second)) })
    }

    private fun checkdbkpmodel() {
        dbkpmodel = if (listOf<String?>("guacamole", "guacamolet", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7ProTMO").contains(device)) "ONEPLUS 7 PRO" else if (listOf<String?>("hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device)) "ONEPLUS 7T PRO" else if ("cepheus" == device) "XIAOMI MI 9" else if ("nabu" == device) "XIAOMI PAD 5" else "UNSUPPORTED"
    }

    private fun checkuefi() {
        ShellUtils.fastCmd("su -c mkdir /sdcard/UEFI")
        finduefi = "\"" + ShellUtils.fastCmd(getString(R.string.uefiChk)) + "\""
        val found = finduefi!!.contains("img")
        listOf(x!!.quickBoot, n!!.flashUefi).forEach(Consumer { v: Button -> v.isEnabled = found })
        listOf(Pair.create(x!!.quickBoot, if (found) R.string.quickboot_title else R.string.uefi_not_found), Pair.create(n!!.flashUefi, if (found) R.string.flash_uefi_title else R.string.uefi_not_found)).forEach(Consumer { v: Pair<Button, Int> -> v.first.setTitle(v.second) })
        listOf(Pair.create(x!!.quickBoot, if (found) getString(R.string.quickboot_subtitle_nabu) else getString(R.string.uefi_not_found_subtitle, device)), Pair.create(n!!.flashUefi, if (found) getString(R.string.flash_uefi_subtitle) else getString(R.string.uefi_not_found_subtitle, device))).forEach(
            Consumer { v: Pair<Button, String> -> v.first.setSubtitle(v.second) })
    }

    private fun checkwin() {
        if (!win!!.isEmpty()) return
        Dlg.show(this, R.string.partition)
        Dlg.setCancelable(false)
        listOf(x!!.mnt, x!!.toolbox, x!!.quickBoot, n!!.flashUefi).forEach(Consumer { v: Button -> v.isEnabled = false })
    }

    private fun checkupdate() {
        if (pref.getAppUpdate(this) || !isNetworkConnected(this)) return

        val version = ShellUtils.fastCmd("echo \"$(su -mm -c find /data/adb -name busybox) wget -q -O - https://raw.githubusercontent.com/n00b69/woa-helper-update/main/README.md\" | su -mm -c sh")
        if (BuildConfig.VERSION_NAME == version) return

        Dlg.show(this, R.string.update1)
        Dlg.setNo(R.string.later) { Dlg.close() }
        Dlg.setYes(R.string.update) {
            Dlg.clearButtons()
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
            ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://raw.githubusercontent.com/n00b69/woa-helper-update/main/woahelper.apk -O %s\" | su -mm -c sh", "$filesDir/woahelper.apk"))
            ShellUtils.fastCmd("pm install $filesDir/woahelper.apk && rm $filesDir/woahelper.apk")
        }.start()
    }

    private fun mountfail() {
        Dlg.show(
            this, """
     ${getString(R.string.mountfail)}
     ${getString(R.string.internalstorage)}
     """.trimIndent()
        )
        Dlg.setDismiss(R.string.cancel) { Dlg.close() }
        Dlg.setYes(R.string.chat) { openLink("https://t.me/woahelperchat") }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (!tablet) return
        listOf(x!!.app, x!!.top).forEach(Consumer { v: LinearLayout -> v.orientation = if (Configuration.ORIENTATION_PORTRAIT == newConfig.orientation && tablet) (if (v === x!!.app) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL) else (if (v === x!!.app) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL) })
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
                ShellUtils.fastCmd("mkdir -p /sdcard/dbkp /sdcard/WOAHelper/Backups || true")
                androidBackup()
                ShellUtils.fastCmd("dd bs=8M if=/sdcard/WOAHelper/Backups/original-boot.img of=/dev/block/by-name/boot$(getprop ro.boot.slot_suffix)")
                ShellUtils.fastCmd("rm /sdcard/WOAHelper/Backups/original-boot.img /sdcard/WOAHelper/Backups/patched-boot.img || true")
                ShellUtils.fastCmd("mv /sdcard/boot.img /sdcard/dbkp/boot.img")
                ShellUtils.fastCmd("cp /sdcard/dbkp/boot.img /sdcard/WOAHelper/Backups/original-boot.img")
                ShellUtils.fastCmd("cp $filesDir/dbkp8150.cfg /sdcard/dbkp/dbkp.cfg")
                ShellUtils.fastCmd("$(find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/DBKP/dbkp -O /sdcard/dbkp/dbkp")
                ShellUtils.fastCmd("cp /sdcard/dbkp/dbkp $filesDir")
                ShellUtils.fastCmd("chmod 777 $filesDir/dbkp")
                ShellUtils.fastCmd("$(find /data/adb -name busybox) wget $link -O /sdcard/dbkp/file.fd")
                ShellUtils.fastCmd("cd /sdcard/dbkp")
                ShellUtils.fastCmd("$(find /data/adb -name magiskboot) unpack boot.img")
                ShellUtils.fastCmd("$filesDir/dbkp /sdcard/dbkp/kernel /sdcard/dbkp/file.fd /sdcard/dbkp/output /sdcard/dbkp/dbkp.cfg /sdcard/dbkp/dbkp.bin")
                ShellUtils.fastCmd("mv output kernel")
                ShellUtils.fastCmd("$(su -c find /data/adb -name magiskboot) repack boot.img")
                ShellUtils.fastCmd("cp new-boot.img /sdcard/WOAHelper/Backups/patched-boot.img")
                ShellUtils.fastCmd("rm -r /sdcard/dbkp")
                if ("cepheus" == device) {
                    ShellUtils.fastCmd("dd if=/sdcard/WOAHelper/Backups/patched-boot.img of=/dev/block/by-name/boot bs=16m")
                } else {
                    ShellUtils.fastCmd("dd if=/sdcard/WOAHelper/Backups/patched-boot.img of=/dev/block/by-name/boot_a bs=16m")
                    ShellUtils.fastCmd("dd if=/sdcard/WOAHelper/Backups/patched-boot.img of=/dev/block/by-name/boot_b bs=16m")
                }
                runOnUiThread {
                    Dlg.setText(getString(R.string.dbkp, message))
                    Dlg.setDismiss(R.string.dismiss) { Dlg.close() }
                    Dlg.setNo(R.string.reboot) { ShellUtils.fastCmd("su -c svc power reboot") }
                }
            }
        }.init(message, link)).start()
    }

    companion object {
        private var x: ActivityMainBinding? = null
        private var k: SetPanelBinding? = null
        private var n: ToolboxBinding? = null
        private var z: ScriptsBinding? = null

        @JvmField
        var context: AppCompatActivity? = null
        private var mounted: String? = null
        private var win: String? = null
        private var winpath: String? = null
        private var panel: String? = null
        private var finduefi: String? = null
        private var device: String? = null
        private var model: String? = null
        private var dbkpmodel: String? = null
        private var boot: String? = null
        private var blur = 0

        @JvmStatic
        fun isNetworkConnected(context: Context): Boolean {
            val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            return null != capabilities && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        }

        private fun flash(uefi: String?) {
            ShellUtils.fastCmd("dd if=$uefi of=/dev/block/bootdevice/by-name/boot$(getprop ro.boot.slot_suffix) bs=16m")
        }

        @JvmStatic
        fun mountUI() {
            if (null == winpath) updateWinPath()
            Dlg.show(context!!, if (isMounted()) context!!.getString(R.string.unmount_question) else context!!.getString(R.string.mount_question, winpath), R.drawable.ic_mnt)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                Handler().postDelayed({
                    Log.d("debug", winpath!!)
                    if (isMounted()) {
                        unmount()
                        Dlg.setText(R.string.unmounted)
                        Dlg.dismissButton()
                        return@postDelayed
                    }
                    mount()
                    if (isMounted()) {
                        Dlg.setText(String.format("%s\n%s", context!!.getString(R.string.mounted), winpath))
                        MountWidget.updateText(context!!, context!!.getString(R.string.mnt_title, context!!.getString(R.string.unmountt)))
                        Dlg.dismissButton()
                        return@postDelayed
                    }
                    Dlg.hideIcon()
                    Dlg.setText(R.string.mountfail)
                    Dlg.setYes(R.string.chat) { openLink("https://t.me/woahelperchat") }
                }, 25L)
            }
        }

        @JvmStatic
        fun quickbootUI() {
            if (null == boot) boot = getBoot()
            if (null == winpath) updateWinPath()
            if (null == device) updateDevice()
            Dlg.show(context!!, R.string.quickboot_question, R.drawable.ic_launcher_foreground)
            Dlg.setNo(R.string.no) { Dlg.close() }
            Dlg.setYes(R.string.yes) {
                Dlg.dialogLoading()
                Handler().postDelayed({
                    mount()
                    var found = ShellUtils.fastCmd(String.format("ls %s | grep boot.img", updateWinPath()))
                    if (pref.getBACKUP(context!!) || (!pref.getAUTO(context!!) && found.isEmpty())) {
                        winBackup()
                        updateLastBackupDate()
                    }
                    found = ShellUtils.fastCmd("find /sdcard | grep boot.img")
                    if (pref.getBACKUP_A(context!!) || (!pref.getAUTO_A(context!!) && found.isEmpty())) {
                        androidBackup()
                        updateLastBackupDate()
                    }
                    if (pref.getDevcfg1(context!!)) {
                        if (!isNetworkConnected(context!!)) {
                            val finddevcfg = ShellUtils.fastCmd("find " + context!!.filesDir + " -maxdepth 1 -name OOS11_devcfg_*")
                            if (finddevcfg.isEmpty()) {
                                nointernet()
                                return@postDelayed
                            }
                        }
                        val devcfgDevice = if (listOf<String?>("guacamole", "OnePlus7Pro", "OnePlus7Pro4G").contains(device)) "guacamole" else (if (listOf<String?>("hotdog", "OnePlus7TPro", "OnePlus7TPro4G").contains(device)) "hotdog" else null)!!
                        val findoriginaldevcfg = ShellUtils.fastCmd("find " + context!!.filesDir + " -maxdepth 1 -name original-devcfg.img")
                        if (findoriginaldevcfg.isEmpty()) {
                            ShellUtils.fastCmd("dd bs=8M if=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix) of=/sdcard/original-devcfg.img")
                            ShellUtils.fastCmd("cp /sdcard/original-devcfg.img " + context!!.filesDir + "/original-devcfg.img")
                        }
                        val finddevcfg = ShellUtils.fastCmd("find " + context!!.filesDir + " -maxdepth 1 -name OOS11_devcfg_*")
                        if (finddevcfg.isEmpty()) {
                            ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_%s.img -O /sdcard/OOS11_devcfg_%s.img\" | su -mm -c sh", devcfgDevice, devcfgDevice))
                            ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_%s.img -O /sdcard/OOS12_devcfg_%s.img\" | su -mm -c sh", devcfgDevice, devcfgDevice))
                            ShellUtils.fastCmd(String.format("cp /sdcard/OOS11_devcfg_%s.img %s", devcfgDevice, context!!.filesDir))
                            ShellUtils.fastCmd(String.format("cp /sdcard/OOS12_devcfg_%s.img %s", devcfgDevice, context!!.filesDir))
                            ShellUtils.fastCmd(String.format("dd bs=8M if=%s/OOS11_devcfg_%s.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)", context!!.filesDir, devcfgDevice))
                        } else {
                            ShellUtils.fastCmd(String.format("dd bs=8M if=%s/OOS11_devcfg_%s.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)", context!!.filesDir, devcfgDevice))
                        }
                    }
                    if (pref.getDevcfg2(context!!) && pref.getDevcfg1(context!!)) {
                        ShellUtils.fastCmd("mkdir $winpath/sta || true ")
                        ShellUtils.fastCmd("cp '" + context!!.filesDir + "/Flash Devcfg.lnk' " + winpath + "/Users/Public/Desktop")
                        ShellUtils.fastCmd("cp " + context!!.filesDir + "/sdd.exe " + winpath + "/sta/sdd.exe")
                        ShellUtils.fastCmd("cp " + context!!.filesDir + "/devcfg-boot-sdd.conf " + winpath + "/sta/sdd.conf")
                        ShellUtils.fastCmd("cp " + context!!.filesDir + "/original-devcfg.img " + winpath + "/original-devcfg.img")
                    }
                    flash(finduefi)
                    ShellUtils.fastCmd("su -c svc power reboot")
                    Dlg.setText(R.string.wrong)
                    Dlg.dismissButton()
                }, 25L)
            }
        }

        internal fun mount() {
            if (null == win) win = getWin()
            ShellUtils.fastCmd("mkdir $winpath || true")
            ShellUtils.fastCmd("cd " + context!!.filesDir)
            ShellUtils.fastCmd("su -mm -c ./mount.ntfs $win $winpath")
            if (isMounted()) {
                // Causes some issues idk. Better be here for later
                updateWinPath()
                ShellUtils.fastCmd("mkdir $winpath || true")
                ShellUtils.fastCmd("cd " + context!!.filesDir)
                ShellUtils.fastCmd("su -mm -c ./mount.ntfs $win $winpath")
            }
            updateMountText()
        }

        private fun unmount() {
            ShellUtils.fastCmd("su -mm -c umount $winpath")
            ShellUtils.fastCmd("rmdir $winpath")
            updateMountText()
        }

        private fun winBackup() {
            mount()
            ShellUtils.fastCmd("su -mm -c dd bs=8m if=$boot of=$winpath/boot.img")
        }

        private fun androidBackup() {
            ShellUtils.fastCmd("su -mm -c dd bs=8m if=$boot of=/sdcard/boot.img")
        }

        internal fun nointernet() {
            Dlg.show(context!!, R.string.internet)
            Dlg.dismissButton()
        }

        @JvmStatic
        fun showBlur() {
            blur++
            runSilently { listOf(x!!.blur, k!!.blur, n!!.blur, z!!.blur).forEach(Consumer { v: RealtimeBlurView -> v.visibility = View.VISIBLE }) }
        }

        @JvmStatic
        fun hideBlur(check: Boolean) {
            if (!check) blur = 1
            blur--
            if (0 < blur) return
            runSilently { listOf(x!!.blur, k!!.blur, n!!.blur, z!!.blur).forEach(Consumer { v: RealtimeBlurView -> v.visibility = View.GONE }) }
        }

        @JvmStatic
        fun runSilently(action: Runnable) {
            try {
                action.run()
            } catch (_: RuntimeException) {
            }
        }

        private fun updateLastBackupDate() {
            val sdf = SimpleDateFormat("dd-MM HH:mm", Locale.US)
            val currentDateAndTime = sdf.format(Date())
            pref.setDATE(context!!, currentDateAndTime)
            x!!.tvDate.text = context!!.getString(R.string.last, pref.getDATE(context!!))
        }

        private fun updateMountText() {
            mounted = if (isMounted()) context!!.getString(R.string.unmountt) else context!!.getString(R.string.mountt)
            context!!.runOnUiThread {
                if (null != x) x!!.mnt.setTitle(String.format(context!!.getString(R.string.mnt_title), mounted))
            }
            MountWidget.updateText(context!!, String.format(context!!.getString(R.string.mnt_title), mounted))
        }

        internal fun getWin(): String {
            val partition = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1")
            return ShellUtils.fastCmd("realpath $partition")
        }

        internal fun updateWinPath(): String {
            winpath = if (pref.getMountLocation(context!!)) "/mnt/Windows" else Environment.getExternalStorageDirectory().path + "/Windows"
            return winpath!!
        }

        private fun updateDevice() {
            ShellUtils.fastCmd("pm uninstall id.kuato.woahelper")
            device = ShellUtils.fastCmd("getprop ro.product.device")
        }

        private fun getBoot(): String {
            val partition = ShellUtils.fastCmd("find /dev/block | grep \"boot$(getprop ro.boot.slot_suffix)$\" | grep -E \"(/boot|/BOOT|/boot_a|/boot_b|/BOOT_a|/BOOT_b)$\" | head -1 ")
            Log.d("INFO", partition)
            return ShellUtils.fastCmd("realpath $partition")
        }

        @JvmStatic
        fun isMounted(): Boolean {
            return !ShellUtils.fastCmd("su -mm -c mount | grep " + getWin()).isEmpty()
        }

        private fun openLink(link: String) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = link.toUri()
            context!!.startActivity(i)
        }
    }
}
