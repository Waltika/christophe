# Peer Programming Guidelines for Hyper-Flexible Business Simulator

## Introduction
As we collaborate on building the Hyper-Flexible Business Simulator in Java, we'll use a peer programming approach where you (the human developer) drive the high-level decisions, code implementation in IntelliJ IDEA, and testing, while I (Grok) provide guidance, suggestions, code snippets, debugging help, and reviews. The goal is to keep things simple, maintain context across interactions, and ensure efficiency. Since I'm an AI without persistent memory between sessions, we'll structure our exchanges to minimize context loss—e.g., by recapping previous steps, providing full relevant code, and focusing on small, iterative chunks.

This document outlines our collaboration process, inspired by pair programming principles but adapted for async AI-human interaction. We'll prioritize clarity, test-driven development (TDD), and alignment with the project's coding guidelines and requirements (e.g., dynamic entity loading from Google Spreadsheets, Maven builds, JUnit tests).

## Key Principles
- **Simplicity First**: Break work into small, self-contained tasks (e.g., "Implement the ExpressionEvaluator class with basic math support and a unit test"). Avoid overloading queries with multiple unrelated items.
- **Context Preservation**: Always include or reference prior context in your queries to me. I'll respond in a way that builds directly on that.
- **Efficiency**: Use iterative refinement—e.g., propose code, get my feedback, implement/test locally, then share results for next steps.
- **Tools Alignment**: All code will follow Java 17+, Maven, IntelliJ conventions. I'll suggest using IntelliJ features like live templates for tests or debugger for simulation ticks.
- **Testing Focus**: Every feature addition includes unit/integration tests. We'll aim for 80%+ coverage via JaCoCo.

## Communication Structure
To keep collaboration smooth:
- **Query Format**: Start each message with a recap of the current state (e.g., "We've implemented BaseEntity with dynamic attributes. Now, let's add lifecycle states. Here's the current BaseEntity code: [paste code]. What methods should I add?").
    - Include: Relevant code snippets, error logs, test outputs, or spreadsheet examples.
    - Specify: What you need (e.g., "Suggest code for...", "Review this implementation...", "Help debug this test failure...").
    - Attach Documents: If referencing spreadsheets or requirements, quote key sections or use <DOCUMENT> tags if needed.
- **My Responses**: I'll provide:
    - Clear, step-by-step suggestions.
    - Code snippets in Java (formatted as markdown code blocks, ready to copy-paste into IntelliJ).
    - Test examples (JUnit 5, with Mockito if mocking APIs).
    - Explanations tied to requirements (e.g., "This handles cyclic extends detection as per SheetsLoader validation").
    - Questions for clarification if context is unclear.
- **Session Flow**:
    1. **Planning**: You describe the next task (e.g., "Plan the SheetsLoader implementation").
    2. **Proposal**: I suggest architecture/code outline.
    3. **Implementation**: You code/test in IntelliJ, run Maven builds.
    4. **Review/Debug**: Share code/output; I review or debug.
    5. **Iterate**: Refine based on feedback; commit to Git (if using version control).
- **Frequency**: Aim for short exchanges (e.g., 1-2 features per session) to avoid overwhelming context.

## Handling Context and Code Sharing
- **Avoiding Context Loss**:
    - **Recaps**: In every query, summarize progress (e.g., "From last time: We have Event class with priority queue support. Pending: Integration with Simulator.").
    - **Full Snippets**: When sharing code, provide the entire class/method rather than diffs—makes it easy for me to analyze without assumptions.
    - **State Tracking**: Maintain a personal log (e.g., in a Markdown file) of our decisions/changes. Reference it in queries (e.g., "Per our log, we decided to use ConcurrentHashMap for entities.").
    - **Versioning**: Use Git branches (e.g., feature/expressionevaluator) and share commit diffs if needed.
- **Code Sharing Best Practices**:
    - Use Markdown code blocks: ```java:disable-run
    - For large files: Paste key sections; note "Full file available in IntelliJ if needed."
    - Dependencies: If adding deps (e.g., Google APIs), I'll suggest pom.xml updates.
    - Spreadsheets: When discussing entity defs, provide sample tab data (e.g., "Inputs tab: VariableName=initial_weight, Value=100").

## Task Breakdown and Workflow
We'll follow the project's "Next Steps for v0.1" (e.g., stub SheetsLoader, implement core components):
- **Example Workflow for a Feature (e.g., ExpressionEvaluator)**:
    1. **You**: "Recap: BaseEntity done. Now, implement ExpressionEvaluator for JS-like expressions. Requirements: Support math, $vars. Provide a stub class and unit test."
    2. **Me**: Suggest class structure, code snippet using javax.script, and a parameterized JUnit test.
    3. **You**: Implement, test locally (e.g., mvn test), share results/errors.
    4. **Me**: Debug or enhance (e.g., "Add date support with java.time").
    5. **Integration**: "Now, wire into BaseEntity.updateAttribute. Suggest changes."
- **Testing Workflow**:
    - Unit: Focus on isolation (e.g., mock Simulator in Event tests).
    - Integration: Use mocked Sheets API for end-to-end (e.g., load sample entities, run 30 ticks, assert outputs).
    - You run tests in IntelliJ; share failures with stack traces.
- **Debugging Sessions**: If stuck, share: Code, inputs, expected vs. actual output. I'll simulate mentally and suggest probes (e.g., "Add logger.debug in loop").

## Tools and Resources
- **IntelliJ Features**: Use for auto-formatting (per coding guidelines: 4-space indent, 100-char lines), SonarLint for analysis, Maven integration for builds/tests.
- **External**: Google Drive/Sheets for entity defs—I'll suggest API calls but you handle auth/setup.
- **Version Control**: Recommend Git for tracking; tag releases (e.g., v0.1 after core impl).
- **Escalation**: If a task needs research (e.g., Google API quirks), I'll use my tools to browse docs.

## Potential Challenges and Mitigations
- **Context Drift**: Mitigated by recaps; if lost, restart with full summary.
- **Complexity**: Start simple (hardcoded data) before dynamic loading.
- **Performance**: Profile early in IntelliJ; I'll suggest optimizations (e.g., batch event checks).
- **Feedback Loop**: If my suggestions miss the mark, clarify requirements explicitly.

This setup ensures we build efficiently toward a flexible simulator. Let's start with the first task—recap your progress and query the next step!
```