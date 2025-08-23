package com.example.foodorderapp.Activity;

import android.os.Bundle;
import android.view.View;

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
        binding.progressBar.setVisibility(View.VISIBLE);

        // SEARCH PATH: first try to match category name, else title-contains
        if (Boolean.TRUE.equals(isSearch) && searchText != null && !searchText.trim().isEmpty()) {
            final String q = searchText.trim().toLowerCase();

            // 1) Load categories to find a matching category name (substring, case-insensitive)
            database.getReference("Category").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer matchedCatId = null;
                    String matchedCatName = null;

                    for (DataSnapshot d : snapshot.getChildren()) {
                        Integer id = d.child("Id").getValue(Integer.class);
                        String name = d.child("Name").getValue(String.class);
                        if (id != null && name != null && name.toLowerCase().contains(q)) {
                            matchedCatId = id;
                            matchedCatName = name;
                            break;
                        }
                    }

                    if (matchedCatId != null) {
                        // 2) Category name matched -> show all foods in that category
                        binding.titleTxt.setText(matchedCatName);
                        Query byCategory = foodsRef.orderByChild("CategoryId").equalTo(matchedCatId);
                        loadFoods(byCategory);
                    } else {
                        // 3) No category match -> fetch all foods and filter by title contains
                        foodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ArrayList<Foods> list = new ArrayList<>();
                                for (DataSnapshot d : snapshot.getChildren()) {
                                    Foods f = d.getValue(Foods.class);
                                    if (f == null) continue;
                                    String title = f.getTitle();
                                    if (title != null && title.toLowerCase().contains(q)) {
                                        list.add(f);
                                    }
                                }
                                String searchResultTitle = getString(R.string.search_result_for) + " " + searchText;
                                binding.titleTxt.setText(searchResultTitle);
                                showList(list);
                            }

                            @Override public void onCancelled(@NonNull DatabaseError error) {
                                binding.progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {
                    binding.progressBar.setVisibility(View.GONE);
                }
            });
            return;
        }

        // NON-SEARCH PATHS (unchanged behavior)
        ArrayList<Foods> list = new ArrayList<>();
        Query query;
        if (Boolean.TRUE.equals(isBest)) {
            query = foodsRef.orderByChild("BestFood").equalTo(true);
            binding.titleTxt.setText(getString(R.string.best_foods));
        } else {
            query = foodsRef.orderByChild("CategoryId").equalTo(categoryId);
            if (categoryName != null) binding.titleTxt.setText(categoryName);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot issue : snapshot.getChildren()) {
                    Foods f = issue.getValue(Foods.class);
                    if (f != null) list.add(f);
                }
                showList(list);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void loadFoods(Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Foods> list = new ArrayList<>();
                for (DataSnapshot d : snapshot.getChildren()) {
                    Foods f = d.getValue(Foods.class);
                    if (f != null) list.add(f);
                }
                showList(list);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showList(ArrayList<Foods> list) {
        if (binding.foodListView.getLayoutManager() == null) {
            binding.foodListView.setLayoutManager(new GridLayoutManager(this, 2));
        }
        adapterListFood = new FoodListAdapter(this, list);
        binding.foodListView.setAdapter(adapterListFood);

        // Optional: empty state
        // binding.emptyTxt.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        binding.progressBar.setVisibility(View.GONE);
    }

    private void getIntentExtra() {
        categoryId = getIntent().getIntExtra("CategoryId", 0);
        categoryName = getIntent().getStringExtra("CategoryName");
        searchText   = getIntent().getStringExtra("text");
        isSearch     = getIntent().getBooleanExtra("isSearch", false);
        isBest       = getIntent().getBooleanExtra("isBest", false);

        if (!Boolean.TRUE.equals(isSearch) && categoryName != null) {
            binding.titleTxt.setText(categoryName);
        }
        binding.backBtn.setOnClickListener(v -> finish());
    }
}