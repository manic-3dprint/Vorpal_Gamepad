// NOT THE REAL SdFat.cpp, this is for the Vorpal_Gamepad application only
// This code simulates the physical activity of the Vorpal Gamepad

#include <ctime>
#include "SdFat.h"
#include <errno.h>

#include <iostream>
#include <string>
#include <fstream>
extern bool isSDcard;
//extern byte TrimMode;
//extern int SRecState;
//extern int GRecState;
extern byte& TrimMode__V2;
extern byte& TrimMode__V3;
//extern int SRecState;
extern int& GRecState__V2;
extern int& GRecState__V3;

// pins
#define NO_VALUE (-1)
#define NO_ANALOG_VALUE 1000
#define DPAD_PIN_NUMBER 1
extern char internalFileDir[BUFFER_SIZE];
extern char errorMessage[BUFFER_SIZE];

int currentDPad = NO_ANALOG_VALUE;
int clickedRow = NO_VALUE;
int clickedColumn = NO_VALUE;
std::time_t powerOnTime;

////////////////////////////////////////////////
// called from java MainActivity
////////////////////////////////////////////////
void gamepadPowerOn() {
    powerOnTime = std::time(nullptr);
}

void gamepadPowerOff() {
    currentDPad = NO_ANALOG_VALUE;
    clickedRow = NO_VALUE;
    clickedColumn = NO_VALUE;
    TrimMode__V2 = 0;
    TrimMode__V3 = 0;
    GRecState__V2 = 0;
    GRecState__V3 = 0;
}

// set up state for the arduino pin read/write to simlulate
// user button presses
void clickGamepadButton(std::string buttonNameC, bool isOn) {
    if (buttonNameC == "b") {
        currentDPad = isOn ? 50 : NO_ANALOG_VALUE;
    } else if (buttonNameC == "l") {
        currentDPad = isOn ? 150 : NO_ANALOG_VALUE;
    } else if (buttonNameC == "r") {
        currentDPad = isOn ? 300 : NO_ANALOG_VALUE;
    } else if (buttonNameC == "f") {
        currentDPad = isOn ? 500 : NO_ANALOG_VALUE;
    } else if (buttonNameC == "w") {
        currentDPad = isOn ? 725 : NO_ANALOG_VALUE;
    } else {
        if (buttonNameC.length() == 2) {
            switch (buttonNameC.at(0)) {
                case 'W'  :
                    clickedRow = isOn ? 0 : NO_VALUE;
                    break;
                case 'D'  :
                    clickedRow = isOn ? 1 : NO_VALUE;
                    break;
                case 'F'  :
                    clickedRow = isOn ? 2 : NO_VALUE;
                    break;
                case 'R'  :
                    clickedRow = isOn ? 3 : NO_VALUE;
                    break;
                default :
                    clickedRow = NO_VALUE;
            }
            clickedColumn = isOn ? buttonNameC.at(1) - '1' : NO_VALUE;
        }
    }
}

////////////////////////////////////////////////
// alternate Arduino system implementation
////////////////////////////////////////////////
void pinMode(int pin, uint8_t value) {
    PIN_MODE[pin] = value;
    if (value == INPUT) {
        PIN_VALUE[pin] = HIGH;
    }
};

void digitalWrite(int pin, uint8_t value) {
    PIN_VALUE[pin] = value;
};

int digitalRead(int pin) {
    int readValue = PIN_VALUE[pin];
    if (pin >= MATRIX_COL_START_ && pin < MATRIX_COL_START_+MATRIX_NCOL_) {
        if (PIN_MODE[MATRIX_ROW_START_+clickedRow] == OUTPUT && pin-MATRIX_COL_START_ == clickedColumn) {
            readValue = LOW;
        } else {
            readValue = HIGH;
        }
    }
    return readValue;
};

int analogRead(uint8_t pin) {return pin == DPAD_PIN_NUMBER ? currentDPad : NO_ANALOG_VALUE;};
unsigned long millis() {return (unsigned long)(1000*difftime (std::time(nullptr), powerOnTime));};
unsigned long micros() {return (unsigned long)(1000000*difftime (std::time(nullptr), powerOnTime));};
void delay(unsigned long) {};
void delayMicroseconds(unsigned int) {};

char error_buffer[BUFFER_SIZE];
void checkError(const char* tag)
{
    int errnum = errno;
    if (errnum > 0) {
        strerror_r(errnum, error_buffer, BUFFER_SIZE);
        strcat(errorMessage, tag);
        strcat(errorMessage, ":");
        strcat(errorMessage, error_buffer);
        strcat(errorMessage, "\r\n");
    }
}
void displayError(const char* tag, const char* message)
{
    strcat(errorMessage, tag);
    strcat(errorMessage, ":");
    strcat(errorMessage, message);
    strcat(errorMessage, "\r\n");
}

////////////////////////////////////////////////
// alternate File implementation
////////////////////////////////////////////////
// the File i/o uses the Android NDK file system underneath to go to the Android internal file system

File::File() {

}
void File::setFILE(FILE* file)
{
    internalFile = file;
}
bool File::seek(uint32_t position_)
{
    if (!isSDcard)
    {
        return false;
    }
    if (internalFile == nullptr) {
        return false;
    }
    errno = 0;
    int i = fseek(internalFile, (long)position_, SEEK_SET);
    checkError("File::seek");
    return (i == 0);
};
int File::available()
{
    if (!isSDcard)
    {
        return (size_t)0;
    }
    if (internalFile == nullptr) {
        return (size_t)0;
    }
    errno = 0;
    long lCurrent = ftell (internalFile);
    checkError("File::available ftell 1");
    errno = 0;
    fseek (internalFile , 0 , SEEK_END);
    checkError("File::available fseek 1");
    errno = 0;
    long lSize = ftell (internalFile);
    checkError("File::available ftell 2");
    errno = 0;
    rewind (internalFile);
    checkError("File::available rewind");
    errno = 0;
    fseek (internalFile , lCurrent , SEEK_SET);
    checkError("File::available fseek 2");
    return (int)(lSize - lCurrent);
}
uint32_t File::position() {
    fpos_t pos;
    fsetpos (internalFile,&pos);
    return (uint32_t)pos;
}
uint32_t File::size() {
    if (!isSDcard)
    {
        return (size_t)0;
    }
    if (internalFile == nullptr) {
        return (size_t)0;
    }
    errno = 0;
    long lCurrent = ftell (internalFile);
    checkError("File::available ftell 1");
    errno = 0;
    fseek (internalFile , 0 , SEEK_END);
    checkError("File::available fseek 1");
    errno = 0;
    long lSize = ftell (internalFile);
    checkError("File::available ftell 2");
    errno = 0;
    rewind (internalFile);
    checkError("File::available rewind");
    errno = 0;
    fseek (internalFile , lCurrent , SEEK_SET);
    checkError("File::available fseek 2");
    return (uint32_t)lSize;
}
int File::read()
{
    if (!isSDcard)
    {
        return -1;
    }
    if (internalFile == nullptr) {
        return -1;
    }
    errno = 0;
    int count = (int)fread((void*) inputBuffer, 1, 1, internalFile);
    checkError("File::read");
    int result = -1;
    if (count == 0)
    {
        result = -1;
    } else if (count == 1)
    {
        result = inputBuffer[0];
    } else {
        displayError("File::read", strcat((char*)"count ", (std::to_string(count)).c_str()));
    }
    return result;
};
size_t File::write(uint8_t data_)
{
    if (!isSDcard)
    {
        return (size_t)0;
    }
    if (internalFile == nullptr) {
        return (size_t)0;
    }
    outputBuffer[0] = data_;
    errno = 0;
    size_t result = fwrite((void*) outputBuffer, 1, 1, internalFile);
    checkError("File::write");
    return result;
};
void File::flush()
{
    if (!isSDcard)
    {
        return;
    }
    if (internalFile != nullptr) {
        fflush(internalFile);
        checkError("File::flush");
    }
};
void File::close()
{
    if (!isSDcard)
    {
        return;
    }
    if (internalFile != nullptr) {
        errno = 0;
        fclose(internalFile);
        checkError("File::close");
    }
    internalFile = nullptr;
};
File::operator bool()
{
    if (!isSDcard)
    {
        return false;
    }
    return internalFile != nullptr;
}

int deleteFile(const char* filePath)
{
    errno = 0;
    int result = remove( filePath );
    checkError("deleteFile");
    return result;
}
////////////////////////////////////////////////
// alternate SdFat implementation
////////////////////////////////////////////////
// physical SD card is not used, the file i/o goes to the Android internal file system
bool SdFat::begin(uint8_t, SPISettings)
{
    return true;
};
File SdFat::open(const char * fileName, oflag_t)
{
    File file;
    if (!isSDcard)
    {
        return file;
    }
    const char* filePath = (std::string(internalFileDir) + "/" + std::string(fileName)).c_str();
    errno = 0;
    FILE* internalFile = fopen(filePath,"a+");
    if (internalFile == nullptr)
    {
        checkError("SdFat::open");
    }
    file.setFILE(internalFile);
    return file;
};
bool SdFat::exists(const char* fileName)
{
    if (!isSDcard)
    {
        return false;
    }
    struct dirent *entry;
    bool isFound = false;
    errno = 0;
    DIR *folder = opendir(internalFileDir);
    checkError("SdFat::exists opendir");
    if (folder != nullptr)
    {
        errno = 0;
        while ((entry = readdir(folder)) && !isFound) {
            checkError("SdFat::exists readdir");
            errno = 0;
            if (strcmp(fileName, entry->d_name) == 0) {
                isFound = true;
            }
        }
        errno = 0;
        closedir(folder);
        checkError("SdFat::exists closedir");
    }
    return isFound;
};
bool SdFat::remove(const char* fileName)
{
    if (!isSDcard)
    {
        return false;
    }
    const char* filePath = (std::string(internalFileDir) + "/" + std::string(fileName)).c_str();
    int result = deleteFile( filePath );
    return (result == 0);
};

////////////////////////////////////////////////
// alternate Sd2Card implementation
////////////////////////////////////////////////
// Sd2Card functions are only called in SDCardFormat(), not used in this application
bool Sd2Card::begin(uint8_t, SPISettings)
{
    if (!isSDcard)
    {
        return false;
    }
    // format SD card
    struct dirent *entry;
    errno = 0;
    DIR *folder = opendir(internalFileDir);
    checkError("Sd2Card::begin opendir");
    if (folder != nullptr)
    {
        errno = 0;
        while ((entry = readdir(folder))) {
            checkError("Sd2Card::begin readdir");
            errno = 0;
            if (strcmp(".",entry->d_name) != 0 && strcmp("..",entry->d_name) != 0)
            {
                const char* filePath = (std::string(internalFileDir) + "/" + std::string(entry->d_name)).c_str();
                int result = deleteFile( filePath );
            }
        }
        errno = 0;
        closedir(folder);
        checkError("Sd2Card::begin closedir");
    }
    return false;
};
uint32_t Sd2Card::cardCapacity()
{
    return 0;
};
uint32_t Sd2Card::cardSize()
{
    return cardCapacity();
}
bool Sd2Card::erase(uint32_t, uint32_t)
{
    return true;
};
bool Sd2Card::readBlock(uint32_t, uint8_t*)
{
    return true;
};
bool Sd2Card::writeBlock(uint32_t, const uint8_t*)
{
    return true;
};
bool Sd2Card::writeData(const uint8_t*)
{
    return true;
};
bool Sd2Card::writeStart(uint32_t, uint32_t)
{
    return true;
};
bool Sd2Card::writeStop()
{
    return true;
};
void Sd2Card::spiStop()
{

};

////////////////////////////////////////////////
// alternate SoftwareSerial implementation
////////////////////////////////////////////////
// SoftwareSerial i/o is received and sent to the java code MainActivity
// to be handled
// not checking for length
size_t SoftwareSerial::write(char* data_) {
    size_t dataSize = std::strlen( data_ );
//    output.push_back(data_);
    for (int loop = 0; loop < dataSize; loop++)
    {
        byte loopByte = (byte)(*(data_+loop));
        output.push_back(loopByte);
    }
    return dataSize;
};

size_t SoftwareSerial::printNumber(unsigned long n, uint8_t base)
{
    char buf[8 * sizeof(long) + 1]; // Assumes 8-bit chars plus zero byte.
    char *str = &buf[sizeof(buf) - 1];

    *str = '\0';

    // prevent crash if called with base == 1
    if (base < 2) base = 10;

    do {
        char c = n % base;
        n /= base;

        *--str = c < 10 ? c + '0' : c + 'A' - 10;
    } while(n);

    return write(str);
}

void SoftwareSerial::begin(unsigned long) {};
int SoftwareSerial::read()
{
    return input[inputPosition++];
};
int SoftwareSerial::available()
{
    return inputPosition < input.size();
};
size_t SoftwareSerial::write(char c)
{
    return print(c);
};


//size_t SoftwareSerial::print(const char[] data_)
size_t SoftwareSerial::print(const char* data_)
{
    return write((char*)data_);
};
size_t SoftwareSerial::print(char c_)
{
    output.push_back((byte)c_);
    return 1;
}
size_t SoftwareSerial::print(unsigned char uc_, int base_)
{
    output.push_back((byte)uc_);
    return 1;
}
size_t SoftwareSerial::print(int num_, int base_)
{
    return print((long) num_, base_);
}
size_t SoftwareSerial::print(unsigned int num_, int base_)
{
    return print((long) num_, base_);
}
size_t SoftwareSerial::print(long num_, int base_)
{
    if (base_ == 0) {
        return 0;//write(num);
    } else if (base_ == 10) {
        if (num_ < 0) {
            int t = print('-');
            num_ = -num_;
            return printNumber(num_, 10) + t;
        }
        return printNumber(num_, 10);
    } else {
        return printNumber(num_, base_);
    }

}
size_t SoftwareSerial::print(unsigned long num_, int base_) {
    return print((long)num_, base_);
}
//size_t SoftwareSerial::println(const char[] data_)
size_t SoftwareSerial::println(const char* data_)
{
    size_t n = print(data_);
    n += println();
    return n;
};
size_t SoftwareSerial::println(char c_)
{
    size_t n = print(c_);
    n += println();
    return n;
};
size_t SoftwareSerial::println(unsigned char uc_, int base_)
{
    size_t n = print(uc_, base_);
    n += println();
    return n;
};
size_t SoftwareSerial::println(int num_, int base_)
{
    size_t n = print(num_, base_);
    n += println();
    return n;
};
size_t SoftwareSerial::println(unsigned int num_, int base_)
{
    size_t n = print(num_, base_);
    n += println();
    return n;
};
size_t SoftwareSerial::println(long num_, int base_)
{
    size_t n = print(num_, base_);
    n += println();
    return n;
};
size_t SoftwareSerial::println(unsigned long num_, int base_)
{
    size_t n = print(num_, base_);
    n += println();
    return n;
};
size_t SoftwareSerial::println(void)
{
    print("\n");
    return (size_t)1;
};

void SoftwareSerial::setInput(std::vector<byte> input_)
{
    input = input_;
    inputPosition = 0;
};
void SoftwareSerial::clearInput()
{
    input.clear();
    inputPosition = 0;
};
void SoftwareSerial::clearOutput()
{
    output.clear();
};
std::vector<byte> SoftwareSerial::getOutput()
{
    return output;
};
