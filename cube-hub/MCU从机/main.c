#include "config.h"

#define jiqqihao 1 //1 2

uchar shownum=0;

uchar xdata order;
uchar xdata time;
uchar xdata soundmark[3];
uchar xdata sit1;
uchar xdata sit2;
uchar xdata sit3;
uchar xdata sta=0;

uint msTime;
void main()
{
	uchar i;
	uchar xdata RxBuf[32];	
	delay_ms(500);
	EA=1;
	init_Timer0();
	InitADC();
	init_NRF24L01() ;
	
	
	delay_ms(100);
	get_ADC_Result(7);
	get_ADC_Result(7);
	
	if(get_ADC_Result(7)>134)//µÁ—πµÕ
	{
		shownum=betL;
		delay_ms(3000);
		shownum=0;
		delay_ms(10);
		EA=0;
		while(1);//–›√ﬂ
	}
	
	
	shownum=kaiji;
	delay_ms(1000);
	shownum=0;
	
	
	init_NRF24L01() ;
	uart_init();
	SetRX_Mode();
	
	
	while(1)
	{
		
		sit1=check_AD();
		
   		if(nRF24L01_RxPacket(RxBuf)!=0)
		{
			if(RxBuf[0]==jiqqihao)
			{
				
				shownum=RxBuf[1];
				
				TxBuf[0]=jiqqihao;
				TxBuf[1]=sit1;
				
				SetTX_Mode();
				nRF24L01_TxPacket(TxBuf);	// Transmit Tx buffer data
				SPI_RW_Reg(WRITE_REG+STATUS,0XFF); 
				SetTX_Mode();
				nRF24L01_TxPacket(TxBuf);	// Transmit Tx buffer data
				SPI_RW_Reg(WRITE_REG+STATUS,0XFF); 				
				SetRX_Mode();
			}
			
			else if(RxBuf[0]==10)//ø’
			{
				shownum=0;
			}
			else if(RxBuf[0]==11)//∂‘
			{
				shownum=yes;
			}
			else if(RxBuf[0]==12)//¥Ì
			{
				shownum=no;
			}
			
			SetRX_Mode();
		}
			
	}
}
