package com.example.foodorderapp.Helper;

import android.content.Context;
import android.widget.Toast;

import com.example.foodorderapp.Domain.Foods;

import java.util.ArrayList;

public class ManagementFavorite {

    private static final String KEY_FAV_LIST = "FavList";

    private Context context;
    private TinyDB tinyDB;

    public ManagementFavorite(Context context) {
        this.context = context;
        this.tinyDB = new TinyDB(context);
    }

    public void insertFood(Foods item) {
        ArrayList<Foods> list = getListFav();
        int idx = indexOfId(list, item.getId());
        if(idx >=  0){
            list.set(idx, item);
        } else {
            list.add(item);
        }
        tinyDB.putListObject(KEY_FAV_LIST,list);
        Toast.makeText(context, "Added to your Favorite", Toast.LENGTH_SHORT).show();
    }

    public void removeById(int id){
        ArrayList<Foods> list = getListFav();
        int idx = indexOfId(list, id);
        if(idx >= 0){
            list.remove(idx);
            tinyDB.putListObject(KEY_FAV_LIST, list);
            Toast.makeText(context, "Removed from Favorite", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isFavoriteById(int id){
        return indexOfId(getListFav(), id) >= 0;
    }

    public ArrayList<Foods> getListFav() {
        ArrayList<Foods> out = tinyDB.getListObject(KEY_FAV_LIST);
        return (out != null) ? out : new ArrayList<>();
    }

    private int indexOfId(ArrayList<Foods> list, int id){
        if(list == null) return -1;
        for(int i = 0 ; i < list.size(); i++){
            Foods f = list.get(i);
            if(f!=null && f.getId()==id) return i;
        }
        return -1;
    }
}
