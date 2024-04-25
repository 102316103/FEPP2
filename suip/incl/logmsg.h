#ifndef _LOGMSG_H
#define _LOGMSG_H
#include  <stdio.h>
#include  <stdlib.h>
#include  <fcntl.h>
#include  <string.h>
#include  <time.h>
#ifdef WIN32
#include  <windows.h>
#include  <io.h>
#endif
#include  <stdarg.h>
#ifdef	__cplusplus
extern "C" {
#endif
int  OpenMyMsgFILE( char* fname ) ;
int  LogMyMSG( int fd, char* msg, ... ) ;
/* int  HSMLogMSG( int fd, char* msg, ... ) ; */
#ifdef	__cplusplus
}
#endif
#endif /* _LOGMSG_H  */
