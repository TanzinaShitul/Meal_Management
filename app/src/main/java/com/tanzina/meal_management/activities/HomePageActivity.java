package com.tanzina.meal_management.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tanzina.meal_management.R;
import com.tanzina.meal_management.adapter.ViewHolder;
import com.tanzina.meal_management.model.Cost;
import com.tanzina.meal_management.model.TempMeal;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "HomePageActivity";
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private TextView totalCost, mealRate, timeTextView, textView, profileName, mail,phoneNumber;
    private String messId, adminId, todayString, previousDate, previousDate2, userName[], meal[];
    private Button addButtonAdmin, meadListButton, debitMoney, costButton;
    private int totalMeal = 0, count = 0, messMembers, countM = 0, totalCostInt = 0;
    private boolean saveValue = false;
    private RecyclerView dailyMealListRecyclerView;
    private Toolbar toolbar;
    private LinearLayout linearLayout10;


    @SuppressLint({"ResourceAsColor", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        toolbar = findViewById(R.id.toolBarId);
        setSupportActionBar(toolbar);
        toolbar.setSubtitleTextColor(R.color.color1);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new SlidingRootNavBuilder(this).withDragDistance(200)
                .withRootViewScale(0.7f)
                .withRootViewElevation(7)
                .withRootViewYTranslation(10)
                .withToolbarMenuToggle(toolbar).withMenuOpened(false).withMenuLayout(R.layout.menu_drawer).inject();

        userName = new String[100];
        meal = new String[1000];
        Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date mydate = new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24));
        Date mydate2 = new Date(System.currentTimeMillis() - (2000 * 120 * 120 * 48));
        todayString = formatter.format(todayDate);
        previousDate = formatter.format(mydate);
        previousDate2 = formatter.format(mydate);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        totalCost = findViewById(R.id.totalCost);
        mealRate = findViewById(R.id.mealRate);
        addButtonAdmin = findViewById(R.id.addButtonAdmin);
        addButtonAdmin.setVisibility(View.INVISIBLE);
        addButtonAdmin.setOnClickListener(this);
        meadListButton = findViewById(R.id.meadListButton);
        meadListButton.setOnClickListener(this);
        timeTextView = findViewById(R.id.timeTextView);
        timeTextView.setText(todayString);
        dailyMealListRecyclerView = findViewById(R.id.dailyMealListRecyclerView);
        dailyMealListRecyclerView.setHasFixedSize(true);
        dailyMealListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        debitMoney = findViewById(R.id.debitMoney);
        debitMoney.setOnClickListener(this);
        costButton = findViewById(R.id.costButton);
        costButton.setOnClickListener(this);
        profileName = findViewById(R.id.txtUserName);
        phoneNumber = findViewById(R.id.phoneNumber);
        mail = findViewById(R.id.txtMail);
        textView = findViewById(R.id.messId);
        linearLayout10 = findViewById(R.id.linearLayout10);
        linearLayout10.setOnClickListener(this);
        setUserName(auth.getUid());
    }

    private void readAllMeals() {
        databaseReference.child("mess").child(messId).child("meal_list").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    int a = snapshot.getValue(Integer.class);
                    meal[countM] = String.valueOf(a);
                    countM = countM + 1;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readAllUsersName() {

        databaseReference.child("mess").child(messId).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    String userId = snap.getValue(String.class);

                    databaseReference.child("user").child(userId).child("user_name").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String name = dataSnapshot.getValue(String.class);
                            userName[count] = name;
                            count = count + 1;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setViewHolder(String Id) {
        FirebaseRecyclerAdapter<TempMeal, ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<TempMeal, ViewHolder>(TempMeal.class, R.layout.daily_meal_layout, ViewHolder.class, databaseReference.child("mess").child(Id).child(todayString + "temp_meal")) {
            @Override
            protected void populateViewHolder(ViewHolder viewHolder, TempMeal tempMeal, int i) {
                viewHolder.setDetails(getApplication(), tempMeal.m1, tempMeal.getM2(), tempMeal.m3, tempMeal.uid);
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);

                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        TextView userId = view.findViewById(R.id.userId);

                        String uID = userId.getText().toString();

                        if (uID.equals(auth.getUid())) {
                            Intent i = new Intent(getApplicationContext(), EditMyMealActivity.class);
                            i.putExtra("uid", uID);
                            i.putExtra("date", todayString);
                            i.putExtra("mess", messId);
                            startActivity(i);
                        } else
                            Toast.makeText(getApplicationContext(), "You just edit your own meal", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onItenLingClick(View view, int position) {

                    }
                });

                return viewHolder;
            }
        };
        dailyMealListRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onClick(View view) {
        if (view == addButtonAdmin) {
            databaseReference.child("mess").child(messId).child("temp_meal_exist").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        databaseReference.child("mess").child(messId).child("members").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String currentUserId = snapshot.getValue(String.class);
                                    TempMeal tempMeal = new TempMeal(0, 0, 0, currentUserId);

                                    if (dataSnapshot.exists()) {
                                        databaseReference.child("mess").child(messId).child(todayString + "temp_meal").child(currentUserId).setValue(tempMeal);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        databaseReference.child("mess").child(messId).child("temp_meal_exist").setValue(todayString);
                    } else if (dataSnapshot.exists()) {
                        databaseReference.child("mess").child(messId).child("temp_meal_exist").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue(String.class).equals(todayString)) {
                                    Toast.makeText(getApplicationContext(), "Day not finished", Toast.LENGTH_SHORT).show();
                                } else if (!dataSnapshot.getValue(String.class).equals(todayString)) {
                                    databaseReference.child("mess").child(messId).child(previousDate + "temp_meal").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                final TempMeal tempMeal = snapshot.getValue(TempMeal.class);
                                                databaseReference.child("mess").child(messId).child("meal_list").child(previousDate + tempMeal.uid).setValue(tempMeal.getM1() + tempMeal.getM2() + tempMeal.getM3());
                                            }
                                            databaseReference.child("mess").child(messId).child("members").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        String currentUserId = snapshot.getValue(String.class);
                                                        TempMeal tempMeal = new TempMeal(0, 0, 0, currentUserId);

                                                        if (dataSnapshot.exists()) {
                                                            databaseReference.child("mess").child(messId).child(todayString + "temp_meal").child(currentUserId).setValue(tempMeal);
                                                        }
                                                    }
                                                    databaseReference.child("mess").child(messId).child(previousDate2 + "temp_meal").addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.exists()) {
                                                                databaseReference.child("mess").child(messId).child(previousDate2 + "temp_meal").setValue(null);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            databaseReference.child("mess").child(messId).child("temp_meal_exist").setValue(todayString);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else if (view == meadListButton) {

            Intent i = new Intent(getApplicationContext(), TotalMealActivity.class);
            i.putExtra("totalMeal", totalMeal);
            i.putExtra("messId", messId);
            i.putExtra("members", messMembers);
            i.putExtra("membersName", userName);
            i.putExtra("meal", meal);
            startActivity(i);
            finish();

        } else if (view == debitMoney) {
            Intent i = new Intent(getApplicationContext(), DebitMoneyActivity.class);
            i.putExtra("admin", adminId);
            i.putExtra("messId", messId);
            startActivity(i);
            finish();
        } else if (view == costButton) {
            Intent i = new Intent(getApplicationContext(), CostActivity.class);
            i.putExtra("totalMeal", totalMeal);
            i.putExtra("messId", messId);
            i.putExtra("members", messMembers);
            i.putExtra("membersName", userName);
            i.putExtra("meal", meal);
            startActivity(i);
            finish();
        } else if (view == linearLayout10) {
            auth.signOut();
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        totalCostInt = 0;
        totalMeal = 0;

        databaseReference.child("user").child(auth.getUid()).child("mess_id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messId = dataSnapshot.getValue(String.class);
                textView.setText(messId);
                readAllUsersName();
                readAllMeals();
                if (dataSnapshot.exists()) {
                    databaseReference.child("mess").child(messId).child("admin").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            adminId = dataSnapshot.getValue(String.class);
                            if (adminId.equals(auth.getUid())) {
                                addButtonAdmin.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    setViewHolder(messId);

                    databaseReference.child("mess").child(messId).child("meal_list").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                totalMeal = totalMeal + snapshot.getValue(Integer.class);
                            }

                            databaseReference.child("mess").child(messId).child("cost").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Cost cost = snapshot.getValue(Cost.class);
                                        totalCostInt = totalCostInt + cost.getTaka();
                                    }
                                    Double av = Double.valueOf(totalCostInt) / Double.valueOf(totalMeal);
                                    if (av.isNaN()) {
                                        mealRate.setText("0");
                                    } else {
                                        mealRate.setText(String.valueOf(new DecimalFormat("##.##").format(av)));
                                    }
                                    totalCost.setText(String.valueOf(totalCostInt));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
                databaseReference.child("mess").child(messId).child("meal_rate").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int a = dataSnapshot.getValue(Integer.class);
                        mealRate.setText(String.valueOf(a));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUserName(String uid) {
        databaseReference.child("user").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String childKey = childSnapshot.getKey();
                    Object childValue = childSnapshot.getValue();
                    if (childKey.equalsIgnoreCase("user_name")){
                        profileName.setText(childValue.toString());
                    }else if (childKey.equalsIgnoreCase("user_phone")){
                        phoneNumber.setText(childValue.toString());
                    }
                }
                mail.setText(auth.getCurrentUser().getEmail());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
