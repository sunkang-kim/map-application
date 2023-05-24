$(document).ready(function() {
    $("#calender_btn_open").click(function() {
        if ($("#calender_container").hasClass("toggle")) {
            $("#calender_btn_open").text("열기");
            $("#calender_container").removeClass("toggle");
        } else {
            $("#calender_btn_open").text("닫기");
            $("#calender_container").addClass("toggle");
        }
    });

//    // li 요소를 클릭했을 때의 이벤트 핸들러 함수
//    $("#place_list").on("click", "li", function() {
//        // 클릭된 li 요소 내부의 id가 lat인 요소의 value 값 가져오기
//        var latValue = $(this).find("#lat").val();
//        var lngValue = $(this).find("#lng").val();
//        // 원하는 동작 수행
//        console.log('lat:', latValue);
//    });
});
