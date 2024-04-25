#ifndef	_SUIPFP_H
#define	_SUIPFP_H

#ifdef	__cplusplus
extern "C" {
#endif

typedef	struct	{
	char	PktType[2] ;
	char	PktRC[4] ;
	char	PktSubRC[4] ;
	char	PktNoREQs[2] ;
	char	PktCTX[1] ;
}	SuipPKT_t, *pSuipPKT_t ;


int DesErr( char *cmds, int res ) ;
int fn101( char* out, char* in ) ;
int fn102( char* out, char* in ) ;
int fn103( char* out, char* in ) ;
int fn104( char* out, char* in ) ;
int fn105( char* out, char* in ) ;
int fn107( char* out, char* in ) ;
int fn108( char* out, char* in ) ;
int fn121( char* out, char* in ) ;
int fn122( char* out, char* in ) ;
int fn123( char* out, char* in ) ;
int fn127( char* out, char* in ) ;
int fn128( char* out, char* in ) ;
int fn129( char* out, char* in ) ;
int fn130( char* out, char* in ) ;
int fn134( char* out, char* in ) ;
int fn201( char* out, char* in ) ;
int fn203( char* out, char* in ) ;
int fn204( char* out, char* in ) ;
int fn205( char* out, char* in ) ;
int fn206( char* out, char* in ) ;
int fn207( char* out, char* in ) ;
int fn208( char* out, char* in ) ;
int fn213( char* out, char* in ) ;
int fn301( char* out, char* in ) ;
int fn307( char* out, char* in ) ;
int fn308( char* out, char* in ) ;
int fn309( char* out, char* in ) ;
int fn311( char* out, char* in ) ;
int fn313( char* out, char* in ) ;
int fn401( char* out, char* in ) ;
int fn402( char* out, char* in ) ;
int fn409( char* out, char* in ) ;
int fn411( char* out, char* in ) ;
int fn413( char* out, char* in ) ;
int fn414( char* out, char* in ) ;
int fn415( char* out, char* in ) ;
int fn416( char* out, char* in ) ;
int fn501( char* out, char* in ) ;
int fn502( char* out, char* in ) ;
int fn503( char* out, char* in ) ;
int fn504( char* out, char* in ) ;
int fn601( char* out, char* in ) ;
int fn801( char* out, char* in ) ;
int fn802( char* out, char* in ) ;
int fn803( char* out, char* in ) ;
int fn805( char* out, char* in ) ;
int fn901( char* out, char* in ) ;
int fn902( char* out, char* in ) ;
int fn999( char* out, char* in ) ;

int CmdXA( char* bufr ) ;
int CmdXC( char* bufr ) ;
void ClearCount( ) ;
int SetFuncCnts( int cmd ) ;
int GetFuncCnts( char* bufr ) ;
void CleanFuncCnts( ) ;

/*   usv3000io.c */
void usv3000f( void ) ;
void hsmprmf( void ) ;
/* int  hsmio( char* cmd3k, int isz ) ; */

unsigned char* c_offset( unsigned char* cmds, int n ) ;
char* sgCmdTXT( char* cmds, char* key, int ksz, int pair, char* icv, unsigned char* ldata ) ;
char* SSCmdTXT( char* cmds, int method, char* tpk, int ksz, char *pvk, char* pinb, char* actno, char* valdat, char* offset, char* dectab ) ;

int  retval( char* ret, int rc ) ;
int  Send2SUIP( char* cmd, int sz ) ;

int  HSMLogMSG( char* msg, ... ) ;
int  DESLogMSG( char* msg, ... ) ;

#ifdef	__cplusplus
}
#endif
#endif	/* _SUIPFP_H */
