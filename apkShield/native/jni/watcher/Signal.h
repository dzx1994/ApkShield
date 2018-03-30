#ifndef SIGNAL_H
#define SIGNAL_H

#include <unistd.h>
#include <stdlib.h>

#include "dalvik/common.h"

typedef struct {
    int fdpipes2r[2];
    int fdpiper2s[2];
} pipe_t;

typedef struct Signal {
    int pid;
    pipe_t *fds;
    long basetimeout;
    void (*callback)();
} Signal;

int communicate_recv(Signal *sign);
int communicate_send(Signal *sign);

void receive_notification(Signal *info);
void send_notification(Signal *cinfo);

void free_signal(Signal *sign);

#endif // SIGNAL_H
