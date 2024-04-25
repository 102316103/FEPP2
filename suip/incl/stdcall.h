#ifndef	_STDCALL_H
#define	_STDCALL_H

#ifndef STDCALL
#ifdef	WIN32

#ifdef  _USRDLL
#define STDCALL	_stdcall
#else
#define STDCALL
#endif /* _USRDLL */

#else  /* NOT WIN32 */
#define STDCALL
#endif  /* WIN32 */

#endif  /* STDCALL */

#endif	/* _STDCALL_H */
