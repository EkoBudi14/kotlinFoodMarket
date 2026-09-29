# Setup Midtrans di App Kotlin FoodMarket

Panduan ini untuk menyambungkan app Android **kotlinFoodMarket** ke backend
**foodmarket-backend-temporary** (Vercel) supaya tombol **Checkout Now** membuka
halaman pembayaran Midtrans (Snap).

Semua kode di bawah sama persis dengan commit `7aa814f` di branch `midtrans`.

---

## Daftar isi

- [Cara tercepat: ambil dari GitHub](#cara-tercepat-ambil-dari-github)
- [Cara kerjanya](#cara-kerjanya)
- [Bagian A — Gradle, Manifest, local.properties](#bagian-a--gradle-manifest-localproperties) (Langkah 1–4)
- [Bagian B — Harga](#bagian-b--harga) (Langkah 5)
- [Bagian C — Koneksi ke backend](#bagian-c--koneksi-ke-backend) (Langkah 6–10)
- [Bagian D — Layar pembayaran](#bagian-d--layar-pembayaran) (Langkah 11–14)
- [Bagian E — Setting di luar app](#bagian-e--setting-di-luar-app) (Langkah 15–17)
- [Bagian F — Testing](#bagian-f--testing)
- [Kalau error](#kalau-error)

---

## Cara tercepat: ambil dari GitHub

Kodenya **sudah di-push**. Kalau nggak mau ketik ulang, cukup tarik saja:

1. Buka project di Android Studio.
2. Pastikan branch aktif `midtrans` (nama branch terlihat di pojok kiri atas / kanan bawah).
   Kalau belum: **Git → Branches… → Remote Branches → origin/midtrans → Checkout**.
3. **Git → Pull…** (atau lewat Terminal: `git pull origin midtrans`).
4. Lanjut ke **Langkah 4** (`local.properties`) dan **Bagian E**.

Kalau mau **belajar dengan mengetik sendiri**, ikuti Langkah 1–17 di bawah secara berurutan.

> **Catatan pemula**
> - Path seperti `app/src/main/java/com/example/foodmarketkotlin/...` paling gampang dilihat di panel kiri
>   Android Studio dengan mode **Project** (bukan *Android*). Ganti lewat dropdown di atas panel kiri.
>   Di mode *Android*, folder `com/example/foodmarketkotlin` tampil sebagai package `com.example.foodmarketkotlin`.
> - Angka di kiri kode (`  1`, `  2`, …) adalah **nomor baris**, **jangan ikut di-copy**.
>   Setiap kode bernomor ada versi tanpa nomor untuk di-copy.
> - Kalau ada tulisan merah setelah paste, biasanya karena file lain belum dibuat.
>   Selesaikan semua langkah dulu, lalu **Build → Rebuild Project**.

---

## Cara kerjanya

```
[App] tombol Checkout Now
   │  POST /api/create-transaction  (productId, qty + token login Firebase)
   ▼
[Backend Vercel] hitung harga → simpan orders/{orderId} (PENDING) → minta token ke Midtrans
   │  balas: orderId, redirectUrl
   ▼
[App] MidtransPaymentFragment buka redirectUrl (halaman Snap) di WebView
   │  user bayar (kartu / GoPay / ShopeePay / VA …)
   ▼
[Midtrans] kirim notifikasi → [Backend] /api/notification → orders/{orderId}.status = PAID
   │
   ▼
[App] dengar Firestore orders/{orderId} → PAID → pindah ke PaymentSuccessFragment
```

Kenapa harga dihitung di backend? Supaya user nggak bisa mengakali harga dari HP.
Angka di layar app cuma untuk tampilan.

### Daftar file

| Langkah | File | Status |
|---|---|---|
| 1 | `gradle/libs.versions.toml` | diubah (1 baris) |
| 2 | `app/build.gradle.kts` | diubah |
| 3 | `app/src/main/AndroidManifest.xml` | diubah (1 baris) |
| 4 | `local.properties` | diubah (1 baris, **tidak** masuk git) |
| 5 | `app/src/main/java/com/example/foodmarketkotlin/utils/priceExt.kt` | diubah |
| 6 | `app/src/main/java/com/example/foodmarketkotlin/data/model/request/CreateTransactionRequest.kt` | **baru** |
| 7 | `app/src/main/java/com/example/foodmarketkotlin/data/model/response/CreateTransactionResponse.kt` | **baru** |
| 8 | `app/src/main/java/com/example/foodmarketkotlin/data/remote/BackendApiService.kt` | **baru** |
| 9 | `app/src/main/java/com/example/foodmarketkotlin/data/remote/BackendInstance.kt` | **baru** |
| 10a | `app/src/main/java/com/example/foodmarketkotlin/data/repository/PaymentRepository.kt` | **baru** |
| 10b | `app/src/main/java/com/example/foodmarketkotlin/viewModel/PaymentViewModel.kt` | **baru** |
| 11 | `app/src/main/res/layout/fragment_midtrans_payment.xml` | **baru** |
| 12 | `app/src/main/java/com/example/foodmarketkotlin/ui/detail/MidtransPaymentFragment.kt` | **baru** |
| 13 | `app/src/main/res/navigation/nav_detail.xml` | diubah |
| 14 | `app/src/main/java/com/example/foodmarketkotlin/ui/detail/PaymentFragment.kt` | diubah |

---

## Bagian A — Gradle, Manifest, local.properties

### Langkah 1. Tambah library `coroutines-play-services`

📄 **File:** `gradle/libs.versions.toml`

Library ini dipakai supaya kita bisa menulis `.await()` untuk menunggu token login Firebase.

Cari baris `kotlinx-coroutines-android = ...` di bagian `[libraries]` (baris 45), lalu
**tambahkan baris 46** tepat di bawahnya:

```toml
 45  kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
 46  kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutines" }
```

Baris yang di-copy:

```toml
kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutines" }
```

Versinya ikut `coroutines = "1.7.3"` yang sudah ada di baris 19, jadi nggak perlu tambah versi baru.

### Langkah 2. Ubah `app/build.gradle.kts`

📄 **File:** `app/build.gradle.kts` (yang di dalam folder `app`, **bukan** yang di root project)

Ada 3 perubahan:

**a. Baris 1–2**: tambahkan import di paling atas file, sebelum `plugins {`:

```kotlin
  1  import java.util.Properties
  2  
  3  plugins {
```

**b. Baris 24–34**: di dalam `defaultConfig { ... }`, setelah baris `testInstrumentationRunner`,
tambahkan kode yang membaca URL backend dari `local.properties`:

```kotlin
 22          testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
 23  
 24          // URL backend Vercel (foodmarket-backend-temporary), harus diakhiri "/".
 25          // Isi di local.properties: BACKEND_BASE_URL=https://<project>.vercel.app/
 26          val localProps = Properties().apply {
 27              val file = rootProject.file("local.properties")
 28              if (file.exists()) file.inputStream().use { load(it) }
 29          }
 30          val backendBaseUrl = localProps.getProperty(
 31              "BACKEND_BASE_URL",
 32              "https://your-project.vercel.app/"
 33          )
 34          buildConfigField("String", "BACKEND_BASE_URL", "\"$backendBaseUrl\"")
 35      }
```

Hasilnya, di kode Kotlin kita bisa memanggil `BuildConfig.BACKEND_BASE_URL`.
`buildConfig = true` di baris 57 **sudah ada** dari sebelumnya, jangan dihapus
(tanpa itu class `BuildConfig` nggak dibuat).

**c. Baris 82**: di bagian `dependencies`, di bawah `kotlinx.coroutines.android`:

```kotlin
 80      // Coroutines
 81      implementation(libs.kotlinx.coroutines.android)
 82      implementation(libs.kotlinx.coroutines.play.services)
```

Isi lengkap `app/build.gradle.kts` (107 baris) supaya bisa dicocokkan, atau ganti seluruh isinya dengan ini:

```kotlin
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.example.foodmarketkotlin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.foodmarketkotlin"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // URL backend Vercel (foodmarket-backend-temporary), harus diakhiri "/".
        // Isi di local.properties: BACKEND_BASE_URL=https://<project>.vercel.app/
        val localProps = Properties().apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) file.inputStream().use { load(it) }
        }
        val backendBaseUrl = localProps.getProperty(
            "BACKEND_BASE_URL",
            "https://your-project.vercel.app/"
        )
        buildConfigField("String", "BACKEND_BASE_URL", "\"$backendBaseUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    // Optional: Tambahkan ini juga untuk View Binding sebagai alternatif
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.glide)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Coil
    implementation(libs.coil)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)


    // Google Play In-App Review
    implementation(libs.play.review)
    implementation(libs.play.review.ktx)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

### Langkah 3. Izin internet di Manifest

📄 **File:** `app/src/main/AndroidManifest.xml`

Tambahkan **baris 5**, di antara `<manifest ...>` dan `<application`:

```xml
  1  <?xml version="1.0" encoding="utf-8"?>
  2  <manifest xmlns:android="http://schemas.android.com/apk/res/android"
  3      xmlns:tools="http://schemas.android.com/tools">
  4  
  5      <uses-permission android:name="android.permission.INTERNET" />
  6  
  7      <application
```

Baris yang di-copy:

```xml
    <uses-permission android:name="android.permission.INTERNET" />
```

### Langkah 4. Isi URL backend di `local.properties`

📄 **File:** `local.properties` (di **root** project, sejajar `settings.gradle.kts`)

File ini **sengaja tidak masuk git** (ada di `.gitignore`), jadi setiap laptop harus isi sendiri.
Tambahkan baris ini di paling bawah. Ganti dengan domain Vercel kamu, dan **wajib diakhiri `/`**:

```properties
BACKEND_BASE_URL=https://nama-project-kamu.vercel.app/
```

Contoh isi `local.properties` setelah ditambah (baris `sdk.dir` biarkan seperti punyamu):

```properties
sdk.dir=C\:\\Users\\kamu\\AppData\\Local\\Android\\Sdk
BACKEND_BASE_URL=https://nama-project-kamu.vercel.app/
```

Domain Vercel-nya bisa dilihat di vercel.com → project backend → **Domains**.

✅ Setelah Langkah 1–4: klik **Sync Now** (bar kuning di atas editor) atau
**File → Sync Project with Gradle Files**.

---

## Bagian B — Harga

### Langkah 5. Tambah ongkir & pajak

📄 **File:** `app/src/main/java/com/example/foodmarketkotlin/utils/priceExt.kt`

Nilainya **harus sama** dengan backend (`DRIVER_FEE=10000`, `TAX_PERCENT=10`). Kalau beda,
angka di layar app nggak cocok dengan yang ditagih Midtrans.

Yang baru: baris 8, 10–11 (konstanta), 16 dan 18 (fungsi `taxOf` dan `totalOf`):

```kotlin
  1  package com.example.foodmarketkotlin.utils
  2  
  3  import com.example.foodmarketkotlin.data.model.response.Product
  4  import java.text.NumberFormat
  5  import java.util.Locale
  6  import kotlin.math.roundToLong
  7  
  8  // Harus sama dengan USD_TO_IDR / DRIVER_FEE / TAX_PERCENT di backend (api/create-transaction.js).
  9  const val USD_TO_IDR = 16000
 10  const val DRIVER_FEE = 10000L
 11  const val TAX_PERCENT = 10
 12  
 13  val Product.priceIDR: Long
 14      get() = (price * USD_TO_IDR).roundToLong()
 15  
 16  fun taxOf(subtotal: Long): Long = (subtotal * TAX_PERCENT / 100.0).roundToLong()
 17  
 18  fun totalOf(subtotal: Long): Long = subtotal + DRIVER_FEE + taxOf(subtotal)
 19  
 20  fun Long.toRupiah(): String =
 21      "Rp." + NumberFormat.getNumberInstance(Locale("in", "ID")).format(this)
```

Paling gampang, **ganti seluruh isi file** dengan ini:

```kotlin
package com.example.foodmarketkotlin.utils

import com.example.foodmarketkotlin.data.model.response.Product
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

// Harus sama dengan USD_TO_IDR / DRIVER_FEE / TAX_PERCENT di backend (api/create-transaction.js).
const val USD_TO_IDR = 16000
const val DRIVER_FEE = 10000L
const val TAX_PERCENT = 10

val Product.priceIDR: Long
    get() = (price * USD_TO_IDR).roundToLong()

fun taxOf(subtotal: Long): Long = (subtotal * TAX_PERCENT / 100.0).roundToLong()

fun totalOf(subtotal: Long): Long = subtotal + DRIVER_FEE + taxOf(subtotal)

fun Long.toRupiah(): String =
    "Rp." + NumberFormat.getNumberInstance(Locale("in", "ID")).format(this)
```

Contoh hitungan: harga Rp159.840 → `taxOf(159840)` = Rp15.984 →
`totalOf(159840)` = 159.840 + 10.000 + 15.984 = **Rp185.824**.

---

## Bagian C — Koneksi ke backend

Urutannya: **model data → API (Retrofit) → Repository → ViewModel**. Ini pola yang sama dengan
`FoodApiService` / `FoodInstance` / `FoodRepository` / `HomeViewModel` yang sudah ada di project kamu.

### Langkah 6. Model request

📄 **File baru:** `app/src/main/java/com/example/foodmarketkotlin/data/model/request/CreateTransactionRequest.kt`

Data yang dikirim app ke backend. Folder `request` **belum ada**: klik kanan folder `data/model` → **New → Package** → ketik `request`.

**Cara buat:** klik kanan folder tujuan → **New → Kotlin Class/File** → pilih **File** → ketik `CreateTransactionRequest` → Enter. Hapus isi bawaannya, lalu **copy-paste seluruh kode di bawah** (8 baris).

```kotlin
package com.example.foodmarketkotlin.data.model.request

import com.google.gson.annotations.SerializedName

data class CreateTransactionRequest(
    @SerializedName("productId") val productId: Int,
    @SerializedName("qty") val qty: Int = 1
)
```

### Langkah 7. Model response

📄 **File baru:** `app/src/main/java/com/example/foodmarketkotlin/data/model/response/CreateTransactionResponse.kt`

Data yang dibalas backend. `ErrorResponse` dipakai untuk membaca pesan error (misal `"productId is required"`). Taruh di folder `data/model/response` (sejajar `FoodResponse.kt`).

**Cara buat:** klik kanan folder tujuan → **New → Kotlin Class/File** → pilih **File** → ketik `CreateTransactionResponse` → Enter. Hapus isi bawaannya, lalu **copy-paste seluruh kode di bawah** (14 baris).

```kotlin
package com.example.foodmarketkotlin.data.model.response

import com.google.gson.annotations.SerializedName

data class CreateTransactionResponse(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("token") val token: String,
    @SerializedName("redirectUrl") val redirectUrl: String,
    @SerializedName("grossAmount") val grossAmount: Long
)

data class ErrorResponse(
    @SerializedName("message") val message: String? = null
)
```

### Langkah 8. Interface API

📄 **File baru:** `app/src/main/java/com/example/foodmarketkotlin/data/remote/BackendApiService.kt`

Mendefinisikan endpoint `POST api/create-transaction`. Header `Authorization` berisi token login Firebase. Taruh sejajar `FoodApiService.kt`.

> Perhatikan `"api/create-transaction"` **tanpa** `/` di depan, karena base URL-nya sudah diakhiri `/`.

**Cara buat:** klik kanan folder tujuan → **New → Kotlin Class/File** → pilih **File** → ketik `BackendApiService` → Enter. Hapus isi bawaannya, lalu **copy-paste seluruh kode di bawah** (16 baris).

```kotlin
package com.example.foodmarketkotlin.data.remote

import com.example.foodmarketkotlin.data.model.request.CreateTransactionRequest
import com.example.foodmarketkotlin.data.model.response.CreateTransactionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface BackendApiService {
    @POST("api/create-transaction")
    suspend fun createTransaction(
        @Header("Authorization") authorization: String,
        @Body body: CreateTransactionRequest
    ): Response<CreateTransactionResponse>
}
```

### Langkah 9. Retrofit untuk backend

📄 **File baru:** `app/src/main/java/com/example/foodmarketkotlin/data/remote/BackendInstance.kt`

Mirip `FoodInstance.kt`, tapi base URL-nya dari `BuildConfig.BACKEND_BASE_URL` (Langkah 2 & 4) dan timeout dinaikkan jadi 30 detik karena Vercel kadang lambat saat pertama dipanggil (*cold start*).

> Kalau `BuildConfig` merah: pastikan Langkah 2 sudah, lalu **Build → Rebuild Project**.

**Cara buat:** klik kanan folder tujuan → **New → Kotlin Class/File** → pilih **File** → ketik `BackendInstance` → Enter. Hapus isi bawaannya, lalu **copy-paste seluruh kode di bawah** (32 baris).

```kotlin
package com.example.foodmarketkotlin.data.remote

import com.example.foodmarketkotlin.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object BackendInstance {

    val api: BackendApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        // Vercel cold start + request ke Midtrans bisa agak lama.
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(BackendApiService::class.java)
    }
}
```

### Langkah 10a. Repository

📄 **File baru:** `app/src/main/java/com/example/foodmarketkotlin/data/repository/PaymentRepository.kt`

Tugasnya: ambil token login Firebase user → panggil backend → kembalikan `Result.success(...)` atau `Result.failure(...)`. Taruh sejajar `FoodRepository.kt`.

`.await()` di baris 25 butuh library dari Langkah 1.

**Cara buat:** klik kanan folder tujuan → **New → Kotlin Class/File** → pilih **File** → ketik `PaymentRepository` → Enter. Hapus isi bawaannya, lalu **copy-paste seluruh kode di bawah** (46 baris).

```kotlin
package com.example.foodmarketkotlin.data.repository

import com.example.foodmarketkotlin.data.model.request.CreateTransactionRequest
import com.example.foodmarketkotlin.data.model.response.CreateTransactionResponse
import com.example.foodmarketkotlin.data.model.response.ErrorResponse
import com.example.foodmarketkotlin.data.remote.BackendApiService
import com.example.foodmarketkotlin.data.remote.BackendInstance
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

class PaymentRepository(
    private val apiService: BackendApiService = BackendInstance.api,
    private val auth: FirebaseAuth = Firebase.auth
) {

    suspend fun createTransaction(productId: Int, qty: Int = 1): Result<CreateTransactionResponse> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("Silakan login terlebih dahulu"))

            // Backend memverifikasi Firebase ID token ini, bukan uid dari client.
            val idToken = user.getIdToken(false).await().token
                ?: return Result.failure(Exception("Gagal mengambil token login"))

            val response = apiService.createTransaction(
                "Bearer $idToken",
                CreateTransactionRequest(productId, qty)
            )
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val message = runCatching {
                    Gson().fromJson(response.errorBody()?.string(), ErrorResponse::class.java)?.message
                }.getOrNull()
                Result.failure(Exception(message ?: "Gagal membuat transaksi (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Langkah 10b. ViewModel

📄 **File baru:** `app/src/main/java/com/example/foodmarketkotlin/viewModel/PaymentViewModel.kt`

Menyimpan status checkout: `Idle` (diam), `Loading` (sedang proses), `Success` (dapat redirectUrl), `Error`. Pola sama dengan `HomeViewModel.kt`. Taruh di folder `viewModel`.

**Cara buat:** klik kanan folder tujuan → **New → Kotlin Class/File** → pilih **File** → ketik `PaymentViewModel` → Enter. Hapus isi bawaannya, lalu **copy-paste seluruh kode di bawah** (42 baris).

```kotlin
package com.example.foodmarketkotlin.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmarketkotlin.data.model.response.CreateTransactionResponse
import com.example.foodmarketkotlin.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()
    data class Success(val transaction: CreateTransactionResponse) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}

class PaymentViewModel(private val repository: PaymentRepository = PaymentRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)

    val uiState: StateFlow<PaymentUiState> = _uiState

    fun checkout(productId: Int, qty: Int = 1) {
        if (_uiState.value is PaymentUiState.Loading) return
        _uiState.value = PaymentUiState.Loading

        viewModelScope.launch {
            repository.createTransaction(productId, qty)
                .onSuccess {
                    _uiState.value = PaymentUiState.Success(it)
                }
                .onFailure {
                    _uiState.value = PaymentUiState.Error(it.message ?: "Unknown error")
                }
        }
    }

    // Dipanggil setelah Success/Error ditangani supaya tidak terpicu lagi saat view dibuat ulang.
    fun resetState() {
        _uiState.value = PaymentUiState.Idle
    }
}
```

---

## Bagian D — Layar pembayaran

### Langkah 11. Layout WebView

📄 **File baru:** `app/src/main/res/layout/fragment_midtrans_payment.xml`

Isinya WebView (untuk halaman Midtrans) dan ProgressBar (loading).

**Cara buat:** klik kanan folder `res/layout` → **New → Layout Resource File** → nama `fragment_midtrans_payment` → OK → pindah ke tab **Code** (pojok kanan atas editor). Hapus isi bawaannya, lalu **copy-paste seluruh kode di bawah** (19 baris).

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ui.detail.MidtransPaymentFragment">

    <WebView
        android:id="@+id/webView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center" />

</FrameLayout>
```

### Langkah 12. Fragment pembayaran Midtrans

📄 **File baru:** `app/src/main/java/com/example/foodmarketkotlin/ui/detail/MidtransPaymentFragment.kt`

Fragment ini yang membuka halaman Midtrans. Taruh di folder `ui/detail` (sejajar `PaymentFragment.kt`).

**Cara buat:** klik kanan folder tujuan → **New → Kotlin Class/File** → pilih **File** → ketik `MidtransPaymentFragment` → Enter. Hapus isi bawaannya, lalu **copy-paste seluruh kode di bawah** (176 baris).

```kotlin
package com.example.foodmarketkotlin.ui.detail

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.databinding.FragmentMidtransPaymentBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

/**
 * Menampilkan halaman Midtrans Snap (redirectUrl dari backend) di WebView.
 *
 * Status pembayaran diambil dari Firestore `orders/{orderId}`, yang di-update backend
 * lewat webhook Midtrans (/api/notification). Jadi app tidak menebak status dari URL.
 */
class MidtransPaymentFragment : Fragment() {

    private var _binding: FragmentMidtransPaymentBinding? = null
    private val binding get() = _binding!!

    private var orderId: String = ""
    private var orderListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMidtransPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as DetailActivity).toolbarPayment()

        orderId = requireArguments().getString(ARG_ORDER_ID).orEmpty()
        val redirectUrl = requireArguments().getString(ARG_REDIRECT_URL).orEmpty()

        setupWebView(redirectUrl)
        setupBackPress()
    }

    // Listener hanya aktif saat layar terlihat. Waktu user kembali dari app e-wallet,
    // Firestore langsung mengirim status terbaru sehingga navigasi tidak terlewat.
    override fun onStart() {
        super.onStart()
        listenOrderStatus()
    }

    override fun onStop() {
        super.onStop()
        orderListener?.remove()
        orderListener = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(url: String) {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                openExternalIfNeeded(request.url)

            override fun onPageFinished(view: WebView?, url: String?) {
                _binding?.progressBar?.visibility = View.GONE
            }
        }
        binding.webView.loadUrl(url)
    }

    /**
     * GoPay / ShopeePay / QRIS dsb. membuka app e-wallet lewat deeplink (gojek://, shopeeid://,
     * intent://). WebView tidak bisa membuka skema itu, jadi diteruskan ke Android.
     */
    private fun openExternalIfNeeded(uri: Uri): Boolean {
        val scheme = uri.scheme ?: return false
        if (scheme == "http" || scheme == "https") return false

        try {
            val intent = if (scheme == "intent") {
                // Batasi ke intent yang memang boleh dibuka dari web.
                Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    component = null
                    selector = null
                }
            } else {
                Intent(Intent.ACTION_VIEW, uri)
            }
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                if (fallbackUrl != null) {
                    binding.webView.loadUrl(fallbackUrl)
                } else {
                    Toast.makeText(context, "Aplikasi pembayaran tidak terpasang", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak bisa membuka link pembayaran", Toast.LENGTH_SHORT).show()
        }
        return true
    }

    private fun setupBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.webView.canGoBack()) {
                        binding.webView.goBack()
                    } else {
                        findNavController().popBackStack()
                    }
                }
            }
        )
    }

    private fun listenOrderStatus() {
        if (orderId.isEmpty() || orderListener != null) return

        orderListener = Firebase.firestore.collection("orders").document(orderId)
            .addSnapshotListener { document, error ->
                if (error != null || document == null || _binding == null) {
                    return@addSnapshotListener
                }

                when (document.getString("status")) {
                    STATUS_PAID -> {
                        orderListener?.remove()
                        orderListener = null
                        findNavController().navigate(R.id.action_payment_success)
                    }

                    STATUS_EXPIRED, STATUS_CANCELLED -> {
                        orderListener?.remove()
                        orderListener = null
                        Toast.makeText(context, "Pembayaran dibatalkan / kedaluwarsa", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.destroy()
        _binding = null
    }

    companion object {
        const val ARG_ORDER_ID = "orderId"
        const val ARG_REDIRECT_URL = "redirectUrl"

        // Nilai status dari backend (orders/{orderId}.status).
        private const val STATUS_PAID = "PAID"
        private const val STATUS_EXPIRED = "EXPIRED"
        private const val STATUS_CANCELLED = "CANCELLED"
    }
}
```

**Penjelasan per bagian `MidtransPaymentFragment.kt`:**

| Baris | Fungsi |
|---|---|
| 46–55 | `onViewCreated`: ambil `orderId` & `redirectUrl` dari halaman sebelumnya, buka WebView |
| 59–68 | `onStart` / `onStop`: pantau status order hanya saat layar tampil |
| 70–83 | `setupWebView`: aktifkan JavaScript (Snap butuh ini), load halaman Midtrans |
| 89–118 | `openExternalIfNeeded`: link `gojek://`, `shopeeid://`, `intent://` dibuka ke app e-wallet |
| 120–133 | Tombol back: mundur di dalam halaman Midtrans dulu, kalau sudah di awal baru keluar |
| 135–159 | `listenOrderStatus`: dengar Firestore `orders/{orderId}`; `PAID` → sukses, `EXPIRED`/`CANCELLED` → kembali |
| 161–165 | Bersihkan WebView saat layar ditutup |

`FragmentMidtransPaymentBinding` otomatis dibuat Android Studio dari layout di Langkah 11
(karena `viewBinding = true`). Kalau masih merah, **Build → Rebuild Project**.

### Langkah 13. Tambah layar baru ke navigasi

📄 **File:** `app/src/main/res/navigation/nav_detail.xml` (buka tab **Code**)

Perubahannya:
- **Baris 25–26**: di `fragmentPayment`, action lama `action_payment_success` **diganti**
  jadi `action_payment_midtrans` (menuju layar Midtrans).
- **Baris 31–49**: `fragmentMidtransPayment` baru. `action_payment_success` sekarang **pindah** ke sini,
  ditambah `popUpTo` supaya setelah sukses tombol back nggak balik ke halaman bayar.

```xml
  1  <?xml version="1.0" encoding="utf-8"?>
  2  <navigation xmlns:android="http://schemas.android.com/apk/res/android"
  3      xmlns:app="http://schemas.android.com/apk/res-auto"
  4      xmlns:tools="http://schemas.android.com/tools"
  5      app:startDestination="@+id/fragmentDetail"
  6      android:id="@+id/nav_detail">
  7  
  8  
  9      <fragment
 10          android:id="@+id/fragmentDetail"
 11          android:name="com.example.foodmarketkotlin.ui.detail.DetailFragment"
 12          tools:layout="@layout/fragment_detail">
 13  
 14          <action android:id="@+id/action_payment"
 15              app:destination="@+id/fragmentPayment"/>
 16  
 17      </fragment>
 18  
 19  
 20      <fragment
 21          android:id="@+id/fragmentPayment"
 22          android:name="com.example.foodmarketkotlin.ui.detail.PaymentFragment"
 23          tools:layout="@layout/fragment_payment">
 24  
 25          <action android:id="@+id/action_payment_midtrans"
 26              app:destination="@+id/fragmentMidtransPayment"/>
 27  
 28      </fragment>
 29  
 30  
 31      <fragment
 32          android:id="@+id/fragmentMidtransPayment"
 33          android:name="com.example.foodmarketkotlin.ui.detail.MidtransPaymentFragment"
 34          tools:layout="@layout/fragment_midtrans_payment">
 35  
 36          <argument
 37              android:name="orderId"
 38              app:argType="string" />
 39  
 40          <argument
 41              android:name="redirectUrl"
 42              app:argType="string" />
 43  
 44          <!-- Setelah bayar, tombol back tidak kembali ke halaman Midtrans / Payment -->
 45          <action android:id="@+id/action_payment_success"
 46              app:destination="@+id/fragmentPaymentSuccess"
 47              app:popUpTo="@+id/fragmentDetail" />
 48  
 49      </fragment>
 50  
 51  
 52      <fragment
 53          android:id="@+id/fragmentPaymentSuccess"
 54          android:name="com.example.foodmarketkotlin.ui.detail.PaymentSuccessFragment"
 55          tools:layout="@layout/fragment_payment_success">
 56  
 57      </fragment>
 58  
 59  
 60  
 61  </navigation>
```

**Ganti seluruh isi file** dengan ini:

```xml
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    app:startDestination="@+id/fragmentDetail"
    android:id="@+id/nav_detail">


    <fragment
        android:id="@+id/fragmentDetail"
        android:name="com.example.foodmarketkotlin.ui.detail.DetailFragment"
        tools:layout="@layout/fragment_detail">

        <action android:id="@+id/action_payment"
            app:destination="@+id/fragmentPayment"/>

    </fragment>


    <fragment
        android:id="@+id/fragmentPayment"
        android:name="com.example.foodmarketkotlin.ui.detail.PaymentFragment"
        tools:layout="@layout/fragment_payment">

        <action android:id="@+id/action_payment_midtrans"
            app:destination="@+id/fragmentMidtransPayment"/>

    </fragment>


    <fragment
        android:id="@+id/fragmentMidtransPayment"
        android:name="com.example.foodmarketkotlin.ui.detail.MidtransPaymentFragment"
        tools:layout="@layout/fragment_midtrans_payment">

        <argument
            android:name="orderId"
            app:argType="string" />

        <argument
            android:name="redirectUrl"
            app:argType="string" />

        <!-- Setelah bayar, tombol back tidak kembali ke halaman Midtrans / Payment -->
        <action android:id="@+id/action_payment_success"
            app:destination="@+id/fragmentPaymentSuccess"
            app:popUpTo="@+id/fragmentDetail" />

    </fragment>


    <fragment
        android:id="@+id/fragmentPaymentSuccess"
        android:name="com.example.foodmarketkotlin.ui.detail.PaymentSuccessFragment"
        tools:layout="@layout/fragment_payment_success">

    </fragment>



</navigation>
```

### Langkah 14. Sambungkan tombol Checkout

📄 **File:** `app/src/main/java/com/example/foodmarketkotlin/ui/detail/PaymentFragment.kt`

| Baris | Apa yang berubah |
|---|---|
| 8, 10–13, 20, 22, 24–26, 30 | import baru |
| 39 | `viewModel` baru |
| 53 | panggil `observeCheckout()` |
| 55–58 | tombol Checkout sekarang memanggil backend (dulu langsung ke halaman sukses) |
| 61–89 | `observeCheckout()`: tombol jadi "Processing...", kalau sukses buka layar Midtrans, kalau gagal tampilkan Toast |
| 104–113 | angka Driver / Tax / Total pakai rumus yang benar (dulu semuanya = harga produk) |
| 155–157 | konstanta `QTY = 1` |

```kotlin
  1  package com.example.foodmarketkotlin.ui.detail
  2  
  3  import android.os.Bundle
  4  import android.view.LayoutInflater
  5  import android.view.View
  6  import android.view.ViewGroup
  7  import android.widget.Toast
  8  import androidx.core.os.bundleOf
  9  import androidx.fragment.app.Fragment
 10  import androidx.fragment.app.viewModels
 11  import androidx.lifecycle.Lifecycle
 12  import androidx.lifecycle.lifecycleScope
 13  import androidx.lifecycle.repeatOnLifecycle
 14  import androidx.navigation.fragment.findNavController
 15  import com.bumptech.glide.Glide
 16  import com.example.foodmarketkotlin.R
 17  import com.example.foodmarketkotlin.data.model.response.Product
 18  import com.example.foodmarketkotlin.databinding.FragmentPaymentBinding
 19  import com.example.foodmarketkotlin.util.parcelableOrNull
 20  import com.example.foodmarketkotlin.utils.DRIVER_FEE
 21  import com.example.foodmarketkotlin.utils.priceIDR
 22  import com.example.foodmarketkotlin.utils.taxOf
 23  import com.example.foodmarketkotlin.utils.toRupiah
 24  import com.example.foodmarketkotlin.utils.totalOf
 25  import com.example.foodmarketkotlin.viewModel.PaymentUiState
 26  import com.example.foodmarketkotlin.viewModel.PaymentViewModel
 27  import com.google.firebase.Firebase
 28  import com.google.firebase.auth.auth
 29  import com.google.firebase.firestore.firestore
 30  import kotlinx.coroutines.launch
 31  
 32  class PaymentFragment : Fragment() {
 33  
 34      private var _binding: FragmentPaymentBinding? = null
 35      private val binding get() = _binding!!
 36  
 37      private var productData: Product? = null
 38  
 39      private val viewModel: PaymentViewModel by viewModels()
 40  
 41      override fun onCreateView(
 42          inflater: LayoutInflater, container: ViewGroup?,
 43          savedInstanceState: Bundle?
 44      ): View {
 45          _binding = FragmentPaymentBinding.inflate(inflater, container, false)
 46          return binding.root
 47      }
 48  
 49      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 50          super.onViewCreated(view, savedInstanceState)
 51          (activity as DetailActivity).toolbarPayment()
 52          setData()
 53          observeCheckout()
 54  
 55          binding.btnCheckout.setOnClickListener {
 56              val product = productData ?: return@setOnClickListener
 57              viewModel.checkout(product.id, QTY)
 58          }
 59      }
 60  
 61      private fun observeCheckout() {
 62          viewLifecycleOwner.lifecycleScope.launch {
 63              viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
 64                  viewModel.uiState.collect { state ->
 65                      binding.btnCheckout.isEnabled = state !is PaymentUiState.Loading
 66                      binding.btnCheckout.text =
 67                          if (state is PaymentUiState.Loading) "Processing..." else "Checkout Now"
 68  
 69                      when (state) {
 70                          is PaymentUiState.Success -> {
 71                              viewModel.resetState()
 72                              val bundle = bundleOf(
 73                                  MidtransPaymentFragment.ARG_ORDER_ID to state.transaction.orderId,
 74                                  MidtransPaymentFragment.ARG_REDIRECT_URL to state.transaction.redirectUrl
 75                              )
 76                              findNavController().navigate(R.id.action_payment_midtrans, bundle)
 77                          }
 78  
 79                          is PaymentUiState.Error -> {
 80                              viewModel.resetState()
 81                              Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
 82                          }
 83  
 84                          else -> Unit
 85                      }
 86                  }
 87              }
 88          }
 89      }
 90  
 91  
 92      private fun setData() {
 93  //        productData = arguments?.parcelableOrNull<Product>("product")
 94  //            ?: IntentCompat.getParcelableExtra(requireActivity().intent, "foodResponse", Product::class.java)
 95  
 96          productData = arguments?.parcelableOrNull<Product>("product")
 97  
 98          val auth = Firebase.auth
 99          val db = Firebase.firestore
100          val currentUser = auth.currentUser?.uid
101  
102  
103          productData?.let { product ->
104              // Hanya untuk tampilan; total yang ditagih dihitung ulang di backend.
105              val subtotal = product.priceIDR * QTY
106              binding.tvTitle.text = product.title
107              binding.textView7.text = product.title
108              binding.tvPrice.text = product.priceIDR.toRupiah()
109              binding.tvHarga.text = subtotal.toRupiah()
110              binding.textView14.text = "$QTY items"
111              binding.textView12.text = DRIVER_FEE.toRupiah()
112              binding.tvTax.text = taxOf(subtotal).toRupiah()
113              binding.tvTotal.text = totalOf(subtotal).toRupiah()
114              Glide.with(requireContext())
115                  .load(product.thumbnail)
116                  .into(binding.ivPoster)
117          }
118  
119          if (currentUser != null) {
120              db.collection("users").document(currentUser).addSnapshotListener { document, error ->
121  
122                  if (error != null) {
123                      Toast.makeText(
124                          context,
125                          "Gagal mengambil data: ${error.message}",
126                          Toast.LENGTH_SHORT
127                      ).show()
128                      return@addSnapshotListener
129                  }
130  
131                  if (document != null && document.exists()) {
132                      val name = document.getString("fullName") ?: document.getString("name")
133                      val phone = document.getString("phone")
134                      val address = document.getString("address")
135                      val city = document.getString("city")
136  
137                      binding.tvName.text = name
138                      binding.textPhoneNo.text = phone
139                      binding.tvAddress.text = address
140                      binding.tvCity.text = city
141                  } else {
142                      Toast.makeText(context, "Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
143                  }
144              }
145          } else {
146              Toast.makeText(context, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
147          }
148      }
149  
150      override fun onDestroyView() {
151          super.onDestroyView()
152          _binding = null
153      }
154  
155      companion object {
156          private const val QTY = 1
157      }
158  }
```

Paling aman, **ganti seluruh isi file** dengan ini:

```kotlin
package com.example.foodmarketkotlin.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.foodmarketkotlin.R
import com.example.foodmarketkotlin.data.model.response.Product
import com.example.foodmarketkotlin.databinding.FragmentPaymentBinding
import com.example.foodmarketkotlin.util.parcelableOrNull
import com.example.foodmarketkotlin.utils.DRIVER_FEE
import com.example.foodmarketkotlin.utils.priceIDR
import com.example.foodmarketkotlin.utils.taxOf
import com.example.foodmarketkotlin.utils.toRupiah
import com.example.foodmarketkotlin.utils.totalOf
import com.example.foodmarketkotlin.viewModel.PaymentUiState
import com.example.foodmarketkotlin.viewModel.PaymentViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private var productData: Product? = null

    private val viewModel: PaymentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as DetailActivity).toolbarPayment()
        setData()
        observeCheckout()

        binding.btnCheckout.setOnClickListener {
            val product = productData ?: return@setOnClickListener
            viewModel.checkout(product.id, QTY)
        }
    }

    private fun observeCheckout() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.btnCheckout.isEnabled = state !is PaymentUiState.Loading
                    binding.btnCheckout.text =
                        if (state is PaymentUiState.Loading) "Processing..." else "Checkout Now"

                    when (state) {
                        is PaymentUiState.Success -> {
                            viewModel.resetState()
                            val bundle = bundleOf(
                                MidtransPaymentFragment.ARG_ORDER_ID to state.transaction.orderId,
                                MidtransPaymentFragment.ARG_REDIRECT_URL to state.transaction.redirectUrl
                            )
                            findNavController().navigate(R.id.action_payment_midtrans, bundle)
                        }

                        is PaymentUiState.Error -> {
                            viewModel.resetState()
                            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }


    private fun setData() {
//        productData = arguments?.parcelableOrNull<Product>("product")
//            ?: IntentCompat.getParcelableExtra(requireActivity().intent, "foodResponse", Product::class.java)

        productData = arguments?.parcelableOrNull<Product>("product")

        val auth = Firebase.auth
        val db = Firebase.firestore
        val currentUser = auth.currentUser?.uid


        productData?.let { product ->
            // Hanya untuk tampilan; total yang ditagih dihitung ulang di backend.
            val subtotal = product.priceIDR * QTY
            binding.tvTitle.text = product.title
            binding.textView7.text = product.title
            binding.tvPrice.text = product.priceIDR.toRupiah()
            binding.tvHarga.text = subtotal.toRupiah()
            binding.textView14.text = "$QTY items"
            binding.textView12.text = DRIVER_FEE.toRupiah()
            binding.tvTax.text = taxOf(subtotal).toRupiah()
            binding.tvTotal.text = totalOf(subtotal).toRupiah()
            Glide.with(requireContext())
                .load(product.thumbnail)
                .into(binding.ivPoster)
        }

        if (currentUser != null) {
            db.collection("users").document(currentUser).addSnapshotListener { document, error ->

                if (error != null) {
                    Toast.makeText(
                        context,
                        "Gagal mengambil data: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val name = document.getString("fullName") ?: document.getString("name")
                    val phone = document.getString("phone")
                    val address = document.getString("address")
                    val city = document.getString("city")

                    binding.tvName.text = name
                    binding.textPhoneNo.text = phone
                    binding.tvAddress.text = address
                    binding.tvCity.text = city
                } else {
                    Toast.makeText(context, "Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "User tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val QTY = 1
    }
}
```

✅ Setelah Langkah 1–14: **Build → Rebuild Project**. Harusnya nggak ada error merah lagi.

---

## Bagian E — Setting di luar app

### Langkah 15. Environment variable di Vercel

vercel.com → project backend → **Settings → Environment Variables**:

| Name | Value |
|---|---|
| `MIDTRANS_SERVER_KEY` | Server Key Sandbox (dashboard Midtrans → Settings → Access Keys, awalan `SB-Mid-server-`) |
| `MIDTRANS_IS_PRODUCTION` | `false` |
| `FIREBASE_SERVICE_ACCOUNT` | isi JSON service account Firebase dalam **satu baris** (Firebase Console → Project settings → Service accounts → Generate new private key) |
| `USD_TO_IDR` | `16000` |
| `DRIVER_FEE` | `10000` |
| `TAX_PERCENT` | `10` |

Lalu **Deployments → ⋯ → Redeploy**.

> ⚠️ Server Key **jangan pernah** ditaruh di app Kotlin. Cukup di Vercel.

### Langkah 16. Notification URL di Midtrans

Dashboard Midtrans **Sandbox** → **Settings → Payment → Notification URL**:

```
https://nama-project-kamu.vercel.app/api/notification
```

Tanpa ini status order nggak akan pernah jadi `PAID`, jadi app akan diam di halaman Midtrans
walaupun sudah bayar.

### Langkah 17. Firestore Rules

Firebase Console → **Firestore Database → Rules**. Tambahkan blok `orders` di dalam
`match /databases/{database}/documents { ... }`. Rules `users` yang sudah ada **biarkan**:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // ... rules users kamu yang sudah ada tetap di sini ...

    match /orders/{orderId} {
      // user hanya boleh membaca order miliknya sendiri
      allow read: if request.auth != null && resource.data.uid == request.auth.uid;
      // yang menulis hanya backend (Admin SDK tidak terkena rules)
      allow write: if false;
    }
  }
}
```

Klik **Publish**.

---

## Bagian F — Testing

1. Jalankan app (▶ Run), login, pilih makanan → **Order Now** → **Checkout Now**.
2. Tombol berubah jadi **Processing...**, lalu terbuka halaman Midtrans.
3. Pilih **Credit/Debit Card**, isi kartu tes Sandbox:
   - Nomor: `4811 1111 1111 1114`
   - Expiry: bulan/tahun di masa depan, misal `12/30`
   - CVV: `123`
   - OTP / 3DS: `112233`
4. Setelah bayar, dalam beberapa detik app pindah ke **"You've Made Order"**.
5. Cek Firestore → koleksi `orders` → dokumen terbaru, `status` = `PAID`.

Untuk GoPay / QRIS di Sandbox, pakai simulator: https://simulator.sandbox.midtrans.com

---

## Kalau error

| Gejala | Penyebab & solusi |
|---|---|
| `Unresolved reference: BuildConfig` / `BACKEND_BASE_URL` | Langkah 2 belum atau belum Sync. **File → Sync Project with Gradle Files**, lalu **Build → Rebuild Project** |
| `Unresolved reference: await` | Langkah 1 atau 2c belum, lalu Sync |
| `Unresolved reference: FragmentMidtransPaymentBinding` | Nama layout harus persis `fragment_midtrans_payment.xml` (Langkah 11), lalu Rebuild |
| `Unresolved reference: action_payment_midtrans` | Langkah 13 belum |
| Toast `Unable to resolve host "your-project.vercel.app"` | `BACKEND_BASE_URL` belum diisi di `local.properties` (Langkah 4). Sync lalu Run ulang |
| Crash `baseUrl must end in /` | URL di `local.properties` kurang `/` di akhir |
| Toast `Invalid or missing Firebase ID token` | User belum login, atau `FIREBASE_SERVICE_ACCOUNT` di Vercel dari project Firebase lain |
| Toast `Gagal membuat transaksi (500)` | Lihat log di Vercel (**Deployments → Logs**). Biasanya `MIDTRANS_SERVER_KEY` salah / kosong |
| Sudah bayar tapi app diam di halaman Midtrans | Notification URL belum diisi (Langkah 16), atau Firestore Rules menolak baca (Langkah 17). Di Logcat muncul `PERMISSION_DENIED` → Langkah 17 |
| GoPay: "Aplikasi pembayaran tidak terpasang" | Normal di emulator. Pakai QRIS + simulator, atau kartu tes |

Log request/response backend bisa dilihat di **Logcat** dengan filter `okhttp` (hanya di build debug).
