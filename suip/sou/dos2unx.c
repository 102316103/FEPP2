#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>

void doit( char* fname ) ;

void main( int argc, char* argv[] )
{
   int i = 1 ;

   if ( argc < 2 ) {
      fprintf( stderr, "usage: %s <filename>\n", argv[0] ) ;
      exit( 1 ) ;
   }
   for ( i = 1; i < argc; i++ ) doit( argv[i] ) ;
}

void doit( char* fname )
{
   int  sz, i, j, ifd, ofd ;
   char buf[1024], obuf[1024], outname[40] ;

   if ( (ifd = open(fname, O_RDONLY)) < 0 ) {
      fprintf( stderr, "file %s not found\n", fname ) ;
      exit( 1 ) ;
   }
   sprintf( outname, "tmp%d", getpid() ) ;
   if ( (ofd = open(outname, O_RDWR|O_CREAT|O_TRUNC, 0666)) < 0 ) {
      fprintf( stderr, "can not create tmp filed\n" ) ;
      exit( 1 ) ;
   }
   while ( sz = read(ifd, buf, sizeof(buf)) ) {
      for ( j = 0, i = 0; i < sz; i++ )
	  if ( *(buf+i) != '\r' )
	     *(obuf+j++) = *(buf+i) ;
      if ( j ) write( ofd, obuf, j ) ;
   }
   close( ifd ) ; close( ofd ) ;
   remove( fname ) ;
   rename( outname, fname ) ;
}
