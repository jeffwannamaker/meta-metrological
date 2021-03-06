require recipes-qt/qt5/qt5.inc
require qtwebkit_features.inc

LICENSE = "BSD & LGPLv2+ | GPL-2.0"
LIC_FILES_CHKSUM = " \
    file://LICENSE.GPLv2;md5=e782f55badfa137e5e59c330f12cc8ed \
    file://Source/WebCore/rendering/RenderApplet.h;endline=22;md5=fb9694013ad71b78f8913af7a5959680 \
    file://Source/WebKit/gtk/webkit/webkit.h;endline=21;md5=b4fbe9f4a944f1d071dba1d2c76b3351 \
    file://Source/JavaScriptCore/parser/Parser.h;endline=21;md5=bd69f72183a7af673863f057576e21ee \
"

DEPENDS += "qtbase qtdeclarative icu ruby-native sqlite3 glib-2.0 libxslt"

# qemuarm build fails with:
# | {standard input}: Assembler messages:
# | {standard input}:106: Error: invalid immediate: 983040 is out of range
# | {standard input}:106: Error: value of 983040 too large for field of 2 bytes at 146
ARM_INSTRUCTION_SET_armv4 = "arm"
ARM_INSTRUCTION_SET_armv5 = "arm"

PV = "5.4.1+metro+git${SRCPV}"

SRC_URI = "git://github.com/Metrological/qtwebkit.git;branch=qt5.4;protocol=http"

SRCREV = "95b96a1781b2030d8487bef374dac7239187580f"

S = "${WORKDIR}/git"

SRC_URI += "\
    file://0001-qtwebkit-fix-QA-issue-bad-RPATH.patch \
    file://0002-Remove-TEXTREL-tag-in-x86.patch \
    file://qt5webkit-add-support-authenticated-post-requests.patch \
    file://qt5webkit-avoid-crash-display-none-root-elements.patch \
    file://qt5webkit-incorrect-type-speculation-reported-by-toprimitive.patch \
    file://qt5webkit-llint-alignment-arm.patch \
    file://qt5webkit-map-css-font-weights.patch \
"

PACKAGECONFIG ??= "${@bb.utils.contains('MACHINE_FEATURES', 'gst010', 'gstreamer010', 'gstreamer', d)}"

PACKAGECONFIG[gstreamer] = "OE_GSTREAMER_ENABLED,,gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-bad,${RDEPS_GST}"
PACKAGECONFIG[gstreamer010] = "OE_GSTREAMER010_ENABLED,,gstreamer gst-plugins-base"
PACKAGECONFIG[qtlocation] = "OE_QTLOCATION_ENABLED,,qtlocation"
PACKAGECONFIG[qtmultimedia] = "OE_QTMULTIMEDIA_ENABLED,,qtmultimedia"
PACKAGECONFIG[qtsensors] = "OE_QTSENSORS_ENABLED,,qtsensors"
PACKAGECONFIG[qtwebchannel] = "OE_QTWEBCHANNEL_ENABLED,,qtwebchannel"
PACKAGECONFIG[libwebp] = "OE_LIBWEBP_ENABLED,,libwebp"

do_configure_prepend() {
    export QMAKE_CACHE_EVAL="CONFIG+=${EXTRA_OECONF}"
    # disable gstreamer-1.0 test if it isn't enabled by PACKAGECONFIG
    sed -e 's/\s\(packagesExist(".*\<gstreamer-1.0\>.*")\)/ OE_GSTREAMER_ENABLED:\1/' -i ${S}/Tools/qmake/mkspecs/features/features.prf
    # disable gstreamer-0.10 test if it isn't enabled by PACKAGECONFIG
    sed -e 's/\s\(packagesExist(".*\<gstreamer-0.10\>.*")\)/ OE_GSTREAMER010_ENABLED:\1/' -i ${S}/Tools/qmake/mkspecs/features/features.prf
    # disable qtlocation test if it isn't enabled by PACKAGECONFIG
    sed -e 's/\s\(qtHaveModule(positioning)\)/ OE_QTLOCATION_ENABLED:\1/' -i ${S}/Tools/qmake/mkspecs/features/features.prf
    # disable qtmultimedia test if it isn't enabled by PACKAGECONFIG
    sed -e 's/(video):\(qtHaveModule(multimediawidgets)\)/(video):OE_QTMULTIMEDIA_ENABLED:\1/' -i ${S}/Tools/qmake/mkspecs/features/features.prf
    # disable qtsensors test if it isn't enabled by PACKAGECONFIG
    sed -e 's/\s\(qtHaveModule(sensors)\)/ OE_QTSENSORS_ENABLED:\1/' -i ${S}/Tools/qmake/mkspecs/features/features.prf
    # disable qtwebchannel test if it isn't enabled by PACKAGECONFIG
    sed -e 's/\s\(qtHaveModule(webchannel)\)/ OE_QTWEBCHANNEL_ENABLED:\1/' -i ${S}/Source/WebKit2/Target.pri
    sed -e 's/\s\(qtHaveModule(webchannel)\)/ OE_QTWEBCHANNEL_ENABLED:\1/' -i ${S}/Source/WebKit2/WebKit2.pri
    # disable libwebp test if it isn't enabled by PACKAGECONFIG
    sed -e 's/\s\(config_libwebp: \)/ OE_LIBWEBP_ENABLED:\1/' -i ${S}/Tools/qmake/mkspecs/features/features.prf
}

# qtwebkit gets terribly big when linking with all debug info, disable by default
QTWEBKIT_DEBUG = "QMAKE_CFLAGS+=-g0 QMAKE_CXXFLAGS+=-g0"

EXTRA_QMAKEVARS_PRE += "${QTWEBKIT_DEBUG} ${QT5WEBKIT_CONFIG} ${QT5WEBKIT_EXTRA_LIBRARIES}"

# remove default ${PN}-examples-dbg ${PN}-examples set in qt5.inc, because it conflicts with ${PN} from separate webkit-examples recipe
PACKAGES = "${PN}-dbg ${PN}-staticdev ${PN}-dev ${PN}-doc ${PN}-locale ${PACKAGE_BEFORE_PN} ${PN} ${PN}-qmlplugins-dbg ${PN}-tools-dbg ${PN}-plugins-dbg ${PN}-qmlplugins ${PN}-tools ${PN}-plugins ${PN}-mkspecs "

# make sure rb files are used from sysroot, not from host
# ruby-1.9.3-always-use-i386.patch is doing target_cpu=`echo $target_cpu | sed s/i.86/i386/`
# we need to replace it too (a bit longer version without importing re)
RUBY_SYS = "${@ '${BUILD_SYS}'.replace('i486', 'i386').replace('i586', 'i386').replace('i686', 'i386') }"
export RUBYLIB="${STAGING_DATADIR_NATIVE}/rubygems:${STAGING_LIBDIR_NATIVE}/ruby:${STAGING_LIBDIR_NATIVE}/ruby/${RUBY_SYS}"

# Fixme: this is a default set and could probably be optimised.
RDEPS_GST = "\
    gstreamer1.0-meta-base \
    gstreamer1.0-meta-audio \
    gstreamer1.0-meta-video \
    gstreamer1.0-meta-debug \
    gstreamer1.0-plugins-base-app \
    gstreamer1.0-plugins-good-isomp4 \
    gstreamer1.0-plugins-bad-videoparsersbad \
"
