// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_delivery.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationDeliveryCWProxy {
  NotificationDelivery id(int id);

  NotificationDelivery event(NotificationDeliveryEventEnum event);

  NotificationDelivery alertId(int? alertId);

  NotificationDelivery briefingRunId(String? briefingRunId);

  NotificationDelivery status(NotificationDeliveryStatusEnum status);

  NotificationDelivery attempts(int attempts);

  NotificationDelivery lastError(String? lastError);

  NotificationDelivery nextAttemptAt(DateTime? nextAttemptAt);

  NotificationDelivery deliveredAt(DateTime? deliveredAt);

  NotificationDelivery notificationId(int? notificationId);

  NotificationDelivery kind(NotificationDeliveryKindEnum? kind);

  NotificationDelivery deferredReason(
    NotificationDeliveryDeferredReasonEnum? deferredReason,
  );

  NotificationDelivery edited(bool? edited);

  NotificationDelivery note(String? note);

  NotificationDelivery escalationStep(int? escalationStep);

  NotificationDelivery attachmentLevel(
    NotificationDeliveryAttachmentLevelEnum? attachmentLevel,
  );

  NotificationDelivery attachments(List<Attachment>? attachments);

  NotificationDelivery shortCode(String? shortCode);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationDelivery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationDelivery(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationDelivery call({
    int id,
    NotificationDeliveryEventEnum event,
    int? alertId,
    String? briefingRunId,
    NotificationDeliveryStatusEnum status,
    int attempts,
    String? lastError,
    DateTime? nextAttemptAt,
    DateTime? deliveredAt,
    int? notificationId,
    NotificationDeliveryKindEnum? kind,
    NotificationDeliveryDeferredReasonEnum? deferredReason,
    bool? edited,
    String? note,
    int? escalationStep,
    NotificationDeliveryAttachmentLevelEnum? attachmentLevel,
    List<Attachment>? attachments,
    String? shortCode,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationDelivery.copyWith(...)` or call `instanceOfNotificationDelivery.copyWith.fieldName(value)` for a single field.
class _$NotificationDeliveryCWProxyImpl
    implements _$NotificationDeliveryCWProxy {
  const _$NotificationDeliveryCWProxyImpl(this._value);

  final NotificationDelivery _value;

  @override
  NotificationDelivery id(int id) => call(id: id);

  @override
  NotificationDelivery event(NotificationDeliveryEventEnum event) =>
      call(event: event);

  @override
  NotificationDelivery alertId(int? alertId) => call(alertId: alertId);

  @override
  NotificationDelivery briefingRunId(String? briefingRunId) =>
      call(briefingRunId: briefingRunId);

  @override
  NotificationDelivery status(NotificationDeliveryStatusEnum status) =>
      call(status: status);

  @override
  NotificationDelivery attempts(int attempts) => call(attempts: attempts);

  @override
  NotificationDelivery lastError(String? lastError) =>
      call(lastError: lastError);

  @override
  NotificationDelivery nextAttemptAt(DateTime? nextAttemptAt) =>
      call(nextAttemptAt: nextAttemptAt);

  @override
  NotificationDelivery deliveredAt(DateTime? deliveredAt) =>
      call(deliveredAt: deliveredAt);

  @override
  NotificationDelivery notificationId(int? notificationId) =>
      call(notificationId: notificationId);

  @override
  NotificationDelivery kind(NotificationDeliveryKindEnum? kind) =>
      call(kind: kind);

  @override
  NotificationDelivery deferredReason(
    NotificationDeliveryDeferredReasonEnum? deferredReason,
  ) => call(deferredReason: deferredReason);

  @override
  NotificationDelivery edited(bool? edited) => call(edited: edited);

  @override
  NotificationDelivery note(String? note) => call(note: note);

  @override
  NotificationDelivery escalationStep(int? escalationStep) =>
      call(escalationStep: escalationStep);

  @override
  NotificationDelivery attachmentLevel(
    NotificationDeliveryAttachmentLevelEnum? attachmentLevel,
  ) => call(attachmentLevel: attachmentLevel);

  @override
  NotificationDelivery attachments(List<Attachment>? attachments) =>
      call(attachments: attachments);

  @override
  NotificationDelivery shortCode(String? shortCode) =>
      call(shortCode: shortCode);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationDelivery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationDelivery(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationDelivery call({
    Object? id = const $CopyWithPlaceholder(),
    Object? event = const $CopyWithPlaceholder(),
    Object? alertId = const $CopyWithPlaceholder(),
    Object? briefingRunId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? attempts = const $CopyWithPlaceholder(),
    Object? lastError = const $CopyWithPlaceholder(),
    Object? nextAttemptAt = const $CopyWithPlaceholder(),
    Object? deliveredAt = const $CopyWithPlaceholder(),
    Object? notificationId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? deferredReason = const $CopyWithPlaceholder(),
    Object? edited = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
    Object? escalationStep = const $CopyWithPlaceholder(),
    Object? attachmentLevel = const $CopyWithPlaceholder(),
    Object? attachments = const $CopyWithPlaceholder(),
    Object? shortCode = const $CopyWithPlaceholder(),
  }) {
    return NotificationDelivery(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as NotificationDeliveryEventEnum,
      alertId: alertId == const $CopyWithPlaceholder()
          ? _value.alertId
          // ignore: cast_nullable_to_non_nullable
          : alertId as int?,
      briefingRunId: briefingRunId == const $CopyWithPlaceholder()
          ? _value.briefingRunId
          // ignore: cast_nullable_to_non_nullable
          : briefingRunId as String?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as NotificationDeliveryStatusEnum,
      attempts: attempts == const $CopyWithPlaceholder() || attempts == null
          ? _value.attempts
          // ignore: cast_nullable_to_non_nullable
          : attempts as int,
      lastError: lastError == const $CopyWithPlaceholder()
          ? _value.lastError
          // ignore: cast_nullable_to_non_nullable
          : lastError as String?,
      nextAttemptAt: nextAttemptAt == const $CopyWithPlaceholder()
          ? _value.nextAttemptAt
          // ignore: cast_nullable_to_non_nullable
          : nextAttemptAt as DateTime?,
      deliveredAt: deliveredAt == const $CopyWithPlaceholder()
          ? _value.deliveredAt
          // ignore: cast_nullable_to_non_nullable
          : deliveredAt as DateTime?,
      notificationId: notificationId == const $CopyWithPlaceholder()
          ? _value.notificationId
          // ignore: cast_nullable_to_non_nullable
          : notificationId as int?,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as NotificationDeliveryKindEnum?,
      deferredReason: deferredReason == const $CopyWithPlaceholder()
          ? _value.deferredReason
          // ignore: cast_nullable_to_non_nullable
          : deferredReason as NotificationDeliveryDeferredReasonEnum?,
      edited: edited == const $CopyWithPlaceholder()
          ? _value.edited
          // ignore: cast_nullable_to_non_nullable
          : edited as bool?,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
      escalationStep: escalationStep == const $CopyWithPlaceholder()
          ? _value.escalationStep
          // ignore: cast_nullable_to_non_nullable
          : escalationStep as int?,
      attachmentLevel: attachmentLevel == const $CopyWithPlaceholder()
          ? _value.attachmentLevel
          // ignore: cast_nullable_to_non_nullable
          : attachmentLevel as NotificationDeliveryAttachmentLevelEnum?,
      attachments: attachments == const $CopyWithPlaceholder()
          ? _value.attachments
          // ignore: cast_nullable_to_non_nullable
          : attachments as List<Attachment>?,
      shortCode: shortCode == const $CopyWithPlaceholder()
          ? _value.shortCode
          // ignore: cast_nullable_to_non_nullable
          : shortCode as String?,
    );
  }
}

extension $NotificationDeliveryCopyWith on NotificationDelivery {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationDelivery.copyWith(...)` or `instanceOfNotificationDelivery.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationDeliveryCWProxy get copyWith =>
      _$NotificationDeliveryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationDelivery _$NotificationDeliveryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationDelivery', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'event', 'status', 'attempts']);
  final val = NotificationDelivery(
    id: $checkedConvert('id', (v) => (v as num).toInt()),
    event: $checkedConvert(
      'event',
      (v) => $enumDecode(_$NotificationDeliveryEventEnumEnumMap, v),
    ),
    alertId: $checkedConvert('alertId', (v) => (v as num?)?.toInt()),
    briefingRunId: $checkedConvert('briefingRunId', (v) => v as String?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$NotificationDeliveryStatusEnumEnumMap, v),
    ),
    attempts: $checkedConvert('attempts', (v) => (v as num).toInt()),
    lastError: $checkedConvert('lastError', (v) => v as String?),
    nextAttemptAt: $checkedConvert(
      'nextAttemptAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    deliveredAt: $checkedConvert(
      'deliveredAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    notificationId: $checkedConvert(
      'notificationId',
      (v) => (v as num?)?.toInt(),
    ),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecodeNullable(_$NotificationDeliveryKindEnumEnumMap, v),
    ),
    deferredReason: $checkedConvert(
      'deferredReason',
      (v) => $enumDecodeNullable(
        _$NotificationDeliveryDeferredReasonEnumEnumMap,
        v,
      ),
    ),
    edited: $checkedConvert('edited', (v) => v as bool?),
    note: $checkedConvert('note', (v) => v as String?),
    escalationStep: $checkedConvert(
      'escalationStep',
      (v) => (v as num?)?.toInt(),
    ),
    attachmentLevel: $checkedConvert(
      'attachmentLevel',
      (v) => $enumDecodeNullable(
        _$NotificationDeliveryAttachmentLevelEnumEnumMap,
        v,
      ),
    ),
    attachments: $checkedConvert(
      'attachments',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Attachment.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    shortCode: $checkedConvert('shortCode', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$NotificationDeliveryToJson(
  NotificationDelivery instance,
) => <String, dynamic>{
  'id': instance.id,
  'event': _$NotificationDeliveryEventEnumEnumMap[instance.event]!,
  'alertId': ?instance.alertId,
  'briefingRunId': ?instance.briefingRunId,
  'status': _$NotificationDeliveryStatusEnumEnumMap[instance.status]!,
  'attempts': instance.attempts,
  'lastError': ?instance.lastError,
  'nextAttemptAt': ?instance.nextAttemptAt?.toIso8601String(),
  'deliveredAt': ?instance.deliveredAt?.toIso8601String(),
  'notificationId': ?instance.notificationId,
  'kind': ?_$NotificationDeliveryKindEnumEnumMap[instance.kind],
  'deferredReason':
      ?_$NotificationDeliveryDeferredReasonEnumEnumMap[instance.deferredReason],
  'edited': ?instance.edited,
  'note': ?instance.note,
  'escalationStep': ?instance.escalationStep,
  'attachmentLevel':
      ?_$NotificationDeliveryAttachmentLevelEnumEnumMap[instance
          .attachmentLevel],
  'attachments': ?instance.attachments?.map((e) => e.toJson()).toList(),
  'shortCode': ?instance.shortCode,
};

const _$NotificationDeliveryEventEnumEnumMap = {
  NotificationDeliveryEventEnum.alert: 'alert',
  NotificationDeliveryEventEnum.briefing: 'briefing',
  NotificationDeliveryEventEnum.system: 'system',
  NotificationDeliveryEventEnum.socialPeriodDivergence: 'social.divergence',
  NotificationDeliveryEventEnum.socialPeriodAttentionGap:
      'social.attention_gap',
  NotificationDeliveryEventEnum.analystPeriodNote: 'analyst.note',
  NotificationDeliveryEventEnum.situationPeriodActivated: 'situation.activated',
  NotificationDeliveryEventEnum.situationPeriodEscalated: 'situation.escalated',
  NotificationDeliveryEventEnum.findingPeriodSignificant: 'finding.significant',
  NotificationDeliveryEventEnum.forecastPeriodResolved: 'forecast.resolved',
  NotificationDeliveryEventEnum.forecastPeriodResolutionNeeded:
      'forecast.resolution_needed',
  NotificationDeliveryEventEnum.hypothesisPeriodBandChanged:
      'hypothesis.band_changed',
  NotificationDeliveryEventEnum.indicatorPeriodObserved: 'indicator.observed',
  NotificationDeliveryEventEnum.assumptionPeriodBroken: 'assumption.broken',
  NotificationDeliveryEventEnum.assessmentPeriodPublished:
      'assessment.published',
  NotificationDeliveryEventEnum.chroniclePeriodProposed: 'chronicle.proposed',
  NotificationDeliveryEventEnum.chroniclePeriodCapWarning:
      'chronicle.cap_warning',
  NotificationDeliveryEventEnum.chroniclePeriodGap: 'chronicle.gap',
  NotificationDeliveryEventEnum.mention: 'mention',
  NotificationDeliveryEventEnum.share: 'share',
  NotificationDeliveryEventEnum.reportPeriodReceived: 'report.received',
  NotificationDeliveryEventEnum.packagePeriodReceived: 'package.received',
  NotificationDeliveryEventEnum.chroniclePeriodAccessPeriodRequested:
      'chronicle.access.requested',
  NotificationDeliveryEventEnum.savedSearchPeriodMatches:
      'saved-search.matches',
  NotificationDeliveryEventEnum.personal: 'personal',
};

const _$NotificationDeliveryStatusEnumEnumMap = {
  NotificationDeliveryStatusEnum.pending: 'pending',
  NotificationDeliveryStatusEnum.delivered: 'delivered',
  NotificationDeliveryStatusEnum.failed: 'failed',
  NotificationDeliveryStatusEnum.suppressed: 'suppressed',
  NotificationDeliveryStatusEnum.unacked: 'unacked',
};

const _$NotificationDeliveryKindEnumEnumMap = {
  NotificationDeliveryKindEnum.send: 'send',
  NotificationDeliveryKindEnum.update: 'update',
  NotificationDeliveryKindEnum.followup: 'followup',
  NotificationDeliveryKindEnum.digest: 'digest',
};

const _$NotificationDeliveryDeferredReasonEnumEnumMap = {
  NotificationDeliveryDeferredReasonEnum.quietHours: 'quiet_hours',
  NotificationDeliveryDeferredReasonEnum.paused: 'paused',
  NotificationDeliveryDeferredReasonEnum.enrichment: 'enrichment',
  NotificationDeliveryDeferredReasonEnum.grouping: 'grouping',
  NotificationDeliveryDeferredReasonEnum.presence: 'presence',
  NotificationDeliveryDeferredReasonEnum.airtime: 'airtime',
};

const _$NotificationDeliveryAttachmentLevelEnumEnumMap = {
  NotificationDeliveryAttachmentLevelEnum.map800: 'map-800',
  NotificationDeliveryAttachmentLevelEnum.map600: 'map-600',
  NotificationDeliveryAttachmentLevelEnum.thumbnail: 'thumbnail',
  NotificationDeliveryAttachmentLevelEnum.textOnly: 'text-only',
};
