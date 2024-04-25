#ifndef	_HSMAPI_H
#define	_HSMAPI_H
#ifdef	__cplusplus
extern "C" {
#endif
/* RG8000 HSM LMK PAIR */
#define ZMKPAIR		4
#define ZPKPAIR		6
#define PVKPAIR		14
#define TPKPAIR		14
#define TAKPAIR		16
#define CVKPAIR		10
#define TMKPAIR		14
#define DIV_KEY_PAIR	18 

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
	char	zmksz ;
	char	zmk[32] ;
	char	data1[16] ;
	char	data2[16] ;
	char    delm ;
	char	kekschm ;
	char	lmkschm ;
	char	kcvmeth ;
}	CMDSA_t ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	zmkeysz ;
	char	zmkey[32] ;
	char	lmkeysz ;
	char	lmkey[32] ;
	char	numn[16] ;
	char	numn1[16] ;
	char	numn2[16] ;
	char	mab1[16] ;
	char	mab2[16] ;
	char	delim[2] ;
}	CMDSB_t ;

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
	char	lmkpair[2] ;
	char    zmksz ;
	char	zmk[32] ;
	char    zmkeysz ;
	char	zmkey[32] ;
	char	numn[16] ;
	char	data1[16] ;
	char	data2[16] ;
}	CMDSC_t ;

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
	char	lmkpair[2] ;
	char	keysz ;
	char	key[32] ;
	char	icv[16] ;
	char	data[16] ;
}	CMDSE_t ;

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
	char	lmkpair1[2] ;
	char    key1sz ;
	char	key1[32] ;
	char	icv1[16] ;
	char	data1[16] ;
	char    delm ;
	char	lmkpair2[2] ;
	char    key2sz ;
	char	key2[32] ;
	char	icv2[16] ;
	char	data2[16] ;
}	CMDSG_t ;

typedef	struct	{
	char	cmd[2] ;
	char	lmkpair1[2] ;
	char	key1[16] ;
	char	icv1[16] ;
	char	data1[16] ;
	char    delm ;
	char	lmkpair2[2] ;
	char	key2[16] ;
	char	icv2[16] ;
	char	data2[16] ;
}   CMDSG ;

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


/*   The following commands are for EMV functionality */


/* for HSM8000 function */

typedef	struct	{  
	char	cmd[2] ;
	char	keytype[3] ;
	char	zmksz ;
	char	zmk[32] ;
	char	zmkeysz ;
	char	zmkey[32] ;
	char    keyschm ;
}	CMDA6 ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	lmkey[16] ;
	char    kcv[6] ;
}	CMDA7 ;

/* Translate TPK from encryption under a specified key to encryption under LMK Pair 14-15 */
typedef	struct	{    
	char	cmd[2] ;
	char	lmkpair[2] ;
	char    ksz ;
	char	key[24] ;
	char    wkkschm ;
	char    tpksz ;
	char	tpk[24] ;
	char	dat[8] ;
}	CMDTA ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	tpk[48] ;
	char	kcv[16] ;
	char	dat[16] ;
}	CMDTB ;

typedef	struct	{    
	char	cmd[2] ;
	char	meth ;
	char	pvvcvvid ;
	char    tpksz ;
	char	tpk[24] ;
	char    pvk[16] ;
	char	pinbk[16] ;
	char	acctno[12] ;
	char	val[16] ;
	char	dec[16] ;
	char	cvk1[16] ;
	char	cvk2[16] ;
	char	pan[40] ;
}	CMDTI ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	pinofs[16] ;
	char	pvvofs[16] ;
	char	cvv[3] ;
	char	mab1[16] ;
	char	mab2[16] ;
}	CMDTJ ;


/* Derive a key from TMK and generate MAB */
typedef	struct	{  
	char	cmd[2] ;
	char	tmkksz ;
	char	tmk[24] ;
	char	divlen ;
	char	divdata[24] ;
	char	ttmksz ;
	char	ttmk[24] ;
	char	kschm ;
	char	icv[16] ;
	char	data[2048] ;
}	CMDTO ;


typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	divktmk[48] ;
	char	divklmk[48] ;
	char	kcv[16] ;
	char	mab[16] ;
}	CMDTP ;

/* Generate Terminal Authentication Data for IC card */

typedef	struct	{  
	char	cmd[2] ;
	char	ksz ;
	char	tmk[48] ;
	char	iccid[16] ;
	char	authdata[16] ;
}	CMDTQ ;


typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	authcode[16] ;
}	CMDTR ;

/* Generate session key and update TMK for IC card */
typedef	struct	{  
	char	cmd[2] ;
	char	zmkksz ;
	char	zmk[24] ;
	char	tmkksz ;
	char	tmk[24] ;
	char	iccid[8] ;
	char	hostdata[8] ;
	char	ifddata[8] ;
}	CMDTS ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	seskey[16] ;
	char	divkey[48] ;    /* tmk decrypted iccid under session key */
}	CMDTT ;

/* Generate session key and update Derived Key for IC card */
typedef	struct	{  
	char	cmd[2] ;
	char	zmkksz ;
	char	zmk[24] ;
	char	tmkksz ;
	char	tmk[24] ;
	char	iccid[8] ;
	char	hostdata[8] ;
	char	ifddata[8] ;
	char	divlen ;
	char	divdata[24] ;
	char	ttmksz ;
	char	ttmk[24] ;
	char	kschm ;
	char	icv[16] ;
	char	data[16] ;
}	CMDTU ;


typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	seskey[16] ;
	char	seskcv[16] ;
	char	divktmk[48] ;
	char	divklmk[48] ;
	char	divkses[48] ;
	char	kcv[16] ;
	char	mab[16] ;
}	CMDTV ;


/* Generate a TAB using a Key diversified from the Seed Key, and optionally, generate two MAB using two different keys */
typedef	struct	{  
	char	cmd[2] ;
	char	sedkid ;
	char    sedskcm ;			/* U, T for Thales, X, Y for ANSI X9.17 */
	char	sedkey[48] ;		/*'U'+32 bytes key */  
	char	divflag ;
	char	divmethod ; 
	char	divdlen[2] ;
	char	divdata[48] ;
	char	delim1 ;
	char	icv[16] ;
	char	tabdat[16] ;
/*	char	delim2 ;	*/
}	CMDUA ;


typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	tab[16] ;
	char	mab1[16] ;    
	char	mab2[16] ;    
}	CMDUB ;

/* Export Seed Key or Diversified Key */
typedef	struct	{  
	char	cmd[2] ;
	char    zmksz ;
	char	zmk[32] ;
	char	kcvtype ;
	char	sedkid ;
	char	sedskcm ;
	char	sedksz ;
	char	sedkey[32] ;
	char	divflag ;
	char	divmethod ; 
	char	divdlen[2] ;
	char	divdata[32] ;
	char	divdelim ;
	char	icv[16] ;
	char	mabdat[16] ;
}	CMDUC ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char	key[32] ;
	char	kcv[16] ;
	char	mab[16] ;    
	char	mab1[16] ;    
}	CMDUD ;

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

typedef	struct	{  
	char	cmd[2] ;
	char	zmkksz ;
	char	zmk[48] ;
	char	tmkksz ;
	char	tmk[48] ;
	char	iccid[16] ;
	char	hostdata[16] ;
	char	ifddata[16] ;
}	CMDxTS ;

typedef	struct	{  
	char	cmd[2] ;
	char	zmkksz ;
	char	zmk[48] ;
	char	tmkksz ;
	char	tmk[48] ;
	char	iccid[16] ;
	char	hostdata[16] ;
	char	ifddata[16] ;
	char	divlen ;
	char	divdata[48] ;
	char	ttmksz ;
	char	ttmk[48] ;
	char	kschm ;
	char	icv[16] ;
	char	data[16] ;
}	CMDxTU ;

typedef	struct	{  
	char	cmd[2] ;
	char	tmkksz ;
	char	tmk[48] ;
	char	divlen ;
	char	divdata[48] ;
	char	ttmksz ;
	char	ttmk[48] ;
	char	kschm ;
	char	icv[16] ;
	char	data[16] ;
}	CMDxTO ;

typedef	struct	{
	char	cmd[2] ;
	char	error[2] ;
	char    tmksz ;
	char	divktmk[48] ;
	char    lmksz ;
	char	divklmk[48] ;
	char	kcv[16] ;
	char	mab[16] ;
}	CMDxTP ;


typedef	struct	{
	char	cmd[2] ;
	char	meth ;
	char	tpksz ;
	char	tpk[32] ;
	char	pvk[16] ;
	char	pinbk[16] ;
	char	acctno[12] ;
	char	val[16] ;
	char	ofs[4] ;
}	CMDfSS ;

typedef	struct	{
	char	cmd[2] ;
	char	meth ;
	char	tpksz ;
	char	tpk[48] ;
	char	pvk[16] ;
	char	pinbk[16] ;
	char	acctno[12] ;
	char	val[16] ;
	char	ofs[4] ;
}	CMDxSS ;
#ifdef	__cplusplus
}
#endif
#endif	/* _HSMAPI_H */
