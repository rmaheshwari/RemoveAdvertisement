/**
  * @author rahul and Moitree
 */

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;


public class sample {

    static int width = 352;
    static int height = 288;

    static int H_Video[][] = new int [256][3];

    static ArrayList<Integer> shots = new ArrayList();
    static ArrayList<Integer> shotslength = new ArrayList();
    static ArrayList<Integer> AD = new ArrayList();
    static ArrayList<Double> shotsAmplitude = new ArrayList();
    static ArrayList<Integer> shotsMotion = new ArrayList();

    static ArrayList<Integer> shotsADMotion = new ArrayList();

    static double []FrameAmp;

    static int average_motion;

    static double average_amplitude;

    static long frames;

   //----------------------Main Function-------------------------//
   public static void main(String[] args) throws UnsupportedAudioFileException, IOException
   {
   	//--video1-jobsgates-full.rgb--video2-messi-full.rgb--video3-wreckitralph-full.rgb--testVideo3_ironman.rgb--//
	String inputVideo = "video3-wreckitralph-full.rgb";
        String inputAudio = "video3-wreckitralph-full.wav";

        //---------------Function for detecting shots in Video-----------------//
        Detect_Shots(inputVideo);

        //---------------Function for calculating average amplitude of shots----------------//
        AverageAmpShots(inputAudio);

        //---------------Function for calculating average motion characteristic s of shots----------------//
        MotionVectorShots(inputVideo);

        //---------------Function for detecting, that certain shots belongs to add or not-----------------//
        DETECT_ADD();

        //---------------Write down the output RGB file------------------------//
        WRITEOUTPUT_RGB(inputVideo,"out11.rgb");

        //---------------Write down the output wav file------------------------//
        WRITEOUTPUT_WAV(inputAudio,"out22.wav");

        System.out.println("Content of Total shots in Video is: "+shots);
        System.out.println("Total shots in Video is: "+shots.size());
        System.out.println("Total length of each shots: "+shotslength);
        System.out.println("Average Amplitude of each shots: "+shotsAmplitude);
        System.out.println("Average Amplitude of Video: "+average_amplitude);
        System.out.println("Average Motion of each shots : "+shotsMotion);

        System.out.println("End of main function");
   }

   //-------------Function for Detecting Shots in Input Video using Color Histogram----------------------//
   public static void Detect_Shots(String fileName)
   {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        try {
	    File file = new File(fileName);
	    InputStream is = new FileInputStream(file);

            long length = file.length();

            frames = length/(352*288*3);  //Number of Frames
            System.out.println(frames);

            int H1[][] = new int[256][3];

            for(int i=0; i<frames; i++)
            {
                int H2[][] = new int[256][3];
                long len = (352*288*3);
                byte[] bytes = new byte[(int)len];

                int offset = 0;
                int numRead = 0;
                while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                    offset += numRead;
                }

                //Histogram_RGB(bytes, H2); //--Compute Histogram
                int ind = 0;
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        byte a = 0;
                        byte b = bytes[ind];
                        byte g = bytes[ind+height*width];
                        byte r = bytes[ind+height*width*2];

                        H2[r+128][0]++;
                        H2[g+128][1]++;
                        H2[b+128][2]++;

                        ind++;
                    }
                }

                //--Add it for calculating average histogram of Video
                for(int l=0; l<256; l++)
                {
                    for(int m=0; m<3; m++)
                    {
                        H_Video[l][m] += H2[l][m];
                    }
                }
                if(i>0)
                {
                    int sum=0;
                    for(int j=0;j<256;j++)
                    {
                        sum +=(Math.abs(H2[j][0]-H1[j][0])+Math.abs(H2[j][1]-H1[j][1])+Math.abs(H2[j][2]-H1[j][2]));
                    }

                    if(Math.sqrt(sum)>350)
                    {
                        System.out.println("Frame "+i+"\tAbsolute difference : "+Math.sqrt(sum));
			shots.add(i);
                    }
                }

                H1=H2;
            }

        } catch (FileNotFoundException e) {
        e.printStackTrace();
        } catch (IOException e) {
        e.printStackTrace();
        }
        shots.add((int)frames);
        System.out.println("Content of Total shots in Video is: "+shots);
        System.out.println("Total shots in Video is: "+shots.size());

        //-------------Get length of each shots in Input Video------------------//
        for(int i=0; i<shots.size();i++)
        {
            if (i>0)
            {
                int temp_length = (int)shots.get(i)-(int)shots.get(i-1);
                shotslength.add(temp_length);
            }
            else
                shotslength.add((int)shots.get(0));
        }
        System.out.println("Total length of each shots: "+shotslength);
   }
   //--------------------------------------------------------------------------------------------//

   //-----------Function for calculating average amplitude of shots in Input Video---------------//
   public static void AverageAmpShots(String filename)
   {

	// opens the inputStream
	FileInputStream inputStream;
	try {
	    inputStream = new FileInputStream(filename);
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    return;
	}
	BufferedInputStream bufferedIn = new BufferedInputStream(inputStream);

	// initializes the playSound Object
	PlaySound1 playSound = new PlaySound1(bufferedIn);

	// plays the sound
	try {
	    playSound.play();
	} catch (PlayWaveException1 e) {
	    e.printStackTrace();
	    return;
	}

       PlaySound1 obj = new PlaySound1();
       FrameAmp = obj.Amplitude();

        int shotsIndex=0;
        double temp_amp=0;
        for (int i = 0; i < frames; i++)
        {
             for (int j=0; j<shotslength.get(shotsIndex)-1;j++)
             {
                  temp_amp += FrameAmp[i];
                  average_amplitude += FrameAmp[i];
                  i++;
             }
             if(shotslength.get(shotsIndex)==1)temp_amp=FrameAmp[i];
             temp_amp/=shotslength.get(shotsIndex);
             shotsAmplitude.add(temp_amp);
             temp_amp=0;
             shotsIndex++;
        }
        average_amplitude /= frames;
        //System.out.println("Contents of AD: " + AD);
        //System.out.println("Total shots after amplitude: " + AD.size());
   }
   //--------------------------------------------------------------------------------------------//

   //------------Function for detecting motion vector for each shots in Input Video--------------//
   public static void MotionVectorShots(String fileName)
   {
       try {

            File file = new File(fileName);
            InputStream is = new FileInputStream(file);

            long len = (352 * 288 * 3);

            byte[] bytes1, bytes2;
            bytes1 = new byte[(int) len];

            int shotSAD = 0;
            int shotsIndex = 0;

            for (int i = 0; i < frames; i++)
            {
                int offset = 0;
                int numRead = 0;
                while (offset < bytes1.length && (numRead = is.read(bytes1, offset, bytes1.length - offset)) >= 0) {
                        offset += numRead;
                }
                shotSAD = 0;

                //------------Go for length of shot--------------//
                for (int j=0; j<shotslength.get(shotsIndex)-1;j++)
                {
                    offset = 0;
                    numRead = 0;
                    bytes2 = new byte[(int) len];
                    while (offset < bytes2.length && (numRead = is.read(bytes2, offset, bytes2.length - offset)) >= 0) {
                        offset += numRead;
                    }
                    int frameSAD = 0;
                    int xVector = 0;
                    int yVector = 0;

                    int blocks[][] = new int[8][2];


                    blocks[0][0] = width / 2;
                    blocks[0][1] = height / 2;
                    blocks[1][0] = width - 128;
                    blocks[1][1] = 112;
                    blocks[2][0] = width -144;
                    blocks[2][1] = 112;
                    blocks[3][0] = width -160;
                    blocks[3][1] = 144;
                    blocks[4][0] = width -176;
                    blocks[4][1] = 144;
                    blocks[5][0] = height - 128;
                    blocks[5][1] = 112;
                    blocks[6][0] = height - 144;
                    blocks[6][1] = 112;
                    blocks[7][0] = height - 160;
                    blocks[7][1] = 144;

                    for (int z = 0; z < blocks.length; z++) {
                        int p = blocks[z][0];
                        int q = blocks[z][1];
                        int minBlockSAD = Integer.MAX_VALUE;

                    for (int y = (((q - 32) < 0) ? 0 : (q - 32)); y < (((q + 47) > (height - 15)) ? (height - 15) : (q + 47)); y++) //everywhere in bytes2
                    {
                        for (int x = (((p - 32) < 0) ? 0 : (p - 32)); x < (((p + 47) > (width - 15)) ? (width - 15) : (p + 47)); x++) {
                            int blockSAD = 0;

                            for (int a = 0; a < 16; a++) //for each pixel
                            {
                                for (int b = 0; b < 16; b++) {
                                    blockSAD += Math.abs(bytes1[width * (q + b) + (p + a)] - bytes2[width * (y + b) + (x + a)]);
                                    blockSAD += Math.abs(bytes1[width * (q + b) + (p + a) + height * width] - bytes2[width * (y + b) + (x + a) + height * width]);
                                    blockSAD += Math.abs(bytes1[width * (q + b) + (p + a) + height * width * 2] - bytes2[width * (y + b) + (x + a) + height * width * 2]);
                                }//for a,b
                            }
                            if (blockSAD < minBlockSAD) {
                                minBlockSAD = blockSAD;
                                xVector = Math.abs(p - x);
                                yVector = Math.abs(q - y);
                            }//if
                        }//for x,y
                    }
                    frameSAD += Math.sqrt(xVector * xVector + yVector * yVector);
                    }//for p,q

                    shotSAD += frameSAD;
                    bytes1 = bytes2;
                    i++;
                }
                shotSAD=shotSAD/shotslength.get(shotsIndex);
                System.out.println("Motion Vector for "+shotsIndex+" is "+shotSAD);
                //System.out.println("Frame number is "+i);
                shotsMotion.add(shotSAD);
                average_motion+=(shotSAD);
                shotsIndex++;

            }
            average_motion=average_motion/shots.size();
            System.out.println("Average Motion of video "+average_motion);

        }//try
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

   }
   //----------------------------------------------------------------------------------//

   //-----------Function for detecting that each shot belongs to add or not------------//
   public static void DETECT_ADD()
   {
       int lengthIndex=0;
       int length1=0;
       int length2=0;
       int flag=0;
       for(int i=0;i<shotsAmplitude.size();i++)
       {
           if(shotsAmplitude.get(i)>average_amplitude)length1+=shotslength.get(i);
           else length2+=shotslength.get(i);
       }
       if(length1>length2)flag=1;
       else flag=0;
       for (int i=0;i<shots.size();i++)
       {
           int k;
           int l;

           if(i==0){l=shots.get(i)-1; k=0;}
           //else if(i==(shots.size()-1)){k=shots.get(i); l=(int) (frames-1);}
           else { k=shots.get(i-1);l=shots.get(i)-1;}

           if(shotslength.get(lengthIndex)<300)
           {
               int m = shotsMotion.get(i);
               if(flag==1)
               {
                    if(shotsMotion.get(i)>(average_motion)|| shotsAmplitude.get(i)<average_amplitude)
                    {
                        shotsADMotion.add(k);
                        shotsADMotion.add(l);
                    }
               }
               else
               {
                   if(shotsMotion.get(i)>(average_motion)|| shotsAmplitude.get(i)>average_amplitude)
                    {
                        shotsADMotion.add(k);
                        shotsADMotion.add(l);
                    }
               }
           }
           else
           {
               //It must be a shot of Video(Assumed)
           }
           lengthIndex++;
       }
       while(shotsADMotion.size()<136)
       {
           shotsADMotion.add((int)frames);
       }

       System.out.println("AD after using MotionVector"+shotsADMotion);
   }
   //----------------------------------------------------------------------------------//

   //----------------Function for writing RGB output file------------------------------//
   public static void WRITEOUTPUT_RGB(String sourcefileName, String targetfileName) throws FileNotFoundException, IOException
   {
      
           File file = new File(sourcefileName);
           InputStream is = new FileInputStream(file);
          FileOutputStream OUTRGB = new FileOutputStream(targetfileName);  //------------File to make----------------//

           int write=1;
           for(int i=0; i<frames; i++)
           {
              write=1;
              long len = (352*288*3);
              byte[] bytes = new byte[(int)len];

              int offset = 0;
              int numRead = 0;
              while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                  offset += numRead;
              }

              for(int j=0; j<shotsADMotion.size();j+=2)
               {
                   if(i>=shotsADMotion.get(j) && i<=shotsADMotion.get(j+1))
                   {
                       write=0;
                       break;
                   }
               }
               if(write==1)OUTRGB.write(bytes);
           }

           OUTRGB.close();
         
        
   }

   //----------------Function for writing Output wav file ---------------------//
   public static void WRITEOUTPUT_WAV(String sourcefileName, String targetfileName) throws UnsupportedAudioFileException,IOException
   {
         final boolean	DEBUG = false;

         File	sourceFile = new File(sourcefileName);
         File	targetFile = new File(targetfileName); //------------File to make----------------//

	 AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(sourceFile);
	 AudioFileFormat.Type	targetFileType = fileFormat.getType();
	 AudioFormat audioFormat = fileFormat.getFormat();

         AudioInputStream inputAIS = AudioSystem.getAudioInputStream(sourceFile);

         {
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int nBufferSize = 4000;
            byte[]	abBuffer = new byte[nBufferSize];

            int write=1;
            for(int i=0;i<frames;i++)
            {
               write=1;
               if (DEBUG) { out("trying to read (bytes): " + abBuffer.length); }
               int	nBytesRead = inputAIS.read(abBuffer);
               if (DEBUG) { out("read (bytes): " + nBytesRead); }
               if (nBytesRead == -1)
               {
                       break;
               }

               for(int j=0; j<shotsADMotion.size();j+=2)
               {
                   if(i>=shotsADMotion.get(j) && i<=shotsADMotion.get(j+1))
                   {
                       write=0;
                       break;
                   }
               }
               if(write==1)baos.write(abBuffer, 0, nBytesRead);
            }

           byte[] abAudioData = baos.toByteArray();

           ByteArrayInputStream bais = new ByteArrayInputStream(abAudioData);
           AudioInputStream outputAIS = new AudioInputStream(bais, audioFormat,abAudioData.length / audioFormat.getFrameSize());

           int nWrittenBytes = AudioSystem.write(outputAIS,targetFileType, targetFile);
           if (DEBUG) { out("Written bytes: " + nWrittenBytes); }
        }
   }

   private static void printUsageAndExit()
   {
        out("AudioDataBuffer: usage:");
	out("\tjava AudioDataBuffer <sourcefile> <targetfile>");
	System.exit(0);
    }

    private static void out(String strMessage)
    {
	System.out.println(strMessage);
    }

}

//#############################################################################################//
//---------------------For getting Amplitude of audio--------------------//

class PlaySound1 {

    //-------------8538, 9892, 10398, 2007---------------//
    private InputStream waveStream;
    public static double []FrameAudioAmp =  new double[(int)(sample.frames)];

    private final int EXTERNAL_BUFFER_SIZE = 4000;//524288; // 128Kb

    /**
     * CONSTRUCTOR
     */
    public PlaySound1(InputStream waveStream) {
	this.waveStream = waveStream;
    }
    public double[] Amplitude() {
        return PlaySound1.FrameAudioAmp;
    }
    public PlaySound1() {
    }

    public void play() throws PlayWaveException1 {

	AudioInputStream audioInputStream = null;
	try {
	    audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);
	} catch (UnsupportedAudioFileException e1) {
	    throw new PlayWaveException1(e1);
	} catch (IOException e1) {
	    throw new PlayWaveException1(e1);
	}

	// Obtain the information about the AudioInputStream
	AudioFormat audioFormat = audioInputStream.getFormat();
	DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

	// opens the audio channel
	SourceDataLine dataLine = null;
	try {
	    dataLine = (SourceDataLine) AudioSystem.getLine(info);
	    dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
	} catch (LineUnavailableException e1) {
	    throw new PlayWaveException1(e1);
	}

	// Starts the music :P
	//dataLine.start();
        int readBytes = 0;
        double temp=0;
        double Frame_Amplitude=0;

	byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];

	try {
            for(int j=0; j<(sample.frames); j++)
            {
                readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
		if (readBytes >= 0)
                {
		    dataLine.write(audioBuffer, 0, readBytes);
		}
                 for(int i=1; i<4000; i+=2)
                 {
                     temp = Math.abs(( audioBuffer[i]  << 8 ) | ( audioBuffer[i-1] & 0xff ));
                     Frame_Amplitude += temp;
                }
                Frame_Amplitude = Frame_Amplitude/2000;
                //System.out.println("Frame Number : " +j + " amplitude : "+Frame_Amplitude);
                FrameAudioAmp[j]=Frame_Amplitude;
                temp=0;
            }
	} catch (IOException e1) {
	    throw new PlayWaveException1(e1);
	} finally {
	    // plays what's left and and closes the audioChannel
	    dataLine.drain();
	    dataLine.close();
	}

    }//play closes
}


class PlayWaveException1 extends Exception {

    public PlayWaveException1(String message) {
	super(message);
    }

    public PlayWaveException1(Throwable cause) {
	super(cause);
    }

    public PlayWaveException1(String message, Throwable cause) {
	super(message, cause);
    }

}
