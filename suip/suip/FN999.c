#include "suip.h"
#include "hsmapi.h"
#include <socketio.h>

/*----------------------------------------------------------*/
/*  FUNCTION CODE FN000999 : HSM & SUIP Status              */
/*----------------------------------------------------------*/
/*
DES_LIB("FN000992"¡A"1S1HSM,  ""¡A""¡AReturn_data_1¡A""¡AReturn_Code)

Input Parameters:
1.HSM
2.SUIP

Output Parameters:
*/

typedef struct	{
	char	f ;					/* FUNCTION CODE */
	char    cmd ;
}	fn999i_t, *pfn999i_t ;

typedef struct	{
	int 	rc ;
	char	status[1024] ;
}	fn999o_t, *pfn999o_t ;

static int Send2HSM( char* IP, int port, char *cmd, int csz ) ;

int fn999( char* out, char* in ) 
{
   char cmds[2048], counter[2048] ; 
   pfn999i_t i999 = (pfn999i_t) in ;
   pfn999o_t o999 = (pfn999o_t) out ;

/* 2012/01/19 modify HSM status */
   memset( cmds, 0, sizeof(cmds) ) ;
   memset( counter, 0, sizeof(counter) ) ;


   if( i999->cmd == 0x02 ) { 
       CmdXA( cmds ) ;   
       strcpy( o999->status, cmds ) ;
       return( o999->rc = 0 ) ;
   }

   if( i999->cmd == 0x03 ){  
       GetFuncCnts( cmds ) ;   
       strcpy( o999->status, cmds ) ;
       GetHsmCount(counter) ; 
       strcpy( o999->status+strlen(cmds), counter ) ;
       return( o999->rc = 0 ) ;
   }

   if( i999->cmd == 0x04 ) {
       CleanFuncCnts() ;
       ClearHsmCount() ; 
       return( o999->rc = 0 ) ;
   }
   return( o999->rc = -99 ) ;
}

  
