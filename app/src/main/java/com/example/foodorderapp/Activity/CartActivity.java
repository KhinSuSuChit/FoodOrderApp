package com.example.foodorderapp.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.Adapter.CartAdapter;
import com.example.foodorderapp.Helper.ChangeNumberItemsListener;
import com.example.foodorderapp.Helper.ManagmentCart;
import com.example.foodorderapp.R;
import com.example.foodorderapp.databinding.ActivityCartBinding;

public class CartActivity extends BaseActivity {

    private ActivityCartBinding binding;
    private RecyclerView.Adapter adapter;
    private ManagmentCart managmentCart;
    private double tax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        managmentCart = new ManagmentCart(this);

        setVariable();
        calculateCart();
        initList();
    }

    private void initList() {
        if(managmentCart.getListCart().isEmpty()){
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollviewCart.setVisibility(View.GONE);
        }else{
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollviewCart.setVisibility(View.VISIBLE);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        binding.cardView.setLayoutManager(linearLayoutManager);
        adapter = new CartAdapter(managmentCart.getListCart(), this, () -> calculateCart());
        binding.cardView.setAdapter(adapter);
    }

    private void calculateCart() {
        double percentTax = 0.02; //percent 2% tax
        double delivery = 10; //10$

        tax = Math.round(managmentCart.getTotalFee()*percentTax*100.0) / 100;

        double total = Math.round((managmentCart.getTotalFee()+tax+delivery) * 100) / 100;
        double itemTotal = Math.round(managmentCart.getTotalFee()*100) / 100;

        binding.totalFeeTxt.setText("$"+ itemTotal);
        binding.taxTxt.setText("$" + tax);
        binding.deliveryTxt.setText("$"+ delivery);
        binding.totalTxt.setText("$" + total);
    }

    private void setVariable() {

        binding.backBtn.setOnClickListener(v -> finish());
    }
}