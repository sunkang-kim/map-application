document.addEventListener('DOMContentLoaded', function () {
    const t = 'T00:00:01'
    const calendarEl = document.getElementById('calendar');
    const calendar = new FullCalendar.Calendar(calendarEl, {
        timeZone: 'UTC',
        initialView: 'dayGridMonth', // 홈페이지에서 다른 형태의 view를 확인할  수 있다.
        editable: false, // false로 변경 시 draggable 작동 x 
        locale: 'ko', //한국어
        selectable: true, //드래그해서 선택가능
        displayEventTime: false, // 시간 표시 x
        events: [ // 일정 데이터 추가 , DB의 event를 가져오려면 JSON 형식으로 변환해 events에 넣어주기

        ],
        headerToolbar: {
            center: 'addEventButton' // headerToolbar에 버튼을 추가
        },
        customButtons: {
            addEventButton: { // 추가한 버튼 설정
                text: "일정 추가",  // 버튼 내용
                click: function () { // 버튼 클릭 시 이벤트 추가
                    $("#calendarModal").modal("show"); // modal 나타내기

                    $("#addCalendar").on("click", function () {  // modal의 추가 버튼 클릭 시
                        const content = $("#calendar_content").val(); //#calendar_content의 value값
                        const startDate = $("#calendar_start_date").val(); //#calendar_start_date의 value값
                        const endDate = $("#calendar_end_date").val(); //#calendarr_end_date의 value값

                        //내용 입력 여부 확인
                        if (content == null || content == "") {
                            alert("내용을 입력하세요.");
                        } else if (startDate == "" || endDate == "") {
                            alert("날짜를 입력하세요.");
                        } else if (new Date(startDate) - new Date(endDate) > 0) { // date 타입으로 변경 후 확인
                            alert("종료일이 시작일보다 먼저입니다.");
                        } else { // 정상 입력 시
                            $("#calendarModal").modal("hide");
                        }
                    });
                }
            }
        },
        eventClick: function (info) {
            // 전체일정 클릭시 모달 창 띄우기
            $("#allPlan").modal("show");
            const title = info.event.title;
            const startD = info.event.start;
            const endD = info.event.end;

            const startDay = moment(startD).format('YYYY-MM-DD'); //'YYYY-MM-DD' 형식의 문자열 반환
            const endDay = moment(endD).format('YYYY-MM-DD'); //'YYYY-MM-DD' 형식의 문자열 반환

            console.log(title);
            console.log(startDay);
            console.log(endDay);

            const event = calendar.getEventById(info.event.id); // 이벤트 가져오기 (getEventById = 해당 id를 가진 일정을 검색하여 반환)
            const start = moment(event.start).toDate(); // 시작일을 날짜객체로 변환
            const end = moment(event.end).toDate(); // 종료일을 날짜객체로 변환
            const container = document.querySelector('.plans'); // 일정 목록 div


            for (let date = start; date <= end; // date가 end보다 작거나 같을 동안 반복
                //getDate() 메소드로 현재 날짜의 일(day)을 가져와서 1을 더한 값을 setDate() 메소드로 다시 date에 대입
                date.setDate(date.getDate() + 1)) {  // date 변수에는 start부터 end까지 날짜가 하루씩 증가하면서 저장
                    const a = document.createElement('a'); // a 태그 생성
                    const text = `일정내용: ${event.title} <br>날짜: ${moment(date).format('YYYY-MM-DD')}`; // 텍스트 생성
                    a.innerHTML = text; // 텍스트 추가
                    a.setAttribute('href', `https://localhost:8080/schedule/${event.id}`); // href 추가
                    a.setAttribute('class', 'daysPlan'); // a태그의 클래스 추가
                    container.appendChild(a); // a 태그 추가
            } 



            $('input[id=modify_content]').val(title); // id가 modify_content인 input에 title값 넣기
            $('input[id=modify_start_date]').val(startDay); // id가 modify_start_date인 input에 startDay값 넣기
            $('input[id=modify_end_date]').val(endDay); // id가 modify_end_date인 input에 endDay값 넣기

            $("#modifyCalendar").on("click", function () {  // modal의 수정 버튼 클릭 시
                $("#modiModal").modal("show");
                const modifiedTitle = $('input[id=modify_content]').val(); //id가 modify_content인 input에 value값 가져오기
                const modifiedStartDate = $('input[id=modify_start_date]').val(); //id가 modify_content인 input에 value값 가져오기
                const modifiedEndDate = $('input[id=modify_end_date]').val(); //id가 modify_content인 input에 value값 가져오기

                //내용 입력 여부 확인
                if (modifiedTitle == null || modifiedTitle == "") {
                    alert("내용을 입력하세요.");
                } else if (modifiedStartDate == "" || modifiedEndDate == "") {
                    alert("날짜를 입력하세요.");
                } else if (new Date(modifiedEndDate) - new Date(modifiedStartDate) < 0) { // date 타입으로 변경 후 확인
                    alert("종료일이 시작일보다 먼저입니다.");
                } else { // 정상적인 입력 시

                    // 변경된 정보 업데이트(일정 수정시)
                    info.event.setProp('title', modifiedTitle); 
                    info.event.setStart(modifiedStartDate);
                    info.event.setEnd(modifiedEndDate);

                    console.log(info.event); // 변경된 이벤트 정보 확인

                    // 모달 창 닫기
                    $("#readModal").modal("hide");

                }

            });
        }



    });
    calendar.render();

});



