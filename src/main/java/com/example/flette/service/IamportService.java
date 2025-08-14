package com.example.flette.service;

import com.example.flette.dto.IamportAccessToken;
import com.example.flette.dto.IamportResponse;
import com.example.flette.dto.PaymentInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class IamportService {

    @Value("${iamport.restApiKey}")
    private String restApiKey;

    @Value("${iamport.restApiSecret}")
    private String restApiSecret;

    // 아임포트 API에 접근 토큰을 요청하는 메서드
    public String getAccessToken() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("imp_key", restApiKey);
            body.add("imp_secret", restApiSecret);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
            
            // 아임포트 토큰 발급 API 호출
            ResponseEntity<IamportResponse> response = restTemplate.exchange(
                "https://api.iamport.kr/users/getToken",
                HttpMethod.POST,
                requestEntity,
                IamportResponse.class
            );

            // API 응답에서 토큰 정보 추출
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                IamportAccessToken token = mapper.convertValue(response.getBody().getResponse(), IamportAccessToken.class);
                return token.getAccess_token();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // 토큰 발급 실패 시 null 반환
    }

    /**
     * imp_uid로 아임포트 서버에서 결제 정보를 조회하는 메서드
     * @param accessToken 아임포트 접근 토큰
     * @param impUid 아임포트 결제 고유 번호
     * @return 결제 정보가 담긴 IamportResponse 객체
     */
    public IamportResponse<PaymentInfo> getPaymentInfo(String accessToken, String impUid) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken); // Bearer 토큰 설정

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 아임포트 결제 정보 조회 API 호출
            ResponseEntity<IamportResponse> response = restTemplate.exchange(
                "https://api.iamport.kr/payments/" + impUid,
                HttpMethod.GET,
                entity,
                IamportResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // PaymentInfo 타입으로 응답 본문을 변환하여 반환
                ObjectMapper mapper = new ObjectMapper();
                PaymentInfo paymentInfo = mapper.convertValue(response.getBody().getResponse(), PaymentInfo.class);
                IamportResponse<PaymentInfo> iamportResponse = new IamportResponse<>();
                iamportResponse.setCode(response.getBody().getCode());
                iamportResponse.setMessage(response.getBody().getMessage());
                iamportResponse.setResponse(paymentInfo);
                return iamportResponse;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
