package com.example.garage_app.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.garage_app.R;
import com.example.garage_app.repository.UserRepository;
import com.example.garage_app.ui.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ShapeableImageView profileImage;
    private TextView usernameTextView, emailTextView;
    private MaterialButton changePasswordButton, logoutButton;
    private Uri imageUri;
    private String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        usernameTextView = view.findViewById(R.id.username_text_view);
        emailTextView = view.findViewById(R.id.email_text_view);
        changePasswordButton = view.findViewById(R.id.change_password_button);
        logoutButton = view.findViewById(R.id.logout_button);

        loadUserProfile();

        profileImage.setOnClickListener(v -> openImageChooser());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (userEmail != null) {
            UserRepository.getUserByEmail(userEmail, user -> {
                if(user != null) {
                    usernameTextView.setText(user.getUsername());
                    emailTextView.setText(user.getMail());
                    loadUserProfileImage(firebaseUser.getUid());
                }
            });
        }
    }

    private void loadUserProfileImage(String userId) {
        Bitmap savedImage = loadImageForUser(userId);
        if (savedImage != null) {
            Glide.with(this)
                    .load(savedImage)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(profileImage);
        } else {
            // Set default placeholder if no image exists
            Glide.with(this)
                    .load(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(profileImage);
        }
    }

    private void saveImageForUser(Bitmap bitmap, String userId) {
        try {
            FileOutputStream fos = requireContext().openFileOutput("profile_" + userId + ".jpg", Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap loadImageForUser(String userId) {
        try {
            FileInputStream fis = requireContext().openFileInput("profile_" + userId + ".jpg");
            return BitmapFactory.decodeStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selectează o imagine"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadProfileImage();
        }
    }

    private void uploadProfileImage() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (imageUri != null && auth.getCurrentUser() != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                saveImageForUser(bitmap, auth.getCurrentUser().getUid());

                Glide.with(this)
                        .load(bitmap)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .circleCrop()
                        .into(profileImage);

                Toast.makeText(getContext(), "Imaginea de profil a fost salvată", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Eroare la procesarea imaginii", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showChangePasswordDialog() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null && user.getEmail() != null) {
            auth.sendPasswordResetEmail(user.getEmail())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Schimbare parolă")
                                    .setMessage("Am trimis un link de resetare a parolei la adresa ta de email.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        } else {
                            Toast.makeText(getContext(), "Eroare la trimiterea email-ului", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmare deconectare")
                .setMessage("Sigur doriți să vă deconectați?")
                .setPositiveButton("Da", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    SharedPreferences prefs = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("is_logged_in", false).apply();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    requireActivity().finish();
                })
                .setNegativeButton("Nu", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}