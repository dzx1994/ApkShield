#include <stdbool.h>
#include "native_create.h"

void onCreate(JNIEnv *env, jobject thiz) {
    super(env, thiz, "onCreate", "()V");

#ifdef DEBUG
    LOGI("In onCreate!!!");
#endif

    jclass cls_ActivityThread = (*env)->FindClass(env, "android/app/ActivityThread");
    jclass cls_AppBindData = (*env)->FindClass(env, "android/app/ActivityThread$AppBindData");
    jclass cls_ProviderClinetRecord
            = (*env)->FindClass(env, "android/app/ActivityThread$ProviderClientRecord");
    jclass cls_LoadedApk = (*env)->FindClass(env, "android/app/LoadedApk");
    jclass cls_ContentProvider = (*env)->FindClass(env, "android/content/ContentProvider");
    jclass cls_Application = (*env)->FindClass(env, "android/app/Application");
    jclass cls_ApplicationInfo = (*env)->FindClass(env, "android/content/pm/ApplicationInfo");

    jclass cls_ProxyApplication = (*env)->FindClass(env, "com/wknight/dexshell/ProxyApplication");
    jclass cls_VERSION = (*env)->FindClass(env, "android/os/Build$VERSION");

    jclass cls_ArrayList = (*env)->FindClass(env, "java/util/ArrayList");
    jclass cls_Collection = (*env)->FindClass(env, "java/util/Collection");
    jclass cls_Iterator = (*env)->FindClass(env, "java/util/Iterator");
#ifdef DEBUG
    assert(cls_ActivityThread != NULL);
    assert(cls_AppBindData != NULL);
    assert(cls_ProviderClinetRecord != NULL);
    assert(cls_LoadedApk != NULL);
    assert(cls_ContentProvider != NULL);
    assert(cls_Application != NULL);
    assert(cls_ApplicationInfo != NULL);
    assert(cls_ProxyApplication != NULL);
    assert(cls_VERSION != NULL);
    assert(cls_ArrayList != NULL);
    assert(cls_Collection != NULL);
    assert(cls_Iterator != NULL);
#endif
    jobject currentActivityThread = callStaticObjectMethod(env, cls_ActivityThread,
                                                           "currentActivityThread",
                                                           "()Landroid/app/ActivityThread;");

    jobject mBoundApplication = getObjectField(
            env, cls_ActivityThread,
            "mBoundApplication",
            "Landroid/app/ActivityThread$AppBindData;",
            currentActivityThread
    );

    jobject loadedApkInfo = getObjectField(
            env, cls_AppBindData,
            "info", "Landroid/app/LoadedApk;",
            mBoundApplication
    );

    setObjectField(env, cls_LoadedApk, "mApplication",
                   "Landroid/app/Application;", loadedApkInfo, NULL);


    jobject mAllApplications = getObjectField(
            env, cls_ActivityThread,
            "mAllApplications", "Ljava/util/ArrayList;",
            currentActivityThread
    );

    jobject oldApplication = getObjectField(
            env, cls_ActivityThread,
            "mInitialApplication",
            "Landroid/app/Application;",
            currentActivityThread
    );

    callBooleanMethod(env, cls_ArrayList, mAllApplications,
                      "remove", "(Ljava/lang/Object;)Z", oldApplication);

    jobject appInfoInLoadedApk = getObjectField(
            env, cls_LoadedApk,
            "mApplicationInfo",
            "Landroid/content/pm/ApplicationInfo;",
            loadedApkInfo
    );

    jobject appInfoInAppBindData = getObjectField(
            env, cls_AppBindData,
            "appInfo", "Landroid/content/pm/ApplicationInfo;",
            mBoundApplication
    );

    jstring appClassName = (jstring) getObjectField(
            env, cls_ProxyApplication,
            "appClassName", "Ljava/lang/String;",
            thiz
    );

    const char *app_class_name = (*env)->GetStringUTFChars(env, appClassName, NULL);
    if (app_class_name[0] == '\0') {
        (*env)->ReleaseStringUTFChars(env, appClassName, app_class_name);
        appClassName = (*env)->NewStringUTF(env, "android.app.Application");
    }

    setObjectField(env, cls_ApplicationInfo, "className", "Ljava/lang/String;",
                   appInfoInLoadedApk, appClassName);

    setObjectField(env, cls_ApplicationInfo, "className", "Ljava/lang/String;",
                   appInfoInAppBindData, appClassName);

    jobject app = callObjectMethod(
            env, cls_LoadedApk, loadedApkInfo,
            "makeApplication", "(ZLandroid/app/Instrumentation;)Landroid/app/Application;",
            false, NULL
    );

    setObjectField(env, cls_LoadedApk, "mApplication", "Landroid/app/Application;",
                   loadedApkInfo, app);

    setObjectField(env, cls_ActivityThread, "mInitialApplication", "Landroid/app/Application;",
                   currentActivityThread, app);

    jobject iterator = NULL;
    jobject collection = NULL;
    jfieldID fID = (*env)->GetStaticFieldID(env, cls_VERSION, "SDK_INT", "I");
    jint sdk_int = (*env)->GetStaticIntField(env, cls_VERSION, fID);

    if (sdk_int < 19) {
        jobject mProviderMap = getObjectField(env, cls_ActivityThread,
                                              "mProviderMap", "Ljava/util/HashMap;",
                                              currentActivityThread);
        jclass cls_HashMap = (*env)->FindClass(env, "java/util/HashMap");
        assert(cls_HashMap != NULL);
        collection = callObjectMethod(env, cls_HashMap, mProviderMap,
                                      "values", "()Ljava/util/Collection;");
    } else {
        jobject mProviderMap = getObjectField(env, cls_ActivityThread,
                                              "mProviderMap", "Landroid/util/ArrayMap;",
                                              currentActivityThread);
        jclass cls_ArrayMap = (*env)->FindClass(env, "android/util/ArrayMap");
        assert(cls_ArrayMap != NULL);
        collection = callObjectMethod(env, cls_ArrayMap, mProviderMap,
                                      "values", "()Ljava/util/Collection;");
    }

    iterator = callObjectMethod(env, cls_Collection, collection,
                                "iterator", "()Ljava/util/Iterator;");

    while (callBooleanMethod(env, cls_Iterator, iterator, "hasNext", "()Z")) {
        jobject providerClientRecord = callObjectMethod(env, cls_Iterator, iterator,
                                                        "next", "()Ljava/lang/Object;");
        if (providerClientRecord) {
            jobject localProvider = getObjectField(env, cls_ProviderClinetRecord,
                                                   "mLocalProvider",
                                                   "Landroid/content/ContentProvider;",
                                                   providerClientRecord);
            if (localProvider) {
                setObjectField(env, cls_ContentProvider,
                               "mContext", "Landroid/content/Context;",
                               localProvider, app);
            }
        }
    }

    callVoidMethod(env, cls_Application, app, "onCreate", "()V");
}