
for /r data\packages\base %%f in (*.spine) do Spine -i "%%f" -o data\packages\base\contents\content\animations -e json