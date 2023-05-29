<template>
    <el-menu
        class="sidemenu"
        :default-active="$route.path"
        mode="vertical"
        router
        active-text-color="coral">
        <el-menu-item v-for="(item,i) in sideList" :key="i" :index="item.name" :class="item.icon">
            {{ item.sideItem }}
        </el-menu-item>
    </el-menu>
</template>

<script>
    export default {
        name: 'SideMenu',
        data(){
            return {
                sideList: [
                    {name: '/cluster?clusterId=1', sideItem: 'Server', icon: 'el-icon-s-home'},
                    {name: '/cluster?clusterId=2', sideItem: 'Database', icon: 'el-icon-s-data'},
                ],
                url: ''
            }
        }
    }
</script>

<style scoped>
    .sidemenu{
        position: fixed;
        top: 80px;
        width: 120px;
        height: 85%;
        text-align: left;
        z-index: 5;
        background: #fff;
        border: 1px solid #eaeaea;
        box-shadow: 0 0 15px #cac6c6;
    }
    .el-menu-item.is-active {
        background-color: #fff !important;
        color: #fff;
    }
    .el-menu-item:hover{
        background-color: #fff;
    }
</style>

