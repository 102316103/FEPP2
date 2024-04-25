#include "suip.h"
#include "hsmapi.h"
/*----------------------------------------------------------------------------*/
/*  FN000504 TPK Key translate from LMK to Public key encrypted               */
/*----------------------------------------------------------------------------*/
/*

DES_LIB("FN000504"，"1T2MAK ATM   807     "，"，Return_data_1，""，Return_Code)

Input Parameters:
key_identify ="1T3 TPK   807     "
Input_data_1 = TPK(48{
Input_data_2 ="0139"+Transcation_DATA(public key )
(輸入的Transcation_DATA可變動前面的長度，例如長度放0032，則後面要接32Bytes)
Output Parameters:
*/


typedef struct	{
	char	f ;				/* FUNCTION CODE */
	char    mode ;          /*  0x00 AES key Translate to Public Encrypt, 0x01 AES Encrypt/Decrypt */ 
	char    meth ;          /*  0x00 - ECB_ENC,  0x01 - ECB_DEC, 0x02 - CBC_ENC,  0x03 - CBC_DEC, 0x04 - CMAC */ 
	char    keytype ;       /*  0x02 - double,  0x03 - triple */ 
	char    DEK[48] ;       /*  LMK pair 32-33 */ 
	char    AESK[64] ;      /*  AES KEY 256bit encrypt under DEK */ 
	char    in1_len[4] ;    /*  0x00 - "0139"+Public KEY (139) DER format, 0x01 - Input_data1 = "0096"+ICV(H32)+message( 16倍數 )  */ 
	char    Publick[512] ;  
}	fn504i_t, *pfn504i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char    length[4] ;
	char    encypted[640] ;
}	fn504o_t, *pfn504o_t ;


int fn504( char* out, char* in ) 
{
   int rc, dsz, publen, len, klen, bits, meth, mode ;
   char cmds[1024], kcv[32], modulus[512], pub_key[512], DEK[50], AESK[64], publ_exp[5], odat[64], iv[32], mac[16] ;

   pfn504i_t i504 = (pfn504i_t) in ;
   pfn504o_t o504 = (pfn504o_t) out ;

   memset( modulus, 0, sizeof(modulus) ) ;
   memset( publ_exp, 0, sizeof(publ_exp) ) ;
   memset( pub_key, 0, sizeof(pub_key) ) ;
   memset( iv, 0, sizeof(iv) ) ;
   memset( DEK, 0, sizeof(DEK) ) ;
   memset( AESK, 0, sizeof(AESK) ) ;

   if( i504->keytype == 0x01 ) klen = 16 ;
   if( i504->keytype == 0x02 ) klen = 32 ;
   if( i504->keytype == 0x03 ) klen = 48 ;

   if( i504->keytype == 0x01 ) return( o504->rc = 5 ) ; /*  key length error */ 

   meth = i504->meth ;
   mode = i504->mode ;
   memcpy( DEK, i504->DEK, klen ) ;         
   len = ldlint( i504->in1_len, 4) ;

   memset( cmds, 0, sizeof( cmds ) ) ;
   memcpy( cmds, "M20000", 6 ) ;                       /*  M2 - Decrypt, 00 - ECB, 00 - input/output Binary */ 
   if( klen == 32 ) 
       memcpy( cmds+6, "00BU",4 ) ;                    /*  DEK LMK pair 32-33, Double length key */ 
   if( klen == 48 )
       memcpy( cmds+6, "00BT",4 ) ;                    /*  DEK LMK pair 32-33, Triple length key */ 
   memcpy( cmds+10, DEK, klen ) ;
   memcpy( cmds+10+klen, "0020", 4 ) ;                 /*  input AESKEY length 64 byte ascii to 32 byte hex */ 
   hexncpy( cmds+14+klen, i504->AESK, 32 ) ;
   HSMLogMSG( "FN000504;", cmds, NULL ) ;
   if( o504->rc = hsmio(cmds, klen+46 ) ){
       HSMLogMSG( "FN000504;", cmds, NULL ) ;
       return( o504->rc ) ;
   }
    memcpy( odat, cmds+8, 32 ) ;

   if( mode == 0 ){
       memcpy( modulus, i504->Publick, len ) ;         /*  public key base64 format */ 
       publen = Base64decode(pub_key, modulus) ;      
       AES_CMAC( kcv, iv, 16, odat, 32 ) ;
       dsz = berenc_keykcv( AESK, odat, kcv, 32) ;
       bits = berdec_pubkey( modulus, publ_exp, pub_key, publen ) ;
       if( bits != 1024 && bits != 2048 ) return( o504->rc = 76 ) ;  /*  public key length error */ 
       memset( pub_key, 0, sizeof(pub_key) ) ;
       if( rc = RSAPublicEncrypt(pub_key, &len, AESK, dsz, modulus, publ_exp, bits) ) return( o504->rc = 77 ) ;  /*  public key length error; */ 
       stlint( len, o504->length, 4) ;
       memcpy(o504->encypted, pub_key, len );
       *(o504->encypted+len) = 0 ;
       return( o504->rc = 0 ) ;
   }

   if( mode == 1 ){
	   dsz = len - 32 ;
	   if( dsz % 32 ) return( o504->rc = 81 ) ; /*  data length error, must be 32 multiple ascii character */ 
       hexncpy( iv, i504->Publick, 16 ) ;                     
	   hexncpy( modulus, i504->Publick+32, dsz = dsz/2 ) ;   /*  input data */ 
       if( meth == 0 ){
           AES_ENCRYPT_ECB(pub_key, modulus, dsz, odat, 32 ) ;
       }
       if( meth == 1 ){
           AES_DECRYPT_ECB(pub_key, modulus, dsz, odat, 32 ) ;
       }
       if( meth == 2 ){
           AES_ENCRYPT_CBC(pub_key, modulus, dsz, iv, odat, 32 ) ;
       }
       if( meth == 3 ){
           AES_DECRYPT_CBC(pub_key, modulus, dsz, iv, odat, 32 ) ;
       }
       if( meth == 4 ){
           AES_CMAC(pub_key, modulus, dsz, odat, 32 ) ;
	       memcpy( mac, pub_key,  16 ) ;
	       dsz = 16 ;
       }
       if( meth == 5 ){    /*  AES_CBC_MAC */ 
           AES_ENCRYPT_CBC(pub_key, modulus, dsz, iv, odat, 32 ) ;
	       memcpy( mac, pub_key+(dsz-16),  8 ) ;
	       dsz = 8 ;
       }
	   len = dsz * 2 ;
       stlint( len, o504->length, 4) ;
       if( meth == 4 || meth == 5 )
           UnpackCHARS(o504->encypted, mac, dsz );
	   else
           UnpackCHARS(o504->encypted, pub_key, dsz );
       *(o504->encypted+len) = 0 ;

       return( o504->rc = 0 ) ;
   }
}



