#ifndef _COMFCNS_H
#define _COMFCNS_H
/* #include "stdcall.h" */

#ifdef	__cplusplus
extern "C" {
#endif

#ifndef	ldint
#define BYTEMASK  0xFF		/* mask for one byte		*/
#define BYTESHFT  8		/* shift for one byte		*/
#define ldint(p)	((short)((((char*)p)[0]<<BYTESHFT)+(((char*)p)[1]&BYTEMASK)))
#define stint(i,p)	(((char*)p)[0]=(char) ((i)>>BYTESHFT),((char*)p)[1]=(char) (i))
#define stwint(p)	{int i=p;stint(i,&p);}
#endif	

#ifndef	OFFSET
#define OFFSET(type, fld)	((int)&(((type *)0)->fld))
#endif	

#ifndef __UCHAR_t__
#define __UCHAR_t__
typedef	unsigned char	uchar ;
#endif

#define	ErrMSG	0x0001
#define CONT	0x0002
#define NOLF	0x0004
#define NONL	0x0004

int  Tolower( int c ) ;
int  Toupper( int c ) ;
int  XorCHARS( uchar *os, uchar *a, uchar *b, int sz ) ;
int  XorDATA( uchar *os, uchar *a, uchar *b, int sz ) ;
char *UnpackCHARS(uchar *hex, uchar *str, int sz ) ;
uchar *upkstr(uchar *hex, uchar *str, int sz ) ;
int  CHARStoHEX( uchar *hex, uchar *str, int sz ) ;
char *PackHEX( uchar *str, uchar *hex, int sz ) ;
int  HEXtoCHARS( uchar *str, uchar *hex, int sz ) ;
int  cpstring( char *dest,char *sour,int len ) ;
uchar *cpstrs( uchar *str, uchar *hex, int sz ) ;
int  AsciiToHex( int asc ) ;
int  HexToAscii( int hex ) ;
int  HexToDigit( int hex ) ;
char *hexncpy( char *str, char *hex, int sz ) ;
char *ldchars( char *from, char *to, int width ) ;
char *ldtoks( char *from, char *to, int to_sz ) ;
void stchars( char *str, char *to, int f_len ) ;
long ldlint( char *ptr, int n ) ;
int  ldrint( char *ptr, int n ) ;
void stlint( long from, char *to, int width ) ;
void stlints( long from, char *to, int width ) ;
void strint( int from, char *to, int width ) ;
long ldlbcd( char *ptr, int width ) ;
void stlbcd( long from, char *to, int width ) ;
void HEXDUMP( uchar* str, uchar* hex, int sz ) ;
void CHARDUMP( uchar* str, uchar* hex, int sz ) ;
void UPPERnSTR( uchar* str, int sz ) ;
void UpperSTR( uchar* str ) ;
void WriteLOG( int obj, char type, char *text1, long size ) ;
void CloseMyLOG( void ) ;
void HEXLOG( char *xtr, char *str1, int sz ) ;
int  HexDEC( uchar* cmds, ... ) ;

#define	OCFN_ECHO	0x01
#define	OCFN_LF		0x02

#ifdef WIN32
int  ONECHAR( int echoLF ) ;
int  password( char* pwd, int n ) ;
#endif

#ifdef	__cplusplus
}
#endif

#endif /* _COMFCNS_H  */
