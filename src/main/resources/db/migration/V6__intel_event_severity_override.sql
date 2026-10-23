-- IntelEventPatch.severity is an operator override that null clears (IntelligenceEvent.severityOverridden).
-- computed_severity holds the severity the event had before the override while one is in force; null means
-- no override, and clearing one restores it.
alter table intelligence_event add column computed_severity varchar(16);
