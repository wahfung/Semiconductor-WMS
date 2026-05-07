import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Select, Space, Tag, message, Popconfirm } from 'antd';
import { PlusOutlined, CheckOutlined, EyeOutlined, EditOutlined } from '@ant-design/icons';
import { stocktakingOrderAPI, warehouseAPI } from '../services/api';
import dayjs from 'dayjs';

const { Option } = Select;

const statusColors = { IN_PROGRESS: 'blue', COMPLETED: 'green' };
const statusLabels = { IN_PROGRESS: '盘点中', COMPLETED: '已完成' };
const stocktakingTypes = [
  { value: 'FULL', label: '全面盘点' },
  { value: 'PARTIAL', label: '部分盘点' },
  { value: 'CYCLE', label: '循环盘点' },
];

const StocktakingOrders = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
    fetchWarehouses();
  }, [pagination.current, pagination.pageSize]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const response = await stocktakingOrderAPI.getAll({ page: pagination.current - 1, size: pagination.pageSize });
      if (response.success) {
        setData(response.data);
        setPagination((prev) => ({ ...prev, total: response.pageInfo?.total || 0 }));
      }
    } catch (error) {
      message.error('获取数据失败');
    } finally {
      setLoading(false);
    }
  };

  const fetchWarehouses = async () => {
    const response = await warehouseAPI.getAllEnabled();
    if (response.success) setWarehouses(response.data);
  };

  const handleAdd = () => {
    form.resetFields();
    setModalVisible(true);
  };

  const handleView = async (record) => {
    const response = await stocktakingOrderAPI.getById(record.id);
    if (response.success) {
      setCurrentRecord(response.data);
      setDetailVisible(true);
    }
  };

  const handleComplete = async (id) => {
    const response = await stocktakingOrderAPI.complete(id);
    if (response.success) {
      message.success('盘点完成');
      fetchData();
    }
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const response = await stocktakingOrderAPI.create(values);
    if (response.success) {
      message.success('创建成功');
      setModalVisible(false);
      fetchData();
    }
  };

  const handleUpdateItem = async (orderId, itemId, actualQuantity) => {
    const response = await stocktakingOrderAPI.updateItem(orderId, itemId, { actualQuantity });
    if (response.success) {
      setCurrentRecord(response.data);
      message.success('更新成功');
    }
  };

  const columns = [
    { title: '盘点单号', dataIndex: 'orderNo', key: 'orderNo', width: 180 },
    { title: '盘点类型', dataIndex: 'stocktakingType', key: 'stocktakingType', width: 100, render: (t) => stocktakingTypes.find((i) => i.value === t)?.label || t },
    { title: '仓库', dataIndex: 'warehouseName', key: 'warehouseName', width: 120 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 100, render: (s) => <Tag color={statusColors[s]}>{statusLabels[s]}</Tag> },
    { title: '操作人', dataIndex: 'operatorName', key: 'operatorName', width: 100 },
    { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 180, render: (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-' },
    { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 180, render: (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-' },
    {
      title: '操作', key: 'action', width: 200,
      render: (_, record) => (
        <Space>
          <Button type="link" icon={<EyeOutlined />} onClick={() => handleView(record)}>查看</Button>
          {record.status === 'IN_PROGRESS' && (
            <Popconfirm title="确定完成盘点吗？" onConfirm={() => handleComplete(record.id)}>
              <Button type="link" icon={<CheckOutlined />} style={{ color: '#52c41a' }}>完成盘点</Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  const itemColumns = [
    { title: '物料编码', dataIndex: 'materialCode', width: 120 },
    { title: '物料名称', dataIndex: 'materialName', width: 150 },
    { title: '货架', dataIndex: 'shelfCode', width: 120 },
    { title: '货位', dataIndex: 'slotCode', width: 140 },
    { title: '系统数量', dataIndex: 'systemQuantity', width: 100 },
    {
      title: '实际数量', dataIndex: 'actualQuantity', width: 120,
      render: (v, record) => currentRecord?.status === 'IN_PROGRESS' ? (
        <InputNumber
          min={0}
          defaultValue={v}
          onBlur={(e) => handleUpdateItem(currentRecord.id, record.id, parseInt(e.target.value))}
          style={{ width: 80 }}
        />
      ) : v ?? '-',
    },
    {
      title: '差异', dataIndex: 'differenceQuantity', width: 80,
      render: (v) => v ? (
        <Tag color={v > 0 ? 'green' : v < 0 ? 'red' : 'default'}>
          {v > 0 ? `+${v}` : v}
        </Tag>
      ) : '-',
    },
    {
      title: '状态', dataIndex: 'status', width: 80,
      render: (s) => <Tag color={s === 'COUNTED' ? 'green' : 'orange'}>{s === 'COUNTED' ? '已盘' : '待盘'}</Tag>,
    },
  ];

  return (
    <div className="fade-in">
      <div className="page-header">
        <h2>盘点管理</h2>
        <p>管理仓库库存盘点</p>
      </div>

      <div className="content-card">
        <div className="action-buttons">
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>新建盘点单</Button>
        </div>

        <Table columns={columns} dataSource={data} rowKey="id" loading={loading}
          pagination={{ ...pagination, showSizeChanger: true, showTotal: (total) => `共 ${total} 条`,
            onChange: (page, pageSize) => setPagination({ ...pagination, current: page, pageSize }) }}
          scroll={{ x: 1200 }}
        />
      </div>

      <Modal title="新建盘点单" open={modalVisible} onOk={handleSubmit} onCancel={() => setModalVisible(false)} width={500}>
        <Form form={form} layout="vertical">
          <Form.Item name="stocktakingType" label="盘点类型" rules={[{ required: true }]}>
            <Select placeholder="选择盘点类型">
              {stocktakingTypes.map((t) => <Option key={t.value} value={t.value}>{t.label}</Option>)}
            </Select>
          </Form.Item>
          <Form.Item name="warehouseId" label="盘点仓库" rules={[{ required: true }]}>
            <Select placeholder="选择仓库">
              {warehouses.map((w) => <Option key={w.id} value={w.id}>{w.warehouseName}</Option>)}
            </Select>
          </Form.Item>
          <Form.Item name="remark" label="备注"><Input.TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>

      <Modal title="盘点单详情" open={detailVisible} onCancel={() => setDetailVisible(false)} footer={null} width={1000}>
        {currentRecord && (
          <div>
            <Space style={{ marginBottom: 16 }}>
              <span><strong>盘点单号：</strong>{currentRecord.orderNo}</span>
              <span><strong>仓库：</strong>{currentRecord.warehouseName}</span>
              <span><strong>状态：</strong><Tag color={statusColors[currentRecord.status]}>{statusLabels[currentRecord.status]}</Tag></span>
            </Space>
            <Table dataSource={currentRecord.items} rowKey="id" pagination={false} size="small"
              columns={itemColumns} scroll={{ x: 900 }}
            />
          </div>
        )}
      </Modal>
    </div>
  );
};

export default StocktakingOrders;
