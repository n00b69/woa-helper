@echo off
(NET FILE||(powershell -command Start-Process '%0' -Verb runAs -ArgumentList '%* '&EXIT /B))>NUL 2>&1 
cd C:\Toolbox
echo You are about to install several frameworks etc.
pause
"C:\Toolbox\Frameworks\2005vcredist_x64.EXE"
"C:\Toolbox\Frameworks\2005vcredist_x86.EXE"
"C:\Toolbox\Frameworks\2008vcredist_x64.exe"
"C:\Toolbox\Frameworks\2008vcredist_x86.exe"
"C:\Toolbox\Frameworks\2010vcredist_x64.exe"
"C:\Toolbox\Frameworks\2010vcredist_x86.exe"
"C:\Toolbox\Frameworks\2012vcredist_x64.exe"
"C:\Toolbox\Frameworks\2012vcredist_x86.exe"
"C:\Toolbox\Frameworks\2013vcredist_x64.exe"
"C:\Toolbox\Frameworks\2013vcredist_x86.exe"
"C:\Toolbox\Frameworks\2015VC_redist.x64.exe"
"C:\Toolbox\Frameworks\2015VC_redist.x86.exe"
"C:\Toolbox\Frameworks\2022VC_redist.arm64.exe"
"C:\Toolbox\Frameworks\dxwebsetup.exe"
"C:\Toolbox\Frameworks\oalinst.exe"
"C:\Toolbox\Frameworks\PhysX-9.13.0604-SystemSoftware-Legacy.msi"
"C:\Toolbox\Frameworks\PhysX_9.23.1019_SystemSoftware.exe"
"C:\Toolbox\Frameworks\xnafx40_redist.msi"
appwiz.cpl
pause