## My adding interface and implemention
First, I read and learn the code of this excellent project. Then, on the basis of this great work, I added the interface and implementation of byte array data, so that we can implement the byte array erasure algorithm. You can apply it to the erasure processing of network data transmission, the efficiency and ability of the algorithm is great.

首先，我阅读并学习这个优秀项目的代码，然后，我在这个很棒的工作的基础上添加了处理字节数组数据的接口和实现，以便我们可以实现字节数组纠删算法的处理。你可以将其应用到网络数据传输的纠删处理上，算法的效率和能力很棒。

## example
	package com.backblaze.erasure.robinliew.dealbytesinterface;

	/**
	 * 
	 * @author RobinLiew 2017.9.21
	 *
	 */
	public class test {
		public static void main(String[] args) {

		IRSErasureCorrection rsProcessor=new RSErasureCorrectionImpl();

		byte[] data=new byte[1000]; 
		for(int i=0; i<data.length; i++) {  
		    data[i] = 1;  
		}  
		for(int i=0; i<500; i++) {  
		    data[i] = (byte) (16 + i);  
		}  


		int sliceCount=4;//The data is 4 copies(数据为4份)
		int fecSliceCount=2;//2 copies of erasure redundancy(纠删冗余为2份)
		int sliceLength=data.length/sliceCount;
		byte[] en_data;
		en_data=rsProcessor.rs_Encoder(data, sliceLength, sliceCount, fecSliceCount);

	//==================Test use: second pieces of data are lost, and the decoding code has the corresponding test code(测试使用：让第二片数据丢失，解码代码中也有对应的测试代码)=====
		byte[] temp = new byte[250];
		System.arraycopy(temp, 0, en_data, 250, 250);						
	//==========================================================================================================

		boolean[] eraserFlag=new boolean[sliceCount+fecSliceCount];
		for(int i=0;i<eraserFlag.length;i++){
			eraserFlag[i]=true;
		}
		eraserFlag[1]=false;

		int result=rsProcessor.rs_Decoder(en_data, sliceLength, sliceCount, fecSliceCount=2, eraserFlag);
		System.out.println("complete test!");//测试完毕！
		}

	}

