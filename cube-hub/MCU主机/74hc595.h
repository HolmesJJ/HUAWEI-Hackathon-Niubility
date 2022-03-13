
#ifndef __74HC595_H__
#define __74HC595_H__

sbit SD	   = P2^0;	//������������
sbit ST_CK = P2^1;	//�洢�Ĵ���ʱ������
sbit SH_CK = P2^2;	//��λ�Ĵ���ʱ������
#define yes  49
#define no   50
#define betL 51
#define kaiji 52
uchar code yinbiao[][32];


void show_yinbiao(uchar a);

void dat_in(unsigned char Data);
void dat_inf(unsigned char Data);
void dat_out(void);
void show_line(char x,uchar dat1,uchar dat2);
void show_xy(char x,char y);



#endif