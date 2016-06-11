package com.kdgwill.chatman.protocol;

/**
 * A Very Simply Connected Protocol for Mesh Networking
 *
 * It attempts to connect to 3 remote host MAX and uses a 4th connection as a Test Connection
 * * We know MAX for ios and android is 10 bluetooth connections total; however,
 * * we must handle one at a time.
 * * Given this instead of keeping constant connection we can simply cache 4 links to keep update
 * * loop tight and and also actively search for a 5th device to replace on of the previous 4
 * ** NOTE: we can scale the max connections and see what works best during high congestion
 * ** This will have to be done by Simulation.
 *
 * These 4 remote host will be ranked by how many unique external connections they have.
 * All of their direct connections will be given to the host as a clumsy way of routing.
 * In turn the host will give it a list of it's own direct connection.
 *
 * In the event that two remote-hosts have direct connection to same remote-host
 * the remote host will drop it's connection to it if needed (I.E. counted against importance)
 *
 * A Hello packet will be sent every 5secs to ensure connection
 * A timer will be implemented and if no response in 10secs then connection will be dropped.
 *
 * In the event of a 5th connection it will be ranked against the other 4
 * in the event that this connection is somehow more valuable than one of the other 4 connections
 * it will replace said connection.
 *
 * Value is determined by unique connections in each list.
 * In the event that the new connection is as valuable as the least valuable connection it will
 * replace the connection
 *
 * Broadcasting will occur as flooding; when a broadcast message is seen it will be cached and
 * immediately re-broadcast. In the event that the same broadcast message reaches the host and
 * determined by comparison in the cache. It will be dropped.
 * Broadcast messages should have a TTL that is represented by real world time and not hop.
 *
 * AT THIS TIME:
 * This implementation will not allow direct messaging and will be re-evaluated in later protocols
 * that involve graphs and the ability to properly route messages without fear of dropping
 * connection
 *
 * We adhere to Bluetooth SIG UUID standards as such any UUID that is not in the form
 * 0000XXXX-0000-1000-8000-00805f9b34fb will broadcast the full 128 bit UUID.
 *
 * When we broadcast we follow the 16bit XXXX form or 32 bit XXXXXXXX form to indicate MAC address.
 * this means we are allowed 2^16 - 2^32(65536-4294967296)
 * remote hosts ideally ignoring possibility for collision.
 *
 * In order to prevent collision with other BLE services we use the full 128 bit UUID.
 *
 * We Use FFFFFFFF 16 bit UID to indicate broadcast and 00000000 is unused
 *
 * Maybe in future implement a way to generate MAC address after seeing full graph to prevent collision
 *
 * Created by kylewilliams on 3/16/16.
 * ~Hail-Innovation
 */
public interface SimpleMeshProtocol{
    //Implemented in .bleservice.MeshBleService
}
