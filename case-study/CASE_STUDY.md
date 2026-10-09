# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**

I would start by agreeing on what the business means by “cost per warehouse” and “cost per store.” Labor, transport, inventory handling, and overhead often come from different systems and are recorded at different levels. Some costs can be tied directly to an order, shipment, or facility; shared costs need an agreed allocation rule. If those rules are unclear, two reports can show different totals even when both are technically correct.

As an engineer, I would focus early on reliable identifiers, data ownership, and traceability. We need to know how warehouse and store codes link across operational and finance systems, what happens when a code changes, and how late invoices, returns, transfers, split shipments, and corrections are represented. I would keep source amounts and allocation rules auditable, and reconcile the resulting totals to finance before using them for decisions.

Questions I would ask include: Which decisions should this reporting support? Which system is authoritative for each cost category? How often must costs be refreshed, and how quickly after month-end must they reconcile? Which allocation drivers are acceptable—for example, labor hours, units handled, storage volume, or distance? How should estimates and corrections be handled, and who approves changes to allocation rules?

In software work, I have found that agreeing on definitions and data ownership early is just as important as the implementation. I would validate the model with finance and operations using a representative period, then deliver reports that let users trace totals back to source records and understand how shared costs were allocated.

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**

I would avoid starting with a blanket cost-cutting target. First I would establish a baseline using measures such as cost per order, cost per unit handled, labor cost per shift, transport cost per shipment, inventory carrying cost, and on-time delivery. I would segment these by warehouse, store, product, route, and time period so we can distinguish a genuine opportunity from a change in order mix or demand.

Potential opportunities include improving inventory placement to reduce inter-warehouse transfers, consolidating shipments where delivery promises allow, optimizing routes and carrier selection, reducing avoidable returns and handling, and improving labor planning against forecast demand. Better capacity utilization and reducing slow-moving or excess stock can also lower cost, but each change needs service-level and safety-stock guardrails.

I would prioritize opportunities by expected savings, customer and operational impact, implementation effort, risk, and confidence in the underlying data. I would run a limited pilot, agree on success measures in advance, and compare results with a suitable baseline or control group. If the pilot reduces cost without worsening metrics such as on-time delivery, order accuracy, or stock availability, I would scale it gradually and monitor for regressions.

Before setting the scope, I would ask which costs are controllable, what service levels cannot be compromised, where the largest cost variances occur, and whether operations can support process changes. I would also confirm the time horizon for savings and how savings will be measured and validated by finance.

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**

Integrating with finance gives the business a consistent view of operational costs and accounting actuals. It reduces manual reconciliation, helps identify variances earlier, and makes warehouse and store reports more useful for budgeting and decisions. “Real time” should be defined with users: operational estimates may need frequent updates, while final financial costs may only be reliable after invoice processing or period close.

I would begin by mapping the systems, data owners, identifiers, cost categories, and accounting dimensions. I would agree which system is authoritative for each field and define how operational records map to the chart of accounts, cost centers, warehouses, stores, and accounting periods. I would then choose an integration pattern that fits the source system—events or APIs for timely updates where supported, and scheduled file or batch imports where that is the dependable option.

For reliability, I would make processing idempotent so retries do not create duplicate costs, track source IDs and processing status, validate incoming records, and route failures to a visible retry or reconciliation queue rather than silently dropping them. I would also monitor freshness, completeness, duplicates, mapping errors, and differences from the general ledger. Access controls, audit logs, and appropriate handling of financial data are important as well.

Key questions are: Which financial system is the source of truth? What latency is actually required? How are corrections, reversals, and closed accounting periods handled? What identifiers and mappings already exist? Who resolves exceptions, and what reconciliation tolerance is acceptable? I would deliver the integration incrementally and reconcile sample periods with finance before treating the data as authoritative.

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**

Budgets and forecasts help operations plan staffing, transport capacity, inventory, and warehouse space before demand arrives. A useful forecast should explain the assumptions behind it and show how a change in volume or service expectations affects costs; a single fixed annual number is unlikely to be enough for a seasonal fulfillment business.

I would identify the main cost drivers with finance and operations: order and unit volume, product mix, labor hours and rates, inbound and outbound shipments, carrier rates, storage needs, returns, and facility overhead. The system should distinguish budget, latest forecast, and actuals, support multiple time horizons, and allow scenarios such as peak season, demand growth, or a carrier-rate change. Forecasts should be versioned so teams can compare what was expected at the time with what eventually happened.

I would also make forecasts explainable. Users should be able to see the drivers, assumptions, data freshness, and level of confidence rather than receiving an unexplained total. Forecast accuracy and bias should be measured over time, and actual-versus-forecast variance should be available at useful levels such as warehouse, store, cost category, and period. Access and approval workflows are needed so assumptions and budget changes have clear owners.

Before implementation, I would ask how budgets are currently created and approved, how often forecasts are revised, which operational drivers are available historically, how seasonality and one-off events are represented, and what level of detail managers need to take action. I would start with a transparent baseline and a small number of scenarios, then improve the model as data quality and user feedback improve.

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**

Reusing the Business Unit Code makes it especially important to distinguish the identity of the old warehouse from the new one. I would preserve the old warehouse record and its cost history, assign the replacement a new immutable internal ID, and treat the business-unit code as a reusable operational identifier rather than the key for all historical financial records. Each cost record should retain the warehouse ID and the effective time period it belongs to.

The replacement should have a clear cutover date and time. Costs incurred before cutover remain with the old warehouse; costs after cutover belong to the new one. Finance and operations should agree how to handle open purchase orders, unpaid invoices, remaining inventory, in-transit stock, shared services, and any transition or closure costs. If stock or costs are transferred, the transfer should be recorded explicitly so it is not counted twice or mistaken for new operating spend.

Preserving history lets the company explain prior actuals, audit financial statements, compare old and new operations, and understand whether the replacement is meeting its business case. At the same time, the new warehouse needs its own baseline budget and forecast; otherwise, costs from the old facility may obscure the new operation’s performance. I would compare the two facilities using normalized measures, such as cost per order or unit, while accounting for differences in volume, service levels, location, and operating model.

Before the cutover, I would ask who owns the effective date, how historical reporting should display a reused code, how open commitments and inventory will be treated, and what cost and service targets define a successful replacement. I would test the cutover and reconciliation with finance before archiving the old warehouse, and retain an auditable link between the old and replacement records.

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
