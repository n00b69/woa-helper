@echo OFF
set "params=%*"
cd /d "%~dp0" && ( if exist "%temp%\getadmin.vbs" del "%temp%\getadmin.vbs" ) && fsutil dirty query %systemdrive% 1>nul 2>nul || (  echo Set UAC = CreateObject^("Shell.Application"^) : UAC.ShellExecute "cmd.exe", "/k cd ""%~sdp0"" && ""%~s0"" %params%", "", "runas", 1 >> "%temp%\getadmin.vbs" && "%temp%\getadmin.vbs" && exit /B )

schtasks /create /sc ONSTART /ru SYSTEM /delay 0000:02 /tr "C:\sta\sta.exe -n" /tn auto-flash 

PowerShell -command " $set= New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries ;  Set-ScheduledTask -TaskName auto-flash -Settings $set"
echo.
echo.
echo To disable, open "Task Scheduler" and delete the "auto-flash" task
pause
exit