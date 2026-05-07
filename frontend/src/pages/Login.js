import React, { useState } from 'react';
import { Form, Input, Button, message } from 'antd';
import { UserOutlined, LockOutlined, BoxPlotOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { authAPI } from '../services/api';
import { useAuthStore } from '../utils/store';

const Login = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { setAuth } = useAuthStore();

  const onFinish = async (values) => {
    setLoading(true);
    try {
      const response = await authAPI.login(values);
      if (response.success) {
        const { token, username, realName, role } = response.data;
        setAuth(token, { username, realName, role });
        message.success('登录成功');
        navigate('/dashboard');
      } else {
        message.error(response.message || '登录失败');
      }
    } catch (error) {
      message.error('登录失败，请检查用户名和密码');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <div className="login-title">
          <BoxPlotOutlined style={{ fontSize: 48, color: '#1890ff', marginBottom: 16 }} />
          <h1>半导体仓库管理系统</h1>
          <p>Semiconductor Warehouse Management System</p>
        </div>
        <Form
          name="login"
          onFinish={onFinish}
          size="large"
          initialValues={{ username: 'admin', password: '123456' }}
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input
              prefix={<UserOutlined style={{ color: '#bfbfbf' }} />}
              placeholder="用户名"
            />
          </Form.Item>
          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              prefix={<LockOutlined style={{ color: '#bfbfbf' }} />}
              placeholder="密码"
            />
          </Form.Item>
          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              style={{
                height: 48,
                fontSize: 16,
                background: 'linear-gradient(90deg, #667eea 0%, #764ba2 100%)',
                border: 'none',
              }}
            >
              登 录
            </Button>
          </Form.Item>
        </Form>
      </div>
    </div>
  );
};

export default Login;
