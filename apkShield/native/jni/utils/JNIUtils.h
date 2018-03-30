#ifndef JNIUTILS_H
#define JNIUTILS_H

#include <jni.h>
#include <string.h>
#ifdef __cplusplus
extern "C" {
#endif
void super(JNIEnv *env, jobject obj, const char *method, const char *sig, ...);
void setObjectField(JNIEnv *env, jclass clazz,
                    const char *field, const char *sig,
                    jobject obj, jobject value);
jobject getObjectField(JNIEnv *env, jclass clazz,
                       const char *field, const char * sig, jobject obj);
jobject callStaticObjectMethod(JNIEnv *env, jclass clazz,
                               const char *method, const char *sig, ...);
jobject callObjectMethod(JNIEnv *env, jclass clazz, jobject obj,
                         const char *method, const char *sig, ...);
void callVoidMethod(JNIEnv *env, jclass clazz, jobject obj,
                    const char *method, const char *sig, ...);
jboolean callBooleanMethod(JNIEnv *env, jclass clazz, jobject obj,
                           const char *method, const char *sig, ...);
#ifdef __cplusplus
}
#endif
#endif // JNIUTILS_H
