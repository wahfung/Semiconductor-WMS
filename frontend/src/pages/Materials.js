import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, InputNumber, Space, Tag, message, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import { materialAPI } from '../services/api';

const { Option } = Select;

const materialTypes = [
  { value: 'RAW_MATERIAL', label: '原材料', color: 'blue' },
  { value: 'SEMI_PRODUCT', label: '半制品', color: 'orange' },
  { value: 'FINISHED_PRODUCT', label: '产成品', color: 'green' },
];

const Materials = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState(null);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
  }, [pagination.current, pagination.pageSize, searchKeyword]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const response = await materialAPI.getAll({
        page: pagination.current - 1,
        size: pagination.pageSize,
        keyword: searchKeyword,
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
      const response = await materialAPI.delete(id);
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
        const response = await materialAPI.update(editingRecord.id, values);
        if (response.success) {
          message.success('更新成功');
        }
      } else {
        const response = await materialAPI.create(values);
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
    { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode', width: 120 },
    { title: '物料名称', dataIndex: 'materialName', key: 'materialName', width: 150 },
    {
      title: '类型',
      dataIndex: 'materialType',
      key: 'materialType',
      width: 100,
      render: (type) => {
        const typeInfo = materialTypes.find((t) => t.value === type);
        return <Tag color={typeInfo?.color}>{typeInfo?.label || type}</Tag>;
      },
    },
    { title: '规格', dataIndex: 'specification', key: 'specification', width: 150 },
    { title: '单位', dataIndex: 'unit', key: 'unit', width: 80 },
    { title: '单价', dataIndex: 'price', key: 'price', width: 100, render: (v) => v ? `¥${v}` : '-' },
    { title: '库存数量', dataIndex: 'totalQuantity', key: 'totalQuantity', width: 100 },
    { title: '供应商', dataIndex: 'supplier', key: 'supplier', width: 150 },
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
        <h2>物料管理</h2>
        <p>管理半导体生产所需的原材料、半制品和产成品</p>
      </div>

      <div className="content-card">
        <div className="action-buttons">
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增物料
          </Button>
          <Input.Search
            placeholder="搜索物料编码或名称"
            allowClear
            style={{ width: 300 }}
            onSearch={(value) => setSearchKeyword(value)}
          />
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
          scroll={{ x: 1200 }}
        />
      </div>

      <Modal
        title={editingRecord ? '编辑物料' : '新增物料'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="materialCode" label="物料编码" rules={[{ required: true, message: '请输入物料编码' }]}>
            <Input placeholder="请输入物料编码" />
          </Form.Item>
          <Form.Item name="materialName" label="物料名称" rules={[{ required: true, message: '请输入物料名称' }]}>
            <Input placeholder="请输入物料名称" />
          </Form.Item>
          <Form.Item name="materialType" label="物料类型" rules={[{ required: true, message: '请选择物料类型' }]}>
            <Select placeholder="请选择物料类型">
              {materialTypes.map((type) => (
                <Option key={type.value} value={type.value}>{type.label}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="specification" label="规格">
            <Input placeholder="请输入规格" />
          </Form.Item>
          <Form.Item name="unit" label="单位">
            <Input placeholder="请输入单位" />
          </Form.Item>
          <Form.Item name="price" label="单价">
            <InputNumber style={{ width: '100%' }} min={0} precision={2} placeholder="请输入单价" />
          </Form.Item>
          <Form.Item name="supplier" label="供应商">
            <Input placeholder="请输入供应商" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Materials;
