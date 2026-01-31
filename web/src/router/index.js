import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/admin',
        component: () => import('../views/admin/Layout.vue'),
        redirect: '/admin/dashboard',
        children: [
            { path: 'dashboard', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue') },
            { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/Users.vue') },
            { path: 'merchants', name: 'AdminMerchants', component: () => import('../views/admin/Merchants.vue') },
            { path: 'orders', name: 'AdminOrders', component: () => import('../views/admin/Orders.vue') },
            { path: 'data-screen', name: 'AdminDataScreen', component: () => import('../views/admin/DataScreen.vue') },
            { path: 'announcements', name: 'AdminAnnouncements', component: () => import('../views/admin/Announcements.vue') },
            { path: 'activities', name: 'AdminActivities', component: () => import('../views/admin/Activities.vue') },
            { path: 'complaints', name: 'AdminComplaints', component: () => import('../views/admin/Complaints.vue') },
            { path: 'finance', name: 'AdminFinance', component: () => import('../views/admin/Finance.vue') }
        ]
    },
    {
        path: '/merchant',
        component: () => import('../views/merchant/Layout.vue'),
        redirect: '/merchant/dashboard',
        children: [
            { path: 'dashboard', name: 'MerchantDashboard', component: () => import('../views/merchant/Dashboard.vue') },
            { path: 'shop', name: 'MerchantShop', component: () => import('../views/merchant/Shop.vue') },
            { path: 'dishes', name: 'MerchantDishes', component: () => import('../views/merchant/Dishes.vue') },
            { path: 'orders', name: 'MerchantOrders', component: () => import('../views/merchant/Orders.vue') },
            { path: 'analytics', name: 'MerchantAnalytics', component: () => import('../views/merchant/Analytics.vue') },
            { path: 'marketing', name: 'MerchantMarketing', component: () => import('../views/merchant/Marketing.vue') },
            { path: 'inventory', name: 'MerchantInventory', component: () => import('../views/merchant/Inventory.vue') }
        ]
    },
    { path: '/', redirect: '/login' }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.path !== '/login' && !token) {
        next('/login')
    } else {
        next()
    }
})

export default router
