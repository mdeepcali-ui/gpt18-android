# 📋 متابعة شغل نظام Credits — APK
**آخر تحديث:** 14 أكتوبر 2026

## ✅ Backend (مكتمل 100%)
- [x] credits.py (471 سطر)
- [x] جداول DB (transactions, subscriptions, users.credits)
- [x] credits_api.py (8 endpoints)
- [x] payments.py (USDT → رصيد)
- [x] daily_usage.py (مربوط بـcredits)
- [x] api.py (5 مواقع accounting)
- [x] chat_stream.py (Media checks + رسائل ودية)
- [x] auth_api.py (/plans + /subscription محدّث)
- [x] topup_required event

## 📱 APK — منجز
- [x] إصلاح Drawer avatar (v5.1.36)
- [x] حذف أسماء النماذج من About
- [x] حذف أرقام الإصدار (باقي Drawer)
- [x] Drawer footer ديناميكي
- [x] DisclaimerScreen جديد (شروط + خصوصية)
- [x] AgeCheckScreen جديد
- [x] زر "موافق" (contentColor داكن)
- [x] Subscription.kt Models محدّثة
- [x] ApiService endpoints جديدة (credits/*)
- [x] SubscriptionScreen جديد (452 سطر)
- [x] SubscriptionViewModel محدّث
- [x] SubscriptionRepository محدّث
- [x] CodeUiState (credits fields) ← آخر خطوة

## 📱 APK — متبقي
- [x] CodeViewModel: دالة loadCredits()
- [x] CodeScreen: رسالة مثبتة للمجاني + زر اشترك
- [x] ChatViewModel: استقبال topup_required
- [x] ChatScreen: زر "اشحن رصيد" عند الرفض
- [x] AppDrawer: عرض الرصيد الحالي
- [x] ProfileScreen: عرض الرصيد + الاستخدام + سجل
- [x] User model: (اختياري — تم)

## 🔨 البناء
- [ ] بناء APK v5.2.0 (مرة وحدة في النهاية)
- [ ] اختبار شامل
- [ ] إصلاح أي خطأ

## 📊 الحالة
- **Backend:** 100% ✅
- **APK:** 40% 🔄
- **البناء:** 0% ⏳
