Option Explicit

Dim objShell, objRegistry, strTabletPostureTaskbar, strMsg, intResponse, intNewValue, strStatus
Const HKEY_CURRENT_USER = &H80000001
Set objShell = CreateObject("WScript.Shell")
Set objRegistry = GetObject("winmgmts:\\.\root\default:StdRegProv")

' Registry path and parameter name
Dim strRegPath, strValueName, Version
strRegPath = "Software\Microsoft\Windows\CurrentVersion\Explorer"
strValueName = "TabletPostureTaskbar"
Version = "Optimized Taskbar Control by Misha803 V1.0"

' Read the current value of TabletPostureTaskbar
objRegistry.GetDWORDValue HKEY_CURRENT_USER, strRegPath, strValueName, strTabletPostureTaskbar

' Check if the parameter exists, if not - create it with value 0 (disabled)
If IsNull(strTabletPostureTaskbar) Then
    objShell.RegWrite "HKEY_CURRENT_USER\" & strRegPath & "\" & strValueName, 0, "REG_DWORD"
    strTabletPostureTaskbar = 0
    MsgBox "Registry parameter 'TabletPostureTaskbar' was not found and has been created with value 0 (disabled).", vbInformation, Version
End If

' Determine the current state
If strTabletPostureTaskbar = 1 Then
    strMsg = "Optimized Taskbar is currently enabled. Do you want to disable it?"
    intNewValue = 0
Else
    strMsg = "Optimized Taskbar is currently disabled. Do you want to enable it?"
    intNewValue = 1
End If

' User prompt with version info in the title
intResponse = MsgBox(strMsg, vbYesNo + vbQuestion, Version)

If intResponse = vbYes Then
    ' Change state
    objShell.RegWrite "HKEY_CURRENT_USER\" & strRegPath & "\" & strValueName, intNewValue, "REG_DWORD"
    
    ' Check for success
    objRegistry.GetDWORDValue HKEY_CURRENT_USER, strRegPath, strValueName, strTabletPostureTaskbar
    If strTabletPostureTaskbar = intNewValue Then
        If intNewValue = 1 Then
            strStatus = "enabled"
        Else
            strStatus = "disabled"
        End If

        ' Restart Explorer 
        objShell.Run "taskkill /F /IM explorer.exe", 0, True
        objShell.Run "explorer.exe", 0, False
        
        MsgBox "Optimized Taskbar successfully " & strStatus & ".", vbInformation, Version
    Else
        MsgBox "Failed to update Optimized Taskbar. Please try again or contact us - https://t.me/ZMchata.", vbExclamation, Version
    End If

    ' Apply changes
    objShell.Run "RUNDLL32.EXE user32.dll,UpdatePerUserSystemParameters", 1, True
End If

' Release objects
Set objShell = Nothing
Set objRegistry = Nothing
