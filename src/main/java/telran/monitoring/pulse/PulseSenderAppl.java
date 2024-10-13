package telran.monitoring.pulse;

import java.net.*;
import java.util.*;
import java.util.stream.IntStream;

import telran.monitoring.pulse.dto.SensorData;

public class PulseSenderAppl {
	private static final int N_PACKETS = 100;
	private static final long TIMEOUT = 50;
	private static final int N_PATIENTS = 5;
	private static final int MIN_PULSE_VALUE = 50;
	private static final int MAX_PULSE_VALUE = 200;
	private static final String HOST = "localhost";
	private static final int PORT = 5000;
	private static final int JUMP_PROBABILITY = 15;
	private static final int JUMP_POSITIVE_PROBABILITY = 70;
	private static final int MIN_JUMP_PERCENT = 10;
	private static final int MAX_JUMP_PERCENT = 100;
	private static final long PATIENT_ID_PRINTED_VALUES = 3;
	private static Random random = new Random();
	static DatagramSocket socket;
	private static HashMap<Long, Integer> previousPulseMap = new HashMap<>();

	public static void main(String[] args) throws Exception {
		socket = new DatagramSocket();
		IntStream.rangeClosed(1, N_PACKETS).forEach(PulseSenderAppl::sendPulse);
	}

	private static void sendPulse(int seqNumber) {
		SensorData data = getRandomSensorData(seqNumber);
		String jsonData = data.toString();
		sendDatagramPacket(jsonData);
		if (data.patientId() == PATIENT_ID_PRINTED_VALUES) {
			System.out.println(jsonData);
		}
		try {
			Thread.sleep(TIMEOUT);
		} catch (InterruptedException e) {

		}
	}

	private static void sendDatagramPacket(String jsonData) {
		byte[] buffer = jsonData.getBytes();
		try {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(HOST), PORT);
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
		Integer value = previousPulseMap.get(patientId);
		if (value == null) {
			value = random.nextInt(MIN_PULSE_VALUE, MAX_PULSE_VALUE + 1);
			previousPulseMap.put(patientId, value);
		} else if(isJump()) {
			value = computePulse(value);
			previousPulseMap.put(patientId, value);
		} 
		return value;
	}

	private static int computePulse(int value) {
		int jumpSign = getJumpSign();
		int jumpPersent = random.nextInt(MIN_JUMP_PERCENT, MAX_JUMP_PERCENT + 1);
		value = value + jumpSign * value * jumpPersent / 100;
		value = fixValueInRange(value);
		return value;
	}

	private static int fixValueInRange(int value) {
		if (value > MAX_PULSE_VALUE) {
			value = MAX_PULSE_VALUE;
		}
		if (value < MIN_PULSE_VALUE) {
			value = MIN_PULSE_VALUE;
		}
		return value;
	}

	private static int getJumpSign() {
		return random.nextInt(100) < JUMP_POSITIVE_PROBABILITY ? 1 : -1;
	}

	private static boolean isJump() {
		return random.nextInt(100) < JUMP_PROBABILITY;
	}

}
