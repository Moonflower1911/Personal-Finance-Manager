package com.example.personal_finance_manager.Util;

import android.content.Context;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TFLiteModelHelper {
    private final Interpreter interpreter;

    public TFLiteModelHelper(Context context) {
        try {
            interpreter = new Interpreter(loadModelFile(context));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load TFLite model", e);
        }
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(context.getAssets().openFd("lite_model.tflite").getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = context.getAssets().openFd("lite_model.tflite").getStartOffset();
        long declaredLength = context.getAssets().openFd("lite_model.tflite").getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public float predict(float[] input) {
        float[][] output = new float[1][1];
        interpreter.run(input, output);
        return output[0][0];
    }
}
