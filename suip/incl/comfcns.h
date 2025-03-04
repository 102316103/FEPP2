#ifndef _COMFCNS_H
#define _COMFCNS_H
#ifdef WIN32
#include <windows.h>
#include <io.h>
#else
#define O_BINARY	0
#endif
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <string.h>
#include <errno.h>

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
#ifndef	NOS
#define NOS(type, fld)	((int)&(((type *)0)->fld))
#endif	

#ifndef	NSZ
#define NSZ(type, fld)  ((int)sizeof((((type *)0)->fld)))
#endif	

#ifndef	TSZ
#define TSZ(array)  (sizeof(array)/sizeof(array[0]))
#endif	

#ifndef __UCHAR_t__
#define __UCHAR_t__
typedef	unsigned char	uchar ;
#endif
#ifdef WIN32
#ifndef __ULONG_t__
#define __ULONG_t__
typedef	unsigned long	ulong ;
#endif
#ifndef __UINT_t__
#define __UINT_t__
typedef	unsigned int	uint ;
#endif
#ifndef __USHORT_t__
#define __USHORT_t__
typedef	unsigned short	ushort ;
#endif
#else
#include <sys/types.h>
#endif


#define	ErrMSG	0x0001
#define CONT	0x0002
#define NOLF	0x0004
#define NONL	0x0004

int  IsFileThisYear( char* f, int rm ) ; /* rm == 1 && file not this year
					    remove this file */
int  IsLocalDate( int date ) ; /* date is in national date format */
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
void stchars_r( char *str, char *to, int f_len ) ;
long ldlint( char *ptr, int n ) ;
int  ldrint( char *ptr, int n ) ;
void stlint( long from, char *to, int width ) ;
void stlints( long from, char *to, int width ) ;
void strint( int from, char *to, int width ) ;
long ldlbcd( char *ptr, int width ) ;
void stlbcd( long from, char *to, int width ) ;
double  ldxdecimal( char* from, int width ) ;
double  ldxdouble( char* from, int width ) ;
void stxdouble( double d_value, char* buf, int width ) ;
void stxdecimal( double d_value, char* buf, int width ) ;
void dbltodecimal( char* buffer, double doub_val, int sign, int len, int dec) ;
void HEXDUMP( char* str, uchar* hex, int sz ) ;
void CHARDUMP( char* str, uchar* hex, int sz ) ;
void PutDumpFile( char* pgm, char* str, uchar* data, int sz ) ;
void HEXDUMP_f( char* pgm, char* str, uchar* hex, int sz ) ;
void CHARDUMP_f( char* pgm, char* str, uchar* hex, int sz ) ;
void nCHARDUMP_f( char* pgm, char* str, int va ) ;
void HEXDUMPx( char* pgm, char* str, uchar* hex, int sz ) ;
void CHARDUMPx( char* pgm, char* str, uchar* hex, int sz ) ;
void CHARDUMP_f1( char* pgm, char* str ) ;
void UPPERnSTR( uchar* str, int sz ) ;
void UpperSTR( uchar* str ) ;
void WriteLOG( int obj, char type, char *text1, long size ) ;
void CloseMyLOG( void ) ;
void CloseDumpFILE( void ) ;
int  ExportDumpFILE( void ) ;
void HEXLOG( char *xtr, char *str1, int sz ) ;
int  HexDEC( uchar* cmds, ... ) ;
long CCYYMMDD( long *localtm ) ;

#define	OCFN_ECHO	0x01
#define	OCFN_LF		0x02

int  password( char* pwd, int n ) ;

#ifdef WIN32
int  ONECHAR( int echoLF ) ;
#endif

#ifdef	__cplusplus
}
#endif

#endif /* _COMFCNS_H  */
