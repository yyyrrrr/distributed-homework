# 流量治理压测说明（JMeter）

## 1. 启动被测服务

优先使用完整模式（含 Nacos）：

```powershell
mvn spring-boot:run
```

如果本机暂未启动 Nacos，可先用降级模式验证流量治理：

```powershell
mvn "-Dspring-boot.run.jvmArguments=-Dspring.cloud.nacos.discovery.enabled=false -Dspring.cloud.nacos.config.enabled=false -Dspring.cloud.service-registry.auto-registration.enabled=false" spring-boot:run
```

## 2. 压测目标接口

- 接口：`GET /api/traffic/governance`
- 参数：
  - `fail`：是否主动抛异常（用于触发熔断）
  - `sleepMs`：模拟下游耗时（用于压测响应时间与降级）

返回值字段 `status`：
- `ok`：正常通过
- `degraded`：触发限流/熔断后执行降级返回

## 3. 打开并运行测试计划

测试计划文件：`docs/jmeter/traffic-governance-test.jmx`

包含 3 组场景：
1. `01-FlowLimit`：高并发下触发限流
2. `02-CircuitBreaker`：高失败率触发熔断
3. `03-DegradeFallback`：慢调用和保护策略下观察降级

## 4. 命令行执行（无 GUI）

```powershell
jmeter -n -t docs/jmeter/traffic-governance-test.jmx -l docs/jmeter/result.jtl -e -o docs/jmeter/report
```

执行完成后打开：`docs/jmeter/report/index.html`

## 5. 结果判定

1. 响应体中出现 `status=degraded`，说明已发生治理保护。
2. 统计中吞吐上限明显受控，说明限流生效。
3. 在失败流量阶段，成功率与平均响应时间趋于稳定，说明熔断/降级起效。

## 6. 可观测指标

可通过 actuator 查看指标：

- `GET /actuator/metrics/resilience4j.ratelimiter.calls`
- `GET /actuator/metrics/resilience4j.circuitbreaker.calls`
