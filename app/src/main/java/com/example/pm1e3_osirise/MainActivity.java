package com.example.pm1e3_osirise;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import android.Manifest;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int IMAGE_CAPTURE_REQUEST = 101;

    private EditText txtId, txtDescripcion, txtPeriodista, txtFecha;
    private Button btnIngresar, btnlista, btnDelUp, btnescuchar;
    private ImageButton imgBtnTomarFotografia;
    private ImageView imagenCapturada;
    private FirebaseFirestore db;
    private Button btnStartRecording;
    private Button btnStopRecording;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private static final int RECORD_AUDIO_PERMISSION_CODE = 200;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialización de los EditText, Button y FirebaseFirestore
        txtId = findViewById(R.id.txtid);
        txtDescripcion = findViewById(R.id.txtdescripcion);
        txtPeriodista = findViewById(R.id.txtperiodista);
        txtFecha = findViewById(R.id.txtfecha);
        btnIngresar = findViewById(R.id.btningresar);
        imgBtnTomarFotografia = findViewById(R.id.imgBtnTomarFotografia4);
        imagenCapturada = findViewById(R.id.imagenCaputar4);
        db = FirebaseFirestore.getInstance();
        btnStartRecording = findViewById(R.id.btnStartRecording);
        btnStopRecording = findViewById(R.id.btnStopRecording);
        btnlista = findViewById(R.id.btnlista);
        btnDelUp = findViewById(R.id.btnDelUp);
        btnescuchar = findViewById(R.id.btnescuchar);

        btnescuchar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un Intent para cambiar a ActividadListaEntrevistas
                Intent intent = new Intent(MainActivity.this, Escuchar_Entrevista.class);
                startActivity(intent); // Inicia la nueva actividad
            }
        });

        btnlista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un Intent para cambiar a ActividadListaEntrevistas
                Intent intent = new Intent(MainActivity.this, ListaEntrevistas.class);
                startActivity(intent); // Inicia la nueva actividad
            }
        });

        btnDelUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un Intent para cambiar a ActividadListaEntrevistas
                Intent intent = new Intent(MainActivity.this, Modificar_Y_Eliminar.class);
                startActivity(intent); // Inicia la nueva actividad
            }
        });

        btnStartRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasRecordPermission()) {
                    requestRecordPermission();
                } else {
                    startRecording();
                    btnStartRecording.setVisibility(View.GONE);
                    btnStopRecording.setVisibility(View.VISIBLE);
                }
            }
        });

        btnStopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                btnStopRecording.setVisibility(View.GONE);
                btnStartRecording.setVisibility(View.VISIBLE);
            }
        });

        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los valores de los EditText
                String id = txtId.getText().toString().trim();
                String descripcion = txtDescripcion.getText().toString().trim();
                String periodista = txtPeriodista.getText().toString().trim();
                String fecha = txtFecha.getText().toString().trim();
                String audioBase64 = convertAudioToBase64(audioFilePath);


                // Validación simple de los campos (puedes agregar más validaciones según tu lógica)
                if (id.isEmpty() || descripcion.isEmpty() || periodista.isEmpty() || fecha.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    // Crear un mapa con los datos a guardar en Firestore
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", id);
                    data.put("descripcion", descripcion);
                    data.put("periodista", periodista);
                    data.put("fecha", fecha);
                    data.put("audioBase64", audioBase64);

                    // Obtener la imagen capturada como Base64
                    Bitmap bitmap = ((BitmapDrawable) imagenCapturada.getDrawable()).getBitmap();
                    String imagenBase64 = convertBitmapToBase64(bitmap);

                    // Agregar la imagen en formato Base64 al mapa de datos
                    data.put("imagenBase64", imagenBase64);

                    // Guardar los datos en Firestore
                    db.collection("Entrevista")
                            .add(data)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    txtId.setText("");
                                    txtDescripcion.setText("");
                                    txtPeriodista.setText("");
                                    txtFecha.setText("");
                                    imagenCapturada.setImageResource(android.R.color.transparent); // Esto limpia la imagen
                                    btnStartRecording.setVisibility(View.VISIBLE);
                                    btnStopRecording.setVisibility(View.GONE);

                                    Toast.makeText(MainActivity.this, "Datos ingresados correctamente", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Error al ingresar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });


        imgBtnTomarFotografia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Solicitar permisos para la cámara
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_REQUEST);
                } else {
                    abrirCamara();
                }
            }
        });
    }


    // Método para abrir la cámara
    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST);
        }
    }

    // Manejo de la respuesta a la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Permiso denegado para la cámara", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                btnStartRecording.setVisibility(View.GONE);
                btnStopRecording.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Permiso de grabación de audio concedido", Toast.LENGTH_SHORT).show();
                startRecording();
            } else {
                // Permiso denegado
                Toast.makeText(this, "Permiso de grabación de audio denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Manejo del resultado de la captura de la imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CAPTURE_REQUEST && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imagenCapturada.setImageBitmap(imageBitmap);
        }
    }

    // Método para convertir un Bitmap a cadena Base64
    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Ajusta el formato y calidad según tu imagen
        byte[] byteArrayImage = baos.toByteArray();
        return Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        audioFilePath = getExternalCacheDir().getAbsolutePath() + "/audio.3gp";
        mediaRecorder.setOutputFile(audioFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Toast.makeText(this, "Grabando audio...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        Toast.makeText(this, "Grabación finalizada", Toast.LENGTH_SHORT).show();
    }

    private boolean hasRecordPermission() {
        return checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRecordPermission() {
        requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
    }

    private String convertAudioToBase64(String audioFilePath) {
        String base64Audio = "";
        try {
            File file = new File(audioFilePath);
            byte[] audioBytes = FileUtils.readFileToByteArray(file);
            base64Audio = Base64.encodeToString(audioBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64Audio;
    }
}