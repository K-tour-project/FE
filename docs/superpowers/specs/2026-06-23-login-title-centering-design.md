# LoginScreen 상단 제목 중앙 정렬 설계

## 목표

`LoginScreen`의 상단 앱바 제목인 "로그인"을 화면 가로 중앙에 배치한다.

## 변경 범위

- Material 3의 `TopAppBar`를 `CenterAlignedTopAppBar`로 교체한다.
- 제목의 글꼴 크기와 굵기, 뒤로가기 동작, 흰색 배경은 유지한다.
- 본문과 로그인 버튼 등 다른 UI는 변경하지 않는다.

## 검증

- 프로젝트가 컴파일되는지 확인한다.
- Compose Preview 또는 실행 화면에서 뒤로가기 아이콘과 무관하게 제목이 앱바 중앙에 표시되는지 확인한다.
