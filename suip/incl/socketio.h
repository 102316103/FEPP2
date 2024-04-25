#ifndef _SOCKETIO_H
#define _SOCKETIO_H
/* #include <stdcall.h> */
#include <sockdefs.h>

#ifdef	__cplusplus
extern "C" {
#endif

SOCKET CMNInitSOCKET( char* hname, unsigned short port ) ;
int CMNsocketIO( SOCKET soc, char* response, char* request, int sz ) ;
int CMNsocketSND( SOCKET soc, char* req, int sz ) ;
int CMNsocketRCV( SOCKET soc, char* res, int sz, int tm ) ;

#ifdef	__cplusplus
}
#endif
#endif /* _SOCKETIO_H */
