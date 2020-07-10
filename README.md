# ![Image](https://github.com/albertkhang/TuneDaily/blob/develop/WorkInProgress/Logo/icon.png) TuneDaily

## Design ##
1. Logo Idea
```
   https://dribbble.com/shots/5710495-Music-Player
   https://dribbble.com/shots/4785105-A-Music-Player-Daily-UI-005
```
2. Color
```
   Main Palette: #4EAEFF(main), #60B6FF, #71BEFF, #83C6FF, #95CEFF, #A7D7FF, #B8DFFF, #CAE7FF.
   Dark Palette: #1E1E1E, #353535, #4B4B4B, #626262, #787878.
   Light Palette: #FBFBFB, #E2E2E2, #C9C9C9, #B0B0B0, #979797.
```
3. Logo Font: Futura
4. UI Idea: xem tại [đây](https://github.com/albertkhang/TuneDaily/tree/develop/WorkInProgress/Idea).
5. UI: phần UI của app được xây dựng dựa trên 2 app chính là Zing MP3 và Spotify. Xem tại [đây](https://github.com/albertkhang/TuneDaily/tree/develop/WorkInProgress/UI).

## Tính năng ##
Tính năng được xây dựng dựa trên những tính năng của những app nghe nhạc phổ biến như Zing MP3, Spotify, NhacCuaTui.
1. Hiển thị miniplayer cho người phép dùng tương tác với bài hát/playlist hiện tại.
```
   1.1. Dừng/phát nhạc.
   1.2. Phát bài hát tiếp theo trong playlist.
   1.3. Thêm vào playlist favorite.
```
2. Hiển thị fullplayer cho người phép dùng ngoài những tính năng có ở miniplayer còn có thêm:
```
   2.1. Phát lại từ đầu khi nhấn nút quay về, và quay lại bài hát trước trong playlist nếu nhấn nhanh 2 lần.
   2.2. Kiểu phát nhạc(ngẫu nhiên/theo thứ tự). - chỉ phát được ngẫu nhiên trong playlist
   2.3. Kiểu lặp lại(không lặp lại/bài hát hiện tại/toàn bộ playlist).
   2.4. Hiển thị thời gian đang phát của bài hát hiện tại.
   2.5. Cho phép người dùng điều chỉnh vị trí phát của bài hát hiện tại.
```
3. Hiển thị playback trên notification bar và lockscreen cho phép người dùng tương tác như fullplayer ngoại trừ tính năng chọn kiểu phát và lặp lại.
4. Hiển thị một danh sách bài hát cho phép người dùng chọn một bài hát trong danh sách để phát nhạc.
5. Nếu nhạc đang phát, thì nhạc vẫn sẽ tiếp tục phát và hiển thị playback kể cả khi người dùng mở ứng dụng khác, clear app hoặc khóa máy.
6. Dừng phát nếu một trình phát nhạc khác bắt đầu phát nhạc.
7. Tùy chỉnh giao diện sáng/tối.
8. **Join vào một stream của server**: khi server mở stream, ở màn hình chính của ứng dụng sẽ tự hiển thị dialog để thông báo và sẽ tự ẩn đi nếu dùng stream.

**Tính năng bổ sung**

9. Tùy chỉnh ngôn ngữ app.
10. Tìm kiếm album/track: trong khi người dùng gõ thì sẽ không search, khi dừng gõ khoảng một giây mới thực hiện search.
11. Download nhạc: chưa thể play được offline do thiết kế api/logic bị lỗi. Nhưng vẫn có thể dùng file đã download để play nếu có kết nối.
12. Thêm nhạc vào playlist hiện tại và reset nếu kill app.
13. Nhấn back hai lần để thoát.
14. Thêm một bài vào playlist.

## Cách sử dụng ##
Về cách sử dụng, TuneDaily cũng có cách sử dụng giống như các app nghe nhạc phổ biến hiện nay.

## Tài nguyên ##
1. Lưu trữ: sử dụng Firebase để lưu trữ dữ liệu
2. Bài hát: được download ngẫu nhiên trên trang https://chiasenhac.vn. Số lượng bài hát: 100.
3. Album: được tạo ngẫu nhiên từ 100 bài hát trên với số lượng bài ở mỗi album khoảng 10 bài. Số lượng album: 30.

## Streaming Server ##
* Nhà cung cấp VPS: [Vultr](https://www.vultr.com/)
* Thông tin server
```
   Location: Singapore
   IP Address: 45.76.150.28
   RAM: 1024 MB
   Storage: 25 GB SSD
   Max Bandwidth: 1000 GB
   OS: Win7_Ult_SP1_x64
```
* Công cụ tạo server: [Node-Media-Server](https://www.npmjs.com/package/node-media-server)
* URL để phát stream: sử dụng OBS để phát stream
```
   URL: rtmp://45.76.150.28/live
   Stream key: android
```
* URL để lắng nghe stream
```
   rtmp://45.76.150.28/live/android
```

## Tài nguyên trong quá trình làm ##
[WorkInProgress](https://github.com/albertkhang/TuneDaily/tree/develop/WorkInProgress)

**Nội Dung**
1. Idea: chứa những hỉnh để lên ý tưởng thiết kế giao UI cho app. Bên cạnh đó cũng dựa trên UI của Zing MP3, Nhaccuatui, Spotify.
2. Logo:  chứa file thiết kế logo app.
3. UI: chứa hình ảnh đã thiết kế của app trước khi bắt đầu code UI.
4. FeaturesTest.xlsx: chứa những test case đã test thành công.
5. Report.pptx: slide báo cáo.

## Thư viện ##
1. [shimmer-android](https://github.com/facebook/shimmer-android): tạo animation trong lúc download dữ liệu.
2. [PageIndicatorView](https://github.com/romandanylyk/PageIndicatorView): animation để chỉ ra fragment nào được chọn trong ViewPager.
3. [Firebase](https://firebase.google.com/docs/android/setup?authuser=0): những thư viện của Firebase giúp thao tác với dữ liệu được lưu trên Firebase.
4. [Glide](https://github.com/bumptech/glide): thư viện hỗ trợ download ảnh và cache ảnh.
5. [EventBus](https://github.com/greenrobot/EventBus): thư viện giúp gửi/nhận broadcast một cách dễ dàng.
6. [Paper](https://github.com/pilgr/Paper): là thư viện kiểu noSQL database, giúp lưu data xuống local ở dạng object.
7. [MediaBrowserServiceCompat](https://developer.android.com/guide/topics/media-apps/audio-app/building-an-audio-app): thư viện của Android được xây dựng để hỗ trợ việc play media.
8. [TedPermission](https://github.com/ParkSangGwon/TedPermission): thư viện giúp request premisstion một cách dễ dàng.
9. [NodeMediaClient-Android](https://github.com/NodeMedia/NodeMediaClient-Android): thư viện giúp giao tiếp với [node-media-server](https://www.npmjs.com/package/node-media-server).
