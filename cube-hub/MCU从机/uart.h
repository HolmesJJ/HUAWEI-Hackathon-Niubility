#ifndef __UART_H__
#define __UART_H__

void uart_init();
void uart_print(uchar *str);
void uart_send(uchar dat);
void printNum(uint num);

//void UART_Printf(const char * fmt,...);

#endif