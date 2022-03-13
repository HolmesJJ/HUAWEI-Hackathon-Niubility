#include "config.h"


void InitADC()
{
	P1M0=0x00;
	P1M1=0x00;
    P1ASF = 0xff;                   //����P1��ΪAD��
    ADC_RES = 0;                    //�������Ĵ���
    ADC_CONTR = ADC_POWER | ADC_SPEEDLL;
    delay_ms(2);                       //ADC�ϵ粢��ʱ
}


uchar get_ADC_Result(uchar ch)
{
    ADC_CONTR = ADC_POWER | ADC_SPEEDLL | ch | ADC_START;
    _nop_();                        //�ȴ�4��NOP
    _nop_();
    _nop_();
    _nop_();
    while (!(ADC_CONTR & ADC_FLAG));//�ȴ�ADCת�����
    ADC_CONTR &= ~ADC_FLAG;         //Close ADC
	
	delay_ms(5);

    return ADC_RES;                 //����ADC���
}

uchar  tongdao[2][5];
//={
////0xff,0xff,0xff,0xff,0xff,
////0xff,0xff,0xff,0xff,0xff
//};


uchar check_AD()
{	
	uchar xdata s1=0;
	uchar xdata s2=0;
	uchar xdata err;
	uchar xdata i;
	for(i=0;i<4;i++)
	{
		tongdao[0][i]=tongdao[0][i+1];
		tongdao[1][i]=tongdao[1][i+1];
	}
	tongdao[0][4]=get_ADC_Result(1);
	tongdao[1][4]=get_ADC_Result(2);
	
	for(i=0;i<5;i++)
	{
		if(tongdao[0][i]<100)s1++;
		if(tongdao[1][i]<100)s2++;
	}
	err=0;
	if(s1>=3)
	{
		err+=1;
	}
	if(s2>=3)
	{
		err+=2;
	}

//	for(i=0;i<5;i++)
//	{
//		printNum(tongdao[0][i]);
//		uart_print("  ");
//		printNum(tongdao[1][i]);
//		uart_print("  ");
//	}
	
	return err;
}













