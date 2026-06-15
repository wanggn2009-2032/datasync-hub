<template>
  <div>
    <el-form :model="form" label-width="80px" size="small">
      <el-form-item label="节点ID">
        <el-input v-model="form.id" disabled />
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="form.label" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.data.type" placeholder="请选择类型">
          <el-option label="task" value="task" />
          <el-option label="start" value="start" />
          <el-option label="end" value="end" />
        </el-select>
      </el-form-item>
      <el-form-item label="表">
        <el-input v-model="form.data.table" placeholder="目标表或来源表" />
      </el-form-item>
      <el-form-item label="配置(JSON)">
        <el-input type="textarea" v-model="configText" rows="6" placeholder='{