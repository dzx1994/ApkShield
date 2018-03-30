#include "select.h"

int handleSelect(int fd, struct timeval *timeout) {
    fd_set fds;
    FD_ZERO(&fds);
    FD_SET(fd, &fds);

    if (!timeout) {
        struct timeval time_to_wait;
        time_to_wait.tv_sec = TIMEOUT_S;
        time_to_wait.tv_usec = 0;
        timeout = &time_to_wait;
    }

    return select(fd + 1, &fds, NULL, NULL, timeout);
}