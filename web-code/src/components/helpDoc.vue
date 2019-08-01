<template>
    <div style="height:100%">
        <content-head :headTitle="'帮助文档'"></content-head>
        <div class="help-doc-wrapper">
            <div class="title">
                <span style=" font-size:26px; font-weight:bold">区块链管理平台使用说明</span>
            </div>
            <h2>
                <span style=" font-weight:bold">1. 阅前关注</span><span></span>
            </h2>
            <p>
                <span>1.1 已经按照《区块链管理平台部署说明》把服务搭建起来</span>
            </p>
            <p>
                <span>1.2 建议使用谷歌浏览器</span>
            </p>
            <p>
                <span>1.3 在新增节点之前，其他多数功能都会受影响，建议优先添加节点信息</span>
            </p>
            <h2>
                <span style=" font-weight:bold">2.登录&登出</span> <span></span>
            </h2>
            <div>
                <span class="help-doc-second-title">2.1 管理员默认帐号</span>
                <p>
                    <span>帐号：admin</span>
                </p>
                <p>
                    <span>密码：Abcd1234</span>
                </p>
            </div>
            
            <div>
                <span class="help-doc-second-title">2.2 登陆页面</span>
                <p>
                    <span>在地址栏输入：http://{ngnixIp}:{nginxPort},然后按回车。如：htttp://localhost/8080</span>
                </p>
                <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.001.png" width="554" height="367"  alt="" :class="{'active':isChoose}" @click="imgScc" />
                    <p>
                        <span>（图2.2-1）</span>
                    </p>
            </div>
            
            <div>
                <span class="help-doc-second-title">2.3 修改密码</span>
                <p>
                    <span>密码由大小写字母和数字组成，且长度为6~18个字符</span>
                </p>
            </div>
            <div>
                <span class="help-doc-second-title">2.3.1 初次修改密码</span>
                <p>
                    <span>初次登录，或者被管理员修改密码后的首次登录，都需要修改登录密码</span>
                </p>
                <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.002.png" width="554" height="372" alt=""/>
                    <p>
                        <span>（图2.3.1-1）</span>
                    </p>
            </div>
            <div>
                <span class="help-doc-second-title">2.3.2 后续修改密码</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.003.png" width="554" height="175" alt=""/>
                </p>
                <p>
                    <span>（图2.3.2-1）</span>
                </p>
                <p>
                    <span>后续修改密码的步骤为：点击右上角的用户名，如“admin”->选择“密码修改”->在弹出框填写参数，并提交</span>
                </p>
            </div>
            <div>
                <span class="help-doc-second-title">2.4 登出</span>
                <p>
                    <img src="/static/image/sa073bb3d-1768-4e40-8ed6-d128178ab766.004.png" width="554" height="206" alt=""/>
                </p>
                <p>
                    <span>登出的步骤为：点击右上角的用户名，如“admin”->选择“退出”</span>
                </p>
            </div>
            
            <h2>
                <span style="font-weight:bold">3. 节点管理</span><span></span>
            </h2>
            <div>
                <span  class="help-doc-second-title">3.1 新增节点</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.005.png" width="554" height="153"/>
                </p>
                <p>
                    <span>（图3.1-1）</span>
                </p>
                <!-- <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.006.png" width="554" height="133" alt=""/>
                </p>
                <p>
                    <span>（图3.1-2）</span>
                </p> -->
                <p>
                    <span>新增节点的步骤为：点击菜单“节点管理” ->在节点管理页面点击“新增节点” 在弹窗分别输入前置服务的ip和端口,确认后系统将会从链上同步该前置对应的节点信息以及群组信息到本地</span>
                </p>
                <!-- <p>
                    <span style="color: #F56C6C">注：A、只能新增本机构的节点，其他机构的节点由系统自动同步</span>
                </p> -->
                <p>
                    <span style="color: #F56C6C">注：节点前置服务端口”是指节点对应的前置服务端口</span>
                </p>
            </div>
            <div>
                <span class="help-doc-second-title">3.2 节点查询</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.007.png" width="554" height="204" alt=""/>
                </p>
                <p>
                    <span>（图3.2-1）</span>
                </p>
                <p>
                    <span>节点查询的步骤为：点击菜单“节点管理”->查询框输入节点名称，点击查询按钮</span>
                </p>
            </div>
            <h2>
                <span style="font-weight:bold">4. 区块浏览</span> <span></span>
            </h2>
            <div>
                <span class="help-doc-second-title">4.1 区块信息查询</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.008.png" width="554" height="138" alt=""/>
                </p>
                <p>
                    <span>（图4.1-1）</span>
                </p>
                <p>
                    <span>区块查询的步骤为：在搜索框输入查询内容，点击查询按钮</span>
                </p>
                <p>
                    <span class="font-color-ed5454">注：不支持区块哈希和块高的模糊查询，请输入完整内容</span>
                </p>
            </div>
            <div >
                <span class="help-doc-second-title">4.2 交易信息查询</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.009.png" width="554" height="337" alt=""/>
                </p>
                <p>
                    <span>（图4.2-1）</span>
                </p>
                <p>
                    <span>交易查询的步骤为：在搜索框输入查询内容，点击查询按钮</span>
                </p>
                <p>
                    <span class="font-color-ed5454">注：不支持交易哈希和块高的模糊查询，请输入完整内容</span>
                </p>
            </div>
            <h2>
                <span style=" font-weight:bold">5. 合约管理</span> <span></span>
            </h2>
            <div>
                <span class="help-doc-second-title">5.1 新建合约</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.010.png" width="554" height="273" alt=""/>
                </p>
                <p>
                    <span>（图5.1-1）</span>
                </p>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.011.png" width="554" height="223" alt=""/>
                </p>
                <p>
                    <span>（图5.1-2）</span>
                </p>
                <p>
                    <span>新建合约的步骤为：点击菜单“合约管理”->点击合约管理页面左上角“新建”->在弹出框输入节点名称和版本号,点击“确定”按钮->接下来，在合约编辑窗口填写合约信息->点击右上角“保存”按钮</span>
                </p>
            </div>
            
            <div>
                <span class="help-doc-second-title">5.2 上传合约</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.012.png" width="554" height="186" alt=""/>
                </p>
                <p>
                    <span>（图5.2-1）</span>
                </p>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.013.png" width="554" height="191" alt=""/>
                </p>
                <p>
                    <span>（图5.2-2）</span>
                </p>
                <p>
                    <span>上传合约的步骤为：点击菜单“合约管理”->点击合约管理页面左上角“上传”->在弹出框依次填写合约版本、选择合约文件->点击“创建”按钮</span>
                </p>
            </div>
            
            <div>
                <span class="help-doc-second-title">5.3 合约的编译和部署</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.014.png" width="554" height="285" alt=""/>
                </p>
                <p>
                    <span>（图5.3-1）</span>
                </p>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.015.png" width="554" height="163" alt=""/>
                </p>
                <p>
                    <span>（图5.3-2）</span>
                </p>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.016.png" width="554" height="94" alt=""/>
                </p>
                <p>
                    <span>（图5.3-3）</span>
                </p>
                <p>
                    <span>部署合约的步骤为：点击菜单“合约管理”->在合约列表点击未部署的合约->点击页面右上角“编译”按钮->点击右上角“部署”按钮->在弹出框选择私钥用户->点击“确定”按钮</span>
                </p>
            </div>
            
            <div>
                <span class="help-doc-second-title">5.4 发送交易</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.017.png" width="554" height="257" alt=""/>
                </p>
                <p>
                    <span>（图5.4-1）</span>
                </p>
                <p>
                    <span>发送交易的步骤为：点击菜单“合约管理”->在合约列表点击已部署的合约（绿点）->点击合约管理页面右上角“发交易”按钮->在弹窗分别选择用户、方法、参数点击“确定”按钮</span>
                </p>
            </div>
            
            <h2>
                <span style="font-weight:bold">6. 私钥管理</span> <span></span>
            </h2>
            <div>
                <span class="help-doc-second-title">6.1新建公(私)钥用户</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.018.png" width="554" height="228" alt=""/>
                </p>
                <p>
                    <span>（图6.1-1）</span>
                </p>
                <p>
                    <span>新建公(私)钥的步骤为：点击菜单“私钥管理”->在私钥管理页面点击“新建用户”->在弹窗填写用户信息，点击“确定”按钮</span>
                </p>
            </div>
            
            <div>
                <span class="help-doc-second-title">6.2 公(私)钥用户查询</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.019.png" width="554" height="229" alt=""/>
                </p>
                <p>
                    <span>（图6.2-1）</span>
                </p>
                <p>
                    <span>公(私)钥用户查询的步骤为：点击菜单“私钥管理”->在用户管理的右上角搜索框输入用户名或者公钥地址（支持模糊查询），点击查询按钮</span>
                </p>
            </div>
            <h2>
                <span style=" font-weight:bold">7. 系统监控</span> <span></span>
            </h2>
            <div>
                <span class="help-doc-second-title">7.1 节点错误日志查看</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.020.png" width="554" height="220" alt=""/>
                </p>
                <p>
                    <span>（图7.1-1）</span>
                </p>
                <p>
                    <span>节点错误日志查看的步骤为：点击菜单“系统监控”->选择错误日志->选择查询条件，点击“确定”按钮</span>
                </p>
                <p>
                    <span class="font-color-ed5454">注：每个节点最多只保留最近一万条数据</span>
                </p>
            </div>
            
            <h2>
                <span style=" font-weight:bold">8. 交易审计</span> <span></span>
            </h2>
            <div>
                <span class="help-doc-second-title">8.1 查看用户交易信息</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.021.png" width="554" height="278" alt=""/>
                </p>
                <p>
                    <span>（图8.1-1）</span>
                </p>
                <p>
                    <span>用户交易查看的步骤为：点击菜单“交易审计”->选择“用户交易”->选择查询条件，点击“确定”按钮</span>
                </p>
                <p>
                    <span class="font-color-ed5454">注：如果是异常的用户，在用户的下拉列表中显示为红色</span>
                </p>
            </div>
            
            <div>
                <span class="help-doc-second-title">8.2 查看异常用户</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.022.png" width="554" height="250" alt=""/>
                </p>
                <p>
                    <span>（图8.2-1）</span>
                </p>
                <p>
                    <span></span>
                </p>
                <p>
                    <span>异常用户查看的步骤为：</span>
                </p>
                <p>
                    <span>点击菜单“交易审计”->选择“异常用户”->选择查询条件，点击“确定”按钮</span>
                </p>
            </div>
            
            <div>
                <span class="help-doc-second-title">8.3 查看异常合约</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.023.png" width="554" height="261" alt=""/>
                </p>
                <p>
                    <span>（图8.3-1）</span>
                </p>
                <p>
                    <span>异常用户查看的步骤为：点击菜单“交易审计”->选择“异常合约”->选择查询条件，点击“确定”按钮</span>
                </p>
            </div>
            
            <h2>
                <span style=" font-weight:bold">9. 账户管理</span> <span></span>
            </h2>
            <p>
                <span class="font-color-ed5454">注：帐号的角色分为管理员和访客，管理员和访客权限上唯一的区别是，管理员可以看到“帐号管理”功能</span>
            </p>
            <div>
                <span class="help-doc-second-title">9.1 新建帐号</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.024.png" width="554" height="236" alt=""/>
                </p>
                <p>
                    <span>（图9.1-1）</span>
                </p>
                <p>
                    <span>新建帐号的步骤为：点击菜单“帐号管理”->点击帐号管理页面的“新增帐号”按钮->在弹窗中填写帐号信息，点击“确定”按钮</span>
                </p>
            </div>
            
            <div>
                <span class="help-doc-second-title">9.2 删除帐号</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.025.png" width="554" height="230" alt=""/>
                </p>
                <p>
                    <span>（图9.2-1）</span>
                </p>
                <p>
                    <span>删除帐号的步骤为：点击菜单“帐号管理”->点击要删除的帐号的“删除”按钮->在弹窗中点击“确定”按钮</span>
                </p>
            </div>
            
            <div>
                <span class="help-doc-second-title">9.3 修改其他帐号密码</span>
                <p>
                    <img src="/static/image/a073bb3d-1768-4e40-8ed6-d128178ab766.026.png" width="554" height="214" alt=""/>
                </p>
                <p>
                    <span>（图9.3-1）</span>
                </p>
                <p>
                    <span>修改其他帐号密码的步骤为：点击菜单“帐号管理”->点击对应帐号的“修改”按钮->在弹窗中输入帐号的新密码，点击“确定”按钮</span>
                </p>
            </div>
            
        </div>
        <div style="height:50px;">

        </div>
    </div>
</template>

<script>
import contentHead from "./contentHead";
export default {
    name: "helpDoc",
    components: {
        contentHead
    },
    data() {
        return {
             isChoose:false
        };
    },
    methods:{
        imgScc:function () {                     
            this.isChoose = !this.isChoose  
        }
    }
};
</script>

<style scoped>
.help-doc-wrapper {
    padding: 30px 0 0 31px;
    background: #fff;
}
.title::after {
    display: block;
    clear: both;
    content: "";
} 
p {
    font-size: 14px;
}
p > span {
}
.help-doc-title {
    margin: 14px 0 10px 0;
}
h2 {
    padding: 20px 0 20px 0;
    font-size: 22px;
}
h2 > span:first-child {
    border-left: 13px solid #2956a3;
    padding-left: 18px;
    display: block;
}
h2 > span:nth-child(2n) {
    border-bottom: 1px solid #ebeef5;
    display: block;
    margin-left: 167px;
    position: relative;
    bottom: 18px;
}
.help-doc-second-title {
    font-size: 16px;
    font-weight: bold;
    padding: 4px 0;
    display: inline-block;
    margin-bottom: 8px;
}
img.active {     
    /* transform: scale(2);          
    position: absolute;          
    z-index: 100;  */
}  
</style>
