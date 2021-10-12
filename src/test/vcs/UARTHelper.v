/***************************************************************************************
* Copyright (c) 2020-2021 Institute of Computing Technology, Chinese Academy of Sciences
* Copyright (c) 2020-2021 Peng Cheng Laboratory
*
* XiangShan is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2.
* You may obtain a copy of Mulan PSL v2 at:
*          http://license.coscl.org.cn/MulanPSL2
*
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
* EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
* MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
*
* See the Mulan PSL v2 for more details.
***************************************************************************************/

import "DPI-C" function void uart_putchar
(
  input byte c
);

module UARTHelper(
  input         clock,
  input         reset,
  input         putchar_valid,
  input  [7:0]  putchar_ch,
  input         getchar_valid,
  output [7:0]  getchar_ch
);

always@(posedge clock) begin
  if(putchar_valid)
    uart_putchar(putchar_ch);
end

assign getchar_ch = 'h0;

endmodule