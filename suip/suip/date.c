#include <stdio.h>
#include <stdlib.h>
#include <time.h>

long gmLocalYYMMDD( long *localtm, long *gmt )
{
   long    xdate ;
   time_t trx_time ;
   struct tm *ptr ;

   (void) time(&trx_time);
   ptr = localtime(&trx_time);
   if ( localtm != (long *) 0 ) {
      *localtm = 10000L * ((long) ptr->tm_hour) ;
      *localtm += (long) (ptr->tm_sec+ptr->tm_min*100) ;
   }
   xdate = 10000L * ((long) (ptr->tm_year - 11) ) ;
   xdate += (long) (ptr->tm_mday+(ptr->tm_mon+1)*100) ;
   if ( gmt != (long *) 0 ) *gmt = *localtm + ((long) xdate) * 1000000L ;
   return( xdate ) ;
}

long CCYYMMDD( long *localtm )
{
   long    nwk ;
   time_t trx_time ;
   struct tm *ptr ;

   (void) time(&trx_time);
   ptr = localtime(&trx_time);
   if ( localtm != (long *) 0 ) {
      *localtm = 10000L * ((long) ptr->tm_hour) ;
      *localtm += (long) (ptr->tm_sec+ptr->tm_min*100) ;
   }
   nwk = 10000L * ((long) (ptr->tm_year+1900) ) ;
   nwk += (long) (ptr->tm_mday+(ptr->tm_mon+1)*100) ;
   return( nwk ) ;
}

long LocalYYMMDD( long *localtm )
{
   long    nwk ;
   time_t trx_time ;
   struct tm *ptr ;

   (void) time(&trx_time);
   ptr = localtime(&trx_time);
   if ( localtm != (long *) 0 ) {
      *localtm = 10000L * ((long) ptr->tm_hour) ;
      *localtm += (long) (ptr->tm_sec+ptr->tm_min*100) ;
   }
   nwk = 10000L * ((long) (ptr->tm_year - 11) ) ;
   nwk += (long) (ptr->tm_mday+(ptr->tm_mon+1)*100) ;
   return( nwk ) ;
}

void AscDateTime( char* ascdatetime )
{
   char ldate[10], ltime[10] ;
   time_t trx_time ;
   struct tm* ptr ;

   (void) time(&trx_time);
   ptr = localtime(&trx_time);
   sprintf( ldate, "%04d%02d%02d", ptr->tm_year+1900, ptr->tm_mon+1, ptr->tm_mday ) ;
   /* sprintf( ldate, "%02d%02d%02d", ptr->tm_year-100, ptr->tm_mon+1, ptr->tm_mday ) ; */
   sprintf( ltime, "%02d%02d%02d", ptr->tm_hour, ptr->tm_min, ptr->tm_sec ) ;
   sprintf( ascdatetime, "%s%s", ldate, ltime ) ;
}

static char datetime[20] ;

char* AscLcaDate( char* ldat, char* ltm )
{
   char ldate[10], ltime[10] ;
   time_t trx_time ;
   struct tm *ptr ;

   (void) time(&trx_time);
   ptr = localtime(&trx_time);
   sprintf( ltime, "%02d:%02d:%02d", ptr->tm_hour, ptr->tm_min, ptr->tm_sec ) ;
   sprintf( ldate, "%02d/%02d/%02d", ptr->tm_year-11, ptr->tm_mon+1, ptr->tm_mday ) ;
   if ( ldat != (char*) 0 ) strcpy( ldat, ldate ) ;
   if ( ltm != (char*) 0 ) strcpy( ltm, ltime ) ;
   sprintf( datetime, "%s %s", ltime, ldate ) ;
   return( datetime ) ;
}

/*   MMDDhhmmss */
long gmMMDDandTIME( long *localyymmdd, long *localhhmmss )
{
   long    xdate ;
   time_t trx_time ;
   struct tm *ptr ;

   (void) time(&trx_time);
   ptr = localtime(&trx_time);
   if ( localhhmmss != (long *) 0 ) {
      *localhhmmss = 10000L * ((long) ptr->tm_hour) ;
      *localhhmmss += (long) (ptr->tm_sec+ptr->tm_min*100) ;
   }
   if ( localyymmdd != (long *) 0 ) {
      *localyymmdd = 10000L * ((long) (ptr->tm_year - 11) ) ;
      *localyymmdd += (long) (ptr->tm_mday+(ptr->tm_mon+1)*100) ;
   }
   ptr = gmtime(&trx_time);
   xdate  = 10000L * ((long) ptr->tm_hour) ;
   xdate += (long) (ptr->tm_sec+ptr->tm_min*100) ;
   xdate += ((long) (ptr->tm_mday+(ptr->tm_mon+1)*100)) * 1000000L ;
   return( xdate ) ;
}

int gmLocalMMDD( long *hhmmss, long *gmmddhhmmss )
{
   int    xdate ;
   time_t trx_time ;
   struct tm *ptr ;

   (void) time(&trx_time);
   ptr = localtime(&trx_time);
   if ( hhmmss != (long *) 0 ) {
      *hhmmss = 10000L * ((long) ptr->tm_hour) ;
      *hhmmss += (long) (ptr->tm_sec+ptr->tm_min*100) ;
   }
   xdate = ptr->tm_mday + (ptr->tm_mon+1)*100 ;

   if ( gmmddhhmmss != (long *) 0 )
      *gmmddhhmmss = *hhmmss + ((long) xdate) * 1000000L ;
   return( xdate ) ;
}
