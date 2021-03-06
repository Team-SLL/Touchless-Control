# Touchless-Control

> ML Kit의 Face Detection 기술을 활용한 Touchless 모바일 제어 기술          
> Touchless YouTube application using ML KIT‘s Face Detection.

- [2021-02 : 01-03 산학프로젝트 문서](https://space.malangmalang.com/open?fileId=m:0:944584451&lang=ko)      
- [2022-01 : 01-03 캡스톤디자인 문서](https://space.malangmalang.com/open?fileId=m:0:1041409819&lang=ko)           
- [깃허브](https://github.com/Team-SLL/Touchless-Control)                    

#### 팀원

2016039085 박시현        
2018068005 전아현            
2019038037 이하은         

#### 지도교수

이종연 교수님

## 주요 기술

> ML KIT의 Face Detection API를 활용한 애플리케이션으로, 동영상 목록 및 재생 등의 환경에서 얼굴 움직임만으로 모든 조작이 가능한 애플리케이션.

1. 유튜브 API를 통해 인기 동영상 및 검색한 동영상 목록을 제공한다.
2. 얼굴 움직임을 통해 동영상 선택, 재생, 일시 중지, 종료, 검색 등의 조작이 가능하다.
3. 검색은 키보드 뿐 아니라 음성을 통해서도 가능하다.
4. 얼굴 움직임을 사용자가 원하는 설정으로 변경 가능하다.
  - ex) 왼쪽 고갯짓이 기본 설정은 왼쪽 동영상으로 이동이지만, 오른쪽 동영상으로 이동 등으로 변경 가능하다.
5. 주 기능은 얼굴 인식을 통한 화면 제어이지만, 부 기능으로 얼굴을 인식하지 못하는 상황 등을 위해 음성을 통해 화면을 조작할 수 있는 기능 제공('재생'이라고 말할 시 동영상 재생 등)

## 패키지 설명

- camera : 전면 카메라를 통해 영상을 가져오고, 해당 영상을 가공해 화면에 출력할 수 있게 하는 패키지
- facedetector : 영상에서 얼굴정보를 추출해 랜드마크 좌표를 출력하고, 해당 좌표를 이용해 얼굴이 어떤 움직임을 하는지 계산하는 패키지
- preference : 얼굴 인식을 위해 기본 설정을 셋팅, 저장 하는 유틸리티 패키지
- screen : 화면에 출력되는 모든 정보를 가진 패키지
- STT : 음성을 텍스트로 변환하는 패키지
- youtube : 유튜브에서 인기동영상 및 검색 동영상을 가져오는 패키지


+ MainActivity : 애플리케이션이 실행되는 액티비티
+ PermissionSupport : 카메라와 마이크의 권한을 설정하는 클래스
+ VisionProcessorBase : 카메라를 통해 영상을 가져올 때 전처리 하는 클래스
