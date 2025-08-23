package com.example.foodorderapp.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.Adapter.FoodListAdapter;
import com.example.foodorderapp.Domain.Foods;
import com.example.foodorderapp.R;
import com.example.foodorderapp.databinding.ActivityListFoodsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListFoodsActivity extends BaseActivity {

    ActivityListFoodsBinding binding;
    private RecyclerView.Adapter adapterListFood;
    private int categoryId;
    private String categoryName;
    private String searchText;
    private Boolean isSearch;
    private Boolean isBest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListFoodsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getIntentExtra();
        initList();

    }


    private void initList() {
        DatabaseReference foodsRef = database.getReference("Foods");
        binding.progressBar.setVisibility(android.view.View.VISIBLE);
        ArrayList<Foods> list = new ArrayList<>();

        // SEARCH: client-side contains (case-insensitive), no redirects here
        if (Boolean.TRUE.equals(isSearch) && searchText != null && !searchText.trim().isEmpty()) {
            final String q = searchText.trim().toLowerCase();
            foodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    for (DataSnapshot d : snapshot.getChildren()) {
                        Foods f = d.getValue(Foods.class);
                        if (f != null && f.getTitle() != null &&
                                f.getTitle().toLowerCase().contains(q)) {
                            list.add(f);
                        }
                    }
                    String searchTitle = "search result for " + searchText;
                    binding.titleTxt.setText(searchTitle);
                    showList(list);
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {
                    binding.progressBar.setVisibility(android.view.View.GONE);
                }
            });
            return;
        }

        // BEST or CATEGORY
        Query query = Boolean.TRUE.equals(isBest)
                ? foodsRef.orderByChild("BestFood").equalTo(true)
                : foodsRef.orderByChild("CategoryId").equalTo(categoryId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot d : snapshot.getChildren()) {
                    Foods f = d.getValue(Foods.class);
                    if (f != null) list.add(f);
                }
                showList(list);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void showList(ArrayList<Foods> list) {
        if (binding.foodListView.getLayoutManager() == null) {
            binding.foodListView.setLayoutManager(new GridLayoutManager(this, 2));
        }
        adapterListFood = new FoodListAdapter(this, list);
        binding.foodListView.setAdapter(adapterListFood);
        binding.progressBar.setVisibility(android.view.View.GONE);
    }

    private void getIntentExtra() {
        categoryId = getIntent().getIntExtra("CategoryId", 0);
        categoryName = getIntent().getStringExtra("CategoryName");
        searchText   = getIntent().getStringExtra("text");
        isSearch     = getIntent().getBooleanExtra("isSearch", false);
        isBest       = getIntent().getBooleanExtra("isBest", false);

        if (!Boolean.TRUE.equals(isSearch) && categoryName != null) {
            binding.titleTxt.setText(categoryName);
        } else if (Boolean.TRUE.equals(isBest)) {
            binding.titleTxt.setText(getString(R.string.best_foods));
        }
        binding.backBtn.setOnClickListener(v -> finish());
    }
}