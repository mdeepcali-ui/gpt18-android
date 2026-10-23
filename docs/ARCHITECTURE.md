# 🏛️ البنية المعمارية — GPT+18

## قاعدة واحدة لا تُكسر

كل طبقة تستورد فقط من طبقة أدنى. لا استيراد من طبقة أعلى.

## التسلسل الطبقي

- screens (أعلى)
- navigation
- service
- components
- helpers
- data
- theme (القاعدة)

## قواعد إضافية

1. ui/screens/chat لا يستورد من ui/screens/code (والعكس)
2. ViewModels عبر Repository — لا Retrofit مباشرة
3. لا ملفات .bak_* في app/src/main/java/

## حماية — pre-commit hook

التثبيت: cp scripts/pre-commit .git/hooks/pre-commit && chmod +x .git/hooks/pre-commit

يفحص 7 قواعد — أي انتهاك = رفض الـ commit.

---
آخر تحديث: 23 أكتوبر 2026
