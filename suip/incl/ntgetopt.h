#ifndef	_NTGETOPT_H
#define	_NTGETOPT_H
#ifdef	__cplusplus
extern "C" {
#endif

/*
Utility function to extract option flags from the command line. 
argv is the command line.
   The options, if any, start with a '-' in argv[1], argv[2], ...
   OptStr is a text string containing all possible options("ab" means -a -b),
   These bol flags are set if and only if the corresponding option
   character occurs in argv [1], argv [2], ...
   The return value is the argv index of the first argument beyond the options.
*/

int nt_getopt (int argc, char* argv[], char* OptStr, char* bol ) ;

#ifdef	__cplusplus
}
#endif

#endif /* _NTGETOPT_H */
