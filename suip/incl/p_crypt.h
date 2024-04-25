#ifndef _P_CRYPT_H
#define _P_CRYPT_H
#ifdef __cplusplus
extern "C" {
#endif

int p_ENCRYPT1_ECB(char* odat, char* idat, int sz, char* key ) ;
int p_DECRYPT1_ECB(char* odat, char* idat, int sz, char* key ) ;
int p_ENCRYPT1_CBC(char* odat, char* idat, int sz, char* iv, char* key ) ;
int p_DECRYPT1_CBC(char* odat, char* idat, int sz, char* iv, char* key ) ;

int p_ENCRYPT2_ECB(char* odat, char* idat, int sz, char* key ) ;
int p_DECRYPT2_ECB(char* odat, char* idat, int sz, char* key ) ;
int p_ENCRYPT2_CBC(char* odat, char* idat, int sz, char* iv, char* key ) ;
int p_DECRYPT2_CBC(char* odat, char* idat, int sz, char* iv, char* key ) ;

int p_GenRandom( char* ran, int sz ) ;

int p_ENCRYPT3_ECB(char* odat, char* idat, int sz, char* key ) ;
int p_DECRYPT3_ECB(char* odat, char* idat, int sz, char* key ) ;
int p_ENCRYPT3_CBC(char* odat, char* idat, int sz, char* iv, char* key ) ;
int p_DECRYPT3_CBC(char* odat, char* idat, int sz, char* iv, char* key ) ;

int TransLateData_CBC( char* odat, char* oicv, char* okey, int oksz, char* idat, int dsz, char* icv,  char* ikey, int iksz ) ;

#ifdef __cplusplus
}
#endif
#endif  /* _P_CRYPT_H */
