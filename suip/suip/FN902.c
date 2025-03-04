#include "suip.h"
#include "hsmapi.h"
/*------------------------------------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000902 : PIN BLOCK轉換                                                              */
/*------------------------------------------------------------------------------------------------------*/
/*
DES_LIB("FN000902"，"2T3PPK ATM   ATM_ID  "+"PPK ATM   950     "，"0016"+ATM_PinBlock(H16)，""，Return_data_1，""，Return_Code)

Input Parameters:
1.ATM 傳來之 PIN BLOCK 轉換為 FISC 之 PIN BLOCK
key_identify ="2S1PPK ATM   ATM_ID  "+"PPK ATM   950     "
Input_data_1 ="0016"+ATM_PinBlock(H16)
2.ATM 傳來之 PIN BLOCK 轉換為信用卡之 PIN BLOCK
key_identify ="2S1PPK ATM   ATM_ID  "+"PPK ATM   80777831  "
Input_data_1 ="0016"+ATM_PinBlock(H16)
3.FISC 傳來之 PIN BLOCK 轉換為信用卡之 PIN BLOCK
key_identify ="2S1 PPK ATM   950     "+"PPK ATM   80777831  "
Input_data_1 ="0016"+FISC_PinBlock(H16)

Output Parameters:
1.Return_data_1= "0016"+FISC_PinBlock(B64)(H16)
*/


typedef struct	{
	char	f ;				/* FUNCTION CODE */
	char	mode ;		/* 0x00 - ANSI X9.8, 0x01 - IBM3624, 0x02 - ANSI X9.8 Host to ATM, 0x03 - IBM 3624 to ANSI X9.8 */
	char    keytype1 ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char	key1[48] ;      /* Source TPK */
	char    keytype2 ;      /* 0x01= SINGLE DES, 0x02 = DOUBLE DES, 0x03= TRIPLE DES */
	char	key2[48] ;      /* For Host use ZPK */
	char    atmpinb[16] ;
	char	ckey[16] ;
    char    transeq[16] ;
    char    ActNo[12] ;     /* 12 right-most digits of the account number, Only for mode - 0x01, IBM3624 PIN BLOCK,  */
}	fn902i_t, *pfn902i_t ;

typedef struct	{
	int 	rc ;
	char	des_pinb[16] ;
}	fn902o_t, *pfn902o_t ;


int fn902( char* out, char* in )
{
   int mode, n, sklen, dklen ;
   char cmds[512], SrcTPK[50], DesZPK[50], epinb[20], xorpin[20], txno[20] ;
   pfn902i_t i902 = (pfn902i_t) in ;
   pfn902o_t o902 = (pfn902o_t) out ;

   CMDSJ *r = (CMDSJ *) cmds ;

   memset( SrcTPK, 0, sizeof(SrcTPK) ) ;
   memset( DesZPK, 0, sizeof(DesZPK) ) ;

   mode = i902->mode ;

   if( i902->keytype1 == 0x01 ){
       memcpy( SrcTPK, i902->key1, 16 ) ;
   }
   if( i902->keytype1 == 0x02 ){
       memcpy( SrcTPK, "U", 1 ) ;
       memcpy( SrcTPK+1, i902->key1, 32 ) ;
   }
   if( i902->keytype1 == 0x03 ){
       memcpy( SrcTPK, "T", 1 ) ;
       memcpy( SrcTPK+1, i902->key1, 48 ) ;
   }

   if( i902->keytype2 == 0x01 ){
       memcpy( DesZPK, i902->key2, 16 ) ;
   }
   if( i902->keytype2 == 0x02 ){
        memcpy( DesZPK, "U", 1 ) ;
        memcpy( DesZPK+1, i902->key2, 32 ) ;
   }
   if( i902->keytype2 == 0x03 ){
        memcpy( DesZPK, "T", 1 ) ;
        memcpy( DesZPK+1, i902->key2, 48 ) ;
   }
   sklen = strlen(SrcTPK) ;
   dklen = strlen(DesZPK) ;

/* decrypt from ATM PIN BLOCK use communication key */
   memset( cmds, 0, sizeof(cmds) ) ;
   memset( epinb, 0, sizeof(epinb) ) ;
   memset( xorpin, 0, sizeof(xorpin) ) ;
   memset( txno, 0, sizeof(txno) ) ;

/* translate ATM PIN BLOCK ANSI X9.8 to Host ZPK encrypt ANSI X9.8 */
   if( mode == 0x00 ){
       memcpy( cmds, "SI", 2 ) ;
       memcpy( cmds+2, "14", 2 ) ;            /* TPK - keytype 002 */
       memcpy( cmds+4, SrcTPK, sklen ) ;
       memcpy( cmds+4+sklen, "06", 2 ) ;      /* ZPK - keytype 001 */
       memcpy( cmds+6+sklen, DesZPK, dklen ) ;
       memcpy( cmds+6+sklen+dklen, i902->atmpinb, 16 ) ;
   }
/* translate ATM PIN BLOCK (IBM 3624 Xor Sequence encrypted under TPK) to ANSI X9.8 Host ZPK encrypted  */
   if( mode == 0x01 ){
       memcpy( cmds, "HG", 2 ) ;
       memcpy(cmds+2, i902->ckey, 16 ) ;
       memcpy(cmds+18, i902->atmpinb, 16 ) ;
       HSMLogMSG( "FN000902;", cmds, NULL ) ;
       if ( o902->rc = hsmio(cmds, strlen(cmds)) ) return( o902->rc ) ;
       hexncpy(epinb, cmds+4, 8 );
       hexncpy(txno, i902->transeq, 8 );
       XorDATA(xorpin, epinb, txno, 8 );
       UnpackCHARS(epinb, xorpin, 8 );

       memset( cmds, 0, sizeof(cmds) ) ;
     /* translate ATM PIN BLOCK to Host ZPK encrypt ANSI X9.8 */
       memcpy( cmds, "CA", 2 ) ;
       /* ATM TPK */
       memcpy(cmds+2, SrcTPK, sklen ) ;
       /* Host ZPK */
       memcpy(cmds+2+sklen, DesZPK, dklen ) ;
       /* max PIN length */
       memcpy(cmds+2+sklen+dklen, "12", 2 ) ;
       /* Source PIN BLOCK read from ATM IBM3624 */
       memcpy(cmds+4+sklen+dklen, epinb, 16 ) ;
       /* Source IBM3624 PIN BLOCK */
       memcpy(cmds+20+sklen+dklen, "03", 2 ) ;
       /* Destination ANSI X9.8 PIN BLOCK */
       memcpy(cmds+22+sklen+dklen, "01", 2 ) ;
       /* Account No 12 right-most digits, excluding the check digit */
       memcpy(cmds+24+sklen+dklen, i902->ActNo, 12 ) ;
   }
/* translate Host PIN BLOCK ANSI X9.8 to ATM PPK encrypt ANSI X9.8 */
   if( mode == 0x02 ){
       memcpy( cmds, "SI", 2 ) ;
       memcpy( cmds+2, "06", 2 ) ;         /* ZPK - keytype 001 */
       memcpy( cmds+4, SrcTPK, sklen ) ;
       memcpy( cmds+4+sklen, "14", 2 ) ;   /* TPK - keytype 002 */
       memcpy( cmds+6+sklen, DesZPK, dklen ) ;
       memcpy( cmds+6+sklen+dklen, i902->atmpinb, 16 ) ;
   }
/* translate ATM PIN BLOCK IBM 3624 encrypted under TPK to ANSI X9.8 encrypted under Host ZPK  */
   if( mode == 0x03 ){
       memset( cmds, 0, sizeof(cmds) ) ;
     /* translate ATM PIN BLOCK to Host ZPK encrypt ANSI X9.8 */
       memcpy( cmds, "CA", 2 ) ;
       /* ATM TPK */
       memcpy(cmds+2, SrcTPK, sklen ) ;
       /* Host ZPK */
       memcpy(cmds+2+sklen, DesZPK, dklen ) ;
       /* max PIN length */
       memcpy(cmds+2+sklen+dklen, "12", 2 ) ;
       /* Source PIN BLOCK read from ATM IBM3624 */
       memcpy(cmds+4+sklen+dklen, i902->atmpinb, 16 ) ;
       /* Source IBM3624 PIN BLOCK */
       memcpy(cmds+20+sklen+dklen, "03", 2 ) ;
       /* Destination ANSI X9.8 PIN BLOCK */
       memcpy(cmds+22+sklen+dklen, "01", 2 ) ;
       /* Account No 12 right-most digits, excluding the check digit */
       memcpy(cmds+24+sklen+dklen, i902->ActNo, 12 ) ;
   }

   HSMLogMSG( "FN000902;", cmds, NULL ) ;
   if( o902->rc = hsmio(cmds, 0) ){
       HSMLogMSG( "FN000902;", cmds, NULL ) ;
       return( o902->rc ) ;
   }
   HSMLogMSG( "FN000901;", cmds, NULL ) ;

   if( mode != 0x00 && mode != 0x02 ){
        memcpy( o902->des_pinb, r->pinbk+2, 16 ) ;
        *( o902->des_pinb+18 ) = '\0' ;
   }
   else{
        memcpy( o902->des_pinb, r->pinbk, 16 ) ;
        *( o902->des_pinb+16 ) = '\0' ;
   }

   return( o902->rc ) ;

}


