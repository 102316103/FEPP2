#include "suip.h"

typedef struct {
	char	method ;
	char	tpk[16] ;
	char	pvk[16] ;
	char	pinb[16] ;
	char	actno[12] ;
	char	valdat[16] ;
	char	offset[4] ;
	char	dectab[16] ;
} TextSS_t, *pTextSS_t ;

char* SSCmdTXT( char* cmds, int method, char* tpk, int ksz, char *pvk, char* pinb, char* actno, char* valdat, char* offset, char* dectab ) 
{
   pTextSS_t ss = (pTextSS_t) cmds ;

   ss->method = method + '0' ;
   if ( ksz == 8 )
      *ss->tpk = 'Z' ;
   else
      *ss->tpk = (ksz == 16 ? 'U' : 'T') ;
   UnpackCHARS( ss->tpk+1, tpk, ksz ) ;
   UnpackCHARS( ss->tpk+(ksz*2+1), pvk, 8 ) ;
   memcpy( ss->tpk+(ksz*2+16+1), pinb, 16 ) ;
   memcpy( ss->tpk+(ksz*2+32+1), actno, 12 ) ;
   memcpy( ss->tpk+(ksz*2+44+1), valdat, 16 ) ;
   memcpy( ss->tpk+(ksz*2+60+1), offset, 4 ) ;
   if( method != 3 ){
      memcpy( ss->tpk+(ksz*2+64+1), dectab, 16 ) ;
      return( ss->tpk+(ksz*2+80+1) ) ;
   }
   else
      return( ss->tpk+(ksz*2+64+1) ) ;
}

/*
char* SSCmdTXT( char* cmds, int method, char* tpk, int ksz, char *pvk, char* pinb, char* actno, char* valdat, char* offset, char* dectab ) 
{
   pTextSS_t ss = (pTextSS_t) cmds ;

   ss->method = method + '0' ;
   if ( ksz == 8 )
      *ss->tpk = 'Z' ;
   else
      *ss->tpk = (ksz == 16 ? 'U' : 'T') ;
   UnpackCHARS( ss->tpk+1, tpk, ksz ) ;
   UnpackCHARS( ss->tpk+(ksz*2+1), pvk, 8 ) ;
   UnpackCHARS( ss->tpk+(ksz*2+16+1), pinb, 8 ) ;
   UnpackCHARS( ss->tpk+(ksz*2+32+1), actno, 6 ) ;
   UnpackCHARS( ss->tpk+(ksz*2+44+1), valdat, 8 ) ;
   UnpackCHARS( ss->tpk+(ksz*2+60+1), offset, 2 ) ;
   if( method != 3 ){
      UnpackCHARS( ss->tpk+(ksz*2+64+1), dectab, 8 ) ;
      return( ss->tpk+(ksz*2+80+1) ) ;
   }
   else
      return( ss->tpk+(ksz*2+64+1) ) ;
}
*/