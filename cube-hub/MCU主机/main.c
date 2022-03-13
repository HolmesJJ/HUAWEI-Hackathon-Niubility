#include "config.h"

uchar show1=0;
uchar show2=0;
uchar show3=0;

uchar result;
uchar key;

uchar check;
uchar AA = 0;
uchar check1 = 0;

uchar xdata order = 0;
uchar xdata time;
uchar xdata soundmark[3] = {0x00,0x00,0x00};
uchar xdata sit1;
uchar xdata sit2;
uchar xdata sit3;
uchar xdata sta=0;
uchar start;
uint msTime;
void main()
{
	uchar i;
	uchar xdata RxBuf[32];	
	delay_ms(500);
	EA=1;
	InitADC();
	init_Timer0();
	delay_ms(100);
	get_ADC_Result(7);
	get_ADC_Result(7);
	
	if(get_ADC_Result(7)>134)//��ѹ��
	{
		show1=betL;
		delay_ms(3000);
		show1=0;
		delay_ms(10);
		EA=0;
		while(1);//����
	}
	
	
	show1=kaiji;
	delay_ms(1000);
	show1=0;
	
	
	init_Timer0();
	init_NRF24L01() ;
	uart_init();
	
	
	SetTX_Mode();
	nRF24L01_TxPacket(TxBuf);	// Transmit Tx buffer data
	SPI_RW_Reg(WRITE_REG+STATUS,0XFF);  
	SetRX_Mode();
	
	while(1)
	{
		
		if(check==0)switch (order)//�������������ʾ���ݷ���������ӻ�1�ӻ�2
		{
			case 0:
			show1=soundmark[0];
			show2=soundmark[1];
			show3=soundmark[2];
			key=5;
				break;
			case 1:
			show1=soundmark[0];
			show3=soundmark[1];
			show2=soundmark[2];
			key=7;
				break;
			case 2:
			show2=soundmark[0];
			show1=soundmark[1];
			show3=soundmark[2];
			key=11;
				break;
			case 3:
			show2=soundmark[0];
			show3=soundmark[1];
			show1=soundmark[2];
			key=15;
				break;
			case 4:
			show3=soundmark[0];
			show1=soundmark[1];
			show2=soundmark[2];
			key=19;
				break;
			case 5:
			show3=soundmark[0];
			show2=soundmark[1];
			show1=soundmark[2];
			key=21;
				break;
		}
		if(check != 0 && AA ==0)//ƴ��һ�� ��˸
		{
			start=0;
			if(check==1)
			{
				check1 =1;
				if(msTime>500)
				{
					show1=yes;
					show2=yes;
					show3=yes;
				}
				else
				{
					show1=0;
					show2=0;
					show3=0;
				}
				
			}
			else if(check==2)
			{
				check1 = 0;
				if(msTime>500)
				{
					show1=no;
					show2=no;
					show3=no;
				}
				else
				{
					show1=0;
					show2=0;
					show3=0;
				}
			}
		}
		else if(AA >= 1)//����
		{
			if(check1==1)//ƴ���� 
			{
					show1=yes;//��ʾЦ��
					show2=yes;
					show3=yes;

				
			}
			else if(check1==0)//ƴ����
			{
					show1=no;//��ʾ����
					show2=no;
					show3=no;
			}
		}
		
		sit1=check_AD();//��ȡ������λ��
		
		
			

		if(sit1!=0 && (sit1+sit2+sit3)==6 )	//�������Ƿ�ƴ��һ��
		{
			if(order==0 && sit1==2 && sit2==3 && sit3==1){check=1;}//������˳���Ƿ���ȷ
			else if(order==1 && sit1==2 && sit2==1 && sit3==3){check=1;}
			else if(order==2 && sit1==3 && sit2==2 && sit3==1){check=1;}
			else if(order==3 && sit1==1 && sit2==2 && sit3==3){check=1;}
			else if(order==4 && sit1==3 && sit2==1 && sit3==2){check=1;}
			else if(order==5 && sit1==1 && sit2==3 && sit3==2){check=1;}
			else//ƴ��
			{
				check=2;
			}
		}
		else //û��ƴ��һ��
		{
			check=0;
		}
		uart_send(check+0x30);//���ֻ����͵�ǰ״̬

	
		if(msTime%100>50 && sta==0)//24L01���ʹӻ�1
		{
			sta=1;
			
			TxBuf[0]=1;
			TxBuf[1]=show2;
			
			SetTX_Mode();
			nRF24L01_TxPacket(TxBuf);	// Transmit Tx buffer data
			SPI_RW_Reg(WRITE_REG+STATUS,0XFF);
			
			TxBuf[0]=1;
			TxBuf[1]=show2;

			SetTX_Mode();
			nRF24L01_TxPacket(TxBuf);	// Transmit Tx buffer data
			SPI_RW_Reg(WRITE_REG+STATUS,0XFF);			
			SetRX_Mode();
		}
		else if(msTime%100<50 && sta==1)//24L01���ʹӻ�2
		{
			sta=0;
			TxBuf[0]=2;
			TxBuf[1]=show3;
			
			SetTX_Mode();
			nRF24L01_TxPacket(TxBuf);	// Transmit Tx buffer data
			SPI_RW_Reg(WRITE_REG+STATUS,0XFF);  
			
			TxBuf[0]=2;
			TxBuf[1]=show3;
			
			SetTX_Mode();
			nRF24L01_TxPacket(TxBuf);	// Transmit Tx buffer data
			SPI_RW_Reg(WRITE_REG+STATUS,0XFF);
			SetRX_Mode();
		}
		
		
		
		
			
	
   		if(nRF24L01_RxPacket(RxBuf)!=0)//24L01����
		{
			if(RxBuf[0]==1)//�ӻ�1
			{
				sit2=RxBuf[1];//��ȡ�ӻ�1λ��
			}
			if(RxBuf[0]==2)//�ӻ�2
			{
				sit3=RxBuf[1];//��ȡ�ӻ�2λ��
			}
			SetRX_Mode();
		}
			
	}
}
