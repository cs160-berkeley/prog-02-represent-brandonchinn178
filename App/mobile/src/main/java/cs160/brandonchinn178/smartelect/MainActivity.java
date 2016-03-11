package cs160.brandonchinn178.smartelect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startCongressionalView(View v) {
        Intent intent = new Intent(this, CongressionalActivity.class);

        if (v.getId() == R.id.current_location_button) {
            intent.putExtra("IS_CURRENT", true);
        } else {
            intent.putExtra("IS_CURRENT", false);

            EditText zipCodeInput = (EditText) findViewById(R.id.zip_code_input);
            int zipCode;
            try {
                zipCode = Integer.parseInt(zipCodeInput.getText().toString());
            } catch (NumberFormatException e) {
                zipCodeInput.setError(getString(R.string.zip_code_input_error));
                return;
            }

            if (!ZipCode.isValid(this, zipCode)) {
                zipCodeInput.setError(getString(R.string.zip_code_input_error));
                return;
            }

            intent.putExtra("ZIP_CODE", zipCode);
        }

        startActivity(intent);
    }
}
