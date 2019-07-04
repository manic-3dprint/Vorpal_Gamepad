// NOT THE REAL SdFat.h, this is for the Vorpal_Gamepad application only

#ifndef VORPAL_GAMEPAD_SDFAT_H
#define VORPAL_GAMEPAD_SDFAT_H

#include <inttypes.h>
#include <string>
#include <vector>
#include <jni.h>
#include <dirent.h>

#define BUFFER_SIZE 1024

typedef uint8_t byte;
void gamepadPowerOn();
void gamepadPowerOff();
void clickGamepadButton(std::string buttonNameC, bool isOn);
static uint8_t PIN_MODE[20];
static uint8_t PIN_VALUE[20];

// replace Arduino system
// Arduino.h
#define HIGH 0x1
#define LOW  0x0

#define INPUT 0x0
#define OUTPUT 0x1
#define INPUT_PULLUP 0x2

#define MATRIX_ROW_START_ 6
#define MATRIX_COL_START_ 2
#define MATRIX_NCOL_ 4

#define lowByte(w) ((uint8_t) ((w) & 0xff))
#define highByte(w) ((uint8_t) ((w) >> 8))

void pinMode(int, uint8_t);
void digitalWrite(int, uint8_t);
int digitalRead(int);
int analogRead(uint8_t);
unsigned long millis(void);
unsigned long micros(void);
void delay(unsigned long);
void delayMicroseconds(unsigned int us);

// replace SdFat
#include "FatStructs.h"
#define O_RDONLY  0X00  ///< Open for reading only.
#define O_RDWR    0X02  ///< Open for reading and writing.
#define O_AT_END  0X04  ///< Open at EOF.
#define O_CREAT   0x10  ///< Create file if it does not exist.

#define IO_BUFFER_SIZE 1024
#define FILE_READ O_RDONLY
#define FILE_WRITE (O_RDWR | O_CREAT | O_AT_END)
// only the methods used by Vorpal-Hexapod-Gamepad.ino are implemented
class File {
public:
    File();
    void setFILE(FILE* file);
    bool seek(uint32_t pos);
    int available();
    uint32_t position();
    uint32_t size();
    int read();
    size_t write(uint8_t);
    void flush();
    void close();
    operator bool();
private:
    FILE* internalFile = nullptr; // the Android NDK file object used
    byte inputBuffer[IO_BUFFER_SIZE];
    byte outputBuffer[IO_BUFFER_SIZE];
};

class SPISettings {};
#define SD_SCK_MHZ(maxMhz) SPISettings()
union cache_t {
    /** Used to access cached file data blocks. */
    uint8_t  data[512];
    /** Used to access cached FAT16 entries. */
    uint16_t fat16[256];
    /** Used to access cached FAT32 entries. */
    uint32_t fat32[128];
    /** Used to access cached directory entries. */
    dir_t    dir[16];
    /** Used to access a cached Master Boot Record. */
    mbr_t    mbr;
    /** Used to access to a cached FAT boot sector. */
    fat_boot_t fbs;
    /** Used to access to a cached FAT32 boot sector. */
    fat32_boot_t fbs32;
    /** Used to access to a cached FAT32 FSINFO sector. */
    fat32_fsinfo_t fsinfo;
};
typedef int oflag_t;

#define SS 0
#define SPI_FULL_SPEED SPISettings()
// only the methods used by Vorpal-Hexapod-Gamepad.ino are implemented
class SdFat {
public:
    bool begin(uint8_t csPin = SS, SPISettings spiSettings = SPI_FULL_SPEED);
    File open(const char *path, oflag_t oflag = FILE_READ);
    bool exists(const char* path);
    bool remove(const char* path);
};

// only the methods used by Vorpal-Hexapod-Gamepad.ino are implemented
class Sd2Card {
public:
    bool begin(uint8_t csPin, SPISettings settings);
    uint32_t cardCapacity();
    uint32_t cardSize();
    bool erase(uint32_t firstBlock, uint32_t lastBlock);
    bool readBlock(uint32_t lba, uint8_t* dst);
    bool writeBlock(uint32_t lba, const uint8_t* src);
    bool writeData(const uint8_t* src);
    bool writeStart(uint32_t blockNumber, uint32_t eraseCount);
    bool writeStop();
    void spiStop();
};

#define A0 0
#define A1 1
#define A2 2
#define A3 3
#define A4 4
#define A5 5

// only the methods used by Vorpal-Hexapod-Gamepad.ino are implemented
#define DEC 10
class SoftwareSerial {
public:
    SoftwareSerial() {};
    SoftwareSerial(int a1, int a2) {};
    void begin(unsigned long baud);
    int read();
    int available();
    size_t write(char);

//    size_t print(const char[]);
    size_t print(const char*);
    size_t print(char);
    size_t print(unsigned char, int = DEC);
    size_t print(int, int = DEC);
    size_t print(unsigned int, int = DEC);
    size_t print(long, int = DEC);
    size_t print(unsigned long, int = DEC);
//    size_t print(double, int = 2);

//    size_t println(const char[]);
    size_t println(const char*);
    size_t println(char);
    size_t println(unsigned char, int = DEC);
    size_t println(int, int = DEC);
    size_t println(unsigned int, int = DEC);
    size_t println(long, int = DEC);
    size_t println(unsigned long, int = DEC);
//    size_t println(double, int = 2);
    size_t println(void);

    void setInput(std::vector<byte>);
    void clearInput();
    void clearOutput();
    std::vector<byte> getOutput();
private:
    size_t write(char* data_);
    size_t printNumber(unsigned long, uint8_t);
    std::vector<byte> input;
    std::vector<byte> output;
    unsigned int inputPosition = 0;
};
#endif //VORPAL_GAMEPAD_ALT_H
