#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define CKR_OK                                0x00000000
#define CKR_CANCEL                            0x00000001
#define CKR_HOST_MEMORY                       0x00000002
#define CKR_GENERAL_ERROR                     0x00000005
#define CKR_FUNCTION_FAILED                   0x00000006
#define FALSE             0
#define TRUE              1

/* aaaack but it's fast and const should make it shared text page. */
static const unsigned char pr2six[256] =
{
    /* ASCII table */
    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 62, 64, 64, 64, 63,
    52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 64, 64, 64, 64, 64, 64,
    64,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
    15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 64, 64, 64, 64, 64,
    64, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
    41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 64, 64, 64, 64, 64,
    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64
};

int Base64decode_len(const char *bufcoded)
{
    int nbytesdecoded;
    register const unsigned char *bufin;
    register int nprbytes;

    bufin = (const unsigned char *) bufcoded;
    while (pr2six[*(bufin++)] <= 63);

    nprbytes = (bufin - (const unsigned char *) bufcoded) - 1;
    nbytesdecoded = ((nprbytes + 3) / 4) * 3;

    return nbytesdecoded + 1;
}

int Base64decode(char *bufplain, const char *bufcoded)
{
    int nbytesdecoded;
    register const unsigned char *bufin;
    register unsigned char *bufout;
    register int nprbytes;

    bufin = (const unsigned char *) bufcoded;
    while (pr2six[*(bufin++)] <= 63);
    nprbytes = (bufin - (const unsigned char *) bufcoded) - 1;
    nbytesdecoded = ((nprbytes + 3) / 4) * 3;

    bufout = (unsigned char *) bufplain;
    bufin = (const unsigned char *) bufcoded;

    while (nprbytes > 4) {
    *(bufout++) =
        (unsigned char) (pr2six[*bufin] << 2 | pr2six[bufin[1]] >> 4);
    *(bufout++) =
        (unsigned char) (pr2six[bufin[1]] << 4 | pr2six[bufin[2]] >> 2);
    *(bufout++) =
        (unsigned char) (pr2six[bufin[2]] << 6 | pr2six[bufin[3]]);
    bufin += 4;
    nprbytes -= 4;
    }

    /* Note: (nprbytes == 1) would be an error, so just ingore that case */
    if (nprbytes > 1) {
    *(bufout++) =
        (unsigned char) (pr2six[*bufin] << 2 | pr2six[bufin[1]] >> 4);
    }
    if (nprbytes > 2) {
    *(bufout++) =
        (unsigned char) (pr2six[bufin[1]] << 4 | pr2six[bufin[2]] >> 2);
    }
    if (nprbytes > 3) {
    *(bufout++) =
        (unsigned char) (pr2six[bufin[2]] << 6 | pr2six[bufin[3]]);
    }

    *(bufout++) = '\0';
    nbytesdecoded -= (4 - nprbytes) & 3;
    return nbytesdecoded;
}

int ber_encode_sequence( unsigned char length_only, unsigned char **seq, int *seq_len, unsigned char *data, int data_len )
{
   unsigned char   *buf = NULL;
   int   len;
/*
    if data_len < 127 use short-form length id
    if data_len < 65536 use long-form length id with 2-byte length field
*/

   if (data_len < 128)
      len = 1 + 1 + data_len;
   else if (data_len < 256)
      len = 1 + (1 + 1) + data_len;
   else if (data_len < (1 << 16))
      len = 1 + (1 + 2) + data_len;
   else if (data_len < (1 << 24))
      len = 1 + (1 + 3) + data_len;
   else{
      return CKR_FUNCTION_FAILED;
   }
   if (length_only == TRUE) {
      *seq_len = len;
      return CKR_OK;
   }

   buf = (unsigned char *)malloc( len );
   if (!buf){
      return CKR_HOST_MEMORY;
   }

   if (data_len < 128) {
      buf[0] = 0x30;       /*  constructed, SEQUENCE */
      buf[1] = data_len;
      memcpy( &buf[2], data, data_len );

      *seq_len = len;
      *seq = buf;
      return CKR_OK;
   }

   if (data_len < 256) {
      buf[0] = 0x30;       /*  constructed, SEQUENCE */
      buf[1] = 0x81;       /*  length header -- 1 length octets */
      buf[2] = data_len;

      memcpy( &buf[3], data, data_len );

      *seq_len = len;
      *seq = buf;
      return CKR_OK;
   }

   if (data_len < (1 << 16)) {
      buf[0] = 0x30;       /*  constructed, SEQUENCE */
      buf[1] = 0x82;       /*  length header -- 2 length octets */
      buf[2] = (data_len >> 8) & 0xFF;
      buf[3] = (data_len     ) & 0xFF;

      memcpy( &buf[4], data, data_len );

      *seq_len = len;
      *seq = buf;
      return CKR_OK;
   }

   if (data_len < (1 << 24)) {
      buf[0] = 0x30;       /*  constructed, SEQUENCE */
      buf[1] = 0x83;       /*  length header -- 3 length octets */
      buf[2] = (data_len >> 16) & 0xFF;
      buf[3] = (data_len >>  8) & 0xFF;
      buf[4] = (data_len      ) & 0xFF;

      memcpy( &buf[5], data, data_len );

      *seq_len = len;
      *seq = buf;
      return CKR_OK;
   }

   return CKR_FUNCTION_FAILED;
}

int ber_decode_sequence( unsigned char  * seq, unsigned char **data, unsigned int *data_len, unsigned int *field_len )
{
   int  len, length_octets;


   if (!seq){
      return CKR_FUNCTION_FAILED;
   }
   if (seq[0] != 0x30){
      return CKR_FUNCTION_FAILED;
   }
   /*  short form lengths are easy */
   if ((seq[1] & 0x80) == 0) {
      len = seq[1] & 0x7F;

      *data = &seq[2];
      *data_len  = len;
      *field_len = 1 + (1) + len;
      return CKR_OK;
   }

   length_octets = seq[1] & 0x7F;

   if (length_octets == 1) {
      len = seq[2];

      *data = &seq[3];
      *data_len  = len;
      *field_len = 1 + (1 + 1) + len;
      return CKR_OK;
   }

   if (length_octets == 2) {
      len = seq[2];
      len = len << 8;
      len |= seq[3];

      *data = &seq[4];
      *data_len  = len;
      *field_len = 1 + (1 + 2) + len;
      return CKR_OK;
   }
   if (length_octets == 3) {
      len = seq[2];
      len = len << 8;
      len |= seq[3];
      len = len << 8;
      len |= seq[4];

      *data = &seq[5];
      *data_len  = len;
      *field_len = 1 + (1 + 3) + len;
      return CKR_OK;
   }

   /*  > 3 length octets implies a length > 16MB */
   return CKR_FUNCTION_FAILED;
}

int ber_decode_int( unsigned char  *ber_int, unsigned char **data, unsigned int *data_len, unsigned int *field_len )
{
   int  len, length_octets;

   if (!ber_int){
      return CKR_FUNCTION_FAILED;
   }
   if (ber_int[0] != 0x02){
      return CKR_FUNCTION_FAILED;
   }
   /*  short form lengths are easy */

   if ((ber_int[1] & 0x80) == 0) {
      len = ber_int[1] & 0x7F;

      *data      = &ber_int[2];
      *data_len  = len;
      *field_len = 1 + 1 + len;
      return CKR_OK;
   }

   length_octets = ber_int[1] & 0x7F;

   if (length_octets == 1) {
      len = ber_int[2];

      *data      = &ber_int[3];
      *data_len  = len;
      *field_len = 1 + (1 + 1) + len;
      return CKR_OK;
   }

   if (length_octets == 2) {
      len = ber_int[2];
      len = len << 8;
      len |= ber_int[3];

      *data      = &ber_int[4];
      *data_len  = len;
      *field_len = 1 + (1 + 2) + len;
      return CKR_OK;
   }
   if (length_octets == 3) {
      len = ber_int[2];
      len = len << 8;
      len |= ber_int[3];
      len = len << 8;
      len |= ber_int[4];

      *data      = &ber_int[5];
      *data_len  = len;
      *field_len = 1 + (1 + 3) + len;
      return CKR_OK;
   }

   /*  > 3 length octets implies a length > 16MB which isn't possible for */
   /*  the coprocessor */

   return CKR_FUNCTION_FAILED;
}

int ber_decode_bitstring( unsigned char  *str, unsigned char **data, unsigned int *data_len, unsigned int *field_len ) 
{
   int  len, length_octets;

    if (!str){
      return CKR_FUNCTION_FAILED;
   }
   if (str[0] != 0x03){
      return CKR_FUNCTION_FAILED;
   }
   /*  short form lengths are easy */

   if ((str[1] & 0x80) == 0) {
	len = str[1] & 0x7F;

	*data = &str[2];
	*data_len  = len;
	*field_len = 1 + (1) + len;
	return CKR_OK;
   }

   length_octets = str[1] & 0x7F;

   if( str[1] == 0x81 ){
       len = str[2] ;
       *data_len  = len;
        if ( str[3] != 0x00 ) {
             *data = &str[3];
             *field_len = 1 + (1 + 1) + len;
        }
        else
        {
             *data = &str[4];
             *field_len = 1 + (1 + 2) + len;
        }
        return CKR_OK;
   }

   if( str[1] == 0x82 ){
       len = str[2] ;
       len = len << 8;
       len |= str[3];
       *data_len  = len;

        if ( str[4] != 0x00 ) {
             *data = &str[4];
             *field_len = 1 + (1 + 2) + len;
        }
        else
        {
             *data = &str[5];
             *field_len = 1 + (1 + 3) + len;
        }
        return CKR_OK;
   }
   /*  > 3 length octets implies a length > 16MB */

   return CKR_FUNCTION_FAILED;
}

int ber_encode_octet( unsigned char length_only, unsigned char **str, int *str_len, unsigned char *data, int data_len )
{
   unsigned char   *buf = NULL;
   int   len;
/*
    I only support Primitive encoding for OCTET STRINGS
    if data_len < 128 use short-form length id
    if data_len < 256 use long-form length id with 1-byte length field
    if data_len < 65536 use long-form length id with 2-byte length field
*/

   if (data_len < 128)
      len = 1 + 1 + data_len;
   else if (data_len < 256)
      len = 1 + (1 + 1) + data_len;
   else if (data_len < (1 << 16))
      len = 1 + (1 + 2) + data_len;
   else if (data_len < (1 << 24))
      len = 1 + (1 + 3) + data_len;
   else{
      return CKR_FUNCTION_FAILED;
   }
   if (length_only == TRUE) {
      *str_len = len;
      return CKR_OK;
   }

   buf = (unsigned char *)malloc( len );
   if (!buf){
      return CKR_HOST_MEMORY;
   }

   if (data_len < 128) {
      buf[0] = 0x04;       /*  primitive, OCTET STRING */
      buf[1] = data_len;
      memcpy( &buf[2], data, data_len );

      *str_len = len;
      *str = buf;
      return CKR_OK;
   }

   if (data_len < 256) {
      buf[0] = 0x04;       /*  primitive, OCTET STRING */
      buf[1] = 0x81;       /*  length header -- 1 length octets */
      buf[2] = data_len;

      memcpy( &buf[3], data, data_len );

      *str_len = len;
      *str = buf;
      return CKR_OK;
   }

   if (data_len < (1 << 16)) {
      buf[0] = 0x04;       /*  primitive, OCTET STRING */
      buf[1] = 0x82;       /*  length header -- 2 length octets */
      buf[2] = (data_len >> 8) & 0xFF;
      buf[3] = (data_len     ) & 0xFF;

      memcpy( &buf[4], data, data_len );

      *str_len = len;
      *str = buf;
      return CKR_OK;
   }

   if (data_len < (1 << 24)) {
      buf[0] = 0x04;       /*  primitive, OCTET STRING */
      buf[1] = 0x83;       /*  length header -- 3 length octets */
      buf[2] = (data_len >> 16) & 0xFF;
      buf[3] = (data_len >>  8) & 0xFF;
      buf[4] = (data_len      ) & 0xFF;

      memcpy( &buf[5], data, data_len );

      *str_len = len;
      *str = buf;
      return CKR_OK;
   }

   /*  we should never reach this */

   free( buf );
   return CKR_FUNCTION_FAILED;
}

unsigned char*  p11_bigint_trim(unsigned char* in, int *size)
{
   int i;

   for (i = 0; (i < *size) && in[i] == 0x00; i++);
        *size -= i;
   return (in + i);
}

void p11_attribute_trim(unsigned char *attr, int len ) {

   unsigned char* ptr;
   int i, size;

      size = len ;
      ptr = p11_bigint_trim((unsigned char *)attr, &size);
      if (ptr != attr) {
         memmove(attr, ptr, size);
      }
}


int berdec_pubkey( char* modulus, char* exponent, char* pubkey, int publen )
{
    unsigned char out[4096], *buf=NULL, *tmp = NULL  ;
    unsigned int rc, len, buf_len, offset, field_len, bits ;

    memset( out, 0, sizeof(out) ) ;
    memcpy( out, pubkey, publen ) ;

/*   pbulic key info */
    if( rc = ber_decode_sequence( out, &buf, &buf_len, &field_len ) ) return(rc);
    offset = 0;
	if( field_len <= 0 ) return( -1 ) ;

   /*  modulus */
   if( rc = ber_decode_int( buf+offset, &tmp, &len, &field_len ) ) return( rc ) ;
   p11_attribute_trim( tmp, len );
   if( len <= 0 ) return( -1 ) ;
   memcpy( modulus, tmp, len ) ;
   offset += field_len;
   len = len/8 * 8 ;
   bits = len * 8 ;

   /*  public exponent */
   if( rc = ber_decode_int( buf+offset, &tmp, &len, &field_len ) ) return( rc );
   p11_attribute_trim( tmp, len );
   if( len <= 0 ) return( -1 ) ;
   memcpy( exponent, tmp, len ) ;
   offset += field_len;
   return( bits ) ;
}

int berenc_keykcv( char *keykcv, char* key, char* kcv, int ksz )
{
    unsigned char out[4096], *buf=NULL, *tmp = NULL, *buf2=NULL  ;
    unsigned int rc, len, buf_len, offset, field_len ;

    offset = 0;
    rc = 0;

    rc |= ber_encode_octet( TRUE, NULL, &len, NULL, ksz ); offset += len;
    rc |= ber_encode_octet( TRUE, NULL, &len, NULL, 8 ); offset += len;    /*  KCV */


   buf = (unsigned char *)malloc(offset);
   offset = 0;

   if( rc = ber_encode_octet( FALSE, &buf2, &len, key, ksz ) ) return( rc ) ;
   memcpy( buf+offset, buf2, len );
   offset += len;
   free( buf2 );

   if( rc = ber_encode_octet( FALSE, &buf2, &len, kcv, 8 ) ) return( rc ) ;
   memcpy( buf+offset, buf2, len );
   offset += len;
   free( buf2 );

   if( rc = ber_encode_sequence( FALSE, &buf2, &len, buf, offset ) ) return( rc ) ;
   memcpy( keykcv, buf2, len ) ;
   return( len ) ;
}

