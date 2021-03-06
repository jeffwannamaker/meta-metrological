 #!/bin/bash
echo "Creating tmp dir"
cd ..
mkdir ./tmp
cd tmp

echo "Fetching buildroot make files"
wget -q https://github.com/Metrological/buildroot-wpe/raw/master/package/wpe/wpe.mk
wget -q https://github.com/Metrological/buildroot-wpe/raw/master/package/wpe-launcher/wpe-launcher.mk
wget -q https://raw.githubusercontent.com/Metrological/buildroot-wpe/master/package/cppsdk/cppsdk.mk
wget -q https://github.com/Metrological/buildroot-wpe/raw/master/package/dxdrm/dxdrm.mk
wget -q https://github.com/Metrological/buildroot-wpe/raw/master/package/libprovision/libprovision.mk
wget -q https://github.com/Metrological/buildroot-wpe/raw/master/package/webbridge/webbridge.mk
wget -q https://github.com/Metrological/buildroot-wpe/raw/master/package/netflix/netflix.mk
wget -q https://github.com/Metrological/buildroot-wpe/raw/master/package/icudata/icudata.mk
cd ..

echo "Checking and updating revs"
python ./tools/check_srcrevs.py -i ./tmp/wpe.mk -o ./recipes-wpe/wpe/wpe_0.1.bb
python ./tools/check_srcrevs.py -i ./tmp/wpe-launcher.mk -o ./recipes-wpe/wpe-launcher/wpe-launcher_0.1.bb
python ./tools/check_srcrevs.py -i ./tmp/cppsdk.mk -o ./recipes-metrological/cppsdk/cppsdk_git.bb
python ./tools/check_srcrevs.py -i ./tmp/dxdrm.mk -o ./recipes-metrological/dxdrm/dxdrm_git.bb
python ./tools/check_srcrevs.py -i ./tmp/libprovision.mk -o ./recipes-metrological/libprovision/libprovision_git.bb
python ./tools/check_srcrevs.py -i ./tmp/webbridge.mk -o ./recipes-metrological/webbridge/webbridge_git.bb
python ./tools/check_srcrevs.py -i ./tmp/netflix.mk -o ./recipes-netflix/netflix/netflix_git.bb
python ./tools/check_srcrevs.py -i ./tmp/icudata.mk  -o ./recipes-support/icu/icu_56%.bbappend

echo "Cleaning up tmp"
rm -r ./tmp

echo "Done."
