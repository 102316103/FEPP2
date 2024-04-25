/*#ident   "@(#)dif:func.h     00.00.01        95/01/06" */
/*#ifndef  FUNC_H */
/*#define  FUNC_H */

/*
 * function structure
 */

/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 11 : PIN BLOCK TRANSLATION                                  */
/*----------------------------------------------------------------------------*/
struct func_11{
    unsigned char fcode;         /* FUNCTION CODE X'11' */
    unsigned char term_id[5];    /* ATM ID (branch-id + machine-id) */
    unsigned char pinblock[8];   /* CIPHER PIN BLOCK INPUT */
    unsigned char reserved[47];
};

struct r_func_11{
    unsigned char retcode;       /* RETURN CODE */
    unsigned char pinblock[8];   /* CIPHER PIN BLOCK OUTPUT */
    unsigned char reserved[51];
};

/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 12 : REQUEST ATM TX KEY                                     */
/*----------------------------------------------------------------------------*/
struct func_12{
    unsigned char fcode;         /* FUNCTION CODE X'12' */
    unsigned char term_id[5];      /* ATM ID (branch-id + machine-id) */
    unsigned char key_id;          /* New or Old */
    unsigned char reserved[54];
};

struct r_func_12{
    unsigned char retcode;       /* RETURN CODE */
    unsigned char atmkey[8];     /* DECIPHERED NEW ATM TX KEY */
    unsigned char reserved[51];
};
/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 14 : INTRA BANK PIN BLOCK GENERATION FOR VOICE TXN          */
/*----------------------------------------------------------------------------*/
struct func_14{
    unsigned char fcode;         /* FUNCTION CODE */
    unsigned char term_id[5];      /* ATM ID */
    unsigned char accdata[16];   /* ACC DATA */
    unsigned char reserved[39];
};

struct r_func_14{
    unsigned char retcode;       /* RETURN CODE */
    unsigned char pinblk[8];     /* CIPHER PIN BLOCK */
    unsigned char reserved[51];
};
/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 20 : MAC/SYNC ITEM GENERATION                               */
/*----------------------------------------------------------------------------*/
struct func_20{
    unsigned char fcode;         /* FUNCTION CODE X'20'       */
    unsigned char key_id;         /* KEY ID X'01' ~ X'03'      */
    unsigned char icv0[8];
    unsigned char unit;          /* NUMBER OF UNIT            */
    unsigned char macdata[32];   /* MAC DATA (OCCURS 4 TIMES) */
    unsigned char reserved[17];
};

struct r_func_20{
    unsigned char retcode;       /* RETURN CODE */
    unsigned char macitem[4];    /* MAC OR SYNC */
    unsigned char reserved[55];
};

/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 15 : ATM INPUT SYNC/MAC GENERATION                          */
/*----------------------------------------------------------------------------*/
struct func_15{
    unsigned char fcode;         /* FUNCTION CODE X'16'       */
    unsigned char atm_id[5];     
    unsigned char tx_acno[16];
    unsigned char tx_tfrin_acno[16];
    unsigned char tx_amount[10];
    unsigned char reserved[12];
};

struct r_func_15{
    unsigned char retcode;       /* RETURN CODE */
    unsigned char atm_sync[4];   /* SYNC */
    unsigned char atm_mac[4];    /* MAC  */
    unsigned char reserved[51];
};

/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 16 : ATM OUTPUT SYNC/MAC GENERATION                         */
/*----------------------------------------------------------------------------*/
struct func_16{
    unsigned char fcode;         /* FUNCTION CODE X'16'       */
    unsigned char atm_id[5];
    unsigned char tx_acno[16];
    unsigned char avail_bal[13];
    unsigned char rcode3[3];
    unsigned char tx_amount[10];
    unsigned char reserved[12];
};

struct r_func_16{
    unsigned char retcode;       /* RETURN CODE */
    unsigned char atm_sync[4];   /* SYNC */
    unsigned char atm_mac[4];    /* MAC  */
    unsigned char reserved[51];
};

/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 21 : INTRA ATM MAC/SYNC ITEM GENERATION                     */
/*----------------------------------------------------------------------------*/
struct func_21{
    unsigned char fcode;         /* FUNCTION CODE             */
    unsigned char term_id[5];         /* ATM ID                    */
    unsigned char icv0[8];
    unsigned char unit;          /* NUMBER OF UNIT            */
    unsigned char macdata[32];   /* MAC DATA (OCCURS 4 TIMES) */
    unsigned char macitem[4];    /* MAC OR SYNC FOR CHECK     */
    unsigned char reserved[10];
};

struct r_func_21{
    unsigned char retcode;       /* RETURN CODE */
    unsigned char macitem[4];     /* MAC OR SYNC gen by ATM comm. key */
    unsigned char reserved[55];
};

/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 22 : BRANCH REQUEST CHANGE RMT KEY                          */
/*----------------------------------------------------------------------------*/
struct func_22{
    unsigned char fcode;         /* FUNCTION CODE X'22'       */
    unsigned char key_id;         /* KEY ID X'80'             */
    unsigned char branch_type;    /* BRANCH TYPE '1'          */
    unsigned char branch_id[3];    /* BRANCH CODE             */
    unsigned char reserved[54];
};

struct r_func_22{
    unsigned char retcode;       /* RETURN CODE                  */
    unsigned char rmt_bkey[8];   /* ENCRYPTED NEW RMT BRANCH KEY */
    unsigned char check_data[4]; /* CHECK DATA                   */
    unsigned char reserved[47];
};
/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 23 : INTRA RMT TX. MAC ITEM CHECK                           */
/*----------------------------------------------------------------------------*/
struct func_23{
    unsigned char fcode;         /* FUNCTION CODE             */
    unsigned char key_id;         /* KEY ID '80'               */
    unsigned char func_type;      /* FUNCTION TYPE            */
    unsigned char branch_id[3];  /* BRANCH CODE             */
    unsigned char unit;          /* NUMBER OF UNIT            */
    unsigned char macdata[32];   /* MAC DATA (OCCURS 4 TIMES) */
    unsigned char macitem[4];    /* MAC OR SYNC FOR CHECK     */
    unsigned char reserved[17];
};

struct r_func_23{
    unsigned char retcode;       /* RETURN CODE */
    unsigned char reserved[59];
};

/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 30 : FISC CHANGE MAC KEY                                    */
/*----------------------------------------------------------------------------*/
struct func_30{
    unsigned char fcode;         /* FUNCTION CODE X'30'                  */
    unsigned char key_id;        /* KEY ID (X'01',X'02','X'03',X'11', 3DES X'4', X'5', X'6', X'12') */
    unsigned char newkey[24];     /* NEW MAC KEY ENCIPHER BY C.D. KEY     */
    unsigned char random[8];     /* RANDOM_NO(N) ENCIPHER BY NEW MAC KEY */
    unsigned char reserved[42];
};

struct r_func_30{
    unsigned char retcode;       /* RETURN CODE                          */
    unsigned char random1[8];    /* (N+1) ENCIPHER BY NEW MAC KEY        */
    unsigned char random2[8];    /* (N+2) ENCIPHER BY NEW MAC KEY        */
    unsigned char reserved[43];
};
/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 32 : UPDATE NEW FISC MAC KEY                                */
/*----------------------------------------------------------------------------*/
struct func_32{
    unsigned char fcode;         /* FUNCTION CODE X'32' */
    unsigned char key_id;         /* KEY ID              */
    unsigned char reserved[58];
};

struct r_func_32{
    unsigned char retcode;       /* RETURN CODE         */
    unsigned char reserved[59];
};

/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 37 : UPDATE NEW M-BANK RMT MAC KEY                          */
/*----------------------------------------------------------------------------*/
struct func_37{
    unsigned char fcode;         /* FUNCTION CODE X'37'  */
    unsigned char key_id;         /* KEY ID X'60' , X'70' */
    unsigned char flag;          /* 'S' STORE            */
    unsigned char bank_id[3];   /* BACNK CODE           */
    unsigned char reserved[54];
};

struct r_func_37{
    unsigned char retcode;       /* RETURN CODE          */
    unsigned char reserved[59];
};

/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 38 : OWN BANK REQUEST CHANGE RMT MAC KEY                    */
/*----------------------------------------------------------------------------*/
struct func_38{
    unsigned char fcode;         /* FUNCTION CODE                */
    unsigned char sbank[3];      /* SOURCE BANK                  */
    unsigned char dbank[3];      /* DESTINATION BANK             */
    unsigned char reserved[53];
};

struct r_func_38{
    unsigned char retcode;       /* RETURN CODE                  */
    unsigned char mackey[8];     /* MAC KEY ENCIPHER BY C.D. KEY */
    unsigned char chkdata[4];
    unsigned char reserved[47];
};
/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 40 : M-BANK REQUEST CHANGE RMT MAC KEY                      */
/*----------------------------------------------------------------------------*/
struct func_40{
    unsigned char fcode;         /* FUNCTION CODE                    */
    unsigned char sbank[3];      /* SOURCE BANK                      */
    unsigned char dbank[3];      /* DESTINATION BANK                 */
    unsigned char mackey[8];     /* NEW MAC KEY ENCIPHER BY C.D. KEY */
    unsigned char chkdata[4];
    unsigned char reserved[41];
};

struct r_func_40{
    unsigned char retcode;       /* RETURN CODE                      */
    unsigned char reserved[59];
};
/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 50 : INTRA BANK PIN BLOCK/SEC CODE                          */
/*----------------------------------------------------------------------------*/
struct func_50{
    unsigned char fcode;         /* FUNCTION CODE X'50' */
    unsigned char key_id;         /* KEY ID              */
    unsigned char term_id[5];
    unsigned char actdata[16];   /* KEY TYPE            */
    unsigned char offset[4];
    unsigned char pinblk[8];     /* CIPHER PIN BLOCK */
    unsigned char reserved[26];
};

struct r_func_50{
    unsigned char retcode;       /* RETURN CODE */
    unsigned char pinblk[8];     /* CIPHER PIN BLOCK */
    unsigned char offset[4];
    unsigned char seccode[3];    /* SECTION CODE */
    unsigned char reserved[44];
};
/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 55 : INTRA BANK CHANGE PIN                                  */
/*----------------------------------------------------------------------------*/
struct func_55{
    unsigned char fcode;         /* FUNCTION CODE X'55' */
    unsigned char key_id;         /* KEY ID              */
    unsigned char term_id[5];
    unsigned char actdata[16];   /* KEY TYPE            */
    unsigned char offset[4];
    unsigned char pinblk[8];     /* CIPHER PIN BLOCK */
    unsigned char reserved[26];
};

struct r_func_55{
    unsigned char retcode;       /* RETURN CODE */
    unsigned char pinblk[8];     /* CIPHER PIN BLOCK */
    unsigned char offset[4];
    unsigned char seccode[3];    /* SECTION CODE */
    unsigned char reserved[44];
};
/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 60 : RMT OWN BANK MAC/SYNC ITEM                          */
/*----------------------------------------------------------------------------*/
struct func_60{
    unsigned char fcode;         /* FUNCTION CODE */
    unsigned char key_id;         /* KEY ID */
    unsigned char bank_id[3];
    unsigned char keytype;       /* KEY TYPE */
    unsigned char icv0[8];
    unsigned char unit;          /* NUMBER OF UNIT */
    unsigned char macdata[32];   /* MAC DATA (OCCURS 4 TIMES) */
    unsigned char reserved[13];
};

struct r_func_60{
    unsigned char retcode;       /* RETURN CODE */
    unsigned char macitem[4];    /* MAC OR SYNC */
    unsigned char reserved[55];
};
/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 70 : RMT MEMBER BANK MAC/SYNC ITEM                          */
/*----------------------------------------------------------------------------*/
struct func_70{
    unsigned char fcode;         /* FUNCTION CODE             */
    unsigned char key_id;         /* KEY ID                    */
    unsigned char bank_id[3];
    unsigned char keytype;       /* KEY TYPE                  */
    unsigned char icv0[8];
    unsigned char unit;          /* NUMBER OF UNIT            */
    unsigned char macdata[32];   /* MAC DATA (OCCURS 4 TIMES) */
    unsigned char macitem[4];    /* MAC OR SYNC FOR CHECK     */
    unsigned char reserved[9];
};

struct r_func_70{
    unsigned char retcode;       /* RETURN CODE */
    unsigned char macitem[4];     /* MAC OR SYNC gen by old or new mac key */
    unsigned char reserved[55];
};

/*----------------------------------------------------------------------------*/
/*  FUNCTION CODE 92 : INTER BANK PIN BLOCK/SEC CODE                          */
/*----------------------------------------------------------------------------*/
struct func_92{
    unsigned char fcode;         /* FUNCTION CODE */
    unsigned char key_id;         /* KEY ID */
    unsigned char actdata[16];   /* KEY TYPE */
    unsigned char offset[4];
    unsigned char pinblk[8];     /* CIPHER PIN BLOCK */
    unsigned char reserved[30];
};

struct r_func_92{
    unsigned char retcode;       /* RETURN CODE */
    unsigned char pinblk[8];     /* CIPHER PIN BLOCK */
    unsigned char offset[4];
    unsigned char seccode[3];    /* SECTION CODE */
    unsigned char reserved[44];
};
/*--------------------------------------------------------------------------*/
/*  SSN & H/W DES. MESSAGE FORMATS ( SSN & H/W DES I/O 1000 BYTES )         */
/*--------------------------------------------------------------------------*/
struct ssn_to_hwdes{
   unsigned char txn_type[2];    /* TXN TYPE */
   unsigned char jobname[8];     /* REQUESTNG NAME */
   unsigned char datetime[12];   /* DATE TIME */
   unsigned char req_fun_type[2];/* REQUEST FUNCTION TYPE */
   unsigned char tx_seq_no[10];  /* TRANSACTION SEQ. NO */
   unsigned char return_code[4]; /* RETURN CODE */
   unsigned char reason_code[4]; /* REASON CODE */
   unsigned char reversed[18];   /* REVERSED */
   unsigned char req_no[2];      /* NO OF SUIP REQUEST */
   unsigned char suip_data[840]; /* 7 times at most */
   unsigned char reserved[98];    /* reserved */
};

/*#endif */
/* end of suip_str.h */
