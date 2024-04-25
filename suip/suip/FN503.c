#include "suip.h"
#include "hsmapi.h"
/*----------------------------------------------------------------------------*/
/*  FN000503 TPK Key translate from LMK to Public key encrypted               */
/*----------------------------------------------------------------------------*/
/*

DES_LIB("FN000503"，"1T2MAK ATM   807     "，"，Return_data_1，""，Return_Code)

Input Parameters:
key_identify ="1T3 TPK   807     "
Input_data_1 = TPK(48{
Input_data_2 ="0139"+Transcation_DATA(public key )
(輸入的Transcation_DATA可變動前面的長度，例如長度放0032，則後面要接32Bytes)
Output Parameters:
*/


typedef struct	{
	char	f ;				/* FUNCTION CODE */
	char    keytype ;
	char    TPK[48] ;
	char    in1_len[4] ;
	char    Publick[512] ;
}	fn503i_t, *pfn503i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char    length[4] ;
	char    encypted[640] ;
}	fn503o_t, *pfn503o_t ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	IV[16] ;
	char	length[4] ;
	char    enc[256] ;
}	CMDGL ;


int fn503( char* out, char* in ) 
{
   int rc, dsz, publen, len, klen, bits ;
   char cmds[1024], kcv[8], modulus[512], pub_key[512], TPK[50], divk[24], publ_exp[5], zmk[24], odat[32], iv[8], kcv1[8] ;

   pfn503i_t i503 = (pfn503i_t) in ;
   pfn503o_t o503 = (pfn503o_t) out ;
   CMDGL *ptr = (CMDGL *) cmds ;

   memset( modulus, 0, sizeof(modulus) ) ;
   memset( publ_exp, 0, sizeof(publ_exp) ) ;
   memset( pub_key, 0, sizeof(pub_key) ) ;
   memset( iv, 0, sizeof(iv) ) ;
   memset( TPK, 0, sizeof(TPK) ) ;

   if( i503->keytype == 0x01 ) klen = 16 ;
   if( i503->keytype == 0x02 ) klen = 32 ;
   if( i503->keytype == 0x03 ) klen = 48 ;

   if( i503->keytype == 0x01 ) return( o503->rc = 5 ) ; /*  key length error */ 

   memcpy( TPK, i503->TPK, klen ) ;         
   len = ldlint( i503->in1_len, 4) ;
   memcpy( modulus, i503->Publick, len ) ;         /*  public key base64 format */ 
   publen = Base64decode(pub_key, modulus) ;      

   memset( cmds, 0, sizeof( cmds ) ) ;
   if( klen == 32 ) 
       memcpy( cmds, "KAU", 3 ) ;                  /*  Get KEY KCV, KA Command+ 'U' - TPK length */ 
   if( klen == 48 ) 
       memcpy( cmds, "KAT", 3 ) ;                  /*  Get KEY KCV, KA Command+ 'T' - TPK length */ 
   memcpy( cmds+3, TPK, klen ) ;
   memcpy( cmds+3+klen, "02", 2 ) ;                   /*  TMK, TPK pair */ 
   HSMLogMSG( "FN000503;", cmds, NULL ) ;
   if( o503->rc = hsmio(cmds, klen+5 ) ){
       HSMLogMSG( "FN000503;", cmds, NULL ) ;
       return( o503->rc ) ;
   }
   hexncpy( kcv, cmds+4, 8 ) ;

   memset( cmds, 0, sizeof( cmds ) ) ;
   memcpy( cmds, "AE", 2 ) ;           /* export TMK from LMK to TMK */ 
   memcpy( cmds+2, "UC4367DE3248D253392FD03FD7A24172F", 33 );    /* The Key only for SinoPack bank Clear = "B9DF0DE679D345E901EA7604D56BF797" */
   if( klen == 32 ){
       memcpy( cmds+35, "U", 1 ) ;
       memcpy( cmds+36, TPK, klen ) ;
	   memcpy( cmds+36+klen, ";X00", 4 ) ;
   }
   if( klen == 48 ){
       memcpy( cmds+35, "T", 1 ) ;
       memcpy( cmds+36, TPK, klen ) ;
	   memcpy( cmds+36+klen, ";Y00", 4 ) ;
   }
   memcpy( cmds+36, TPK, klen ) ;
   HSMLogMSG( "FN000503;", cmds, NULL ) ;
   if( o503->rc = hsmio(cmds, klen+40 ) ){
       HSMLogMSG( "FN000503;", cmds, NULL ) ;
       return( o503->rc ) ;
   }

   hexncpy( zmk, "B9DF0DE679D345E901EA7604D56BF797", 16 ) ;    
   memset(TPK, 0, sizeof(TPK) ) ;
   if( klen == 32 ){
	   hexncpy( divk, cmds+5, 16 );
	   p_DECRYPT2_ECB(odat, divk, 16, zmk ) ;
	   p_ENCRYPT2_ECB(kcv1, iv, 8, odat ) ;
	   if( memcmp( kcv, kcv1, 3 ) ) return( o503->rc = -11 ) ;
       dsz = berenc_keykcv( TPK, odat, kcv1, 16) ;
   }
   if( klen == 48 ){
	   hexncpy( divk, cmds+5, 24 );
	   p_DECRYPT2_ECB(odat, divk, 24, zmk ) ;
	   p_ENCRYPT3_ECB(kcv1, iv, 8, odat ) ;
	   if( memcmp( kcv, kcv1, 3 ) ) return( o503->rc = -11 ) ;
	   dsz = berenc_keykcv( TPK, odat, kcv1, 24) ;
   }
   bits = berdec_pubkey( modulus, publ_exp, pub_key, publen ) ;
   if( bits != 1024 && bits != 2048 ) return( o503->rc = 76 ) ;  /*  public key length error */ 
   memset( pub_key, 0, sizeof(pub_key) ) ;

   if( rc = RSAPublicEncrypt(pub_key, &len, TPK, dsz, modulus, publ_exp, bits) ) return( o503->rc = 77 ) ;  /*  public key length error; */ 

   stlint( len, o503->length, 4) ;
   memcpy(o503->encypted, pub_key, len );

   return( o503->rc = 0 ) ;
}

