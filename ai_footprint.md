# 개요
다음은 '궤도의 숲(Orbital Forest)'(이하 제품) 개발에 AI가 어떻게 사용되었는지 검토할 수 있는 문서입니다. 제품 개발에 있어 기획, 디자인, 개발로 나누어 어느 부분에 어떻게 참여했는지 기술합니다.
# 기획
제품 기획에는 AI를 사용하지 않았습니다. [기초 아이디어 메모](Sources/memo.jpg)를 기반으로, AI에게 개발을 지시하는 문서인 [`plan.md`](plan.md)를 작성했습니다. 단, 오류 수정 및 기획의 정상적인 반영을 위해 추가적인 프롬프트를 입력했을 수 있고, 이 사례는 [`SESSION_CONTEXT.md`](SESSION_CONTEXT.md)를 참고해주십시오.
# 디자인
제품 디자인에서는 AI를 사용하지 않았습니다. [다음 Figma 파일](https://www.figma.com/design/XVa59k8Gp7XJ35DsmMK69i/Orbital-Forest?node-id=0-1&t=zwQrh9RXNZXRCABD-1)을 검토하여 판단할 수 있습니다. 단, 개발을 위해 [Figma MCP](https://developers.figma.com/docs/figma-mcp-server/)를 통해 AI 모델에게 내용을 전송했습니다.
# 개발
제품 개발에는 AI가 사용되었습니다. AI가 개발을 진행하고 사용자가 QA를 진행하여 오류를 파악하고 수정하는 과정으로 진행했습니다. 이 과정은 [`SESSION_CONTEXT.md`](SESSION_CONTEXT.md) 문서를 참고할 수 있습니다.

다음과 같은 부분에서 AI를 활용했습니다.
- 제품 핵심 로직 구현
- 제품 프론트엔드 구현
- 제품 디자인 구현
- 제품 번역
- 제품 오류 수정

다음과 같은 AI 모델을 사용했습니다.
- Claude Opus 4.6 (Claude Code를 통해)
- Gemini 3 Flash Preview (Android Studio를 통해)

---

# Overview
This document allows you to review how AI was used in the development of 'Orbital Forest' (hereinafter referred to as the Product). It describes which parts of the product development — planning, design, and development — AI was involved in, and how.
# Planning
AI was not used in planning the Product. Based on [initial idea notes](Sources/memo.jpg), [`plan.md`](plan.md) — a document that instructs AI on development — was written.  However, additional prompts may have been entered for error correction and to ensure planning was properly reflected; refer to [`SESSION_CONTEXT.md`](SESSION_CONTEXT.md) for those cases.
# Design
AI was not used in designing the Product. This can be verified by reviewing the [following Figma file](https://www.figma.com/design/XVa59k8Gp7XJ35DsmMK69i/Orbital-Forest?node-id=0-1&t=zwQrh9RXNZXRCABD-1). However, for development purposes, content was sent to the AI model via [Figma MCP](https://developers.figma.com/docs/figma-mcp-server/).
# Development
AI was used in developing the Product. The process involved AI carrying out the development, with the user performing QA to identify and correct errors. This process can be referenced in the [`SESSION_CONTEXT.md`](SESSION_CONTEXT.md) document.

AI was utilized in the following areas:
- Implementation of core product logic
- Implementation of product frontend
- Implementation of product design
- Product translation
- Product bug fixing

The following AI models were used:
- Claude Opus 4.6 (via Claude Code)
- Gemini 3 Flash Preview (via Android Studio)
