 //...actual Java code...

 // 
--------------------------------------------------------------------------------
// 
--------------------------------------------------------------------------------
// class InstallFirmwareSettings
 //
 // This class is used to pass in all necessary settings to the
 // installNewRabbitFirmware function.
 //

 class InstallFirmwareSettings extends Object{

 public byte loadFirmwareCmd;
 public byte noAction;
 public byte error;
 public byte sendDataCmd;
 public byte dataCmd;
 public byte exitCmd;

 }//end of class InstallFirmwareSettings
 // 
--------------------------------------------------------------------------------
// 
--------------------------------------------------------------------------------

 // 
--------------------------------------------------------------------------------
// Capulin1::installNewRabbitFirmware
 //
 // Transmits the Rabbit firmware image to the UT board to replace the existing
 // code.
 //
 // See corresponding function in the parent class Board.
 //

 public void installNewRabbitFirmware()
 {

 //create an object to hold codes specific to the UT board for use by the
 //firmware installer method

 InstallFirmwareSettings settings = new InstallFirmwareSettings();
 settings.loadFirmwareCmd = LOAD_FIRMWARE_CMD;
 settings.noAction = NO_ACTION;
 settings.error = ERROR;
 settings.sendDataCmd = SEND_DATA_CMD;
 settings.dataCmd = DATA_CMD;
 settings.exitCmd = EXIT_CMD;

 installNewRabbitFirmwareHelper(
 "UT", "Rabbit\\CAPULIN UT BOARD.bin", settings);

 }//end of Capulin1::installNewRabbitFirmware
 // 
--------------------------------------------------------------------------------

 // 
--------------------------------------------------------------------------------
// Board::installNewRabbitFirmwareHelper
 //
 // Transmits the Rabbit firmware code to the specified board to replace the
 // existing code.
 //
 // The firmware in the Rabbit is stored in flash memory. There is a slight
 // danger in installing new firmware because the Rabbit may become inoperable\
 // if a power glitch occurs during the process -- a reload via serial cable
 // would then be required.
 //
 // Since there is a danger of locking up the Rabbit and the flash memory can be
 // written to a finite number of times, the code is not sent each time the
 // system starts as is done with the FPGA code. Rather, it is only updated
 // by explicit command.
 //
 // This function uses TCP/IP and transmits to the single board handled by
 // this Board object. If multiple boards are being loaded simultaneously,
 // the load time increases significantly.
 //
 // This function uses the "binary file" (*.bin) produced by the Dynamic C
 // compiler.
 //
 // The file is transmitted in 1025 byte blocks: one command byte followed by
 // 1024 data bytes. The last block is truncated as necessary.
 //
 // The remote should send the command SEND_DATA when it is ready for each
 // block, including the first one.
 //

 void installNewRabbitFirmwareHelper(String pBoardType, String pFilename,
 InstallFirmwareSettings pS)
 {

 int CODE_BUFFER_SIZE = 1025; //transfer command word and 1024 data bytes
 byte[] codeBuffer;
 codeBuffer = new byte[CODE_BUFFER_SIZE];
 int remoteStatus;
 int timeOutRead;
 int bufPtr;
 int pktCounter = 0;

 boolean fileDone = false;

 FileInputStream inFile = null;

 try {

 sendBytes(pS.loadFirmwareCmd); //send command to initiate loading

 logger.logMessage(pBoardType + " " + ipAddrS +
 " loading Rabbit firmware..." + "\n");

 timeOutRead = 0;
 inFile = new FileInputStream(pFilename);
 int c, inCount;

 while(timeOutRead < FIRMWARE_LOAD_TIMEOUT){

 inBuffer[0] = pS.noAction; //clear request byte from host
 //clear status word (upper byte) from host
 inBuffer[1] = pS.noAction;
 //clear status word (lower byte) from host
 inBuffer[2] = pS.noAction;

 remoteStatus = 0;

 //check for a request from the remote if connected
 if (byteIn != null){
 inCount = byteIn.available();
 //0 = buffer offset, 2 = number of bytes to read
 if (inCount >= 3) {byteIn.read(inBuffer, 0, 3);}
 remoteStatus = (int)((inBuffer[0]<<8) &amp; 0xff00)
 + (int)(inBuffer[1] &amp; 0xff);
 }

 //trap and respond to messages from the remote
 if (inBuffer[0] == pS.error){
 logger.logMessage(pBoardType + " " + ipAddrS +
 " error loading firmware, error code: " + remoteStatus + "\n");
 return;
 }

 //send data packet when requested by remote
 if (inBuffer[0] == pS.sendDataCmd &amp;&amp; !fileDone){

 bufPtr = 0; c = 0;
 codeBuffer[bufPtr++] = pS.dataCmd; // command byte = data packet

 //be sure to check bufPtr on left side or a byte will get read
 //and ignored every time bufPtr test fails
 while (bufPtr<CODE_BUFFER_SIZE &amp;&amp; (c = inFile.read()) != -1 ) {

 //stuff the bytes into the buffer after the command byte
 codeBuffer[bufPtr++] = (byte)c;

 //reset timer in this loop so it only gets reset when
 //a request has been received AND not at end of file
 timeOutRead = 0;

 }//while (bufPtr<CODE_BUFFER_SIZE...

 if (c == -1) {fileDone = true;}

 //send packet to remote -- at the end of the file, this may have
 //random values as padding to fill out the buffer
 byteOut.write(codeBuffer, 0 /*offset*/, CODE_BUFFER_SIZE);

 pktCounter++; //track number of packets sent

 //send the exit command when the file is done
 if (fileDone){
 codeBuffer[0] = pS.exitCmd;
 byteOut.write(codeBuffer, 0 /*offset*/, 1);
 break;
 }//if (fileDone)

 }//if (inBuffer[0] == SEND_DATA)

 //count loops - will exit when max reached
 //this is reset whenever a packet request is received and the end of
 //file not reached - when end of file reached, loop will wait until
 //timeout reached again before exiting in order to catch success/error
 //messages from the remote

 timeOutRead++;

 }// while(timeOutGet <...

 if (fileDone) {
 logger.logMessage(
 pBoardType + " " + ipAddrS + " firmware installed." + "\n");
 }
 else {
 logger.logMessage(pBoardType + " " + ipAddrS +
 " error loading firmware - contact lost after " + pktCounter +
 " packets." + "\n");
 }

 }//try
 catch(IOException e){
 System.err.println(getClass().getName() + " - Error: 947");
 logger.logMessage(
 pBoardType + " " + ipAddrS + " error loading firmware!" + "\n");
 }
 finally {
 if (inFile != null) {try {inFile.close();}catch(IOException e){}}
 }//finally


 //display status message sent back from remote

 String msg = null;
 int c = 0;

 //loop for a bit looking for messages from the remote
 do{
 try{msg = in.readLine();} catch(IOException e){}
 if (msg != null){
 logger.logMessage("UT " + ipAddrS + " says " + msg + "\n");
 msg = null;
 }
 }while(c++ < 50);

 }//end of Board::installNewRabbitFirmwareHelper
 // 