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
extern uchar shownum;

void Timer0_handel() interrupt 1
{
	TH0=(65536-1000)/256;
	TL0=(65536-1000)%256;
	
	showtime++;
	if(showtime>15)showtime=0;
	show_yinbiao(shownum);
	
	msTime++;
	if(msTime>1000)msTime=0;
	
}