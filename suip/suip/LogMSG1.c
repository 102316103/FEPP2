#include "../incl/logmsg.h"

static int x_lines = 0 ;
static long GetDateTime( long *xtime ) ;

static long GetDateTime( long *xtime )
{
   long    xdate ;
   time_t trx_time, time();
   struct tm *ptr, *localtime();

   (void) time(&trx_time);
   ptr = localtime(&trx_time);
   if ( xtime != (long *) 0 ) {
      *xtime = 10000L * ((long) ptr->tm_hour) ;
      *xtime += (long) (ptr->tm_sec+ptr->tm_min*100) ;
   }
   xdate = 10000L * (((long) (ptr->tm_year - 11)) % 100) ;
   xdate += (long) (ptr->tm_mday+(ptr->tm_mon+1)*100) ;
   
   return( xdate ) ;
}

static void GetToday( char* datename)
{
	/* printf("size of datename:%d\n", sizeof(*datename)); */
	struct timeval tv;
	struct tm* ptm;
	gettimeofday(&tv, NULL);
	ptm = localtime(&tv.tv_sec);
	
	strftime(datename, 12, "%Y-%m-%d", ptm);
/*
        printf("%s", datename);
        return( xdate ) ;
*/
}


static void x_AscLcaDate( char* datetime )
{
    struct timeval tv;
    struct tm* ptm;
    char time_str[128];
    long milliseconds;
    gettimeofday(&tv, NULL);
    ptm = localtime(&tv.tv_sec);
    strftime(time_str, sizeof(time_str), "%Y/%m/%d %H:%M:%S", ptm);
	/* printf("x_AscLcaDate %s", time_str); */
    milliseconds = tv.tv_usec % 10000 ;
    /* sprintf(datetime, "%s.%03d\n", time_str, milliseconds); */
    sprintf(datetime, "%s:%03d\n", time_str, milliseconds);
}

int  OpenMyMsgFILE( char* fname )
{
   char buf[1024] ;
   FILE *fp ;

   x_lines = 0 ;
   if ( (fp = fopen(fname, "r")) != NULL ) {
      while( fgets(buf, sizeof(buf), fp) != NULL ) x_lines++ ;
      fclose( fp ) ;
   }
   return( open(fname, O_RDWR|O_CREAT|O_APPEND, 0666) ) ;
}


static int  MySeq = -1, LOGFD = -1, LOGFD1 = -1, LOGFD2 = -1, LOGFD3 = -1 ;
static long logdate = 0 ;
static char   _CMNpathPRX_[256] = "\0" ;
static void OpenHsmLOG1()
{
   long wdate ;
   int  append = O_APPEND ;
   char path[40] ;
   char today[1028];
   
   GetToday(today);
   
   wdate = GetDateTime( (long *) 0 ) ;
   if ( wdate != logdate ) {
      if ( LOGFD >= 0 ) { MySeq = -1 ; close( LOGFD ) ; }
/*
      sprintf( path, "../LOG/HSMLOG%04ld.txt", (logdate = wdate)%10000 ) ;
*/
      sprintf( path, "/fep/logs/%s/HSM1LOG%04ld.txt", today, (logdate = wdate)%10000 ) ;
      printf("LogMSG1 path = %s\n", path ) ;
      if ( !IsFileThisYear(path, 1) ) append = O_TRUNC ;
      LOGFD = open( path, O_RDWR|O_CREAT|append, 0666 ) ;
#ifndef WIN32
      fcntl( LOGFD, F_SETFD, 1 ) ;
#endif
   }
}

void CloseHSMLOG1( void )
{
   if ( LOGFD >= 0 ) close( LOGFD ) ;
   LOGFD = (MySeq = -1) ;
   logdate = 0 ;
}

int  HSMLogMSG( char* msg, ... )
{
   char buf[4096], *ptr ;
   va_list  arg_marker  ;

   OpenHsmLOG1() ;
   x_lines++ ;
   x_AscLcaDate( buf ) ;
   strcat( buf, msg ) ;
   va_start( arg_marker, msg ) ;
   while( (ptr = va_arg(arg_marker, char*)) != NULL )
        strcat( buf, ptr ) ;
   va_end( arg_marker ) ;
   strcat( buf, "\r\n" ) ;
   write( LOGFD, buf, strlen(buf) ) ;
   return( ++x_lines ) ;
}

/*  input data translate to hex code */
int cDump(char *outbuf, unsigned char *hex, int len)
{
    int  sgn = len ;

   if ( len < 0 ) len = -len ;
   while( len-- ){
       sprintf( outbuf, "%02x ", *hex++ ) ;
       outbuf +=2 ;
   }
   return(0) ;
}
