package thanhdnh.ueh.edu.article_app;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ViewUserActivity extends AppCompatActivity {
    ImageView iv_avatar;
    TextView tv_detail_username, tv_detail_id, tv_detail_email, tv_detail_phone, tv_detail_hobby, tv_detail_description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user);
        getSupportActionBar().hide();

        iv_avatar = findViewById(R.id.iv_avatar);
        tv_detail_username = findViewById(R.id.tv_detail_username);
        tv_detail_id = findViewById(R.id.tv_detail_id);
        tv_detail_email = findViewById(R.id.tv_detail_email);
        tv_detail_phone = findViewById(R.id.tv_detail_phone);
        tv_detail_hobby = findViewById(R.id.tv_detail_hobby);
        tv_detail_description = findViewById(R.id.tv_detail_description);

        int id = (int) getIntent().getLongExtra("id", 0);
        UserProfile user = UserData.getUserFromId(id);

        if (user != null) {
            Picasso.get().load(user.getAvatar_url()).resize(400, 400).centerCrop().into(iv_avatar);
            tv_detail_username.setText(user.getUsername());
            tv_detail_id.setText("ID: " + user.getId());
            tv_detail_email.setText("Email: " + user.getEmail());
            tv_detail_phone.setText("Tel: " + user.getPhone());
            tv_detail_hobby.setText("Hobby: " + user.getHobby());
            tv_detail_description.setText(user.getDescription());
        }
    }
}
