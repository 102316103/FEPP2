#ifndef	_SUIP_H
#define	_SUIP_H
#ifdef WIN32
#include <windows.h>
#include <io.h>
#else
#define O_BINARY	0
#endif
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include "comfcns.h"
#include "hsmapi.h"
#include "suipfp.h"
#include "suiperr.h"

#define  MaxHSM		10
#define  MyBnkID	807    /* 永豐銀行 */
#define  SUIP_TCP_PORT	8001
#define  SUIP_TCP_PORT2	8002
#define  HSM_TCP_PORT	1500
#define  FIELDOFFSET(type, fld)  ((int)&(((type *)0)->fld))

#ifdef	__cplusplus
extern "C" {
#endif

extern int merrno ;

/*
typedef	struct	{
	char	BnkID[3] ;
	char	recind ;
	char	Rnzmkdate[4] ;
	char    Rnzmksz ;
	char	Rnzmk[16] ;
	char	Rczmk[16] ;
	char	Rntak[16] ;
	char	Rctak[16] ;
	char	RntakOK ;
	char	Rfiller[3] ;
	char	Snzmkdate[4] ;
	char    Snzmksz ;
	char	Snzmk[16] ;
	char	Sczmk[16] ;
	char	Sntak[16] ;
	char	Sctak[16] ;
	char	SntakOK ;
	char	Sfiller[3] ;
}	RmtKeyFILE_t, *pRmtKeyFILE_t ;

typedef	struct	{
	char	BnkID[3] ; // 950 
	char	recind ;
	char	nzmkdate[4] ;
	char    zmksz ;
	char	nzmk[16] ;
	char	czmk[16] ;
	char	filler ;
	char	ntakOK ;
	char	nopckey[16] ;
	char	natmkey[16] ;
	char	nrmtkey[16] ;
	char	nppkey[16] ;
	char	cfiller1[2] ;
	char	copckey[16] ;
	char	catmkey[16] ;
	char	crmtkey[16] ;
	char	cppkey[16] ;
	char	cfiller2[2] ;
}	FiscKeyFILE_t, *pFiscKeyFILE_t ;

typedef	struct	{
	char	PktLEN[4] ;
//	char	PktStatus[1] ; 
	char	PktType[2] ;
	char	PktFILLER1[32] ;
	char	PktRC[4] ;
	char	PktSubRC[4] ;
	char	PktFILLER2[18] ;
	char	PktNoREQs[2] ;
	char	PktCTX[1] ;
}	SuipPKT_t, *pSuipPKT_t ;

typedef	struct	{
	char	BnkID[4] ;			
	char	npvk[24] ;			
	char	cpvk[24] ;			
	char	c0k[24] ;			
	char	c1k[24] ;
	char	c3k[24] ;
	char	c4k[24] ;
	char	c5k[24] ;
	char	c6k[24] ;
	char	d6k[24] ;
	char	d7k[24] ;
}	MyBankKeyFILE_t, *pMyBankKeyFILE_t ;

typedef	struct	{
	char	AtmID[5] ;
	char	filler ;
	char	ntmkdate[4] ;
	char	ntmk[8] ;
	char	ctmk[8] ;
	char	tpk[8] ;
}	AtmKeyFILE_t, *pAtmKeyFILE_t ;

*/

typedef	struct	{
	char	BnkID[3] ;
	char	recind ;
	char	Rnzmkdate[4] ;
	char    Rnzmksz ;
	char	Rnzmk[16] ;
	char	Rczmk[16] ;
	char	Rntak[16] ;
	char	Rctak[16] ;
	char	RntakOK ;
	char	Rfiller[3] ;
	char	Snzmkdate[4] ;
	char    Snzmksz ;
	char	Snzmk[16] ;
	char	Sczmk[16] ;
	char	Sntak[16] ;
	char	Sctak[16] ;
	char	SntakOK ;
	char	Sfiller[3] ;
}	RmtKeyFILE_t, *pRmtKeyFILE_t ;

typedef	struct	{
	char	BnkID[3] ; /* 950 */
	char	recind ;
	char	nzmkdate[4] ;
	char    zmksz ;
	char	nzmk[16] ;
	char	czmk[16] ;
	char	filler ;
	char	ntakOK ;
	char	nopckey[16] ;
	char	natmkey[16] ;
	char	nrmtkey[16] ;
	char	nppkey[16] ;
	char	cfiller1[2] ;
	char	copckey[16] ;
	char	catmkey[16] ;
	char	crmtkey[16] ;
	char	cppkey[16] ;
	char	cfiller2[2] ;
}	FiscKeyFILE_t, *pFiscKeyFILE_t ;

typedef	struct	{
	char	BnkID[3] ;
	char	recind ;
	char	RMRcSync[4] ;
	char	RMScSync[4] ;
	char	Rfiller[2] ;
	char	RMRnSync[4] ;
	char	RMSnSync[4] ;
	char	Sfiller[2] ;
}	RmtSyncFILE_t, *pRmtSyncFILE_t ;

/*
typedef struct  {
	int 	PmMheaderSZ ;             
	short	PmNBRofHSM ;	          
	short	PmMhostPORT ;             
//	char	PmMhostNAME[20] ; 
	char	PmMhostNAME[MaxHSM][20] ; 
}	Parameter_t, *pParameter_t ;
*/

typedef	struct	{
	char	AtmID[8] ;
	char	filler ;
	char	ntmkdate[4] ;
	char	ctmk[24] ;
	char	ntmk[24] ;
	char	tpk[24] ;
	char	mak[24] ;
	char	zmk[24] ;   /* for FN000901 */
	char	ipimk[24] ; /* tcp/ip */
	char	ipcdk[24] ;
	char	ipmak[24] ;
/*	char    kpair ;    LMK PAIR */
/*	char    ksz ;      'Z' ksz = 8, 'U' ksz = 16, 'T' ksz = 24 */
/*	char    kind ;     'Z' - DES, 'U' - 2DES, 'T' - 3DES */
}	AtmKeyFILE_t, *pAtmKeyFILE_t ;

typedef	struct	{
	char	AtmID[8] ;
	char	filler ;
	char	ntmkdate[4] ;
	char	ipimk[24] ; /* tcp/ip */
	char	ipcdk[24] ;
	char	ipmak[24] ;
/*	char    kpair ;    LMK PAIR */
/*	char    ksz ;      'Z' ksz = 8, 'U' ksz = 16, 'T' ksz = 24 */
/*	char    kind ;     'Z' - DES, 'U' - 2DES, 'T' - 3DES */
}	TCPKeyFILE_t, *pTCPKeyFILE_t ;

typedef struct	{
	char	key_kind1[4] ;				/* CDK, MAC, TAC, PVK, PPK, ICC, WK */
	char	key_fn1[6] ;				/* FISC, RMR, RMS, RMF, OPC, ATM, C0, C1, C3, C4, C5, C6, D6, D7  */
	char	key_sub_code1[8] ;			/* 950, 807, Bank-id, ATM-id = 分行別(N3)+序號(N2),C0 - 23, C1 - 22, C3 - 21, C4 - 24, C5 - 25, C6 - 26, D6 - 27, D7 - 28 */
	char 	key_version1[2] ;
	char	key_type2[2] ;				/* S1 - Single DES, T2 - Double key length, T3 - Triple key length */
	char	key_kind2[4] ;
	char	key_fn2[6] ;
	char	key_sub_code2[8] ;
	char 	key_version2[2] ;
	char	key_type3[2] ;				/* S1 - Single DES, T2 - Double key length, T3 - Triple key length */
	char	key_kind3[4] ;
	char	key_fn3[6] ;
	char	key_sub_code3[8] ;
	char 	key_version3[2] ;
} KEYDATA_t, *pKEYDATA_t ;

typedef struct	{ 
	char	key_qty ;					/* key_qty : 使用幾把同的key, 最多暫定3把 */
	char	key_type[2] ;				
	char	keys[sizeof(KEYDATA_t)] ;
}	KEY_ID_t, *pKEY_ID_t ;


typedef struct	{ 
	char	datalen[4] ;   /* input_length */
	char	data[128] ;    /* input_string */
}	DATASTR_t, *pDATASTR_t ;

typedef struct	{ 
	char	fn[2] ;  /* FN */
	char	xx[2] ;  /* 00 - Reserve */
	char	yy[2] ;  /* 類別 01 - KEY, 02 - PIN, 03 - MAC, 04 - TAC, 05 - ICC */
	char	zz[2] ;  /* 功能 */
}	DESFUN_t, *pDESFUN_t ;

typedef struct	{ 
	char	key_identify[sizeof(KEY_ID_t)] ;				
	char	datalen1[4] ;
	char	data1[128] ;
	char	datalen2[4] ;
	char	data2[128] ;
}	InputStr_t, *pInputStr_t ;

typedef struct	{ 
	char	fn_count[4] ;				
	char	mutifun1[sizeof(InputStr_t)] ;
	char	mutifun2[sizeof(InputStr_t)] ;
	char	mutifun3[sizeof(InputStr_t)] ;
}	MultiStr_t, *pMultiStr_t ;


#ifdef	__cplusplus
}
#endif
#endif	/* _SUIP_H */


