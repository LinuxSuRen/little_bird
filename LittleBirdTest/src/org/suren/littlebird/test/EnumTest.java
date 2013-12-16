package org.suren.littlebird.test;

public class EnumTest {
	
	enum Test
	{
//		RED(1), BLUE(10);
//		
//		Test(int code)
//		{
//		}
//
//		@Override
//		public String toString() {
//			return super.toString();
//		}
		RED, BLUE
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(Test.BLUE);
	}

}
