import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Select, Space, Tag, message, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { shelfAPI, warehouseAPI } from '../services/api';

const { Option } = Select;

const Shelves = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
    fetchWarehouses();
  }, [pagination.current, pagination.pageSize]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const response = await shelfAPI.getAll({
        page: pagination.current - 1,
        size: pagination.pageSize,
      });
      if (response.success) {
        setData(response.data);
        setPagination((prev) => ({
          ...prev,
          total: response.pageInfo?.total || 0,
        }));
      }
    } catch (error) {
      message.error('获取数据失败');
    } finally {
      setLoading(false);
    }
  };

  const fetchWarehouses = async () => {
    try {
      const response = await warehouseAPI.getAllEnabled();
      if (response.success) {
        setWarehouses(response.data);
      }
    } catch (error) {
      console.error('Failed to fetch warehouses:', error);
    }
  };

  const handleAdd = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({
      rowNum: 4,
      columnNum: 5,
      layerNum: 3,
      positionX: 0,
      positionY: 0,
      positionZ: 0,
      width: 200,
      depth: 80,
      height: 250,
    });
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      const response = await shelfAPI.delete(id);
      if (response.success) {
        message.success('删除成功');
        fetchData();
      }
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingRecord) {
        const response = await shelfAPI.update(editingRecord.id, values);
        if (response.success) {
          message.success('更新成功');
        }
      } else {
        const response = await shelfAPI.create(values);
        if (response.success) {
          message.success('创建成功');
        }
      }
      setModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const columns = [
    { title: '货架编码', dataIndex: 'shelfCode', key: 'shelfCode', width: 130 },
    { title: '货架名称', dataIndex: 'shelfName', key: 'shelfName', width: 150 },
    { title: '所属仓库', dataIndex: 'warehouseName', key: 'warehouseName', width: 120 },
    {
      title: '规格(行×列×层)',
      key: 'spec',
      width: 130,
      render: (_, record) => `${record.rowNum}×${record.columnNum}×${record.layerNum}`,
    },
    {
      title: '位置(X,Y,Z)',
      key: 'position',
      width: 120,
      render: (_, record) => `(${record.positionX}, ${record.positionY}, ${record.positionZ})`,
    },
    {
      title: '货位使用',
      key: 'slots',
      width: 120,
      render: (_, record) => (
        <Tag color={record.usedSlots > record.totalSlots * 0.8 ? 'red' : 'green'}>
          {record.usedSlots || 0} / {record.totalSlots || 0}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>编辑</Button>
          <Popconfirm title="确定删除吗？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="fade-in">
      <div className="page-header">
        <h2>货架管理</h2>
        <p>管理仓库货架及货位配置</p>
      </div>

      <div className="content-card">
        <div className="action-buttons">
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增货架
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (page, pageSize) => setPagination({ ...pagination, current: page, pageSize }),
          }}
        />
      </div>

      <Modal
        title={editingRecord ? '编辑货架' : '新增货架'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="warehouseId" label="所属仓库" rules={[{ required: true, message: '请选择仓库' }]}>
            <Select placeholder="请选择仓库">
              {warehouses.map((w) => (
                <Option key={w.id} value={w.id}>{w.warehouseName}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="shelfCode" label="货架编码" rules={[{ required: true, message: '请输入货架编码' }]}>
            <Input placeholder="请输入货架编码" />
          </Form.Item>
          <Form.Item name="shelfName" label="货架名称" rules={[{ required: true, message: '请输入货架名称' }]}>
            <Input placeholder="请输入货架名称" />
          </Form.Item>
          <Space style={{ width: '100%' }} size="large">
            <Form.Item name="rowNum" label="行数" style={{ width: 120 }}>
              <InputNumber min={1} max={10} />
            </Form.Item>
            <Form.Item name="columnNum" label="列数" style={{ width: 120 }}>
              <InputNumber min={1} max={10} />
            </Form.Item>
            <Form.Item name="layerNum" label="层数" style={{ width: 120 }}>
              <InputNumber min={1} max={10} />
            </Form.Item>
          </Space>
          <Space style={{ width: '100%' }} size="large">
            <Form.Item name="positionX" label="位置X" style={{ width: 120 }}>
              <InputNumber />
            </Form.Item>
            <Form.Item name="positionY" label="位置Y" style={{ width: 120 }}>
              <InputNumber />
            </Form.Item>
            <Form.Item name="positionZ" label="位置Z" style={{ width: 120 }}>
              <InputNumber />
            </Form.Item>
          </Space>
          <Space style={{ width: '100%' }} size="large">
            <Form.Item name="width" label="宽度" style={{ width: 120 }}>
              <InputNumber min={1} />
            </Form.Item>
            <Form.Item name="depth" label="深度" style={{ width: 120 }}>
              <InputNumber min={1} />
            </Form.Item>
            <Form.Item name="height" label="高度" style={{ width: 120 }}>
              <InputNumber min={1} />
            </Form.Item>
          </Space>
        </Form>
      </Modal>
    </div>
  );
};

export default Shelves;
