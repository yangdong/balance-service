import k6 from 'k6';
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '1m', target: 50 },    // 快速增加到50用户
        { duration: '3m', target: 100 },   // 保持100用户一段时间
        { duration: '3m', target: 150 },   // 继续增加到150用户
        { duration: '1m', target: 0 },     // 缓慢降低到0
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95%的请求必须在500ms内完成
        http_req_failed: ['rate<0.01'],   // 错误率必须小于1%
        http_reqs: ['rate>100'],          // 每秒至少处理100个请求
    },
};

// 生成随机账号
function getRandomAccount() {
    const accountNum = Math.floor(Math.random() * 1000) + 1;
    return 'acc_' + accountNum.toString().padStart(6, '0');
}

// 生成随机金额
function getRandomAmount() {
    return Math.floor(Math.random() * 100).toString();
}

export default function () {
    // 获取两个不同的随机账号
    let sourceAccount = getRandomAccount();
    let targetAccount;
    do {
        targetAccount = getRandomAccount();
    } while (targetAccount === sourceAccount);

    let url = 'http://localhost:8088/api/transactions';
    let payload = JSON.stringify({
        sourceAccount: sourceAccount,
        targetAccount: targetAccount,
        amount: getRandomAmount(),
    });

    let params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    let res = http.post(url, payload, params);

    // 检查响应
    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    sleep(0.5);
}