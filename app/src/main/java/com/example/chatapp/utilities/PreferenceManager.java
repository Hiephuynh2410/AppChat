package com.example.chatapp.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private final SharedPreferences sharedPreferences;

    //Đây là phương thức khởi tạo của lớp PreferenceManager. Nó nhận một tham số là Context để có thể truy cập và sử dụng SharedPreferences.
    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(constant.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    // Phương thức này được sử dụng để lưu trữ một giá trị kiểu Boolean vào SharedPreferences.
    // Nó nhận một tham số key là tên khóa (key) và một tham số value là giá trị kiểu Boolean cần lưu trữ.
    // Phương thức này sẽ tạo một SharedPreferences.Editor, lưu giá trị Boolean vào SharedPreferences và áp dụng các thay đổi.
    public void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    //Phương thức này được sử dụng để truy xuất một giá trị kiểu Boolean từ SharedPreferences. Nó nhận một tham số key là tên khóa (key) cần truy xuất.
    // Phương thức này trả về giá trị Boolean tương ứng với khóa (key) được cung cấp. Nếu không tìm thấy giá trị, giá trị mặc định là false sẽ được trả về.
    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }


    //Phương thức này được sử dụng để lưu trữ một giá trị kiểu String vào SharedPreferences.
    // Nó nhận một tham số key là tên khóa (key) và một tham số value là giá trị kiểu String cần lưu trữ.
    // Phương thức này sẽ tạo một SharedPreferences.Editor, lưu giá trị String vào SharedPreferences và áp dụng các thay đổi.
    public void putString (String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    // Phương thức này được sử dụng để truy xuất một giá trị kiểu String từ SharedPreferences. Nó nhận một tham số key là tên khóa (key) cần truy xuất.
    // Phương thức này trả về giá trị String tương ứng với khóa (key) được cung cấp. Nếu không tìm thấy giá trị, giá trị mặc định là null sẽ được trả về.
    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

   // Phương thức này được sử dụng để xóa tất cả các giá trị được lưu trữ trong SharedPreferences.
   // Nó tạo một SharedPreferences.Editor, xóa tất cả các giá trị và áp dụng các thay đổi.
    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
