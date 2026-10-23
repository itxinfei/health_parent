/**
 * 预约管理业务单测。
 *
 * <p>类上的 {@code @Tag("dev")} 不是装饰：根 POM 把 surefire 的 {@code groups} 绑到
 * {@code ${profiles.active}}，没有匹配标签的用例不会被执行（BUILD SUCCESS 但 Tests run: 0）。
 * 新增测试类务必带上该标签，或按框架约定改用例标签。
 */
package com.xinfei.health.appointment.service.impl;
