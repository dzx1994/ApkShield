#ifndef NATIVE_CREATE_H
#define NATIVE_CREATE_H

#include <jni.h>
#include <assert.h>

#include "dalvik/common.h"
#include "utils/JNIUtils.h"
#ifdef __cplusplus
extern "C" {
#endif
void onCreate(JNIEnv *env, jobject thiz);
#ifdef __cplusplus
}
#endif
#endif // NATIVE_CREATE_H
