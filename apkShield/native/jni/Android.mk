LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := verify
LOCAL_ARM_MODE	:= thumb
LOCAL_CFLAGS	:= -fexceptions
#LOCAL_CFLAGS    += -DDEBUG
LOCAL_SRC_FILES := \
	sign/digest_file.c \
	sign/base64.c \
	sign/sha1.c   \
	sign/sha256.c
include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := lzma
LOCAL_ARM_MODE	:= thumb
LOCAL_CFLAGS	:= -fexceptions
#LOCAL_CFLAGS    += -DDEBUG
LOCAL_SRC_FILES := \
	lzma/Alloc.c \
	lzma/LzmaDec.c \
	lzma/LzmaLib.c
include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE	:= assets
LOCAL_ARM_MODE	:= thumb
LOCAL_CPPFLAG	:= -std=gnu++11 -fpermissive -O0 -fexceptions
LOCAL_SRC_FILES := \
	assets/assets.cpp
include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := watcher
LOCAL_SRC_FILES := \
                watcher/FileWatcher.c\
                watcher/Signal.c\
                watcher/select.c

include $(BUILD_STATIC_LIBRARY)

cmd-strip = $(TOOLCHAIN_PREFIX)strip --strip-debug -x $1
include $(CLEAR_VARS)
LOCAL_MODULE    := WKnightShield
LOCAL_ARM_MODE	:= thumb
LOCAL_LDLIBS	:= -llog -lz
LOCAL_CPPFLAG	:= -std=gnu++11 -fpermissive -O0 -fexceptions
LOCAL_CPPFLAG	+= -fvisibility=hidden
LOCAL_CFLAGS	:= -fpermissive -O0 -fexceptions
#LOCAL_CFLAGS    += -mllvm -xse
#LOCAL_CFLAGS    += -mllvm -sub -mllvm -fla -mllvm -bcf
LOCAL_CFLAGS    += -DDEBUG
#LOCAL_CFLAGS    += -DSHAREPRE
#LOCAL_CFLAGS    += -DCERT
#LOCAL_CFLAGS    += -DDECRYPT
#LOCAL_CFLAGS    += -DREPLACE
#LOCAL_CFLAGS    += -DANTIDEBUG
LOCAL_CFLAGS	+= -fvisibility=hidden
#LOCAL_LDFLAGS	:= -v

LOCAL_SRC_FILES := \
	HookUtils.c\
	utils/JNIUtils.c\
	RC4.c\
	anti_debug.c\
	decompress.c\
	native_create.c\
	Shield.cpp

LOCAL_STATIC_LIBRARIES := \
	libwatcher	\
	libassets   \
	liblzma

include $(BUILD_SHARED_LIBRARY)
