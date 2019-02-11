package com.antonio.ferrari;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.antonioejemplos.agendarecyclerview.R;

import java.sql.SQLException;

import Beans.Contactos;
import controlador.SQLControlador;

//import com.videumcorp.desarrolladorandroid.navigatio.R;

//import antonio.ejemplos.agendacomercial.R;

public class AltaUsuarios extends AppCompatActivity {

    public static final int C_CREAR = 552;
    public static final int C_EDITAR = 553;
    public static final int C_ELIMINAR = 554;
    private EditText nombre;
    private EditText apellidos;
    private EditText direc;
    private EditText telefono;
    private EditText email;
    private RadioButton radio1, radio2, radio3, radio4;
    private EditText observaciones;
    //private Button cancelar;
    //private Button guardar;

    private SQLControlador Connection;
    private boolean validar = true;
    private Toolbar toolbar;
    // Modo del formulario
    private int modo;
    private int id_recogido;
    private RelativeLayout contendorAltaUsuarios;
    private Contactos contactoRecogido;
    private int posicion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alta_usuarios_material);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        nombre =  findViewById(R.id.nombre);
        apellidos =  findViewById(R.id.apellidos);
        direc =  findViewById(R.id.direc);
        telefono =  findViewById(R.id.telefono);
        email =  findViewById(R.id.email);


        radio1 =  findViewById(R.id.radio1);
        radio2 =  findViewById(R.id.radio2);
        radio3 =  findViewById(R.id.radio3);
        radio4 =  findViewById(R.id.radio4);


        observaciones =  findViewById(R.id.observaciones);
        toolbar =  findViewById(R.id.toolbar);
        contendorAltaUsuarios=findViewById(R.id.contendorAltaUsuarios);

        //La acitivity debe extender de AppCompatActivity para poder hacer el seteo a ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TypedValue typedValueColorPrimaryDark = new TypedValue();
        AltaUsuarios.this.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValueColorPrimaryDark, true);
        final int colorPrimaryDark = typedValueColorPrimaryDark.data;
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(colorPrimaryDark);
        }


        //EN EL BUNDLE NOS LLEGA EL MODO(ALTA O MODIFICACIÓN) Y
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            establecerModo(bundle.getInt(MainActivity.C_MODO));
            posicion=bundle.getInt("Position");
        }

        if (modo == MainActivity.C_EDITAR) {
        //Mostramos los datos recogidos del formulario si el modo invocado es editar...


             contactoRecogido= (Contactos) bundle.getSerializable("ContactoSerializado");

            //nombre.setEnabled(false);

            id_recogido = (int) contactoRecogido.get_id();
            nombre.setText(contactoRecogido.getNombre());
            apellidos.setText(contactoRecogido.getApellidos());
            direc.setText(contactoRecogido.getDireccion());
            telefono.setText(contactoRecogido.getTelefono());
            email.setText(contactoRecogido.getEmail());

            if (contactoRecogido.getId_Categoria() == 1) {
                //radio1.isChecked();
                radio1.setChecked(true);
            }
            if (contactoRecogido.getId_Categoria() == 2) {
                //radio2.isChecked();
                radio2.setChecked(true);
            }
            if (contactoRecogido.getId_Categoria() == 3) {
                //radio3.isChecked();
                radio3.setChecked(true);
            }
            if (contactoRecogido.getId_Categoria() == 4) {
                //radio4.isChecked();
                radio4.setChecked(true);
            }

            observaciones.setText(contactoRecogido.getObservaciones());
            this.setTitle(contactoRecogido.getNombre());

        }


        //Asignamos eventos al edittext observaciones para incluir contador de caracteres.
        observaciones.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //Sacamos el número de caracteres en un textview
                TextView contador = (TextView) findViewById(R.id.texto_contador);


              /*String tamanoString = String.valueOf(s.length());
              contador.setText(tamanoString);*/


                //Establecemos  el maxlengt del control observaciones dinámicamente a 200 caracteres.
                int maxLength = 200;
                InputFilter[] fArray = new InputFilter[1];
                fArray[0] = new InputFilter.LengthFilter(maxLength);
                observaciones.setFilters(fArray);

                //Restamos sobre 200 que es el maxlength de observaciones
                int resta = maxLength - s.length();
                String tamano = String.valueOf(resta);
                contador.setText(tamano);


            }
        });

        //configurarPalete();



    }

    private void configurarPalete() {
        //Palette
        //Pasamos a bitmap el recurso imagen que vamos a utilizar en Palette
        Bitmap miImagen= BitmapFactory.decodeResource(this.getResources(), R.drawable.ferrari_alta);
        //Palette.generateAsync(miImagen, this);


        Palette p = Palette.from(miImagen).generate();
        // Asynchronous
        Palette.from(miImagen).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                // Use generated instance
                Palette.Swatch vibrantSwatch = p.getVibrantSwatch();

                Palette.Swatch darkVibrantSwatch = p.getDarkVibrantSwatch();

                Palette.Swatch lightVibrantSwatch = p.getLightVibrantSwatch();

                Palette.Swatch mutedSwatch = p.getVibrantSwatch();

                Palette.Swatch darkMutedSwatch = p.getDarkMutedSwatch();

                Palette.Swatch lightMutedSwatch = p.getLightMutedSwatch();
                //PONEMOS LOS COLORES QUE PALETTE HA GENERADO DONDE LO CREAMOS OPORTUNO...


                if(mutedSwatch!=null) {
                    nombre.setTextColor(mutedSwatch.getRgb());
                    //contendorAltaUsuarios.setBackgroundColor(mutedSwatch.getRgb());
                    toolbar.setBackgroundColor(mutedSwatch.getRgb());

                }

            }
        });




    }



    private void establecerModo(int m) {
        this.modo = m;

        if (modo == MainActivity.C_VISUALIZAR) {
            //this.setTitle(nombre.getText().toString());
            this.setEdicion(false);
        } else if (modo == MainActivity.C_CREAR) {
            // this.setTitle(R.string.hipoteca_crear_titulo);
            this.setEdicion(true);
        } else if (modo == MainActivity.C_EDITAR) {
            //this.setTitle(R.string.agenda_editar_titulo);
            this.setEdicion(true);
        }
    }

    private void setEdicion(boolean opcion) {
        // Lineas para ocultar el teclado virtual (Hide keyboard)
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nombre.getWindowToken(), 0);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
//Evitamos que funcione la tecla del menú que traen por defecto los samsung...
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                // Toast.makeText(this, "Menú presionado",
                //       Toast.LENGTH_LONG);
                //toolbar.canShowOverflowMenu();
                //toolbar.setFocusable(true);
                //toolbar.collapseActionView();


                return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    public void guardar() {

        String nom = nombre.getText().toString();
        String apell = apellidos.getText().toString();
        String direccion = direc.getText().toString();
        String tele = telefono.getText().toString();
        String correo = email.getText().toString();
        long Id_Categ = 0;

        if (radio1.isChecked()) {
            Id_Categ = 1;

        } else if (radio2.isChecked()) {
            Id_Categ = 2;
        } else if (radio3.isChecked()) {
            Id_Categ = 3;
        } else if (radio4.isChecked()) {
            Id_Categ = 4;
        }


        String observa = observaciones.getText().toString();

        if (modo == MainActivity.C_CREAR) {
            Context context=AltaUsuarios.this;
            if (validar(validar)) {

                try {
                    Connection = new SQLControlador(getApplicationContext());//Objeto SQLControlador
                    Connection.abrirBaseDeDatos(2);
                    Connection.InsertarUsuario(nom, apell, direccion, tele, correo, Id_Categ, observa);
                    Toast.makeText(getApplicationContext(), "Se ha incluido en la agenda a " + nom, Toast.LENGTH_SHORT).show();
                    Connection.cerrar();

                    Contactos nuevoContacto=new Contactos(nom,apell,direccion,tele,correo,(int)Id_Categ,observa);


                  /*  AdaptadorRecyclerViewSearch adaptadorRecyclerViewSearch=new AdaptadorRecyclerViewSearch(this);
                    adaptadorRecyclerViewSearch.add(0,nuevoContacto);*/




                 /*   Intent intent=new Intent(AltaUsuarios.this,MainActivity.class);
                    setResult(RESULT_OK);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("ContactoNuevo", nuevoContacto);
                    intent.putExtras(bundle);*/

               /*     Intent intent = new Intent(AltaUsuarios.this,MainActivity.class);
                    intent.putExtra("ContactoNuevo",nuevoContacto);
                    setResult(Activity.RESULT_OK, intent);Para ponerlo en el OnDestroy()*/

                    Intent intent = new Intent(AltaUsuarios.this,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("ContactoNuevo",nuevoContacto);
                    startActivityForResult(intent, C_CREAR);


                    finish();
                    //Para actualizar datos en MainActivity Se va a llamar a Consultar() desde Onrestart() del com.agendacomercial.navigatio.

                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }


            }//Fin validar em modo C_CREAR

        }

        if (modo == MainActivity.C_EDITAR) {
            if (validar(validar)) {

                try {
                    Connection = new SQLControlador(getApplicationContext());//Objeto SQLControlador
                    Connection.abrirBaseDeDatos(2);

                    Connection.ModificarContacto(id_recogido, nom, apell, direccion, tele, correo, (int) Id_Categ, observa);

                    // String alta=String.format((R.string.agenda_crear_informacion),nom);
                    Toast.makeText(getApplicationContext(), "Se ha modificado en la agenda a " + nom, Toast.LENGTH_SHORT).show();
                    Connection.cerrar();

                  /*  AdaptadorRecyclerViewSearch adaptadorRecyclerViewSearch=new AdaptadorRecyclerViewSearch(this);
                    adaptadorRecyclerViewSearch.(10,contactoRecogido);*/


                    Contactos contactoModificado=new Contactos(nom,apell,direccion,tele,correo,(int)Id_Categ,observa);


                    Intent intent = new Intent(AltaUsuarios.this,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("ContactoModificado",contactoModificado);
                    startActivityForResult(intent, C_EDITAR);

                    finish();


                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }


            }//Fin validar em modo C_EDITAR

        }

    }


    private void borrar(final long id) {
        /*
		 * Borramos el registro y refrescamos la lista
		 */
        AlertDialog.Builder dialogEliminar = new AlertDialog.Builder(this);

        dialogEliminar.setIcon(android.R.drawable.ic_dialog_alert);
        dialogEliminar.setTitle(getResources().getString(
                R.string.agenda_eliminar_titulo));
        dialogEliminar.setMessage(getResources().getString(
                R.string.agenda_eliminar_mensaje));
        dialogEliminar.setCancelable(false);

        dialogEliminar.setPositiveButton(
                getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int boton) {


                        Connection = new SQLControlador(
                                getApplicationContext());
                        try {
                            Connection.abrirBaseDeDatos(2);
                        } catch (SQLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }// Escritura. Borrar

                        Connection.delete(id);

                        Toast.makeText(AltaUsuarios.this,
                                R.string.agenda_eliminar_confirmacion,
                                Toast.LENGTH_SHORT).show();
                        Connection.cerrar();


                        Intent intent = new Intent(AltaUsuarios.this,MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivityForResult(intent, C_ELIMINAR);


                        //Intent intent=new Intent(AltaUsuarios.this,MainActivity.class);

                        finish();


                    }
                });

        dialogEliminar.setNegativeButton(android.R.string.no, null);

        dialogEliminar.show();
    }


    //Validaci�n para que el nombre y el teléfono no se dejen vac�os
    private boolean validar(boolean validar) {
        if ((nombre.getText().toString().equals("")) || (telefono.getText().toString().equals(""))) {
            //if (nombre.getText().toString().length() == 0){

            //Toast.makeText(getApplicationContext(), "Es obligatorio rellenar el nombre" , Toast.LENGTH_LONG).show();

            //Se prepara la alerta creando nueva instancia
            AlertDialog.Builder dialogValidar = new AlertDialog.Builder(this);
            dialogValidar.setIcon(android.R.drawable.ic_dialog_alert);//icono
            dialogValidar.setTitle(getResources().getString(R.string.agenda_crear_titulo));//T�tulo
            dialogValidar.setMessage(getResources().getString(R.string.agenda_texto_vacio));
            //Se a�ade un solo bot�n para que el usuario confirme...
            dialogValidar.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            dialogValidar.create().show();


            return false;
        }

        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if (modo == MainActivity.C_EDITAR) {
            getMenuInflater().inflate(R.menu.edit, menu);
            getMenuInflater().inflate(R.menu.compartir, menu);
        } else {
            getMenuInflater().inflate(R.menu.alta, menu);
            //getMenuInflater().inflate(R.menu.compartir, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.nuevo_usuario) {

            guardar();


            return true;
        }


        if (id == R.id.modificar_usuario) {

            guardar();

            return true;
        }

        if (id == R.id.eliminar_usuario) {

            borrar(id_recogido);
            return true;
        }

        if (id == R.id.compartir) {
            Uri uri = Uri.parse("smsto:" + telefono.getText().toString());
            Intent i = new Intent(Intent.ACTION_SENDTO, uri);

            //i.putExtra("sms_body", smsText);
            i.setPackage("com.whatsapp");
            startActivity(i);

            return true;
        }



        if(id==R.id.enviarEmail){

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL  , new String[] {email.getText().toString()});
            //intent.putExtra(Intent.EXTRA_SUBJECT, "My subject");
            startActivity(Intent.createChooser(intent, "Excoge una aplicación para enviar el email"));
            //startActivity(intent);


            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
