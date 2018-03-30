#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <dlfcn.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/system_properties.h>
#include <fcntl.h>
#include <sys/stat.h>

#include "dalvik/common.h"
#include "dalvik/vm/DvmDex.h"
#include "dalvik/vm/JarFile.h"
#include "dalvik/vm/Object.h"
#include "dalvik/vm/Globals.h"
#include "HookUtils.h"
#include "RC4.h"
#include "Shield.h"
#include "anti_debug.h"
#include "decompress.h"
#include "assets/assets.h"
#include "utils/JNIUtils.h"
#include "native_create.h"

void printChar(const char *msg, char * src, int len);
extern "C" void setPackageName(JNIEnv* env, jobject thiz, jstring pName);

typedef int (*fnexecv)(const char *name, char **argv);
fnexecv ori_execv = NULL;

//android 6.0 use
typedef size_t (*fnread) (int fd, void *buf, size_t count);
fnread ori_read = NULL;
fnread ori_read2 = NULL;

typedef size_t (*fnwrite) (int fd, const void * buf, size_t count);
fnwrite ori_write = NULL;

//android 7.1 use
typedef size_t (*fn__read_chk) (int fd, void *buf, size_t nbytes, size_t buflen);
fn__read_chk ori__read_chk = NULL;
fn__read_chk ori__read_chk2 = NULL;

typedef size_t (*fn__write_chk) (int fd, void* buf, size_t count, size_t buf_size);
fn__write_chk ori__write_chk = NULL;

typedef void* (*fnmmap) (void* start,size_t length,int prot,int flags,int fd,off_t offset);
fnmmap ori_mmap = NULL;
fnmmap ori_mmap1 = NULL;

typedef int (*fnopen) (const char * pathname, int flags);
fnopen ori_open = NULL;

typedef int (*fncreat) (const char * pathname, int mode);
fncreat ori_create = NULL;

typedef int (*fnfstat) (int filedes, struct stat *buf);
fnfstat ori_fstat = NULL;

typedef long (*Dalvik_dalvik_system_DexFile_openDexFileNative_func)(const u4* args, JValue* pResult);
Dalvik_dalvik_system_DexFile_openDexFileNative_func Dalvik_dalvik_system_DexFile_openDexFileNative_ptr = NULL;

typedef int (*Dalvik_dalvik_system_DexFile_openDexFileNative_yunos)(VMethodEntryStruct* , void**, void*, uint32_t);
Dalvik_dalvik_system_DexFile_openDexFileNative_yunos Dalvik_dalvik_system_DexFile_openDexFileNative_ptr_yunos = NULL;

typedef int (*HashCompareFunc)(const void* tableItem, const void* looseItem);

JNIEnv* env = NULL;
JavaVM *gvm = NULL;

dexInfo *dexhead;

static Method* openDexFileNative_med = NULL;
static VMethodEntryStruct* openDexFileNative_med2 = NULL;

char *packageName = NULL;
char *payloadDexPath = NULL;
char *payloadDexPath_secondary = NULL;
char * FULL_SECONDARY_FOLDER_NAME = NULL;

size_t dexLength = 0;
unsigned char* dex;
size_t sum = 0;

int last = 0;

unsigned char s[256] = {0};    //S-box
unsigned char key[256] = {0};

unsigned char s_sp[256] = {0};
unsigned char key_sp[256] = {0};

const char *object;
const char *dex2oat;
const char *path1;
const char *path2;
const char *path3;
const char *dalvik_system_DexFile_class;
const char *dvm_dalvik_system_DexFile_class;
const char *openDexFileMethod1;
const char *openDexFileMethod2;
const char *dvmDecodeRefThread;
const char *dvmCreateCstr;
const char *execv_method;
const char *fstat_method;
const char *read_chk_method;
const char *read_method;
const char *mmap_method;
const char *libart_so;
const char *libdvm_so;
const char *applicationName;
extern "C" {
    void _init(void) {}
}

struct JNIEnvExt {
    const struct JNINativeInterface* funcTable;     /* must be first */

    const struct JNINativeInterface* baseFuncTable;

    u4      envThreadId;
    Thread* self;

    /* if nonzero, we are in a "critical" JNI call */
    int     critical;

    struct JNIEnvExt* prev;
    struct JNIEnvExt* next;
};

struct RawDexFile {
    char*       cacheFileName;
    DvmDex*     pDvmDex;
};

struct DexOrJar {
    char*       fileName;
    bool        isDex;
    bool        okayToFree;
    RawDexFile* pRawDexFile;
    JarFile*    pJarFile;
    u1*         pDexMemory;
};

int doXor(char message[], size_t messageLen) {
	char key = 'a';
	int i;
	for(i = 0; i < messageLen; i++) {
		message[i] = message[i] ^ key;
	}
	return i;
}

bool isDex2oat(const char *name){
	if(strcmp(name,dex2oat) == 0){
		return true;
	}else{
		return false;
	}

}

size_t getFileLength(char *path)
{
	size_t filesize = 0;
	struct stat statbuff;
	if(stat(path, &statbuff) < 0){
		return filesize;
	}else{
		filesize = statbuff.st_size;
	}
	return filesize;
}

bool isFDPayloadDex(int fd)
{
	char s[256], name[256];
	snprintf(s, 255, "/proc/%d/fd/%d", getpid(), fd);
	memset(name, 0, sizeof(name));
	readlink(s, name, 255);

	if(payloadDexPath==NULL || payloadDexPath_secondary==NULL)
	{
		return false;
	}
	else if((strncmp(payloadDexPath,name,strlen(payloadDexPath)) == 0) || (strncmp(payloadDexPath_secondary,name,strlen(payloadDexPath_secondary)) == 0))
	{
	    LOGD("isFDPayloadDex return true");
		return true;
	}
	else
	{
	    LOGD("isFDPayloadDex return false");
		return false;
	}
}

dexInfo* findDexByFd(int fd){

	char ss[256], name[256];
    snprintf(ss, 255, "/proc/%d/fd/%d", getpid(), fd) ;
    memset(name, 0, sizeof(name));
    readlink(ss, name, 255);

    dexInfo *pointer = dexhead->next;
    while(pointer != NULL){
        LOGI("pointer->fileName:%s name:%s", pointer->fileName, name);
    	if(strncmp(pointer->fileName, name, strlen(pointer->fileName)) == 0){
#ifdef DEBUG
    		LOGI("pointer->fileName:%s name:%s", pointer->fileName, name);
#endif
    		return pointer;
    	} else {
    		pointer = pointer->next;
    	}
    }
#ifdef DEBUG
    LOGI("in findDexByFd, dex not found");
#endif
    return NULL;
}

int hook_execv(const char *name, char **argv) {
	int ret = 0;
	char tmp[512];
	if (isDex2oat(name)) {
#ifdef DEBUG
    	LOGI("is Dex2oat,exit name:%s", name);
#endif
		_exit(0);
	}

	return ori_execv(name, argv);
}

int hook_open(const char * pathname, int flags) {
	int ret = ori_open(pathname, flags);
//#ifdef DEBUG
    LOGI("in %s, pathname:%s fd:%d", "hook_open start", pathname, ret);

    if (ret == -1){
        LOGE("in hoo_open, open file fail:%s" , strerror( errno ));
    }
//#endif
	return ret;
}

int hook_create(const char * pathname, int flags) {
	int ret = ori_create(pathname, flags);
//#ifdef DEBUG
    LOGI("in %s, pathname:%s fd:%d", "hook_create start", pathname, ret);

    if (ret == -1){
        LOGE("in hook_create, open file fail:%s" , strerror( errno ));
    }
//#endif
	return ret;
}

size_t hook__read_chk(int fd, void *buf, size_t nbytes, size_t buflen){
	size_t ret;
#ifdef DEBUG
	char ss[256], name[256];
    snprintf(ss, 255, "/proc/%d/fd/%d", getpid(), fd);
    memset(name, 0, sizeof(name));
    readlink(ss, name, 255);
    LOGI("in hook_read name:%s", name);
#endif
    if(isFDPayloadDex(fd))
    {
    	off_t offset = lseek(fd, 0, SEEK_CUR);
    	lseek(fd, 0, SEEK_SET);

		dexInfo* now = findDexByFd(fd);

        void* dexBuf = malloc(now->compressed_size+1);
        memset(dexBuf, 0, now->compressed_size+1);
        memcpy(dexBuf, now->addr, now->compressed_size);

#ifdef DECRYPT
		LOGI("now->fileName:%s", now->fileName);
		printChar("dexBuf&now->addr", (char*)now->addr, 256);
#endif
		getKey(key, now->compressed_size);
    	rc4_init(s,key,sizeof(key));
    	rc4_crypt(s,(unsigned char*)dexBuf,now->compressed_size);
#ifdef DECRYPT
    	printChar("dexBufDecrypted", (char*)dexBuf, 256);
#endif
		now->uncompressed_size = uncompress((unsigned char*)dexBuf, (size_t)now->compressed_size, (unsigned char**)&now->uncompressed_addr);

#ifdef DECRYPT
        LOGI("now->uncompressed_addr:%08x", now->uncompressed_addr);
#endif
        memcpy(buf, now->uncompressed_addr, nbytes);

#ifdef DEBUG
		printChar("uncompressed dex", (char*)now->uncompressed_addr, 256);
		printChar("uncompressed dex magic", (char*)buf, 4);
#endif

        lseek(fd, offset+nbytes, SEEK_SET);
    	ret = nbytes;
    	free(dexBuf);
    }else{
    	ret = ori__read_chk(fd, buf, nbytes, buflen);
    }
	return ret;
}

size_t hook_read(int fd, void *buf, size_t count) {
	size_t ret;
#ifdef DEBUG
	char ss[256], name[256];
    snprintf(ss, 255, "/proc/%d/fd/%d", getpid(), fd);
    memset(name, 0, sizeof(name));
    readlink(ss, name, 255);
	LOGI("in hook_read start name:%s", name);
#endif
	if(isFDPayloadDex(fd))
	{
		off_t offset = lseek(fd, 0, SEEK_CUR);
		lseek(fd, 0, SEEK_SET);

		dexInfo* now = findDexByFd(fd);

		void* dexBuf = malloc(now->compressed_size+1);
		memset(dexBuf, 0, now->compressed_size+1);
		memcpy(dexBuf, now->addr, now->compressed_size);

#ifdef DECRYPT
		LOGI("now->fileName:%s", now->fileName);
		printChar("dexBuf&now->addr", (char*)now->addr, 256);
#endif
		getKey(key, now->compressed_size);
		rc4_init(s,key,sizeof(key));
		rc4_crypt(s,(unsigned char*)dexBuf,now->compressed_size);
#ifdef DECRYPT
		printChar("dexBufDecrypted", (char*)dexBuf, 256);
#endif
        now->uncompressed_size = uncompress((unsigned char*)dexBuf, (size_t)now->compressed_size, (unsigned char**)&now->uncompressed_addr);
#ifdef DECRYPT
        LOGI("now->uncompressed_addr:%08x", now->uncompressed_addr);
#endif
        memcpy(buf, now->uncompressed_addr, count);

#ifdef DEBUG
		printChar("uncompressed dex", (char*)now->uncompressed_addr, 256);
		printChar("uncompressed dex magic", (char*)buf, 4);
#endif
        lseek(fd, offset+count, SEEK_SET);
		ret = count;
		free(dexBuf);
	}else{
		ret = ori_read(fd, buf, count);
	}
	return ret;
}

/*
 *SharedPreferences的加解密，兼容性差，印象性能，暂时停用
*/

//------------------SharedPreferences start --------------------------------

size_t hook_write (int fd, const void * buf, size_t count){
    char ss[256], name[256];
    snprintf(ss, 255, "/proc/%d/fd/%d", getpid(), fd);
    memset(name, 0, sizeof(name));
    int state = readlink(ss, name, 255);
    LOGI("in hook_write start ss:%s\nname:%s\nstate:%d\nerror:%s", ss, name, state, strerror(errno));

    char* spPath = (char*)malloc(strlen("/data/data/") + strlen(packageName) + strlen("/shared_prefs") + 1);
    strcpy(spPath, "/data/data/");
    strcat(spPath, packageName);
    strcat(spPath, "/shared_prefs");

    if(strncmp(spPath,name,strlen(spPath)) != 0){
        return ori_write(fd, buf, count);
    }

    printChar("before buf", (char*)buf, count);
    LOGI("count:%d", count);

    getKey(key_sp, count);
    printChar("write key", (char*)key_sp, sizeof(key_sp));
    rc4_init(s_sp,key_sp,sizeof(key_sp));
    rc4_crypt(s_sp,(unsigned char*)buf, count);

    size_t r = ori_write(fd, buf, count);

    printChar("after buf", (char*)buf, count);
    LOGI("count:%d", count);

    return r;
}

size_t hook__write_chk (int fd, void* buf, size_t count, size_t buf_size){
    char ss[256], name[256];
    snprintf(ss, 255, "/proc/%d/fd/%d", getpid(), fd);
    memset(name, 0, sizeof(name));
    readlink(ss, name, 255);
    LOGI("in hook_write start ss%s\nname:%s", ss, name);

    char* spPath = (char*)malloc(strlen("/data/data/") + strlen(packageName) + strlen("/shared_prefs") + 1);
    strcpy(spPath, "/data/data/");
    strcat(spPath, packageName);
    strcat(spPath, "/shared_prefs");

    if(strncmp(spPath,name,strlen(spPath)) != 0){
        return ori__write_chk(fd, buf, count, buf_size);
    }

    printChar("before buf", (char*)buf, count);
    LOGI("count:%d", count);

    getKey(key_sp, count);
    printChar("write key", (char*)key_sp, sizeof(key_sp));
    rc4_init(s_sp,key_sp,sizeof(key_sp));
    rc4_crypt(s_sp,(unsigned char*)buf, count);

    size_t r = ori__write_chk(fd, buf, count, buf_size);

    printChar("after buf", (char*)buf, count);
    LOGI("count:%d", count);

    return r;
}

size_t hook_read2(int fd, void *buf, size_t count) {
    char ss[256], name[256];
    snprintf(ss, 255, "/proc/%d/fd/%d", getpid(), fd);
    memset(name, 0, sizeof(name));
    readlink(ss, name, 255);
    LOGI("in hook_read2 start name:%s", name);
    return ori_read2(fd, buf, count);
/**
    char* spPath = (char*)malloc(strlen("/data/data/") + strlen(packageName) + strlen("/shared_prefs") + 1);
    strcpy(spPath, "/data/data/");
    strcat(spPath, packageName);
    strcat(spPath, "/shared_prefs");

    if(strncmp(spPath,name,strlen(spPath)) != 0) {
        return ori_read2(fd, buf, count);
    }

    size_t r = ori_read2(fd, buf, count);

    size_t c_sp = getFileLength(name);
    printChar("before buf", (char*)buf, c_sp);
    LOGI("c_sp:%d", c_sp);

    getKey(key_sp, c_sp);
    printChar("read key", (char*)key_sp, sizeof(key_sp));
    rc4_init(s_sp,key_sp,sizeof(key_sp));
    rc4_crypt(s_sp,(unsigned char*)buf, c_sp);

    printChar("after buf", (char*)buf, c_sp);
    LOGI("c_sp:%d", c_sp);

    return r; **/
}

size_t hook__read_chk2 (int fd, void *buf, size_t nbytes, size_t buflen){
    char ss[256], name[256];
    snprintf(ss, 255, "/proc/%d/fd/%d", getpid(), fd);
    memset(name, 0, sizeof(name));
    readlink(ss, name, 255);
    LOGI("in hook_read2 start name:%s", name);

    char* spPath = (char*)malloc(strlen("/data/data/") + strlen(packageName) + strlen("/shared_prefs") + 1);
    strcpy(spPath, "/data/data/");
    strcat(spPath, packageName);
    strcat(spPath, "/shared_prefs");

    LOGI("in hook__read_chk2 path:%s", spPath);

    if(strncmp(spPath,name,strlen(spPath)) != 0) {
        return ori__read_chk2(fd, buf, nbytes, buflen);
    }

    size_t r = ori__read_chk2(fd, buf, nbytes, buflen);

    size_t c_sp = getFileLength(name);
    printChar("before buf", (char*)buf, c_sp);
    LOGI("c_sp:%d", c_sp);

    getKey(key_sp, c_sp);
    printChar("read key", (char*)key_sp, sizeof(key_sp));
    rc4_init(s_sp,key_sp,sizeof(key_sp));
    rc4_crypt(s_sp,(unsigned char*)buf, c_sp);

    printChar("after buf", (char*)buf, c_sp);
    LOGI("c_sp:%d", c_sp);

    return r;
}

//------------------SharedPreferences end--------------------------------


void* hook_mmap(void* start,size_t length,int prot,int flags,int fd,off_t offset)
{
#ifdef DEBUG

    char ss[256], name[256];
    snprintf(ss, 255, "/proc/%d/fd/%d", getpid(), fd);
    memset(name, 0, sizeof(name));
    readlink(ss, name, 255);
    LOGI("in %s start:%d length:%d dexArrayLen:%d fd:%d name:%s offset:%d", "hook_mmap start", start, length, dexLength, fd, name, offset);
#endif
	void* ret;
	if(isFDPayloadDex(fd))
	{
		dexInfo* now = findDexByFd(fd);
		ret = ori_mmap(start, now->uncompressed_size, prot, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);

#ifdef DEBUG
        if((int)ret == -1){
        	LOGI("mmap fail errno:%s",strerror(errno));
        }else{
        	LOGI("mmap succeed ret:%08X",ret);
        }
#endif
        int mret = mprotect(ret, now->uncompressed_size, PROT_READ | PROT_WRITE);
        memcpy(ret, (void*)now->uncompressed_addr, now->uncompressed_size);

#ifdef DEBUG
       // printChar("mmap dexFile ret", (char*)ret, 256);
#endif
        mret = mprotect(ret, now->uncompressed_size, prot);
        free(now->uncompressed_addr);
        now->uncompressed_addr = 0;
	}else{
		ret = ori_mmap(start, length, prot, flags, fd, offset);
	}
	return ret;
}

void *hook_mmap1(void *start, size_t length, int prot, int flags, int fd, off_t offset) {
#ifdef DEBUG
    char ss[256], name[256];
    snprintf(ss, 255, "/proc/%d/fd/%d", getpid(), fd);
    memset(name, 0, sizeof(name));
    readlink(ss, name, 255);
    LOGI("in %s start:%d length:%d dexArrayLen:%d fd:%d name:%s offset:%d", "hook_mmap start", start, length, dexLength, fd, name, offset);
#endif
    void *ret;
    if (isFDPayloadDex(fd)) {
        dexInfo *now = findDexByFd(fd);
        ret = ori_mmap(start, now->uncompressed_size, prot, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);

#ifdef DEBUG
        if((int)ret == -1){
            LOGI("mmap fail errno:%s",strerror(errno));
        }else{
            LOGI("mmap succeed ret:%08X",ret);
        }
#endif
        int mret = mprotect(ret, now->uncompressed_size, PROT_READ | PROT_WRITE);
        memcpy(ret, (void *) now->uncompressed_addr, now->uncompressed_size);

#ifdef DEBUG
        printChar("mmap dexFile ret", (char*)ret, 256);
#endif
        mret = mprotect(ret, now->uncompressed_size, prot);
        free(now->uncompressed_addr);
        now->uncompressed_addr = 0;
    } else {
        ret = ori_mmap(start, length, prot, flags, fd, offset);
    }
    return ret;
}

int hook_fstat(int filedes, struct stat *buf){
	int rc = ori_fstat(filedes, buf);
#ifdef DEBUG
	char ss[256], name[256];
	snprintf(ss, 255, "/proc/%d/fd/%d", getpid(), filedes);
	memset(name, 0, sizeof(name));
	readlink(ss, name, 255);
	LOGI("in hook_fstat start fd:%d name:%s" , filedes, name);
#endif
	if(isFDPayloadDex(filedes)){
		dexInfo* now = findDexByFd(filedes);
		if(now->uncompressed_size != 0)
			buf->st_size = now->uncompressed_size;
		else
			buf->st_size = 1024;
	}
	return rc;
}

int lookup(JNINativeMethod *table, const char *name, const char *sig, void (**fnPtrout)(u4 const *, union JValue *))
{
     int i = 0;
     while (table[i].name != NULL)
     {
         if ((strcmp(name, table[i].name) == 0)
                && (strcmp(sig, table[i].signature) == 0))
         {
             *fnPtrout = (void (*)(u4 const *, union JValue *))table[i].fnPtr;
             return 1;
         }
         i++;
     }
     return 0;
}

void Dalvik_dalvik_system_DexFile_my_openDexFileNative(const u4* args, JValue* pResult)
{
	StringObject* sourceNameObj = (StringObject*) args[0];
	char* sourceName;

	if (sourceNameObj == NULL)
	{
		return;
	}

	void *ldvm = (void*) dlopen(libdvm_so, RTLD_LAZY);

	typedef char* (*dvmCreateCstrFromStringFunc)(const StringObject* jstr);
	dvmCreateCstrFromStringFunc dvmCreateCstrFromString_ptr = (dvmCreateCstrFromStringFunc)dlsym(ldvm, dvmCreateCstr);
	sourceName = dvmCreateCstrFromString_ptr(sourceNameObj);
	if((strncmp(payloadDexPath,sourceName,strlen(payloadDexPath)) != 0) &&
					(strncmp(payloadDexPath_secondary,sourceName,strlen(payloadDexPath_secondary)) != 0) &&
					(strncmp(FULL_SECONDARY_FOLDER_NAME, sourceName, strlen(FULL_SECONDARY_FOLDER_NAME)) != 0))
	{
		Dalvik_dalvik_system_DexFile_openDexFileNative_ptr(args, pResult);
		return;
	}

	JNINativeMethod *dvm_dalvik_system_DexFile;
	void (*openDexFile)(const u4* args, JValue* pResult);

	u1 *dexByte = NULL;
	dvm_dalvik_system_DexFile = (JNINativeMethod*) dlsym(ldvm, dvm_dalvik_system_DexFile_class);
	if(0 == lookup(dvm_dalvik_system_DexFile, openDexFileMethod1, "([B)I", &openDexFile))
	{
		openDexFile = NULL;
		return;
	}else
	{
#ifdef DEBUG
		LOGI("method found ! HAVE_BIG_ENDIAN");
#endif
	}

	dexInfo *now = dexhead->next;
	while(now != NULL){
		if(strcmp(now->fileName, sourceName) == 0)
			break;
		else
			now = now->next;
	}

    dexByte = (u1*)malloc(now->compressed_size+1);
    memset(dexByte, 0, now->compressed_size+1);
    memcpy(dexByte, now->addr, now->compressed_size);

	getKey(key, now->compressed_size);
	rc4_init(s,key,sizeof(key));
	rc4_crypt(s,(unsigned char*)dexByte,now->compressed_size);

	now->uncompressed_size = uncompress((unsigned char*)dexByte, (size_t)now->compressed_size, (unsigned char**)&now->uncompressed_addr);
#ifdef DEBUG
	printChar("uncompress dex", (char*)now->uncompressed_addr, 64);
#endif
	jbyte *by = (jbyte*)now->uncompressed_addr;
	jbyteArray jDexArray = env->NewByteArray(now->uncompressed_size);
	env->SetByteArrayRegion(jDexArray, 0, now->uncompressed_size, by);
	free(dexByte);
	free(now->uncompressed_addr);

	typedef Object* (*dvmDecodeIndirectRefFunc) (Thread* self, jobject jobj);
	dvmDecodeIndirectRefFunc dvmDecodeIndirectRef_ptr = (dvmDecodeIndirectRefFunc)dlsym(ldvm, dvmDecodeRefThread);
	ArrayObject* arrObj = (ArrayObject*) dvmDecodeIndirectRef_ptr(((JNIEnvExt*)env)->self, jDexArray);

	u4 open_args[] = { (u4)arrObj };
	openDexFile(open_args, pResult);
	free(sourceName);
}

static int hashcmpDexOrJar(const void* tableVal, const void* newVal)
{
    return (int) newVal - (int) tableVal;
}

int Dalvik_dalvik_system_DexFile_yunos_openDexFileNative(VMethodEntryStruct* method, void** nativeArgs, void* selfThread, uint32_t retValue)
{
	void *libvm = dlopen("libvmkid_lemur.so", RTLD_NOW);
	typedef char* (*GetChar)(void*);
	GetChar getChar = (GetChar)dlsym(libvm, "vGetStringUTFChars");
	char *sourceName = (char*)getChar(*nativeArgs);

	if((strncmp(payloadDexPath,sourceName,strlen(payloadDexPath)) != 0) &&
					(strncmp(payloadDexPath_secondary,sourceName,strlen(payloadDexPath_secondary)) != 0) &&
					(strncmp(FULL_SECONDARY_FOLDER_NAME, sourceName, strlen(FULL_SECONDARY_FOLDER_NAME)) != 0))
	{
		return Dalvik_dalvik_system_DexFile_openDexFileNative_ptr_yunos(method, nativeArgs, selfThread, retValue);
	}

	dexInfo *now = dexhead->next;
	while(now != NULL){
		if(strcmp(now->fileName, sourceName) == 0)
			break;
		else
			now = now->next;
	}

	u4 length;
	u1* pBytes;
	RawDexFile* pRawDexFile;
	DexOrJar* pDexOrJar = NULL;

	pBytes = (u1*)malloc(now->compressed_size+1);
	memset(pBytes, 0, now->compressed_size+1);
	memcpy(pBytes, now->addr, now->compressed_size);

	getKey(key, now->compressed_size);
	rc4_init(s,key,sizeof(key));
	rc4_crypt(s,(unsigned char*)pBytes,now->compressed_size);

	now->uncompressed_size = uncompress((unsigned char*)pBytes, (size_t)now->compressed_size, (unsigned char**)&now->uncompressed_addr);
	length = now->uncompressed_size;

	free(pBytes);
	pBytes = (u1*)malloc(now->uncompressed_size+1);
	memset(pBytes, 0, now->uncompressed_size+1);
	memcpy(pBytes, now->uncompressed_addr, now->uncompressed_size);
	free(now->uncompressed_addr);
	now->uncompressed_addr = 0;

#ifdef DEBUG
	printChar("uncompress dex", (char*)pBytes, 64);
#endif
	typedef int (*dvmRawDexFileOpenArray)(unsigned char *, unsigned int, RawDexFile **);
	dvmRawDexFileOpenArray RawDexFileOpenArray = (dvmRawDexFileOpenArray)dlsym(libvm, "_Z22dvmRawDexFileOpenArrayPhjPP10RawDexFile");

	if (RawDexFileOpenArray(pBytes, length, &pRawDexFile) != 0) {
		LOGE("Unable to open in-memory DEX file");
		free(pBytes);
		return NULL;
	}

	pDexOrJar = (DexOrJar*) malloc(sizeof(DexOrJar));
	pDexOrJar->isDex = true;
	pDexOrJar->pRawDexFile = pRawDexFile;
	pDexOrJar->pDexMemory = pBytes;
	pDexOrJar->fileName = strdup("<memory>");

	void* gDvm = dlsym(libvm, "gDvm");

	typedef void* (*dvmHashTableLookup)(void*, u4 , void*, HashCompareFunc, bool doAdd);
	dvmHashTableLookup hashTableLookup = (dvmHashTableLookup)dlsym(libvm, "_Z18kvmHashTableLookupP9HashTablejPvPFiPKvS3_Eb");

	u4 hash = (u4) pDexOrJar;

	HashTable *table = *(HashTable**)(gDvm);

	pthread_mutex_lock(&table->lock);
	void* result = hashTableLookup(table, hash, pDexOrJar, hashcmpDexOrJar, true);
	pthread_mutex_unlock(&table->lock);

	if (result != pDexOrJar) {
		LOGE("Pointer has already been added?");
		return NULL;
	}

	pDexOrJar->okayToFree = true;

	free(sourceName);
	*((int*)retValue) = ((int)pDexOrJar);

	return (int)pDexOrJar;
}

void printChar(const char *msg, char * src, int len){
#ifdef DEBUG
	char * a = (char*)malloc(512);
	int n;
	if (len > 256){
	    n = 256;
	} else{
	    n = len;
	}
	int i;
	for(i=0; i<n; i++){
	    sprintf((a + i*2), "%02x", (char)(*(src + i)));
	}
	LOGI("%s hex:%s\n", msg, a);
#endif
}

extern "C" void * getDexAddr(void *baseAddr, uint32_t *len) {
	char buf[sizeof(Elf32_Ehdr) + 14 * sizeof(Elf32_Phdr)];
	memcpy(buf, baseAddr, sizeof(uint32_t));
	uint32_t *magic = (uint32_t *)buf;
#ifdef DEBUG
	LOGI("magic:%08x", magic);
#endif
	if (*magic != ELF_MAGIC) {
		LOGE("can't resolve this type of file.");
		exit(1);
	}

	Elf32_Ehdr *ehdr;
	memcpy(buf, baseAddr, sizeof(buf));
	ehdr = (Elf32_Ehdr *)buf;

	Elf32_Phdr *phdr = (Elf32_Phdr *)((unsigned int)baseAddr + ehdr->e_phoff);

	unsigned offset = 0;
	int n = 0;
	for (int i = 0; i < ehdr->e_phnum; i++) {
		if (phdr->p_type == PT_LOAD) {
			++n;
			if (n == 2) {
				offset = (phdr->p_paddr == phdr->p_vaddr) ? 0 : phdr->p_paddr;
#ifdef DEBUG
				LOGD("in getDexAddr: offset: %x", offset);
#endif
				break;
			}
		}
		phdr++;
	}

	unsigned char *data = 0;

	if (offset) {
		data = (unsigned char *)baseAddr + offset;
		*len = *(uint32_t *)data;//len + dexArray
#ifdef DECRYPT
		LOGD("in getDexAddr, base:%08x, offset:%x, data:%08x, len: %x",
				baseAddr, offset, data, *len);
		printChar("getDexAddr data", (char*)data, 32);
		printChar("getDexAddr data", (char*)(data + 4 + *len - 32), 32);
#endif
		data += 4;
	} else {
#ifdef DEBUG
		LOGD("in getDexAddr: offset error.");
#endif
	}

	return (void *) data;
}

jobject hook_getDex(JNIEnv* env, jclass thiz){
	return NULL;
}

void createNewFile(JNIEnv* env, char* dexPath){
	jstring apkFileName = env->NewStringUTF(dexPath);

    jclass cls_File = env->FindClass("java/io/File");
    jmethodID mid_file_init = env->GetMethodID(cls_File, "<init>", "(Ljava/lang/String;)V");
    jobject dexFile = env->NewObject(cls_File, mid_file_init, apkFileName);

    jboolean isExists = callBooleanMethod(env, cls_File, dexFile, "exists", "()Z");
    if(!isExists){
    	callBooleanMethod(env, cls_File, dexFile, "createNewFile", "()Z");
    }
}

void mkdirs(JNIEnv* env, char* dirPath){
	jstring FileName = env->NewStringUTF(dirPath);

    jclass cls_File = env->FindClass("java/io/File");
    jmethodID mid_file_init = env->GetMethodID(cls_File, "<init>", "(Ljava/lang/String;)V");
    jobject dexFile = env->NewObject(cls_File, mid_file_init, FileName);

    jboolean isExists = callBooleanMethod(env, cls_File, dexFile, "exists", "()Z");
    if(!isExists){
    	callBooleanMethod(env, cls_File, dexFile, "mkdirs", "()Z");
    }
}

dexInfo* findAllDexInfo(JNIEnv* env, jstring dexPath, jstring odexPath, jint sdk_int){
	char sdkchar[16];
    __system_property_get("ro.build.version.sdk", sdkchar);
    int sdkVersion = atoi(sdkchar);

	uint32_t totalDex = 0;
	void* compressedAddr;
	if (sdkVersion < 21){
		compressedAddr = get_dex_in_mem_4("classes.dex", &totalDex);
	}else if (sdkVersion < 23){
		compressedAddr = get_dex_in_mem_4("base.apk@classes.dex", &totalDex);
	} else if (sdkVersion < 26) {
	    LOGI("start find_dex_in_mem Android 7.0、7.1");
		compressedAddr = get_dex_in_mem_4("base.odex", &totalDex);
	} else {
	    compressedAddr = get_dex_in_mem_4("base.vdex", &totalDex);
	}
   LOGI("finish find_dex_in_mem ");
	void* headAddr = malloc(sizeof(dexInfo));
	memset(headAddr, 0, sizeof(dexInfo));
	dexhead = (dexInfo*)headAddr;
	dexInfo* now = dexhead;
#ifdef DEBUG
	LOGI("total dex number:%d", totalDex);
#endif
	char dexName[] = "/classesX.dex";

	char *c_dexPath = (char*)env->GetStringUTFChars(dexPath, 0);
	char *c_odexPath = (char*)env->GetStringUTFChars(odexPath, 0);

	int i;
	for(i=0; i<totalDex; i++){
		dexInfo* info = (dexInfo*)malloc(sizeof(dexInfo));

		uint32_t *magic;
        magic = (uint32_t*)compressedAddr;
        uint32_t compressedLen = *magic;
        magic++;

        if(sdk_int <= 20 && i > 0){
        	dexName[8] = (i+1) + '0';
        	const char * SECONDARY_FOLDER_NAME = "/code_cache/secondary-dexes";
			FULL_SECONDARY_FOLDER_NAME = (char*)malloc(strlen(payloadDexPath) - strlen(path3) + strlen(SECONDARY_FOLDER_NAME) + 1);
			memset(FULL_SECONDARY_FOLDER_NAME, 0, strlen(payloadDexPath) - strlen(path3) + strlen(SECONDARY_FOLDER_NAME) + 1);
			strncpy(FULL_SECONDARY_FOLDER_NAME, payloadDexPath, strlen(payloadDexPath) - strlen(path3));
			strcat(FULL_SECONDARY_FOLDER_NAME, SECONDARY_FOLDER_NAME);
			mkdirs(env, FULL_SECONDARY_FOLDER_NAME);

			info->fileName = (char*) malloc(strlen(FULL_SECONDARY_FOLDER_NAME) + strlen(dexName) + 1);
			memset(info->fileName, 0, strlen(FULL_SECONDARY_FOLDER_NAME) + strlen(dexName) + 1);
			strcpy(info->fileName, FULL_SECONDARY_FOLDER_NAME);
			strcat(info->fileName, dexName);
			createNewFile(env, info->fileName);

			info->addr = (void*) magic;
            info->compressed_size = compressedLen;
            info->uncompressed_size = 0;

        }else{
        	dexName[8] = i + '0';
            info->fileName = (char*) malloc(strlen(payloadDexPath) + strlen(dexName) + 1);
			memset(info->fileName, 0, strlen(payloadDexPath) + strlen(dexName) + 1);
			strcpy(info->fileName, payloadDexPath);
			strcat(info->fileName, (dexName+1));
			createNewFile(env, info->fileName);

			char* dexFileName = (char*) malloc(strlen(c_dexPath) + strlen(dexName) + 1);
			memset(dexFileName, 0, strlen(c_dexPath) + strlen(dexName) + 1);
			strcpy(dexFileName, c_dexPath);
			strcat(dexFileName, dexName);
			createNewFile(env, dexFileName);

			char* odexFileName = (char*) malloc(strlen(c_odexPath) + strlen(dexName) + 1);
			memset(odexFileName, 0, strlen(c_odexPath) + strlen(dexName) + 1);
			strcpy(odexFileName, c_odexPath);
			strcat(odexFileName, dexName);
			createNewFile(env, odexFileName);

			info->addr = (void*) magic;
			info->compressed_size = compressedLen;
			info->uncompressed_size = 0;

			LOGD("Dexinfo->fileName:%s\ndexFileName:%s\nodexFileName:%s\npayloadDexPath_secondary:%s", info->fileName, dexFileName, odexFileName, payloadDexPath_secondary);

			free(dexFileName);
			free(odexFileName);
        }

		now->next = info;
		now = now->next;

		compressedAddr = (char*)compressedAddr + compressedLen + 4;
	}
	now->next = NULL;
	return dexhead;
}

extern "C" void loadDexClass(JNIEnv* env, jobject thiz,
										jstring pName, jstring dexPath,
										jstring odexPath, jstring libPath){

	//==========================================================================
	setPackageName(env, thiz, pName);
	//==========================================================================

	jclass cls_VERSION = env->FindClass("android/os/Build$VERSION");
    jfieldID fID = env->GetStaticFieldID(cls_VERSION, "SDK_INT", "I");
    jint sdk_int = env->GetStaticIntField(cls_VERSION, fID);

     // 配置动态加载环境:
	jclass cls_ActivityThread = env->FindClass("android/app/ActivityThread");
	jobject currentActivityThread = callStaticObjectMethod(env, cls_ActivityThread,
                                                               "currentActivityThread",
                                                               "()Landroid/app/ActivityThread;");

    jclass cls_Application = env->FindClass("android/content/ContextWrapper");
    jobject mClassLoader = callObjectMethod(env, cls_Application, thiz, "getClassLoader", "()Ljava/lang/ClassLoader;");

    jclass cls_proxyApplication = env->FindClass("com/wknight/dexshell/ProxyApplication");

	//====================find dex files in stub start============================
    dexhead = findAllDexInfo(env, dexPath, odexPath, sdk_int);
    dexInfo* nowDex = dexhead->next;
    //====================find dex files in stub end============================

    LOGD("nowDex->fileName:%s", nowDex->fileName);

	jclass cls_DexClassLoader = env->FindClass("dalvik/system/DexClassLoader");
	jmethodID mid_dexLoader_init = env->GetMethodID(cls_DexClassLoader, "<init>",
                                  "(Ljava/lang/String;Ljava/lang/String;"
                                  "Ljava/lang/String;Ljava/lang/ClassLoader;)V");
	if(!mid_dexLoader_init){
		LOGI("dexClassLoader method id null");
	}

	jobject dexClassLoader = mClassLoader;
	while(nowDex != NULL){
		jstring apkFileName = env->NewStringUTF(nowDex->fileName);
		dexClassLoader = env->NewObject(cls_DexClassLoader, mid_dexLoader_init,
        							apkFileName, odexPath, libPath, dexClassLoader);

        callVoidMethod(env, cls_proxyApplication, thiz, "addPathList", "(Ljava/lang/ClassLoader;Ldalvik/system/DexClassLoader;)V",
            						mClassLoader, dexClassLoader);

        nowDex = nowDex->next;
	}

    jobject weakReference = NULL;

    if (sdk_int < 19) {
    	jobject mPackages = getObjectField(env, cls_ActivityThread,
            				"mPackages", "Ljava/util/HashMap;", currentActivityThread);

		jclass cls_hashMap = env->FindClass("java/util/HashMap");
        weakReference = callObjectMethod(env, cls_hashMap, mPackages, "get", "(Ljava/lang/Object;)Ljava/lang/Object;", pName);

    } else {
    	jobject mPackages = getObjectField(env, cls_ActivityThread,
                    				"mPackages", "Landroid/util/ArrayMap;", currentActivityThread);

        jclass cls_hashMap = env->FindClass("android/util/ArrayMap");
        weakReference = callObjectMethod(env, cls_hashMap, mPackages, "get", "(Ljava/lang/Object;)Ljava/lang/Object;", pName);
    }

    jclass cls_weakReference = env->FindClass("java/lang/ref/WeakReference");
    jobject referenceObj = callObjectMethod(env, cls_weakReference, weakReference, "get", "()Ljava/lang/Object;");

	jclass cls_LoadedApk = env->FindClass("android/app/LoadedApk");
	fID = env->GetFieldID(cls_LoadedApk, "mClassLoader", "Ljava/lang/ClassLoader;");
	env->SetObjectField(referenceObj, fID, dexClassLoader);
}


extern "C" void setPackageName(JNIEnv* env, jobject thiz, jstring pName)
{
//	char *packageName;
	jclass clsstring = env->FindClass("java/lang/String");
	jstring strencode = env->NewStringUTF("utf-8");
	jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
	jbyteArray barr = (jbyteArray)env->CallObjectMethod(pName, mid, strencode);
	jsize alen = env->GetArrayLength(barr);
	jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
	if (alen > 0)
	{
		packageName = (char*)malloc(alen + 1);    //packageName = char[alen+1]
		memcpy(packageName, ba, alen);
		packageName[alen] = 0;
	}
	env->ReleaseByteArrayElements(barr, ba, 0);

	payloadDexPath = (char*)malloc(strlen(path1) + strlen(packageName) + strlen(path3) + 1);
	strcpy(payloadDexPath, path1);
	strcat(payloadDexPath, packageName);
	strcat(payloadDexPath, path3);
#ifdef DEBUG
	LOGI("%s", payloadDexPath);
#endif
	payloadDexPath_secondary = (char*)malloc(strlen(path2) + strlen(packageName) + strlen(path3) + 1);
	strcpy(payloadDexPath_secondary, path2);
	strcat(payloadDexPath_secondary, packageName);
	strcat(payloadDexPath_secondary, path3);
#ifdef DEBUG
	LOGI("%s", payloadDexPath_secondary);
#endif
}


jint rsaVerify(JNIEnv *env, jobject obj, jbyteArray rsaArray1, jint rsalen1, jbyteArray rsaArray2, jint rsalen2){
	unsigned char *rsa1;
	unsigned char *rsa2;
	int rsaLen = rsalen1;
	int rsaLen2 = rsalen2;
#ifdef CERT
	LOGI("rsaVerify begin, rsaLen:%d, rsaLen2:%d", rsaLen, rsaLen2);
#endif
	char* tmpRsa = (char *)env->GetByteArrayElements(rsaArray1, 0);
	rsa2 = (unsigned char *)env->GetByteArrayElements(rsaArray2, 0);

	rsaLen = uncompress((unsigned char*)tmpRsa, (size_t)rsaLen, &rsa1);
#ifdef CERT
	printChar("tmpRsa", tmpRsa, 64);
	LOGI("after compress, rsaLen:%d, rsaLen2:%d", rsaLen, rsaLen2);
	printChar("rsa1", (char*)rsa1, 64);
	printChar("rsa2", (char*)rsa2, 64);
#endif
	if(rsaLen != rsaLen2) return -1;

	int i;
	for(i=0; i<rsaLen; i++){
		if(*(rsa1 +i) != *(rsa2 + i)) return -1;
	}

	return 0;
}

static void handleString(){
	size_t messageLen = sizeof(objectEncrypted);
    doXor(objectEncrypted, messageLen);
    object = objectEncrypted;

    messageLen = sizeof(path1Encrypted);
    doXor(path1Encrypted, messageLen);
    path1 = path1Encrypted;

    messageLen = sizeof(path2Encrypted);
    doXor(path2Encrypted, messageLen);
    path2 = path2Encrypted;

    messageLen = sizeof(path3Encrypted);
    doXor(path3Encrypted, messageLen);
    path3 = path3Encrypted;

    messageLen = sizeof(dex2oatEncrypted);
    doXor(dex2oatEncrypted, messageLen);
    dex2oat = dex2oatEncrypted;

    messageLen = sizeof(dalvik_system_DexFile_classEncrypted);
    doXor(dalvik_system_DexFile_classEncrypted, messageLen);
    dalvik_system_DexFile_class = dalvik_system_DexFile_classEncrypted;

    messageLen = sizeof(dvm_dalvik_system_DexFile_classEncrypted);
    doXor(dvm_dalvik_system_DexFile_classEncrypted, messageLen);
    dvm_dalvik_system_DexFile_class = dvm_dalvik_system_DexFile_classEncrypted;

    messageLen = sizeof(openDexFileMethod1Encrypted);
    doXor(openDexFileMethod1Encrypted, messageLen);
    openDexFileMethod1 = openDexFileMethod1Encrypted;

    messageLen = sizeof(openDexFileMethod2Encrypted);
    doXor(openDexFileMethod2Encrypted, messageLen);
    openDexFileMethod2 = openDexFileMethod2Encrypted;

    messageLen = sizeof(dvmDecodeRefThreadEncrypted);
    doXor(dvmDecodeRefThreadEncrypted, messageLen);
    dvmDecodeRefThread = dvmDecodeRefThreadEncrypted;

    messageLen = sizeof(dvmCreateCstrEncrypted);
    doXor(dvmCreateCstrEncrypted, messageLen);
    dvmCreateCstr = dvmCreateCstrEncrypted;

    messageLen = sizeof(execv_methodEncrypted);
    doXor(execv_methodEncrypted, messageLen);
    execv_method = execv_methodEncrypted;

    messageLen = sizeof(fstat_methodEncrypted);
    doXor(fstat_methodEncrypted, messageLen);
    fstat_method = fstat_methodEncrypted;

    messageLen = sizeof(read_chk_methodEncrypted);
    doXor(read_chk_methodEncrypted, messageLen);
    read_chk_method = read_chk_methodEncrypted;

    messageLen = sizeof(read_methodEncrypted);
    doXor(read_methodEncrypted, messageLen);
    read_method = read_methodEncrypted;

    messageLen = sizeof(mmap_methodEncrypted);
    doXor(mmap_methodEncrypted, messageLen);
    mmap_method = mmap_methodEncrypted;

    messageLen = sizeof(libart_soEncrypted);
    doXor(libart_soEncrypted, messageLen);
    libart_so = libart_soEncrypted;

	messageLen = sizeof(libdvm_soEncrypted);
    doXor(libdvm_soEncrypted, messageLen);
    libdvm_so = libdvm_soEncrypted;

    messageLen = sizeof(applicationNameEncrypted);
    doXor(applicationNameEncrypted, messageLen);
    applicationName = applicationNameEncrypted;
}

static JNINativeMethod getMethods[] =
{
	{"loadDexClass", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", (void*)loadDexClass},
	{"rsaVerify", "([BI[BI)I", (void*)rsaVerify},
	{"onCreate", "()V", (void*)onCreate}
};

static int registerNativeMethods(JNIEnv* env, const char* className, JNINativeMethod* gMethods, int numMethods)
{
	jclass clazz;
	clazz = env->FindClass(className);
	if (clazz == NULL)
	{
		return 0;
	}
	if (env->RegisterNatives(clazz, gMethods, numMethods) < 0)
	{
		return 0;
	}
	return 1;
}

static int registerNatives(JNIEnv* env)
{
    if (!registerNativeMethods(env, applicationName, getMethods, sizeof(getMethods) / sizeof(getMethods[0])))
    		return JNI_FALSE;

	return JNI_TRUE;
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void *reserved)
{
#ifdef ANTIDEBUG
	last = time((time_t*)NULL);
#else
	LOGI("%s", "in jni_onload!!!");
#endif
	gvm = vm;
	jint result = -1;
	if(vm->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK)
		return -1;

	handleString();

	char sdkchar[16];
    __system_property_get("ro.build.version.sdk", sdkchar);
    int sdkVersion = atoi(sdkchar);

	if(!registerNatives(env))
		return -1;

#ifdef ANTIDEBUG

#ifdef __arm__
	if (sdkVersion > 16){
		anti_break("/system/bin/linker");
	}
#endif

	if (sdkVersion > 19){
		anti_port();
	}

	if (sdkVersion > 18){
		anti_watcher(sdkVersion);
	}
#else
	LOGI("start anti debug");
#endif

	hookAssetManagerRead(sdkVersion);

#ifdef DEBUG
	LOGI("in jni_onload version:%d", sdkVersion);
#endif

	if(sdkVersion>20)
	{
		void* libArt = NULL;
		void* libandroidfw = NULL;
		if(sdkVersion>23){
			libArt = getTargetSI_7(libart_so);
			libandroidfw = getTargetSI_7("libandroidfw.so");
		}else{
			libArt = getTargetSI_6(libart_so);
			libandroidfw = getTargetSI_6("libandroidfw.so");
		}
		if(libandroidfw == NULL){
		LOGI("%s", "can not find libandroidfw");
		}
		if(libArt == NULL)
		{
#ifdef DEBUG
			LOGI("%s", "can not find libart");
#endif
			return -1;
		}

        if (sdkVersion>23) {
        	replaceFunc_7(libArt, execv_method, (void *)hook_execv, (void **)&ori_execv, sdkVersion);
        	if(sdkVersion > 25){
                replaceFunc_7(libArt, read_method, (void *)hook_read, (void **)&ori_read, sdkVersion);
            } else {
                replaceFunc_7(libArt, read_chk_method, (void *)hook__read_chk, (void **)&ori__read_chk, sdkVersion);
            }
            replaceFunc_7(libArt, mmap_method, (void *)hook_mmap, (void **)&ori_mmap, sdkVersion);
            replaceFunc_7(libArt, fstat_method, (void *)hook_fstat, (void **)&ori_fstat, sdkVersion);
            replaceFunc_7(libandroidfw, "open", (void *) hook_open,(void **) &ori_open, sdkVersion);
            replaceFunc_7(libandroidfw, "read", (void *)hook_read2, (void **)&ori_read2, sdkVersion);
            replaceFunc_7(libandroidfw, "mmap", (void *)hook_mmap1, (void **)&ori_mmap1, sdkVersion);


#ifdef SHAREPRE //SharedPreferences 停用
            void* libruntime = getTargetSI_7("libjavacore.so");
            if(libruntime == NULL) {
                LOGI("can not find libjavacore.so");
            }
            if(sdkVersion > 25){
                replaceFunc_7(libruntime, "read", (void *)hook_read2, (void **)&ori_read2, sdkVersion);
                replaceFunc_7(libruntime, "write", (void *)hook_write, (void **)&ori_write, sdkVersion);
            }else{
                replaceFunc_7(libruntime, "__read_chk", (void *)hook__read_chk2, (void **)&ori__read_chk2, sdkVersion);
                replaceFunc_7(libruntime, "__write_chk", (void *)hook__write_chk, (void **)&ori__write_chk, sdkVersion);
            }
#endif

       } else {
        	replaceFunc_6(libArt, execv_method, (void *)hook_execv, (void **)&ori_execv, sdkVersion);
            replaceFunc_6(libArt, read_method, (void *)hook_read, (void **)&ori_read, sdkVersion);
            replaceFunc_6(libArt, mmap_method, (void *)hook_mmap, (void **)&ori_mmap, sdkVersion);
            replaceFunc_6(libArt, fstat_method, (void *)hook_fstat, (void **)&ori_fstat, sdkVersion);
            replaceFunc_6(libandroidfw, "open", (void *) hook_open,(void **) &ori_open, sdkVersion);
            replaceFunc_6(libandroidfw, "read", (void *)hook_read2, (void **)&ori_read2, sdkVersion);
            replaceFunc_6(libandroidfw, "mmap", (void *)hook_mmap1, (void **)&ori_mmap1, sdkVersion);

#ifdef SHAREPRE   //SharedPreferences 停用

            void* libruntime = getTargetSI_6("libjavacore.so");
            if(libruntime == NULL){
                LOGI("can not find libjavacore.so");
            }
            replaceFunc_6(libruntime, "read", (void *)hook_read2, (void **)&ori_read2, sdkVersion);
            replaceFunc_6(libruntime, "write", (void *)hook_write, (void **)&ori_write, sdkVersion);
#endif
        }
        LOGI("jni onload complete replaceFunc");
	}
	else
	{
		jmethodID JJ;
		jclass jf = env->FindClass(dalvik_system_DexFile_class);
		if(sdkVersion<19)
		{
			//4.0-4.3 hook openDexFile
			openDexFileNative_med = (Method*)env->GetStaticMethodID(jf, openDexFileMethod1, "(Ljava/lang/String;Ljava/lang/String;I)I");
			openDexFileNative_med2 = (VMethodEntryStruct*)env->GetStaticMethodID(jf, openDexFileMethod1, "(Ljava/lang/String;Ljava/lang/String;I)I");
		}
		else
		{
			//4.4 hook openDexFileNative
			openDexFileNative_med = (Method*)env->GetStaticMethodID(jf, openDexFileMethod2, "(Ljava/lang/String;Ljava/lang/String;I)I");
			openDexFileNative_med2 = (VMethodEntryStruct*)env->GetStaticMethodID(jf, openDexFileMethod2, "(Ljava/lang/String;Ljava/lang/String;I)I");

			JNINativeMethod gMethod[] = {
                        {"getDex", "()Lcom/android/dex/Dex;", (void *)hook_getDex},
            };

            jclass clazzTarget = env->FindClass("java/lang/Class");

            if (env->RegisterNatives(clazzTarget, gMethod, 1) < 0) {
            	LOGE("RegisterNativers error, errorno:%s", strerror(errno));
            	return -1;
            }
		}

		if(openDexFileNative_med == NULL)
		{
#ifdef DEBUG
			LOGI("openDexFileNative_med,Can not find open");
#endif
		}
		else
		{
#ifdef DEBUG
			LOGI("openDexFileNative_med,Find open");
#endif
		}

		u4 **addr = (u4**)openDexFileNative_med->insns;

		if(sdkVersion >= 14)
		{
			void *libvm = dlopen("libvmkid_lemur.so", RTLD_NOW);
			if(libvm){
				Dalvik_dalvik_system_DexFile_openDexFileNative_ptr_yunos = (Dalvik_dalvik_system_DexFile_openDexFileNative_yunos)openDexFileNative_med2->point5;
				openDexFileNative_med2->point5 = (void*)&Dalvik_dalvik_system_DexFile_yunos_openDexFileNative;
			} else if (openDexFileNative_med->insns == 0) {
				Dalvik_dalvik_system_DexFile_openDexFileNative_ptr = (Dalvik_dalvik_system_DexFile_openDexFileNative_func)openDexFileNative_med->nativeFunc;
				openDexFileNative_med->nativeFunc = (void (*)(const unsigned int *, JValue *, const Method *, Thread *))Dalvik_dalvik_system_DexFile_my_openDexFileNative;
			} else {
				Dalvik_dalvik_system_DexFile_openDexFileNative_ptr = (Dalvik_dalvik_system_DexFile_openDexFileNative_func)addr[10];
				addr[10] = (u4*)Dalvik_dalvik_system_DexFile_my_openDexFileNative;
			}
		}
	}

#ifdef ANTIDEBUG
	int now = time((time_t*)NULL);
	if((now - last) > 3000){
		LOGD("anti debug over time ,kill");
		int pid = getpid();
		int ret = kill(pid, SIGKILL);
    }
#else
	LOGI("anti debug checkpoint");
#endif

	return JNI_VERSION_1_4;
}
