package thanhdnh.ueh.edu.article_app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ViewUserActivity extends AppCompatActivity {
  ImageView iv_detail;
  View svDetailContent;
  View llDetailProgress;
  ProgressBar progressBarDetail;
  TextView tvDetailProgress;
  TextView tv_detail_title, tv_detail_email, tv_detail_phone, tv_detail_hobby, tv_detail_description;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_view_user);
    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }

    svDetailContent = findViewById(R.id.sv_detail_content);
    llDetailProgress = findViewById(R.id.ll_detail_progress);
    progressBarDetail = findViewById(R.id.progressBarDetail);
    tvDetailProgress = findViewById(R.id.tv_detail_progress);
    iv_detail = findViewById(R.id.iv_detail);
    tv_detail_title = findViewById(R.id.tv_detail_title);
    tv_detail_email = findViewById(R.id.tv_detail_email);
    tv_detail_phone = findViewById(R.id.tv_detail_phone);
    tv_detail_hobby = findViewById(R.id.tv_detail_hobby);
    tv_detail_description = findViewById(R.id.tv_detail_description);

    int id = (int) getIntent().getLongExtra("id", 0);
    UserProfile user = UserData.getUserFromId(id);

    if (user != null) {
      tv_detail_title.setText(user.getUsername());
      tv_detail_email.setText("Email: " + user.getEmail());
      tv_detail_phone.setText("Tel: " + user.getPhone());
      tv_detail_hobby.setText("Hobby: " + user.getHobby());
      tv_detail_description.setText(user.getDescription());

      // Ẩn toàn bộ nội dung chi tiết, chỉ khi thanh tiến trình chạy xong 100% mới hiển thị
      if (svDetailContent != null) {
        svDetailContent.setVisibility(View.INVISIBLE);
      }

      // Chạy thanh tiến trình tải ảnh ở giữa màn hình nhanh từ 0% đến 100%
      Downloader.downloadWithProgress(
          user.getAvatar_url(),
          new Handler(Looper.getMainLooper()),
          getBaseContext(),
          getCacheDir(),
          llDetailProgress,
          svDetailContent,
          progressBarDetail,
          tvDetailProgress,
          iv_detail
      );
    }
  }
}
