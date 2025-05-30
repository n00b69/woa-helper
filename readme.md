# WOA Helper
<p float="left" >
<img src="Helper-dark.png" width="120" alt="">
<img src="Helper-light.png" width="120" alt="">
</p>

> [!WARNING]
>
> We're not responsible for bricked devices, missing recovery partitions, dead microSD cards, dead cats or dogs, nuclear wars or you getting fired because of you forgetting to boot back into Android for your alarm.

## Project status
This project is in late stages of development. Most features have already been added, while some others are still in development. The code may also be refined in the future, to make everything more seamless.

## Features
| Feature                         | Notes                                                                           | Status |
|---------------------------------|---------------------------------------------------------------------------------|--------|
| Backup Android Boot             | Will be stored in internal storage and/or in Windows                            | ✅     |
| Mount/Unmount Windows partition | Needed to view/modify Windows files from Android                                | ✅     |
| Mount Quick Settings toggle     |                                                                                 | ✅     |
| Mount widget                    | To be added in the future                                                       | ❌     |
| Automount Windows partition     | Optionally automatically mounts Windows when the device starts                  | ✅     |
| Quickboot to Windows            | To use the Quick Boot feature, you need to place the UEFI image in /sdcard/UEFI | ✅     |
| Quickboot Quick Settings toggle |                                                                                 | ✅     |
| Quickboot widget                | To be added in the future                                                       | ❌     |
| StA Creator & Auto boot flasher | Creates dualboot files & tool to automatically flash boot.img                   | ✅     |
| Provisioning Modem              | Only for devices that may need it for LTE to work                               | ✅     |
| DBKP (Dualboot Kernel Patcher)  | Only for supported devices: Oneplus 7(T) Pro 4G, Xiaomi Mi 9 & Xiaomi Pad 5     | ✅     |
| DBKP uninstaller                | To be added in the future. To uninstall manually, flash the boot.img backup     | ❌     |
| Devcfg flasher                  | Only for Oneplus 7(T) Pro 4G, needed on OOS12 to boot Windows                   | ✅     |
| Devcfg Quick Settings toggle    | To be added in the future                                                       | ❌     |
| Devcfg flasher widget           | To be added in the future                                                       | ❌     |
| Edge & Defender Remover         | To remove Microsoft Edge & Windows Defender                                     | ✅     |
| USB host mode toggle            | To switch USB modes; ON for USB, OFF for charging                               | ✅     |
| Display rotation script         | Shortcuts to rotate the screen, especially useful for devices without sensors   | ✅     |
| AtlasOS and ReviOS              | Modified versions to remove the charging requirement                            | ✅     |
| Framework installers            | DirectX, C++ redistributables, XNA framework, OpenAL, OpenGL compatibility pack | ✅     |
| In-app updater                  | If an app update is available, you'll receive an in-app popup (can be disabled) | ✅     |

## Requirements
- Android 7 and up (Only Android 8+ was tested).
- Rooted device with Windows installed (Windows partition must be named **win**, **windows**, **mindows**, **Win**, **Windows**, or **Mindows**).
- UEFI image (To boot Windows).

## Credits
- [KuatoDev](https://github.com/KuatoDev) for making the original app
- [Halal Beef](https://github.com/halal-beef) for updating the original app
- [Bibarub](https://github.com/bibarub) for being involved in the original app and making the USB host mode script and StA
- [Marius586](https://github.com/Marius586) for updating the original app, adding lots of features and refactoring code when needed
- [Victoria Freeman](https://github.com/Victoria-Freeman) for adding lots of features and refactoring code when needed
- [M2K](https://github.com/remtrik) for updating the app and refactoring the entire code
- [the sog](https://github.com/n00b69) for updating Marius' fork and adding lots of features
- [Ali](https://github.com/gixousiyq) for helping refine code
- [Misha803](https://github.com/Misha803) for testing the app

## Supported languages & language credits
- Arabic ([maydoxx1](https://github.com/maydoxx1), [Ali](https://github.com/gixousiyq) & rivas)
- Azerbaijani (Aven1us)
- Belarusian ([Gosha](https://github.com/Xhdsos) & [Victoria Freeman](https://github.com/Victoria-Freeman))
- Chinese ([Chiyuki](https://github.com/chiyuki0325), Sui2786 & TTK)
- Czech ([index986](https://github.com/index986))
- Dutch ([the sog](https://github.com/n00b69))
- English ([the sog](https://github.com/n00b69))
- French (YourAvgEngineer)
- German ([the sog](https://github.com/n00b69))
- Georgian (Nikka)
- Indonesian ([ArToSeVeN](https://github.com/Artoseven))
- Japanese (Hiroshi Takaoka)
- Korean ([galaxysollector](https://github.com/galaxysollector))
- Malay ([ArToSeVeN](https://github.com/Artoseven))
- Moldovan ([Victoria Freeman](https://github.com/Victoria-Freeman))
- Persian (rivas)
- Polish (Win Polish)
- Portuguese ([AdrianoA3](https://github.com/AdrianoA3))
- Romanian ([Graphael](https://github.com/grphks), [Victoria Freeman](https://github.com/Victoria-Freeman) & [David42069](https://github.com/david-42069))
- Russian ([Misha803](https://github.com/Misha803) & [Nikroks](https://github.com/N1kroks)
- Spanish ([carloss15](https://github.com/rodriguezst))
- Thai ([JadeKubPom](https://www.facebook.com/groups/jadekubpomservicethailand/))
- Turkish (sercancamli, [ErdilS](https://github.com/erdilS) & [Kaan Dikeç](https://github.com/dikeckaan))
- Ukrainian ([Ost268](https://github.com/Ost268) & [Ilya114](https://github.com/Ilya114))
- Vietnamese (HieusayHi & bobert10)
