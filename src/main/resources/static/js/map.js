let map; // 지도
let service; // 서비스
let infowindow; // 인포윈도우
let openInfowindow = null; // 열린 인포윈도우
let currentMarkerId = null; // 현재 선택된 마커의 ID
let markerIdCounter = 0; // 마커 ID 카운트
let markers = []; // 마커의 값들을 담은 배열
const placeItems = [];

// 지도 초기화
function initMap() {
    const seoul = new google.maps.LatLng(37.555, 126.9725);

    // 지도
    map = new google.maps.Map(document.getElementById("map"), {
        center: seoul,
        zoom: 15,
        gestureHandling: "greedy", // 더블 클릭, 스크롤 시 확대/축소 가능
    });

    // [이벤트] 지도 클릭
    map.addListener("click", (event) => {
        addMarker(event.latLng);
    });

    // 인포윈도우
    infowindow = new google.maps.InfoWindow();

    // 검색창 자동완성
    searchAutocomplete(map); // 함수 호출

    // DB에서 마커 불러오기
    databaseMarker(); // 함수 호출
}

// [함수] DB에서 마커 불러오기
function databaseMarker() {
    const parentId = document.getElementById("parentId").value;
    const url = `http://localhost:8080/places/data/${parentId}`;

    // DB에서 parentId에 해당하는 places 데이터 가져오기
    fetch(url)
        .then((res) => res.json())
        .then((data) => {
            console.log(data); // data 확인
            const places = data;
            const markerCenter = new google.maps.LatLng(data[0].lat, data[0].lng); // daily일정의 첫 장소의 lat,lng를 markerCenter에 저장
            map.setCenter(markerCenter) // 페이지 로딩시 map의 중앙을 첫 마커 중심으로 설정

            // places 배열에 있는 각 장소의 위도, 경도를 기반으로 지도에 마커를 추가 -> place에 할당
            places.forEach((place) => {
                const position = new google.maps.LatLng(place.lat, place.lng);
                addMarker(position); // 함수 호출

                // 장소 정보 가져오기
                const list = document.getElementById("place_list");

                const placeId = place.place_id;
                const lat = place.lat;
                const lng = place.lng;
                const placeRating = place.rating === undefined ? 0 : place.rating;
                const placeName = place.name === undefined ? "이름 없음" : place.name;
                const placePhoneNumber = place.formatted_phone_number === undefined ? "전화번호 없음" : place.formatted_phone_number;
                const address = place.formatted_address === undefined ? "주소정보 없음" : place.formatted_address
                let openingHoursHTML = "로딩 중..."; // 초기 값 설정
                let reviewsHTML = "로딩 중..."; // 초기 값 설정

                // Reverse Geocoding API 호출
                const geocoder = new google.maps.Geocoder();
                geocoder.geocode({location: position}, (results, status) => {
                    if (status === "OK") {
                        // 장소 정보 가져오기
                        const request = {
                            placeId: placeId,
                            fields: ["name", "rating", "formatted_phone_number", "geometry", 'formatted_address', 'opening_hours', 'reviews'],
                        };

                        // 장소 정보
                        const placesService = new google.maps.places.PlacesService(map);
                        placesService.getDetails(request, (temp, status) => {

                            let reviews = temp.reviews;
                            reviewsHTML = "";
                            if (Array.isArray(reviews)) {
                                reviews.forEach(review => {
                                    const reviewText = review.text;
                                    const reviewNameText = review.author_name;
                                    reviewsHTML += reviewText + "</br> 작성자: " + reviewNameText + "</br></br>";

                                });
                            } else {
                                reviewsHTML = "리뷰 없음";
                            }

                            let opening_hours = temp.opening_hours === undefined ? "영업시간 정보없음" : temp.opening_hours.weekday_text;
                            openingHoursHTML = "";
                            if (Array.isArray(opening_hours)) {
                                opening_hours.forEach(opening => {
                                    openingHoursHTML += opening + `</br>`;

                                });
                            } else {
                                openingHoursHTML = opening_hours;
                            }
                            const listItem = document.createElement("li");
                            listItem.innerHTML = `
                            <label for='name'>이름</label><input name='name' id='name' value='${placeName}' readonly />
                            <label for='address'>주소</label><input name='address' id='address' value='${address}' readonly /></br>
                            <label for='phone'>전화번호</label><input name='phoneNumber' id='phone' value='${placePhoneNumber}' readonly />
                            <label for='rating'>별점</label><input name='rating' id='rating' value='${placeRating}' readonly />
                            <label for='placeId'></label><input type='hidden' name='placeId' id='placeId' value='${placeId}' readonly />
                            <label for='lat'></label><input type='hidden' name='latitude' id='lat' value='${lat}' readonly />
                            <label for='longitude'></label><input type='hidden' name='longitude' id='longitude' value='${lng}' readonly />
                            <label for='openingHours'>영업시간</label></br>
                            <div class='valueText'>${openingHoursHTML}</div>
                            <label for='reviews'>리뷰</label></br>
                            <div class='valueText'>${reviewsHTML}</div>
                            <input type='button' value='삭제' onclick='deletePlace(this)' />
                            `;

                            list.appendChild(listItem);

                            placeItems.push({
                                "name": placeName,
                                "phoneNumber": placePhoneNumber,
                                "rating": placeRating,
                                "placeId": placeId,
                                "latitude": lat,
                                "longitude": lng,
                                "parentId": parentId,
                                "address": address
                            });
                        });
                    }
                });
            });
        })
        .catch((err) => {
            console.error("Error :", err);
        });
}

//장소 저장
function saveData() {
    const parentId = document.getElementById("parentId").value;
    console.log(placeItems)
    // AJAX 요청 보내기
    fetch('/places/' + parentId, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(placeItems)
    })
        .then(response => response.json())
        .then(data => {
            // 요청이 성공적으로 완료된 후 처리할 코드
        })
        .catch(error => {
            // 요청이 실패한 경우 처리할 코드
        });
}


// [함수] 마커 추가
function addMarker(position) {
    // 마커 생성
    const marker = new google.maps.Marker({
        position: position,
        map: map,
        id: markerIdCounter++, // 마커 ID 증가
    });

    // Reverse Geocoding API 호출
    const geocoder = new google.maps.Geocoder();
    geocoder.geocode({location: position}, (results, status) => {
        if (status === "OK") {
            // placeId 추출
            const placeId = results[0].place_id;
            marker.set("placeId", placeId);

            // 장소 정보 가져오기
            const request = {
                placeId: placeId,
                fields: ["name", "rating", "formatted_phone_number", "geometry", 'formatted_address', 'opening_hours', 'reviews'],
            };

            // 장소 정보
            const placesService = new google.maps.places.PlacesService(map);
            placesService.getDetails(request, (place, status) => {
                const lat = place.geometry.location.lat();
                const lng = place.geometry.location.lng();
                const placeRating = place.rating === undefined ? 0 : place.rating;
                const placeName = place.name === undefined ? "이름 없음" : place.name;
                const placePhoneNumber = place.formatted_phone_number === undefined ? "전화번호 없음" : place.formatted_phone_number;
                const address = place.formatted_address === undefined ? "주소정보 없음" : place.formatted_address

                const reviews = place.reviews
                let reviewsHTML = "";
                if (Array.isArray(reviews)) {
                    reviews.forEach(review => {
                        const reviewText = review.text;
                        const reviewNameText = review.author_name
                        reviewsHTML += reviewText + "</br> 작성자: " + reviewNameText + "</br></br>";

                    });
                } else {
                    reviewsHTML = "리뷰 없음";
                }

                const opening_hours = place.opening_hours === undefined ? "영업시간 정보없음" : place.opening_hours.weekday_text;
                let openingHoursHTML = "";
                if (Array.isArray(opening_hours)) {
                    opening_hours.forEach(opening => {
                        openingHoursHTML += opening + `</br>`;

                    });
                } else {
                    openingHoursHTML = opening_hours;

                }
                // 마커에 장소 정보 설정
                marker.place = place;

                // 인포윈도우 내용
                // addInfowindow() 함수 내 content에 들어가는 내용
                const content =
                    "<form id='infowindow'>" + `
                        <div id = 'infowindowWrap'>
                            <label for='name'>이름</label><input name='name' id='name' value='${placeName}' readonly /></br>
                            <label for='address'>주소</label><input name='address' id='address' value='${address}' type='hidden' readonly /></br>
                            <div class='valueText'>${address}</div>
                            <label for='phone'>전화번호</label><input name='phoneNumber' id='phone' value='${placePhoneNumber}' readonly /></br>
                            <label for='rating'>별점</label><input name='rating' id='rating' value='${placeRating}' readonly />
                            <label for='placeId'></label><input type='hidden' name='placeId' id='placeId' value='${placeId}' readonly />
                            <label for='lat'></label><input type='hidden' name='latitude' id='lat' value='${lat}' readonly />
                            <label for='longitude'></label><input type='hidden' name='longitude' id='longitude' value='${lng}' readonly />
                            <label for='opening_hours'>영업시간</label></br>
                            <div class='valueText'>${openingHoursHTML}</div>
                            <label for='reviews'>리뷰</label></br>
                            <div class='valueText'>${reviewsHTML}</div>
                        </div>` +
                    "<div id='infowindow_btn_group'>" +
                    "<input type='button' value='장소 추가' onclick='addPlace()' />" +
                    "<input type='button' value='마커 삭제' onclick='deleteMarker()' />" +
                    "<input type='button' value='창 닫기' onclick='closeInfowindow()' />" +
                    "</div>" + "</form>";

                // 인포윈도우 추가
                addInfowindow(marker, content); // 함수 호출
            });
        }
    });

    // makers에 marker 값 담기
    markers.push(marker);
}

// [함수] 인포윈도우 추가
// marker, content를 받아 해당 마커에 인포윈도우 추가
function addInfowindow(marker, content) {
    // 인포윈도우 생성
    const infowindow = new google.maps.InfoWindow({
        content: content,
    });

    // 마커에 인포윈도우 속성을 설정
    // 마커와 인포윈도우를 연결 -> 마커 삭제, 인포윈도우 조작 가능
    marker.infowindow = infowindow;

    // [이벤트] 마커 클릭 시 인포윈도우 열기, 닫기
    marker.addListener("click", () => {
        // 이미 열린 인포윈도우가 있을 경우 열린 인포윈도우 닫기
        if (openInfowindow && openInfowindow.getMap()) {
            openInfowindow.close();
        }

        // getMap() : 인포윈도우가 지도에 연결되었는지 유무 확인
        // 인포윈도우 표시 시 연결된 지도를 반환, 미표시 시 Null 반환
        if (infowindow.getMap()) {
            // 지도에 인포윈도우 표시 시 인포윈도우 닫기
            infowindow.close();
        } else {
            // 지도에 인포윈도우 미표시 시 인포윈도우 열기
            // map에서 인포윈도우를 열기, 특정 marker와 연결
            infowindow.open(map, marker);
            openInfowindow = infowindow;
        }

        // 현재 선택된 마커의 ID 설정
        // 설정 이유 : 마커 선택 시 ID 필요
        currentMarkerId = marker.id;
    });

    // [이벤트] 지도 클릭 시 인포윈도우 닫기
    map.addListener("click", () => {
        infowindow.close();

        // 현재 선택된 마커의 ID 초기화
        // 초기화 이유 : 미선택 시 초기화 필요 (마커 선택 시만 ID 필요)
        currentMarkerId = null;
    });
}

// [함수] 인포윈도우 닫기
function closeInfowindow() {
    // find() : 배열의 요소에 대해 조건 함수를 실행 후, true를 반환하는 첫 번째 요소를 반환 (조건 미부합 undefined 반환)
    // currentMarkerId와 일치하는 ID를 가진 마커를 markers에서 찾기
    const marker = markers.find((marker) => marker.id === currentMarkerId);

    // 마커 표시 중 인포윈도우 닫기, 마커 ID 초기화
    if (marker) {
        // 인포윈도우 생성, 마커와 인포윈도우를 연결
        const infowindow = marker.infowindow;

        // 인포윈도우 표시 중 인포윈도우 닫기
        if (infowindow) {
            infowindow.close();
        }

        // 현재 선택된 마커의 ID 초기화
        currentMarkerId = null;
    }
}

// [함수] 전체 장소 초기화
function initPlace(map) {
    const q = confirm("초기화를 하시겠습니까?");

    if (q) {
        // 확인 버튼 클릭 시 로직
        // 전체 markers 값 초기화
        if (markers && markers.length > 0) {
            for (let i = 0; i < markers.length; i++) {
                markers[i].setMap(null);
            }

            markers = [];
            alert("초기화를 성공했습니다.");
        } else {
            alert("초기화할 장소가 없습니다.");
        }
    } else {
        // 취소 버튼 클릭 시 로직
        alert("초기화를 실패했습니다.");
    }
}

// [함수] 전체 장소 삭제
function deleteAllPlace() {
    const list = document.getElementById("place_list");
    placeItems.length = 0;
    // 장소 정보 리스트 전체 삭제
    list.innerHTML = "";
}

// [함수] 장소 삭제
function deletePlace(button) {
    // button -> onclick="deletePlace(this)" 속성을 가진 input type="button" 요소
    const placeId = button.parentNode.querySelector('input[name="placeId"]').value;
    const listItem = button.closest("li");

    if (listItem && placeId) {
        // 삭제할 값의 고유 식별자(placeId)를 기반으로 해당 요소를 찾습니다.
        const index = placeItems.findIndex(item => item.placeId === placeId);

        if (index !== -1) {
            // 찾은 요소를 배열에서 제거합니다.
            placeItems.splice(index, 1);
        }

        // 근접한 "li"를 삭제합니다.
        listItem.remove();
    }
}

// [함수] 장소 추가
function addPlace() {
    // currentMarkerId와 일치하는 ID를 가진 마커를 markers에서 찾기
    const marker = markers.find((marker) => marker.id === currentMarkerId);

    // 장소 정보 가져오기
    const place = marker.place;
    const placeId = marker.get("placeId");
    const lat = marker.getPosition().lat();
    const lng = marker.getPosition().lng();
    const placeRating = place.rating === undefined ? 0 : place.rating;
    const placeName = place.name === undefined ? "이름 없음" : place.name;
    const placePhoneNumber = place.formatted_phone_number === undefined ? "전화번호 없음" : place.formatted_phone_number;
    const address = place.formatted_address === undefined ? "주소정보 없음" : place.formatted_address
    const reviews = place.reviews
    let reviewsHTML = "";
    if (Array.isArray(reviews)) {
        reviews.forEach(review => {
            const reviewText = review.text;
            const reviewNameText = review.author_name
            reviewsHTML += reviewText + "</br> 작성자: " + reviewNameText + "</br></br>";

        });
    } else {
        reviewsHTML = "리뷰 없음";
    }

    const opening_hours = place.opening_hours === undefined ? "영업시간 정보없음" : place.opening_hours.weekday_text;
    let openingHoursHTML = "";
    if (Array.isArray(opening_hours)) {
        opening_hours.forEach(opening => {
            openingHoursHTML += opening + `</br>`;

        });
    } else {
        openingHoursHTML = opening_hours;
    }
    // 장소 정보 리스트 생성하기
    const list = document.getElementById("place_list");
    const listItem = document.createElement("li");

    // 장소 정보 리스트 내용
    listItem.innerHTML = `
        <label for='name'>이름</label><input name='name' id='name' value='${placeName}' readonly />
        <label for='address'>주소</label><input name='address' id='address' value='${address}' readonly /></br>
        <label for='phone'>전화번호</label><input name='phoneNumber' id='phone' value='${placePhoneNumber}' readonly />
        <label for='rating'>별점</label><input name='rating' id='rating' value='${placeRating}' readonly />
        <label for='placeId'></label><input type='hidden' name='placeId' id='placeId' value='${placeId}' readonly />
        <label for='lat'></label><input type='hidden' name='latitude' id='lat' value='${lat}' readonly />
        <label for='longitude'></label><input type='hidden' name='longitude' id='longitude' value='${lng}' readonly />
        <label for='openingHours'>영업시간</label></br>
        <div class='valueText'>${openingHoursHTML}</div>
        <label for='reviews'>리뷰</label></br>
        <div class='valueText'>${reviewsHTML}</div>
        <input type='button' value='삭제' onclick='deletePlace(this)' />

    `;

    // 장소 정보 리스트 추가
    list.appendChild(listItem);
    alert("장소를 추가했습니다.");

    const parentId = document.getElementById("parentId").value;
    // placeItems list에 넣기
    placeItems.push({
        "name": placeName,
        "phoneNumber": placePhoneNumber,
        "rating": placeRating,
        "placeId": placeId,
        "latitude": lat,
        "longitude": lng,
        "parentId": parentId,
        "address": address
    });

    // 인포윈도우 닫기
    closeInfowindow(); // 함수 호출
}

// [함수] 마커 삭제
function deleteMarker() {
    // currentMarkerId와 일치하는 ID를 가진 마커를 markers에서 찾기
    const marker = markers.find((marker) => marker.id === currentMarkerId);

    // 마커 표시 중 인포윈도우 닫기, 마커 ID 초기화
    if (marker) {
        // marker, markers 값 초기화
        marker.setMap(null);
        markers[marker] = null;

        // 인포윈도우 생성, 마커와 인포윈도우를 연결
        const infowindow = marker.infowindow;

        // 인포윈도우 표시 중 인포윈도우 닫기
        if (infowindow) {
            infowindow.close();
        }

        // filter() : 배열의 요소에 대해 조건 함수를 실행 후, true를 반환하는 요소들로 새로운 배열을 구성
        // currentMarkerId와 일치하지 않는 마커들로 이루어진 새로운 배열 생성 (마커를 삭제한 효과와 동일)
        markers = markers.filter((m) => m.id !== currentMarkerId);
        alert("마커를 삭제했습니다.");

        // 현재 선택된 마커의 ID 초기화
        currentMarkerId = null;
    }
}

// [함수] 장소 검색
function searchPlace() {
    const query = document.getElementById("map_search_input").value;

    const request = {
        query: query,
        fields: ["ALL"],
    };

    service = new google.maps.places.PlacesService(map);
    service.findPlaceFromQuery(request, (results, status) => {
        if (status === google.maps.places.PlacesServiceStatus.OK && results) {
            const result = results[0];
            const position = result.geometry.location;
            addSearchMarker(position, result); // 함수 호출
            map.setCenter(result.geometry.location);
        }
    });
}

// [함수] 마커 추가 (검색)
function addSearchMarker(position, place) {
    addMarker(position); // 함수 호출
}

// [함수] 검색창 자동완성
// 검색창 자동완성
function searchAutocomplete(map) {
    // 검색창
    const input = document.getElementById("map_search_input");

    const options = {
        fields: ["name", "geometry"],
        strictBounds: false, // 넓은 범위 검색 (true : 설정된 지도 범위 내)
        // types: ["establishment"], // 검색할 장소의 유형을 제한 (establishment : 사업체나 기관과 같은 공공장소)
    };

    // 자동완성
    autocomplete = new google.maps.places.Autocomplete(input, options);

    // 지도의 경계에 바인딩
    // 검색 결과가 지도의 가시 영역으로 제한 -> 사용자가 특정 지도 영역 내의 장소를 검색할 때 유용
    autocomplete.bindTo("bounds", map);

    autocomplete.addListener("place_changed", () => {
        // 선택한 장소의 세부 정보 반환
        const place = autocomplete.getPlace();

        // 지도에서 보여줄 영역
        if (place.geometry.viewport) {
            // 선택한 장소의 위치 좌표를 지도의 확대/축소 수준과 지도의 중심으로 자동 설정
            map.fitBounds(place.geometry.viewport);
        } else {
            // 선택한 장소의 위치 좌표를 지도의 중심으로 설정
            map.setCenter(place.geometry.location);
            map.setZoom(15);
        }

        addMarker(place.geometry.location); // 함수 호출
    });
}

window.initMap = initMap;

document.addEventListener("DOMContentLoaded", function() {
  // li 요소를 클릭했을 때의 이벤트 핸들러 함수
  document.getElementById("place_list").addEventListener("click", function(event) {
    // 클릭된 요소가 li인지 확인
    if (event.target.tagName === "LI" || event.target.closest("li")) {
      // 클릭된 li 요소 또는 li 요소의 자식 요소 내부의 id가 lat인 요소 찾기
      var liElement = event.target.closest("li");
      var latElement = liElement.querySelector("#lat");
      var lngElement = liElement.querySelector("#longitude");
      // 값이 있는지 확인하고 가져오기
      var latValue = latElement ? latElement.value : null;
      var lngValue = lngElement ? lngElement.value : null;
      // 원하는 동작 수행
      // console.log('lat:', latValue);
      // console.log('lng:', lngValue);
      const markerCenter = new google.maps.LatLng(latValue, lngValue); // daily일정의 첫 장소의 lat,lng를 markerCenter에 저장
      map.setCenter(markerCenter) // 페이지 로딩시 map의 중앙을 첫 마커 중심으로 설정
    }
  });
});


