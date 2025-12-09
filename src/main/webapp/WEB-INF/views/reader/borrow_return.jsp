<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <title>借还功能</title>
  <style>
    body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,"Microsoft YaHei",sans-serif;margin:0;padding:20px;padding-top:5rem;background:#f7f7f7;color:#374151}
    .container{max-width:1200px;margin:0 auto}
    .card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:24px;margin-bottom:20px}
    .title{font-size:28px;font-weight:bold;margin-bottom:20px;color:#2c3e50;text-align:center}
    .tabs{border-bottom:1px solid #e5e7eb;margin-bottom:20px;display:flex}
    .tab-item{padding:10px 20px;cursor:pointer;font-size:18px;font-weight:bold;color:#6b7280;text-decoration:none;border-bottom:3px solid transparent}
    .tab-item.active{color:#2563eb;border-bottom-color:#2563eb}
    .tab-item:hover{color:#2563eb}
    .form-section{margin-bottom:30px;max-width:500px;margin-left:auto;margin-right:auto}
    .form-section h2{font-size:22px;margin-bottom:15px;border-bottom:1px solid #e5e7eb;padding-bottom:10px}
    .form-group{margin-bottom:15px}
    .form-group label{display:block;margin-bottom:5px;font-weight:bold}
    .form-group input{width:100%;padding:10px;border:1px solid #ccc;border-radius:6px;box-sizing:border-box}
    .btn{background:#2563eb;color:#fff;border:none;padding:10px 20px;border-radius:8px;cursor:pointer;font-size:16px;display:inline-block}
    .btn:hover{background:#1d4ed8}
    .btn-return{background:#10b981}
    .btn-return:hover{background:#059669}
    .toast{position:fixed;top:20px;left:50%;transform:translateX(-50%);background:#2ecc71;color:#fff;padding:10px 20px;border-radius:6px;box-shadow:0 4px 10px rgba(0,0,0,0.15);z-index:9999;display:none}
    .toast.error{background:#e57373}
    .toast.show{display:block;animation:fadeInOut 2s ease forwards}
    @keyframes fadeInOut{0%{opacity:0;transform:translate(-50%,-20px)}20%{opacity:1;transform:translate(-50%,0)}80%{opacity:1}100%{opacity:0;transform:translate(-50%,-20px)}}
  </style>
</head>
<body>
<%@ include file="/WEB-INF/common/navbar.jsp" %>
<div class="container">
  <h1 class="title">📚 我的借阅</h1>

  <div class="tabs">
    <a href="<%=request.getContextPath()%>/reader/borrow-records" class="tab-item">借阅记录</a>
    <a href="<%=request.getContextPath()%>/reader/borrow-return" class="tab-item active">借还功能</a>
  </div>

  <div class="card">
    <!-- 借阅图书 -->
    <div class="form-section">
      <h2>借阅图书</h2>
      <form id="borrowForm">
        <div class="form-group">
          <label for="borrowReaderId">读者ID:</label>
          <input type="text" id="borrowReaderId" value="1" required readonly style="background:#eee">
        </div>
        <div class="form-group">
          <label for="borrowBarcode">图书条形码:</label>
          <input type="text" id="borrowBarcode" placeholder="请输入图书条形码" required>
        </div>
        <button type="submit" class="btn">确认借阅</button>
      </form>
    </div>

    <!-- 归还图书 -->
    <div class="form-section">
      <h2>归还图书</h2>
      <form id="returnForm">
        <div class="form-group">
          <label for="returnReaderId">读者ID:</label>
          <input type="text" id="returnReaderId" value="1" required readonly style="background:#eee">
        </div>
        <div class="form-group">
          <label for="returnBarcode">图书条形码:</label>
          <input type="text" id="returnBarcode" placeholder="请输入图书条形码" required>
        </div>
        <button type="submit" class="btn btn-return">确认归还</button>
      </form>
    </div>
  </div>
</div>

<!-- 提示框 -->
<div id="toast" class="toast"></div>

<script>
  document.getElementById('borrowForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const readerId = document.getElementById('borrowReaderId').value;
    const barcode = document.getElementById('borrowBarcode').value;
    const url = '<%=request.getContextPath()%>/api/borrowing/borrow?readerId=' + readerId + '&barcode=' + encodeURIComponent(barcode);

    fetch(url, { method: 'POST' })
      .then(handleResponse)
      .then(data => {
        showToast(data.data || data.message || '借阅成功', '');
        document.getElementById('borrowBarcode').value = '';
      })
      .catch(error => {
        showToast(error.message || '借阅失败', 'error');
      });
  });

  document.getElementById('returnForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const readerId = document.getElementById('returnReaderId').value;
    const barcode = document.getElementById('returnBarcode').value;
    const url = '<%=request.getContextPath()%>/api/borrowing/return?readerId=' + readerId + '&barcode=' + encodeURIComponent(barcode);

    fetch(url, { method: 'POST' })
      .then(handleResponse)
      .then(data => {
        showToast(data.message || '归还成功', '');
        document.getElementById('returnBarcode').value = '';
      })
      .catch(error => {
        showToast(error.message || '归还失败', 'error');
      });
  });

  function handleResponse(response) {
    return response.json().then(data => {
      if (!response.ok) {
        throw new Error(data.message || `HTTP error! status: ${response.status}`);
      }
      return data;
    });
  }

  function showToast(message, type) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = 'toast' + (type ? ' ' + type : '') + ' show';
    setTimeout(() => {
      toast.classList.remove('show');
    }, 2000);
  }
</script>
</body>
</html>