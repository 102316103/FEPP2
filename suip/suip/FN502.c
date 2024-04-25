#include "suip.h"
#include "hsmapi.h"
/*----------------------------------------------------------------------------*/
/*  FN000XXX產生R2製卡資料                                    */
/*----------------------------------------------------------------------------*/
/*
Application Cryptogram          9F26    (ARQC)
Application Transaction Counter 9F36    (ATC)

DES_LIB("FN000502"，"1T2MAK ATM   807     "，"0020"+PAN(8)+ATC(2)+ARQC(8)，"00XX"+Transcation_DATA，Return_data_1，""，Return_Code)

Input Parameters:
key_identify ="1T2CPVK ATM   807     "
Input_data_1 ="0020"+PAN(8)+ATC(2)+ARQC(8)
Input_data_2 ="00XX"+Transcation_DATA
(輸入的Transcation_DATA可變動前面的長度，例如長度放0032，則後面要接32Bytes, MAX LEN = 255)
Output Parameters:
*/
/*
'109' : MK-AC encrypted under LMK 28-29 variant 1
'209' : MK-SMI encrypted under LMK 28-29 variant 2
'309' : MK-SMC encrypted under LMK 28-29 variant 3
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE */
    char    Modeflag ;       /* 0x01 - DKac */ 
                             /* 0x02 - DKsmi */ 
	                         /* 0x03 - DKsmc */ 
    char    ZMK[32] ;        /*  any ZMK read from DB */         
    char    KEK[32] ;       
    char    MK[32] ;        
    char    PAN[16] ;           
}	fn502i_t, *pfn502i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char    DivKey[32] ;
	char    KCV[6] ;
}	fn502o_t, *pfn502o_t ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char    ketype ;
    char	KEY[32] ;
}	CMDxJT ;

int fn502( char* out, char* in ) 
{
   char cmds[512], divdata[16], xordata[8], KEY[32], ZMK[32], KEK[32], flag ;
   pfn502i_t i502 = (pfn502i_t) in ;
   pfn502o_t o502 = (pfn502o_t) out ;
   CMDxJT *jt = (CMDxJT *) cmds ;

   memset( cmds, 0, sizeof( cmds ) ) ;
   memcpy( ZMK, i502->ZMK, 32 ) ;
   hexncpy( divdata, i502->PAN, 8 ) ;

   flag = i502->Modeflag ;

   memcpy( cmds, "A8", 2 ) ;           /* export MK from LMK to ZMK */ 
   if( flag == 0x01 )
	   memcpy( cmds+2, "109", 3 ) ;
   if( flag == 0x02 )
	   memcpy( cmds+2, "209", 3 ) ;
   if( flag == 0x03 )
	   memcpy( cmds+2, "309", 3 ) ;
   memcpy( cmds+5, "U", 1 ) ;
   memcpy( cmds+6, ZMK, 32 ) ;
   memcpy( cmds+38, "U", 1 ) ;
   memcpy( cmds+39, i502->MK, 32 ) ;
   memcpy( cmds+71, "X", 1 ) ;
   HSMLogMSG( "FN000502;", cmds, NULL ) ;
   if( o502->rc = hsmio(cmds, 72 ) ){
       HSMLogMSG( "FN000502;", cmds, NULL ) ;
       return( o502->rc ) ;
   }
   memcpy(KEY, jt->KEY, 32 ) ;   /*  the key encrypted under ZMK  */ 

   memset( cmds, 0, sizeof( cmds ) ) ;
   memcpy( cmds, "A6", 2 ) ;           /* import MK from ZMK to LMK */ 
   memcpy( cmds+2, "002", 3 ) ;   /*  TMK  */ 
   memcpy( cmds+5, "U", 1 ) ;
   memcpy( cmds+6, ZMK, 32 ) ;
   memcpy( cmds+38, "X", 1 ) ;
   memcpy( cmds+39, KEY, 32 ) ;
   memcpy( cmds+71, "U", 1 ) ;
   HSMLogMSG( "FN000502;", cmds, NULL ) ;
   if( o502->rc = hsmio(cmds, 72 ) ){
       HSMLogMSG( "FN000502;", cmds, NULL ) ;
       return( o502->rc ) ;
   }
   memcpy(KEY, jt->KEY, 32 ) ;   /*  the key encrypted under LMK */  


   memset( xordata, 0xff, 8 ) ;
   XorDATA( divdata+8, divdata, xordata, 8 ) ;

   memset( cmds, 0, sizeof( cmds ) ) ;
   memcpy( cmds, "TO", 2 ) ;           
   memcpy( cmds+2, "U", 1 ) ;
   memcpy( cmds+3, KEY, 32 ) ;
   memcpy( cmds+35, "2", 1 ) ;
   UnpackCHARS( cmds+36, divdata, 16 ) ;  
   memcpy( cmds+68, "U", 1 ) ;
   memcpy( cmds+69, i502->KEK, 32 ) ;
   memcpy( cmds+101, "X", 1 ) ;
   HSMLogMSG( "FN000502;", cmds, NULL ) ;
   if( o502->rc = hsmio(cmds, 102 ) ){
       HSMLogMSG( "FN000502;", cmds, NULL ) ;
       return( o502->rc ) ;
   }
   memcpy( o502->DivKey, cmds+5, 32 ) ;   
   memcpy( o502->KCV, cmds+70, 6 ) ;   
   HSMLogMSG( "FN000502;", cmds, NULL ) ;
   return( o502->rc ) ;

}

