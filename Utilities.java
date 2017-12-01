/*
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class Utilities {

	private static ByteArrayOutputStream streamHandle = new ByteArrayOutputStream();
	private static ReentrantLock lock = new ReentrantLock();
	private static Condition borrowedStream = lock.newCondition();
	private static boolean isStreamInUse = false;
/**
	public static byte[] getBytes(int i)
	{
		byte[] result = new byte[4];
		result[0] = (byte) (0xFF & (i >> 24));
		result[1] = (byte) (i >> 16);
		result[2] = (byte) (i >> 8);
		result[3] = (byte) (i /*>> 0);
	/*	return result;
	}*/
/*
	public static int getIntFromByte(byte[] array, int index){
		ByteBuffer buffer = ByteBuffer.wrap(array, index, 4);
		return buffer.getInt();
	}
*/
//}


