package com.example.androidtodolist;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidtodolist.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements CustomAdapter.ItemClickListener {

    private ActivityMainBinding binding;
    CustomAdapter adapter;
    ArrayList<Task> tasks;
    ArrayList<Task> allTasks;
    Database database;
    static EditText search;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    static ArrayList<CategoriesModel> categoriesModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // BAZA DANYCH
        database = new Database(this);
        ////


        FloatingActionButton floatingActionButtonAdd = findViewById(R.id.fabAdd);
        floatingActionButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAddDialog();
            }
        });
        FloatingActionButton floatingActionButtonSet = findViewById(R.id.fabSet);
        floatingActionButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCategoriesDialog();
            }
        });
        FloatingActionButton floatingActionButtonCat = findViewById(R.id.fabCat);
        floatingActionButtonCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCategoriesDialog();
            }
        });
        search = findViewById(R.id.search_input);
        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                tasks = filterTasks(allTasks);
                setUpAdapter();

            }
        });

        TypedArray typedArray = getResources().obtainTypedArray(R.array.categories);

        for(int i = 0 ; i < typedArray.length() ; i++) {
            categoriesModels.add(new CategoriesModel(true,typedArray.getString(i)));
        }

        loadTasksFromDatabase();

        setUpAdapter();

    }

    public static ArrayList<Task> filterTasks(ArrayList<Task> arr)
    {
        ArrayList<Task> sortedList = new ArrayList<>();
        Collections.sort(arr,new CustomComparator());
        ArrayList<String> actualCategories = new ArrayList<>();
        for(CategoriesModel cm : categoriesModels)
        {
            if(cm.getSelected()) actualCategories.add(cm.getName());
        }


        for(Task t : arr)
        {

            if(t.getTitle().toLowerCase(Locale.ROOT).contains(search.getText().toString().toLowerCase(Locale.ROOT)) )
            {
                if(!actualCategories.contains(t.getCategory())) continue;
                if(true == t.getDone()) continue;
                sortedList.add(t);
            }
        }

        return sortedList;
    }


    public void loadTasksFromDatabase()
    {
        try
        {
            allTasks = database.getTasks();
            tasks = filterTasks(allTasks);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        setUpAdapter();
    }

    public void setUpAdapter()
    {
        if (tasks.isEmpty()) {

            TextView emptyList = findViewById(R.id.empty_list);
            emptyList.setVisibility(View.VISIBLE);

        } else {

            TextView emptyList = findViewById(R.id.empty_list);
            emptyList.setVisibility(View.GONE);
        }

        RecyclerView rv = findViewById(R.id.MyRecycleViewer);
        rv.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager lm = new LinearLayoutManager(MainActivity.this);
        rv.setLayoutManager(lm);

        adapter = new CustomAdapter(tasks, MainActivity.this);

        rv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view2 = view.findViewById(R.id.expand_view);
                if(view2.getVisibility() != View.VISIBLE)
                {
                    view2.setVisibility(View.VISIBLE);
                }
                else
                {
                    view2.setVisibility(View.GONE);
                }
            }
        });

        rv.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createEditDialog(Task oldTask)
    {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.layout_list_item_edit);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        EditText title = dialog.findViewById(R.id.titleTextInput);
        EditText  description = dialog.findViewById(R.id.descriptionTextInput);
        Spinner spinner = dialog.findViewById(R.id.categorySpinner);
        DatePicker datePicker =  dialog.findViewById(R.id.calendarView);
        Button button = dialog.findViewById(R.id.dialogSaveButton);
        TimePicker timePicker = dialog.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        title.setText(oldTask.getTitle());
        description.setText(oldTask.getDescription());

        String oldDate = oldTask.getToDoDate();
        String[] oldDateInfo = oldDate.split("\\s|:|-|/");
        datePicker.updateDate(Integer.parseInt(oldDateInfo[4]),Integer.parseInt(oldDateInfo[3])-1,Integer.parseInt(oldDateInfo[2]));
        timePicker.setHour(Integer.parseInt(oldDateInfo[0]));
        timePicker.setMinute(Integer.parseInt(oldDateInfo[1]));

        TypedArray typedArray = getResources().obtainTypedArray(R.array.categories);

        for(int i = 0 ; i < typedArray.length() ; i++) {
            if(oldTask.getCategory().equals(typedArray.getString(i)))
            {
                spinner.setSelection(i);
                break;
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(title != null)
                {
                    try {
                        Task task = new Task();
                        task.setTitle(title.getText().toString());
                        task.setDescription(description.getText().toString());
                        task.setCreated(new Date());
                        task.setDone(false);
                        task.setNotificationStatus(false);
                        task.setHidden(false);
                        task.setId(oldTask.getId());
                        task.setCategory(spinner.getSelectedItem().toString());
                        String toDoDate = timePicker.getHour()
                                +":"+timePicker.getMinute()
                                +" "+datePicker.getDayOfMonth()
                                +"/"+(datePicker.getMonth()+1)
                                +"/"+datePicker.getYear();
                        task.setToDoDate(sdf.parse(toDoDate));


                        database.editTask(task);

                        loadTasksFromDatabase();

                        dialog.dismiss();

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public void createAddDialog()
    {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.layout_list_item_edit);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        EditText title = dialog.findViewById(R.id.titleTextInput);
        EditText  description = dialog.findViewById(R.id.descriptionTextInput);
        Spinner spinner = dialog.findViewById(R.id.categorySpinner);
        DatePicker datePicker =  dialog.findViewById(R.id.calendarView);
        Button button = dialog.findViewById(R.id.dialogSaveButton);
        TimePicker timePicker = dialog.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);


        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {


                if(title != null)
                {

                    try {
                        Task task = new Task();
                        task.setTitle(title.getText().toString());
                        task.setDescription(description.getText().toString());
                        task.setCreated(new Date());
                        task.setDone(false);
                        task.setNotificationStatus(false);
                        task.setHidden(false);
                        task.setCategory(spinner.getSelectedItem().toString());
                        String toDoDate = timePicker.getHour()
                                +":"+timePicker.getMinute()
                                +" "+datePicker.getDayOfMonth()
                                +"/"+(datePicker.getMonth()+1)
                                +"/"+datePicker.getYear();
                        task.setToDoDate(sdf.parse(toDoDate));

                        database.addTask(task);

                        loadTasksFromDatabase();

                        dialog.dismiss();


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public void createCategoriesDialog()
    {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.layout_list_of_categories);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        CategoriesAdapter adapter = new CategoriesAdapter(this,categoriesModels,MainActivity.this);

        ListView listView = dialog.findViewById(R.id.listViewOfCategories);
        listView.setAdapter(adapter);

        dialog.show();
        dialog.getWindow().setAttributes(lp);


    }

    public void createSettingsDialog()
    {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.layout_list_of_categories);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        CategoriesAdapter adapter = new CategoriesAdapter(this,categoriesModels,MainActivity.this);

        ListView listView = dialog.findViewById(R.id.listViewOfCategories);
        listView.setAdapter(adapter);

        dialog.show();
        dialog.getWindow().setAttributes(lp);


    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onItemClick(View v, int position) {

        switch (v.getId())
        {
            case R.id.deleteButton:
                database.deleteTask(tasks.get(position));
                tasks.remove(position);
                adapter.notifyItemRemoved(position);
                if(tasks.isEmpty())
                {
                    System.out.println("NIE MA TASKÓW WYSWIETL TO GDZIEś :)");
                }
                break;

            case R.id.cv:
                LinearLayout linearLayout = v.findViewById(R.id.expand_view);
                TextView textView = v.findViewById(R.id.notExpandedTaskDateInput);
                if(linearLayout.getVisibility() != View.VISIBLE)
                {
                    textView.setTextColor(getResources().getColor(R.color.white));
                    linearLayout.setVisibility(View.VISIBLE);
                }
                else
                {
                    textView.setTextColor(getResources().getColor(R.color.greyish));
                    linearLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.editButton:
                createEditDialog(tasks.get(position));
                break;
            case R.id.cb:
                categoriesModels.get(position).changeSelected();
                tasks = filterTasks(allTasks);
                setUpAdapter();
        }
    }




}