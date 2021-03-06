#include "config.h"



uchar code liexuan[]={0x01,0x02,0x04,0x08,0x10,0x20,0x40,0x80};
//串行数据输入
void dat_in(unsigned char Data)
{
	unsigned char i;
	for(i = 0; i < 8; i++)
	{
		SH_CK = 0;			//CLOCK_MAX=100MHz
		SD = Data & 0x80;
		Data <<= 1;
		SH_CK = 1;
	}
}

void dat_inf(unsigned char Data)
{
	unsigned char i;
//	Data=Data<<1|0x01;
	for(i = 0; i < 8; i++)
	{
		SH_CK = 0;			//CLOCK_MAX=100MHz
		SD = Data & 0x01;
		Data >>= 1;
		SH_CK = 1;
	}
}

//并行数据输出
void dat_out(void)
{
	ST_CK = 0;
	ST_CK = 1;
}


void show_line(char x,uchar dat1,uchar dat2)
{
	dat_inf(~dat1);
	dat_inf(~dat2);
	dat_in( (x-7)>0 ? liexuan[x-8] : 0);
	dat_in(	(x-7)<=0 ? liexuan[x] : 0);
	dat_out();
}

void show_xy(char x,char y)
{
	dat_inf(~(  (y-7)>0 ? liexuan[y-8] : 0  ));
	dat_inf(~(  (y-7)<=0 ? liexuan[y] : 0  ));
	dat_in( (x-7)>0 ? liexuan[x-8] : 0);
	dat_in(	(x-7)<=0 ? liexuan[x] : 0);
	dat_out();
}
uchar xdata chazidian[]={0,0,0,0,0,0,0,0,0,1,0,2,3,4,0,0,0,0,5,6,7,0,0,0,8,0,9,0,10,0,0,0,11,0,0,0,0,0,0,0,0,12,0,0,13,14,0,15,16,17,18,19};
uchar showtime;
void show_yinbiao(uchar a)
{
	if(a==0)
	{
			show_line(showtime,0x00,0x00);

	}
	else
	{
		a--;
		show_line(showtime,yinbiao[chazidian[a]-1][showtime],yinbiao[chazidian[a]-1][showtime+16]);
	}
}


uchar code yinbiao[][32]={









0x00,0x00,0x00,0x00,0x00,0x80,0x40,0x40,0x40,0x40,0x80,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0E,0x12,0x12,0x12,0x12,0x0F,0x00,0x00,0x00,0x00,0x00,

0x00,0x00,0x00,0x40,0x40,0x40,0x40,0x80,0x80,0x40,0x40,0x40,0x40,0x80,0x00,0x00,0x00,0x00,0x1C,0x22,0x22,0x22,0x12,0x0F,0x12,0x22,0x22,0x22,0x22,0x13,0x00,0x00,
0x00,0x00,0x00,0x80,0xC0,0x40,0x40,0x40,0x80,0x00,0x00,0x00,0xC4,0x00,0x00,0x00,0x00,0x00,0x00,0x1F,0x32,0x22,0x22,0x22,0x13,0x02,0x00,0x00,0x3F,0x00,0x00,0x00,
0x00,0x00,0x00,0x80,0x40,0x40,0x40,0xC0,0x80,0x00,0x00,0xC4,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x1C,0x22,0x22,0x22,0x12,0x3F,0x00,0x00,0x3F,0x00,0x00,0x00,0x00,




0x00,0x80,0x40,0x40,0x40,0x80,0x00,0x00,0x00,0xC0,0x00,0x00,0x00,0x00,0xC0,0x00,0x18,0x34,0x22,0x22,0x12,0x3F,0x00,0x00,0x00,0x1F,0x20,0x20,0x20,0x10,0x3F,0x00,
0x00,0x40,0x40,0x40,0x40,0x80,0x00,0x00,0x00,0xC0,0x00,0x00,0x00,0x00,0xC0,0x00,0x0C,0x12,0x12,0x12,0x12,0x0F,0x00,0x00,0x00,0x1F,0x20,0x20,0x20,0x10,0x3F,0x00,
0x00,0x00,0x00,0x00,0x00,0xC0,0x80,0x40,0x40,0x40,0x80,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xFF,0x08,0x10,0x10,0x10,0x0F,0x00,0x00,0x00,0x00,0x00,



0x00,0x00,0x00,0x00,0x00,0xFC,0x00,0x00,0x00,0x80,0x40,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x3F,0x04,0x02,0x05,0x18,0x20,0x00,0x00,0x00,0x00,0x00,

0x00,0x00,0x00,0x00,0x00,0x00,0x40,0xF8,0x4C,0x44,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x3F,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,

0x00,0x00,0x00,0x00,0x00,0x80,0x40,0x40,0x40,0x80,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x11,0x22,0x22,0x24,0x18,0x00,0x00,0x00,0x00,0x00,0x00,



0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xF8,0x04,0x04,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x20,0x40,0x40,0x3F,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,








0x00,0x00,0x00,0x00,0x00,0x80,0x00,0x80,0x80,0x80,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x7F,0x01,0x00,0x00,0x00,0x7F,0x00,0x00,0x00,0x00,0x00,


0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xF8,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x7F,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x80,0x80,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x7F,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,

0x00,0x00,0x00,0x80,0x00,0x00,0x00,0x80,0x00,0x00,0x00,0x00,0x80,0x00,0x00,0x00,0x00,0x00,0x00,0x07,0x38,0x60,0x1E,0x01,0x07,0x38,0x60,0x1C,0x01,0x00,0x00,0x00,


0x00,0x00,0xC0,0x20,0x10,0x88,0x08,0x08,0x08,0x08,0x88,0x10,0x24,0xC8,0x04,0x02,
0x00,0x00,0x0F,0x18,0x20,0x25,0x68,0x48,0x48,0x68,0x25,0x20,0x18,0x0F,0x00,0x00,
0x00,0x00,0x80,0x40,0x20,0x10,0x10,0x10,0x10,0x10,0x10,0x20,0x40,0xA8,0x10,0x28,
0x00,0x00,0x0F,0x10,0x20,0x53,0x48,0x48,0x48,0x48,0x53,0x20,0x10,0x0F,0x00,0x00,


0x00,0x00,0x00,0x00,0xF8,0x08,0x0C,0x0C,0x0C,0x0C,0x08,0xF8,0x00,0x00,0x00,0x00,
0x00,0x00,0x00,0x00,0x3F,0x20,0x28,0x28,0x28,0x28,0x20,0x3F,0x00,0x00,0x00,0x00,

0xFF,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0xFF,
0xFF,0x80,0x80,0x80,0x80,0x80,0x80,0x80,0x80,0x80,0x80,0x80,0x80,0x80,0x80,0xFF,
};



