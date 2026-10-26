package dev.tumbling.springailab.ai;

/** 키가 아직 설정되지 않은 상태에서 모델을 쓰려고 할 때 던진다. 컨트롤러는 이를 409로 바꿔 화면이 입력창을 다시 띄우게 한다. */
public class ApiKeyMissingException extends RuntimeException {

	public ApiKeyMissingException() {
		super("API 키가 설정되지 않았습니다. 첫 실행 화면에서 키를 입력해 주세요.");
	}
}
