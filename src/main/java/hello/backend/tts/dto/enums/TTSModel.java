package hello.backend.tts.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TTSModel {
    NGOEUN("고은", "ngoeun", "여성"),
    NAPPLE("늘봄", "napple", "여성"),
    NMINJEONG("민정", "nminjeong", "여성"),
    NOYJ("봄달", "noyj", "여성"),
    NARA_CALL("아라(상담원)", "nara_call", "여성"),
    NEUNYOUNG("은영", "neunyoung", "여성"),
    NSUNHEE("선희", "nsunhee", "여성(중년)"),

    NSINU("신우", "nsinu", "남성"),
    NDONGHYUN("동현", "ndonghyun", "남성"),
    NMINSANG("민상", "nminsang", "남성"),
    NJIHUN("지훈", "njihun", "남성"),
    NJOOAHN("주안", "njooahn", "남성"),
    NWONTAK("원탁", "nwontak", "남성(중년)");

    private final String name;
    private final String code;
    private final String gender;
}
