# LifeStealS

[![Java](https://img.shields.io/badge/java-17-ED8B00.svg?logo=java)](https://openjdk.java.net/)
[![Kotlin](https://img.shields.io/badge/kotlin-1.6.10-585DEF.svg?logo=kotlin)](http://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/gradle-7.3.3-02303A.svg?logo=gradle)](https://gradle.org)
[![GitHub](https://img.shields.io/github/license/superjoy0502/LifeSteal)](https://github.com/superjoy0502/LifeSteal/blob/master/LICENSE.md)

PVP 기반의 미니게임인 LifeStealS은 다른 플레이어를 죽여 그 플레이어의 체력을 일부 빼앗고 끝까지 살아남기 위해 노력하는 게임입니다.

모든 체력을 다른 플레이어에게 빼앗기면 게임에서 탈락하며, 게임의 속행을 위해 게임의 난이도를 상승시키는 몇 가지 패널티가 준비되어 있습니다.

## 게임 규칙

1. 게임 시작시 모든 플레이어의 체력은 20으로 고정되며, 월드 내 랜덤한 위치로 이동합니다
  + 기본으로 지급되는 아이템
    + 돌 칼, 돌 도끼, 돌 곡괭이 각각 한 자루
    + 빵 10개
2. 빠른 진행을 위해 5분 간격으로 패널티가 살아남은 플레이어들에게 주어집니다
   + 예외적으로, 처음 10분간은 패널티가 주어지지 않습니다
   + 패널티는 하술하였습니다
3. 사망한 경우 아래와 같은 패널티가 해당 플레이어에게 부여됩니다
   + 다른 플레이어에 의해 사망한 경우
     + 죽인 플레이어가 죽은 플레이어의 하트 1개(기본 값. 게임 중 변동 가능) 가져감
     + 죽은 플레이어가 이로 인해 모든 하트가 소멸되면 탈락
     + 인벤 세이브 **없음**
   + 다른 몬스터에 의해 사망한 경우
     + 하트 1개(기본 값) 영구적 소멸
   + 기타 다른 사유에 의해 사망한 경우
     + 현재 소지하고 있는 하트 개수의 절반 삭제 (단, 버림으로 계산)
4. 플레이어가 게임에 참여하고 있는 도중에 서버에서 나가게 될 경우 아래와 같은 패널티를 얻습니다
  + 3분 이내 재접속한 경우
    + 패널티 없음. 게임 지속
  + 3분이 초과되었으나 재접속하지 않은 경우
    + 해당 플레이어 탈락 처리. 플레이어가 소지하고 있던 하트는 모두 소멸
  + 단, 게임 시작 60분 후에 서버를 나갈 시, 즉시 탈락 처리 됩니다
5. 나침반은 서버 시스템 상 조합할 수도, 자연적인 방법으로 얻을 수도 없습니다.

## 페널티

1. 처음 10분 (00:00 ~ 10:00)
   + 페널티 없음

2. 30분 까지 (10:00 ~ 30:00)
  + 5분마다 아래 패널티 중 1개, 최대 각 1회 적용 (확률은 모두 동일)
    1. 빼앗기는 하트의 수 1개 증가 (영구 적용)
    2. 5분 동안 모든 플레이어 발광 효과 부여
    3. 모든 플레이어의 하트 1개 영구적 소멸
    4. 5분동안 밤 12시, 난이도 보통 유지
    5. 5분동안 날씨 뇌우 유지

3. 60분 까지 (30:00 ~ 60:00)
  + 3분마다 아래 페널티 중 1개 부여
    1. 빼앗기는 하트의 수 1개 증가 (영구 적용)
       + 한 번에 빼앗기는 하트의 수는 최대 5개까지 늘어날 수 있습니다
    2. 가장 가까운 플레이어의 위치를 가리키는 나침반 제공
    3. 모든 플레이어의 하트 1개 영구적 소멸
    4. 모든 플레이어에게 발광 효과 부여
    5. 3분동안 밤 12시, 난이도 어려움 고정
    6. 3분동안 뇌우 유지, 난이도 어려움 고정
    7. 디버프 부여 (디버프 별로 확률 상이, 우유로 해제 가능)
       1. 채굴 피로 Level 1
       2. 채굴 피로 Level 2
       3. 허기
       4. 독 Level 1
       5. 독 Level 2
       6. 위더 Level 1
       7. 위더 Level 2
       8. 흉조
  + 위와 별개로 처음 30분이 지나면 월드 보더가 1초당 2블럭 씩 중심 좌표를 중심으로 한 변의 길이가 100칸으로 줄어들 때까지 감소합니다

4. 시작하고 60분이 경과한 경우  (60:00 ~)
   + 빼앗기는 하트의 수 5개로 고정
   + 난이도 어려움 고정
   + 밤, 뇌우 고정
   + 모든 플레이어에게 영구적으로 발광 효과 부여
   + 허기, 흉조 디버프 모두 Level 1로 고정 (영구 지속)
   + 더 이상 주기적 페널티 없음

## 명령어
* /lifesteal - 이 도움말을 출력합니다
* /lifesteal init - LifeStealS 게임의 중심 좌표(기본값 0, 0)를 정합니다
* /lifesteal start - LifeStealS 게임을 시작합니다
* /compass - (조건부) 가장 가까운 플레이어를 추적하는 나침반을 얻습니다

## 크레딧
* Developers
  * [@superjoy0502](https://github.com/superjoy0502) - Main Developer
  * [@sweetenpotato](https://github.com/sweetenpotato) - Designer
* Testers
  * [@Daybreak365](https://github.com/Daybreak365)
* Special Thanks
  * [@monun](https://github.com/monun) - External Libraries
      * #### `io.github.monun:heartbeat-coroutines` [![Maven Central](https://img.shields.io/maven-central/v/io.github.monun/heartbeat-coroutines)](https://search.maven.org/artifact/io.github.monun/heartbeat-coroutines/) [![Github](https://img.shields.io/github/license/monun/heartbeat-coroutines)](https://github.com/monun/heartbeat-coroutines/blob/master/LICENSE.md)
  * [플래그 Flag](https://www.youtube.com/channel/UC2eGI7us9gmMahbByyPnTwg)