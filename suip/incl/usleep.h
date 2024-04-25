#ifndef _USLEEP_H
#define _USLEEP_H
#include <stdio.h>
#ifdef WIN32
#include <windows.h>
#else
#include <sys/types.h>
#include <sys/select.h>
#endif
#ifdef __cplusplus
extern "C" {
#endif

int uSleep( long ms ) ; /* microseconds for UNIX */

#ifdef __cplusplus
}
#endif
#endif /* _USLEEP_H */
