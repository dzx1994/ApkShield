#include <jni.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdlib.h>
#include <pthread.h>
#include <sys/ptrace.h>
#include <signal.h>
#include <sys/mman.h>
#include <elf.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdint.h>
#include <errno.h>
#include "watcher/FileWatcher.h"
#include "watcher/Signal.h"
#include "dalvik/common.h"

#ifdef __cplusplus
extern "C" {
#endif
void callback();
//void anti_sigtrap();
void anti_watcher(int sdkVersion);
int anti_break(const char* soname);
int anti_port();
#ifdef __cplusplus
}
#endif