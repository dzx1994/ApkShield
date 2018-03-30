#include "Signal.h"
#include "select.h"


int writepipe(int fd, const char sig) {
    return (int) write(fd, &sig, 1);
}

int readpipe(int fd, char *sig, int (*select_check)(int, struct timeval *),
             struct timeval *timeout) {
    int ret = 1;
    if (select_check) {
        ret = select_check(fd, timeout);
    }

    if (ret > 0) {
        if (read(fd, sig, 1) > 0) {
            if (*sig == '\x10' || *sig == '\x21' || *sig == '\x02') {
                return 1;
            } else {
                return 0;
            }
        }
    }

    return -1;
}

int communicate_send(Signal *sign) {
    char send_sig = '\x10';
    writepipe(sign->fds->fdpipes2r[1], send_sig);

#ifdef DEBUG
    LOGI("1. Send signal to parent: %d\n", send_sig);
    sleep(1);
#endif

    struct timeval timeout;
    timeout.tv_sec = sign->basetimeout;
    timeout.tv_usec = 0;
    int sig = readpipe(sign->fds->fdpiper2s[0], &send_sig, handleSelect, &timeout);

    if (sig == 1) {

#ifdef DEBUG
        LOGI("4. Received signal from parent correctly: %d\n", send_sig);
        sleep(1);
#endif

        send_sig = (char) ((send_sig + 1) & '\x0F');
        writepipe(sign->fds->fdpipes2r[1], send_sig);

#ifdef DEBUG
        LOGI("5. Send signal to parent again: %d\n", send_sig);
        sleep(1);
#endif

    } else {
        LOGI("Parent signal lost.\n");
        return -1;
    }

    return 0;
}

int communicate_recv(Signal *sign) {
    char receive_sig;
    struct timeval timeout;
    timeout.tv_sec = sign->basetimeout + 1;
    timeout.tv_usec = 0;
    int sig = readpipe(sign->fds->fdpipes2r[0], &receive_sig,
                       handleSelect, &timeout);
    if (sig == 1) {

#ifdef DEBUG
        LOGI("2. Received signal from child: %d\n", receive_sig);
        sleep(1);
#endif

        receive_sig = (char) ((receive_sig + '\x10') | 1);
        writepipe(sign->fds->fdpiper2s[1], receive_sig);

#ifdef DEBUG
        LOGI("3. Send signal to child: %d\n", receive_sig);
        sleep(1);
#endif

    } else {
        LOGI("No child signal.\n");
        return -1;
    }

    timeout.tv_sec = sign->basetimeout + 1;
    sig = readpipe(sign->fds->fdpipes2r[0], &receive_sig, handleSelect, &timeout);
    if (sig != 1) {
        LOGI("Child signal lost.\n");
        return -1;
    }

#ifdef DEBUG
    LOGI("6. Received confirm signal from child: %d\n", receive_sig);
    sleep(1);
#endif

    return 0;
}

void receive_notification(Signal *cinfo) {
    if (!cinfo) {
        LOGE("Receive argument communicate_info can't be NULL.");
        return;
    }

    close(cinfo->fds->fdpiper2s[0]);
    close(cinfo->fds->fdpipes2r[1]);

    while (communicate_recv(cinfo) != -1);

    LOGI("Lost connection in receive.\n");
    if (cinfo->callback) {
        (cinfo->callback)();
    }
    free_signal(cinfo);
}

void send_notification(Signal *cinfo) {
    if (!cinfo) {
        LOGE("Argument communicate_info can't be NULL.");
        return;
    }

    close(cinfo->fds->fdpiper2s[1]);
    close(cinfo->fds->fdpipes2r[0]);
    while (communicate_send(cinfo) != -1);

    LOGI("Lost connection in send.\n");
    if (cinfo->callback) {
        (cinfo->callback)();
    }
    free_signal(cinfo);
}

void free_signal(Signal *sign) {
    pipe_t *fds = sign->fds;
    close(fds->fdpiper2s[0]);
    close(fds->fdpiper2s[1]);
    close(fds->fdpipes2r[0]);
    close(fds->fdpipes2r[1]);
    free(fds);
    free(sign);
}