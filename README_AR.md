# HomeSync Berlin

نسخة برلين فقط من مشروع تجميع السكن، مبنية بـ Expo Router + Zustand + SQLite، وموسعة بطبقة Worker ومصادر حية لبرلين.

## ما الجديد في v2.0.1
- تمت إضافة **ملفات GitHub للبناء والتشغيل الآلي**.
- تمت إضافة GitHub Actions من أجل:
  - فحص التطبيق
  - فحص الـWorker
  - بناء EAS اليدوي
  - نشر Cloudflare Worker يدويًا
- تمت إضافة `.gitignore` و`.nvmrc` و`eas.json` و`workers/wrangler.toml` و`GITHUB_SETUP.md`.
- تمت إضافة أوامر أكثر أمانًا للاستخدام داخل CI.

## تشغيل التطبيق
```bash
npm install
npm run start
```

## إعداد GitHub
راجع:
- `GITHUB_SETUP.md`
- `.github/workflows/app-ci.yml`
- `.github/workflows/worker-ci.yml`
- `.github/workflows/eas-build.yml`
- `.github/workflows/worker-deploy.yml`

## إعداد الـWorker
راجع:
- `WORKER_CONTRACT.md`
- `workers/README.md`
- `workers/wrangler.toml`
- `workers/.dev.vars.example`

## ملاحظة مهمة
ما زالت هذه النسخة **MVP / scaffold-first**. تم تجهيز GitHub للبناء والفحص، لكن لم يتم تأكيد تشغيل Expo أو بناء iOS/Android نهائيًا داخل هذه البيئة.
