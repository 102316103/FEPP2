#include <stdio.h>
#include <stdlib.h>
#include <ntgetopt.h>

/* Utility function to extract option flags from the command line.  */


/*
argv is the command line.
   The options, if any, start with a '-' in argv[1], argv[2], ...
   OptStr is a text string containing all possible options,
   These flags are set if and only if the corresponding option
   character occurs in argv [1], argv [2], ...
   The return value is the argv index of the first argument beyond the options.
*/

int nt_getopt (int argc, char* argv[], char* OptStr, char* bol )
{
   int i, j ;

   for ( i = 0; OptStr[i] ; i++ ) {
       *(bol+i) = 0 ;
       for ( j = 1; j < argc ; j++ ) {
	   if ( argv[j][0] != '-' ) continue ;
	   if ( argv[j][1] == OptStr[i] ) {
	      *(bol+i) = -1 ;
	      if ( (j+1) < argc ) {
	         if ( argv[j+1][0] != '-' )
		    *(bol+i) = j+1 ;
	      }
	      break ;
	   }
       }
   }
   for ( i = 1; i < argc && argv[i][0] == '-'; i++) ;
   return( i ) ;
}
