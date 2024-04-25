#include <stdio.h>
#include <fcntl.h>
#include <openssl/rsa.h>

int RSAPublicEncrypt(  unsigned char* output, int *outputLen, unsigned char *data, int dsz, unsigned char *modulus, unsigned char *exponent, int bits )
{
    int elen ;
    RSA *pubkey ; 
    char rsa_e[6], rsa_n[1024], encrypt[1024] ;

    pubkey = RSA_new();

/*
    UnpackCHARS( rsa_e, exponent, 3 ) ;
    UnpackCHARS( rsa_n, modulus, bits/8 ) ;
    BN_hex2bn(&(pubkey->e), rsa_e);
    BN_hex2bn(&(pubkey->n), rsa_n);
*/

    pubkey->e = BN_bin2bn(exponent, 3, NULL );
    pubkey->n = BN_bin2bn(modulus, bits/8, NULL );

    elen = RSA_public_encrypt(dsz, data, encrypt, pubkey, RSA_PKCS1_PADDING ) ;
    *outputLen = elen ;

    memcpy( output, encrypt, elen ) ;

    return(0) ;
}

