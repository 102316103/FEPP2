#ifndef	_SUIPERR_H
#define	_SUIPERR_H
#include <errno.h>
#ifdef	__cplusplus
extern "C" {
#endif
#define	 E_HSM_DOWN		    91
#define	 E_HSM_INVALID		93
/*
//#define	 E_PRM_INVALID		94
//#define	 E_PRM_NOTFOUND		95
*/
#define	 E_KEYDB_NOTFOUND	-87
/*
//#define	 E_UPDATE_INVALID	98
//#define	 E_READ_INVALID 	99
*/
#define	 E_MAC_INVALID		101
#define	 E_TAC_INVALID		102
#define	 E_KEY_INVALID		5003
#define	 E_ATM_INVALID		5201
#define	 E_BR_INVALID		5211
#define	 E_NO_VOICEPVK		5212
#ifdef	__cplusplus
}
#endif
#endif	/* _SUIPERR_H */
