package com.northeastern.finly;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class NumberExtractorActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int CSV_REQUEST_CODE = 102;

    private TextView resultTextView;
    private Uri photoUri;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_extractor);

        resultTextView = findViewById(R.id.resultTextView);
        Button csvButton = findViewById(R.id.csvButton);
        Button cameraButton = findViewById(R.id.cameraButton);

        csvButton.setOnClickListener(v -> openCsvPicker());
        cameraButton.setOnClickListener(v -> checkCameraPermission());
    }

    private void openCsvPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        startActivityForResult(Intent.createChooser(intent, "Select CSV File"), CSV_REQUEST_CODE);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
                return;
            }
            if (photoFile != null) {
                try {
                    photoUri = FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                } catch (Exception e) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw new Exception("Could not create directory");
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    private List<Double> readNumbersFromCsv(Uri uri) {
        List<Double> numbers = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    getContentResolver().openInputStream(uri)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                for (String value : values) {
                    try {
                        numbers.add(Double.parseDouble(value.trim()));
                    } catch (NumberFormatException e) {
                        // Skip non-numeric values
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading CSV: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
        return numbers;
    }

    private void processImage() {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String extractedText = visionText.getText();
                        List<Double> numbers = new ArrayList<>();

                        String[] words = extractedText.split("\\s+");
                        for (String word : words) {
                            try {
                                numbers.add(Double.parseDouble(word.trim()));
                            } catch (NumberFormatException e) {
                                // Skip non-numeric values
                            }
                        }

                        displayNumbers(numbers);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Text recognition failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CSV_REQUEST_CODE && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    List<Double> numbers = readNumbersFromCsv(uri);
                    displayNumbers(numbers);
                }
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                Toast.makeText(this, "Image captured, processing...", Toast.LENGTH_SHORT).show();
                if (photoFile != null && photoFile.exists()) {
                    processImage();
                } else {
                    Toast.makeText(this, "Error: Image file not found", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Operation cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayNumbers(List<Double> numbers) {
        if (numbers.isEmpty()) {
            resultTextView.setText("No numbers found");
            return;
        }

        StringBuilder result = new StringBuilder("Numbers found:\n");
        for (Double number : numbers) {
            result.append(number).append("\n");
        }
        resultTextView.setText(result.toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}