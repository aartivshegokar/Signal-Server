package org.whispersystems.dispatch.redis;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.dispatch.io.RedisInputStream;
import org.whispersystems.dispatch.redis.protocol.ArrayReplyHeader;
import org.whispersystems.dispatch.redis.protocol.IntReply;
import org.whispersystems.dispatch.redis.protocol.StringReplyHeader;
import org.whispersystems.dispatch.util.Util;

public class PubSubConnection {

  private final Logger logger = LoggerFactory.getLogger(PubSubConnection.class);

  private static final byte[] UNSUBSCRIBE_TYPE    = {'u', 'n', 's', 'u', 'b', 's', 'c', 'r', 'i', 'b', 'e'     };
  private static final byte[] SUBSCRIBE_TYPE      = {'s', 'u', 'b', 's', 'c', 'r', 'i', 'b', 'e'               };
  private static final byte[] MESSAGE_TYPE        = {'m', 'e', 's', 's', 'a', 'g', 'e'                         };
  private static final byte[] SUBSCRIBE_COMMAND   = {'S', 'U', 'B', 'S', 'C', 'R', 'I', 'B', 'E', ' '          };
  private static final byte[] UNSUBSCRIBE_COMMAND = {'U', 'N', 'S', 'U', 'B', 'S', 'C', 'R', 'I', 'B', 'E', ' '};
  private static final byte[] CRLF                = {'\r', '\n'                                                };

  /*
   * START: Custom Changes to accept redis authentication with password
   */
  private final String           password;
  private static final byte[] AUTH_COMMAND        = {'A', 'U', 'T', 'H', ' '                                   };
  
	public void auth() throws IOException {
		if (closed.get())
			throw new IOException("Connection closed!");
		if (password != null) {
			byte[] command = Util.combine(AUTH_COMMAND, password.getBytes(), CRLF);
			outputStream.write(command);
			flush();
			String authResult = inputStream.readLine();
			logger.info("Redis AUTH result: " + authResult);
		}
	}

	protected void flush() {

		try {
			outputStream.flush();
		} catch (IOException e) {
			logger.warn("Exception while flushing", e);
		}
	}
  /*
   * END: Custom Changes to accept redis authentication with password
   */
  
  private final OutputStream     outputStream;
  private final RedisInputStream inputStream;
  private final Socket           socket;
  private final AtomicBoolean    closed;

  public PubSubConnection(Socket socket, String password) throws IOException {
    this.socket       = socket;
    this.outputStream = socket.getOutputStream();
    this.inputStream  = new RedisInputStream(new BufferedInputStream(socket.getInputStream()));
    this.closed       = new AtomicBoolean(false);
    
    /*
     * START: Custom changes
     */
    this.password = password;
    this.auth();
    /*
     * END: Custom changes
     */
  }

  public void subscribe(String channelName) throws IOException {
    if (closed.get()) throw new IOException("Connection closed!");

    byte[] command = Util.combine(SUBSCRIBE_COMMAND, channelName.getBytes(), CRLF);
    outputStream.write(command);
  }

  public void unsubscribe(String channelName) throws IOException {
    if (closed.get()) throw new IOException("Connection closed!");

    byte[] command = Util.combine(UNSUBSCRIBE_COMMAND, channelName.getBytes(), CRLF);
    outputStream.write(command);
  }

  public PubSubReply read() throws IOException {
    if (closed.get()) throw new IOException("Connection closed!");

    ArrayReplyHeader replyHeader = new ArrayReplyHeader(inputStream.readLine());

    if (replyHeader.getElementCount() != 3) {
      throw new IOException("Received array reply header with strange count: " + replyHeader.getElementCount());
    }

    StringReplyHeader replyTypeHeader = new StringReplyHeader(inputStream.readLine());
    byte[]            replyType       = inputStream.readFully(replyTypeHeader.getStringLength());
    inputStream.readLine();

    if      (Arrays.equals(SUBSCRIBE_TYPE, replyType))   return readSubscribeReply();
    else if (Arrays.equals(UNSUBSCRIBE_TYPE, replyType)) return readUnsubscribeReply();
    else if (Arrays.equals(MESSAGE_TYPE, replyType))     return readMessageReply();
    else throw new IOException("Unknown reply type: " + new String(replyType));
  }

  public void close() {
    try {
      this.closed.set(true);
      this.inputStream.close();
      this.outputStream.close();
      this.socket.close();
    } catch (IOException e) {
      logger.warn("Exception while closing", e);
    }
  }

  private PubSubReply readMessageReply() throws IOException {
    StringReplyHeader channelNameHeader = new StringReplyHeader(inputStream.readLine());
    byte[]            channelName       = inputStream.readFully(channelNameHeader.getStringLength());
    inputStream.readLine();

    StringReplyHeader messageHeader = new StringReplyHeader(inputStream.readLine());
    byte[]            message       = inputStream.readFully(messageHeader.getStringLength());
    inputStream.readLine();

    return new PubSubReply(PubSubReply.Type.MESSAGE, new String(channelName), Optional.of(message));
  }

  private PubSubReply readUnsubscribeReply() throws IOException {
    String channelName = readSubscriptionReply();
    return new PubSubReply(PubSubReply.Type.UNSUBSCRIBE, channelName, Optional.empty());
  }

  private PubSubReply readSubscribeReply() throws IOException {
    String channelName = readSubscriptionReply();
    return new PubSubReply(PubSubReply.Type.SUBSCRIBE, channelName, Optional.empty());
  }

  private String readSubscriptionReply() throws IOException {
    StringReplyHeader channelNameHeader = new StringReplyHeader(inputStream.readLine());
    byte[]            channelName       = inputStream.readFully(channelNameHeader.getStringLength());
    inputStream.readLine();

    IntReply subscriptionCount = new IntReply(inputStream.readLine());

    return new String(channelName);
  }

}
