import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Statistic, Table, Tag, Spin, Progress } from 'antd';
import {
  AppstoreOutlined,
  InboxOutlined,
  ImportOutlined,
  ExportOutlined,
  ShoppingCartOutlined,
  AuditOutlined,
} from '@ant-design/icons';
import { dashboardAPI } from '../services/api';

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState(null);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const response = await dashboardAPI.getData();
      if (response.success) {
        setData(response.data);
      }
    } catch (error) {
      console.error('Failed to fetch dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const statCards = [
    { title: '物料种类', value: data?.totalMaterials || 0, icon: <AppstoreOutlined />, color: 'blue' },
    { title: '库存记录', value: data?.totalInventory || 0, icon: <InboxOutlined />, color: 'green' },
    { title: '今日入库', value: data?.todayInbound || 0, icon: <ImportOutlined />, color: 'orange' },
    { title: '今日出库', value: data?.todayOutbound || 0, icon: <ExportOutlined />, color: 'purple' },
  ];

  const recentColumns = [
    { title: '类型', dataIndex: 'type', key: 'type', render: (type) => (
      <Tag color={type === '入库' ? 'green' : type === '出库' ? 'orange' : 'blue'}>{type}</Tag>
    )},
    { title: '单号', dataIndex: 'orderNo', key: 'orderNo' },
    { title: '描述', dataIndex: 'description', key: 'description' },
    { title: '操作人', dataIndex: 'operatorName', key: 'operatorName' },
    { title: '时间', dataIndex: 'time', key: 'time' },
  ];

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div className="fade-in">
      <div className="page-header">
        <h2>工作台</h2>
        <p>欢迎使用半导体仓库管理系统</p>
      </div>

      <Row gutter={[24, 24]}>
        {statCards.map((card, index) => (
          <Col xs={24} sm={12} lg={6} key={index}>
            <div className={`stat-card ${card.color}`} style={{ position: 'relative', overflow: 'hidden' }}>
              <div className="stat-content">
                <div className="stat-value">{card.value}</div>
                <div className="stat-label">{card.title}</div>
              </div>
              <div className="stat-icon">{card.icon}</div>
            </div>
          </Col>
        ))}
      </Row>

      <Row gutter={[24, 24]} style={{ marginTop: 24 }}>
        <Col xs={24} lg={12}>
          <Card title="物料类型分布" className="content-card">
            {data?.materialTypeStats?.map((item, index) => (
              <div key={index} style={{ marginBottom: 16 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                  <span>{item.type}</span>
                  <span>{item.quantity} 件</span>
                </div>
                <Progress
                  percent={Math.min(100, (item.quantity / 15000) * 100)}
                  strokeColor={index === 0 ? '#1890ff' : index === 1 ? '#52c41a' : '#722ed1'}
                  showInfo={false}
                />
              </div>
            ))}
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="仓库使用情况" className="content-card">
            {data?.warehouseStats?.map((item, index) => (
              <div key={index} style={{ marginBottom: 16 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                  <span>{item.warehouseName}</span>
                  <span>{item.usedCapacity} / {item.totalCapacity}</span>
                </div>
                <Progress
                  percent={item.usageRate}
                  strokeColor={item.usageRate > 80 ? '#ff4d4f' : item.usageRate > 50 ? '#faad14' : '#52c41a'}
                  format={(percent) => `${percent.toFixed(1)}%`}
                />
              </div>
            ))}
          </Card>
        </Col>
      </Row>

      <Row gutter={[24, 24]} style={{ marginTop: 24 }}>
        <Col span={24}>
          <Card title="最近操作记录" className="content-card">
            <Table
              columns={recentColumns}
              dataSource={data?.recentOperations}
              rowKey={(record, index) => index}
              pagination={false}
              size="middle"
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[24, 24]} style={{ marginTop: 24 }}>
        <Col xs={24} sm={12}>
          <Card className="content-card">
            <Statistic
              title="待处理采购单"
              value={data?.pendingPurchase || 0}
              prefix={<ShoppingCartOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12}>
          <Card className="content-card">
            <Statistic
              title="进行中盘点"
              value={data?.pendingStocktaking || 0}
              prefix={<AuditOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;
