// NOT THE REAL EEPROM.h, this is for the Vorpal_Gamepad application only

#ifndef VORPAL_GAMEPAD_EEPROM_H
#define VORPAL_GAMEPAD_EEPROM_H

class EEPROM_class {
private:
    uint8_t data [256];
public:
    uint8_t read( int idx )              { return data[idx]; }
    void update( int idx, uint8_t val )  { data[idx] =  val; }
};
EEPROM_class EEPROM; //!!!!! added

#endif //VORPAL_GAMEPAD_EEPROM_H


