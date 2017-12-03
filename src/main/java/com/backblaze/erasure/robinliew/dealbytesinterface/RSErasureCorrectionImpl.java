package com.backblaze.erasure.robinliew.dealbytesinterface;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.backblaze.erasure.ReedSolomon;

/**
 * Implementation of RS algorithm codec interface
 * Encoding: incoming byte[] data data containing N slice data, encoding the array of N+M slice data after encoding, and M as the number of erasure check pieces
 * Decode: the data byte[] rs_data after the afferent code, and the information of the data sheet, the erasure check, the recorded lost data sheet
 * RS算法编解码接口实现
 * 编码：传入包含N片数据的byte[] data数据，编码后生成N+M片数据数组，M为纠删校验片的数量
 * 解码：传入编码后的数据byte[] rs_data,以及数据片、纠删校验片、记录的丢失数据片的信息
 * @author RobinLiew  2017.9.21
 *
 */
public class RSErasureCorrectionImpl implements IRSErasureCorrection{

    public  int DATA_SHARDS = 4;//Default number of data slices(默认的数据片数量)
    public  int PARITY_SHARDS = 2;//Default number of checkout data(默认的校验片数据数量)
    public  int TOTAL_SHARDS = 6;//The total number of the default slices(默认的切片的总数量)

    public  int BYTES_IN_INT = 4;
   
	@Override
	public byte[] rs_Encoder(byte[] srcBuffer, int sliceLength, int sliceCount,
			int fecSliceCount) {
		
			byte[] rsData=null;
			
			try{  
		         //The length of the data of the payload (equivalent to the length of the file)净荷的数据长度（相当于文件的长度）
		         final int dataSize = (int) srcBuffer.length;
		         DATA_SHARDS=sliceCount;
		         PARITY_SHARDS=fecSliceCount;
		         TOTAL_SHARDS=DATA_SHARDS+PARITY_SHARDS;
		         
		         
		         // Figure out how big each shard will be.  The total size stored
		         final int storedSize = dataSize; //The total size of the incoming data(传入数据的总大小)
		         final int shardSize = (storedSize) / DATA_SHARDS;//The size of each piece of data(每片数据的大小)
		
		         // Create a buffer holding the srcBuffer size, followed by
		         final int bufferSize = shardSize * DATA_SHARDS;
		         final byte [] allBytes = new byte[bufferSize];
		         ByteBuffer.wrap(allBytes).putInt(dataSize);
		         InputStream in = new ByteArrayInputStream(srcBuffer);
		         int bytesRead = in.read(allBytes, 0, dataSize);
		         if (bytesRead != dataSize) {
		             throw new IOException("not enough bytes read");
		         }
		         in.close();
		
		         // Make the buffers to hold the shards.
		         byte [] [] shards = new byte [TOTAL_SHARDS] [shardSize];
		
		         // Fill in the data shards
		         for (int i = 0; i < DATA_SHARDS; i++) {
		             System.arraycopy(allBytes, i * shardSize, shards[i], 0, shardSize);
		         }
		
		         // Use Reed-Solomon to calculate the parity.
		         ReedSolomon reedSolomon = ReedSolomon.create(DATA_SHARDS, PARITY_SHARDS);
		         reedSolomon.encodeParity(shards, 0, shardSize);
		         
		         List<Byte> list=new ArrayList<>();
		         
		         rsData=new byte[TOTAL_SHARDS*shardSize];
		         int index=0;
		         for(int i = 0; i < TOTAL_SHARDS; i++){
		        	 for(int j=0;j<shards[i].length;j++){
		            	 rsData[index]=shards[i][j];
		            	 index++;
		             }
		         }
		         	       
		  	 }catch(Exception e){
		  		 e.printStackTrace();
		  	 }

		return rsData;
		
	}
	
	@Override
	public int rs_Decoder(byte[] srcEraseBuff, int sliceLen, int sliceCount,
			int rsSliceCount, boolean[] eraserFlag) {//eraserFlag used to record information of lost pieces(用来记录丢失片的信息)
		
		try{
    		
			 DATA_SHARDS=sliceCount;
	         PARITY_SHARDS=rsSliceCount;
	         TOTAL_SHARDS=DATA_SHARDS+PARITY_SHARDS;
	        
	         
	        
	        final byte [] [] shards = new byte [TOTAL_SHARDS] [];
	        boolean [] shardPresent = new boolean [TOTAL_SHARDS];//Information for recording the existence and loss of subsections(用来记录子片存在与丢失的信息)
	        
	        shardPresent=eraserFlag;
	        
	        int shardSize =sliceLen;
	        int shardCount = 0;//The number of subsections that exist(记录存在的子片的数量)
	        int offset=0;
	        
	        for(int i = 0; i < TOTAL_SHARDS; i++){
	        	
	        	shards[i] = new byte [shardSize];
	        	System.arraycopy(srcEraseBuff, offset, shards[i], 0, sliceLen);
	        	if(shardPresent[i]==false){
	        		shardCount--;
	        	}
	        	shardCount += 1;
	        	offset=offset+sliceLen;	        	
	        }
	        
	        // We need at least DATA_SHARDS to be able to reconstruct the file.
	        if (shardCount < DATA_SHARDS) {
	            System.out.println("The number of lost data is too much, beyond the erasure ability of the RS erasure algorithm!");//丢失的数据数量过多，超出RS纠删算法的纠删能力！
	            return 1;
	        }
	
	        // Make empty buffers for the missing shards. 
	        for (int i = 0; i < TOTAL_SHARDS; i++) {
	            if (!shardPresent[i]) {//A piece of data is lost and an empty piece of data is set up to take up the position(某一片数据丢了，建立空的数据片来占位)
	                shards[i] = new byte [shardSize];
	            }
	        }
	
	        // Use Reed-Solomon to fill in the missing shards
	        ReedSolomon reedSolomon = ReedSolomon.create(DATA_SHARDS, PARITY_SHARDS);
	        reedSolomon.decodeMissing(shards, shardPresent, 0, shardSize);
	
	        // Combine the data shards into one buffer for convenience.
	        // (This is not efficient, but it is convenient.)
	        byte [] allBytes = new byte [shardSize * DATA_SHARDS];
	        for (int i = 0; i < DATA_SHARDS; i++) {
	            System.arraycopy(shards[i], 0, allBytes, shardSize * i, shardSize);
	        }
	        System.arraycopy(allBytes,  0,  srcEraseBuff,  0,  allBytes.length);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
		
		return 0;//Return 0 to represent erasure success(返回0表示纠删成功)
	}
	 
}
