@echo off
(NET FILE||(powershell -command Start-Process '%0' -Verb runAs -ArgumentList '%* '&EXIT /B))>NUL 2>&1 
cd C:\Toolbox
echo You are about to install several frameworks etc.
pause
"C:\Toolbox\2005vcredist_x64.EXE"
"C:\Toolbox\2005vcredist_x86.EXE"
"C:\Toolbox\2008vcredist_x64.exe"
"C:\Toolbox\2008vcredist_x86.exe"
"C:\Toolbox\2010vcredist_x64.exe"
"C:\Toolbox\2010vcredist_x86.exe"
"C:\Toolbox\2012vcredist_x64.exe"
"C:\Toolbox\2012vcredist_x86.exe"
"C:\Toolbox\2013vcredist_x64.exe"
"C:\Toolbox\2013vcredist_x86.exe"
"C:\Toolbox\2015VC_redist.x64.exe"
"C:\Toolbox\2015VC_redist.x86.exe"
"C:\Toolbox\2022VC_redist.arm64.exe"
"C:\Toolbox\dxwebsetup.exe"
"C:\Toolbox\oalinst.exe"
"C:\Toolbox\PhysX-9.13.0604-SystemSoftware-Legacy.msi"
"C:\Toolbox\PhysX_9.23.1019_SystemSoftware.exe"
"C:\Toolbox\xnafx40_redist.msi"
appwiz.cpl
pause