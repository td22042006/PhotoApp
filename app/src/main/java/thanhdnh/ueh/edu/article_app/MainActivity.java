package thanhdnh.ueh.edu.article_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public GridView gridview;
    public View llProgress;
    public ProgressBar progressBar;
    public TextView tvProgress;

    private final AdapterView.OnItemClickListener onitemclick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getBaseContext(), ViewUserActivity.class);
            if (gridview.getAdapter() != null) {
                intent.putExtra("id", gridview.getAdapter().getItemId(position));
                startActivity(intent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        gridview = findViewById(R.id.gridview);
        llProgress = findViewById(R.id.ll_progress);
        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tv_progress);

        // Tải danh sách User từ GitHub Raw
        new UserData(getBaseContext(), gridview).loadData("https://raw.githubusercontent.com/td22042006/PhotoApp/master/user.json", this, llProgress, progressBar, tvProgress);
        gridview.setOnItemClickListener(onitemclick);
    }
}
