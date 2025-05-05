package id.kuato.woahelper.widgets;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.topjohnwu.superuser.ShellUtils;

import java.util.Objects;

import id.kuato.woahelper.R;
import id.kuato.woahelper.preference.pref;

public class WidgetActivity extends AppCompatActivity {
    @SuppressLint({"StringFormatInvalid", "SdCardPath"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String widget_type = intent.getStringExtra("WIDGET_TYPE");

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        if (Objects.equals(widget_type, "quickboot")) {
            String finduefi = "\""+ ShellUtils.fastCmd(getString(R.string.uefiChk))+"\"";
            if (!finduefi.contains("img")) {
                String device = ShellUtils.fastCmd("getprop ro.product.device ");
                alertDialog
                        .setTitle(getString(R.string.uefi_not_found))
                        .setMessage(getString(R.string.uefi_not_found_subtitle, device))
                        .setPositiveButton(getString(R.string.dismiss), (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        });
            }
            else
                alertDialog
                        .setTitle(getString(R.string.quickboot_question))
                        .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                            // SOG ADD QUICKBOOT!!
                            finish();
                        })
                        .setNegativeButton(getString(R.string.no), (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        });
        }
        else if (Objects.equals(widget_type, "mount")) {
            String checkingforwin = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1");
            if (checkingforwin.isEmpty()) {
                alertDialog
                        .setTitle(getString(R.string.partition))
                        .setPositiveButton(getString(R.string.dismiss), (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        });
            }

            if (!checkingforwin.isEmpty()) {
                String win = ShellUtils.fastCmd("realpath " + checkingforwin);
                String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + win);
                if (mnt_stat.isEmpty())
                    alertDialog
                            .setTitle(getString(R.string.mount_question, pref.getMountLocation(this) ? "/mnt/Windows" : "/mnt/sdcard/Windows"))
                            .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                                // SOG ADD MOUNTING!!
                                finish();
                            });

                else
                    alertDialog
                            .setTitle(getString(R.string.unmount_question))
                            .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                                // SOG ADD UNMOUNTING!!
                                finish();
                            });

                alertDialog.setNegativeButton(getString(R.string.no), (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
            }
        }

        alertDialog.setOnCancelListener((dialog) -> {
            dialog.dismiss();
            finish();
        });
        alertDialog.show();
    }
}