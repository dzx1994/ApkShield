#include "assets.h"
#include <string.h>
static bool gAttached = false;
std::map<jlong, unsigned char *> map;

void crypt(unsigned char *buf, unsigned len);

JNIEnv *getJNIEnv() {
    int status;
    JNIEnv *env = NULL;
    if(!gvm) {
        return NULL;
    }

    status = gvm->GetEnv((void **) &env, JNI_VERSION_1_4);
    if (status != JNI_OK) {
        status = gvm->AttachCurrentThread(&env, NULL);
        if (status != JNI_OK) {
            return NULL;
        }
        gAttached = true;
    }
    return env;
}

void DetachCurrent() {
    if (gAttached) {
        gvm->DetachCurrentThread();
    }
}

bool isPicture(unsigned char *b) {
    bool isPic = false;

    if (b[0] == 0xff && b[1] == 0xd8) {
    LOGI("===b[0] == 0xff && b[1] == 0xd8");
        isPic = true;
    } else if (b[0] == 0x89 && b[1] == 0x50 &&
            b[2] == 0x4e && b[3] == 0x47 &&
            b[4] == 0x0d && b[5] == 0x0a &&
            b[6] == 0x1a && b[7] == 0x0a) {
            LOGI("===b[0] == 0x89 && b[1] == 0x50");
        isPic = true;
    }

    return isPic;
}

JNIEXPORT jint hook_readAsset(JNIEnv *env, jobject thiz, jlong asset,
                              jbyteArray bArray, jint off, jint len) {

    android::Asset* a = reinterpret_cast<android::Asset*>(asset);

    off64_t length = a->getLength();
    off64_t remainingLength = a->getRemainingLength();
    off64_t offset = length - remainingLength;

    unsigned char *buf = NULL;
    buf = map[asset];
    if (buf == NULL) {
        unsigned char *buffer = (unsigned char *)a->getBuffer(false);
        buf = (unsigned char *)malloc(length);
        memcpy(buf, buffer, length);

        crypt(buf, length);

        bool shouldDecypher = isPicture(buf);
        if (!shouldDecypher) {
            free(buf);
            buf = NULL;
        } else {
            map[asset] = buf;
        }
    }

    if (a == NULL || bArray == NULL) {
        return -1;
    }

    if (len == 0) {
        return 0;
    }

    jsize bLen = env->GetArrayLength(bArray);
    if (off < 0 || off >= bLen || len < 0 || len > bLen || (off+len) > bLen) {
        return -1;
    }

    jbyte* b = env->GetByteArrayElements(bArray, NULL);
    ssize_t res = a->read(b + off, len);
    if (buf) {
        memcpy(b + off, buf + offset, res);
    }
    env->ReleaseByteArrayElements(bArray, b, 0);

    if (res > 0) return static_cast<jint>(res);

    if (res < 0) {
        return -1;
    }
    return -1;
}

JNIEXPORT  jlong hook_openAsset(JNIEnv *env, jobject clazz,jstring fileName, jint mode) {

    LOGI("invoke openAsset Method");

    return 100;
}

JNIEXPORT jint hook_readAsset18(JNIEnv *env, jobject thiz, jint asset,
                              jbyteArray bArray, jint off, jint len) {

    return (jint)hook_readAsset(env, thiz, (jlong)asset, bArray, off, len);
}

void hook_destroyAsset(JNIEnv *env, jobject thiz, jlong asset) {
    android::Asset* a = reinterpret_cast<android::Asset*>(asset);

    if (a == NULL) {
        return;
    }

    unsigned char *buffer = map[asset];
    if (buffer != NULL) {
        free(buffer);
        map.erase(asset);
    }

    delete a;
}

void hook_destroyAsset18(JNIEnv *env, jobject thiz, jint asset) {
    hook_destroyAsset(env, thiz, (jlong)asset);
}

void MethodHook(HookInfo info[]) {
    JNIEnv *env = getJNIEnv();

    jclass clazzTarget = env->FindClass(info[0].tClazz);

    JNINativeMethod gMethod[] = {
            {info[0].tMethod, info[0].tMethodSig, (void *)hook_readAsset},
            {info[1].tMethod, info[1].tMethodSig, (void *)hook_destroyAsset}
    };

    if (info[0].sdkVersion > 19) {
        gMethod[0].fnPtr = (void *)hook_readAsset;
        gMethod[1].fnPtr = (void *)hook_destroyAsset;
    } else {
        gMethod[0].fnPtr = (void *)hook_readAsset18;
        gMethod[1].fnPtr = (void *)hook_destroyAsset18;
    }

    if (env->RegisterNatives(clazzTarget, gMethod, 2) < 0) {
        LOGE("RegisterNativers error.");
        return;
    }

    DetachCurrent();
}

void hookAssetManagerRead(int sdkVersion) {
    if (sdkVersion > 19) {
        HookInfo hookInfo[] = {
                {"android/content/res/AssetManager", "readAsset", "(J[BII)I", sdkVersion},
                {"android/content/res/AssetManager", "destroyAsset", "(J)V", sdkVersion}
        };
        MethodHook(hookInfo);
    } else {
        HookInfo hookInfo[] = {
                {"android/content/res/AssetManager", "readAsset", "(I[BII)I", sdkVersion},
                {"android/content/res/AssetManager", "destroyAsset", "(I)V", sdkVersion}
        };
        MethodHook(hookInfo);
    }
}

/**********     RC4     **************/
void crypt(unsigned char *buf, unsigned len) {
    int i = 0, j = 0, t = 0;
    unsigned long k = 0;
    unsigned char tmp = 0;
    unsigned char key[256] = { 0 };
    unsigned char key_tmp[256] = { 0 };
    unsigned char s[256] = { 0 };
    unsigned char tmpChar[128] = {  'q', 'F', 'h', '~', 'g', '+', 'f', '?',\
									'\"', 'c', 'e', 't', 'j', '`', '^', '2',\
									'@', '%', 'b', '<', '>', ';', ':', '{',\
									'\\', '\n', '}', 'y', 'i', '(', 'O', '7',\
									'9', 'l', 'A', '$', 'h', 'd', 'r', 'o',\
									'p', 'h', '&', 'S', 'x', '|', ')', 'k',\
									'e', 'l', ' ', 'i', 'z', 'W', ':', '.',\
									'x', 'z', 'N', 'u', 'r', 'M', ',', 'v',\
									'g', 'w', 'b', '4', '!', '-', 'f', '_',\
									'c', '\t', 'p', 'd', 'h', '#', 'g', '0',\
									'.', '+', 'x', '~', '`', 'c', 'A', 'F',\
									'A', 'K', ']', '[', '*', 'Y', 'I', 'P',\
									'1', '>', '\"', '-', '*', 'E', '$', 't',\
									'L', '3', 'S', 'V', '4', '\\', 'm', 'n',\
									'x', 'Q', 'R', 'q', 'c', 't', ',', 'w',\
									']', '/', 's', 'T', 'x', 'b', '5', '.'	};
    for(i = 0; i < 256; ++i) {
        if(i%2 != 0) {
            key[i] = len % (i + 1);
        } else {
            key[i] = tmpChar[i / 2];
        }
    }

    unsigned long Len = sizeof(key);

    for(i = 0; i < 256; ++i) {
        s[i] = i;
        key_tmp[i] = key[i % Len];
    }

    for (i = 0; i < 256; ++i) {
        j = (j + s[i] + key_tmp[i]) % 256;
        tmp = s[i];
        s[i] = s[j];
        s[j] = tmp;
    }

    for(k = 0, i = 0, j = 0; k < len; ++k) {
        i = (i + 1) % 256;
        j = (j + s[i]) % 256;
        tmp = s[i];
        s[i] = s[j];
        s[j] = tmp;
        t=(s[i] + s[j]) % 256;
        buf[k] ^= s[t];
    }
}