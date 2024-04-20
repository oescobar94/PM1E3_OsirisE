package com.example.pm1e3_osirise;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.pm1e3_osirise.configuracion.*;
import com.example.pm1e3_osirise.EntrevistaAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
public class ListaEntrevistas extends AppCompatActivity{

    private RecyclerView recyclerView;
    private EntrevistaAdapter entrevistaAdapter;
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
                Intent intent = new Intent(ListaEntrevistas.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Llama al método para obtener las entrevistas de Firestore
        obtenerListaEntrevistas();

        // Crear el adaptador y asignarlo al RecyclerView
        entrevistaAdapter = new EntrevistaAdapter(this, listaEntrevistas);
        recyclerView.setAdapter(entrevistaAdapter);
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
                        Toast.makeText(ListaEntrevistas.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarRecyclerView() {
        // Verifica si la lista no está vacía y actualiza el adaptador
        if (!listaEntrevistas.isEmpty()) {
            entrevistaAdapter = new EntrevistaAdapter(this, listaEntrevistas);
            recyclerView.setAdapter(entrevistaAdapter);
        } else {
            Toast.makeText(ListaEntrevistas.this, "No hay datos disponibles", Toast.LENGTH_SHORT).show();
        }
    }

}
