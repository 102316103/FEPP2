#include <usleep.h>

#ifndef WIN32
#include <sys/time.h>

int uSleep( long ms ) /* microseconds for UNIX */
{
   struct timeval tmo, *tmp = &tmo ;

   memset( (char*) &tmo, 0, sizeof(tmo) ) ;
   tmo.tv_sec = (long) ms / 1000000 ;
   tmo.tv_usec = (long) ms % 1000000 ;
   return ( select(0, NULL, NULL, NULL, tmp) ) ;
}
#else
int uSleep( long ms ) /* microseconds for NT */
{
   Sleep( ms / 1000 ) ;
   return( 0 ) ;
}
#endif
