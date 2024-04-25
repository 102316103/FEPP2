#ifndef	_ASCEBC_H
#define	_ASCEBC_H
#ifdef	__cplusplus
extern "C" {
#endif

void asc2ebc( unsigned char* ebc, unsigned char* asc, int sz ) ;
void ebc2asc( unsigned char* asc, unsigned char* ebc, int sz ) ;

#ifdef	__cplusplus
}
#endif
#endif	/* _ASCEBC_H */
