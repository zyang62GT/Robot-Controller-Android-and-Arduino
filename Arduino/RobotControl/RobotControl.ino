/*
 * RobotControl Arduino sketch
 * VIP I-Natural Robot Team
 * Last Edited by Yao Lu, December 1, 2012
 */

// include Android-Arduino bluetooth communications library
// and Servo library
#include "MeetAndroid.h"
#include <Servo.h>

MeetAndroid meetAndroid;
int mSpeed;
int mAngle;

// pin assignments
int motorEnable1 = 2;
int motorEnable2 = 3;
int leftPin = 5;
int rightPin = 6;

// servo objects and initial angles
Servo armLeft;
Servo armRight;
int leftPos = 90;
int rightPos = 90;
int i;

void setup()
{
  Serial.begin(115200);
  
  // assign pins as outputs
  pinMode(motorEnable1, OUTPUT);
  pinMode(motorEnable2, OUTPUT);
  pinMode(leftPin, OUTPUT);
  pinMode(rightPin, OUTPUT);
  
  // enable motors to know when setup has finished
  digitalWrite(motorEnable1, HIGH);
  digitalWrite(motorEnable2, HIGH);
  
  // setup servo pins and set to initial angles
  armLeft.attach(9);
  armRight.attach(10);
  armLeft.write(leftPos);
  armRight.write(rightPos);

  // register functions to be called with the library
  meetAndroid.registerFunction(mLeft, 'l');
  meetAndroid.registerFunction(mRight, 'r');
  meetAndroid.registerFunction(mArmLeft, 'a');
  meetAndroid.registerFunction(mArmRight, 'b');
}

void loop()
{
  // always check for data from Android
  meetAndroid.receive();
}

// function that is called that controls the left wheel motor
void mLeft(byte flag, byte numOfValues)
{
  // an int between -10 and 10 is received that specifies speed
  mSpeed = meetAndroid.getInt();
  // disable motor if speed is 0, otherwise, enable motors
  // and convert to a value between 0 and 255
  if(mSpeed == 0){
    digitalWrite(motorEnable1, LOW);
  }else{
    digitalWrite(motorEnable1, HIGH);
    analogWrite(leftPin, (mSpeed+10)*255/20);
  }
}

// right wheel motor function
void mRight(byte flag, byte numOfValues)
{
  mSpeed = meetAndroid.getInt();
  if(mSpeed == 0){
    digitalWrite(motorEnable2, LOW);
  }else{
    digitalWrite(motorEnable2, HIGH);
    analogWrite(rightPin, 255 - (mSpeed+10)*255/20);
  }
}

// function that is called that controls the left servo
void mArmLeft(byte flag, byte numOfValues)
{
  // an int between 0 and 180 is received
  mAngle = meetAndroid.getInt();
  // update the position of the motor and write it to the servo
  leftPos = mAngle;
  armLeft.write(leftPos);
  delay(10);
}

// right servo function
void mArmRight(byte flag, byte numOfValues)
{
  mAngle = meetAndroid.getInt();
  rightPos = mAngle;
  armRight.write(rightPos);
  delay(10);
}
