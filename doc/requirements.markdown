# Hyper-Flexible Business Simulator Requirements (v0.1 Spec)

## Overview
- **Goal**: Build a Java-based simulator that models complex businesses using entity definitions from Google Spreadsheets. Entities (e.g., Cow, Herd, Slaughterhouse, Farmer, PayRoll) have lifecycles, event schedules, containment relationships, and dynamic behaviors driven by user-defined variables. Support for financial outputs (e.g., salaries, P&L) to enable realistic business plans.
- **Key Principles**:
    - Hyper-flexible: No hardcoding entity types/behaviors in Java; all loaded dynamically from spreadsheets.
    - Decoupled: Use passive notifications and reactions for inter-entity interactions (e.g., Cow maturity notifies Slaughterhouse for queuing).
    - Time-based: Discrete event simulation with priority queue for scheduled actions.
    - Test-Driven: Include JUnit unit tests (e.g., for expression eval, queuing) and integration tests (e.g., full sim cycles with mocked Sheets API).
- **Tech Stack**:
    - Java 17+ (records, sealed classes for entities).
    - Build: Maven (for deps like Google APIs, Jackson, JUnit5, Mockito).
    - IDE: IntelliJ IDEA (use for debugging sim ticks, live templates for tests).
    - Deps: Google Sheets/Drive API (for loading), javax.script (for expr eval), Jackson (JSON parsing).
    - No concrete entity classes; use single `BaseEntity` with dynamic attrs.

## Spreadsheet Structure and Discovery
- **Discovery Mechanism**: No central config sheet. Use Google Drive API to list spreadsheets in a user-provided folder ID. Load all files except those starting with "_" (for inactivation, e.g., _OldHerd). Assume filename = entity class (e.g., Cow.xlsx → class "Cow").
    - Validation: Skip invalid files (missing required tabs); log warnings.
    - Globals: Special file like Globals.xlsx (no "_" prefix) for shared vars (e.g., market_price, sim_start_date).
- **Per-Entity Spreadsheet Tabs** (Required: Inputs, DerivedAttributes, States, Actions, CaptureDeletion):
    - **Inputs**: User vars (columns: VariableName, Value, Type, Description). Include flags like "is_output: true" for financial entities (e.g., PayRoll).
    - **DerivedAttributes**: Dynamic exprs (columns: AttributeName, Expression, DependsOn, Trigger). E.g., for Cow: weight = $initial_weight + $growth_rate * $months.
    - **States**: Lifecycles (columns: StateName, Initial, NextStates, ConditionToNext, NotificationEvent). E.g., Mature state notifies "Cow.Maturity".
    - **Actions**: Creations/destructions/reactions (columns: ActionName, Trigger, ActorEntityType, Effect, NewEntityType, MapAttributes, RemoveSource). E.g., Slaughterhouse queues on "Cow.Maturity", processes with destruction.
    - **CaptureDeletion**: Containment/queuing/rules (columns: RuleName, Type [capture/queue/delete], TargetEntityType, Condition, Container/Queue, Outputs [JSON array for derived on delete]).
- **Expression Language**: String-based (JS-like via ScriptEngine). Prefix vars with "$" (attrs/globals). Support math, logic, dates. Eval during events/transitions.

## Core Components
- **Entities (`BaseEntity` implements Entity interface)**:
    - Dynamic attrs (Map<String, Object>), including lists for queues/contained.
    - Methods: updateAttribute, addContained/removeContained (enforced via "isContainer" flag), getQueue.
    - Loaded per spreadsheet; multiple instances possible (e.g., via Instances tab).
- **Lifecycles**: List of states; auto-transitions via conditions; trigger notifications on entry.
- **Events**: PriorityQueue in Simulator; defs from Actions (e.g., recurring via cycle_length). Types: Notifications (passive signals), Reactions (e.g., queuing), Processing (destruction/creation).
    - Smart Generation: Check conditions/cycles per tick (e.g., for Farmer salary: if (currentTime - startTick) % cycle_length == 0, notify "Farmer.SalaryDue").
    - Evaluation: Use ExpressionEvaluator for conditions/effects (e.g., "queue.add($notified_entity_id) if queue.size < $max_capacity").
- **Containment/Queuing**: Dynamic lists; queue without removal (e.g., cow stays in Herd while queued). Destruction removes from map/containers, creates outputs.
- **Financial Outputs**: Marked entities (e.g., PayRoll) react to notifications (e.g., "Farmer.SalaryDue" → add entry to "accounting_entries", update "expenses += $amount").
    - Aggregation: Derived attrs for rollups (e.g., net_profit = $revenue - $expenses).
    - Reports: At sim end, collect attrs from "is_output" entities (export JSON/CSV).

## Simulation Flow
- **Loader (`SheetsLoader`)**: Use Drive API to discover/load sheets; parse tabs into entity attrs (e.g., actions as List<Map>).
- **Simulator Class**:
    - Load entities/globals.
    - Initialize queue with starting events.
    - Run(maxTime): Poll/apply events, check smart events (cycles, conditions), advance time.
    - Helpers: getEntitiesByType, enqueueEvent, destroyEntity (handle outputs), generateReports.
- **Example Cycle (Farmer Salary)**:
    1. Farmer entity with attrs: salary=3000, start_date=2023-01-01, cycle_length=30.
    2. On tick: If due, generate "Farmer.SalaryDue" notification with {amount, farmer_id}.
    3. PayRoll reacts: Add to accounting_entries, expenses += $amount.
    4. Derived: net_pay = $expenses * (1 - $tax_rate).

## Testing Strategy
- **Unit Tests (JUnit5 + Mockito)**:
    - ExpressionEvaluator: Test math/conditions (e.g., assert eval("100 + 5 * 6") == 130).
    - Event.apply: Mock sim, test queuing/destruction (assert queue contains ID, entity removed).
    - SheetsLoader: Mock Drive/Sheets, test discovery/skips, parsing (assert attrs loaded).
- **Integration Tests**: Mock folder with sample sheets (Cow, Farmer, PayRoll); run 90 ticks, assert PayRoll expenses == 9000 (3 months), with entries list size 3.
- **Coverage**: Aim for 80%+; use IntelliJ's debugger for tick-stepping.

## Next Steps for v0.1
- Stub SheetsLoader with hardcoded data for initial tests.
- Implement core: BaseEntity, Event, Simulator, ExpressionEvaluator.
- Add salary example as first integration test.
- Refine: Handle dates (extend evaluator with java.time), batch events for perf.

This spec is open for tweaks—e.g., add sections as we brainstorm more.