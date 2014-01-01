package org.cowboycoders.ant.interfaces;

/**
 *     Copyright (c) 2012, Will Szumski
 *
 *     This file is part of formicidae.
 *
 *     formicidae is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     formicidae is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with formicidae.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.usb.UsbClaimException;
import javax.usb.UsbConst;
import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbPipe;
import javax.usb.UsbServices;

import org.cowboycoders.ant.messages.StandardMessage;
import org.cowboycoders.ant.messages.commands.ResetMessage;
import org.cowboycoders.ant.utils.ByteUtils;
import org.cowboycoders.ant.utils.UsbUtils;

public class AntTransceiver extends AbstractAntTransceiver {

	public final static Logger LOGGER = Logger.getLogger("Ant Transceiver");

	public static final Level LOG_LEVEL = Level.ALL;

	static {
		// set logging level
		AntTransceiver.LOGGER.setLevel(LOG_LEVEL);
		ConsoleHandler handler = new ConsoleHandler();
		// PUBLISH this level
		handler.setLevel(LOG_LEVEL);
		AntTransceiver.LOGGER.addHandler(handler);
	}

	/**
	 * usb device id
	 */
	private static final short DEVICE_ID = 0x1008;

	/**
	 * usb vendor
	 */
	private static final short VENDOR_ID = 0x0fcf;

	/**
	 * sync byte
	 */
	private static byte MESSAGE_TX_SYNC = (byte) 0xA4;

	/**
	 * Used to set read buffer size
	 */
	private static final int MAX_MSG_LENGTH = 23;

	// private static final int MESSAGE_OFFSET_SYNC = 0;

	private static final int MESSAGE_OFFSET_MSG_LENGTH = 1;

	/**
	 * Usb Interface
	 */
	private UsbInterface _interface;

	/**
	 * opened
	 */
	private boolean running = false;

	/**
	 * class lock
	 */
	private ReentrantLock lock = new ReentrantLock();

	/**
	 * interface claimed lock
	 */
	private ReentrantLock interfaceLock = new ReentrantLock();

	private boolean readEndpoint = true;

	private UsbEndpoint endpointIn;

	private UsbEndpoint endpointOut;

	private UsbDevice device;

	UsbPipe inPipe = null;

	private UsbReader usbReader;

	// private int deviceNumber;

	public AntTransceiver(int deviceNumber) {
		// this.deviceNumber = deviceNumber;
		doInit(deviceNumber);

	}

	/**
	 * Testing only
	 */
	AntTransceiver() {

	}

	private void doInit(int deviceNumber) {
		UsbServices usbServices = null;
		UsbHub rootHub;

		try {
			usbServices = UsbHostManager.getUsbServices();
			rootHub = usbServices.getRootUsbHub();
		} catch (SecurityException e) {
			throw new AntCommunicationException(e);
		} catch (UsbException e) {
			throw new AntCommunicationException(e);
		}

		List<UsbDevice> devices = UsbUtils.getUsbDevicesWithId(rootHub,
				VENDOR_ID, DEVICE_ID);

		// devices = UsbUtils.getAllUsbDevices(rootHub);

		// causes javax.usb.UsbException: Strings not supported by device

		// for (UsbDevice d : devices) {
		// try {
		// LOGGER.finer("Found device: " + d.getProductString());
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// } catch (UsbDisconnectedException e) {
		// e.printStackTrace();
		// } catch (UsbException e) {
		//
		// //e.printStackTrace();
		// }
		// }

		LOGGER.finer("Number of devices: " + devices.size());

		if (devices.size() < deviceNumber + 1) {
			throw new AntCommunicationException("Device not found");
		}

		UsbDevice device = devices.get(deviceNumber);

		this.device = device;
	}

	private void logData(Level level, byte[] data, String tag) {
		StringBuffer logBuffer = new StringBuffer();

		for (Byte b : data) {
			logBuffer.append(String.format("%x ", b));
		}

		logBuffer.append((String.format("\n")));

		LOGGER.log(level, tag + " : " + logBuffer);
	}

	public class UsbReader extends Thread {
		private byte[] last;

		private static final int BUFFER_SIZE = 64;

		/**
		 * @param data
		 *            buffer to check
		 * @return data remaining
		 */
		private byte[] lookForSync(byte[] data, int len) {
			if (data == null || data.length < 1) {
				return new byte[0];
			}

			if (data[0] != MESSAGE_TX_SYNC) {
				int index = -1;
				for (int i = 0; i < len; i++) {
					if (data[i] == MESSAGE_TX_SYNC) {
						index = i;
						break;
					}
				}
				// not found
				if (index < 0) {
					LOGGER.warning("data read from usb endpoint does not contain a sync byte : ignoring");
					return new byte[0]; // zero length array
				}
				LOGGER.info("found non-zero sync byte index");
				data = Arrays.copyOfRange(data, index, data.length);
			}
			return data;
		}

		// All this array copying inefficient but simple (could keep reference
		// to current index instead)
		private byte[] skipCurrentSync(byte[] data) {
			if (data.length < 1) {
				return new byte[0];
			}
			data = Arrays.copyOfRange(data, 1, data.length);
			return data;
		}

		/**
		 * Gets the next message and notifies interested listeners.
		 * 
		 * @param data
		 *            - message data
		 * @param len
		 *            - message length
		 */
		void processBuffer(byte[] data, int len) {
			while (len > 0) {
				data = lookForSync(data, len);

				if (data.length <= MESSAGE_OFFSET_MSG_LENGTH) {
					LOGGER.info("data length too small, checking next packet");
					// assume rest will arrive in next packet
					last = data;
					break;
				}

				int msgLength = data[MESSAGE_OFFSET_MSG_LENGTH];

				// negative length does not make sense
				if (msgLength < 0) {
					LOGGER.warning("msgLength appears to be incorrect (ignorning). Length : "
							+ msgLength);
					data = skipCurrentSync(data);
					continue;
				}

				int checkSumIndex = msgLength + 3;

				if (checkSumIndex >= data.length) {
					// unreasonably large checkSumIndex (dont span multiple
					// buffers)
					if (checkSumIndex >= BUFFER_SIZE - 1) {
						LOGGER.warning("msgLength appears to be incorrect (ignorning). Length : "
								+ msgLength);
						data = skipCurrentSync(data);
						continue;
					}

					// we try assume continued in next buffer
					last = data;
					break;
				}

				// data minus sync and checksum
				byte[] cleanData = new byte[msgLength + 2];

				for (int i = 0; i < msgLength + 2; i++) {
					cleanData[i] = data[i + 1];
				}

				if (getChecksum(cleanData) != data[checkSumIndex]) {
					LOGGER.warning("checksum incorrect : ignoring");
					data = skipCurrentSync(data);
					continue;
				}

				AntTransceiver.this.broadcastRxMessage(cleanData);
				// cleandata length + sync + checksum
				len -= (cleanData.length + 2);
				data = Arrays.copyOfRange(data, cleanData.length + 2,
						data.length);
			}
		}

		
		/*
		 * Two Modifications (David George - 11/June/2013)
		 * 
		 * 1. continue if we get a USB Exception on read from lower layers, this
		 * is a timeout and we don't care
		 * 
		 * 2. use returned data length to make code more efficient (hopefully).
		 * No more searching for SYNC bytes in zero data
		 */
		@Override
		public void run() {

			try {
				while (readEndpoint) {

					try {
						// interfaceLock.lock();
						// byte [] data = new byte[MAX_MSG_LENGTH];
						byte[] data = new byte[BUFFER_SIZE];
						int len;
						try {
							// inPipe.open();
							LOGGER.finest("pre read");
							len = inPipe.syncSubmit(data);
							// System.out.println("received " + len);
						} catch (UsbException e) {
							// carry on regardless
							LOGGER.warning(e.getMessage());
							continue;
						} finally {
							// inPipe.close();
						}

						logData(Level.FINER, data, "read");

						// process remaining bytes from last buffer
						if (last != null) {
							// TODO len is bigger due to remaining bytes
							len += last.length;
							data = ByteUtils.joinArray(last, data);
							last = null;
						}

						processBuffer(data, len);

					} finally {
						// interfaceLock.unlock();
					}
				}

			} catch (UsbNotActiveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UsbNotOpenException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UsbDisconnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			LOGGER.finest(this.getClass().toString() + " killed");
		}
	}

	/**
	 * 
	 * @param _interface
	 *            interface to claim / release
	 * @param claim
	 *            true to claim, false to release
	 */
	private void claimInterface(UsbInterface _interface, boolean claim) {

		try {
			interfaceLock.lock();
			if (claim) {
				_interface.claim();
			} else {
				if (_interface.isClaimed()) {
					_interface.release();
				}
			}
		} catch (UsbClaimException e) {
			e.printStackTrace();
			throw new AntCommunicationException(e);
		} catch (UsbNotActiveException e) {
			throw new AntCommunicationException(e);
		} catch (UsbDisconnectedException e) {
			throw new AntCommunicationException(e);
		} catch (UsbException e) {
			throw new AntCommunicationException(e);
		} finally {
			interfaceLock.unlock();
		}

	}

	// FIXME : TAKES an age to start with reference javax.usb implementation
	@Override
	public boolean start() {
		try {
			lock.lock();
			// already started
			if (running)
				return true;

			if (!device.isConfigured()) {
				throw new AntCommunicationException(
						"Ant stick not configured by OS");
			}

			UsbInterface _interface = device.getActiveUsbConfiguration()
					.getUsbInterface((byte) 0);

			this._interface = _interface;

			claimInterface(_interface, true);

			@SuppressWarnings("unchecked")
			List<UsbEndpoint> endpoints = _interface.getUsbEndpoints();

			if (endpoints.size() != 2) {
				throw new AntCommunicationException(
						"Unexpected number of endpoints");
			}

			for (UsbEndpoint endpoint : endpoints) {
				if (endpoint.getDirection() == UsbConst.ENDPOINT_DIRECTION_IN)
					this.endpointIn = endpoint;
				else
					this.endpointOut = endpoint;
			}

			if (this.endpointOut == null || this.endpointIn == null) {
				throw new AntCommunicationException("Endpoints not found");
			}

			// FIXME: without these two // don't seem to receive replies to
			// first few messages
			// StandardMessage msg = new ResetMessage();
			// send(msg.encode());
			// FIXME: if we don't write some garbage it doesn't response to
			// first few
			// messages

			try {
				write(new byte[128]);
			} catch (UsbException e) {
				LOGGER.finest("device wake up failed");
			}

			inPipe = endpointIn.getUsbPipe();

			try {
				inPipe.open();
			} catch (UsbException e) {
				throw new AntCommunicationException("Error opening inPipe");
			}

			readEndpoint = true;

			this.usbReader = new UsbReader();

			// SharedThreadPool.getThreadPool().execute(this.usbReader);

			this.usbReader.start();

			running = true;

		} catch (RuntimeException e) {
			e.printStackTrace();
			claimInterface(_interface, false);
			throw e;
		} finally {
			lock.unlock();
		}

		// TODO Auto-generated method stub
		return true;
	}

	// private static class TransferKiller extends Thread {
	//
	// private boolean running = false;
	//
	// private boolean stop = false;
	//
	// /**
	// * should use lock
	// * @return the running
	// */
	// public boolean isRunning() {
	// return running;
	// }
	//
	// /**
	// * stops the killer
	// */
	// public void kill() {
	// this.stop = true;
	// }
	//
	// /**
	// * @return the lock
	// */
	// public Lock getLock() {
	// return lock;
	// }
	//
	// /**
	// * @return the statusChanged
	// */
	// public Condition getStatusChanged() {
	// return statusChanged;
	// }
	//
	// private Lock lock = new ReentrantLock();
	//
	// private Condition statusChanged = lock.newCondition();
	//
	// private UsbPipe pipe = null;
	//
	// public TransferKiller(UsbPipe pipe) {
	// this.pipe = pipe;
	// }
	//
	// @Override
	// public void run() {
	//
	// try{
	//
	// lock.lock();
	// running = true;
	// statusChanged.signalAll();
	// AntTransceiver.LOGGER.finest("TransferKiller: started");
	// } finally {
	// lock.unlock();
	// }
	//
	// try {
	// while(!stop) {
	// try {
	// lock.lock();
	// AntTransceiver.LOGGER.finest("TransferKiller: abortAllSubmissions");
	// pipe.abortAllSubmissions();
	// } finally {
	// lock.unlock();
	// }
	// Thread.sleep(10);
	// }
	// } catch (InterruptedException e) {
	// try {
	// lock.lock();
	// stop = true;
	// } finally {
	// lock.unlock();
	// }
	// }
	//
	// try {
	// lock.lock();
	// running = false;
	// AntTransceiver.LOGGER.finest("TransferKiller: killed");
	// statusChanged.signalAll();
	//
	// } finally {
	// lock.unlock();
	// }
	//
	//
	//
	// }
	//
	//
	// }

	// @Override
	// public void stop() {
	// try {
	// lock.lock();

	private void killUsbReader() {

		readEndpoint = false;

		// Doesn't seem to work so we send a message instead
		// inPipe.abortAllSubmissions();

		StandardMessage msg = new ResetMessage();

		send(msg.encode());

		try {
			usbReader.join();
		} catch (InterruptedException e) {
			LOGGER.severe("interrupted waiting to shutdown device");
		}
	}

	@Override
	public void stop() {
		try {
			lock.lock();

			if (!running)
				return;

			// TransferKiller inPipeKiller = new TransferKiller(inPipe);
			// inPipeKiller.start();

			killUsbReader();

			// try {
			// interfaceLock.lock();
			// try {
			// inPipeKiller.getLock().lock();
			// inPipeKiller.kill();
			// while(inPipeKiller.isRunning()) {
			// inPipeKiller.getStatusChanged().await();
			// }
			//
			// } catch (InterruptedException e) {
			// LOGGER.severe("interrupted waiting for killer to stop");
			// } finally {
			// inPipeKiller.getLock().unlock();
			//
			// }

			try {
				// inPipe.abortAllSubmissions();
				inPipe.close();
			} catch (UsbException e) {
				throw new AntCommunicationException("Error closing inPipe", e);
			}

			_interface.release();

			// } finally {
			// interfaceLock.unlock();
			// }

			// _interface.release();

			running = false;
		} catch (UsbClaimException e) {
			throw new AntCommunicationException(e);
		} catch (UsbNotActiveException e) {
			throw new AntCommunicationException(e);
		} catch (UsbDisconnectedException e) {
			throw new AntCommunicationException(e);
		} catch (UsbException e) {
			throw new AntCommunicationException(e);
		} finally {
			lock.unlock();
		}

	}

	private void write(byte[] data) throws UsbNotActiveException,
			UsbNotOpenException, IllegalArgumentException,
			UsbDisconnectedException, UsbException {
		UsbPipe pipe = null;

		try {
			lock.lock();
			pipe = endpointOut.getUsbPipe();
			if (!pipe.isOpen())
				pipe.open();
			LOGGER.finest("pre submit");
			pipe.syncSubmit(data);
			logData(Level.FINER, data, "wrote");
		} finally {
			if (pipe != null) {
				pipe.close();
			}
			lock.unlock();
		}

	}

	@Override
	public void send(byte[] message) throws AntCommunicationException {
		try {
			if (!running)
				throw new AntCommunicationException(
						"AntTransceiver not running. Use start()");
			write(addExtras(message));

		} catch (UsbNotActiveException e) {
			throw new AntCommunicationException(e);
		} catch (UsbNotOpenException e) {
			throw new AntCommunicationException(e);
		} catch (IllegalArgumentException e) {
			throw new AntCommunicationException(e);
		} catch (UsbDisconnectedException e) {
			throw new AntCommunicationException(e);
		} catch (UsbException e) {
			throw new AntCommunicationException(e);
		} finally {
		}
	}

	public byte getChecksum(byte[] nocheck) {
		byte checksum = 0;
		checksum = MESSAGE_TX_SYNC;
		for (byte b : nocheck) {
			checksum ^= b % 0xff;
		}
		return checksum;
	}

	public byte[] addExtras(byte[] nocheck) {
		byte[] data = new byte[nocheck.length + 2];
		data[0] = MESSAGE_TX_SYNC;
		for (int i = 1; i < data.length - 1; i++) {
			data[i] = nocheck[i - 1];
		}
		data[data.length - 1] = getChecksum(nocheck);
		return data;
	}

	@Override
	public boolean isRunning() {
		try {
			lock.lock();
			return running;
		} finally {
			lock.unlock();
		}

	}

}
