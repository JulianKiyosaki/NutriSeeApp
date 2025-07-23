# NutriSee 🍎📷

**NutriSee** adalah aplikasi mobile berbasis Android yang memanfaatkan teknologi **Deep Learning YOLOv8** untuk mendeteksi makanan melalui gambar dan memberikan informasi nutrisi secara otomatis.  
Aplikasi ini dikembangkan sebagai bagian dari inovasi kesehatan untuk memberdayakan masyarakat dalam memahami asupan gizi mereka sehari-hari.

---

## 📱 Fitur Utama

- 📸 **Deteksi Makanan Otomatis**  
  Pengguna dapat memotret makanan, kemudian aplikasi mendeteksi jenis makanan menggunakan model **YOLOv8**.

- 📊 **Informasi Nutrisi**  
  Setelah makanan terdeteksi, aplikasi menampilkan informasi kandungan gizi seperti kalori, protein, lemak, dan karbohidrat.

- 📂 **Manajemen Data**  
  Data hasil deteksi disimpan secara lokal dan dapat diakses kembali oleh pengguna.

---

## ⚙️ Teknologi yang Digunakan

- **Android (Kotlin)**
- **TensorFlow Lite** (untuk integrasi model YOLOv8)
- **Google Colab** (untuk training model)
- **Firebase / SQLite** (opsional, untuk penyimpanan data)
- **OpenCV** (jika diperlukan untuk preprocessing gambar)

---

## 🏗️ Arsitektur Aplikasi

1. **Model Training**  
   Dataset gambar makanan dilatih di Google Colab menggunakan YOLOv8. Model diekspor ke format `.tflite` agar kompatibel dengan Android.

2. **Integrasi Mobile**  
   Model TFLite diintegrasikan ke project Android menggunakan **TensorFlow Lite Interpreter**.

3. **Alur Kerja**  
   - Pengguna memilih/memotret gambar.
   - Gambar diproses dan dideteksi objek makanannya.
   - Bounding box dan label ditampilkan.
   - Informasi nutrisi diambil dari basis data dan ditampilkan ke pengguna.

---

## 🚀 Cara Menjalankan

1. **Clone Repository**
   ```bash
   git clone https://github.com/JulianKiyosaki/NutriSeeApp.git

   Buka di Android Studio

2. **Pastikan sudah terinstal Android Studio terbaru.**

- Buka folder project.
- Sinkronkan Gradle.

3. **Jalankan Aplikasi**

- Hubungkan perangkat Android.
- Jalankan aplikasi melalui Android Studio.
