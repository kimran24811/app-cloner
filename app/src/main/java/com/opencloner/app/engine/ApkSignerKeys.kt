package com.opencloner.app.engine

import android.util.Base64
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec

/**
 * Embedded unencrypted RSA 2048-bit Private Key and self-signed X.509 Certificate.
 * Eliminates all PKCS12 / BouncyCastle keystore decryption issues on Android OS.
 */
object ApkSignerKeys {

    // 2048-bit RSA Private Key in PKCS#8 DER (Base64)
    private const val PRIVATE_KEY_B64 =
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDNuplxCpwpl+X1pbe+5zCR69WF" +
        "+PUJIRk7fxlOy8Z3wBPwhcxq4Ki2XaIrbKrYcuy719rgDwoSZo9R/kaQanPYy702JHH4TqJYaqhp" +
        "3FngXi2w6HmvgVdkZ1kkYFUhS+68NmS5doRtsqPqabRrnpjKC7YJr2eer5lEx1JRO+FLFTqjSHwL" +
        "pswWAzCRjpOzC6IoXCzuQsBMjABT12XtD65La3ZgC+lF0JKbcv0I6VmoqQiY9sdIXG3yXLVUpAxc" +
        "VqhWUA0a0F1tE2wD9RietpYHQznTmfF15pdUTMD59dG4CI8xbc8ebSX4e2L7iHSnazWbUVwULyfe" +
        "Axs/soq+mJ2JAgMBAAECggEAIWtMfbZxg7gvHOZcoOBBXZLnzqmQdHkx9h5BaDFl/hHvq0Sw3jO8" +
        "dFNlgpRWdG7G0gTQg1EuG6sZYQJjSBZi1zbRd7gX43QynxZEYV3wI6ho17IMAlyKa772Fhin+46w" +
        "K1bSqeX8wS0hv3SIYGsU+s1INu6NcJOp3Chc0OFfSajQWLS4T8zCdvWCpyWpEspeh0qNklIfEhBe" +
        "Nm+Xb0zw32tMnMob1S5lcjE9Cwd3JepOe4Vx36w2cQKwFfP8u5kH0tpgbPAz3MwOR/9koLHVxo2Ge" +
        "J5M484xFepPs6yY+vzam+ogKC8x/B3mqs4MirNLAIX5GP5khHI2Z0LCAQYf2QKBgQD5pxy2ZB23" +
        "wHhc1DQgpOVxkHa/L1s4xT+jM0zW3srkySvdXmwE6a1G9W/djaxABFN5D2Z8kaWIstLuhcvMjY/U" +
        "80k/AAo9/bwe5YwPoPKVQd0otAXDf7iWBltFPryWYFp2h6F22rH3zjblIt+4obCdQpS8eSgZBsQH" +
        "YqkSv9yC6wKBgQDS9ZrLdhHl7lW912mLnrsN+XOF9rMrqoQY3RI7l7XA/NV0jLFVu4yav2ORdK+O" +
        "w4L/yn2+D/kqR9TGjVcN8fA/dHrGbRgmh1draoy0iZwGua5HOYfU16mhmLHRMXo88IYwTn/sSbXW" +
        "gf4EJ1KE47shm+ZIATCS+w7qd8XeDdU8WwKBgQDWE8z1QiiRl0QfJgCFNaFinqSVEQDCT7JgOe9c" +
        "GbQWN8ObkgpCtDbYMEL60y+vvYXuu5wAmcw+yklCLA00nPeHfg+DLH+b5ObZ/7D4/eTGxLHm42hF" +
        "2lHopA7D9CY3NOyd03RLvQD7JCPCnQMHCe+vCpcMf0A5JaoXQy0EXO20fQKBgC9rC2dOW8mOH/Gz" +
        "GMNMY4lJl+D1yeI3ysd38aA5gp4ldqli8n1t0bX67RuF2exCIBD05/yvxHbvfHFA1d+JkVICEZLG" +
        "dNY8N7cqVQRDpjGQzQ6vbMu3yiZLlzSJZYuLGO6ExpZLHzdfMsSmPhMmySpNy55FvjBbhEfJsk0u" +
        "WM7fAoGAS3H0nxE8836pZEQY85LICM2DvpSFvJyOKGXNbgJ2sfxUw9l3Ug5G2BTvfyY21CbCgurE" +
        "7KG6TdoEMK6NdIqe4QHYaHILQ7ZrA1x9rwzD7+2I2+HNzAN9yfNpPyeeUOGGRG4sT2a69GNboegT" +
        "yKuS61Zet9Fw4hBf96OhSyUYhDQ="

    // X.509 Certificate in DER (Base64) - Valid until 2050
    private const val CERTIFICATE_B64 =
        "MIIC/jCCAeagAwIBAgICBTkwDQYJKoZIhvcNAQELBQAwQTEdMBsGA1UEAwwUT3BlbkNsb25lciBE" +
        "ZWJ1ZyBLZXkxEzARBgNVBAoMCk9wZW5DbG9uZXIxCzAJBgNVBAYTAlVTMCAXDTIwMDEwMTAwMDAw" +
        "MFoYDzIwNTAwMTAxMDAwMDAwWjBBMR0wGwYDVQQDDBRPcGVuQ2xvbmVyIERlYnVnIEtleTETMBEG" +
        "A1UECgwKT3BlbkNsb25lcjELMAkGA1UEBhMCVVMwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEK" +
        "AoIBAQDNuplxCpwpl+X1pbe+5zCR69WF+PUJIRk7fxlOy8Z3wBPwhcxq4Ki2XaIrbKrYcuy719rg" +
        "DwoSZo9R/kaQanPYy702JHH4TqJYaqhp3FngXi2w6HmvgVdkZ1kkYFUhS+68NmS5doRtsqPqabRr" +
        "npjKC7YJr2eer5lEx1JRO+FLFTqjSHwLpswWAzCRjpOzC6IoXCzuQsBMjABT12XtD65La3ZgC+lF" +
        "0JKbcv0I6VmoqQiY9sdIXG3yXLVUpAxcVqhWUA0a0F1tE2wD9RietpYHQznTmfF15pdUTMD59dG4" +
        "CI8xbc8ebSX4e2L7iHSnazWbUVwULyfeAxs/soq+mJ2JAgMBAAEwDQYJKoZIhvcNAQELBQADggEB" +
        "ADhbAkJmJxWIQV7qA5sNEF/w4v+1VO6lyvJCbdAQaMf4E+8b9XViXa6DDjLuzyHFyPUthkQFj9ib" +
        "xTEV235nT0128xw70AHtvlRGjc2J3VdSyxt/Q+SdJ0vT4w8RlMehJQnl0JoFB/kXGkgJScF+YbN6" +
        "rTwxjL/4tn3Z4lJthILM7GvBc/o/6OqSjNJ64Lyr20xW4xHqulP/HWwwvDQgZ9Qtj+8Va/MuJYwy" +
        "ZakPk9BmuzEYKd+uZB0bfz/xeQsbhBTrIs0vUnROOGlI8pjwJv0+UeJKI3W5pXVAxS0HrbR1Zelj" +
        "JZhw39VtjDVRlmK5aZUR7Ajltt0iB3M2lZ03ci0="

    fun getPrivateKey(): PrivateKey {
        val privBytes = Base64.decode(PRIVATE_KEY_B64, Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(privBytes)
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    }

    fun getCertificate(): X509Certificate {
        val certBytes = Base64.decode(CERTIFICATE_B64, Base64.DEFAULT)
        val certFactory = CertificateFactory.getInstance("X.509")
        return certFactory.generateCertificate(ByteArrayInputStream(certBytes)) as X509Certificate
    }
}
