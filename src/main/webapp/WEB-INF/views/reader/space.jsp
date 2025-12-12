<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.demo0.auth.model.Reader" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>座位预约</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/base.css" />
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; margin:0; padding:0; background:#f7f9fc; }
        .page { max-width: 1080px; margin: 80px auto 40px; padding: 0 16px; }
        .card { background:#fff; border:1px solid #e5e7eb; border-radius:12px; padding:20px; margin-bottom:16px; box-shadow:0 4px 8px rgba(0,0,0,0.03); }
        .title { margin:0 0 12px; font-size:24px; font-weight:700; color:#1f2937; }
        .controls { display:flex; gap:12px; align-items:center; flex-wrap:wrap; margin-bottom:12px; }
        select, button { padding:8px 12px; border:1px solid #d1d5db; border-radius:8px; font-size:14px; }
        button { background:#2563eb; color:#fff; border:none; cursor:pointer; }
        button.secondary { background:#f8fafc; color:#1f2937; border:1px solid #d1d5db; }
        .grid { display:grid; grid-template-columns: repeat(auto-fill, minmax(90px, 1fr)); gap:10px; }
        .seat { padding:12px; border:1px solid #e5e7eb; border-radius:10px; text-align:center; cursor:pointer; background:#f8fafc; transition:all .2s; }
        .seat:hover { border-color:#2563eb; }
        .seat.reserved { background:#fee2e2; border-color:#f87171; color:#b91c1c; cursor:not-allowed; }
        .seat.me { background:#dcfce7 !important; border-color:#34d399 !important; color:#166534 !important; }
        .legend { display:flex; gap:10px; align-items:center; font-size:13px; color:#6b7280; margin-bottom:8px; }
        .badge { width:14px; height:14px; border-radius:4px; display:inline-block; margin-right:4px; }
        table { width:100%; border-collapse:collapse; margin-top:12px; }
        th, td { padding:10px; border-bottom:1px solid #e5e7eb; text-align:left; font-size:14px; }
        .toast { position:fixed; top:20px; left:50%; transform:translateX(-50%); background:#111827; color:#fff; padding:10px 16px; border-radius:8px; display:none; }
        .toast.show { display:block; animation:fade 2s ease forwards; }
        @keyframes fade { 0%{opacity:0;transform:translate(-50%,-10px);}20%{opacity:1;transform:translate(-50%,0);}80%{opacity:1;}100%{opacity:0;transform:translate(-50%,-6px);} }
        .modal-overlay { position:fixed; inset:0; background:rgba(0,0,0,0.6); display:none; justify-content:center; align-items:center; z-index:2000; }
        .modal-overlay.show { display:flex; }
        .modal-content { background:#fff; padding:2rem; border-radius:12px; width:90%; max-width:450px; position:relative; box-shadow:0 20px 25px -5px rgba(0,0,0,0.1); }
        .close-button { position:absolute; top:1rem; right:1rem; font-size:2rem; line-height:1; border:none; background:none; cursor:pointer; color:#9ca3af; }
        .time-display { background:#f3f4f6; padding:1rem; border-radius:8px; margin:1rem 0; }
        .time-display p { margin:0.25rem 0; }
        .reserve-btn { width:100%; background:#2563eb; color:#fff; padding:0.75rem; border:none; border-radius:8px; font-weight:bold; cursor:pointer; }
        .reserve-btn:disabled { background:#9ca3af; cursor:not-allowed; }
        .error-message { color:#dc2626; text-align:center; margin-top:1rem; font-size:0.875rem; }
    </style>
</head>
<%
    Reader currentUser = (Reader) session.getAttribute("currentUser");
    boolean loggedIn = currentUser != null && currentUser.getReaderId() != null;
%>
<body data-logged-in="<%= loggedIn %>" data-my-id="<%= loggedIn ? currentUser.getReaderId() : -1 %>">
<%@ include file="/WEB-INF/common/navbar.jsp" %>
<div class="page">
    <div class="card">
        <h1 class="title">座位预约</h1>
        <div class="controls">
            <label>楼栋
                <select id="building" onchange="onBuildingChange()">
                </select>
            </label>
            <label>楼层
                <select id="floor" onchange="loadSeats()">
                </select>
            </label>
            <label>日期
                <select id="date" onchange="loadSeats()">
                </select>
            </label>
            <label>时间段
                <select id="timeSlot" onchange="loadSeats()">
                    <option value="8-10">8:00-10:00</option>
                    <option value="10-12">10:00-12:00</option>
                    <option value="14-16">14:00-16:00</option>
                    <option value="16-18">16:00-18:00</option>
                </select>
            </label>
            <button onclick="loadSeats()">刷新</button>
            <button class="secondary" onclick="loadMy()">我的预约</button>
        </div>
        <div class="legend">
            <span><span class="badge" style="background:#f8fafc; border:1px solid #e5e7eb;"></span>空闲</span>
            <span><span class="badge" style="background:#fee2e2; border:1px solid #f87171;"></span>已被占用</span>
            <span><span class="badge" style="background:#dcfce7; border:1px solid #34d399;"></span>我已预约</span>
        </div>
        <div id="seatGrid" class="grid"></div>
    </div>

    <div class="card">
        <h2 class="title" style="font-size:20px;">我的预约</h2>
        <div id="myList"></div>
    </div>
</div>

<div id="toast" class="toast"></div>

<div id="reservationModal" class="modal-overlay">
    <div class="modal-content">
        <button class="close-button" onclick="closeReservationModal()">&times;</button>
        <h2 class="title" style="font-size:1.5rem; margin-bottom:0.5rem;">预约座位：<span id="modalSeatCode"></span></h2>
        <div style="color:#6b7280; margin-bottom:1.5rem;">
            位置：<span id="modalLocation"></span>
        </div>
        <div class="time-display">
            <p><strong>日期：</strong><span id="modalDate"></span></p>
            <p><strong>时间段：</strong><span id="modalTimeSlot"></span></p>
        </div>
        <button id="confirmReserveBtn" class="reserve-btn" onclick="confirmReservation()">确认预约</button>
        <p id="reservationError" class="error-message" style="display:none;"></p>
    </div>
</div>

<script>

    const ctx = '<%=request.getContextPath()%>';
    const loggedIn = document.body.getAttribute('data-logged-in') === 'true';
    const myId = parseInt(document.body.getAttribute('data-my-id') || '-1', 10);

    function showToast(msg) {
        const t = document.getElementById('toast');
        t.textContent = msg;
        t.classList.add('show');
        setTimeout(() => t.classList.remove('show'), 2000);
    }

    // 格式化时间字符串（用于我的预约列表）
    // 将 2025-02-01T08:00:00 转换为 2025-02-01 08:00
    function formatTimeStr(timeStr) {
        if (!timeStr || timeStr === 'null' || timeStr === 'undefined') {
            return '<span style="color:#9ca3af; font-size:12px;">--</span>';
        }
        let s = String(timeStr);
        s = s.replace('T', ' '); // 去掉T
        if (s.length >= 16) {
            return s.substring(0, 16); // 去掉秒
        }
        return s;
    }

    async function loadSeats() {
        const building = document.getElementById('building').value;
        const floor = document.getElementById('floor').value;
        const timeSlot = document.getElementById('timeSlot').value;

        // 获取当前选择的日期（value格式为 yyyy-MM-dd）
        const date = getValidDateValue();
        if (!date) return;

        if (!building || !floor || !timeSlot) return;

        try {
            const url = '<%=request.getContextPath()%>/reader/space/seats?buildingId=' + building + '&floor=' + floor + '&date=' + encodeURIComponent(date) + '&timeSlot=' + encodeURIComponent(timeSlot);
            const res = await fetch(url);
            if (!res.ok) throw new Error('加载失败: ' + res.status);
            const text = await res.text();
            const data = JSON.parse(text);
            if (data && data.length > 0) {
                renderSeats(data);
            } else {
                document.getElementById('seatGrid').innerHTML = '<p>该楼层暂无座位数据</p>';
            }
        } catch (e) {
            showToast(e.message || '加载失败');
        }
    }

    async function loadBuildings() {
        try {
            const url = '<%=request.getContextPath()%>/reader/space/buildings';
            const res = await fetch(url);
            if (!res.ok) throw new Error('加载楼栋失败');
            const list = await res.json();
            const sel = document.getElementById('building');
            sel.innerHTML = '';
            if (list && Array.isArray(list) && list.length > 0) {
                list.forEach((building) => {
                    const opt = document.createElement('option');
                    opt.value = building.buildingId;
                    opt.textContent = building.buildingName || ('楼栋' + building.buildingId);
                    sel.appendChild(opt);
                });
                // 确保先初始化日期，再加载楼层
                initDateSelector();
                await loadFloors(list[0].buildingId);
            } else {
                sel.innerHTML = '<option value="">暂无楼栋数据</option>';
                showToast('数据库中没有座位数据');
            }
        } catch (e) {
            showToast(e.message || '加载楼栋失败');
        }
    }

    async function loadFloors(buildingId) {
        try {
            const url = '<%=request.getContextPath()%>/reader/space/floors?buildingId=' + buildingId;
            const res = await fetch(url);
            if (!res.ok) throw new Error('加载楼层失败');
            const list = await res.json();
            const sel = document.getElementById('floor');
            sel.innerHTML = '';
            if (list && Array.isArray(list) && list.length > 0) {
                list.forEach((f) => {
                    let floorNum = (typeof f === 'object') ? f : f;
                    const opt = document.createElement('option');
                    opt.value = String(floorNum);
                    opt.textContent = String(floorNum) + '层';
                    sel.appendChild(opt);
                });
                sel.value = list[0];
                loadSeats();
            } else {
                sel.innerHTML = '<option value="">暂无楼层</option>';
                document.getElementById('seatGrid').innerHTML = '<p>暂无楼层数据</p>';
            }
        } catch (e) {
            showToast(e.message || '加载楼层失败');
        }
    }

    function onBuildingChange() {
        const building = document.getElementById('building').value;
        loadFloors(building);
    }

    function renderSeats(list) {
        const grid = document.getElementById('seatGrid');
        grid.innerHTML = '';
        if (!list || list.length === 0) {
            grid.innerHTML = '<p>暂无座位数据</p>';
            return;
        }
        list.forEach(seat => {
            const div = document.createElement('div');
            div.className = 'seat';
            if (seat.status === 'reserved') div.classList.add('reserved');
            const seatReaderId = seat.readerId != null ? Number(seat.readerId) : null;
            const currentMyId = getMyId();
            if (seatReaderId !== null && seatReaderId === currentMyId) {
                div.classList.add('me');
            }
            div.textContent = seat.seatCode || '未知';
            div.title = seat.nickname ? `已占用：${seat.nickname}` : '空闲';
            div.onclick = () => onSeatClick(seat);
            grid.appendChild(div);
        });
    }

    function getMyId() { return myId; }

    let currentSelectedSeat = null;

    async function onSeatClick(seat) {
        if (!loggedIn) {
            showToast('请先登录');
            return;
        }
        const seatReaderId = seat.readerId != null ? Number(seat.readerId) : null;
        const currentMyId = getMyId();

        if (seat.status === 'reserved' && seatReaderId !== null && seatReaderId !== currentMyId) {
            showToast('该座位已被占用');
            return;
        }

        const isMine = seatReaderId !== null && seatReaderId === currentMyId;
        if (isMine) {
            const confirmed = confirm(`确定要取消座位 ${seat.seatCode} 的预约吗？`);
            if (!confirmed) return;
            const building = document.getElementById('building').value;
            const floor = document.getElementById('floor').value;
            const url = '<%=request.getContextPath()%>/reader/space/reservations/cancel';
            const params = new URLSearchParams({ buildingId: building, floor, seatCode: seat.seatCode });
            try {
                const res = await fetch(url, { method: 'POST', body: params });
                const data = await res.json();
                if (!res.ok || data.success === false) throw new Error(data.message || '操作失败');
                showToast(data.message || '已取消预约');
                loadSeats();
                loadMy();
            } catch (e) {
                showToast(e.message || '操作失败');
            }
        } else {
            currentSelectedSeat = seat;
            showReservationModal(seat);
        }
    }

    function showReservationModal(seat) {
        const building = document.getElementById('building');
        const floor = document.getElementById('floor');
        const date = document.getElementById('date');
        const timeSlot = document.getElementById('timeSlot');
        const buildingName = building.options[building.selectedIndex]?.text || building.value;

        document.getElementById('modalSeatCode').textContent = seat.seatCode;
        document.getElementById('modalLocation').textContent = `${buildingName} ${floor.value}层`;

        // 直接使用日期下拉框中显示的文本
        const dateText = date.options[date.selectedIndex]?.text || date.value;
        document.getElementById('modalDate').textContent = dateText;

        document.getElementById('modalTimeSlot').textContent = timeSlot.options[timeSlot.selectedIndex].text;
        document.getElementById('reservationError').style.display = 'none';
        document.getElementById('reservationModal').classList.add('show');
    }

    function closeReservationModal() {
        document.getElementById('reservationModal').classList.remove('show');
        currentSelectedSeat = null;
    }

    function getTimeSlotHours(timeSlot) {
        const map = {
            '8-10': { start: 8, end: 10 },
            '10-12': { start: 10, end: 12 },
            '14-16': { start: 14, end: 16 },
            '16-18': { start: 16, end: 18 }
        };
        return map[timeSlot] || { start: 8, end: 10 };
    }

    async function confirmReservation() {
        if (!currentSelectedSeat) return;
        const building = document.getElementById('building').value;
        const floor = document.getElementById('floor').value;
        const timeSlot = document.getElementById('timeSlot').value;
        const date = getValidDateValue(); // 获取 value (yyyy-MM-dd)

        if (!date) {
            document.getElementById('reservationError').textContent = '日期无效';
            document.getElementById('reservationError').style.display = 'block';
            return;
        }

        const hours = getTimeSlotHours(timeSlot);
        const dateObj = new Date(date);
        const startTime = new Date(dateObj);
        startTime.setHours(hours.start, 0, 0, 0);
        const endTime = new Date(dateObj);
        endTime.setHours(hours.end, 0, 0, 0);

        const btn = document.getElementById('confirmReserveBtn');
        const errorEl = document.getElementById('reservationError');
        btn.disabled = true;
        btn.textContent = '正在预约...';
        errorEl.style.display = 'none';

        try {
            const params = new URLSearchParams({
                buildingId: building,
                floor: floor,
                seatCode: currentSelectedSeat.seatCode,
                startTime: startTime.toISOString(),
                endTime: endTime.toISOString()
            });
            const res = await fetch('<%=request.getContextPath()%>/reader/space/reservations/seat', {
                method: 'POST',
                body: params
            });
            const data = await res.json();

            if (!res.ok || data.success === false) {
                throw new Error(data.message || '预约失败');
            }

            showToast(data.message || '预约成功');
            closeReservationModal();
            loadSeats();
            loadMy();
        } catch (e) {
            errorEl.textContent = e.message || '预约失败，请稍后再试';
            errorEl.style.display = 'block';
        } finally {
            btn.disabled = false;
            btn.textContent = '确认预约';
        }
    }

    /**
     * 初始化日期选择器 - 兼容性优化版
     */
    function initDateSelector() {
        var dateSelect = document.getElementById('date');
        if (!dateSelect) return;
        dateSelect.innerHTML = '';

        // 获取今天
        var startDate = new Date();
        startDate.setHours(0, 0, 0, 0);

        var weekdays = ['日', '一', '二', '三', '四', '五', '六'];

        for (var i = 0; i < 5; i++) {
            var d = new Date(startDate);
            d.setDate(startDate.getDate() + i);

            // Value: yyyy-MM-dd (必须补零以供后端解析)
            var year = d.getFullYear();
            var month = d.getMonth() + 1;
            var day = d.getDate();

            // 使用传统方式补零，避免使用 padStart
            var monthPad = month < 10 ? '0' + month : '' + month;
            var dayPad = day < 10 ? '0' + day : '' + day;
            var dateValue = year + '-' + monthPad + '-' + dayPad;

            // Text: yyyy-M-d 周x (您要求的显示格式)
            // 注意：getDay() 返回 0-6，如果日期无效会 NaN，但 new Date() 应该总是有效的
            var weekdayIndex = d.getDay();
            var weekday = (weekdayIndex >= 0 && weekdayIndex < 7) ? weekdays[weekdayIndex] : '';

            var dateText = year + '-' + month + '-' + day + ' 周' + weekday;
            if (i === 0) {
                dateText += ' (今天)';
            }

            var opt = document.createElement('option');
            opt.value = dateValue;
            opt.textContent = dateText;
            dateSelect.appendChild(opt);
        }

        // 默认选中第一个
        if (dateSelect.options.length > 0) {
            dateSelect.selectedIndex = 0;
            dateSelect.value = dateSelect.options[0].value;
        }
    }

    function isValidDateFormat(dateStr) {
        if (!dateStr || typeof dateStr !== 'string') return false;
        // 简单正则：yyyy-MM-dd
        return /^\d{4}-\d{2}-\d{2}$/.test(dateStr);
    }

    function getValidDateValue() {
        var dateEl = document.getElementById('date');
        if (!dateEl) return null;
        if (dateEl.options.length === 0) {
            initDateSelector();
        }

        var val = dateEl.value;
        if (!isValidDateFormat(val) && dateEl.options.length > 0) {
            dateEl.selectedIndex = 0;
            val = dateEl.value;
        }
        return isValidDateFormat(val) ? val : null;
    }

    async function loadMy() {
        if (!loggedIn) {
            document.getElementById('myList').innerHTML = '<p style="color:#ef4444;">请先登录</p>';
            return;
        }
        try {
            const res = await fetch('<%=request.getContextPath()%>/reader/space/my-reservations');
            if (!res.ok) throw new Error('加载失败');
            const list = await res.json();
            if (list.length === 0) {
                document.getElementById('myList').innerHTML = '<p>暂无预约</p>';
                return;
            }
            let html = '<table><thead><tr><th>楼栋</th><th>楼层</th><th>座位</th><th>开始时间</th><th>结束时间</th><th>状态</th><th>操作</th></tr></thead><tbody>';
            list.forEach(r => {
                const buildingName = String(r.buildingName != null ? r.buildingName : (r.buildingId != null ? '楼栋' + r.buildingId : ''));
                const floor = String(r.floor || '');
                const seatCode = String(r.seatCode || '');

                // 格式化时间
                const reservedAt = formatTimeStr(r.reservedAt);
                const endAt = formatTimeStr(r.endAt);

                const status = String(r.status || '');
                const canCancel = status === '未完成';
                const cancelButton = canCancel
                    ? '<button class="secondary" onclick="cancel(\'' + (r.buildingId||'') + '\',\'' + floor + '\',\'' + seatCode + '\')">取消</button>'
                    : '<span style="color:#9ca3af;">-</span>';

                html += '<tr>' +
                    '<td>' + buildingName + '</td>' +
                    '<td>' + floor + '</td>' +
                    '<td>' + seatCode + '</td>' +
                    '<td>' + reservedAt + '</td>' +
                    '<td>' + endAt + '</td>' +
                    '<td>' + status + '</td>' +
                    '<td>' + cancelButton + '</td>' +
                    '</tr>';
            });
            html += '</tbody></table>';
            document.getElementById('myList').innerHTML = html;
        } catch (e) {
            document.getElementById('myList').innerHTML = '<p style="color:#ef4444;">加载失败: ' + e.message + '</p>';
        }
    }

    async function cancel(building, floor, seatCode) {
        const confirmed = confirm('确定要取消座位 ' + seatCode + ' 的预约吗？');
        if (!confirmed) return;

        const params = new URLSearchParams({ buildingId: building, floor: floor, seatCode: seatCode });
        try {
            const res = await fetch('<%=request.getContextPath()%>/reader/space/reservations/cancel', { method: 'POST', body: params });
            const data = await res.json();
            if (!res.ok || data.success === false) throw new Error(data.message || '取消失败');
            showToast(data.message || '已取消');
            loadSeats();
            loadMy();
        } catch (e) {
            showToast(e.message || '取消失败');
        }
    }

    // 初始化执行
    (function() {
        initDateSelector();
        loadBuildings();
        loadMy();
    })();
</script>
</body>
</html>