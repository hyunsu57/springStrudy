"use client";

import { useCallback, useEffect, useState } from "react";

/**
 * 홈 화면.
 *
 * 세 구역으로 나뉜다.
 * 1) 백엔드 연결 확인 — 기존 기능. Spring 서버가 떠 있는지 /api/health 로 본다.
 * 2) API 키 설정 — 첫 실행 흐름. /api/setup/status 가 configured=false 이면 입력창을 보여 주고,
 *    /api/setup/api-key 로 저장이 성공하면 숨긴다. 저장된 뒤에는 "키 변경" 버튼으로만 다시 연다.
 * 3) 요약 실습 — 모델을 실제로 호출하는 최소 소비자. 키가 없거나(409) 거부되면(401) 응답의 needsApiKey 를 보고
 *    입력창을 다시 연다. 잘못된 키로 저장했을 때 화면이 영영 잠기지 않게 하기 위한 장치다.
 *
 * 모든 요청은 Next.js 의 /api/* → Spring rewrite 를 거친다. 키 값은 서버로 보내기만 하고 어디에도 다시 표시하지 않는다.
 */

/** 서버 /api/setup/status 응답. 키 값은 절대 포함되지 않는다. */
type SetupStatus = {
  configured: boolean;
  source: "FILE" | "ENV" | "NONE";
  file: string;
};

/** fetch 응답을 unknown 으로 받은 뒤 필요한 필드만 검사해서 꺼낸다. TypeScript 타입은 외부 JSON 을 검증해 주지 않는다. */
async function readJson(response: Response): Promise<unknown> {
  try {
    return await response.json();
  } catch {
    return null;
  }
}

function isSetupStatus(body: unknown): body is SetupStatus {
  return (
    typeof body === "object" &&
    body !== null &&
    "configured" in body &&
    typeof body.configured === "boolean" &&
    "source" in body &&
    typeof body.source === "string"
  );
}

function errorMessage(body: unknown, fallback: string): string {
  if (typeof body === "object" && body !== null && "error" in body && typeof body.error === "string") {
    return body.error;
  }
  return fallback;
}

/** 서버 오류 응답의 needsApiKey 가 true 면 입력창을 다시 연다. */
function needsApiKey(body: unknown): boolean {
  return typeof body === "object" && body !== null && "needsApiKey" in body && body.needsApiKey === true;
}

/** 서버에 설정 여부를 묻는다. 실패(서버 꺼짐 등)는 null 로 돌려주고, 화면 상태 변경은 호출한 쪽이 한다. */
async function fetchSetupStatus(): Promise<SetupStatus | null> {
  try {
    const response = await fetch("/api/setup/status", { cache: "no-store", signal: AbortSignal.timeout(10000) });
    const body = await readJson(response);
    return response.ok && isSetupStatus(body) ? body : null;
  } catch {
    return null;
  }
}

export default function Home() {
  // ---- 1) 연결 확인 ----
  const [status, setStatus] = useState("아직 연결을 확인하지 않았습니다.");
  const [loading, setLoading] = useState(false);

  // ---- 2) API 키 설정 ----
  // setup: 서버가 알려 준 현재 상태. null 이면 아직 조회 전이거나 서버에 닿지 못한 것이다.
  const [setup, setSetup] = useState<SetupStatus | null>(null);
  // showKeyForm: 입력창 표시 여부. configured=false 이거나 "키 변경"을 눌렀거나 서버가 needsApiKey 를 보냈을 때 true.
  const [showKeyForm, setShowKeyForm] = useState(false);
  const [apiKey, setApiKey] = useState("");
  const [keyMessage, setKeyMessage] = useState("");
  const [savingKey, setSavingKey] = useState(false);
  // keyRequired: 서버가 "키가 없거나 거부됐다"(needsApiKey)고 알린 상태. true 인 동안은 취소 버튼을 숨겨 입력창을 닫을 수 없다.
  const [keyRequired, setKeyRequired] = useState(false);

  // ---- 3) 요약 실습 ----
  const [text, setText] = useState("");
  const [summary, setSummary] = useState("");
  const [summaryMessage, setSummaryMessage] = useState("");
  const [summarizing, setSummarizing] = useState(false);

  /** 조회 결과를 화면 상태에 반영한다. configured=false 면 입력창을 연다. */
  const applySetupStatus = useCallback((result: SetupStatus | null) => {
    if (result) {
      setSetup(result);
      // 미설정이면 입력창을 연다. 설정돼 있어도 강제로 닫지는 않는다(키 거부로 열린 입력창이나 "키 변경"으로 연 입력창을 유지).
      if (!result.configured) {
        setShowKeyForm(true);
        setKeyMessage("");
      }
      return;
    }
    // Spring 이 아직 안 떠 있으면 상태를 모른다. 입력창은 닫아 두고 연결 확인부터 하게 안내한다.
    setSetup(null);
    setShowKeyForm(false);
    setKeyMessage("Spring 서버에서 설정 상태를 가져오지 못했습니다. 먼저 연결을 확인해 주세요.");
  }, []);

  // 화면이 처음 뜰 때 한 번 상태를 조회한다. 이것이 "값이 없으면 입력창이 뜬다"의 출발점이다.
  // 응답이 온 뒤(then) 에만 상태를 바꾸고, 화면이 먼저 사라졌으면(cancelled) 반영하지 않는다.
  useEffect(() => {
    let cancelled = false;
    fetchSetupStatus().then((result) => {
      if (!cancelled) applySetupStatus(result);
    });
    return () => {
      cancelled = true;
    };
  }, [applySetupStatus]);

  async function checkConnection() {
    setLoading(true);
    setStatus("Spring 서버에 연결하는 중입니다.");
    try {
      const response = await fetch("/api/health", { cache: "no-store", signal: AbortSignal.timeout(10000) });
      if (!response.ok) throw new Error("응답 오류");
      const body: unknown = await response.json();
      if (typeof body !== "object" || body === null || !("status" in body) || body.status !== "UP") {
        throw new Error("잘못된 응답");
      }
      setStatus("연결 성공 · Spring 서버가 정상 응답했습니다.");
      // 연결이 되면 설정 상태도 다시 읽는다(서버를 늦게 켠 경우를 위해).
      applySetupStatus(await fetchSetupStatus());
    } catch {
      setStatus("연결하지 못했습니다. Spring 서버 실행 상태와 BACKEND_URL을 확인해 주세요.");
    } finally {
      setLoading(false);
    }
  }

  /** 키 저장. 성공하면 서버가 돌려준 상태로 갱신하고 입력창을 닫는다. 실패하면 메시지를 보여 주고 입력창은 그대로 둔다. */
  async function saveApiKey(event: React.FormEvent) {
    event.preventDefault();
    setSavingKey(true);
    setKeyMessage("");
    try {
      const response = await fetch("/api/setup/api-key", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ apiKey }),
        signal: AbortSignal.timeout(10000),
      });
      const body = await readJson(response);
      if (!response.ok) {
        setKeyMessage(errorMessage(body, "키를 저장하지 못했습니다."));
        return;
      }
      if (!isSetupStatus(body)) throw new Error("잘못된 응답");
      setSetup(body);
      setShowKeyForm(false);
      setKeyRequired(false);
      setApiKey(""); // 입력값은 화면 상태에 남기지 않는다.
      setKeyMessage("키를 저장했습니다. 재시작 없이 바로 사용할 수 있습니다.");
    } catch {
      setKeyMessage("키를 저장하지 못했습니다. Spring 서버 실행 상태를 확인해 주세요.");
    } finally {
      setSavingKey(false);
    }
  }

  /** 요약 호출. 키 문제(409/401)면 needsApiKey 를 보고 입력창을 다시 연다. */
  async function summarize() {
    setSummarizing(true);
    setSummary("");
    setSummaryMessage("모델을 호출하는 중입니다.");
    try {
      const response = await fetch("/api/ai/summarize", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text }),
        signal: AbortSignal.timeout(120000),
      });
      const body = await readJson(response);
      if (!response.ok) {
        setSummaryMessage(errorMessage(body, "요약에 실패했습니다."));
        if (needsApiKey(body)) {
          // 키가 없거나 거부됐다. 입력창을 강제로 열고, 새 키를 저장하기 전까지는 닫을 수 없게 한다.
          setKeyRequired(true);
          setShowKeyForm(true);
          setKeyMessage(errorMessage(body, "API 키를 다시 입력해 주세요."));
        }
        return;
      }
      if (typeof body !== "object" || body === null || !("summary" in body) || typeof body.summary !== "string") {
        throw new Error("잘못된 응답");
      }
      setSummary(body.summary);
      setSummaryMessage("model" in body && typeof body.model === "string" ? `모델: ${body.model}` : "");
    } catch {
      setSummaryMessage("요약 요청을 보내지 못했습니다. Spring 서버 실행 상태를 확인해 주세요.");
    } finally {
      setSummarizing(false);
    }
  }

  const sourceLabel =
    setup?.source === "FILE" ? "파일에 저장된 키 사용 중" : setup?.source === "ENV" ? "환경 변수의 키 사용 중" : "키 없음";

  return (
    <main>
      <header><span className="brand">TUMBLING / LAB</span><span className="badge">02 · 첫 AI 호출</span></header>
      <section className="intro">
        <p className="eyebrow">SPRING AI × NEXT.JS</p>
        <h1>작은 실험에서,<br />실제로 쓰는 AI까지.</h1>
        <p className="description">화면은 Next.js로, AI와 업무 로직은 Spring으로.<br />API 키를 저장하고 첫 번째 요약 API를 호출해 봅니다.</p>
      </section>

      <section className="connection" aria-labelledby="connection-title">
        <div><p className="eyebrow">CONNECTION</p><h2 id="connection-title">백엔드 연결 확인</h2><p className="hint">실습을 시작하기 전에 Spring 서버의 응답을 확인하세요.</p></div>
        <button onClick={checkConnection} disabled={loading}>{loading ? "확인 중…" : "연결 확인하기 ↗"}</button>
        <p role="status" aria-live="polite" className="status">{status}</p>
      </section>

      <section className="panel" aria-labelledby="setup-title">
        <div className="panel-head">
          <div><p className="eyebrow">API KEY</p><h2 id="setup-title">모델 API 키</h2>
            <p className="hint">{keyRequired ? "저장된 키로는 호출할 수 없습니다. 새 키를 입력해 주세요." : setup ? sourceLabel : "설정 상태를 아직 모릅니다. Spring 서버를 켠 뒤 연결 확인하기를 누르면 다시 조회합니다."}{!keyRequired && setup?.configured && setup.source === "FILE" ? ` · ${setup.file}` : ""}</p>
          </div>
          {setup?.configured && !showKeyForm && (
            <button type="button" className="secondary" onClick={() => setShowKeyForm(true)}>키 변경</button>
          )}
        </div>
        {showKeyForm && (
          <form onSubmit={saveApiKey} className="key-form">
            <label htmlFor="api-key">Anthropic API 키</label>
            <input
              id="api-key"
              type="password"
              autoComplete="off"
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              placeholder="발급받은 키를 붙여 넣으세요"
              required
            />
            <div className="actions">
              <button type="submit" disabled={savingKey || apiKey.trim().length === 0}>{savingKey ? "저장 중…" : "저장하기"}</button>
              {setup?.configured && !keyRequired && (
                <button type="button" className="secondary" onClick={() => { setShowKeyForm(false); setApiKey(""); }}>취소</button>
              )}
            </div>
            <p className="hint">키는 Spring 서버의 config/secrets.properties 에만 저장되며 화면에 다시 표시되지 않습니다.</p>
          </form>
        )}
        <p role="status" aria-live="polite" className="status">{keyMessage}</p>
      </section>

      <section className="panel" aria-labelledby="summary-title">
        <div className="panel-head">
          <div><p className="eyebrow">SUMMARIZE</p><h2 id="summary-title">요약 실습</h2><p className="hint">글을 넣고 요약을 요청합니다. 키가 없거나 거부되면 위 입력창이 다시 열립니다.</p></div>
        </div>
        <textarea
          aria-label="요약할 글"
          value={text}
          onChange={(e) => setText(e.target.value)}
          rows={6}
          maxLength={4000}
          placeholder="요약할 글을 입력하세요 (4000자 이하)"
        />
        <div className="actions">
          <button type="button" onClick={summarize} disabled={summarizing || text.trim().length === 0}>{summarizing ? "요약 중…" : "요약하기"}</button>
        </div>
        {summary && <blockquote className="summary">{summary}</blockquote>}
        <p role="status" aria-live="polite" className="status">{summaryMessage}</p>
      </section>

      <section className="steps" aria-label="실습 순서">
        <article><span>01</span><h3>화면과 서버</h3><p>Next.js와 Spring의 요청 흐름 이해하기</p></article>
        <article><span>02</span><h3>첫 AI 호출</h3><p>키를 저장하고 ChatClient로 문장 요약 API 호출하기</p></article>
        <article><span>03</span><h3>결과를 화면으로</h3><p>입력·응답·실패 상태 연결하기</p></article>
      </section>
      <footer>현재 단계 · 첫 AI 호출 / 키는 서버에만 저장</footer>
    </main>
  );
}
