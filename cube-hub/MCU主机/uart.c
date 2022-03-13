#include "config.h"

void uart_init()
{
	SCON = 0x50;		//8位数据,可变波特率
	AUXR |= 0x01;		//串口1选择定时器2为波特率发生器
	AUXR |= 0x04;		//定时器2时钟为Fosc,即1T
	T2L = 0xE8;		//设定定时初值
	T2H = 0xFF;		//设定定时初值
	AUXR |= 0x10;		//启动定时器2
	ES=1;
}

void uart_send(uchar dat)
{
	SBUF = dat;
	while(!TI);//等待发送完毕
	TI = 0;    
}

void uart_print(uchar *str)
{
	while(*str!=0)
	{
		uart_send(*str);
		str++;
	}
}

void printNum(uint num)
{
	char i;
	uchar sss[5];
	for(i=4;i>=0;i--)
	{
		sss[i]=num%10;
		num/=10;
	}
	for(i=0;i<5;i++)
	{
		uart_send(sss[i]+0x30);
	}
}

//void UART_Printf(const char * fmt,...)
//{
//  char xdata UART_String[128];
//  va_list ap;
//  va_start(ap,fmt);
//  vsprintf(UART_String,fmt,ap);
//  uart_print((uchar *)UART_String);
//  va_end(ap);
//}        


uchar temp;
uchar xdata startReceive;
uchar xdata RxTimes;
uchar xdata uart_RxBuf[20];

extern xdata uchar order;
extern xdata uchar time;
extern xdata uchar soundmark[3];
extern uchar start;
extern uchar second;

extern uchar AA;
void uart_handel() interrupt 4
{
	if(RI)
	{
		RI=0;
		temp=SBUF;
		
		if(temp=='A')
		{
			startReceive=1;
			RxTimes=0;
		}
		else if(temp=='D')
		{
			AA = 0;
			order=uart_RxBuf[0]-0x30;
			time=(uart_RxBuf[2]-0x30)*10+(uart_RxBuf[3]-0x30);
			
			soundmark[0]=(uart_RxBuf[5]-0x30)*10+(uart_RxBuf[6]-0x30);
			soundmark[1]=(uart_RxBuf[7]-0x30)*10+(uart_RxBuf[8]-0x30);
			soundmark[2]=(uart_RxBuf[9]-0x30)*10+(uart_RxBuf[10]-0x30);
			start=1;
			second=0;
		}
		else
		{
			uart_RxBuf[RxTimes]=temp;
			RxTimes++;
		}
	}
}