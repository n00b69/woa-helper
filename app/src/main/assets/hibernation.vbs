' Create an instance of WScript.Shell object
Set objShell = CreateObject("WScript.Shell")

' Read the value of the registry key
strValue = objShell.RegRead("HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Power\HiberbootEnabled")

' Check the value and perform the corresponding action
If strValue = 1 Then
    ' Fast Startup Disabled 
    MsgBox "Fast Startup is currently enabled.", vbInformation, "Fast Startup Control V1.0"
    ' Ask the user if they want to disable it
    res = MsgBox("Do you want to disable Fast Startup?", vbYesNo + vbQuestion + vbDefaultButton2, "Fast Startup Control V1.0")
    If res = vbYes Then
        ' Disable fast startup 
        Set objShell = CreateObject("Shell.Application")
        objShell.ShellExecute "cmd.exe", "/c Reg.exe add ""HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Power"" /v ""HiberbootEnabled"" /t REG_DWORD /d ""0"" /f", "", "runas", 0
        WScript.Sleep 0500 ' Wait for 0.5 seconds to ensure the registry change is applied
        ' Verify if the change was successful
        Set objShell = CreateObject("WScript.Shell")
        strValue = objShell.RegRead("HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Power\HiberbootEnabled")
        If strValue = 0 Then
            MsgBox "Fast Startup successfully disabled.", vbInformation, "Fast Startup Control V1.0"
        Else
            MsgBox "Failed to disable Fast Startup, make sure you click YES in the window that appears.", vbCritical, "Fast Startup Control"
        End If
    End If
ElseIf strValue = 0 Then
    ' Fast Startup disabled
    MsgBox "Fast Startup is currently disabled.", vbInformation, "Fast Startup Control V1.0"
    ' Ask the user if they want to enable it
    res = MsgBox("Do you want to enable Fast Startup?", vbYesNo + vbQuestion + vbDefaultButton2, "Fast Startup Control V1.0")
    If res = vbYes Then
        ' Enable Fast Startup 
        Set objShell = CreateObject("Shell.Application")
        objShell.ShellExecute "cmd.exe", "/c Reg.exe add ""HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Power"" /v ""HiberbootEnabled"" /t REG_DWORD /d ""1"" /f", "", "runas", 0
        WScript.Sleep 0500 ' Wait for 0.5 seconds to ensure the registry change is applied
        ' Verify if the change was successful
        Set objShell = CreateObject("WScript.Shell")
        strValue = objShell.RegRead("HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Power\HiberbootEnabled")
        If strValue = 1 Then
            MsgBox "Fast Startup successfully enabled.", vbInformation, "Fast Startup Control V1.0"
        Else
            MsgBox "Failed to enable Fast Startup, make sure that you click YES in the window that appears.", vbCritical, "Fast Startup Control V1.0"
        End If
    End If
End If

' Close the object
Set objShell = Nothing
