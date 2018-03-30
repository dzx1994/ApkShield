#include <jni.h>
#include <stdio.h>
#include <string.h>
#include "dalvik/common.h"
#include "sign/digest_file.h"
#include "jni_export.h"

struct Lcode *linkhead;
int linkLen = 0;

jint filedigest(JNIEnv *env, jobject obj,
                 jbyteArray fileByteArr,jint filelen,jint tab,jstring fileName, jint total){
    char *filecontent,*filename;
    char *filedigest,*allfilename;
    struct Lcode *p;
    int len=0;
    int t = total;
    struct Lcode *mflink=NULL;

    if (!linkhead){
        return -1;
    }

    if(t != linkLen){
#ifdef DEBUG
        LOGI("total:%d linkLen:%d", t, linkLen);
#endif
        return -1;
    }

    filecontent = (char *)(*env)->GetByteArrayElements(env, fileByteArr, 0);
    filename = (char *)(*env)->GetStringUTFChars(env, fileName, 0);

    mflink = linkhead;
    while(mflink!=NULL){
        if(strcmp(mflink->name,filename)==0){
//            LOGI(">>>match file!!");
            filedigest = getfiledigest(filecontent,mflink->alg,filelen);
            int flag = 0;
            if (strcmp(mflink->digest,filedigest) != 0){
#ifdef DEBUG
                LOGI("digest different! filename:%s mflink->digest:%s filedigest:%s", filename, mflink->digest, filedigest);
#endif
                flag = -1;
            }
            mflink = linkhead;
            return flag;
        }
        mflink = mflink->next;
    }
#ifdef DEBUG
//    LOGI("=== no match filename:%s digest:%s", filename, filedigest);
    LOGI("=== no match filename:%s", filename);
#endif
    return -1;
}

int getLinkList(char* manifile) {
    int ret = 0;
//    apps_startup();
    //获取mf链表
    linkhead=getmflink(manifile);
    if(!linkhead){
        ret = -1;
    }
    ret = 1;
    struct Lcode *mflink = linkhead;


    while(mflink!=NULL){
//        LOGI("link->name:%s link->algo:%s link->digest:%s", mflink->name, mflink->alg, mflink->digest);
        mflink = mflink->next;
        linkLen++;
    }
#ifdef DEBUG
    LOGI("LinkList number:%d", linkLen);
#endif
    return ret;
}