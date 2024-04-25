#include "suip.h"
#include "hsmapi.h"
#include "svsocknt.h"
/*---------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000411 :���ͥ�����ҽX(TAC_DATA)                         */
/*---------------------------------------------------------------------------*/
/*
DES_LIB("FN000401"�A"1T3ICC C6    807     "�AInput_data_1�AInput_data_2�A""�A""�AReturn_Code)

Input Parameters:
1.	
Input_data_1="0072"+����Ǹ�(N8)+����N��(N4)+
������B(NS14)+�ݥ��N��(N8)+�ݥ��d�ֽX(AN8)+
�������ɶ�(N14)+��X�b��(N16)
2.	
Input_data_1="0088"+����Ǹ�(N8)+����N��(N4)+
������B(NS14)+�ݥ��N��(N8)+�ݥ��d�ֽX(AN8)+
�������ɶ�(N14)+��J�b��(N16)+��X�b��(N16)
3.	
Input_data_1="0040"+����Ǹ�(N8)+����N��(N4)+
�ݥ��d�ֽX(AN8)+��X�b��(N16)
4.	
Input_data_1="0096"+����Ǹ�(N8)+����N��(N4)+
ú�O���B(NS14)+�ݥ��N��(N8)+�ݥ��d�ֽX(AN8)+
�������ɶ�(N14)+ú�����O(N5)+��X�b��(N16)+
�P�b�s��(N16)
Input_data_2="0064"+ICV(H16)+DivData(N48)
Output Parameters�GN/A
*/

typedef struct	{
	char	f ;				/* FUNCTION CODE X'08'  */
	char    keytype ;
	char    key[48] ;
	char    in2_len[4] ;
	char    icv[16] ;
	char    divdata[48] ;
	char    in1_len[4] ;
	char    input1[128] ;
}	fn411i_t, *pfn411i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	tac[16] ;   	    /*  tac(H8)    */
}	fn411o_t, *pfn411o_t ;

int fn411( char* out, char* in ) 
{
   int n, divlen ;
   char cmds[512] ;
   pfn411i_t i411 = (pfn411i_t) in ;
   pfn411o_t o411 = (pfn411o_t) out ;

   CMDxTO *to = (CMDxTO *) cmds ;
   CMDxTP *tp = (CMDxTP *) cmds ;
   
 
   divlen = ldrint(i411->in2_len, 4) - 16 ;  /*  divdata length */
        n = ldrint(i411->in1_len, 4) ;       /*  macvdata length */

   memset( cmds, 0, sizeof(cmds) ) ;
   memcpy( to->cmd, "TO", 2 ) ;
   if( i411->keytype == 0x02 ){
       to->tmkksz = 'U' ;
       memcpy(to->tmk, i411->key, 32 ) ;  /* MAC seed KEY */
       if( divlen == 32 )
           *(to->tmk+32)= '2' ;
       if( divlen == 48 )
           *(to->tmk+32)= '3' ;
       memcpy( to->tmk+33, i411->divdata, divlen ) ;
	   *(to->tmk+33+divlen) = to->tmkksz ;
       memcpy(to->tmk+34+divlen, i411->key, 32 ) ;  
	   if( divlen == 32 )
           *(to->tmk+66+divlen) ='X' ;
	   if( divlen == 48 )
           *(to->tmk+66+divlen) ='Y' ;
       memcpy( to->tmk+67+divlen, i411->icv, 16 ) ;
       memcpy( to->tmk+83+divlen, i411->input1, n ) ;
   }
   if( i411->keytype == 0x03 ){
       to->tmkksz = 'T' ;
       memcpy(to->tmk, i411->key, 48 ) ;  /* MAC seed KEY */
       if( divlen == 32 ){
           to->divlen= '2' ;
           memcpy( to->divdata, i411->divdata, divlen  ) ;
	       *(to->divdata+divlen) = to->tmkksz ;
           memcpy(to->divdata+divlen+1, i411->key, 48 ) ;  
           *(to->divdata+divlen+49) ='X' ;
           memcpy( to->divdata+divlen+50, i411->icv, 16 ) ;
           memcpy( to->divdata+divlen+66, i411->input1, n ) ;
       }
       if( divlen == 48 ){ 
           to->divlen= '3' ;
           memcpy( to->divdata, i411->divdata, divlen  ) ;
	       to->ttmksz = to->tmkksz ;
           memcpy(to->ttmk, i411->key, 48 ) ;  
           to->kschm ='Y' ;
           memcpy( to->icv, i411->icv, 16 ) ;
           memcpy( to->data, i411->input1, n ) ;
	   }
   }

   HSMLogMSG( "FN000411;", cmds, NULL ) ;
   if( o411->rc = hsmio(cmds, 0) ){
   HSMLogMSG( "FN000411;", cmds, NULL ) ;
	   return( o411->rc ) ;
   }
   if( divlen == 32 )   memcpy( o411->tac, tp->tmksz+82, 16 );
   if( divlen == 48 )   memcpy( o411->tac, tp->mab, 16 );

   HSMLogMSG( "FN000411;", cmds, NULL ) ;
   return( o411->rc ) ;
}
