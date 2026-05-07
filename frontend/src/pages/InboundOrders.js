import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Select, Space, Tag, message, Popconfirm } from 'antd';
import { PlusOutlined, CheckOutlined, EyeOutlined } from '@ant-design/icons';
import { inboundOrderAPI, materialAPI, warehouseAPI, shelfAPI } from '../services/api';
import dayjs from 'dayjs';

const { Option } = Select;

const statusColors = { PENDING: 'orange', COMPLETED: 'green' };
const statusLabels = { PENDING: '待入库', COMPLETED: '已完成' };
const inboundTypes = [
  { value: 'PURCHASE', label: '采购入库' },
  { value: 'RETURN', label: '退货入库' },
  { value: 'TRANSFER', label: '调拨入库' },
  { value: 'OTHER', label: '其他入库' },
];

const InboundOrders = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  const [materials, setMaterials] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [shelves, setShelves] = useState([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState(null);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
    fetchMaterials();
    fetchWarehouses();
  }, [pagination.current, pagination.pageSize]);

  useEffect(() => {
    if (selectedWarehouseId) {
      fetchShelves(selectedWarehouseId);
    }
  }, [selectedWarehouseId]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const response = await inboundOrderAPI.getAll({ page: pagination.current - 1, size: pagination.pageSize });
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

  const fetchMaterials = async () => {
    const response = await materialAPI.getAllEnabled();
    if (response.success) setMaterials(response.data);
  };

  const fetchWarehouses = async () => {
    const response = await warehouseAPI.getAllEnabled();
    if (response.success) setWarehouses(response.data);
  };

  const fetchShelves = async (warehouseId) => {
    const response = await shelfAPI.getByWarehouseId(warehouseId);
    if (response.success) setShelves(response.data);
  };

  const handleAdd = () => {
    form.resetFields();
    form.setFieldsValue({ items: [{}] });
    setSelectedWarehouseId(null);
    setShelves([]);
    setModalVisible(true);
  };

  const handleView = async (record) => {
    const response = await inboundOrderAPI.getById(record.id);
    if (response.success) {
      setCurrentRecord(response.data);
      setDetailVisible(true);
    }
  };

  const handleConfirm = async (id) => {
    const response = await inboundOrderAPI.confirm(id);
    if (response.success) {
      message.success('入库确认成功');
      fetchData();
    }
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const response = await inboundOrderAPI.create(values);
    if (response.success) {
      message.success('创建成功');
      setModalVisible(false);
      fetchData();
    }
  };

  const columns = [
    { title: '入库单号', dataIndex: 'orderNo', key: 'orderNo', width: 180 },
    { title: '入库类型', dataIndex: 'inboundType', key: 'inboundType', width: 100, render: (t) => inboundTypes.find((i) => i.value === t)?.label || t },
    { title: '仓库', dataIndex: 'warehouseName', key: 'warehouseName', width: 120 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 100, render: (s) => <Tag color={statusColors[s]}>{statusLabels[s]}</Tag> },
    { title: '操作人', dataIndex: 'operatorName', key: 'operatorName', width: 100 },
    { title: '入库时间', dataIndex: 'inboundTime', key: 'inboundTime', width: 180, render: (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-' },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180, render: (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-' },
    {
      title: '操作', key: 'action', width: 180,
      render: (_, record) => (
        <Space>
          <Button type="link" icon={<EyeOutlined />} onClick={() => handleView(record)}>查看</Button>
          {record.status === 'PENDING' && (
            <Popconfirm title="确定入库吗？" onConfirm={() => handleConfirm(record.id)}>
              <Button type="link" icon={<CheckOutlined />} style={{ color: '#52c41a' }}>确认入库</Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="fade-in">
      <div className="page-header">
        <h2>入库管理</h2>
        <p>管理物料入库操作</p>
      </div>

      <div className="content-card">
        <div className="action-buttons">
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>新建入库单</Button>
        </div>

        <Table columns={columns} dataSource={data} rowKey="id" loading={loading}
          pagination={{ ...pagination, showSizeChanger: true, showTotal: (total) => `共 ${total} 条`,
            onChange: (page, pageSize) => setPagination({ ...pagination, current: page, pageSize }) }}
          scroll={{ x: 1200 }}
        />
      </div>

      <Modal title="新建入库单" open={modalVisible} onOk={handleSubmit} onCancel={() => setModalVisible(false)} width={800}>
        <Form form={form} layout="vertical">
          <Space style={{ width: '100%' }} size="large">
            <Form.Item name="inboundType" label="入库类型" rules={[{ required: true }]} style={{ width: 200 }}>
              <Select placeholder="选择入库类型">
                {inboundTypes.map((t) => <Option key={t.value} value={t.value}>{t.label}</Option>)}
              </Select>
            </Form.Item>
            <Form.Item name="warehouseId" label="目标仓库" rules={[{ required: true }]} style={{ width: 200 }}>
              <Select placeholder="选择仓库" onChange={(v) => setSelectedWarehouseId(v)}>
                {warehouses.map((w) => <Option key={w.id} value={w.id}>{w.warehouseName}</Option>)}
              </Select>
            </Form.Item>
          </Space>
          <Form.List name="items" initialValue={[{}]}>
            {(fields, { add, remove }) => (
              <>
                {fields.map(({ key, name, ...restField }) => (
                  <Space key={key} style={{ display: 'flex', marginBottom: 8 }} align="baseline">
                    <Form.Item {...restField} name={[name, 'materialId']} rules={[{ required: true }]}>
                      <Select style={{ width: 180 }} placeholder="选择物料">
                        {materials.map((m) => <Option key={m.id} value={m.id}>{m.materialCode} - {m.materialName}</Option>)}
                      </Select>
                    </Form.Item>
                    <Form.Item {...restField} name={[name, 'shelfId']}>
                      <Select style={{ width: 150 }} placeholder="选择货架" allowClear>
                        {shelves.map((s) => <Option key={s.id} value={s.id}>{s.shelfCode}</Option>)}
                      </Select>
                    </Form.Item>
                    <Form.Item {...restField} name={[name, 'batchNo']}>
                      <Input placeholder="批次号" style={{ width: 120 }} />
                    </Form.Item>
                    <Form.Item {...restField} name={[name, 'quantity']} rules={[{ required: true }]}>
                      <InputNumber min={1} placeholder="数量" />
                    </Form.Item>
                    {fields.length > 1 && <Button type="link" danger onClick={() => remove(name)}>删除</Button>}
                  </Space>
                ))}
                <Button type="dashed" onClick={() => add()} block>添加物料</Button>
              </>
            )}
          </Form.List>
          <Form.Item name="remark" label="备注"><Input.TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>

      <Modal title="入库单详情" open={detailVisible} onCancel={() => setDetailVisible(false)} footer={null} width={700}>
        {currentRecord && (
          <div>
            <p><strong>入库单号：</strong>{currentRecord.orderNo}</p>
            <p><strong>入库类型：</strong>{inboundTypes.find((t) => t.value === currentRecord.inboundType)?.label}</p>
            <p><strong>仓库：</strong>{currentRecord.warehouseName}</p>
            <p><strong>状态：</strong><Tag color={statusColors[currentRecord.status]}>{statusLabels[currentRecord.status]}</Tag></p>
            <Table dataSource={currentRecord.items} rowKey="id" pagination={false} size="small"
              columns={[
                { title: '物料编码', dataIndex: 'materialCode' },
                { title: '物料名称', dataIndex: 'materialName' },
                { title: '货架', dataIndex: 'shelfCode' },
                { title: '批次号', dataIndex: 'batchNo' },
                { title: '数量', dataIndex: 'quantity' },
              ]}
            />
          </div>
        )}
      </Modal>
    </div>
  );
};

export default InboundOrders;
