package com.example.translationapp;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private TranslatorOptions translatorOptions;
    private Translator translator;
    private ProgressDialog progressDialog;
    private Button btnTranslate;
    ImageView btnSpeakFrom,btnSpeakTo,btnMicrophone,btnSwitch,btnCamera,btnCopy;
    private EditText input,output;
    private Spinner spFrom,spTo;
    private ArrayList<ModelLanguage>languageArrayList;
    private String sourceLanguageCode="en";
    private  String targetLanguageCode="ar";
    private  Intent speechRecognizerIntent;
    private String sourceLanguageTitle;
    private  String targetLanguageTitle;
    private String sequence="";
    private TextToSpeech textToSpeech;
    public int q=0;
    private int fromPosition=12;
    private int toPosition=2;
    private String inputText="";
    private String outputText="";
    private String[] cameraPermission;
    private String[] storagePermission;
    private Uri imageUri=null;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 101;
    private TextRecognizer textRecognition;
    private LanguageIdentificationOptions options;
    private LanguageIdentifier languageIdentifier;
    private int speak_tranlate_flag=1;
    List<String>languagesChoose=new ArrayList<>();
    
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTranslate=findViewById(R.id.btntranslate);
        input=findViewById(R.id.input);
        output=findViewById(R.id.output);
        spFrom=findViewById(R.id.spfrom);
        spTo=findViewById(R.id.spto);
        btnSpeakFrom=findViewById(R.id.btnspeak);
        btnSpeakTo=findViewById(R.id.btntospeak);
        btnSwitch=findViewById(R.id.btnSwitch);
        btnCamera=findViewById(R.id.btnCamera);
        btnCopy=findViewById(R.id.btnCopy);
        output.setFocusable(false);
        output.setClickable(false);
        output.setLongClickable(false);
        output.setVerticalScrollBarEnabled(true);
        output.setBackground(null);
        input.setBackground(null);
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait For Moments");
        progressDialog.setCanceledOnTouchOutside(false);
        btnMicrophone=findViewById(R.id.btnmicro);
        getWindow().setStatusBarColor(0xFF14A7EA);



        loadAvailableLanguage();
        getLanguageChoose();

        cameraPermission=new String[]{Manifest.permission.CAMERA};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        textRecognition= TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        options = new LanguageIdentificationOptions.Builder()
                        .setConfidenceThreshold(0.5f)
                        .build();
        languageIdentifier = LanguageIdentification.getClient();


        ArrayAdapter<String> Adapter=new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1,languagesChoose);
        spTo.setAdapter(Adapter);
        spFrom.setAdapter(Adapter);
        spFrom.setSelection(12);
        spTo.setSelection(2);

        spTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                targetLanguageCode=languageArrayList.get(position).getLanguageCode();
                targetLanguageTitle=languageArrayList.get(position).getLanguageTitle();
                fromPosition=position;
                TextView textView=((TextView) parent.getChildAt(0));
                if(textView != null)
                    textView.setTextColor(Color.WHITE);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sourceLanguageCode=languageArrayList.get(position).getLanguageCode();
                sourceLanguageTitle=languageArrayList.get(position).getLanguageTitle();
                toPosition=position;
                TextView textView=((TextView) parent.getChildAt(0));
                if(textView != null)
                    textView.setTextColor(Color.WHITE);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(input.getText().toString().isEmpty())
                    Toast.makeText(MainActivity.this,"Please Enter Text To Translate",Toast.LENGTH_SHORT).show();
                else {
                    identifyLanguage(input.getText().toString());
                }

            }
        });

        btnSpeakFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!input.getText().toString().isEmpty()){
                    q= 1;
                    speak_tranlate_flag = 0;
                    initialSpeech();
                }

            }
        });
        btnSpeakTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!output.getText().toString().isEmpty()){
                    q=2;
                    speak_tranlate_flag=0;
                    initialSpeech();
                }

            }
        });

        btnMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                q=0;
                speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLanguageCode);
                startActivityForResult(speechRecognizerIntent,100);
            }
        });
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spFrom.setSelection(fromPosition);
                spTo.setSelection(toPosition);


                inputText=input.getText().toString();
                outputText=output.getText().toString();
                if(!outputText.equals("")) {
                    input.setText(outputText);
                    output.setText(inputText);
                }
            }
        });
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!output.getText().toString().isEmpty())
                    copyTextToClipboard();
            }
        });
        input.setOnKeyListener(new View.OnKeyListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if(input.getText().toString().isEmpty())
                        Toast.makeText(MainActivity.this,"Please Enter Text To Translate",Toast.LENGTH_SHORT).show();
                    else {
                        identifyLanguage(input.getText().toString());
                    }
                    hideKeyboard(input);
                    return true;
                }
                else return false;
            }
        });

    }
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = 0;
            if(q==1){
                identifyLanguage(input.getText().toString());
                if(sourceLanguageCode.equals("fr"))
                    result = textToSpeech.setLanguage(Locale.FRENCH);
                result = textToSpeech.setLanguage(new Locale(sourceLanguageCode));
            }
            else if(q==2){
                if(targetLanguageCode.equals("fr"))
                    result = textToSpeech.setLanguage(Locale.FRENCH);
                else result = textToSpeech.setLanguage(new Locale(targetLanguageCode));
            }

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(MainActivity.this, "Language is not supported",Toast.LENGTH_SHORT).show();
            }
            else{
                if(q==1){
                    textToSpeech.speak(input.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                    q=0;
                }
                else if(q==2){
                    textToSpeech.speak(output.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                    q=0;
                }
            }

        } else {
            Toast.makeText(MainActivity.this, "Initialization failed",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    private void startTranslate() {
        progressDialog.setMessage("Processing language model...");
        progressDialog.show();



        translatorOptions=new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguageCode)
                .setTargetLanguage(targetLanguageCode)
                .build();
        translator=Translation.getClient(translatorOptions);
        DownloadConditions downloadConditions=new DownloadConditions.Builder()
                .requireWifi().build();
        translator.downloadModelIfNeeded(downloadConditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.setMessage("Translating...");
                progressDialog.show();
                translator.translate(input.getText().toString()).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        progressDialog.setMessage("Translation completed successfully");
                        progressDialog.dismiss();
                        output.setText(s);

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
            }
        });
    }

    private void getLanguageChoose(){
        for(int i=0;i<languageArrayList.size();i++){
            languagesChoose.add(i,languageArrayList.get(i).getLanguageTitle());

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            String outputText= null;
            if (data != null) {
                outputText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            }
            input.setText(outputText);
        }
    }

    private void loadAvailableLanguage() {
        languageArrayList=new ArrayList<>();

        List<String>langugeCodeList=TranslateLanguage.getAllLanguages();
        for(String languageCode: langugeCodeList){
            String languageTitle=new Locale(languageCode).getDisplayLanguage();
            ModelLanguage modelLanguage=new ModelLanguage(languageCode,languageTitle);
            languageArrayList.add(modelLanguage);
        }
    }

    private void showInputDialog() {
        PopupMenu popupMenu=new PopupMenu(this,btnCamera);

        popupMenu.getMenu().add(Menu.NONE,1,1,"Camera");
        popupMenu.getMenu().add(Menu.NONE,2,2,"Gallery");
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if(id==1){
                    if (checkSCameraPermission())
                        pickImageFromCamera();
                    else requestCameraPermission();
                }
                if (id==2){
                    if(!checkStoragePermission()) {
                        requestForStoragePermission();}
                    else pickImageFromGallery();

                }

                return false;
            }
        });


    }
    private void initialSpeech(){
        textToSpeech = new TextToSpeech(this, this);

    }
    private void pickImageFromGallery(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }
    private final ActivityResultLauncher<Intent>galleryActivityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if(o.getResultCode()== Activity.RESULT_OK){
                imageUri=o.getData().getData();
                recognizeTextFromImage();
                progressDialog.dismiss();

            }
            else {
                Toast.makeText(MainActivity.this,"Cancelled",Toast.LENGTH_SHORT).show();
            }
        }
    });

    public void pickImageFromCamera(){
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"image");

        imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        cameraActivityResultLauncher.launch(intent);

    }
    private final ActivityResultLauncher<Intent>cameraActivityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if(o.getResultCode()== Activity.RESULT_OK){
                
                recognizeTextFromImage();
                progressDialog.dismiss();
            }
            else{
                Toast.makeText(MainActivity.this,"Cancelled",Toast.LENGTH_SHORT).show();
            }
        }
    });

    public boolean checkStoragePermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestForStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_PERMISSION_REQUEST_CODE);
    }
    public boolean checkSCameraPermission(){
        boolean result=ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        return result;
    }
    private void requestCameraPermission() {
        // Request the camera permission
        ActivityCompat.requestPermissions(this,
                cameraPermission,
                CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_PERMISSION_REQUEST_CODE:
                if(grantResults.length >0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted )
                        pickImageFromCamera();
                    else Toast.makeText(this,"Camera are required",Toast.LENGTH_SHORT).show();

                }
            case STORAGE_PERMISSION_REQUEST_CODE:
                if(grantResults.length >0){
                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted)
                        pickImageFromGallery();
                    else Toast.makeText(this,"Storage are required",Toast.LENGTH_SHORT).show();

                }


        }
    }
    public void recognizeTextFromImage(){
        progressDialog.setMessage("Prepare image....");
        progressDialog.show();

        try {
            InputImage inputImage=InputImage.fromFilePath(this,imageUri);
            progressDialog.setMessage("Recognize text ....");
            Task<Text> textResult=textRecognition.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text text) {
                    identifyLanguage(text.getText());
                    input.setText(text.getText());
                }
            });


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private void identifyLanguage(String text) {
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                assert languageCode != null;
                                if (languageCode.equals("und")) {
                                    startTranslate();
                                }
                                else {
                                    if(targetLanguageCode.equals(languageCode)) {
                                        spFrom.setSelection(fromPosition);
                                        spTo.setSelection(toPosition);
                                    }

                                    else{
                                       sourceLanguageCode = languageCode;
                                       String language = getLanguageFromCode(languageCode);
                                       int index = languagesChoose.indexOf(language);
                                       spFrom.setSelection(index);
                                       if(speak_tranlate_flag==1)
                                           startTranslate();
                                       else speak_tranlate_flag=1;
                                    }
                                }

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
    }
    public static String getLanguageFromCode(String languageCode) {
        Locale locale = new Locale(languageCode);
        return locale.getDisplayLanguage();
    }
    private void copyTextToClipboard() {

        String textToCopy = output.getText().toString();

        // Get the ClipboardManager
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // Create a ClipData object to store the text
        ClipData clip = ClipData.newPlainText("Copied Text", textToCopy);

        // Set the ClipData on the ClipboardManager
        clipboard.setPrimaryClip(clip);

        // Notify the user that the text has been copied
        Toast.makeText(this, "Text copied", Toast.LENGTH_SHORT).show();
    }
}
