#ifdef WIN32
#include <windows.h>
#include <io.h>
#else
#define  O_BINARY	0
#include <signal.h>
#endif
#include <stdio.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <memory.h>
#include <string.h>
#include "hsmio.h"
#include "comfcns.h"
#include "usleep.h"
#include "date.h"
#include "logmsg.h"
#include <pthread.h>

#define  htoni(c)	htonl((long) c)
#define  DEBUG_LOG(c)	c

pthread_t thrdID;
pthread_mutex_t mutex;

static int  backend_available( char* prmbuf ) ;

typedef	struct	{
	char	HSMhostNAME[20] ;
	SOCKET	sock ;
	long long okcnt ;
	uint	parcnt ;
	uint	nokcnt ;
	char	status ;
	char	secchl ;
	char	filler[2] ;
} SUIPSocTAB_t, *pSUIPSocTAB_t ;

static SUIPSocTAB_t MySocTAB[MaxHSM] ;
static pSUIPSocTAB_t MyTAB = NULL ;
static short port ;
DEBUG_LOG(static int   lfd = -1 ;)
static int   ctmva = 1, MyTimeSTAMP = -1 ;
static time_t stmtime ;
static int MyCurrIX = 0, MyTabIX = -1 ;
static int HsmHeaderSZ = 0 ;
int    CltHeaderSZ = 0, f_err_code = -1 ;
extern int _xSVapALIVE_ ;
unsigned long cnipadr[MaxCONN] ;

static void reconnet( int i ) 
{
     pthread_mutex_lock(&mutex);
     if( (MyTAB+i)->status == 0 ){
         (MyTAB+i)->sock = CMNInitSOCKET( (MyTAB+i)->HSMhostNAME, port ) ;
         if ( (MyTAB+i)->sock != INVALID_SOCKET ) {
            HSMLogMSG("connect to HSM ip ",  (MyTAB+i)->HSMhostNAME, " successfully", NULL);
            (MyTAB+i)->status = 1 ;  /* set HSM status alive */
         }
     }
     pthread_mutex_unlock(&mutex);
     return 0;
}

static int backend_available( char* mbuf )
{
   int  fd, i, j ;
   SOCKET sock = INVALID_SOCKET ;
   char prmbuf[sizeof(Parameter_t)] ;
   pParameter_t pm = (pParameter_t) prmbuf ;
   struct stat *st = (struct stat *) prmbuf ;

   MyTAB = MySocTAB ;
   memset( (char*) MyTAB, 0, sizeof(MySocTAB) ) ;
   if ( mbuf != NULL )
      pm = (pParameter_t) mbuf ;
   else {
      if ( stat(NamePrm3K, st) ) return( -1 ) ;
      stmtime = st->st_mtime ;
      if ( (fd = open(NamePrm3K, O_RDONLY|O_BINARY)) < 0 )
         return( E_USV_NO_PRM ) ;
      read( fd, prmbuf, sizeof(prmbuf) ) ;
      close( fd ) ;
   }
   HsmHeaderSZ = pm->PmHSMheaderSZ ;
   ctmva  = pm->PmCmdTimeout ;
   port = pm->PmMhostPORT ;
   MyTimeSTAMP = pm->PmTimeSTAMP ;
   for ( i = 0; i < MaxCONN; i++ ) {
       if ( strlen(pm->PmChostNAME[i]) == 0 )
	  cnipadr[i] = INADDR_NONE ;
       else
          cnipadr[i] = inet_addr( pm->PmChostNAME[i] );
   }
   for ( j = -1, i = 0; i < MaxHSM; i++ ) {
       if ( strlen(pm->PmMhostNAME[i]) == 0 ) continue ;
       j++ ;
       strcpy( (MyTAB+j)->HSMhostNAME, pm->PmMhostNAME[i] ) ;
       (MyTAB+j)->secchl = 0 ;
       (MyTAB+j)->sock = CMNInitSOCKET( pm->PmMhostNAME[i], pm->PmMhostPORT ) ;
       if ( (MyTAB+j)->sock == INVALID_SOCKET ) {
	    HSMLogMSG("HSM ip ",  (MyTAB+j)->HSMhostNAME, " connect error", NULL);
	   (MyTAB+j)->status = 0 ;  /* set HSM status not alive */
           continue ;
       }
       else{
           HSMLogMSG("HSM ip ",  (MyTAB+j)->HSMhostNAME, " connected", NULL);
	   (MyTAB+j)->status = 1 ;
	   continue ;
      }
   }
   if ( (MyTabIX = (j+1)) <= 0 ) {
      MyTAB = NULL ;
      HSMLogMSG("backend_available E_USV_INVALID_PRM", NULL, NULL);
      return( E_USV_INVALID_PRM ) ;
   }
   return( 0 ) ;
}

/* every 30sec check connect status and send NC command to HSM */
static int CheckConnSTA( int ix, int alive )
{
   int   rc, iLen ;
   char  buf[1024] ;
   SOCKET soc = (MyTAB+ix)->sock ;
   fd_set rmask ;
   struct timeval tv ;

   alive = 1 ;
   if ( alive ) {
      memset( buf, ' ', HsmHeaderSZ+2 ) ;
      memcpy( buf+HsmHeaderSZ+2, "NC", 2 ) ;
      stint( 6, buf ) ;

      if ( CMNsocketSNDsw(soc, buf, HsmHeaderSZ+4, 0) ) {
         (MyTAB+ix)->sock = INVALID_SOCKET ;
         (MyTAB+ix)->status = 0 ;   
         return( -1 ) ;
      }
      if ( (rc = CMNsocketRCV(soc, buf, 256, ctmva)) <= 0 ) {
         (MyTAB+ix)->sock = INVALID_SOCKET ;
         (MyTAB+ix)->status = 0 ;   
         return( -1 ) ;
      }
       (MyTAB+ix)->status = 1 ;
   }

   while ( 1 ) {
      FD_ZERO( &rmask ) ;
      FD_SET( soc, &rmask ) ;
      memset( (char*) &tv, 0, sizeof(tv) ) ;
      if ( alive )
         tv.tv_usec = 1000 ;
      else
         tv.tv_usec = 1 ;
      if ( (rc=select(soc+1, &rmask, NULL, NULL, &tv)) <= 0 ) {
         return( 0 ) ;
      }
      else
      {
         HSMLogMSG("HSM ip ",  (MyTAB+ix)->HSMhostNAME, " line down", NULL);
		 (MyTAB+ix)->status = 0 ;  /* set HSM status not alive */
         return( -1 ) ;
      }
   }

}

int backend_connect( int sechl )
{
   int  i, cnt = 0 ;

   for ( i = 0; i < MyTabIX; i++ ) {
       if ( (MyTAB+i)->sock != INVALID_SOCKET ) {
	  if ( CheckConnSTA(i, 0) == 0 ) {
	     cnt++ ;
	     continue ;
	  }
       }
       if ( (sechl >= 0) && (sechl != (MyTAB+i)->secchl) )
	   continue ;
       if ( (MyTAB+i)->sock != 0 ){  /* for re-connect HSM how to modify ? */
         pthread_create(&thrdID, NULL, (void *) reconnet, i );
/*
         (MyTAB+i)->sock = CMNInitSOCKET( (MyTAB+i)->HSMhostNAME, port ) ;
*/
       }
       if ( (MyTAB+i)->sock == INVALID_SOCKET ) {
	    (MyTAB+i)->status = 0 ;  /* set HSM status not alive */
             HSMLogMSG("HSM ip ", (MyTAB+i)->HSMhostNAME, " status not alive", NULL);
             return( -1 ) ;
/*
	     continue ;
*/
       }
       else{
            if((MyTAB+i)->status == 1 )
               HSMLogMSG("re-connect to HSM ip ",  (MyTAB+i)->HSMhostNAME, " successfully", NULL);
       }
      cnt++ ;
      if ( sechl == (MyTAB+i)->secchl ) return( i ) ;
   }
   return( sechl >= 0 ? -1 : cnt ) ;
}

void wakeup_handler( )
{
   int  rc, fd, i ;
   char prmbuf[sizeof(Parameter_t)] ;
   pParameter_t pm = (pParameter_t) prmbuf ;
   struct stat *st = (struct stat *) prmbuf ;
 
   if ( !_xSVapALIVE_ ) {       
      if ( backend_connect(-1) ) _xSVapALIVE_ = 1 ;
      return ;
   }
   if ( stat(NamePrm3K, st) ) return ;
   if ( st->st_mtime == stmtime ) {
      if ( MyTAB != NULL )
	 _xSVapALIVE_ = (backend_connect(-1) ? 1 : 0 ) ;
      return;
   }
   stmtime = st->st_mtime ;
   if ( (fd = open(NamePrm3K, O_RDONLY|O_BINARY)) < 0 )
       return ;
   read( fd, prmbuf, sizeof(*pm) ) ;
   close( fd ) ;
   if ( MyTimeSTAMP == pm->PmTimeSTAMP ) return ;
   if ( MyTAB != NULL ) {
      for ( i = 0; i < MyTabIX; i++ ) {
          closesocket( (MyTAB+i)->sock ) ;
      }
   }
   MyTAB = NULL ;
   MyCurrIX = 0 ;
   rc = backend_available( prmbuf ) ;
}

static int choose_backend( int sechl )
{
   int  i = MyCurrIX ;

   while( 1 ) { /* seach curr connection TAB */
      if ( (sechl == (MyTAB+i)->secchl) && (MyTAB+i)->sock != INVALID_SOCKET ) {
	 if ( CheckConnSTA(i, 0) == 0 ) {         /* check socket line status */
	    if ( (MyCurrIX = i + 1) >= MyTabIX ) MyCurrIX = 0 ;
	    return( i ) ;
	 }
      }
      if ( ++i >= MyTabIX ) i = 0 ;
      if ( i == MyCurrIX ) break ;
   }
   MyCurrIX = 0 ;
   _xSVapALIVE_ = 0 ;
   HSMLogMSG("choose_backend E_USV_HSM_DOWN", NULL, NULL);
   return( E_USV_HSM_DOWN ) ;
}

int hsmio( pCtxtKEY_t pCntx, int max )
{
   int  f_err_cnt = 0 ;
   char cmd[2048] ;
   int  rc, bsz = sizeof(cmd) ;
   int  ix, iLen ;

   if ( MyTAB == NULL ) {                                   /* first time start suip server */
      if ( rc = backend_available(NULL) ) return( rc ) ;    /* connect backend hsm */
   }
   if ( pCntx == NULL ) return( 0 ) ;                       /* no data receive */

   while( 1 ) {
      if ( (ix = choose_backend(0)) < 0 ) {                 /* chose backend hsm  */
	    return( ix ) ;
      }
      iLen = max ;
      if ( iLen <= 0 ) iLen = strlen( pCntx ) ;
      memset( cmd, 0, sizeof(cmd) ) ;
      memset( cmd, ' ', HsmHeaderSZ+2 ) ;
      memcpy( cmd+HsmHeaderSZ+2, pCntx, iLen ) ;
      iLen = iLen+HsmHeaderSZ ;
      stint( iLen, cmd ) ;

      HSMLogMSG("sendto   ",  (MyTAB+ix)->HSMhostNAME, NULL);
      if ( CMNsocketSNDsw((MyTAB+ix)->sock, cmd, iLen+2, 0) ) {
         closesocket( (MyTAB+ix)->sock ) ;
         (MyTAB+ix)->sock = INVALID_SOCKET ;
         (MyTAB+ix)->nokcnt++ ;
	 continue ;
      }
      if ( (rc = CMNsocketRCV((MyTAB+ix)->sock, cmd, bsz, ctmva)) <= 0 ) {
         closesocket( (MyTAB+ix)->sock ) ;
         (MyTAB+ix)->sock = INVALID_SOCKET ;
         (MyTAB+ix)->nokcnt++ ;
	 continue ;
      }
      (MyTAB+ix)->okcnt++ ;
      break ;
   }
   HSMLogMSG("recvfrom ",  (MyTAB+ix)->HSMhostNAME, NULL);
   memset( pCntx, 0, strlen(pCntx) ) ;
   memcpy( pCntx, cmd+HsmHeaderSZ+2, rc - (HsmHeaderSZ+2) ) ;
   /*
   return ( ldrint(cmd+HsmHeaderSZ+4, 2) ) ;
   */
   return ( hex2int(cmd+HsmHeaderSZ+4, 2) ) ;
}


int CmdXA( char *bufr )
{
   int  i, rc ;
   char xbuf[1024] ;

   memset( xbuf, 0, sizeof(xbuf) ) ;

   for ( i = 0; i < MyTabIX; i++ ) {
         rc = ((MyTAB+i)->sock == INVALID_SOCKET ? 0 : 1) ;
         sprintf(xbuf, "ID=%d,IpADDR=%s,port=%d,Status=%d;", (i+1), (MyTAB+i)->HSMhostNAME, port, rc );
         strcat(bufr, xbuf) ;
   }
   return( 0 ) ;
}


int GetHsmCount( char* bufr )
{
   int i ;
   char xbuf[2048], count[20], localtime[20] ;

   memset( count, 0, sizeof(count) ) ;
   memset( xbuf, 0, sizeof(xbuf) ) ;

   for ( i = 0; i < MyTabIX; i++ ) {
       LONG2cobPIC( count, (MyTAB+i)->okcnt, "9999999999" ) ;
       sprintf(xbuf, "%s,okcnt=%s",  (MyTAB+i)->HSMhostNAME, count );
       strcat(bufr, xbuf) ;
   }
   return( 0 ) ;
}

void ClearHsmCount( )
{
   int i ;

   for ( i = 0; i < MyTabIX; i++ ) 
        (MyTAB+i)->okcnt = 0 ;
}


