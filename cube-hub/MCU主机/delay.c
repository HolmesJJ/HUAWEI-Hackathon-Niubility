#include "config.h"

void delay_ms(uint i)
{
	uint j;
	while(i--)
	{
		for(j=0;j<125*4;j++)
		{
			;
		}
	}
}

void delay_us(uint i)
{
	while(i--);
}