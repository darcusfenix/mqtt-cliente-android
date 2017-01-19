package com.example.darcusfenix.mqtt_cliente;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";
    private static final String URI = "tcp://activemq3870.cloudapp.net";
    private static final String TOPICO = "cemex/prospectos";

    MqttAndroidClient cliente;
    String mensajeAEnviar;
    TextView tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.texto);

        String clientId = MqttClient.generateClientId();
        mensajeAEnviar = "Hola! Soy un Android";
        cliente = new MqttAndroidClient(
                this.getApplicationContext(),
                URI,
                clientId);

        conectarse();

    }

    private void conectarse() {

        try {

            IMqttToken token = cliente.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Log.d(TAG, "Estoy conectado a MQTT");
                    suscribirse();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    Log.d(TAG, "Falló la conexión");
                    Log.d(TAG, exception.getMessage());

                }
            });

        } catch (MqttException e) {

            Log.d(TAG, e.getMessage());

        }

    }

    private void escucharMensajesARecibir() {

        cliente.setCallback(new MqttCallback() {

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                Log.d(TAG, "MENSAJE ENTRANTE POR: " + topic);
                String mensajeRecibido = message.toString();
                tv.setText(mensajeRecibido);
                Toast.makeText(getApplication(), mensajeRecibido, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }

        });

    }

    private void enviarMensaje(String mensaje) {

        byte[] encodedPayload = new byte[0];
        try {

            encodedPayload = mensaje.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);

            cliente.publish(TOPICO, message);

        } catch (UnsupportedEncodingException | MqttException e) {

            e.printStackTrace();

        }

    }

    public void suscribirse() {

        int qos = 1;
        try {

            IMqttToken subToken = cliente.subscribe(TOPICO, qos);
            subToken.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Log.d(TAG, "ESTOY SUSCRITO A: " + TOPICO);

                    // TODO, OPCIONAL, PARA PRUEBAS
                    enviarMensaje(mensajeAEnviar);

                    // TODO, IMPORTANTE
                    escucharMensajesARecibir();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                    Log.d(TAG, "ERROR DE CONEXIÓN AL TOPICO: " + TOPICO);

                }

            });

        } catch (MqttException e) {

            e.printStackTrace();

        }

    }

}
