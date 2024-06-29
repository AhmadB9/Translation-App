package com.example.translationapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;


import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.example.translationapp.ml.Efficientdet;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;

import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


import org.tensorflow.lite.support.image.TensorImage;


import java.io.IOException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;


public class TouristMode extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private Button btnVoiceToVoice, btnScan;

    Uri imageUri=null;
    private ImageView btnSpeak, btnSwitchLanguage, btnCamera, btnExit, btnPlay;
    private Spinner spFrom, spTO, spLanguage;
    private Intent speechRecognizerIntent;
    private String sourceLanguageTitle;
    private String targetLanguageTitle;
    private String countryLanguage = "";
    private String sourceLanguageCode = "en";
    private String targetLanguageCode = "ar";
    public String sequence = "";
    private TextToSpeech textToSpeech;
    private ArrayList<ModelLanguage> languageArrayList;
    private TranslatorOptions translatorOptions;
    private Translator translator;
    private LocationManager locationManager;
    private static HashMap<String, String> countryLanguageMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1000;
    private int scanId = 0;
    List<String> languagesChoose = new ArrayList<>();
    private int fromPosition = 12;
    private int toPosition = 2;
    private TextView txtWelcome,txtTitle;
    TextRecognizer textRecognition;
    private ImageView imageView;
    private String[] cameraPermission;
    private String[] storagePermission;
    private int fromCamera_1=0;
    Bitmap imageBitmap =null;
    private int UriWidth;
    private int UriHeight;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch btnSwitch;
    private ProgressDialog progressDialog;
    private RelativeLayout relativeLayout;
    private LinearLayout scanLinearLayout;
    private LanguageIdentifier languageIdentifier;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourist_mode);
        txtWelcome = findViewById(R.id.txtWelcome);
        btnSpeak = findViewById(R.id.btnMic);
        imageView = findViewById(R.id.image_output);
        spTO = findViewById(R.id.spTo);
        spFrom = findViewById(R.id.spFrom);
        btnSwitch = findViewById(R.id.btnswitchTourist);
        btnSwitchLanguage = findViewById(R.id.btnswitch);
        btnCamera = findViewById(R.id.CameraView);
        btnExit = findViewById(R.id.btnExit);
        btnPlay = findViewById(R.id.btnpaly);
        spLanguage = findViewById(R.id.splanguage);
        btnVoiceToVoice = findViewById(R.id.btnvoicetovoce);
        btnScan = findViewById(R.id.btnScan);
        scanLinearLayout = findViewById(R.id.scanLinearlayout);
        relativeLayout = findViewById(R.id.relativelayout);
        txtTitle=findViewById(R.id.txt_titleoutput);

        cameraPermission = new String[]{Manifest.permission.CAMERA};

        textRecognition = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        getWindow().setStatusBarColor(0xFF14A7EA);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait For Moments");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                btnSwitch.setChecked(false);
            }
        });

        loadAvailableLanguage();
        getLanguageChoose();

        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LanguageIdentificationOptions options = new LanguageIdentificationOptions.Builder()
                .setConfidenceThreshold(0.5f)
                .build();
        languageIdentifier = LanguageIdentification.getClient(options);
        btnSwitch = findViewById(R.id.btnswitchTourist);

        ArrayAdapter<String> Adapter = new ArrayAdapter<>(TouristMode.this, android.R.layout.simple_list_item_1, languagesChoose);
        spTO.setAdapter(Adapter);
        spFrom.setAdapter(Adapter);
        spLanguage.setAdapter(Adapter);
        spLanguage.setSelection(2);
        spFrom.setSelection(12);
        spTO.setSelection(2);

        countryLanguageMap = new HashMap<>();
        countryLanguageMap.put("united states", "English");
        countryLanguageMap.put("united kingdom", "English");
        countryLanguageMap.put("france", "French");
        countryLanguageMap.put("germany", "German");
        countryLanguageMap.put("lebanon", "Arabic");

        spTO.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                targetLanguageCode = languageArrayList.get(position).getLanguageCode();
                targetLanguageTitle = languageArrayList.get(position).getLanguageTitle();
                spLanguage.setSelection(position);
                fromPosition = position;
                TextView textView = ((TextView) parent.getChildAt(0));
                if (textView != null)
                    textView.setTextColor(Color.WHITE);
                if (!Objects.equals(targetLanguageTitle, countryLanguage))
                    btnSwitch.setChecked(false);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sourceLanguageCode = languageArrayList.get(position).getLanguageCode();
                sourceLanguageTitle = languageArrayList.get(position).getLanguageTitle();
                toPosition = position;
                TextView textView = ((TextView) parent.getChildAt(0));
                if (textView != null)
                    textView.setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spLanguage.getVisibility() == View.VISIBLE) {
                    targetLanguageCode = languageArrayList.get(position).getLanguageCode();
                    targetLanguageTitle = languageArrayList.get(position).getLanguageTitle();
                    sequence="";
                    TextView textView = ((TextView) parent.getChildAt(0));
                    if (textView != null)
                        textView.setTextColor(Color.WHITE);
                    spTO.setSelection(position);
                    if (textToSpeech != null) {
                        textToSpeech.stop();
                        textToSpeech.shutdown();
                    }
                    if(scanId==1)
                        recognizeText(uriToBitmap(TouristMode.this,imageUri));
                    else if(scanId==2){
                        if(fromCamera_1==1)
                            objectDetectionFromImage(imageBitmap);
                        else objectDetectionFromImage(uriToBitmap(TouristMode.this,imageUri));

                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputSelectionDialog();
            }
        });


        btnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ContextCompat.checkSelfPermission(TouristMode.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // Request permission
                        ActivityCompat.requestPermissions(TouristMode.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                        btnSwitch.setChecked(false);
                    } else {
                        startLocationUpdates();
                    }
                }
            }
        });
        btnSwitchLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spFrom.setSelection(fromPosition);
                spTO.setSelection(toPosition);

                targetLanguageCode = languageArrayList.get(fromPosition).getLanguageCode();
                targetLanguageTitle = languageArrayList.get(fromPosition).getLanguageTitle();


                sourceLanguageCode = languageArrayList.get(toPosition).getLanguageCode();
                sourceLanguageTitle = languageArrayList.get(toPosition).getLanguageTitle();
            }
        });

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TTS();


            }
        });
        btnVoiceToVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TTS();
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSCameraPermission()) {
                    sequence = "";
                    showInputDialog();
                } else requestCameraPermission();
            }
        });
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSCameraPermission()) {
                    sequence = "";
                    showInputDialog();
                } else requestCameraPermission();
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sequence = "";
                setVisibility();
                imageView.setImageDrawable(null);
                imageBitmap=null;
                imageUri=null;
                if (textToSpeech != null) {
                    textToSpeech.stop();
                    textToSpeech.shutdown();
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sequence.isEmpty())
                    initialSpeech();
                else
                    Toast.makeText(TouristMode.this, "No text detected", Toast.LENGTH_SHORT).show();

            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null) {
                sequence = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
                startTranslate();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates();

            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (cameraAccepted)
                    pickImageFromCamera();
                else
                    Toast.makeText(this, "Camera & storage are required", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void startTranslate() {
        progressDialog.setMessage("Processing language model...");
        progressDialog.show();


        translatorOptions = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguageCode)
                .setTargetLanguage(targetLanguageCode)
                .build();
        translator = Translation.getClient(translatorOptions);
        DownloadConditions downloadConditions = new DownloadConditions.Builder()
                .requireWifi().build();
        translator.downloadModelIfNeeded(downloadConditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.setMessage("Translating...");
                progressDialog.show();
                translator.translate(sequence).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        progressDialog.setMessage("Translation completed successfully");
                        progressDialog.dismiss();
                        sequence = s;
                        initialSpeech();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.setMessage("Translation failed.Please try again");
                        progressDialog.setCanceledOnTouchOutside(true);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.setMessage("Please try again");
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.dismiss();
            }
        });
    }


    public static String getLanguageFromCode(String languageCode) {
        Locale locale = new Locale(languageCode);
        return locale.getDisplayLanguage();
    }

    private void getLanguageChoose() {
        for (int i = 0; i < languageArrayList.size(); i++) {
            languagesChoose.add(i, languageArrayList.get(i).getLanguageTitle());

        }
    }

    private void loadAvailableLanguage() {
        languageArrayList = new ArrayList<>();

        List<String> langugeCodeList = TranslateLanguage.getAllLanguages();
        for (String languageCode : langugeCodeList) {
            String languageTitle = new Locale(languageCode).getDisplayLanguage();
            ModelLanguage modelLanguage = new ModelLanguage(languageCode, languageTitle);
            languageArrayList.add(modelLanguage);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result;
            if (targetLanguageCode.equals("fr"))
                result = textToSpeech.setLanguage(Locale.FRENCH);
            else result = textToSpeech.setLanguage(new Locale(targetLanguageCode));

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(TouristMode.this, "Language is not supported", Toast.LENGTH_SHORT).show();
            } else {
                textToSpeech.speak(sequence, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }
    }

    private void startLocationUpdates() {
        // Check if the location provider is enabled
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Request location updates
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(TouristMode.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }
            progressDialog.setMessage("Find language base on your location...");
            progressDialog.show();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 2000, new LocationListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    // Get latitude and longitude
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Use Geocoder to find country name
                    Geocoder geocoder = new Geocoder(TouristMode.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            String countryName = "";
                            countryName = addresses.get(0).getCountryName();
                            if (!countryName.isEmpty()) {
                                txtWelcome.setVisibility(View.VISIBLE);
                                txtWelcome.setText("Welcome to " + countryName.toUpperCase());
                                countryLanguage = getLanguage(countryName);
                                int index = languagesChoose.indexOf(countryLanguage);
                                targetLanguageCode = languageArrayList.get(index).languageCode;
                                spTO.setSelection(index);
                                progressDialog.dismiss();

                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            });
        } else {
            // Show a message to enable GPS
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show();
            btnSwitch.setChecked(false);
        }
    }

    public static String getLanguage(@NonNull String country) {
        // Convert the country name to lowercase for case-insensitive matching
        String lowercaseCountry = country.toLowerCase();

        // Check if the country is in the map
        return countryLanguageMap.getOrDefault(lowercaseCountry, "Language not found");
    }

    private void initialSpeech() {
        textToSpeech = new TextToSpeech(this, this);

    }

    public boolean checkSCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        return result;
    }

    private void requestCameraPermission() {
        // Request the camera permission
        ActivityCompat.requestPermissions(this,
                cameraPermission,
                CAMERA_PERMISSION_REQUEST_CODE);
    }

    public void pickImageFromCamera() {
        if(scanId==1){
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "image");

            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Intent intent_translate = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent_translate.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            cameraActivityResultLauncher.launch(intent_translate);}

        else if(scanId==2) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Set the desired resolution
            // 1 for highest quality
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            // Set the desired size limit for the image
            intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1080* 1080 * 3);
            cameraActivityResultLauncher.launch(intent);
        }

    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getResultCode() == Activity.RESULT_OK) {
                if(scanId==2){
                    Bundle extras = o.getData().getExtras();
                    imageBitmap = (Bitmap) extras.get("data");
                    imageView.setImageBitmap(imageBitmap);
                }else imageView.setImageURI(imageUri);
                sequence="";
                btnSwitch.setVisibility(View.INVISIBLE);
                btnSpeak.setVisibility(View.INVISIBLE);
                btnSwitchLanguage.setVisibility(View.INVISIBLE);
                btnCamera.setVisibility(View.INVISIBLE);
                spFrom.setVisibility(View.INVISIBLE);
                spTO.setVisibility(View.INVISIBLE);
                btnVoiceToVoice.setVisibility(View.INVISIBLE);
                btnScan.setVisibility(View.INVISIBLE);
                relativeLayout.setVisibility(View.INVISIBLE);
                txtWelcome.setVisibility(View.INVISIBLE);

                txtTitle.setVisibility(View.VISIBLE);
                scanLinearLayout.setVisibility(View.VISIBLE);
                spLanguage.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
                btnExit.setVisibility(View.VISIBLE);
                btnPlay.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(imageBitmap);
                if (scanId == 1){
                    recognizeText(uriToBitmap(TouristMode.this,imageUri));}
                else if (scanId == 2){
                    objectDetectionFromImage(imageBitmap);
                }

            } else {
                setVisibility();
                imageView.setImageDrawable(null);
                Toast.makeText(TouristMode.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private void recognizeText(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        progressDialog.setMessage("Scanning...");
        progressDialog.show();
        // Process the image for text recognition
        textRecognition.process(image)
                .addOnCompleteListener(new OnCompleteListener<Text>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(@NonNull Task<Text> task) {
                        if (task.isSuccessful()) {
                            Text result = task.getResult();
                            if (result != null) {
                                processTextBlocks(result);
                            } else {
                                Toast.makeText(TouristMode.this, "No text detected", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(TouristMode.this, "Text recognition failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static Bitmap uriToBitmap(Context context, Uri uri) {
        try {
            ContentResolver resolver = context.getContentResolver();
            // Open an input stream from the URI
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeStream(resolver.openInputStream(uri), null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void processTextBlocks(Text result) {
        List<Text.TextBlock> blocks = result.getTextBlocks();

        List<Rect> boundingBoxes = new ArrayList<>();
        List<String> texts = new ArrayList<>();

        int[] imageSize =getImageSizeFromUri(TouristMode.this, imageUri);

        if (imageSize != null) {
            UriWidth = imageSize[0];
            UriHeight = imageSize[1];
        }
        //Threshold for the minimum width or height of the text block
        int minTextSizeThreshold;
        if((UriHeight >860 || UriWidth >1080)){
            minTextSizeThreshold = 750;
        }else minTextSizeThreshold = 50;
        for (Text.TextBlock block : blocks) {
            Rect boundingBox = block.getBoundingBox();

            // Check if the width or height of the bounding box is greater than the threshold
            assert boundingBox != null;
            if (boundingBox.width() >= minTextSizeThreshold || boundingBox.height() >= minTextSizeThreshold) {
                boundingBoxes.add(boundingBox);
                texts.add(block.getText());
            }
        }
        if (texts.isEmpty()) {
            Toast.makeText(TouristMode.this, "No text detected", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        } else
            drawBoundingBoxesOnBitmap(Objects.requireNonNull(uriToBitmap(TouristMode.this,imageUri)), boundingBoxes, texts);
    }


    private void drawBoundingBoxesOnBitmap(Bitmap bitmap, List<Rect> boundingBoxes, List<String> texts) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        if((UriHeight >860 || UriWidth >1080))
            paint.setTextSize(150);
        else paint.setTextSize(15);

        // Counter for translation tasks

        AtomicInteger translationTasksCount = new AtomicInteger(boundingBoxes.size());

        // Iterate through bounding boxes and texts
        for (int i = 0; i < boundingBoxes.size(); i++) {
            int finalI = i;
            languageIdentifier.identifyLanguage(texts.get(finalI))
                    .addOnSuccessListener(
                            new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(@Nullable String languageCode) {
                                    assert languageCode != null;
                                    if (languageCode.equals("und")) {
                                    } else {
                                        sourceLanguageCode=languageCode;
                                    }
                                    TranslatorOptions translatorOptions = new TranslatorOptions.Builder()
                                            .setSourceLanguage(sourceLanguageCode)
                                            .setTargetLanguage(targetLanguageCode)
                                            .build();
                                    translator = Translation.getClient(translatorOptions);
                                    DownloadConditions downloadConditions = new DownloadConditions.Builder()
                                            .requireWifi().build();

                                    translator.downloadModelIfNeeded(downloadConditions).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            translator.translate(texts.get(finalI)).addOnSuccessListener(new OnSuccessListener<String>() {
                                                @Override
                                                public void onSuccess(String s) {
                                                    sequence = sequence + s + ".";
                                                    Rect boundingBox = boundingBoxes.get(finalI);

                                                    // Set the rectangle fill color (blue in this example)
                                                    paint.setColor(Color.GRAY);
                                                    paint.setStyle(Paint.Style.FILL);

                                                    // Draw a filled rectangle with the specified color inside the bounding box
                                                    canvas.drawRect(boundingBox, paint);

                                                    // Set the text color (white in this example)
                                                    paint.setColor(Color.WHITE);

                                                    float textX = boundingBox.left;
                                                    float textY = boundingBox.top + (float) boundingBox.height() / 2;

                                                    canvas.drawText(s, textX, textY, paint);

                                                    paint.setStyle(Paint.Style.STROKE);
                                                    paint.setStrokeWidth(1.50f);

                                                    // Draw the border of the rectangle
                                                    canvas.drawRect(boundingBox, paint);

                                                    // Check if all translation tasks are completed
                                                    if (translationTasksCount.decrementAndGet() == 0) {
                                                        imageView.setImageBitmap(mutableBitmap);
                                                        progressDialog.dismiss();


                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(TouristMode.this, "Translation failed. Please try again", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(TouristMode.this, "Please try again", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
        }


    }

    private void setVisibility() {

        btnSwitch.setVisibility(View.VISIBLE);
        btnSpeak.setVisibility(View.VISIBLE);
        btnSwitchLanguage.setVisibility(View.VISIBLE);
        btnCamera.setVisibility(View.VISIBLE);
        spFrom.setVisibility(View.VISIBLE);
        spTO.setVisibility(View.VISIBLE);
        btnVoiceToVoice.setVisibility(View.VISIBLE);
        btnScan.setVisibility(View.VISIBLE);
        relativeLayout.setVisibility(View.VISIBLE);
        if(!txtWelcome.getText().toString().isEmpty())
            txtWelcome.setVisibility(View.VISIBLE);

        txtTitle.setVisibility(View.INVISIBLE);
        scanLinearLayout.setVisibility(View.INVISIBLE);
        spLanguage.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.INVISIBLE);
        btnExit.setVisibility(View.INVISIBLE);
        btnPlay.setVisibility(View.INVISIBLE);
    }

    private void TTS() {
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLanguageCode);
        startActivityForResult(speechRecognizerIntent, 100);
    }

    private void objectDetectionFromImage(Bitmap originalBitmap) {

        try {
            Efficientdet model = Efficientdet.newInstance(this);

            TensorImage image = TensorImage.fromBitmap(originalBitmap);

            // Runs model inference and gets result

            Efficientdet.Outputs outputs = model.process(image);
            List<Efficientdet.DetectionResult> detectionResults = outputs.getDetectionResultList();

            // Create a mutable copy of the original bitmap to draw on.
            Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);

            // Initialize paint objects.
            Paint rectPaint = new Paint();
            rectPaint.setStyle(Paint.Style.STROKE);
            rectPaint.setColor(Color.GREEN);
            rectPaint.setStrokeWidth(1);

            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(10);

            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (Efficientdet.DetectionResult detectionResult : detectionResults) {
                // Gets result from DetectionResult.
                RectF location = detectionResult.getLocationAsRectF();
                // Create a new adjusted bounding box
                RectF adjustedBox = new RectF(location.left, location.top, location.right, location.bottom);
                String category = detectionResult.getCategoryAsString();
                float score = detectionResult.getScoreAsFloat();

                if (score > 0.4) {
                    CompletableFuture<Void> future = new CompletableFuture<>();
                    futures.add(future);

                    TranslatorOptions translatorOptions = new TranslatorOptions.Builder()
                            .setSourceLanguage("en")
                            .setTargetLanguage(targetLanguageCode)
                            .build();
                    translator = Translation.getClient(translatorOptions);
                    DownloadConditions downloadConditions = new DownloadConditions.Builder()
                            .requireWifi().build();

                    translator.downloadModelIfNeeded(downloadConditions).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            translator.translate(category).addOnSuccessListener(new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    sequence = sequence + s + " .";

                                    // Draw the rectangle around the detected object.
                                    canvas.drawRect(adjustedBox, rectPaint);

                                    // Draw the label and the score.
                                    @SuppressLint("DefaultLocale") String label = s + " (" + String.format("%.2f", score) + ")";
                                    canvas.drawText(label, adjustedBox.left, adjustedBox.top - 10, textPaint);

                                    future.complete(null); // Signal completion of this task
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    future.completeExceptionally(e); // Signal failure of this task
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            future.completeExceptionally(e); // Signal failure of this task
                        }
                    });
                }
            }

            // Combine all futures to wait for all tasks to complete
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            allFutures.thenAccept(ignored -> imageView.setImageBitmap(mutableBitmap))
                    .exceptionally(ex -> {
                        // Handle any exceptions that occurred during the tasks
                        ex.printStackTrace();
                        return null;
                    });
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }


    }


    private void showInputDialog() {
        PopupMenu popupMenu = new PopupMenu(this, btnCamera);
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Translation");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Object Detection");
        popupMenu.show();
        scanId=0;
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id = item.getItemId();
                if (id == 1) {
                    scanId = 1;
                    txtTitle.setText("Translation");
                    showInputSelectionDialog();
                } else if (id == 2) {
                    txtTitle.setText("Object Detection");
                    scanId = 2;
                    showInputSelectionDialog();
                }
                return false;
            }
        });

    }

    private void showInputSelectionDialog() {

        PopupMenu popupMenu = new PopupMenu(this, btnCamera);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id = item.getItemId();
                if (id == 1) {
                    if (checkSCameraPermission()){
                        fromCamera_1=1;
                        pickImageFromCamera();
                    }
                    else requestCameraPermission();
                }
                if (id == 2) {
                    if (!checkStoragePermission()) {
                        requestForStoragePermission();
                    } else {
                        fromCamera_1=0;
                        pickImageFromGallery();
                    }

                }

                return false;
            }
        });
    }
    public boolean checkStoragePermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestForStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_PERMISSION_REQUEST_CODE);
    }
    private void pickImageFromGallery(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }
    private final ActivityResultLauncher<Intent>galleryActivityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getResultCode() == Activity.RESULT_OK) {
                assert o.getData() != null;
                imageUri = o.getData().getData();

                sequence="";
                btnSwitch.setVisibility(View.INVISIBLE);
                btnSpeak.setVisibility(View.INVISIBLE);
                btnSwitchLanguage.setVisibility(View.INVISIBLE);
                btnCamera.setVisibility(View.INVISIBLE);
                spFrom.setVisibility(View.INVISIBLE);
                spTO.setVisibility(View.INVISIBLE);
                btnVoiceToVoice.setVisibility(View.INVISIBLE);
                btnScan.setVisibility(View.INVISIBLE);
                relativeLayout.setVisibility(View.INVISIBLE);
                txtWelcome.setVisibility(View.INVISIBLE);

                txtTitle.setVisibility(View.VISIBLE);
                scanLinearLayout.setVisibility(View.VISIBLE);
                spLanguage.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
                btnExit.setVisibility(View.VISIBLE);
                btnPlay.setVisibility(View.VISIBLE);
                if(scanId==2 && imageUri != null)
                    objectDetectionFromImage(uriToBitmap(TouristMode.this,imageUri));
                if(scanId==1 && imageUri != null)
                    recognizeText(uriToBitmap(TouristMode.this,imageUri));
            }
        }
    });
    public static int[] getImageSizeFromUri(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;  // Decode image bounds only

            BitmapFactory.decodeStream(inputStream, null, options);

            int width = options.outWidth;
            int height = options.outHeight;

            return new int[]{width, height};
        } catch (Exception e) {
            e.printStackTrace();
            return null;  // Return null in case of an error
        }
    }
}
