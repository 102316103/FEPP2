#ifndef _SOCKDEFS_H
#define _SOCKDEFS_H
#ifdef	WIN32
#include <windows.h>
#include <winsock.h>
#else
#include <sys/types.h>
#include <sys/time.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <netinet/in.h>
#include <netdb.h>
#define SOCKET		int
#define INVALID_SOCKET	-1
#define SOCKET_ERROR	-1
#define closesocket(c)	close(c)
#define GetLastError()	-1
#define WSADATA		int
#define WSAStartup(s, c)	0
#endif
#endif /* _SOCKDEFS_H */
