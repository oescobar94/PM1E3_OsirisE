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

import com.example.pm1e3_osirise.configuracion.Entrevista;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
public class Modificar_Y_Eliminar extends AppCompatActivity{

    private RecyclerView recyclerView;
    private EntrevistaAdapter entrevistaAdapter;
    private List<Entrevista> listaEntrevistas;
    Button btnRegresar, btnEliminar, btnActualizar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_y_eliminar);
        btnRegresar = findViewById(R.id.btnRegresar);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnEliminar =  findViewById(R.id.btnEliminar);
        recyclerView = findViewById(R.id.recyclerViewEntrevistas);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        btnEliminar.setOnClickListener(v -> {
            // Notifica al adaptador para actualizar la vista
            entrevistaAdapter.notifyDataSetChanged();

            // Obtener los IDs de Firestore de los elementos seleccionados
            List<String> idsSeleccionados = entrevistaAdapter.obtenerIdsSeleccionados();

            // Eliminar los documentos correspondientes de Firestore
            eliminarDocumentosFirestore(idsSeleccionados);
        });

        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Modificar_Y_Eliminar.this, MainActivity.class);
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
                            String idFirestore = document.getId();
                            Entrevista entrevista = document.toObject(Entrevista.class);
                            entrevista.setIdFirestore(idFirestore);
                            listaEntrevistas.add(entrevista);
                        }
                        // Luego de completar la lista, actualiza el RecyclerView
                        actualizarRecyclerView();
                    } else {
                        // Verifica si la lista no está vacía y actualiza el adaptador
                        if (!listaEntrevistas.isEmpty()) {
                            entrevistaAdapter = new EntrevistaAdapter(this, listaEntrevistas);
                            recyclerView.setAdapter(entrevistaAdapter);

                            // Aquí asignamos la instancia actualizada del adaptador
                            RecyclerView.Adapter adapter = recyclerView.getAdapter();
                            if (adapter instanceof EntrevistaAdapter) {
                                entrevistaAdapter = (EntrevistaAdapter) adapter;
                            }
                        } else {
                            Toast.makeText(Modificar_Y_Eliminar.this, "No hay datos disponibles", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void actualizarRecyclerView() {
        // Verifica si la lista no está vacía y actualiza el adaptador
        if (!listaEntrevistas.isEmpty()) {
            entrevistaAdapter = new EntrevistaAdapter(this, listaEntrevistas);
            recyclerView.setAdapter(entrevistaAdapter);
        } else {
                Toast.makeText(Modificar_Y_Eliminar.this, "No hay datos disponibles", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarDocumentosFirestore(List<String> idsFirestore) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (String id : idsFirestore) {
            db.collection("Entrevista")
                    .document(id)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        entrevistaAdapter.actualizarListaDespuesEliminar(idsFirestore);
                        Toast.makeText(Modificar_Y_Eliminar.this, "Eliminado de firebase ", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Error al eliminar
                        // Puedes mostrar un mensaje de error o manejar la excepción aquí
                        Toast.makeText(Modificar_Y_Eliminar.this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

}
