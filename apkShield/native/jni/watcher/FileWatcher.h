#ifndef FILEWATCHER_H_
#define FILEWATCHER_H_

#include <string.h>
#include <sys/inotify.h>
#include <unistd.h>
#include <stdbool.h>
#include <stdio.h>
#include <errno.h>
#include "dalvik/common.h"
#include "Signal.h"

#define LEN 10

typedef struct queue_entry {
    struct queue_entry * next_ptr;
    struct inotify_event inot_ev;
} *queue_entry_t;

typedef struct queue_struct {
    struct queue_entry * head;
    struct queue_entry * tail;
} *queue_t;

typedef struct FileWatcher {
    int fd;
    int pid;
    int child;
    int watched_item_number;

    int timeout;
    Signal *sinfo;

    queue_t queue;
    char **pathes;
    const char *files[LEN];
    bool trigger;

    char *tracerPath;
    int sdkVersion;
} FileWatcher;

FileWatcher *init_watcher(int pid);
bool start(FileWatcher *watcher);
void remove_watchers(FileWatcher *watcher);

#endif // FILEWATCHER_H_
