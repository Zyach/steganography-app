package app.vit.imgtextsteganosoftware.activities.encrypt;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import app.vit.imgtextsteganosoftware.R;
import app.vit.imgtextsteganosoftware.activities.stego.StegoActivity;
import app.vit.imgtextsteganosoftware.utils.Constants;
import app.vit.imgtextsteganosoftware.utils.ImageUtils;
import app.vit.imgtextsteganosoftware.utils.StandardMethods;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EncryptImageActivity extends AppCompatActivity implements EncryptView {

  @BindView(R.id.etSecretMessage)
  EditText etSecretMessage;
  @BindView(R.id.ivCoverImage)
  ImageView ivCoverImage;
  @BindView(R.id.ivSecretImage)
  ImageView ivSecretImage;
  @BindView(R.id.tvCapacity)
  TextView tvCapacity;

  /*@BindView(R.id.rbText)
  RadioButton rbText;
  @BindView(R.id.rbImage)
  RadioButton rbImage;

  @OnCheckedChanged({R.id.rbText, R.id.rbImage})
  public void onRadioButtonClick() {
    if (rbImage.isChecked()) {
      etSecretMessage.setVisibility(View.GONE);
      ivSecretImage.setVisibility(View.VISIBLE);
      secretMessageType = Constants.TYPE_IMAGE;
    } else if (rbText.isChecked()) {
      etSecretMessage.setVisibility(View.VISIBLE);
      ivSecretImage.setVisibility(View.GONE);
      secretMessageType = Constants.TYPE_TEXT;
    }
  }
*/
  @OnClick({R.id.ivCoverImage, R.id.ivSecretImage})
  public void onCoverSecretImageClick(View view) {

    final CharSequence[] items = {
      getString(R.string.take_image_dialog),
      getString(R.string.select_image_dialog)
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(EncryptImageActivity.this);
    builder.setTitle(getString(R.string.select_image_title));
    builder.setCancelable(false);
    builder.setItems(items, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int item) {
        if (items[item].equals(getString(R.string.take_image_dialog))) {
          openCamera();
        } else if (items[item].equals(getString(R.string.select_image_dialog))) {
          chooseImage();
        }
      }
    });

    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
      }
    });

    if (view.getId() == R.id.ivCoverImage) {
      whichImage = Constants.COVER_IMAGE;
    } else if (view.getId() == R.id.ivSecretImage) {
      whichImage = Constants.SECRET_IMAGE;
    }

    builder.show();
  }

  @OnClick(R.id.bEncrypt)
  public void onButtonClick() {
    if (secretMessageType == Constants.TYPE_IMAGE) {
      mPresenter.encryptImage();
    } else if (secretMessageType == Constants.TYPE_TEXT) {
      String text = getSecretMessage();

      if (!text.isEmpty()) {
        mPresenter.encryptText();
      } else {
        showToast(R.string.secret_text_empty);
      }
    }
  }

  private ProgressDialog progressDialog;
  private EncryptPresenter mPresenter;
  private int whichImage = -1;
  private int secretMessageType = Constants.TYPE_IMAGE;
  private ActivityResultLauncher<PickVisualMediaRequest> coverPicker;
  private ActivityResultLauncher<PickVisualMediaRequest> secretPicker;
  private ActivityResultLauncher<Uri> cameraLauncher;
  private Uri pendingCameraUri;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_encrypt_image);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle("Encrypt Image into Image");

/*    ivSecretImage.setVisibility(View.GONE);
    secretMessageType = Constants.TYPE_TEXT;*/

    ButterKnife.bind(this);

    initPickers();

    //initToolbar();

    progressDialog = new ProgressDialog(EncryptImageActivity.this);
    progressDialog.setMessage("Please wait...");

    mPresenter = new EncryptPresenterImpl(this);

    SharedPreferences sp = getSharedPrefs();
    String filePath = sp.getString(Constants.PREF_COVER_PATH, "");
    boolean isCoverSet = sp.getBoolean(Constants.PREF_COVER_IS_SET, false);

    if (isCoverSet) {
      setCoverImage(new File(filePath));
    }
  }

/*  @Override
  public void initToolbar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setTitle("Encryption");
    }
  }*/

  @Override
  public void openCamera() {
    pendingCameraUri = createTempUri();
    cameraLauncher.launch(pendingCameraUri);
  }

  @Override
  public void chooseImage() {
    ActivityResultLauncher<PickVisualMediaRequest> launcher =
      whichImage == Constants.COVER_IMAGE ? coverPicker : secretPicker;
    launcher.launch(new PickVisualMediaRequest.Builder()
      .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
      .build());
  }

  private void initPickers() {
    coverPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
      if (uri != null) {
        handlePickedImage(uri, Constants.COVER_IMAGE);
      }
    });

    secretPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
      if (uri != null) {
        handlePickedImage(uri, Constants.SECRET_IMAGE);
      }
    });

    cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
      if (success && pendingCameraUri != null) {
        handlePickedImage(pendingCameraUri, whichImage);
      }
    });
  }

  private void handlePickedImage(Uri uri, int target) {
    try {
      int IMAGE_SIZE = 1500;
      if (target == Constants.SECRET_IMAGE) {
        IMAGE_SIZE = 150 + IMAGE_SIZE / 6;
      }
      Bitmap bitmap = ImageUtils.decodeUriToBitmap(this, uri, IMAGE_SIZE);
      File file = writeTempFile(bitmap, target == Constants.COVER_IMAGE ? "cover" : "secret");
      this.whichImage = target;
      mPresenter.selectImage(target, file.getAbsolutePath());
    } catch (IOException e) {
      e.printStackTrace();
      showToast(R.string.compress_error);
    }
  }

  private File writeTempFile(Bitmap bitmap, String prefix) throws IOException {
    File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    if (dir == null) dir = getCacheDir();
    File file = File.createTempFile(prefix + "_", ".png", dir);
    try (FileOutputStream fos = new FileOutputStream(file)) {
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
    }
    return file;
  }

  private Uri createTempUri() {
    File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    if (dir == null) {
      dir = getCacheDir();
    }
    File file = new File(dir, "captured_" + System.currentTimeMillis() + ".png");
    return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
  }

  @Override
  public void startStegoActivity(String filePath) {
    Intent intent = new Intent(EncryptImageActivity.this, StegoActivity.class);
    intent.putExtra(Constants.EXTRA_STEGO_IMAGE_PATH, filePath);
    startActivity(intent);
  }

  @Override
  public Bitmap getCoverImage() {
    return ((BitmapDrawable) ivCoverImage.getDrawable()).getBitmap();
  }

  @Override
  public void setCoverImage(File file) {
    showProgressDialog();
    Picasso.get()
      .load(file)
      .fit()
      .placeholder(R.drawable.ic_upload)
      .into(ivCoverImage);
    stopProgressDialog();
    whichImage = -1;

    SharedPreferences.Editor editor = getSharedPrefs().edit();
    editor.putString(Constants.PREF_COVER_PATH, file.getAbsolutePath());
    editor.putBoolean(Constants.PREF_COVER_IS_SET, true);
    editor.apply();

    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
    if (bitmap != null) {
      long capacity = ImageUtils.estimateCapacityBytes(bitmap);
      tvCapacity.setText(getString(R.string.capacity_label, ImageUtils.formatBytes(capacity)));
    }
  }

  @Override
  public Bitmap getSecretImage() {
    return ((BitmapDrawable) ivSecretImage.getDrawable()).getBitmap();
  }

  @Override
  public void setSecretImage(File file) {
    showProgressDialog();
    Picasso.get()
      .load(file)
      .fit()
      .placeholder(R.drawable.ic_upload)
      .into(ivSecretImage);
    stopProgressDialog();
    whichImage = -1;
  }

  @Override
  public String getSecretMessage() {
    return etSecretMessage.getText().toString().trim();
  }

  @Override
  public void setSecretMessage(String secretMessage) {
    etSecretMessage.setText(secretMessage);
  }

  @Override
  public void showToast(int message) {
    StandardMethods.showToast(this, message);
  }

  @Override
  public void showProgressDialog() {
    if (progressDialog != null && !progressDialog.isShowing()) {
      progressDialog.show();
    }
  }

  @Override
  public void stopProgressDialog() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  @Override
  public SharedPreferences getSharedPrefs() {
    return getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
  }
}
