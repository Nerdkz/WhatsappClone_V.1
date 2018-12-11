package br.com.whatsappandroid.cursoandroid.whatsapp.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;

import java.util.HashMap;
import java.util.Random;

import br.com.whatsappandroid.cursoandroid.whatsapp.R;
import br.com.whatsappandroid.cursoandroid.whatsapp.helper.Permissao;
import br.com.whatsappandroid.cursoandroid.whatsapp.helper.Preferencias;

public class LoginActivity extends AppCompatActivity {

    private EditText nome;
    private EditText telefone;
    private EditText codArea;
    private EditText codPais;
    private Button botaoCadastrar;
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.SEND_SMS,
            Manifest.permission.INTERNET
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Permissao.validaPermissoes( 1, this , permissoesNecessarias );

        nome = findViewById(R.id.edit_nome);
        telefone = findViewById(R.id.edit_telefone);
        codArea = findViewById(R.id.edit_area);
        codPais = findViewById(R.id.edit_cod_pais);
        botaoCadastrar = findViewById(R.id.bt_cadastrar);


        /* Definir as máscaras */
        SimpleMaskFormatter simpleMaskCodPais = new SimpleMaskFormatter( "+NN" );
        SimpleMaskFormatter simpleMaskCodArea = new SimpleMaskFormatter( "NN" );
        SimpleMaskFormatter simpleMaskTelefone = new SimpleMaskFormatter( "NNNNN-NNNN" );


        MaskTextWatcher maskCodPais = new MaskTextWatcher( codPais, simpleMaskCodPais );
        MaskTextWatcher maskCodArea = new MaskTextWatcher( codArea, simpleMaskCodArea);
        final MaskTextWatcher maskTelefone = new MaskTextWatcher( telefone, simpleMaskTelefone );

        codPais.addTextChangedListener( maskCodPais );
        codArea.addTextChangedListener( maskCodArea );
        telefone.addTextChangedListener( maskTelefone );

        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nomeUsuario = nome.getText().toString();
                String telefoneCompleto = codPais.getText().toString() + codArea.getText().toString() + telefone.getText().toString();
                String telefoneSemFormatacao = telefoneCompleto.replace("+","").replace("-", "").replace(" ", "");
                //telefoneSemFormatacao = telefoneSemFormatacao.replace("-", "");

                //Gerar Token
                Random randomico = new Random();
                int numeroRandomico = randomico.nextInt( 8999) + 1000;

                String token = String.valueOf( numeroRandomico );

                String mensagemEnvio = "Whatsapp Código de Confirmação: " + token;

                // Salvar os dados para a validação
                Preferencias preferencias = new Preferencias( LoginActivity.this ); // ou getApplicationContext()
                preferencias.salvarUsuarioPreferencias( nomeUsuario, telefoneSemFormatacao, token );

                //Envio do SMS
                boolean enviadoSMS = enviaSMS( "+" + telefoneSemFormatacao, mensagemEnvio );

                if( enviadoSMS ){

                    Intent intent = new Intent( LoginActivity.this, ValidadorActivity.class );
                    startActivity( intent );
                    finish();
                }
                else{
                    Toast.makeText( LoginActivity.this, "Problema ao enviar SMS, tente novamente!!", Toast.LENGTH_LONG ).show();
                }


                /*
                HashMap<String, String> usuario = preferencias.getDadosUsuario();
                Log.i("TOKEN:", "T: " + usuario.get("token") );
                Log.i("TOKEN:", "T: " + usuario.get("nome") );
                Log.i("TOKEN:", "T: " + usuario.get("telefone") );
                */

            }
        });

    }

    /* Envio do SMS*/

    private boolean enviaSMS( String telefone, String mensagem ){
        try{

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage( telefone, null, mensagem, null, null);

            return true;
        }
        catch ( Exception e ){
            e.printStackTrace();
            return false;
        }
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        for( int resultado : grantResults ){

            Log.i("GrantResults", "Resultado: " + grantResults);

            if( resultado == PackageManager.PERMISSION_DENIED ){
                alertaValidacaoPermissao();

            }
        }

    }


    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle("Permissões negadas");
        builder.setMessage("Para utilizar esse app, é necessário aceitar as permissões");

        builder.setPositiveButton("CONFIRMAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}