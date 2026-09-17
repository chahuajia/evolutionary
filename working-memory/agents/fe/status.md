# FE agent status锛坋xtreme pressure 路 slice 8b锛?

**鍒嗘敮**锛歚topic/fe-ddd-rsc`  
**鏃ユ湡**锛?026-09-17  
**蹇冭烦**锛歴lice 8b IoT COMM_LOST 璇婃柇瀹㈡埛绔矝  
**HEAD**锛?(瀵归綈 DTO 鍚庡洖濉?*

## 瀹屾垚

### Slice 8b锛圛oT FE 路 鍔犲帤锛?

- `domains/iot/infrastructure/iot-gateway.ts`锛歚postDetectCommLost` 鈫?`POST /iot/batteries/{id}/detect-comm-lost`锛涢粯璁?`BAT-IOT-1`锛涘榻?BE 鎵佸钩 DTO `{ batteryId, stale, raised, alertType, ticketId }`锛涢敊璇粡 `fetchJson` suggestion
- `app/iot/`锛氳矾鐢?`/iot` + `CommLostPanel` 瀹㈡埛绔矝锛堝睍绀?stale / COMM_LOST / ticketId锛?
- 棣栭〉閾惧埌 `/iot`
- `npx tsc --noEmit` 閫氳繃
- 鏈敼 backend锛涙湭 push

### 鍓嶅簭鍒囩墖鎽樿

- Slice 7锛氳閲忔潈鐩婃崲鐢靛矝
- Slice 6锛氫俊鐢ㄨ繕娆捐В鍐诲矝
- Slice 4鈥?锛氭潈鐩婃崲鐢靛矝 + `fetchJson` suggestion
- Slice 1鈥?锛歊SC 绔欏垪琛?+ gateway

## 闃诲

- 鏃狅紙BE 8a 宸蹭氦浠樻墎骞?DetectCommLostView锛夈€?

## 璋冪敤绀轰緥

```http
POST /iot/batteries/BAT-IOT-1/detect-comm-lost
```
