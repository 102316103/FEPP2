#include "suipfp.h"

unsigned char* c_offset( unsigned char* cmds, int n )
{
   unsigned char *ptr = cmds ;

   while ( n-- ) ptr = ptr + (*ptr+1) ;
   return( ptr ) ;
}
