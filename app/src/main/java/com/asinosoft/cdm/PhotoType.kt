package com.asinosoft.cdm

@Deprecated("Класс устарел!")
enum class PhotoType {
    Thum,
    Full;

    companion object{
        fun getInt(photoType: PhotoType) = if (photoType.equals(Thum)) 0 else 1

        fun getType(int: Int) = if (int.equals(0)) Thum else Full
    }
}