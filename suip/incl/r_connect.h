#ifndef _R_CONNECT_H
#define _R_CONNECT_H
#include <comfcns.h>
#include <sockdefs.h>

#ifdef	__cplusplus
extern "C" {
#endif

SOCKET r_connect( char* verb, char* hname, ushort port, ushort lport ) ;

#ifdef	__cplusplus
}
#endif
#endif _R_CONNECT_H
