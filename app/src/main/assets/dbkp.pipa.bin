.include "DummyHead.S"

/* Shell Code for Xiaomi Pad 6 (Pipa) */
_ShellCodeStart:
    movz    x4, #0xe004
    movk    x4, #0xf96, lsl #16    // Store 0xf96E004 to x4, which is address of GPIO 110.
    ldr     w6, [x4]                // Get status value from address stored in x4 and store it to w6.
    nop
    nop
    and     w6, w6, #1              // w6 & 1 and stored in w6.
    cbnz    w6, _UEFI               // Compare w6 with 0, if w6 == 0 goto _UEFI, else execute next instruction(jmp Linux).

.include "CommonTail.S"
/* Do not remove the last line  */
