// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stream_events200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StreamEvents200ResponseCWProxy {
  StreamEvents200Response streamId(String streamId);

  StreamEvents200Response topics(List<String> topics);

  StreamEvents200Response watermarkSeq(int? watermarkSeq);

  StreamEvents200Response filterVersion(int filterVersion);

  StreamEvents200Response streamKey(String? streamKey);

  StreamEvents200Response seq(int seq);

  StreamEvents200Response source_(String source_);

  StreamEvents200Response externalId(String externalId);

  StreamEvents200Response kind(String kind);

  StreamEvents200Response version(int version);

  StreamEvents200Response observedAt(DateTime observedAt);

  StreamEvents200Response tombstone(bool tombstone);

  StreamEvents200Response geometry(Geometry? geometry);

  StreamEvents200Response payload(Map<String, Object>? payload);

  StreamEvents200Response prevSeq(int? prevSeq);

  StreamEvents200Response historyTier(HistoryTier? historyTier);

  StreamEvents200Response persistReason(PersistReason? persistReason);

  StreamEvents200Response eventTime(DateTime eventTime);

  StreamEvents200Response validUntil(DateTime? validUntil);

  StreamEvents200Response qaFlags(int? qaFlags);

  StreamEvents200Response bbox(List<num>? bbox);

  StreamEvents200Response prevPersistedBbox(List<num>? prevPersistedBbox);

  StreamEvents200Response prevLiveBbox(List<num>? prevLiveBbox);

  StreamEvents200Response leftFilter(bool? leftFilter);

  StreamEvents200Response purged(bool? purged);

  StreamEvents200Response validFrom(DateTime? validFrom);

  StreamEvents200Response globalRef(String? globalRef);

  StreamEvents200Response sourceId(String sourceId);

  StreamEvents200Response prevBbox(List<num>? prevBbox);

  StreamEvents200Response properties(Map<String, Object>? properties);

  StreamEvents200Response quality(Quality? quality);

  StreamEvents200Response reason(String reason);

  StreamEvents200Response type(StreamEvents200ResponseTypeEnum type);

  StreamEvents200Response alert(Alert alert);

  StreamEvents200Response suppressed(bool? suppressed);

  StreamEvents200Response exercise(bool? exercise);

  StreamEvents200Response id(int id);

  StreamEvents200Response event(StreamEvents200ResponseEventEnum event);

  StreamEvents200Response subtype(String subtype);

  StreamEvents200Response title(String title);

  StreamEvents200Response shortTitle(String? shortTitle);

  StreamEvents200Response bodyMarkdown(String? bodyMarkdown);

  StreamEvents200Response severity(
    StreamEvents200ResponseSeverityEnum severity,
  );

  StreamEvents200Response url(String? url);

  StreamEvents200Response links(List<NotificationLink>? links);

  StreamEvents200Response data(Map<String, Object>? data);

  StreamEvents200Response tags(List<String>? tags);

  StreamEvents200Response attachments(List<Attachment>? attachments);

  StreamEvents200Response updateOf(int? updateOf);

  StreamEvents200Response origin(String? origin);

  StreamEvents200Response createdAt(DateTime? createdAt);

  StreamEvents200Response atSeq(int atSeq);

  StreamEvents200Response resumeAfter(int? resumeAfter);

  StreamEvents200Response canonicalEventId(String canonicalEventId);

  StreamEvents200Response ref(String ref);

  StreamEvents200Response family(String family);

  StreamEvents200Response status(StreamEvents200ResponseStatusEnum status);

  StreamEvents200Response regionCodes(List<String>? regionCodes);

  StreamEvents200Response observationCount(int observationCount);

  StreamEvents200Response evidenceGroupCount(int evidenceGroupCount);

  StreamEvents200Response qualityFlags(
    List<StreamEvents200ResponseQualityFlagsEnum> qualityFlags,
  );

  StreamEvents200Response flagsAdded(
    List<StreamEvents200ResponseFlagsAddedEnum>? flagsAdded,
  );

  StreamEvents200Response mergedInto(String? mergedInto);

  StreamEvents200Response mergedFrom(List<String>? mergedFrom);

  StreamEvents200Response envelope(ObjectEnvelope? envelope);

  StreamEvents200Response role(StreamEvents200ResponseRoleEnum role);

  StreamEvents200Response sharedBy(PrincipalSummary sharedBy);

  StreamEvents200Response message(String message);

  StreamEvents200Response at(DateTime at);

  StreamEvents200Response annotationId(String annotationId);

  StreamEvents200Response parentId(String? parentId);

  StreamEvents200Response targetRef(String targetRef);

  StreamEvents200Response author(PrincipalSummary author);

  StreamEvents200Response excerpt(String? excerpt);

  StreamEvents200Response savedSearchId(String savedSearchId);

  StreamEvents200Response name(String name);

  StreamEvents200Response newMatches(int newMatches);

  StreamEvents200Response refs(List<String>? refs);

  StreamEvents200Response reportRef(String reportRef);

  StreamEvents200Response tlp(Tlp tlp);

  StreamEvents200Response revision(int revision);

  StreamEvents200Response from(PrincipalSummary? from);

  StreamEvents200Response receivedId(String receivedId);

  StreamEvents200Response packageId(String packageId);

  StreamEvents200Response senderInstanceId(String senderInstanceId);

  StreamEvents200Response signatureStatus(
    StreamEvents200ResponseSignatureStatusEnum? signatureStatus,
  );

  StreamEvents200Response chronicleId(String chronicleId);

  StreamEvents200Response link(ChronicleRelatedPageItemsInner link);

  StreamEvents200Response requestId(String requestId);

  StreamEvents200Response linkId(String linkId);

  StreamEvents200Response requesterHandle(String? requesterHandle);

  StreamEvents200Response layerId(String? layerId);

  StreamEvents200Response summary(IntelEventSummary summary);

  StreamEvents200Response snapshot(Map<String, Object>? snapshot);

  StreamEvents200Response gap(ChronicleThreadItemGap? gap);

  StreamEvents200Response backfilled(bool? backfilled);

  StreamEvents200Response previousStatus(
    StreamEvents200ResponsePreviousStatusEnum? previousStatus,
  );

  StreamEvents200Response capState(
    StreamEvents200ResponseCapStateEnum? capState,
  );

  StreamEvents200Response bytesUsed(int? bytesUsed);

  StreamEvents200Response findingId(String findingId);

  StreamEvents200Response scope(SocialVelocityEventScope scope);

  StreamEvents200Response platform(String platform);

  StreamEvents200Response language(String? language);

  StreamEvents200Response velocityRatio(num velocityRatio);

  StreamEvents200Response uniqueAuthors(int uniqueAuthors);

  StreamEvents200Response zRobust(num? zRobust);

  StreamEvents200Response detectedAt(DateTime detectedAt);

  StreamEvents200Response platformId(SocialPlatformId platformId);

  StreamEvents200Response state(StreamEvents200ResponseStateEnum state);

  StreamEvents200Response previousState(
    StreamEvents200ResponsePreviousStateEnum? previousState,
  );

  StreamEvents200Response spend(SocialSpend? spend);

  StreamEvents200Response narrative(NarrativeSummary narrative);

  StreamEvents200Response changed(
    List<StreamEvents200ResponseChangedEnum>? changed,
  );

  StreamEvents200Response claim(Claim claim);

  StreamEvents200Response fingerprintId(String fingerprintId);

  StreamEvents200Response narrativeId(String narrativeId);

  StreamEvents200Response earlierNarrativeId(String? earlierNarrativeId);

  StreamEvents200Response firstSeenAt(DateTime firstSeenAt);

  StreamEvents200Response significantIndicators(
    List<StreamEvents200ResponseSignificantIndicatorsEnum>
    significantIndicators,
  );

  StreamEvents200Response comparisonId(String comparisonId);

  StreamEvents200Response claimId(String claimId);

  StreamEvents200Response evaluatedAt(DateTime evaluatedAt);

  StreamEvents200Response cue(SocialCue cue);

  StreamEvents200Response expected(num expected);

  StreamEvents200Response observed(num observed);

  StreamEvents200Response platforms(List<String> platforms);

  StreamEvents200Response change(PresenceEventChange? change);

  StreamEvents200Response bindingId(String bindingId);

  StreamEvents200Response ruleId(String ruleId);

  StreamEvents200Response ruleName(String? ruleName);

  StreamEvents200Response firedAt(DateTime firedAt);

  StreamEvents200Response acknowledgedAt(DateTime? acknowledgedAt);

  StreamEvents200Response enrichedAt(DateTime? enrichedAt);

  StreamEvents200Response record(Record? record);

  StreamEvents200Response rev(int rev);

  StreamEvents200Response acknowledgedBy(String? acknowledgedBy);

  StreamEvents200Response resolvedAt(DateTime? resolvedAt);

  StreamEvents200Response resolvedBy(String? resolvedBy);

  StreamEvents200Response resolvedSeq(int? resolvedSeq);

  StreamEvents200Response feedback(
    StreamEvents200ResponseFeedbackEnum? feedback,
  );

  StreamEvents200Response feedbackAt(DateTime? feedbackAt);

  StreamEvents200Response note(AnalystNote note);

  StreamEvents200Response snoozedUntil(DateTime? snoozedUntil);

  StreamEvents200Response suppressedReason(
    StreamEvents200ResponseSuppressedReasonEnum? suppressedReason,
  );

  StreamEvents200Response recordSnapshot(AlertRecordSnapshot? recordSnapshot);

  StreamEvents200Response summaryCitations(List<AskCitation>? summaryCitations);

  StreamEvents200Response sensitive(bool? sensitive);

  StreamEvents200Response region(String region);

  StreamEvents200Response context(AlertContext? context);

  StreamEvents200Response exerciseRunId(String? exerciseRunId);

  StreamEvents200Response signals(List<AlertSignal>? signals);

  StreamEvents200Response unread(int unread);

  StreamEvents200Response unreadCritical(int unreadCritical);

  StreamEvents200Response latestId(int? latestId);

  StreamEvents200Response eventId(String eventId);

  StreamEvents200Response proposal(AnalystProposal proposal);

  StreamEvents200Response situation(SituationSummary situation);

  StreamEvents200Response previousSeverity(Severity? previousSeverity);

  StreamEvents200Response finding(FindingEventFinding finding);

  StreamEvents200Response precursor(PrecursorEventPrecursor precursor);

  StreamEvents200Response question(String question);

  StreamEvents200Response questionId(String? questionId);

  StreamEvents200Response forecastId(String forecastId);

  StreamEvents200Response forecasterId(String forecasterId);

  StreamEvents200Response madeAt(DateTime madeAt);

  StreamEvents200Response probability(num? probability);

  StreamEvents200Response estimativeTerm(IcdTerm? estimativeTerm);

  StreamEvents200Response private(bool? private);

  StreamEvents200Response setId(String setId);

  StreamEvents200Response revisionId(String? revisionId);

  StreamEvents200Response cause(Object? cause);

  StreamEvents200Response hypotheses(
    List<HypothesisProbabilityState>? hypotheses,
  );

  StreamEvents200Response bandChanges(
    List<HypothesisSetEventBandChangesInner>? bandChanges,
  );

  StreamEvents200Response indicatorId(String indicatorId);

  StreamEvents200Response description(String? description);

  StreamEvents200Response signalFamily(String? signalFamily);

  StreamEvents200Response evidence(Object? evidence);

  StreamEvents200Response assumptionId(String assumptionId);

  StreamEvents200Response text(String? text);

  StreamEvents200Response linchpin(bool? linchpin);

  StreamEvents200Response publishedAt(DateTime publishedAt);

  StreamEvents200Response windows(List<OutlookWindow>? windows);

  StreamEvents200Response recordRef(String? recordRef);

  StreamEvents200Response indexId(String indexId);

  StreamEvents200Response day(DateTime day);

  StreamEvents200Response value(num value);

  StreamEvents200Response previousValue(num? previousValue);

  StreamEvents200Response delta24h(num? delta24h);

  StreamEvents200Response delta7d(num? delta7d);

  StreamEvents200Response weightsOrigin(
    StreamEvents200ResponseWeightsOriginEnum? weightsOrigin,
  );

  StreamEvents200Response fitId(String fitId);

  StreamEvents200Response weightsVersion(int weightsVersion);

  StreamEvents200Response proposedWeights(Map<String, num> proposedWeights);

  StreamEvents200Response skill(RegionalIndexDefSkill? skill);

  StreamEvents200Response autoApplied(bool? autoApplied);

  StreamEvents200Response hazardType(
    StreamEvents200ResponseHazardTypeEnum hazardType,
  );

  StreamEvents200Response sourceRef(String? sourceRef);

  StreamEvents200Response impactFamily(String impactFamily);

  StreamEvents200Response admin1(String admin1);

  StreamEvents200Response pImpact(num pImpact);

  StreamEvents200Response supersededBy(String? supersededBy);

  StreamEvents200Response assessmentId(String assessmentId);

  StreamEvents200Response cycleId(String? cycleId);

  StreamEvents200Response situationId(String? situationId);

  StreamEvents200Response cadence(StreamEvents200ResponseCadenceEnum cadence);

  StreamEvents200Response periodStart(DateTime? periodStart);

  StreamEvents200Response periodEnd(DateTime? periodEnd);

  StreamEvents200Response headline(String? headline);

  StreamEvents200Response runId(String runId);

  StreamEvents200Response workspaceId(String workspaceId);

  StreamEvents200Response patch_(Map<String, Object> patch_);

  StreamEvents200Response originClientId(String? originClientId);

  StreamEvents200Response principal(String? principal);

  StreamEvents200Response clients(List<PresenceEventClientsInner> clients);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StreamEvents200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StreamEvents200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  StreamEvents200Response call({
    String streamId,
    List<String> topics,
    int? watermarkSeq,
    int filterVersion,
    String? streamKey,
    int seq,
    String source_,
    String externalId,
    String kind,
    int version,
    DateTime observedAt,
    bool tombstone,
    Geometry? geometry,
    Map<String, Object>? payload,
    int? prevSeq,
    HistoryTier? historyTier,
    PersistReason? persistReason,
    DateTime eventTime,
    DateTime? validUntil,
    int? qaFlags,
    List<num>? bbox,
    List<num>? prevPersistedBbox,
    List<num>? prevLiveBbox,
    bool? leftFilter,
    bool? purged,
    DateTime? validFrom,
    String? globalRef,
    String sourceId,
    List<num>? prevBbox,
    Map<String, Object>? properties,
    Quality? quality,
    String reason,
    StreamEvents200ResponseTypeEnum type,
    Alert alert,
    bool? suppressed,
    bool? exercise,
    int id,
    StreamEvents200ResponseEventEnum event,
    String subtype,
    String title,
    String? shortTitle,
    String? bodyMarkdown,
    StreamEvents200ResponseSeverityEnum severity,
    String? url,
    List<NotificationLink>? links,
    Map<String, Object>? data,
    List<String>? tags,
    List<Attachment>? attachments,
    int? updateOf,
    String? origin,
    DateTime? createdAt,
    int atSeq,
    int? resumeAfter,
    String canonicalEventId,
    String ref,
    String family,
    StreamEvents200ResponseStatusEnum status,
    List<String>? regionCodes,
    int observationCount,
    int evidenceGroupCount,
    List<StreamEvents200ResponseQualityFlagsEnum> qualityFlags,
    List<StreamEvents200ResponseFlagsAddedEnum>? flagsAdded,
    String? mergedInto,
    List<String>? mergedFrom,
    ObjectEnvelope? envelope,
    StreamEvents200ResponseRoleEnum role,
    PrincipalSummary sharedBy,
    String message,
    DateTime at,
    String annotationId,
    String? parentId,
    String targetRef,
    PrincipalSummary author,
    String? excerpt,
    String savedSearchId,
    String name,
    int newMatches,
    List<String>? refs,
    String reportRef,
    Tlp tlp,
    int revision,
    PrincipalSummary? from,
    String receivedId,
    String packageId,
    String senderInstanceId,
    StreamEvents200ResponseSignatureStatusEnum? signatureStatus,
    String chronicleId,
    ChronicleRelatedPageItemsInner link,
    String requestId,
    String linkId,
    String? requesterHandle,
    String? layerId,
    IntelEventSummary summary,
    Map<String, Object>? snapshot,
    ChronicleThreadItemGap? gap,
    bool? backfilled,
    StreamEvents200ResponsePreviousStatusEnum? previousStatus,
    StreamEvents200ResponseCapStateEnum? capState,
    int? bytesUsed,
    String findingId,
    SocialVelocityEventScope scope,
    String platform,
    String? language,
    num velocityRatio,
    int uniqueAuthors,
    num? zRobust,
    DateTime detectedAt,
    SocialPlatformId platformId,
    StreamEvents200ResponseStateEnum state,
    StreamEvents200ResponsePreviousStateEnum? previousState,
    SocialSpend? spend,
    NarrativeSummary narrative,
    List<StreamEvents200ResponseChangedEnum>? changed,
    Claim claim,
    String fingerprintId,
    String narrativeId,
    String? earlierNarrativeId,
    DateTime firstSeenAt,
    List<StreamEvents200ResponseSignificantIndicatorsEnum>
    significantIndicators,
    String comparisonId,
    String claimId,
    DateTime evaluatedAt,
    SocialCue cue,
    num expected,
    num observed,
    List<String> platforms,
    PresenceEventChange? change,
    String bindingId,
    String ruleId,
    String? ruleName,
    DateTime firedAt,
    DateTime? acknowledgedAt,
    DateTime? enrichedAt,
    Record? record,
    int rev,
    String? acknowledgedBy,
    DateTime? resolvedAt,
    String? resolvedBy,
    int? resolvedSeq,
    StreamEvents200ResponseFeedbackEnum? feedback,
    DateTime? feedbackAt,
    AnalystNote note,
    DateTime? snoozedUntil,
    StreamEvents200ResponseSuppressedReasonEnum? suppressedReason,
    AlertRecordSnapshot? recordSnapshot,
    List<AskCitation>? summaryCitations,
    bool? sensitive,
    String region,
    AlertContext? context,
    String? exerciseRunId,
    List<AlertSignal>? signals,
    int unread,
    int unreadCritical,
    int? latestId,
    String eventId,
    AnalystProposal proposal,
    SituationSummary situation,
    Severity? previousSeverity,
    FindingEventFinding finding,
    PrecursorEventPrecursor precursor,
    String question,
    String? questionId,
    String forecastId,
    String forecasterId,
    DateTime madeAt,
    num? probability,
    IcdTerm? estimativeTerm,
    bool? private,
    String setId,
    String? revisionId,
    Object? cause,
    List<HypothesisProbabilityState>? hypotheses,
    List<HypothesisSetEventBandChangesInner>? bandChanges,
    String indicatorId,
    String? description,
    String? signalFamily,
    Object? evidence,
    String assumptionId,
    String? text,
    bool? linchpin,
    DateTime publishedAt,
    List<OutlookWindow>? windows,
    String? recordRef,
    String indexId,
    DateTime day,
    num value,
    num? previousValue,
    num? delta24h,
    num? delta7d,
    StreamEvents200ResponseWeightsOriginEnum? weightsOrigin,
    String fitId,
    int weightsVersion,
    Map<String, num> proposedWeights,
    RegionalIndexDefSkill? skill,
    bool? autoApplied,
    StreamEvents200ResponseHazardTypeEnum hazardType,
    String? sourceRef,
    String impactFamily,
    String admin1,
    num pImpact,
    String? supersededBy,
    String assessmentId,
    String? cycleId,
    String? situationId,
    StreamEvents200ResponseCadenceEnum cadence,
    DateTime? periodStart,
    DateTime? periodEnd,
    String? headline,
    String runId,
    String workspaceId,
    Map<String, Object> patch_,
    String? originClientId,
    String? principal,
    List<PresenceEventClientsInner> clients,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStreamEvents200Response.copyWith(...)` or call `instanceOfStreamEvents200Response.copyWith.fieldName(value)` for a single field.
class _$StreamEvents200ResponseCWProxyImpl
    implements _$StreamEvents200ResponseCWProxy {
  const _$StreamEvents200ResponseCWProxyImpl(this._value);

  final StreamEvents200Response _value;

  @override
  StreamEvents200Response streamId(String streamId) => call(streamId: streamId);

  @override
  StreamEvents200Response topics(List<String> topics) => call(topics: topics);

  @override
  StreamEvents200Response watermarkSeq(int? watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  @override
  StreamEvents200Response filterVersion(int filterVersion) =>
      call(filterVersion: filterVersion);

  @override
  StreamEvents200Response streamKey(String? streamKey) =>
      call(streamKey: streamKey);

  @override
  StreamEvents200Response seq(int seq) => call(seq: seq);

  @override
  StreamEvents200Response source_(String source_) => call(source_: source_);

  @override
  StreamEvents200Response externalId(String externalId) =>
      call(externalId: externalId);

  @override
  StreamEvents200Response kind(String kind) => call(kind: kind);

  @override
  StreamEvents200Response version(int version) => call(version: version);

  @override
  StreamEvents200Response observedAt(DateTime observedAt) =>
      call(observedAt: observedAt);

  @override
  StreamEvents200Response tombstone(bool tombstone) =>
      call(tombstone: tombstone);

  @override
  StreamEvents200Response geometry(Geometry? geometry) =>
      call(geometry: geometry);

  @override
  StreamEvents200Response payload(Map<String, Object>? payload) =>
      call(payload: payload);

  @override
  StreamEvents200Response prevSeq(int? prevSeq) => call(prevSeq: prevSeq);

  @override
  StreamEvents200Response historyTier(HistoryTier? historyTier) =>
      call(historyTier: historyTier);

  @override
  StreamEvents200Response persistReason(PersistReason? persistReason) =>
      call(persistReason: persistReason);

  @override
  StreamEvents200Response eventTime(DateTime eventTime) =>
      call(eventTime: eventTime);

  @override
  StreamEvents200Response validUntil(DateTime? validUntil) =>
      call(validUntil: validUntil);

  @override
  StreamEvents200Response qaFlags(int? qaFlags) => call(qaFlags: qaFlags);

  @override
  StreamEvents200Response bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  StreamEvents200Response prevPersistedBbox(List<num>? prevPersistedBbox) =>
      call(prevPersistedBbox: prevPersistedBbox);

  @override
  StreamEvents200Response prevLiveBbox(List<num>? prevLiveBbox) =>
      call(prevLiveBbox: prevLiveBbox);

  @override
  StreamEvents200Response leftFilter(bool? leftFilter) =>
      call(leftFilter: leftFilter);

  @override
  StreamEvents200Response purged(bool? purged) => call(purged: purged);

  @override
  StreamEvents200Response validFrom(DateTime? validFrom) =>
      call(validFrom: validFrom);

  @override
  StreamEvents200Response globalRef(String? globalRef) =>
      call(globalRef: globalRef);

  @override
  StreamEvents200Response sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  StreamEvents200Response prevBbox(List<num>? prevBbox) =>
      call(prevBbox: prevBbox);

  @override
  StreamEvents200Response properties(Map<String, Object>? properties) =>
      call(properties: properties);

  @override
  StreamEvents200Response quality(Quality? quality) => call(quality: quality);

  @override
  StreamEvents200Response reason(String reason) => call(reason: reason);

  @override
  StreamEvents200Response type(StreamEvents200ResponseTypeEnum type) =>
      call(type: type);

  @override
  StreamEvents200Response alert(Alert alert) => call(alert: alert);

  @override
  StreamEvents200Response suppressed(bool? suppressed) =>
      call(suppressed: suppressed);

  @override
  StreamEvents200Response exercise(bool? exercise) => call(exercise: exercise);

  @override
  StreamEvents200Response id(int id) => call(id: id);

  @override
  StreamEvents200Response event(StreamEvents200ResponseEventEnum event) =>
      call(event: event);

  @override
  StreamEvents200Response subtype(String subtype) => call(subtype: subtype);

  @override
  StreamEvents200Response title(String title) => call(title: title);

  @override
  StreamEvents200Response shortTitle(String? shortTitle) =>
      call(shortTitle: shortTitle);

  @override
  StreamEvents200Response bodyMarkdown(String? bodyMarkdown) =>
      call(bodyMarkdown: bodyMarkdown);

  @override
  StreamEvents200Response severity(
    StreamEvents200ResponseSeverityEnum severity,
  ) => call(severity: severity);

  @override
  StreamEvents200Response url(String? url) => call(url: url);

  @override
  StreamEvents200Response links(List<NotificationLink>? links) =>
      call(links: links);

  @override
  StreamEvents200Response data(Map<String, Object>? data) => call(data: data);

  @override
  StreamEvents200Response tags(List<String>? tags) => call(tags: tags);

  @override
  StreamEvents200Response attachments(List<Attachment>? attachments) =>
      call(attachments: attachments);

  @override
  StreamEvents200Response updateOf(int? updateOf) => call(updateOf: updateOf);

  @override
  StreamEvents200Response origin(String? origin) => call(origin: origin);

  @override
  StreamEvents200Response createdAt(DateTime? createdAt) =>
      call(createdAt: createdAt);

  @override
  StreamEvents200Response atSeq(int atSeq) => call(atSeq: atSeq);

  @override
  StreamEvents200Response resumeAfter(int? resumeAfter) =>
      call(resumeAfter: resumeAfter);

  @override
  StreamEvents200Response canonicalEventId(String canonicalEventId) =>
      call(canonicalEventId: canonicalEventId);

  @override
  StreamEvents200Response ref(String ref) => call(ref: ref);

  @override
  StreamEvents200Response family(String family) => call(family: family);

  @override
  StreamEvents200Response status(StreamEvents200ResponseStatusEnum status) =>
      call(status: status);

  @override
  StreamEvents200Response regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  StreamEvents200Response observationCount(int observationCount) =>
      call(observationCount: observationCount);

  @override
  StreamEvents200Response evidenceGroupCount(int evidenceGroupCount) =>
      call(evidenceGroupCount: evidenceGroupCount);

  @override
  StreamEvents200Response qualityFlags(
    List<StreamEvents200ResponseQualityFlagsEnum> qualityFlags,
  ) => call(qualityFlags: qualityFlags);

  @override
  StreamEvents200Response flagsAdded(
    List<StreamEvents200ResponseFlagsAddedEnum>? flagsAdded,
  ) => call(flagsAdded: flagsAdded);

  @override
  StreamEvents200Response mergedInto(String? mergedInto) =>
      call(mergedInto: mergedInto);

  @override
  StreamEvents200Response mergedFrom(List<String>? mergedFrom) =>
      call(mergedFrom: mergedFrom);

  @override
  StreamEvents200Response envelope(ObjectEnvelope? envelope) =>
      call(envelope: envelope);

  @override
  StreamEvents200Response role(StreamEvents200ResponseRoleEnum role) =>
      call(role: role);

  @override
  StreamEvents200Response sharedBy(PrincipalSummary sharedBy) =>
      call(sharedBy: sharedBy);

  @override
  StreamEvents200Response message(String message) => call(message: message);

  @override
  StreamEvents200Response at(DateTime at) => call(at: at);

  @override
  StreamEvents200Response annotationId(String annotationId) =>
      call(annotationId: annotationId);

  @override
  StreamEvents200Response parentId(String? parentId) =>
      call(parentId: parentId);

  @override
  StreamEvents200Response targetRef(String targetRef) =>
      call(targetRef: targetRef);

  @override
  StreamEvents200Response author(PrincipalSummary author) =>
      call(author: author);

  @override
  StreamEvents200Response excerpt(String? excerpt) => call(excerpt: excerpt);

  @override
  StreamEvents200Response savedSearchId(String savedSearchId) =>
      call(savedSearchId: savedSearchId);

  @override
  StreamEvents200Response name(String name) => call(name: name);

  @override
  StreamEvents200Response newMatches(int newMatches) =>
      call(newMatches: newMatches);

  @override
  StreamEvents200Response refs(List<String>? refs) => call(refs: refs);

  @override
  StreamEvents200Response reportRef(String reportRef) =>
      call(reportRef: reportRef);

  @override
  StreamEvents200Response tlp(Tlp tlp) => call(tlp: tlp);

  @override
  StreamEvents200Response revision(int revision) => call(revision: revision);

  @override
  StreamEvents200Response from(PrincipalSummary? from) => call(from: from);

  @override
  StreamEvents200Response receivedId(String receivedId) =>
      call(receivedId: receivedId);

  @override
  StreamEvents200Response packageId(String packageId) =>
      call(packageId: packageId);

  @override
  StreamEvents200Response senderInstanceId(String senderInstanceId) =>
      call(senderInstanceId: senderInstanceId);

  @override
  StreamEvents200Response signatureStatus(
    StreamEvents200ResponseSignatureStatusEnum? signatureStatus,
  ) => call(signatureStatus: signatureStatus);

  @override
  StreamEvents200Response chronicleId(String chronicleId) =>
      call(chronicleId: chronicleId);

  @override
  StreamEvents200Response link(ChronicleRelatedPageItemsInner link) =>
      call(link: link);

  @override
  StreamEvents200Response requestId(String requestId) =>
      call(requestId: requestId);

  @override
  StreamEvents200Response linkId(String linkId) => call(linkId: linkId);

  @override
  StreamEvents200Response requesterHandle(String? requesterHandle) =>
      call(requesterHandle: requesterHandle);

  @override
  StreamEvents200Response layerId(String? layerId) => call(layerId: layerId);

  @override
  StreamEvents200Response summary(IntelEventSummary summary) =>
      call(summary: summary);

  @override
  StreamEvents200Response snapshot(Map<String, Object>? snapshot) =>
      call(snapshot: snapshot);

  @override
  StreamEvents200Response gap(ChronicleThreadItemGap? gap) => call(gap: gap);

  @override
  StreamEvents200Response backfilled(bool? backfilled) =>
      call(backfilled: backfilled);

  @override
  StreamEvents200Response previousStatus(
    StreamEvents200ResponsePreviousStatusEnum? previousStatus,
  ) => call(previousStatus: previousStatus);

  @override
  StreamEvents200Response capState(
    StreamEvents200ResponseCapStateEnum? capState,
  ) => call(capState: capState);

  @override
  StreamEvents200Response bytesUsed(int? bytesUsed) =>
      call(bytesUsed: bytesUsed);

  @override
  StreamEvents200Response findingId(String findingId) =>
      call(findingId: findingId);

  @override
  StreamEvents200Response scope(SocialVelocityEventScope scope) =>
      call(scope: scope);

  @override
  StreamEvents200Response platform(String platform) => call(platform: platform);

  @override
  StreamEvents200Response language(String? language) =>
      call(language: language);

  @override
  StreamEvents200Response velocityRatio(num velocityRatio) =>
      call(velocityRatio: velocityRatio);

  @override
  StreamEvents200Response uniqueAuthors(int uniqueAuthors) =>
      call(uniqueAuthors: uniqueAuthors);

  @override
  StreamEvents200Response zRobust(num? zRobust) => call(zRobust: zRobust);

  @override
  StreamEvents200Response detectedAt(DateTime detectedAt) =>
      call(detectedAt: detectedAt);

  @override
  StreamEvents200Response platformId(SocialPlatformId platformId) =>
      call(platformId: platformId);

  @override
  StreamEvents200Response state(StreamEvents200ResponseStateEnum state) =>
      call(state: state);

  @override
  StreamEvents200Response previousState(
    StreamEvents200ResponsePreviousStateEnum? previousState,
  ) => call(previousState: previousState);

  @override
  StreamEvents200Response spend(SocialSpend? spend) => call(spend: spend);

  @override
  StreamEvents200Response narrative(NarrativeSummary narrative) =>
      call(narrative: narrative);

  @override
  StreamEvents200Response changed(
    List<StreamEvents200ResponseChangedEnum>? changed,
  ) => call(changed: changed);

  @override
  StreamEvents200Response claim(Claim claim) => call(claim: claim);

  @override
  StreamEvents200Response fingerprintId(String fingerprintId) =>
      call(fingerprintId: fingerprintId);

  @override
  StreamEvents200Response narrativeId(String narrativeId) =>
      call(narrativeId: narrativeId);

  @override
  StreamEvents200Response earlierNarrativeId(String? earlierNarrativeId) =>
      call(earlierNarrativeId: earlierNarrativeId);

  @override
  StreamEvents200Response firstSeenAt(DateTime firstSeenAt) =>
      call(firstSeenAt: firstSeenAt);

  @override
  StreamEvents200Response significantIndicators(
    List<StreamEvents200ResponseSignificantIndicatorsEnum>
    significantIndicators,
  ) => call(significantIndicators: significantIndicators);

  @override
  StreamEvents200Response comparisonId(String comparisonId) =>
      call(comparisonId: comparisonId);

  @override
  StreamEvents200Response claimId(String claimId) => call(claimId: claimId);

  @override
  StreamEvents200Response evaluatedAt(DateTime evaluatedAt) =>
      call(evaluatedAt: evaluatedAt);

  @override
  StreamEvents200Response cue(SocialCue cue) => call(cue: cue);

  @override
  StreamEvents200Response expected(num expected) => call(expected: expected);

  @override
  StreamEvents200Response observed(num observed) => call(observed: observed);

  @override
  StreamEvents200Response platforms(List<String> platforms) =>
      call(platforms: platforms);

  @override
  StreamEvents200Response change(PresenceEventChange? change) =>
      call(change: change);

  @override
  StreamEvents200Response bindingId(String bindingId) =>
      call(bindingId: bindingId);

  @override
  StreamEvents200Response ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  StreamEvents200Response ruleName(String? ruleName) =>
      call(ruleName: ruleName);

  @override
  StreamEvents200Response firedAt(DateTime firedAt) => call(firedAt: firedAt);

  @override
  StreamEvents200Response acknowledgedAt(DateTime? acknowledgedAt) =>
      call(acknowledgedAt: acknowledgedAt);

  @override
  StreamEvents200Response enrichedAt(DateTime? enrichedAt) =>
      call(enrichedAt: enrichedAt);

  @override
  StreamEvents200Response record(Record? record) => call(record: record);

  @override
  StreamEvents200Response rev(int rev) => call(rev: rev);

  @override
  StreamEvents200Response acknowledgedBy(String? acknowledgedBy) =>
      call(acknowledgedBy: acknowledgedBy);

  @override
  StreamEvents200Response resolvedAt(DateTime? resolvedAt) =>
      call(resolvedAt: resolvedAt);

  @override
  StreamEvents200Response resolvedBy(String? resolvedBy) =>
      call(resolvedBy: resolvedBy);

  @override
  StreamEvents200Response resolvedSeq(int? resolvedSeq) =>
      call(resolvedSeq: resolvedSeq);

  @override
  StreamEvents200Response feedback(
    StreamEvents200ResponseFeedbackEnum? feedback,
  ) => call(feedback: feedback);

  @override
  StreamEvents200Response feedbackAt(DateTime? feedbackAt) =>
      call(feedbackAt: feedbackAt);

  @override
  StreamEvents200Response note(AnalystNote note) => call(note: note);

  @override
  StreamEvents200Response snoozedUntil(DateTime? snoozedUntil) =>
      call(snoozedUntil: snoozedUntil);

  @override
  StreamEvents200Response suppressedReason(
    StreamEvents200ResponseSuppressedReasonEnum? suppressedReason,
  ) => call(suppressedReason: suppressedReason);

  @override
  StreamEvents200Response recordSnapshot(AlertRecordSnapshot? recordSnapshot) =>
      call(recordSnapshot: recordSnapshot);

  @override
  StreamEvents200Response summaryCitations(
    List<AskCitation>? summaryCitations,
  ) => call(summaryCitations: summaryCitations);

  @override
  StreamEvents200Response sensitive(bool? sensitive) =>
      call(sensitive: sensitive);

  @override
  StreamEvents200Response region(String region) => call(region: region);

  @override
  StreamEvents200Response context(AlertContext? context) =>
      call(context: context);

  @override
  StreamEvents200Response exerciseRunId(String? exerciseRunId) =>
      call(exerciseRunId: exerciseRunId);

  @override
  StreamEvents200Response signals(List<AlertSignal>? signals) =>
      call(signals: signals);

  @override
  StreamEvents200Response unread(int unread) => call(unread: unread);

  @override
  StreamEvents200Response unreadCritical(int unreadCritical) =>
      call(unreadCritical: unreadCritical);

  @override
  StreamEvents200Response latestId(int? latestId) => call(latestId: latestId);

  @override
  StreamEvents200Response eventId(String eventId) => call(eventId: eventId);

  @override
  StreamEvents200Response proposal(AnalystProposal proposal) =>
      call(proposal: proposal);

  @override
  StreamEvents200Response situation(SituationSummary situation) =>
      call(situation: situation);

  @override
  StreamEvents200Response previousSeverity(Severity? previousSeverity) =>
      call(previousSeverity: previousSeverity);

  @override
  StreamEvents200Response finding(FindingEventFinding finding) =>
      call(finding: finding);

  @override
  StreamEvents200Response precursor(PrecursorEventPrecursor precursor) =>
      call(precursor: precursor);

  @override
  StreamEvents200Response question(String question) => call(question: question);

  @override
  StreamEvents200Response questionId(String? questionId) =>
      call(questionId: questionId);

  @override
  StreamEvents200Response forecastId(String forecastId) =>
      call(forecastId: forecastId);

  @override
  StreamEvents200Response forecasterId(String forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  StreamEvents200Response madeAt(DateTime madeAt) => call(madeAt: madeAt);

  @override
  StreamEvents200Response probability(num? probability) =>
      call(probability: probability);

  @override
  StreamEvents200Response estimativeTerm(IcdTerm? estimativeTerm) =>
      call(estimativeTerm: estimativeTerm);

  @override
  StreamEvents200Response private(bool? private) => call(private: private);

  @override
  StreamEvents200Response setId(String setId) => call(setId: setId);

  @override
  StreamEvents200Response revisionId(String? revisionId) =>
      call(revisionId: revisionId);

  @override
  StreamEvents200Response cause(Object? cause) => call(cause: cause);

  @override
  StreamEvents200Response hypotheses(
    List<HypothesisProbabilityState>? hypotheses,
  ) => call(hypotheses: hypotheses);

  @override
  StreamEvents200Response bandChanges(
    List<HypothesisSetEventBandChangesInner>? bandChanges,
  ) => call(bandChanges: bandChanges);

  @override
  StreamEvents200Response indicatorId(String indicatorId) =>
      call(indicatorId: indicatorId);

  @override
  StreamEvents200Response description(String? description) =>
      call(description: description);

  @override
  StreamEvents200Response signalFamily(String? signalFamily) =>
      call(signalFamily: signalFamily);

  @override
  StreamEvents200Response evidence(Object? evidence) =>
      call(evidence: evidence);

  @override
  StreamEvents200Response assumptionId(String assumptionId) =>
      call(assumptionId: assumptionId);

  @override
  StreamEvents200Response text(String? text) => call(text: text);

  @override
  StreamEvents200Response linchpin(bool? linchpin) => call(linchpin: linchpin);

  @override
  StreamEvents200Response publishedAt(DateTime publishedAt) =>
      call(publishedAt: publishedAt);

  @override
  StreamEvents200Response windows(List<OutlookWindow>? windows) =>
      call(windows: windows);

  @override
  StreamEvents200Response recordRef(String? recordRef) =>
      call(recordRef: recordRef);

  @override
  StreamEvents200Response indexId(String indexId) => call(indexId: indexId);

  @override
  StreamEvents200Response day(DateTime day) => call(day: day);

  @override
  StreamEvents200Response value(num value) => call(value: value);

  @override
  StreamEvents200Response previousValue(num? previousValue) =>
      call(previousValue: previousValue);

  @override
  StreamEvents200Response delta24h(num? delta24h) => call(delta24h: delta24h);

  @override
  StreamEvents200Response delta7d(num? delta7d) => call(delta7d: delta7d);

  @override
  StreamEvents200Response weightsOrigin(
    StreamEvents200ResponseWeightsOriginEnum? weightsOrigin,
  ) => call(weightsOrigin: weightsOrigin);

  @override
  StreamEvents200Response fitId(String fitId) => call(fitId: fitId);

  @override
  StreamEvents200Response weightsVersion(int weightsVersion) =>
      call(weightsVersion: weightsVersion);

  @override
  StreamEvents200Response proposedWeights(Map<String, num> proposedWeights) =>
      call(proposedWeights: proposedWeights);

  @override
  StreamEvents200Response skill(RegionalIndexDefSkill? skill) =>
      call(skill: skill);

  @override
  StreamEvents200Response autoApplied(bool? autoApplied) =>
      call(autoApplied: autoApplied);

  @override
  StreamEvents200Response hazardType(
    StreamEvents200ResponseHazardTypeEnum hazardType,
  ) => call(hazardType: hazardType);

  @override
  StreamEvents200Response sourceRef(String? sourceRef) =>
      call(sourceRef: sourceRef);

  @override
  StreamEvents200Response impactFamily(String impactFamily) =>
      call(impactFamily: impactFamily);

  @override
  StreamEvents200Response admin1(String admin1) => call(admin1: admin1);

  @override
  StreamEvents200Response pImpact(num pImpact) => call(pImpact: pImpact);

  @override
  StreamEvents200Response supersededBy(String? supersededBy) =>
      call(supersededBy: supersededBy);

  @override
  StreamEvents200Response assessmentId(String assessmentId) =>
      call(assessmentId: assessmentId);

  @override
  StreamEvents200Response cycleId(String? cycleId) => call(cycleId: cycleId);

  @override
  StreamEvents200Response situationId(String? situationId) =>
      call(situationId: situationId);

  @override
  StreamEvents200Response cadence(StreamEvents200ResponseCadenceEnum cadence) =>
      call(cadence: cadence);

  @override
  StreamEvents200Response periodStart(DateTime? periodStart) =>
      call(periodStart: periodStart);

  @override
  StreamEvents200Response periodEnd(DateTime? periodEnd) =>
      call(periodEnd: periodEnd);

  @override
  StreamEvents200Response headline(String? headline) =>
      call(headline: headline);

  @override
  StreamEvents200Response runId(String runId) => call(runId: runId);

  @override
  StreamEvents200Response workspaceId(String workspaceId) =>
      call(workspaceId: workspaceId);

  @override
  StreamEvents200Response patch_(Map<String, Object> patch_) =>
      call(patch_: patch_);

  @override
  StreamEvents200Response originClientId(String? originClientId) =>
      call(originClientId: originClientId);

  @override
  StreamEvents200Response principal(String? principal) =>
      call(principal: principal);

  @override
  StreamEvents200Response clients(List<PresenceEventClientsInner> clients) =>
      call(clients: clients);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StreamEvents200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StreamEvents200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StreamEvents200Response call({
    Object? streamId = const $CopyWithPlaceholder(),
    Object? topics = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
    Object? filterVersion = const $CopyWithPlaceholder(),
    Object? streamKey = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? tombstone = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? payload = const $CopyWithPlaceholder(),
    Object? prevSeq = const $CopyWithPlaceholder(),
    Object? historyTier = const $CopyWithPlaceholder(),
    Object? persistReason = const $CopyWithPlaceholder(),
    Object? eventTime = const $CopyWithPlaceholder(),
    Object? validUntil = const $CopyWithPlaceholder(),
    Object? qaFlags = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? prevPersistedBbox = const $CopyWithPlaceholder(),
    Object? prevLiveBbox = const $CopyWithPlaceholder(),
    Object? leftFilter = const $CopyWithPlaceholder(),
    Object? purged = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? globalRef = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? prevBbox = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
    Object? quality = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? alert = const $CopyWithPlaceholder(),
    Object? suppressed = const $CopyWithPlaceholder(),
    Object? exercise = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? event = const $CopyWithPlaceholder(),
    Object? subtype = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? shortTitle = const $CopyWithPlaceholder(),
    Object? bodyMarkdown = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? data = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? attachments = const $CopyWithPlaceholder(),
    Object? updateOf = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? atSeq = const $CopyWithPlaceholder(),
    Object? resumeAfter = const $CopyWithPlaceholder(),
    Object? canonicalEventId = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? family = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? observationCount = const $CopyWithPlaceholder(),
    Object? evidenceGroupCount = const $CopyWithPlaceholder(),
    Object? qualityFlags = const $CopyWithPlaceholder(),
    Object? flagsAdded = const $CopyWithPlaceholder(),
    Object? mergedInto = const $CopyWithPlaceholder(),
    Object? mergedFrom = const $CopyWithPlaceholder(),
    Object? envelope = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? sharedBy = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? annotationId = const $CopyWithPlaceholder(),
    Object? parentId = const $CopyWithPlaceholder(),
    Object? targetRef = const $CopyWithPlaceholder(),
    Object? author = const $CopyWithPlaceholder(),
    Object? excerpt = const $CopyWithPlaceholder(),
    Object? savedSearchId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? newMatches = const $CopyWithPlaceholder(),
    Object? refs = const $CopyWithPlaceholder(),
    Object? reportRef = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? revision = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? receivedId = const $CopyWithPlaceholder(),
    Object? packageId = const $CopyWithPlaceholder(),
    Object? senderInstanceId = const $CopyWithPlaceholder(),
    Object? signatureStatus = const $CopyWithPlaceholder(),
    Object? chronicleId = const $CopyWithPlaceholder(),
    Object? link = const $CopyWithPlaceholder(),
    Object? requestId = const $CopyWithPlaceholder(),
    Object? linkId = const $CopyWithPlaceholder(),
    Object? requesterHandle = const $CopyWithPlaceholder(),
    Object? layerId = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? snapshot = const $CopyWithPlaceholder(),
    Object? gap = const $CopyWithPlaceholder(),
    Object? backfilled = const $CopyWithPlaceholder(),
    Object? previousStatus = const $CopyWithPlaceholder(),
    Object? capState = const $CopyWithPlaceholder(),
    Object? bytesUsed = const $CopyWithPlaceholder(),
    Object? findingId = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? platform = const $CopyWithPlaceholder(),
    Object? language = const $CopyWithPlaceholder(),
    Object? velocityRatio = const $CopyWithPlaceholder(),
    Object? uniqueAuthors = const $CopyWithPlaceholder(),
    Object? zRobust = const $CopyWithPlaceholder(),
    Object? detectedAt = const $CopyWithPlaceholder(),
    Object? platformId = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? previousState = const $CopyWithPlaceholder(),
    Object? spend = const $CopyWithPlaceholder(),
    Object? narrative = const $CopyWithPlaceholder(),
    Object? changed = const $CopyWithPlaceholder(),
    Object? claim = const $CopyWithPlaceholder(),
    Object? fingerprintId = const $CopyWithPlaceholder(),
    Object? narrativeId = const $CopyWithPlaceholder(),
    Object? earlierNarrativeId = const $CopyWithPlaceholder(),
    Object? firstSeenAt = const $CopyWithPlaceholder(),
    Object? significantIndicators = const $CopyWithPlaceholder(),
    Object? comparisonId = const $CopyWithPlaceholder(),
    Object? claimId = const $CopyWithPlaceholder(),
    Object? evaluatedAt = const $CopyWithPlaceholder(),
    Object? cue = const $CopyWithPlaceholder(),
    Object? expected = const $CopyWithPlaceholder(),
    Object? observed = const $CopyWithPlaceholder(),
    Object? platforms = const $CopyWithPlaceholder(),
    Object? change = const $CopyWithPlaceholder(),
    Object? bindingId = const $CopyWithPlaceholder(),
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? ruleName = const $CopyWithPlaceholder(),
    Object? firedAt = const $CopyWithPlaceholder(),
    Object? acknowledgedAt = const $CopyWithPlaceholder(),
    Object? enrichedAt = const $CopyWithPlaceholder(),
    Object? record = const $CopyWithPlaceholder(),
    Object? rev = const $CopyWithPlaceholder(),
    Object? acknowledgedBy = const $CopyWithPlaceholder(),
    Object? resolvedAt = const $CopyWithPlaceholder(),
    Object? resolvedBy = const $CopyWithPlaceholder(),
    Object? resolvedSeq = const $CopyWithPlaceholder(),
    Object? feedback = const $CopyWithPlaceholder(),
    Object? feedbackAt = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
    Object? snoozedUntil = const $CopyWithPlaceholder(),
    Object? suppressedReason = const $CopyWithPlaceholder(),
    Object? recordSnapshot = const $CopyWithPlaceholder(),
    Object? summaryCitations = const $CopyWithPlaceholder(),
    Object? sensitive = const $CopyWithPlaceholder(),
    Object? region = const $CopyWithPlaceholder(),
    Object? context = const $CopyWithPlaceholder(),
    Object? exerciseRunId = const $CopyWithPlaceholder(),
    Object? signals = const $CopyWithPlaceholder(),
    Object? unread = const $CopyWithPlaceholder(),
    Object? unreadCritical = const $CopyWithPlaceholder(),
    Object? latestId = const $CopyWithPlaceholder(),
    Object? eventId = const $CopyWithPlaceholder(),
    Object? proposal = const $CopyWithPlaceholder(),
    Object? situation = const $CopyWithPlaceholder(),
    Object? previousSeverity = const $CopyWithPlaceholder(),
    Object? finding = const $CopyWithPlaceholder(),
    Object? precursor = const $CopyWithPlaceholder(),
    Object? question = const $CopyWithPlaceholder(),
    Object? questionId = const $CopyWithPlaceholder(),
    Object? forecastId = const $CopyWithPlaceholder(),
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? madeAt = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? estimativeTerm = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? setId = const $CopyWithPlaceholder(),
    Object? revisionId = const $CopyWithPlaceholder(),
    Object? cause = const $CopyWithPlaceholder(),
    Object? hypotheses = const $CopyWithPlaceholder(),
    Object? bandChanges = const $CopyWithPlaceholder(),
    Object? indicatorId = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? signalFamily = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? assumptionId = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
    Object? linchpin = const $CopyWithPlaceholder(),
    Object? publishedAt = const $CopyWithPlaceholder(),
    Object? windows = const $CopyWithPlaceholder(),
    Object? recordRef = const $CopyWithPlaceholder(),
    Object? indexId = const $CopyWithPlaceholder(),
    Object? day = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? previousValue = const $CopyWithPlaceholder(),
    Object? delta24h = const $CopyWithPlaceholder(),
    Object? delta7d = const $CopyWithPlaceholder(),
    Object? weightsOrigin = const $CopyWithPlaceholder(),
    Object? fitId = const $CopyWithPlaceholder(),
    Object? weightsVersion = const $CopyWithPlaceholder(),
    Object? proposedWeights = const $CopyWithPlaceholder(),
    Object? skill = const $CopyWithPlaceholder(),
    Object? autoApplied = const $CopyWithPlaceholder(),
    Object? hazardType = const $CopyWithPlaceholder(),
    Object? sourceRef = const $CopyWithPlaceholder(),
    Object? impactFamily = const $CopyWithPlaceholder(),
    Object? admin1 = const $CopyWithPlaceholder(),
    Object? pImpact = const $CopyWithPlaceholder(),
    Object? supersededBy = const $CopyWithPlaceholder(),
    Object? assessmentId = const $CopyWithPlaceholder(),
    Object? cycleId = const $CopyWithPlaceholder(),
    Object? situationId = const $CopyWithPlaceholder(),
    Object? cadence = const $CopyWithPlaceholder(),
    Object? periodStart = const $CopyWithPlaceholder(),
    Object? periodEnd = const $CopyWithPlaceholder(),
    Object? headline = const $CopyWithPlaceholder(),
    Object? runId = const $CopyWithPlaceholder(),
    Object? workspaceId = const $CopyWithPlaceholder(),
    Object? patch_ = const $CopyWithPlaceholder(),
    Object? originClientId = const $CopyWithPlaceholder(),
    Object? principal = const $CopyWithPlaceholder(),
    Object? clients = const $CopyWithPlaceholder(),
  }) {
    return StreamEvents200Response(
      streamId: streamId == const $CopyWithPlaceholder() || streamId == null
          ? _value.streamId
          // ignore: cast_nullable_to_non_nullable
          : streamId as String,
      topics: topics == const $CopyWithPlaceholder() || topics == null
          ? _value.topics
          // ignore: cast_nullable_to_non_nullable
          : topics as List<String>,
      watermarkSeq: watermarkSeq == const $CopyWithPlaceholder()
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int?,
      filterVersion:
          filterVersion == const $CopyWithPlaceholder() || filterVersion == null
          ? _value.filterVersion
          // ignore: cast_nullable_to_non_nullable
          : filterVersion as int,
      streamKey: streamKey == const $CopyWithPlaceholder()
          ? _value.streamKey
          // ignore: cast_nullable_to_non_nullable
          : streamKey as String?,
      seq: seq == const $CopyWithPlaceholder() || seq == null
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int,
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      tombstone: tombstone == const $CopyWithPlaceholder() || tombstone == null
          ? _value.tombstone
          // ignore: cast_nullable_to_non_nullable
          : tombstone as bool,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      payload: payload == const $CopyWithPlaceholder()
          ? _value.payload
          // ignore: cast_nullable_to_non_nullable
          : payload as Map<String, Object>?,
      prevSeq: prevSeq == const $CopyWithPlaceholder()
          ? _value.prevSeq
          // ignore: cast_nullable_to_non_nullable
          : prevSeq as int?,
      historyTier: historyTier == const $CopyWithPlaceholder()
          ? _value.historyTier
          // ignore: cast_nullable_to_non_nullable
          : historyTier as HistoryTier?,
      persistReason: persistReason == const $CopyWithPlaceholder()
          ? _value.persistReason
          // ignore: cast_nullable_to_non_nullable
          : persistReason as PersistReason?,
      eventTime: eventTime == const $CopyWithPlaceholder() || eventTime == null
          ? _value.eventTime
          // ignore: cast_nullable_to_non_nullable
          : eventTime as DateTime,
      validUntil: validUntil == const $CopyWithPlaceholder()
          ? _value.validUntil
          // ignore: cast_nullable_to_non_nullable
          : validUntil as DateTime?,
      qaFlags: qaFlags == const $CopyWithPlaceholder()
          ? _value.qaFlags
          // ignore: cast_nullable_to_non_nullable
          : qaFlags as int?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      prevPersistedBbox: prevPersistedBbox == const $CopyWithPlaceholder()
          ? _value.prevPersistedBbox
          // ignore: cast_nullable_to_non_nullable
          : prevPersistedBbox as List<num>?,
      prevLiveBbox: prevLiveBbox == const $CopyWithPlaceholder()
          ? _value.prevLiveBbox
          // ignore: cast_nullable_to_non_nullable
          : prevLiveBbox as List<num>?,
      leftFilter: leftFilter == const $CopyWithPlaceholder()
          ? _value.leftFilter
          // ignore: cast_nullable_to_non_nullable
          : leftFilter as bool?,
      purged: purged == const $CopyWithPlaceholder()
          ? _value.purged
          // ignore: cast_nullable_to_non_nullable
          : purged as bool?,
      validFrom: validFrom == const $CopyWithPlaceholder()
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime?,
      globalRef: globalRef == const $CopyWithPlaceholder()
          ? _value.globalRef
          // ignore: cast_nullable_to_non_nullable
          : globalRef as String?,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      prevBbox: prevBbox == const $CopyWithPlaceholder()
          ? _value.prevBbox
          // ignore: cast_nullable_to_non_nullable
          : prevBbox as List<num>?,
      properties: properties == const $CopyWithPlaceholder()
          ? _value.properties
          // ignore: cast_nullable_to_non_nullable
          : properties as Map<String, Object>?,
      quality: quality == const $CopyWithPlaceholder()
          ? _value.quality
          // ignore: cast_nullable_to_non_nullable
          : quality as Quality?,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as StreamEvents200ResponseTypeEnum,
      alert: alert == const $CopyWithPlaceholder() || alert == null
          ? _value.alert
          // ignore: cast_nullable_to_non_nullable
          : alert as Alert,
      suppressed: suppressed == const $CopyWithPlaceholder()
          ? _value.suppressed
          // ignore: cast_nullable_to_non_nullable
          : suppressed as bool?,
      exercise: exercise == const $CopyWithPlaceholder()
          ? _value.exercise
          // ignore: cast_nullable_to_non_nullable
          : exercise as bool?,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as StreamEvents200ResponseEventEnum,
      subtype: subtype == const $CopyWithPlaceholder() || subtype == null
          ? _value.subtype
          // ignore: cast_nullable_to_non_nullable
          : subtype as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      shortTitle: shortTitle == const $CopyWithPlaceholder()
          ? _value.shortTitle
          // ignore: cast_nullable_to_non_nullable
          : shortTitle as String?,
      bodyMarkdown: bodyMarkdown == const $CopyWithPlaceholder()
          ? _value.bodyMarkdown
          // ignore: cast_nullable_to_non_nullable
          : bodyMarkdown as String?,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as StreamEvents200ResponseSeverityEnum,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<NotificationLink>?,
      data: data == const $CopyWithPlaceholder()
          ? _value.data
          // ignore: cast_nullable_to_non_nullable
          : data as Map<String, Object>?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      attachments: attachments == const $CopyWithPlaceholder()
          ? _value.attachments
          // ignore: cast_nullable_to_non_nullable
          : attachments as List<Attachment>?,
      updateOf: updateOf == const $CopyWithPlaceholder()
          ? _value.updateOf
          // ignore: cast_nullable_to_non_nullable
          : updateOf as int?,
      origin: origin == const $CopyWithPlaceholder()
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as String?,
      createdAt: createdAt == const $CopyWithPlaceholder()
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime?,
      atSeq: atSeq == const $CopyWithPlaceholder() || atSeq == null
          ? _value.atSeq
          // ignore: cast_nullable_to_non_nullable
          : atSeq as int,
      resumeAfter: resumeAfter == const $CopyWithPlaceholder()
          ? _value.resumeAfter
          // ignore: cast_nullable_to_non_nullable
          : resumeAfter as int?,
      canonicalEventId:
          canonicalEventId == const $CopyWithPlaceholder() ||
              canonicalEventId == null
          ? _value.canonicalEventId
          // ignore: cast_nullable_to_non_nullable
          : canonicalEventId as String,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as StreamEvents200ResponseStatusEnum,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      observationCount:
          observationCount == const $CopyWithPlaceholder() ||
              observationCount == null
          ? _value.observationCount
          // ignore: cast_nullable_to_non_nullable
          : observationCount as int,
      evidenceGroupCount:
          evidenceGroupCount == const $CopyWithPlaceholder() ||
              evidenceGroupCount == null
          ? _value.evidenceGroupCount
          // ignore: cast_nullable_to_non_nullable
          : evidenceGroupCount as int,
      qualityFlags:
          qualityFlags == const $CopyWithPlaceholder() || qualityFlags == null
          ? _value.qualityFlags
          // ignore: cast_nullable_to_non_nullable
          : qualityFlags as List<StreamEvents200ResponseQualityFlagsEnum>,
      flagsAdded: flagsAdded == const $CopyWithPlaceholder()
          ? _value.flagsAdded
          // ignore: cast_nullable_to_non_nullable
          : flagsAdded as List<StreamEvents200ResponseFlagsAddedEnum>?,
      mergedInto: mergedInto == const $CopyWithPlaceholder()
          ? _value.mergedInto
          // ignore: cast_nullable_to_non_nullable
          : mergedInto as String?,
      mergedFrom: mergedFrom == const $CopyWithPlaceholder()
          ? _value.mergedFrom
          // ignore: cast_nullable_to_non_nullable
          : mergedFrom as List<String>?,
      envelope: envelope == const $CopyWithPlaceholder()
          ? _value.envelope
          // ignore: cast_nullable_to_non_nullable
          : envelope as ObjectEnvelope?,
      role: role == const $CopyWithPlaceholder() || role == null
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as StreamEvents200ResponseRoleEnum,
      sharedBy: sharedBy == const $CopyWithPlaceholder() || sharedBy == null
          ? _value.sharedBy
          // ignore: cast_nullable_to_non_nullable
          : sharedBy as PrincipalSummary,
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      annotationId:
          annotationId == const $CopyWithPlaceholder() || annotationId == null
          ? _value.annotationId
          // ignore: cast_nullable_to_non_nullable
          : annotationId as String,
      parentId: parentId == const $CopyWithPlaceholder()
          ? _value.parentId
          // ignore: cast_nullable_to_non_nullable
          : parentId as String?,
      targetRef: targetRef == const $CopyWithPlaceholder() || targetRef == null
          ? _value.targetRef
          // ignore: cast_nullable_to_non_nullable
          : targetRef as String,
      author: author == const $CopyWithPlaceholder() || author == null
          ? _value.author
          // ignore: cast_nullable_to_non_nullable
          : author as PrincipalSummary,
      excerpt: excerpt == const $CopyWithPlaceholder()
          ? _value.excerpt
          // ignore: cast_nullable_to_non_nullable
          : excerpt as String?,
      savedSearchId:
          savedSearchId == const $CopyWithPlaceholder() || savedSearchId == null
          ? _value.savedSearchId
          // ignore: cast_nullable_to_non_nullable
          : savedSearchId as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      newMatches:
          newMatches == const $CopyWithPlaceholder() || newMatches == null
          ? _value.newMatches
          // ignore: cast_nullable_to_non_nullable
          : newMatches as int,
      refs: refs == const $CopyWithPlaceholder()
          ? _value.refs
          // ignore: cast_nullable_to_non_nullable
          : refs as List<String>?,
      reportRef: reportRef == const $CopyWithPlaceholder() || reportRef == null
          ? _value.reportRef
          // ignore: cast_nullable_to_non_nullable
          : reportRef as String,
      tlp: tlp == const $CopyWithPlaceholder() || tlp == null
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp,
      revision: revision == const $CopyWithPlaceholder() || revision == null
          ? _value.revision
          // ignore: cast_nullable_to_non_nullable
          : revision as int,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as PrincipalSummary?,
      receivedId:
          receivedId == const $CopyWithPlaceholder() || receivedId == null
          ? _value.receivedId
          // ignore: cast_nullable_to_non_nullable
          : receivedId as String,
      packageId: packageId == const $CopyWithPlaceholder() || packageId == null
          ? _value.packageId
          // ignore: cast_nullable_to_non_nullable
          : packageId as String,
      senderInstanceId:
          senderInstanceId == const $CopyWithPlaceholder() ||
              senderInstanceId == null
          ? _value.senderInstanceId
          // ignore: cast_nullable_to_non_nullable
          : senderInstanceId as String,
      signatureStatus: signatureStatus == const $CopyWithPlaceholder()
          ? _value.signatureStatus
          // ignore: cast_nullable_to_non_nullable
          : signatureStatus as StreamEvents200ResponseSignatureStatusEnum?,
      chronicleId:
          chronicleId == const $CopyWithPlaceholder() || chronicleId == null
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String,
      link: link == const $CopyWithPlaceholder() || link == null
          ? _value.link
          // ignore: cast_nullable_to_non_nullable
          : link as ChronicleRelatedPageItemsInner,
      requestId: requestId == const $CopyWithPlaceholder() || requestId == null
          ? _value.requestId
          // ignore: cast_nullable_to_non_nullable
          : requestId as String,
      linkId: linkId == const $CopyWithPlaceholder() || linkId == null
          ? _value.linkId
          // ignore: cast_nullable_to_non_nullable
          : linkId as String,
      requesterHandle: requesterHandle == const $CopyWithPlaceholder()
          ? _value.requesterHandle
          // ignore: cast_nullable_to_non_nullable
          : requesterHandle as String?,
      layerId: layerId == const $CopyWithPlaceholder()
          ? _value.layerId
          // ignore: cast_nullable_to_non_nullable
          : layerId as String?,
      summary: summary == const $CopyWithPlaceholder() || summary == null
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as IntelEventSummary,
      snapshot: snapshot == const $CopyWithPlaceholder()
          ? _value.snapshot
          // ignore: cast_nullable_to_non_nullable
          : snapshot as Map<String, Object>?,
      gap: gap == const $CopyWithPlaceholder()
          ? _value.gap
          // ignore: cast_nullable_to_non_nullable
          : gap as ChronicleThreadItemGap?,
      backfilled: backfilled == const $CopyWithPlaceholder()
          ? _value.backfilled
          // ignore: cast_nullable_to_non_nullable
          : backfilled as bool?,
      previousStatus: previousStatus == const $CopyWithPlaceholder()
          ? _value.previousStatus
          // ignore: cast_nullable_to_non_nullable
          : previousStatus as StreamEvents200ResponsePreviousStatusEnum?,
      capState: capState == const $CopyWithPlaceholder()
          ? _value.capState
          // ignore: cast_nullable_to_non_nullable
          : capState as StreamEvents200ResponseCapStateEnum?,
      bytesUsed: bytesUsed == const $CopyWithPlaceholder()
          ? _value.bytesUsed
          // ignore: cast_nullable_to_non_nullable
          : bytesUsed as int?,
      findingId: findingId == const $CopyWithPlaceholder() || findingId == null
          ? _value.findingId
          // ignore: cast_nullable_to_non_nullable
          : findingId as String,
      scope: scope == const $CopyWithPlaceholder() || scope == null
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as SocialVelocityEventScope,
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String,
      language: language == const $CopyWithPlaceholder()
          ? _value.language
          // ignore: cast_nullable_to_non_nullable
          : language as String?,
      velocityRatio:
          velocityRatio == const $CopyWithPlaceholder() || velocityRatio == null
          ? _value.velocityRatio
          // ignore: cast_nullable_to_non_nullable
          : velocityRatio as num,
      uniqueAuthors:
          uniqueAuthors == const $CopyWithPlaceholder() || uniqueAuthors == null
          ? _value.uniqueAuthors
          // ignore: cast_nullable_to_non_nullable
          : uniqueAuthors as int,
      zRobust: zRobust == const $CopyWithPlaceholder()
          ? _value.zRobust
          // ignore: cast_nullable_to_non_nullable
          : zRobust as num?,
      detectedAt:
          detectedAt == const $CopyWithPlaceholder() || detectedAt == null
          ? _value.detectedAt
          // ignore: cast_nullable_to_non_nullable
          : detectedAt as DateTime,
      platformId:
          platformId == const $CopyWithPlaceholder() || platformId == null
          ? _value.platformId
          // ignore: cast_nullable_to_non_nullable
          : platformId as SocialPlatformId,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as StreamEvents200ResponseStateEnum,
      previousState: previousState == const $CopyWithPlaceholder()
          ? _value.previousState
          // ignore: cast_nullable_to_non_nullable
          : previousState as StreamEvents200ResponsePreviousStateEnum?,
      spend: spend == const $CopyWithPlaceholder()
          ? _value.spend
          // ignore: cast_nullable_to_non_nullable
          : spend as SocialSpend?,
      narrative: narrative == const $CopyWithPlaceholder() || narrative == null
          ? _value.narrative
          // ignore: cast_nullable_to_non_nullable
          : narrative as NarrativeSummary,
      changed: changed == const $CopyWithPlaceholder()
          ? _value.changed
          // ignore: cast_nullable_to_non_nullable
          : changed as List<StreamEvents200ResponseChangedEnum>?,
      claim: claim == const $CopyWithPlaceholder() || claim == null
          ? _value.claim
          // ignore: cast_nullable_to_non_nullable
          : claim as Claim,
      fingerprintId:
          fingerprintId == const $CopyWithPlaceholder() || fingerprintId == null
          ? _value.fingerprintId
          // ignore: cast_nullable_to_non_nullable
          : fingerprintId as String,
      narrativeId:
          narrativeId == const $CopyWithPlaceholder() || narrativeId == null
          ? _value.narrativeId
          // ignore: cast_nullable_to_non_nullable
          : narrativeId as String,
      earlierNarrativeId: earlierNarrativeId == const $CopyWithPlaceholder()
          ? _value.earlierNarrativeId
          // ignore: cast_nullable_to_non_nullable
          : earlierNarrativeId as String?,
      firstSeenAt:
          firstSeenAt == const $CopyWithPlaceholder() || firstSeenAt == null
          ? _value.firstSeenAt
          // ignore: cast_nullable_to_non_nullable
          : firstSeenAt as DateTime,
      significantIndicators:
          significantIndicators == const $CopyWithPlaceholder() ||
              significantIndicators == null
          ? _value.significantIndicators
          // ignore: cast_nullable_to_non_nullable
          : significantIndicators
                as List<StreamEvents200ResponseSignificantIndicatorsEnum>,
      comparisonId:
          comparisonId == const $CopyWithPlaceholder() || comparisonId == null
          ? _value.comparisonId
          // ignore: cast_nullable_to_non_nullable
          : comparisonId as String,
      claimId: claimId == const $CopyWithPlaceholder() || claimId == null
          ? _value.claimId
          // ignore: cast_nullable_to_non_nullable
          : claimId as String,
      evaluatedAt:
          evaluatedAt == const $CopyWithPlaceholder() || evaluatedAt == null
          ? _value.evaluatedAt
          // ignore: cast_nullable_to_non_nullable
          : evaluatedAt as DateTime,
      cue: cue == const $CopyWithPlaceholder() || cue == null
          ? _value.cue
          // ignore: cast_nullable_to_non_nullable
          : cue as SocialCue,
      expected: expected == const $CopyWithPlaceholder() || expected == null
          ? _value.expected
          // ignore: cast_nullable_to_non_nullable
          : expected as num,
      observed: observed == const $CopyWithPlaceholder() || observed == null
          ? _value.observed
          // ignore: cast_nullable_to_non_nullable
          : observed as num,
      platforms: platforms == const $CopyWithPlaceholder() || platforms == null
          ? _value.platforms
          // ignore: cast_nullable_to_non_nullable
          : platforms as List<String>,
      change: change == const $CopyWithPlaceholder()
          ? _value.change
          // ignore: cast_nullable_to_non_nullable
          : change as PresenceEventChange?,
      bindingId: bindingId == const $CopyWithPlaceholder() || bindingId == null
          ? _value.bindingId
          // ignore: cast_nullable_to_non_nullable
          : bindingId as String,
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      ruleName: ruleName == const $CopyWithPlaceholder()
          ? _value.ruleName
          // ignore: cast_nullable_to_non_nullable
          : ruleName as String?,
      firedAt: firedAt == const $CopyWithPlaceholder() || firedAt == null
          ? _value.firedAt
          // ignore: cast_nullable_to_non_nullable
          : firedAt as DateTime,
      acknowledgedAt: acknowledgedAt == const $CopyWithPlaceholder()
          ? _value.acknowledgedAt
          // ignore: cast_nullable_to_non_nullable
          : acknowledgedAt as DateTime?,
      enrichedAt: enrichedAt == const $CopyWithPlaceholder()
          ? _value.enrichedAt
          // ignore: cast_nullable_to_non_nullable
          : enrichedAt as DateTime?,
      record: record == const $CopyWithPlaceholder()
          ? _value.record
          // ignore: cast_nullable_to_non_nullable
          : record as Record?,
      rev: rev == const $CopyWithPlaceholder() || rev == null
          ? _value.rev
          // ignore: cast_nullable_to_non_nullable
          : rev as int,
      acknowledgedBy: acknowledgedBy == const $CopyWithPlaceholder()
          ? _value.acknowledgedBy
          // ignore: cast_nullable_to_non_nullable
          : acknowledgedBy as String?,
      resolvedAt: resolvedAt == const $CopyWithPlaceholder()
          ? _value.resolvedAt
          // ignore: cast_nullable_to_non_nullable
          : resolvedAt as DateTime?,
      resolvedBy: resolvedBy == const $CopyWithPlaceholder()
          ? _value.resolvedBy
          // ignore: cast_nullable_to_non_nullable
          : resolvedBy as String?,
      resolvedSeq: resolvedSeq == const $CopyWithPlaceholder()
          ? _value.resolvedSeq
          // ignore: cast_nullable_to_non_nullable
          : resolvedSeq as int?,
      feedback: feedback == const $CopyWithPlaceholder()
          ? _value.feedback
          // ignore: cast_nullable_to_non_nullable
          : feedback as StreamEvents200ResponseFeedbackEnum?,
      feedbackAt: feedbackAt == const $CopyWithPlaceholder()
          ? _value.feedbackAt
          // ignore: cast_nullable_to_non_nullable
          : feedbackAt as DateTime?,
      note: note == const $CopyWithPlaceholder() || note == null
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as AnalystNote,
      snoozedUntil: snoozedUntil == const $CopyWithPlaceholder()
          ? _value.snoozedUntil
          // ignore: cast_nullable_to_non_nullable
          : snoozedUntil as DateTime?,
      suppressedReason: suppressedReason == const $CopyWithPlaceholder()
          ? _value.suppressedReason
          // ignore: cast_nullable_to_non_nullable
          : suppressedReason as StreamEvents200ResponseSuppressedReasonEnum?,
      recordSnapshot: recordSnapshot == const $CopyWithPlaceholder()
          ? _value.recordSnapshot
          // ignore: cast_nullable_to_non_nullable
          : recordSnapshot as AlertRecordSnapshot?,
      summaryCitations: summaryCitations == const $CopyWithPlaceholder()
          ? _value.summaryCitations
          // ignore: cast_nullable_to_non_nullable
          : summaryCitations as List<AskCitation>?,
      sensitive: sensitive == const $CopyWithPlaceholder()
          ? _value.sensitive
          // ignore: cast_nullable_to_non_nullable
          : sensitive as bool?,
      region: region == const $CopyWithPlaceholder() || region == null
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as String,
      context: context == const $CopyWithPlaceholder()
          ? _value.context
          // ignore: cast_nullable_to_non_nullable
          : context as AlertContext?,
      exerciseRunId: exerciseRunId == const $CopyWithPlaceholder()
          ? _value.exerciseRunId
          // ignore: cast_nullable_to_non_nullable
          : exerciseRunId as String?,
      signals: signals == const $CopyWithPlaceholder()
          ? _value.signals
          // ignore: cast_nullable_to_non_nullable
          : signals as List<AlertSignal>?,
      unread: unread == const $CopyWithPlaceholder() || unread == null
          ? _value.unread
          // ignore: cast_nullable_to_non_nullable
          : unread as int,
      unreadCritical:
          unreadCritical == const $CopyWithPlaceholder() ||
              unreadCritical == null
          ? _value.unreadCritical
          // ignore: cast_nullable_to_non_nullable
          : unreadCritical as int,
      latestId: latestId == const $CopyWithPlaceholder()
          ? _value.latestId
          // ignore: cast_nullable_to_non_nullable
          : latestId as int?,
      eventId: eventId == const $CopyWithPlaceholder() || eventId == null
          ? _value.eventId
          // ignore: cast_nullable_to_non_nullable
          : eventId as String,
      proposal: proposal == const $CopyWithPlaceholder() || proposal == null
          ? _value.proposal
          // ignore: cast_nullable_to_non_nullable
          : proposal as AnalystProposal,
      situation: situation == const $CopyWithPlaceholder() || situation == null
          ? _value.situation
          // ignore: cast_nullable_to_non_nullable
          : situation as SituationSummary,
      previousSeverity: previousSeverity == const $CopyWithPlaceholder()
          ? _value.previousSeverity
          // ignore: cast_nullable_to_non_nullable
          : previousSeverity as Severity?,
      finding: finding == const $CopyWithPlaceholder() || finding == null
          ? _value.finding
          // ignore: cast_nullable_to_non_nullable
          : finding as FindingEventFinding,
      precursor: precursor == const $CopyWithPlaceholder() || precursor == null
          ? _value.precursor
          // ignore: cast_nullable_to_non_nullable
          : precursor as PrecursorEventPrecursor,
      question: question == const $CopyWithPlaceholder() || question == null
          ? _value.question
          // ignore: cast_nullable_to_non_nullable
          : question as String,
      questionId: questionId == const $CopyWithPlaceholder()
          ? _value.questionId
          // ignore: cast_nullable_to_non_nullable
          : questionId as String?,
      forecastId:
          forecastId == const $CopyWithPlaceholder() || forecastId == null
          ? _value.forecastId
          // ignore: cast_nullable_to_non_nullable
          : forecastId as String,
      forecasterId:
          forecasterId == const $CopyWithPlaceholder() || forecasterId == null
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String,
      madeAt: madeAt == const $CopyWithPlaceholder() || madeAt == null
          ? _value.madeAt
          // ignore: cast_nullable_to_non_nullable
          : madeAt as DateTime,
      probability: probability == const $CopyWithPlaceholder()
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num?,
      estimativeTerm: estimativeTerm == const $CopyWithPlaceholder()
          ? _value.estimativeTerm
          // ignore: cast_nullable_to_non_nullable
          : estimativeTerm as IcdTerm?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      revisionId: revisionId == const $CopyWithPlaceholder()
          ? _value.revisionId
          // ignore: cast_nullable_to_non_nullable
          : revisionId as String?,
      cause: cause == const $CopyWithPlaceholder()
          ? _value.cause
          // ignore: cast_nullable_to_non_nullable
          : cause as Object?,
      hypotheses: hypotheses == const $CopyWithPlaceholder()
          ? _value.hypotheses
          // ignore: cast_nullable_to_non_nullable
          : hypotheses as List<HypothesisProbabilityState>?,
      bandChanges: bandChanges == const $CopyWithPlaceholder()
          ? _value.bandChanges
          // ignore: cast_nullable_to_non_nullable
          : bandChanges as List<HypothesisSetEventBandChangesInner>?,
      indicatorId:
          indicatorId == const $CopyWithPlaceholder() || indicatorId == null
          ? _value.indicatorId
          // ignore: cast_nullable_to_non_nullable
          : indicatorId as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      signalFamily: signalFamily == const $CopyWithPlaceholder()
          ? _value.signalFamily
          // ignore: cast_nullable_to_non_nullable
          : signalFamily as String?,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as Object?,
      assumptionId:
          assumptionId == const $CopyWithPlaceholder() || assumptionId == null
          ? _value.assumptionId
          // ignore: cast_nullable_to_non_nullable
          : assumptionId as String,
      text: text == const $CopyWithPlaceholder()
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String?,
      linchpin: linchpin == const $CopyWithPlaceholder()
          ? _value.linchpin
          // ignore: cast_nullable_to_non_nullable
          : linchpin as bool?,
      publishedAt:
          publishedAt == const $CopyWithPlaceholder() || publishedAt == null
          ? _value.publishedAt
          // ignore: cast_nullable_to_non_nullable
          : publishedAt as DateTime,
      windows: windows == const $CopyWithPlaceholder()
          ? _value.windows
          // ignore: cast_nullable_to_non_nullable
          : windows as List<OutlookWindow>?,
      recordRef: recordRef == const $CopyWithPlaceholder()
          ? _value.recordRef
          // ignore: cast_nullable_to_non_nullable
          : recordRef as String?,
      indexId: indexId == const $CopyWithPlaceholder() || indexId == null
          ? _value.indexId
          // ignore: cast_nullable_to_non_nullable
          : indexId as String,
      day: day == const $CopyWithPlaceholder() || day == null
          ? _value.day
          // ignore: cast_nullable_to_non_nullable
          : day as DateTime,
      value: value == const $CopyWithPlaceholder() || value == null
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num,
      previousValue: previousValue == const $CopyWithPlaceholder()
          ? _value.previousValue
          // ignore: cast_nullable_to_non_nullable
          : previousValue as num?,
      delta24h: delta24h == const $CopyWithPlaceholder()
          ? _value.delta24h
          // ignore: cast_nullable_to_non_nullable
          : delta24h as num?,
      delta7d: delta7d == const $CopyWithPlaceholder()
          ? _value.delta7d
          // ignore: cast_nullable_to_non_nullable
          : delta7d as num?,
      weightsOrigin: weightsOrigin == const $CopyWithPlaceholder()
          ? _value.weightsOrigin
          // ignore: cast_nullable_to_non_nullable
          : weightsOrigin as StreamEvents200ResponseWeightsOriginEnum?,
      fitId: fitId == const $CopyWithPlaceholder() || fitId == null
          ? _value.fitId
          // ignore: cast_nullable_to_non_nullable
          : fitId as String,
      weightsVersion:
          weightsVersion == const $CopyWithPlaceholder() ||
              weightsVersion == null
          ? _value.weightsVersion
          // ignore: cast_nullable_to_non_nullable
          : weightsVersion as int,
      proposedWeights:
          proposedWeights == const $CopyWithPlaceholder() ||
              proposedWeights == null
          ? _value.proposedWeights
          // ignore: cast_nullable_to_non_nullable
          : proposedWeights as Map<String, num>,
      skill: skill == const $CopyWithPlaceholder()
          ? _value.skill
          // ignore: cast_nullable_to_non_nullable
          : skill as RegionalIndexDefSkill?,
      autoApplied: autoApplied == const $CopyWithPlaceholder()
          ? _value.autoApplied
          // ignore: cast_nullable_to_non_nullable
          : autoApplied as bool?,
      hazardType:
          hazardType == const $CopyWithPlaceholder() || hazardType == null
          ? _value.hazardType
          // ignore: cast_nullable_to_non_nullable
          : hazardType as StreamEvents200ResponseHazardTypeEnum,
      sourceRef: sourceRef == const $CopyWithPlaceholder()
          ? _value.sourceRef
          // ignore: cast_nullable_to_non_nullable
          : sourceRef as String?,
      impactFamily:
          impactFamily == const $CopyWithPlaceholder() || impactFamily == null
          ? _value.impactFamily
          // ignore: cast_nullable_to_non_nullable
          : impactFamily as String,
      admin1: admin1 == const $CopyWithPlaceholder() || admin1 == null
          ? _value.admin1
          // ignore: cast_nullable_to_non_nullable
          : admin1 as String,
      pImpact: pImpact == const $CopyWithPlaceholder() || pImpact == null
          ? _value.pImpact
          // ignore: cast_nullable_to_non_nullable
          : pImpact as num,
      supersededBy: supersededBy == const $CopyWithPlaceholder()
          ? _value.supersededBy
          // ignore: cast_nullable_to_non_nullable
          : supersededBy as String?,
      assessmentId:
          assessmentId == const $CopyWithPlaceholder() || assessmentId == null
          ? _value.assessmentId
          // ignore: cast_nullable_to_non_nullable
          : assessmentId as String,
      cycleId: cycleId == const $CopyWithPlaceholder()
          ? _value.cycleId
          // ignore: cast_nullable_to_non_nullable
          : cycleId as String?,
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      cadence: cadence == const $CopyWithPlaceholder() || cadence == null
          ? _value.cadence
          // ignore: cast_nullable_to_non_nullable
          : cadence as StreamEvents200ResponseCadenceEnum,
      periodStart: periodStart == const $CopyWithPlaceholder()
          ? _value.periodStart
          // ignore: cast_nullable_to_non_nullable
          : periodStart as DateTime?,
      periodEnd: periodEnd == const $CopyWithPlaceholder()
          ? _value.periodEnd
          // ignore: cast_nullable_to_non_nullable
          : periodEnd as DateTime?,
      headline: headline == const $CopyWithPlaceholder()
          ? _value.headline
          // ignore: cast_nullable_to_non_nullable
          : headline as String?,
      runId: runId == const $CopyWithPlaceholder() || runId == null
          ? _value.runId
          // ignore: cast_nullable_to_non_nullable
          : runId as String,
      workspaceId:
          workspaceId == const $CopyWithPlaceholder() || workspaceId == null
          ? _value.workspaceId
          // ignore: cast_nullable_to_non_nullable
          : workspaceId as String,
      patch_: patch_ == const $CopyWithPlaceholder() || patch_ == null
          ? _value.patch_
          // ignore: cast_nullable_to_non_nullable
          : patch_ as Map<String, Object>,
      originClientId: originClientId == const $CopyWithPlaceholder()
          ? _value.originClientId
          // ignore: cast_nullable_to_non_nullable
          : originClientId as String?,
      principal: principal == const $CopyWithPlaceholder()
          ? _value.principal
          // ignore: cast_nullable_to_non_nullable
          : principal as String?,
      clients: clients == const $CopyWithPlaceholder() || clients == null
          ? _value.clients
          // ignore: cast_nullable_to_non_nullable
          : clients as List<PresenceEventClientsInner>,
    );
  }
}

extension $StreamEvents200ResponseCopyWith on StreamEvents200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStreamEvents200Response.copyWith(...)` or `instanceOfStreamEvents200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StreamEvents200ResponseCWProxy get copyWith =>
      _$StreamEvents200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StreamEvents200Response _$StreamEvents200ResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StreamEvents200Response', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'streamId',
      'topics',
      'watermarkSeq',
      'filterVersion',
      'seq',
      'source',
      'externalId',
      'kind',
      'version',
      'observedAt',
      'tombstone',
      'eventTime',
      'sourceId',
      'reason',
      'type',
      'alert',
      'id',
      'event',
      'subtype',
      'title',
      'severity',
      'atSeq',
      'canonicalEventId',
      'ref',
      'family',
      'status',
      'observationCount',
      'evidenceGroupCount',
      'qualityFlags',
      'role',
      'sharedBy',
      'message',
      'at',
      'annotationId',
      'targetRef',
      'author',
      'savedSearchId',
      'name',
      'newMatches',
      'reportRef',
      'tlp',
      'revision',
      'receivedId',
      'packageId',
      'senderInstanceId',
      'chronicleId',
      'link',
      'requestId',
      'linkId',
      'summary',
      'findingId',
      'scope',
      'platform',
      'velocityRatio',
      'uniqueAuthors',
      'detectedAt',
      'platformId',
      'state',
      'narrative',
      'claim',
      'fingerprintId',
      'narrativeId',
      'firstSeenAt',
      'significantIndicators',
      'comparisonId',
      'claimId',
      'evaluatedAt',
      'cue',
      'expected',
      'observed',
      'platforms',
      'change',
      'bindingId',
      'ruleId',
      'firedAt',
      'rev',
      'note',
      'region',
      'unread',
      'unreadCritical',
      'eventId',
      'proposal',
      'situation',
      'finding',
      'precursor',
      'question',
      'questionId',
      'forecastId',
      'forecasterId',
      'madeAt',
      'setId',
      'indicatorId',
      'assumptionId',
      'publishedAt',
      'indexId',
      'day',
      'value',
      'fitId',
      'weightsVersion',
      'proposedWeights',
      'hazardType',
      'impactFamily',
      'admin1',
      'pImpact',
      'assessmentId',
      'cadence',
      'runId',
      'workspaceId',
      'patch',
      'clients',
    ],
  );
  final val = StreamEvents200Response(
    streamId: $checkedConvert('streamId', (v) => v as String),
    topics: $checkedConvert(
      'topics',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num?)?.toInt()),
    filterVersion: $checkedConvert('filterVersion', (v) => (v as num).toInt()),
    streamKey: $checkedConvert('streamKey', (v) => v as String?),
    seq: $checkedConvert('seq', (v) => (v as num).toInt()),
    source_: $checkedConvert('source', (v) => v as String),
    externalId: $checkedConvert('externalId', (v) => v as String),
    kind: $checkedConvert('kind', (v) => v as String),
    version: $checkedConvert('version', (v) => (v as num).toInt()),
    observedAt: $checkedConvert(
      'observedAt',
      (v) => DateTime.parse(v as String),
    ),
    tombstone: $checkedConvert('tombstone', (v) => v as bool),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    payload: $checkedConvert(
      'payload',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    prevSeq: $checkedConvert('prevSeq', (v) => (v as num?)?.toInt()),
    historyTier: $checkedConvert(
      'historyTier',
      (v) => $enumDecodeNullable(_$HistoryTierEnumMap, v),
    ),
    persistReason: $checkedConvert(
      'persistReason',
      (v) => $enumDecodeNullable(_$PersistReasonEnumMap, v),
    ),
    eventTime: $checkedConvert('eventTime', (v) => DateTime.parse(v as String)),
    validUntil: $checkedConvert(
      'validUntil',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    qaFlags: $checkedConvert('qaFlags', (v) => (v as num?)?.toInt()),
    bbox: $checkedConvert(
      'bbox',
      (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
    ),
    prevPersistedBbox: $checkedConvert(
      'prevPersistedBbox',
      (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
    ),
    prevLiveBbox: $checkedConvert(
      'prevLiveBbox',
      (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
    ),
    leftFilter: $checkedConvert('leftFilter', (v) => v as bool? ?? false),
    purged: $checkedConvert('purged', (v) => v as bool?),
    validFrom: $checkedConvert(
      'validFrom',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    globalRef: $checkedConvert('globalRef', (v) => v as String?),
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    prevBbox: $checkedConvert(
      'prevBbox',
      (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
    ),
    properties: $checkedConvert(
      'properties',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    quality: $checkedConvert(
      'quality',
      (v) => v == null ? null : Quality.fromJson(v as Map<String, dynamic>),
    ),
    reason: $checkedConvert('reason', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$StreamEvents200ResponseTypeEnumEnumMap, v),
    ),
    alert: $checkedConvert(
      'alert',
      (v) => Alert.fromJson(v as Map<String, dynamic>),
    ),
    suppressed: $checkedConvert('suppressed', (v) => v as bool? ?? false),
    exercise: $checkedConvert('exercise', (v) => v as bool? ?? false),
    id: $checkedConvert('id', (v) => (v as num).toInt()),
    event: $checkedConvert(
      'event',
      (v) => $enumDecode(_$StreamEvents200ResponseEventEnumEnumMap, v),
    ),
    subtype: $checkedConvert('subtype', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    shortTitle: $checkedConvert('shortTitle', (v) => v as String?),
    bodyMarkdown: $checkedConvert('bodyMarkdown', (v) => v as String?),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecode(_$StreamEvents200ResponseSeverityEnumEnumMap, v),
    ),
    url: $checkedConvert('url', (v) => v as String?),
    links: $checkedConvert(
      'links',
      (v) => (v as List<dynamic>?)
          ?.map((e) => NotificationLink.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    data: $checkedConvert(
      'data',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    tags: $checkedConvert(
      'tags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    attachments: $checkedConvert(
      'attachments',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Attachment.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    updateOf: $checkedConvert('updateOf', (v) => (v as num?)?.toInt()),
    origin: $checkedConvert('origin', (v) => v as String?),
    createdAt: $checkedConvert(
      'createdAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    atSeq: $checkedConvert('atSeq', (v) => (v as num).toInt()),
    resumeAfter: $checkedConvert('resumeAfter', (v) => (v as num?)?.toInt()),
    canonicalEventId: $checkedConvert('canonicalEventId', (v) => v as String),
    ref: $checkedConvert('ref', (v) => v as String),
    family: $checkedConvert('family', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$StreamEvents200ResponseStatusEnumEnumMap, v),
    ),
    regionCodes: $checkedConvert(
      'regionCodes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    observationCount: $checkedConvert(
      'observationCount',
      (v) => (v as num).toInt(),
    ),
    evidenceGroupCount: $checkedConvert(
      'evidenceGroupCount',
      (v) => (v as num).toInt(),
    ),
    qualityFlags: $checkedConvert(
      'qualityFlags',
      (v) => (v as List<dynamic>)
          .map(
            (e) => $enumDecode(
              _$StreamEvents200ResponseQualityFlagsEnumEnumMap,
              e,
            ),
          )
          .toList(),
    ),
    flagsAdded: $checkedConvert(
      'flagsAdded',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) =>
                $enumDecode(_$StreamEvents200ResponseFlagsAddedEnumEnumMap, e),
          )
          .toList(),
    ),
    mergedInto: $checkedConvert('mergedInto', (v) => v as String?),
    mergedFrom: $checkedConvert(
      'mergedFrom',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    envelope: $checkedConvert(
      'envelope',
      (v) =>
          v == null ? null : ObjectEnvelope.fromJson(v as Map<String, dynamic>),
    ),
    role: $checkedConvert(
      'role',
      (v) => $enumDecode(_$StreamEvents200ResponseRoleEnumEnumMap, v),
    ),
    sharedBy: $checkedConvert(
      'sharedBy',
      (v) => PrincipalSummary.fromJson(v as Map<String, dynamic>),
    ),
    message: $checkedConvert('message', (v) => v as String),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    annotationId: $checkedConvert('annotationId', (v) => v as String),
    parentId: $checkedConvert('parentId', (v) => v as String?),
    targetRef: $checkedConvert('targetRef', (v) => v as String),
    author: $checkedConvert(
      'author',
      (v) => PrincipalSummary.fromJson(v as Map<String, dynamic>),
    ),
    excerpt: $checkedConvert('excerpt', (v) => v as String?),
    savedSearchId: $checkedConvert('savedSearchId', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    newMatches: $checkedConvert('newMatches', (v) => (v as num).toInt()),
    refs: $checkedConvert(
      'refs',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    reportRef: $checkedConvert('reportRef', (v) => v as String),
    tlp: $checkedConvert('tlp', (v) => $enumDecode(_$TlpEnumMap, v)),
    revision: $checkedConvert('revision', (v) => (v as num).toInt()),
    from: $checkedConvert(
      'from',
      (v) => v == null
          ? null
          : PrincipalSummary.fromJson(v as Map<String, dynamic>),
    ),
    receivedId: $checkedConvert('receivedId', (v) => v as String),
    packageId: $checkedConvert('packageId', (v) => v as String),
    senderInstanceId: $checkedConvert('senderInstanceId', (v) => v as String),
    signatureStatus: $checkedConvert(
      'signatureStatus',
      (v) => $enumDecodeNullable(
        _$StreamEvents200ResponseSignatureStatusEnumEnumMap,
        v,
      ),
    ),
    chronicleId: $checkedConvert('chronicleId', (v) => v as String),
    link: $checkedConvert(
      'link',
      (v) => ChronicleRelatedPageItemsInner.fromJson(v as Map<String, dynamic>),
    ),
    requestId: $checkedConvert('requestId', (v) => v as String),
    linkId: $checkedConvert('linkId', (v) => v as String),
    requesterHandle: $checkedConvert('requesterHandle', (v) => v as String?),
    layerId: $checkedConvert('layerId', (v) => v as String?),
    summary: $checkedConvert(
      'summary',
      (v) => IntelEventSummary.fromJson(v as Map<String, dynamic>),
    ),
    snapshot: $checkedConvert(
      'snapshot',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    gap: $checkedConvert(
      'gap',
      (v) => v == null
          ? null
          : ChronicleThreadItemGap.fromJson(v as Map<String, dynamic>),
    ),
    backfilled: $checkedConvert('backfilled', (v) => v as bool?),
    previousStatus: $checkedConvert(
      'previousStatus',
      (v) => $enumDecodeNullable(
        _$StreamEvents200ResponsePreviousStatusEnumEnumMap,
        v,
      ),
    ),
    capState: $checkedConvert(
      'capState',
      (v) =>
          $enumDecodeNullable(_$StreamEvents200ResponseCapStateEnumEnumMap, v),
    ),
    bytesUsed: $checkedConvert('bytesUsed', (v) => (v as num?)?.toInt()),
    findingId: $checkedConvert('findingId', (v) => v as String),
    scope: $checkedConvert(
      'scope',
      (v) => SocialVelocityEventScope.fromJson(v as Map<String, dynamic>),
    ),
    platform: $checkedConvert('platform', (v) => v as String),
    language: $checkedConvert('language', (v) => v as String?),
    velocityRatio: $checkedConvert('velocityRatio', (v) => v as num),
    uniqueAuthors: $checkedConvert('uniqueAuthors', (v) => (v as num).toInt()),
    zRobust: $checkedConvert('zRobust', (v) => v as num?),
    detectedAt: $checkedConvert(
      'detectedAt',
      (v) => DateTime.parse(v as String),
    ),
    platformId: $checkedConvert(
      'platformId',
      (v) => $enumDecode(_$SocialPlatformIdEnumMap, v),
    ),
    state: $checkedConvert(
      'state',
      (v) => $enumDecode(_$StreamEvents200ResponseStateEnumEnumMap, v),
    ),
    previousState: $checkedConvert(
      'previousState',
      (v) => $enumDecodeNullable(
        _$StreamEvents200ResponsePreviousStateEnumEnumMap,
        v,
      ),
    ),
    spend: $checkedConvert(
      'spend',
      (v) => v == null ? null : SocialSpend.fromJson(v as Map<String, dynamic>),
    ),
    narrative: $checkedConvert(
      'narrative',
      (v) => NarrativeSummary.fromJson(v as Map<String, dynamic>),
    ),
    changed: $checkedConvert(
      'changed',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => $enumDecode(_$StreamEvents200ResponseChangedEnumEnumMap, e),
          )
          .toList(),
    ),
    claim: $checkedConvert(
      'claim',
      (v) => Claim.fromJson(v as Map<String, dynamic>),
    ),
    fingerprintId: $checkedConvert('fingerprintId', (v) => v as String),
    narrativeId: $checkedConvert('narrativeId', (v) => v as String),
    earlierNarrativeId: $checkedConvert(
      'earlierNarrativeId',
      (v) => v as String?,
    ),
    firstSeenAt: $checkedConvert(
      'firstSeenAt',
      (v) => DateTime.parse(v as String),
    ),
    significantIndicators: $checkedConvert(
      'significantIndicators',
      (v) => (v as List<dynamic>)
          .map(
            (e) => $enumDecode(
              _$StreamEvents200ResponseSignificantIndicatorsEnumEnumMap,
              e,
            ),
          )
          .toList(),
    ),
    comparisonId: $checkedConvert('comparisonId', (v) => v as String),
    claimId: $checkedConvert('claimId', (v) => v as String),
    evaluatedAt: $checkedConvert(
      'evaluatedAt',
      (v) => DateTime.parse(v as String),
    ),
    cue: $checkedConvert(
      'cue',
      (v) => SocialCue.fromJson(v as Map<String, dynamic>),
    ),
    expected: $checkedConvert('expected', (v) => v as num),
    observed: $checkedConvert('observed', (v) => v as num),
    platforms: $checkedConvert(
      'platforms',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    change: $checkedConvert(
      'change',
      (v) => v == null
          ? null
          : PresenceEventChange.fromJson(v as Map<String, dynamic>),
    ),
    bindingId: $checkedConvert('bindingId', (v) => v as String),
    ruleId: $checkedConvert('ruleId', (v) => v as String),
    ruleName: $checkedConvert('ruleName', (v) => v as String?),
    firedAt: $checkedConvert('firedAt', (v) => DateTime.parse(v as String)),
    acknowledgedAt: $checkedConvert(
      'acknowledgedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    enrichedAt: $checkedConvert(
      'enrichedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    record: $checkedConvert(
      'record',
      (v) => v == null ? null : Record.fromJson(v as Map<String, dynamic>),
    ),
    rev: $checkedConvert('rev', (v) => (v as num).toInt()),
    acknowledgedBy: $checkedConvert('acknowledgedBy', (v) => v as String?),
    resolvedAt: $checkedConvert(
      'resolvedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    resolvedBy: $checkedConvert('resolvedBy', (v) => v as String?),
    resolvedSeq: $checkedConvert('resolvedSeq', (v) => (v as num?)?.toInt()),
    feedback: $checkedConvert(
      'feedback',
      (v) =>
          $enumDecodeNullable(_$StreamEvents200ResponseFeedbackEnumEnumMap, v),
    ),
    feedbackAt: $checkedConvert(
      'feedbackAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    note: $checkedConvert(
      'note',
      (v) => AnalystNote.fromJson(v as Map<String, dynamic>),
    ),
    snoozedUntil: $checkedConvert(
      'snoozedUntil',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    suppressedReason: $checkedConvert(
      'suppressedReason',
      (v) => $enumDecodeNullable(
        _$StreamEvents200ResponseSuppressedReasonEnumEnumMap,
        v,
      ),
    ),
    recordSnapshot: $checkedConvert(
      'recordSnapshot',
      (v) => v == null
          ? null
          : AlertRecordSnapshot.fromJson(v as Map<String, dynamic>),
    ),
    summaryCitations: $checkedConvert(
      'summaryCitations',
      (v) => (v as List<dynamic>?)
          ?.map((e) => AskCitation.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    sensitive: $checkedConvert('sensitive', (v) => v as bool?),
    region: $checkedConvert('region', (v) => v as String),
    context: $checkedConvert(
      'context',
      (v) =>
          v == null ? null : AlertContext.fromJson(v as Map<String, dynamic>),
    ),
    exerciseRunId: $checkedConvert('exerciseRunId', (v) => v as String?),
    signals: $checkedConvert(
      'signals',
      (v) => (v as List<dynamic>?)
          ?.map((e) => AlertSignal.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    unread: $checkedConvert('unread', (v) => (v as num).toInt()),
    unreadCritical: $checkedConvert(
      'unreadCritical',
      (v) => (v as num).toInt(),
    ),
    latestId: $checkedConvert('latestId', (v) => (v as num?)?.toInt()),
    eventId: $checkedConvert('eventId', (v) => v as String),
    proposal: $checkedConvert(
      'proposal',
      (v) => AnalystProposal.fromJson(v as Map<String, dynamic>),
    ),
    situation: $checkedConvert(
      'situation',
      (v) => SituationSummary.fromJson(v as Map<String, dynamic>),
    ),
    previousSeverity: $checkedConvert(
      'previousSeverity',
      (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
    ),
    finding: $checkedConvert(
      'finding',
      (v) => FindingEventFinding.fromJson(v as Map<String, dynamic>),
    ),
    precursor: $checkedConvert(
      'precursor',
      (v) => PrecursorEventPrecursor.fromJson(v as Map<String, dynamic>),
    ),
    question: $checkedConvert('question', (v) => v as String),
    questionId: $checkedConvert('questionId', (v) => v as String?),
    forecastId: $checkedConvert('forecastId', (v) => v as String),
    forecasterId: $checkedConvert('forecasterId', (v) => v as String),
    madeAt: $checkedConvert('madeAt', (v) => DateTime.parse(v as String)),
    probability: $checkedConvert('probability', (v) => v as num?),
    estimativeTerm: $checkedConvert(
      'estimativeTerm',
      (v) => $enumDecodeNullable(_$IcdTermEnumMap, v),
    ),
    private: $checkedConvert('private', (v) => v as bool?),
    setId: $checkedConvert('setId', (v) => v as String),
    revisionId: $checkedConvert('revisionId', (v) => v as String?),
    cause: $checkedConvert('cause', (v) => v),
    hypotheses: $checkedConvert(
      'hypotheses',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) =>
                HypothesisProbabilityState.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    bandChanges: $checkedConvert(
      'bandChanges',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => HypothesisSetEventBandChangesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    indicatorId: $checkedConvert('indicatorId', (v) => v as String),
    description: $checkedConvert('description', (v) => v as String?),
    signalFamily: $checkedConvert('signalFamily', (v) => v as String?),
    evidence: $checkedConvert('evidence', (v) => v),
    assumptionId: $checkedConvert('assumptionId', (v) => v as String),
    text: $checkedConvert('text', (v) => v as String?),
    linchpin: $checkedConvert('linchpin', (v) => v as bool?),
    publishedAt: $checkedConvert(
      'publishedAt',
      (v) => DateTime.parse(v as String),
    ),
    windows: $checkedConvert(
      'windows',
      (v) => (v as List<dynamic>?)
          ?.map((e) => OutlookWindow.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    recordRef: $checkedConvert('recordRef', (v) => v as String?),
    indexId: $checkedConvert('indexId', (v) => v as String),
    day: $checkedConvert('day', (v) => DateTime.parse(v as String)),
    value: $checkedConvert('value', (v) => v as num),
    previousValue: $checkedConvert('previousValue', (v) => v as num?),
    delta24h: $checkedConvert('delta24h', (v) => v as num?),
    delta7d: $checkedConvert('delta7d', (v) => v as num?),
    weightsOrigin: $checkedConvert(
      'weightsOrigin',
      (v) => $enumDecodeNullable(
        _$StreamEvents200ResponseWeightsOriginEnumEnumMap,
        v,
      ),
    ),
    fitId: $checkedConvert('fitId', (v) => v as String),
    weightsVersion: $checkedConvert(
      'weightsVersion',
      (v) => (v as num).toInt(),
    ),
    proposedWeights: $checkedConvert(
      'proposedWeights',
      (v) => Map<String, num>.from(v as Map),
    ),
    skill: $checkedConvert(
      'skill',
      (v) => v == null
          ? null
          : RegionalIndexDefSkill.fromJson(v as Map<String, dynamic>),
    ),
    autoApplied: $checkedConvert('autoApplied', (v) => v as bool?),
    hazardType: $checkedConvert(
      'hazardType',
      (v) => $enumDecode(_$StreamEvents200ResponseHazardTypeEnumEnumMap, v),
    ),
    sourceRef: $checkedConvert('sourceRef', (v) => v as String?),
    impactFamily: $checkedConvert('impactFamily', (v) => v as String),
    admin1: $checkedConvert('admin1', (v) => v as String),
    pImpact: $checkedConvert('pImpact', (v) => v as num),
    supersededBy: $checkedConvert('supersededBy', (v) => v as String?),
    assessmentId: $checkedConvert('assessmentId', (v) => v as String),
    cycleId: $checkedConvert('cycleId', (v) => v as String?),
    situationId: $checkedConvert('situationId', (v) => v as String?),
    cadence: $checkedConvert(
      'cadence',
      (v) => $enumDecode(_$StreamEvents200ResponseCadenceEnumEnumMap, v),
    ),
    periodStart: $checkedConvert(
      'periodStart',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    periodEnd: $checkedConvert(
      'periodEnd',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    headline: $checkedConvert('headline', (v) => v as String?),
    runId: $checkedConvert('runId', (v) => v as String),
    workspaceId: $checkedConvert('workspaceId', (v) => v as String),
    patch_: $checkedConvert(
      'patch',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
    originClientId: $checkedConvert('originClientId', (v) => v as String?),
    principal: $checkedConvert('principal', (v) => v as String?),
    clients: $checkedConvert(
      'clients',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                PresenceEventClientsInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source', 'patch_': 'patch'});

Map<String, dynamic> _$StreamEvents200ResponseToJson(
  StreamEvents200Response instance,
) => <String, dynamic>{
  'streamId': instance.streamId,
  'topics': instance.topics,
  'watermarkSeq': instance.watermarkSeq,
  'filterVersion': instance.filterVersion,
  'streamKey': ?instance.streamKey,
  'seq': instance.seq,
  'source': instance.source_,
  'externalId': instance.externalId,
  'kind': instance.kind,
  'version': instance.version,
  'observedAt': instance.observedAt.toIso8601String(),
  'tombstone': instance.tombstone,
  'geometry': ?instance.geometry?.toJson(),
  'payload': ?instance.payload,
  'prevSeq': ?instance.prevSeq,
  'historyTier': ?_$HistoryTierEnumMap[instance.historyTier],
  'persistReason': ?_$PersistReasonEnumMap[instance.persistReason],
  'eventTime': instance.eventTime.toIso8601String(),
  'validUntil': ?instance.validUntil?.toIso8601String(),
  'qaFlags': ?instance.qaFlags,
  'bbox': ?instance.bbox,
  'prevPersistedBbox': ?instance.prevPersistedBbox,
  'prevLiveBbox': ?instance.prevLiveBbox,
  'leftFilter': ?instance.leftFilter,
  'purged': ?instance.purged,
  'validFrom': ?instance.validFrom?.toIso8601String(),
  'globalRef': ?instance.globalRef,
  'sourceId': instance.sourceId,
  'prevBbox': ?instance.prevBbox,
  'properties': ?instance.properties,
  'quality': ?instance.quality?.toJson(),
  'reason': instance.reason,
  'type': _$StreamEvents200ResponseTypeEnumEnumMap[instance.type]!,
  'alert': instance.alert.toJson(),
  'suppressed': ?instance.suppressed,
  'exercise': ?instance.exercise,
  'id': instance.id,
  'event': _$StreamEvents200ResponseEventEnumEnumMap[instance.event]!,
  'subtype': instance.subtype,
  'title': instance.title,
  'shortTitle': ?instance.shortTitle,
  'bodyMarkdown': ?instance.bodyMarkdown,
  'severity': _$StreamEvents200ResponseSeverityEnumEnumMap[instance.severity]!,
  'url': ?instance.url,
  'links': ?instance.links?.map((e) => e.toJson()).toList(),
  'data': ?instance.data,
  'tags': ?instance.tags,
  'attachments': ?instance.attachments?.map((e) => e.toJson()).toList(),
  'updateOf': ?instance.updateOf,
  'origin': ?instance.origin,
  'createdAt': ?instance.createdAt?.toIso8601String(),
  'atSeq': instance.atSeq,
  'resumeAfter': ?instance.resumeAfter,
  'canonicalEventId': instance.canonicalEventId,
  'ref': instance.ref,
  'family': instance.family,
  'status': _$StreamEvents200ResponseStatusEnumEnumMap[instance.status]!,
  'regionCodes': ?instance.regionCodes,
  'observationCount': instance.observationCount,
  'evidenceGroupCount': instance.evidenceGroupCount,
  'qualityFlags': instance.qualityFlags
      .map((e) => _$StreamEvents200ResponseQualityFlagsEnumEnumMap[e]!)
      .toList(),
  'flagsAdded': ?instance.flagsAdded
      ?.map((e) => _$StreamEvents200ResponseFlagsAddedEnumEnumMap[e]!)
      .toList(),
  'mergedInto': ?instance.mergedInto,
  'mergedFrom': ?instance.mergedFrom,
  'envelope': ?instance.envelope?.toJson(),
  'role': _$StreamEvents200ResponseRoleEnumEnumMap[instance.role]!,
  'sharedBy': instance.sharedBy.toJson(),
  'message': instance.message,
  'at': instance.at.toIso8601String(),
  'annotationId': instance.annotationId,
  'parentId': ?instance.parentId,
  'targetRef': instance.targetRef,
  'author': instance.author.toJson(),
  'excerpt': ?instance.excerpt,
  'savedSearchId': instance.savedSearchId,
  'name': instance.name,
  'newMatches': instance.newMatches,
  'refs': ?instance.refs,
  'reportRef': instance.reportRef,
  'tlp': _$TlpEnumMap[instance.tlp]!,
  'revision': instance.revision,
  'from': ?instance.from?.toJson(),
  'receivedId': instance.receivedId,
  'packageId': instance.packageId,
  'senderInstanceId': instance.senderInstanceId,
  'signatureStatus':
      ?_$StreamEvents200ResponseSignatureStatusEnumEnumMap[instance
          .signatureStatus],
  'chronicleId': instance.chronicleId,
  'link': instance.link.toJson(),
  'requestId': instance.requestId,
  'linkId': instance.linkId,
  'requesterHandle': ?instance.requesterHandle,
  'layerId': ?instance.layerId,
  'summary': instance.summary.toJson(),
  'snapshot': ?instance.snapshot,
  'gap': ?instance.gap?.toJson(),
  'backfilled': ?instance.backfilled,
  'previousStatus':
      ?_$StreamEvents200ResponsePreviousStatusEnumEnumMap[instance
          .previousStatus],
  'capState': ?_$StreamEvents200ResponseCapStateEnumEnumMap[instance.capState],
  'bytesUsed': ?instance.bytesUsed,
  'findingId': instance.findingId,
  'scope': instance.scope.toJson(),
  'platform': instance.platform,
  'language': ?instance.language,
  'velocityRatio': instance.velocityRatio,
  'uniqueAuthors': instance.uniqueAuthors,
  'zRobust': ?instance.zRobust,
  'detectedAt': instance.detectedAt.toIso8601String(),
  'platformId': _$SocialPlatformIdEnumMap[instance.platformId]!,
  'state': _$StreamEvents200ResponseStateEnumEnumMap[instance.state]!,
  'previousState':
      ?_$StreamEvents200ResponsePreviousStateEnumEnumMap[instance
          .previousState],
  'spend': ?instance.spend?.toJson(),
  'narrative': instance.narrative.toJson(),
  'changed': ?instance.changed
      ?.map((e) => _$StreamEvents200ResponseChangedEnumEnumMap[e]!)
      .toList(),
  'claim': instance.claim.toJson(),
  'fingerprintId': instance.fingerprintId,
  'narrativeId': instance.narrativeId,
  'earlierNarrativeId': ?instance.earlierNarrativeId,
  'firstSeenAt': instance.firstSeenAt.toIso8601String(),
  'significantIndicators': instance.significantIndicators
      .map((e) => _$StreamEvents200ResponseSignificantIndicatorsEnumEnumMap[e]!)
      .toList(),
  'comparisonId': instance.comparisonId,
  'claimId': instance.claimId,
  'evaluatedAt': instance.evaluatedAt.toIso8601String(),
  'cue': instance.cue.toJson(),
  'expected': instance.expected,
  'observed': instance.observed,
  'platforms': instance.platforms,
  'change': instance.change?.toJson(),
  'bindingId': instance.bindingId,
  'ruleId': instance.ruleId,
  'ruleName': ?instance.ruleName,
  'firedAt': instance.firedAt.toIso8601String(),
  'acknowledgedAt': ?instance.acknowledgedAt?.toIso8601String(),
  'enrichedAt': ?instance.enrichedAt?.toIso8601String(),
  'record': ?instance.record?.toJson(),
  'rev': instance.rev,
  'acknowledgedBy': ?instance.acknowledgedBy,
  'resolvedAt': ?instance.resolvedAt?.toIso8601String(),
  'resolvedBy': ?instance.resolvedBy,
  'resolvedSeq': ?instance.resolvedSeq,
  'feedback': ?_$StreamEvents200ResponseFeedbackEnumEnumMap[instance.feedback],
  'feedbackAt': ?instance.feedbackAt?.toIso8601String(),
  'note': instance.note.toJson(),
  'snoozedUntil': ?instance.snoozedUntil?.toIso8601String(),
  'suppressedReason':
      ?_$StreamEvents200ResponseSuppressedReasonEnumEnumMap[instance
          .suppressedReason],
  'recordSnapshot': ?instance.recordSnapshot?.toJson(),
  'summaryCitations': ?instance.summaryCitations
      ?.map((e) => e.toJson())
      .toList(),
  'sensitive': ?instance.sensitive,
  'region': instance.region,
  'context': ?instance.context?.toJson(),
  'exerciseRunId': ?instance.exerciseRunId,
  'signals': ?instance.signals?.map((e) => e.toJson()).toList(),
  'unread': instance.unread,
  'unreadCritical': instance.unreadCritical,
  'latestId': ?instance.latestId,
  'eventId': instance.eventId,
  'proposal': instance.proposal.toJson(),
  'situation': instance.situation.toJson(),
  'previousSeverity': ?_$SeverityEnumMap[instance.previousSeverity],
  'finding': instance.finding.toJson(),
  'precursor': instance.precursor.toJson(),
  'question': instance.question,
  'questionId': instance.questionId,
  'forecastId': instance.forecastId,
  'forecasterId': instance.forecasterId,
  'madeAt': instance.madeAt.toIso8601String(),
  'probability': ?instance.probability,
  'estimativeTerm': ?_$IcdTermEnumMap[instance.estimativeTerm],
  'private': ?instance.private,
  'setId': instance.setId,
  'revisionId': ?instance.revisionId,
  'cause': ?instance.cause,
  'hypotheses': ?instance.hypotheses?.map((e) => e.toJson()).toList(),
  'bandChanges': ?instance.bandChanges?.map((e) => e.toJson()).toList(),
  'indicatorId': instance.indicatorId,
  'description': ?instance.description,
  'signalFamily': ?instance.signalFamily,
  'evidence': ?instance.evidence,
  'assumptionId': instance.assumptionId,
  'text': ?instance.text,
  'linchpin': ?instance.linchpin,
  'publishedAt': instance.publishedAt.toIso8601String(),
  'windows': ?instance.windows?.map((e) => e.toJson()).toList(),
  'recordRef': ?instance.recordRef,
  'indexId': instance.indexId,
  'day': instance.day.toIso8601String(),
  'value': instance.value,
  'previousValue': ?instance.previousValue,
  'delta24h': ?instance.delta24h,
  'delta7d': ?instance.delta7d,
  'weightsOrigin':
      ?_$StreamEvents200ResponseWeightsOriginEnumEnumMap[instance
          .weightsOrigin],
  'fitId': instance.fitId,
  'weightsVersion': instance.weightsVersion,
  'proposedWeights': instance.proposedWeights,
  'skill': ?instance.skill?.toJson(),
  'autoApplied': ?instance.autoApplied,
  'hazardType':
      _$StreamEvents200ResponseHazardTypeEnumEnumMap[instance.hazardType]!,
  'sourceRef': ?instance.sourceRef,
  'impactFamily': instance.impactFamily,
  'admin1': instance.admin1,
  'pImpact': instance.pImpact,
  'supersededBy': ?instance.supersededBy,
  'assessmentId': instance.assessmentId,
  'cycleId': ?instance.cycleId,
  'situationId': ?instance.situationId,
  'cadence': _$StreamEvents200ResponseCadenceEnumEnumMap[instance.cadence]!,
  'periodStart': ?instance.periodStart?.toIso8601String(),
  'periodEnd': ?instance.periodEnd?.toIso8601String(),
  'headline': ?instance.headline,
  'runId': instance.runId,
  'workspaceId': instance.workspaceId,
  'patch': instance.patch_,
  'originClientId': ?instance.originClientId,
  'principal': ?instance.principal,
  'clients': instance.clients.map((e) => e.toJson()).toList(),
};

const _$HistoryTierEnumMap = {
  HistoryTier.STANDARD: 'STANDARD',
  HistoryTier.TRACK: 'TRACK',
};

const _$PersistReasonEnumMap = {
  PersistReason.FIRST: 'FIRST',
  PersistReason.REAPPEAR: 'REAPPEAR',
  PersistReason.SIGNIFICANT: 'SIGNIFICANT',
  PersistReason.TOMBSTONE: 'TOMBSTONE',
  PersistReason.CHANGE: 'CHANGE',
  PersistReason.SAMPLE: 'SAMPLE',
  PersistReason.KEYFRAME: 'KEYFRAME',
  PersistReason.HEARTBEAT: 'HEARTBEAT',
  PersistReason.TRIGGER_EDGE: 'TRIGGER_EDGE',
};

const _$StreamEvents200ResponseTypeEnumEnumMap = {
  StreamEvents200ResponseTypeEnum.fired: 'fired',
  StreamEvents200ResponseTypeEnum.updated: 'updated',
  StreamEvents200ResponseTypeEnum.acknowledged: 'acknowledged',
  StreamEvents200ResponseTypeEnum.snoozed: 'snoozed',
  StreamEvents200ResponseTypeEnum.resolved: 'resolved',
  StreamEvents200ResponseTypeEnum.report: 'report',
  StreamEvents200ResponseTypeEnum.investigation: 'investigation',
  StreamEvents200ResponseTypeEnum.chronicleExtract: 'chronicle-extract',
  StreamEvents200ResponseTypeEnum.evidence: 'evidence',
  StreamEvents200ResponseTypeEnum.alert: 'alert',
  StreamEvents200ResponseTypeEnum.finding: 'finding',
  StreamEvents200ResponseTypeEnum.situationChange: 'situation-change',
  StreamEvents200ResponseTypeEnum.hypothesisBandChange:
      'hypothesis-band-change',
  StreamEvents200ResponseTypeEnum.forecastResolution: 'forecast-resolution',
  StreamEvents200ResponseTypeEnum.firstAppearance: 'first-appearance',
  StreamEvents200ResponseTypeEnum.layerEdit: 'layer-edit',
  StreamEvents200ResponseTypeEnum.captureGap: 'capture-gap',
  StreamEvents200ResponseTypeEnum.capWarning: 'cap-warning',
  StreamEvents200ResponseTypeEnum.capFallback: 'cap-fallback',
  StreamEvents200ResponseTypeEnum.statusChange: 'status-change',
  StreamEvents200ResponseTypeEnum.backfill: 'backfill',
  StreamEvents200ResponseTypeEnum.narrativeChange: 'narrative-change',
  StreamEvents200ResponseTypeEnum.comparisonChange: 'comparison-change',
};

const _$StreamEvents200ResponseEventEnumEnumMap = {
  StreamEvents200ResponseEventEnum.forecastPeriodSubmitted:
      'forecast.submitted',
};

const _$StreamEvents200ResponseSeverityEnumEnumMap = {
  StreamEvents200ResponseSeverityEnum.info: 'info',
  StreamEvents200ResponseSeverityEnum.warning: 'warning',
  StreamEvents200ResponseSeverityEnum.critical: 'critical',
};

const _$StreamEvents200ResponseStatusEnumEnumMap = {
  StreamEvents200ResponseStatusEnum.ok: 'ok',
  StreamEvents200ResponseStatusEnum.partial: 'partial',
  StreamEvents200ResponseStatusEnum.digestOnly: 'digest-only',
  StreamEvents200ResponseStatusEnum.failed: 'failed',
};

const _$StreamEvents200ResponseQualityFlagsEnumEnumMap = {
  StreamEvents200ResponseQualityFlagsEnum.ECHO: 'ECHO',
  StreamEvents200ResponseQualityFlagsEnum.TIMESTAMP_CONFLICT:
      'TIMESTAMP_CONFLICT',
  StreamEvents200ResponseQualityFlagsEnum.SINGLE_ORIGINATOR:
      'SINGLE_ORIGINATOR',
  StreamEvents200ResponseQualityFlagsEnum.LATE_REVISION: 'LATE_REVISION',
};

const _$StreamEvents200ResponseFlagsAddedEnumEnumMap = {
  StreamEvents200ResponseFlagsAddedEnum.ECHO: 'ECHO',
  StreamEvents200ResponseFlagsAddedEnum.TIMESTAMP_CONFLICT:
      'TIMESTAMP_CONFLICT',
  StreamEvents200ResponseFlagsAddedEnum.SINGLE_ORIGINATOR: 'SINGLE_ORIGINATOR',
  StreamEvents200ResponseFlagsAddedEnum.LATE_REVISION: 'LATE_REVISION',
};

const _$StreamEvents200ResponseRoleEnumEnumMap = {
  StreamEvents200ResponseRoleEnum.viewer: 'viewer',
  StreamEvents200ResponseRoleEnum.commenter: 'commenter',
  StreamEvents200ResponseRoleEnum.editor: 'editor',
  StreamEvents200ResponseRoleEnum.owner: 'owner',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};

const _$StreamEvents200ResponseSignatureStatusEnumEnumMap = {
  StreamEvents200ResponseSignatureStatusEnum.valid: 'valid',
  StreamEvents200ResponseSignatureStatusEnum.invalid: 'invalid',
  StreamEvents200ResponseSignatureStatusEnum.unknownKey: 'unknown-key',
};

const _$StreamEvents200ResponsePreviousStatusEnumEnumMap = {
  StreamEvents200ResponsePreviousStatusEnum.solid: 'solid',
  StreamEvents200ResponsePreviousStatusEnum.caveated: 'caveated',
  StreamEvents200ResponsePreviousStatusEnum.unsupported: 'unsupported',
  StreamEvents200ResponsePreviousStatusEnum.broken: 'broken',
};

const _$StreamEvents200ResponseCapStateEnumEnumMap = {
  StreamEvents200ResponseCapStateEnum.ok: 'ok',
  StreamEvents200ResponseCapStateEnum.warning: 'warning',
  StreamEvents200ResponseCapStateEnum.exceeded: 'exceeded',
};

const _$SocialPlatformIdEnumMap = {
  SocialPlatformId.blueskyJetstream: 'bluesky-jetstream',
  SocialPlatformId.blueskySearch: 'bluesky-search',
  SocialPlatformId.mastodon: 'mastodon',
  SocialPlatformId.reddit: 'reddit',
  SocialPlatformId.youtube: 'youtube',
  SocialPlatformId.x: 'x',
  SocialPlatformId.threads: 'threads',
  SocialPlatformId.instagramHashtags: 'instagram-hashtags',
};

const _$StreamEvents200ResponseStateEnumEnumMap = {
  StreamEvents200ResponseStateEnum.pending: 'pending',
  StreamEvents200ResponseStateEnum.emerging: 'emerging',
  StreamEvents200ResponseStateEnum.observed: 'observed',
  StreamEvents200ResponseStateEnum.absent: 'absent',
  StreamEvents200ResponseStateEnum.unknownCoverage: 'unknown-coverage',
  StreamEvents200ResponseStateEnum.retired: 'retired',
  StreamEvents200ResponseStateEnum.reported: 'reported',
};

const _$StreamEvents200ResponsePreviousStateEnumEnumMap = {
  StreamEvents200ResponsePreviousStateEnum.pending: 'pending',
  StreamEvents200ResponsePreviousStateEnum.emerging: 'emerging',
  StreamEvents200ResponsePreviousStateEnum.observed: 'observed',
  StreamEvents200ResponsePreviousStateEnum.absent: 'absent',
  StreamEvents200ResponsePreviousStateEnum.unknownCoverage: 'unknown-coverage',
  StreamEvents200ResponsePreviousStateEnum.retired: 'retired',
  StreamEvents200ResponsePreviousStateEnum.reported: 'reported',
};

const _$StreamEvents200ResponseChangedEnumEnumMap = {
  StreamEvents200ResponseChangedEnum.state: 'state',
  StreamEvents200ResponseChangedEnum.status: 'status',
  StreamEvents200ResponseChangedEnum.postCount: 'postCount',
  StreamEvents200ResponseChangedEnum.evidenceGroupCount: 'evidenceGroupCount',
  StreamEvents200ResponseChangedEnum.velocity: 'velocity',
  StreamEvents200ResponseChangedEnum.merged: 'merged',
  StreamEvents200ResponseChangedEnum.split: 'split',
  StreamEvents200ResponseChangedEnum.title: 'title',
};

const _$StreamEvents200ResponseSignificantIndicatorsEnumEnumMap = {
  StreamEvents200ResponseSignificantIndicatorsEnum.synchrony: 'synchrony',
  StreamEvents200ResponseSignificantIndicatorsEnum.textDuplication:
      'textDuplication',
  StreamEvents200ResponseSignificantIndicatorsEnum.accountAgeSkew:
      'accountAgeSkew',
  StreamEvents200ResponseSignificantIndicatorsEnum.coRepostBursts:
      'coRepostBursts',
};

const _$StreamEvents200ResponseFeedbackEnumEnumMap = {
  StreamEvents200ResponseFeedbackEnum.useful: 'useful',
  StreamEvents200ResponseFeedbackEnum.noise: 'noise',
  StreamEvents200ResponseFeedbackEnum.late_: 'late',
  StreamEvents200ResponseFeedbackEnum.falsePositive: 'false_positive',
};

const _$StreamEvents200ResponseSuppressedReasonEnumEnumMap = {
  StreamEvents200ResponseSuppressedReasonEnum.alertSnoozed: 'alert_snoozed',
  StreamEvents200ResponseSuppressedReasonEnum.ruleSnoozed: 'rule_snoozed',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};

const _$IcdTermEnumMap = {
  IcdTerm.almostNoChance: 'almost no chance',
  IcdTerm.veryUnlikely: 'very unlikely',
  IcdTerm.unlikely: 'unlikely',
  IcdTerm.roughlyEvenChance: 'roughly even chance',
  IcdTerm.likely: 'likely',
  IcdTerm.veryLikely: 'very likely',
  IcdTerm.almostCertain: 'almost certain',
};

const _$StreamEvents200ResponseWeightsOriginEnumEnumMap = {
  StreamEvents200ResponseWeightsOriginEnum.default_: 'default',
  StreamEvents200ResponseWeightsOriginEnum.fitted: 'fitted',
  StreamEvents200ResponseWeightsOriginEnum.operator_: 'operator',
};

const _$StreamEvents200ResponseHazardTypeEnumEnumMap = {
  StreamEvents200ResponseHazardTypeEnum.tropicalWind34: 'tropical-wind-34',
  StreamEvents200ResponseHazardTypeEnum.tropicalWind50: 'tropical-wind-50',
  StreamEvents200ResponseHazardTypeEnum.tropicalWind64: 'tropical-wind-64',
  StreamEvents200ResponseHazardTypeEnum.tropicalTrack: 'tropical-track',
  StreamEvents200ResponseHazardTypeEnum.tornado: 'tornado',
  StreamEvents200ResponseHazardTypeEnum.wind: 'wind',
  StreamEvents200ResponseHazardTypeEnum.hail: 'hail',
  StreamEvents200ResponseHazardTypeEnum.severeCategorical: 'severe-categorical',
  StreamEvents200ResponseHazardTypeEnum.excessiveRain: 'excessive-rain',
  StreamEvents200ResponseHazardTypeEnum.riverFlood: 'river-flood',
  StreamEvents200ResponseHazardTypeEnum.stormCell: 'storm-cell',
};

const _$StreamEvents200ResponseCadenceEnumEnumMap = {
  StreamEvents200ResponseCadenceEnum.daily: 'daily',
  StreamEvents200ResponseCadenceEnum.weekly: 'weekly',
  StreamEvents200ResponseCadenceEnum.monthly: 'monthly',
  StreamEvents200ResponseCadenceEnum.onDemand: 'on-demand',
};
