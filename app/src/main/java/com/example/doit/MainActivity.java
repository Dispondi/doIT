package com.example.doit;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.doit.databinding.ActivityMainBinding;
import com.example.doit.databinding.FragmentNoteListBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = mainBinding.getRoot();
        setContentView(view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // cause first fragment is logging
        BottomNavigationView bottomNavigationView = mainBinding.bottomNavigation;
        // bottomNavigationView.setVisibility(View.GONE);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        navController.addOnDestinationChangedListener(
                new NavController.OnDestinationChangedListener() {
                    @Override
                    public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {
                        boolean showBottomNav = false;
                        if (bundle != null) {
                            showBottomNav = bundle.getBoolean("ShowAppBar", false);
                        }
                        if(showBottomNav) {
                            bottomNavigationView.setVisibility(View.VISIBLE);
                        } else {
                            bottomNavigationView.setVisibility(View.GONE);
                        }
                    }
                }
        );

//        NavController navController = Navigation.findNavController(view);
//        navController.addOnDestinationChangedListener((navController1, navDestination, bundle) -> {
//            if (navDestination.getId() == R.id.loginFragment || navDestination.getId() == R.id.registrationFragment) {
//                bottomNavigationView.setVisibility(View.GONE);
//            } else bottomNavigationView.setVisibility(View.VISIBLE);
//        });

    }
}