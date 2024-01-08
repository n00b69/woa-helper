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

import android.widget.Switch;
import android.widget.Toast;
import android.widget.CompoundButton;

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
    String mounted;
    String finduefi;
    String finduefi1;
    String finduefi2;

    boolean dual;

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
        String mount_stat = ShellUtils.Executer("su -c mount | grep /dev/block/sda33");
        if (mount_stat.isEmpty())
            mounted = "MOUNT";
        else
            mounted = "UNMOUNT";
        x.tvMnt.setText(String.format(getString(R.string.mnt_title), mounted));
        x.tvDumpModem.setText(getString(R.string.dump_modem_title));
        x.tvAppCreator.setText("Marius586 & Vern Kuato & halal-beef @2024");
        x.tvBackup.setText(getString(R.string.backup_boot_title));
        x.tvBackup2.setText(getString(R.string.backup_boot_title2));
        x.tvBackupSub.setText(getString(R.string.backup_boot_subtitle));
        x.tvMntSubtitle.setText(getString(R.string.mnt_subtitle));
        x.tvModemSubtitle.setText(getString(R.string.dump_modem_subtitle));

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

        x.cvQuickBoot.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowBlur();
                        noButton.setText(getString(R.string.no));
                        yesButton.setText(getString(R.string.yes));
                        yesButton.setVisibility(View.VISIBLE);
                        dismissButton.setVisibility(View.GONE);
                        noButton.setVisibility(View.VISIBLE);
                        icons.setImageDrawable(icon);
                        if (dual)
                            messages.setText(getString(R.string.quickboot_question1));
                        else
                            messages.setText(getString(R.string.quickboot_question));
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
                                                                    mount();
                                                                    dump();
                                                                    unmount();
                                                                    if (dual)
                                                                        flash(finduefi1);
                                                                    else
                                                                        flash(finduefi);
                                                                    String run = ShellUtils.Executer("su -c reboot");
                                                                    messages.setText("Reboot to windows now...");
                                                                    dismissButton.setVisibility(View.VISIBLE);
                                                                } catch (Exception error) {
                                                                    error.printStackTrace();
                                                                }
                                                            }
                                                        },
                                                        500);
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

        x.cvQuickBoot2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowBlur();
                        noButton.setText(getString(R.string.no));
                        yesButton.setText(getString(R.string.yes));
                        yesButton.setVisibility(View.VISIBLE);
                        dismissButton.setVisibility(View.GONE);
                        noButton.setVisibility(View.VISIBLE);
                        icons.setImageDrawable(icon);
                        messages.setText(getString(R.string.quickboot_question2));
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
                                                                    mount();
                                                                    dump();
                                                                    unmount();
                                                                    flash(finduefi2);
                                                                    String run = ShellUtils.Executer("su -c reboot");
                                                                    messages.setText("Reboot to windows now...");
                                                                    dismissButton.setVisibility(View.VISIBLE);
                                                                } catch (Exception error) {
                                                                    error.printStackTrace();
                                                                }
                                                            }
                                                        },
                                                        500);
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
                        noButton.setVisibility(View.VISIBLE);
                        yesButton.setText(getString(R.string.yes));
                        dismissButton.setText(getString(R.string.dismiss));
                        yesButton.setVisibility(View.VISIBLE);
                        dismissButton.setVisibility(View.GONE);
                        ShowBlur();
                        icons.setImageDrawable(icon);
                        String mnt_stat = ShellUtils.Executer("su -c mount | grep /dev/block/sda33");
                        if (mnt_stat.isEmpty())
                            messages.setText("Mount Windows?");
                        else
                            messages.setText("Unmount Windows?");

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
                                                                    String mnt_stat = ShellUtils.Executer("su -c mount | grep /dev/block/sda33");
                                                                    if (mnt_stat.isEmpty()) {
                                                                        mount();
                                                                        messages.setText("Windows mounted");
                                                                    } else {
                                                                        unmount();
                                                                        messages.setText("Windows unmounted");
                                                                    }
                                                                    dismissButton.setText(getString(R.string.dismiss));
                                                                    dismissButton.setVisibility(View.VISIBLE);
                                                                } catch (Exception error) {
                                                                    error.printStackTrace();
                                                                }
                                                            }
                                                        },
                                                        500);
                                    }
                                });
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
                        ShowBlur();
                        noButton.setText(getString(R.string.no));
                        dismissButton.setText(getString(R.string.dismiss));
                        yesButton.setText(getString(R.string.yes));
                        dismissButton.setVisibility(View.GONE);
                        yesButton.setVisibility(View.VISIBLE);
                        noButton.setVisibility(View.VISIBLE);
                        icons.setImageDrawable(modem);
                        messages.setText(getString(R.string.dump_modem_question));
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
                                                                String mnt_stat = ShellUtils.Executer("su -c mount | grep /dev/block/sda33");
                                                                if (mnt_stat.isEmpty()) {
                                                                    mount();
                                                                    dump();
                                                                    unmount();
                                                                } else
                                                                    dump();
                                                                messages.setText("enjoy LTE on Windows");
                                                                dismissButton.setVisibility(View.VISIBLE);
                                                            }
                                                        },
                                                        500);
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
                        ShowBlur();
                        noButton.setText(getString(R.string.no));
                        dismissButton.setText(getString(R.string.dismiss));
                        yesButton.setText(getString(R.string.yes));
                        dismissButton.setVisibility(View.GONE);
                        yesButton.setVisibility(View.VISIBLE);
                        noButton.setVisibility(View.VISIBLE);
                        messages.setText(String.format(getString(R.string.flash_uefi_question), panel));
                        if(dual)
                            messages.setText(String.format(getString(R.string.flash_uefi_question1), panel));
                        icons.setImageDrawable(uefi);
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
                                                                    if (dual){
                                                                        flash(finduefi1);
                                                                        messages.setText(
                                                                                "UEFI for  "
                                                                                        + panel
                                                                                        + " 60Hz is flashed sucsessfully.\nNext reboot will boot into Windows.");
                                                                }
                                                                    else {
                                                                        flash(finduefi);
                                                                        messages.setText(
                                                                                "UEFI for the "
                                                                                        + panel
                                                                                        + " panel variant successfully flashed to boot partition.\nNext reboot will boot into Windows.");

                                                                    }
                                                                    dismissButton.setVisibility(View.VISIBLE);
                                                                } catch (Exception error) {
                                                                    error.printStackTrace();
                                                                }
                                                            }
                                                        },
                                                        500);
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

        x.cvFlashUefi2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowBlur();
                        noButton.setText(getString(R.string.no));
                        dismissButton.setText(getString(R.string.dismiss));
                        yesButton.setText(getString(R.string.yes));
                        dismissButton.setVisibility(View.GONE);
                        yesButton.setVisibility(View.VISIBLE);
                        noButton.setVisibility(View.VISIBLE);
                        messages.setText(String.format(getString(R.string.flash_uefi_question2), panel, ramvalue));
                        icons.setImageDrawable(uefi);
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
                                                                    flash(finduefi2);
                                                                    messages.setText(
                                                                            "UEFI for the "
                                                                                    + panel
                                                                                    + " panel with 120hz.\nNext reboot will boot into Windows.");
                                                                    dismissButton.setVisibility(View.VISIBLE);
                                                                } catch (Exception error) {
                                                                    error.printStackTrace();
                                                                }
                                                            }
                                                        },
                                                        500);
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


        x.cvBackup.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowBlur();
                        yesButton.setText(getString(R.string.yes));
                        noButton.setText(getString(R.string.no));
                        dismissButton.setText(getString(R.string.dismiss));
                        icons.setImageDrawable(boot);
                        noButton.setVisibility(View.VISIBLE);
                        yesButton.setVisibility(View.VISIBLE);
                        dismissButton.setVisibility(View.GONE);
                        messages.setText(getString(R.string.backup_boot_question));
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
                                                                String mnt_stat = ShellUtils.Executer("su -c mount | grep /dev/block/sda33");
                                                                if (mnt_stat.isEmpty()) {
                                                                    mount();
                                                                    ShellUtils.Executer(getString(R.string.backup));
                                                                    unmount();
                                                                } else
                                                                    ShellUtils.Executer(getString(R.string.backup));
                                                                messages.setText("Backup boot image successful...\n");
                                                                dismissButton.setVisibility(View.VISIBLE);
                                                            }
                                                        },
                                                        500);
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

    x.cvBackup2.setOnClickListener(
            new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ShowBlur();
            yesButton.setText(getString(R.string.yes));
            noButton.setText(getString(R.string.no));
            dismissButton.setText(getString(R.string.dismiss));
            icons.setImageDrawable(boot);
            noButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
            dismissButton.setVisibility(View.GONE);
            messages.setText(getString(R.string.backup_boot_question1));
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
                                                    String mnt_stat = ShellUtils.Executer("su -c mount | grep /dev/block/sda33");
                                                    if (mnt_stat.isEmpty()) {
                                                        mount();
                                                        ShellUtils.Executer(getString(R.string.backup1));
                                                        unmount();
                                                    } else
                                                        ShellUtils.Executer(getString(R.string.backup1));
                                                    messages.setText("Backup boot image successful...\n");
                                                    dismissButton.setVisibility(View.VISIBLE);
                                                }
                                            },
                                            500);
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


    public void flash(String uefi) {
        String run = ShellUtils.Executer("su -c dd if=" + uefi + " of=/dev/block/by-name/boot");
    }

    public void mount() {
        String run = ShellUtils.Executer(getString(R.string.mk));
        String run1 = ShellUtils.Executer(getString(R.string.mount));
        mounted = "UNMOUNT";
        x.tvMnt.setText(String.format(getString(R.string.mnt_title), mounted));
    }

    public void unmount() {
        String run = ShellUtils.Executer(getString(R.string.unmount));
        String run1 = ShellUtils.Executer(getString(R.string.rm));
        mounted = "MOUNT";
        x.tvMnt.setText(String.format(getString(R.string.mnt_title), mounted));
    }

    public void dump() {
        String run1 = ShellUtils.Executer(getString(R.string.modem1));
        String run2 = ShellUtils.Executer(getString(R.string.modem2));
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
        finduefi1 = ShellUtils.Executer(getString(R.string.uefiChk1) + "| grep  " + panel.toLowerCase());
        finduefi2 = ShellUtils.Executer(getString(R.string.uefiChk2) + "| grep  " + panel.toLowerCase());
        if (finduefi1.isEmpty() || finduefi2.isEmpty()) {
            dual=false;
            finduefi = ShellUtils.Executer(getString(R.string.uefiChk) +"| grep  " + panel.toLowerCase() );
            if (finduefi.isEmpty()) {
                x.cvFlashUefi.setEnabled(false);
                x.tvFlashUefi.setText(getString(R.string.uefi_not_found));
                x.tvUefiSubtitle.setVisibility(View.VISIBLE);
                x.tvUefiSubtitle.setText(getString(R.string.uefi_not_found_subtitle));
            } else {
                x.tvQuickBoot.setText(getString(R.string.quickboot_title));
                x.tvBootSubtitle.setText(getString(R.string.quickboot_subtitle));
                x.tvFlashUefi.setText(getString(R.string.flash_uefi_title));
                x.cvQuickBoot.setVisibility(View.VISIBLE);
                x.cvFlashUefi.setEnabled(true);
                x.tvUefiSubtitle.setText(String.format(getString(R.string.flash_uefi_subtitle), panel));
                x.tvUefiSubtitle2.setText(String.format(getString(R.string.flash_uefi_subtitle2), panel));
                x.tvUefiSubtitle.setVisibility(View.VISIBLE);
            }
        }
        else {
            x.tvQuickBoot.setText(getString(R.string.quickboot_title1));
            x.tvBootSubtitle.setText(getString(R.string.quickboot_subtitle1));
            dual=true;
            x.cvQuickBoot2.setVisibility(View.VISIBLE);
            x.tvQuickBoot2.setText(getString(R.string.quickboot_title2));
            x.tvBootSubtitle2.setText(getString(R.string.quickboot_subtitle2));
            x.cvFlashUefi.setEnabled(true);
            x.tvFlashUefi.setText(getString(R.string.flash_uefi_title1));
            x.tvUefiSubtitle.setText(String.format(getString(R.string.flash_uefi_subtitle1), panel));
            x.cvFlashUefi2.setVisibility(View.VISIBLE);
            x.tvFlashUefi2.setText(getString(R.string.flash_uefi_title2));
            x.tvUefiSubtitle2.setText(String.format(getString(R.string.flash_uefi_subtitle2), panel));
            x.cvQuickBoot.setVisibility(View.VISIBLE);
        }

    }


  public void ShowBlur() {
    x.blur.setVisibility(View.VISIBLE);
  }

  public void HideBlur() {
    x.blur.setVisibility(View.GONE);
  }
}
