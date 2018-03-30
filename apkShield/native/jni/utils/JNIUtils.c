#include "utils/JNIUtils.h"

void super(JNIEnv *env, jobject obj, const char *method, const char *sig, ...) {
    va_list args;
    va_start(args, sig);

    jclass selfClazz = (*env)->GetObjectClass(env, obj);
    jclass superClazz = (*env)->GetSuperclass(env, selfClazz);
    jmethodID mID = (*env)->GetMethodID(env, superClazz, method, sig);
    (*env)->CallNonvirtualVoidMethod(env, obj, superClazz, mID);
    va_end(args);
}

jobject getObjectField(JNIEnv *env, jclass clazz, const char *field, const char *sig,
                       jobject obj) {
    jfieldID fID = (*env)->GetFieldID(env, clazz, field, sig);
    return (*env)->GetObjectField(env, obj, fID);
}

void setObjectField(JNIEnv *env, jclass clazz, const char *field, const char *sig,
                    jobject obj, jobject value) {
    jfieldID fID = (*env)->GetFieldID(env, clazz, field, sig);
    (*env)->SetObjectField(env, obj, fID, value);
}

jobject callStaticObjectMethod(JNIEnv *env, jclass clazz,
                               const char *method, const char *sig, ...) {
    va_list args;
    va_start(args, sig);

    jmethodID mID = (*env)->GetStaticMethodID(env, clazz, method, sig);
    jobject obj = (*env)->CallStaticObjectMethodV(env, clazz, mID, args);
    va_end(args);
    return obj;
}

jobject callObjectMethod(JNIEnv *env, jclass clazz, jobject object,
                         const char *method, const char *sig, ...) {
    va_list args;
    va_start(args, sig);

    jmethodID mID = (*env)->GetMethodID(env, clazz, method, sig);
    jobject obj = (*env)->CallObjectMethodV(env, object, mID, args);
    va_end(args);
    return obj;
}

void callVoidMethod(JNIEnv *env, jclass clazz, jobject obj,
                    const char *method, const char *sig, ...) {
    va_list args;
    va_start(args, sig);

    jmethodID mID = (*env)->GetMethodID(env, clazz, method, sig);
    (*env)->CallVoidMethodV(env, obj, mID, args);
    va_end(args);
}

jboolean callBooleanMethod(JNIEnv *env, jclass clazz, jobject obj,
                           const char *method, const char *sig, ...) {
    va_list args;
    va_start(args, sig);

    jmethodID mID = (*env)->GetMethodID(env, clazz, method, sig);
    jboolean b = (*env)->CallBooleanMethodV(env, obj, mID, args);
    va_end(args);

    return b;
}