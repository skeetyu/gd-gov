<template>
    <el-container>
        <el-header>
            <Nav-menu></Nav-menu>
        </el-header>
        <router-view/>
    </el-container>
</template>

<script>
    import NavMenu from './common/NavMenu.vue'
    export default {
        name: 'Home',
        components: { NavMenu }
    }
</script>

<style scoped>

</style>