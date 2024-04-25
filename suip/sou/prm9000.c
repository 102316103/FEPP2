#ifdef WIN32
#include <windows.h>
#include <io.h>
#else
#define	 O_BINARY	0
#endif
#include <stdio.h>
#include <fcntl.h>
#include <errno.h>
#include "hsmio.h"

static void cnamecpy( pParameter_t pm, int f ) ;
static void hnamecpy( pParameter_t pm, int f ) ;

void main( int argc, char* argv[] )
{
   int  fd, i ;
   char buf[sizeof(Parameter_t)], wk[160] ;
   pParameter_t pm = (pParameter_t) buf ;

   if ( (fd = open("../LOG/HsmPRM.dat",O_RDWR|O_BINARY|O_CREAT,0666)) < 0 ) {
      printf( "can not open/create SuipPRM.dat(%d)\n", errno ) ;
      exit( 1 ) ;
   }
   memset( buf, 0, sizeof(buf) ) ;
   read( fd, buf, sizeof(Parameter_t) ) ;

   printf( " COMMAND TIMEOUT VALUE  : %d\n", pm->PmCmdTimeout ) ;
   printf( "              NEW VALUE : " ) ;
   (void) gets( wk ) ;
   if ( strlen(wk) ) pm->PmCmdTimeout = atoi(wk) ;

   printf( "HSM COMMAND HEADERSZ : %d\n", pm->PmHSMheaderSZ ) ;
   printf( "             NEW VALUE : " ) ;
/*   (void) gets( wk ) ; */
   gets( wk ) ;
   if ( strlen(wk) ) pm->PmHSMheaderSZ = atoi(wk) ;
   printf( "HSM TCP/IP host PORT : %d\n", pm->PmMhostPORT ) ;
   printf( "             NEW VALUE : " ) ;
/*   (void) gets( wk ) ; */
   gets( wk ) ;
   if ( strlen(wk) ) pm->PmMhostPORT = atoi(wk) ;
   for ( i = 0; i < MaxHSM; i++ ) {
       printf( "HSM TCP/IP host NAME : %s\n", pm->PmMhostNAME[i] ) ;
       printf( "             NEW VALUE : " ) ;
       gets( wk ) ;
       if ( !memcmp(wk, "/d", 2) ) {
          *pm->PmMhostNAME[i] = 0 ;
          continue ;
       }
       if ( strlen(wk) )
          strcpy( pm->PmMhostNAME[i], wk ) ;
       else
          if ( strlen(pm->PmMhostNAME[i]) == 0 ) break ;
   }
   for ( i = 0; i < MaxCONN; i++ ) {
       printf( "CLIENT TCP/IP host ADDR : %-16s\n", pm->PmChostNAME[i] ) ;
       printf( "    NEW VALUE HOST ADDR : " ) ;
       (void) gets( wk ) ;
       if ( !memcmp(wk, "/d", 2) ) {
          cnamecpy(pm, i) ;
          i-- ;
          continue ;
       }
       if ( strlen(wk) )
          strcpy( pm->PmChostNAME[i], wk ) ;
       else
          if ( strlen(pm->PmChostNAME[i]) == 0 ) break ;
   }

   lseek( fd, 0L, 0 ) ;
   write( fd, buf, sizeof(Parameter_t) ) ;
   close( fd ) ;
}

static void hnamecpy( pParameter_t pm, int f )
{
   while ( f < (MaxHSM -1) ) {
       strcpy( pm->PmMhostNAME[f], pm->PmMhostNAME[f+1] ) ;
       f++ ;
   }
   *pm->PmMhostNAME[f] = 0 ;
}

static void cnamecpy( pParameter_t pm, int f )
{
   while ( f < (MaxCONN -1) ) {
       strcpy( pm->PmChostNAME[f], pm->PmChostNAME[f+1] ) ;
       f++ ;
   }
   *pm->PmChostNAME[f] = 0 ;
}

