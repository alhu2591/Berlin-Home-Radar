# GitHub build and deploy setup

This repo now includes ready-to-use GitHub files for CI and optional deployment.

## Included workflows
- `.github/workflows/app-ci.yml`
  - installs app dependencies
  - runs `npm run typecheck:logic`
  - runs `npm run test:ci`
- `.github/workflows/worker-ci.yml`
  - installs worker dependencies
  - runs `npm run typecheck` inside `workers`
- `.github/workflows/eas-build.yml`
  - manual GitHub Action for EAS builds
- `.github/workflows/worker-deploy.yml`
  - manual GitHub Action for Cloudflare Worker deployment

## Recommended GitHub secrets
### App / EAS
- `EXPO_TOKEN`

### Cloudflare Worker
- `CLOUDFLARE_API_TOKEN`
- `CLOUDFLARE_ACCOUNT_ID`
- `WORKER_BEARER_TOKEN`

## Typical GitHub flow
1. Push the repository.
2. Let `App CI` and `Worker CI` run on pushes and pull requests.
3. Add secrets in GitHub repository settings.
4. Trigger `EAS Build` manually when you want mobile binaries.
5. Trigger `Worker Deploy` manually when you want to publish live worker changes.

## Important note
The mobile workflow intentionally checks the stable logic/test layer, not a full Expo production build on every push. This keeps CI more reliable for the current scaffold-first stage.
