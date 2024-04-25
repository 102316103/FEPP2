#ifdef WIN32
#include  <windows.h>
#include  <io.h>
#else
#define   O_BINARY	0
#endif
#include  <stdio.h>
#include  <stdlib.h>
#include  <fcntl.h>
#include  <string.h>
#include  <sys/types.h>
#include  <sys/stat.h>
#include  <time.h>
#include  <stdarg.h>
#include  <comfcns.h>

static long GetDateTime( long *xtime ) ;
void dbltodecimal( char* buffer, double doub_val, int sign, int len, int dec) ;

static int  xmds[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
char   _CMNpathPRX_[256] = "\0" ;

int  IsLocalDate( int date ) /* date is in national date format */
{
   int	yy, mm, dd ;

   if ( date >= 0 )
      yy = date / 10000 + 1911 ;
   else {
      date = -date ;
      yy = date / 10000 ;
   }
   date = date % 10000 ;
   mm = date / 100 - 1 ;
   dd = date % 100 ;
   if (mm > 11 || mm < 0 || dd <= 0) return( 0 );
   xmds[1] = 28 + (yy%4  == 0 && yy % 100 != 0 || yy%400 == 0);
   return ( dd > xmds[mm] ? 0 : 1 ) ;
}

int  IsFileThisYear( char* f, int rm )
{
   int    rc, yy ;
   struct stat buf;
   time_t trx_time ;
   struct tm *ptr ;

   if ( stat(f, &buf) ) return( 2 ) ;
   ptr = localtime(&buf.st_ctime);
   yy = ptr->tm_year ;
   (void) time(&trx_time);
   ptr = localtime(&trx_time);
   rc = ( ptr->tm_year == yy ? 1 : 0 ) ;
   if ( rc == 0 && rm == 1 ) unlink( f ) ;
   return( rc ) ;
}

Tolower( int c )
{
   if ( c >= 'A' && c <= 'Z' )
      return( 'a' + c - 'A' ) ;
   return( c ) ;
}

Toupper( int c )
{
   if ( c >= 'a' && c <= 'z' )
      return( 'A' + c - 'a' ) ;
   return( c ) ;
}

XorCHARS( uchar *os, uchar *a, uchar *b, int sz )
{
   int  i ;

   for ( i = 0; i < sz; i++ ) *(os+i) = *(a+i) ^ *(b+i) ;
   return( 0 ) ;
}

XorDATA( uchar *os, uchar *a, uchar *b, int sz )
{
   return( XorCHARS(os, a, b, sz) ) ;
}

char *UnpackCHARS(uchar *hex, uchar *str, int sz )
{
   int  i = 0, j = 0, va ;

   while ( sz-- > 0 ) {
      if ( (va = *(str+j) >> 4) >= 10 )
         *(hex+i++) = va - 10 + 'A' ;
      else
         *(hex+i++) = va + '0' ;
      if ( (va = *(str+j++) & 0x0f) >= 10 )
         *(hex+i++) = va - 10 + 'A' ;
      else
         *(hex+i++) = va + '0' ;
   }
   return( (char *) hex ) ;
}

CHARStoHEX( uchar *hex, uchar *str, int sz )
{
    UnpackCHARS( hex, str, sz ) ;
    *(hex+(sz*2)) = '\0' ;
    return( 0 ) ;
}

char *PackHEX( uchar *str, uchar *hex, int sz )
{
   int  c, i = 0, j = 0 ;

   while ( sz-- > 0 ) {
      c = Toupper( *(hex+j++) ) ;
      if ( (c >= 'A') && (c <= 'F') )
         *(str+i) = (c - 'A' + 10 ) << 4 ;
      else
         *(str+i) = (c & 0x0f) << 4 ;

      c = Toupper( *(hex+j++) ) ;
      if ( (c >= 'A') && (c <= 'F') )
         *(str+i++) += c - 'A' + 10 ;
      else
         *(str+i++) += (c & 0x0f ) ;
   }
   return( (char *) str ) ;
}

HEXtoCHARS( uchar *str, uchar *hex, int sz )
{
    PackHEX( str, hex, sz ) ;
    *(str+sz) = '\0' ;
    return( 0 ) ;
}

cpstring( char *dest,char *sour,int len )
{
   memcpy( dest, sour, len ) ;
   *(dest+len) = 0;
   return( 0 ) ;
}

AsciiToHex( int asc )
{
      if (Toupper(asc) >= 'A' && Toupper(asc) <= 'F')
         return( Toupper(asc) - 'A' + 10 ) ;
      return( asc - '0' ) ;
}

HexToAscii( int hex )
{
      if ( hex >= 0 && hex <= 9 ) return( hex + '0' ) ;
      return( hex - 10 + 'A' ) ;
}

HexToDigit( int hex )
{
      return( hex + '0' ) ;
}

char *hexncpy( char *str, char *hex, int sz )
{
   return( PackHEX((uchar*) str, (uchar*) hex, sz) ) ;
}

static char MyTokDEL[20] = " \n\t" ;
char *_ExpMyTokDEL_ = MyTokDEL+3 ;

static int d_Tok_YES( int c )
{
   int  i, sz = strlen(MyTokDEL) ;

   for ( i = 0; i < sz; i++ ) {
       if ( *(MyTokDEL+i) == c)
	  return( 1 ) ;
   }
   return( 0 ) ;
}

char  *ldtoks( char *from, char *to, int tsz )
{
   int  i ;

   memset( to, 0, tsz ) ;
   while ( *from ) {
      if ( !d_Tok_YES(*from) ) break ;
      from++ ;
   }
   if ( *from == 0 ) return( (char*) 0 ) ;
   tsz-- ;
   for ( i = 0, tsz--; i < tsz ; i++ ) {
     if ( *from == 0 || d_Tok_YES(*from) ) break ;
     *(to+i) = *from++ ;
   }
   while ( *from ) {
      if ( !d_Tok_YES(*from) ) break ;
      from++ ;
   }
   return( from );
/*
   memset( to, 0, tsz ) ;
   while ( *from == ' ' || *from == '\n' || *from == '\t' )
      from++ ;
   if ( *from == 0 ) return( (char*) 0 ) ;
   for ( i = 0; i < tsz - 1 ; i++ ) {
     if ( *from == ' ' || *from == '\0' || *from == '\n' || *from == '\t' )
	break ;
     *(to+i) = *from++ ;
   }
   while ( *from != ' ' && *from != '\0' && *from != '\n' && *from != '\t' )
      from++ ;
   return( from );
*/
}

char  *ldchars( char *from, char *to, int width )
{
   for ( width--; width >= 0; width-- )
     if ( *(from + width) != ' ' && *(from + width) != '\0' )
        break ;
   memcpy( to, from, ++width ) ;
   *(to + width) = '\0' ;
   return( to );
}

void  stchars( char *str, char *to, int f_len )
{
   int   copy_bytes ;

   copy_bytes =  strlen(str) ;

   if ( copy_bytes > f_len )  copy_bytes =  f_len ;

   /* Copy the data into the record buffer. */
   memcpy( to, str, copy_bytes ) ;

   /* Make the rest of the field blank. */
   memset( to+copy_bytes, (int) ' ', f_len-copy_bytes ) ;
}

void  stchars_r( char *str, char *to, int i_len )
{
   int  len, f_len = i_len ;

   if ( f_len < 0 ) f_len = -f_len ;
   if ( i_len < 0 ) memset( to, '0', f_len ) ;
   if ( (len = strlen(str)) > f_len ) {
      if ( i_len < 0 ) str += len - f_len ;
      len = f_len ;
   }
   to += f_len - len ;
   memcpy( to, str, len ) ;
}

int hex2int(char *hex, int len)
{
    int i, val = 0;
    char byte ;

    if( *hex >= '0' && *hex <= '9' ) return( ldrint( hex, 2 ) ) ; 

    for( i = 0; i < len; i++ )
    {
        /* get current character then increment */
        byte = *hex++;
        /* transform hex character to the 4bit equivalent number, using the ascii table indexes */
        if (byte >= '0' && byte <= '9') byte = byte - '0';
        else if (byte >= 'a' && byte <='f') byte = byte - 'a' + 10;
        else if (byte >= 'A' && byte <='F') byte = byte - 'A' + 10;
        /* shift 4 to make space for new digit, and add the 4 bits of the new digit */
        val = (val << 4) | (byte & 0xF);
    }
    return val;
}

long  ldlint( char *ptr, int n )
{
   char buf[64] ;

   if ( n >= sizeof(buf) )  n= sizeof(buf) -1 ;
   memcpy(buf, ptr, n) ;
   buf[n] = '\0' ;
   return (atol(buf) ) ;
}

int  ldrint( char *ptr, int n )
{
   return( (int) ldlint( ptr, n ) ) ;
}

void stlint( long from, char *to, int iwidth )
{
   char buf[20], *ptr = buf ;
   int  len, width = iwidth ;

   if ( width < 0 ) width = -iwidth ;
   sprintf( buf, "%ld", from ) ;
   if ( (len = strlen(buf)) > width ) {
      ptr += len - width ;
      len = width ;
   }
   memset( to, '0', width ) ;
   to += width - len ;
   memcpy( to, ptr, len ) ;
   if ( iwidth < 0 ) *(to+width) = 0 ;
}

void stlints( long from, char *to, int width )
{
   char buf[20], *ptr = buf ;
   int  len ;

   sprintf( buf, "%ld", from ) ;
   if ( (len = strlen(buf)) > width ) {
      ptr += len - width ;
      len = width ;
   }
   memset( to, ' ', width ) ;
   to += width - len ;
   memcpy( to, ptr, len ) ;
}

void strint( int from, char *to, int width )
{
   stlint( (long) from, to, width ) ;
}

long ldlbcd( char *ptr, int width )
{
   char buf[40] ;

   UnpackCHARS( (uchar*) buf, (uchar*) ptr, width ) ;
   return( ldlint(buf, width*2) );
}

void stlbcd( long from, char *to, int width )
{
   char buf[40] ;

   stlint( from, buf, width*2 ) ;
   hexncpy( to, buf, width ) ;
}

double  ldxdouble( char* from, int width )
{
   char buffer[128] ;

   if ( width >= sizeof(buffer) ) width = sizeof(buffer) - 1 ;
   memcpy( buffer, from, width ) ;
   buffer[width] = '\0' ;
   return( atof(buffer) ) ;
}

void stxdouble( double d_value, char* buf, int width )
{
   char  buffer[256] ;

   /* Convert the 'double' to ASCII and then copy it into the record buffer. */
   dbltodecimal( buffer, d_value, 0, width, 0 ) ;
   memcpy( buf, buffer, width ) ;
}

double  ldxdecimal( char* from, int width )
{
   char buffer[256] ;

   memcpy( buffer, from, width-2 ) ;
   buffer[width-2] = '.' ;
   memcpy( &buffer[width-1], from+(width-2), 2 ) ;
   buffer[width+1] = '\0' ;
   return( atof(buffer) ) ;
}

void stxdecimal( double d_value, char* buf, int width )
{
   char buffer[256] ;

   /* Convert the 'double' to ASCII and then copy it into the record buffer. */
   dbltodecimal( buffer, d_value, 1, width, 2 ) ;
   memcpy( buf, buffer, width ) ;
}


/* dbltodecimal( char* buffer, double doub_val, int len, int dec)

   - formats a double to a string
   - if there is an overflow, '*' are returned

   Return

      Pointer to the Formatted String
*/

extern char  *fcvt() ;

void dbltodecimal( char* buffer, double doub_val, int sign, int len, int dec)
{
   int	 dec_val, sign_val ;
   int	 pre_len, post_len, sign_pos ; /* pre and post decimal point lengths */
   char *result, *out_string ;

   if ( len < 0 )    len = -len  ;
   if ( len > 128 )  len =  128 ;

   memset( buffer, (int) '0',  len) ;
   if ( doub_val == 0 )
   {
      if ( sign )
         buffer[0] = '+' ;
      return ;
   }
   out_string =  buffer ;

   if (dec > 0)
   {
      post_len =  dec ;
      if (post_len > 15)     post_len =  15 ;
      if (post_len > len)  post_len =  len ;
      pre_len  =  len - post_len ;
   }
   else
   {
      pre_len  =  len ;
      post_len = 0 ;
   }

   result =  fcvt( doub_val, post_len, &dec_val, &sign_val) ;

   sign_pos = 0 ;
   if ( dec_val > pre_len ||  pre_len<0  ||  sign_pos< 0 && sign_val)
   {
      /* overflow */
      memset( out_string, (int) '*',  len) ;
      return ;
   }

   if (dec_val > 0)
   {
      memset( out_string, (int) '0',  pre_len - dec_val) ;
      memmove( out_string + pre_len - dec_val, result, dec_val) ;
   }
   else
   {
      if (pre_len> 0)  memset( out_string, (int) ' ',  (pre_len-1)) ;
   }
   if ( sign_val)
      out_string[sign_pos] = '-' ;
   else
      if ( sign )
         out_string[sign_pos] = '+' ;


   out_string += pre_len ;
   if (dec_val >= 0)
   {
      result+= dec_val ;
   }
   else
   {
      out_string    -= dec_val ;
      post_len += dec_val ;
   }

   if ( post_len > (int) strlen(result) )
      post_len =  strlen( result) ;

   /*  - out_string   points to where the digits go to
       - result       points to where the digits are to be copied from
       - post_len     is the number to be copied
   */

   if (post_len > 0)   memmove( out_string, result, post_len) ;

   buffer[len] =  '\0' ;

   return ;
}

void  HEXDUMP( char* str, uchar* hex, int sz )
{
   int  sgn = sz ;

   if ( sz < 0 ) sz = -sz ;
   fprintf( stderr, "%s", str ) ;
   while( sz-- ) fprintf( stderr, "%02x ", *hex++ ) ;
   if ( sgn > 0 ) fprintf( stderr, "\n" ) ;
}

void  CHARDUMP( char* str, uchar* hex, int sz )
{
   int  sgn = sz ;

   if ( sz < 0 ) sz = -sz ;
   fprintf( stderr, "%s", str ) ;
   while( sz-- ) {
      if ( *hex >= 0x20 && *hex < 0x7e ) {
         fprintf( stderr, "%c", *hex++ ) ;
	 continue ;
      }
      fprintf( stderr, "~%02x", *hex++ ) ;
   }
   if ( sgn > 0 ) fprintf( stderr, "\n" ) ;
}

static int DmpFd = -1, f_date = 0 ;

static long OpenDumpFILE( int apnd )
{
   char line[128] ;
   long x_time, w_date ;

   if ( (w_date = GetDateTime(&x_time)) != f_date ) {
      int  append ;

      if ( DmpFd >= 0 ) close( DmpFd ) ;
      sprintf( line, "%sDMP%04ld.txt", _CMNpathPRX_, (f_date = w_date)%10000 );
      if ( IsFileThisYear(line, 1) )
         append = (apnd ? O_APPEND : 0) ;
      else
         append = O_TRUNC ;
      DmpFd = open( line, O_RDWR|O_CREAT|O_BINARY|append, 0666 ) ;
#ifndef WIN32
      fcntl( DmpFd, F_SETFD, 1 ) ;
#endif
      if ( lseek(DmpFd, 0, 2) == 0 ) {
         memset( line, 0, sizeof(line) ) ;
         memset( line, '*', 60 ) ;
         strcat( line, "\r\n" ) ;
	 write( DmpFd, line, strlen(line) ) ;
      }
   }
   return( x_time ) ;
}

int  ExportDumpFILE( void )
{
   return( DmpFd ) ;
}

void CloseDumpFILE( void )
{
   if ( DmpFd >= 0 ) close( DmpFd ) ;
   DmpFd = (f_date = -1) ;
}

void  PutDumpFile( char* pgm, char* str, uchar* data, int sz )
{
   int  sgn = sz ;
   long xtime ;
   char line[2048] ;

   if ( sz < 0 ) sz = -sz ;
   if ( sz == 0 ) sz = strlen((void*) data) ;
   xtime = OpenDumpFILE(1) ;
   memset( line, 0, sizeof(line) ) ;
   if ( pgm != (char*) 0 )
      sprintf( line, "%06ld==> %s %s", xtime, pgm, str ) ;
   write( DmpFd, line, strlen(line) ) ;
   write( DmpFd, data, sz ) ;
   if ( sgn >= 0 ) write( DmpFd, "\r\n", 2 ) ;
}

void  HEXDUMP_f( char* pgm, char* str, uchar* hex, int sz )
{
   int  sgn = sz, bsz, xsz ;
   long xtime ;
   char line[2048] ;

   if ( sz < 0 ) sz = -sz ;
   if ( sz == 0 ) sz = strlen((void*) hex) ;
   xtime = OpenDumpFILE(1) ;
   memset( line, 0, sizeof(line) ) ;
   if ( pgm != (char*) 0 )
      sprintf( line, "%06ld==> %s %s", xtime, pgm, str ) ;
   bsz = strlen(line) ;
   xsz = sizeof(line) - bsz - 6 ;
   while( sz-- ) {
      if ( bsz >= xsz ) {
         write( DmpFd, line, strlen(line) ) ;
         xsz = sizeof(line) - 6 ;
	 bsz = 0 ;
	 memset( line, 0, sizeof(line) ) ;
      }
      sprintf( line+bsz, "%02x ", *hex++ ) ;
      bsz += 3 ;
   }
   if ( sgn >= 0 ) {
      strcat( line, "\r\n" ) ;
      write( DmpFd, line, strlen(line) ) ;
   }
   else
      write( DmpFd, line, strlen(line) ) ;
}

void  CHARDUMP_f( char* pgm, char* str, uchar* hex, int sz )
{
   int  sgn = sz, bsz, xsz, lcs = 0 ;
   long xtime ;
   char line[2048] ;

   if ( sz < 0 ) sz = -sz ;
   if ( sz == 0 || sz >= 999999 ) {
      if ( sz >= 999999 ) lcs = 1 ;
      sz = strlen((void*) hex) ;
   }
   xtime = OpenDumpFILE(1) ;
   memset( line, 0, sizeof(line) ) ;
   if ( pgm != (char*) 0 )
      sprintf( line, "%06ld==> %s %s", xtime, pgm, str ) ;
   bsz = strlen(line) ;
   xsz = sizeof(line) - bsz - 6 ;
   while( sz-- ) {
      if ( bsz >= xsz ) {
	 write( DmpFd, line, strlen(line) ) ;
         xsz = sizeof(line) - 6 ;
	 bsz = 0 ;
	 memset( line, 0, sizeof(line) ) ;
      }
      if ( (*hex >= 0x20 && *hex < 0x7e) || lcs ) {
         sprintf( line+bsz++, "%c", *hex++ ) ;
	 continue ;
      }
      sprintf( line+bsz, "~%02x", *hex++ ) ;
      bsz += 3 ;
   }
   if ( sgn >= 0 ) {
      strcat( line, "\r\n" ) ;
      write( DmpFd, line, strlen(line) ) ;
   }
   else
      write( DmpFd, line, strlen(line) ) ;
}

void  nCHARDUMP_f( char* pgm, char* str, int va )
{
   char line[48] ;

   sprintf( line, "(%d, 0x%02x)", va, va ) ;
   CHARDUMP_f( pgm, str, (uchar *) line, 0 ) ;
}

void  CHARDUMP_f1( char* pgm, char* str )
{
   long xtime ;
   char line[128] ;

   CloseDumpFILE() ;
   xtime = OpenDumpFILE(0) ;
   lseek( DmpFd, 0, 0 ) ;
   memset( line, 0, sizeof(line) ) ;
   sprintf( line, "%06ld==> %s %s", xtime, pgm, str ) ;
   write( DmpFd, line, strlen(line) > 60 ? 60 : strlen(line) ) ;
   CloseDumpFILE() ;
}

void UPPERnSTR( uchar* str, int sz )
{
   while( sz-- ) {
      *str = Toupper( *str ) ;
      str++ ;
   }
}

void UpperSTR( uchar* str )
{
   while ( *str ) {
      *str = Toupper( *str ) ;
      str++ ;
   }
}

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

static int  MySeq = -1, LOGFD = -1 ;
static long logdate = 0 ;
static void CKopenLOG()
{
   long wdate ;
   int  append = O_APPEND ;
   char path[40] ;

   wdate = GetDateTime( (long *) 0 ) ;
   if ( wdate != logdate ) {
      if ( LOGFD >= 0 ) { MySeq = -1 ; close( LOGFD ) ; }
      sprintf( path, "%sLOG%04ld.txt", _CMNpathPRX_, (logdate = wdate)%10000 ) ;
      if ( !IsFileThisYear(path, 1) ) append = O_TRUNC ;
      LOGFD = open( path, O_RDWR|O_CREAT|append|O_BINARY, 0666 ) ;
#ifndef WIN32
      fcntl( LOGFD, F_SETFD, 1 ) ;
#endif
   }
}

void CloseMyLOG( void )
{
   if ( LOGFD >= 0 ) close( LOGFD ) ;
   LOGFD = (MySeq = -1) ;
   logdate = 0 ;
}

void WriteLOG( int obj, char type, char *text1, long size )
{
   long mdate, mtime ;
   unsigned char* text = (unsigned char*) text1 ;
   char hsz = 12, nl = 1, pline[256], *ptr ;

   CKopenLOG() ;
   if ( (type != 'I') && size < 0 ) { size = -size ; nl = 0; }
   memset( pline, 0, sizeof(pline) ) ;
   mdate = GetDateTime( &mtime ) ;
   if ( !(obj & CONT) ) {
      MySeq++ ;
      sprintf( pline, "%06ld%02d:", mtime, MySeq%100 ) ;
      write( LOGFD, pline, strlen(pline) ) ;
      memset( pline, 0, sizeof(pline) ) ;
   }
   if ( obj & NOLF ) nl = 0 ;
   switch( type ) {
	case 'I' :
   	   write( LOGFD, text, strlen((char*) text) ) ;
	   sprintf( pline+strlen(pline), "0x%08lx  ", size ) ;
	   sprintf( pline+strlen(pline), "%ld\r\n", size ) ;
   	   write( LOGFD, pline, strlen(pline) ) ;
	   return ;
	case 'F' :
   	   write( LOGFD, text, strlen((char*) text) ) ;
	   sprintf( pline+strlen(pline), "%f\r\n", *((float *) size) ) ;
   	   write( LOGFD, pline, strlen(pline) ) ;
	   return ;
	case 'D' :
   	   write( LOGFD, text, strlen((char*) text) ) ;
	   sprintf( pline+strlen(pline), "%f\r\n", *((double *) size) ) ;
   	   write( LOGFD, pline, strlen(pline) ) ;
	   return ;
	case 'P' :
	   ptr = (char *) size ;
   	   write( LOGFD, text, strlen((char*) text) ) ;
   	   write( LOGFD, ptr, strlen(ptr) ) ;
   	   write( LOGFD, "\r\n", 2 ) ;
	   return ;
	default :
	   break ;
   }
   if ( size == 0 ) size = strlen((char*) text) ;
   if ( type == 'S' ) if ( *(text+4) >= 128 ) hsz = 20 ;
   for ( mdate = strlen(pline),mtime = 0 ; mtime < size ; mtime++ )
   {
	if ( mdate >= (sizeof(pline) - 5) ) {
	   write( LOGFD, pline, strlen(pline) ) ;
	   memset( pline, 0, sizeof(pline) ) ;
	   mdate = 0 ;
	}
	if ( type == 'H' ) {
	   sprintf( pline+mdate, "%02x ", *(text+mtime) ) ;
	   mdate += 3 ;
	   continue ;
	}
        if ( type == 'S' ) {
	   if ( mtime >= 4 && mtime < hsz ) {
	      if ( mtime == 4 )
	         sprintf( pline+mdate++, " %02x ", *(text+mtime) ) ;
	      else
	         sprintf( pline+mdate, "%02x ", *(text+mtime) ) ;
	      mdate += 3 ;
	      continue ;
           }
	}
	if ( *(text+mtime) >= ' ' )
	   *(pline+mdate++) = *(text+mtime) ;
	else {
	   *(pline+mdate++) = '^' ;
	   *(pline+mdate++) = 0x40 | *(text+mtime) ;
	}
   }
   if ( nl ) strcpy ( pline+strlen(pline), "\r\n" ) ;
   write( LOGFD, pline, strlen(pline) ) ;
}

void HEXLOG( char *xtr, char *str1, int sz )
{
   char *ptr ;
   unsigned char* str = (unsigned char*) str1 ;

   if ( sz == 0 ) sz = strlen(str1) ;
   ptr = malloc( strlen(xtr)+sz*3+10 ) ;
   sprintf( ptr, "%s", xtr ) ;
   while ( sz-- > 0 ) sprintf( ptr+strlen(ptr), "%02x ", *str++ ) ;
   WriteLOG( ErrMSG, ' ', ptr, 0L ) ;
   free( ptr ) ;
}

static NUMBER( uchar* cmd, int sz )
{

   while ( sz-- > 0 ) {
      if ( *cmd < '0' || *cmd > '9' ) return( 0 ) ;
      cmd++ ;
   }
   return( 1 ) ;
}

static HEXDECIMAL( uchar* cmd, int sz )
{
   while ( sz-- > 0 ) {
      if ( *cmd >= '0' && *cmd <= '9' ) { cmd++; continue ; }
      *cmd = Toupper( *cmd ) ;
      if ( *cmd < 'A' || *cmd > 'F')  return( 0 ) ;
      cmd++ ;
   }
   return( 1 ) ;
}

int HexDEC( uchar* cmds, ... )
{
   int  ofs, sz, rc = 1 ;
   va_list  arg_marker  ;

   va_start( arg_marker, cmds ) ;
   for ( ofs=va_arg(arg_marker,int); ofs >= 0 ; ofs=va_arg(arg_marker,int) ) {
       if ( (sz = va_arg(arg_marker, int)) < 0 ) { /* Numeric digit only */
	  if ( (rc = NUMBER(cmds+ofs, -sz)) == 0 ) break ;
       }
       else
	  if ( (rc = HEXDECIMAL(cmds+ofs, sz)) == 0 ) break ;
   }
   va_end( arg_marker ) ;
   return( rc ) ;
}

uchar *cpstrs( uchar *str, uchar *hex, int sz )
{
   memcpy( str, hex, sz ) ;
   *(str+sz) = '\0' ;
   return( str ) ;
}

uchar *upkstr(uchar *hex, uchar *str, int sz )
{
   UnpackCHARS( hex, str, sz ) ;
   *(hex+(sz*2)) = '\0' ;
   return( hex ) ;
}

#ifdef	WIN32
int ONECHAR( int echo )
{
   char   cin ;
   DWORD  crds ;
   HANDLE hin, hot ;

   hin = GetStdHandle( STD_INPUT_HANDLE ) ;
   hot = GetStdHandle( STD_OUTPUT_HANDLE ) ;
   SetConsoleMode( hin, 0 ) ;
   ReadConsole(hin, &cin, 1, &crds, NULL ) ;
   if ( echo & OCFN_ECHO )
      WriteConsole(hot, &cin, 1, &crds, NULL ) ;
   if ( echo & OCFN_LF )
      WriteConsole(hot, "\n", 1, &crds, NULL ) ;
   SetConsoleMode( hin, ENABLE_LINE_INPUT |
		   ENABLE_ECHO_INPUT | ENABLE_PROCESSED_INPUT ) ;
   return( cin ) ;
}

int password( char* pwd, int n )
{
   int    n1 = n, nx  = 0 ;
   char   cin ;
   DWORD  crds ;
   HANDLE hin, hot ;

   if ( n < 0 ) n = -n ;
   hin = GetStdHandle( STD_INPUT_HANDLE ) ;
   hot = GetStdHandle( STD_OUTPUT_HANDLE ) ;
   SetConsoleMode( hin, 0 ) ;
   while ( nx < n ) {
      ReadConsole(hin, &cin, 1, &crds, NULL ) ;
      if ( cin == '\r' ) break ;
      if ( cin == '\b' ) {
	 if ( nx ) {
            WriteConsole(hot, "\b \b", 3, &crds, NULL ) ;
	    nx-- ;
	    *(pwd+nx) = '\0' ;
	 }
	 continue ;
      }
      if ( n1 < 0 )
         WriteConsole(hot, &cin, 1, &crds, NULL ) ;
      else
         WriteConsole(hot, "*", 1, &crds, NULL ) ;
      *(pwd+nx++) = cin ;
   }
   SetConsoleMode( hin, ENABLE_LINE_INPUT |
		   ENABLE_ECHO_INPUT | ENABLE_PROCESSED_INPUT ) ;
   printf( "\n" ) ;
   return( nx ) ;
}
#else /* LINUX or UNIX */
#include  <sys/termio.h>
#include  <sys/ioctl.h>
#include <sys/types.h>
static struct termio	TermATTR;
#ifndef CTRL
#define  CTRL(c)	(c & 037)
#endif

static void PwdECHOon( void )
{
	(void) ioctl(0, TCSETAF, &TermATTR);
}

static void PwdECHOoff( void )
{
   struct termio	termbuf;

   (void) ioctl(fileno(stdin), TCGETA, &TermATTR) ;
   memcpy( (char*) &termbuf, (char*) &TermATTR, sizeof(TermATTR) ) ;
   termbuf.c_iflag &= ~(INLCR | ICRNL | IUCLC | ISTRIP | IXON | BRKINT);
   termbuf.c_oflag &= ~OPOST;
   termbuf.c_lflag &= ~(ICANON | ISIG | ECHO);
   termbuf.c_cflag |= (CLOCAL | CS8);
   termbuf.c_cc[VMIN] = 1;
   termbuf.c_cc[VTIME] = 128;
   (void) ioctl(fileno(stdin), TCSETAF, &termbuf) ;
}

int password( char* pwd, int n )
{
   int  i = 0, c, n1 = n ;

   if ( n < 0 ) n = -n ;
   PwdECHOoff() ;
   memset( pwd, 0, n ) ;
   while ( i < n ) {
       if ( (c = getchar()) == '\r' ) break ;
       if ( c == 0x7f ) { /* backspace */
	  if ( i ) {
	     printf( "\b \b" ) ;
             fflush( stdout ) ;
	     i-- ;
	     *(pwd+i) = '\0' ;
	   }
	  continue ;
       }
       if ( n1 < 0 )
          putchar( c ) ;
       else
          putchar( '*' ) ;
       fflush( stdout ) ;
       *(pwd+i++) = c ;
   }
   PwdECHOon() ;
   printf( "\n" ) ;
   return( i ) ;
}
#endif	/* WIN32 */
