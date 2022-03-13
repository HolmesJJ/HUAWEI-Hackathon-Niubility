#include "config.h"

void init_Timer0()
{
	TMOD=0x01;
	TH0=(65536-1000)/256;
	TL0=(65536-1000)%256;
	ET0=1;
	TR0=1;
}

extern uint msTime;
extern uchar showtime;
extern uchar show1;
extern uchar check;
extern uchar AA;
extern xdata uchar soundmark[3];
uchar second;
extern uchar start;

extern xdata uchar time;


void Timer0_handel() interrupt 1
{
	TH0=(65536-1000)/256;
	TL0=(65536-1000)%256;
	
	showtime++;
	if(showtime>15)showtime=0;
	show_yinbiao(show1);
	
	msTime++;
	if(msTime>1000)
	{
		msTime=0;
		if(check!=0)
		{
			second++;
			if(second>5)
			{
				soundmark[0]=0;
				soundmark[1]=0;
				soundmark[2]=0;
				check=0;
				start=0;
				ES=1;
				AA = 1;
			}
		}
		if(start==1)
		{
			time--;
			if(time==0)
			{
				
				check=2;
				start=0;
				ES=1;
			}	
		}
		
		
	}
	
}