<template>
	<view>
		<u-grid :col="3" :border="false" @click="toNav">
			<u-grid-item index="1">
				<u-badge :count="msgNum.thumb_collect" :offset="[10, 50]"></u-badge>
				<image class="nav-icon" src="/static/images/icon/souc.png"></image>
				<view class="grid-text">赞和收藏</view>
			</u-grid-item>
			<u-grid-item index="2">
				<u-badge :count="msgNum.follow" :offset="[10, 50]"></u-badge>
				<image class="nav-icon" src="/static/images/icon/gz.png"></image>
				<view class="grid-text">新增关注</view>
			</u-grid-item>
			<u-grid-item index="3">
				<u-badge :count="msgNum.comment" :offset="[10, 50]"></u-badge>
				<image class="nav-icon" src="/static/images/icon/pl.png"></image>
				<view class="grid-text">评论</view>
			</u-grid-item>
		</u-grid>
		<view class="msg-wrap">
			<view class="title">
				<view class="left">私信</view>
			</view>
			<!-- 图文消息 -->
			<block v-if="msgNum.article_msg_list.length > 0">
				<navigator @longpress="onPressArticle(item.m_id,index)" :url="'/pages/article-detail/article-detail?id='+item.post_id+'&time='+item.create_time"
				 class="msg-item" v-for="(item,index) in msgNum.article_msg_list" :key="index">
					<view class="avatar-box">
						<u-badge v-if="item.status == 0" :is-dot="true" :offset="[5,10]" type="error"></u-badge>
						<u-avatar class="avatar" :src="item.user_info.avatar"></u-avatar>
					</view>
					<view class="right">
						<view>
							<text style="margin-right: 10rpx;">{{item.user_info.username}}</text>
							<u-tag bg-color="#8687fd" text="官方" type="error" size="mini" mode="dark" />
						</view>
						<view class="desc">
							<text>{{item.article_title}}</text>
							<text class="time">{{item.create_time | timeFrom}}</text>
						</view>
					</view>
				</navigator>
			</block>
			<block v-if="msgNum.chat_msg_list.length > 0">
				<navigator @longpress="onPressChat(item.user_info.uid,index)" :url="'/pages/chat/chat?user='+ $f.tostring(item.user_info)"
				 class="msg-item" v-for="(item,index) in msgNum.chat_msg_list" :key="index">
					<view class="avatar-box">
						<u-badge :count="item.count" :offset="[5,10]" type="error"></u-badge>
						<u-avatar class="avatar" :src="item.user_info.avatar"></u-avatar>
					</view>
					<view class="right">
						<view>
							<text style="margin-right: 10rpx;">{{item.user_info.username}}</text>
						</view>
						<view class="desc">
							<text>{{item.msg.content}}</text>
							<text class="time">{{item.create_time | timeFrom}}</text>
						</view>
					</view>
				</navigator>
			</block>
			<block v-if="msgNum.chat_msg_list.length == 0 && msgNum.article_msg_list.length == 0">
				<view class="msg-empty">
					<image class="img" mode="widthFix" src="/static/empty.png"></image>
					<text class="txt">暂无新消息</text>
				</view>
			</block>
		</view>
		<!-- 删除图文消息弹窗 -->
		<u-action-sheet :list="sheetList" v-model="showSheet" @click="onSheetItem"></u-action-sheet>
		<!-- 删除私信消息弹窗 -->
		<u-action-sheet :list="sheetList" v-model="showChatSheet" @click="onSheeChat"></u-action-sheet>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				showSheet: false,
				showChatSheet:false,
				sheetList: [{
					text: '删除',
					color: 'red'
				}],
				msgNum: {
					thumb_collect: 0,
					follow: 0,
					comment: 0,
					article_msg_list: [],
					chat_msg_list:[]
				},
				checkedMsgId: "",
				checkedIndex: "",
				checkedMsgUid:""
			};
		},
		onShow() {
			this.getMsgNum();
		},
		methods: {
			// 长按图文消息
			onPressArticle(id, index) {
				this.showSheet = true;
				this.checkedMsgId = id;
				this.checkedIndex = index;
			},
			// 长按私信
			onPressChat(uid,index){
				this.showChatSheet = true;
				this.checkedIndex = index;
				this.checkedMsgUid = uid;
			},
			// 删除图文消息
			onSheetItem(index) {
				if (index == 0) {
					this.$H.post("message/delMsg", {
						id: this.checkedMsgId
					}).then(res => {
						if (res.code == 200) {
							this.msgNum.article_msg_list.splice(this.checkedIndex, 1)
						}
					})
				}
			},
			// 删除私信消息
			onSheeChat(index){
				if (index == 0) {
					this.$H.post("message/delChat", {
						uid: this.checkedMsgUid
					}).then(res => {
						if (res.code == 200) {
							this.msgNum.chat_msg_list.splice(this.checkedIndex, 1)
						}
					})
				}
			},
			getMsgNum() {
				this.$H.post("message/num").then(res => {

					this.msgNum = res.result;

					let num = res.result;
					if (num.all_count > 0) {
						uni.setTabBarBadge({
							index: 3,
							text: num.all_count.toString()
						})
					} else {
						uni.removeTabBarBadge({
							index: 3
						})
					}
				})
			},
			toNav(e) {
				uni.navigateTo({
					url: "/pages/message-list/message-list?type=" + e
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import 'message.scss';
</style>
