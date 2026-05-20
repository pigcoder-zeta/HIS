<template>
  <div>
    <h2 class="page-title">📊 工作台</h2>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">智慧医疗</div>
          <div class="stat-label">管理系统</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card card-blue">
          <div class="stat-value">{{ userStore.roleName }}</div>
          <div class="stat-label">当前角色</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card card-green">
          <div class="stat-value">{{ dayjs().format('YYYY-MM-DD') }}</div>
          <div class="stat-label">今日日期</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top:20px">
      <template #header><span style="font-weight:bold">快捷导航</span></template>
      <el-row :gutter="16">
        <template v-if="userStore.roleId === 2">
          <el-col :span="6"><el-button type="primary" style="width:100%;margin:4px 0" @click="$router.push('/doctor/today')">今日待诊</el-button></el-col>
          <el-col :span="6"><el-button type="success" style="width:100%;margin:4px 0" @click="$router.push('/doctor/record')">病历管理</el-button></el-col>
          <el-col :span="6"><el-button type="warning" style="width:100%;margin:4px 0" @click="$router.push('/doctor/prescription')">开具处方</el-button></el-col>
        </template>
        <template v-if="userStore.roleId === 5">
          <el-col :span="6"><el-button type="primary" style="width:100%;margin:4px 0" @click="$router.push('/patient/appointment')">预约挂号</el-button></el-col>
          <el-col :span="6"><el-button type="success" style="width:100%;margin:4px 0" @click="$router.push('/patient/records')">我的病历</el-button></el-col>
          <el-col :span="6"><el-button type="warning" style="width:100%;margin:4px 0" @click="$router.push('/patient/exam')">体检预约</el-button></el-col>
        </template>
        <template v-if="userStore.roleId === 3">
          <el-col :span="6"><el-button type="primary" style="width:100%;margin:4px 0" @click="$router.push('/pharmacy/prescriptions')">处方审核</el-button></el-col>
          <el-col :span="6"><el-button type="success" style="width:100%;margin:4px 0" @click="$router.push('/pharmacy/drugs')">药品管理</el-button></el-col>
          <el-col :span="6"><el-button type="danger" style="width:100%;margin:4px 0" @click="$router.push('/pharmacy/stock')">库存预警</el-button></el-col>
        </template>
        <el-col :span="6"><el-button type="info" style="width:100%;margin:4px 0" @click="$router.push('/ai/triage')">AI 智能导诊</el-button></el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { useUserStore } from '@/store/user'
import dayjs from 'dayjs'

const userStore = useUserStore()
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
.stat-card { text-align: center; padding: 20px 0; }
.stat-value { font-size: 28px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 14px; color: #999; margin-top: 8px; }
.card-blue .stat-value { color: #67c23a; }
.card-green .stat-value { color: #e6a23c; }
</style>
