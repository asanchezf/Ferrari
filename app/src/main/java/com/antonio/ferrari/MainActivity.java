package com.antonio.ferrari;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.antonioejemplos.agendarecyclerview.R;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import Beans.Contactos;
import controlador.SQLControlador;
import util.ImportarContactos2;

import static android.widget.SearchView.OnQueryTextListener;

public class MainActivity extends AppCompatActivity implements AdaptadorRecyclerViewSearch.OnItemClickListener, OnQueryTextListener, SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener {
    //CONSTANTES PARA EL MODO FORMULARIO Y PARA LOS TIPOS DE LLAMADA.============================
    public static final String C_MODO = "modo";
    public static final int C_VISUALIZAR = 551;
    public static final int C_CREAR = 552;
    public static final int C_EDITAR = 553;
    public static final int C_ELIMINAR = 554;
    private static final int SOLICITUD_ACCESS_READ_CONTACTS = 1;//Para control de permisos en Android M o superior e importar contactos
    private static final int SOLICITUD_ACCESS_CALL_PHONE = 2;//Para control de permisos en Android M o superior y poder realizar llamadas
    //FIN CONSTANTES==============================================================================
    private RecyclerView lista;
    private AdaptadorRecyclerViewSearch adaptadorBuscador;
    private SQLControlador dbConnection;//CONTIENE LAS CONEXIONES A BBDD (CREADA EN DBHELPER.CLASS) Y LOS M�TODOS INSERT, UPDATE, DELETE, BUSCAR....
    private ArrayList<Contactos> arrayListContactos;
    private static long back_pressed;//Contador para cerrar la app al pulsar dos veces seguidas el btón de cerrar. Se gestiona en el evento onBackPressed
    private ProgressBar barraProgreso;
    private Handler manejador = new Handler();//Manejador del hilo
    private int selectedPosition;
    private Contactos contactos;
    private static int primeraVez=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        arrayListContactos = new ArrayList<Contactos>();
        //Se hace aquí para preserver el scroll del RecyclerView??
        arrayListContactos = consultar();
        inicializarControles();
    }

    private void inicializarControles() {

        Toolbar toolbar = findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        // Configuración del RecyclerView-----------------------------
        lista = findViewById(R.id.lstLista);
        lista.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));//Layout para el RecyclerView
        lista.setItemAnimator(new DefaultItemAnimator());//Animación por defecto....
        LinearLayoutManager llmanager = new LinearLayoutManager(this);
        llmanager.setOrientation(LinearLayoutManager.VERTICAL);

        //Cambiamos el color del Floating Action Button
        FloatingActionButton btnFab = findViewById(R.id.btnFab);
        int miColor = getResources().getColor(R.color.color_floating);
        ColorStateList csl = new ColorStateList(new int[][]{new int[0]}, new int[]{miColor});
        btnFab.setBackgroundTintList(csl);

        btnFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crear(view);
            }
        });

        CollapsingToolbarLayout ctlLayout = findViewById(R.id.ctlLayout);
        ctlLayout.setTitle(getString(R.string.app_name));

        if (arrayListContactos != null) {
            adaptadorBuscador = new AdaptadorRecyclerViewSearch(arrayListContactos, this, this, lista);//Implementa el adapatador: pasamos ahora tres parámetros....
            lista.setAdapter(adaptadorBuscador);
        }



        if(primeraVez==0){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permisosPorAplicacion(contactos, 1);
                primeraVez=1;
            } else {

                //ABRE NUEVA ACTIVITY CON TAMAÑO ALERTDIALOG--FUNCIONA CORRECTAMENTE
                  /*  Intent i = new Intent(this, ImportarContactos.class);
                    startActivity(i);*/

                importarContactos();
                primeraVez=1;
            }

        }


    }

    private ArrayList<Contactos> consultar() {
        //ES EL PRIMER MÉTODO LLAMADO QUE ACCEDE A LA BB.DD DONDE SE ENCUENTRAN LOS REGISTROS.
        //SI LA BB.DD NO EXISTE SE CREARÁ. SI YA EXISTE LA DEVUELVE SEGÚN EL MODO EN QUE LLAMEMOS: EXCRITURA O LECTURA.
        //AL INSTALAR LA APP ES AQUÍ DONDE REALMENTE SE CREA PQ LA CLASE DBhelper QUE ES LA ENCARGADA DE CREAR LA BB.DD
        //SE INSTANCIA DESDE LA CLASE SQLcontrolador DISTINGUIENDO SI LLAMA A ONCREATE O A ONUPGRADE.. PARA GESTIONAR LAS
        //VERSIONES DE LA bb.dd.
        dbConnection = new SQLControlador(getApplicationContext());
        try {
            dbConnection.abrirBaseDeDatos(1);//Modo lectura
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }// Lectura. Solo para ver


        arrayListContactos = dbConnection.BuscarTodos();// llamamos a BuscarTodos() que devuelve un arraylist de contactos...+
        dbConnection.cerrar();

        return arrayListContactos;
    }

    private void crear(View view) {
        //Generamos transiciones en el flotaingbuton:
        Pair<View, String> pair = Pair.create(view.findViewById(R.id.btnFab), "fab_transition");
        ActivityOptionsCompat options;
        Activity act = MainActivity.this;
        options = ActivityOptionsCompat.makeSceneTransitionAnimation(act, pair);
        Intent transitionIntent = new Intent(act, AltaUsuarios.class);
        transitionIntent.putExtra(C_MODO, C_CREAR);
        //act.startActivityForResult(transitionIntent, adaptadorBuscador.getItemCount(), options.toBundle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            act.startActivityForResult(transitionIntent, C_CREAR, options.toBundle());
            transitionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        } else {
            startActivityForResult(transitionIntent, C_CREAR);
            transitionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == C_CREAR && resultCode == RESULT_OK) {
            Contactos contactoNuevo = (Contactos) data.getExtras().getSerializable("ContactoNuevo");
            adaptadorBuscador.add(arrayListContactos.size() - 1, contactoNuevo);
        } else if (requestCode == C_EDITAR && resultCode == RESULT_OK) {
            Contactos contactoEditar = (Contactos) data.getExtras().getSerializable("ContactoModificado");
            adaptadorBuscador.update(selectedPosition, contactoEditar);
        } else if (requestCode == C_ELIMINAR && resultCode == RESULT_OK) {
            //Contactos contactoEditar= (Contactos) data.getExtras().getSerializable("ContactoModificado");
            adaptadorBuscador.remove(selectedPosition);
            recargarRecyclerView();
        }

    }

    private void editar(int id) throws SQLException {

        //SI EL ID ES EL DE LA BB.DD.
  /*      dbConnection = new SQLControlador(getApplicationContext());
        dbConnection.abrirBaseDeDatos(1);// Lectura. Solo para ver
        Cursor c = dbConnection.CursorBuscarUno(id);// Devuelve un Cursor
        int idenviado = c.getInt(c.getColumnIndex("_id"));
        String nombre = c.getString(c.getColumnIndex("Nombre"));
        String apellidos = c.getString(c.getColumnIndex("Apellidos"));
        String direccion = c.getString(c.getColumnIndex("Direccion"));
        String telefono = c.getString(c.getColumnIndex("Telefono"));
        String email = c.getString(c.getColumnIndex("Email"));
        int Id_Categ = c.getInt(c.getColumnIndex("Id_Categoria"));
        String observ = c.getString(c.getColumnIndex("Observaciones"));
        dbConnection.cerrar();
        // Pasamos datos al formulario en modo visualizar
        Intent i = new Intent(MainActivity.this, AltaUsuarios.class);
        i.putExtra("_id", idenviado);
        i.putExtra("Nombre", nombre);
        i.putExtra("Apellidos", apellidos);
        i.putExtra("Direccion", direccion);
        i.putExtra("Telefono", telefono);
        i.putExtra("Email", email);
        i.putExtra("Id_Categoria", Id_Categ);
        i.putExtra("Observaciones", observ);
        i.putExtra(C_MODO, C_EDITAR);
        startActivityForResult(i, C_EDITAR);*/


        Contactos contactosSerilizado = arrayListContactos.get(id);
        //Toast.makeText(this, "contacto " + contactosSerilizado.getNombre(), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(MainActivity.this, AltaUsuarios.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("ContactoSerializado", contactosSerilizado);
        i.putExtras(bundle);

        i.putExtra(C_MODO, C_EDITAR);
        i.putExtra("Position", id);
        //startActivityForResult(i, C_EDITAR);
        startActivity(i);


    }

    @Override
    protected void onResume() {

        super.onResume();

        //Toast.makeText(this, "onResume() " + selectedPosition, Toast.LENGTH_SHORT).show();
        //adaptadorBuscador.setSelectedPosition(selectedPosition);

        //consultar();

     /*   adaptadorBuscador.notifyDataSetChanged();
     */
        if (adaptadorBuscador != null) {

            adaptadorBuscador.setSelectedPosition(selectedPosition);

        }

    }

    private void recargarRecyclerView() {

        consultar();
        adaptadorBuscador = new AdaptadorRecyclerViewSearch(arrayListContactos, this, this, lista);//Implementa el adapatador: pasamos ahora tres parámetros....
        lista.setAdapter(adaptadorBuscador);


    }


    @Override
    protected void onPause() {

//    	 Indica que la actividad est� a punto de ser lanzada a segundo plano, normalmente porque otra actividad es lanzada.
//    	 Es el lugar adecuado para detener animaciones, m�sica o almacenar los datos que estaban en edici�n.


        if (adaptadorBuscador != null) {
            //selectedPosition = adaptadorBuscador.getSelectedPosition();

            //adaptadorBuscador.setSelectedPosition(selectedPosition);

            selectedPosition = adaptadorBuscador.getSelectedPosition();//5 de marzo
            //Toast.makeText(this, "selectedPosition desde onPause " + selectedPosition, Toast.LENGTH_SHORT).show();
        }


        //Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
        super.onPause();

    }

    @Override
    public void onClick(RecyclerView.ViewHolder holder, final int idPromocion, final View view) {//idPromocion y View se definen como final pq son llamada desde la clase interna del evento onclick() del AlertDialog

        Log.i("Demo Recycler", "Se ha pulsado en la siguiente view: " + holder);
        /*Toast.makeText(this, "Modificamos "+holder.getAdapterPosition(), Toast.LENGTH_SHORT).show();
        selectedPosition=holder.getAdapterPosition();*/

        //CREAMOS UN OBJETO A PARTIR DE LA SELECCION QUE HAYA HECHO EL USUARIO Y SE LOS PASAMOS A TODOS LOS PROCEDIMIENTOS:EDITAR,LLAMAR,UBICAR Y VISITAR...
        for (Contactos contactoSeleccionado : arrayListContactos) {
            if(contactoSeleccionado.get_id()==idPromocion){
                contactos=contactoSeleccionado;
            }
        }


        if (view.getId() == R.id.category) {
            //Toast.makeText(MainActivity.this, "Se ha pulsado en categoría: " + idPromocion + " " + holder, Toast.LENGTH_SHORT).show();
        }

        //Pulsando en en el btn de llamar se abre el dialer para llamar al contacto seleccionado
        else if (view.getId() == R.id.btncontactar) {

            //Si la Api es igual o superior a Android M gestionamos el control de permisos en ejecución...
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                //permisosPorAplicacion(holder.getAdapterPosition(), 2);//Antes
                permisosPorAplicacion(contactos, 2);//Antes
            } else {
                //llamar(holder.getAdapterPosition());
                llamar(contactos);
            }

        }//Fin else if

        else if (view.getId() == R.id.txtubicacion) {
            //INICIA POSICIONAMIENTO DESDE LA UBICACIÓN ACTUAL A LA DIRECCIÓN INTRODUCIDA....
            //ubicar(holder.getAdapterPosition());
            ubicar(contactos);
        } else if (view.getId() == R.id.txtruta) {
            //INICIA NAVEGACIÓN DESDE LA UBICACIÓN ACTUAL A LA DIRECCIÓN INTRODUCIDA....
            //visitar(holder.getAdapterPosition());
            visitar(contactos);
        }

        //Pulsando en otra parte de los CardView distinta se abre la Activity para editar o eliminar el contacto
        else {

            editarContacto(contactos,holder.getAdapterPosition());
        }
    }

    //Método que realiza la gestión de la llamada telefónica
    private void llamar(final int id) {
        //Toast.makeText(this, "contacto " + contactoTelefonico.getTelefono(), Toast.LENGTH_SHORT).show();

        AlertDialog.Builder dialogoContactar = new AlertDialog.Builder(this);
        dialogoContactar.setIcon(android.R.drawable.ic_dialog_alert);
        dialogoContactar.setTitle(getResources().getString(
                R.string.agenda_call_titulo));
        dialogoContactar.setMessage(getResources().getString(
                R.string.agenda_call_mensaje));
        dialogoContactar.setCancelable(false);
        dialogoContactar.setPositiveButton(
                getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int boton) {

                        Contactos contactoTelefonico = arrayListContactos.get(id);
                        String telefono = contactoTelefonico.getTelefono();
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telefono));
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider callingi
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }

                        startActivity(intent);
                    }
                });

        dialogoContactar.setNegativeButton(android.R.string.no, null);
        dialogoContactar.show();


    }


    private void llamar(final Contactos contactos) {

        AlertDialog.Builder dialogoContactar = new AlertDialog.Builder(this);
        dialogoContactar.setIcon(android.R.drawable.ic_dialog_alert);
        dialogoContactar.setTitle(getResources().getString(
                R.string.agenda_call_titulo));
        dialogoContactar.setMessage(getResources().getString(
                R.string.agenda_call_mensaje));
        dialogoContactar.setCancelable(false);
        dialogoContactar.setPositiveButton(
                getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int boton) {

                        /*Contactos contactoTelefonico = arrayListContactos.get(id);*/

                        String telefono = contactos.getTelefono();
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telefono));
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider callingi
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }

                        startActivity(intent);
                    }
                });

        dialogoContactar.setNegativeButton(android.R.string.no, null);
        dialogoContactar.show();

    }


    private void editarSerializable(int adapterPosition) {

        selectedPosition = adapterPosition;


        Contactos contactosSerilizado = arrayListContactos.get(adapterPosition);


        //Toast.makeText(this, "contacto " + contactosSerilizado.getNombre(), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(MainActivity.this, AltaUsuarios.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("ContactoSerializado", contactosSerilizado);
        i.putExtras(bundle);

        i.putExtra(C_MODO, C_EDITAR);
        i.putExtra("Position", adapterPosition);
        //startActivityForResult(i, C_EDITAR);
        startActivity(i);

        //FALTA ACTUALIZAR EL ARRAYLIST DE CONTACTOS...

        //adaptadorBuscador.notifyDataSetChanged();
    }

    private void editarContacto(Contactos contacto, int adapterPosition) {
        //Toast.makeText(this, "contacto " + contactosSerilizado.getNombre(), Toast.LENGTH_SHORT).show();
        selectedPosition = adapterPosition;
        Intent i = new Intent(MainActivity.this, AltaUsuarios.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("ContactoSerializado", contacto);
        i.putExtras(bundle);

        i.putExtra(C_MODO, C_EDITAR);
        i.putExtra("Position", selectedPosition);
        //startActivityForResult(i, C_EDITAR);
        startActivity(i);



    }

    private void visitar(Contactos contacto) {

     /*   Contactos contactoUbicar = arrayListContactos.get(idPromocion);
        String direccion = contactoUbicar.getDireccion();*/
        String direccion = contacto.getDireccion();

        if (direccion.equals("")) {

            //Para cambiar el tipo de letra dentro del snackbar utilizamos Spannable
            SpannableStringBuilder snackbarText = new SpannableStringBuilder();
            //snackbarText.append("Add ");
            int boldStart = snackbarText.length();
            snackbarText.append("Este contacto no tiene ninguna dirección asignada para que el gps de su terminal le dirija hacia ella.");
            snackbarText.setSpan(new ForegroundColorSpan(0x99FFFFFF), boldStart, snackbarText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            snackbarText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), boldStart, snackbarText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //snackbarText.append(" to Snackbar text");

            //Snackbar snack = Snackbar.make(lista, R.string.agenda_contacto_sin_direccion, Snackbar.LENGTH_LONG);
            Snackbar snack = Snackbar.make(lista, R.string.agenda_contacto_sin_direccion, Snackbar.LENGTH_LONG);
            ViewGroup group = (ViewGroup) snack.getView();
            //group.setBackgroundColor(getResources().getColor(R.color.color_floating));
            //snack.setActionTextColor(Color.BLACK);
            group.setBackground(ContextCompat.getDrawable(this, R.drawable.degradado_sncackbar));
            snack.show();


        } else {

            //Si el GPS no está habilitado
            LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                Snackbar snack = Snackbar.make(lista, R.string.agenda_gps_no_activado, Snackbar.LENGTH_LONG);
                ViewGroup group = (ViewGroup) snack.getView();
                //group.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                //snack.setActionTextColor(Color.BLACK);
                group.setBackground(ContextCompat.getDrawable(this, R.drawable.degradado_sncackbar));
                snack.show();
                habilitarGPS();

            } else {
                //Uri.parse("google.navigation:q=an+Mestizaje, 2+Alcorcon"));

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + direccion));
                startActivity(intent);
            }

        }
    }


    private void ubicar(Contactos contacto) {

        /*Contactos contactoUbicar = arrayListContactos.get(idPromocion);
        String direccion = contactoUbicar.getDireccion();*/
        String direccion = contacto.getDireccion();

        if (direccion.equals("")) {

            //Toast.makeText(MainActivity.this,"Este contacto no tiene ninguna dirección asignada..!",Toast.LENGTH_LONG).show();

            Snackbar snack = Snackbar.make(lista, R.string.agenda_contacto_sin_direccion, Snackbar.LENGTH_LONG);
            ViewGroup group = (ViewGroup) snack.getView();
            //group.setBackgroundColor(getResources().getColor(R.color.md_deep_orange_500));
            group.setBackground(ContextCompat.getDrawable(this, R.drawable.degradado_sncackbar));
            snack.show();

        } else {

            //Si el GPS no está habilitado
            LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {


             /*   Snackbar snack = Snackbar.make(lista, R.string.agenda_gps_no_activado, Snackbar.LENGTH_LONG);
                ViewGroup group = (ViewGroup) snack.getView();
                group.setBackgroundColor(getResources().getColor(R.color.md_deep_orange_500));
                snack.show();*/

                habilitarGPS();

            }

            //El GPS está habilitado y el contacto tiene dirección asociada
            else {

                String uri = String.format(Locale.ENGLISH, "geo:0,0?q=" + direccion);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);

            }
        }
    }

    private void habilitarGPS() {
        AlertDialog alert = null;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.activarGps)
                .setTitle("Activar Gps")
                .setCancelable(false)
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(R.string.configurargps, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });

        alert = builder.create();
        alert.show();


    }

    public void borrarTodos() {
        /*
         * Borramos todos los registros y refrescamos el recyclerView
		 */
        AlertDialog.Builder dialogEliminar = new AlertDialog.Builder(this);

        dialogEliminar.setIcon(android.R.drawable.ic_dialog_alert);
        dialogEliminar.setTitle(getResources().getString(
                R.string.agenda_eliminar_todos_titulo));
        dialogEliminar.setMessage(getResources().getString(
                R.string.agenda_eliminar_todos_mensaje));
        dialogEliminar.setCancelable(false);

        dialogEliminar.setPositiveButton(
                getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int boton) {

                        dbConnection = new SQLControlador(
                                getApplicationContext());
                        try {
                            dbConnection.abrirBaseDeDatos(2);
                        } catch (SQLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }// Escritura. Borrar

                        dbConnection.borrarTodos();

                        Toast.makeText(MainActivity.this,
                                R.string.agenda_eliminar_todos_confirmacion,
                                Toast.LENGTH_SHORT).show();
                        dbConnection.cerrar();
                        //consultar();
                        recargarRecyclerView();
                    }
                });

        dialogEliminar.setNegativeButton(android.R.string.no, null);

        dialogEliminar.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //SE IMPLEMENTA EL MENÚ BUSCAR. Se añaden a la clase dos interfaces y se implmenta sus métodos más abajo...


        // MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItem searchItem = menu.findItem(R.id.buscar);
        //SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getResources().getString(R.string.buscar_en_searchview));

        //Personalizamos con color y tamaño de letra
        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setHintTextColor(getResources().getColor(android.R.color.white));
        searchAutoComplete.setTextSize(16);


        //searchView.setSubmitButtonEnabled(true);


        searchView.setOnQueryTextListener(this);


        // LISTENER PARA LA APERTURA Y CIERRE DEL WIDGET
        //MenuItemCompat.setOnActionExpandListener(searchItem, this);
        //FIN IMPLEMENTACION DEL MENU BUSCAR

        /*searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adaptadorBuscador.getFilter().filter(newText);
                return false;
            }
        });*/


        return super.onCreateOptionsMenu(menu);
        //return true;
    }

    //1-Gestionamos los permisos según la versión. A partir de Android M algnos permisos catalogados como peligrosos se gestionan en tiempo de ejecución
    private void permisosPorAplicacion(final Contactos contacto, int idPermiso) {


        switch (idPermiso) {

            case 1://Acceso a los contactos
                //Permisos para acceder a los Contactos
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    //1-La aplicación tiene permisos....

                    //ABRE NUEVA ACTIVITY CON TAMAÑO ALERTDIALOG--FUNCIONA CORRECTAMENTE
                  /*  Intent i = new Intent(this, ImportarContactos.class);
                    startActivity(i);*/

                    importarContactos();

                } else {//No tiene permisos

                    solicitarPermisoImportContacts();
                }
                break;

            case 2://Permiso para las llamadas
                //Permiso para realizar llamadas
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    //1-La aplicación ya tiene permisos....

                    //llamar((id));
                    llamar(contacto);

                } else {//No tiene permisos

                    //explicarUsoPermiso();
                    //solicitarPermiso();
                    //id_Contacto_Llamada = id;
                    //id_Contacto_Llamada = (int) contacto.get_id();
                    solicitarPermisoLlamadas();
                }

                break;

            default:
                break;
        }

    }

    public void importarContactos() {
        barraProgreso = findViewById(R.id.barra);
        AlertDialog.Builder dialogImportar = new AlertDialog.Builder(this);
        dialogImportar.setIcon(R.mipmap.ic_launcher);
        dialogImportar.setTitle(getResources().getString(
                R.string.title_activity_importar_contactos));
        dialogImportar.setMessage(getResources().getString(
                R.string.aceptar_import));
        dialogImportar.setCancelable(false);

        dialogImportar.setPositiveButton(
                getResources().getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int boton) {

                        //barraProgreso.setVisibility(View.VISIBLE);

                        final ProgressDialog pDialog = new ProgressDialog(MainActivity.this);
                        pDialog.setTitle("ACTUALIZAR CONTACTOS");
                        pDialog.setIcon(R.mipmap.ic_launcher);
                        pDialog.setMessage("Actualizando los contactos espera por favor...");
                        pDialog.show();

                        // ---Hacer alg�n trabajo en el hilo de fondo---
                        final ImportarContactos2 importarContactos2 = new ImportarContactos2(MainActivity.this, arrayListContactos);
                        new Thread(new Runnable() {
                            public void run() {

                                importarContactos2.importar();
                                // ---ocultar la barra de progreso---
                                manejador.post(new Runnable() {
                                    public void run() {
                                        // ---0 - VISIBLE; 4 - INVISIBLE; 8 - GONE---
                                        //barraProgreso.setVisibility(View.INVISIBLE);
                                        //consultar();
                                        recargarRecyclerView();
                                        pDialog.dismiss();

                                    }
                                });
                            }


                        }).start();


                    }//Fin onclick del alert

                });//Fin setPositiveButton


        dialogImportar.setNegativeButton(android.R.string.no,


                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int boton) {
                        barraProgreso.setVisibility(View.INVISIBLE);

                        return;

                    }
                });
        dialogImportar.show();


    }


    private void solicitarPermisoLlamadas() {
        //1-BREVE EXPLICACIÓN DE PARA QUÉ SE SOLICITAN LOS PERMISOS...
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CALL_PHONE)) {
            //4-Pequeña explicación de para qué queremos los permisos
            CoordinatorLayout contenedor = (CoordinatorLayout) findViewById(R.id.contenedor);//Para el contexto del snackbar
            Snackbar.make(contenedor, "La Aplicación no tiene permisos para realizar esta acción.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CALL_PHONE},
                                    SOLICITUD_ACCESS_CALL_PHONE);
                        }
                    })
                    .show();
        } else {
            //5-Se muetra cuadro de diálogo predeterminado del sistema para que concedamos o denegemos el permiso
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    SOLICITUD_ACCESS_CALL_PHONE);
        }
    }


    //2-GESTIONAMOS LA CONCESIÓN O NO DE LOS PERMISOS Y LA EXPLICACIÓN PARA QUE TENGAN QUE CONCEDERSE:
    private void solicitarPermisoImportContacts() {
        //1-BREVE EXPLICACIÓN DE PARA QUÉ SE SOLICITAN LOS PERMISOS...
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)) {
            //4-Pequeña explicación de para qué queremos los permisos
            CoordinatorLayout contenedor = (CoordinatorLayout) findViewById(R.id.contenedor);//Para el contexto del snackbar
            Snackbar.make(contenedor, "La Aplicación no tiene permisos para realizar esta acción.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.READ_CONTACTS},
                                    SOLICITUD_ACCESS_READ_CONTACTS);
                        }
                    })
                    .show();
        } else {
            //5-Se muetra cuadro de diálogo predeterminado del sistema para que concedamos o denegemos el permiso
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    SOLICITUD_ACCESS_READ_CONTACTS);
        }
    }

    //3-GESTIONAMOS EL RESULTADO DE LA ELECCIÓN DEL USUARIO EN LA CONCESIÓN DE PERMISOS...

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        //Si se preguntara por más permisos el resultado se gestionaría desde aquí.
        if (requestCode == SOLICITUD_ACCESS_READ_CONTACTS) {//6-Se ha concedido los permisos... procedemos a ejecutar el proceso
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //ABRE NUEVA ACTIVITY CON TAMAÑO ALERTDIALOG--FUNCIONA CORRECTAMENTE
                  /*  Intent i = new Intent(this, ImportarContactos.class);
                    startActivity(i);*/

                importarContactos();
            } else {//7-NO se han concedido los permisos. No se puede ejecutar el proceso. Se le informa de ello al usuario.

                /*Snackbar.make(vista, "Sin el permiso, no puedo realizar la" +
                        "acción", Snackbar.LENGTH_SHORT).show();*/
                //1-Seguimos el proceso de ejecucion sin esta accion: Esto lo recomienda Google
                //2-Cancelamos el proceso actual
                //3-Salimos de la aplicacion
                //Toast.makeText(this, "No se han concedido los permisos necesarios para poder importar los contactos a la Aplicación.", Toast.LENGTH_SHORT).show();
                permisosManualmente();
            }


        } else if (requestCode == SOLICITUD_ACCESS_CALL_PHONE) {//6-Se ha concedido los permisos... procedemos a ejecutar el proceso
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                //llamar(id_Contacto_Llamada);
                llamar(contactos);

            } else {//7-NO se han concedido los permisos. No se puede ejecutar el proceso. Se le informa de ello al usuario.

                /*Snackbar.make(vista, "Sin el permiso, no puedo realizar la" +
                        "acción", Snackbar.LENGTH_SHORT).show();*/
                //1-Seguimos el proceso de ejecucion sin esta accion: Esto lo recomienda Google
                //2-Cancelamos el proceso actual
                //3-Salimos de la aplicacion
                //Toast.makeText(this, "No se han concedido los permisos necesarios para poder realizar llamadas desde la Aplicación.", Toast.LENGTH_LONG).show();
                permisosManualmente();
            }
        }


    }


    private void permisosManualmente() {
        final CharSequence[] opciones = {"Si", "No"};
        final AlertDialog.Builder alertOpciones = new AlertDialog.Builder(MainActivity.this);
        alertOpciones.setTitle("¿Desea configurar los permisos de forma manual?");
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("Si")) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Los permisos no fueron aceptados", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            }
        });
        alertOpciones.show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_traer_contactos) {
            //MIGRAR DATOS DE LA AGENDA DE ANDROID=========================
            //Intent i = new Intent(this, ImportarContactos.class);
            //startActivity(i);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permisosPorAplicacion(contactos, 1);
            } else {

                //ABRE NUEVA ACTIVITY CON TAMAÑO ALERTDIALOG--FUNCIONA CORRECTAMENTE
                  /*  Intent i = new Intent(this, ImportarContactos.class);
                    startActivity(i);*/

                importarContactos();
            }

            return true;
        }

        if (id == R.id.menu_borrar_todos) {

            borrarTodos();

            return true;
        }

        if (id == R.id.menu_borrar_algunos) {
            //finish();
            Intent intent = new Intent(MainActivity.this, BorrarUsuarios.class);
            intent.putExtra(C_MODO, C_ELIMINAR);
            //startActivity(intent);
            startActivityForResult(intent, C_ELIMINAR);
            //consultar();

            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        switch (keyCode) {
            //Evitamos que funcione la tecla del menú que traen por defecto los samsung...
            case KeyEvent.KEYCODE_MENU:
                // Toast.makeText(this, "Menú presionado",
                //       Toast.LENGTH_LONG);
                //toolbar.canShowOverflowMenu();
                //toolbar.setFocusable(true);
                //toolbar.collapseActionView();

                return true;

            /*case KeyEvent.KEYCODE_BACK:
                //int contadorsalida=0;
                //Toast.makeText(this,"Has pulsado tecla atras",Toast.LENGTH_SHORT).show();
                contadorsalida++;
                Toast.makeText(this,"Pulsa otra vez para salir",Toast.LENGTH_SHORT).show();
                if(contadorsalida==2){
                    finish();
                }*/
        }

        return super.onKeyUp(keyCode, event);
    }


    @Override
    public void onBackPressed() {
/**
 * Cierra la app cuando se ha pulsado dos veces seguidas en un intervalo inferior a dos segundos.
 */

        if (back_pressed + 2000 > System.currentTimeMillis())
            super.onBackPressed();
        else
            Toast.makeText(getBaseContext(), R.string.agenda_salir, Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
        // super.onBackPressed();
    }

    /**
     * Para el buscador de la Toolbar
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * Para el buscador de la Toolbar
     */
    @Override
    public boolean onQueryTextChange(String newText) {

        //Pasamos dos parámetros. Uno para el filtrado y otro para el cambio de color de letra
        adaptadorBuscador.filter = newText;//Color de letra
        adaptadorBuscador.getFilter().filter(newText);//filtrado

        return false;//se cambia a true
    }

    /**
     * Called when a menu item with {@link #SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW}
     * is expanded.
     *
     * @param item Item that was expanded
     * @return true if the item should expand, false if expansion should be suppressed.
     */
    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    /**
     * Called when a menu item with {@link #SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW}
     * is collapsed.
     *
     * @param item Item that was collapsed
     * @return true if the item should collapse, false if collapsing should be suppressed.
     */
    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {


        return false;//Se cambia el return a true
    }
}
