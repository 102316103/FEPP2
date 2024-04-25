#ifndef _NSVSOCKCMN_H
#define _NSVSOCKCMN_H

#ifdef	__cplusplus
extern "C" {
#endif
void* ALCmemTEXT( void ) ;
void* REPmemTAB( unsigned long ipaddr, unsigned short port ) ;
void* GETmemTAB( void ) ;
void* GETcntxTAB( int* idx ) ;
int   GETusageTAB( void ) ;
void  FREEmemTAB( void *pVoid ) ;
void  ALLsockCLOSE( void ) ;
void  cmnDmpSesTAB( char* fname ) ;

#ifdef	__cplusplus
}
#endif
#endif /* _NSVSOCKCMN_H */
