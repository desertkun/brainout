cd ../brainout-localization
git checkout localization
git pull
cp -R packages/base/localization/* ../brainout/data/packages/base/localization
cp -R packages/mainmenu/localization/* ../brainout/data/packages/mainmenu/localization
cp -R packages/editor/localization/* ../brainout/data/packages/editor/localization

