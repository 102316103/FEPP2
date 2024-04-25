#ifndef	_ISS96API_H
#define	_ISS96API_H
#ifdef	__cplusplus
extern "C" {
#endif
#define PVKPAIR		14
#define TPKPAIR		14
#define TAKPAIR		16
#define CVKPAIR		10
#define TMKPAIR		14
#define ZPKPAIR		6
#define DIV_KEY_PAIR	18 /* LMK PAIR 36 */

typedef	struct	{
	char	cmd[2] ;
	char	lmkpair[2] ;
	char    ksz ;
	char	zmk[24] ;
	char	data1[8] ;
	char	data2[8] ;
	char    delm ;
	char	kekschm ;
	char	lmkschm ;
	char	kcvmeth ;
}	CMDzSA_t, *pCMDzSA_t ;

typedef	struct	{
	char	cmd[2] ;
	char	lmkpair[2] ;
	char	zmk[16] ;
	char	data1[16] ;
	char	data2[16] ;
}	CMDSA ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	zmkey[16] ;
	char	lmkey[16] ;
	char	numn[16] ;
	char	numn1[16] ;
	char	numn2[16] ;
	char	mab1[16] ;
	char	mab2[16] ;
	char	delim[2] ;
}	CMDSB ;

typedef	struct	{
	char	cmd[2] ;
	char	lmkpair[2] ;
	char    zmksz ;
	char	zmk[24] ;
	char    wkkschm ;
	char    wkksz ;
	char	zmkey[24] ;
	char	numn[8] ;
	char	data1[8] ;
	char	data2[8] ;
	char    delm ;
	char	kekschm ;
	char	lmkschm ;
	char	kcvmeth ;
}	CMDzSC_t, *pCMDzSC_t ;

typedef	struct	{
	char	cmd[2] ;
	char	lmkpair[2] ;
	char	zmk[16] ;
	char	zmkey[16] ;
	char	numn[16] ;
	char	data1[16] ;
	char	data2[16] ;
}	CMDSC ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey[16] ;
	char	numn1[16] ;
	char	numn2[16] ;
	char	mab1[16] ;
	char	mab2[16] ;
	char	delim[2] ;
}	CMDSD ;

typedef	struct	{
	char	cmd[2] ;
	char	lmkpair[2] ;
	char	key[16] ;
	char	icv[16] ;
	char	data[16] ;
}	CMDSE ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	mab[16] ;
	char	delim[2] ;
}	CMDSF ;

/* parser string "2 ? H16 ;H" from MABLK to MABzLK_t */
typedef struct {
	char    lmkpair[2] ;
	char    key[16] ;
	char    icv[16] ;
	char    data[16] ;
}	MABLK, * pMABLK ;

typedef struct {
	char    lmkpair[2] ;
	char    ksz ;
	char    key[24] ;
	char    icv[8] ;
	char    data[1] ;
}	MABzLK_t, * pMABzLK_t ;

typedef	struct	{
	char	cmd[2] ;
	char	lmkpair[2] ;
	char	key[16] ;
	char	icv[16] ;
	char	data[16] ;
}	CMDSG ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	mab1[16] ;
	char	mab2[16] ;
	char	delim[2] ;
}	CMDSH ;

typedef	struct	{
	char	cmd[2] ;
	char	slmkpair[2] ;
	char	skey[16] ;
	char	dlmkpair[2] ;
	char	dkey[16] ;
	char	pinbk[16] ;
}	CMDSI ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	pinbk[16] ;
	char	mab1[16] ;
	char	mab2[16] ;
	char	delim[2] ;
}	CMDSJ ;

typedef	struct	{
	char	cmd[2] ;
	char	meth ;
	char	tpk[16] ;
	char	pvk[16] ;
	char	opinbk[16] ;
	char	pinbk[16] ;
	char	acctno[12] ;
	char	val[16] ;
	char	ofs[4] ;
	char	dec[16] ;
}	CMDSM ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	ofs[4] ;
	char	delim[2] ;
}	CMDSN ;

typedef	struct	{
	char	cmd[2] ;
	char	meth ;
	char	pvk[16] ;
	char	val[16] ;
	char	wei[4] ;
	char	dec[16] ;
}	CMDSO ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	pin[5] ;
	char	ofs[4] ;
	char	pchk[1] ;
	char	delim[2] ;
}	CMDSP ;

typedef	struct	{
	char	cmd[2] ;
	char	meth ;
	char	tpk[16] ;
	char	opvk[16] ;
	char	pvk[16] ;
	char	pinbk[16] ;
	char	acctno[12] ;
	char	val[16] ;
	char	ofs[4] ;
	char	dec[16] ;
}	CMDSQ ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	ofs[4] ;
	char	delim[2] ;
}	CMDSR ;

typedef	struct	{
	char	cmd[2] ;
	char	meth ;
	char	tpk[16] ;
	char	pvk[16] ;
	char	pinbk[16] ;
	char	acctno[12] ;
	char	val[16] ;
	char	ofs[4] ;
	char	dec[16] ;
}	CMDSS ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	mab1[16] ;
	char	mab2[16] ;
}	CMDST ;

typedef	struct	{
	char	cmd[2] ;
	char	meth ;
	char	zpk[16] ;
	char	pvk[16] ;
	char	pinbk[16] ;
	char	acctno[12] ;
	char	val[16] ;
	char	ofs[4] ;
	char	dec[16] ;
}	CMDSU ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	mab1[16] ;
	char	mab2[16] ;
}	CMDSV ;

typedef	struct	{
	char	cmd[2] ;
	char	key[16] ;
}	CMDSW ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMDSX ;

/* key length indicator = 'Z'- single , 'U' - double or 'T' - tripple */
typedef	struct	{
	char	cmd[2] ;
	char    ksz ;
	char	zpk[24] ;
	char	pinbk[8] ;
	char	pinfmt ;
	char	acctno[6] ;
}	CMDzJE_t, *pCMDzJE_t ;

typedef	struct	{
	char	cmd[2] ;
	char	zpk[16] ;
	char	pinbk[16] ;
	char	pinfmt[2] ;
	char	acctno[12] ;
}	CMDJE ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	epin[5] ;
	char	delim[2] ;
}	CMDJF ;

typedef	struct	{
	char	cmd[2] ;
	char	ksz ;
	char	zpk[24] ;
	char	pinfmt ;
	char	acctno[6] ;
	char	epin[4] ;
}	CMDzJG_t, *pCMDzJG_t ;

typedef	struct	{
	char	cmd[2] ;
	char	zpk[16] ;
	char	pinfmt[2] ;
	char	acctno[12] ;
	char	epin[4] ;
}	CMDJG ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	pinbk[16] ;
	char	delim[2] ;
}	CMDJH ;

typedef	struct	{
	char	cmd[2] ;
	char	acctno[12] ;
}	CMDJA ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	epin[14] ;
	char	delim[2] ;
}	CMDJB ;

typedef	struct	{ /* Command JS is to generate visa PIN & PVV */
	char	cmd[2] ;
	char	pvk[32] ;
        char	acctno[12] ;
        char	pvki ;
        char	filler ;
}	CMDJS ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
        char	pvv[4] ;
	char	epin[14] ;
}	CMDJT ;

typedef	struct	{
	char	cmd[2] ;
	char	cvk[32] ;
        char	acctno[40] ;
        char	filler ;
}	CMDCW ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	cvv[3] ;
	char	delim[2] ;
}	CMDCX ;

typedef	struct	{
	char	cmd[2] ;
	char	cvk[32] ;
	char	cvv[3] ;
        char	acctno[40] ;
        char	filler ;
}	CMDCY ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	delim[2] ;
}	CMDCZ ;

typedef	struct	{
	char	cmd[2] ;
	char	tpk[16] ;
	char	pvk[16] ;
	char	filler[2] ;
	char	pinbk[16] ;
	char	pinfmt[2] ;
	char	psz[2] ;
	char	acctno[12] ;
	char	dec[16] ;
	char	val[12] ;
	char	ofs[12] ;
}	CMDDA ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	delim[2] ;
}	CMDDB ;

typedef	struct	{
	char	cmd[2] ;
        char	tpksz ;
	char	tpk[24] ;
        char	pvksz ;
	char	pvk[24] ;
	char	pinbk[8] ;
	char	pinfmt ;
        char	acctno[6] ;
        char	pvki ;
        char	pvv[4] ;
        char	filler ;
}	CMDzDC_t, *pCMDzDC_t ;

typedef	struct	{
	char	cmd[2] ;
	char	tpk[16] ;
	char	pvk[32] ;
	char	pinbk[16] ;
	char	pinfmt[2] ;
        char	acctno[12] ;
        char	pvki ;
        char	pvv[4] ;
        char	filler ;
}	CMDDC ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	delim[2] ;
}	CMDDD ;

typedef	struct	{
	char	cmd[2] ;
	char	pvk[16] ;
	char	pin[5] ;
	char	psz[42] ;
/*
        char	acctno[12] ;
        char	dec[16] ;
        char	val[12] ;
*/
}	CMDDE ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	ofs[12] ;
	char	delim[2] ;
}	CMDDF ;

typedef	struct	{
	char	cmd[2] ;
	char	ksz ;
	char	pvk[24] ;
	char	epin[28] ;
        char	acctno[6] ;
        char	pvki ;
}	CMDzDG_t, *pCMDzDG_t ;

typedef	struct	{
	char	cmd[2] ;
	char	pvk[32] ;
	char	epin[26] ;
}	CMDDG ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	pvv[4] ;
	char	delim[2] ;
}	CMDDH ;

typedef	struct	{
	char	cmd[2] ;
	char	pvk[16] ;
	char	ofs[12] ;
	char	psz[2] ;
        char	acctno[12] ;
        char	dec[16] ;
        char	val[12] ;
}	CMDEE ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	epin[16] ;
}	CMDEF ;

typedef	struct	{
	char	cmd[2] ;
}	CMDAS ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey1[16] ;
	char	lmkey2[16] ;
	char	kcv1[6] ;
	char	kcv2[6] ;
	char	delim[2] ;
}	CMDAT ;

typedef	struct	{
	char	cmd[2] ;
	char	zmk[16] ;
	char	cvk1[16] ;
	char	cvk2[16] ;
}	CMDAU ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	cvk1[16] ;
	char	cvk2[16] ;
	char	kcv1[6] ;
	char	kcv2[6] ;
	char	delim[2] ;
}	CMDAV ;

typedef	struct	{
	char	cmd[2] ;
	char	zmk[16] ;
}	CMDFG ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey1[16] ;
	char	lmkey2[16] ;
	char	zmkey1[16] ;
	char	zmkey2[16] ;
	char	kcv1[16] ;
	char	kcv2[16] ;
	char	delim[2] ;
}	CMDFH ;

typedef	struct	{
	char	cmd[2] ;
	char	wkktype ; /* 0 - ZEK(30), 1 - ZAK(26) */
	char	zmk[16] ;
}	CMDFI ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	zmkey[16] ;
	char	lmkey[16] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMDFJ ;

typedef	struct	{
	char	cmd[2] ;
	char	wkktype ; /* 0 - ZEK(30), 1 - ZAK(26) */
	char	zmk[16] ;
	char	zmkey[16] ; /* key encrypted under zmk */
}	CMDFK ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey[16] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMDFL ;

typedef	struct	{
	char	cmd[2] ;
	char	wkktype ; /* 0 - ZEK(30), 1 - ZAK(26) */
	char	zmk[16] ;
	char	lmkey[16] ; /* key encrypted under lmk */
}	CMDFM ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	zmkey[16] ;
	char	delim[2] ;
}	CMDFN ;

typedef	struct	{
	char	cmd[2] ;
	char	ksz ;
	char	kek[24] ;
	char    delm ;
	char	kekschm ;
	char	lmkschm ;
	char	kcvmeth ;
}	CMDzIA_t, *pCMDzIA_t ;

typedef	struct	{
	char	cmd[2] ;
	char	kek[16] ;
}	CMDIA ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	kekey[16] ;
	char	lmkey[16] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMDIB ;

typedef	struct	{
	char	cmd[2] ;
	char	kcomp1[16] ;
	char	kcomp2[16] ;
}	CMDIG ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey[16] ;
	char	delim[2] ;
}	CMDIH ;

typedef	struct	{
	char	cmd[2] ;
	char	zmk1[16] ;
	char	zmk2[16] ;
	char	zmk3[16] ;
}	CMDGG ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	zmk[16] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMDGH ;

typedef	struct	{
	char	cmd[2] ;
	char	fields[8] ;
}	CMDOE ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	key[36] ;
}	CMDOF ;

typedef	struct	{
	char	cmd[2] ;
	char	filler ;
	char	acctno[12] ;
	char	pin[16] ;
}	CMDPE ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	delim[2] ;
}	CMDPF ;

typedef	struct	{
	char	cmd[2] ;
	char	zmksz ;
	char	zmk[24] ;
	char	zpkschm ;
	char	zpksz ;
	char	zpk[24] ;
	char    delm ;
	char	kekschm ;
	char	lmkschm ;
	char	kcvmeth ;
}	CMDzFA_t, *pCMDzFA_t ;

typedef	struct	{
	char	cmd[2] ;
	char	zmk[16] ;
	char	zpk[16] ;
}	CMDFA ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	zpk[16] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMDFB ;

typedef	struct	{
	char	cmd[2] ;
	char	skey[16] ;
	char	dkey[16] ;
	char	pinsz[2] ;
	char	pinbk[16] ;
	char	spinfmt[2] ;
	char	dpinfmt[2] ;
	char	acctno[12] ;
}	CMDCA ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	psz[2] ;
	char	pinbk[16] ;
	char	dpinfmt[2] ;
	char	delim[2] ;
}	CMDCB ;

typedef	struct	{ /* generate a kcv for any key with different method */
	char	cmd[2] ;
	char	key[16] ;
	char	pair[2] ;
	char	meth[2] ;
}	CMDK0 ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMDK1 ;

typedef	struct	{
	char	cmd[2] ;
	char	key[16] ;
	char	type[2] ;
}	CMDKA ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMDKB ;

typedef	struct	{	  /* To translate a key encrypted under kek to */
	char	cmd[2] ;  /* encrypted under a specified lmk pair */
	char	kektype[2] ;
	char	kek[16] ; /* Command XM works the other way */
	char	wkktype[2] ; /* BUGS : wkktype & kektype must be pair / 2 */
	char	wkk[16] ;
}	CMDXK ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	wkk[16] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMDXL;

typedef	struct	{	  /* upgrade a key from old LMK to new LMK */
	char	cmd[2] ;  
	char	pair[2] ;
	char	ksz[2] ; /* 16, 32, 48 */
	char	key[16] ; /* 16, 32, 48 HEX digits */
}	CMDXS ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	key[16] ;
}	CMDXT ;

typedef	struct	{
	char	cmd[2] ;
	char	tak[16] ;
	char	mab[16] ;
}	CMDHE;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	mab[16] ;
	char	delim[2] ;
}	CMDHF;

typedef	struct	{
	char	cmd[2] ;
	char	pin[16] ;
}	CMDBA;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	epin[14] ;
	char	delim[2] ;
}	CMDBB;

typedef	struct	{
	char	cmd[2] ;
	char	acctno[12] ;
	char	epin[4] ;
}	CMDNG;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	pin[14] ;
}	CMDNH;

typedef	struct	{
	char	cmd[2] ;
	char	tpk[16] ;
	char	pinbk[16] ;
	char	pinfmt[2] ;
	char	acctno[12] ;
	char	epin[2] ;
}	CMDBC ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	delim[2] ;
}	CMDBD ;

typedef	struct	{
	char	cmd[2] ;
}	CMDNC ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkcv[16] ;
	char	delim[2] ;
}	CMDND ;

typedef	struct	{	  /* upgrade a key from old LMK to new LMK */
	char	cmd[2] ;  
	char	key[16] ; /* 16, 32, 48 HEX digits */
}	CMDGE ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	key[16] ;
}	CMDGF ;

/*   The following Y'series commands are for KMS */
/*
     for YA, YC, YE commands only :
     for compatibility, kcvtype redefined as following
	 1st digit : 0 or 1 for single length key, 2 - double, 3 - tripple
	 2nd digit : kcv type
*/
typedef	struct	{	  /* To encrypt a clear key comp under lmk pair */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	kcvtype[2] ;
	char	key[16] ;
}	CMDYA ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey[16] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMDYB;

typedef	struct	{	  /* To form encrypted key comps under lmk pair */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	kcvtype[2] ;
	char	nocomps[2] ;
	char	key[16] ;
}	CMDYC ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey[16] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMDYD;

typedef	struct	{	  /* To generate key comps encrypted under lmk pair */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	kcvtype[2] ;
	char	nocomps[2] ;
}	CMDYE ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	keykcv[16] ;
	char	cmpkey[16] ;
	char	cmpkcv[16] ;
}	CMDYF;

/*
typedef	struct	{ // YE command response format for tripple DES 
	char	cmd[2] ;
	char	error[2] ;
	char	keykcv[16] ;
	char	cmpkey[ksz*2*nocomps] ; // ksz = 16 or 24
	char	cmpkcv[16*nocomps] ;
}	CMDYF ;
*/

/*   The following commands are for EMV functionality */
typedef	struct	{
	char	cmd[2] ;  
	char	mode ;
	char	scheme ;
	char	DMKac[32] ;
	char	PAN[8] ;
	char	ATC[2] ;
	char	UN[4] ;
	char	dsz[2] ; /* HEX char 00 - FF */
	char	data[0] ;
}	CMDKQ ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	ARPC[8] ;
/*	char	DiaDATA[8] ; */
}	CMDKR;

typedef	struct	{
	char	cmd[2] ;  
	char	mode ; /* == '0' */
	char	scheme ;
	char	MKdac[32] ;
	char	PAN[8] ;
	char	DAC[2] ;
}	CMDKS0 ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	DiaDATA[2] ;
}	CMDKT0;

typedef	struct	{
	char	cmd[2] ;  
	char	mode ; /* == '1' */
	char	scheme ;
	char	MKdn[32] ;
	char	PAN[8] ;
	char	DN[2] ;
	char	ATC[2] ;
	char	UN[4] ;
}	CMDKS1 ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	DiaDATA[2] ;
}	CMDKT1;


typedef	struct	{
	char	cmd[2] ;  
	char	mode ; /* == '0' integrity only */
	char	scheme ;
	char	MKsmi[32] ;
	char	PAN[8] ;
	char	ISdata[8] ;
	char	pdlen[4] ;
	char	pdata[0] ;
}	CMDKU0 ;

typedef	struct	{
	char	TK[32] ;
	char	CSdata[8] ;
	char	offset[4] ;
	char	cdlen[4] ;
	char	cdata[0] ;
}	CMDKU1 ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	MAC[8] ;
	char	redlen[4] ;
	char	reddata[0] ;
}	CMDKV;

/*   The following '2' series commands are for double length 3DES */

typedef	struct {
	char    filler ; /* ';' */
	char	mode ;   /* '0' - ECB, '1' - CBC, '2' - MAC */
	char	icv[16] ;
	char	data[0] ;
} XAzKCV_t, *pXAzKCV_t ;

typedef	struct	{	  /* To encrypt a clear key comp under lmk pair */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	key[32] ;
/*
	char    filler ; // ';'
	char	mode ;   // '0' - ECB, '1' - CBC, '2' - MAC
	char	icv[16] ;
	char	data[0] ;
*/
}	CMD2A ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey[32] ;
/*
	char	data[0] ;
*/
	char	delim[2] ;
}	CMD2B;

typedef	struct	{	  /* To form encrypted key comps under lmk pair */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	nocomps[2] ; /* >= 2 && <= 9 */
	char	key[32] ;
/*
	char    filler ; // ';'
	char	mode ;   // '0' - ECB, '1' - CBC, '2' - MAC
	char	icv[16] ;
	char	data[0] ;
*/
}	CMD2C ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey[32] ;
/*
	char	data[0] ;
*/
	char	delim[2] ;
}	CMD2D;

typedef	struct	{	  /* 3Des Encryption(ECB) */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	key[32] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD2E ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD2F;

typedef	struct	{	  /* 3Des Decryption(ECB) */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	key[32] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD2G ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD2H;

typedef	struct	{	  /* 3Des Encryption(CBC) */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	key[32] ;
	char	icv[16] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD2I ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD2J;

typedef	struct	{	  /* 3Des Decryption(CBC) */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	key[32] ;
	char	icv[16] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD2K ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD2L;

typedef	struct	{	  /* 3Des MAC */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	key[32] ;
	char	icv[16] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD2M ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	MAC[16] ;
	char	delim[2] ;
}	CMD2N;

typedef	struct	{	  /* generate a 3DES key */
	char	cmd[2] ;  
	char	kekpair[2] ;
	char	kek[32] ;
	char	lmkpair[2] ;
}	CMD2O ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	kekey[32] ;
	char	lmkey[32] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMD2P;

typedef	struct	{	  /* translate a 3DES KEY from KEK to LMK */
	char	cmd[2] ;  
	char	kekpair[2] ;
	char	kek[32] ;
	char	kekey[32] ;
	char	lmkpair[2] ;
}	CMD2Q ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey[32] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMD2R;

typedef	struct	{	  /* translate a 3DES KEY from LMK to KEK */
	char	cmd[2] ;  
	char	kekpair[2] ;
	char	kek[32] ;
	char	lmkpair[2] ;
	char	lmkey[32] ;
}	CMD2S ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	kekey[32] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMD2T;

typedef	struct	{	  /* 3Des SMAC(EMV) */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	key[32] ;
	char	icv[16] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD2U ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	MAC[16] ;
	char	delim[2] ;
}	CMD2V;

typedef	struct	{	  /* visa EMV key diversified function */
	char	cmd[2] ;  
	char	mdkpair[2] ;
	char	mdk[32] ;
	char	kekpair[2] ;
	char	kek[32] ;
	char	PAN[16] ;
}	CMD2W ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	udk[32] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMD2X;

typedef	struct	{	  /* pin block translate, this is the internal format */
	char	cmd[2] ;  
	char	spair[2] ;
	char	sksz ;
	char	skey[24] ;
	char	tpair[2] ;
	char	tksz ;
	char	tkey[24] ;
	char	pinbk[8] ;
}	CMDx3W ;

typedef	struct	{	  /* pin block translate, this is the external format */
	char	cmd[2] ;  
	char	spair[2] ;
	char	skey[16] ;
	char	tpair[2] ;
	char	tkey[16] ;
	char	pinbk[16] ;
}	CMD3W ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	pinbk[16] ;
	char	delim[2] ;
}	CMD3X;

/* the following commands are general purpose rsa operation */

typedef	struct	{/* rsa key gen, exp operation */
	char	cmd[2] ;  
	char	f[2] ; /* 00 - key gen, 01 - pri exp, 02 - pub exp */
	char	modbytes[4] ; /* modulus bytes */
	char	expbytes[4] ; /* pub or priv key exponent */
	char	modexp[16] ; /* mod(modbytes) + exp(expbytes)+data(modbytes) */
}	CMD9W ;

typedef	struct	{
	char	cmd[2] ;  
	char	error[2] ;
	char	keyordata[16] ;
}	CMD9X ;


/*   The following '3' series commands are for tripple length 3DES */
/*   it can be used for double length 3DES */
typedef	struct	{	  /* To encrypt a clear key comp under lmk pair */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	key[48] ;
/*
	char    filler ; // ';'
	char	mode ;   // '0' - ECB, '1' - CBC, '2' - MAC
	char	icv[16] ;
	char	data[0] ;
*/
}	CMD3A ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey[48] ;
/*
	char	data[0] ;
*/
	char	delim[2] ;
}	CMD3B;

typedef	struct	{	  /* To form encrypted key comps under lmk pair */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	nocomps[2] ; /* >= 2 && <= 9 */
	char	key[48] ;
/*
	char    filler ; // ';'
	char	mode ;   // '0' - ECB, '1' - CBC, '2' - MAC
	char	icv[16] ;
	char	data[0] ;
*/
}	CMD3C ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey[48] ;
/*
	char	data[0] ;
*/
	char	delim[2] ;
}	CMD3D;

typedef	struct	{	  /* 3Des Encryption(ECB) */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	key[48] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD3E ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD3F;

typedef	struct	{	  /* 3Des Decryption(ECB) */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	key[48] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD3G ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD3H;

typedef	struct	{	  /* 3Des Encryption(CBC) */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	key[48] ;
	char	icv[16] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD3I ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD3J;

typedef	struct	{	  /* 3Des Decryption(CBC) */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	key[48] ;
	char	icv[16] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD3K ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD3L;

typedef	struct	{	  /* 3Des MAC */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	key[48] ;
	char	icv[16] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD3M ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	MAC[16] ;
	char	delim[2] ;
}	CMD3N;

typedef	struct	{	  /* generate a 3DES key */
	char	cmd[2] ;  
	char	kekpair[2] ;
	char	kek[48] ;
	char	lmkpair[2] ;
}	CMD3O ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	kekey[48] ;
	char	lmkey[48] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMD3P;

typedef	struct	{	  /* translate a 3DES KEY from KEK to LMK */
	char	cmd[2] ;  
	char	kekpair[2] ;
	char	kek[48] ;
	char	kekey[48] ;
	char	lmkpair[2] ;
}	CMD3Q ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey[48] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMD3R;

typedef	struct	{	  /* translate a 3DES KEY from LMK to KEK */
	char	cmd[2] ;  
	char	kekpair[2] ;
	char	kek[48] ;
	char	lmkpair[2] ;
	char	lmkey[48] ;
}	CMD3S ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	kekey[48] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMD3T;

typedef	struct	{	  /* 3Des SMAC(EMV) */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	key[48] ;
	char	icv[16] ;
	char	dsz[4] ;
	char	data[0] ;
}	CMD3U ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	MAC[16] ;
	char	delim[2] ;
}	CMD3V;

typedef	struct	{	  /* decrypt msg block or generate MAC with div key */
	char	cmd[2] ;  
	char	key[48] ;
	char	div[16] ;
	char	mode[2] ; /* "00" - ECB, "01" - CBC, "02" - MAC */
	char	icv[16] ;
	char	dsz[4] ;
	char	data[16] ;
}	CMD3Y ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	dsz[4] ;
	char	data[16] ;
}	CMD3Z ;

typedef	struct	{	  /* generate a FISC DIV KEY */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	ksz[2] ;
	char	key[48] ;
	char	div[16] ;
}	CMD5A ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	divkey[48] ;
}	CMD5B ;

typedef	struct	{	  /* translate a key from one lmk to another lmk */
	char	cmd[2] ;  
	char	slmkpair[2] ;
	char	dlmkpair[2] ;
	char	key[16] ;
}	CMD5C ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	key[16] ;
	char	kcv[16] ;
}	CMD5D ;

typedef	struct	{	  /* generate a FISC DIV KEY */
	char	cmd[2] ;  
	char	lmkpair[2] ;
	char	divf ;
	char	filler ;
	char	ksz[2] ;
	char	key[48] ;
	char	div[48] ;
}	CMD5E ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	divkey[48] ;
}	CMD5F ;

typedef	struct	{	  /* generate a DIV KEY decrypted by session key */
	char	cmd[2] ;  
	char	sesdivf ;
	char	seskey[48] ;
	char	sesdiv[48] ;
	char	R1[16] ;
	char	R2[16] ;
	char	divf ;
	char	key[48] ;
	char	div[48] ;
}	CMD5G ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	divkey[48] ;
}	CMD5H ;

typedef	struct	{	  /* fisc OTP function */
	char	cmd[2] ;  
	char	divf ;
	char	filler ;
	char	key[48] ;
	char	div[48] ;
	char	inotp[8] ;
	char	range[4] ;  
	char	seqno[8] ;
	char	icv[16] ;  /* "FISCCARD" */
	char	data[16] ; /* "BAROCARD" */
}	CMD5O ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	seqno[8] ;
}	CMD5P ;

/*  all lmk pair must be greater or equal to 20 for 4series command */
typedef	struct	{	  /* generate a DES key */
	char	cmd[2] ;  
	char	keksz ; /* '1' - single des,  '2' or '3' - tripple des */
	char	wkksz ; /* '1' - single des,  '2' or '3' - tripple des */
	char	kekpair[2] ;
	char	kek[48] ;
	char	lmkpair[2] ;
}	CMD4O ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	kekey[48] ;
	char	lmkey[48] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMD4P;

typedef	struct	{ /* translate des key from kek to lmk */
	char	cmd[2] ;
	char	keksz ; /* '1' - single des,  '2' or '3' - tripple des */
	char	wkksz ; /* '1' - single des,  '2' or '3' - tripple des */
	char	kekpair[2] ;
	char	kek[48] ;
	char	lmkpair[2] ;
	char	kekey[48] ;
}	CMD4Q ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey[48] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMD4R ;

typedef	struct	{ /* translate des key from lmk to kek */
	char	cmd[2] ;
	char	keksz ; /* '1' - single des,  '2' or '3' - tripple des */
	char	wkksz ; /* '1' - single des,  '2' or '3' - tripple des */
	char	kekpair[2] ;
	char	kek[48] ;
	char	lmkpair[2] ;
	char	lmkey[48] ;
}	CMD4S ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	kekey[48] ;
	char	kcv[16] ;
	char	delim[2] ;
}	CMD4T ;

typedef	struct	{	  /* decrypt, encrypt or MAC msg block by div key */
	char	cmd[2] ;  
	char	ksz ; /* '1' - single des,  '2' or '3' - tripple des */
	char	enc ; /* 'D' - decrypt, 'E' - encrypt, 'M' - MAC */
	char	mode ; /* '0' - ECB, '1' - CBC */
	char	divf ; /* '1' - '5' */
	char	lmkpair[2] ; /* lmk pair which key is encrypted under */
	char	key[48] ;
	char	div[48] ;
	char	xord[48] ;
	char	icv[16] ; /* MAC generation only : field separator ';' */
	char	dsz[4] ;  /* be used to input multiple msg block */
	char	data[16] ; /* starting from icv thru data */
}	CMD4Y ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	dsz[4] ;
	char	data[16] ;
}	CMD4Z ;

typedef	struct	{ /* to generate SEC_CODE & PIN BLOCK for HLSB */
	char	cmd[2] ;
	char	pvk[16] ;
	char	ppk[16] ;
	char	pvkpair[2] ; /* LMK pair for pvk */
	char	ppkpair[2] ; /* TPK or ZPK */
        char	acctno[16] ;
        char	offset[4] ;
        char	bnkid[3] ;
        char	filler ;
}	CMD8A ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
        char	epinblk[16] ;
	char	sec_code[3] ;
	char	pinchk ;
	char	delim[2] ;
}	CMD8B ;

typedef	struct	{ /* to generate SEC_CODE & OFFSET for HLSB */
	char	cmd[2] ;
	char	pvk[16] ;
	char	ppk[16] ;
        char	epinblk[16] ;
	char	pvkpair[2] ; /* LMK pair for pvk */
	char	ppkpair[2] ; /* TPK or ZPK */
        char	acctno[16] ;
        char	bnkid[3] ;
        char	filler ;
}	CMD8C ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
        char	offset[4] ;
	char	sec_code[3] ;
	char	delim[1] ;
}	CMD8D ;

typedef	struct	{ /* to generate SEC_CODE & EPIN & offset for HLSB */
	char	cmd[2] ;
	char	pvk[16] ;
	char	pvkpair[2] ; /* LMK pair for pvk */
        char	acctno[16] ;
        char	bnkid[3] ;
        char	filler ;
}	CMD8E ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	offset[4] ;
        char	epin[5] ;
	char	sec_code[3] ;
	char	pinchk ;
	char	delim[1] ;
}	CMD8F ;

/* EDS PIN Block Translation */
typedef	struct	{
	char	cmd[2] ;
	char	skey[16] ;
	char	dkey[16] ;
	char	pinsz[2] ;
	char	pinbk[16] ;
	char	spinfmt[2] ;
	char	dpinfmt[2] ;
	char	s_acctno[12] ;
	char	d_acctno[12] ;
}	CMD8G ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	psz[2] ;
	char	pinbk[16] ;
	char	dpinfmt[2] ;
	char	delim[2] ;
}	CMD8H ;

/* the following commands are for setting up a secret channel */

typedef	struct	{/* packet format flow thru secured channel */
	char	datLen[4] ; /* actual length of packet data before padding */
	char	pktSeq[4] ;
	char	pktData[0] ;
/*	char	MAC[4] ; */
}	SecPKT_t, *pSecPKT_t ;

typedef	struct	{/* set up session key */
	char	cmd[2] ;  
	char	randata[16] ; /* random number generated by client */
	char	modbytes[4] ; /* client public key info */
	char	pubexpsz[4] ; /* in bytes */
	char	modexp[16] ; /* mod(modbytes) + pubexp */
}	CMD9Y ;

typedef	struct	{
	char	cmd[2] ;  
	char	error[2] ;
	char	keydata[16] ; /* random data generated by m2000, ENC by pub key */
}	CMD9Z ;

#ifdef	__cplusplus
}
#endif
#endif	/* _ISS96API_H */
