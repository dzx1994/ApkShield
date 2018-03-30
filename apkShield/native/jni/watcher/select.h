#ifndef SELECT_H_
#define SELECT_H_

#include <unistd.h>

#define TIMEOUT_S 2

int handleSelect(int fd, struct timeval *timeout);

#endif // SELECT_H_
