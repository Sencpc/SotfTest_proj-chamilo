# Pembagian Tugas Testing - Kelompok (4 Orang)

## 👤 Person 1 (2 halaman + Komponen Umum)

### ⭐ PENTING: Komponen Umum (Tes 1x saja, berlaku untuk semua halaman)

**Test sections:**

1. **Pengujian Navigasi & Header** - Tes sekali pada halaman mana saja, berlaku untuk semua halaman (basic navigation only, NOT including Global Search)
2. **Pengujian Footer** - Tes sekali pada halaman mana saja, berlaku untuk semua halaman

> **Catatan:** Test navbar dan footer ini cukup dilakukan sekali pada halaman apapun. Hasilnya berlaku untuk semua 10 halaman karena komponen ini sama di semua halaman. Global Search adalah fitur kompleks terpisah yang ditest oleh Person 2.

---

### Halaman 1: Homepage

- URL: https://chamilo.org/en/
- Sections: 6 test sections
  - Hero Section
  - Need Help?
  - Chamilo Universe
  - Do You Like Chamilo?
  - Acknowledgments
  - Link Validation

### Halaman 2: Demo

- URL: https://chamilo.org/en/demo/
- Sections: 4 test sections
  - Page Title
  - Wanna try Chamilo?
  - CTA Button
  - Content Verification

**Total sections:** 12 test sections (2 shared components + 10 page-specific)

---

## 👤 Person 2 (2 halaman + Global Search Feature)

### ⭐ Global Search Functionality (Complex Feature Test)

**Test sections:**

1. **Pengujian Global Search** - Test search functionality yang ada di header untuk search seluruh website
   - Verifikasi search box terlihat dan dapat diklik
   - Verifikasi placeholder text ditampilkan
   - Test search dengan keyword valid (misal: "Chamilo LMS")
   - Verifikasi hasil search muncul dan relevan
   - Test search dengan keyword yang tidak ada
   - Verifikasi pesan "no results" handling
   - Test search dengan special characters
   - Verifikasi search performance (loading time reasonable)

> **Catatan:** Global Search adalah fitur kompleks yang search seluruh website, bukan bagian dari basic navbar testing.

---

### Halaman 3: Forum

- URL: https://chamilo.org/en/forum/
- Sections: 6 test sections
  - Page Title
  - Need a hand with Chamilo?
  - Forum Image
  - Global Forum
  - Conference Banner
  - External Links Validation

### Halaman 4: Download

- URL: https://chamilo.org/en/download/
- Sections: 9 test sections
  - Page Title
  - Documentation Links
  - Version Information
  - Download Section Display
  - ZIP Download Option
  - TAR.GZ Download Option
  - Conference Banner
  - External Links Validation
  - Content Accuracy

**Total sections:** 16 test sections (1 complex feature + 15 page-specific)

- Page Title
- Year Headers
- 2025 Events
- 2024 Events
- 2023 Events
  **Total sections:** 16 test sections (1 complex feature + 15 page-specific)

---

## 👤 Person 3 (3 halaman)

### Halaman 5: Contact

- URL: https://chamilo.org/en/contact/
- Sections: 8 test sections
  - Page Title
  - Contact Info Section
  - Physical Address
  - Map Testing
  - Contact Form (done)
  - Form Submission
  - Form Validation (error handling)
  - Link Validation

### Halaman 6: Official Providers

- URL: https://chamilo.org/en/providers/
- Sections: 7 test sections
  - Page Title
  - Introduction Section
  - Find a Provider Tool
  - Provider Cards/List
  - Provider Details
  - Link Validation
  - Image Loading

### Halaman 7: Chamilo Association

- URL: https://chamilo.org/en/chamilo-2/
- Sections: 5 test sections
  - What Chamilo?
  - Board of Directors
  - Our Community Leaders
  - External Links Validation
  - Image Loading

**Total sections:** 20 test sections

---

## 👤 Person 4 (3 halaman)

### Halaman 8: Training & Certification

- URL: https://chamilo.org/en/training/
- Sections: 7 test sections
  - Page Title
  - CCHAPA Section
  - CHACOBU Section
  - CHATEBU Section
  - Certification Links
  - Content Verification
  - Link Validation

### Halaman 9: Contribute

- URL: https://chamilo.org/en/contribute/
- Sections: 8 test sections
  - Page Title
  - Membership Tiers (Bronze/Silver/Gold)
  - PayPal Integration
  - Budget Allocation
  - Download Info
  - Collaboration Options
  - Link Validation & Download
  - Content Info

### Halaman 10: Events

- URL: https://chamilo.org/en/eventos/
- Sections: 11 test sections
  - Page Title
  - Year Headers
  - 2025 Events
  - 2024 Events
  - 2023 Events
  - 2022 Events
  - 2021 Events
  - Previous Years Events
  - External Links Validation
  - Multilingual Content
  - Content Organization

**Total sections:** 26 test sections

---

## 📊 Ringkasan Pembagian

| Person   | Halaman   | Total Test Sections                       | Estimasi Waktu |
| -------- | --------- | ----------------------------------------- | -------------- |
| Person 1 | 2 halaman | 12 sections (shared components + 2 pages) | 40-50 menit    |
| Person 2 | 2 halaman | 16 sections (Global Search + 2 pages)     | 50-60 menit    |
| Person 3 | 3 halaman | 20 sections                               | 60-75 menit    |
| Person 4 | 3 halaman | 26 sections                               | 75-90 menit    |

---

## ⚡ Perubahan Penting

### ❌ Yang DIHAPUS (Redundan):

- **Navigasi & Header testing** pada setiap halaman → Hanya tes 1x di BAGIAN A
- **Footer testing** pada setiap halaman → Hanya tes 1x di BAGIAN A
- **Cross-Browser Compatibility** tests → Dihapus semua
- **Responsive Design** tests → Dihapus semua
- **Negative Testing Scenarios** → Dihapus semua (kecuali form validation)

### ✅ Yang DIPERTAHANKAN:

- **Konten spesifik** setiap halaman
- **Form validation** tests (error handling yang legitimate)
- **Link validation** per halaman (karena link berbeda-beda)
- **Functionality tests** untuk fitur unik setiap halaman

---

## 📝 Catatan Penting

1. **Person 1 bertanggung jawab untuk shared components**: Test navbar (basic navigation only) dan footer sekali saja pada halaman apapun, hasilnya berlaku untuk semua halaman
2. **Person 2 test Global Search**: Fitur kompleks yang search seluruh website - bukan bagian dari basic navbar testing
   - **Automated Test Suite**: GlobalSearchTest.java memiliki 12 test cases komprehensif:
     - 4 original tests (search input, valid query, no results, special chars)
     - 8 new comprehensive tests (exact match, partial match, case insensitive, empty, clickability, categorization, redirect, multiple searches)
     - Semua tests termasuk detailed logging dengan logSuccess() method
     - Run: `mvn test -Dtest=GlobalSearchTest`
3. **Tidak perlu duplicate**: Setiap orang fokus hanya pada konten unik halaman mereka
4. **Person 4 paling banyak**: Events page sangat comprehensive, tapi mereka dapat halaman lain yang lebih simple
5. **File lengkap**: Semua tetap ada di `CekList.md` - cukup cari halaman yang ditugaskan
6. **Browser**: Gunakan Brave sesuai setup project
7. **Language**: Hanya test versi English (/en/)

---

## 🎯 Tips Efisiensi

1. **Person 1 mulai duluan**: Karena mereka test shared components (navbar & footer), sebaiknya selesai dulu sebelum yang lain mulai
2. **Person 2 test Global Search di awal**: Fitur ini kompleks dan butuh waktu lebih lama
3. **Sequential testing**: Test halaman secara berurutan, tidak perlu loncat-loncat
4. **Screenshot**: Ambil screenshot untuk bug yang ditemukan
5. **Document clearly**: Catat semua masalah dengan detail (URL, langkah reproduksi, expected vs actual)
6. **Communication**: Koordinasi kalau ada halaman yang down atau masalah akses

---

**File Reference:** `CekList.md` (main file)  
**Last Updated:** 2026-01-03
