
package com.roadsafety.service;

import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class RoadDamageDetectionService {

    private OrtEnvironment environment;
    private OrtSession session;

    private static final int INPUT_SIZE = 640;
    private static final float CONFIDENCE_THRESHOLD = 0.25f;

    private static final String[] CLASS_NAMES = {
        "Alligator Crack",
        "Block Crack",
        "Crack",
        "Edge Crack",
        "Longitudinal Crack",
        "Pothole",
        "Transverse Crack"
    };

    private final Path modelPath = Paths.get(
        "src", "main", "resources", "models",
        "dfa3bdd81716fc3918be93865054140a880dd6ca7cf0cafbe20e25d392f8eeb8.onnx"
    );

    @PostConstruct
    public void loadModel() {

        try {
            environment = OrtEnvironment.getEnvironment();

            OrtSession.SessionOptions options =
                new OrtSession.SessionOptions();

            try {
                session = environment.createSession(
                    modelPath.toAbsolutePath().toString(),
                    options
                );
            } finally {
                options.close();
            }

            System.out.println(
                "Road Damage ONNX Model Loaded Successfully!"
            );

            System.out.println(
                "Input names: " + session.getInputNames()
            );

            System.out.println(
                "Output names: " + session.getOutputNames()
            );

        } catch (Exception e) {

            session = null;

            System.err.println(
                "AI Model loading failed: " + e.getMessage()
            );
        }
    }

    public boolean isModelLoaded() {
        return session != null;
    }

    public List<Map<String, Object>> detect(Mat originalImage)
            throws OrtException {

        List<Map<String, Object>> detections =
            new ArrayList<>();

        if (session == null) {
            throw new IllegalStateException(
                "AI model is not loaded."
            );
        }

        if (originalImage == null || originalImage.empty()) {
            throw new IllegalArgumentException(
                "Invalid image."
            );
        }

        int originalWidth = originalImage.cols();
        int originalHeight = originalImage.rows();

        double scale = Math.min(
            (double) INPUT_SIZE / originalWidth,
            (double) INPUT_SIZE / originalHeight
        );

        int resizedWidth =
            (int) Math.round(originalWidth * scale);

        int resizedHeight =
            (int) Math.round(originalHeight * scale);

        int padX = (INPUT_SIZE - resizedWidth) / 2;
        int padY = (INPUT_SIZE - resizedHeight) / 2;

        Mat resized = new Mat();
        Mat padded = new Mat();
        Mat rgb = new Mat();
        Mat blob = new Mat();

        try {

            Imgproc.resize(
                originalImage,
                resized,
                new Size(resizedWidth, resizedHeight)
            );

            Core.copyMakeBorder(
                resized,
                padded,
                padY,
                INPUT_SIZE - resizedHeight - padY,
                padX,
                INPUT_SIZE - resizedWidth - padX,
                Core.BORDER_CONSTANT,
                new Scalar(114, 114, 114)
            );

            Imgproc.cvtColor(
                padded,
                rgb,
                Imgproc.COLOR_BGR2RGB
            );

            blob = org.opencv.dnn.Dnn.blobFromImage(
                rgb,
                1.0 / 255.0,
                new Size(INPUT_SIZE, INPUT_SIZE),
                new Scalar(0, 0, 0),
                false,
                false,
                CvType.CV_32F
            );

            float[] inputData = new float[
                3 * INPUT_SIZE * INPUT_SIZE
            ];

            int copiedValues = blob.get(
            	    new int[]{0, 0, 0, 0},
            	    inputData
            	);

            	System.out.println("Blob values copied: " + copiedValues);
            	System.out.println("Expected values: " + inputData.length);
            long[] inputShape = {
                1, 3, INPUT_SIZE, INPUT_SIZE
            };

            try (
                OnnxTensor inputTensor =
                    OnnxTensor.createTensor(
                        environment,
                        FloatBuffer.wrap(inputData),
                        inputShape
                    );

                OrtSession.Result output =
                    session.run(
                        Map.of("images", inputTensor)
                    )
            ) {

                float[][][] predictions =
                    (float[][][]) output.get(0).getValue();
                float maxConfidence = 0;

                for (float[] prediction : predictions[0]) {
                    maxConfidence = Math.max(maxConfidence, prediction[4]);
                }

                System.out.println("Maximum AI Confidence: " + maxConfidence);

                for (int i = 0; i < Math.min(5, predictions[0].length); i++) {
                    System.out.println(
                        "Raw Detection " + i + ": "
                        + java.util.Arrays.toString(predictions[0][i])
                    );
                }

                for (float[] prediction : predictions[0]) {

                    float confidence = prediction[4];

                    int classId =
                        Math.round(prediction[5]);

                    if (confidence < CONFIDENCE_THRESHOLD) {
                        continue;
                    }

                    if (classId < 0 ||
                        classId >= CLASS_NAMES.length) {
                        continue;
                    }

                    int x1 = clamp(
                        (int) ((prediction[0] - padX) / scale),
                        0,
                        originalWidth
                    );

                    int y1 = clamp(
                        (int) ((prediction[1] - padY) / scale),
                        0,
                        originalHeight
                    );

                    int x2 = clamp(
                        (int) ((prediction[2] - padX) / scale),
                        0,
                        originalWidth
                    );

                    int y2 = clamp(
                        (int) ((prediction[3] - padY) / scale),
                        0,
                        originalHeight
                    );

                    if (x2 <= x1 || y2 <= y1) {
                        continue;
                    }

                    Map<String, Object> detection =
                        new HashMap<>();

                    detection.put("classId", classId);
                    detection.put(
                        "damageType",
                        CLASS_NAMES[classId]
                    );

                    detection.put(
                        "confidence",
                        Math.round(
                            confidence * 10000.0
                        ) / 100.0
                    );

                    detection.put("x1", x1);
                    detection.put("y1", y1);
                    detection.put("x2", x2);
                    detection.put("y2", y2);

                    detections.add(detection);

                    Imgproc.rectangle(
                        originalImage,
                        new Point(x1, y1),
                        new Point(x2, y2),
                        new Scalar(0, 0, 255),
                        3
                    );

                    Imgproc.putText(
                        originalImage,
                        CLASS_NAMES[classId] +
                            " " +
                            String.format(
                                "%.1f%%",
                                confidence * 100
                            ),
                        new Point(
                            x1,
                            Math.max(20, y1 - 8)
                        ),
                        Imgproc.FONT_HERSHEY_SIMPLEX,
                        0.6,
                        new Scalar(0, 0, 255),
                        2
                    );
                }
            }

        } finally {

            resized.release();
            padded.release();
            rgb.release();
            blob.release();
        }

        System.out.println(
            "AI detections found: " + detections.size()
        );

        return detections;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    @PreDestroy
    public void closeModel() {

        try {
            if (session != null) {
                session.close();
            }

            if (environment != null) {
                environment.close();
            }

            System.out.println("ONNX Model Closed.");

        } catch (Exception e) {
            System.err.println(
                "Error closing ONNX model: " + e.getMessage()
            );
        }
    }
}
