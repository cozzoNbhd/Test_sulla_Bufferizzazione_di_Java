import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

public class Bufferizzazione {

    private static void readBuffer(ReadableByteChannel src, WritableByteChannel dest, ByteBuffer buffer) throws IOException {
        while (src.read(buffer) != -1) {
            buffer.flip();
            dest.write (buffer);
            buffer.compact();
        }
        buffer.flip();
        while (buffer.hasRemaining())
            dest.write (buffer);
        src.close();
        dest.close();
    }

    public static void CopyDirectBuffered(String src_file, String dest_file) throws IOException {
        ReadableByteChannel src_channel = Channels.newChannel(new FileInputStream(src_file));
        WritableByteChannel dest_channel = Channels.newChannel(new FileOutputStream(dest_file));
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 16);
        readBuffer(src_channel, dest_channel, buffer);
    }

    public static void CopyNotDirectBuffered(String src_file, String dest_file) throws IOException {
        ReadableByteChannel src_channel = Channels.newChannel(new FileInputStream(src_file));
        WritableByteChannel dest_channel = Channels.newChannel(new FileOutputStream(dest_file));
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 16);
        readBuffer(src_channel, dest_channel, buffer);
    }

    public static void CopyBufferedStream(String src_file, String dest_file) {
        byte[] buffer = new byte[1024];
        try {
            BufferedInputStream src = new BufferedInputStream(new FileInputStream(src_file));
            BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(dest_file));
            int nBytes;
            while ((nBytes = src.read(buffer)) != -1)
                dest.write(buffer, 0, nBytes);
            src.close();
            dest.close();
        } catch (IOException e) {}
    }

    public static void CopyReadByteStream(String src_file, String dest_file) {
        byte[] buffer = new byte[1024];
        try {
            FileInputStream src = new FileInputStream(src_file);
            FileOutputStream dest = new FileOutputStream(dest_file);
            int nBytes;
            while ((nBytes = src.read(buffer)) != -1)
                dest.write(buffer, 0, nBytes);
            src.close();
            dest.close();
        } catch (IOException e) {}
    }

    public static void CopyTransferTo(String src_file, String dest_file) throws IOException {
        RandomAccessFile from_file = new RandomAccessFile(src_file, "rw");
        FileChannel from_channel = from_file.getChannel();
        RandomAccessFile to_file = new RandomAccessFile(dest_file, "rw");
        FileChannel to_channel = to_file.getChannel();
        long pos = 0; long size_channel = from_channel.size();
        from_channel.transferTo(pos, size_channel, to_channel);
        from_channel.close();
        to_channel.close();
    }

    public static void main(String [] args) throws IOException {

        if (args.length != 2) {
            System.out.println("Errore! Inserire in args[0] il path assoluto del file sorgente e in args[1] di quello destinazione!");
            System.exit(1);
        }

        File f1 = new File(args[0]);
        File f2 = new File(args[1]);

        if (!f1.isFile()) {
            System.out.println("Il percorso sorgente NON corrisponde ad alcun file!");
            System.exit(1);
        }

        if (!f2.isFile()) {
            System.out.println("Il percorso destinazione NON corrisponde ad alcun file!");
            System.exit(1);
        }


        String src_file = f1.getAbsolutePath();
        String dest_file = f2.getAbsolutePath();

        long[] tempi = new long[5];

        long tempoInizio1 = System.currentTimeMillis();
        CopyDirectBuffered(src_file, dest_file);
        long tempoFine1 = System.currentTimeMillis();
        long tempoImpiegato1 = tempoFine1 - tempoInizio1;
        tempi[0] = tempoImpiegato1;

        long tempoInizio2 = System.currentTimeMillis();
        CopyNotDirectBuffered(src_file, dest_file);
        long tempoFine2 = System.currentTimeMillis();
        long tempoImpiegato2 = tempoFine2 - tempoInizio2;
        tempi[1] = tempoImpiegato2;

        long tempoInizio3 = System.currentTimeMillis();
        CopyTransferTo(src_file, dest_file);
        long tempoFine3 = System.currentTimeMillis();
        long tempoImpiegato3 = tempoFine3 - tempoInizio3;
        tempi[2] = tempoImpiegato3;

        long tempoInizio4 = System.currentTimeMillis();
        CopyBufferedStream(src_file, dest_file);
        long tempoFine4 = System.currentTimeMillis();
        long tempoImpiegato4 = tempoFine4 - tempoInizio4;
        tempi[3] = tempoImpiegato4;

        long tempoInizio5 = System.currentTimeMillis();
        CopyReadByteStream(src_file, dest_file);
        long tempoFine5 = System.currentTimeMillis();
        long tempoImpiegato5 = tempoFine5 - tempoInizio5;
        tempi[4] = tempoImpiegato5;

        Arrays.sort(tempi);

        if (tempoImpiegato1 == tempi[0]) System.out.println("Ha vinto la strategia CopyDirectBuffered");

        if (tempoImpiegato2 == tempi[0]) System.out.println("Ha vinto la strategia CopyNotDirectBuffered");

        if (tempoImpiegato3 == tempi[0]) System.out.println("Ha vinto la strategia CopyTransferTo");

        if (tempoImpiegato4 == tempi[0]) System.out.println("Ha vinto la strategia CopyBufferedStream");

        if (tempoImpiegato5 == tempi[0]) System.out.println("Ha vinto la strategia CopyReadByteStream");


    }
}