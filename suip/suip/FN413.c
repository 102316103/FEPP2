#include "suip.h"
#include "hsmapi.h"
#include "svsocknt.h"
/*---------------------------------------------------------------------------*/
/*  FUNCTION CODE FN000413 :                                                 */
/*---------------------------------------------------------------------------*/
/*
DES_LIB("FN000413"£¬"1T3ICC C6    807     "£¬Input_data_1£¬Input_data_2£¬""£¬""£¬Return_Code)

Input Parameters:
Output Parameters£ºN/A
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
}	fn413i_t, *pfn413i_t ;

typedef struct	{
	int 	rc ;			/* RETURN CODE  */
	char	tac[128] ;   	    /*  tac(H8)    */
}	fn413o_t, *pfn413o_t ;

int fn413( char* out, char* in ) 
{
   int dsz, divlen, n, count ;
   char cmds[512], msg[128], xcmds[512] ;
   pfn413i_t i413 = (pfn413i_t) in ;
   pfn413o_t o413 = (pfn413o_t) out ;

   CMDxTO *to = (CMDxTO *) cmds ;
   CMDxTP *tp = (CMDxTP *) xcmds ;
   
   divlen = ldrint(i413->in2_len, 4) - 16 ;  /*  divdata length */
        dsz = ldrint(i413->in1_len, 4) ;       /*  macvdata length */

   if( dsz % 16 ) return( -81 ) ;
   count = (dsz-32) / 16  ;

   memset( cmds, 0, sizeof(cmds) ) ;
   memset( xcmds, 0, sizeof(xcmds) ) ;
   memset( msg, 0, sizeof(msg) ) ;
   memcpy( to->cmd, "TO", 2 ) ;
   if( i413->keytype == 0x02 ){
       to->tmkksz = 'U' ;
       memcpy(to->tmk, i413->key, 32 ) ;  /* MAC seed KEY */
       if( divlen == 32 )
           *(to->tmk+32)= '2' ;
       if( divlen == 48 )
           *(to->tmk+32)= '3' ;
       memcpy( to->tmk+33, i413->divdata, divlen ) ;
	   *(to->tmk+33+divlen) = to->tmkksz ;
       memcpy(to->tmk+34+divlen, i413->key, 32 ) ;  
	   if( divlen == 32 )
           *(to->tmk+66+divlen) ='X' ;
	   if( divlen == 48 )
           *(to->tmk+66+divlen) ='Y' ;
/*  encrypt CardNo 32bytes */
       memcpy( to->tmk+67+divlen, i413->icv, 16 ) ;
       memcpy( to->tmk+83+divlen, i413->input1, 16 ) ;
	   memcpy( xcmds, cmds, strlen( cmds ) ) ;
       HSMLogMSG( "FN000413;", xcmds, NULL ) ;
	   if( o413->rc = hsmio(xcmds, 0) ){
           HSMLogMSG( "FN000413;", xcmds, NULL ) ;
		   return( o413->rc ) ;
	  }
      HSMLogMSG( "FN000413;", xcmds, NULL ) ;
	  if( divlen == 32 )
		   memcpy( msg, tp->error+84, 16 ) ;
	  else
		   memcpy( msg, tp->mab, 16 ) ;

       memcpy( to->tmk+67+divlen, msg, 16 ) ;
       memcpy( to->tmk+83+divlen, i413->input1+16, 16 ) ;
	   memcpy( xcmds, cmds, strlen( cmds ) ) ;
       HSMLogMSG( "FN000413;", xcmds, NULL ) ;
	   if( o413->rc = hsmio(xcmds, 0) ){
           HSMLogMSG( "FN000413;", xcmds, NULL ) ;
		   return( o413->rc ) ;
	  }
      HSMLogMSG( "FN000413;", xcmds, NULL ) ;
	  if( divlen == 32 )
		   memcpy( msg+16, tp->error+84, 16 ) ;
	  else
		   memcpy( msg+16, tp->mab, 16 ) ;

/* encrypt pin & cvv */
       memcpy( to->tmk+67+divlen, i413->icv, 16 ) ;
	   for( n = 0; n < count; n++ ){
            memcpy( to->tmk+83+divlen, i413->input1+32+(n*16), 16 ) ;
			memcpy( xcmds, cmds, strlen( cmds ) ) ;
                HSMLogMSG( "FN000413;", xcmds, NULL ) ;
				if( o413->rc = hsmio(xcmds, 0) ){
                HSMLogMSG( "FN000413;", xcmds, NULL ) ;
				return( o413->rc ) ;
			}
            HSMLogMSG( "FN000413;", xcmds, NULL ) ;
			if( divlen == 32 )
				 memcpy( msg+32+(n*16), tp->error+84, 16 ) ;
			 else
				 memcpy( msg+32+(n*16), tp->mab, 16 ) ;
	   }
   }


   if( i413->keytype == 0x03 ){
       to->tmkksz = 'T' ;
       memcpy(to->tmk, i413->key, 48 ) ;  /* MAC seed KEY */
       if( divlen == 32 ){
           to->divlen= '2' ;
           memcpy( to->divdata, i413->divdata, divlen  ) ;
	       *(to->divdata+divlen) = to->tmkksz ;
           memcpy(to->divdata+divlen+1, i413->key, 48 ) ;  
           *(to->divdata+divlen+49) ='X' ;
/* encrypt cardno 32bytes */
           memcpy( to->divdata+divlen+50, i413->icv, 16 ) ;
           memcpy( to->divdata+divlen+66, i413->input1, 16 ) ;
		   memcpy( xcmds, cmds, strlen( cmds ) ) ;
           HSMLogMSG( "FN000413;", xcmds, NULL ) ;
		   if( o413->rc = hsmio(xcmds, 0) ){
               HSMLogMSG( "FN000413;", xcmds, NULL ) ;
			   return( o413->rc ) ;
		   }
           HSMLogMSG( "FN000413;", xcmds, NULL ) ;
		   memcpy( msg, tp->error+84, 16 ) ;

           memcpy( to->divdata+divlen+50, msg, 16 ) ;
           memcpy( to->divdata+divlen+66, i413->input1+16, 16 ) ;
		   memcpy( xcmds, cmds, strlen( cmds ) ) ;
           HSMLogMSG( "FN000413;", xcmds, NULL ) ;
		   if( o413->rc = hsmio(xcmds, 0) ){
               HSMLogMSG( "FN000413;", xcmds, NULL ) ;
			   return( o413->rc ) ;
		   }
           HSMLogMSG( "FN000413;", xcmds, NULL ) ;
		   memcpy( msg+16, tp->error+84, 16 ) ;

/* encrypt pin & cvv */
           memcpy( to->divdata+divlen+50, i413->icv, 16 ) ;
	       for( n = 0; n < count; n++ ){
                memcpy( to->divdata+divlen+66, i413->input1+32+(n*16), 16 ) ;
			    memcpy( xcmds, cmds, strlen( cmds ) ) ;
                HSMLogMSG( "FN000413;", xcmds, NULL ) ;
				if( o413->rc = hsmio(xcmds, 0) ){
                   HSMLogMSG( "FN000413;", xcmds, NULL ) ;
				   return( o413->rc ) ;
			   }
                HSMLogMSG( "FN000413;", xcmds, NULL ) ;
				memcpy( msg+32+(n*16), tp->error+84, 16 ) ;
	       }

       }
       if( divlen == 48 ){ 
           to->divlen= '3' ;
           memcpy( to->divdata, i413->divdata, divlen  ) ;
	       to->ttmksz = to->tmkksz ;
           memcpy(to->ttmk, i413->key, 48 ) ;  
           to->kschm ='Y' ;
/* encrypt cardno 32bytes */
           memcpy( to->icv, i413->icv, 16 ) ;
           memcpy( to->data, i413->input1, 16 ) ;
		   memcpy( xcmds, cmds, strlen( cmds ) ) ;
           HSMLogMSG( "FN000413;", xcmds, NULL ) ;
           if( o413->rc = hsmio(xcmds, 0) ){
               HSMLogMSG( "FN000413;", xcmds, NULL ) ;
			   return( o413->rc ) ;
		   }
		   HSMLogMSG( "FN000413;", xcmds, NULL ) ;
		   memcpy( msg, tp->mab, 16 ) ;

           memcpy( to->icv, msg, 16 ) ;
           memcpy( to->data, i413->input1+16, 16 ) ;
		   memcpy( xcmds, cmds, strlen( cmds ) ) ;
           HSMLogMSG( "FN000413;", xcmds, NULL ) ;
           if( o413->rc = hsmio(xcmds, 0) ){
               HSMLogMSG( "FN000413;", xcmds, NULL ) ;
			   return( o413->rc ) ;
		   }
		   HSMLogMSG( "FN000413;", xcmds, NULL ) ;
		   memcpy( msg+16, tp->mab, 16 ) ;

/* encrypt pin & cvv */
           memcpy( to->icv, i413->icv, 16 ) ;
	       for( n = 0; n < count; n++ ){
                memcpy( to->data, i413->input1+32+(n*16), 16 ) ;
			   	memcpy( xcmds, cmds, strlen( cmds ) ) ;
                HSMLogMSG( "FN000413;", xcmds, NULL ) ;
                if( o413->rc = hsmio(xcmds, 0) ){
                    HSMLogMSG( "FN000413;", xcmds, NULL ) ;
				    return( o413->rc ) ;
			   }
			   HSMLogMSG( "FN000413;", xcmds, NULL ) ;
			   memcpy( msg+32+(n*16), tp->mab, 16 ) ;
	       }
	   }
   }
   memcpy( o413->tac, msg, dsz );
   return( o413->rc ) ;
}
