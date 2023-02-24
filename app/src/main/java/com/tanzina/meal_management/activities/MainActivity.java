package com.tanzina.meal_management.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tanzina.meal_management.R;
import com.tanzina.meal_management.model.AddMess;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private LinearLayout addlinearLayout, enterNameLinerLayout;
    private ProgressBar progressBar;
    private Button creteMessButton, enterNameButton, joinMessWithMessIdButton;
    private EditText userNameEditText, messIdEditText, userPhoneEditText;
    private String newMessId;
    private boolean pressBackButton = false;
    int a = 0, total_process;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        progressBar = findViewById(R.id.progressBar);
        addlinearLayout = findViewById(R.id.addLinerLayout);
        addlinearLayout.setVisibility(View.INVISIBLE);
        creteMessButton = findViewById(R.id.creteMessButton);
        creteMessButton.setOnClickListener(this);
        userNameEditText = findViewById(R.id.userNameEditText);
        userPhoneEditText = findViewById(R.id.userPhoneEditText);
        enterNameButton = findViewById(R.id.enterNameButton);
        enterNameButton.setOnClickListener(this);
        enterNameLinerLayout = findViewById(R.id.enterNameLinerLayout);
        enterNameLinerLayout.setVisibility(View.INVISIBLE);
        messIdEditText = findViewById(R.id.messIdEditText);
        joinMessWithMessIdButton = findViewById(R.id.joinMessWithMessIdButton);
        joinMessWithMessIdButton.setOnClickListener(this);

        readMessHaveOrNot();
    }

    private void readMessHaveOrNot() {
        databaseReference.child("user").child(auth.getUid()).child("user_name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    enterNameLinerLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    enterNameLinerLayout.setVisibility(View.INVISIBLE);
                    databaseReference.child("user").child(auth.getUid()).child("mess_id").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                progressBar.setVisibility(View.INVISIBLE);
                                addlinearLayout.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            onBackPressed();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onBackPressed();
            }
        });
    }


    private void restart() {
        databaseReference.child("user").child(auth.getUid()).child("mess_id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    readMessHaveOrNot();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == creteMessButton) {
            createMess(getSaltString());
        } else if (view == enterNameButton) {
            String name = userNameEditText.getText().toString();
            String phone = userPhoneEditText.getText().toString();
            if (name.isEmpty()) {
                userNameEditText.setError("Enter your name");
                userNameEditText.requestFocus();
            } else if (!validName(name)) {
                userNameEditText.setError("Enter valid name");
                userNameEditText.requestFocus();
            } else if (phone.isEmpty()) {
                userPhoneEditText.setError("Enter your phone number");
                userPhoneEditText.requestFocus();
            } else if (!validateNumber(phone)) {
                userPhoneEditText.setError("Enter valid phone number");
                userPhoneEditText.requestFocus();
            } else {
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("user_name", name);
                childUpdates.put("user_phone", phone);

                databaseReference.child("user").child(auth.getUid()).setValue(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Name added successfully", Toast.LENGTH_SHORT).show();
                            restart();
                        }
                    }
                });
            }
        } else if (view == joinMessWithMessIdButton) {
            final String messId = messIdEditText.getText().toString();

            if (messId.isEmpty()) {
                messIdEditText.setError("Enter an ID");
                messIdEditText.requestFocus();
            } else {
                databaseReference.child("total_mess_id").child(messId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            messIdEditText.setError("Wrong ID");
                            messIdEditText.requestFocus();
                        } else {
                            databaseReference.child("user").child(Objects.requireNonNull(auth.getUid())).child("mess_id").setValue(messId);
                            databaseReference.child("mess").child(messId).child("total_members").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    int total_members = dataSnapshot.getValue(Integer.class);
                                    total_process++;
                                    if (total_process == 1) {
                                        Toast.makeText(getApplicationContext(), String.valueOf(total_members), Toast.LENGTH_SHORT).show();
                                        int a = total_members + 1;
                                        databaseReference.child("mess").child(messIdEditText.getText().toString()).child("total_members").setValue(a);
                                        databaseReference.child("mess").child(messIdEditText.getText().toString()).child("members").child("user" + a).setValue(auth.getUid());
                                        finish();
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
            }
        }
    }

    private void createMess(final String messId) {
        databaseReference.child("total_mess_id").child(messId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() && a == 0) {
                    AddMess addMess = new AddMess(auth.getUid(), 0, 0, 1);
                    databaseReference.child("mess").child(messId).setValue(addMess).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            databaseReference.child("user").child(auth.getUid()).child("mess_id").setValue(messId).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        databaseReference.child("total_mess_id").child(messId).setValue(messId).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful() && a == 0) {
                                                    databaseReference.child("mess").child(messId).child("members").child("user1").setValue(auth.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            a = a + 1;
                                                            finish();
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });


                } else if (dataSnapshot.exists() && a == 1) {
                    createMess(getSaltString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected String getSaltString() {
        String SALTCHARS = "abcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 5) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    private boolean validName(String name) {
        String m = "^[A-Za-z_][A-Za-z0-9_]{3,29}$";
        Pattern pattern = Pattern.compile(m, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);

        return matcher.find();
    }

    public boolean validateNumber(String mobileNumber) {
        return mobileNumber.matches("^(?:\\+88|88)?(01[3-9]\\d{8})$");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        finish();
    }
}
