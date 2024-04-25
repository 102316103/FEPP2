#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static void make_kn(unsigned char *k1, const unsigned char *l, int bl)
{
    int i;
    unsigned char c = l[0], carry = c >> 7, cnext;

    /* Shift block to left, including carry */
    for (i = 0; i < bl - 1; i++, c = cnext)
        k1[i] = (c << 1) | ((cnext = l[i + 1]) >> 7);

    /* If MSB set fixup with R */
    k1[i] = (c << 1) ^ ((0 - carry) & (bl == 16 ? 0x87 : 0x1b));
}

void padding( unsigned char *lastb, unsigned char *pad, int length )
{
     int         j;

     /* original last block */
     for ( j=0; j<16; j++ ) {
          if ( j < length ) {
              pad[j] = lastb[j];
        } else if ( j == length ) {
            pad[j] = 0x80;
           } else {
            pad[j] = 0x00;
        }
     }
}

void AES_CMAC( unsigned char *mac, unsigned char *input, int length, unsigned char *key, int ksz )
{
      int n, i, flag;
      unsigned char X[16],Y[16], Z[16], L[16], M_last[16], padded[16];
      unsigned char K1[16], K2[16];

      memset( Z, 0, sizeof( Z ) ) ;
      AES_ENCRYPT_ECB( L, Z, 16, key, ksz ) ;
      make_kn(K1, L, 16 );
      make_kn(K2, K1, 16 );

      n = (length+15) / 16;       /* n is number of rounds */

      if ( n == 0 ) {
          n = 1;
          flag = 0;
      } else {
          if ( (length%16) == 0 ) { /* last block is a complete block */
              flag = 1;
          } else { /* last block is not complete block */
              flag = 0;
          }
      }

      if ( flag ) { /* last block is complete block */
          XorDATA(M_last, &input[16*(n-1)], K1, 16 );
      } else {
          padding(&input[16*(n-1)],padded,length%16);
          XorDATA( M_last, padded, K2, 16 );
      }

      for ( i=0; i<16; i++ ) X[i] = 0;
      for ( i=0; i<n-1; i++ ) {
          XorDATA( Y, X, &input[16*i], 16 ); /* Y := Mi (+) X  */
          AES_ENCRYPT_ECB( X, Y, 16, key, ksz ) ;
      }

      XorDATA(Y, X, M_last, 16 );
      AES_ENCRYPT_ECB( X, Y, 16, key, ksz ) ;

      for ( i=0; i<16; i++ ) {
          mac[i] = X[i];
      }
}


