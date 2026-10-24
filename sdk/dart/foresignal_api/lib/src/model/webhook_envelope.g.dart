// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'webhook_envelope.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WebhookEnvelopeCWProxy {
  WebhookEnvelope event(WebhookEnvelopeEventEnum event);

  WebhookEnvelope subtype(String subtype);

  WebhookEnvelope message(NotificationWebhookMessage message);

  WebhookEnvelope data(Map<String, Object>? data);

  WebhookEnvelope foresignalOrigin(String foresignalOrigin);

  WebhookEnvelope exercise(bool? exercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WebhookEnvelope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WebhookEnvelope(...).copyWith(id: 12, name: "My name")
  /// ```
  WebhookEnvelope call({
    WebhookEnvelopeEventEnum event,
    String subtype,
    NotificationWebhookMessage message,
    Map<String, Object>? data,
    String foresignalOrigin,
    bool? exercise,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWebhookEnvelope.copyWith(...)` or call `instanceOfWebhookEnvelope.copyWith.fieldName(value)` for a single field.
class _$WebhookEnvelopeCWProxyImpl implements _$WebhookEnvelopeCWProxy {
  const _$WebhookEnvelopeCWProxyImpl(this._value);

  final WebhookEnvelope _value;

  @override
  WebhookEnvelope event(WebhookEnvelopeEventEnum event) => call(event: event);

  @override
  WebhookEnvelope subtype(String subtype) => call(subtype: subtype);

  @override
  WebhookEnvelope message(NotificationWebhookMessage message) =>
      call(message: message);

  @override
  WebhookEnvelope data(Map<String, Object>? data) => call(data: data);

  @override
  WebhookEnvelope foresignalOrigin(String foresignalOrigin) =>
      call(foresignalOrigin: foresignalOrigin);

  @override
  WebhookEnvelope exercise(bool? exercise) => call(exercise: exercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WebhookEnvelope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WebhookEnvelope(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WebhookEnvelope call({
    Object? event = const $CopyWithPlaceholder(),
    Object? subtype = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? data = const $CopyWithPlaceholder(),
    Object? foresignalOrigin = const $CopyWithPlaceholder(),
    Object? exercise = const $CopyWithPlaceholder(),
  }) {
    return WebhookEnvelope(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as WebhookEnvelopeEventEnum,
      subtype: subtype == const $CopyWithPlaceholder() || subtype == null
          ? _value.subtype
          // ignore: cast_nullable_to_non_nullable
          : subtype as String,
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as NotificationWebhookMessage,
      data: data == const $CopyWithPlaceholder()
          ? _value.data
          // ignore: cast_nullable_to_non_nullable
          : data as Map<String, Object>?,
      foresignalOrigin:
          foresignalOrigin == const $CopyWithPlaceholder() ||
              foresignalOrigin == null
          ? _value.foresignalOrigin
          // ignore: cast_nullable_to_non_nullable
          : foresignalOrigin as String,
      exercise: exercise == const $CopyWithPlaceholder()
          ? _value.exercise
          // ignore: cast_nullable_to_non_nullable
          : exercise as bool?,
    );
  }
}

extension $WebhookEnvelopeCopyWith on WebhookEnvelope {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWebhookEnvelope.copyWith(...)` or `instanceOfWebhookEnvelope.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WebhookEnvelopeCWProxy get copyWith => _$WebhookEnvelopeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WebhookEnvelope _$WebhookEnvelopeFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('WebhookEnvelope', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['event', 'subtype', 'message', 'foresignalOrigin'],
  );
  final val = WebhookEnvelope(
    event: $checkedConvert(
      'event',
      (v) => $enumDecode(_$WebhookEnvelopeEventEnumEnumMap, v),
    ),
    subtype: $checkedConvert('subtype', (v) => v as String),
    message: $checkedConvert(
      'message',
      (v) => NotificationWebhookMessage.fromJson(v as Map<String, dynamic>),
    ),
    data: $checkedConvert(
      'data',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    foresignalOrigin: $checkedConvert('foresignalOrigin', (v) => v as String),
    exercise: $checkedConvert('exercise', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$WebhookEnvelopeToJson(WebhookEnvelope instance) =>
    <String, dynamic>{
      'event': _$WebhookEnvelopeEventEnumEnumMap[instance.event]!,
      'subtype': instance.subtype,
      'message': instance.message.toJson(),
      'data': ?instance.data,
      'foresignalOrigin': instance.foresignalOrigin,
      'exercise': ?instance.exercise,
    };

const _$WebhookEnvelopeEventEnumEnumMap = {
  WebhookEnvelopeEventEnum.alert: 'alert',
  WebhookEnvelopeEventEnum.briefing: 'briefing',
  WebhookEnvelopeEventEnum.system: 'system',
  WebhookEnvelopeEventEnum.chroniclePeriodProposed: 'chronicle.proposed',
  WebhookEnvelopeEventEnum.chroniclePeriodCapWarning: 'chronicle.cap_warning',
  WebhookEnvelopeEventEnum.chroniclePeriodGap: 'chronicle.gap',
  WebhookEnvelopeEventEnum.socialPeriodDivergence: 'social.divergence',
  WebhookEnvelopeEventEnum.socialPeriodAttentionGap: 'social.attention_gap',
  WebhookEnvelopeEventEnum.analystPeriodNote: 'analyst.note',
  WebhookEnvelopeEventEnum.situationPeriodActivated: 'situation.activated',
  WebhookEnvelopeEventEnum.situationPeriodEscalated: 'situation.escalated',
  WebhookEnvelopeEventEnum.findingPeriodSignificant: 'finding.significant',
  WebhookEnvelopeEventEnum.forecastPeriodResolved: 'forecast.resolved',
  WebhookEnvelopeEventEnum.forecastPeriodResolutionNeeded:
      'forecast.resolution_needed',
  WebhookEnvelopeEventEnum.hypothesisPeriodBandChanged:
      'hypothesis.band_changed',
  WebhookEnvelopeEventEnum.indicatorPeriodObserved: 'indicator.observed',
  WebhookEnvelopeEventEnum.assumptionPeriodBroken: 'assumption.broken',
  WebhookEnvelopeEventEnum.assessmentPeriodPublished: 'assessment.published',
  WebhookEnvelopeEventEnum.mention: 'mention',
  WebhookEnvelopeEventEnum.share: 'share',
  WebhookEnvelopeEventEnum.reportPeriodReceived: 'report.received',
  WebhookEnvelopeEventEnum.packagePeriodReceived: 'package.received',
  WebhookEnvelopeEventEnum.chroniclePeriodAccessPeriodRequested:
      'chronicle.access.requested',
  WebhookEnvelopeEventEnum.savedSearchPeriodMatches: 'saved-search.matches',
  WebhookEnvelopeEventEnum.test: 'test',
  WebhookEnvelopeEventEnum.personal: 'personal',
};
