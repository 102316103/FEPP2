char c_key[8] = { 0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef } ;

shuffer(char *buf1, int len1)
{
	trans_to_blind(buf1,len1);
	shuff(buf1,len1);
}

unshuffer(char *buf1, int len1)
{
	shuff(buf1,len1);
	trans_to_see(buf1,len1);
}

trans_to_blind(char *temp_buf, int temp_len)
{
	int i;

	for(i=0;i <= temp_len;i++)
		if((temp_buf[i] >= 65) && (temp_buf[i] <= 70))
			temp_buf[i] = temp_buf[i] - 7;
}

trans_to_see(char *temp_buf, int temp_len)
{
	int i;

	for(i=0;i <= temp_len;i++)
		if((temp_buf[i] >= 58) && (temp_buf[i] <= 63))
			temp_buf[i] = temp_buf[i] + 7;
}

shuff(char *temp_buf, int temp_len)
{
	int new_len;
	
	if(temp_len>16) {
		printf("There are a error in shuff function -- length > 16\n");
		exit(0);
	}
	new_len = pack(temp_buf,temp_len);
	if(new_len > 4) {
		shu(&temp_buf[new_len - 4],4);
		shu(temp_buf,new_len - 4);
	}
	else
		shu(temp_buf,new_len);
	unpack(temp_buf,new_len,temp_len);
}

pack(char *t_buf, int t_len)
{
	char pack_buf[8];
	int j,i = 7;

	j = t_len;
	for( ;j > 0;j = j - 2) 
		if(j > 1)
			pack_buf[i--] = (((t_buf[j-2] & 0x0f) << 4) | (t_buf[j-1] & 0x0f));
		else
			pack_buf[i--] = (t_buf[j-1] & 0x0f);
	memcpy(t_buf,&pack_buf[i+1],7-i);
	return(t_len % 2 ? t_len / 2 + 1 : t_len / 2);
}

unpack(char *t_buf, int t1_len,int t2_len)
{
	char temp[16];
	int j;

	for(j=0;j < t1_len;j++) {
		temp[2 * j] = ((t_buf[j] >> 4) & 0x0f) | 0x30;
		temp[2 * j + 1] = (t_buf[j] & 0x0f) | 0x30;
	}
	if(t2_len % 2)
		memcpy(t_buf,&temp[1],t2_len);
	else
		memcpy(t_buf,temp,t2_len);
}

shu(t_buf,t_len)
char t_buf[];
int t_len;
{
	int i,j = 3;
       
	for(i = t_len - 1;i>=0;i--,j--)
		t_buf[i] = t_buf[i] ^ c_key[j];
}

xor(temp1,temp2,temp_len)
char temp1[],temp2[];
int temp_len;
{
	int i;

	for(i=0;i<temp_len;i++)
		temp1[i] = temp1[i] ^ temp2[i];
}

/*

main( int argc, char *argv[] )
{
   char buffer[4] = "ABCD";

   shuffer(buffer,4)  ;
   printf("buffer = %s\n", buffer ) ;

   unshuffer(buffer,4);
   printf("buffer = %s\n", buffer ) ;
   
}
*/