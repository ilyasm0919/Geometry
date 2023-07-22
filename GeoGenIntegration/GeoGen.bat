@echo off
set dir=%~dp0
if exist "GeoGen/Output" rmdir /s /q "GeoGen/Output"
mkdir "GeoGen/Output"
robocopy GeoGen %GeoGen%/Temp input.txt /mir > nul
cd %GeoGen%
if exist "Temp/Output" rmdir /s /q "Temp/Output"
mkdir "Temp/Output"
GeoGen.MainLauncher %dir%/GeoGen/settings.json || exit
for %%f in (Temp/Output/*) do %dir%/build/bin/native/releaseExecutable/GeoGenIntegration.exe Temp/Output/%%f %dir%/GeoGen/Output
