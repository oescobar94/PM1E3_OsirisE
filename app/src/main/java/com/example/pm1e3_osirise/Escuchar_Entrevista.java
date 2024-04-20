package com.example.pm1e3_osirise;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pm1e3_osirise.configuracion.Entrevista;
import com.example.pm1e3_osirise.configuracion.Utilidades;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Escuchar_Entrevista extends AppCompatActivity{

    private RecyclerView recyclerView;
    private EscucharAudio_Adapter Escuchar_audio_adapter;
    private List<Entrevista> listaEntrevistas;
    Button btnRegresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_entrevista);

        btnRegresar = findViewById(R.id.btnRegresar);
        recyclerView = findViewById(R.id.recyclerViewEntrevistas);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Escuchar_Entrevista.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Llama al método para obtener las entrevistas de Firestore
        obtenerListaEntrevistas();

        // Crear el adaptador y asignarlo al RecyclerView
        Escuchar_audio_adapter = new EscucharAudio_Adapter(this, listaEntrevistas);
        recyclerView.setAdapter(Escuchar_audio_adapter);
    }

    private void obtenerListaEntrevistas() {
        listaEntrevistas = new ArrayList<>(); // Inicializa la lista vacía

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Entrevista")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Entrevista entrevista = document.toObject(Entrevista.class);
                            listaEntrevistas.add(entrevista);
                        }
                        // Luego de completar la lista, actualiza el RecyclerView
                        actualizarRecyclerView();
                    } else {
                        Toast.makeText(Escuchar_Entrevista.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarRecyclerView() {
        // Verifica si la lista no está vacía y actualiza el adaptador
        if (!listaEntrevistas.isEmpty()) {
            if (Escuchar_audio_adapter == null) {
                Escuchar_audio_adapter = new EscucharAudio_Adapter(this, listaEntrevistas);
                recyclerView.setAdapter(Escuchar_audio_adapter);
            } else {
                Escuchar_audio_adapter.notifyDataSetChanged();
            }
        } else {
            Toast.makeText(Escuchar_Entrevista.this, "No hay datos disponibles", Toast.LENGTH_SHORT).show();
        }
    }
}
