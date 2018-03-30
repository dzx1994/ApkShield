#include "FileWatcher.h"
#include "select.h"

#define EVENT_SIZE (sizeof(struct inotify_event))
#define EVENT_BUFF_LEN (1024 * EVENT_SIZE)
#define BUFF_LEN 1024 * 4
#define PATH_LEN 1024

void processInotifyEvent(FileWatcher *watcher);
void addWatches(FileWatcher *watcher);

void queue_enqueue (queue_entry_t d, queue_t q) {
    d->next_ptr = NULL;
    if (q->tail) {
        q->tail->next_ptr = d;
        q->tail = d;
    } else {
        q->head = q->tail = d;
    }
}

int queue_empty (queue_t q) {
    return q->head == NULL;
}

queue_t queue_create () {
    queue_t q;
    q = (queue_t) malloc(sizeof (struct queue_struct));
    if (q == NULL)
        exit(-1);

    q->head = q->tail = NULL;
    return q;
}

void queue_destroy (queue_t q) {
    if (q != NULL) {
        while (q->head != NULL) {
            queue_entry_t next = q->head;
            q->head = next->next_ptr;
            next->next_ptr = NULL;
            free(next);
        }
        q->head = q->tail = NULL;
        free(q);
    }
}

queue_entry_t  queue_dequeue (queue_t q) {
    queue_entry_t first = q->head;
    if (first) {
        q->head = first->next_ptr;
        if (q->head == NULL) {
            q->tail = NULL;
        }
        first->next_ptr = NULL;
    }
    return first;
}

FileWatcher *init_watcher(int pid) {
#ifdef DEBUG
    LOGI("in init watcher.");
#endif

    FileWatcher *watcher = (FileWatcher *)malloc(sizeof(FileWatcher));

    int fd = inotify_init();
    if (fd == -1) {
        LOGE("inotify_init error [errno: %d, desc: %s", errno, strerror(errno));
        free(watcher);
        return NULL;
    }

    char *tracerPath = (char *)malloc(30);
    sprintf(tracerPath, "/proc/%d/status", pid);

    watcher->fd = fd;
    watcher->pid = pid;
    watcher->child = getpid();
    watcher->watched_item_number = 0;
    watcher->trigger = true;
    watcher->timeout = 2;
    watcher->sinfo = NULL;
    watcher->sdkVersion = 0;
    watcher->queue = queue_create();
    watcher->tracerPath = tracerPath;

    return watcher;
}

void remove_watchers(FileWatcher *watcher) {
    free(watcher->tracerPath);
    queue_destroy(watcher->queue);
    if (watcher->sinfo != NULL) {
        free(watcher->sinfo);
    }
    free(watcher);
}

bool destroyWatches(FileWatcher *watcher) {
#ifdef DEBUG
    LOGD("terminating...");
    LOGD("watches removed.");
#endif

    close(watcher->fd);
    return false;
}

bool start(FileWatcher *watcher) {
#ifndef DEBUG
    if (watcher->sdkVersion < 24)
#endif
        addWatches(watcher);
    processInotifyEvent(watcher);
    return destroyWatches(watcher);
}

int addWatch(int fd, char *path) {
    int wd = inotify_add_watch(fd, path, IN_ALL_EVENTS);
    if (wd < 0) {
        LOGW("Cannot add watch to file or dir %s.", path);
        LOGW("error: %s", strerror(errno));
    } else {
        LOGI("Add watch success. Path: %s, fd: %d.", path, fd);
    }
    return wd;
}

void addWatches(FileWatcher *watcher) {
    int wd = 0;

#ifdef DEBUG
    LOGD("start adding watches");
#endif

    char *path[2];
    char **pathes = watcher->pathes;

    int i;
    for (i = 0; pathes[i] != NULL; i++) {
        char tmp[PATH_LEN];

        path[0] = (char *)malloc(PATH_LEN);
        strcpy(tmp, "/proc/%d/");
        sprintf(path[0], strcat(tmp, pathes[i]), watcher->pid);

        wd = addWatch(watcher->fd, path[0]);

        if (wd > 0) {
            watcher->watched_item_number++;
        }
        watcher->files[wd - 1] = path[0];

        path[1] = (char *)malloc(PATH_LEN);
        strcpy(tmp, "/proc/%d/task/%d/");
        sprintf(path[1], strcat(tmp, pathes[i]), watcher->pid, watcher->pid);

        wd = addWatch(watcher->fd, path[1]);
        if (wd > 0) {
            watcher->watched_item_number++;
        }
        watcher->files[wd - 1] = path[1];
    }
}

int readEvents(FileWatcher *watcher) {
    char buffer[EVENT_BUFF_LEN];
    ssize_t event_len;
    struct inotify_event *pevent;
    queue_entry_t event;
    size_t event_size, q_event_size;
    int count = 0;

    event_len = read(watcher->fd, buffer, EVENT_BUFF_LEN);
    if (event_len < 0) {
        return (int) event_len;
    }
    size_t buffer_i = 0;
    while (buffer_i < event_len) {
        pevent = (struct inotify_event *) &buffer[buffer_i];
        event_size = offsetof(struct inotify_event, name) + pevent->len;
        q_event_size = offsetof(struct queue_entry, inot_ev.name) + pevent->len;
        event = (queue_entry_t) malloc(q_event_size);
        memmove(&(event->inot_ev), pevent, event_size);
        queue_enqueue(event, watcher->queue);
        buffer_i += event_size;
        count++;
    }

    return count;
}

void handleEvent(queue_entry_t event, FileWatcher *watcher) {
    char *cur_event_filename = NULL;
    const char *cur_event_file_or_dir;
    int cur_event_wd = event->inot_ev.wd;
    unsigned long flags;

    if (event->inot_ev.len) {
        cur_event_filename = event->inot_ev.name;
    }
    if (event->inot_ev.mask & IN_ISDIR) {
        cur_event_file_or_dir = "Dir";
    } else {
        cur_event_file_or_dir = "File";
    }
    flags = event->inot_ev.mask &
            ~(IN_ALL_EVENTS | IN_UNMOUNT | IN_Q_OVERFLOW | IN_IGNORED);

    const char *filename = watcher->files[cur_event_wd - 1];

    switch (event->inot_ev.mask &
            (IN_ALL_EVENTS | IN_UNMOUNT | IN_Q_OVERFLOW | IN_IGNORED)) {
        case IN_ACCESS:
//            LOGI("ACCESS: %s \"%s%s\"\n",
//                 cur_event_file_or_dir, filename, cur_event_filename);
            break;
        default:
//            LOGI("UNKNOWN EVENT \"%X\" OCCURRED for file \"%s\"\n",
//                 event->inot_ev.mask, filename);
            break;
    }

    /* If any flags were set other than IN_ISDIR, report the flags */
    if (flags & (~IN_ISDIR)) {
        flags = event->inot_ev.mask;
        LOGD("Flags=%lX\n", flags);
    }
}

void handleEvents(FileWatcher *watcher) {
    queue_entry_t event;
    while (!queue_empty(watcher->queue)) {
        event = queue_dequeue(watcher->queue);
        handleEvent(event, watcher);
        free(event);
    }
}

int getTracerpid(const char *path, int child) {
    char buf[4 * 1024];
    FILE *fp;
    int tracerpid = 0;
    const char *needle = "TracerPid:";

    fp = fopen(path, "r");
    if (fp == NULL) {
        LOGE("status open failed:[error:%d, desc:%s]", errno, strerror(errno));
        return -1;
    }

    while (fgets(buf, 4 * 1024, fp)) {
        if (!strncmp(buf, needle, strlen(needle))) {
            sscanf(buf, "TracerPid: %d", &tracerpid);
            if (tracerpid > 0 && tracerpid != child) {
                fclose(fp);
                return tracerpid;
            } else {
                break;
            }
        }
    }
    fclose(fp);

    return 0;
}

void processInotifyEvent(FileWatcher *watcher) {
    int ret;
    while (watcher->trigger && watcher->watched_item_number >= 0) {
        struct timeval time_to_wait;
        time_to_wait.tv_sec = watcher->timeout;
        time_to_wait.tv_usec = 0;

        int pid = getTracerpid(watcher->tracerPath, watcher->child);
        if (pid > 0) {
            LOGD("%d being traced, tracer pid is %d. Terminating...", watcher->pid, pid);
            watcher->trigger = false;
            break;
        }

        int r = handleSelect(watcher->fd, &time_to_wait);
        if (r < 0) {
            LOGE("select failed [error:%d, desc:%s]", errno, strerror(errno));
        } else if (r > 0) {
            //handle event
            int event = readEvents(watcher);
            if (event < 0) {
                LOGE("inotify_event read failed [errno:%d, desc:%s]", errno, strerror(errno));
                break;
            } else {
                handleEvents(watcher);
                // communicating
                if (watcher->sinfo) {
                    ret = communicate_send(watcher->sinfo);
                    if (ret == -1) {
                        LOGI("Lost connection.\n");
                        watcher->trigger = false;
                        if (watcher->sinfo->callback) {
                            (watcher->sinfo->callback)();
                        }
                        break;
                    }
                }
            }
        } else if (r == 0) {
            // communicating
            if (watcher->sinfo) {
                ret = communicate_send(watcher->sinfo);
                if (ret == -1) {
                    LOGI("Lost connection.\n");
                    watcher->trigger = false;
                    if (watcher->sinfo->callback) {
                        (watcher->sinfo->callback)();
                    }
                    break;
                }
            }
        }
    }
}