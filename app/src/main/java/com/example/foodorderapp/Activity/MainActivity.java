package com.example.foodorderapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.foodorderapp.Adapter.BestFoodsAdapter;
import com.example.foodorderapp.Adapter.CategoryAdapter;
import com.example.foodorderapp.Domain.Category;
import com.example.foodorderapp.Domain.Foods;
import com.example.foodorderapp.Domain.Location;
import com.example.foodorderapp.Domain.Price;
import com.example.foodorderapp.Domain.Time;
import com.example.foodorderapp.R;
import com.example.foodorderapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initLocation();
        initPrice();
        initTime();
        initBestFood();
        initCategory();
        setVariable();
    }

    private void setVariable() {

        FirebaseUser user = mAuth.getCurrentUser();
        String email = null;
        if (user != null) {
            email = user.getEmail();
        }
        if (email != null) {
            String username = email.substring(0, email.indexOf("@"));
            binding.userName.setText(username);
        }

        binding.logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
        });

        binding.searchBtn.setOnClickListener(v -> {
            String text = binding.searchEdit.getText().toString().trim();
            if (!text.isEmpty()) {
                routeSearch(text);
            }
        });

        binding.cardBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CartActivity.class)));

        binding.viewAll.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ListFoodsActivity.class);
            intent.putExtra("isBest", true);
            intent.putExtra("CategoryName", "Today's Best Foods");
            startActivity(intent);
        });

        binding.favBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListFoodsActivity.class);
            intent.putExtra("isFavorites", true);
            startActivity(intent);
        });
    }

    private void routeSearch(String query) {
        final String q = query.toLowerCase();

        DatabaseReference root   = database.getReference();
        DatabaseReference catRef = root.child("Category");

        // 1) Try category name match
        catRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer matchedCatId = null;
                String matchedCatName = null;

                for (DataSnapshot d : snapshot.getChildren()) {
                    Integer id = d.child("Id").getValue(Integer.class);
                    String name = d.child("Name").getValue(String.class);
                    if (id != null && name != null && name.toLowerCase().contains(q)) {
                        matchedCatId = id; matchedCatName = name;
                        break;
                    }
                }

                if (matchedCatId != null) {
                    // Category hit → open category list
                    Intent i = new Intent(MainActivity.this, ListFoodsActivity.class);
                    i.putExtra("CategoryId", matchedCatId);
                    i.putExtra("CategoryName", matchedCatName);
                    i.putExtra("isSearch", false);
                    startActivity(i);
                    overridePendingTransition(0, 0);
                } else {
                    // No category hit → open search list (ListFoodsActivity handles empty state)
                    Intent i = new Intent(MainActivity.this, ListFoodsActivity.class);
                    i.putExtra("isSearch", true);
                    i.putExtra("text", query);
                    startActivity(i);
                    overridePendingTransition(0, 0);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                // On error, still open search list; ListFoodsActivity shows empty state if needed
                Intent i = new Intent(MainActivity.this, ListFoodsActivity.class);
                i.putExtra("isSearch", true);
                i.putExtra("text", query);
                startActivity(i);
                overridePendingTransition(0, 0);
            }
        });
    }


    private void initBestFood() {
        DatabaseReference myRef = database.getReference("Foods");
        binding.progressBarBestFood.setVisibility(View.VISIBLE);
        ArrayList<Foods> list = new ArrayList<>();
        Query query = myRef.orderByChild("BestFood").equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot issue: snapshot.getChildren()){
                        list.add(issue.getValue(Foods.class));
                    }
                    if(!list.isEmpty()){
                        binding.bestFoodView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL,false));
                        RecyclerView.Adapter<BestFoodsAdapter.viewholder> adapter = new BestFoodsAdapter(list);
                        binding.bestFoodView.setAdapter(adapter);
                    }
                    else {
                        binding.bestFoodView.setVisibility(View.GONE);
                    }
                    binding.progressBarBestFood.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void initCategory() {
        DatabaseReference myRef = database.getReference("Category");
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        ArrayList<Category> list = new ArrayList<>();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot issue: snapshot.getChildren()){
                        list.add(issue.getValue(Category.class));
                    }
                    if(!list.isEmpty()){
                        binding.categoryView.setLayoutManager(new GridLayoutManager(MainActivity.this,4));
                        RecyclerView.Adapter<CategoryAdapter.viewholder> adapter = new CategoryAdapter(list);
                        binding.categoryView.setAdapter(adapter);
                    }
                    else {
                        binding.categoryView.setVisibility(View.GONE);
                    }
                    binding.progressBarCategory.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initLocation() {
        DatabaseReference myRed = database.getReference("Location");
        ArrayList<Location> list = new ArrayList<>();
        myRed.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot issue: snapshot.getChildren()){
                        list.add(issue.getValue(Location.class));
                    }
                    ArrayAdapter<Location> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.sp_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.locationSp.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initPrice() {
        DatabaseReference myRed = database.getReference("Price");
        ArrayList<Price> list = new ArrayList<>();
        myRed.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot issue: snapshot.getChildren()){
                        list.add(issue.getValue(Price.class));
                    }
                    ArrayAdapter<Price> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.sp_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.priceSp.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initTime() {
        DatabaseReference myRed = database.getReference("Time");
        ArrayList<Time> list = new ArrayList<>();
        myRed.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot issue: snapshot.getChildren()){
                        list.add(issue.getValue(Time.class));
                    }
                    ArrayAdapter<Time> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.sp_item, list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.timeSp.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }






}