call gradlew.bat steam:dist
"C:\\SteamCMD\\steamcmd.exe" +login desertkun +run_app_build %cd%/scripts/steam/app_build_578310_win.vdf +quit
