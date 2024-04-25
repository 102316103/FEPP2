#include "suip.h"
#include "hsmapi.h"
/*----------------------------------------------------------------------------*/
/*  FN000501檢查ARQC與產生ARPC                                     */
/*----------------------------------------------------------------------------*/
/*
Application Cryptogram          9F26    (ARQC)
Application Transaction Counter 9F36    (ATC)

DES_LIB("FN000501"，"1T2MAK ATM   807     "，"0020"+PAN(8)+ATC(2)+ARQC(8)，"00XX"+Transcation_DATA，Return_data_1，""，Return_Code)

Input Parameters:
key_identify ="1T2CPVK ATM   807     "
Input_data_1 ="0020"+PAN(8)+ATC(2)+ARQC(8)
Input_data_2 ="00XX"+Transcation_DATA
(輸入的Transcation_DATA可變動前面的長度，例如長度放0032，則後面要接32Bytes, MAX LEN = 255)
Output Parameters:
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE */
/*
    char    Modeflag ;       //0 = Perform ARQC Verification only, 
                             //1 = Perform ARQC Verification and ARPC generation
	                         //2 = Perform ARPC generation only
*/
	char    flag ;
	char    keylen ;
    char    MKAC[48] ;           
    char    PAN[16] ;           
    char    ATC[4] ;   
	char    UN[8] ;
    char    ARQC[16] ;       
    char    ARC[4] ;       /* Not required for Mode Flag 0. */        
    char    len[4] ;           
    char    data[2] ;           
}	fn501i_t, *pfn501i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char    ARPC[16] ;
}	fn501o_t, *pfn501o_t ;

typedef	struct	{ /* Command JS  */
	char	cmd[2] ;
	char	Modeflag ;
    char	SchemeId ;
	char    keytype ;
    char	MKAC[32] ;
    char	PAN[8] ;
    char	ATC[2] ;
    char	UN[4] ;
    char	Len[2] ;
    char	TData[128] ;
}	CMDxJS ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
    char	ARPC[8] ;
}	CMDxJT ;

int fn501( char* out, char* in ) 
{
   int len ;
   char cmds[512] ;
   pfn501i_t i501 = (pfn501i_t) in ;
   pfn501o_t o501 = (pfn501o_t) out ;

   CMDxJS *js = (CMDxJS *) cmds ;
   CMDxJT *jt = (CMDxJT *) cmds ;

   memset( cmds, 0, sizeof( cmds ) ) ;
   memcpy( js->cmd, "KQ", 2 ) ;          /* Verify ARQC/ARPC */
   js->Modeflag = '1' ;                  /* 0 = Perform ARQC verification only */
                                         /* 1 = Perform ARQC Verification and ARPC generation  */ 
   js->SchemeId = i501->flag ;                  /* '0' : Visa VSDC or UKIS, '1' : Europay or MasterCard M/Chip */    
   js->keytype = 'U' ;
   memcpy( js->MKAC, i501->MKAC, 32 ) ;           
   hexncpy( js->PAN, i501->PAN, 8 ) ;   /*  BINARY  */       
   hexncpy( js->ATC, i501->ATC, 2 ) ;   /*  BINARY */
   hexncpy( js->UN, i501->UN, 4 ) ;   /*  Unpredictable Number. Present for all modes. A four byte value must be supplied, */ 
                                        /*  though it is not used, for Scheme ID = '0'. */ 
   len = ldrint(i501->len, 4)/2 ;
   if( len > 255 ) return( o501->rc = -80 ) ;
   sprintf(js->Len, "%02X", len ) ;
   hexncpy( js->TData, i501->data, len ) ;         
   memcpy( js->TData+len, ";", 1 ) ;   
   hexncpy( js->TData+len+1, i501->ARQC, 8 ) ;  /* BINARY */       
   hexncpy( js->TData+len+9, i501->ARC, 2 ) ;   /* Not required for Mode Flag 0.  */
   len = 53+len+11 ;

   HSMLogMSG( "FN000501;", cmds, NULL ) ;
   if( o501->rc = hsmio(cmds, len ) ){
       HSMLogMSG( "FN000501;", cmds, NULL ) ;
       return( o501->rc ) ;
   }
   UnpackCHARS(o501->ARPC, jt->ARPC, 8 ) ;   /* BINARY */ 
   HSMLogMSG( "FN000501;", cmds, NULL ) ;
   return( o501->rc ) ;

}

