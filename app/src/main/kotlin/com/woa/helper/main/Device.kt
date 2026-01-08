package com.woa.helper.main

import com.woa.helper.R
import android.view.View
data class DeviceConfig(
    val guideLink: String,
    val groupLink: String,
    val image: Int,
    val panel: Int = View.GONE,
    val dbkp: Int = View.GONE,
    val modem: Int = View.GONE,
    val unsupported : Boolean = false
)


object Device {
    fun getVars(codename : String): DeviceConfig{
        return when (codename) {
            "alphalm", "alphaplus", "alpha_lao_com", "alphalm_lao_com", "alphaplus_lao_com" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-alphaplus",
                groupLink = "https://t.me/lgedevices",
                image = R.drawable.alphaplus,
            )

            "betalm", "betalm_lao_com" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-betalm",
                groupLink = "https://t.me/lgedevices",
                image = R.drawable.betalm,
            )

            "flashlmdd", "flash_lao_com", "flashlm", "flashlmdd_lao_com" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-flashlmdd",
                groupLink = "https://t.me/lgedevices",
                image = R.drawable.flashlmdd,
            )

            "mh2lm", "mh2lm_lao_com" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-mh2lm",
                groupLink = "https://t.me/lgedevices",
                image = R.drawable.mh2lm,
                panel = View.VISIBLE,
            )

            "mh2lm5g", "mh2lm5g_lao_com" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-mh2lm5g",
                groupLink = "https://t.me/lgedevices",
                image = R.drawable.mh2lm,
                panel = View.VISIBLE,
            )

            "judyln", "judyp", "judypn" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-everything",
                groupLink = "https://t.me/lgedevices",
                image = R.drawable.unknown,
            )

            "joan" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://t.me/lgedevices",
                image = R.drawable.unknown,
            )

            "andromeda" -> DeviceConfig (
                guideLink = "https://project-aloha.github.io/",
                groupLink = "https://t.me/project_aloha_issues",
                image = R.drawable.unknown,
            )

            "beryllium" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-beryllium",
                groupLink = "https://t.me/WinOnF1",
                image = R.drawable.beryllium,
                panel = View.VISIBLE,
            )

            "bhima", "vayu" -> DeviceConfig (
                guideLink = "https://github.com/WaLoVayu/POCOX3Pro-Windows-Guides",
                groupLink = "https://t.me/WaLoVayu",
                image = R.drawable.vayu,
                panel = View.VISIBLE,
            )

            "cepheus" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-cepheus",
                groupLink = "http://t.me/woahelperchat",
                image = R.drawable.cepheus,
                panel = View.VISIBLE,
                dbkp = View.VISIBLE,
                modem = View.VISIBLE,
            )

            "chiron" -> DeviceConfig (
                guideLink = "https://renegade-project.tech/",
                groupLink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA",
                image = R.drawable.chiron,
            )

            "curtana", "curtana2", "curtana_india", "curtana_cn", "curtanacn", "durandal", "durandal_india", "excalibur", "excalibur2", "excalibur_india", "gram", "joyeuse", "miatoll" -> DeviceConfig (
                guideLink = "https://github.com/woa-miatoll/Miatoll-Guide",
                groupLink = "http://t.me/woamiatoll",
                image = R.drawable.miatoll,
                panel = View.VISIBLE,
            )

            "dipper" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-dipper",
                groupLink = "https://t.me/woadipper",
                image = R.drawable.dipper,
            )

            "equuleus", "ursa" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-equuleus",
                groupLink = "https://t.me/woaequuleus",
                image = R.drawable.equuleus,
            )

            "lisa" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-lisa",
                groupLink = "https://t.me/lisawoa",
                image = R.drawable.lisa,
                panel = View.VISIBLE,
            )

            "nabu" -> DeviceConfig (
                guideLink = "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5",
                groupLink = "https://t.me/nabuwoa",
                image = R.drawable.nabu,
                panel = View.VISIBLE,
                dbkp = View.VISIBLE,
            )

            "perseus" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-perseus",
                groupLink = "https://t.me/woaperseus",
                image = R.drawable.perseus,
            )

            "pipa" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://t.me/xiaomi_pipa",
                image = R.drawable.pipa,
                panel = View.VISIBLE,
                dbkp = View.VISIBLE,

            )

            "polaris" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-polaris",
                groupLink = "https://t.me/WinOnMIX2S",
                image = R.drawable.polaris,
                panel = View.VISIBLE,
            )

            "raphael", "raphaelin", "raphaels" -> DeviceConfig (
                guideLink = "https://github.com/new-WoA-Raphael/woa-raphael",
                groupLink = "https://t.me/woaraphael",
                image = R.drawable.raphael,
                panel = View.VISIBLE,
                modem= View.VISIBLE,
            )

            "surya", "karna" -> DeviceConfig (
                guideLink = "https://github.com/woa-surya/POCOX3NFC-Guides",
                groupLink = "https://t.me/windows_on_pocox3_nfc",
                image = R.drawable.vayu,
                panel = View.VISIBLE,
            )

            "sagit" -> DeviceConfig (
                guideLink = "https://renegade-project.tech/",
                groupLink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA",
                image = R.drawable.unknown,
            )

            "ingres" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://discord.gg/Dx2QgMx7Sv",
                image = R.drawable.ingres,
            )

            "vili", "lavender" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://discord.gg/Dx2QgMx7Sv",
                image = R.drawable.unknown,
            )

            "OnePlus5", "cheeseburger" -> DeviceConfig (
                guideLink = "https://renegade-project.tech/",
                groupLink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA",
                image = R.drawable.cheeseburger,
            )

            "OnePlus5T", "dumpling" -> DeviceConfig (
                guideLink = "https://renegade-project.tech/",
                groupLink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA",
                image = R.drawable.dumpling,
            )

            "OnePlus6", "fajita" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-op6",
                groupLink = "https://t.me/WinOnOP6",
                image = R.drawable.fajita,
            )

            "OnePlus6T", "OnePlus6TSingle", "enchilada" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-op6",
                groupLink = "https://t.me/WinOnOP6",
                image = R.drawable.enchilada,
            )

            "hotdog", "OnePlus7TPro", "OnePlus7TPro4G" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-op7",
                groupLink = "https://t.me/oneplus7woa",
                image = R.drawable.hotdog,
                dbkp = View.VISIBLE,
                modem= View.VISIBLE,
            )

            "guacamole", "guacamolet", "OnePlus7Pro", "OnePlus7Pro4G", "OnePlus7ProTMO" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-op7",
                groupLink = "https://t.me/oneplus7woa",
                image = R.drawable.guacamole,
                dbkp = View.VISIBLE,
                modem= View.VISIBLE,
            )

            "guacamoleb", "hotdogb", "OnePlus7T", "OnePlus7" -> DeviceConfig (
                guideLink = "https://project-aloha.github.io/",
                groupLink = "https://t.me/project_aloha_issues",
                image = R.drawable.unknown,
                modem = View.VISIBLE,
            )

            "OnePlus7TPro5G", "OnePlus7TProNR", "hotdogg" -> DeviceConfig (
                guideLink = "https://project-aloha.github.io/",
                groupLink = "https://t.me/project_aloha_issues",
                image = R.drawable.hotdog,
            )

            "OP7ProNRSpr", "OnePlus7ProNR", "guacamoleg", "guacamoles" -> DeviceConfig (
                guideLink = "https://project-aloha.github.io/",
                groupLink = "https://t.me/project_aloha_issues",
                image = R.drawable.guacamole,
            )

            "a52sxq" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-a52s",
                groupLink = "https://t.me/a52sxq_uefi",
                image = R.drawable.a52sxq,
            )

            "beyond1lte", "beyond1qlte", "beyond1" -> DeviceConfig (
                guideLink = "https://github.com/sonic011gamer/Mu-Samsung",
                groupLink = "https://t.me/woahelperchat",
                image = R.drawable.beyond1,
            )

            "dm3q", "dm3" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://t.me/dumanthecat",
                image = R.drawable.dm3q,
            )

            "e3q" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://t.me/biskupmuf",
                image = R.drawable.e3q,
            )

            "gts6l", "gts6lwifi" -> DeviceConfig (
                guideLink = "https://project-aloha.github.io/",
                groupLink = "https://t.me/project_aloha_issues",
                image = R.drawable.gts6l,
            )

            "q2q" -> DeviceConfig (
                guideLink = "https://project-aloha.github.io/",
                groupLink = "https://t.me/project_aloha_issues",
                image = R.drawable.q2q,
            )

            "star2qlte", "star2qltechn", "r3q" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://discord.gg/Dx2QgMx7Sv",
                image = R.drawable.unknown,
            )

            "winnerx", "winner" -> DeviceConfig (
                guideLink = "https://github.com/n00b69/woa-winner",
                groupLink = "https://t.me/project_aloha_issues",
                image = R.drawable.winner,
            )

            "venus" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://discord.gg/Dx2QgMx7Sv",
                image = R.drawable.venus,
            )

            "alioth" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://discord.gg/Dx2QgMx7Sv",
                image = R.drawable.alioth,
            )

            "davinci" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://t.me/woa_davinci",
                image = R.drawable.raphael,
            )

            "marble" -> DeviceConfig (
                guideLink = "https://github.com/Xhdsos/woa-marble",
                groupLink = "https://t.me/woa_marble",
                image = R.drawable.marble,
            )

            "Pong", "pong" -> DeviceConfig (
                guideLink = "https://github.com/index986/woa-pong",
                groupLink = "https://t.me/WoA_spacewar_pong",
                image = R.drawable.pong,
            )

            "xpeng" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://t.me/woahelperchat",
                image = R.drawable.xpeng,
            )

            "RMX2061" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://discord.gg/Dx2QgMx7Sv",
                image = R.drawable.rmx2061,
            )

            "RMX2170" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://discord.gg/Dx2QgMx7Sv",
                image = R.drawable.rmx2170,
            )

            "cmi" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://t.me/dumanthecat",
                image = R.drawable.cmi,
            )

            "houji" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://t.me/dumanthecat",
                image = R.drawable.houji,
            )

            "meizu20pro", "meizu20Pro" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://t.me/dumanthecat",
                image = R.drawable.meizu20pro,
            )

            "husky" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://t.me/dumanthecat",
                image = R.drawable.husky,
            )

            "redfin", "herolte", "crownlte" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://t.me/dumanthecat",
                image = R.drawable.redfin,
            )

            "haotian" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://t.me/dumanthecat",
                image = R.drawable.haotian,
            )

            "Nord", "nord" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://t.me/dikeckaan",
                image = R.drawable.nord,
            )

            "nx729j", "NX729J", "NX729J-UN" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://t.me/woahelperchat",
                image = R.drawable.nx729j,
            )

            "brepdugl" -> DeviceConfig (
                guideLink = "https://github.com/Project-Silicium/Guides/blob/main/README.md",
                groupLink = "https://discord.gg/Dx2QgMx7Sv",
                image = R.drawable.unknown,
            )

            else -> DeviceConfig(
                guideLink = "https://renegade-project.tech/",
                groupLink = "https://t.me/joinchat/MNjTmBqHIokjweeN0SpoyA",
                image = R.drawable.unknown,
                modem = View.VISIBLE,
                unsupported = true
                )
        }
    }
}