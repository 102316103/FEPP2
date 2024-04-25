#ifndef	_HSMIO_H
#define	_HSMIO_H
#include "comfcns.h"
#include "svsocknt.h"
#include "socketio.h"
#define  NamePrm3K	"../LOG/HsmPRM.dat"
/*
//#define  NameLog3K	"m3000sui.log"
*/

#ifdef __cplusplus
extern "C" {
#endif
#define MaxHSM			10
#define MaxCONN			40
#define RsaBYTES		64

#define E_USV_NO_CLRCHNL	-90
#define E_USV_DEC_PKT		-91
#define E_USV_ENC_PKT		-92
#define E_USV_SAM_INIT		-97
#define E_USV_HSM_DOWN		-98
#define E_USV_NO_SECCHNL	-99
#define E_USV_NO_PRM		-81
#define E_USV_INVALID_PRM	-82
#define E_USV_NO_ATMKeyFile	-83
#define E_USV_NO_RMTKeyFile	-84
#define E_USV_KEY_IO_ERR	-89


typedef struct  {
	short  	PmTimeSTAMP ;	          
	short	PmCmdTimeout ;            
	char 	PmHSMheaderSZ ;	          
	short	PmMhostPORT ;             
	char	PmMhostNAME[MaxHSM][20] ; 
	char	PmChostNAME[MaxCONN][20] ;   /* modify 2012/05/28 */
}       Parameter_t, *pParameter_t ;


typedef	struct { /* this struct is to redefine pCntx->reserved */
	int    secchl ;
}	VsecDEF_t, *pVsecDEF_t ;

typedef	struct	{
	char	ncmd ; /* number of command, sign bit on - key in hex format */
	char	cmd ;  /* 0 - read, 1 - write, 2 - replace */
	short	foffset ;
	char	KeyID[16] ;
}	Usv2000Cmd_t, *pUsv2000Cmd_t ;

typedef	struct	{
	char	cmd[2] ; /* "XA" */
}	UsvCMDzXA_t, *pUsvCMDzXA_t ;

typedef	struct	{
	uchar	ConID[1] ;
	uchar	IpADDR[4] ;
	uchar	port[2] ;
	uchar	status[1] ;
}	StrCMDzXB_t, *pStrCMDzXB_t ;

typedef	struct	{
	char	cmd[2] ; /* "XB" */
	char	error[2] ;
	StrCMDzXB_t xbstr ;
}	UsvCMDzXB_t, *pUsvCMDzXB_t ;

typedef	struct	{
	char	cmd[2] ; /* "XC" */
	uchar	ConID ;
}	UsvCMDzXC_t, *pUsvCMDzXC_t ;

typedef	struct	{
	uint	totcnt ;
	uint	okcnt ;
	uint	parcnt ;
	uint	nokcnt ;
}	StrCMDzXD_t, *pStrCMDzXD_t ;

typedef	struct	{
	char	cmd[2] ; /* "XD" */
	char	error[2] ;
	uchar	status[1] ;
	uchar	cnts[3] ;
}	UsvCMDzXD_t, *pUsvCMDzXD_t ;

int  hsmio( pCtxtKEY_t pCntx, int sz ) ;
int  usvReInitSOC( int sechl ) ;
void wakeup_handler( void ) ;

#ifdef __cplusplus
}
#endif
#endif	/* _HSMIO_H */
