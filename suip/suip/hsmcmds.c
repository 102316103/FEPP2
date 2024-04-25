#include "stdio.h"
#include "suipfp.h"

#define  ArraySize( A )	 (sizeof( A )/sizeof( A[0] ))

typedef struct {
   int  cmd ;
   int  (*f)(char *, char* ) ;
}  CmdTAB_t, *pCmdTAB_t ;
/*
//Add fn313 on 2013-07-09
//Add fn501, 502 on 2016-04-01
//Add fn411 on 2016-10-03
//Add fn205, 206 on 2017-05-22
//Add fn413, 414 on 2019-02-14
//Add fn415 on 2019-12-18
//Add fn409 on 2019-06-28
//Add fn207, 208 on 2020-07-23
//Add fn503 on 2020-09-11
*/
static CmdTAB_t fn[] = {
	0x01, fn101, 0x02, fn102, 0x03, fn103, 0x04, fn104,
	0x05, fn105, 0x07, fn107, 0x08, fn108, 0x11, fn121, 0x12, fn122,
	0x17, fn127, 0x18, fn128, 0x19, fn129,
	0x1A, fn130, 0x1E, fn134,
	0x21, fn201, 0x23, fn203, 0x24, fn204, 0x25, fn205,
	0x26, fn206, 0x27, fn207, 0x28, fn208, 0x2C, fn213,
	0x31, fn301, 0x37, fn307, 0x38, fn308, 0x39, fn309,
	0x3B, fn311, 0x3C, fn313, 
	0x41, fn401, 0x42, fn416, 0x49, fn409, 0x4B, fn411, 
	0x4D, fn413, 0x4E, fn414, 0x4F, fn415, 
	0x51, fn501, 0x52, fn502, 0x53, fn503, 0x54, fn504,
	0x61, fn601,
	0x81, fn801, 0x82, fn802, 0x83, fn803, 0x85, fn805,
	0x91, fn901, 0x92, fn902, 0x99, fn999,
} ;

static pCmdTAB_t GetCmdIDX( unsigned char *cmds )
{
   int  i ;

   for ( i = 0; i < ArraySize(fn); i++ ) {
       if ( *cmds != fn[i].cmd ) continue ;
       return( &fn[i] ) ;
   }
   return( NULL ) ;
}

int hsmcmd( char *out, char* in )
{
   pCmdTAB_t t ;

   if ( (t = GetCmdIDX(in)) == NULL ) {
      printf("can not found suip cmd\n" ) ;
      *out = 93 ;
      return( -1 ) ;
   }
   return( t->f(out, in) ) ;
}

