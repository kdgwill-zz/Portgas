package com.kdgwill.chatman.protocol;

/**
 * ONLY FOR DEMONSTRATION SAKE!!
 *
 * A very simple connectionless protocol for ble mesh networking
 * <p>
 * This protocol simply floods the network with intended messages, upon receiving a message,
 * it first checks to see if the message is already cached; If it is it drops the message,
 * in the event that it is a new message it caches the message for a set duration rebroadcasts
 * the message
 * <p>
 * This Protocol does not support fragmentation or directed messages as the overhead of
 * the message organization would be to high do to hardware limitations of Bluetooth Low Energy
 * <p>
 * Created by Kyle D. Williams on 3/14/16.
 * ~Hail-Innovation
 */
public interface ConnectionlessProtocol {
    //Implemented in ConnectionlessMeshProtocol
}
