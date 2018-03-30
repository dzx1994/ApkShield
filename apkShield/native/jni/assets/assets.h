#ifndef ANTI_DEBUG_MASTER_ASSETS_H
#define ANTI_DEBUG_MASTER_ASSETS_H

#include <stdlib.h>
#include <map>

#include "../dalvik/common.h"
#include "Asset.h"
#include <jni.h>

extern JavaVM *gvm;

typedef struct {
    const char *tClazz;
    const char *tMethod;
    const char *tMethodSig;
    int sdkVersion;
} HookInfo;

void hookAssetManagerRead(int sdkVersion);

#endif //ANTI_DEBUG_MASTER_ASSETS_H