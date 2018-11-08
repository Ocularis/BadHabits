package no.hiof.andrekar.badhabits;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

//TODO: Handle merging if account exits and data is present for anon user in database?

public class SettingsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private Button buttonRegUser;
    private TextView userIdTW;

    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

         buttonRegUser = findViewById(R.id.buttonRegisterUser);
         userIdTW = findViewById(R.id.userIdTextView);

         // Button to register the user
        buttonRegUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().enableAnonymousUsersAutoUpgrade().setAvailableProviders(providers).build(), 200);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null) {
            if (currentUser.isAnonymous()) {
                userIdTW.setText("Du er ikke logget inn, data er ikke lagret til en spesifikk konto");
                buttonRegUser.setVisibility(View.VISIBLE);
            } else {
                userIdTW.setText("Du er logget inn som " + currentUser.getEmail());
                buttonRegUser.setVisibility(View.INVISIBLE);
            }
        } else {
            userIdTW.setText("Error: sign in failed.");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null) {
                    userIdTW.setText("Du er logget inn som " + FirebaseAuth.getInstance().getCurrentUser().getEmail());
                } else {
                    userIdTW.setText("Error: sign in failed.");
                }
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
}