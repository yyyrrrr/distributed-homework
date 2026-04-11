// 页面加载完成后执行
window.addEventListener('DOMContentLoaded', function() {
    // 从URL参数中获取商品ID
    const urlParams = new URLSearchParams(window.location.search);
    const productId = urlParams.get('id');
    
    if (!productId) {
        showError('缺少商品ID参数');
        return;
    }
    
    // 加载商品详情
    loadProductDetail(productId);
    
    // 绑定按钮事件
    bindEvents(productId);
});

// 加载商品详情
function loadProductDetail(productId) {
    showLoading();
    
    fetch(`/api/product/detail/${productId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('商品不存在');
            }
            return response.json();
        })
        .then(product => {
            hideLoading();
            displayProductDetail(product);
        })
        .catch(error => {
            console.error('加载商品详情失败:', error);
            hideLoading();
            showError('商品不存在或加载失败');
        });
}

// 显示商品详情
function displayProductDetail(product) {
    document.getElementById('productName').textContent = product.name;
    document.getElementById('productPrice').textContent = product.price.toFixed(2);
    document.getElementById('productStock').textContent = product.stock;
    document.getElementById('productDescription').textContent = product.description || '暂无描述';
    
    // 设置商品图片
    const productImage = document.getElementById('productImage');
    productImage.src = `/images/product${product.id}.jpg`;
    productImage.alt = product.name;
    
    // 显示商品详情区域
    document.getElementById('productDetail').style.display = 'block';
}

// 绑定按钮事件
function bindEvents(productId) {
    // 加入购物车按钮
    const addToCartBtn = document.getElementById('addToCartBtn');
    if (addToCartBtn) {
        addToCartBtn.addEventListener('click', function() {
            alert('已加入购物车！');
        });
    }
    
    // 秒杀按钮
    const seckillBtn = document.getElementById('seckillBtn');
    if (seckillBtn) {
        seckillBtn.addEventListener('click', function() {
            handleSeckill(productId);
        });
    }
}

// 处理秒杀
function handleSeckill(productId) {
    if (!confirm('确定要参与秒杀吗？')) {
        return;
    }
    
    // 模拟用户ID，实际应用中应该从登录状态获取
    const userId = 1;
    
    fetch('/seckill/do', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `userId=${userId}&productId=${productId}`
    })
        .then(response => response.text())
        .then(result => {
            alert(result);
            // 重新加载商品详情以更新库存
            loadProductDetail(productId);
        })
        .catch(error => {
            console.error('秒杀失败:', error);
            alert('秒杀失败，库存不足或活动已结束');
        });
}

// 显示加载状态
function showLoading() {
    document.getElementById('loading').style.display = 'block';
    document.getElementById('error').style.display = 'none';
    document.getElementById('productDetail').style.display = 'none';
}

// 隐藏加载状态
function hideLoading() {
    document.getElementById('loading').style.display = 'none';
}

// 显示错误信息
function showError(message) {
    document.getElementById('loading').style.display = 'none';
    document.getElementById('error').style.display = 'block';
    document.getElementById('error').querySelector('p').textContent = message;
    document.getElementById('productDetail').style.display = 'none';
}