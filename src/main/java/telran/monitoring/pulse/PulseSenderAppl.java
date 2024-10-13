package telran.monitoring.pulse;
import java.net.*;
import java.util.Random;
import java.util.stream.IntStream;

import telran.monitoring.pulse.dto.SensorData;

public class PulseSenderAppl {
	private static final int N_PACKETS = 20;
	private static final long TIMEOUT = 500;
	private static final int N_PATIENTS = 5;
	private static final int MIN_PULSE_VALUE = 50;
	private static final int MAX_PULSE_VALUE = 200;
	private static final String HOST = "localhost";
	private static final int PORT = 5000;
	private static Random random = new Random();
	static DatagramSocket socket;
	public static void main(String[] args) throws Exception {
		socket = new DatagramSocket();
		IntStream.rangeClosed(1, N_PACKETS)
			.forEach(PulseSenderAppl::sendPulse);
	}
	private static void sendPulse(int seqNumber) {
		SensorData data = getRandomSensorData(seqNumber);
		String jsonData = data.toString();
		sendDatagramPacket(jsonData);
		try {
			Thread.sleep(TIMEOUT);
		} catch (InterruptedException e) {
			
		}
	}
	private static void sendDatagramPacket(String jsonData) {
		byte[] buffer = jsonData.getBytes();
		try {
			DatagramPacket packet = new DatagramPacket(
					buffer, buffer.length, InetAddress.getByName(HOST), PORT);
			socket.send(packet);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	private static SensorData getRandomSensorData(int seqNumber) {
		long patientId = random.nextInt(1, N_PATIENTS + 1);
		int value = getRandomPulseValue(patientId);
		return new SensorData(seqNumber, patientId, value, System.currentTimeMillis());
	}
	private static int getRandomPulseValue(long patientId) {
		// FIXME for HW #61
		return random.nextInt(MIN_PULSE_VALUE, MAX_PULSE_VALUE + 1);
	}

}
