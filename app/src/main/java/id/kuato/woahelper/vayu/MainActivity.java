package id.kuato.woahelper.vayu;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.net.Uri;
import android.content.Intent;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import com.google.android.material.button.MaterialButton;
//import com.itsaky.androidide.logsender.LogSender;
import java.util.Locale;

import id.kuato.woahelper.R;
import id.kuato.woahelper.databinding.ActivityMainBinding;
import id.kuato.woahelper.util.MemoryUtils;
import id.kuato.woahelper.util.RAM;
import id.kuato.woahelper.util.ShellUtils;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding x;
  private Context context;
  int ram;
  int ramvalue;
  String panel;
  String finduefi;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // Remove this line if you don't want AndroidIDE to show this app's logs
    //LogSender.startLogging(this);
    super.onCreate(savedInstanceState);
    // Inflate and get instance of binding
    x = ActivityMainBinding.inflate(getLayoutInflater());
    // set content view to binding's root
    setContentView(x.getRoot());
    Drawable iconToolbar =
        ResourcesCompat.getDrawable(getResources(), R.drawable.ic_launcher_foreground, null);
    setSupportActionBar(x.toolbarlayout.toolbar);
    x.toolbarlayout.toolbar.setTitle(getString(R.string.app_title));
    x.toolbarlayout.toolbar.setSubtitle(getString(R.string.app_subtitle));
    x.toolbarlayout.toolbar.setNavigationIcon(iconToolbar);
    checkdevice();
    checkuefi();
    x.tvMnt.setText(getString(R.string.mnt_title));
    x.tvDumpModem.setText(getString(R.string.dump_modem_title));
    x.tvAppCreator.setText("Marius586 & Vern Kuato & halal-beef @2023");
    x.tvBackupBoot.setText(getString(R.string.backup_boot_title));
    x.tvBackupSubtitle.setText(getString(R.string.backup_boot_subtitle));
    x.tvMntSubtitle.setText(getString(R.string.mnt_subtitle));
    x.tvModemSubtitle.setText(getString(R.string.dump_modem_subtitle));
    x.tvQuickBoot.setText(getString(R.string.quickboot_title));
    x.tvBootSubtitle.setText(getString(R.string.quickboot_subtitle));

    final Dialog dialog = new Dialog(MainActivity.this);
    dialog.setContentView(R.layout.dialog);
    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    MaterialButton yesButton = dialog.findViewById(R.id.yes);
    MaterialButton noButton = dialog.findViewById(R.id.no);
    MaterialButton dismissButton = dialog.findViewById(R.id.dismiss);
    TextView messages = dialog.findViewById(R.id.messages);
    ImageView icons = dialog.findViewById(R.id.icon);

    Drawable uefi = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_uefi, null);
    Drawable boot = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_disk, null);
    Drawable sensors = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sensor, null);
    Drawable modem = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_modem, null);

    Drawable icon =
        ResourcesCompat.getDrawable(getResources(), R.drawable.ic_launcher_foreground, null);
    icon.setColorFilter(
        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            getColor(R.color.colorPrimary), BlendModeCompat.SRC_IN));

    x.tvRamvalue.setText(String.format(getString(R.string.ramvalue), ramvalue));
    x.tvPanel.setText(String.format(getString(R.string.paneltype), panel));

    String uefiname = finduefi;
    String findbackup = ShellUtils.Executer(getString(R.string.findbackup));
    
    if (uefiname.isEmpty()) {
      uefiname = getString(R.string.not_found);
    }
    if (findbackup.isEmpty()) {
      findbackup = getString(R.string.not_found);
      x.tvBackupStatus.setTextColor(R.color.red);
    }


    x.tvBackupStatus.setText(
        String.format(getString(R.string.backup_status), findbackup.replace("/sdcard/", "")));
    x.cvGuide.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            String url = "https://github.com/woa-vayu/Port-Windows-11-Poco-X3-pro";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
          }
        });

    x.cvGroup.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            String url = "https://t.me/winonvayualt";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
          }
        });

    x.cvQuickboot.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
          	ShowBlur();
          	noButton.setText(getString(R.string.no));
            yesButton.setVisibility(View.VISIBLE);
            dismissButton.setVisibility(View.GONE);
            noButton.setVisibility(View.VISIBLE);
            icons.setImageDrawable(icon);
            messages.setText(getString(R.string.quickboot_question));
            yesButton.setText(getString(R.string.yes));
            yesButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    yesButton.setVisibility(View.GONE);
                    noButton.setVisibility(View.GONE);
                    messages.setText(getString(R.string.please_wait));
                    new Handler()
                        .postDelayed(
                            new Runnable() {
                              @Override
                              public void run() {
                                try {
                                  String run =
                                      ShellUtils.Executer(
                                              "su -c if ! [ -a  /mnt/pass_through/0/emulated/0/Windows/Windows ]; then su -c mkdir /mnt/pass_through/0/emulated/0/Windows || true && su -mm -c mount.ntfs /dev/block/by-name/win /mnt/pass_through/0/emulated/0/Windows ; fi "
                                                      + " && su -c dd if=/dev/block/by-name/modemst1 of=/mnt/pass_through/0/emulated/0/Windows/Windows/System32/DriverStore/FileRepository/qcremotefs8150.inf_arm64_04af705613ed2d36/bootmodem_fs1 "
                                                      + " && su -c dd if=/dev/block/by-name/modemst2 of=/mnt/pass_through/0/emulated/0/Windows/Windows/System32/DriverStore/FileRepository/qcremotefs8150.inf_arm64_04af705613ed2d36/bootmodem_fs2 "
                                                      + " && su -c dd if="
                                              + finduefi
                                              + " of=/dev/block/by-name/boot && su -c reboot");
                                  messages.setText("Reboot to windows now...");
                                  dismissButton.setVisibility(View.VISIBLE);
                                } catch (Exception error) {
                                  error.printStackTrace();
                                }
                              }
                            },
                            1000);
                  }
                });
            dismissButton.setText(getString(R.string.dismiss));
            dismissButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    HideBlur();
                    dialog.dismiss();
                  }
                });
                noButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    HideBlur();
                    dialog.dismiss();
                  }
                });
            dialog.setCancelable(false);
            dialog.show();
          }
        });

    x.cvMnt.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
          	noButton.setText(getString(R.string.no));
          	dismissButton.setText(getString(R.string.dismiss));
            yesButton.setVisibility(View.VISIBLE);
            noButton.setVisibility(View.VISIBLE);
            dismissButton.setVisibility(View.GONE);
            ShowBlur();
            icons.setImageDrawable(icon);
            String mnt_question = ShellUtils.Executer("su -c if mount | grep /dev/block/sda33 >/dev/null ; then echo 'Unmount Windows?'; else echo 'Mount Windows?' ; fi");
            messages.setText(mnt_question);
            yesButton.setText(getString(R.string.yes));
            yesButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    yesButton.setVisibility(View.GONE);
                    noButton.setVisibility(View.GONE);
                    messages.setText(getString(R.string.please_wait));
                    new Handler()
                        .postDelayed(
                            new Runnable() {
                              @Override
                              public void run() {
                                try {
                                  String r =
                                      ShellUtils.Executer(
                                          "su -mm -c if [ -a /mnt/pass_through/0/emulated/0/Windows/Windows ]; then su -mm -c umount /dev/block/by-name/win && su -mm -c rmdir /mnt/pass_through/0/emulated/0/Windows; else su -mm -c mkdir /mnt/pass_through/0/emulated/0/Windows || true && su -mm -c umount /dev/block/by-name/win || true && su -mm -c mount.ntfs /dev/block/by-name/win /mnt/pass_through/0/emulated/0/Windows ; fi");
                                  String stat = ShellUtils.Executer("su -c if mount | grep /dev/block/sda33 > /dev/null; then echo Mounted; else echo Unmounted ; fi");
                                  messages.setText(stat);
                                  dismissButton.setVisibility(View.VISIBLE);
                                } catch (Exception error) {
                                  error.printStackTrace();
                                }
                              }
                            },
                            2000);
                  }
                });

            dismissButton.setText(getString(R.string.dismiss));
            dismissButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    HideBlur();
                    dialog.dismiss();
                  }
                });
                noButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    HideBlur();
                    dialog.dismiss();
                  }
                });
            dialog.setCancelable(false);
            dialog.show();
          }
        });

    x.cvDumpModem.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
          	noButton.setText(getString(R.string.no));
          	dismissButton.setText(getString(R.string.dismiss));
           dismissButton.setVisibility(View.GONE);
            yesButton.setVisibility(View.VISIBLE);
            ShowBlur();
            icons.setImageDrawable(modem);
            messages.setText(getString(R.string.dump_modem_question));
            yesButton.setText(getString(R.string.yes));
            yesButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    noButton.setVisibility(View.GONE);
                    yesButton.setVisibility(View.GONE);
                    messages.setText(getString(R.string.please_wait));
                    new Handler()
                        .postDelayed(
                            new Runnable() {
                              @Override
                              public void run() {
                                try {
                                  String run =
                                      ShellUtils.Executer(
                                              "su -c if ! [ -a  /mnt/pass_through/0/emulated/0/Windows/Windows ]; then su -c mkdir /mnt/pass_through/0/emulated/0/Windows || true && su -mm -c mount.ntfs /dev/block/by-name/win /mnt/pass_through/0/emulated/0/Windows ; fi "
                                              + " && su -c dd if=/dev/block/by-name/modemst1 of=/mnt/pass_through/0/emulated/0/Windows/Windows/System32/DriverStore/FileRepository/qcremotefs8150.inf_arm64_04af705613ed2d36/bootmodem_fs1 "
                                              + "&& su -c dd if=/dev/block/by-name/modemst2 of=/mnt/pass_through/0/emulated/0/Windows/Windows/System32/DriverStore/FileRepository/qcremotefs8150.inf_arm64_04af705613ed2d36/bootmodem_fs2 ");
                                  messages.setText(
                                      "Modem Provisioned Successfully...\nEnjoy LTE on Windows :)");
                                  dismissButton.setVisibility(View.VISIBLE);
                                } catch (Exception error) {
                                  error.printStackTrace();
                                }
                              }
                            },
                            2000);
                  }
                });
            dismissButton.setText(getString(R.string.dismiss));
            dismissButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    HideBlur();
                    dialog.dismiss();
                  }
                });
                noButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    HideBlur();
                    dialog.dismiss();
                  }
                });
            dialog.setCancelable(false);
            dialog.show();
          }
        });

    x.cvFlashUefi.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
          	noButton.setText(getString(R.string.no));
          	dismissButton.setText(getString(R.string.dismiss));
            dismissButton.setVisibility(View.GONE);
            ShowBlur();
            icons.setImageDrawable(uefi);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(
                String.format(getString(R.string.flash_uefi_question), panel, ramvalue));
            yesButton.setText(getString(R.string.yes));
            yesButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    noButton.setVisibility(View.GONE);
                    yesButton.setVisibility(View.GONE);
                    messages.setText(getString(R.string.please_wait));
                    new Handler()
                        .postDelayed(
                            new Runnable() {
                              @Override
                              public void run() {
                                try {
                                  String run =
                                      ShellUtils.Executer(
                                          "su -c dd if="
                                              + finduefi
                                              + " of=/dev/block/by-name/boot");
                                  messages.setText(
                                      "UEFI for the "
                                          + panel
                                          + " panel "
                                          + " variant successfully flashed to boot partition.\nNext reboot will boot into Windows.");
                                  dismissButton.setVisibility(View.VISIBLE);
                                } catch (Exception error) {
                                  error.printStackTrace();
                                }
                              }
                            },
                            2000);
                  }
                });
            dismissButton.setText(getString(R.string.dismiss));
            dismissButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    HideBlur();
                    dialog.dismiss();
                  }
                });
                noButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    HideBlur();
                    dialog.dismiss();
                  }
                });
            dialog.setCancelable(false);
            dialog.show();
          }
        });

    x.cvBackupBoot.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
          	noButton.setVisibility(View.VISIBLE);
          dismissButton.setVisibility(View.GONE);
          	noButton.setText(getString(R.string.no));
            dismissButton.setText(getString(R.string.dismiss));
            ShowBlur();
            icons.setImageDrawable(boot);
            yesButton.setVisibility(View.VISIBLE);
            messages.setText(getString(R.string.backup_boot_question));
            yesButton.setText(getString(R.string.yes));
            yesButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    noButton.setVisibility(View.GONE);
                    yesButton.setVisibility(View.GONE);
                    messages.setText(getString(R.string.please_wait));
                    new Handler()
                        .postDelayed(
                            new Runnable() {
                              @Override
                              public void run() {
                                try {
                                  String run =
                                      ShellUtils.Executer(
                                          "su -c dd if=/dev/block/by-name/boot of=/sdcard/boot.img");
                                  messages.setText("Backup boot image successful...\n" + run);
                                  dismissButton.setVisibility(View.VISIBLE);
                                } catch (Exception error) {
                                  error.printStackTrace();
                                }
                              }
                            },
                            2000);
                  }
                });
            dismissButton.setText(getString(R.string.dismiss));
            dismissButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    HideBlur();
                    dialog.dismiss();
                  }
                });
                noButton.setOnClickListener(
                new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    HideBlur();
                    dialog.dismiss();
                  }
                });
            dialog.setCancelable(false);
            dialog.show();
          }
        });

    HideBlur();
  }

  public void checkdevice() {
    try {
      String stram =
          MemoryUtils.extractNumberFromString(new RAM().getMemory(getApplicationContext()));
      ram = Integer.parseInt(stram);
      if (ram > 600) {
        ramvalue = 8;
      } else {
        ramvalue = 6;
      }
    } catch (NumberFormatException n) {
      System.out.println(n);
    }

    String run = ShellUtils.Executer("su -c cat /proc/cmdline");
    if (run.isEmpty()) {
    } else if (run.contains("j20s_42")) {
      panel = "Huaxing";
    } else if (run.contains("j20s_36")) {
      panel = "Tianma";
    } else {
      panel = "Unknown";
    }
  }

  public void checkuefi() {

      ShellUtils.Executer("su -c mkdir /sdcard/UEFI"); // Create UEFI Folder if it doesnt exist.
      finduefi =
        ShellUtils.Executer(
            "su -c find /mnt/sdcard/UEFI/ -type f  | grep '.img' | grep  "
                + panel.toLowerCase() );
    if (finduefi.isEmpty()) {
      x.cvFlashUefi.setEnabled(false);
      x.tvFlashUefi.setText(getString(R.string.uefi_not_found));
      x.tvUefiSubtitle.setVisibility(View.VISIBLE);
      x.tvUefiSubtitle.setText(getString(R.string.uefi_not_found_subtitle));
    } else {
      x.tvFlashUefi.setText(getString(R.string.flash_uefi_title));
      x.cvQuickboot.setVisibility(View.VISIBLE);
      x.cvFlashUefi.setEnabled(true);
      x.tvUefiSubtitle.setText(
          String.format(getString(R.string.flash_uefi_subtitle), panel, ramvalue));
      x.tvUefiSubtitle.setVisibility(View.VISIBLE);
    }
  }

  public void ShowBlur() {
    x.blur.setVisibility(View.VISIBLE);
  }

  public void HideBlur() {
    x.blur.setVisibility(View.GONE);
  }
}
