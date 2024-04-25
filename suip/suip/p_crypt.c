#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <openssl/aes.h>
#include <openssl/des.h>

#define AES_BLOCK_SIZE   16
#define BlockSize 16

#ifndef MAX
#define MAX(a,b) ((a)>(b)?(a):(b))
#endif
#ifndef MIN
#define MIN(a,b) ((a)<(b)?(a):(b))
#endif

int p_GenRandom( char* ran, int sz )
{
   RAND_bytes(ran, sz) ;
   return( 0 ) ;
}

static int ENCRYPT_ECB( char *obuf, char *ibuf, int sz, char *key, int ksz, int enc )
{
     des_key_schedule ks, ks2, ks3;
     int i ;

     unsigned char inputBlock[8], work[8] ;

     DES_set_key_unchecked(key, &ks);
     if( ksz == 16 ) 
         DES_set_key_unchecked(key+8, &ks2);
     if( ksz == 24 ) {
         DES_set_key_unchecked(key+8, &ks2);
         DES_set_key_unchecked(key+16, &ks3);
     }

     for(i = 0; i < sz/8; i++) {
         memcpy(inputBlock, &ibuf[8*i], 8);
         if( ksz == 8 ) 
             des_ecb_encrypt(inputBlock, work, ks, enc ) ;  /*  0 - decrypt */ 
         if( ksz == 16 ) 
             des_ecb2_encrypt(inputBlock, work, ks, ks2, enc ) ;  /*  0 - decrypt */ 
         if( ksz == 24 ) 
             des_ecb3_encrypt(inputBlock, work, ks, ks2, ks3, enc ) ;  /*  0 - decrypt */ 
         memcpy(&obuf[8*i], work, 8);
     }

     return 0 ;
}

static int ENCRYPT_CBC( char *obuf, char *ibuf, int sz, char *iv, char *key, int ksz, int enc )
{
     des_key_schedule ks, ks2, ks3;

     if( ksz == 8 ) {
         DES_set_key_unchecked(key, &ks);
         des_ede3_cbc_encrypt(ibuf, obuf, sz, ks, ks, ks, iv, enc ) ;  /*  0 - decrypt */ 
     }
     if( ksz == 16 ){
         DES_set_key_unchecked(key, &ks);
         DES_set_key_unchecked(key+8, &ks2);
         des_ede3_cbc_encrypt(ibuf, obuf, sz, ks, ks2, ks, iv, enc ) ;  /*  0 - decrypt */ 
     }
     if( ksz == 24 ){
         DES_set_key_unchecked(key, &ks);
         DES_set_key_unchecked(key+8, &ks2);
         DES_set_key_unchecked(key+16, &ks3);
         des_ede3_cbc_encrypt(ibuf, obuf, sz, ks, ks2, ks3, iv, enc ) ;  /*  0 - decrypt */ 
     }

     return 0 ;
}

int p_ENCRYPT_ECB( char* obuf, char* ibuf, int sz, char* ikey, int ksz )
{
	int rc ;

	rc = ENCRYPT_ECB( obuf, ibuf, sz, ikey, ksz, 1 ) ;
	return(rc) ;
}

int p_ENCRYPT_CBC( char* obuf, char* ibuf, int sz, char* iv, char* ikey, int ksz )
{
	int rc ;

	rc = ENCRYPT_CBC( obuf, ibuf, sz, iv, ikey, ksz, 1 ) ;
	return(rc) ;
}

int p_DECRYPT_ECB( char* obuf, char* ibuf, int sz, char* ikey, int ksz )
{
	int rc ;

	rc = ENCRYPT_ECB( obuf, ibuf, sz, ikey, ksz, 0 ) ;
	return(rc) ;

}

int p_DECRYPT_CBC( char* obuf, char* ibuf, int sz, char* iv, char* ikey, int ksz )
{
	int rc ;

	rc = ENCRYPT_CBC( obuf, ibuf, sz, iv, ikey, ksz, 0 ) ;
	return(rc) ;
}

int p_ENCRYPT2_ECB(char* odat, char* idat, int sz, char* key )
{
   return( p_ENCRYPT_ECB( odat, idat, sz, key, 16 ) ) ;
}

int p_DECRYPT2_ECB(char* odat, char* idat, int sz, char* key )
{
   return( p_DECRYPT_ECB( odat, idat, sz, key, 16 ) ) ;
}

int p_ENCRYPT2_CBC(char* odat, char* idat, int sz, char* iv, char* key )
{
   return( p_ENCRYPT_CBC( odat, idat, sz, iv, key, 16 ) ) ;
}

int p_DECRYPT2_CBC(char* odat, char* idat, int sz, char* iv, char* key )
{
   return( p_DECRYPT_CBC( odat, idat, sz, iv, key, 16 ) ) ;
}

int p_ENCRYPT3_ECB(char* odat, char* idat, int sz, char* key )
{
   return( p_ENCRYPT_ECB( odat, idat, sz, key, 24 ) ) ;
}

int p_DECRYPT3_ECB(char* odat, char* idat, int sz, char* key )
{
   return( p_DECRYPT_ECB( odat, idat, sz, key, 24 ) ) ;
}

int p_ENCRYPT3_CBC(char* odat, char* idat, int sz, char* iv, char* key )
{
   return( p_ENCRYPT_CBC( odat, idat, sz, iv, key, 24 ) ) ;
}

int p_DECRYPT3_CBC(char* odat, char* idat, int sz, char* iv, char* key )
{
   return( p_DECRYPT_CBC( odat, idat, sz, iv, key, 24 ) ) ;
}

int TransLateData_CBC( char* odat, char* oicv, char* okey, int oksz, char* idat, int dsz, char* icv,  char* ikey, int iksz )
{
	char tempbuf[1024];

	memset(tempbuf, 0, sizeof(tempbuf) ) ;

	if( iksz != 16 && iksz != 24 && oksz != 16 && oksz != 24 ) return( -81 ) ;

	p_DECRYPT_CBC(tempbuf, idat, dsz, icv, ikey, iksz ) ;
	p_ENCRYPT_CBC(odat, tempbuf, dsz, oicv, okey, oksz ) ;

	return(0) ;
}

int AES_ENCRYPT_ECB( unsigned char *out, unsigned char *data, int dsz, unsigned char *aeskey, int ksz )
{
    int len = 0 ;
    AES_KEY enc_key ;

    if( ksz != 16 && ksz != 24 && ksz != 32 ) return( -24 ) ;
    if( dsz % 16 ) return( -81 ) ;

    AES_set_encrypt_key(aeskey, ksz *8, &enc_key);
    while(len < dsz) {
          AES_encrypt(data+len, out+len, &enc_key);
          len += 16;
  }   
 return( 0 ) ;
}

int AES_DECRYPT_ECB( unsigned char *out, unsigned char *data, int dsz, unsigned char *aeskey, int ksz )
{
    int len = 0 ;
    AES_KEY dec_key ;

    if( ksz != 16 && ksz != 24 && ksz != 32 ) return( -24 ) ;
    if( dsz % 16 ) return( -81 ) ;
  
    AES_set_decrypt_key(aeskey, ksz *8, &dec_key);
    while(len < dsz) {
          AES_decrypt(data+len, out+len, &dec_key);
          len += 16;
   }   
  return( 0 ) ;
}

int AES_ENCRYPT_CBC( unsigned char *c, unsigned char *p, int length, unsigned char *iv, unsigned char *key, int ksz )
{
   int i;

   /* CBC mode operates in a block-by-block fashion */ 
   while(length >= 16)
   {
      /* XOR input block with IV contents */ 
      for(i = 0; i < 16; i++)
      {
         c[i] = p[i] ^ iv[i];
      }

      /* Encrypt the current block based upon the output */ 
      /* of the previous encryption */ 
      /* cipher->encryptBlock(context, c, c); */ 
      AES_ENCRYPT_ECB( c, c, 16, key, ksz ) ;

      /* Update IV with output block contents */ 
      memcpy(iv, c, 16);

      /* Next block */ 
      p += 16;
      c += 16 ;
      length -= 16 ;
   }

   /* The plaintext must be a multiple of the block size */ 
   if(length != 0)
      return -81;

   /* Successful encryption */ 
   return 0;
}


int AES_DECRYPT_CBC( unsigned char *p, unsigned char *c, int length, unsigned char *iv, unsigned char *key, int ksz )
{
   int i;
   unsigned char t[16];

   /* CBC mode operates in a block-by-block fashion */ 
   while(length >= 16 )
   {
      /* Save input block */ 
      memcpy(t, c, 16 ) ;

      /* Decrypt the current block */ 
      /* cipher->decryptBlock(context, c, p); */ 
      AES_DECRYPT_ECB(p, c, 16, key, ksz ) ;

      /* XOR output block with IV contents */ 
      for(i = 0; i < 16; i++)
      {
         p[i] ^= iv[i];
      }

      /* Update IV with input block contents */ 
      memcpy(iv, t, 16 ) ;

      /* Next block */ 
      c += 16 ;
      p += 16 ;
      length -= 16 ;
   }

   /* The ciphertext must be a multiple of the block size */ 
   if(length != 0)
      return -81 ;

   /* Successful encryption */ 
   return 0;
}
