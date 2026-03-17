// 页面加载完成后执行
window.addEventListener('DOMContentLoaded', function() {
    // 模拟商品数据
    const products = [
        { id: 1, name: '商品1', price: 99.99, image: '/static/images/product1.jpg' },
        { id: 2, name: '商品2', price: 199.99, image: '/static/images/product2.jpg' },
        { id: 3, name: '商品3', price: 299.99, image: '/static/images/product3.jpg' }
    ];

    // 初始化页面
    initPage();

    // 初始化页面函数
    function initPage() {
        // 绑定按钮事件
        bindEvents();
        
        // 模拟加载商品数据
        loadProducts();
    }

    // 绑定事件
    function bindEvents() {
        // 立即抢购按钮点击事件
        const buyBtn = document.querySelector('.hero .btn');
        if (buyBtn) {
            buyBtn.addEventListener('click', function() {
                alert('抢购功能开发中！');
            });
        }

        // 加入购物车按钮点击事件
        const addToCartBtns = document.querySelectorAll('.product-item .btn');
        addToCartBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const productName = this.parentElement.querySelector('h3').textContent;
                alert(`${productName} 已加入购物车！`);
            });
        });
    }

    // 加载商品数据
    function loadProducts() {
        // 这里可以通过API获取商品数据
        console.log('加载商品数据:', products);
    }

    // 模拟API请求
    function fetchProducts() {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                resolve(products);
            }, 1000);
        });
    }

    // 显示加载动画
    function showLoading() {
        // 实现加载动画
        console.log('显示加载动画');
    }

    // 隐藏加载动画
    function hideLoading() {
        // 隐藏加载动画
        console.log('隐藏加载动画');
    }
});