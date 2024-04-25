#include <stdio.h>
#include <fcntl.h>
#include <openssl/hmac.h>

int hmac(int hashtype, const unsigned char *data, size_t len, const unsigned char *key, int len_key, unsigned char *out)
{
    int dsz ;
    unsigned char hmac[128] ;

    memset( hmac, '\0', sizeof(hmac) ) ;
    switch( hashtype )
    {
            case 0:
               HMAC(EVP_sha1(), key, len_key, data, len, hmac, &dsz);
               break ;
            case 1:
               HMAC(EVP_sha224(), key, len_key, data, len, hmac, &dsz);
               break ;
            case 2:
               HMAC(EVP_sha256(), key, len_key, data, len, hmac, &dsz);
               break ;
            case 3:
               HMAC(EVP_sha384(), key, len_key, data, len, hmac, &dsz);
              break ;
            case 4:
               HMAC(EVP_sha512(), key, len_key, data, len, hmac, &dsz);
              break ;
	   default:
	      return( -79 ) ;
    }
    memcpy( out, hmac, dsz ) ;
    return(0) ;
}

