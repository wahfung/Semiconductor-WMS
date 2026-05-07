import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Space, Progress, message, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { warehouseAPI } from '../services/api';

const Warehouses = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
  }, [pagination.current, pagination.pageSize]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const response = await warehouseAPI.getAll({
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

  const handleAdd = () => {
    setEditingRecord(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      const response = await warehouseAPI.delete(id);
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
        const response = await warehouseAPI.update(editingRecord.id, values);
        if (response.success) {
          message.success('更新成功');
        }
      } else {
        const response = await warehouseAPI.create(values);
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
    { title: '仓库编码', dataIndex: 'warehouseCode', key: 'warehouseCode', width: 120 },
    { title: '仓库名称', dataIndex: 'warehouseName', key: 'warehouseName', width: 150 },
    { title: '位置', dataIndex: 'address', key: 'address', width: 150 },
    { title: '负责人', dataIndex: 'manager', key: 'manager', width: 100 },
    { title: '联系电话', dataIndex: 'phone', key: 'phone', width: 130 },
    {
      title: '容量使用',
      key: 'capacity',
      width: 200,
      render: (_, record) => {
        const percent = record.totalCapacity > 0
          ? (record.usedCapacity / record.totalCapacity) * 100
          : 0;
        return (
          <div>
            <Progress
              percent={percent}
              size="small"
              strokeColor={percent > 80 ? '#ff4d4f' : percent > 50 ? '#faad14' : '#52c41a'}
              format={() => `${record.usedCapacity || 0}/${record.totalCapacity || 0}`}
            />
          </div>
        );
      },
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
        <h2>仓库管理</h2>
        <p>管理仓库基本信息和容量配置</p>
      </div>

      <div className="content-card">
        <div className="action-buttons">
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增仓库
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
        title={editingRecord ? '编辑仓库' : '新增仓库'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={500}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="warehouseCode" label="仓库编码" rules={[{ required: true, message: '请输入仓库编码' }]}>
            <Input placeholder="请输入仓库编码" />
          </Form.Item>
          <Form.Item name="warehouseName" label="仓库名称" rules={[{ required: true, message: '请输入仓库名称' }]}>
            <Input placeholder="请输入仓库名称" />
          </Form.Item>
          <Form.Item name="address" label="位置">
            <Input placeholder="请输入仓库位置" />
          </Form.Item>
          <Form.Item name="manager" label="负责人">
            <Input placeholder="请输入负责人姓名" />
          </Form.Item>
          <Form.Item name="phone" label="联系电话">
            <Input placeholder="请输入联系电话" />
          </Form.Item>
          <Form.Item name="totalCapacity" label="总容量">
            <InputNumber style={{ width: '100%' }} min={0} placeholder="请输入总容量" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Warehouses;
