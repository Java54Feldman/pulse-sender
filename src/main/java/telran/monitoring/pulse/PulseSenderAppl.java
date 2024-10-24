package telran.monitoring.pulse;

import java.net.*;
import java.util.*;
import java.util.stream.IntStream;

import telran.monitoring.pulse.dto.SensorData;

public class PulseSenderAppl {
	private static final int N_PACKETS = 50;
	private static final long TIMEOUT = 50;
	private static final int N_PATIENTS = 5;
	private static final int MIN_PULSE_VALUE = 50;
	private static final int MAX_PULSE_VALUE = 200;
    private static final int MAX_ERROR_PULSE_VALUE = MAX_PULSE_VALUE + 10;
    private static final int MIN_ERROR_PULSE_VALUE = MIN_PULSE_VALUE - 10;
    private static final int MAX_ERROR_PULSE_PROB = 5;
    private static final int MIN_ERROR_PULSE_PROB = 3;
	private static final String HOST = "localhost";
    private static final int PORT = 5000;
	private static final int JUMP_PROB = 15;
	private static final int JUMP_POSITIVE_PROB = 70;
	private static final int MIN_JUMP_PERCENT = 10;
	private static final int MAX_JUMP_PERCENT = 100;
	private static final long PATIENT_ID_PRINTED_VALUES = 3;
	private static Random random = new Random();
	static DatagramSocket socket;
	private static HashMap<Long, Integer> patientIdPulseValue = new HashMap<>();

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
		int valueRes = patientIdPulseValue.computeIfAbsent(patientId,
				k -> random.nextInt(MIN_PULSE_VALUE, MAX_PULSE_VALUE + 1));
		
		if (chance(MAX_ERROR_PULSE_PROB)) {
            valueRes = random.nextInt(MAX_ERROR_PULSE_VALUE + 1, MAX_ERROR_PULSE_VALUE + 30);
        } else if (chance(MIN_ERROR_PULSE_PROB)) {
            valueRes = random.nextInt(0, MIN_ERROR_PULSE_VALUE);
        } else if (chance(JUMP_PROB)) {
			valueRes = getValueWithJump(valueRes);
			patientIdPulseValue.put(patientId, valueRes);
		}
		return valueRes;
	}


	private static int getValueWithJump(int previousValue) {
		int jumpPercent = random.nextInt(MIN_JUMP_PERCENT, MAX_JUMP_PERCENT + 1);
		int jumpValue = previousValue * jumpPercent / 100;
		if (!chance(JUMP_POSITIVE_PROB)) {
			jumpValue = -jumpValue;
		}
		int res = previousValue + jumpValue;
		if (res < MIN_PULSE_VALUE) {
			res = MIN_PULSE_VALUE;
		} else if (res > MAX_PULSE_VALUE) {
			res = MAX_PULSE_VALUE;
		}
		return res;
	}

	private static boolean chance(int prob) {

		return random.nextInt(0, 100) < prob;
	}

}
