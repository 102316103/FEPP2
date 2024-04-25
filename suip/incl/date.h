#ifndef _DATE_H
#define _DATE_H

#ifdef	__cplusplus
extern "C" {
#endif

long gmLocalYYMMDD( long *localtm, long *gmt ) ;
long gmLocalMMDD( long *hhmmss, long *gmtmmddhhmmss ) ;
long LocalYYMMDD( long *localtm ) ;
long CCYYMMDD( long *localtm ) ;
long gmMMDDandTIME( long *localyymmdd, long *localhhmmss ) ;
char* AscLcaDate( char* ldat, char* ltm ) ;

#ifdef	__cplusplus
}
#endif
#endif /* _DATE_H */
