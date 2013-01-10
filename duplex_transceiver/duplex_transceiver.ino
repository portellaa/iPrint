#include <PN532.h>

#define SCK 13
#define MOSI 11
#define SS 10
#define MISO 12

byte recept[16];
byte dataIn[16];
int length;
byte DataOut[16]; //16bytes
byte DataIn[16];//Should be 16bytes

int i;

PN532 nfc(SCK, MISO, MOSI, SS);

void setup(){

  Serial.begin(115200);      //Configure serial port
  Serial.println("Hello!");
  Serial.flush();
  
  DataOut[0] = 49;

  nfc.begin();

  uint32_t versiondata = nfc.getFirmwareVersion();
  if (! versiondata) {
    Serial.print("Didn't find PN53x board");
    while (1); // halt
  }

  // configure board to read RFID tags and cards
  nfc.SAMConfig();
}

void loop(){
  // Configure PN532 as Peer to Peer Target
  if(nfc.configurePeerAsTarget()) //if connection is error-free
  {
    //trans-receive data
    if(nfc.targetTxRx((char*)DataOut,(char*)DataIn))
    {
      Serial.print((char*)DataIn);
    }
  }  
}

void serialEvent(){

  length=Serial.available();
  Serial.readBytes((char*)recept, length);//read bytes from serial port
  
      //trans-receive data
  if(nfc.configurePeerAsInitiator(2)) //if connection is error-free
    if(nfc.initiatorTxRx((char*)recept,(char*)dataIn))
    {
      Serial.print((char*)dataIn);
      Serial.flush();
      for(i = 0; i < 16; i++)
      {
        recept[i] = 0;
      }
    }
}




