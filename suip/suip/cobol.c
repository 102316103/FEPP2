#include <stdio.h>
#include <stdlib.h>

int  LONG2cobPIC( char *cob, long long dbt, char *pic )
{
   char num[20] ;

   /* sprintf( num, "%01ld.00", dbt ) ; */
   sprintf( num, "%01lli.00", dbt ) ;
   return( CDEC2cobPIC(cob, num, pic) ) ;
}

int  CDEC2cobPIC( char *cob, char *num1, char *pic )
{
   int  i, j, LEFTsignON = 0, LEFTpicSGN = 0 ;
   int  sgnofs, sign = 0, mpcdec, numdec, prev = ' ' ;
   char mpic[60], *num = num1 ;

   strcpy( mpic, pic ) ;
   UpperSTR( mpic ) ;

   if ( *num == '-' ) { sign = 1 ; num++; }

   /* PIC 9 REPLACEED by X */
   for ( i = strlen(mpic) - 1 ; i >= 0; i-- )
       if ( *(mpic+i) == '9' ) *(mpic+i) = '\02' ;

   if ( *(mpic+(strlen(mpic)-1)) == '-' ) {
      LEFTpicSGN = 1 ;
      *(mpic+(sgnofs=strlen(mpic)-1)) = '\0' ;
   }

   /* find dec point for pic */
   mpcdec = strlen(mpic) ;
   for ( i = strlen(mpic) - 1 ; i >= 0; i-- ) {
       if ( *(mpic+i) == '.' ) {
	  mpcdec = i ;
	  break ;
       }
   }

   /* find dec point for num */
   numdec = strlen(num) ;
   for ( i = strlen(num) - 1 ; i >= 0; i-- ) {
       if ( *(num+i) == '.' ) {
	  numdec = i ;
	  break ;
       }
   }
   /*  move right digits of dec point to mpic */
   for ( i = numdec + 1, j = mpcdec + 1; ; i++, j++ ) {
       if ( i >= (int) strlen(num) ) break ;
       if ( j >= (int) strlen(mpic) ) break ;
       *(mpic+j) = *(num+i) ;
   }
   /* ZERO out leftover 'X' of right side */
   for ( ; j < (int) strlen(mpic); j++ ) *(mpic+j) = '0' ;

   if ( LEFTpicSGN )
      if ( sign == 0 )
	  *(mpic+sgnofs) = ' ' ;
      else {
	  *(mpic+sgnofs) = '-' ;
          LEFTsignON = 1 ;
       }

   /*  move left digits of dec point to mpic, skipping ',' */
   for ( i = numdec - 1, j = mpcdec - 1; i >= 0 && j >= 0 ; j-- ) {
       if ( *(mpic+j) == ',' || *(mpic+j) == ':' || *(mpic+j) == '/')
	  continue ;
       if ( *(mpic+j) == '_' ) { *(mpic+j) = '-' ; continue ; }
       *(mpic + j) = *(num + i--) ;
   }
   /* handle suppress char & left over */
   for ( i = 0; i <= j; i++ ) {
       switch( *(mpic+i) ) {
	  case '-' :
	  case 'Z' :
	     *(mpic+i) = ' ' ;
	     prev = ' ' ;
	     continue ;
	  case ',' :
	     *(mpic+i) = prev ;
	     continue ;
	  case '\02' :
	     *(mpic+i) = '0' ;
	     continue ;
	  case '*' :
	     prev = '*' ;
	     continue ;
	  default :
	     break ;
       }
       break ;
   }
   if ( sign && LEFTsignON == 0 ) {
      if ( i > 0 ) i-- ;
      *(mpic+i) = '-' ;
   }
   strcpy( cob, mpic ) ;
   return( strlen(cob) ) ;
}
