#include "suip.h"

typedef struct {
	char	lmkpair[2] ;
	char	key[16] ;
	char	icv[16] ;
	char	data[2] ;
} TextSG_t, *pTextSG_t ;

char* sgCmdTXT( char* cmds, char* key, int ksz, int pair,
		char* icv, unsigned char *ldata )
{
   int  dsz ;
   pTextSG_t sg = (pTextSG_t) cmds ;

   strint( pair, sg->lmkpair, sizeof(sg->lmkpair) ) ;
   if ( ksz == 8 )
      *sg->key = 'Z' ;
   else
      *sg->key = (ksz == 16 ? 'U' : 'T') ;
   UnpackCHARS( sg->key+1, key, ksz ) ;
   if ( icv == NULL )
      memset( sg->key+(ksz*2+1), '0', 16 ) ;
   else
      UnpackCHARS( sg->key+(ksz*2+1), icv, 8 ) ;
   memcpy( sg->key+(ksz*2+16+1), ldata, dsz = strlen(ldata) ) ;
   return( sg->key + ((ksz)*2+16+1)+dsz ) ;
}

/*
char* sgCmdTXT( char* cmds, char* key, int ksz, int pair,
		char* icv, unsigned char* ldata )
{
   int  dsz ;
   pTextSG_t sg = (pTextSG_t) cmds ;

   strint( pair, sg->lmkpair, sizeof(sg->lmkpair) ) ;
   if ( ksz == 8 )
      *sg->key = 'Z' ;
   else
      *sg->key = (ksz == 16 ? 'U' : 'T') ;
   UnpackCHARS( sg->key+1, key, ksz ) ;
   if ( icv == NULL )
      memset( sg->key+(ksz*2+1), '0', 16 ) ;
   else
      UnpackCHARS( sg->key+(ksz*2+1), icv, 8 ) ;
   UnpackCHARS( sg->key+(ksz*2+16+1), ldata+1, dsz = *ldata ) ;
   return( sg->key + ((dsz+ksz)*2+16+1) ) ;
}
*/