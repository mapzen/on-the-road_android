package com.mapzen.http

import android.os.Build
import okhttp3.ConnectionSpec
import okhttp3.TlsVersion
import android.util.Log
import okhttp3.OkHttpClient
import java.security.KeyStore
import java.util.ArrayList
import java.util.Arrays
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


/**
 * Created by sarahlensing on 5/25/17.
 */
class Tls12OkHttpClientFactory {

  /**
   * Enables TLS 1.2 support for pre-lollipop on given [OkHttpClient.Builder].
   * @param client Client to enable TLS on.
   * *
   * @return TLS enabled client.
   */
  companion object {
    fun enableTls12OnPreLollipop(client: OkHttpClient.Builder): OkHttpClient.Builder {
      if (Build.VERSION.SDK_INT in 16..21) {
        try {
          val sc = SSLContext.getInstance("TLSv1.2")
          sc.init(null, null, null)

          val trustManagerFactory = TrustManagerFactory.getInstance(
              TrustManagerFactory.getDefaultAlgorithm())
          trustManagerFactory.init(null as KeyStore?)
          val trustManagers = trustManagerFactory.getTrustManagers()
          if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
            throw IllegalStateException(
                "Unexpected default trust managers:" + Arrays.toString(trustManagers))
          }
          val trustManager = trustManagers[0] as X509TrustManager
          client.sslSocketFactory(Tls12SocketFactory(sc.getSocketFactory()), trustManager)

          val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
              .tlsVersions(TlsVersion.TLS_1_2)
              .build()

          val specs = ArrayList<ConnectionSpec>()
          specs.add(cs)
          specs.add(ConnectionSpec.COMPATIBLE_TLS)
          specs.add(ConnectionSpec.CLEARTEXT)

          client.connectionSpecs(specs)
        } catch (exc: Exception) {
          Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc)
        }

      }
      return client
    }
  }
}
