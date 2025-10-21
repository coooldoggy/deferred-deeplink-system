package com.deeplink.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.deeplink.sample.databinding.ActivityMainBinding
import com.deeplink.sdk.DeepLinkSDKHelper
import com.deeplink.sdk.models.DeepLinkResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        checkDeferredDeepLink()
    }
    
    private fun setupUI() {
        binding.btnCheckAgain.setOnClickListener {
            checkDeferredDeepLink()
        }
        
        binding.btnReset.setOnClickListener {
            resetAndCheck()
        }
        
        binding.btnOpenProduct.setOnClickListener {
            // 딥링크로 이동한 화면 예제
            startActivity(Intent(this, ProductActivity::class.java))
        }
        
        binding.btnTestLink.setOnClickListener {
            // 테스트용: 브라우저로 딥링크 열기
            openTestLink()
        }
    }
    
    private fun checkDeferredDeepLink() {
        showLoading(true)
        addLog("Deferred Deep Link 확인 시작...")
        
        lifecycleScope.launch {
            try {
                val result = DeepLinkSDKHelper.checkDeferredDeepLinkSuspend(this@MainActivity)
                handleResult(result)
            } catch (e: Exception) {
                addLog("에러: ${e.message}", isError = true)
                showLoading(false)
            }
        }
    }
    
    private fun handleResult(result: DeepLinkResult) {
        showLoading(false)
        
        when (result) {
            is DeepLinkResult.Success -> {
                val response = result.response
                
                addLog("✅ 매칭 성공!", isSuccess = true)
                addLog("Link ID: ${response.linkId}")
                addLog("Target URL: ${response.targetUrl}")
                addLog("Campaign: ${response.campaignName ?: "없음"}")
                addLog("Match Score: ${String.format("%.2f", response.matchScore ?: 0.0)}")
                
                response.customData?.let { data ->
                    addLog("Custom Data:")
                    data.forEach { (key, value) ->
                        addLog("  - $key: $value")
                    }
                }
                
                // 결과 표시
                binding.resultCard.visibility = View.VISIBLE
                binding.tvResultTitle.text = "✅ 딥링크 매칭 성공"
                binding.tvResultContent.text = buildString {
                    append("Target: ${response.targetUrl}\n")
                    append("Campaign: ${response.campaignName ?: "-"}\n")
                    append("Score: ${String.format("%.0f%%", (response.matchScore ?: 0.0) * 100)}")
                }
                binding.btnOpenProduct.visibility = View.VISIBLE
                
                // 🔥 자동으로 해당 화면으로 이동
                navigateToDeepLink(response)
            }
            
            is DeepLinkResult.NoMatch -> {
                addLog("ℹ️ 매칭된 딥링크 없음 (일반 설치)")
                
                binding.resultCard.visibility = View.VISIBLE
                binding.tvResultTitle.text = "일반 설치"
                binding.tvResultContent.text = "매칭된 딥링크가 없습니다.\n홈 화면을 표시합니다."
                binding.btnOpenProduct.visibility = View.GONE
            }
            
            is DeepLinkResult.Error -> {
                addLog("❌ 에러 발생: ${result.message}", isError = true)
                result.exception?.let {
                    addLog("상세: ${it.message}", isError = true)
                }
                
                binding.resultCard.visibility = View.VISIBLE
                binding.tvResultTitle.text = "❌ 에러 발생"
                binding.tvResultContent.text = result.message
                binding.btnOpenProduct.visibility = View.GONE
            }
        }
    }
    
    private fun resetAndCheck() {
        addLog("🔄 초기화 중...")
        DeepLinkSDKHelper.resetDeferredDeepLinkCheck(this)
        binding.resultCard.visibility = View.GONE
        binding.logTextView.text = ""
        addLog("초기화 완료. 다시 확인합니다...")
        checkDeferredDeepLink()
    }
    
    private fun openTestLink() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("${SampleApplication.SERVER_URL}/d/test-link-id")
        }
        startActivity(intent)
    }
    
    private fun addLog(message: String, isError: Boolean = false, isSuccess: Boolean = false) {
        val timestamp = dateFormat.format(Date())
        val prefix = when {
            isError -> "❌"
            isSuccess -> "✅"
            else -> "ℹ️"
        }
        
        val logMessage = "[$timestamp] $prefix $message\n"
        binding.logTextView.append(logMessage)
        
        // 스크롤을 맨 아래로
        binding.scrollView.post {
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnCheckAgain.isEnabled = !show
        binding.btnReset.isEnabled = !show
    }
    
    /**
     * Deep Link에 따라 자동으로 화면 이동
     */
    private fun navigateToDeepLink(response: com.deeplink.sdk.models.DeviceMatchResponse) {
        val targetUrl = response.targetUrl ?: return
        
        addLog("🚀 자동 이동: $targetUrl", isSuccess = true)
        
        // URL 파싱 (예: coooldoggy://product/123)
        when {
            targetUrl.contains("product") -> {
                // 상품 화면으로 이동
                val intent = Intent(this, ProductActivity::class.java).apply {
                    response.customData?.let { data ->
                        data["productId"]?.let { putExtra("productId", it) }
                        data["discount"]?.let { putExtra("discount", it) }
                    }
                }
                startActivity(intent)
            }
            
            targetUrl.contains("promo") -> {
                // 프로모션 화면으로 이동
                addLog("프로모션 화면으로 이동 (구현 필요)")
                // startActivity(Intent(this, PromotionActivity::class.java))
            }
            
            else -> {
                addLog("알 수 없는 URL 형식: $targetUrl")
            }
        }
    }
}

