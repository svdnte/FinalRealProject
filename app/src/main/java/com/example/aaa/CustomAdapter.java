package com.example.aaa;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CustomAdapter extends BaseAdapter {
    Context context;
    Object[] userArrayList;
    List<Integer> selectedItems = new LinkedList<>();
    LayoutInflater layoutInflater;
    String sort = "По алфавиту";
    boolean direction = false; // t - возрастанию, f - убыванию

    public CustomAdapter(Context appContext, Object[] arrayList) {
        this.context = appContext;
        setArrayMyData(arrayList);
        layoutInflater = (LayoutInflater.from(appContext));
    }

    @Override
    public int getCount() {
        return userArrayList.length;
    }

    @Override
    public User getItem(int position) {
        return (User) userArrayList[position];
    }

    @Override
    public long getItemId(int position) {
        Object us = userArrayList[position];
        if (us != null) {
            return ((User)us).getId();
        }
        return 0;
    }

    public void setArrayMyData(Object[] arrayList) {
        this.userArrayList = arrayList;
        sortThis();
    }

    private void sortThis(){
        Arrays.sort(userArrayList, new Sorter(sort, direction));
        if (!direction) {
            reverse(userArrayList);
        }
        this.notifyDataSetChanged();
    }

    public void setSort(String sort, boolean direction) {
        this.sort = sort;
        this.direction = direction;
        sortThis();
    }

    public void set_direction(boolean d) {
        direction = d;
        sortThis();
    }

    public int getLength() {
        return userArrayList.length;
    }

    public int getSum() {
        int sum = 0;
        for (Object u: userArrayList){
            sum += ((User)u).getSum();
        }
        return sum;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.activity_list_view, null);
        }
        User us = (User) userArrayList[position];
        if (selectedItems.contains((int) us.getId())) {
            view.setBackgroundColor(context.getColor(R.color.selected_item_background));
        } else {
            view.setBackgroundColor(context.getColor(com.google.android.material.R.color.design_default_color_surface));
        }
        String name;
        if (us.getAnon() == 1) {
            name = "Аноним";
        } else {
            name = us.getSurname() + " " + us.getName() + " " + us.getOtch();
        }
        ((TextView) view.findViewById(R.id.name_text_view)).setText(name);

        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(us.getDate());

        ((TextView) view.findViewById(R.id.date_text_view)).setText(date);

        ((TextView) view.findViewById(R.id.sum_text_view)).setText("" + us.getSum() + " p.");

        return view;
    }

    private void reverse(Object[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            Object temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }

    public List<Integer> getSelectedItems(){
        return selectedItems;
    }

    public int getSelectedCount(){
        return selectedItems.size();
    }

    public void setSelected(long id) {
        if (!selectedItems.contains((int) id)) {
            selectedItems.add((int) id);
        } else {
            selectedItems.remove((Integer) (int) id);
        }

    }
}


class Sorter implements Comparator<Object> {
    String sortMethod;
    boolean direction;

    Sorter(String sortMethod, boolean direction) {
        this.sortMethod = sortMethod;
        this.direction = direction;
    }

    @Override
    public int compare(Object o1, Object o2) {
        User us1 = (User) o1, us2 = (User) o2;
        if (Objects.equals(sortMethod, "По алфавиту")){
            Log.w("SOOOOORT", "ALPH");
            String s1 = us1.getSurname().toLowerCase() + " " + us1.getName().toLowerCase() + " " + us1.getOtch().toLowerCase();
            String s2 = us2.getSurname().toLowerCase() + " " + us2.getName().toLowerCase() + " " + us2.getOtch().toLowerCase();

            int minLength = Math.min(s1.length(), s2.length());

            for (int i = 0; i < minLength; i++) {
                char c1 = s1.charAt(i);
                char c2 = s2.charAt(i);

                if (c1 != c2) {
                    return c1 - c2;
                }
            }
            return s1.length() - s2.length();
        } else if (Objects.equals(sortMethod, "По дате")) {
            Log.w("SOOOOORT", "DATE");
            return (int) (us1.getDate() - us2.getDate());
        } else {
            return us1.getSum() - us2.getSum();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}