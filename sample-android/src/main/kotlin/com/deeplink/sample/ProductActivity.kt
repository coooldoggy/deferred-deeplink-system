package com.deeplink.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.deeplink.sample.databinding.ActivityProductBinding

/**
 * 딥링크로 이동하는 상품 화면 예제
 */
class ProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 딥링크에서 전달된 데이터 처리
        val productId = intent.getStringExtra("productId") ?: "123"
        val discount = intent.getStringExtra("discount") ?: "0"
        
        binding.tvProductTitle.text = "상품 #$productId"
        binding.tvProductDescription.text = "딥링크를 통해 진입했습니다!"
        binding.tvDiscount.text = "할인: $discount%"
        
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}

