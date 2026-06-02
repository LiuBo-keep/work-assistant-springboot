<template>
  <div class="qr-page">

    <!-- ===== Tab 切换 ===== -->
    <div class="mode-tabs">
      <button
        class="mode-tab"
        :class="{ active: mode === 'normal' }"
        @click="mode = 'normal'"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
          <rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="4" height="4"/>
        </svg>
        普通二维码
      </button>
      <button
        class="mode-tab"
        :class="{ active: mode === 'transparent' }"
        @click="mode = 'transparent'"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="2" y="2" width="20" height="20" rx="2"/>
          <path d="M7 7h4v4H7zM13 7h4v4h-4zM7 13h4v4H7z" stroke-width="1.5"/>
          <circle cx="15" cy="15" r="4" fill="currentColor" opacity="0.3"/>
        </svg>
        透明二维码
      </button>
    </div>

    <div class="workspace">

      <!-- ========== 普通二维码 ========== -->
      <template v-if="mode === 'normal'">
        <div class="panel settings-panel">
          <div class="panel-title">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>
            参数设置
          </div>

          <div class="form-group">
            <label>内容</label>
            <textarea
              v-model="normal.text"
              class="input-text"
              placeholder="输入文本或链接，例如 https://example.com"
              rows="3"
              @input="debouncedGenNormal"
            />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>尺寸 <span class="val-badge">{{ normal.size }}px</span></label>
              <input type="range" v-model.number="normal.size" min="128" max="512" step="16" @input="debouncedGenNormal" />
            </div>
            <div class="form-group">
              <label>外边距 <span class="val-badge">{{ normal.margin }}</span></label>
              <input type="range" v-model.number="normal.margin" min="0" max="8" step="1" @input="debouncedGenNormal" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>纠错级别</label>
              <div class="seg-ctrl">
                <button
                  v-for="lvl in ['L','M','Q','H']"
                  :key="lvl"
                  :class="{ active: normal.ecLevel === lvl }"
                  @click="normal.ecLevel = lvl; debouncedGenNormal()"
                >{{ lvl }}</button>
              </div>
            </div>
          </div>

          <div class="form-row color-row">
            <div class="form-group">
              <label>前景色</label>
              <div class="color-pick-wrap">
                <input type="color" v-model="normal.darkColor" @input="debouncedGenNormal" />
                <span class="color-hex">{{ normal.darkColor }}</span>
              </div>
            </div>
            <div class="form-group">
              <label>背景色</label>
              <div class="color-pick-wrap">
                <input type="color" v-model="normal.lightColor" @input="debouncedGenNormal" />
                <span class="color-hex">{{ normal.lightColor }}</span>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>Logo（可选）</label>
            <div
              class="upload-zone"
              :class="{ 'has-file': normal.logoSrc }"
              @click="$refs.logoInput.click()"
              @dragover.prevent
              @drop.prevent="onLogoDrop"
            >
              <template v-if="!normal.logoSrc">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                <span>点击或拖拽上传 Logo</span>
              </template>
              <template v-else>
                <img :src="normal.logoSrc" class="logo-preview" />
                <span class="logo-filename">{{ normal.logoName }}</span>
                <button class="rm-btn" @click.stop="normal.logoSrc = ''; debouncedGenNormal()">✕</button>
              </template>
            </div>
            <input ref="logoInput" type="file" accept="image/*" hidden @change="onLogoChange" />
          </div>

          <button class="gen-btn" @click="generateNormal" :disabled="!normal.text">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
            生成二维码
          </button>
        </div>

        <!-- 预览区 -->
        <div class="panel preview-panel">
          <div class="panel-title">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            预览
          </div>
          <div class="preview-box" ref="normalPreviewBox">
            <div v-show="!normalQRDataUrl" class="empty-hint">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" opacity="0.25">
                <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
                <rect x="3" y="14" width="7" height="7"/><rect x="14" y="18" width="3" height="3"/>
                <rect x="19" y="14" width="2" height="2"/><rect x="14" y="14" width="2" height="2"/>
              </svg>
              <p>在左侧设置参数后生成</p>
            </div>
            <img v-show="normalQRDataUrl" :src="normalQRDataUrl || ''" class="qr-result-img" />
          </div>
          <div class="preview-actions" v-if="normalQRDataUrl">
            <button class="dl-btn" @click="downloadNormal('png')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
              下载 PNG
            </button>
            <button class="dl-btn secondary" @click="downloadNormal('svg')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
              下载 SVG
            </button>
          </div>
        </div>
      </template>

      <!-- ========== 透明二维码 ========== -->
      <template v-if="mode === 'transparent'">
        <div class="panel settings-panel">
          <div class="panel-title">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>
            参数设置
          </div>

          <div class="form-group">
            <label>内容</label>
            <textarea
              v-model="trans.text"
              class="input-text"
              placeholder="输入文本或链接"
              rows="3"
              @input="debouncedGenTrans"
            />
          </div>

          <div class="form-group">
            <label>背景图片</label>
            <div
              class="upload-zone tall"
              :class="{ 'has-file': trans.bgSrc }"
              @click="$refs.bgInput.click()"
              @dragover.prevent
              @drop.prevent="onBgDrop"
            >
              <template v-if="!trans.bgSrc">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                <span>点击或拖拽上传背景图</span>
                <span class="upload-hint">名片、海报等图片</span>
              </template>
              <template v-else>
                <img :src="trans.bgSrc" class="bg-thumb" />
                <span class="logo-filename">{{ trans.bgName }}</span>
                <button class="rm-btn" @click.stop="trans.bgSrc = ''; debouncedGenTrans()">✕</button>
              </template>
            </div>
            <input ref="bgInput" type="file" accept="image/*" hidden @change="onBgChange" />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>二维码尺寸 <span class="val-badge">{{ trans.size }}px</span></label>
              <input type="range" v-model.number="trans.size" min="80" max="400" step="8" @input="debouncedGenTrans" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>透明度 <span class="val-badge">{{ Math.round(trans.opacity * 100) }}%</span></label>
              <input type="range" v-model.number="trans.opacity" min="0.1" max="1" step="0.05" @input="debouncedGenTrans" />
            </div>
          </div>

          <div class="form-row color-row">
            <div class="form-group">
              <label>二维码颜色</label>
              <div class="color-pick-wrap">
                <input type="color" v-model="trans.darkColor" @input="debouncedGenTrans" />
                <span class="color-hex">{{ trans.darkColor }}</span>
              </div>
            </div>
            <div class="form-group">
              <label>纠错级别</label>
              <div class="seg-ctrl">
                <button
                  v-for="lvl in ['L','M','Q','H']"
                  :key="lvl"
                  :class="{ active: trans.ecLevel === lvl }"
                  @click="trans.ecLevel = lvl; debouncedGenTrans()"
                >{{ lvl }}</button>
              </div>
            </div>
          </div>

          <div class="tip-box">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            生成后可在预览区<strong>拖拽</strong>调整二维码位置，再下载合成图片
          </div>

          <button class="gen-btn" @click="generateTrans" :disabled="!trans.text || !trans.bgSrc">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
            生成透明二维码
          </button>
        </div>

        <!-- 透明预览区 -->
        <div class="panel preview-panel">
          <div class="panel-title">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            预览 · 拖拽调整位置
          </div>
          <div class="preview-box trans-preview-box" ref="transPreviewBox">
            <div v-if="!trans.bgSrc || !transQRDataUrl" class="empty-hint">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" opacity="0.25"><rect x="2" y="2" width="20" height="20" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
              <p>上传背景图并输入内容后生成</p>
            </div>
            <div
              v-else
              class="trans-canvas-wrap"
              ref="transWrap"
              :style="transWrapStyle"
            >
              <img :src="trans.bgSrc" class="trans-bg-img-fixed" draggable="false" />
              <img
                :src="transQRDataUrl"
                class="trans-qr-overlay"
                :style="transQRStyle"
                draggable="false"
                @mousedown="startDrag"
                @touchstart.prevent="startDragTouch"
              />
            </div>
          </div>
          <div class="preview-actions" v-if="transQRDataUrl && trans.bgSrc">
            <button class="dl-btn" @click="downloadTrans">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
              下载合成图片
            </button>
            <span class="pos-hint">位置: {{ Math.round(trans.qrX) }}, {{ Math.round(trans.qrY) }}</span>
          </div>
        </div>
      </template>

    </div>

    <!-- 隐藏 canvas（用于合成） -->
    <canvas ref="mergeCanvas" hidden />

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'

/* ============================================================
   QR Code 生成（纯 JS 实现，无需外部库）
   使用 qrcode.js CDN 加载方案 → 运行时动态加载
   ============================================================ */

const mode = ref('normal')

/* ---------- 普通模式状态 ---------- */
const normal = reactive({
  text: '',
  size: 256,
  margin: 2,
  ecLevel: 'M',
  darkColor: '#000000',
  lightColor: '#ffffff',
  logoSrc: '',
  logoName: ''
})
const normalQRDataUrl = ref('')

/* ---------- 透明模式状态 ---------- */
const trans = reactive({
  text: '',
  bgSrc: '',
  bgName: '',
  size: 160,
  opacity: 0.85,
  darkColor: '#000000',
  ecLevel: 'H',
  // 二维码在背景图上的位置（相对于原始背景图像素坐标）
  qrX: 20,
  qrY: 20,
  // 背景图原始尺寸
  bgNaturalW: 0,
  bgNaturalH: 0
})
const transQRDataUrl = ref('')

/* ---------- Refs ---------- */
const logoInput = ref(null)
const bgInput = ref(null)
const mergeCanvas = ref(null)
const transWrap = ref(null)
const transPreviewBox = ref(null)

/* ============================================================
   动态加载 QRCode 库
   ============================================================ */
let QRCode = null

async function loadQRLib() {
  if (QRCode) return QRCode
  return new Promise((resolve, reject) => {
    if (window.__QRCode__) { QRCode = window.__QRCode__; resolve(QRCode); return }
    const s = document.createElement('script')
    s.src = 'https://cdnjs.cloudflare.com/ajax/libs/qrcodejs/1.0.0/qrcode.min.js'
    s.onload = () => {
      QRCode = window.QRCode
      resolve(QRCode)
    }
    s.onerror = reject
    document.head.appendChild(s)
  })
}

/* ============================================================
   普通二维码生成
   ============================================================ */
// 防并发：用版本号标记，只有最新一次生成结果才会更新视图
let normalGenVersion = 0

async function generateNormal() {
  if (!normal.text.trim()) return
  const version = ++normalGenVersion
  try {
    await loadQRLib()
    const canvas = document.createElement('canvas')
    const size = normal.size

    // 使用 QRCode 到临时 div
    const div = document.createElement('div')
    div.style.display = 'none'
    document.body.appendChild(div)
    const qr = new QRCode(div, {
      text: normal.text,
      width: size,
      height: size,
      colorDark: normal.darkColor,
      colorLight: normal.lightColor,
      correctLevel: QRCode.CorrectLevel[normal.ecLevel]
    })
    await nextTick()

    // 等待内部 canvas/img 渲染
    await new Promise(r => setTimeout(r, 80))

    const qrImg = div.querySelector('img') || div.querySelector('canvas')
    document.body.removeChild(div)

    if (!qrImg) return

    canvas.width = size
    canvas.height = size
    const ctx = canvas.getContext('2d')

    // 背景
    ctx.fillStyle = normal.lightColor
    ctx.fillRect(0, 0, size, size)

    const margin = normal.margin * (size / 25)

    await new Promise((res, rej) => {
      const tmp = new Image()
      tmp.onload = () => {
        ctx.drawImage(tmp, margin, margin, size - margin * 2, size - margin * 2)
        res()
      }
      tmp.onerror = rej
      tmp.src = qrImg.tagName === 'IMG' ? qrImg.src : qrImg.toDataURL()
    })

    // 叠加 Logo
    if (normal.logoSrc) {
      await new Promise((res) => {
        const logo = new Image()
        logo.onload = () => {
          const logoSize = size * 0.22
          const logoX = (size - logoSize) / 2
          const logoY = (size - logoSize) / 2
          // 白色底
          ctx.fillStyle = '#ffffff'
          const pad = logoSize * 0.12
          ctx.beginPath()
          ctx.roundRect(logoX - pad, logoY - pad, logoSize + pad * 2, logoSize + pad * 2, 8)
          ctx.fill()
          ctx.drawImage(logo, logoX, logoY, logoSize, logoSize)
          res()
        }
        logo.onerror = res
        logo.src = normal.logoSrc
      })
    }

    // 只有最新的那次调用才更新视图，丢弃过期结果
    if (version === normalGenVersion) {
      normalQRDataUrl.value = canvas.toDataURL('image/png')
    }
  } catch (e) {
    console.error('QR生成失败', e)
  }
}

/* ============================================================
   透明二维码生成
   ============================================================ */
async function generateTrans() {
  if (!trans.text.trim() || !trans.bgSrc) return
  try {
    await loadQRLib()
    const div = document.createElement('div')
    div.style.display = 'none'
    document.body.appendChild(div)
    const qr = new QRCode(div, {
      text: trans.text,
      width: trans.size,
      height: trans.size,
      colorDark: trans.darkColor,
      colorLight: 'rgba(0,0,0,0)', // 透明背景
      correctLevel: QRCode.CorrectLevel[trans.ecLevel]
    })

    await new Promise(r => setTimeout(r, 80))

    const qrEl = div.querySelector('img') || div.querySelector('canvas')
    document.body.removeChild(div)
    if (!qrEl) return

    // 重绘：只保留暗色模块，亮色变透明
    const tmpCanvas = document.createElement('canvas')
    tmpCanvas.width = trans.size
    tmpCanvas.height = trans.size
    const tCtx = tmpCanvas.getContext('2d')

    await new Promise((res, rej) => {
      const tmp = new Image()
      tmp.onload = () => {
        tCtx.drawImage(tmp, 0, 0, trans.size, trans.size)
        res()
      }
      tmp.onerror = rej
      tmp.src = qrEl.tagName === 'IMG' ? qrEl.src : qrEl.toDataURL()
    })

    // 将浅色像素变透明，深色像素调整透明度
    const imgData = tCtx.getImageData(0, 0, trans.size, trans.size)
    const d = imgData.data
    const darkR = parseInt(trans.darkColor.slice(1, 3), 16)
    const darkG = parseInt(trans.darkColor.slice(3, 5), 16)
    const darkB = parseInt(trans.darkColor.slice(5, 7), 16)

    for (let i = 0; i < d.length; i += 4) {
      const brightness = (d[i] + d[i+1] + d[i+2]) / 3
      if (brightness > 200) {
        // 浅色 → 透明
        d[i+3] = 0
      } else {
        // 深色 → 自定义颜色 + 透明度
        d[i]   = darkR
        d[i+1] = darkG
        d[i+2] = darkB
        d[i+3] = Math.round(255 * trans.opacity)
      }
    }
    tCtx.putImageData(imgData, 0, 0)

    transQRDataUrl.value = tmpCanvas.toDataURL('image/png')

    // 初始位置：居中偏右下
    await nextTick()
    initTransQRPos()
  } catch (e) {
    console.error('透明QR生成失败', e)
  }
}

/* ============================================================
   透明二维码拖拽
   ============================================================ */
// 预览区缩放比例（预览图 vs 实际图）
const previewScale = computed(() => {
  if (!trans.bgNaturalW || !transWrap.value) return 1
  return transWrap.value.offsetWidth / trans.bgNaturalW
})

// 二维码在预览区的显示样式（px）
const transQRStyle = computed(() => {
  const s = previewScale.value
  return {
    left: trans.qrX * s + 'px',
    top:  trans.qrY * s + 'px',
    width:  trans.size * s + 'px',
    height: trans.size * s + 'px',
    opacity: trans.opacity,
    cursor: 'move'
  }
})

// 预览容器样式（保持背景图宽高比）
const transWrapStyle = computed(() => {
  if (!trans.bgNaturalW || !trans.bgNaturalH) return {}
  return {
    position: 'relative',
    display: 'inline-block', // 让包裹层紧紧贴合图片的实际渲染大小
    maxWidth: '100%',
    maxHeight: '100%'
  }
})

function initTransQRPos() {
  if (!trans.bgNaturalW || !trans.bgNaturalH) return
  // 默认放右下角，留 20px 边距
  trans.qrX = trans.bgNaturalW - trans.size - 20
  trans.qrY = trans.bgNaturalH - trans.size - 20
}

let dragging = false
let dragStartX = 0, dragStartY = 0
let qrStartX = 0, qrStartY = 0

function startDrag(e) {
  e.preventDefault()
  dragging = true
  dragStartX = e.clientX
  dragStartY = e.clientY
  qrStartX = trans.qrX
  qrStartY = trans.qrY
  window.addEventListener('mousemove', onDrag)
  window.addEventListener('mouseup', stopDrag)
}

function startDragTouch(e) {
  const touch = e.touches[0]
  dragging = true
  dragStartX = touch.clientX
  dragStartY = touch.clientY
  qrStartX = trans.qrX
  qrStartY = trans.qrY
  window.addEventListener('touchmove', onDragTouch, { passive: false })
  window.addEventListener('touchend', stopDrag)
}

function onDrag(e) {
  if (!dragging) return
  const s = previewScale.value
  const dx = (e.clientX - dragStartX) / s
  const dy = (e.clientY - dragStartY) / s
  clampQRPos(qrStartX + dx, qrStartY + dy)
}

function onDragTouch(e) {
  e.preventDefault()
  if (!dragging) return
  const touch = e.touches[0]
  const s = previewScale.value
  const dx = (touch.clientX - dragStartX) / s
  const dy = (touch.clientY - dragStartY) / s
  clampQRPos(qrStartX + dx, qrStartY + dy)
}

function clampQRPos(x, y) {
  const maxX = trans.bgNaturalW - trans.size
  const maxY = trans.bgNaturalH - trans.size
  trans.qrX = Math.max(0, Math.min(x, maxX))
  trans.qrY = Math.max(0, Math.min(y, maxY))
}

function stopDrag() {
  dragging = false
  window.removeEventListener('mousemove', onDrag)
  window.removeEventListener('mouseup', stopDrag)
  window.removeEventListener('touchmove', onDragTouch)
  window.removeEventListener('touchend', stopDrag)
}

onBeforeUnmount(() => stopDrag())

/* ============================================================
   下载
   ============================================================ */
function downloadNormal(fmt) {
  if (!normalQRDataUrl.value) return
  if (fmt === 'svg') {
    // 将 canvas 数据包裹为 SVG image
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${normal.size}" height="${normal.size}">
  <image href="${normalQRDataUrl.value}" width="${normal.size}" height="${normal.size}"/>
</svg>`
    const blob = new Blob([svg], { type: 'image/svg+xml' })
    const url = URL.createObjectURL(blob)
    triggerDownload(url, 'qrcode.svg')
    URL.revokeObjectURL(url)
  } else {
    triggerDownload(normalQRDataUrl.value, 'qrcode.png')
  }
}

async function downloadTrans() {
  if (!transQRDataUrl.value || !trans.bgSrc) return
  const canvas = mergeCanvas.value
  const bgImg = await loadImage(trans.bgSrc)
  canvas.width  = bgImg.naturalWidth  || bgImg.width
  canvas.height = bgImg.naturalHeight || bgImg.height
  const ctx = canvas.getContext('2d')
  ctx.drawImage(bgImg, 0, 0)

  const qrImg = await loadImage(transQRDataUrl.value)
  ctx.globalAlpha = 1 // 透明度已经编码在 qrImg 像素里
  ctx.drawImage(qrImg, trans.qrX, trans.qrY, trans.size, trans.size)

  triggerDownload(canvas.toDataURL('image/png'), 'qr-composite.png')
}

function loadImage(src) {
  return new Promise((res, rej) => {
    const img = new Image()
    img.onload = () => res(img)
    img.onerror = rej
    img.src = src
  })
}

function triggerDownload(url, name) {
  const a = document.createElement('a')
  a.href = url; a.download = name; a.click()
}

/* ============================================================
   文件上传处理
   ============================================================ */
function readFileAsDataURL(file) {
  return new Promise((res, rej) => {
    const reader = new FileReader()
    reader.onload = e => res(e.target.result)
    reader.onerror = rej
    reader.readAsDataURL(file)
  })
}

async function onLogoChange(e) {
  const file = e.target.files[0]; if (!file) return
  normal.logoSrc  = await readFileAsDataURL(file)
  normal.logoName = file.name
  debouncedGenNormal()
}

async function onLogoDrop(e) {
  const file = e.dataTransfer.files[0]; if (!file || !file.type.startsWith('image/')) return
  normal.logoSrc  = await readFileAsDataURL(file)
  normal.logoName = file.name
  debouncedGenNormal()
}

async function onBgChange(e) {
  const file = e.target.files[0]; if (!file) return
  await setBgFile(file)
}

async function onBgDrop(e) {
  const file = e.dataTransfer.files[0]; if (!file || !file.type.startsWith('image/')) return
  await setBgFile(file)
}

async function setBgFile(file) {
  trans.bgSrc  = await readFileAsDataURL(file)
  trans.bgName = file.name
  // 获取原始尺寸
  const img = new Image()
  img.onload = () => {
    trans.bgNaturalW = img.naturalWidth
    trans.bgNaturalH = img.naturalHeight
  }
  img.src = trans.bgSrc
  debouncedGenTrans()
}

/* ============================================================
   防抖
   ============================================================ */
let normalTimer = null
let transTimer = null

function debouncedGenNormal() {
  clearTimeout(normalTimer)
  normalTimer = setTimeout(generateNormal, 400)
}

function debouncedGenTrans() {
  clearTimeout(transTimer)
  transTimer = setTimeout(generateTrans, 400)
}

onMounted(() => {
  loadQRLib().catch(() => {})
})
</script>

<style lang="scss" scoped>
.qr-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
  gap: 16px;
  background: var(--el-bg-color-page, #f5f7fa);
}

/* ── Mode Tabs ── */
.mode-tabs {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.mode-tab {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 8px 20px;
  border-radius: 8px;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  background: var(--el-bg-color, #fff);
  color: var(--el-text-color-secondary, #909399);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.18s ease;

  &:hover {
    border-color: var(--el-color-primary, #409eff);
    color: var(--el-color-primary, #409eff);
  }

  &.active {
    background: var(--el-color-primary, #409eff);
    border-color: var(--el-color-primary, #409eff);
    color: #fff;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.35);
  }
}

/* ── Workspace ── */
.workspace {
  display: flex;
  gap: 16px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* ── Panel ── */
.panel {
  background: var(--el-bg-color, #fff);
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow-y: auto;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.1); border-radius: 2px; }
}

.settings-panel {
  width: 320px;
  flex-shrink: 0;
}

.preview-panel {
  flex: 1;
  min-width: 0;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary, #909399);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
}

/* ── Form ── */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;

  label {
    font-size: 12px;
    font-weight: 500;
    color: var(--el-text-color-regular, #606266);
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.form-row {
  display: flex;
  gap: 12px;

  .form-group { flex: 1; }
}

.val-badge {
  font-size: 11px;
  padding: 1px 6px;
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);
  border-radius: 4px;
  font-weight: 600;
  font-family: monospace;
}

.input-text {
  width: 100%;
  padding: 8px 10px;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 6px;
  font-size: 13px;
  resize: none;
  box-sizing: border-box;
  background: var(--el-bg-color, #fff);
  color: var(--el-text-color-primary, #303133);
  transition: border-color 0.18s;
  font-family: inherit;

  &:focus {
    outline: none;
    border-color: var(--el-color-primary, #409eff);
  }
}

input[type="range"] {
  width: 100%;
  accent-color: var(--el-color-primary, #409eff);
  cursor: pointer;
}

/* ── Segment control ── */
.seg-ctrl {
  display: flex;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 6px;
  overflow: hidden;

  button {
    flex: 1;
    padding: 5px 0;
    border: none;
    background: transparent;
    font-size: 12px;
    font-weight: 600;
    color: var(--el-text-color-regular, #606266);
    cursor: pointer;
    transition: all 0.15s;
    border-right: 1px solid var(--el-border-color, #dcdfe6);

    &:last-child { border-right: none; }
    &:hover { background: var(--el-fill-color-light, #f5f7fa); }
    &.active {
      background: var(--el-color-primary, #409eff);
      color: #fff;
    }
  }
}

/* ── Color picker ── */
.color-pick-wrap {
  display: flex;
  align-items: center;
  gap: 8px;

  input[type="color"] {
    width: 36px;
    height: 30px;
    border: 1.5px solid var(--el-border-color, #dcdfe6);
    border-radius: 6px;
    padding: 2px;
    cursor: pointer;
    background: none;
  }
}

.color-hex {
  font-size: 12px;
  font-family: monospace;
  color: var(--el-text-color-secondary, #909399);
}

/* ── Upload Zone ── */
.upload-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 16px;
  border: 1.5px dashed var(--el-border-color, #dcdfe6);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.18s;
  position: relative;
  min-height: 80px;
  background: var(--el-fill-color-lighter, #fafafa);

  &.tall { min-height: 100px; }

  &:hover {
    border-color: var(--el-color-primary, #409eff);
    background: var(--el-color-primary-light-9, #ecf5ff);
  }

  &.has-file {
    border-style: solid;
    border-color: var(--el-color-success, #67c23a);
  }

  span {
    font-size: 12px;
    color: var(--el-text-color-secondary, #909399);
    text-align: center;
  }

  .upload-hint {
    font-size: 11px;
    color: var(--el-text-color-placeholder, #c0c4cc);
  }

  svg { color: var(--el-text-color-placeholder, #c0c4cc); }
}

.logo-preview {
  width: 48px;
  height: 48px;
  object-fit: contain;
  border-radius: 4px;
}

.bg-thumb {
  width: 80px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.logo-filename {
  font-size: 11px;
  color: var(--el-text-color-secondary, #909399);
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rm-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: none;
  background: var(--el-color-danger, #f56c6c);
  color: #fff;
  font-size: 11px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

/* ── Generate Button ── */
.gen-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 10px 0;
  border-radius: 8px;
  border: none;
  background: var(--el-color-primary, #409eff);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.18s;
  margin-top: auto;

  &:hover:not(:disabled) {
    background: var(--el-color-primary-dark-2, #337ecc);
    box-shadow: 0 3px 10px rgba(64, 158, 255, 0.4);
    transform: translateY(-1px);
  }

  &:disabled {
    opacity: 0.45;
    cursor: not-allowed;
  }
}

/* ── Tip Box ── */
.tip-box {
  display: flex;
  align-items: flex-start;
  gap: 7px;
  padding: 10px 12px;
  background: var(--el-color-warning-light-9, #fdf6ec);
  border: 1px solid var(--el-color-warning-light-5, #f5dab1);
  border-radius: 8px;
  font-size: 12px;
  color: var(--el-color-warning-dark-2, #b88230);
  line-height: 1.5;

  svg { flex-shrink: 0; margin-top: 1px; }
  strong { font-weight: 700; }
}

/* ── Preview Box ── */
.preview-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-lighter, #fafafa);
  border: 1.5px dashed var(--el-border-color-lighter, #ebeef5);
  border-radius: 10px;
  min-height: 280px;
  overflow: hidden;
  /* 关键：绝对定位子元素，防止内容变化引起高度重排 */
  position: relative;
  contain: layout size;
}

.empty-hint {
  /* 绝对居中，不占据文档流高度，不触发重排 */
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  pointer-events: none;

  p {
    font-size: 13px;
    color: var(--el-text-color-placeholder, #c0c4cc);
    margin: 0;
    white-space: nowrap;
  }
}

.qr-result-img {
  border-radius: 6px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  max-width: min(360px, 90%);
  max-height: 90%;
  object-fit: contain;
  /* 图片内容替换时淡入，掩盖重绘瞬间 */
  transition: opacity 0.15s ease;
}

/* ── Transparent Preview ── */
.trans-preview-box {
  display: flex;         /* 启用 flex 居中 */
  align-items: center;   /* 垂直居中 */
  justify-content: center;/* 水平居中 */
  padding: 10px;         /* 留一点点内边距，防止图片贴边 */
  box-sizing: border-box;
}

.trans-canvas-wrap {
  position: relative;
  width: auto;           /* 撤销原本的 width: 100% */
  height: auto;          /* 撤销原本的 height: 0 */
  max-width: 100%;       /* 严格限制，不能超过外层盒子 */
  max-height: 100%;      /* 严格限制，不能超过外层盒子 */
  overflow: visible;     /* 允许里面的二维码边缘有一点点悬空 */
}

.trans-bg-img-fixed {
  display: block;
  max-width: 100%;       /* 宽度自适应 */
  max-height: 55vh;      /* 关键：限制图片在视口中的最大高度（可根据实际微调，比如50vh-60vh） */
  width: auto;
  height: auto;
  pointer-events: none;
  user-select: none;
}

.trans-qr-overlay {
  position: absolute;
  user-select: none;
  -webkit-user-drag: none;

  &:hover {
    outline: 2px dashed var(--el-color-primary, #409eff);
    outline-offset: 2px;
  }
}

/* ── Preview Actions ── */
.preview-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.dl-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border-radius: 7px;
  border: none;
  background: var(--el-color-success, #67c23a);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.18s;

  &:hover {
    background: var(--el-color-success-dark-2, #529b2e);
    transform: translateY(-1px);
    box-shadow: 0 3px 10px rgba(103, 194, 58, 0.4);
  }

  &.secondary {
    background: transparent;
    border: 1.5px solid var(--el-border-color, #dcdfe6);
    color: var(--el-text-color-regular, #606266);

    &:hover {
      border-color: var(--el-color-primary, #409eff);
      color: var(--el-color-primary, #409eff);
      box-shadow: none;
      background: var(--el-color-primary-light-9, #ecf5ff);
    }
  }
}

.pos-hint {
  font-size: 11px;
  font-family: monospace;
  color: var(--el-text-color-placeholder, #c0c4cc);
}
</style>