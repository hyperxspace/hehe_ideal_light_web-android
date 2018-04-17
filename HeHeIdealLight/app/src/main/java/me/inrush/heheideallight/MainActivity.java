package me.inrush.heheideallight;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author inrush
 */
public class MainActivity extends AppCompatActivity {

    private EditText mQuestion;
    private TextView mResult;
    private RadioGroup mChooseGroup;
    private QMUIRoundButton mEmptyBtn;
    private QMUIRoundButton mSearchBtn;
    private QMUITopBar mTopBar;
    private int mType = 0;

    private SparseArray<QMUITipDialog> mLoadingMap = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTopBar = findViewById(R.id.topbar);
        mQuestion = findViewById(R.id.question);
        mResult = findViewById(R.id.result);
        mChooseGroup = findViewById(R.id.chooseGroup);
        mEmptyBtn = findViewById(R.id.empty);
        mSearchBtn = findViewById(R.id.search);
        mTopBar.setTitle("答题助手");
        mEmptyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuestion.setText("");
            }
        });

        mChooseGroup.check(R.id.one);
        mChooseGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.one) {
                    mType = 0;
                } else {
                    mType = 1;
                }
            }
        });
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question = mQuestion.getText().toString();
                if (!Objects.equals(question, "")) {
                    sendQuestion(question, mType);
                }
            }
        });
    }

    private void sendQuestion(String question, int type) {
        showLoading(1, "正在获取答案");
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                String.format(Locale.CHINA, "{\"title\":\"%s\",\"type\":%d}", question, type));
        final Request request = new Request.Builder()
                .url("http://hehe.ngrok.inrush.me")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                mResult.post(new Runnable() {
                    @Override
                    public void run() {
                        dismissLoading(1);
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            StringBuilder resultText = new StringBuilder();
                            if (jsonObject.get("result") instanceof JSONArray) {
                                JSONArray result = jsonObject.getJSONArray("result");
                                for (int i = 0; i < result.length(); i++) {
                                    resultText.append(result.getString(i) + "\n");
                                }
                            } else {
                                resultText.append(jsonObject.getString("result"));
                            }
                            mResult.setText(resultText.toString());
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }


    private void showLoading(int code, String message) {
        final QMUITipDialog dialog = getDialog(QMUITipDialog.Builder.ICON_TYPE_LOADING, message);
        dialog.show();
        mLoadingMap.put(code, dialog);
    }

    private QMUITipDialog getDialog(int iconType, String message) {
        return new QMUITipDialog.Builder(this)
                .setIconType(iconType)
                .setTipWord(message)
                .create();
    }

    protected void dismissLoading(int code) {
        QMUITipDialog dialog = mLoadingMap.get(code);
        if (dialog != null) {
            dialog.dismiss();
        }
    }

}
