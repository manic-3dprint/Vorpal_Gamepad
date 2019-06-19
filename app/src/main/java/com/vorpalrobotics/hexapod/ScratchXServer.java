package com.vorpalrobotics.hexapod;

/**
 * Interface for a ScratchX (http) server implementation
 *
 */
public interface ScratchXServer {
    byte[] EMPTY_SERIAL_INPUT = new byte[]{}; // clear serial input

    /**
     * start the Server
     * @return true if the Server started successfully, false otherwise
     */
    boolean startMe();

    /**
     * get the Serial input from the caller (ScratchX page)
     * @return the Serial input
     */
    byte[] getSerialInput();

    /**
     * send the Serial output to the caller (ScratchX page)
     * @param serialOutput_ the Serial output
     */
    void setSerialOutput(byte[] serialOutput_);
}
