@echo off

:: First Stop Microsoft Edge and Edge Update Tasks and Services
taskkill /F /IM msedge.exe >nul 2>&1
taskkill /F /IM MicrosoftEdgeUpdate.exe >nul 2>&1

:: Attempt to stop the Microsoft Edge Update service if it exists
net stop "MicrosoftEdgeUpdate" >nul 2>&1

CD %HOMEDRIVE%%HOMEPATH%\Desktop
echo Current directory: %CD%

REM ************ Main process *****************

echo *** Removing Microsoft Edge ***
call :killdir C:\Windows\SystemApps\Microsoft.MicrosoftEdge_8wekyb3d8bbwe
call :killdir "C:\Program Files (x86)\Microsoft\Edge"
call :killdir "C:\Program Files (x86)\Microsoft\EdgeUpdate"
call :killdir "C:\Program Files (x86)\Microsoft\EdgeCore"
call :killdir "C:\Program Files (x86)\Microsoft\Temp"
echo *** Modifying registry ***
call :editreg
echo *** Removing shortcuts ***
call :delshortcut "C:\Users\Public\Desktop\Microsoft Edge.lnk"
call :delshortcut "%ProgramData%\Microsoft\Windows\Start Menu\Programs\Microsoft Edge.lnk"
call :delshortcut "%APPDATA%\Microsoft\Internet Explorer\Quick Launch\User Pinned\TaskBar\Microsoft Edge.lnk"
echo Finished!
pause
exit

REM ************ KillDir: Take ownership and remove a directory *****************

:killdir
setlocal
set retries=3
set delay=5
echo|set /p=Removing dir %1...

if exist %1 (
    takeown /a /r /d Y /f %1 > NUL
    icacls %1 /grant administrators:f /t > NUL

    :retry
    rd /s /q %1 > NUL
    if exist %1 (
        set /a retries-=1
        if %retries% geq 0 (
            echo Access denied. Retrying in %delay% seconds...
            timeout /t %delay% /nobreak > NUL
            goto retry
        )
        echo ...Failed.
    ) else (
        echo ...Deleted.
    )
) else (
    echo ...does not exist.
)
endlocal
exit /B 0

REM ************ Edit registry to add do not update Edge key *****************

:editreg
echo|set /p=Editing registry
echo Windows Registry Editor Version 5.00 > RemoveEdge.reg
echo. >> RemoveEdge.reg
echo [HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\EdgeUpdate] >> RemoveEdge.reg
echo "DoNotUpdateToEdgeWithChromium"=dword:00000001 >> RemoveEdge.reg
echo. >> RemoveEdge.reg
echo [-HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Active Setup\Installed Components\{9459C573-B17A-45AE-9F64-1857B5D58CEE}] >> RemoveEdge.reg

regedit /s RemoveEdge.reg
del RemoveEdge.reg
echo ...done.
exit /B 0

:: Delete Edge Icons, from all users
for /f "delims=" %%a in ('dir /b "C:\Users"') do (
    del /S /Q "C:\Users\%%a\Desktop\edge.lnk" >nul 2>&1
    del /S /Q "C:\Users\%%a\Desktop\Microsoft Edge.lnk" >nul 2>&1
)

:: Delete additional files in System32 if still present
if exist "C:\Windows\System32\MicrosoftEdgeCP.exe" (
    for /f "delims=" %%a in ('dir /b "C:\Windows\System32\MicrosoftEdge*"') do (
        takeown /f "C:\Windows\System32\%%a" > NUL 2>&1
        icacls "C:\Windows\System32\%%a" /inheritance:e /grant "%UserName%:(OI)(CI)F" /T /C > NUL 2>&1
        del /S /Q "C:\Windows\System32\%%a" > NUL 2>&1
    )
)

:delshortcut
echo|set /p=Removing shortcut %1...
if exist %1 (
    del %1
    echo ...done.
) else (
    echo ...does not exist.
)
exit /B 0
