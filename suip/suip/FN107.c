#include "suip.h"
#include "hsmapi.h"

/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000107 : FISC CHANGE TR31 Key Block KEY                   */
/*----------------------------------------------------------------------------*/

/*
DES_LIB("FN000107" "2T2PPK TR31K  950   1 KBPK FISC  950   1 "
KBPK(key)(H32)+TR31K(key)(H80) Return_Code)

key_identify ="2T2PPK TR31K   950   1 KBPK FISC  950   1 "
*/

typedef struct	{
	char	f ;		     /* FUNCTION CODE X'07' */
	char    mode ;       /* 0x00 - for FISC Credit Card change TPK, 0x01 - for Host change ZPK */
	char    keytype ;    /* 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char	key[48] ;	
	char	TR31K[80] ;  /* TR31 Key Block */
}	fn107i_t, *pfn107i_t ;

typedef struct	{
	int 	rc ;		/* RETURN CODE                          */
	char    KEY[32] ;       /* for unpdate DB */
	char    KCV[6] ;        
}	fn107o_t, *pfn107o_t ;

int fn107( char* out, char* in ) 
{
   int ksz ;
   char cmds[512] ;
   pfn107i_t i107 = (pfn107i_t) in ;
   pfn107o_t o107 = (pfn107o_t) out ;

   CMDSD *sd = (CMDSD *) cmds ;

   memset( cmds, 0, sizeof(cmds) ) ;

   if( i107->mode != 0x00 && i107->mode != 0x01 ) return( 15 ) ;
   if( i107->keytype == 0x01 ) return( 15 ) ;
   if( i107->keytype == 0x02 ) ksz = 32 ;
   if( i107->keytype == 0x03 ) ksz = 48 ;
   if( ldrint(i107->TR31K+1, 4) != 80 )  return( 80 ) ;

   memcpy( cmds, "A6", 2 ) ;
   if( i107->mode == 0x00 ) 
       memcpy( cmds+2, "002", 3 ) ;    /* TPK, for FISC credit card change PPK */
   else
       memcpy( cmds+2, "001", 3 ) ;    /* ZPK for Translate PINB to Host */
   if( i107->keytype == 0x02 ) 
       memcpy( cmds+5, "U", 1 ) ;
   if( i107->keytype == 0x03 )
       memcpy( cmds+5, "T", 1 ) ;
   memcpy( cmds+6, i107->key, ksz ) ;
   memcpy( cmds+6+ksz, "R", 1 ) ;
   memcpy( cmds+7+ksz, i107->TR31K, 80 ) ;
   memcpy( cmds+87+ksz, "U", 1 ) ;
   HSMLogMSG( "FN000107;", cmds, NULL ) ;
   o107->rc = hsmio(cmds, 0) ;
   if( o107->rc != 0 ) {
       HSMLogMSG( "FN000107;",cmds, NULL ) ;
       return( o107->rc ) ;
   }
   HSMLogMSG( "FN000107;", cmds, NULL ) ;

   memcpy( o107->KEY, sd->lmkey+1, 32 ) ;
   memcpy( o107->KCV, sd->lmkey+33, 6 ) ;

   return( o107->rc ) ;
}
