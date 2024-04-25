#include <stdio.h>
#include "suipfp.h"
#include "comfcns.h"

extern char StartDate[20] ;

#define  ArraySize( A )	 (sizeof( A )/sizeof( A[0] ))

typedef	struct	{
    int cmd ;
    char funcname[9] ;
    long long cnts ; 
} funcnt_t, *pfuncnt_t ;

static funcnt_t fnflag[] = {
    0x01, "FN000101", 0, 0x02, "FN000102", 0, 0x03, "FN000103", 0, 0x04, "FN000104", 0,
    0x05, "FN000105", 0, 0x07, "FN000107", 0, 0x08, "FN000108", 0,
    0x11, "FN000121", 0, 0x12, "FN000122", 0, 0x17, "FN000127", 0, 0x18, "FN000128", 0,
    0x19, "FN000129", 0, 0x1A, "FN000130", 0, 0x1E, "FN000134", 0, 
    0x21, "FN000201", 0, 0x23, "FN000203", 0, 0x24, "FN000204", 0, 0x25, "FN000205", 0,
    0x26, "FN000206", 0, 0x27, "FN000207", 0, 0x28, "FN000208", 0, 0x2C, "FN000213", 0,
    0x31, "FN000301", 0, 0x37, "FN000307", 0, 0x38, "FN000308", 0, 0x39, "FN000309", 0,  
    0x3B, "FN000311", 0, 0x3C, "FN000313", 0, 0x41, "FN000401", 0, 0x42, "FN000416", 0,
    0x49, "FN000409", 0, 0x4B, "FN000411", 0, 0x4D, "FN000413", 0, 0x4E, "FN000414", 0, 
    0x4F, "FN000415", 0, 
    0x51, "FN000501", 0, 0x52, "FN000502", 0, 0x53, "FN000503", 0, 0x54, "FN000504", 0,
    0x51, "FN000601", 0,
    0x81, "FN000801", 0, 0x82, "FN000802", 0, 0x83, "FN000803", 0, 0x85, "FN000805", 0,
    0x91, "FN000901", 0, 0x92, "FN009902", 0, 
} ;

#define TabSIZE(A)	(sizeof(A)/sizeof(A[0]))

static pfuncnt_t FuncTAB = NULL ;


int SetFuncCnts( int cmd )
{
   int i ;

   for ( i = 0; i < TabSIZE(fnflag); i++ )
       if ( cmd == fnflag[i].cmd ){
           HSMLogMSG( fnflag[i].funcname, " in", NULL ) ;
		   fnflag[i].cnts++ ;
           if( fnflag[i].cnts >= 9999999999 ) fnflag[i].cnts = 0 ;
	   }
   return(-1) ;
}

int GetFuncCnts( char* bufr )
{
   int i ;
   char xbuf[64], count[20], localtime[20] ;

   memcpy( bufr, StartDate+2, 12 ) ;
   AscDateTime( localtime ) ;      /* current datetime */ 
   memcpy( bufr+12, localtime+2, 12 ) ;
   
   for ( i = 0; i < TabSIZE(fnflag); i++ ){
       if( fnflag[i].cnts > 0 ) {
           LONG2cobPIC( count, fnflag[i].cnts, "9999999999" ) ;
           sprintf(xbuf, "%s=%s,",  fnflag[i].funcname, count );
           strcat(bufr, xbuf) ;
       }
   }
   return( 0 ) ;
}


void CleanFuncCnts( )
{
   int i ;

   AscDateTime( StartDate ) ;    /* Reset StartDate */ 
   for ( i = 0; i < TabSIZE(fnflag); i++ )
       fnflag[i].cnts = 0 ;

}
